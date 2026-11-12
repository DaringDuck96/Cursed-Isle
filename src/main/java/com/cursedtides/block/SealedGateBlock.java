package com.cursedtides.block;

import com.cursedtides.dungeon.Dungeons;
import com.cursedtides.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/**
 * An iron-bound vault door that can't be broken. A Rusted Key opens it: every connected
 * gate block crumbles away and the key is used up.
 */
public class SealedGateBlock extends Block {
    public SealedGateBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos,
                                             PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!stack.isOf(ModItems.RUSTED_KEY)) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (world instanceof ServerWorld serverWorld) {
            Set<BlockPos> gate = connected(world, pos);
            for (BlockPos p : gate) {
                world.setBlockState(p, Blocks.AIR.getDefaultState(), 3);
                serverWorld.spawnParticles(ParticleTypes.POOF, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 3, 0.2, 0.2, 0.2, 0.01);
            }
            world.playSound(null, pos, SoundEvents.BLOCK_IRON_DOOR_OPEN, SoundCategory.BLOCKS, 1.0f, 0.5f);
            world.playSound(null, pos, SoundEvents.BLOCK_CHAIN_BREAK, SoundCategory.BLOCKS, 1.0f, 0.7f);
            if (!player.getAbilities().creativeMode) {
                stack.decrement(1);
            }
            if (player instanceof ServerPlayerEntity serverPlayer) {
                Dungeons.onVaultOpened(serverPlayer, pos);
            }
        }
        return ItemActionResult.success(world.isClient);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            player.sendMessage(Text.literal("Sealed fast. The lock wants a key.").formatted(Formatting.GRAY), true);
        }
        return ActionResult.success(world.isClient);
    }

    private static Set<BlockPos> connected(World world, BlockPos start) {
        Set<BlockPos> found = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        while (!queue.isEmpty() && found.size() < 64) {
            BlockPos p = queue.poll();
            if (!found.add(p)) continue;
            for (Direction d : Direction.values()) {
                BlockPos n = p.offset(d);
                if (!found.contains(n) && world.getBlockState(n).getBlock() instanceof SealedGateBlock) {
                    queue.add(n);
                }
            }
        }
        return found;
    }
}
