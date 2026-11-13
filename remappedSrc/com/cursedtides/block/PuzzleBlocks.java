package com.cursedtides.block;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
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

    private record Pending(long at, ServerLevel world, BlockPos pos, float pitch) {}
    private static final List<Pending> PENDING = new ArrayList<>();
    private static final Map<UUID, List<Integer>> STRUCK = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(PuzzleBlocks::tick);
    }

    private static void tick(MinecraftServer server) {
        if (!PENDING.isEmpty()) {
            for (Pending p : List.copyOf(PENDING)) {
                if (p.world().getGameTime() < p.at()) continue;
                PENDING.remove(p);
                p.world().playSound(null, p.pos(), SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 1.5f, p.pitch());
                p.world().sendParticles(ParticleTypes.NOTE, p.pos().getX() + 0.5, p.pos().getY() + 1.3, p.pos().getZ() + 0.5, 1, 0, 0, 0, 0);
            }
        }
        // Moon seals near players in dungeons follow the time of day
        if (server.getTickCount() % 100 != 0) return;
        ServerLevel world = server.overworld();
        long t = world.getDayTime() % 24000;
        boolean night = t >= 13000 && t < 23000;
        for (ServerPlayer p : world.players()) {
            if (p.getY() > 40) continue; // dungeons are deep
            BlockPos c = p.blockPosition();
            for (BlockPos q : BlockPos.betweenClosed(c.offset(-12, -4, -12), c.offset(12, 5, 12))) {
                BlockState s = world.getBlockState(q);
                if (s.is(ModBlocks.MOON_SEAL) && s.getValue(MoonSealBlock.OPEN) != night) {
                    world.setBlockAndUpdate(q, s.setValue(MoonSealBlock.OPEN, night));
                    if (night) world.sendParticles(ParticleTypes.END_ROD, q.getX() + 0.5, q.getY() + 0.5, q.getZ() + 0.5, 3, 0.2, 0.3, 0.2, 0.01);
                }
            }
        }
    }

    // ------------------------------------------------------------------ blocks

    public static class SongAltarBlock extends Block {
        public SongAltarBlock(BlockBehaviour.Properties settings) { super(settings); }

        @Override
        protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
            if (!(world instanceof ServerLevel sw)) return InteractionResult.SUCCESS;
            int[] t = tune(pos);
            for (int i = 0; i < t.length; i++) PENDING.add(new Pending(sw.getGameTime() + 8 + i * 14L, sw, pos, PITCH[t[i]]));
            STRUCK.remove(player.getUUID());
            player.displayClientMessage(Component.literal("The altar hums four notes. Play them back on the chime stones.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
            return InteractionResult.SUCCESS;
        }
    }

    public static class ChimeStoneBlock extends Block {
        public static final IntegerProperty NOTE = IntegerProperty.create("note", 0, 3);

        public ChimeStoneBlock(BlockBehaviour.Properties settings) {
            super(settings);
            registerDefaultState(getStateDefinition().any().setValue(NOTE, 0));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(NOTE); }

        @Override
        protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
            if (!(world instanceof ServerLevel sw)) return InteractionResult.SUCCESS;
            int note = state.getValue(NOTE);
            sw.playSound(null, pos, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 1.2f, PITCH[note]);
            sw.sendParticles(ParticleTypes.NOTE, pos.getX() + 0.5, pos.getY() + 1.3, pos.getZ() + 0.5, 1, 0, 0, 0, 0);
            BlockPos altar = null;
            List<BlockPos> seals = new ArrayList<>();
            for (BlockPos q : BlockPos.betweenClosed(pos.offset(-9, -3, -9), pos.offset(9, 4, 9))) {
                BlockState s = sw.getBlockState(q);
                if (s.is(ModBlocks.SONG_ALTAR)) altar = q.immutable();
                else if (s.is(ModBlocks.RUNE_SEAL)) seals.add(q.immutable());
            }
            if (altar == null || seals.isEmpty()) return InteractionResult.SUCCESS;
            int[] t = tune(altar);
            List<Integer> played = STRUCK.computeIfAbsent(player.getUUID(), k -> new ArrayList<>());
            played.add(note);
            if (played.get(played.size() - 1) != t[played.size() - 1]) {
                played.clear();
                sw.playSound(null, pos, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 1f, 0.5f);
                player.displayClientMessage(Component.literal("A sour note. The stones fall silent.").withStyle(ChatFormatting.GRAY), true);
                return InteractionResult.SUCCESS;
            }
            if (played.size() == t.length) {
                played.clear();
                for (BlockPos q : seals) {
                    sw.destroyBlock(q, false);
                    sw.sendParticles(ParticleTypes.ENCHANT, q.getX() + 0.5, q.getY() + 0.5, q.getZ() + 0.5, 12, 0.3, 0.3, 0.3, 0.5);
                }
                sw.playSound(null, altar, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1f, 1.5f);
                player.displayClientMessage(Component.literal("The song is answered. The seals crumble.").withStyle(ChatFormatting.LIGHT_PURPLE), false);
                if (player instanceof ServerPlayer sp) com.cursedtides.pirate.PirateData.addXp(sp, com.cursedtides.pirate.Weapon.EXPLORATION, 30);
            }
            return InteractionResult.SUCCESS;
        }
    }

    public static class MoonSealBlock extends Block {
        public static final BooleanProperty OPEN = BooleanProperty.create("open");

        public MoonSealBlock(BlockBehaviour.Properties settings) {
            super(settings);
            registerDefaultState(getStateDefinition().any().setValue(OPEN, false));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(OPEN); }

        @Override
        protected VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
            return state.getValue(OPEN) ? Shapes.empty() : Shapes.block();
        }

        @Override
        protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
            return state.getValue(OPEN) ? Block.box(0, 0, 0, 16, 1, 16) : Shapes.block();
        }

        @Override
        protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
            if (!world.isClientSide && !state.getValue(OPEN)) {
                player.displayClientMessage(Component.literal("Cold stone, carved with a crescent moon. It won't budge by daylight.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
            }
            return InteractionResult.SUCCESS;
        }

        @Override
        protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
            long t = world.getDayTime() % 24000;
            boolean night = t >= 13000 && t < 23000;
            if (state.getValue(OPEN) != night) world.setBlockAndUpdate(pos, state.setValue(OPEN, night));
        }

        @Override
        protected boolean isRandomlyTicking(BlockState state) {
            return true;
        }
    }
}
