package com.cursedtides.living;

import com.cursedtides.trade.Ports;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.Nullable;

/**
 * How well each port is doing, 0 to 100. Trade, defended invasions and finished jobs lift a town; raids, blockades and
 * the cursed coming ashore drag it down. A thriving port sells cheaper and talks happier; a struggling one pays more
 * for what it lacks. Everything drifts back toward 50 over the days.
 */
public class Prosperity extends PersistentState {
    private final int[] values = new int[Ports.ALL.size()];

    private Prosperity() {
        java.util.Arrays.fill(values, 50);
    }

    private static final Type<Prosperity> TYPE = new Type<>(Prosperity::new, Prosperity::fromNbt, null);

    public static Prosperity get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, "cursedtides_prosperity");
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        nbt.putIntArray("Values", values);
        return nbt;
    }

    private static Prosperity fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        Prosperity p = new Prosperity();
        int[] saved = nbt.getIntArray("Values");
        System.arraycopy(saved, 0, p.values, 0, Math.min(saved.length, p.values.length));
        return p;
    }

    @Nullable
    public static Ports.Port nearestPort(int x, int z, int within) {
        Ports.Port best = null;
        double bestD = (double) within * within;
        for (Ports.Port port : Ports.ALL) {
            double dx = port.island().cx() - x, dz = port.island().cz() - z;
            double d = dx * dx + dz * dz;
            if (d < bestD) { bestD = d; best = port; }
        }
        return best;
    }

    public static int at(@Nullable MinecraftServer server, int x, int z) {
        if (server == null) return 50;
        Ports.Port port = nearestPort(x, z, 160);
        return port == null ? 50 : get(server).values[Ports.ALL.indexOf(port)];
    }

    public static int of(MinecraftServer server, Ports.Port port) {
        int i = Ports.ALL.indexOf(port);
        return i < 0 ? 50 : get(server).values[i];
    }

    public static String mood(@Nullable MinecraftServer server, int x, int z) {
        int v = at(server, x, z);
        return v >= 80 ? "booming" : v >= 60 ? "doing well" : v >= 40 ? "getting by" : v >= 20 ? "struggling" : "on its knees";
    }

    /** Price multiplier for what a port sells: thriving towns are cheaper, struggling ones dearer. */
    public static double priceFactor(MinecraftServer server, Ports.Port port) {
        return 1.0 + (50 - of(server, port)) / 250.0;
    }

    public static void adjust(MinecraftServer server, int x, int z, int delta, @Nullable String why) {
        Ports.Port port = nearestPort(x, z, 160);
        if (port == null || server == null) return;
        Prosperity s = get(server);
        int i = Ports.ALL.indexOf(port);
        int before = s.values[i];
        s.values[i] = Math.max(0, Math.min(100, before + delta));
        s.markDirty();
        if (why != null && before / 20 != s.values[i] / 20) {
            server.getPlayerManager().broadcast(Text.literal(port.island().name() + " is " + mood(server, port.island().cx(), port.island().cz())
                    + " (" + why + ").").formatted(delta > 0 ? Formatting.GREEN : Formatting.RED), false);
        }
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 24000 != 0 || server.getTicks() == 0) return;
            Prosperity s = get(server);
            for (int i = 0; i < s.values.length; i++) {
                if (s.values[i] > 50) s.values[i] -= 2;
                else if (s.values[i] < 50) s.values[i] += 2;
            }
            s.markDirty();
        });
    }
}
