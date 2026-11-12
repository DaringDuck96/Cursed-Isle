package com.cursedtides.living;

import com.cursedtides.faction.FactionData;
import com.cursedtides.trade.Ports;
import com.cursedtides.trade.Wallet;
import com.cursedtides.world.Archipelago;
import com.cursedtides.world.ModWorld;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/** The packet boats: from any port, buy passage to any other port you've already found. /ferry, /ferry <port>. */
public final class Ferry {
    private Ferry() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((d, a, e) -> d.register(literal("ferry")
                .executes(ctx -> { list(ctx.getSource().getPlayerOrThrow()); return 1; })
                .then(argument("port", StringArgumentType.greedyString()).suggests((c, b) -> {
                    for (Ports.Port p : Ports.ALL) b.suggest(p.island().name().split(",")[0].replace(' ', '_'));
                    return b.buildFuture();
                }).executes(ctx -> sail(ctx.getSource().getPlayerOrThrow(), StringArgumentType.getString(ctx, "port"))))));
    }

    private static boolean known(ServerPlayerEntity p, Ports.Port port) {
        int i = Archipelago.ISLANDS.indexOf(port.island());
        return (p.getAttachedOrElse(ModWorld.DISCOVERED, 0) & (1 << i)) != 0;
    }

    private static void list(ServerPlayerEntity p) {
        Ports.Port here = Prosperity.nearestPort(p.getBlockX(), p.getBlockZ(), 90);
        p.sendMessage(Text.literal(here == null ? "Packet boats sail from any port. Go to one first." : "Passage from " + here.island().name() + ":")
                .formatted(Formatting.GOLD), false);
        if (here == null) return;
        for (Ports.Port port : Ports.ALL) {
            if (port == here || !known(p, port)) continue;
            p.sendMessage(Text.literal("  " + port.island().name() + " - " + Wallet.format(fare(here, port))).formatted(Formatting.WHITE), false);
        }
        p.sendMessage(Text.literal("  /ferry <port>").formatted(Formatting.DARK_GRAY), false);
    }

    private static int fare(Ports.Port a, Ports.Port b) {
        double d = Math.hypot(a.island().cx() - b.island().cx(), a.island().cz() - b.island().cz());
        return 20 + (int) (d / 6);
    }

    private static int sail(ServerPlayerEntity p, String name) {
        Ports.Port here = Prosperity.nearestPort(p.getBlockX(), p.getBlockZ(), 90);
        if (here == null) return fail(p, "You'll need to be in a port to catch the packet boat.");
        if (FactionData.isWanted(p)) return fail(p, "No packet captain will carry a wanted pirate.");
        if (p.hasVehicle()) return fail(p, "Step off your ship first.");
        String want = name.replace('_', ' ').toLowerCase();
        Ports.Port dest = null;
        for (Ports.Port port : Ports.ALL) if (port.island().name().toLowerCase().startsWith(want)) dest = port;
        if (dest == null || dest == here) return fail(p, "No packet sails there from here.");
        if (!known(p, dest)) return fail(p, "You'll have to find " + dest.island().name() + " yourself before you can book passage.");
        int fare = fare(here, dest);
        if (!Wallet.pay(p, fare)) return fail(p, "Passage is " + Wallet.format(fare) + ".");
        var w = p.getServerWorld();
        BlockPos at = Residents.openGround(w, dest.island().cx() + 4, dest.island().cz() + 4, 14);
        if (at == null) at = new BlockPos(dest.island().cx(), com.cursedtides.world.Builds.groundY(dest.island().cx(), dest.island().cz()), dest.island().cz());
        p.teleport(w, at.getX() + 0.5, at.getY(), at.getZ() + 0.5, p.getYaw(), p.getPitch());
        p.sendMessage(Text.literal("A day and a night on the packet boat, and you step ashore at " + dest.island().name() + ".").formatted(Formatting.GOLD), false);
        return 1;
    }

    private static int fail(ServerPlayerEntity p, String msg) {
        p.sendMessage(Text.literal(msg).formatted(Formatting.RED), false);
        return 0;
    }
}
