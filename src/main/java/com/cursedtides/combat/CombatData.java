package com.cursedtides.combat;

import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
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

    private static long now(ServerPlayer player) {
        return player.level().getServer() == null ? 0 : player.level().getServer().getTickCount();
    }

    public static boolean startParry(ServerPlayer player, int ticks) {
        long now = now(player);
        UUID id = player.getUUID();
        if (parryReady.getOrDefault(id, 0L) > now) {
            return false;
        }
        ticks += (int) com.cursedtides.pirate.PerkData.stat(player, com.cursedtides.pirate.Perk.Stat.PARRY_TICKS);
        parryUntil.put(id, now + ticks);
        parryReady.put(id, now + ticks + 20);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARMOR_EQUIP_CHAIN.value(), SoundSource.PLAYERS, 0.8f, 1.4f);
        return true;
    }

    public static void dodge(ServerPlayer player, int direction) {
        long now = now(player);
        UUID id = player.getUUID();
        if (dodgeReady.getOrDefault(id, 0L) > now || !player.onGround() || player.isPassenger()) {
            return;
        }
        Vec3 forward = Vec3.directionFromRotation(0.0f, player.getYRot());
        Vec3 right = new Vec3(-forward.z, 0.0, forward.x);
        Vec3 dir = switch (direction) {
            case DODGE_LEFT -> right.scale(-1.0);
            case DODGE_RIGHT -> right;
            default -> forward.scale(-1.0);
        };
        player.setDeltaMovement(dir.x * 0.95, 0.25, dir.z * 0.95);
        player.syncVelocity = true;
        player.causeFoodExhaustion(1.5f);
        dodgeUntil.put(id, now + 8);
        dodgeReady.put(id, now + Math.max(10, 30 - (long) com.cursedtides.pirate.PerkData.stat(player,
                com.cursedtides.pirate.Perk.Stat.DODGE_CD)));
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_WEAK, SoundSource.PLAYERS, 0.7f, 1.6f);
    }

    private static boolean allowDamage(LivingEntity entity, DamageSource source, float amount) {
        if (!(entity instanceof ServerPlayer player) || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return true;
        }
        long now = now(player);
        UUID id = player.getUUID();

        if (dodgeUntil.getOrDefault(id, 0L) > now && source.getEntity() != null) {
            if (player.level() instanceof ServerLevel world) {
                world.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.5, player.getZ(), 6, 0.3, 0.2, 0.3, 0.02);
            }
            return false;
        }

        Entity attacker = source.getEntity();
        boolean melee = attacker instanceof LivingEntity && source.getDirectEntity() == attacker
                && !source.is(DamageTypeTags.IS_PROJECTILE) && !source.is(DamageTypeTags.IS_EXPLOSION);
        if (melee && parryUntil.getOrDefault(id, 0L) > now) {
            parryUntil.remove(id);
            LivingEntity foe = (LivingEntity) attacker;
            Vec3 push = foe.position().subtract(player.position()).multiply(1, 0, 1).normalize();
            com.cursedtides.util.Compat.knock(foe, 0.8, -push.x, -push.z);
            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 2), player);
            foe.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0), player);
            player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 40, 0), player);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    com.cursedtides.sound.ModSounds.SWORD_CLASH.value(), SoundSource.PLAYERS, 1.0f, 0.9f + player.getRandom().nextFloat() * 0.3f);
            if (player.level() instanceof ServerLevel world) {
                world.sendParticles(ParticleTypes.CRIT, player.getX(), player.getEyeY() - 0.4, player.getZ(), 10, 0.3, 0.3, 0.3, 0.2);
            }
            player.sendSystemMessage(Component.literal("Parried!").withStyle(ChatFormatting.GOLD), true);
            PirateData.addXp(player, Weapon.CUTLASS, 2);
            return false;
        }
        return true;
    }
}
