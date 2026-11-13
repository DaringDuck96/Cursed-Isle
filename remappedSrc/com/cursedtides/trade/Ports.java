package com.cursedtides.trade;

import com.cursedtides.world.Archipelago;
import com.cursedtides.world.Archipelago.Island;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.cursedtides.trade.TradeGood.*;

/** What each port grows, makes, and goes short of. Black markets deal in contraband too. */
public final class Ports {
    private Ports() {}

    public enum Kind { PRODUCES, DEMANDS, NEUTRAL }

    public record Port(Island island, Set<TradeGood> produces, Set<TradeGood> demands, boolean blackMarket) {
        public Kind kind(TradeGood g) {
            if (produces.contains(g)) return Kind.PRODUCES;
            if (demands.contains(g)) return Kind.DEMANDS;
            return Kind.NEUTRAL;
        }

        public boolean deals(TradeGood g) {
            return blackMarket || !g.contraband;
        }

        public double factor(TradeGood g) {
            return switch (kind(g)) {
                case PRODUCES -> g.contraband ? 0.7 : 0.6;
                case DEMANDS -> g.contraband ? 1.9 : 1.6;
                case NEUTRAL -> g.contraband ? 1.2 : 1.0;
            };
        }
    }

    private static Port port(Island island, Set<TradeGood> produces, Set<TradeGood> demands, boolean black) {
        return new Port(island, produces, demands, black);
    }

    public static final List<Port> ALL = List.of(
            port(Archipelago.SALTMARROW, EnumSet.of(RUM, SALT_FISH), EnumSet.of(SILK, IRON, TIMBER), false),
            port(Archipelago.PORT_MERIDIAN, EnumSet.of(COTTON, SUGAR), EnumSet.of(SPICES, COFFEE, RUM), false),
            port(Archipelago.KINGSPORT, EnumSet.of(IRON, SILK), EnumSet.of(SUGAR, COFFEE, RUM, COTTON), false),
            port(Archipelago.MIREFEN_BAYOU, EnumSet.of(SUGAR, COFFEE), EnumSet.of(SALT_FISH, IRON), false),
            port(Archipelago.MARINERS_REST, EnumSet.of(SALT_FISH, TIMBER), EnumSet.of(RUM, COTTON, SUGAR), false),
            port(Archipelago.SAINT_VESPER, EnumSet.of(SPICES, COFFEE), EnumSet.of(SALT_FISH, SILK, TIMBER), false),
            port(Archipelago.GOLDWATER_CAY, EnumSet.of(IRON), EnumSet.of(RUM, SALT_FISH, TIMBER, COFFEE), false),
            port(Archipelago.CROWNWATCH, EnumSet.noneOf(TradeGood.class), EnumSet.of(TIMBER, IRON, SALT_FISH, COTTON), false),
            port(Archipelago.RAVENS_LANDING, EnumSet.of(UNTAXED_RUM, POWDER), EnumSet.of(CURSED_IDOL, STOLEN_PLATE, SILK), true),
            port(Archipelago.BLACKTHORN_ISLE, EnumSet.of(STOLEN_PLATE, SPICES), EnumSet.of(UNTAXED_RUM, POWDER, CURSED_IDOL), true)
    );

    /** A neutral market for traders outside the known ports (or outside the archipelago). */
    public static final Port NOWHERE = new Port(Archipelago.DRIFTWOOD_ISLAND, EnumSet.noneOf(TradeGood.class),
            EnumSet.noneOf(TradeGood.class), false);

    public static Port at(int x, int z) {
        int index = Archipelago.islandIndexAt(x, z);
        if (index < 0) return NOWHERE;
        Island island = Archipelago.ISLANDS.get(index);
        for (Port p : ALL) {
            if (p.island() == island) return p;
        }
        return NOWHERE;
    }

    public static int index(Port p) {
        return p == NOWHERE ? -1 : ALL.indexOf(p);
    }
}
