package com.cursedtides.poker;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

/**
 * One no-limit Texas Hold'em table: the player in seat 0 and three NPC opponents. Rotating
 * button, blinds, four betting rounds, all-ins with side pots, showdown and split pots.
 * Chips are doubloons. The server drives it; the client only shows the state.
 */
public final class PokerGame {
    public static final int SEATS = 4;
    public static final int SMALL_BLIND = 1, BIG_BLIND = 2;
    public static final int PREFLOP = 0, FLOP = 1, TURN = 2, RIVER = 3, SHOWDOWN = 4;

    static final class Seat {
        String name;
        boolean human;
        java.util.UUID owner;
        boolean leaving;
        int stack;
        int bet;          // this street
        int contributed;  // this hand
        int[] hole = {-1, -1};
        boolean folded, allIn, acted, out;
        double tight, aggression, bluff;
    }

    private static final String[][] OPPONENTS = {
            // name, tight, aggression, bluff
            {"Old Hatch", "0.8", "0.3", "0.05"},
            {"Vesna Quill", "0.4", "0.7", "0.15"},
            {"Bartholomew Pike", "0.3", "0.4", "0.05"},
            {"Lark the Fox", "0.5", "0.6", "0.35"},
            {"Mother Grue", "0.7", "0.5", "0.1"},
            {"Ned Tallowby", "0.2", "0.3", "0.1"},
            {"Ingrid Saltwick", "0.6", "0.8", "0.2"},
            {"Tom Two-Rings", "0.45", "0.5", "0.25"}
    };

    final Seat[] seats = new Seat[SEATS];
    private final List<Integer> deck = new ArrayList<>();
    final List<Integer> community = new ArrayList<>();
    private final Random random = new Random();
    private final Deque<String> log = new ArrayDeque<>();
    int dealer = 0;
    int street = SHOWDOWN;
    int currentBet;
    int minRaise;
    int acting = -1;
    boolean handOver = true;
    boolean revealAll = false;
    int npcDelay = 0;
    private int nextOpponent;

    public PokerGame(String playerName, int buyIn) {
        nextOpponent = random.nextInt(OPPONENTS.length);
        for (int i = 0; i < SEATS; i++) {
            seats[i] = new Seat();
            if (i == 0) {
                seats[i].name = playerName;
                seats[i].human = true;
                seats[i].stack = buyIn;
            } else {
                seatNewOpponent(seats[i]);
            }
        }
        dealer = random.nextInt(SEATS);
        say("You take a seat. Blinds are " + SMALL_BLIND + "/" + BIG_BLIND + ".");
    }

    private void seatNewOpponent(Seat s) {
        String[] o = OPPONENTS[nextOpponent++ % OPPONENTS.length];
        s.name = o[0];
        s.tight = Double.parseDouble(o[1]);
        s.aggression = Double.parseDouble(o[2]);
        s.bluff = Double.parseDouble(o[3]);
        s.stack = 30 + random.nextInt(31);
        s.out = false;
    }

    public int playerStack() {
        return seats[0].stack;
    }

    public boolean playerBroke() {
        return seats[0].stack <= 0 && handOver;
    }

    public int stack(int seat) {
        return seats[seat].stack;
    }

    public java.util.UUID owner(int seat) {
        return seats[seat].owner;
    }

    public boolean isHuman(int seat) {
        return seats[seat].human && !seats[seat].leaving;
    }

    /** Puts a player in a seat held by a house player. Only between hands. Returns the seat, or -1 if none is free. */
    public int seatHuman(java.util.UUID id, String name, int buyIn) {
        if (!handOver) return -1;
        for (int i = 0; i < SEATS; i++) {
            if (!seats[i].human || seats[i].leaving) {
                Seat st = seats[i];
                st.human = true;
                st.leaving = false;
                st.owner = id;
                st.name = name;
                st.stack = buyIn;
                say(name + " takes a seat at the table.");
                return i;
            }
        }
        return -1;
    }

    void seatsOwner(int seat, java.util.UUID id) {
        seats[seat].owner = id;
    }

