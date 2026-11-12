package com.cursedtides.world;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * Grows the ports, the navy strongholds and the pirate hideouts out to the size of the towns in the old pirate
 * games (Port Royal and Tortuga were some 130-175 blocks across). Each keeps its old heart; around it, roads run
 * inland from the harbour, lined with whatever that kind of place has: houses, warehouses and shops in a port,
 * barracks, storehouses and watchtowers in a navy town, shacks, tents and fire pits in a hideout. Ports get more
 * jetties along the harbour. Every plot is checked for open, fairly level ground that nothing else has claimed.
 */
final class Districts {
    private Districts() {}

    enum Kind { PORT, FREEPORT, NAVY, HIDEOUT }

    record Town(Archipelago.Island island, Kind kind, int reach) {}

    static final List<Town> TOWNS = List.of(
            new Town(Archipelago.SALTMARROW, Kind.FREEPORT, 92),
            new Town(Archipelago.PORT_MERIDIAN, Kind.PORT, 92),
            new Town(Archipelago.KINGSPORT, Kind.NAVY, 96),
            new Town(Archipelago.SAINT_VESPER, Kind.PORT, 80),
            new Town(Archipelago.IRONWATCH_ISLE, Kind.NAVY, 80),
            new Town(Archipelago.CROWNWATCH, Kind.NAVY, 86),
            new Town(Archipelago.RAVENS_LANDING, Kind.HIDEOUT, 76),
            new Town(Archipelago.BLACKTHORN_ISLE, Kind.HIDEOUT, 70),
            new Town(Archipelago.BLOODWAKE_CAY, Kind.HIDEOUT, 64));

    private static BlockState s(net.minecraft.block.Block b) {
        return b.getDefaultState();
    }

    /** Called from Builds' setup, after the hand-built towns are in and before the caverns and gaols look for room. */
    static void addAll(List<Builds.Build> out) {
        for (Town t : TOWNS) grow(out, t);
    }

    private static void grow(List<Builds.Build> out, Town t) {
        Archipelago.Island is = t.island();
        int cx = is.cx(), cz = is.cz();
        // Roads fan out inland: west, north-west, north, north-east, east (the harbour is to the south)
        double[] angles = {Math.PI, Math.PI * 1.25, Math.PI * 1.5, Math.PI * 1.75, 0};
        List<int[]> roadCells = new ArrayList<>();
        int plot = 0;
        for (double a : angles) {
            double dx = Math.cos(a), dz = Math.sin(a);
            for (int r = 12; r <= t.reach(); r++) {
                int x = cx + (int) Math.round(dx * r), z = cz + (int) Math.round(dz * r);
                if (!land(x, z) || Builds.blocksPlants(x, z)) continue;
                roadCells.add(new int[]{x, z});
            }
            // Plots on both sides, every 13 blocks along
            for (int r = 26; r <= t.reach() - 4; r += 13) {
                for (int side = -1; side <= 1; side += 2) {
                    int px = cx + (int) Math.round(dx * r - dz * side * 9);
                    int pz = cz + (int) Math.round(dz * r + dx * side * 9);
                    int rxp = cx + (int) Math.round(dx * r), rzp = cz + (int) Math.round(dz * r);
                    int half = 6;
                    if (!buildable(px, pz, half)) continue;
                    final int bx = px, bz = pz, k = plot++;
                    final Direction door = facing(rxp - px, rzp - pz);
                    out.add(new Builds.Build(px - half, pz - half, px + half, pz + half, true,
                            c -> building(c, t.kind(), k, bx, bz, door, cx, cz)));
                }
            }
        }
        com.cursedtides.CursedTides.LOGGER.info("Town of {}: {} new plots", is.name(), plot);
        // The roads themselves (after the plots are claimed, so a road never runs through a building)
        final List<int[]> cells = List.copyOf(roadCells);
        int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (int[] k : cells) {
            minX = Math.min(minX, k[0] - 2); maxX = Math.max(maxX, k[0] + 2);
            minZ = Math.min(minZ, k[1] - 2); maxZ = Math.max(maxZ, k[1] + 2);
        }
        if (!cells.isEmpty()) {
            out.add(new Builds.Build(minX, minZ, maxX, maxZ, false, c -> roads(c, t.kind(), cells)));
        }
        // More jetties along the harbour
        if (t.kind() != Kind.HIDEOUT) {
            for (int off : new int[]{-34, 34}) {
                int x = cx + off;
                int shore = Integer.MIN_VALUE;
                for (int z = cz; z < cz + is.radius() * 2; z++) {
                    if (!land(x, z)) { shore = z; break; }
                }
                if (shore == Integer.MIN_VALUE || Builds.blocksPlants(x, shore - 3)) continue;
                final int z0 = shore - 3, z1 = shore + 46;
                out.add(new Builds.Build(x - 2, z0, x + 2, z1, true, c -> Builds.pier(c, x, z0, z1, 2)));
            }
        }
    }

