package com.cursedtides.world;

import net.minecraft.world.item.DyeColor;
import com.cursedtides.world.Archipelago.Column;
import com.cursedtides.world.Archipelago.Style;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Island terrain, ore veins and vegetation for one chunk. */
final class Terrain {
    private Terrain() {}

    private static BlockState s(Block b) {
        return b.defaultBlockState();
    }

    // ---------------- Land ----------------

    static void shapeLand(Canvas c) {
        c.columns(c.minX, c.minZ, c.maxX, c.maxZ, (x, z) -> {
            Column col = Archipelago.column(x, z);
            if (col.island() == null || col.top() <= Archipelago.SEA_FLOOR) {
                return;
            }
            int top = col.top();
            for (int y = Archipelago.SEA_FLOOR + 1; y <= top; y++) {
                c.set(x, y, z, blockFor(col, x, y, z, top));
            }
            if (col.crater()) {
                for (int y = top + 1; y <= col.lavaTop(); y++) {
                    c.set(x, y, z, s(Blocks.LAVA));
                }
            }
        });
    }

    private static BlockState blockFor(Column col, int x, int y, int z, int top) {
        int depth = top - y;
        double r = Noise.rand(x, y, z, 5);
        boolean shore = top <= Archipelago.WATER_TOP + 1;
        Style style = col.island().style();

        switch (style) {
            case REEF -> {
                if (depth == 0 && top <= 59) {
                    return s(r < 0.25 ? Blocks.BRAIN_CORAL_BLOCK : r < 0.5 ? Blocks.TUBE_CORAL_BLOCK
                            : r < 0.7 ? Blocks.FIRE_CORAL_BLOCK : r < 0.85 ? Blocks.HORN_CORAL_BLOCK : Blocks.SAND);
                }
                return depth <= 3 ? s(Blocks.SAND) : s(Blocks.SANDSTONE);
            }
            case VOLCANIC -> {
                if (shore) {
                    // Black sand beaches
                    return depth <= 2 ? s(Blocks.CONCRETE_POWDER.pick(DyeColor.BLACK)) : s(Blocks.BLACKSTONE);
                }
                if (depth == 0) {
                    if (col.dist() < 0.32 && r < 0.2) return s(Blocks.MAGMA_BLOCK);
                    return s(r < 0.4 ? Blocks.BASALT : r < 0.7 ? Blocks.BLACKSTONE : r < 0.85 ? Blocks.TUFF : Blocks.COARSE_DIRT);
                }
                return depth <= 3 ? s(Blocks.TUFF) : s(Blocks.BLACKSTONE);
            }
            case CURSED -> {
                if (shore) {
                    return depth <= 2 ? s(r < 0.5 ? Blocks.GRAVEL : Blocks.SAND) : s(Blocks.STONE);
                }
                if (depth == 0) {
                    return s(r < 0.35 ? Blocks.PODZOL : r < 0.65 ? Blocks.COARSE_DIRT : r < 0.8 ? Blocks.SOUL_SOIL : Blocks.GRASS_BLOCK);
                }
                return depth <= 3 ? s(Blocks.DIRT) : s(r < 0.5 ? Blocks.DEEPSLATE : Blocks.STONE);
            }
            case BAYOU -> { // mud flats, moss and black-water pools
                if (depth == 0) {
                    if (top > Archipelago.WATER_TOP) {
                        return s(r < 0.5 ? Blocks.MUD : r < 0.82 ? Blocks.GRASS_BLOCK : Blocks.MOSS_BLOCK);
                    }
                    return s(r < 0.6 ? Blocks.MUD : Blocks.CLAY);
                }
                if (depth <= 3) return s(r < 0.6 ? Blocks.MUD : Blocks.DIRT);
                return depth <= 6 ? s(Blocks.DIRT) : s(Blocks.STONE);
            }
            case JUNGLE -> { // white sand, then deep loam under dense undergrowth
                if (shore) return depth <= 3 ? s(Blocks.SAND) : s(Blocks.SANDSTONE);
                if (depth == 0) return s(r < 0.8 ? Blocks.GRASS_BLOCK : r < 0.92 ? Blocks.PODZOL : Blocks.MOSS_BLOCK);
                return depth <= 4 ? s(Blocks.DIRT) : s(Blocks.STONE);
            }
            case CLIFFS -> { // grey cliffs streaked with calcite, a thin cap of turf
                if (shore) return depth <= 1 ? s(r < 0.5 ? Blocks.GRAVEL : Blocks.STONE) : s(Blocks.STONE);
                if (depth == 0 && top > 80) return s(r < 0.8 ? Blocks.GRASS_BLOCK : Blocks.COARSE_DIRT);
                double band = Noise.rand(0, y, 0, 9);
                return s(band < 0.15 ? Blocks.CALCITE : band < 0.45 ? Blocks.ANDESITE : r < 0.1 ? Blocks.COBBLESTONE : Blocks.STONE);
            }
            case CRAGS -> { // storm-scoured rock, no soil to speak of
                if (depth == 0 && !shore && r < 0.35) return s(Blocks.MOSS_BLOCK);
                return s(r < 0.5 ? Blocks.STONE : r < 0.75 ? Blocks.DEEPSLATE : r < 0.9 ? Blocks.TUFF : Blocks.GRAVEL);
            }
            case DESERT -> { // dunes over red rock
                if (depth <= 3) return s(depth == 0 && r < 0.12 && !shore ? Blocks.RED_SAND : Blocks.SAND);
                double band = Noise.rand(0, y, 0, 13);
                if (depth <= 8) return s(Blocks.SANDSTONE);
                return s(band < 0.3 ? Blocks.TERRACOTTA : band < 0.55 ? Blocks.DYED_TERRACOTTA.pick(DyeColor.ORANGE) : band < 0.7 ? Blocks.RED_SANDSTONE : Blocks.SANDSTONE);
            }
            case KEYS -> { // white sand shallows, grassy islets, old stone beneath
                if (top <= Archipelago.WATER_TOP) return depth <= 2 ? s(Blocks.SAND) : s(Blocks.SANDSTONE);
                if (shore) return depth <= 2 ? s(Blocks.SAND) : s(Blocks.SANDSTONE);
                if (depth == 0) return s(r < 0.85 ? Blocks.GRASS_BLOCK : Blocks.MOSSY_COBBLESTONE);
                return depth <= 2 ? s(Blocks.DIRT) : s(r < 0.3 ? Blocks.MOSSY_STONE_BRICKS : Blocks.STONE);
            }
            case ATOLL -> { // pale sand, with soul sand where the veil is thinnest
                if (depth == 0 && top > Archipelago.WATER_TOP + 1 && r < 0.12) return s(Blocks.SOUL_SAND);
                if (depth == 0 && top < Archipelago.WATER_TOP - 2 && r < 0.3) {
                    return s(r < 0.1 ? Blocks.TUBE_CORAL_BLOCK : r < 0.2 ? Blocks.BRAIN_CORAL_BLOCK : Blocks.HORN_CORAL_BLOCK);
                }
                return depth <= 4 ? s(Blocks.SAND) : s(Blocks.SANDSTONE);
            }
            case ANCIENT -> { // terraces faced with old masonry
                if (shore) return depth <= 2 ? s(Blocks.SAND) : s(Blocks.SANDSTONE);
                if (depth == 0) return s(r < 0.88 ? Blocks.GRASS_BLOCK : Blocks.MOSSY_STONE_BRICKS);
                if (depth <= 2) return s(Blocks.DIRT);
                return s(r < 0.35 ? Blocks.MOSSY_STONE_BRICKS : r < 0.6 ? Blocks.STONE_BRICKS : r < 0.7 ? Blocks.CRACKED_STONE_BRICKS : Blocks.STONE);
            }
            case FORTRESS -> { // rocky coastline, short turf over stone
                if (shore) {
                    return depth <= 1 ? s(r < 0.5 ? Blocks.GRAVEL : Blocks.STONE) : s(Blocks.STONE);
                }
                if (depth == 0) return s(r < 0.85 ? Blocks.GRASS_BLOCK : Blocks.COARSE_DIRT);
                return depth <= 1 ? s(Blocks.DIRT) : s(r < 0.7 ? Blocks.STONE : Blocks.ANDESITE);
            }
            default -> { // SANDY, TOWN: white sand beaches, green interiors
                if (shore || top <= Archipelago.WATER_TOP + 2 && depth <= 1) {
                    return depth <= 3 ? s(Blocks.SAND) : s(Blocks.SANDSTONE);
                }
                if (depth == 0) return s(Blocks.GRASS_BLOCK);
                return depth <= 3 ? s(Blocks.DIRT) : s(Blocks.STONE);
            }
        }
    }

