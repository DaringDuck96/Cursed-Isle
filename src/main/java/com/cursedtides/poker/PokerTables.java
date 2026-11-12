package com.cursedtides.poker;

import com.cursedtides.CursedTides;
import com.cursedtides.item.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Seats players at Hold'em tables (one table per player, three NPC opponents) and relays the play. */
public final class PokerTables {
    private PokerTables() {}

    public static final int MIN_BUY_IN = 10, MAX_BUY_IN = 64;

    public record State(NbtCompound data) implements CustomPayload {
        public static final CustomPayload.Id<State> ID = new CustomPayload.Id<>(CursedTides.id("poker_state"));
        public static final PacketCodec<RegistryByteBuf, State> CODEC =
                PacketCodec.tuple(PacketCodecs.NBT_COMPOUND, State::data, State::new);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    /** action: 0 fold, 1 check/call, 2 raise to amount, 3 all-in, 10 deal next hand, 11 leave. */
    public record Action(int action, int amount) implements CustomPayload {
        public static final CustomPayload.Id<Action> ID = new CustomPayload.Id<>(CursedTides.id("poker_action"));
        public static final PacketCodec<RegistryByteBuf, Action> CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_INT, Action::action, PacketCodecs.VAR_INT, Action::amount, Action::new);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public static final int DEAL = 10, LEAVE = 11;

    private static final Map<UUID, PokerGame> games = new HashMap<>();

    public static void register() {
        PayloadTypeRegistry.playS2C().register(State.ID, State.CODEC);
        PayloadTypeRegistry.playC2S().register(Action.ID, Action.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Action.ID, (payload, context) ->
                context.server().execute(() -> handle(context.player(), payload)));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (games.isEmpty()) return;
            for (var entry : Map.copyOf(games).entrySet()) {
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
                if (player == null) {
                    games.remove(entry.getKey());
                    continue;
                }
                if (entry.getValue().tick()) {
                    send(player, entry.getValue());
                }
            }
        });
        // Cash out anyone who disconnects mid-game
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> leave(handler.getPlayer(), false));
    }

    public static boolean isSeated(ServerPlayerEntity player) {
        return games.containsKey(player.getUuid());
    }

    /** Sit down with as many doubloons as you carry, between the table limits. */
    public static void join(ServerPlayerEntity player) {
        if (isSeated(player)) {
            send(player, games.get(player.getUuid()));
            return;
        }
        int carried = player.getInventory().count(ModItems.DOUBLOON);
        if (carried < MIN_BUY_IN) {
            player.sendMessage(Text.literal("The buy-in is at least " + MIN_BUY_IN + " doubloons.").formatted(Formatting.GRAY), false);
            return;
        }
        int buyIn = Math.min(MAX_BUY_IN, carried);
        take(player, buyIn);
        PokerGame game = new PokerGame(com.cursedtides.character.CharacterData.pirateName(player), buyIn);
        games.put(player.getUuid(), game);
        player.sendMessage(Text.literal("You buy in for " + buyIn + " doubloons.").formatted(Formatting.GOLD), false);
        game.startHand();
        send(player, game);
    }

    private static void handle(ServerPlayerEntity player, Action action) {
        PokerGame game = games.get(player.getUuid());
        if (game == null) return;
        switch (action.action()) {
            case DEAL -> {
                if (game.playerBroke()) {
                    player.sendMessage(Text.literal("You're out of chips. Better luck next time.").formatted(Formatting.GRAY), false);
                    leave(player, true);
                    return;
                }
                game.startHand();
            }
            case LEAVE -> {
                leave(player, true);
                return;
            }
            default -> {
                if (!game.act(0, action.action(), action.amount())) return;
                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.PLAYERS, 0.4f, 1.6f);
            }
        }
        send(player, game);
    }

    public static void leave(ServerPlayerEntity player, boolean notify) {
        PokerGame game = games.remove(player.getUuid());
        if (game == null) return;
        int chips = game.handOver ? game.playerStack() : game.playerStack(); // chips already in the pot stay there
        give(player, chips);
        if (notify) {
            player.sendMessage(Text.literal("You cash out " + chips + " doubloons.").formatted(Formatting.GOLD), false);
            NbtCompound closed = new NbtCompound();
            closed.putBoolean("Closed", true);
            ServerPlayNetworking.send(player, new State(closed));
        }
    }

    private static void send(ServerPlayerEntity player, PokerGame game) {
        ServerPlayNetworking.send(player, new State(game.toNbt()));
    }

    private static void take(ServerPlayerEntity player, int amount) {
        var inv = player.getInventory();
        int left = amount;
        for (int i = 0; i < inv.size() && left > 0; i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isOf(ModItems.DOUBLOON)) {
                int t = Math.min(left, stack.getCount());
                stack.decrement(t);
                left -= t;
            }
        }
    }

    private static void give(ServerPlayerEntity player, int amount) {
        while (amount > 0) {
            int n = Math.min(64, amount);
            ItemStack coins = new ItemStack(ModItems.DOUBLOON, n);
            if (!player.getInventory().insertStack(coins)) player.dropItem(coins, false);
            amount -= n;
        }
    }
}
