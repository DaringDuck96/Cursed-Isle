package com.cursedtides.entity;

import com.cursedtides.item.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

/**
 * A cursed skeleton deckhand. Fights up close with a cutlass (holding a non-bow item
 * makes skeleton AI switch to melee), burns in daylight like any undead.
 */
public class CursedPirateEntity extends SkeletonEntity {

    public CursedPirateEntity(EntityType<? extends SkeletonEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createPirateAttributes() {
        return AbstractSkeletonEntity.createAbstractSkeletonAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 26.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.28)
                .add(EntityAttributes.GENERIC_ARMOR, 2.0);
    }

    @Override
    protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
        this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(ModItems.CUTLASS));
        this.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.05f);
    }

    /**
     * Natural spawns: dark, solid ground, not in water. Done here instead of a
     * spawn-restriction registration so it works without access wideners.
     */
    @Override
    public boolean canSpawn(WorldAccess world, SpawnReason reason) {
        if (reason != SpawnReason.NATURAL && reason != SpawnReason.CHUNK_GENERATION) {
            return super.canSpawn(world, reason);
        }
        if (world.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        BlockPos pos = this.getBlockPos();
        BlockPos below = pos.down();
        if (!world.getFluidState(pos).isEmpty()) {
            return false;
        }
        if (!world.getBlockState(below).isSolidBlock(world, below)) {
            return false;
        }
        return world instanceof ServerWorldAccess serverWorld
                && HostileEntity.isSpawnDark(serverWorld, pos, this.random);
    }
}
