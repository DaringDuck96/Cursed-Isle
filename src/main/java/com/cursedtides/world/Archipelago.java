package com.cursedtides.world;

import com.cursedtides.CursedTides;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.MathHelper;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.biome.Biome;

import java.util.List;

/**
 * The fixed layout of the Shattered Reach: thirty islands at set coordinates, at the old pirate games' scale.
 * The world type's flat layers put sand at y 36-38 and water from 39 to 62.
 */
public final class Archipelago {
    private Archipelago() {}

    public static final RegistryKey<Biome> BIOME = RegistryKey.of(RegistryKeys.BIOME, CursedTides.id("cursed_sea"));

    public static final int SEA_FLOOR = 38;
    public static final int WATER_TOP = 62;

    public enum Style {
        SANDY, TOWN, REEF, VOLCANIC, CURSED, FORTRESS, BAYOU, JUNGLE,
        /** Sheer cliffs around a high plateau. */
        CLIFFS,
        /** Jagged rock ringed by sea stacks. */
        CRAGS,
        /** Dry sand, sandstone and red rock. */
        DESERT,
        /** A scatter of islets over shallow water. */
        KEYS,
        /** A ring of sand around a lagoon. */
        ATOLL,
        /** Broad terraces climbing to a summit. */
        ANCIENT
    }

    /**
     * An island. {@code cx, cz} is where it sits in the world; {@code ox, oz} is where it sat on the old, small
     * chart the content was first laid out on (see {@link #relocate}). Besides its core (the old island, with its
     * town and harbour on the south shore) most islands have a hinterland: a much larger lobe of wild land
     * stretching away from the harbour, which brings them up to the size of the islands in the old pirate games.
     */
    public record Island(String name, int cx, int cz, int radius, int peak, Style style, long seed,
                         int ox, int oz, int lobeX, int lobeZ, int lobeR) {
        Island(String name, int ox, int oz, int radius, int peak, Style style, long seed) {
            this(name, placeX(name, ox, oz), placeZ(name, ox, oz), radius, peak, style, seed, ox, oz,
                    lobe(name, ox, oz, radius, style)[0], lobe(name, ox, oz, radius, style)[1], lobe(name, ox, oz, radius, style)[2]);
        }

        public boolean hasLobe() {
            return lobeR > 0;
        }

        /** Whether a point is within the island's land (core or hinterland), give or take the coast. */
        public boolean covers(double x, double z, double margin) {
            double r = radius + margin;
            if ((x - cx) * (x - cx) + (z - cz) * (z - cz) <= r * r) return true;
            if (lobeR <= 0) return false;
            double l = lobeR + margin;
            return (x - lobeX) * (x - lobeX) + (z - lobeZ) * (z - lobeZ) <= l * l;
        }

        /** Distance from a point to the island's coast (negative inside). */
        public double edgeDistance(double x, double z) {
            double d = Math.hypot(x - cx, z - cz) - radius;
            if (lobeR > 0) d = Math.min(d, Math.hypot(x - lobeX, z - lobeZ) - lobeR);
            return d;
        }

        /** Roughly how far the island reaches from its core centre. */
        public int extent() {
            return lobeR <= 0 ? radius : (int) Math.max(radius, Math.hypot(lobeX - cx, lobeZ - cz) + lobeR);
        }
    }

    // ------------------------------------------------------------------ the chart at full scale

    /**
     * The old chart was drawn small. At the scale of the old pirate games, islands sit about fourteen times
     * further apart (some 3,600 blocks between neighbours, two to three minutes under full sail), and the
     * starter isle, Driftwood, is a short row from Saltmarrow. Driftwood stays at the world's centre.
     */
    public static final double SPREAD = 14.0;
    /**
     * Where the spread chart would put Driftwood, subtracted so that it lands at 0, 0: Saltmarrow (old 230, -30)
     * times 14, less its old offset from Driftwood stretched 2.6 times (about 620 blocks, a short row).
     * Written out as plain numbers so they're set before the islands below are placed.
     */
    private static final int SHIFT_X = 2622, SHIFT_Z = -342;

    /** An open-sea point of the old chart, placed on the full-scale one. */
    public static int seaX(double ox) {
        return (int) Math.round(ox * SPREAD) - SHIFT_X;
    }

    public static int seaZ(double oz) {
        return (int) Math.round(oz * SPREAD) - SHIFT_Z;
    }

    static int placeX(String name, int ox, int oz) {
        return name.equals("Driftwood Island") ? 0 : seaX(ox);
    }

    static int placeZ(String name, int ox, int oz) {
        return name.equals("Driftwood Island") ? 0 : seaZ(oz);
    }

