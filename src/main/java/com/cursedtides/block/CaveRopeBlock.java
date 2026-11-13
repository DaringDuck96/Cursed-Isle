package com.cursedtides.block;

import com.cursedtides.dungeon.Caverns;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** An old knotted rope down into a grand cavern (or back up out of one). Use it to climb. */
public class CaveRopeBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(6, 0, 6, 10, 16, 10);

    public CaveRopeBlock(BlockBehaviour.Properties settings) {
        super(settings);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!world.isClientSide() && player instanceof ServerPlayer sp) Caverns.climb(sp, pos);
        return InteractionResult.SUCCESS;
    }
}
