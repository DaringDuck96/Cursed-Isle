package com.cursedtides.block;

import com.cursedtides.CursedTides;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class ModBlocks {
    private ModBlocks() {}

    /** Unbreakable vault door; opened only with a Rusted Key. */
    public static final Block SEALED_GATE = Registry.register(BuiltInRegistries.BLOCK, CursedTides.id("sealed_gate"),
            new SealedGateBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(-1.0f, 3600000.0f)
                    .noLootTable()
                    .sound(SoundType.CHAIN)
                    .pushReaction(PushReaction.BLOCK)));

    public static final Block GLYPH_STONE = Registry.register(BuiltInRegistries.BLOCK, CursedTides.id("glyph_stone"),
            new GlyphBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(-1.0f, 3600000.0f)
                    .noLootTable().sound(SoundType.STONE).pushReaction(PushReaction.BLOCK), false));

    public static final Block GLYPH_ALTAR = Registry.register(BuiltInRegistries.BLOCK, CursedTides.id("glyph_altar"),
            new GlyphBlock(BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(-1.0f, 3600000.0f)
                    .noLootTable().sound(SoundType.STONE).lightLevel(s -> 7).pushReaction(PushReaction.BLOCK), true));

    /** Stone seal that crumbles when the glyph puzzle beside it is solved. */
    public static final Block RUNE_SEAL = Registry.register(BuiltInRegistries.BLOCK, CursedTides.id("rune_seal"),
            new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(-1.0f, 3600000.0f)
                    .noLootTable().sound(SoundType.DEEPSLATE_BRICKS).lightLevel(s -> 4).pushReaction(PushReaction.BLOCK)));

    public static final Block SONG_ALTAR = Registry.register(BuiltInRegistries.BLOCK, CursedTides.id("song_altar"),
            new PuzzleBlocks.SongAltarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(-1.0f, 3600000.0f)
                    .noLootTable().sound(SoundType.AMETHYST).lightLevel(s -> 6).pushReaction(PushReaction.BLOCK)));

    public static final Block CHIME_STONE = Registry.register(BuiltInRegistries.BLOCK, CursedTides.id("chime_stone"),
            new PuzzleBlocks.ChimeStoneBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(-1.0f, 3600000.0f)
                    .noLootTable().sound(SoundType.AMETHYST).pushReaction(PushReaction.BLOCK)));

    public static final Block MOON_SEAL = Registry.register(BuiltInRegistries.BLOCK, CursedTides.id("moon_seal"),
            new PuzzleBlocks.MoonSealBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(-1.0f, 3600000.0f)
                    .noLootTable().noOcclusion().randomTicks().sound(SoundType.DEEPSLATE_TILES).lightLevel(s -> s.getValue(PuzzleBlocks.MoonSealBlock.OPEN) ? 8 : 2)
                    .pushReaction(PushReaction.BLOCK)));

    public static final Block CAVE_ROPE = Registry.register(BuiltInRegistries.BLOCK, CursedTides.id("cave_rope"),
            new CaveRopeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(-1.0f, 3600000.0f)
                    .noLootTable().noCollission().noOcclusion().sound(SoundType.WOOL).pushReaction(PushReaction.BLOCK)));

    public static final Block WARP_TOTEM = Registry.register(BuiltInRegistries.BLOCK, CursedTides.id("warp_totem"),
            new WarpTotemBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(-1.0f, 3600000.0f)
                    .noLootTable().lightLevel(s -> 10).sound(SoundType.LODESTONE).pushReaction(PushReaction.BLOCK)));

    public static final Block NOTICE_BOARD = Registry.register(BuiltInRegistries.BLOCK, CursedTides.id("notice_board"),
            new NoticeBoardBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(-1.0f, 3600000.0f)
                    .noLootTable().sound(SoundType.WOOD).pushReaction(PushReaction.BLOCK)));

    public static void register() {
        PuzzleBlocks.register();
        CursedTides.LOGGER.debug("Registered Cursed Tides blocks");
    }
}