    // ---------------- Ores ----------------

    static void ores(Canvas c) {
        int cx = c.minX >> 4, cz = c.minZ >> 4;
        for (int i = 0; i < 28; i++) {
            long h = Noise.hash(cx, i, cz, 777);
            int x = c.minX + (int) ((h >>> 4) & 15);
            int z = c.minZ + (int) ((h >>> 8) & 15);
            int topStone = Math.max(34, Archipelago.column(x, z).top() - 4);
            int y = -60 + (int) (((h >>> 12) & 0xFFFF) % (topStone + 61));
            double kind = Noise.rand(cx, i, cz, 778);
            BlockState ore;
            int size;
            if (kind < 0.33) { ore = s(Blocks.COAL_ORE); size = 6; }
            else if (kind < 0.62) { ore = s(Blocks.IRON_ORE); size = 5; }
            else if (kind < 0.76) { ore = s(Blocks.COPPER_ORE); size = 5; }
            else if (kind < 0.85) { ore = s(Blocks.GOLD_ORE); size = 4; }
            else if (kind < 0.92) { ore = s(Blocks.REDSTONE_ORE); size = 4; }
            else if (kind < 0.97) { ore = s(Blocks.LAPIS_ORE); size = 3; }
            else { ore = y < 10 ? s(Blocks.DIAMOND_ORE) : s(Blocks.EMERALD_ORE); size = 2; }
            // Goldwater Cay lives up to its name
            var isle = Archipelago.column(x, z).island();
            if (isle == Archipelago.GOLDWATER_CAY && kind < 0.6) { ore = s(Blocks.GOLD_ORE); size = 6; }
            if (isle == Archipelago.EMBERFALL && kind > 0.9) { ore = s(Blocks.DIAMOND_ORE); size = 3; }

            int px = x, py = y, pz = z;
            for (int n = 0; n < size; n++) {
                BlockState here = c.get(px, py, pz);
                if (here.is(Blocks.STONE) || here.is(Blocks.DEEPSLATE) || here.is(Blocks.BLACKSTONE)
                        || here.is(Blocks.TUFF) || here.is(Blocks.SANDSTONE)) {
                    c.set(px, py, pz, ore);
                }
                long step = Noise.hash(px, py, pz, 779 + n);
                px += (int) (step & 3) - 1;
                py += (int) ((step >>> 2) & 3) - 1;
                pz += (int) ((step >>> 4) & 3) - 1;
                px = Math.max(c.minX, Math.min(c.maxX, px));
                pz = Math.max(c.minZ, Math.min(c.maxZ, pz));
            }
        }
    }

