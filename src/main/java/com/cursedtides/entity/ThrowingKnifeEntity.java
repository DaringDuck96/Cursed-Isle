package com.cursedtides.entity;

import com.cursedtides.item.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public class ThrowingKnifeEntity extends ThrownItemEntity {
    public ThrowingKnifeEntity(EntityType<? extends ThrowingKnifeEntity> type, World world) {
        super(type, world);
    }

    public ThrowingKnifeEntity(World world, LivingEntity owner) {
        super(ModEntities.THROWING_KNIFE, owner, world);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.THROWING_KNIFE;
    }

    @Override
    protected void onEntityHit(EntityHitResult hit) {
        super.onEntityHit(hit);
        hit.getEntity().damage(this.getDamageSources().thrown(this, this.getOwner()), 6.0f);
        if (!this.getWorld().isClient) {
            if (this.random.nextBoolean()) dropKnife();
            this.discard();
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult hit) {
        super.onBlockHit(hit);
        if (!this.getWorld().isClient) {
            this.getWorld().playSound(null, getX(), getY(), getZ(), SoundEvents.ITEM_TRIDENT_HIT_GROUND, SoundCategory.PLAYERS, 0.5f, 1.6f);
            dropKnife();
            this.discard();
        }
    }

    private void dropKnife() {
        if (!(this.getOwner() instanceof PlayerEntity p && p.getAbilities().creativeMode)) {
            this.dropStack(new ItemStack(ModItems.THROWING_KNIFE));
        }
    }
}
