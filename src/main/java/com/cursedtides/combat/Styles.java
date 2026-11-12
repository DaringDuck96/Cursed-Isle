package com.cursedtides.combat;

import com.cursedtides.appearance.Appearance;
import com.cursedtides.item.DualBladesItem;
import com.cursedtides.item.HeavyWeaponItem;
import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The rhythm of a sword fight. Melee blows chain into combos (each hit a little harder), the fourth is a
 * finisher that hurls and staggers; any blow can land a critical; a crouched blow is a heavy attack.
 * Staggered foes are slowed and can't swing for a moment.
 */
public final class Styles {
    private Styles() {}

    private record Combo(int count, long last) {}
    private static final Map<UUID, Combo> COMBOS = new HashMap<>();
    private static boolean reapplying;

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(Styles::allowDamage);
    }

    private static boolean allowDamage(LivingEntity victim, DamageSource source, float amount) {
        if (reapplying || !(source.getAttacker() instanceof ServerPlayerEntity player) || source.getSource() != player) return true;
        if (source.isIn(DamageTypeTags.IS_PROJECTILE) || source.isIn(DamageTypeTags.IS_EXPLOSION) || victim == player) return true;
        if (victim.timeUntilRegen > 10) return true; // still flashing from the last blow: let vanilla ignore it
        Item held = player.getMainHandStack().getItem();
        long now = player.getServer() == null ? 0 : player.getServer().getTicks();
        int window = held instanceof DualBladesItem ? 50 : 30;
        Combo prev = COMBOS.get(player.getUuid());
        int count = prev != null && now - prev.last() <= window ? prev.count() + 1 : 1;
        boolean finisher = count >= 4;
        COMBOS.put(player.getUuid(), new Combo(finisher ? 0 : count, now));

        float mult = 1f + (count - 1) * 0.1f;
        String label = null;
        if (finisher) { mult = 1.6f; label = "Finishing blow!"; }

        boolean heavy = player.isSneaking();
        if (heavy) mult *= 1.35f;
        boolean heavyWeapon = held instanceof HeavyWeaponItem;

        int swords = PirateData.level(player, Weapon.CUTLASS);
        double critChance = 0.05 + swords * 0.004 + Appearance.special(player, "crit");
        boolean crit = player.getRandom().nextDouble() < critChance;
        if (crit) {
            mult *= heavyWeapon ? 1.8f : 1.5f;
            label = finisher ? "Critical finisher!" : "Critical!";
        }
        mult = Math.min(mult, 3.0f); // stacking everything at once still has a ceiling
        if (mult <= 1.001f && !heavyWeapon && !heavy) return true;

        // Execution: a finisher on a foe under a fifth of its health ends it outright (not bosses)
        if (finisher && victim.getHealth() < victim.getMaxHealth() * 0.2f && !victim.getCommandTags().contains("cursedtides_boss")
                && !(victim instanceof ServerPlayerEntity)) {
            amount = victim.getHealth() + 10;
            label = "Executed!";
        } else {
            amount *= mult;
        }

        reapplying = true;
        try {
            victim.damage(source, amount);
        } finally {
            reapplying = false;
        }

        ServerWorld world = (ServerWorld) player.getWorld();
        if (crit) {
            world.spawnParticles(ParticleTypes.CRIT, victim.getX(), victim.getBodyY(0.6), victim.getZ(), 14, 0.4, 0.4, 0.4, 0.3);
            world.playSound(null, victim.getBlockPos(), com.cursedtides.sound.ModSounds.CRITICAL.value(), SoundCategory.PLAYERS, 1f, 0.9f + world.random.nextFloat() * 0.2f);
        }
        if (finisher || heavy || heavyWeapon) {
            stagger(victim, finisher ? 40 : heavyWeapon ? 30 : 20);
            Vec3d push = victim.getPos().subtract(player.getPos()).multiply(1, 0, 1);
            if (push.lengthSquared() > 1e-4 && finisher) {
                push = push.normalize();
                victim.takeKnockback(1.2, -push.x, -push.z);
            }
        }
        if (finisher) {
            world.spawnParticles(ParticleTypes.SWEEP_ATTACK, victim.getX(), victim.getBodyY(0.5), victim.getZ(), 4, 0.5, 0.3, 0.5, 0);
            world.playSound(null, victim.getBlockPos(), com.cursedtides.sound.ModSounds.FINISHER.value(), SoundCategory.PLAYERS, 1f, 1f);
            PirateData.addXp(player, Weapon.CUTLASS, 2);
        }
        if (heavy) player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 10, 1, false, false));
        if (label != null) player.sendMessage(Text.literal(label).formatted(crit ? Formatting.YELLOW : Formatting.GOLD), true);
        else if (count >= 2) player.sendMessage(Text.literal("Combo x" + count).formatted(Formatting.GRAY), true);
        return false;
    }

    /** Knock a foe off balance: slowed, weakened, and (for mobs) brought to a halt. */
    public static void stagger(LivingEntity target, int ticks) {
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, ticks, 3, false, false));
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, ticks, 1, false, false));
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, ticks, 2, false, false));
        if (target instanceof MobEntity mob) {
            mob.getNavigation().stop();
            if (mob.getTarget() != null) mob.getLookControl().lookAt(mob.getTarget());
        }
        if (target.getWorld() instanceof ServerWorld w) {
            w.spawnParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getEyeY() + 0.3, target.getZ(), 6, 0.25, 0.1, 0.25, 0.02);
        }
    }
}