    /** The hinterland lobe: {x, z, radius}, or radius 0 for islands that stay as they are (reefs, atolls). */
    static int[] lobe(String name, int ox, int oz, int radius, Style style) {
        if (style == Style.REEF || style == Style.ATOLL) return new int[]{0, 0, 0};
        int cx = placeX(name, ox, oz), cz = placeZ(name, ox, oz);
        int l = (int) Math.min(200, Math.round(radius * 2.3));
        // Away from the harbour: nearly every harbour faces south, Stormbreak's faces north
        int dir = name.equals("Stormbreak Isle") ? 1 : -1;
        int off = (int) Math.round(radius * 0.55 + l * 0.8);
        return new int[]{cx, cz + dir * off, l};
    }

    /**
     * Where a point of the old, small chart is now. Anything on or near an island moves with that island (so the
     * town, its dungeons and its secrets keep their places on it); anything out at sea spreads with the chart.
     */
    public static int rx(int ox, int oz) {
        Island is = oldIslandNear(ox, oz);
        return is == null ? seaX(ox) : ox - is.ox() + is.cx();
    }

    public static int rz(int ox, int oz) {
        Island is = oldIslandNear(ox, oz);
        return is == null ? seaZ(oz) : oz - is.oz() + is.cz();
    }

    private static Island oldIslandNear(int ox, int oz) {
        Island best = null;
        double bd = Double.MAX_VALUE;
        for (Island is : ALL_ISLANDS_FOR_RELOCATION) {
            double d = Math.hypot(ox - is.ox(), oz - is.oz()) - is.radius();
            if (d < 120 && d < bd) { bd = d; best = is; }
        }
        return best;
    }

    public static final Island DRIFTWOOD_ISLAND = new Island("Driftwood Island", 0, 0, 34, 70, Style.SANDY, 11);
    public static final Island SALTMARROW = new Island("Saltmarrow", 230, -30, 62, 72, Style.TOWN, 22);
    public static final Island SUNKEN_REACH = new Island("The Sunken Reach", 70, 240, 40, 56, Style.REEF, 33);
    public static final Island ASHFALL_ISLAND = new Island("Ashfall Island", -210, 150, 64, 112, Style.VOLCANIC, 44);
    public static final Island HOLLOWGRAVE_ISLAND = new Island("Hollowgrave Island", -110, -250, 52, 86, Style.CURSED, 55);

    public static final Island IRONWATCH_ISLE = new Island("Ironwatch Isle", 430, 170, 46, 74, Style.FORTRESS, 66);

    public static final Island MIREFEN_BAYOU = new Island("Mirefen Bayou", -320, -40, 60, 64, Style.BAYOU, 77);

    public static final Island PORT_MERIDIAN = new Island("Port Meridian", 420, -200, 60, 72, Style.TOWN, 88);
    public static final Island BLACKTHORN_ISLE = new Island("Blackthorn Isle", 150, -330, 42, 80, Style.FORTRESS, 99);
    public static final Island EMERALD_CAY = new Island("Emerald Cay", -60, 420, 56, 84, Style.JUNGLE, 111);
    public static final Island WHISPERING_KEY = new Island("Whispering Key", -420, -280, 46, 78, Style.CURSED, 122);
    public static final Island GOLDWATER_CAY = new Island("Goldwater Cay", 300, 400, 46, 82, Style.FORTRESS, 133);

    // The outer reaches
    public static final Island KINGSPORT = new Island("Kingsport", 600, -20, 66, 72, Style.TOWN, 144);
    public static final Island CROOKED_REEF = new Island("Crooked Reef", 180, 560, 46, 58, Style.REEF, 155);
    public static final Island SAINT_VESPER = new Island("Saint Vesper", -560, 120, 50, 80, Style.TOWN, 166);
    public static final Island MARINERS_REST = new Island("Mariner's Rest", 40, -560, 48, 72, Style.SANDY, 177);
    public static final Island WIDOWS_ROCK = new Island("Widow's Rock", -250, -500, 38, 104, Style.CLIFFS, 188);
    public static final Island SERPENTS_HOLLOW = new Island("Serpent's Hollow", -330, 420, 54, 86, Style.JUNGLE, 199);
    public static final Island BLOODWAKE_CAY = new Island("Bloodwake Cay", -40, -720, 50, 78, Style.DESERT, 211);
    public static final Island STORMBREAK_ISLE = new Island("Stormbreak Isle", 760, 330, 40, 92, Style.CRAGS, 222);
    public static final Island RAVENS_LANDING = new Island("Raven's Landing", -620, -160, 48, 74, Style.FORTRESS, 233);
    public static final Island EMBERFALL = new Island("Emberfall", -700, 620, 72, 132, Style.VOLCANIC, 244);
    public static final Island FORGOTTEN_KEYS = new Island("The Forgotten Keys", 480, 700, 80, 70, Style.KEYS, 255);
    public static final Island BLACKVEIL_ATOLL = new Island("Blackveil Atoll", 900, -400, 62, 68, Style.ATOLL, 266);
    public static final Island CROWNWATCH = new Island("Crownwatch", 820, -60, 58, 76, Style.FORTRESS, 277);
    public static final Island LAST_HORIZON = new Island("The Last Horizon", 0, 1100, 64, 112, Style.ANCIENT, 288);
    /** Entirely under the sea: the drowned city of Vesh Amaru. */
    public static final Island DROWNED_CITY = new Island("Vesh Amaru, the Drowned City", -470, 860, 64, 52, Style.REEF, 299);
    // Hidden islands: not on any chart until revealed (or found by chance)
    public static final Island ISLE_THAT_ISNT = new Island("The Isle That Isn't", -940, -720, 42, 86, Style.CURSED, 311);
    public static final Island KINGSREST = new Island("Kingsrest", 1140, 320, 52, 104, Style.ANCIENT, 322);
    public static final Island GULLSCREAM_ROCK = new Island("Gullscream Rock", 720, -860, 32, 96, Style.CRAGS, 333);

