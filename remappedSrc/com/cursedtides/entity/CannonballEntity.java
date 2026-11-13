package com.cursedtides.entity;

import com.cursedtides.item.ModItems;
import com.cursedtides.ship.ShipAmmo;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Heavy shot from a ship's gun or a Ship's Cannon. Round shot and shells burst on impact
 * (hurting entities, not blocks: change ExplosionSourceType.NONE to TNT to allow that).
 * Chain shot tears sails; grapeshot is many small, crew-killing balls.
 */
public class CannonballEntity extends ThrowableItemProjectile {
    private ShipAmmo ammo = ShipAmmo.ROUND;

    public CannonballEntity(EntityType<? extends CannonballEntity> type, Level world) {
        super(type, world);
    }

    public CannonballEntity(Level world, LivingEntity owner) {
        super(ModEntities.CANNONBALL, owner, world);
    }

    /** Fired from a ship's gun port; the caller sets the owner (gunner or ship). */
    public CannonballEntity(Level world, double x, double y, double z) {
        super(ModEntities.CANNONBALL, x, y, z, world);
    }

    public void setAmmo(ShipAmmo ammo) {
        this.ammo = ammo;
        this.setItem(new ItemStack(ammo.item()));
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.CANNONBALL;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
        } else if (this.tickCount > 200) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        Entity target = hit.getEntity();
        float damage = switch (ammo) {
            case ROUND -> 10.0f;
            case CHAIN -> target instanceof ShipEntity ? 4.0f : 8.0f;
            case GRAPE -> target instanceof ShipEntity ? 2.0f : 7.0f;
            case EXPLOSIVE -> 12.0f;
            case FIREBRAND -> 7.0f;
            case BANE -> target instanceof ShipEntity s && (s.isGhost() || s.getFaction() == com.cursedtides.ship.ShipFaction.DROWNED) ? 17.0f
                    : target instanceof LivingEntity l && l.getType().is(net.minecraft.tags.EntityTypeTags.UNDEAD) ? 14.0f : 9.0f;
        };
        target.hurt(this.damageSources().thrown(this, this.getOwner()), damage);
        if (ammo == ShipAmmo.FIREBRAND) {
            target.igniteForSeconds(6);
            if (target instanceof ShipEntity ship) ship.setBurning(this.level().getGameTime() + 120);
            for (Entity e : target.getPassengers()) e.igniteForSeconds(6);
        }
        if (ammo == ShipAmmo.CHAIN && target instanceof ShipEntity ship) {
            ship.tearRigging(400);
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (this.level().isClientSide) {
            return;
        }
        float power = switch (ammo) {
            case ROUND -> 1.8f;
            case EXPLOSIVE -> 3.0f;
            case FIREBRAND -> 0.8f;
            default -> 0.0f;
        };
        if (this.level() instanceof net.minecraft.server.level.ServerLevel sw) {
            com.cursedtides.world.Forts.onImpact(sw, hit.getLocation(), this.getOwner(), this);
            com.cursedtides.quest.Lessons.onCannonImpact(this.getOwner(), hit.getLocation());
        }
        if (power > 0) {
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), power, Level.ExplosionInteraction.NONE);
        }
        this.discard();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("Ammo", ammo.ordinal());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        ammo = ShipAmmo.byIndex(nbt.getInt("Ammo"));
    }
}
