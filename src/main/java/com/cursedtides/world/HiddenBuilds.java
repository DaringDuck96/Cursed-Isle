package com.cursedtides.world;

import com.cursedtides.world.Archipelago.Island;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

/**
 * The hidden islands: the mist shrine on the Isle That Isn't, the throne hall of Kingsrest, the smugglers' cache in
 * Gullscream Rock, and the gun batteries that make the forts dangerous.
 */
public final class HiddenBuilds {
    private HiddenBuilds() {}

    static void addAll() {
        Island mist = Archipelago.ISLE_THAT_ISNT;
        Builds.add(mist.cx() - 8, mist.cz() - 8, mist.cx() + 8, mist.cz() + 8, c -> mistShrine(c, mist.cx(), mist.cz()));
        Island kr = Archipelago.KINGSREST;
        Builds.add(kr.cx() - 13, kr.cz() - 15, kr.cx() + 13, kr.cz() + 15, c -> throneHall(c, kr.cx(), kr.cz()));
        Island gr = Archipelago.GULLSCREAM_ROCK;
        Builds.add(gr.cx() - 6, gr.cz() - 6, gr.cx() + 6, gr.cz() + 6, c -> cache(c, gr.cx(), gr.cz()));
        for (Forts.Fort f : Forts.FORTS) {
            for (int[] b : f.batteries()) {
                Builds.add(b[0] - 2, b[1] - 2, b[0] + 2, b[1] + 2, c -> battery(c, b[0], b[1]));
            }
        }
    }

    private static BlockState s(net.minecraft.block.Block b) { return b.getDefaultState(); }

    /** A ring of standing stones in the mist, with a cracked pedestal in the middle. */
    private static void mistShrine(Canvas c, int cx, int cz) {
        int y = Builds.floorAt(cx, cz);
        c.fill(cx - 4, y - 1, cz - 4, cx + 4, y - 1, cz + 4, s(Blocks.MOSSY_COBBLESTONE));
        for (int i = 0; i < 8; i++) {
            double a = Math.PI * 2 * i / 8;
            int x = cx + (int) Math.round(Math.cos(a) * 6), z = cz + (int) Math.round(Math.sin(a) * 6);
            int fy = Builds.floorAt(x, z);
            int h = 2 + (i % 3);
            c.fill(x, fy, z, x, fy + h, z, s(i % 2 == 0 ? Blocks.MOSSY_STONE_BRICKS : Blocks.CRACKED_STONE_BRICKS));
        }
        c.set(cx, y, cz, s(Blocks.CHISELED_STONE_BRICKS));
        c.set(cx, y + 1, cz, s(Blocks.LODESTONE));
        c.set(cx + 1, y, cz, s(Blocks.SOUL_LANTERN));
        c.set(cx - 1, y, cz, s(Blocks.SOUL_LANTERN));
    }

