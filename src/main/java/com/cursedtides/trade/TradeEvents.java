package com.cursedtides.trade;

import com.cursedtides.item.ModItems;
import com.cursedtides.living.Prosperity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * World events that come out of the markets. A shortage becomes a relief run: the port pays a bonus on every crate
 * of the missing good until the need is met, and the biggest contributor is rewarded. A port can be quarantined
 * (its markets shut for a while; the Tidewatch turns ships away). And now and then a good booms everywhere at once.
 */
public final class TradeEvents {
    private TradeEvents() {}

    static final class Relief {
        final int port, good, needed; final long expires;
        int delivered;
        final Map<UUID, Integer> by = new HashMap<>();
        Relief(int port, int good, int needed, long expires) { this.port = port; this.good = good; this.needed = needed; this.expires = expires; }
    }

    private static final List<Relief> RELIEFS = new ArrayList<>();
    private static int quarantinedPort = -1;
    private static long quarantineEnds;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(TradeEvents::tick);
    }

    /** Markets call this when a shortage is declared. */
    static void onShortage(MinecraftServer server, int port, TradeGood g, long expires) {
        Relief r = new Relief(port, g.ordinal(), 20 + server.getOverworld().random.nextInt(21), expires);
        RELIEFS.add(r);
        server.getPlayerManager().broadcast(Text.literal("Relief run! " + Ports.ALL.get(port).island().name() + " needs " + r.needed + " "
                + g.displayName.toLowerCase() + ". Every crate sold there pays a bonus until the need is met.").formatted(Formatting.GOLD), false);
    }

    /** Markets call this after a player sells goods at a port. */
    static void onSold(ServerPlayerEntity p, Ports.Port port, TradeGood g, int count) {
        int pi = Ports.ALL.indexOf(port);
        for (Relief r : List.copyOf(RELIEFS)) {
            if (r.port != pi || r.good != g.ordinal()) continue;
            int counted = Math.min(count, r.needed - r.delivered);
            if (counted <= 0) continue;
            r.delivered += counted;
            r.by.merge(p.getUuid(), counted, Integer::sum);
            Wallet.give(p, counted * 4);
            p.sendMessage(Text.literal("Relief bonus: " + Wallet.format(counted * 4) + " (" + r.delivered + "/" + r.needed + ")").formatted(Formatting.GOLD), true);
            if (r.delivered >= r.needed) finish(p.getServer(), r);
        }
    }

    private static void finish(MinecraftServer server, Relief r) {
        RELIEFS.remove(r);
        Ports.Port port = Ports.ALL.get(r.port);
        Prosperity.adjust(server, port.island().cx(), port.island().cz(), 12, "the relief ships came in");
        UUID best = null;
        int most = 0;
        for (var e : r.by.entrySet()) if (e.getValue() > most) { most = e.getValue(); best = e.getKey(); }
        ServerPlayerEntity hero = best == null ? null : server.getPlayerManager().getPlayer(best);
        server.getPlayerManager().broadcast(Text.literal(port.island().name() + " is relieved!"
                + (hero != null ? " " + hero.getName().getString() + " brought the most, and the town won't forget it." : "")).formatted(Formatting.GREEN), false);
        if (hero != null) {
            hero.giveItemStack(new ItemStack(ModItems.DOUBLOON, 20));
            com.cursedtides.faction.FactionData.adjust(hero, com.cursedtides.faction.Faction.NAVY, 25);
        }
        // Clear the shortage early: the town's been supplied
        Markets.State s = Markets.State.get(server);
        s.events.removeIf(e -> e.port() == r.port && e.good() == r.good);
        s.markDirty();
    }

    public static boolean quarantined(Ports.Port port, ServerWorld world) {
        return quarantinedPort >= 0 && Ports.ALL.indexOf(port) == quarantinedPort && world.getTime() < quarantineEnds;
    }

    private static void tick(MinecraftServer server) {
        if (server.getTicks() % 1200 != 0 || !com.cursedtides.world.ModWorld.isArchipelago()) return;
        ServerWorld world = server.getOverworld();
        long now = world.getTime();
        RELIEFS.removeIf(r -> r.expires < now);
        if (quarantinedPort >= 0 && now >= quarantineEnds) {
            server.getPlayerManager().broadcast(Text.literal("The quarantine at " + Ports.ALL.get(quarantinedPort).island().name()
                    + " is lifted. Its markets reopen.").formatted(Formatting.GREEN), false);
            quarantinedPort = -1;
        }
        if (server.getTicks() % 24000 != 0 || world.random.nextFloat() > 0.5f) return;
        if (world.random.nextBoolean() && quarantinedPort < 0) {
            List<Ports.Port> honest = Ports.ALL.stream().filter(p -> !p.blackMarket()).toList();
            Ports.Port port = honest.get(world.random.nextInt(honest.size()));
            quarantinedPort = Ports.ALL.indexOf(port);
            quarantineEnds = now + 9600;
            Prosperity.adjust(server, port.island().cx(), port.island().cz(), -8, "fever in the harbour");
            server.getPlayerManager().broadcast(Text.literal("Fever at " + port.island().name() + "! The port is quarantined and its markets are shut. "
                    + "Smugglers say the black markets will pay double for spices while it lasts.").formatted(Formatting.RED), false);
            Markets.State s = Markets.State.get(server);
            for (Ports.Port bm : Ports.ALL) if (bm.blackMarket()) s.events.add(new Markets.Event(Ports.ALL.indexOf(bm), TradeGood.SPICES.ordinal(), 2.0, quarantineEnds));
            s.markDirty();
        } else {
            List<TradeGood> honestGoods = new ArrayList<>();
            for (TradeGood g : TradeGood.values()) if (!g.contraband) honestGoods.add(g);
            TradeGood g = honestGoods.get(world.random.nextInt(honestGoods.size()));
            Markets.State s = Markets.State.get(server);
            for (int i = 0; i < Ports.ALL.size(); i++) s.events.add(new Markets.Event(i, g.ordinal(), 1.5, now + 12000));
            s.markDirty();
            server.getPlayerManager().broadcast(Text.literal("A " + g.displayName.toLowerCase() + " boom! Every port in the Reach is paying half again for it today.")
                    .formatted(Formatting.GOLD), false);
        }
    }
}
