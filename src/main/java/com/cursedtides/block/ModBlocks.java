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

    public static void register() {
        CursedTides.LOGGER.debug("Registered Cursed Tides blocks");
    }
}
