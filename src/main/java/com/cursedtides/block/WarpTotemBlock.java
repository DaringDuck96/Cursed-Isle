package com.cursedtides.block;

import com.cursedtides.living.Totems;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** A port's warp stone. Touch it once to attune; after that, it can carry you to any stone you're attuned to. */
public class WarpTotemBlock extends Block {
    public WarpTotemBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient && player instanceof ServerPlayerEntity sp) Totems.touch(sp, pos);
        return ActionResult.success(world.isClient);
    }
}
