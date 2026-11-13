package com.cursedtides.entity;

import com.cursedtides.item.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** A straight-flying curse. Slows its target; at Hex mastery 5+ it also withers. */
public class HexBoltEntity extends net.minecraft.world.entity.projectile.ThrowableItemProjectile {
    private static final int MAX_AGE = 60;
    private int hexLevel;
    private float bonusDamage;

    public void setBonusDamage(float bonus) {
        this.bonusDamage = bonus;
    }

    public HexBoltEntity(EntityType<? extends HexBoltEntity> type, Level world) {
        super(type, world);
        this.setNoGravity(true);
    }

    public HexBoltEntity(Level world, LivingEntity owner, int hexLevel) {
        super(ModEntities.HEX_BOLT, owner, world);
        this.hexLevel = hexLevel;
        this.setNoGravity(true);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.HEX_ORB;
    }

    @Override
    public void tick() {
        super.tick();
        Level world = this.level();
        if (world.isClientSide) {
            world.addParticle(ParticleTypes.WITCH, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
            world.addParticle(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
        } else if (this.tickCount > MAX_AGE) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        Entity target = hit.getEntity();
        target.hurt(this.damageSources().indirectMagic(this, this.getOwner()), 6.0f + hexLevel * 0.3f + bonusDamage);
        if (target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60 + hexLevel * 4, 1), this.getOwner());
            if (hexLevel >= 5) {
                living.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, hexLevel >= 15 ? 1 : 0), this.getOwner());
            }
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (this.level() instanceof ServerLevel serverWorld) {
            serverWorld.sendParticles(ParticleTypes.WITCH, this.getX(), this.getY(), this.getZ(), 12, 0.3, 0.3, 0.3, 0.05);
            this.discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("HexLevel", hexLevel);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        hexLevel = nbt.getInt("HexLevel");
    }
}