    public static final List<Island> ISLANDS = List.of(DRIFTWOOD_ISLAND, SALTMARROW, SUNKEN_REACH, ASHFALL_ISLAND,
            HOLLOWGRAVE_ISLAND, IRONWATCH_ISLE, MIREFEN_BAYOU, PORT_MERIDIAN, BLACKTHORN_ISLE, EMERALD_CAY,
            WHISPERING_KEY, GOLDWATER_CAY, KINGSPORT, CROOKED_REEF, SAINT_VESPER, MARINERS_REST, WIDOWS_ROCK,
            SERPENTS_HOLLOW, BLOODWAKE_CAY, STORMBREAK_ISLE, RAVENS_LANDING, EMBERFALL, FORGOTTEN_KEYS,
            BLACKVEIL_ATOLL, CROWNWATCH, LAST_HORIZON, DROWNED_CITY, ISLE_THAT_ISNT, KINGSREST, GULLSCREAM_ROCK);

    private static final List<Island> ALL_ISLANDS_FOR_RELOCATION = ISLANDS;

    /** Islands left off the charts. */
    public static final List<Island> HIDDEN = List.of(ISLE_THAT_ISNT, KINGSREST, GULLSCREAM_ROCK);

    public static boolean isHidden(int index) {
        return index >= 0 && index < ISLANDS.size() && HIDDEN.contains(ISLANDS.get(index));
    }

    /** Index in ISLANDS, or -1. Used for discovery tracking. */
    public static int islandIndexAt(int x, int z) {
        for (int i = 0; i < ISLANDS.size(); i++) {
            Island is = ISLANDS.get(i);
            if (is.covers(x, z, 0)) {
                return i;
            }
        }
        return -1;
    }

    // Story locations (x, z)
    public static final int SPAWN_X = 1, SPAWN_Z = 3; // Driftwood stays at the centre
    public static final int ALTAR_X = rx(-110, -250), ALTAR_Z = rz(-110, -250);

    public record Column(int top, Island island, double dist, boolean crater, int lavaTop) {
        public boolean isLand() {
            return island != null && top > WATER_TOP;
        }
    }

    private static final Column OPEN_SEA = new Column(SEA_FLOOR, null, 9, false, 0);

    /** Terrain at a column: the highest island surface, or the open sea floor. */
    public static Column column(int x, int z) {
        Column best = null;
        for (Island is : ISLANDS) {
            Column c = shape(is, x, z, is.cx(), is.cz(), is.radius(), is.peak(), is.seed(), true);
            if (c != null && (best == null || c.top() > best.top())) best = c;
            if (is.hasLobe()) {
                c = shape(is, x, z, is.lobeX(), is.lobeZ(), is.lobeR(), Math.max(WATER_TOP + 10, is.peak() - 6), is.seed() + 101, false);
                if (c != null && (best == null || c.top() > best.top())) best = c;
            }
        }
        return best == null ? OPEN_SEA : best;
    }

