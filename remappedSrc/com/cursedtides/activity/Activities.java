package com.cursedtides.activity;

import com.cursedtides.dialogue.Specials;

import java.util.List;

/** Registers every pastime and the conversation hooks that start them. */
public final class Activities {
    private Activities() {}

    public static void register() {
        Deliveries.init();
        Leaderboards.register();
        Challenges.register();
        Arena.register();
        ShootingRange.register();
        StackRun.register();
        Emotes.register();
        CannonDefense.register();
        TreasureSets.register();
        MiniGames.register();
        PrivateerWar.register();
        Lookout.register();

        Specials.register("defense", CannonDefense::start);
        Specials.register("bj_deal", Blackjack::deal);
        Specials.register("bj_hit", (p, npc) -> Blackjack.hit(p));
        Specials.register("bj_stand", (p, npc) -> Blackjack.stand(p));
        Specials.register("privateer", (p, npc) -> { PrivateerWar.join(p); return List.of("Signed and sealed. Off to Bloodwake with you when the call goes up."); });

        Specials.register("arena", Arena::start);
        Specials.register("range", ShootingRange::start);
        Specials.register("course", StackRun::start);
        Specials.register("drink_start", DrinkingContest::start);
        Specials.register("drink_round", (p, npc) -> DrinkingContest.round(p));
        Specials.register("drink_concede", (p, npc) -> DrinkingContest.concede(p));
        Specials.register("parcel", (p, npc) -> {
            if (p.getInventory().countItem(Deliveries.PARCEL) >= 3) return List.of("You're carrying three already. Deliver those first.");
            p.addItem(ParcelItem.make(p, npc));
            return List.of("Here. Sealed, so don't peek. The address is on the label, and so is the pay.");
        });
        for (Leaderboards.Board b : Leaderboards.BOARDS) {
            Specials.register("top_" + b.id(), (p, npc) -> {
                List<String> top = Leaderboards.top(p.getServer(), b.id(), 3);
                if (top.isEmpty()) return List.of("Nobody's name on the board yet. Could be yours.");
                return List.of("The board reads: " + String.join("; ", top) + ".");
            });
        }
        Specials.register("challenges", (p, npc) -> {
            Challenges.show(p);
            return List.of("Today's notice says: " + Challenges.describeDaily(p) + ". The weekly's pinned underneath.");
        });
    }
}
