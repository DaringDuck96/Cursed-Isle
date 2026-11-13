package com.cursedtides.poker;

import com.cursedtides.CursedTides;
import com.cursedtides.item.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Hold'em tables. Each dealer runs one shared table: players who sit down at the same dealer play each other,
 * with house players filling the empty seats. Players who join mid-hand are dealt in at the next hand.
 */
public final class PokerTables {
    private PokerTables() {}

    public static final int MIN_BUY_IN = 10, MAX_BUY_IN = 64;

    public record State(CompoundTag data) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<State> ID = new CustomPacketPayload.Type<>(CursedTides.id("poker_state"));
        public static final StreamCodec<RegistryFriendlyByteBuf, State> CODEC =
                StreamCodec.composite(ByteBufCodecs.COMPOUND_TAG, State::data, State::new);
        @Override public Type<? extends CustomPacketPayload> type() { return ID; }
    }

    /** action: 0 fold, 1 check/call, 2 raise to amount, 3 all-in, 10 deal next hand, 11 leave. */
    public record Action(int action, int amount) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<Action> ID = new CustomPacketPayload.Type<>(CursedTides.id("poker_action"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Action> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Action::action, ByteBufCodecs.VAR_INT, Action::amount, Action::new);
        @Override public Type<? extends CustomPacketPayload> type() { return ID; }
    }

    public static final int DEAL = 10, LEAVE = 11;

    private static final class Table {
        final PokerGame game;
        final Map<UUID, Integer> seatOf = new HashMap<>();
        /** Waiting for the current hand to end before sitting down: player -> buy-in already taken. */
        final Map<UUID, Integer> waiting = new HashMap<>();
        Table(PokerGame game) { this.game = game; }
    }

    private static final Map<UUID, Table> TABLES = new HashMap<>();   // keyed by dealer (or player, for a private table)
    private static final Map<UUID, UUID> TABLE_OF = new HashMap<>();   // player -> table key

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(State.ID, State.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(Action.ID, Action.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Action.ID, (payload, context) ->
                context.server().execute(() -> handle(context.player(), payload)));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (TABLES.isEmpty()) return;
            for (var e : Map.copyOf(TABLES).entrySet()) {
                Table t = e.getValue();
                for (UUID id : Map.copyOf(t.seatOf).keySet()) {
                    if (server.getPlayerList().getPlayer(id) == null) leaveById(server, id, false);
                }
                if (!TABLES.containsKey(e.getKey())) continue;
                if (t.game.tick()) broadcast(server, t);
                if (t.game.handOver && !t.waiting.isEmpty()) seatWaiting(server, t);
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> leave(handler.getPlayer(), false));
    }

    public static boolean isSeated(ServerPlayer player) {
        return TABLE_OF.containsKey(player.getUUID());
    }

    /** A private table against the house. */
    public static void join(ServerPlayer player) {
        join(player, null);
    }

    /** Sit down at this dealer's table, with as many doubloons as you carry between the table limits. */
    public static void join(ServerPlayer player, @Nullable Entity dealer) {
        UUID existing = TABLE_OF.get(player.getUUID());
        if (existing != null) {
            Table t = TABLES.get(existing);
            if (t != null) send(player, t);
            return;
        }
        int carried = player.getInventory().countItem(ModItems.DOUBLOON);
        if (carried < MIN_BUY_IN) {
            player.sendSystemMessage(Component.literal("The buy-in is at least " + MIN_BUY_IN + " doubloons.").withStyle(ChatFormatting.GRAY), false);
            return;
        }
        int buyIn = Math.min(MAX_BUY_IN, carried);
        UUID key = dealer != null ? dealer.getUUID() : player.getUUID();
        String name = com.cursedtides.character.CharacterData.pirateName(player);
        Table t = TABLES.get(key);
        if (t == null) {
            take(player, buyIn);
            PokerGame game = new PokerGame(name, buyIn);
            t = new Table(game);
            game.seatsOwner(0, player.getUUID());
            t.seatOf.put(player.getUUID(), 0);
            TABLES.put(key, t);
            TABLE_OF.put(player.getUUID(), key);
            player.sendSystemMessage(Component.literal("You buy in for " + buyIn + " doubloons.").withStyle(ChatFormatting.GOLD), false);
            game.startHand();
            send(player, t);
            return;
        }
        if (t.game.humans() + t.waiting.size() >= PokerGame.SEATS) {
            player.sendSystemMessage(Component.literal("The table's full. Wait for a seat.").withStyle(ChatFormatting.GRAY), false);
            return;
        }
        take(player, buyIn);
        TABLE_OF.put(player.getUUID(), key);
        if (t.game.handOver) {
            int seat = t.game.seatHuman(player.getUUID(), name, buyIn);
            t.seatOf.put(player.getUUID(), seat);
            player.sendSystemMessage(Component.literal("You buy in for " + buyIn + " doubloons and join the table.").withStyle(ChatFormatting.GOLD), false);
            broadcast(player.level().getServer(), t);
        } else {
            t.waiting.put(player.getUUID(), buyIn);
            player.sendSystemMessage(Component.literal("You buy in for " + buyIn + " doubloons. You'll be dealt in at the next hand.")
                    .withStyle(ChatFormatting.GOLD), false);
            send(player, t);
        }
    }

    private static void seatWaiting(MinecraftServer server, Table t) {
        for (var w : Map.copyOf(t.waiting).entrySet()) {
            ServerPlayer p = server.getPlayerList().getPlayer(w.getKey());
            t.waiting.remove(w.getKey());
            if (p == null) continue;
            int seat = t.game.seatHuman(p.getUUID(), com.cursedtides.character.CharacterData.pirateName(p), w.getValue());
            if (seat < 0) {
                give(p, w.getValue());
                TABLE_OF.remove(p.getUUID());
                continue;
            }
            t.seatOf.put(p.getUUID(), seat);
        }
        broadcast(server, t);
    }

    private static void handle(ServerPlayer player, Action action) {
        UUID key = TABLE_OF.get(player.getUUID());
        Table t = key == null ? null : TABLES.get(key);
        if (t == null) return;
        Integer seat = t.seatOf.get(player.getUUID());
        if (action.action() == LEAVE) {
            leave(player, true);
            return;
        }
        if (seat == null) {
            send(player, t);
            return;
        }
        if (action.action() == DEAL) {
            if (t.game.stack(seat) <= 0 && t.game.handOver) {
                player.sendSystemMessage(Component.literal("You're out of chips. Better luck next time.").withStyle(ChatFormatting.GRAY), false);
                leave(player, true);
                return;
            }
            for (var e : Map.copyOf(t.seatOf).entrySet()) {
                if (t.game.stack(e.getValue()) > 0) continue;
                ServerPlayer busted = player.level().getServer().getPlayerList().getPlayer(e.getKey());
                if (busted != null) {
                    busted.sendSystemMessage(Component.literal("You're out of chips and give up your seat.").withStyle(ChatFormatting.GRAY), false);
                    leave(busted, true);
                }
            }
            if (!TABLES.containsValue(t)) return;
            if (!t.waiting.isEmpty()) seatWaiting(player.level().getServer(), t);
            t.game.startHand();
        } else {
            if (!t.game.act(seat, action.action(), action.amount())) return;
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.4f, 1.6f);
        }
        broadcast(player.level().getServer(), t);
    }

    public static void leave(ServerPlayer player, boolean notify) {
        if (player.level().getServer() == null) return;
        int chips = leaveById(player.level().getServer(), player.getUUID(), notify);
        if (chips < 0) return;
        give(player, chips);
        if (notify) {
            player.sendSystemMessage(Component.literal("You cash out " + chips + " doubloons.").withStyle(ChatFormatting.GOLD), false);
            CompoundTag closed = new CompoundTag();
            closed.putBoolean("Closed", true);
            ServerPlayNetworking.send(player, new State(closed));
        }
    }

    /** Removes a player from their table; returns the chips they walk away with (-1 if they weren't seated). */
    private static int leaveById(MinecraftServer server, UUID id, boolean notify) {
        UUID key = TABLE_OF.remove(id);
        if (key == null) return -1;
        Table t = TABLES.get(key);
        if (t == null) return -1;
        Integer waitingBuyIn = t.waiting.remove(id);
        int chips = waitingBuyIn != null ? waitingBuyIn : 0;
        Integer seat = t.seatOf.remove(id);
        if (seat != null) {
            chips = t.game.stack(seat); // chips already in the pot stay there
            t.game.playerLeaves(seat);
        }
        if (t.seatOf.isEmpty() && t.waiting.isEmpty()) TABLES.remove(key);
        else broadcast(server, t);
        return chips;
    }

    private static void broadcast(@Nullable MinecraftServer server, Table t) {
        if (server == null) return;
        for (var e : t.seatOf.entrySet()) {
            ServerPlayer p = server.getPlayerList().getPlayer(e.getKey());
            if (p != null) ServerPlayNetworking.send(p, new State(t.game.toNbt(e.getValue())));
        }
        for (UUID id : t.waiting.keySet()) {
            ServerPlayer p = server.getPlayerList().getPlayer(id);
            if (p != null) send(p, t);
        }
    }

    private static void send(ServerPlayer player, Table t) {
        Integer seat = t.seatOf.get(player.getUUID());
        CompoundTag data = t.game.toNbt(seat == null ? 0 : seat);
        if (seat == null) {
            // Watching from the rail until the next deal: don't show anyone's cards
            data.putIntArray("Cards", new int[PokerGame.SEATS * 2]);
            int[] hidden = data.getIntArray("Cards").orElse(new int[0]);
            java.util.Arrays.fill(hidden, -1);
            data.putIntArray("Cards", hidden);
            data.putInt("ToCall", 0);
            data.putInt("Acting", -1);
        }
        ServerPlayNetworking.send(player, new State(data));
    }

    private static void take(ServerPlayer player, int amount) {
        var inv = player.getInventory();
        int left = amount;
        for (int i = 0; i < inv.getContainerSize() && left > 0; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(ModItems.DOUBLOON)) {
                int t = Math.min(left, stack.getCount());
                stack.shrink(t);
                left -= t;
            }
        }
    }

    private static void give(ServerPlayer player, int amount) {
        while (amount > 0) {
            int n = Math.min(64, amount);
            ItemStack coins = new ItemStack(ModItems.DOUBLOON, n);
            if (!player.getInventory().add(coins)) player.drop(coins, false, net.minecraft.util.Prediction.SERVER_ONLY);
            amount -= n;
        }
    }
}