    public int humans() {
        int n = 0;
        for (Seat st : seats) if (st.human && !st.leaving) n++;
        return n;
    }

    private void say(String line) {
        log.addLast(line);
        while (log.size() > 6) log.removeFirst();
    }

    // ================================================================== hand flow

    public void startHand() {
        if (!handOver || humans() == 0) return;
        for (int i = 0; i < SEATS; i++) {
            if (seats[i].leaving || (seats[i].human && seats[i].stack <= 0)) {
                seats[i].human = false;
                seats[i].leaving = false;
                seats[i].owner = null;
                seatNewOpponent(seats[i]);
                say(seats[i].name + " takes the empty seat.");
            }
        }
        for (int i = 0; i < SEATS; i++) {
            if (!seats[i].human && seats[i].stack <= 0) {
                String gone = seats[i].name;
                seatNewOpponent(seats[i]);
                say(gone + " is cleaned out. " + seats[i].name + " sits down.");
            }
        }
        deck.clear();
        for (int c = 0; c < 52; c++) deck.add(c);
        Collections.shuffle(deck, random);
        community.clear();
        for (Seat s : seats) {
            s.bet = 0;
            s.contributed = 0;
            s.folded = false;
            s.allIn = false;
            s.acted = false;
            s.hole = new int[]{deck.removeLast(), deck.removeLast()};
        }
        handOver = false;
        revealAll = false;
        street = PREFLOP;
        dealer = (dealer + 1) % SEATS;
        int sb = next(dealer, true);
        int bb = next(sb, true);
        post(sb, SMALL_BLIND);
        post(bb, BIG_BLIND);
        currentBet = BIG_BLIND;
        minRaise = BIG_BLIND;
        acting = next(bb, false);
        npcDelay = 20;
        say("New hand. " + seats[dealer].name + " has the button.");
        skipIfStuck();
    }

    private void post(int i, int amount) {
        Seat s = seats[i];
        int paid = Math.min(amount, s.stack);
        s.stack -= paid;
        s.bet += paid;
        s.contributed += paid;
        if (s.stack == 0) s.allIn = true;
    }

    /** Next seat after i that's still in the hand (and, unless anySeat, able to act). */
    private int next(int i, boolean anySeat) {
        for (int k = 1; k <= SEATS; k++) {
            int j = (i + k) % SEATS;
            Seat s = seats[j];
            if (s.folded) continue;
            if (!anySeat && s.allIn) continue;
            return j;
        }
        return -1;
    }

    private int inHand() {
        int n = 0;
        for (Seat s : seats) if (!s.folded) n++;
        return n;
    }

    private int canAct() {
        int n = 0;
        for (Seat s : seats) if (!s.folded && !s.allIn) n++;
        return n;
    }

    public int toCall(int i) {
        return Math.max(0, currentBet - seats[i].bet);
    }

    // ================================================================== actions

    public static final int FOLD = 0, CHECK_CALL = 1, RAISE = 2, ALL_IN = 3;

    /** Applies an action for the seat whose turn it is. Raise amount = the new total bet for the street. */
    public boolean act(int seat, int action, int raiseTo) {
        if (handOver || seat != acting) return false;
        Seat s = seats[seat];
        int call = toCall(seat);
        switch (action) {
            case FOLD -> {
                if (call == 0) { // folding for free is just a check
                    s.acted = true;
                    say(s.name + " checks.");
                    break;
                }
                s.folded = true;
                say(s.name + " folds.");
            }
            case CHECK_CALL -> {
                if (call == 0) {
                    say(s.name + " checks.");
                } else {
                    int paid = Math.min(call, s.stack);
                    pay(s, paid);
                    say(s.name + (s.allIn ? " calls all-in (" + paid + ")." : " calls " + paid + "."));
                }
                s.acted = true;
            }
            case RAISE, ALL_IN -> {
                int maxTo = s.bet + s.stack;
                int target = action == ALL_IN ? maxTo : Math.max(raiseTo, currentBet + minRaise);
                target = Math.min(target, maxTo);
                if (target <= currentBet) { // can't raise: it's a call
                    return act(seat, CHECK_CALL, 0);
                }
                int raiseBy = target - currentBet;
                pay(s, target - s.bet);
                if (raiseBy >= minRaise) {
                    minRaise = raiseBy;
                    for (Seat o : seats) if (o != s) o.acted = false; // a full raise reopens the betting
                }
                currentBet = target;
                s.acted = true;
                say(s.name + (s.allIn ? " goes all-in for " + target + "!" : " raises to " + target + "."));
            }
            default -> { return false; }
        }
        afterAction();
        return true;
    }