    private static boolean land(int x, int z) {
        Archipelago.Column col = Archipelago.column(x, z);
        return col.isLand() && !col.crater() && col.top() >= Archipelago.WATER_TOP + 2;
    }

    private static boolean buildable(int x, int z, int half) {
        int lo = Integer.MAX_VALUE, hi = Integer.MIN_VALUE;
        for (int dx = -half; dx <= half; dx += half) {
            for (int dz = -half; dz <= half; dz += half) {
                if (!land(x + dx, z + dz) || Builds.blocksPlants(x + dx, z + dz)) return false;
                int top = Archipelago.column(x + dx, z + dz).top();
                lo = Math.min(lo, top);
                hi = Math.max(hi, top);
            }
        }
        return hi - lo <= 7;
    }

    private static Direction facing(int dx, int dz) {
        if (Math.abs(dx) > Math.abs(dz)) return dx > 0 ? Direction.EAST : Direction.WEST;
        return dz > 0 ? Direction.SOUTH : Direction.NORTH;
    }

    // ------------------------------------------------------------------ roads

    private static void roads(Canvas c, Kind kind, List<int[]> cells) {
        for (int[] k : cells) {
            for (int ox = -1; ox <= 1; ox++) {
                for (int oz = -1; oz <= 1; oz++) {
                    int x = k[0] + ox, z = k[1] + oz;
                    if (!c.contains(x, z)) continue;
                    int y = Builds.floorAt(x, z) - 1;
                    if (y <= Archipelago.WATER_TOP) continue;
                    double r = Noise.rand(x, z, 7071);
                    BlockState road = switch (kind) {
                        case PORT -> s(r < 0.6 ? Blocks.COBBLESTONE : r < 0.85 ? Blocks.STONE_BRICKS : Blocks.GRAVEL);
                        case FREEPORT -> s(r < 0.5 ? Blocks.GRAVEL : r < 0.8 ? Blocks.COARSE_DIRT : Blocks.COBBLESTONE);
                        case NAVY -> s(r < 0.6 ? Blocks.STONE_BRICKS : r < 0.85 ? Blocks.ANDESITE : Blocks.CRACKED_STONE_BRICKS);
                        case HIDEOUT -> s(r < 0.6 ? Blocks.DIRT_PATH : Blocks.COARSE_DIRT);
                    };
                    c.set(x, y, z, road);
                    c.set(x, y + 1, z, s(Blocks.AIR));
                    c.set(x, y + 2, z, s(Blocks.AIR));
                }
            }
            // Lamps along the way
            if (kind != Kind.HIDEOUT && Math.floorMod(k[0] * 31 + k[1] * 17, 23) == 0) {
                int x = k[0] + 2, z = k[1];
                if (c.contains(x, z)) {
                    int y = Builds.floorAt(x, z);
                    c.fill(x, y, z, x, y + 2, z, s(Blocks.SPRUCE_FENCE));
                    c.set(x, y + 3, z, s(Blocks.LANTERN));
                }
            }
        }
    }

    // ------------------------------------------------------------------ buildings