    /** One round landmass of an island (its core, or its hinterland) at a column; null if this column isn't on it. */
    private static Column shape(Island is, int x, int z, int ccx, int ccz, int radius, int peak, long seed, boolean core) {
        Column best = null;
        {
            double dx = x - ccx;
            double dz = z - ccz;
            double maxR = radius * 1.4;
            double distSq = dx * dx + dz * dz;
            if (distSq > maxR * maxR) {
                return null;
            }
            double raw = Math.sqrt(distSq) / radius;
            double d = raw + (Noise.value(x / 22.0, z / 22.0, seed) - 0.5) * 0.3;
            if (is.style() == Style.CRAGS && d >= 1.0) {
                // Sea stacks: lone pillars of rock in the water around the island
                double n = Noise.value(x / 4.0, z / 4.0, seed + 21);
                if (n > 0.8) {
                    int top = WATER_TOP + 3 + (int) ((n - 0.8) * 90);
                    return new Column(top, is, d, false, 0);
                }
                return null;
            }
            if (d >= 1.0) {
                return null;
            }
            double t = 1.0 - d;
            double exponent = switch (is.style()) {
                case SANDY -> 0.55;
                case TOWN -> core ? 0.35 : 0.55;
                case REEF -> 0.5;
                case VOLCANIC -> 1.0;
                case CURSED -> 0.75;
                case FORTRESS -> core ? 0.3 : 0.6;
                case BAYOU -> 0.2;
                case JUNGLE -> 0.6;
                case CLIFFS -> 0.12;
                case CRAGS -> 0.9;
                case DESERT -> 0.45;
                case KEYS, ATOLL -> 1.0;
                case ANCIENT -> 0.5;
            };
            double rough = switch (is.style()) {
                case CURSED -> 6.0;
                case VOLCANIC -> 4.0;
                case CRAGS -> 9.0;
                case CLIFFS -> 3.5;
                default -> core ? 2.5 : 4.0;
            };
            double h = SEA_FLOOR + (peak - SEA_FLOOR) * Math.pow(t, exponent);
            h += (Noise.value(x / 8.0, z / 8.0, seed + 7) - 0.5) * rough * t;
            if (!core) {
                // The wilds roll: broad hills and hollows across the hinterland
                h += (Noise.value(x / 37.0, z / 37.0, seed + 9) - 0.5) * 18 * t;
            }

            if (is.style() == Style.BAYOU) {
                // Low, flat mud flats broken by shallow black-water pools, dropping off at the edges.
                double n = Noise.value(x / 9.0, z / 9.0, seed + 11);
                double land = n > 0.42 ? (n > 0.74 ? 64 : 63) : 60;
                double edge = MathHelper.clamp((d - 0.8) / 0.2, 0.0, 1.0);
                h = land * (1.0 - edge) + SEA_FLOOR * edge;
            }

            if (is.style() == Style.KEYS) {
                // Islets wherever the noise rises, over shallows of white sand
                double n = Noise.value(x / 13.0, z / 13.0, seed + 13);
                double edge = MathHelper.clamp((d - 0.85) / 0.15, 0.0, 1.0);
                double land = n > 0.56 ? WATER_TOP + 1 + Math.min(7, (n - 0.56) * 45) : 57 + n * 3;
                h = land * (1.0 - edge) + SEA_FLOOR * edge;
            } else if (is.style() == Style.ATOLL) {
                // A sand ring peaking at 0.78 of the radius, broken by two channels, around a shallow lagoon
                double ring = 1.0 - Math.abs(raw - 0.78) / 0.2;
                double angle = Math.atan2(z - ccz, x - ccx);
                boolean channel = Math.abs(Math.sin(angle)) < 0.09;
                if (ring > 0 && !channel) {
                    h = WATER_TOP - 2 + ring * 6 + (Noise.value(x / 6.0, z / 6.0, seed + 5) - 0.5) * 2;
                } else if (raw < 0.6) {
                    h = 55 + (raw < 0.12 ? 9 : 0); // lagoon floor, with the shrine islet in the middle
                } else {
                    h = SEA_FLOOR + (WATER_TOP - 4 - SEA_FLOOR) * Math.max(0, 1.0 - (raw - 0.9) / 0.5);
                }
            } else if (is.style() == Style.ANCIENT) {
                h = WATER_TOP + 1 + Math.floor((h - WATER_TOP - 1) / 7.0) * 7.0; // flat terraces, seven blocks a step
                if (h < WATER_TOP + 1) h = SEA_FLOOR + (peak - SEA_FLOOR) * Math.pow(t, 0.5);
            }

            boolean crater = false;
            int lavaTop = 0;
            if (is.style() == Style.VOLCANIC && core) {
                if (raw < 0.12) {
                    crater = true;
                    lavaTop = peak - 12;
                    h = lavaTop - 3;
                } else if (raw < 0.18) {
                    h = Math.max(h, peak - 4 + (Noise.value(x / 3.0, z / 3.0, seed + 3) - 0.5) * 4);
                }
            }

            int top = (int) Math.floor(h);
            best = new Column(top, is, d, crater, lavaTop);
        }
        return best;
    }

    /** The y a player stands at on this column (on land), or the water surface. */
    public static int standY(int x, int z) {
        return Math.max(WATER_TOP, column(x, z).top()) + 1;
    }

    /** Standing level of the temple platform on Hollowgrave Island. */
    public static int templeFloorY() {
        return column(ALTAR_X, ALTAR_Z).top() + 1;
    }
}
