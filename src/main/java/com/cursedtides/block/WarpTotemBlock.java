package com.cursedtides.block;

import com.cursedtides.living.Totems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** A port's warp stone. Touch it once to attune; after that, it can carry you to any stone you're attuned to. */
public class WarpTotemBlock extends Block {
    public WarpTotemBlock(BlockBehaviour.Properties settings) {
        super(settings);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!world.isClientSide() && player instanceof ServerPlayer sp) Totems.touch(sp, pos);
        return InteractionResult.SUCCESS;
    }
}