    private static void building(Canvas c, Kind kind, int k, int x, int z, Direction door, int townX, int townZ) {
        switch (kind) {
            case PORT, FREEPORT -> {
                switch (k % 6) {
                    case 0, 3 -> Builds.house(c, x, z, x + door.getOffsetX() * 9, z + door.getOffsetZ() * 9);
                    case 1 -> warehouse(c, x, z, door, kind == Kind.FREEPORT);
                    case 2 -> shop(c, x, z, door, kind == Kind.FREEPORT);
                    case 4 -> market(c, x, z);
                    default -> townhouse(c, x, z, door, kind == Kind.FREEPORT);
                }
            }
            case NAVY -> {
                switch (k % 4) {
                    case 0 -> barracks(c, x, z, door);
                    case 1 -> watchtower(c, x, z);
                    case 2 -> warehouse(c, x, z, door, false);
                    default -> drillYard(c, x, z);
                }
            }
            case HIDEOUT -> {
                switch (k % 4) {
                    case 0, 2 -> shack(c, x, z, door);
                    case 1 -> tents(c, x, z);
                    default -> firePit(c, x, z);
                }
            }
        }
    }

    private static void door(Canvas c, int x1, int z1, int x2, int z2, int floor, Direction door) {
        int mx = (x1 + x2) / 2, mz = (z1 + z2) / 2;
        switch (door) {
            case NORTH -> Builds.doorway(c, mx, z1, floor);
            case SOUTH -> Builds.doorway(c, mx, z2, floor);
            case WEST -> Builds.doorway(c, x1, mz, floor);
            default -> Builds.doorway(c, x2, mz, floor);
        }
    }

    private static void warehouse(Canvas c, int cx, int cz, Direction door, boolean rough) {
        int x1 = cx - 5, x2 = cx + 5, z1 = cz - 4, z2 = cz + 4;
        int floor = Builds.floorAt(cx, cz);
        Builds.room(c, x1, z1, x2, z2, floor, 5, s(rough ? Blocks.SPRUCE_PLANKS : Blocks.BRICKS),
                s(Blocks.STRIPPED_SPRUCE_LOG), s(Blocks.SPRUCE_PLANKS));
        Builds.roof(c, x1, z1, x2, z2, floor + 5, s(Blocks.DARK_OAK_PLANKS));
        c.fill(x1 + 1, floor, z1 + 1, x2 - 1, floor + 4, z2 - 1, s(Blocks.AIR));
        door(c, x1, z1, x2, z2, floor, door);
        // Crates and sacks
        for (int i = 0; i < 9; i++) {
            int px = x1 + 1 + (int) (Noise.rand(cx, i, cz, 7101) * 9), pz = z1 + 1 + (int) (Noise.rand(cx, i, cz, 7102) * 7);
            int h = 1 + (int) (Noise.rand(px, pz, 7103) * 3);
            c.fill(px, floor, pz, px, floor + h - 1, pz, s(i % 3 == 0 ? Blocks.HAY_BLOCK : Blocks.BARREL));
        }
        c.fill(cx, floor, cz, cx, floor + 1, cz, s(Blocks.AIR));
        c.set(cx, floor + 4, cz, Builds.hangingLantern());
    }

