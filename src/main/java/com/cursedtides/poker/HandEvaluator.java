package com.cursedtides.poker;

import java.util.Arrays;
import java.util.List;

/**
 * Texas Hold'em hand ranking. Cards are ints 0-51: rank = card / 4 (0 = two ... 12 = ace),
 * suit = card % 4. A score compares hands directly: higher wins, equal splits.
 */
public final class HandEvaluator {
    private HandEvaluator() {}

    public static final String[] CATEGORY = {"High Card", "Pair", "Two Pair", "Three of a Kind", "Straight",
            "Flush", "Full House", "Four of a Kind", "Straight Flush"};

    public static int rank(int card) { return card / 4; }
    public static int suit(int card) { return card % 4; }

    /** Best 5-card score from up to 7 cards. */
    public static long best(List<Integer> cards) {
        int n = cards.size();
        if (n < 5) return scoreFive(cards.stream().mapToInt(Integer::intValue).toArray());
        long best = -1;
        int[] five = new int[5];
        for (int a = 0; a < n; a++)
            for (int b = a + 1; b < n; b++)
                for (int c = b + 1; c < n; c++)
                    for (int d = c + 1; d < n; d++)
                        for (int e = d + 1; e < n; e++) {
                            five[0] = cards.get(a); five[1] = cards.get(b); five[2] = cards.get(c);
                            five[3] = cards.get(d); five[4] = cards.get(e);
                            best = Math.max(best, scoreFive(five));
                        }
        return best;
    }

    public static int category(long score) {
        return (int) (score >> 20);
    }

    /** Score of exactly five cards (fewer works for partial boards: treated as high cards). */
    static long scoreFive(int[] cards) {
        int[] counts = new int[13];
        int[] suits = new int[4];
        for (int c : cards) {
            counts[rank(c)]++;
            suits[suit(c)]++;
        }
        boolean flush = cards.length == 5 && Arrays.stream(suits).anyMatch(s -> s == 5);
        int straightHigh = -1;
        if (cards.length == 5) {
            for (int high = 12; high >= 3; high--) {
                boolean run = true;
                for (int k = 0; k < 5; k++) {
                    int r = high - k;
                    int idx = r < 0 ? 12 : r; // wheel: A-2-3-4-5
                    if (r == -1) idx = 12;
                    if (counts[idx] == 0) { run = false; break; }
                }
                if (run) { straightHigh = high; break; }
            }
        }
        // Ranks ordered by (count, rank) descending, for tie-breaks
        Integer[] order = new Integer[13];
        for (int i = 0; i < 13; i++) order[i] = 12 - i;
        Arrays.sort(order, (x, y) -> counts[y] != counts[x] ? counts[y] - counts[x] : y - x);
        int[] groups = Arrays.stream(order).filter(r -> counts[r] > 0).mapToInt(Integer::intValue).toArray();
        int top = counts[groups[0]];
        int second = groups.length > 1 ? counts[groups[1]] : 0;

        int category;
        long kickers;
        if (straightHigh >= 0 && flush) { category = 8; kickers = straightHigh; }
        else if (top == 4) { category = 7; kickers = pack(groups); }
        else if (top == 3 && second >= 2) { category = 6; kickers = pack(groups); }
        else if (flush) { category = 5; kickers = pack(groups); }
        else if (straightHigh >= 0) { category = 4; kickers = straightHigh; }
        else if (top == 3) { category = 3; kickers = pack(groups); }
        else if (top == 2 && second == 2) { category = 2; kickers = pack(groups); }
        else if (top == 2) { category = 1; kickers = pack(groups); }
        else { category = 0; kickers = pack(groups); }
        return ((long) category << 20) | kickers;
    }

    private static long pack(int[] ranks) {
        long v = 0;
        for (int i = 0; i < 5; i++) {
            v = (v << 4) | (i < ranks.length ? ranks[i] : 0);
        }
        return v;
    }

    public static String cardName(int card) {
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
        String[] suits = {"♠", "♥", "♦", "♣"};
        return ranks[rank(card)] + suits[suit(card)];
    }
}
