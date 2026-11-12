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
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.PersistentState;

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
    public record Data(NbtCompound data) implements CustomPayload {
        public static final Id<Data> ID = new Id<>(CursedTides.id("market"));
        public static final PacketCodec<RegistryByteBuf, Data> CODEC =
                PacketCodec.tuple(PacketCodecs.NBT_COMPOUND, Data::data, Data::new);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    /** Client to server: buy or sell at the merchant with this entity id. */
    public record Action(int merchant, int good, int action) implements CustomPayload {
        public static final Id<Action> ID = new Id<>(CursedTides.id("market_action"));
        public static final PacketCodec<RegistryByteBuf, Action> CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_INT, Action::merchant, PacketCodecs.VAR_INT, Action::good, PacketCodecs.VAR_INT, Action::action,
                Action::new);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
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
    public static int sellPrice(MinecraftServer server, Ports.Port port, TradeGood g, ServerPlayerEntity player) {
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

    public static void open(ServerPlayerEntity player, Entity merchant) {
        Ports.Port port = Ports.at(merchant.getBlockX(), merchant.getBlockZ());
        MinecraftServer server = player.getServer();
        if (server == null) return;
        if (TradeEvents.quarantined(port, player.getServerWorld())) {
            player.sendMessage(Text.literal("The market's shut: the port is under quarantine. Come back when the fever's passed.").formatted(Formatting.RED), false);
            return;
        }
        NbtCompound data = new NbtCompound();
        data.putInt("Merchant", merchant.getId());
        data.putString("Port", port == Ports.NOWHERE ? "Open market" : port.island().name());
        data.putBoolean("Black", port.blackMarket());
        data.putInt("Wallet", Wallet.total(player));
        State s = State.get(server);
        NbtList goods = new NbtList();
        for (TradeGood g : TradeGood.values()) {
            if (!port.deals(g)) continue;
            NbtCompound e = new NbtCompound();
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
        NbtList news = new NbtList();
        for (Event ev : s.events) {
            Ports.Port where = Ports.ALL.get(ev.port);
            news.add(net.minecraft.nbt.NbtString.of((ev.mult > 1 ? "Shortage of " : "Glut of ")
                    + TradeGood.values()[ev.good].displayName.toLowerCase() + " at " + where.island().name()));
        }
        data.put("News", news);
        ServerPlayNetworking.send(player, new Data(data));
    }

    private static void act(ServerPlayerEntity player, Action a) {
        Entity merchant = player.getWorld().getEntityById(a.merchant());
        if (merchant == null || merchant.squaredDistanceTo(player) > 10 * 10) return;
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
                    player.sendMessage(Text.literal("No room: your pockets and your hold are full.").formatted(Formatting.RED), true);
                    break;
                }
                spent += cost;
                bought++;
                s.nudge(k, STEP);
            }
            if (bought > 0) {
                player.sendMessage(Text.literal("Bought " + bought + " " + g.displayName + " for " + Wallet.format(spent))
                        .formatted(Formatting.GOLD), true);
                ding(player, 1.2f);
            } else if (Wallet.total(player) < price(server, port, g)) {
                player.sendMessage(Text.literal("You can't afford that.").formatted(Formatting.RED), true);
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
                player.sendMessage(Text.literal("Sold " + sold + " " + g.displayName + " for " + Wallet.format(earned))
                        .formatted(Formatting.GOLD), true);
                ding(player, 0.9f);
                player.playSoundToPlayer(com.cursedtides.sound.ModSounds.COINS.value(), net.minecraft.sound.SoundCategory.PLAYERS, 0.7f, 1f);
            }
        }
        s.markDirty();
        open(player, merchant); // refresh the screen
    }

    private static void ding(ServerPlayerEntity player, float pitch) {
        player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_VILLAGER_TRADE, SoundCategory.NEUTRAL, 0.6f, pitch);
    }

    // ------------------------------------------------------------------ cargo: pockets and the hold

    /** The player's own ships close by: cargo can be sold straight from the hold. */
    private static List<Inventory> holds(ServerPlayerEntity player) {
        List<Inventory> out = new ArrayList<>();
        var box = player.getBoundingBox().expand(40);
        for (ShipEntity ship : player.getWorld().getEntitiesByClass(ShipEntity.class, box,
                s -> !s.isHostile() && s.getOwner() != null && s.getOwner().equals(player.getUuid()))) {
            out.add(ship.cargo());
        }
        return out;
    }

    public static int count(ServerPlayerEntity player, TradeGood g) {
        int n = player.getInventory().count(g.item());
        for (Inventory hold : holds(player)) n += hold.count(g.item());
        return n;
    }

    private static boolean take(ServerPlayerEntity player, TradeGood g) {
        var inv = player.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            if (inv.getStack(i).isOf(g.item())) {
                inv.getStack(i).decrement(1);
                return true;
            }
        }
        for (Inventory hold : holds(player)) {
            for (int i = 0; i < hold.size(); i++) {
                if (hold.getStack(i).isOf(g.item())) {
                    hold.getStack(i).decrement(1);
                    hold.markDirty();
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean store(ServerPlayerEntity player, ItemStack stack) {
        if (player.getInventory().insertStack(stack.copy())) return true;
        for (Inventory hold : holds(player)) {
            if (hold instanceof net.minecraft.inventory.SimpleInventory simple && simple.canInsert(stack)) {
                simple.addStack(stack.copy());
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ drift and events

    private static void tick(MinecraftServer server) {
        if (server.getTicks() % 1200 != 0) return;
        ServerWorld world = server.getOverworld();
        State s = State.get(server);
        s.pressure.replaceAll((k, v) -> Math.abs(v) < 0.01 ? 0.0 : v * 0.92);
        long now = world.getTime();
        s.events.removeIf(e -> e.expires < now);
        // Roughly every ten minutes, something happens to a market somewhere
        if (ModWorld.isArchipelago() && server.getTicks() % 12000 == 0 && s.events.size() < 4 && world.random.nextFloat() < 0.6f) {
            int portIndex = world.random.nextInt(Ports.ALL.size());
            Ports.Port port = Ports.ALL.get(portIndex);
            List<TradeGood> options = new ArrayList<>();
            for (TradeGood g : TradeGood.values()) if (!g.contraband && port.deals(g)) options.add(g);
            TradeGood g = options.get(world.random.nextInt(options.size()));
            boolean shortage = world.random.nextBoolean();
            s.events.add(new Event(portIndex, g.ordinal(), shortage ? 2.0 : 0.5, now + EVENT_LENGTH));
            s.markDirty();
            if (shortage) TradeEvents.onShortage(server, portIndex, g, now + EVENT_LENGTH);
            server.getPlayerManager().broadcast(Text.literal(shortage
                    ? "Word from " + port.island().name() + ": they've run short of " + g.displayName.toLowerCase() + ". Prices have doubled."
                    : "Word from " + port.island().name() + ": the warehouses are overflowing with " + g.displayName.toLowerCase()
                    + ". It's going for half price.").formatted(Formatting.YELLOW), false);
        }
    }

    // ------------------------------------------------------------------ saved state

    record Event(int port, int good, double mult, long expires) {}


    public static final class State extends PersistentState {
        final Map<String, Double> pressure = new HashMap<>();
        final List<Event> events = new ArrayList<>();
        private static final Type<State> TYPE = new Type<>(State::new, State::fromNbt, null);

        public static State get(MinecraftServer server) {
            return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, "cursedtides_markets");
        }

        void nudge(String k, double d) {
            pressure.put(k, MathHelper.clamp(pressure.getOrDefault(k, 0.0) + d, MIN_P, MAX_P));
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
        public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
            NbtCompound p = new NbtCompound();
            pressure.forEach(p::putDouble);
            nbt.put("Pressure", p);
            NbtList list = new NbtList();
            for (Event e : events) {
                NbtCompound c = new NbtCompound();
                c.putInt("Port", e.port);
                c.putInt("Good", e.good);
                c.putDouble("Mult", e.mult);
                c.putLong("Expires", e.expires);
                list.add(c);
            }
            nbt.put("Events", list);
            return nbt;
        }

        private static State fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
            State s = new State();
            NbtCompound p = nbt.getCompound("Pressure");
            for (String k : p.getKeys()) s.pressure.put(k, p.getDouble(k));
            NbtList list = nbt.getList("Events", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < list.size(); i++) {
                NbtCompound c = list.getCompound(i);
                s.events.add(new Event(c.getInt("Port"), c.getInt("Good"), c.getDouble("Mult"), c.getLong("Expires")));
            }
            return s;
        }
    }
}
