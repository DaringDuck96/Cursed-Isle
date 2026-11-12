package com.cursedtides.block;

import com.cursedtides.CursedTides;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;

public final class ModBlocks {
    private ModBlocks() {}

    /** Unbreakable vault door; opened only with a Rusted Key. */
    public static final Block SEALED_GATE = Registry.register(Registries.BLOCK, CursedTides.id("sealed_gate"),
            new SealedGateBlock(AbstractBlock.Settings.create()
                    .mapColor(MapColor.IRON_GRAY)
                    .strength(-1.0f, 3600000.0f)
                    .dropsNothing()
                    .sounds(BlockSoundGroup.CHAIN)
                    .pistonBehavior(PistonBehavior.BLOCK)));

    public static final Block GLYPH_STONE = Registry.register(Registries.BLOCK, CursedTides.id("glyph_stone"),
            new GlyphBlock(AbstractBlock.Settings.create().mapColor(MapColor.STONE_GRAY).strength(-1.0f, 3600000.0f)
                    .dropsNothing().sounds(BlockSoundGroup.STONE).pistonBehavior(PistonBehavior.BLOCK), false));

    public static final Block GLYPH_ALTAR = Registry.register(Registries.BLOCK, CursedTides.id("glyph_altar"),
            new GlyphBlock(AbstractBlock.Settings.create().mapColor(MapColor.GOLD).strength(-1.0f, 3600000.0f)
                    .dropsNothing().sounds(BlockSoundGroup.STONE).luminance(s -> 7).pistonBehavior(PistonBehavior.BLOCK), true));

    /** Stone seal that crumbles when the glyph puzzle beside it is solved. */
    public static final Block RUNE_SEAL = Registry.register(Registries.BLOCK, CursedTides.id("rune_seal"),
            new Block(AbstractBlock.Settings.create().mapColor(MapColor.PURPLE).strength(-1.0f, 3600000.0f)
                    .dropsNothing().sounds(BlockSoundGroup.DEEPSLATE_BRICKS).luminance(s -> 4).pistonBehavior(PistonBehavior.BLOCK)));

    public static final Block SONG_ALTAR = Registry.register(Registries.BLOCK, CursedTides.id("song_altar"),
            new PuzzleBlocks.SongAltarBlock(AbstractBlock.Settings.create().mapColor(MapColor.CYAN).strength(-1.0f, 3600000.0f)
                    .dropsNothing().sounds(BlockSoundGroup.AMETHYST_BLOCK).luminance(s -> 6).pistonBehavior(PistonBehavior.BLOCK)));

    public static final Block CHIME_STONE = Registry.register(Registries.BLOCK, CursedTides.id("chime_stone"),
            new PuzzleBlocks.ChimeStoneBlock(AbstractBlock.Settings.create().mapColor(MapColor.LIGHT_BLUE).strength(-1.0f, 3600000.0f)
                    .dropsNothing().sounds(BlockSoundGroup.AMETHYST_BLOCK).pistonBehavior(PistonBehavior.BLOCK)));

    public static final Block MOON_SEAL = Registry.register(Registries.BLOCK, CursedTides.id("moon_seal"),
            new PuzzleBlocks.MoonSealBlock(AbstractBlock.Settings.create().mapColor(MapColor.WHITE).strength(-1.0f, 3600000.0f)
                    .dropsNothing().nonOpaque().ticksRandomly().sounds(BlockSoundGroup.DEEPSLATE_TILES).luminance(s -> s.get(PuzzleBlocks.MoonSealBlock.OPEN) ? 8 : 2)
                    .pistonBehavior(PistonBehavior.BLOCK)));

    public static final Block CAVE_ROPE = Registry.register(Registries.BLOCK, CursedTides.id("cave_rope"),
            new CaveRopeBlock(AbstractBlock.Settings.create().mapColor(MapColor.BROWN).strength(-1.0f, 3600000.0f)
                    .dropsNothing().noCollision().nonOpaque().sounds(BlockSoundGroup.WOOL).pistonBehavior(PistonBehavior.BLOCK)));

    public static final Block WARP_TOTEM = Registry.register(Registries.BLOCK, CursedTides.id("warp_totem"),
            new WarpTotemBlock(AbstractBlock.Settings.create().mapColor(MapColor.STONE_GRAY).strength(-1.0f, 3600000.0f)
                    .dropsNothing().luminance(s -> 10).sounds(BlockSoundGroup.LODESTONE).pistonBehavior(PistonBehavior.BLOCK)));

    public static final Block NOTICE_BOARD = Registry.register(Registries.BLOCK, CursedTides.id("notice_board"),
            new NoticeBoardBlock(AbstractBlock.Settings.create().mapColor(MapColor.OAK_TAN).strength(-1.0f, 3600000.0f)
                    .dropsNothing().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.BLOCK)));

    public static void register() {
        PuzzleBlocks.register();
        CursedTides.LOGGER.debug("Registered Cursed Tides blocks");
    }
}