    private static void shop(Canvas c, int cx, int cz, Direction door, boolean rough) {
        int x1 = cx - 4, x2 = cx + 4, z1 = cz - 4, z2 = cz + 4;
        int floor = Builds.floorAt(cx, cz);
        Builds.room(c, x1, z1, x2, z2, floor, 4, s(rough ? Blocks.OAK_PLANKS : Blocks.WHITE_TERRACOTTA),
                s(Blocks.DARK_OAK_LOG), s(Blocks.OAK_PLANKS));
        c.fill(x1 + 1, floor, z1 + 1, x2 - 1, floor + 3, z2 - 1, s(Blocks.AIR));
        Builds.roof(c, x1, z1, x2, z2, floor + 4, s(rough ? Blocks.SPRUCE_PLANKS : Blocks.RED_TERRACOTTA));
        Builds.windows(c, x1, z1, x2, z2, floor + 1);
        door(c, x1, z1, x2, z2, floor, door);
        // Counter and shelves
        c.fill(x1 + 2, floor, cz - 1, x2 - 2, floor, cz - 1, s(Blocks.SPRUCE_SLAB));
        c.fill(x1 + 1, floor, z1 + 1, x2 - 1, floor + 1, z1 + 1, s(Blocks.CHISELED_BOOKSHELF));
        c.set(cx, floor + 3, cz, Builds.hangingLantern());
        // An awning over the door
        int ax = cx + door.getOffsetX() * 5, az = cz + door.getOffsetZ() * 5;
        BlockState cloth = s(Noise.rand(cx, cz, 7111) < 0.5 ? Blocks.RED_WOOL : Blocks.YELLOW_WOOL);
        if (door.getAxis() == Direction.Axis.Z) c.fill(ax - 2, floor + 3, az, ax + 2, floor + 3, az, cloth);
        else c.fill(ax, floor + 3, az - 2, ax, floor + 3, az + 2, cloth);
    }

    private static void townhouse(Canvas c, int cx, int cz, Direction door, boolean rough) {
        int x1 = cx - 4, x2 = cx + 4, z1 = cz - 3, z2 = cz + 3;
        int floor = Builds.floorAt(cx, cz);
        BlockState lower = s(rough ? Blocks.COBBLESTONE : Blocks.STONE_BRICKS);
        BlockState upper = s(rough ? Blocks.SPRUCE_PLANKS : Blocks.BIRCH_PLANKS);
        Builds.room(c, x1, z1, x2, z2, floor, 4, lower, s(Blocks.SPRUCE_LOG), s(Blocks.SPRUCE_PLANKS));
        c.fill(x1, floor + 4, z1, x2, floor + 4, z2, s(Blocks.SPRUCE_PLANKS));
        c.walls(x1, floor + 5, z1, x2, floor + 7, z2, upper);
        c.fill(x1 + 1, floor, z1 + 1, x2 - 1, floor + 3, z2 - 1, s(Blocks.AIR));
        c.fill(x1 + 1, floor + 5, z1 + 1, x2 - 1, floor + 7, z2 - 1, s(Blocks.AIR));
        Builds.roof(c, x1, z1, x2, z2, floor + 8, s(Blocks.DARK_OAK_PLANKS));
        Builds.windows(c, x1, z1, x2, z2, floor + 1);
        Builds.windows(c, x1, z1, x2, z2, floor + 6);
        door(c, x1, z1, x2, z2, floor, door);
        BlockState ladder = s(Blocks.LADDER).with(net.minecraft.block.LadderBlock.FACING, Direction.SOUTH);
        for (int y = floor; y <= floor + 4; y++) c.set(x1 + 1, y, z1 + 1, ladder);
        c.set(x2 - 1, floor + 5, z2 - 1, s(Blocks.BARREL));
        c.set(cx, floor + 3, cz, Builds.hangingLantern());
    }

    private static void market(Canvas c, int cx, int cz) {
        int floor = Builds.floorAt(cx, cz);
        c.foundation(cx - 5, cz - 5, cx + 5, cz + 5, floor, s(Blocks.COBBLESTONE));
        c.fill(cx - 5, floor - 1, cz - 5, cx + 5, floor - 1, cz + 5, s(Blocks.SMOOTH_STONE));
        BlockState[] canopies = {s(Blocks.RED_WOOL), s(Blocks.WHITE_WOOL), s(Blocks.BLUE_WOOL), s(Blocks.YELLOW_WOOL)};
        int i = 0;
        for (int[] k : new int[][]{{-3, -3}, {3, -3}, {-3, 3}, {3, 3}}) {
            Builds.stall(c, cx + k[0], cz + k[1], floor - 1, canopies[i++ % canopies.length]);
        }
        c.set(cx, floor, cz, s(Blocks.CAULDRON));
    }

