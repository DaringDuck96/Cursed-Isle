package com.cursedtides.block;

import com.cursedtides.quest.Contracts;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** A port's notice board, papered with contracts. */
public class NoticeBoardBlock extends Block {
    public NoticeBoardBlock(BlockBehaviour.Properties settings) {
        super(settings);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!world.isClientSide && player instanceof ServerPlayer sp) Contracts.board(sp, pos);
        return InteractionResult.sidedSuccess(world.isClientSide);
    }
}
