package com.cursedtides.activity;

import com.cursedtides.profession.Profession;
import com.cursedtides.profession.ProfessionData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

/**
 * Tankard for tankard against the tavern's house champion. Each round your head swims more; a Brewing
 * background helps you hold it. The champion has a limit too, somewhere between five and ten.
 */
public final class DrinkingContest {
    private DrinkingContest() {}

    private static final String[] CHAMPIONS = {"Big Hob", "Mother Ganny", "Stoat Pellow", "The Twins (both of them)", "Captain Dregs"};

    private record Bout(int round, int champLimit, String champ) {}
    private static final Map<UUID, Bout> BOUTS = new HashMap<>();

    public static boolean active(ServerPlayer p) {
        return BOUTS.containsKey(p.getUUID());
    }

    public static List<String> start(ServerPlayer p, Entity keeper) {
        if (active(p)) return List.of("You're already at the table. Drink or yield!");
        if (!com.cursedtides.trade.Wallet.pay(p, 20)) return List.of("Two doubloons buys your seat and the first round. You're short.");
        String champ = CHAMPIONS[p.getRandom().nextInt(CHAMPIONS.length)];
        BOUTS.put(p.getUUID(), new Bout(0, 5 + p.getRandom().nextInt(6), champ));
        return List.of("Clear the table! " + champ + " takes the challenge.", champ + " cracks their knuckles and slides a tankard your way.");
    }

    public static List<String> round(ServerPlayer p) {
        Bout b = BOUTS.get(p.getUUID());
        if (b == null) return List.of("You've no contest going. Pay your two doubloons first.");
        int round = b.round() + 1;
        p.level().playSound(null, p.blockPosition(), SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 1f, 0.9f);
        p.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120 + round * 60, 0));
        if (round >= 4) p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, round / 4));
        int brewing = ProfessionData.level(p, Profession.BREWING);
        double fall = Math.max(0, (round - 3) * 0.12 - brewing * 0.008);
        if (p.getRandom().nextDouble() < fall) {
            BOUTS.remove(p.getUUID());
            p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
            return List.of("Round " + round + ". The room tilts. The floor comes up to meet you, gently.",
                    "When you wake, " + b.champ() + " is singing on the bar. Better luck next time.");
        }
        if (round >= b.champLimit()) {
            BOUTS.remove(p.getUUID());
            Leaderboards.add(p, "drinking", 1);
            Achievements.grant(p, "tavern_champion");
            p.addItem(new ItemStack(com.cursedtides.item.ModItems.DOUBLOON, 10 + round * 2));
            ProfessionData.addXp(p, Profession.BREWING, 30);
            p.level().playSound(null, p.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 0.8f);
            return List.of("Round " + round + ". " + b.champ() + " raises the tankard... sways... and goes over backwards like a felled mast!",
                    "The tavern roars. The purse is yours, and a song will be made about this. A bad one.");
        }
        BOUTS.put(p.getUUID(), new Bout(round, b.champLimit(), b.champ()));
        List<String> out = new ArrayList<>();
        out.add("Round " + round + ". Down in one. " + b.champ() + " matches you" + (round > b.champLimit() - 2 ? ", but their eyes are crossing." : ", grinning."));
        if (round >= 3) out.add(round >= 6 ? "Your own hands have started to look like someone else's." : "The candles are doubling.");
        return out;
    }

    public static List<String> concede(ServerPlayer p) {
        Bout b = BOUTS.remove(p.getUUID());
        return List.of(b == null ? "Nothing to concede." : b.champ() + " accepts your surrender with a belch of great dignity.");
    }
}
