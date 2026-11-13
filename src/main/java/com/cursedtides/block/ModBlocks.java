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
    public static final Block SEALED_GATE = Registry.register(BuiltInRegistries.BLOCK, com.cursedtides.util.Reg.blockId("sealed_gate"),
            new SealedGateBlock(BlockBehaviour.Properties.of().setId(com.cursedtides.util.Reg.blockKey())
                    .mapColor(MapColor.METAL)
                    .strength(-1.0f, 3600000.0f)
                    .noLootTable()
                    .sound(SoundType.CHAIN)
                    .pushReaction(PushReaction.IMMOVEABLE)));

    public static final Block GLYPH_STONE = Registry.register(BuiltInRegistries.BLOCK, com.cursedtides.util.Reg.blockId("glyph_stone"),
            new GlyphBlock(BlockBehaviour.Properties.of().setId(com.cursedtides.util.Reg.blockKey()).mapColor(MapColor.STONE).strength(-1.0f, 3600000.0f)
                    .noLootTable().sound(SoundType.STONE).pushReaction(PushReaction.IMMOVEABLE), false));

    public static final Block GLYPH_ALTAR = Registry.register(BuiltInRegistries.BLOCK, com.cursedtides.util.Reg.blockId("glyph_altar"),
            new GlyphBlock(BlockBehaviour.Properties.of().setId(com.cursedtides.util.Reg.blockKey()).mapColor(MapColor.GOLD).strength(-1.0f, 3600000.0f)
                    .noLootTable().sound(SoundType.STONE).lightLevel(s -> 7).pushReaction(PushReaction.IMMOVEABLE), true));

    /** Stone seal that crumbles when the glyph puzzle beside it is solved. */
    public static final Block RUNE_SEAL = Registry.register(BuiltInRegistries.BLOCK, com.cursedtides.util.Reg.blockId("rune_seal"),
            new Block(BlockBehaviour.Properties.of().setId(com.cursedtides.util.Reg.blockKey()).mapColor(MapColor.COLOR_PURPLE).strength(-1.0f, 3600000.0f)
                    .noLootTable().sound(SoundType.DEEPSLATE_BRICKS).lightLevel(s -> 4).pushReaction(PushReaction.IMMOVEABLE)));

    public static final Block SONG_ALTAR = Registry.register(BuiltInRegistries.BLOCK, com.cursedtides.util.Reg.blockId("song_altar"),
            new PuzzleBlocks.SongAltarBlock(BlockBehaviour.Properties.of().setId(com.cursedtides.util.Reg.blockKey()).mapColor(MapColor.COLOR_CYAN).strength(-1.0f, 3600000.0f)
                    .noLootTable().sound(SoundType.AMETHYST).lightLevel(s -> 6).pushReaction(PushReaction.IMMOVEABLE)));

    public static final Block CHIME_STONE = Registry.register(BuiltInRegistries.BLOCK, com.cursedtides.util.Reg.blockId("chime_stone"),
            new PuzzleBlocks.ChimeStoneBlock(BlockBehaviour.Properties.of().setId(com.cursedtides.util.Reg.blockKey()).mapColor(MapColor.COLOR_LIGHT_BLUE).strength(-1.0f, 3600000.0f)
                    .noLootTable().sound(SoundType.AMETHYST).pushReaction(PushReaction.IMMOVEABLE)));

    public static final Block MOON_SEAL = Registry.register(BuiltInRegistries.BLOCK, com.cursedtides.util.Reg.blockId("moon_seal"),
            new PuzzleBlocks.MoonSealBlock(BlockBehaviour.Properties.of().setId(com.cursedtides.util.Reg.blockKey()).mapColor(MapColor.SNOW).strength(-1.0f, 3600000.0f)
                    .noLootTable().noOcclusion().randomTicks().sound(SoundType.DEEPSLATE_TILES).lightLevel(s -> s.getValue(PuzzleBlocks.MoonSealBlock.OPEN) ? 8 : 2)
                    .pushReaction(PushReaction.IMMOVEABLE)));

    public static final Block CAVE_ROPE = Registry.register(BuiltInRegistries.BLOCK, com.cursedtides.util.Reg.blockId("cave_rope"),
            new CaveRopeBlock(BlockBehaviour.Properties.of().setId(com.cursedtides.util.Reg.blockKey()).mapColor(MapColor.COLOR_BROWN).strength(-1.0f, 3600000.0f)
                    .noLootTable().noCollision().noOcclusion().sound(SoundType.WOOL).pushReaction(PushReaction.IMMOVEABLE)));

    public static final Block WARP_TOTEM = Registry.register(BuiltInRegistries.BLOCK, com.cursedtides.util.Reg.blockId("warp_totem"),
            new WarpTotemBlock(BlockBehaviour.Properties.of().setId(com.cursedtides.util.Reg.blockKey()).mapColor(MapColor.STONE).strength(-1.0f, 3600000.0f)
                    .noLootTable().lightLevel(s -> 10).sound(SoundType.LODESTONE).pushReaction(PushReaction.IMMOVEABLE)));

    public static final Block NOTICE_BOARD = Registry.register(BuiltInRegistries.BLOCK, com.cursedtides.util.Reg.blockId("notice_board"),
            new NoticeBoardBlock(BlockBehaviour.Properties.of().setId(com.cursedtides.util.Reg.blockKey()).mapColor(MapColor.WOOD).strength(-1.0f, 3600000.0f)
                    .noLootTable().sound(SoundType.WOOD).pushReaction(PushReaction.IMMOVEABLE)));

    public static void register() {
        PuzzleBlocks.register();
        CursedTides.LOGGER.debug("Registered Cursed Tides blocks");
    }
}
