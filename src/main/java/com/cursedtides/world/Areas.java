package com.cursedtides.world;

import net.minecraft.world.item.DyeColor;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The named wilds. Each island's hinterland is divided into three areas with names of their own (the Weeping
 * Barrows, Gull's Heights, the Briar Thicket...), each a little more dangerous than the last, each with a landmark at
 * its heart (a ruined tower, standing stones, an abandoned camp, a shrine) and a named elite foe who keeps it.
 */
public final class Areas {
    private Areas() {}

    public record Area(Archipelago.Island island, String name, int x, int z, int r, int bonus, int landmark) {
        public boolean contains(double px, double pz) {
            return (px - x) * (px - x) + (pz - z) * (pz - z) <= (double) r * r;
        }
    }

    private static final String[] ADJ = {"Black", "Weeping", "Hanged Man's", "Gull", "Rotten", "Saint's", "Smuggler's",
            "Drowned", "Briar", "Mossy", "Crooked", "Widow's", "Bone", "Ember", "Salt", "Thorn", "Lantern", "Old King's",
            "Pale", "Rattling", "Red", "Silent", "Fiddler's", "Grey", "Hollow", "Beggar's", "Long", "Twisted"};

    private static String[] features(Archipelago.Style s) {
        return switch (s) {
            case SANDY, DESERT -> new String[]{"Dunes", "Flats", "Scrub", "Gully", "Barrens"};
            case TOWN, FORTRESS -> new String[]{"Woods", "Fields", "Ridge", "Quarry", "Hollow", "Downs"};
            case JUNGLE, ANCIENT -> new String[]{"Thicket", "Tangle", "Jungle", "Ravine", "Falls", "Canopy"};
            case VOLCANIC -> new String[]{"Ashlands", "Scree", "Cinder Fields", "Vents", "Slag"};
            case CURSED -> new String[]{"Barrows", "Moor", "Wood", "Gallows Hill", "Mire"};
            case BAYOU -> new String[]{"Mire", "Fen", "Backwater", "Sloughs", "Bog"};
            case CLIFFS, CRAGS -> new String[]{"Heights", "Ridge", "Crags", "Eyrie", "Scarp"};
            case KEYS, REEF, ATOLL -> new String[]{"Shallows", "Shoals", "Cays", "Flats"};
        };
    }

    public static final List<Area> ALL = build();

    private static List<Area> build() {
        List<Area> out = new ArrayList<>();
        for (Archipelago.Island is : Archipelago.ISLANDS) {
            if (!is.hasLobe()) continue;
            String[] feat = features(is.style());
            List<String> used = new ArrayList<>();
            for (int k = 0; k < 3; k++) {
                double a = Math.PI * 2 * k / 3 + Noise.rand(is.seed(), k, 501) * 0.8;
                double d = is.lobeR() * (k == 2 ? 0.25 : 0.55);
                int x = is.lobeX() + (int) Math.round(Math.cos(a) * d), z = is.lobeZ() + (int) Math.round(Math.sin(a) * d);
                String name;
                int tries = 0;
                do {
                    String adj = ADJ[(int) (Noise.rand(is.seed(), k * 31 + tries, 502) * ADJ.length)];
                    String f = feat[(int) (Noise.rand(is.seed(), k * 37 + tries, 503) * feat.length)];
                    name = (adj.endsWith("'s") ? "" : "the ") + adj + " " + f;
                    tries++;
                } while (used.contains(name) && tries < 20);
                used.add(name);
                out.add(new Area(is, name, x, z, (int) (is.lobeR() * 0.42), k + 1, (int) (Noise.rand(is.seed(), k, 504) * 4)));
            }
        }
        return List.copyOf(out);
    }

    public static Area at(double x, double z) {
        for (Area a : ALL) if (a.contains(x, z)) return a;
        return null;
    }

    // ------------------------------------------------------------------ landmarks

    static void addBuilds(List<Builds.Build> out) {
        for (Area a : ALL) {
            int x = a.x(), z = a.z();
            if (!Archipelago.column(x, z).isLand() || Archipelago.column(x, z).top() < Archipelago.WATER_TOP + 2) continue;
            boolean clear = true;
            for (int dx = -6; dx <= 6; dx += 6) for (int dz = -6; dz <= 6; dz += 6) if (Builds.blocksPlants(x + dx, z + dz)) clear = false;
            if (!clear) continue;
            out.add(new Builds.Build(x - 6, z - 6, x + 6, z + 6, true, c -> landmark(c, a)));
        }
    }

