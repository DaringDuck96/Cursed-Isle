package com.cursedtides.profession;

import com.cursedtides.world.Archipelago;
import com.cursedtides.world.ModWorld;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Fishing with any rod. Every fish you reel in earns Fishing xp, and may bring up one of the
 * Reach's own species as well, depending on the waters, the hour and your skill.
 */
public final class Fishing {
    private Fishing() {}

    private static final Map<UUID, Integer> LAST_COUNT = new HashMap<>();

    static void register() {
        ServerTickEvents.END_SERVER_TICK.register(Fishing::tick);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> LAST_COUNT.remove(handler.getPlayer().getUUID()));
    }

    private static void tick(MinecraftServer server) {
        if (server.getTickCount() % 5 != 0) return;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            int count = p.getStats().getValue(Stats.CUSTOM, Stats.FISH_CAUGHT);
            Integer last = LAST_COUNT.put(p.getUUID(), count);
            if (last == null || count <= last) continue;
            for (int i = last; i < count; i++) onCatch(p);
        }
    }

    private static void onCatch(ServerPlayer p) {
        ProfessionData.addXp(p, Profession.FISHING, 6);
        com.cursedtides.activity.Achievements.grant(p, "first_catch");
        if (p.getRandom().nextFloat() < 0.03f) {
            p.addItem(com.cursedtides.explore.Collectibles.randomFor(p, com.cursedtides.explore.Collectibles.Kind.BOTTLE));
            p.sendSystemMessage(net.minecraft.network.chat.Component.literal("Something glassy came up tangled in the line.").withStyle(net.minecraft.ChatFormatting.AQUA), false);
        }
        com.cursedtides.activity.Challenges.progress(p, "fish", 1);
        com.cursedtides.activity.Leaderboards.add(p, "fishing", 1);
        int level = ProfessionData.level(p, Profession.FISHING);
        RandomSource r = p.getRandom();
        if (r.nextFloat() > 0.12f + level * 0.012f) return;
        CatchItem species = pick(p, level, r);
        if (species == null) return;
        int n = level >= 15 && r.nextFloat() < 0.2f ? 2 : 1;
        for (int i = 0; i < n; i++) {
            ItemStack fish = new ItemStack(species);
            double skew = Math.pow(r.nextDouble(), 1.6 - level * 0.02); // skill lands bigger fish
            int weight = species.minWeight + (int) ((species.maxWeight - species.minWeight) * skew);
            ProfessionItems.setInt(fish, "Weight", weight);
            if (!p.getInventory().add(fish)) p.drop(fish, false, net.minecraft.util.Prediction.SERVER_ONLY);
            p.sendSystemMessage(Component.literal("You also landed a ").append(fish.getHoverName())
                    .append(Component.literal(String.format(" (%.1f lb)!", weight / 10.0))).withStyle(ChatFormatting.AQUA), false);
        }
        ProfessionData.addXp(p, Profession.FISHING, species == ProfessionItems.GOLDEN_MARLIN ? 150 : 12);
        if (species == ProfessionItems.GOLDEN_MARLIN) com.cursedtides.activity.Achievements.grant(p, "golden_marlin");
        if (species == ProfessionItems.GOLDEN_MARLIN && p.level().getServer() != null) {
            p.level().getServer().getPlayerList().broadcastSystemMessage(Component.literal(p.getName().getString() + " has landed a Golden Marlin!")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
            p.level().playSound(null, p.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 0.8f, 1.0f);
        }
    }

    /** Which species these waters offer right now. */
    private static CatchItem pick(ServerPlayer p, int level, RandomSource r) {
        ServerLevel world = p.level();
        boolean night = world.isDarkOutside();
        List<CatchItem> pool = new ArrayList<>();
        Archipelago.Island near = null;
        if (ModWorld.isArchipelago()) {
            for (Archipelago.Island is : Archipelago.ISLANDS) {
                if (is.edgeDistance(p.getX(), p.getZ()) < is.radius() * 0.6) { near = is; break; }
            }
        }
        if (near == null) {
            pool.add(ProfessionItems.SILVERFIN_SNAPPER);
            pool.add(ProfessionItems.SILVERFIN_SNAPPER);
            if (level >= 25 && r.nextFloat() < 0.04f) return ProfessionItems.GOLDEN_MARLIN;
        } else {
            switch (near.style()) {
                case REEF, KEYS, ATOLL -> { pool.add(ProfessionItems.REEF_PARROTFISH); pool.add(ProfessionItems.REEF_PARROTFISH); }
                case BAYOU, JUNGLE -> { pool.add(ProfessionItems.BAYOU_CATFISH); pool.add(ProfessionItems.BAYOU_CATFISH); }
                case CRAGS -> pool.add(ProfessionItems.STORM_SWORDFISH);
                default -> pool.add(ProfessionItems.SILVERFIN_SNAPPER);
            }
            if ((near.style() == Archipelago.Style.CURSED || near.style() == Archipelago.Style.ATOLL) && night && level >= 5) {
                pool.add(ProfessionItems.GHOSTFIN);
            }
        }
        if (night) pool.add(ProfessionItems.LANTERN_EEL);
        return pool.get(r.nextInt(pool.size()));
    }
}
