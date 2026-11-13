package com.cursedtides.ship;

import com.cursedtides.world.Noise;

/**
 * One wind for the whole world, worked out from world time so client and server
 * always agree without syncing. It veers slowly and gusts over a few minutes.
 */
public final class Wind {
    private Wind() {}

    /** The yaw the wind blows TOWARD (Minecraft yaw: 0 = south, 90 = west). */
    public static float yaw(long worldTime) {
        return (float) (Noise.value(worldTime / 4800.0, 0.5, 31337) * 720.0);
    }

    /** 0.55 (light air) to 1.0 (strong breeze). */
    public static float strength(long worldTime) {
        return (float) (0.55 + 0.45 * Noise.value(worldTime / 2400.0, 7.5, 4242));
    }

    /** Sail efficiency for a heading: 1.0 running before the wind, 0.3 heading straight into it. */
    public static double efficiency(float shipYaw, long worldTime) {
        double align = Math.cos(Math.toRadians(shipYaw - yaw(worldTime)));
        return 0.3 + 0.7 * (align + 1.0) / 2.0;
    }

    public static String pointOfSail(float shipYaw, long worldTime) {
        double align = Math.cos(Math.toRadians(shipYaw - yaw(worldTime)));
        if (align > 0.5) return "running";
        if (align > -0.3) return "reaching";
        return "close-hauled";
    }

    /** Compass name of the direction the wind comes FROM. */
    public static String fromName(long worldTime) {
        return compass(yaw(worldTime) + 180.0f);
    }

    public static String strengthName(long worldTime) {
        float s = strength(worldTime);
        if (s > 0.88f) return "strong";
        if (s > 0.7f) return "fair";
        return "light";
    }

    public static String compass(float yaw) {
        String[] names = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
        int index = (int) Math.round((((yaw % 360) + 360) % 360) / 45.0) % 8;
        return names[index];
    }
}
