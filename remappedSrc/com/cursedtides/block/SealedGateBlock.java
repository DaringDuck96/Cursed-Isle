package com.cursedtides.block;

import com.cursedtides.dungeon.Dungeons;
import com.cursedtides.item.ModItems;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * An iron-bound vault door that can't be broken. A Rusted Key opens it: every connected
 * gate block crumbles away and the key is used up.
 */
public class SealedGateBlock extends Block {
    public SealedGateBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos,
                                             Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(ModItems.RUSTED_KEY)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (world instanceof ServerLevel serverWorld) {
            Set<BlockPos> gate = connected(world, pos);
            for (BlockPos p : gate) {
                world.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                serverWorld.sendParticles(ParticleTypes.POOF, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 3, 0.2, 0.2, 0.2, 0.01);
            }
            world.playSound(null, pos, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.0f, 0.5f);
            world.playSound(null, pos, SoundEvents.CHAIN_BREAK, SoundSource.BLOCKS, 1.0f, 0.7f);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            if (player instanceof ServerPlayer serverPlayer) {
                Dungeons.onVaultOpened(serverPlayer, pos);
            }
        }
        return ItemInteractionResult.sidedSuccess(world.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!world.isClientSide) {
            player.displayClientMessage(Component.literal("Sealed fast. The lock wants a key.").withStyle(ChatFormatting.GRAY), true);
        }
        return InteractionResult.sidedSuccess(world.isClientSide);
    }

    private static Set<BlockPos> connected(Level world, BlockPos start) {
        Set<BlockPos> found = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        while (!queue.isEmpty() && found.size() < 64) {
            BlockPos p = queue.poll();
            if (!found.add(p)) continue;
            for (Direction d : Direction.values()) {
                BlockPos n = p.relative(d);
                if (!found.contains(n) && world.getBlockState(n).getBlock() instanceof SealedGateBlock) {
                    queue.add(n);
                }
            }
        }
        return found;
    }
}