    /** The Pretender's seat: a roofless hall of blackstone with a throne at the far end. */
    private static void throneHall(Canvas c, int cx, int cz) {
        int y = Builds.floorAt(cx, cz);
        int x1 = cx - 8, x2 = cx + 8, z1 = cz - 12, z2 = cz + 12;
        c.fill(x1, y - 1, z1, x2, y - 1, z2, s(Blocks.POLISHED_BLACKSTONE_BRICKS));
        c.fill(x1, y - 4, z1, x2, y - 2, z2, s(Blocks.BLACKSTONE));
        c.fill(x1 + 1, y, z1 + 1, x2 - 1, y + 7, z2 - 1, s(Blocks.AIR));
        for (int x = x1; x <= x2; x++) {
            for (int z = z1; z <= z2; z++) {
                boolean wall = x == x1 || x == x2 || z == z1 || z == z2;
                if (!wall) continue;
                boolean door = z == z2 && Math.abs(x - cx) <= 1;
                int h = 3 + (int) (Noise.rand(x, z, 5151) * 4);
                for (int yy = y; yy <= y + h; yy++) c.set(x, yy, z, door && yy <= y + 2 ? s(Blocks.AIR)
                        : s(Noise.rand(x, yy, z, 5153) < 0.25 ? Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS : Blocks.POLISHED_BLACKSTONE_BRICKS));
            }
        }
        // Pillars and a carpet down the middle
        for (int z = z1 + 3; z <= z2 - 3; z += 4) {
            c.fill(cx - 5, y, z, cx - 5, y + 6, z, s(Blocks.GILDED_BLACKSTONE));
            c.fill(cx + 5, y, z, cx + 5, y + 6, z, s(Blocks.GILDED_BLACKSTONE));
        }
        c.fill(cx - 1, y, z1 + 4, cx + 1, y, z2 - 1, s(Blocks.RED_CARPET));
        // The throne
        c.fill(cx - 2, y, z1 + 1, cx + 2, y, z1 + 3, s(Blocks.GOLD_BLOCK));
        c.set(cx, y + 1, z1 + 2, s(Blocks.POLISHED_BLACKSTONE_STAIRS).with(StairsBlock.FACING, Direction.NORTH));
        c.fill(cx - 1, y + 1, z1 + 1, cx + 1, y + 3, z1 + 1, s(Blocks.GILDED_BLACKSTONE));
        c.set(cx, y + 4, z1 + 1, s(Blocks.SOUL_LANTERN));
        c.chest(cx + 3, y, z1 + 1, Direction.SOUTH, Builds.TREASURE, new ItemStack(com.cursedtides.item.ModItems.ANCIENT_RELIC));
        for (int z = z1 + 2; z <= z2 - 2; z += 6) {
            c.set(x1 + 1, y + 3, z, s(Blocks.SOUL_LANTERN));
            c.set(x2 - 1, y + 3, z, s(Blocks.SOUL_LANTERN));
        }
    }

    /** A cleft in the rock, stacked with contraband. */
    private static void cache(Canvas c, int cx, int cz) {
        int y = Builds.floorAt(cx, cz);
        c.fill(cx - 3, y, cz - 3, cx + 3, y + 3, cz + 3, s(Blocks.AIR));
        c.fill(cx - 3, y - 1, cz - 3, cx + 3, y - 1, cz + 3, s(Blocks.SPRUCE_PLANKS));
        c.fill(cx - 4, y + 4, cz - 4, cx + 4, y + 4, cz + 4, s(Blocks.STONE));
        c.chest(cx - 2, y, cz - 2, Direction.SOUTH, Builds.TREASURE);
        c.chest(cx + 2, y, cz - 2, Direction.SOUTH, Builds.TREASURE, new ItemStack(com.cursedtides.trade.TradeGood.UNTAXED_RUM.item(), 8));
        c.set(cx, y, cz - 3, s(Blocks.BARREL));
        c.set(cx - 3, y, cz, s(Blocks.BARREL));
        c.set(cx + 3, y, cz, s(Blocks.BARREL));
        c.set(cx, y + 3, cz, s(Blocks.LANTERN).with(net.minecraft.block.LanternBlock.HANGING, true));
    }

    /** A gun emplacement: a stone platform and the gun itself. */
    private static void battery(Canvas c, int x, int z) {
        int y = Builds.floorAt(x, z);
        c.fill(x - 1, y - 1, z - 1, x + 1, y - 1, z + 1, s(Blocks.STONE_BRICKS));
        c.fill(x - 1, y, z - 1, x + 1, y + 2, z + 1, s(Blocks.AIR));
        c.set(x - 1, y, z - 1, s(Blocks.STONE_BRICK_WALL));
        c.set(x + 1, y, z - 1, s(Blocks.STONE_BRICK_WALL));
        c.set(x, y, z, s(Blocks.ANVIL));
        c.set(x, y, z + 1, s(Blocks.CHAIN));
    }
}