    private static BlockState s(net.minecraft.world.level.block.Block b) {
        return b.defaultBlockState();
    }

    private static void landmark(Canvas c, Area a) {
        int x = a.x(), z = a.z(), y = Builds.floorAt(x, z);
        switch (a.landmark()) {
            case 0 -> { // a ruined watchtower
                c.foundation(x - 3, z - 3, x + 3, z + 3, y, s(Blocks.COBBLESTONE));
                for (int dx = -3; dx <= 3; dx++) for (int dz = -3; dz <= 3; dz++) {
                    if (Math.abs(dx) != 3 && Math.abs(dz) != 3) continue;
                    int h = 3 + (int) (Noise.rand(x + dx, z + dz, 511) * 8);
                    for (int k = 0; k < h; k++) {
                        c.set(x + dx, y + k, z + dz, s(Noise.rand(x + dx, y + k, z + dz, 512) < 0.3 ? Blocks.MOSSY_STONE_BRICKS
                                : Noise.rand(x + dx, y + k, z + dz, 513) < 0.2 ? Blocks.CRACKED_STONE_BRICKS : Blocks.STONE_BRICKS));
                    }
                }
                c.set(x, y + 1, z + 3, s(Blocks.AIR));
                c.set(x, y, z + 3, s(Blocks.AIR));
                c.chest(x - 2, y, z - 2, net.minecraft.core.Direction.SOUTH, Builds.TREASURE);
            }
            case 1 -> { // standing stones
                for (int k = 0; k < 7; k++) {
                    double ang = Math.PI * 2 * k / 7;
                    int px = x + (int) Math.round(Math.cos(ang) * 5), pz = z + (int) Math.round(Math.sin(ang) * 5);
                    int py = Builds.floorAt(px, pz);
                    int h = 2 + (int) (Noise.rand(px, pz, 521) * 3);
                    c.fill(px, py, pz, px, py + h - 1, pz, s(Blocks.MOSSY_COBBLESTONE));
                }
                c.set(x, y - 1, z, s(Blocks.CHISELED_STONE_BRICKS));
                c.set(x, y, z, s(Blocks.SOUL_CAMPFIRE).setValue(CampfireBlock.LIT, true));
            }
            case 2 -> { // an abandoned camp
                for (int[] k : new int[][]{{-3, -2}, {3, 2}}) {
                    int tx = x + k[0], tz = z + k[1], ty = Builds.floorAt(tx, tz);
                    c.fill(tx - 1, ty, tz - 1, tx + 1, ty, tz + 1, s(Blocks.WOOL.pick(DyeColor.BROWN)));
                    c.fill(tx - 1, ty, tz, tx + 1, ty, tz, s(Blocks.AIR));
                    c.set(tx, ty + 1, tz - 1, s(Blocks.WOOL.pick(DyeColor.BROWN)));
                    c.set(tx, ty + 1, tz + 1, s(Blocks.WOOL.pick(DyeColor.BROWN)));
                    c.set(tx, ty + 1, tz, s(Blocks.AIR));
                }
                c.set(x, y, z, s(Blocks.CAMPFIRE).setValue(CampfireBlock.LIT, false));
                c.set(x + 2, y, z - 2, s(Blocks.BARREL));
                c.chest(x - 2, y, z + 2, net.minecraft.core.Direction.NORTH, Builds.TREASURE);
            }
            default -> { // a forgotten shrine
                c.foundation(x - 2, z - 2, x + 2, z + 2, y, s(Blocks.COBBLESTONE));
                c.fill(x - 2, y - 1, z - 2, x + 2, y - 1, z + 2, s(Blocks.POLISHED_ANDESITE));
                for (int[] k : new int[][]{{-2, -2}, {-2, 2}, {2, -2}, {2, 2}}) {
                    c.fill(x + k[0], y, z + k[1], x + k[0], y + 3, z + k[1], s(Blocks.STONE_BRICK_WALL));
                }
                c.fill(x - 2, y + 4, z - 2, x + 2, y + 4, z + 2, s(Blocks.STONE_BRICK_SLAB));
                c.set(x, y, z, s(Blocks.LODESTONE));
                c.set(x, y + 1, z, s(Blocks.CANDLE));
            }
        }
    }
}
