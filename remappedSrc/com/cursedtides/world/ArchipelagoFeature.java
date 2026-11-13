package com.cursedtides.world;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Runs once per chunk in the Cursed Archipelago world type: shapes island terrain,
 * seeds ores, grows plants, then places this chunk's slice of every prebuilt structure.
 */
public class ArchipelagoFeature extends Feature<NoneFeatureConfiguration> {
    public ArchipelagoFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        Canvas canvas = new Canvas(context.level(), new ChunkPos(context.origin()));
        Terrain.shapeLand(canvas);
        Terrain.ores(canvas);
        Terrain.vegetation(canvas);
        Builds.placeAll(canvas);
        return true;
    }
}