    // ---------------- Vegetation ----------------

    private static final int TREE_CELL = 9;

    static void vegetation(Canvas c) {
        // Ground cover
        c.columns(c.minX, c.minZ, c.maxX, c.maxZ, (x, z) -> {
            Column col = Archipelago.column(x, z);
            if (col.island() != null && col.island().style() == Style.BAYOU && !Builds.blocksPlants(x, z)) {
                double r = Noise.rand(x, z, 37);
                if (col.top() < Archipelago.WATER_TOP && col.dist() < 0.85) {
                    if (r < 0.14) c.set(x, Archipelago.WATER_TOP + 1, z, s(Blocks.LILY_PAD));
                    else if (r < 0.2) c.set(x, col.top() + 1, z, s(Blocks.SEAGRASS));
                    return;
                }
                if (col.isLand()) {
                    BlockState ground = c.get(x, col.top(), z);
                    if (r < 0.12) c.set(x, col.top() + 1, z, s(Blocks.MOSS_CARPET));
                    else if (r < 0.2 && ground.is(Blocks.GRASS_BLOCK)) c.set(x, col.top() + 1, z, s(Blocks.SHORT_GRASS));
                    else if (r < 0.215) c.set(x, col.top() + 1, z, s(Blocks.BLUE_ORCHID));
                }
                return;
            }
            if (!col.isLand() || Builds.blocksPlants(x, z)) {
                return;
            }
            int top = col.top();
            BlockState ground = c.get(x, top, z);
            double r = Noise.rand(x, z, 31);
            if (ground.is(Blocks.GRASS_BLOCK) && r < 0.14) {
                c.set(x, top + 1, z, s(r < 0.02 ? Blocks.FERN : Blocks.SHORT_GRASS));
            } else if ((ground.is(Blocks.COARSE_DIRT) || ground.is(Blocks.PODZOL)) && r < 0.04) {
                c.set(x, top + 1, z, s(Blocks.DEAD_BUSH));
            } else if (col.island().style() == Style.JUNGLE && ground.is(Blocks.GRASS_BLOCK) && r < 0.3) {
                c.set(x, top + 1, z, s(r < 0.04 ? Blocks.BAMBOO : r < 0.08 ? Blocks.MELON : r < 0.2 ? Blocks.FERN : Blocks.SHORT_GRASS));
            } else if (ground.is(Blocks.SAND) && col.island().style() == Style.SANDY && r < 0.01) {
                c.set(x, top + 1, z, s(Blocks.SUGAR_CANE));
            } else if (col.island().style() == Style.DESERT && (ground.is(Blocks.SAND) || ground.is(Blocks.RED_SAND))
                    && top > Archipelago.WATER_TOP + 2) {
                if (r < 0.012) {
                    int tall = 1 + (int) (Noise.rand(x, z, 32) * 3);
                    for (int i = 1; i <= tall; i++) c.set(x, top + i, z, s(Blocks.CACTUS));
                } else if (r < 0.04) {
                    c.set(x, top + 1, z, s(Blocks.DEAD_BUSH));
                }
            } else if (col.island().style() == Style.ATOLL && ground.is(Blocks.SOUL_SAND) && r < 0.06) {
                c.set(x, top + 1, z, s(Blocks.WITHER_ROSE));
            }
        });

        // Trees: one candidate per 9x9 cell, scanning neighbours so crowns cross chunk edges.
        int gx1 = Math.floorDiv(c.minX - 5, TREE_CELL), gx2 = Math.floorDiv(c.maxX + 5, TREE_CELL);
        int gz1 = Math.floorDiv(c.minZ - 5, TREE_CELL), gz2 = Math.floorDiv(c.maxZ + 5, TREE_CELL);
        for (int gx = gx1; gx <= gx2; gx++) {
            for (int gz = gz1; gz <= gz2; gz++) {
                long h = Noise.hash(gx, 0, gz, 4242);
                int x = gx * TREE_CELL + (int) ((h >>> 3) % TREE_CELL + TREE_CELL) % TREE_CELL;
                int z = gz * TREE_CELL + (int) ((h >>> 11) % TREE_CELL + TREE_CELL) % TREE_CELL;
                Column col = Archipelago.column(x, z);
                boolean bayou = col.island() != null && col.island().style() == Style.BAYOU;
                if (!col.isLand() || (!bayou && col.top() < Archipelago.WATER_TOP + 2) || Builds.blocksPlants(x, z)) {
                    continue;
                }
                double chance = Noise.rand(gx, gz, 4243);
                switch (col.island().style()) {
                    case SANDY -> { if (chance < 0.6) palm(c, x, col.top() + 1, z, h); }
                    case TOWN -> { if (chance < 0.3) palm(c, x, col.top() + 1, z, h); }
                    case CURSED -> { if (chance < 0.55) deadTree(c, x, col.top() + 1, z, h); }
                    case BAYOU -> { if (chance < 0.7) mangrove(c, x, col.top() + 1, z, h); }
                    case JUNGLE -> { if (chance < 0.85) jungleTree(c, x, col.top() + 1, z, h); }
                    case KEYS -> { if (chance < 0.4) palm(c, x, col.top() + 1, z, h); }
                    case ATOLL -> { if (chance < 0.2) palm(c, x, col.top() + 1, z, h); else if (chance < 0.35) deadTree(c, x, col.top() + 1, z, h); }
                    case ANCIENT -> { if (chance < 0.3) jungleTree(c, x, col.top() + 1, z, h); }
                    case CLIFFS -> { if (chance < 0.12 && col.top() > 80) deadTree(c, x, col.top() + 1, z, h); }
                    default -> { }
                }
            }
        }
    }

