package com.cursedtides.combat;

import com.cursedtides.appearance.Appearance;
import com.cursedtides.item.DualBladesItem;
import com.cursedtides.item.HeavyWeaponItem;
import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
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
        if (reapplying || !(source.getEntity() instanceof ServerPlayer player) || source.getDirectEntity() != player) return true;
        if (source.is(DamageTypeTags.IS_PROJECTILE) || source.is(DamageTypeTags.IS_EXPLOSION) || victim == player) return true;
        if (victim.invulnerableTime > 10) return true; // still flashing from the last blow: let vanilla ignore it
        Item held = player.getMainHandItem().getItem();
        long now = player.getServer() == null ? 0 : player.getServer().getTickCount();
        int window = held instanceof DualBladesItem ? 50 : 30;
        Combo prev = COMBOS.get(player.getUUID());
        int count = prev != null && now - prev.last() <= window ? prev.count() + 1 : 1;
        boolean finisher = count >= 4;
        COMBOS.put(player.getUUID(), new Combo(finisher ? 0 : count, now));

        float mult = 1f + (count - 1) * 0.1f;
        String label = null;
        if (finisher) { mult = 1.6f; label = "Finishing blow!"; }

        boolean heavy = player.isShiftKeyDown();
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
        if (finisher && victim.getHealth() < victim.getMaxHealth() * 0.2f && !victim.getTags().contains("cursedtides_boss")
                && !(victim instanceof ServerPlayer)) {
            amount = victim.getHealth() + 10;
            label = "Executed!";
        } else {
            amount *= mult;
        }

        reapplying = true;
        try {
            victim.hurt(source, amount);
        } finally {
            reapplying = false;
        }

        ServerLevel world = (ServerLevel) player.level();
        if (crit) {
            world.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY(0.6), victim.getZ(), 14, 0.4, 0.4, 0.4, 0.3);
            world.playSound(null, victim.blockPosition(), com.cursedtides.sound.ModSounds.CRITICAL.value(), SoundSource.PLAYERS, 1f, 0.9f + world.random.nextFloat() * 0.2f);
        }
        if (finisher || heavy || heavyWeapon) {
            stagger(victim, finisher ? 40 : heavyWeapon ? 30 : 20);
            Vec3 push = victim.position().subtract(player.position()).multiply(1, 0, 1);
            if (push.lengthSqr() > 1e-4 && finisher) {
                push = push.normalize();
                victim.knockback(1.2, -push.x, -push.z);
            }
        }
        if (finisher) {
            world.sendParticles(ParticleTypes.SWEEP_ATTACK, victim.getX(), victim.getY(0.5), victim.getZ(), 4, 0.5, 0.3, 0.5, 0);
            world.playSound(null, victim.blockPosition(), com.cursedtides.sound.ModSounds.FINISHER.value(), SoundSource.PLAYERS, 1f, 1f);
            PirateData.addXp(player, Weapon.CUTLASS, 2);
        }
        if (heavy) player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 1, false, false));
        if (label != null) player.displayClientMessage(Component.literal(label).withStyle(crit ? ChatFormatting.YELLOW : ChatFormatting.GOLD), true);
        else if (count >= 2) player.displayClientMessage(Component.literal("Combo x" + count).withStyle(ChatFormatting.GRAY), true);
        return false;
    }

    /** Knock a foe off balance: slowed, weakened, and (for mobs) brought to a halt. */
    public static void stagger(LivingEntity target, int ticks) {
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, 3, false, false));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, ticks, 1, false, false));
        target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, ticks, 2, false, false));
        if (target instanceof Mob mob) {
            mob.getNavigation().stop();
            if (mob.getTarget() != null) mob.getLookControl().setLookAt(mob.getTarget());
        }
        if (target.level() instanceof ServerLevel w) {
            w.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getEyeY() + 0.3, target.getZ(), 6, 0.25, 0.1, 0.25, 0.02);
        }
    }
}
