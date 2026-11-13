package com.cursedtides.trade;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/**
 * Trading between players, with the goods held in escrow so nobody can be cheated:
 * /trade offer gives the held stack to the deal, /trade accept pays and takes it.
 * Also /pay for plain coin.
 */
public final class PlayerTrade {
    private PlayerTrade() {}

    private static final int EXPIRY = 20 * 90;

    private record Offer(UUID from, String fromName, ItemStack stack, int price, int expires) {}
    /** Keyed by the player the offer is made to. */
    private static final Map<UUID, Offer> OFFERS = new HashMap<>();
    /** Goods to hand back to sellers who were offline when their offer lapsed. */
    private static final Map<UUID, List<ItemStack>> RETURNS = new HashMap<>();

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> {
            dispatcher.register(literal("trade")
                    .then(literal("offer").then(argument("player", EntityArgument.player())
                            .then(argument("doubloons", IntegerArgumentType.integer(0, 100000)).executes(PlayerTrade::offer))))
                    .then(literal("accept").executes(PlayerTrade::accept))
                    .then(literal("decline").executes(PlayerTrade::decline)));
            dispatcher.register(literal("pay").then(argument("player", EntityArgument.player())
                    .then(argument("doubloons", IntegerArgumentType.integer(1, 100000)).executes(PlayerTrade::pay))));
        });
        ServerTickEvents.END_SERVER_TICK.register(PlayerTrade::tick);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            List<ItemStack> back = RETURNS.remove(handler.getPlayer().getUUID());
            if (back != null) {
                for (ItemStack s : back) giveBack(handler.getPlayer(), s);
                handler.getPlayer().sendSystemMessage(Component.literal("An offer you made lapsed while you were away; the goods are back in your pack.")
                        .withStyle(ChatFormatting.GRAY), false);
            }
        });
    }

    private static int offer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        if (target == p) return fail(p, "You can't trade with yourself.");
        if (p.distanceTo(target) > 16) return fail(p, "Get within 16 blocks to trade face to face.");
        if (OFFERS.containsKey(target.getUUID())) return fail(p, target.getName().getString() + " already has an offer to consider.");
        ItemStack held = p.getMainHandItem();
        if (held.isEmpty()) return fail(p, "Hold what you want to offer.");
        int price = IntegerArgumentType.getInteger(ctx, "doubloons");
        ItemStack escrow = held.copy();
        held.setCount(0);
        OFFERS.put(target.getUUID(), new Offer(p.getUUID(), p.getName().getString(), escrow, price,
                ctx.getSource().getServer().getTickCount() + EXPIRY));
        Component what = Component.literal(escrow.getCount() + " x ").append(escrow.getHoverName());
        p.sendSystemMessage(Component.literal("You offer ").append(what).append(Component.literal(" to " + target.getName().getString()
                + " for " + price + " doubloons.")).withStyle(ChatFormatting.GOLD), false);
        target.sendSystemMessage(Component.literal(p.getName().getString() + " offers you ").append(what).append(Component.literal(
                " for " + price + " doubloons. /trade accept or /trade decline")).withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private static int accept(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        Offer o = OFFERS.get(p.getUUID());
        if (o == null) return fail(p, "Nobody is offering you anything.");
        int silver = o.price() * Wallet.SILVER_PER_DOUBLOON;
        if (!Wallet.pay(p, silver)) return fail(p, "You can't afford " + o.price() + " doubloons.");
        OFFERS.remove(p.getUUID());
        giveBack(p, o.stack());
        ServerPlayer seller = ctx.getSource().getServer().getPlayerList().getPlayer(o.from());
        if (seller != null) {
            Wallet.give(seller, silver);
            seller.sendSystemMessage(Component.literal(p.getName().getString() + " accepted your offer. " + o.price() + " doubloons received.")
                    .withStyle(ChatFormatting.GOLD), false);
        } else {
            // The seller has gone; their coin waits at the auction house
            AuctionHouse.State s = AuctionHouse.State.get(ctx.getSource().getServer());
            s.payouts.merge(o.from(), silver, Integer::sum);
            s.setDirty();
        }
        p.sendSystemMessage(Component.literal("Done: you paid " + o.price() + " doubloons.").withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private static int decline(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        Offer o = OFFERS.remove(p.getUUID());
        if (o == null) return fail(p, "Nobody is offering you anything.");
        returnToSeller(ctx.getSource().getServer(), o, p.getName().getString() + " declined your offer.");
        p.sendSystemMessage(Component.literal("Offer declined.").withStyle(ChatFormatting.GRAY), false);
        return 1;
    }

    private static int pay(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        int amount = IntegerArgumentType.getInteger(ctx, "doubloons");
        if (target == p) return fail(p, "You count your own coins. Still there.");
        if (!Wallet.pay(p, amount * Wallet.SILVER_PER_DOUBLOON)) return fail(p, "You don't have " + amount + " doubloons.");
        Wallet.give(target, amount * Wallet.SILVER_PER_DOUBLOON);
        p.sendSystemMessage(Component.literal("Paid " + target.getName().getString() + " " + amount + " doubloons.").withStyle(ChatFormatting.GOLD), false);
        target.sendSystemMessage(Component.literal(p.getName().getString() + " paid you " + amount + " doubloons.").withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private static void tick(MinecraftServer server) {
        if (server.getTickCount() % 20 != 0 || OFFERS.isEmpty()) return;
        int now = server.getTickCount();
        List<UUID> lapsed = new ArrayList<>();
        OFFERS.forEach((to, o) -> { if (o.expires() < now) lapsed.add(to); });
        for (UUID to : lapsed) {
            Offer o = OFFERS.remove(to);
            returnToSeller(server, o, "Your offer lapsed; nobody took it.");
        }
    }

    private static void returnToSeller(MinecraftServer server, Offer o, String why) {
        ServerPlayer seller = server.getPlayerList().getPlayer(o.from());
        if (seller != null) {
            giveBack(seller, o.stack());
            seller.sendSystemMessage(Component.literal(why + " The goods are back in your pack.").withStyle(ChatFormatting.GRAY), false);
        } else {
            RETURNS.computeIfAbsent(o.from(), k -> new ArrayList<>()).add(o.stack());
        }
    }

    private static void giveBack(ServerPlayer p, ItemStack stack) {
        if (!p.getInventory().add(stack)) p.drop(stack, false, net.minecraft.util.Prediction.SERVER_ONLY);
    }

    private static int fail(ServerPlayer p, String msg) {
        p.sendSystemMessage(Component.literal(msg).withStyle(ChatFormatting.RED), false);
        return 0;
    }
}