    private static void palm(Canvas c, int x, int y, int z, long h) {
        int height = 5 + (int) ((h >>> 20) & 3);
        Direction lean = Direction.Plane.HORIZONTAL.stream().toList().get((int) ((h >>> 24) & 3));
        BlockState log = s(Blocks.JUNGLE_LOG);
        BlockState leaves = s(Blocks.JUNGLE_LEAVES).setValue(LeavesBlock.PERSISTENT, true);
        int tx = x, tz = z;
        for (int i = 0; i < height; i++) {
            if (i == height / 2 + 1) {
                tx += lean.getStepX();
                tz += lean.getStepZ();
            }
            c.set(tx, y + i, tz, log);
        }
        int ty = y + height;
        c.set(tx, ty, tz, leaves);
        for (Direction d : Direction.Plane.HORIZONTAL) {
            for (int k = 1; k <= 3; k++) {
                int yy = k == 3 ? ty - 1 : ty;
                c.set(tx + d.getStepX() * k, yy, tz + d.getStepZ() * k, leaves);
            }
            Direction side = d.getClockWise();
            c.set(tx + d.getStepX() + side.getStepX(), ty, tz + d.getStepZ() + side.getStepZ(), leaves);
        }
        c.set(tx, ty + 1, tz, leaves);
        // A coconut or two
        if (((h >>> 30) & 1) == 0) {
            c.set(tx + 1, ty - 1, tz, s(Blocks.COCOA).setValue(net.minecraft.world.level.block.CocoaBlock.FACING, Direction.WEST)
                    .setValue(net.minecraft.world.level.block.CocoaBlock.AGE, 2));
        }
    }

