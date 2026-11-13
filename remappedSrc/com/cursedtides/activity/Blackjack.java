package com.cursedtides.activity;

import com.cursedtides.item.ModItems;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

/** Twenty-One at the card table: beat the dealer without going over. Played through conversation. */
public final class Blackjack {
    private Blackjack() {}

    private static final String[] RANKS = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
    private static final String[] SUITS = {"♠", "♥", "♦", "♣"};

    private static final class Hand {
        final List<Integer> deck = new ArrayList<>(), mine = new ArrayList<>(), house = new ArrayList<>();
        final int bet;
        Hand(int bet, Random r) {
            this.bet = bet;
            for (int i = 0; i < 52; i++) deck.add(i);
            java.util.Collections.shuffle(deck, r);
        }
        int draw() { return deck.remove(deck.size() - 1); }
    }
    private static final Map<UUID, Hand> HANDS = new HashMap<>();

    private static int value(List<Integer> cards) {
        int total = 0, aces = 0;
        for (int c : cards) {
            int r = c % 13;
            if (r == 0) { aces++; total += 11; } else total += Math.min(10, r + 1);
        }
        while (total > 21 && aces-- > 0) total -= 10;
        return total;
    }

    private static String show(List<Integer> cards) {
        StringBuilder sb = new StringBuilder();
        for (int c : cards) sb.append(sb.isEmpty() ? "" : " ").append(RANKS[c % 13]).append(SUITS[c / 13]);
        return sb + " (" + value(cards) + ")";
    }

    public static boolean playing(ServerPlayer p) { return HANDS.containsKey(p.getUUID()); }

    public static List<String> deal(ServerPlayer p, Entity dealer) {
        if (playing(p)) return status(p);
        if (p.getInventory().countItem(ModItems.DOUBLOON) < 5) return List.of("Five doubloons a hand, friend.");
        p.getInventory().clearOrCountMatchingItems(s -> s.is(ModItems.DOUBLOON), 5, p.inventoryMenu.getCraftSlots());
        Hand h = new Hand(5, new Random(p.getRandom().nextLong()));
        h.mine.add(h.draw()); h.house.add(h.draw()); h.mine.add(h.draw()); h.house.add(h.draw());
        HANDS.put(p.getUUID(), h);
        if (value(h.mine) == 21) return finish(p, "Twenty-one on the deal!");
        return status(p);
    }

    private static List<String> status(ServerPlayer p) {
        Hand h = HANDS.get(p.getUUID());
        return List.of("Your cards: " + show(h.mine) + ".   Dealer shows: " + RANKS[h.house.get(0) % 13] + SUITS[h.house.get(0) / 13] + " and one face down.");
    }

    public static List<String> hit(ServerPlayer p) {
        Hand h = HANDS.get(p.getUUID());
        if (h == null) return List.of("You've no hand in play.");
        h.mine.add(h.draw());
        if (value(h.mine) > 21) {
            HANDS.remove(p.getUUID());
            return List.of("You draw... " + show(h.mine) + ". Bust! The house takes your five.");
        }
        if (value(h.mine) == 21) return finish(p, "Twenty-one!");
        return status(p);
    }

    public static List<String> stand(ServerPlayer p) {
        return HANDS.containsKey(p.getUUID()) ? finish(p, "You stand.") : List.of("You've no hand in play.");
    }

    private static List<String> finish(ServerPlayer p, String opener) {
        Hand h = HANDS.remove(p.getUUID());
        while (value(h.house) < 17) h.house.add(h.draw());
        int me = value(h.mine), them = value(h.house);
        boolean natural = me == 21 && h.mine.size() == 2;
        String verdict;
        int pay = 0;
        if (them > 21 || me > them) {
            pay = natural ? h.bet * 5 / 2 + h.bet : h.bet * 2;
            verdict = "You win " + pay + " doubloons!";
        } else if (me == them) {
            pay = h.bet;
            verdict = "A push. Your stake comes back.";
        } else {
            verdict = "The house wins.";
        }
        if (pay > 0) p.addItem(new ItemStack(ModItems.DOUBLOON, pay));
        return List.of(opener + " Your cards: " + show(h.mine) + ". Dealer: " + show(h.house) + ".", verdict);
    }
}
