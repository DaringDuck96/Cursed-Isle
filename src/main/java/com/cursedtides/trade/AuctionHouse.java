package com.cursedtides.trade;

import com.cursedtides.CursedTides;
import com.cursedtides.entity.StoryNpcEntity;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/**
 * The Kingsport auction house. List what's in your hand near the auctioneer, browse and buy from his
 * board, and collect the takings of anything you sold while you were away. Prices are in doubloons.
 */
public final class AuctionHouse {
    private AuctionHouse() {}

    public static final int MAX_LISTINGS = 8;
    private static final double REACH = 12;

    public record Entry(int id, ItemStack stack, int price, String seller) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Entry::id, ItemStack.STREAM_CODEC, Entry::stack,
                ByteBufCodecs.VAR_INT, Entry::price, ByteBufCodecs.STRING_UTF8, Entry::seller, Entry::new);
    }

    /** Server to client: the board. */
    public record Board(List<Entry> entries) implements CustomPacketPayload {
        public static final Type<Board> ID = new Type<>(CursedTides.id("auction_board"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Board> CODEC =
                StreamCodec.composite(Entry.CODEC.apply(ByteBufCodecs.list()), Board::entries, Board::new);
        @Override public Type<? extends CustomPacketPayload> type() { return ID; }
    }

    /** Client to server: buy listing {@code id}. */
    public record Buy(int id) implements CustomPacketPayload {
        public static final Type<Buy> ID = new Type<>(CursedTides.id("auction_buy"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Buy> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, Buy::id, Buy::new);
        @Override public Type<? extends CustomPacketPayload> type() { return ID; }
    }

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(Board.ID, Board.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(Buy.ID, Buy.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Buy.ID, (payload, ctx) -> ctx.server().execute(() -> buy(ctx.player(), payload.id())));
        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> dispatcher.register(literal("auction")
                .then(literal("sell").then(argument("doubloons", IntegerArgumentType.integer(1, 100000)).executes(AuctionHouse::sell)))
                .then(literal("cancel").then(argument("id", IntegerArgumentType.integer(0)).executes(AuctionHouse::cancel)))
                .then(literal("mine").executes(AuctionHouse::mine))));
    }

    private static boolean nearAuctioneer(ServerPlayer p) {
        return !p.level().getEntitiesOfClass(StoryNpcEntity.class, p.getBoundingBox().inflate(REACH),
                n -> n.getRole() == StoryNpcEntity.Role.AUCTIONEER).isEmpty();
    }

    /** The auctioneer: pays out takings, then shows the board. */
    public static void open(ServerPlayer p) {
        State s = State.get(p.level().getServer());
        Integer owed = s.payouts.remove(p.getUUID());
        if (owed != null && owed > 0) {
            Wallet.give(p, owed);
            p.sendSystemMessage(Component.literal("The auctioneer hands over your takings: " + Wallet.format(owed) + ".").withStyle(ChatFormatting.GOLD), false);
            s.setDirty();
        }
        List<Entry> entries = new ArrayList<>();
        for (Listing l : s.listings.values()) entries.add(new Entry(l.id, l.stack.copy(), l.price, l.sellerName));
        entries.sort((a, b) -> Integer.compare(a.id(), b.id()));
        ServerPlayNetworking.send(p, new Board(entries));
    }

    private static int sell(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        if (!nearAuctioneer(p)) return fail(p, "Find the auctioneer in Kingsport's town hall to list goods.");
        ItemStack held = p.getMainHandItem();
        if (held.isEmpty()) return fail(p, "Hold what you want to sell.");
        State s = State.get(p.level().getServer());
        long mine = s.listings.values().stream().filter(l -> l.seller.equals(p.getUUID())).count();
        if (mine >= MAX_LISTINGS) return fail(p, "You already have " + MAX_LISTINGS + " lots on the board.");
        int price = IntegerArgumentType.getInteger(ctx, "doubloons");
        Listing l = new Listing(s.nextId++, p.getUUID(), p.getName().getString(), price, held.copy());
        held.setCount(0);
        s.listings.put(l.id, l);
        s.setDirty();
        p.sendSystemMessage(Component.literal("Lot #" + l.id + ": ").append(l.stack.getHoverName()).append(Component.literal(" x" + l.stack.getCount()
                + " listed for " + price + " doubloons. (/auction cancel " + l.id + " to take it back)")).withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private static int cancel(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        State s = State.get(p.level().getServer());
        Listing l = s.listings.get(IntegerArgumentType.getInteger(ctx, "id"));
        if (l == null || !l.seller.equals(p.getUUID())) return fail(p, "That's not one of your lots.");
        s.listings.remove(l.id);
        s.setDirty();
        if (!p.getInventory().add(l.stack)) p.drop(l.stack, false, net.minecraft.util.Prediction.SERVER_ONLY);
        p.sendSystemMessage(Component.literal("Lot #" + l.id + " withdrawn.").withStyle(ChatFormatting.GRAY), false);
        return 1;
    }

    private static int mine(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        State s = State.get(p.level().getServer());
        boolean any = false;
        for (Listing l : s.listings.values()) {
            if (!l.seller.equals(p.getUUID())) continue;
            any = true;
            p.sendSystemMessage(Component.literal("#" + l.id + " ").append(l.stack.getHoverName()).append(Component.literal(" x" + l.stack.getCount()
                    + " for " + l.price + " doubloons")).withStyle(ChatFormatting.GRAY), false);
        }
        if (!any) p.sendSystemMessage(Component.literal("You have nothing on the board.").withStyle(ChatFormatting.GRAY), false);
        Integer owed = s.payouts.get(p.getUUID());
        if (owed != null) p.sendSystemMessage(Component.literal("Takings waiting at the auctioneer: " + Wallet.format(owed)).withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private static void buy(ServerPlayer p, int id) {
        if (!nearAuctioneer(p)) return;
        MinecraftServer server = p.level().getServer();
        State s = State.get(server);
        Listing l = s.listings.get(id);
        if (l == null) {
            p.sendSystemMessage(Component.literal("That lot has already gone.").withStyle(ChatFormatting.RED), true);
            open(p);
            return;
        }
        if (l.seller.equals(p.getUUID())) {
            p.sendSystemMessage(Component.literal("That's your own lot. Use /auction cancel " + id + ".").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        int silver = l.price * Wallet.SILVER_PER_DOUBLOON;
        if (!Wallet.pay(p, silver)) {
            p.sendSystemMessage(Component.literal("You can't afford " + l.price + " doubloons.").withStyle(ChatFormatting.RED), true);
            return;
        }
        s.listings.remove(id);
        if (!p.getInventory().add(l.stack.copy())) p.drop(l.stack.copy(), false, net.minecraft.util.Prediction.SERVER_ONLY);
        // The house keeps five percent
        int takings = silver - silver / 20;
        ServerPlayer seller = server.getPlayerList().getPlayer(l.seller);
        if (seller != null) {
            Wallet.give(seller, takings);
            seller.sendSystemMessage(Component.literal("Your lot #" + id + " sold to " + p.getName().getString() + " for " + l.price
                    + " doubloons (less the house's cut).").withStyle(ChatFormatting.GOLD), false);
        } else {
            s.payouts.merge(l.seller, takings, Integer::sum);
        }
        s.setDirty();
        p.level().playSound(null, p.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 0.7f, 1.0f);
        p.sendSystemMessage(Component.literal("Sold to the captain in the front row!").withStyle(ChatFormatting.GOLD), true);
        open(p);
    }

    private static int fail(ServerPlayer p, String msg) {
        p.sendSystemMessage(Component.literal(msg).withStyle(ChatFormatting.RED), false);
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

    public static final class State extends SavedData {
        final Map<Integer, Listing> listings = new HashMap<>();
        final Map<UUID, Integer> payouts = new HashMap<>();
        int nextId = 1;
        private static final net.minecraft.world.level.saveddata.SavedDataType<State> TYPE = com.cursedtides.util.Saved.type("cursedtides_auction", State::new, State::fromNbt, (st__, l__) -> st__.save(new CompoundTag(), l__));

        static State get(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(TYPE);
        }

                public CompoundTag save(CompoundTag nbt, HolderLookup.Provider lookup) {
            nbt.putInt("NextId", nextId);
            ListTag list = new ListTag();
            for (Listing l : listings.values()) {
                CompoundTag c = new CompoundTag();
                c.putInt("Id", l.id);
                c.store("Seller", net.minecraft.core.UUIDUtil.CODEC, l.seller);
                c.putString("SellerName", l.sellerName);
                c.putInt("Price", l.price);
                c.put("Item", com.cursedtides.util.Compat.saveStack(l.stack, lookup));
                list.add(c);
            }
            nbt.put("Listings", list);
            ListTag pay = new ListTag();
            for (var e : payouts.entrySet()) {
                CompoundTag c = new CompoundTag();
                c.store("Id", net.minecraft.core.UUIDUtil.CODEC, e.getKey());
                c.putInt("Silver", e.getValue());
                pay.add(c);
            }
            nbt.put("Payouts", pay);
            return nbt;
        }

        private static State fromNbt(CompoundTag nbt, HolderLookup.Provider lookup) {
            State s = new State();
            s.nextId = Math.max(1, nbt.getIntOr("NextId", 0));
            ListTag list = nbt.getListOrEmpty("Listings");
            for (int i = 0; i < list.size(); i++) {
                CompoundTag c = list.getCompoundOrEmpty(i);
                ItemStack stack = com.cursedtides.util.Compat.loadStack(lookup, c.get("Item")).orElse(ItemStack.EMPTY);
                if (stack.isEmpty()) continue;
                Listing l = new Listing(c.getIntOr("Id", 0), c.read("Seller", net.minecraft.core.UUIDUtil.CODEC).orElse(null), c.getStringOr("SellerName", ""), c.getIntOr("Price", 0), stack);
                s.listings.put(l.id, l);
            }
            ListTag pay = nbt.getListOrEmpty("Payouts");
            for (int i = 0; i < pay.size(); i++) {
                CompoundTag c = pay.getCompoundOrEmpty(i);
                s.payouts.put(c.read("Id", net.minecraft.core.UUIDUtil.CODEC).orElse(null), c.getIntOr("Silver", 0));
            }
            return s;
        }
    }
}
