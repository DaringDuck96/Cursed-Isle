package com.cursedtides.living;

import com.cursedtides.world.Archipelago;
import com.cursedtides.world.Builds;
import com.cursedtides.world.Canvas;
import com.cursedtides.world.ModWorld;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LanternBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameRules;

import java.util.ArrayList;
import java.util.List;

/**
 * Defeat, the old way: you keep everything you carried and wake up, patched and whole, in the nearest port's
 * gaol. The cell door hangs open. Walk out and go back for another try.
 */
public final class Gaols {
    private Gaols() {}

    public record Gaol(Archipelago.Island island, int x, int y, int z) {}

    private static final Archipelago.Island[] PORTS = {
            Archipelago.DRIFTWOOD_ISLAND, Archipelago.SALTMARROW, Archipelago.PORT_MERIDIAN, Archipelago.KINGSPORT,
            Archipelago.SAINT_VESPER, Archipelago.IRONWATCH_ISLE, Archipelago.BLACKTHORN_ISLE, Archipelago.GOLDWATER_CAY,
            Archipelago.RAVENS_LANDING, Archipelago.CROWNWATCH};

    private static volatile List<Gaol> built;

    public static List<Gaol> all() {
        if (built == null) Builds.groundY(0, 0);
        return built == null ? List.of() : built;
    }

    /** Called at the end of Builds' setup. */
    public static synchronized void addBuilds(List<Builds.Build> out) {
        if (built != null) return;
        List<Gaol> list = new ArrayList<>();
        for (Archipelago.Island is : PORTS) {
            int[] at = com.cursedtides.dungeon.Caverns.findMouth(is);
            Gaol g = new Gaol(is, at[0], Builds.groundY(at[0], at[1]), at[1]);
            list.add(g);
            out.add(new Builds.Build(at[0] - 5, at[1] - 4, at[0] + 5, at[1] + 4, true, c -> place(c, g)));
        }
        built = List.copyOf(list);
    }

    private static BlockState s(net.minecraft.block.Block b) {
        return b.getDefaultState();
    }

    /** A squat stone gaolhouse: a barred cell (door gone), a guard's corner, a door out. */
    private static void place(Canvas c, Gaol g) {
        int x = g.x(), y = g.y(), z = g.z();
        c.foundation(x - 4, z - 3, x + 4, z + 3, y, s(Blocks.COBBLESTONE));
        c.fill(x - 4, y - 1, z - 3, x + 4, y - 1, z + 3, s(Blocks.STONE_BRICKS));
        c.fill(x - 4, y, z - 3, x + 4, y + 3, z + 3, s(Blocks.AIR));
        c.walls(x - 4, y, z - 3, x + 4, y + 3, z + 3, s(Blocks.STONE_BRICKS));
        c.fill(x - 4, y + 4, z - 3, x + 4, y + 4, z + 3, s(Blocks.STONE_BRICK_SLAB));
        for (int[] k : new int[][]{{-4, -3}, {-4, 3}, {4, -3}, {4, 3}}) {
            c.fill(x + k[0], y, z + k[1], x + k[0], y + 3, z + k[1], s(Blocks.MOSSY_STONE_BRICKS));
        }
        BlockState barsX = s(Blocks.IRON_BARS).with(PaneBlock.NORTH, true).with(PaneBlock.SOUTH, true);
        // The cell's bars, with the door long gone
        for (int dz = -2; dz <= 2; dz++) {
            if (dz == 0) continue;
            c.fill(x, y, z + dz, x, y + 2, z + dz, barsX);
        }
        c.set(x, y + 3, z, s(Blocks.STONE_BRICKS));
        // Windows
        BlockState barsZ = s(Blocks.IRON_BARS).with(PaneBlock.EAST, true).with(PaneBlock.WEST, true);
        c.set(x - 2, y + 2, z - 3, barsZ);
        c.set(x - 2, y + 2, z + 3, barsZ);
        c.set(x + 2, y + 2, z - 3, barsZ);
        // The way out
        c.fill(x + 4, y, z, x + 4, y + 1, z, s(Blocks.AIR));
        c.set(x + 5, y - 1, z, s(Blocks.COBBLESTONE));
        // Cell furnishings
        c.set(x - 3, y, z - 2, s(Blocks.HAY_BLOCK));
        c.set(x - 3, y, z + 2, s(Blocks.CAULDRON));
        c.set(x - 2, y + 3, z, s(Blocks.LANTERN).with(LanternBlock.HANGING, true));
        // The gaoler's corner
        c.set(x + 3, y, z - 2, s(Blocks.BARREL));
        c.set(x + 2, y, z - 2, s(Blocks.SPRUCE_STAIRS));
        c.set(x + 3, y + 3, z + 2, s(Blocks.LANTERN).with(LanternBlock.HANGING, true));
    }

    public static Gaol nearest(double x, double z) {
        Gaol best = null;
        double bd = Double.MAX_VALUE;
        for (Gaol g : all()) {
            double d = (g.x() - x) * (g.x() - x) + (g.z() - z) * (g.z() - z);
            if (d < bd) { bd = d; best = g; }
        }
        return best;
    }

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (ModWorld.isArchipelago() && !server.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
                server.getGameRules().get(GameRules.KEEP_INVENTORY).set(true, server);
                com.cursedtides.CursedTides.LOGGER.info("Cursed Tides: defeated pirates keep their belongings (keepInventory on)");
            }
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldP, p, alive) -> {
            if (alive || !ModWorld.isArchipelago()) return;
            ServerWorld overworld = p.getServer().getOverworld();
            if (oldP.getWorld() != overworld) return;
            Gaol g = nearest(oldP.getX(), oldP.getZ());
            if (g == null) return;
            p.teleport(overworld, g.x() - 1.5, g.y(), g.z() + 0.5, 270f, 0f);
            p.setHealth(p.getMaxHealth());
            p.sendMessage(Text.literal("Defeated! You come to in the " + g.island().name() + " gaol, patched up and none the poorer. "
                    + "The cell door hangs open.").formatted(Formatting.GOLD), false);
        });
    }
}
