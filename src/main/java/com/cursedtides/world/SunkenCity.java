package com.cursedtides.world;

import net.minecraft.world.entity.EntityTypes;
import com.cursedtides.explore.Collectibles;
import com.cursedtides.world.Archipelago.Island;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Vesh Amaru, the drowned city: streets and broken houses on a sunken mound, a temple at its heart, and a grotto
 * beneath the temple floor. Everything is under water; bring a diving helmet.
 */
public final class SunkenCity {
    private SunkenCity() {}

    public static final Island ISLAND = Archipelago.DROWNED_CITY;
    private static final int CX = ISLAND.cx(), CZ = ISLAND.cz();
    private static final int R = 46;

    private static BlockState water() { return Blocks.WATER.defaultBlockState(); }

    private static int floor(int x, int z) {
        return Archipelago.column(x, z).top();
    }

    private static BlockState ruin(int x, int y, int z) {
        double n = Noise.rand(x, y, z, 777);
        return (n < 0.3 ? Blocks.PRISMARINE_BRICKS : n < 0.5 ? Blocks.CRACKED_STONE_BRICKS : n < 0.7 ? Blocks.MOSSY_STONE_BRICKS
                : n < 0.85 ? Blocks.PRISMARINE : Blocks.STONE_BRICKS).defaultBlockState();
    }

    static void addAll() {
        // Streets and houses, chunk by chunk
        Builds.add(CX - R, CZ - R, CX + R, CZ + R, SunkenCity::city);
    }

    private static void city(Canvas c) {
        c.columns(CX - R, CZ - R, CX + R, CZ + R, (x, z) -> {
            int dx = x - CX, dz = z - CZ;
            if (dx * dx + dz * dz > R * R) return;
            int top = floor(x, z);
            if (top >= Archipelago.WATER_TOP - 3) return;
            boolean plaza = Math.abs(dx) <= 9 && Math.abs(dz) <= 9;
            int gx = Math.floorMod(dx + 6, 13), gz = Math.floorMod(dz + 6, 13);
            boolean street = gx <= 2 || gz <= 2;
            if (plaza) {
                c.set(x, top, z, Blocks.DARK_PRISMARINE.defaultBlockState());
                for (int y = top + 1; y <= top + 9; y++) c.set(x, y, z, water());
                return;
            }
            if (street) {
                c.set(x, top, z, (Noise.rand(x, z, 31) < 0.15 ? Blocks.SAND : Blocks.PRISMARINE_BRICKS).defaultBlockState());
                for (int y = top + 1; y <= top + 3; y++) c.set(x, y, z, water());
                if (Noise.rand(x, z, 53) < 0.06) c.set(x, top + 1, z, Blocks.SEAGRASS.defaultBlockState());
                return;
            }
            // House plot: walls on the edge of the 10x10 block between streets, rubble inside
            int lx = gx - 3, lz = gz - 3; // 0..9
            boolean wall = lx == 0 || lx == 9 || lz == 0 || lz == 9;
            boolean door = (lx == 4 || lx == 5) && lz == 0;
            int blockId = Math.floorDiv(dx + 6, 13) * 31 + Math.floorDiv(dz + 6, 13);
            int height = 2 + (int) (Noise.rand(blockId, 5, 909) * 5);
            c.set(x, top, z, Blocks.STONE_BRICKS.defaultBlockState());
            if (wall && !door) {
                int h = height - (Noise.rand(x, z, 41) < 0.25 ? 1 + (int) (Noise.rand(x, z, 43) * 2) : 0); // broken tops
                for (int y = top + 1; y <= top + h; y++) {
                    boolean window = y == top + 2 && (lx == 4 || lz == 4) && !(lx == 0 && lz == 0);
                    c.set(x, y, z, window ? water() : ruin(x, y, z));
                }
            } else {
                for (int y = top + 1; y <= top + 6; y++) c.set(x, y, z, water());
                if (Noise.rand(x, z, 61) < 0.05) c.set(x, top + 1, z, ruin(x, top + 1, z));
                if (lx == 5 && lz == 5 && Noise.rand(blockId, 7, 911) < 0.35) c.set(x, top + 1, z, Blocks.SEA_LANTERN.defaultBlockState());
            }
        });
        houseChests(c);
        temple(c);
        grotto(c);
        guardians(c);
    }

    private static void houseChests(Canvas c) {
        for (int bx = -3; bx <= 3; bx++) {
            for (int bz = -3; bz <= 3; bz++) {
                if (bx == 0 && bz == 0) continue;
                int x = CX + bx * 13 + 1, z = CZ + bz * 13 + 1; // a corner inside each house
                int dx = x - CX, dz = z - CZ;
                if (dx * dx + dz * dz > (R - 6) * (R - 6) || Noise.rand(bx, bz, 913) > 0.3 || !c.contains(x, z)) continue;
                int top = floor(x, z);
                if (top >= Archipelago.WATER_TOP - 3) continue;
                c.chest(x, top + 1, z, Direction.SOUTH, Builds.TREASURE);
            }
        }
    }

