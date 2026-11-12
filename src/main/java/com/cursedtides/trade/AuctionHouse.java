package com.cursedtides.trade;

import com.cursedtides.CursedTides;
import com.cursedtides.entity.StoryNpcEntity;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.PersistentState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * The Kingsport auction house. List what's in your hand near the auctioneer, browse and buy from his
 * board, and collect the takings of anything you sold while you were away. Prices are in doubloons.
 */
public final class AuctionHouse {
    private AuctionHouse() {}

    public static final int MAX_LISTINGS = 8;
    private static final double REACH = 12;

    public record Entry(int id, ItemStack stack, int price, String seller) {
        public static final PacketCodec<RegistryByteBuf, Entry> CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_INT, Entry::id, ItemStack.PACKET_CODEC, Entry::stack,
                PacketCodecs.VAR_INT, Entry::price, PacketCodecs.STRING, Entry::seller, Entry::new);
    }

    /** Server to client: the board. */
    public record Board(List<Entry> entries) implements CustomPayload {
        public static final Id<Board> ID = new Id<>(CursedTides.id("auction_board"));
        public static final PacketCodec<RegistryByteBuf, Board> CODEC =
                PacketCodec.tuple(Entry.CODEC.collect(PacketCodecs.toList()), Board::entries, Board::new);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    /** Client to server: buy listing {@code id}. */
    public record Buy(int id) implements CustomPayload {
        public static final Id<Buy> ID = new Id<>(CursedTides.id("auction_buy"));
        public static final PacketCodec<RegistryByteBuf, Buy> CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, Buy::id, Buy::new);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(Board.ID, Board.CODEC);
        PayloadTypeRegistry.playC2S().register(Buy.ID, Buy.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Buy.ID, (payload, ctx) -> ctx.server().execute(() -> buy(ctx.player(), payload.id())));
        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> dispatcher.register(literal("auction")
                .then(literal("sell").then(argument("doubloons", IntegerArgumentType.integer(1, 100000)).executes(AuctionHouse::sell)))
                .then(literal("cancel").then(argument("id", IntegerArgumentType.integer(0)).executes(AuctionHouse::cancel)))
                .then(literal("mine").executes(AuctionHouse::mine))));
    }

    private static boolean nearAuctioneer(ServerPlayerEntity p) {
        return !p.getWorld().getEntitiesByClass(StoryNpcEntity.class, p.getBoundingBox().expand(REACH),
                n -> n.getRole() == StoryNpcEntity.Role.AUCTIONEER).isEmpty();
    }

    /** The auctioneer: pays out takings, then shows the board. */
    public static void open(ServerPlayerEntity p) {
        State s = State.get(p.getServer());
        Integer owed = s.payouts.remove(p.getUuid());
        if (owed != null && owed > 0) {
            Wallet.give(p, owed);
            p.sendMessage(Text.literal("The auctioneer hands over your takings: " + Wallet.format(owed) + ".").formatted(Formatting.GOLD), false);
            s.markDirty();
        }
        List<Entry> entries = new ArrayList<>();
        for (Listing l : s.listings.values()) entries.add(new Entry(l.id, l.stack.copy(), l.price, l.sellerName));
        entries.sort((a, b) -> Integer.compare(a.id(), b.id()));
        ServerPlayNetworking.send(p, new Board(entries));
    }

    private static int sell(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        if (!nearAuctioneer(p)) return fail(p, "Find the auctioneer in Kingsport's town hall to list goods.");
        ItemStack held = p.getMainHandStack();
        if (held.isEmpty()) return fail(p, "Hold what you want to sell.");
        State s = State.get(p.getServer());
        long mine = s.listings.values().stream().filter(l -> l.seller.equals(p.getUuid())).count();
        if (mine >= MAX_LISTINGS) return fail(p, "You already have " + MAX_LISTINGS + " lots on the board.");
        int price = IntegerArgumentType.getInteger(ctx, "doubloons");
        Listing l = new Listing(s.nextId++, p.getUuid(), p.getName().getString(), price, held.copy());
        held.setCount(0);
        s.listings.put(l.id, l);
        s.markDirty();
        p.sendMessage(Text.literal("Lot #" + l.id + ": ").append(l.stack.getName()).append(Text.literal(" x" + l.stack.getCount()
                + " listed for " + price + " doubloons. (/auction cancel " + l.id + " to take it back)")).formatted(Formatting.GOLD), false);
        return 1;
    }

    private static int cancel(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        State s = State.get(p.getServer());
        Listing l = s.listings.get(IntegerArgumentType.getInteger(ctx, "id"));
        if (l == null || !l.seller.equals(p.getUuid())) return fail(p, "That's not one of your lots.");
        s.listings.remove(l.id);
        s.markDirty();
        if (!p.getInventory().insertStack(l.stack)) p.dropItem(l.stack, false);
        p.sendMessage(Text.literal("Lot #" + l.id + " withdrawn.").formatted(Formatting.GRAY), false);
        return 1;
    }

    private static int mine(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        State s = State.get(p.getServer());
        boolean any = false;
        for (Listing l : s.listings.values()) {
            if (!l.seller.equals(p.getUuid())) continue;
            any = true;
            p.sendMessage(Text.literal("#" + l.id + " ").append(l.stack.getName()).append(Text.literal(" x" + l.stack.getCount()
                    + " for " + l.price + " doubloons")).formatted(Formatting.GRAY), false);
        }
        if (!any) p.sendMessage(Text.literal("You have nothing on the board.").formatted(Formatting.GRAY), false);
        Integer owed = s.payouts.get(p.getUuid());
        if (owed != null) p.sendMessage(Text.literal("Takings waiting at the auctioneer: " + Wallet.format(owed)).formatted(Formatting.GOLD), false);
        return 1;
    }

    private static void buy(ServerPlayerEntity p, int id) {
        if (!nearAuctioneer(p)) return;
        MinecraftServer server = p.getServer();
        State s = State.get(server);
        Listing l = s.listings.get(id);
        if (l == null) {
            p.sendMessage(Text.literal("That lot has already gone.").formatted(Formatting.RED), true);
            open(p);
            return;
        }
        if (l.seller.equals(p.getUuid())) {
            p.sendMessage(Text.literal("That's your own lot. Use /auction cancel " + id + ".").formatted(Formatting.GRAY), true);
            return;
        }
        int silver = l.price * Wallet.SILVER_PER_DOUBLOON;
        if (!Wallet.pay(p, silver)) {
            p.sendMessage(Text.literal("You can't afford " + l.price + " doubloons.").formatted(Formatting.RED), true);
            return;
        }
        s.listings.remove(id);
        if (!p.getInventory().insertStack(l.stack.copy())) p.dropItem(l.stack.copy(), false);
        // The house keeps five percent
        int takings = silver - silver / 20;
        ServerPlayerEntity seller = server.getPlayerManager().getPlayer(l.seller);
        if (seller != null) {
            Wallet.give(seller, takings);
            seller.sendMessage(Text.literal("Your lot #" + id + " sold to " + p.getName().getString() + " for " + l.price
                    + " doubloons (less the house's cut).").formatted(Formatting.GOLD), false);
        } else {
            s.payouts.merge(l.seller, takings, Integer::sum);
        }
        s.markDirty();
        p.getWorld().playSound(null, p.getBlockPos(), SoundEvents.ENTITY_VILLAGER_YES, SoundCategory.NEUTRAL, 0.7f, 1.0f);
        p.sendMessage(Text.literal("Sold to the captain in the front row!").formatted(Formatting.GOLD), true);
        open(p);
    }

    private static int fail(ServerPlayerEntity p, String msg) {
        p.sendMessage(Text.literal(msg).formatted(Formatting.RED), false);
        return 0;
    }

    // ------------------------------------------------------------------ saved state

    static final class Listing {
        final int id;
        final UUID seller;
        final String sellerName;
        final int price;
        final ItemStack stack;

        Listing(int id, UUID seller, String sellerName, int price, ItemStack stack) {
            this.id = id;
            this.seller = seller;
            this.sellerName = sellerName;
            this.price = price;
            this.stack = stack;
        }
    }

    public static final class State extends PersistentState {
        final Map<Integer, Listing> listings = new HashMap<>();
        final Map<UUID, Integer> payouts = new HashMap<>();
        int nextId = 1;
        private static final Type<State> TYPE = new Type<>(State::new, State::fromNbt, null);

        static State get(MinecraftServer server) {
            return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, "cursedtides_auction");
        }

        @Override
        public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
            nbt.putInt("NextId", nextId);
            NbtList list = new NbtList();
            for (Listing l : listings.values()) {
                NbtCompound c = new NbtCompound();
                c.putInt("Id", l.id);
                c.putUuid("Seller", l.seller);
                c.putString("SellerName", l.sellerName);
                c.putInt("Price", l.price);
                c.put("Item", l.stack.encode(lookup));
                list.add(c);
            }
            nbt.put("Listings", list);
            NbtList pay = new NbtList();
            for (var e : payouts.entrySet()) {
                NbtCompound c = new NbtCompound();
                c.putUuid("Id", e.getKey());
                c.putInt("Silver", e.getValue());
                pay.add(c);
            }
            nbt.put("Payouts", pay);
            return nbt;
        }

        private static State fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
            State s = new State();
            s.nextId = Math.max(1, nbt.getInt("NextId"));
            NbtList list = nbt.getList("Listings", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < list.size(); i++) {
                NbtCompound c = list.getCompound(i);
                ItemStack stack = ItemStack.fromNbt(lookup, c.get("Item")).orElse(ItemStack.EMPTY);
                if (stack.isEmpty()) continue;
                Listing l = new Listing(c.getInt("Id"), c.getUuid("Seller"), c.getString("SellerName"), c.getInt("Price"), stack);
                s.listings.put(l.id, l);
            }
            NbtList pay = nbt.getList("Payouts", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < pay.size(); i++) {
                NbtCompound c = pay.getCompound(i);
                s.payouts.put(c.getUuid("Id"), c.getInt("Silver"));
            }
            return s;
        }
    }
}
