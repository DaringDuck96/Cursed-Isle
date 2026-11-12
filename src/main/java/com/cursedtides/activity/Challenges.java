package com.cursedtides.activity;

import com.cursedtides.CursedTides;
import com.cursedtides.appearance.Accessories;
import com.cursedtides.item.ModItems;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * One daily and one weekly challenge, the same for everyone, chosen by the real-world date.
 * Progress is per pirate; finishing pays out once.
 */
public final class Challenges {
    private Challenges() {}

    public record Kind(String key, String verb, int daily, int weekly) {}

    public static final List<Kind> KINDS = List.of(
            new Kind("undead", "Put down %d of the cursed", 15, 120),
            new Kind("fish", "Land %d fish", 10, 60),
            new Kind("trade", "Sell %d trade goods at market", 12, 80),
            new Kind("sink", "Sink %d ships", 2, 10),
            new Kind("hunt", "Hunt %d wild creatures", 6, 40),
            new Kind("cook", "Cook %d dishes", 6, 40),
            new Kind("vault", "Open %d dungeon vaults", 1, 5),
            new Kind("duel", "Win %d duels", 1, 5),
            new Kind("arena", "Survive %d tournament waves", 3, 15),
            new Kind("range", "Hit %d targets at the range", 10, 60),
            new Kind("parcel", "Deliver %d parcels", 1, 6),
            new Kind("bottle", "Find %d collectibles", 1, 4));

    public static final AttachmentType<Map<String, Integer>> PROGRESS = AttachmentRegistry.<Map<String, Integer>>builder()
            .persistent(Codec.unboundedMap(Codec.STRING, Codec.INT)).copyOnDeath().initializer(HashMap::new)
            .buildAndRegister(CursedTides.id("challenges"));

    private static long day() { return LocalDate.now().toEpochDay(); }
    private static long week() { return Math.floorDiv(day() - 4, 7); } // weeks start on Monday

    public static Kind daily() { return KINDS.get(new Random(day() * 31 + 7).nextInt(KINDS.size())); }
    public static Kind weekly() { return KINDS.get(new Random(week() * 131 + 3).nextInt(KINDS.size())); }

    public static String describeDaily(ServerPlayerEntity p) {
        Kind k = daily();
        return String.format(k.verb(), k.daily()).toLowerCase();
    }

    public static void progress(ServerPlayerEntity p, String key, int amount) {
        Map<String, Integer> m = new HashMap<>(p.getAttachedOrElse(PROGRESS, Map.of()));
        boolean changed = false;
        if (daily().key().equals(key)) changed |= step(p, m, "d" + day(), daily().daily(), amount, false);
        if (weekly().key().equals(key)) changed |= step(p, m, "w" + week(), weekly().weekly(), amount, true);
        if (changed) p.setAttached(PROGRESS, m);
    }

    private static boolean step(ServerPlayerEntity p, Map<String, Integer> m, String slot, int goal, int amount, boolean weekly) {
        if (m.getOrDefault(slot + "_done", 0) == 1) return false;
        int now = Math.min(goal, m.getOrDefault(slot, 0) + amount);
        m.put(slot, now);
        if (now >= goal) {
            m.put(slot + "_done", 1);
            reward(p, weekly);
        } else if (now % Math.max(1, goal / 4) == 0) {
            p.sendMessage(Text.literal((weekly ? "Weekly" : "Daily") + " challenge: " + now + "/" + goal).formatted(Formatting.AQUA), true);
        }
        return true;
    }

    private static void reward(ServerPlayerEntity p, boolean weekly) {
        p.sendMessage(Text.literal("✔ " + (weekly ? "Weekly" : "Daily") + " challenge complete!").formatted(Formatting.GREEN, Formatting.BOLD), false);
        p.getWorld().playSound(null, p.getBlockPos(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 0.8f, 1.0f);
        p.giveItemStack(new ItemStack(ModItems.DOUBLOON, weekly ? 30 : 6));
        if (weekly) {
            p.giveItemStack(new ItemStack(Accessories.random(p.getRandom(), true)));
            Achievements.grant(p, "weekly_legend");
        } else {
            Achievements.grant(p, "daily_grind");
        }
    }

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity p = handler.getPlayer();
            // forget anything older than this week
            Map<String, Integer> m = new HashMap<>(p.getAttachedOrElse(PROGRESS, Map.of()));
            m.keySet().removeIf(k -> !(k.startsWith("d" + day()) || k.startsWith("w" + week())));
            p.setAttached(PROGRESS, m);
            show(p);
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> dispatcher.register(literal("challenges").executes(ctx -> {
            show(ctx.getSource().getPlayerOrThrow());
            return 1;
        })));
    }

    public static void show(ServerPlayerEntity p) {
        Map<String, Integer> m = p.getAttachedOrElse(PROGRESS, Map.of());
        Kind d = daily(), w = weekly();
        int dp = m.getOrDefault("d" + day(), 0), wp = m.getOrDefault("w" + week(), 0);
        boolean dd = m.getOrDefault("d" + day() + "_done", 0) == 1, wd = m.getOrDefault("w" + week() + "_done", 0) == 1;
        p.sendMessage(Text.literal("Today: ").formatted(Formatting.AQUA).append(Text.literal(String.format(d.verb(), d.daily())
                + (dd ? "  ✔" : "  (" + dp + "/" + d.daily() + ")")).formatted(dd ? Formatting.GREEN : Formatting.WHITE)), false);
        p.sendMessage(Text.literal("This week: ").formatted(Formatting.AQUA).append(Text.literal(String.format(w.verb(), w.weekly())
                + (wd ? "  ✔" : "  (" + wp + "/" + w.weekly() + ")")).formatted(wd ? Formatting.GREEN : Formatting.WHITE)), false);
    }
}
