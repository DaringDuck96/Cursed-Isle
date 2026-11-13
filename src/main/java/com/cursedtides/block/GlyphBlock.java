package com.cursedtides.block;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Carved glyph stones. The turning kind rotate through sun, wave, skull and star when struck; the altar kind
 * never moves and shows the answer. When every turning stone near an altar matches it, the rune seals nearby
 * crumble.
 */
public class GlyphBlock extends Block {
    public static final IntegerProperty GLYPH = IntegerProperty.create("glyph", 0, 3);
    public static final String[] NAMES = {"sun", "wave", "skull", "star"};
    private final boolean altar;

    public GlyphBlock(Properties settings, boolean altar) {
        super(settings);
        this.altar = altar;
        registerDefaultState(getStateDefinition().any().setValue(GLYPH, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(GLYPH);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (altar) {
            if (!world.isClientSide()) com.cursedtides.util.Compat.tell(player, Component.literal("The altar bears the sign of the " + NAMES[state.getValue(GLYPH)] + ".")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
            return InteractionResult.SUCCESS;
        }
        if (world.isClientSide()) return InteractionResult.SUCCESS;
        BlockState turned = state.setValue(GLYPH, (state.getValue(GLYPH) + 1) % 4);
        world.setBlockAndUpdate(pos, turned);
        world.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.6f, 1.2f + 0.1f * turned.getValue(GLYPH));
        check((ServerLevel) world, pos, player);
        return InteractionResult.SUCCESS;
    }

    private static void check(ServerLevel world, BlockPos around, Player player) {
        BlockPos altarPos = null;
        List<BlockPos> stones = new ArrayList<>();
        List<BlockPos> seals = new ArrayList<>();
        for (BlockPos p : BlockPos.betweenClosed(around.offset(-9, -3, -9), around.offset(9, 4, 9))) {
            BlockState s = world.getBlockState(p);
            if (s.is(ModBlocks.GLYPH_ALTAR)) altarPos = p.immutable();
            else if (s.is(ModBlocks.GLYPH_STONE)) stones.add(p.immutable());
            else if (s.is(ModBlocks.RUNE_SEAL)) seals.add(p.immutable());
        }
        if (altarPos == null || seals.isEmpty()) return;
        int want = world.getBlockState(altarPos).getValue(GLYPH);
        for (BlockPos p : stones) if (world.getBlockState(p).getValue(GLYPH) != want) return;
        for (BlockPos p : seals) {
            world.destroyBlock(p, false);
            world.sendParticles(ParticleTypes.ENCHANT, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 12, 0.3, 0.3, 0.3, 0.5);
        }
        world.playSound(null, altarPos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1f, 1.5f);
        com.cursedtides.util.Compat.tell(player, Component.literal("The glyphs align. Somewhere close, stone grinds aside.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
        if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
            com.cursedtides.pirate.PirateData.addXp(sp, com.cursedtides.pirate.Weapon.EXPLORATION, 30);
        }
    }
}
