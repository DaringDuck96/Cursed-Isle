package com.cursedtides.world;

import com.mojang.serialization.Codec;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

/**
 * Runs once per chunk in the Cursed Archipelago world type: shapes island terrain,
 * seeds ores, grows plants, then places this chunk's slice of every prebuilt structure.
 */
public class ArchipelagoFeature extends Feature<DefaultFeatureConfig> {
    public ArchipelagoFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        Canvas canvas = new Canvas(context.getWorld(), new ChunkPos(context.getOrigin()));
        Terrain.shapeLand(canvas);
        Terrain.ores(canvas);
        Terrain.vegetation(canvas);
        Builds.placeAll(canvas);
        return true;
    }
}
