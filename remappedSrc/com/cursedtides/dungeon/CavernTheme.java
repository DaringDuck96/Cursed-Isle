package com.cursedtides.dungeon;

import com.cursedtides.entity.ModEntities;
import com.cursedtides.world.Noise;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;

/** The look and inhabitants of a grand cavern. */
public enum CavernTheme {
    CURSED, VOLCANIC, JUNGLE, MINE, DROWNED, CRYSTAL;

    private static BlockState s(Block b) { return b.defaultBlockState(); }

    public BlockState wall(int x, int y, int z) {
        double r = Noise.rand(x, y, z, 901);
        return switch (this) {
            case CURSED -> s(r < 0.5 ? Blocks.DEEPSLATE : r < 0.75 ? Blocks.COBBLED_DEEPSLATE : r < 0.9 ? Blocks.TUFF : Blocks.SOUL_SOIL);
            case VOLCANIC -> s(r < 0.45 ? Blocks.BASALT : r < 0.8 ? Blocks.BLACKSTONE : r < 0.93 ? Blocks.MAGMA_BLOCK : Blocks.TUFF);
            case JUNGLE -> s(r < 0.4 ? Blocks.MOSSY_COBBLESTONE : r < 0.65 ? Blocks.STONE : r < 0.85 ? Blocks.MOSS_BLOCK : Blocks.ROOTED_DIRT);
            case MINE -> s(r < 0.55 ? Blocks.STONE : r < 0.75 ? Blocks.ANDESITE : r < 0.9 ? Blocks.GRAVEL : r < 0.96 ? Blocks.IRON_ORE : Blocks.GOLD_ORE);
            case DROWNED -> s(r < 0.4 ? Blocks.PRISMARINE : r < 0.7 ? Blocks.DARK_PRISMARINE : r < 0.9 ? Blocks.STONE : Blocks.CLAY);
            case CRYSTAL -> s(r < 0.5 ? Blocks.CALCITE : r < 0.8 ? Blocks.SMOOTH_BASALT : r < 0.95 ? Blocks.AMETHYST_BLOCK : Blocks.BUDDING_AMETHYST);
        };
    }

    public BlockState floor(int x, int y, int z) {
        double r = Noise.rand(x, y, z, 903);
        return switch (this) {
            case CURSED -> s(r < 0.6 ? Blocks.COBBLED_DEEPSLATE : r < 0.85 ? Blocks.GRAVEL : Blocks.BONE_BLOCK);
            case VOLCANIC -> s(r < 0.6 ? Blocks.BLACKSTONE : r < 0.85 ? Blocks.BASALT : Blocks.MAGMA_BLOCK);
            case JUNGLE -> s(r < 0.6 ? Blocks.MOSS_BLOCK : r < 0.85 ? Blocks.MUD : Blocks.MOSSY_COBBLESTONE);
            case MINE -> s(r < 0.6 ? Blocks.GRAVEL : r < 0.85 ? Blocks.COARSE_DIRT : Blocks.COBBLESTONE);
            case DROWNED -> s(r < 0.6 ? Blocks.SAND : r < 0.85 ? Blocks.GRAVEL : Blocks.PRISMARINE);
            case CRYSTAL -> s(r < 0.6 ? Blocks.CALCITE : r < 0.85 ? Blocks.SMOOTH_BASALT : Blocks.AMETHYST_BLOCK);
        };
    }

    public BlockState liquid() {
        return this == VOLCANIC ? s(Blocks.LAVA) : s(Blocks.WATER);
    }

    public BlockState dais() {
        return switch (this) {
            case CURSED -> s(Blocks.POLISHED_DEEPSLATE);
            case VOLCANIC -> s(Blocks.POLISHED_BLACKSTONE_BRICKS);
            case JUNGLE -> s(Blocks.MOSSY_STONE_BRICKS);
            case MINE -> s(Blocks.STONE_BRICKS);
            case DROWNED -> s(Blocks.PRISMARINE_BRICKS);
            case CRYSTAL -> s(Blocks.CHISELED_QUARTZ_BLOCK);
        };
    }

    public double spikeChance() { return this == MINE ? 0.006 : 0.02; }
    public double scatterChance() { return 0.04; }

