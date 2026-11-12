package com.cursedtides.entity;

import com.cursedtides.item.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

/** A straight-flying curse. Slows its target; at Hex mastery 5+ it also withers. */
public class HexBoltEntity extends net.minecraft.entity.projectile.thrown.ThrownItemEntity {
    private static final int MAX_AGE = 60;
    private int hexLevel;
    private float bonusDamage;

    public void setBonusDamage(float bonus) {
        this.bonusDamage = bonus;
    }

    public HexBoltEntity(EntityType<? extends HexBoltEntity> type, World world) {
        super(type, world);
        this.setNoGravity(true);
    }

    public HexBoltEntity(World world, LivingEntity owner, int hexLevel) {
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
        World world = this.getWorld();
        if (world.isClient) {
            world.addParticle(ParticleTypes.WITCH, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
            world.addParticle(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
        } else if (this.age > MAX_AGE) {
            this.discard();
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult hit) {
        super.onEntityHit(hit);
        Entity target = hit.getEntity();
        target.damage(this.getDamageSources().indirectMagic(this, this.getOwner()), 6.0f + hexLevel * 0.3f + bonusDamage);
        if (target instanceof LivingEntity living) {
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60 + hexLevel * 4, 1), this.getOwner());
            if (hexLevel >= 5) {
                living.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 60, hexLevel >= 15 ? 1 : 0), this.getOwner());
            }
        }
    }

    @Override
    protected void onCollision(HitResult hit) {
        super.onCollision(hit);
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.WITCH, this.getX(), this.getY(), this.getZ(), 12, 0.3, 0.3, 0.3, 0.05);
            this.discard();
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("HexLevel", hexLevel);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        hexLevel = nbt.getInt("HexLevel");
    }
}
