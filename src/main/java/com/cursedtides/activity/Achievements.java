package com.cursedtides.activity;

import com.cursedtides.CursedTides;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

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
            "first_contract", "contractor", "letter_reader", "postmaster", "drowned_lights");

    private static AdvancementEntry entry(ServerPlayerEntity p, String id) {
        if (p.getServer() == null) return null;
        return p.getServer().getAdvancementLoader().get(CursedTides.id("feats/" + id));
    }

    public static void grant(ServerPlayerEntity p, String id) {
        AdvancementEntry e = entry(p, id);
        if (e != null) p.getAdvancementTracker().grantCriterion(e, "done");
    }

    public static boolean has(ServerPlayerEntity p, String id) {
        AdvancementEntry e = entry(p, id);
        return e != null && p.getAdvancementTracker().getProgress(e).isDone();
    }

    public static int count(ServerPlayerEntity p) {
        int n = 0;
        for (String id : ALL) if (has(p, id)) n++;
        return n;
    }
}