    /** A tall jungle tree with buttressed roots, a layered canopy and hanging vines. */
    private static void jungleTree(Canvas c, int x, int y, int z, long h) {
        BlockState log = s(Blocks.JUNGLE_LOG);
        BlockState leaves = s(Blocks.JUNGLE_LEAVES).setValue(LeavesBlock.PERSISTENT, true);
        int height = 9 + (int) ((h >>> 20) & 7);
        c.fill(x, y, z, x, y + height, z, log);
        for (Direction d : Direction.Plane.HORIZONTAL) {
            c.set(x + d.getStepX(), y, z + d.getStepZ(), log); // buttress roots
        }
        for (int layer = 0; layer < 2; layer++) {
            int ly = y + height - layer * 4;
            int radius = 3 + layer;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dz * dz <= radius * radius && Noise.rand(x + dx, ly, z + dz, 51) < 0.9) {
                        c.set(x + dx, ly, z + dz, leaves);
                        if (dx * dx + dz * dz <= (radius - 1) * (radius - 1)) c.set(x + dx, ly + 1, z + dz, leaves);
                    }
                }
            }
        }
        // a few vines and a cocoa pod
        if (((h >>> 26) & 1) == 0) {
            c.set(x + 1, y + 3, z, s(Blocks.COCOA).setValue(net.minecraft.world.level.block.CocoaBlock.FACING, Direction.WEST)
                    .setValue(net.minecraft.world.level.block.CocoaBlock.AGE, 2));
        }
        for (int i = 1; i <= 3; i++) {
            c.set(x - 3, y + height - i, z, s(Blocks.VINE).setValue(net.minecraft.world.level.block.VineBlock.EAST, true));
        }
    }

    /** Mangrove on a tangle of roots, with a broad low canopy. */
    private static void mangrove(Canvas c, int x, int y, int z, long h) {
        BlockState roots = s(Blocks.MANGROVE_ROOTS);
        BlockState muddy = s(Blocks.MUDDY_MANGROVE_ROOTS);
        BlockState log = s(Blocks.MANGROVE_LOG);
        BlockState leaves = s(Blocks.MANGROVE_LEAVES).setValue(LeavesBlock.PERSISTENT, true);
        c.set(x, y - 1, z, muddy);
        for (Direction d : Direction.Plane.HORIZONTAL) {
            c.set(x + d.getStepX(), y, z + d.getStepZ(), roots);
            if (((h >>> (d.get2DDataValue() * 3)) & 1) == 0) {
                c.set(x + d.getStepX() * 2, y, z + d.getStepZ() * 2, roots);
            }
        }
        int height = 4 + (int) ((h >>> 20) & 3);
        c.fill(x, y, z, x, y + height, z, log);
        int top = y + height;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                int dist = dx * dx + dz * dz;
                if (dist <= 9 && Noise.rand(x + dx, top, z + dz, 41) < 0.85) c.set(x + dx, top, z + dz, leaves);
                if (dist <= 4) c.set(x + dx, top + 1, z + dz, leaves);
                if (dist >= 5 && dist <= 9 && Noise.rand(x + dx, top - 1, z + dz, 42) < 0.25) {
                    c.set(x + dx, top - 1, z + dz, leaves); // drooping fringe
                }
            }
        }
        c.set(x, top, z, log);
    }

    private static void deadTree(Canvas c, int x, int y, int z, long h) {
        int height = 4 + (int) ((h >>> 20) & 3);
        BlockState log = s(Blocks.DARK_OAK_LOG);
        for (int i = 0; i < height; i++) {
            c.set(x, y + i, z, log);
        }
        Direction a = Direction.Plane.HORIZONTAL.stream().toList().get((int) ((h >>> 24) & 3));
        Direction b = a.getOpposite();
        int by = y + height - 2;
        c.set(x + a.getStepX(), by, z + a.getStepZ(), log.setValue(RotatedPillarBlock.AXIS, a.getAxis()));
        c.set(x + a.getStepX() * 2, by + 1, z + a.getStepZ() * 2, log);
        c.set(x + b.getStepX(), by + 1, z + b.getStepZ(), log.setValue(RotatedPillarBlock.AXIS, b.getAxis()));
        if (((h >>> 28) & 3) == 0) {
            c.set(x + b.getStepX(), by, z + b.getStepZ(), s(Blocks.COBWEB));
        }
    }
}