    private void pay(Seat s, int amount) {
        amount = Math.min(amount, s.stack);
        s.stack -= amount;
        s.bet += amount;
        s.contributed += amount;
        if (s.stack == 0) s.allIn = true;
    }

    private void afterAction() {
        if (inHand() == 1) {
            awardUncontested();
            return;
        }
        if (roundComplete()) {
            advanceStreet();
        } else {
            acting = nextNeedingAction(acting);
            npcDelay = 15;
        }
    }

    /** The next seat that still owes a decision this street. */
    private int nextNeedingAction(int i) {
        for (int k = 1; k <= SEATS; k++) {
            int j = (i + k) % SEATS;
            Seat s = seats[j];
            if (s.folded || s.allIn) continue;
            if (!s.acted || s.bet < currentBet) return j;
        }
        return next(i, false);
    }

    private boolean roundComplete() {
        for (Seat s : seats) {
            if (s.folded || s.allIn) continue;
            if (!s.acted || s.bet != currentBet) return false;
        }
        return true;
    }

    private void advanceStreet() {
        for (Seat s : seats) {
            s.bet = 0;
            s.acted = false;
        }
        currentBet = 0;
        minRaise = BIG_BLIND;
        street++;
        switch (street) {
            case FLOP -> {
                deck.removeLast(); // burn
                for (int i = 0; i < 3; i++) community.add(deck.removeLast());
                say("Flop: " + cards(community));
            }
            case TURN, RIVER -> {
                deck.removeLast();
                int card = deck.removeLast();
                community.add(card);
                say((street == TURN ? "Turn: " : "River: ") + HandEvaluator.cardName(card));
            }
            default -> {
                showdown();
                return;
            }
        }
        acting = next(dealer, false);
        npcDelay = 25;
        skipIfStuck();
    }

    /** With fewer than two players able to bet, deal the rest of the board without stopping. */
    private void skipIfStuck() {
        if (!handOver && canAct() <= 1 && roundCompleteForStuck()) {
            revealAll = true;
            advanceStreet();
        }
    }

    private boolean roundCompleteForStuck() {
        for (Seat s : seats) {
            if (s.folded || s.allIn) continue;
            if (s.bet < currentBet) return false; // the last free player still owes a call
        }
        return true;
    }

    private void awardUncontested() {
        int pot = pot();
        for (Seat s : seats) {
            if (!s.folded) {
                s.stack += pot;
                say(s.name + " takes the pot of " + pot + ".");
            }
        }
        endHand();
    }

