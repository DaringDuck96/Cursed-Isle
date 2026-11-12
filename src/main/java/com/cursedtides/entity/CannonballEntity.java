package com.cursedtides.entity;

import com.cursedtides.item.ModItems;
import com.cursedtides.ship.ShipAmmo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

/**
 * Heavy shot from a ship's gun or a Ship's Cannon. Round shot and shells burst on impact
 * (hurting entities, not blocks: change ExplosionSourceType.NONE to TNT to allow that).
 * Chain shot tears sails; grapeshot is many small, crew-killing balls.
 */
public class CannonballEntity extends ThrownItemEntity {
    private ShipAmmo ammo = ShipAmmo.ROUND;

    public CannonballEntity(EntityType<? extends CannonballEntity> type, World world) {
        super(type, world);
    }

    public CannonballEntity(World world, LivingEntity owner) {
        super(ModEntities.CANNONBALL, owner, world);
    }

    /** Fired from a ship's gun port; the caller sets the owner (gunner or ship). */
    public CannonballEntity(World world, double x, double y, double z) {
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
        if (this.getWorld().isClient) {
            this.getWorld().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
        } else if (this.age > 200) {
            this.discard();
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult hit) {
        super.onEntityHit(hit);
        Entity target = hit.getEntity();
        float damage = switch (ammo) {
            case ROUND -> 10.0f;
            case CHAIN -> target instanceof ShipEntity ? 4.0f : 8.0f;
            case GRAPE -> target instanceof ShipEntity ? 2.0f : 7.0f;
            case EXPLOSIVE -> 12.0f;
        };
        target.damage(this.getDamageSources().thrown(this, this.getOwner()), damage);
        if (ammo == ShipAmmo.CHAIN && target instanceof ShipEntity ship) {
            ship.tearRigging(400);
        }
    }

    @Override
    protected void onCollision(HitResult hit) {
        super.onCollision(hit);
        if (this.getWorld().isClient) {
            return;
        }
        float power = switch (ammo) {
            case ROUND -> 1.8f;
            case EXPLOSIVE -> 3.0f;
            default -> 0.0f;
        };
        if (this.getWorld() instanceof net.minecraft.server.world.ServerWorld sw) {
            com.cursedtides.world.Forts.onImpact(sw, hit.getPos(), this.getOwner(), this);
            com.cursedtides.quest.Lessons.onCannonImpact(this.getOwner(), hit.getPos());
        }
        if (power > 0) {
            this.getWorld().createExplosion(this, this.getX(), this.getY(), this.getZ(), power, World.ExplosionSourceType.NONE);
        }
        this.discard();
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Ammo", ammo.ordinal());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        ammo = ShipAmmo.byIndex(nbt.getInt("Ammo"));
    }
}
