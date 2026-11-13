package com.cursedtides.social;

import com.cursedtides.CursedTides;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import java.util.ArrayList;
import java.util.List;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/**
 * Titles you've earned, worn under your name: story endings (Commodore of the Reach, Lightkeeper...), feats, your
 * infamy ranks and festival honours. /title lists them; click one to wear it.
 */
public final class Titles {
    private Titles() {}

    public static final AttachmentType<String> WORN = AttachmentRegistry.<String>builder().persistent(Codec.STRING).copyOnDeath()
            .initializer(() -> "").buildAndRegister(CursedTides.id("title_worn"));
    /** Titles given outright (festival honours), kept as a list. */
    public static final AttachmentType<List<String>> EXTRA = AttachmentRegistry.<List<String>>builder()
            .persistent(Codec.STRING.listOf()).copyOnDeath().initializer(List::of).buildAndRegister(CursedTides.id("titles_extra"));

    public static void grant(ServerPlayer p, String title) {
        List<String> l = new ArrayList<>(p.getAttachedOrElse(EXTRA, List.of()));
        if (l.contains(title)) return;
        l.add(title);
        p.setAttached(EXTRA, l);
        p.sendSystemMessage(Component.literal("New title: " + title + " (wear it with /title)").withStyle(ChatFormatting.GOLD), false);
    }

    /** Every title this pirate has earned. */
    public static List<String> earned(ServerPlayer p) {
        List<String> t = new ArrayList<>();
        String crown = com.cursedtides.quest.CrownArc.title(p);
        if (crown != null) t.add(crown);
        String horizon = com.cursedtides.quest.HorizonArc.horizonTitle(p);
        if (horizon != null) t.add(horizon);
        int lantern = com.cursedtides.quest.LanternArc.ending(p);
        if (lantern == com.cursedtides.quest.LanternArc.RETURNED) t.add("Lightkeeper of the Reach");
        if (lantern == com.cursedtides.quest.LanternArc.KEPT) t.add("Heart of the Abyss");
        int muster = com.cursedtides.quest.MusterArc.ending(p);
        if (muster == com.cursedtides.quest.MusterArc.NAVY_ENDING) t.add("Commodore of the Reach");
        if (muster == com.cursedtides.quest.MusterArc.BRETHREN_ENDING) t.add("Scourge of the Muster");
        String[][] feats = {{"pillar_of_the_reach", "Pillar of the Reach"}, {"all_delves", "Delver of the Deep"},
                {"sea_monster", "Monster Hunter"}, {"boarder", "Boarding Master"}, {"hundred_arms", "Bane of Hundred-Arms"},
                {"arena_champion", "Arena Champion"}, {"tavern_champion", "Tavern Champion"}, {"seven_relics", "Keeper of the Seven Relics"},
                {"hidden_isle", "Finder of the Hidden Isle"}, {"privateer", "Licensed Privateer"}};
        for (String[] f : feats) if (com.cursedtides.activity.Achievements.has(p, f[0])) t.add(f[1]);
        int l = Infamy.landRank(p), s = Infamy.seaRank(p);
        if (l >= 3) t.add(Infamy.LAND_RANKS[l]);
        if (s >= 3) t.add(Infamy.SEA_RANKS[s]);
        t.addAll(p.getAttachedOrElse(EXTRA, List.of()));
        return t;
    }

    /** The title worn, if it's still one they've earned. */
    public static String worn(ServerPlayer p) {
        String w = p.getAttachedOrElse(WORN, "");
        return w.isEmpty() || !earned(p).contains(w) ? null : w;
    }

    public static void register() {
        WORN.identifier();
        EXTRA.identifier();
        CommandRegistrationCallback.EVENT.register((d, a, env) -> d.register(literal("title")
                .executes(ctx -> { list(ctx.getSource().getPlayerOrException()); return 1; })
                .then(literal("none").executes(ctx -> { set(ctx.getSource().getPlayerOrException(), ""); return 1; }))
                .then(literal("wear").then(argument("n", IntegerArgumentType.integer(1)).executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    List<String> t = earned(p);
                    int n = IntegerArgumentType.getInteger(ctx, "n");
                    if (n <= t.size()) set(p, t.get(n - 1));
                    return 1;
                })))));
    }

    private static void set(ServerPlayer p, String title) {
        p.setAttached(WORN, title);
        p.sendSystemMessage(Component.literal(title.isEmpty() ? "You go untitled." : "You'll be known as " + title + ".").withStyle(ChatFormatting.GOLD), false);
        Nameplates.refresh(p);
    }

    private static void list(ServerPlayer p) {
        List<String> t = earned(p);
        if (t.isEmpty()) {
            p.sendSystemMessage(Component.literal("No titles yet. The story, feats, infamy and festivals all give them.").withStyle(ChatFormatting.GRAY), false);
            return;
        }
        p.sendSystemMessage(Component.literal("Your titles (click to wear):").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        String worn = worn(p);
        for (int i = 0; i < t.size(); i++) {
            final int n = i + 1;
            String title = t.get(i);
            p.sendSystemMessage(Component.literal((title.equals(worn) ? "  » " : "  ") + title).withStyle(st -> st.withColor(title.equals(worn) ? ChatFormatting.YELLOW : ChatFormatting.WHITE)
                    .withClickEvent(new ClickEvent.RunCommand("/title wear " + n))), false);
        }
        p.sendSystemMessage(Component.literal("  [Wear none]").withStyle(st -> st.withColor(ChatFormatting.GRAY)
                .withClickEvent(new ClickEvent.RunCommand("/title none"))), false);
    }
}
