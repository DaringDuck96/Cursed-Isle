package com.cursedtides.home;

import net.minecraft.world.item.DyeColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;

/** The buildings a deed raises. Each faces south (door on the +z side); width along x, depth along z. */
public final class Blueprints {
    private Blueprints() {}

    public record Size(int w, int d, int h) {}

    public static Size size(Homes.Kind kind) {
        return switch (kind) {
            case HOUSE -> new Size(9, 7, 6);
            case HIDEOUT -> new Size(7, 7, 5);
            case TAVERN -> new Size(13, 9, 7);
            case HQ -> new Size(15, 11, 8);
        };
    }

    /** Builds with its north-west floor corner at origin. Returns the door position. */
    public static BlockPos build(ServerLevel w, Homes.Kind kind, BlockPos o) {
        Size s = size(kind);
        BlockState floor, wall, corner, roof;
        switch (kind) {
            case HIDEOUT -> { floor = s(Blocks.COBBLESTONE); wall = s(Blocks.MOSSY_COBBLESTONE); corner = s(Blocks.STONE_BRICKS); roof = s(Blocks.MOSSY_STONE_BRICKS); }
            case TAVERN -> { floor = s(Blocks.SPRUCE_PLANKS); wall = s(Blocks.OAK_PLANKS); corner = s(Blocks.STRIPPED_OAK_LOG); roof = s(Blocks.DARK_OAK_PLANKS); }
            case HQ -> { floor = s(Blocks.POLISHED_ANDESITE); wall = s(Blocks.STONE_BRICKS); corner = s(Blocks.DARK_OAK_LOG); roof = s(Blocks.DEEPSLATE_TILES); }
            default -> { floor = s(Blocks.SPRUCE_PLANKS); wall = s(Blocks.BIRCH_PLANKS); corner = s(Blocks.SPRUCE_LOG); roof = s(Blocks.SPRUCE_PLANKS); }
        }
        int x0 = o.getX(), y0 = o.getY(), z0 = o.getZ();
        int x1 = x0 + s.w() - 1, z1 = z0 + s.d() - 1;
        // Foundation down to the ground, cleared space above
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                for (int y = y0 - 1; y >= y0 - 6; y--) {
                    BlockPos p = new BlockPos(x, y, z);
                    if (!w.getBlockState(p).canBeReplaced() && !w.getBlockState(p).is(Blocks.WATER)) break;
                    w.setBlockAndUpdate(p, s(Blocks.COBBLESTONE));
                }
                w.setBlockAndUpdate(new BlockPos(x, y0 - 1, z), floor);
                for (int y = y0; y < y0 + s.h() + 3; y++) w.setBlockAndUpdate(new BlockPos(x, y, z), s(Blocks.AIR));
            }
        }
        int wallTop = y0 + s.h() - 2;
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                boolean edgeX = x == x0 || x == x1, edgeZ = z == z0 || z == z1;
                if (!(edgeX || edgeZ)) continue;
                for (int y = y0; y <= wallTop; y++) {
                    boolean window = y == y0 + 1 && !(edgeX && edgeZ) && ((edgeZ && (x - x0) % 3 == 1) || (edgeX && (z - z0) % 3 == 1));
                    BlockState st = edgeX && edgeZ ? corner : window ? (kind == Homes.Kind.HIDEOUT ? s(Blocks.IRON_BARS) : s(Blocks.GLASS_PANE)) : wall;
                    w.setBlockAndUpdate(new BlockPos(x, y, z), st);
                }
            }
        }
        // Roof: a flat deck with a stepped slab rim
        for (int x = x0 - 1; x <= x1 + 1; x++) {
            for (int z = z0 - 1; z <= z1 + 1; z++) {
                boolean rim = x == x0 - 1 || x == x1 + 1 || z == z0 - 1 || z == z1 + 1;
                w.setBlockAndUpdate(new BlockPos(x, wallTop + 1, z), rim ? slab(roof) : roof);
            }
        }
        // Door, centred on the south wall
        int dx = x0 + s.w() / 2;
        BlockPos door = new BlockPos(dx, y0, z1);
        BlockState d = (kind == Homes.Kind.HIDEOUT ? Blocks.SPRUCE_DOOR : kind == Homes.Kind.HQ ? Blocks.DARK_OAK_DOOR : Blocks.OAK_DOOR)
                .defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH);
        w.setBlockAndUpdate(door, d.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        w.setBlockAndUpdate(door.above(), d.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        w.setBlockAndUpdate(door.above(2), corner);
        w.setBlockAndUpdate(new BlockPos(dx + 1, y0 + 1, z1 + 1), s(Blocks.LANTERN));
        // Light inside
        for (int[] l : new int[][]{{x0 + 1, z0 + 1}, {x1 - 1, z0 + 1}, {x0 + s.w() / 2, z0 + s.d() / 2}}) {
            w.setBlockAndUpdate(new BlockPos(l[0], wallTop, l[1]), s(Blocks.LANTERN).setValue(LanternBlock.HANGING, true));
        }
        furnish(w, kind, x0, y0, z0, x1, z1);
        return door;
    }

    private static void furnish(ServerLevel w, Homes.Kind kind, int x0, int y0, int z0, int x1, int z1) {
        switch (kind) {
            case HOUSE, HIDEOUT, HQ -> {
                bed(w, new BlockPos(x0 + 1, y0, z0 + 1));
                if (kind == Homes.Kind.HQ) {
                    bed(w, new BlockPos(x0 + 3, y0, z0 + 1));
                    bed(w, new BlockPos(x0 + 5, y0, z0 + 1));
                    w.setBlockAndUpdate(new BlockPos(x0 + 7, y0, z0 + 5), s(Blocks.CARTOGRAPHY_TABLE));
                    w.setBlockAndUpdate(new BlockPos(x0 + 8, y0, z0 + 5), s(Blocks.LECTERN));
                    for (int x = x0 + 5; x <= x0 + 9; x++) w.setBlockAndUpdate(new BlockPos(x, y0, z0 + 7), s(Blocks.DARK_OAK_FENCE));
                    w.setBlockAndUpdate(new BlockPos(x1 - 1, y0, z1 - 2), s(Blocks.SMITHING_TABLE));
                    w.setBlockAndUpdate(new BlockPos(x1 - 1, y0, z1 - 3), s(Blocks.ANVIL));
                }
                w.setBlockAndUpdate(new BlockPos(x1 - 1, y0, z0 + 1), s(Blocks.CHEST).setValue(ChestBlock.FACING, Direction.SOUTH));
                w.setBlockAndUpdate(new BlockPos(x1 - 2, y0, z0 + 1), s(Blocks.CHEST).setValue(ChestBlock.FACING, Direction.SOUTH));
                w.setBlockAndUpdate(new BlockPos(x1 - 1, y0, z0 + 3), s(Blocks.CRAFTING_TABLE));
                w.setBlockAndUpdate(new BlockPos(x1 - 1, y0, z0 + 4), s(Blocks.FURNACE));
                w.setBlockAndUpdate(new BlockPos(x0 + 1, y0, z1 - 1), s(Blocks.BARREL));
                if (kind == Homes.Kind.HIDEOUT) w.setBlockAndUpdate(new BlockPos(x0 + 3, y0, z0 + 3), s(Blocks.CARTOGRAPHY_TABLE));
            }
            case TAVERN -> {
                // The bar along the back wall, barrels behind it
                for (int x = x0 + 2; x <= x1 - 2; x++) {
                    w.setBlockAndUpdate(new BlockPos(x, y0, z0 + 2), s(Blocks.STRIPPED_SPRUCE_LOG));
                    w.setBlockAndUpdate(new BlockPos(x, y0, z0 + 1), (x % 2 == 0 ? Blocks.BARREL : Blocks.CHEST).defaultBlockState());
                }
                // Tables with stools
                for (int[] t : new int[][]{{x0 + 3, z0 + 5}, {x1 - 3, z0 + 5}, {x0 + 6, z1 - 2}}) {
                    w.setBlockAndUpdate(new BlockPos(t[0], y0, t[1]), s(Blocks.OAK_FENCE));
                    w.setBlockAndUpdate(new BlockPos(t[0], y0 + 1, t[1]), s(Blocks.OAK_PRESSURE_PLATE));
                    w.setBlockAndUpdate(new BlockPos(t[0] - 1, y0, t[1]), s(Blocks.SPRUCE_STAIRS).setValue(StairBlock.FACING, Direction.WEST));
                    w.setBlockAndUpdate(new BlockPos(t[0] + 1, y0, t[1]), s(Blocks.SPRUCE_STAIRS).setValue(StairBlock.FACING, Direction.EAST));
                }
                w.setBlockAndUpdate(new BlockPos(x0 + 1, y0, z1 - 1), s(Blocks.JUKEBOX));
                w.setBlockAndUpdate(new BlockPos(x1 - 1, y0, z1 - 1), s(Blocks.CAMPFIRE));
            }
        }
    }

    private static void bed(ServerLevel w, BlockPos foot) {
        BlockState bed = Blocks.BED.pick(DyeColor.RED).defaultBlockState().setValue(BedBlock.FACING, Direction.NORTH);
        w.setBlockAndUpdate(foot.south(), bed.setValue(BedBlock.PART, BedPart.FOOT));
        w.setBlockAndUpdate(foot, bed.setValue(BedBlock.PART, BedPart.HEAD));
    }

    private static BlockState slab(BlockState full) {
        if (full.is(Blocks.SPRUCE_PLANKS)) return s(Blocks.SPRUCE_SLAB).setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (full.is(Blocks.DARK_OAK_PLANKS)) return s(Blocks.DARK_OAK_SLAB);
        if (full.is(Blocks.DEEPSLATE_TILES)) return s(Blocks.DEEPSLATE_TILE_SLAB);
        return s(Blocks.MOSSY_STONE_BRICK_SLAB);
    }

    private static BlockState s(net.minecraft.world.level.block.Block b) {
        return b.defaultBlockState();
    }
}
