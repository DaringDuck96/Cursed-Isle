package com.cursedtides.block;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Two more kinds of dungeon puzzle.
 * Chime stones: the song altar hums four notes; strike the chime stones in that order to break the rune seals.
 * Moon seals: these only slide open at night, when the tide in the stone runs out.
 */
public final class PuzzleBlocks {
    private PuzzleBlocks() {}

    private static final float[] PITCH = {0.7f, 0.9f, 1.15f, 1.45f};

    /** The tune for the altar at this spot: four notes, always the same for that altar. */
    static int[] tune(BlockPos altar) {
        long h = altar.asLong() * 0x9E3779B97F4A7C15L;
        int[] t = new int[4];
        for (int i = 0; i < 4; i++) { t[i] = (int) ((h >>> (i * 7)) & 3); }
        if (t[0] == t[1] && t[1] == t[2] && t[2] == t[3]) t[3] = (t[3] + 1) % 4;
        return t;
    }

    // ------------------------------------------------------------------ the scheduler for hummed tunes

    private record Pending(long at, ServerWorld world, BlockPos pos, float pitch) {}
    private static final List<Pending> PENDING = new ArrayList<>();
    private static final Map<UUID, List<Integer>> STRUCK = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(PuzzleBlocks::tick);
    }

    private static void tick(MinecraftServer server) {
        if (!PENDING.isEmpty()) {
            for (Pending p : List.copyOf(PENDING)) {
                if (p.world().getTime() < p.at()) continue;
                PENDING.remove(p);
                p.world().playSound(null, p.pos(), SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.BLOCKS, 1.5f, p.pitch());
                p.world().spawnParticles(ParticleTypes.NOTE, p.pos().getX() + 0.5, p.pos().getY() + 1.3, p.pos().getZ() + 0.5, 1, 0, 0, 0, 0);
            }
        }
        // Moon seals near players in dungeons follow the time of day
        if (server.getTicks() % 100 != 0) return;
        ServerWorld world = server.getOverworld();
        long t = world.getTimeOfDay() % 24000;
        boolean night = t >= 13000 && t < 23000;
        for (ServerPlayerEntity p : world.getPlayers()) {
            if (p.getY() > 40) continue; // dungeons are deep
            BlockPos c = p.getBlockPos();
            for (BlockPos q : BlockPos.iterate(c.add(-12, -4, -12), c.add(12, 5, 12))) {
                BlockState s = world.getBlockState(q);
                if (s.isOf(ModBlocks.MOON_SEAL) && s.get(MoonSealBlock.OPEN) != night) {
                    world.setBlockState(q, s.with(MoonSealBlock.OPEN, night));
                    if (night) world.spawnParticles(ParticleTypes.END_ROD, q.getX() + 0.5, q.getY() + 0.5, q.getZ() + 0.5, 3, 0.2, 0.3, 0.2, 0.01);
                }
            }
        }
    }

    // ------------------------------------------------------------------ blocks

    public static class SongAltarBlock extends Block {
        public SongAltarBlock(AbstractBlock.Settings settings) { super(settings); }

        @Override
        protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
            if (!(world instanceof ServerWorld sw)) return ActionResult.SUCCESS;
            int[] t = tune(pos);
            for (int i = 0; i < t.length; i++) PENDING.add(new Pending(sw.getTime() + 8 + i * 14L, sw, pos, PITCH[t[i]]));
            STRUCK.remove(player.getUuid());
            player.sendMessage(Text.literal("The altar hums four notes. Play them back on the chime stones.").formatted(Formatting.GRAY, Formatting.ITALIC), true);
            return ActionResult.SUCCESS;
        }
    }

    public static class ChimeStoneBlock extends Block {
        public static final IntProperty NOTE = IntProperty.of("note", 0, 3);

        public ChimeStoneBlock(AbstractBlock.Settings settings) {
            super(settings);
            setDefaultState(getStateManager().getDefaultState().with(NOTE, 0));
        }

        @Override
        protected void appendProperties(StateManager.Builder<Block, BlockState> builder) { builder.add(NOTE); }

        @Override
        protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
            if (!(world instanceof ServerWorld sw)) return ActionResult.SUCCESS;
            int note = state.get(NOTE);
            sw.playSound(null, pos, SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.BLOCKS, 1.2f, PITCH[note]);
            sw.spawnParticles(ParticleTypes.NOTE, pos.getX() + 0.5, pos.getY() + 1.3, pos.getZ() + 0.5, 1, 0, 0, 0, 0);
            BlockPos altar = null;
            List<BlockPos> seals = new ArrayList<>();
            for (BlockPos q : BlockPos.iterate(pos.add(-9, -3, -9), pos.add(9, 4, 9))) {
                BlockState s = sw.getBlockState(q);
                if (s.isOf(ModBlocks.SONG_ALTAR)) altar = q.toImmutable();
                else if (s.isOf(ModBlocks.RUNE_SEAL)) seals.add(q.toImmutable());
            }
            if (altar == null || seals.isEmpty()) return ActionResult.SUCCESS;
            int[] t = tune(altar);
            List<Integer> played = STRUCK.computeIfAbsent(player.getUuid(), k -> new ArrayList<>());
            played.add(note);
            if (played.get(played.size() - 1) != t[played.size() - 1]) {
                played.clear();
                sw.playSound(null, pos, SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.BLOCKS, 1f, 0.5f);
                player.sendMessage(Text.literal("A sour note. The stones fall silent.").formatted(Formatting.GRAY), true);
                return ActionResult.SUCCESS;
            }
            if (played.size() == t.length) {
                played.clear();
                for (BlockPos q : seals) {
                    sw.breakBlock(q, false);
                    sw.spawnParticles(ParticleTypes.ENCHANT, q.getX() + 0.5, q.getY() + 0.5, q.getZ() + 0.5, 12, 0.3, 0.3, 0.3, 0.5);
                }
                sw.playSound(null, altar, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 1f, 1.5f);
                player.sendMessage(Text.literal("The song is answered. The seals crumble.").formatted(Formatting.LIGHT_PURPLE), false);
                if (player instanceof ServerPlayerEntity sp) com.cursedtides.pirate.PirateData.addXp(sp, com.cursedtides.pirate.Weapon.EXPLORATION, 30);
            }
            return ActionResult.SUCCESS;
        }
    }

    public static class MoonSealBlock extends Block {
        public static final BooleanProperty OPEN = BooleanProperty.of("open");

        public MoonSealBlock(AbstractBlock.Settings settings) {
            super(settings);
            setDefaultState(getStateManager().getDefaultState().with(OPEN, false));
        }

        @Override
        protected void appendProperties(StateManager.Builder<Block, BlockState> builder) { builder.add(OPEN); }

        @Override
        protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
            return state.get(OPEN) ? VoxelShapes.empty() : VoxelShapes.fullCube();
        }

        @Override
        protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
            return state.get(OPEN) ? Block.createCuboidShape(0, 0, 0, 16, 1, 16) : VoxelShapes.fullCube();
        }

        @Override
        protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
            if (!world.isClient && !state.get(OPEN)) {
                player.sendMessage(Text.literal("Cold stone, carved with a crescent moon. It won't budge by daylight.").formatted(Formatting.GRAY, Formatting.ITALIC), true);
            }
            return ActionResult.SUCCESS;
        }

        @Override
        protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
            long t = world.getTimeOfDay() % 24000;
            boolean night = t >= 13000 && t < 23000;
            if (state.get(OPEN) != night) world.setBlockState(pos, state.with(OPEN, night));
        }

        @Override
        protected boolean hasRandomTicks(BlockState state) {
            return true;
        }
    }
}
