package com.cursedtides.trade;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

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
                    .then(literal("offer").then(argument("player", EntityArgumentType.player())
                            .then(argument("doubloons", IntegerArgumentType.integer(0, 100000)).executes(PlayerTrade::offer))))
                    .then(literal("accept").executes(PlayerTrade::accept))
                    .then(literal("decline").executes(PlayerTrade::decline)));
            dispatcher.register(literal("pay").then(argument("player", EntityArgumentType.player())
                    .then(argument("doubloons", IntegerArgumentType.integer(1, 100000)).executes(PlayerTrade::pay))));
        });
        ServerTickEvents.END_SERVER_TICK.register(PlayerTrade::tick);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            List<ItemStack> back = RETURNS.remove(handler.getPlayer().getUuid());
            if (back != null) {
                for (ItemStack s : back) giveBack(handler.getPlayer(), s);
                handler.getPlayer().sendMessage(Text.literal("An offer you made lapsed while you were away; the goods are back in your pack.")
                        .formatted(Formatting.GRAY), false);
            }
        });
    }

    private static int offer(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
        if (target == p) return fail(p, "You can't trade with yourself.");
        if (p.distanceTo(target) > 16) return fail(p, "Get within 16 blocks to trade face to face.");
        if (OFFERS.containsKey(target.getUuid())) return fail(p, target.getName().getString() + " already has an offer to consider.");
        ItemStack held = p.getMainHandStack();
        if (held.isEmpty()) return fail(p, "Hold what you want to offer.");
        int price = IntegerArgumentType.getInteger(ctx, "doubloons");
        ItemStack escrow = held.copy();
        held.setCount(0);
        OFFERS.put(target.getUuid(), new Offer(p.getUuid(), p.getName().getString(), escrow, price,
                ctx.getSource().getServer().getTicks() + EXPIRY));
        Text what = Text.literal(escrow.getCount() + " x ").append(escrow.getName());
        p.sendMessage(Text.literal("You offer ").append(what).append(Text.literal(" to " + target.getName().getString()
                + " for " + price + " doubloons.")).formatted(Formatting.GOLD), false);
        target.sendMessage(Text.literal(p.getName().getString() + " offers you ").append(what).append(Text.literal(
                " for " + price + " doubloons. /trade accept or /trade decline")).formatted(Formatting.GOLD), false);
        return 1;
    }

    private static int accept(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        Offer o = OFFERS.get(p.getUuid());
        if (o == null) return fail(p, "Nobody is offering you anything.");
        int silver = o.price() * Wallet.SILVER_PER_DOUBLOON;
        if (!Wallet.pay(p, silver)) return fail(p, "You can't afford " + o.price() + " doubloons.");
        OFFERS.remove(p.getUuid());
        giveBack(p, o.stack());
        ServerPlayerEntity seller = ctx.getSource().getServer().getPlayerManager().getPlayer(o.from());
        if (seller != null) {
            Wallet.give(seller, silver);
            seller.sendMessage(Text.literal(p.getName().getString() + " accepted your offer. " + o.price() + " doubloons received.")
                    .formatted(Formatting.GOLD), false);
        } else {
            // The seller has gone; their coin waits at the auction house
            AuctionHouse.State s = AuctionHouse.State.get(ctx.getSource().getServer());
            s.payouts.merge(o.from(), silver, Integer::sum);
            s.markDirty();
        }
        p.sendMessage(Text.literal("Done: you paid " + o.price() + " doubloons.").formatted(Formatting.GOLD), false);
        return 1;
    }

    private static int decline(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        Offer o = OFFERS.remove(p.getUuid());
        if (o == null) return fail(p, "Nobody is offering you anything.");
        returnToSeller(ctx.getSource().getServer(), o, p.getName().getString() + " declined your offer.");
        p.sendMessage(Text.literal("Offer declined.").formatted(Formatting.GRAY), false);
        return 1;
    }

    private static int pay(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
        int amount = IntegerArgumentType.getInteger(ctx, "doubloons");
        if (target == p) return fail(p, "You count your own coins. Still there.");
        if (!Wallet.pay(p, amount * Wallet.SILVER_PER_DOUBLOON)) return fail(p, "You don't have " + amount + " doubloons.");
        Wallet.give(target, amount * Wallet.SILVER_PER_DOUBLOON);
        p.sendMessage(Text.literal("Paid " + target.getName().getString() + " " + amount + " doubloons.").formatted(Formatting.GOLD), false);
        target.sendMessage(Text.literal(p.getName().getString() + " paid you " + amount + " doubloons.").formatted(Formatting.GOLD), false);
        return 1;
    }

    private static void tick(MinecraftServer server) {
        if (server.getTicks() % 20 != 0 || OFFERS.isEmpty()) return;
        int now = server.getTicks();
        List<UUID> lapsed = new ArrayList<>();
        OFFERS.forEach((to, o) -> { if (o.expires() < now) lapsed.add(to); });
        for (UUID to : lapsed) {
            Offer o = OFFERS.remove(to);
            returnToSeller(server, o, "Your offer lapsed; nobody took it.");
        }
    }

    private static void returnToSeller(MinecraftServer server, Offer o, String why) {
        ServerPlayerEntity seller = server.getPlayerManager().getPlayer(o.from());
        if (seller != null) {
            giveBack(seller, o.stack());
            seller.sendMessage(Text.literal(why + " The goods are back in your pack.").formatted(Formatting.GRAY), false);
        } else {
            RETURNS.computeIfAbsent(o.from(), k -> new ArrayList<>()).add(o.stack());
        }
    }

    private static void giveBack(ServerPlayerEntity p, ItemStack stack) {
        if (!p.getInventory().insertStack(stack)) p.dropItem(stack, false);
    }

    private static int fail(ServerPlayerEntity p, String msg) {
        p.sendMessage(Text.literal(msg).formatted(Formatting.RED), false);
        return 0;
    }
}
