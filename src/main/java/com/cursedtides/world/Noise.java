package com.cursedtides.world;

import net.minecraft.util.math.MathHelper;

/**
 * Fixed-seed hash noise. The archipelago does not use the world seed, so every
 * Cursed Archipelago world has exactly the same islands.
 */
public final class Noise {
    private Noise() {}

    public static long hash(long x, long y, long z, long seed) {
        long h = seed * 0x9E3779B97F4A7C15L + x * 0xC2B2AE3D27D4EB4FL + y * 0x27D4EB2F165667C5L + z * 0x165667B19E3779F9L;
        h ^= h >>> 33;
        h *= 0xFF51AFD7ED558CCDL;
        h ^= h >>> 33;
        h *= 0xC4CEB9FE1A85EC53L;
        h ^= h >>> 33;
        return h;
    }

    /** Uniform 0..1 for an integer coordinate. */
    public static double rand(long x, long y, long z, long seed) {
        return (hash(x, y, z, seed) >>> 11) * 0x1.0p-53;
    }

    public static double rand(long x, long z, long seed) {
        return rand(x, 0, z, seed);
    }

    /** Smooth value noise, 0..1. */
    public static double value(double x, double z, long seed) {
        int x0 = MathHelper.floor(x);
        int z0 = MathHelper.floor(z);
        double fx = x - x0;
        double fz = z - z0;
        fx = fx * fx * (3 - 2 * fx);
        fz = fz * fz * (3 - 2 * fz);
        double a = rand(x0, z0, seed);
        double b = rand(x0 + 1, z0, seed);
        double c = rand(x0, z0 + 1, seed);
        double d = rand(x0 + 1, z0 + 1, seed);
        return MathHelper.lerp(fz, MathHelper.lerp(fx, a, b), MathHelper.lerp(fx, c, d));
    }
}