    private static void barracks(Canvas c, int cx, int cz, Direction door) {
        int x1 = cx - 5, x2 = cx + 5, z1 = cz - 4, z2 = cz + 4;
        int floor = Builds.floorAt(cx, cz);
        Builds.room(c, x1, z1, x2, z2, floor, 5, s(Blocks.STONE_BRICKS), s(Blocks.POLISHED_ANDESITE), s(Blocks.SPRUCE_PLANKS));
        c.fill(x1 + 1, floor, z1 + 1, x2 - 1, floor + 4, z2 - 1, s(Blocks.AIR));
        c.fill(x1 - 1, floor + 5, z1 - 1, x2 + 1, floor + 5, z2 + 1, s(Blocks.STONE_BRICK_SLAB));
        Builds.windows(c, x1, z1, x2, z2, floor + 2);
        door(c, x1, z1, x2, z2, floor, door);
        for (int x = x1 + 1; x <= x2 - 2; x += 2) Builds.bed(c, x, floor, z1 + 1, Direction.SOUTH);
        c.fill(x1 + 1, floor, z2 - 1, x2 - 1, floor, z2 - 1, s(Blocks.BARREL));
        c.set(cx, floor + 4, cz, Builds.hangingLantern());
        c.spawn(cx + 0.5, floor, cz + 0.5, 0, w -> com.cursedtides.entity.ModEntities.MARINE.create(w));
    }

    private static void watchtower(Canvas c, int cx, int cz) {
        int floor = Builds.floorAt(cx, cz);
        c.foundation(cx - 2, cz - 2, cx + 2, cz + 2, floor, s(Blocks.COBBLESTONE));
        c.walls(cx - 2, floor, cz - 2, cx + 2, floor + 10, cz + 2, s(Blocks.STONE_BRICKS));
        c.fill(cx - 1, floor, cz - 1, cx + 1, floor + 10, cz + 1, s(Blocks.AIR));
        Builds.doorway(c, cx, cz + 2, floor);
        BlockState ladder = s(Blocks.LADDER).with(net.minecraft.block.LadderBlock.FACING, Direction.SOUTH);
        for (int y = floor; y <= floor + 11; y++) c.set(cx, y, cz - 1, ladder);
        c.fill(cx - 3, floor + 11, cz - 3, cx + 3, floor + 11, cz + 3, s(Blocks.SPRUCE_PLANKS));
        c.set(cx, floor + 11, cz - 1, ladder);
        for (int x = cx - 3; x <= cx + 3; x++) for (int z = cz - 3; z <= cz + 3; z++) {
            if (Math.abs(x - cx) == 3 || Math.abs(z - cz) == 3) {
                c.set(x, floor + 12, z, ((x + z) & 1) == 0 ? s(Blocks.STONE_BRICK_WALL) : s(Blocks.AIR));
            }
        }
        c.set(cx + 2, floor + 12, cz + 2, s(Blocks.LANTERN));
        c.spawn(cx + 1.5, floor + 12, cz + 1.5, 180, w -> com.cursedtides.entity.ModEntities.MARINE.create(w));
    }

    private static void drillYard(Canvas c, int cx, int cz) {
        int floor = Builds.floorAt(cx, cz);
        c.foundation(cx - 5, cz - 5, cx + 5, cz + 5, floor, s(Blocks.COBBLESTONE));
        c.fill(cx - 5, floor - 1, cz - 5, cx + 5, floor - 1, cz + 5, s(Blocks.GRAVEL));
        c.walls(cx - 5, floor, cz - 5, cx + 5, floor, cz + 5, s(Blocks.SPRUCE_FENCE));
        c.set(cx + 5, floor, cz, s(Blocks.AIR));
        c.set(cx - 5, floor, cz, s(Blocks.AIR));
        for (int[] k : new int[][]{{-2, -2}, {2, -2}, {0, 2}}) {
            c.set(cx + k[0], floor, cz + k[1], s(Blocks.HAY_BLOCK));
            c.set(cx + k[0], floor + 1, cz + k[1], s(Blocks.TARGET));
            c.set(cx + k[0], floor + 2, cz + k[1], s(Blocks.CARVED_PUMPKIN));
        }
        c.set(cx + 4, floor, cz + 4, s(Blocks.STONE_BRICKS));
        c.set(cx + 4, floor + 1, cz + 4, s(Blocks.LANTERN));
    }

