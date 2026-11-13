package com.cursedtides.social;

import com.cursedtides.CursedTides;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import java.util.ArrayList;
import java.util.List;

/**
 * Land and sea infamy: the old pirate game's ranks for fighting other pirates. Win duels and best pirates ashore
 * for land infamy; sink other pirates' ships for sea infamy. Each has eight ranks with a name, and a badge by
 * your name (a sword for land, an anchor for sea) once you're past the first.
 */
public final class Infamy {
    private Infamy() {}

    public static final AttachmentType<Integer> LAND = AttachmentRegistry.<Integer>builder().persistent(Codec.INT).copyOnDeath()
            .initializer(() -> 0).buildAndRegister(CursedTides.id("land_infamy"));
    public static final AttachmentType<Integer> SEA = AttachmentRegistry.<Integer>builder().persistent(Codec.INT).copyOnDeath()
            .initializer(() -> 0).buildAndRegister(CursedTides.id("sea_infamy"));

    public static final int[] AT = {0, 50, 150, 300, 600, 1000, 1600, 2500};
    public static final String[] LAND_RANKS = {"Landlubber", "Scrapper", "Brawler", "Duelist", "Swashbuckler", "Blademaster",
            "Terror of the Isles", "Legend of the Land"};
    public static final String[] SEA_RANKS = {"Bilge Rat", "Raider", "Privateer", "Corsair", "Sea Wolf", "Scourge of the Sea",
            "Terror of the Deep", "Dread of the Seven Tides"};

    public static int rank(int points) {
        int r = 0;
        for (int i = 0; i < AT.length; i++) if (points >= AT[i]) r = i;
        return r;
    }

    public static int landRank(ServerPlayer p) { return rank(p.getAttachedOrElse(LAND, 0)); }
    public static int seaRank(ServerPlayer p) { return rank(p.getAttachedOrElse(SEA, 0)); }

    public static void register() {
        LAND.identifier();
        SEA.identifier();
        // Besting another pirate outside a duel (crewmates can't hurt each other; see Nameplates)
        ServerLivingEntityEvents.AFTER_DEATH.register((victim, source) -> {
            if (victim instanceof ServerPlayer v && source.getEntity() instanceof ServerPlayer a && a != v) land(a, 10);
        });
    }

    public static void land(ServerPlayer p, int points) { add(p, LAND, points, LAND_RANKS, "land"); }

    public static void sea(ServerPlayer p, int points) { add(p, SEA, points, SEA_RANKS, "sea"); }

    private static void add(ServerPlayer p, AttachmentType<Integer> type, int points, String[] names, String what) {
        int before = p.getAttachedOrElse(type, 0), after = before + points;
        p.setAttached(type, after);
        p.sendSystemMessage(Component.literal("+" + points + " " + what + " infamy").withStyle(ChatFormatting.DARK_RED), true);
        if (rank(after) > rank(before)) {
            p.sendSystemMessage(Component.literal("Your " + what + " infamy rises: you're known as a " + names[rank(after)] + "!")
                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
            Nameplates.refresh(p);
        }
    }

    /** The little badges that go after your level: a sword and an anchor, with your rank. */
    public static String badges(ServerPlayer p) {
        int l = landRank(p), s = seaRank(p);
        return (l > 0 ? " ⚔" + l : "") + (s > 0 ? " ⚓" + s : "");
    }

    public static List<String> journal(ServerPlayer p) {
        List<String> lines = new ArrayList<>();
        int lp = p.getAttachedOrElse(LAND, 0), sp = p.getAttachedOrElse(SEA, 0);
        int l = rank(lp), s = rank(sp);
        lines.add("§fLand infamy: §c" + LAND_RANKS[l] + " §7(" + lp + (l + 1 < AT.length ? "/" + AT[l + 1] : "") + ") from duels and pirate fights");
        lines.add("§fSea infamy: §c" + SEA_RANKS[s] + " §7(" + sp + (s + 1 < AT.length ? "/" + AT[s + 1] : "") + ") from sinking other pirates' ships");
        return lines;
    }
}
