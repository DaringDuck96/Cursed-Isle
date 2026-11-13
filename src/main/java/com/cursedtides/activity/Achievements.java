package com.cursedtides.activity;

import com.cursedtides.CursedTides;
import java.util.List;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerPlayer;

/**
 * Feats, shown in the vanilla advancements screen under their own tab. Each is an advancement with a single
 * "done" criterion that the mod grants when the deed is done.
 */
public final class Achievements {
    private Achievements() {}

    public static final List<String> ALL = List.of(
            "first_blood", "sink_ship", "capture_ship", "first_catch", "golden_marlin", "first_vault", "ten_vaults", "all_islands",
            "wanted", "bounty_claimed", "duel_won", "crew_founded", "homeowner", "tavern_champion", "arena_champion", "sharpshooter",
            "stack_runner", "dressed_to_kill", "ultimate", "festival", "collector", "encyclopedist", "seven_relics", "horizon",
            "sunken_city", "deep_diver", "legendary_find", "daily_grind", "weekly_legend", "delivery",
            "fort_breaker", "hollow_crown", "hidden_isle", "storm_caller",
            "hundred_arms", "dread_sunk", "harbour_guard", "collector_of_sets", "privateer", "siege_breaker",
            "spelunker", "cavern_master", "deep_delver",
            "first_contract", "contractor", "letter_reader", "postmaster", "drowned_lights",
            "boarder", "sea_monster", "black_muster", "wreck_diver", "spire_climber", "all_delves", "errand_runner", "pillar_of_the_reach");

    private static AdvancementHolder entry(ServerPlayer p, String id) {
        if (p.level().getServer() == null) return null;
        return p.level().getServer().getAdvancements().get(CursedTides.id("feats/" + id));
    }

    public static void grant(ServerPlayer p, String id) {
        AdvancementHolder e = entry(p, id);
        if (e != null) p.getAdvancements().award(e, "done");
    }

    public static boolean has(ServerPlayer p, String id) {
        AdvancementHolder e = entry(p, id);
        return e != null && p.getAdvancements().getOrStartProgress(e).isDone();
    }

    public static int count(ServerPlayer p) {
        int n = 0;
        for (String id : ALL) if (has(p, id)) n++;
        return n;
    }
}
