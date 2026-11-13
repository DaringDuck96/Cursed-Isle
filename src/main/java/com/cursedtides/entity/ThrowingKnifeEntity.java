package com.cursedtides.entity;

import com.cursedtides.item.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class ThrowingKnifeEntity extends ThrowableItemProjectile {
    public ThrowingKnifeEntity(EntityType<? extends ThrowingKnifeEntity> type, Level world) {
        super(type, world);
    }

    public ThrowingKnifeEntity(Level world, LivingEntity owner) {
        super(ModEntities.THROWING_KNIFE, owner, world, net.minecraft.world.item.ItemStack.EMPTY);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.THROWING_KNIFE;
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        hit.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 6.0f);
        if (!this.level().isClientSide()) {
            if (this.random.nextBoolean()) dropKnife();
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        if (!this.level().isClientSide()) {
            this.level().playSound(null, getX(), getY(), getZ(), SoundEvents.TRIDENT_HIT_GROUND, SoundSource.PLAYERS, 0.5f, 1.6f);
            dropKnife();
            this.discard();
        }
    }

    private void dropKnife() {
        if (!(this.getOwner() instanceof Player p && p.getAbilities().instabuild)) {
            com.cursedtides.util.Compat.drop(this, new ItemStack(ModItems.THROWING_KNIFE));
        }
    }
}