    private void showdown() {
        street = SHOWDOWN;
        revealAll = true;
        long[] scores = new long[SEATS];
        for (int i = 0; i < SEATS; i++) {
            if (seats[i].folded) continue;
            List<Integer> seven = new ArrayList<>(community);
            seven.add(seats[i].hole[0]);
            seven.add(seats[i].hole[1]);
            scores[i] = HandEvaluator.best(seven);
            say(seats[i].name + ": " + HandEvaluator.CATEGORY[HandEvaluator.category(scores[i])]);
        }
        // Side pots: slice the contributions level by level
        int[] left = new int[SEATS];
        for (int i = 0; i < SEATS; i++) left[i] = seats[i].contributed;
        while (true) {
            int level = Integer.MAX_VALUE;
            for (int v : left) if (v > 0) level = Math.min(level, v);
            if (level == Integer.MAX_VALUE) break;
            int slice = 0;
            List<Integer> eligible = new ArrayList<>();
            for (int i = 0; i < SEATS; i++) {
                if (left[i] <= 0) continue;
                slice += Math.min(level, left[i]);
                if (!seats[i].folded) eligible.add(i);
                left[i] -= Math.min(level, left[i]);
            }
            if (eligible.isEmpty()) {
                // everyone in this slice folded: return it to the last caller standing
                for (int i = 0; i < SEATS; i++) if (!seats[i].folded) { eligible.add(i); break; }
            }
            long bestScore = -1;
            for (int i : eligible) bestScore = Math.max(bestScore, scores[i]);
            List<Integer> winners = new ArrayList<>();
            for (int i : eligible) if (scores[i] == bestScore) winners.add(i);
            int share = slice / winners.size();
            int odd = slice - share * winners.size();
            for (int w : winners) seats[w].stack += share;
            seats[winners.get(0)].stack += odd;
            StringBuilder names = new StringBuilder();
            for (int w : winners) names.append(names.isEmpty() ? "" : " & ").append(seats[w].name);
            say(names + (winners.size() > 1 ? " split " : " wins ") + slice
                    + " with " + HandEvaluator.CATEGORY[HandEvaluator.category(bestScore)] + ".");
        }
        endHand();
    }

    private void endHand() {
        handOver = true;
        acting = -1;
        for (Seat s : seats) s.bet = 0;
    }

    public int pot() {
        int total = 0;
        for (Seat s : seats) total += s.contributed;
        return total;
    }

    private static String cards(List<Integer> list) {
        StringBuilder sb = new StringBuilder();
        for (int c : list) sb.append(sb.isEmpty() ? "" : " ").append(HandEvaluator.cardName(c));
        return sb.toString();
    }

    // ================================================================== NPC play

    /** Called every server tick; lets the NPC whose turn it is think, then act. */
    private int humanWait;
    private int lastActing = -2;

    public boolean tick() {
        if (handOver || acting < 0) return false;
        if (acting != lastActing) {
            lastActing = acting;
            humanWait = 0;
        }
        if (seats[acting].human && !seats[acting].leaving) {
            // A real player gets 45 seconds before the table acts for them
            if (++humanWait < 900) return false;
            say(seats[acting].name + " took too long.");
            act(acting, toCall(acting) == 0 ? CHECK_CALL : FOLD, 0);
            return true;
        }
        if (--npcDelay > 0) return false;
        npcDecide(acting);
        return true;
    }

    private void npcDecide(int i) {
        Seat s = seats[i];
        double strength = strength(s);
        int call = toCall(i);
        int pot = pot();
        double roll = random.nextDouble();
        boolean bluffing = roll < s.bluff;

        if (call == 0) {
            if (strength > 0.62 - s.aggression * 0.2 || bluffing) {
                act(i, RAISE, currentBet + raiseSize(s, pot, strength));
            } else {
                act(i, CHECK_CALL, 0);
            }
            return;
        }
        double potOdds = call / (double) (pot + call);
        if (strength < potOdds + s.tight * 0.25 && !bluffing) {
            act(i, FOLD, 0);
        } else if ((strength > 0.78 - s.aggression * 0.15 || (bluffing && random.nextBoolean())) && s.stack > call) {
            if (strength > 0.9 && random.nextDouble() < s.aggression) {
                act(i, ALL_IN, 0);
            } else {
                act(i, RAISE, currentBet + raiseSize(s, pot, strength));
            }
        } else {
            act(i, CHECK_CALL, 0);
        }
    }

    private int raiseSize(Seat s, int pot, double strength) {
        int base = street == PREFLOP ? BIG_BLIND * (2 + random.nextInt(2)) : Math.max(BIG_BLIND, pot / 2);
        if (strength > 0.85) base = Math.max(base, pot);
        return Math.max(minRaise, base);
    }

