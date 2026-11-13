package com.cursedtides.trade;

import com.cursedtides.CursedTides;
import com.cursedtides.entity.ShipEntity;
import com.cursedtides.faction.ContractType;
import com.cursedtides.faction.Faction;
import com.cursedtides.faction.FactionData;
import com.cursedtides.world.ModWorld;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Port markets. Every port prices each good from what it grows and what it lacks, and the price
 * moves as players buy and sell, drifting back over time. Shortages and gluts come and go.
 */
public final class Markets {
    private Markets() {}

    public static final int BUY_1 = 0, BUY_5 = 1, SELL_1 = 2, SELL_ALL = 3;
    private static final double STEP = 0.04, MIN_P = -0.5, MAX_P = 1.0;
    private static final int EVENT_LENGTH = 24000;

    // ------------------------------------------------------------------ payloads

    /** Server to client: the market screen's contents. */
    public record Data(CompoundTag data) implements CustomPacketPayload {
        public static final Type<Data> ID = new Type<>(CursedTides.id("market"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Data> CODEC =
                StreamCodec.composite(ByteBufCodecs.COMPOUND_TAG, Data::data, Data::new);
        @Override public Type<? extends CustomPacketPayload> type() { return ID; }
    }

    /** Client to server: buy or sell at the merchant with this entity id. */
    public record Action(int merchant, int good, int action) implements CustomPacketPayload {
        public static final Type<Action> ID = new Type<>(CursedTides.id("market_action"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Action> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Action::merchant, ByteBufCodecs.VAR_INT, Action::good, ByteBufCodecs.VAR_INT, Action::action,
                Action::new);
        @Override public Type<? extends CustomPacketPayload> type() { return ID; }
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(Data.ID, Data.CODEC);
        PayloadTypeRegistry.playC2S().register(Action.ID, Action.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Action.ID, (payload, ctx) -> ctx.server().execute(() -> act(ctx.player(), payload)));
        ServerTickEvents.END_SERVER_TICK.register(Markets::tick);
    }

    // ------------------------------------------------------------------ prices

    public static int price(MinecraftServer server, Ports.Port port, TradeGood g) {
        State s = State.get(server);
        double p = s.pressure.getOrDefault(key(port, g), 0.0);
        double ev = s.eventMultiplier(port, g);
        double mood = port == Ports.NOWHERE ? 1.0 : com.cursedtides.living.Prosperity.priceFactor(server, port);
        return Math.max(1, (int) Math.round(g.basePrice * port.factor(g) * (1.0 + p) * ev * mood));
    }

    /** What the merchant pays you. Black markets pay well for contraband, poorly for honest cargo. */
    public static int sellPrice(MinecraftServer server, Ports.Port port, TradeGood g, ServerPlayer player) {
        double spread = port.blackMarket() && !g.contraband ? 0.7 : 0.8;
        if (port.blackMarket() && g.contraband) {
            int rep = FactionData.rep(player, Faction.SMUGGLERS);
            spread += rep >= 600 ? 0.15 : rep >= 300 ? 0.10 : rep >= 100 ? 0.05 : 0.0;
        }
        return Math.max(1, (int) Math.round(price(server, port, g) * spread));
    }

    private static String key(Ports.Port port, TradeGood g) {
        return Ports.index(port) + ":" + g.ordinal();
    }

    // ------------------------------------------------------------------ the screen

    public static void open(ServerPlayer player, Entity merchant) {
        Ports.Port port = Ports.at(merchant.getBlockX(), merchant.getBlockZ());
        MinecraftServer server = player.getServer();
        if (server == null) return;
        if (TradeEvents.quarantined(port, player.serverLevel())) {
            player.displayClientMessage(Component.literal("The market's shut: the port is under quarantine. Come back when the fever's passed.").withStyle(ChatFormatting.RED), false);
            return;
        }
        CompoundTag data = new CompoundTag();
        data.putInt("Merchant", merchant.getId());
        data.putString("Port", port == Ports.NOWHERE ? "Open market" : port.island().name());
        data.putBoolean("Black", port.blackMarket());
        data.putInt("Wallet", Wallet.total(player));
        State s = State.get(server);
        ListTag goods = new ListTag();
        for (TradeGood g : TradeGood.values()) {
            if (!port.deals(g)) continue;
            CompoundTag e = new CompoundTag();
            e.putInt("Id", g.ordinal());
            e.putString("Name", g.displayName);
            e.putInt("Buy", price(server, port, g));
            e.putInt("Sell", sellPrice(server, port, g, player));
            e.putInt("Have", count(player, g));
            e.putBoolean("Contraband", g.contraband);
            String kind = switch (port.kind(g)) {
                case PRODUCES -> "local";
                case DEMANDS -> "wanted";
                case NEUTRAL -> "";
            };
            String event = s.eventLabel(port, g);
            e.putString("Note", event != null ? event : kind);
            goods.add(e);
        }
        data.put("Goods", goods);
        // Current shortages and gluts elsewhere, for planning a route
        ListTag news = new ListTag();
        for (Event ev : s.events) {
            Ports.Port where = Ports.ALL.get(ev.port);
            news.add(net.minecraft.nbt.StringTag.valueOf((ev.mult > 1 ? "Shortage of " : "Glut of ")
                    + TradeGood.values()[ev.good].displayName.toLowerCase() + " at " + where.island().name()));
        }
        data.put("News", news);
        ServerPlayNetworking.send(player, new Data(data));
    }

    private static void act(ServerPlayer player, Action a) {
        Entity merchant = player.level().getEntity(a.merchant());
        if (merchant == null || merchant.distanceToSqr(player) > 10 * 10) return;
        if (a.good() < 0 || a.good() >= TradeGood.values().length) return;
        TradeGood g = TradeGood.values()[a.good()];
        Ports.Port port = Ports.at(merchant.getBlockX(), merchant.getBlockZ());
        if (!port.deals(g)) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;
        State s = State.get(server);
        String k = key(port, g);

        if (a.action() == BUY_1 || a.action() == BUY_5) {
            int want = a.action() == BUY_1 ? 1 : 5;
            int bought = 0, spent = 0;
            for (int i = 0; i < want; i++) {
                int cost = price(server, port, g);
                if (!Wallet.pay(player, cost)) break;
                if (!store(player, new ItemStack(g.item()))) {
                    Wallet.give(player, cost);
                    player.displayClientMessage(Component.literal("No room: your pockets and your hold are full.").withStyle(ChatFormatting.RED), true);
                    break;
                }
                spent += cost;
                bought++;
                s.nudge(k, STEP);
            }
            if (bought > 0) {
                player.displayClientMessage(Component.literal("Bought " + bought + " " + g.displayName + " for " + Wallet.format(spent))
                        .withStyle(ChatFormatting.GOLD), true);
                ding(player, 1.2f);
            } else if (Wallet.total(player) < price(server, port, g)) {
                player.displayClientMessage(Component.literal("You can't afford that.").withStyle(ChatFormatting.RED), true);
            }
        } else {
            int have = count(player, g);
            int n = a.action() == SELL_1 ? Math.min(1, have) : have;
            int earned = 0, sold = 0;
            for (int i = 0; i < n; i++) {
                int pay = sellPrice(server, port, g, player);
                if (!take(player, g)) break;
                earned += pay;
                sold++;
                s.nudge(k, -STEP);
                if (g.contraband && port.blackMarket()) {
                    FactionData.adjust(player, Faction.SMUGGLERS, 2);
                    FactionData.progress(player, ContractType.SMUGGLER_RUN);
                    if (g == TradeGood.CURSED_IDOL) FactionData.progress(player, ContractType.SMUGGLER_IDOLS);
                }
            }
            if (earned > 0) {
                Wallet.give(player, earned);
                if (port != Ports.NOWHERE && !port.blackMarket() && sold >= 3) {
                    com.cursedtides.living.Prosperity.adjust(server, port.island().cx(), port.island().cz(), 1, "trade is brisk");
                }
                com.cursedtides.activity.Challenges.progress(player, "trade", sold);
                TradeEvents.onSold(player, port, g, sold);
                player.displayClientMessage(Component.literal("Sold " + sold + " " + g.displayName + " for " + Wallet.format(earned))
                        .withStyle(ChatFormatting.GOLD), true);
                ding(player, 0.9f);
                player.playNotifySound(com.cursedtides.sound.ModSounds.COINS.value(), net.minecraft.sounds.SoundSource.PLAYERS, 0.7f, 1f);
            }
        }
        s.setDirty();
        open(player, merchant); // refresh the screen
    }

    private static void ding(ServerPlayer player, float pitch) {
        player.level().playSound(null, player.blockPosition(), SoundEvents.VILLAGER_TRADE, SoundSource.NEUTRAL, 0.6f, pitch);
    }

    // ------------------------------------------------------------------ cargo: pockets and the hold

    /** The player's own ships close by: cargo can be sold straight from the hold. */
    private static List<Container> holds(ServerPlayer player) {
        List<Container> out = new ArrayList<>();
        var box = player.getBoundingBox().inflate(40);
        for (ShipEntity ship : player.level().getEntitiesOfClass(ShipEntity.class, box,
                s -> !s.isHostile() && s.getOwner() != null && s.getOwner().equals(player.getUUID()))) {
            out.add(ship.cargo());
        }
        return out;
    }

    public static int count(ServerPlayer player, TradeGood g) {
        int n = player.getInventory().countItem(g.item());
        for (Container hold : holds(player)) n += hold.countItem(g.item());
        return n;
    }

    private static boolean take(ServerPlayer player, TradeGood g) {
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).is(g.item())) {
                inv.getItem(i).shrink(1);
                return true;
            }
        }
        for (Container hold : holds(player)) {
            for (int i = 0; i < hold.getContainerSize(); i++) {
                if (hold.getItem(i).is(g.item())) {
                    hold.getItem(i).shrink(1);
                    hold.setChanged();
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean store(ServerPlayer player, ItemStack stack) {
        if (player.getInventory().add(stack.copy())) return true;
        for (Container hold : holds(player)) {
            if (hold instanceof net.minecraft.world.SimpleContainer simple && simple.canAddItem(stack)) {
                simple.addItem(stack.copy());
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ drift and events

    private static void tick(MinecraftServer server) {
        if (server.getTickCount() % 1200 != 0) return;
        ServerLevel world = server.overworld();
        State s = State.get(server);
        s.pressure.replaceAll((k, v) -> Math.abs(v) < 0.01 ? 0.0 : v * 0.92);
        long now = world.getGameTime();
        s.events.removeIf(e -> e.expires < now);
        // Roughly every ten minutes, something happens to a market somewhere
        if (ModWorld.isArchipelago() && server.getTickCount() % 12000 == 0 && s.events.size() < 4 && world.random.nextFloat() < 0.6f) {
            int portIndex = world.random.nextInt(Ports.ALL.size());
            Ports.Port port = Ports.ALL.get(portIndex);
            List<TradeGood> options = new ArrayList<>();
            for (TradeGood g : TradeGood.values()) if (!g.contraband && port.deals(g)) options.add(g);
            TradeGood g = options.get(world.random.nextInt(options.size()));
            boolean shortage = world.random.nextBoolean();
            s.events.add(new Event(portIndex, g.ordinal(), shortage ? 2.0 : 0.5, now + EVENT_LENGTH));
            s.setDirty();
            if (shortage) TradeEvents.onShortage(server, portIndex, g, now + EVENT_LENGTH);
            server.getPlayerList().broadcastSystemMessage(Component.literal(shortage
                    ? "Word from " + port.island().name() + ": they've run short of " + g.displayName.toLowerCase() + ". Prices have doubled."
                    : "Word from " + port.island().name() + ": the warehouses are overflowing with " + g.displayName.toLowerCase()
                    + ". It's going for half price.").withStyle(ChatFormatting.YELLOW), false);
        }
    }

    // ------------------------------------------------------------------ saved state

    record Event(int port, int good, double mult, long expires) {}


    public static final class State extends SavedData {
        final Map<String, Double> pressure = new HashMap<>();
        final List<Event> events = new ArrayList<>();
        private static final Factory<State> TYPE = new Factory<>(State::new, State::fromNbt, null);

        public static State get(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(TYPE, "cursedtides_markets");
        }

        void nudge(String k, double d) {
            pressure.put(k, Mth.clamp(pressure.getOrDefault(k, 0.0) + d, MIN_P, MAX_P));
        }

        double eventMultiplier(Ports.Port port, TradeGood g) {
            int pi = Ports.index(port);
            for (Event e : events) if (e.port == pi && e.good == g.ordinal()) return e.mult;
            return 1.0;
        }

        String eventLabel(Ports.Port port, TradeGood g) {
            double m = eventMultiplier(port, g);
            return m > 1 ? "SHORTAGE" : m < 1 ? "GLUT" : null;
        }

        @Override
        public CompoundTag save(CompoundTag nbt, HolderLookup.Provider lookup) {
            CompoundTag p = new CompoundTag();
            pressure.forEach(p::putDouble);
            nbt.put("Pressure", p);
            ListTag list = new ListTag();
            for (Event e : events) {
                CompoundTag c = new CompoundTag();
                c.putInt("Port", e.port);
                c.putInt("Good", e.good);
                c.putDouble("Mult", e.mult);
                c.putLong("Expires", e.expires);
                list.add(c);
            }
            nbt.put("Events", list);
            return nbt;
        }

        private static State fromNbt(CompoundTag nbt, HolderLookup.Provider lookup) {
            State s = new State();
            CompoundTag p = nbt.getCompound("Pressure");
            for (String k : p.getAllKeys()) s.pressure.put(k, p.getDouble(k));
            ListTag list = nbt.getList("Events", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag c = list.getCompound(i);
                s.events.add(new Event(c.getInt("Port"), c.getInt("Good"), c.getDouble("Mult"), c.getLong("Expires")));
            }
            return s;
        }
    }
}
