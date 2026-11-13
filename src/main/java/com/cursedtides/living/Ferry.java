package com.cursedtides.living;

import com.cursedtides.faction.FactionData;
import com.cursedtides.trade.Ports;
import com.cursedtides.trade.Wallet;
import com.cursedtides.world.Archipelago;
import com.cursedtides.world.ModWorld;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/** The packet boats: from any port, buy passage to any other port you've already found. /ferry, /ferry <port>. */
public final class Ferry {
    private Ferry() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((d, a, e) -> d.register(literal("ferry")
                .executes(ctx -> { list(ctx.getSource().getPlayerOrException()); return 1; })
                .then(argument("port", StringArgumentType.greedyString()).suggests((c, b) -> {
                    for (Ports.Port p : Ports.ALL) b.suggest(p.island().name().split(",")[0].replace(' ', '_'));
                    return b.buildFuture();
                }).executes(ctx -> sail(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "port"))))));
    }

    private static boolean known(ServerPlayer p, Ports.Port port) {
        int i = Archipelago.ISLANDS.indexOf(port.island());
        return (p.getAttachedOrElse(ModWorld.DISCOVERED, 0) & (1 << i)) != 0;
    }

    private static void list(ServerPlayer p) {
        Ports.Port here = Prosperity.nearestPort(p.getBlockX(), p.getBlockZ(), 90);
        p.sendSystemMessage(Component.literal(here == null ? "Packet boats sail from any port. Go to one first." : "Passage from " + here.island().name() + ":")
                .withStyle(ChatFormatting.GOLD), false);
        if (here == null) return;
        for (Ports.Port port : Ports.ALL) {
            if (port == here || !known(p, port)) continue;
            p.sendSystemMessage(Component.literal("  " + port.island().name() + " - " + Wallet.format(fare(here, port))).withStyle(ChatFormatting.WHITE), false);
        }
        p.sendSystemMessage(Component.literal("  /ferry <port>").withStyle(ChatFormatting.DARK_GRAY), false);
    }

    private static int fare(Ports.Port a, Ports.Port b) {
        double d = Math.hypot(a.island().cx() - b.island().cx(), a.island().cz() - b.island().cz());
        return 20 + (int) (d / 6);
    }

    private static int sail(ServerPlayer p, String name) {
        Ports.Port here = Prosperity.nearestPort(p.getBlockX(), p.getBlockZ(), 90);
        if (here == null) return fail(p, "You'll need to be in a port to catch the packet boat.");
        if (FactionData.isWanted(p)) return fail(p, "No packet captain will carry a wanted pirate.");
        if (p.isPassenger()) return fail(p, "Step off your ship first.");
        String want = name.replace('_', ' ').toLowerCase();
        Ports.Port dest = null;
        for (Ports.Port port : Ports.ALL) if (port.island().name().toLowerCase().startsWith(want)) dest = port;
        if (dest == null || dest == here) return fail(p, "No packet sails there from here.");
        if (!known(p, dest)) return fail(p, "You'll have to find " + dest.island().name() + " yourself before you can book passage.");
        int fare = fare(here, dest);
        if (!Wallet.pay(p, fare)) return fail(p, "Passage is " + Wallet.format(fare) + ".");
        var w = p.level();
        BlockPos at = Residents.openGround(w, dest.island().cx() + 4, dest.island().cz() + 4, 14);
        if (at == null) at = new BlockPos(dest.island().cx(), com.cursedtides.world.Builds.groundY(dest.island().cx(), dest.island().cz()), dest.island().cz());
        com.cursedtides.util.Compat.teleport(p, w, at.getX() + 0.5, at.getY(), at.getZ() + 0.5, p.getYRot(), p.getXRot());
        p.sendSystemMessage(Component.literal("A day and a night on the packet boat, and you step ashore at " + dest.island().name() + ".").withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private static int fail(ServerPlayer p, String msg) {
        p.sendSystemMessage(Component.literal(msg).withStyle(ChatFormatting.RED), false);
        return 0;
    }
}
