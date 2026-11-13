package com.cursedtides.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Runs once per chunk in the Cursed Archipelago world type: shapes island terrain,
 * seeds ores, grows plants, then places this chunk's slice of every prebuilt structure.
 */
public record ArchipelagoFeature() implements Feature {
    public static final MapCodec<ArchipelagoFeature> CODEC = MapCodec.unit(ArchipelagoFeature::new);

    @Override
    public MapCodec<ArchipelagoFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        Canvas canvas = new Canvas(level, ChunkPos.containing(origin));
        Terrain.shapeLand(canvas);
        Terrain.ores(canvas);
        Terrain.vegetation(canvas);
        Builds.placeAll(canvas);
        return true;
    }
}
