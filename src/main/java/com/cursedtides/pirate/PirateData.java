package com.cursedtides.pirate;

import com.cursedtides.CursedTides;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Pirate progression: per-weapon mastery (level 0-30), notoriety (overall level 1-50 from all reputation;
 * levels, 0-120) with a title, and a regenerating voodoo energy bar for hexes.
 */
public final class PirateData {
    private PirateData() {}

    public static final int MAX_LEVEL = 30;

    public static final AttachmentType<Integer> VOODOO = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT)
            .copyOnDeath()
            .initializer(() -> 100)
            .buildAndRegister(CursedTides.id("voodoo"));

    private static final String[] TITLES = {
            "title.cursedtides.landlubber",   // 0
            "title.cursedtides.deckhand",     // 5
            "title.cursedtides.buccaneer",    // 15
            "title.cursedtides.corsair",      // 30
            "title.cursedtides.sea_wolf",     // 50
            "title.cursedtides.dread_captain",// 75
            "title.cursedtides.tide_legend"   // 100
    };
    private static final int[] TITLE_THRESHOLDS = {1, 5, 10, 18, 26, 36, 46};

    /** Forces the attachment types to register during mod init. */
    public static void init() {
        Weapon.values();
        VOODOO.identifier();
    }

    // ---- Mastery ----

    public static int levelForXp(int xp) {
        return Math.min(MAX_LEVEL, (int) Math.floor(Math.sqrt(xp / 8.0)));
    }

    public static int xpForLevel(int level) {
        return level * level * 8;
    }

    public static int xp(PlayerEntity player, Weapon weapon) {
        return player.getAttachedOrElse(weapon.xpType(), 0);
    }

    public static int level(PlayerEntity player, Weapon weapon) {
        return levelForXp(xp(player, weapon));
    }

    public static final int MAX_NOTORIETY = 50;

    /** All reputation earned, across every skill. */
    public static int totalXp(PlayerEntity player) {
        int total = 0;
        for (Weapon w : Weapon.values()) {
            total += xp(player, w);
        }
        return total;
    }

    /** Notoriety: the pirate's overall level (1-50), set by all the reputation they've earned. */
    public static int notoriety(PlayerEntity player) {
        return Math.min(MAX_NOTORIETY, 1 + (int) Math.floor(Math.sqrt(totalXp(player) / 18.0)));
    }

    public static int totalXpForNotoriety(int level) {
        return 18 * (level - 1) * (level - 1);
    }

    public static Text title(int notoriety) {
        int index = 0;
        for (int i = 0; i < TITLE_THRESHOLDS.length; i++) {
            if (notoriety >= TITLE_THRESHOLDS[i]) {
                index = i;
            }
        }
        return Text.translatable(TITLES[index]);
    }

    public static void addXp(ServerPlayerEntity player, Weapon weapon, int amount) {
        int oldXp = xp(player, weapon);
        int oldLevel = levelForXp(oldXp);
        int oldNotoriety = notoriety(player);
        if (oldLevel >= MAX_LEVEL) {
            return;
        }
        double bonus = PerkData.stat(player, Perk.Stat.XP);
        bonus += com.cursedtides.living.Festivals.bonus(weapon);
        if (weapon == Weapon.SAILING || weapon == Weapon.EXPLORATION) bonus += com.cursedtides.appearance.Appearance.special(player, "explore");
        amount = (int) Math.round(amount * (1.0 + bonus));
        player.setAttached(weapon.xpType(), oldXp + amount);

        int newNotoriety = notoriety(player);
        if (newNotoriety > oldNotoriety) {
            player.sendMessage(Text.literal("Notoriety " + newNotoriety + "! Your health rises.")
                    .formatted(Formatting.GOLD, Formatting.BOLD), false);
            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 0.6f, 1.3f);
            com.cursedtides.combat.Levels.refreshPlayer(player);
            player.setHealth(player.getMaxHealth());
            if (!title(oldNotoriety).equals(title(newNotoriety))) {
                player.sendMessage(Text.translatable("message.cursedtides.new_title",
                        title(newNotoriety).copy().formatted(Formatting.GOLD, Formatting.BOLD)), false);
            }
        }
        int newLevel = level(player, weapon);
        if (newLevel > oldLevel) {
            player.sendMessage(Text.translatable("message.cursedtides.mastery_up",
                    Text.translatable(weapon.translationKey()).formatted(weapon.color(), Formatting.BOLD),
                    newLevel).formatted(Formatting.YELLOW), false);
            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.8f, 1.2f);

            if (weapon == Weapon.CUTLASS && oldLevel < 5 && newLevel >= 5) {
                player.sendMessage(Text.translatable("message.cursedtides.unlock_whirl")
                        .formatted(Formatting.AQUA), false);
            }
            if (weapon == Weapon.HEX && oldLevel < 5 && newLevel >= 5) {
                player.sendMessage(Text.translatable("message.cursedtides.unlock_wither")
                        .formatted(Formatting.AQUA), false);
            }
            for (int tier = 0; tier < Perk.TIER_LEVELS.length; tier++) {
                if (oldLevel < Perk.TIER_LEVELS[tier] && newLevel >= Perk.TIER_LEVELS[tier]) {
                    player.sendMessage(Text.literal("A new perk tier is open in your "
                            + Text.translatable(weapon.translationKey()).getString() + " tree. Press K to choose.")
                            .formatted(Formatting.AQUA), false);
                }
            }
        }
    }

    // ---- Voodoo energy ----

    public static int maxVoodoo(PlayerEntity player) {
        return 100 + level(player, Weapon.HEX) * 5;
    }

    public static int voodoo(PlayerEntity player) {
        return player.getAttachedOrElse(VOODOO, 100);
    }

    public static boolean spendVoodoo(PlayerEntity player, int cost) {
        if (player.getAbilities().creativeMode) {
            return true;
        }
        int current = voodoo(player);
        if (current < cost) {
            return false;
        }
        player.setAttached(VOODOO, current - cost);
        return true;
    }

    public static void regenVoodoo(PlayerEntity player, int amount) {
        int max = maxVoodoo(player);
        int current = voodoo(player);
        if (current < max) {
            player.setAttached(VOODOO, Math.min(max, current + amount));
        }
    }
}