    private static void shack(Canvas c, int cx, int cz, Direction door) {
        int x1 = cx - 3, x2 = cx + 3, z1 = cz - 3, z2 = cz + 3;
        int floor = Builds.floorAt(cx, cz);
        Builds.room(c, x1, z1, x2, z2, floor, 3, s(Noise.rand(cx, cz, 7201) < 0.5 ? Blocks.SPRUCE_PLANKS : Blocks.STRIPPED_OAK_WOOD),
                s(Blocks.SPRUCE_LOG), s(Blocks.COARSE_DIRT));
        c.fill(x1 + 1, floor, z1 + 1, x2 - 1, floor + 2, z2 - 1, s(Blocks.AIR));
        c.fill(x1 - 1, floor + 3, z1 - 1, x2 + 1, floor + 3, z2 + 1, s(Blocks.SPRUCE_SLAB));
        // Patched with whatever was to hand
        for (int i = 0; i < 4; i++) {
            int px = x1 + (int) (Noise.rand(cx, i, cz, 7202) * 7), pz = Noise.rand(cx, i, cz, 7203) < 0.5 ? z1 : z2;
            c.set(px, floor + 1, pz, s(i % 2 == 0 ? Blocks.MOSSY_COBBLESTONE : Blocks.OAK_PLANKS));
        }
        door(c, x1, z1, x2, z2, floor, door);
        c.set(x1 + 1, floor, z1 + 1, s(Blocks.BARREL));
        c.set(x2 - 1, floor, z1 + 1, s(Blocks.HAY_BLOCK));
        c.set(cx, floor + 2, cz, Builds.hangingLantern());
    }

    private static void tents(Canvas c, int cx, int cz) {
        int floor = Builds.floorAt(cx, cz);
        BlockState[] cloth = {s(Blocks.BROWN_WOOL), s(Blocks.WHITE_WOOL), s(Blocks.RED_WOOL)};
        int i = 0;
        for (int[] k : new int[][]{{-3, -2}, {3, -2}, {0, 3}}) {
            int tx = cx + k[0], tz = cz + k[1];
            int y = Builds.floorAt(tx, tz);
            BlockState w = cloth[i++ % cloth.length];
            c.fill(tx - 1, y, tz - 1, tx + 1, y, tz + 1, w);
            c.fill(tx - 1, y, tz, tx + 1, y, tz, s(Blocks.AIR));
            c.set(tx, y + 1, tz - 1, w);
            c.set(tx, y + 1, tz + 1, w);
            c.set(tx, y + 2, tz, w);
            c.set(tx, y + 1, tz, s(Blocks.AIR));
        }
        c.set(cx, floor, cz, s(Blocks.CAMPFIRE).with(CampfireBlock.LIT, true));
    }

    private static void firePit(Canvas c, int cx, int cz) {
        int floor = Builds.floorAt(cx, cz);
        for (int x = cx - 3; x <= cx + 3; x++) for (int z = cz - 3; z <= cz + 3; z++) {
            double d = Math.hypot(x - cx, z - cz);
            if (d <= 3.4 && c.contains(x, z)) c.set(x, Builds.floorAt(x, z) - 1, z, s(Blocks.COARSE_DIRT));
            if (d > 2.4 && d <= 3.4 && Noise.rand(x, z, 7301) < 0.4) c.set(x, Builds.floorAt(x, z), z, s(Blocks.SPRUCE_LOG));
        }
        c.set(cx, floor - 1, cz, s(Blocks.NETHERRACK));
        c.set(cx, floor, cz, s(Blocks.FIRE));
        c.set(cx + 2, floor, cz - 1, s(Blocks.BARREL));
        c.set(cx - 2, floor, cz + 1, s(Blocks.BARREL));
    }
}
