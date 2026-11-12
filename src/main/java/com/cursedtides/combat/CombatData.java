package com.cursedtides.combat;

import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
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
 * Parrying and dodging. Both open a short window, tracked here by server tick:
 * a parry turns aside the next melee blow and staggers the attacker; a dodge roll
 * slips everything (melee and missiles) for a split second.
 */
public final class CombatData {
    private CombatData() {}

    public static final int DODGE_LEFT = 0, DODGE_RIGHT = 1, DODGE_BACK = 2;

    private static final Map<UUID, Long> parryUntil = new HashMap<>();
    private static final Map<UUID, Long> parryReady = new HashMap<>();
    private static final Map<UUID, Long> dodgeUntil = new HashMap<>();
    private static final Map<UUID, Long> dodgeReady = new HashMap<>();

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(CombatData::allowDamage);
    }

    private static long now(ServerPlayerEntity player) {
        return player.getServer() == null ? 0 : player.getServer().getTicks();
    }

    public static boolean startParry(ServerPlayerEntity player, int ticks) {
        long now = now(player);
        UUID id = player.getUuid();
        if (parryReady.getOrDefault(id, 0L) > now) {
            return false;
        }
        ticks += (int) com.cursedtides.pirate.PerkData.stat(player, com.cursedtides.pirate.Perk.Stat.PARRY_TICKS);
        parryUntil.put(id, now + ticks);
        parryReady.put(id, now + ticks + 20);
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ITEM_ARMOR_EQUIP_CHAIN.value(), SoundCategory.PLAYERS, 0.8f, 1.4f);
        return true;
    }

    public static void dodge(ServerPlayerEntity player, int direction) {
        long now = now(player);
        UUID id = player.getUuid();
        if (dodgeReady.getOrDefault(id, 0L) > now || !player.isOnGround() || player.hasVehicle()) {
            return;
        }
        Vec3d forward = Vec3d.fromPolar(0.0f, player.getYaw());
        Vec3d right = new Vec3d(-forward.z, 0.0, forward.x);
        Vec3d dir = switch (direction) {
            case DODGE_LEFT -> right.multiply(-1.0);
            case DODGE_RIGHT -> right;
            default -> forward.multiply(-1.0);
        };
        player.setVelocity(dir.x * 0.95, 0.25, dir.z * 0.95);
        player.velocityModified = true;
        player.addExhaustion(1.5f);
        dodgeUntil.put(id, now + 8);
        dodgeReady.put(id, now + Math.max(10, 30 - (long) com.cursedtides.pirate.PerkData.stat(player,
                com.cursedtides.pirate.Perk.Stat.DODGE_CD)));
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, SoundCategory.PLAYERS, 0.7f, 1.6f);
    }

    private static boolean allowDamage(LivingEntity entity, DamageSource source, float amount) {
        if (!(entity instanceof ServerPlayerEntity player) || source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return true;
        }
        long now = now(player);
        UUID id = player.getUuid();

        if (dodgeUntil.getOrDefault(id, 0L) > now && source.getAttacker() != null) {
            if (player.getWorld() instanceof ServerWorld world) {
                world.spawnParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.5, player.getZ(), 6, 0.3, 0.2, 0.3, 0.02);
            }
            return false;
        }

        Entity attacker = source.getAttacker();
        boolean melee = attacker instanceof LivingEntity && source.getSource() == attacker
                && !source.isIn(DamageTypeTags.IS_PROJECTILE) && !source.isIn(DamageTypeTags.IS_EXPLOSION);
        if (melee && parryUntil.getOrDefault(id, 0L) > now) {
            parryUntil.remove(id);
            LivingEntity foe = (LivingEntity) attacker;
            Vec3d push = foe.getPos().subtract(player.getPos()).multiply(1, 0, 1).normalize();
            foe.takeKnockback(0.8, -push.x, -push.z);
            foe.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 2), player);
            foe.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 0), player);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 40, 0), player);
            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    com.cursedtides.sound.ModSounds.SWORD_CLASH.value(), SoundCategory.PLAYERS, 1.0f, 0.9f + player.getRandom().nextFloat() * 0.3f);
            if (player.getWorld() instanceof ServerWorld world) {
                world.spawnParticles(ParticleTypes.CRIT, player.getX(), player.getEyeY() - 0.4, player.getZ(), 10, 0.3, 0.3, 0.3, 0.2);
            }
            player.sendMessage(Text.literal("Parried!").formatted(Formatting.GOLD), true);
            PirateData.addXp(player, Weapon.CUTLASS, 2);
            return false;
        }
        return true;
    }
}
