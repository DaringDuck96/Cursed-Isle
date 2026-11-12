package com.cursedtides.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Carved glyph stones. The turning kind rotate through sun, wave, skull and star when struck; the altar kind
 * never moves and shows the answer. When every turning stone near an altar matches it, the rune seals nearby
 * crumble.
 */
public class GlyphBlock extends Block {
    public static final IntProperty GLYPH = IntProperty.of("glyph", 0, 3);
    public static final String[] NAMES = {"sun", "wave", "skull", "star"};
    private final boolean altar;

    public GlyphBlock(Settings settings, boolean altar) {
        super(settings);
        this.altar = altar;
        setDefaultState(getStateManager().getDefaultState().with(GLYPH, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(GLYPH);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (altar) {
            if (!world.isClient) player.sendMessage(Text.literal("The altar bears the sign of the " + NAMES[state.get(GLYPH)] + ".")
                    .formatted(Formatting.GRAY, Formatting.ITALIC), true);
            return ActionResult.success(world.isClient);
        }
        if (world.isClient) return ActionResult.SUCCESS;
        BlockState turned = state.with(GLYPH, (state.get(GLYPH) + 1) % 4);
        world.setBlockState(pos, turned);
        world.playSound(null, pos, SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.BLOCKS, 0.6f, 1.2f + 0.1f * turned.get(GLYPH));
        check((ServerWorld) world, pos, player);
        return ActionResult.SUCCESS;
    }

    private static void check(ServerWorld world, BlockPos around, PlayerEntity player) {
        BlockPos altarPos = null;
        List<BlockPos> stones = new ArrayList<>();
        List<BlockPos> seals = new ArrayList<>();
        for (BlockPos p : BlockPos.iterate(around.add(-9, -3, -9), around.add(9, 4, 9))) {
            BlockState s = world.getBlockState(p);
            if (s.isOf(ModBlocks.GLYPH_ALTAR)) altarPos = p.toImmutable();
            else if (s.isOf(ModBlocks.GLYPH_STONE)) stones.add(p.toImmutable());
            else if (s.isOf(ModBlocks.RUNE_SEAL)) seals.add(p.toImmutable());
        }
        if (altarPos == null || seals.isEmpty()) return;
        int want = world.getBlockState(altarPos).get(GLYPH);
        for (BlockPos p : stones) if (world.getBlockState(p).get(GLYPH) != want) return;
        for (BlockPos p : seals) {
            world.breakBlock(p, false);
            world.spawnParticles(ParticleTypes.ENCHANT, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 12, 0.3, 0.3, 0.3, 0.5);
        }
        world.playSound(null, altarPos, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 1f, 1.5f);
        player.sendMessage(Text.literal("The glyphs align. Somewhere close, stone grinds aside.").formatted(Formatting.LIGHT_PURPLE), false);
        if (player instanceof net.minecraft.server.network.ServerPlayerEntity sp) {
            com.cursedtides.pirate.PirateData.addXp(sp, com.cursedtides.pirate.Weapon.EXPLORATION, 30);
        }
    }
}