    private static void temple(Canvas c) {
        int y = floor(CX, CZ);
        BlockState dark = Blocks.DARK_PRISMARINE.defaultBlockState();
        // Eight columns ringing the plaza
        for (int i = 0; i < 8; i++) {
            double a = Math.PI * 2 * i / 8;
            int x = CX + (int) Math.round(Math.cos(a) * 8), z = CZ + (int) Math.round(Math.sin(a) * 8);
            int h = 4 + (int) (Noise.rand(i, 3, 917) * 5);
            c.fill(x, y + 1, z, x, y + h, z, Blocks.PRISMARINE_BRICKS.defaultBlockState());
            if (h >= 7) c.set(x, y + h + 1, z, Blocks.SEA_LANTERN.defaultBlockState());
        }
        // The sanctum: a 9x9 room, open doorway south
        c.fill(CX - 4, y + 1, CZ - 4, CX + 4, y + 5, CZ + 4, dark);
        c.fill(CX - 3, y + 1, CZ - 3, CX + 3, y + 4, CZ + 3, water());
        c.fill(CX - 1, y + 1, CZ + 4, CX + 1, y + 3, CZ + 4, water());
        c.set(CX, y + 5, CZ, Blocks.SEA_LANTERN.defaultBlockState());
        c.set(CX - 3, y + 1, CZ - 3, Blocks.SEA_LANTERN.defaultBlockState());
        c.set(CX + 3, y + 1, CZ - 3, Blocks.SEA_LANTERN.defaultBlockState());
        // The heart chest, with two curios that belong to this place
        ItemStack painting = Collectibles.stack(Collectibles.byId("painting_city"));
        ItemStack bell = Collectibles.stack(Collectibles.byId("instrument_bell"));
        c.chest(CX, y + 1, CZ - 3, Direction.SOUTH, Builds.TREASURE, painting, bell, new ItemStack(com.cursedtides.item.ModItems.ANCIENT_RELIC));
        // The way down to the grotto
        c.fill(CX - 1, y - 7, CZ - 1, CX, y, CZ, water());
    }

    private static void grotto(Canvas c) {
        int y = floor(CX, CZ) - 12;
        c.fill(CX - 6, y - 1, CZ - 6, CX + 6, y + 5, CZ + 6, Blocks.DARK_PRISMARINE.defaultBlockState());
        c.fill(CX - 5, y, CZ - 5, CX + 5, y + 4, CZ + 5, water());
        c.fill(CX - 1, y + 4, CZ - 1, CX, y + 6, CZ, water());
        for (int[] l : new int[][]{{-5, -5}, {5, -5}, {-5, 5}, {5, 5}}) c.set(CX + l[0], y, CZ + l[1], Blocks.SEA_LANTERN.defaultBlockState());
        c.chest(CX + 4, y, CZ, Direction.WEST, Builds.TREASURE, new ItemStack(com.cursedtides.item.ModItems.TREASURE_MAP),
                new ItemStack(com.cursedtides.appearance.Accessories.STORM_CLOAK));
        c.chest(CX - 4, y, CZ, Direction.EAST, Builds.TREASURE, Collectibles.stack(Collectibles.byId("log_explorer")));
        for (int i = 0; i < 2; i++) {
            final int k = i;
            c.spawn(CX + 0.5 + (k == 0 ? 3 : -3), y, CZ + 0.5, 0, w -> {
                Mob m = EntityTypes.GUARDIAN.create(w, EntitySpawnReason.EVENT);
                if (m != null) m.setPersistenceRequired();
                return m;
            });
        }
    }

    private static void guardians(Canvas c) {
        for (int n = 0; n < 10; n++) {
            final int i = n;
            double a = Math.PI * 2 * i / 10;
            int x = CX + (int) (Math.cos(a) * 24), z = CZ + (int) (Math.sin(a) * 24);
            if (!c.contains(x, z)) continue;
            int top = floor(x, z);
            if (top >= Archipelago.WATER_TOP - 3) continue;
            c.spawn(x + 0.5, top + 1, z + 0.5, 0, w -> {
                Mob m = (i % 3 == 0 ? EntityTypes.GUARDIAN : EntityTypes.DROWNED).create(w, EntitySpawnReason.EVENT);
                if (m != null) {
                    m.setPersistenceRequired();
                    m.finalizeSpawn(w, w.getCurrentDifficultyAt(m.blockPosition()), EntitySpawnReason.STRUCTURE, null);
                }
                return m;
            });
        }
    }
}