    /** Rough hand strength, 0 to 1, from what this seat can see. */
    private double strength(Seat s) {
        int r1 = HandEvaluator.rank(s.hole[0]), r2 = HandEvaluator.rank(s.hole[1]);
        boolean suited = HandEvaluator.suit(s.hole[0]) == HandEvaluator.suit(s.hole[1]);
        if (community.isEmpty()) {
            double v = (r1 + r2) / 24.0 * 0.5;
            if (r1 == r2) v = 0.5 + r1 / 24.0;
            if (suited) v += 0.05;
            if (Math.abs(r1 - r2) == 1) v += 0.03;
            return Math.min(1.0, v + random.nextDouble() * 0.08);
        }
        List<Integer> cards = new ArrayList<>(community);
        cards.add(s.hole[0]);
        cards.add(s.hole[1]);
        long score = HandEvaluator.best(cards);
        int cat = HandEvaluator.category(score);
        double[] base = {0.15, 0.42, 0.65, 0.75, 0.82, 0.86, 0.92, 0.97, 0.99};
        double v = base[cat];
        if (cat == 0) v += Math.max(r1, r2) / 60.0;
        if (cat == 1) {
            int boardHigh = community.stream().mapToInt(HandEvaluator::rank).max().orElse(0);
            if (r1 == boardHigh || r2 == boardHigh || (r1 == r2 && r1 > boardHigh)) v += 0.12; // top pair or overpair
        }
        if (street < RIVER && cat < 4) {
            int[] suitCount = new int[4];
            for (int c : cards) suitCount[HandEvaluator.suit(c)]++;
            for (int n : suitCount) if (n == 4) v += 0.12; // flush draw
        }
        return Math.min(1.0, v + random.nextDouble() * 0.06);
    }

    // ================================================================== player leaves mid-hand

    public void playerLeaves() {
        playerLeaves(0);
    }

    /** A player gets up: folds if mid-hand; the seat goes back to the house at the next deal. */
    public void playerLeaves(int seat) {
        Seat st = seats[seat];
        if (!handOver && !st.folded) {
            if (acting == seat) act(seat, FOLD, 0);
            else st.folded = true;
        }
        st.stack = 0;
        st.leaving = true;
        say(st.name + " leaves the table.");
    }

    // ================================================================== state for the screen

    public CompoundTag toNbt() {
        return toNbt(0);
    }

    /** The table as seen from one seat: that seat is drawn at the bottom (index 0) and only its cards show. */
    public CompoundTag toNbt(int viewer) {
        CompoundTag nbt = new CompoundTag();
        ListTag names = new ListTag();
        int[] stacks = new int[SEATS], bets = new int[SEATS], flags = new int[SEATS], shown = new int[SEATS * 2];
        for (int d = 0; d < SEATS; d++) {
            int i = (viewer + d) % SEATS;
            Seat s = seats[i];
            names.add(StringTag.valueOf(s.name + (s.human && i != viewer ? " ★" : "")));
            stacks[d] = s.stack;
            bets[d] = s.bet;
            flags[d] = (s.folded ? 1 : 0) | (s.allIn ? 2 : 0);
            boolean visible = i == viewer || (revealAll && !s.folded);
            shown[d * 2] = visible ? s.hole[0] : -1;
            shown[d * 2 + 1] = visible ? s.hole[1] : -1;
        }
        nbt.put("Names", names);
        nbt.putIntArray("Stacks", stacks);
        nbt.putIntArray("Bets", bets);
        nbt.putIntArray("Flags", flags);
        nbt.putIntArray("Cards", shown);
        nbt.putIntArray("Board", community.stream().mapToInt(Integer::intValue).toArray());
        nbt.putInt("Pot", pot());
        nbt.putInt("ToCall", acting == viewer ? toCall(viewer) : 0);
        nbt.putInt("MinRaiseTo", currentBet + minRaise);
        nbt.putInt("CurrentBet", currentBet);
        nbt.putInt("Acting", acting < 0 ? acting : (acting - viewer + SEATS) % SEATS);
        nbt.putInt("Dealer", (dealer - viewer + SEATS) % SEATS);
        nbt.putBoolean("HandOver", handOver);
        nbt.putString("Log", String.join("\n", log));
        return nbt;
    }
}