    public BlockState spikeBlock() {
        return switch (this) {
            case VOLCANIC -> s(Blocks.BASALT);
            case CRYSTAL -> s(Blocks.AMETHYST_BLOCK);
            case CURSED -> s(Blocks.BONE_BLOCK);
            default -> s(Blocks.POINTED_DRIPSTONE);
        };
    }

    public BlockState scatter(int x, int z) {
        double r = Noise.rand(x, z, 905);
        return switch (this) {
            case CURSED -> s(r < 0.4 ? Blocks.COBWEB : r < 0.7 ? Blocks.SKELETON_SKULL : Blocks.CANDLE);
            case VOLCANIC -> s(r < 0.5 ? Blocks.FIRE : Blocks.BLACKSTONE_SLAB);
            case JUNGLE -> s(r < 0.5 ? Blocks.MOSS_CARPET : r < 0.8 ? Blocks.FERN : Blocks.BROWN_MUSHROOM);
            case MINE -> s(r < 0.5 ? Blocks.COBBLESTONE_SLAB : r < 0.8 ? Blocks.BARREL : Blocks.COBWEB);
            case DROWNED -> s(r < 0.5 ? Blocks.SEA_PICKLE : Blocks.BRAIN_CORAL_FAN);
            case CRYSTAL -> s(Blocks.AMETHYST_CLUSTER);
        };
    }

    public BlockState hangingLight() {
        return switch (this) {
            case CURSED -> s(Blocks.SOUL_LANTERN).setValue(LanternBlock.HANGING, true);
            case VOLCANIC -> s(Blocks.SHROOMLIGHT);
            case JUNGLE -> s(Blocks.SHROOMLIGHT);
            case MINE -> s(Blocks.LANTERN).setValue(LanternBlock.HANGING, true);
            case DROWNED -> s(Blocks.SEA_LANTERN);
            case CRYSTAL -> s(Blocks.GLOWSTONE);
        };
    }

    public BlockState floorLight() {
        return switch (this) {
            case CURSED -> s(Blocks.SOUL_CAMPFIRE);
            case VOLCANIC -> s(Blocks.MAGMA_BLOCK);
            case JUNGLE -> s(Blocks.OCHRE_FROGLIGHT);
            case MINE -> s(Blocks.LANTERN);
            case DROWNED -> s(Blocks.SEA_LANTERN);
            case CRYSTAL -> s(Blocks.PEARLESCENT_FROGLIGHT);
        };
    }

    public EntityType<?> mob(int i) {
        return switch (this) {
            case CURSED -> i % 3 == 0 ? EntityType.STRAY : i % 3 == 1 ? ModEntities.CURSED_PIRATE : EntityType.SKELETON;
            case VOLCANIC -> i % 3 == 0 ? EntityType.MAGMA_CUBE : i % 3 == 1 ? EntityType.WITHER_SKELETON : EntityType.HUSK;
            case JUNGLE -> i % 3 == 0 ? EntityType.CAVE_SPIDER : i % 3 == 1 ? com.cursedtides.wildlife.Wildlife.SNAKE : EntityType.SPIDER;
            case MINE -> i % 2 == 0 ? EntityType.ZOMBIE : ModEntities.CURSED_PIRATE;
            case DROWNED -> i % 2 == 0 ? EntityType.DROWNED : ModEntities.CURSED_PIRATE;
            case CRYSTAL -> i % 2 == 0 ? EntityType.STRAY : EntityType.SKELETON;
        };
    }

    /** What packs of the living (or not quite) roam the halls. */
    public EntityType<?> pack(int i) {
        return switch (this) {
            case CURSED -> i % 2 == 0 ? ModEntities.CURSED_PIRATE : EntityType.SKELETON;
            case VOLCANIC -> i % 3 == 0 ? EntityType.WITHER_SKELETON : EntityType.HUSK;
            case JUNGLE -> i % 3 == 0 ? com.cursedtides.wildlife.Wildlife.JAGUAR : i % 3 == 1 ? com.cursedtides.wildlife.Wildlife.SNAKE : EntityType.SPIDER;
            case MINE -> i % 3 == 0 ? EntityType.PILLAGER : i % 3 == 1 ? EntityType.VINDICATOR : EntityType.ZOMBIE;
            case DROWNED -> i % 2 == 0 ? EntityType.DROWNED : ModEntities.CURSED_PIRATE;
            case CRYSTAL -> i % 3 == 0 ? EntityType.SKELETON : EntityType.STRAY;
        };
    }
}
