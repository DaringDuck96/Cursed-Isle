package com.cursedtides.dungeon;

import com.cursedtides.CursedTides;
import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import com.cursedtides.world.Archipelago;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * Every dungeon in the Shattered Reach: two or three per island. Each island's dungeons
 * sit at different depths so their tunnels never collide.
 */
public final class Dungeons {
    private Dungeons() {}

    //                          index name                          entrance x, z   theme                  rooms level tier
    public static final List<Dungeon> ALL = List.of(
            // Driftwood Island
            new Dungeon(0, "Wrecker's Cellar", Archipelago.rx(-16, -14), Archipelago.rz(-16, -14), DungeonTheme.SMUGGLER, 5, 0, 1),
            new Dungeon(1, "Tide Caves", Archipelago.rx(13, 13), Archipelago.rz(13, 13), DungeonTheme.GROTTO, 6, 1, 1),
            // Saltmarrow
            new Dungeon(2, "Bilge Tunnels", Archipelago.rx(262, -2), Archipelago.rz(262, -2), DungeonTheme.SMUGGLER, 7, 0, 1),
            new Dungeon(3, "Old Rum Vaults", Archipelago.rx(196, -56), Archipelago.rz(196, -56), DungeonTheme.FORT, 7, 1, 1),
            // The Sunken Reach (flooded: bring water breathing)
            new Dungeon(4, "Drowned Chapel", Archipelago.rx(52, 228), Archipelago.rz(52, 228), DungeonTheme.SEA_CRYPT, 6, 0, 2),
            new Dungeon(5, "Coral Grotto", Archipelago.rx(88, 254), Archipelago.rz(88, 254), DungeonTheme.SEA_CAVE, 7, 1, 2),
            // Ashfall Island
            new Dungeon(6, "Magma Vents", Archipelago.rx(-200, 128), Archipelago.rz(-200, 128), DungeonTheme.VOLCANIC, 7, 0, 2),
            new Dungeon(7, "Smugglers' Deep Mine", Archipelago.rx(-236, 172), Archipelago.rz(-236, 172), DungeonTheme.SMUGGLER, 8, 1, 2),
            new Dungeon(8, "Obsidian Shrine", Archipelago.rx(-190, 176), Archipelago.rz(-190, 176), DungeonTheme.VOLCANIC, 8, 2, 3),
            // Hollowgrave Island
            new Dungeon(9, "The Ossuary", Archipelago.rx(-128, -236), Archipelago.rz(-128, -236), DungeonTheme.CRYPT, 7, 0, 2),
            new Dungeon(10, "Catacombs of the Drowned", Archipelago.rx(-96, -268), Archipelago.rz(-96, -268), DungeonTheme.SEA_CRYPT, 8, 1, 3),
            new Dungeon(11, "Thorne's Crypt", Archipelago.rx(-127, -265), Archipelago.rz(-127, -265), DungeonTheme.CRYPT, 9, 2, 3),
            // Ironwatch Isle
            new Dungeon(12, "Old Casemates", Archipelago.rx(457, 150), Archipelago.rz(457, 150), DungeonTheme.FORT, 7, 0, 2),
            new Dungeon(13, "Sealed Powder Store", Archipelago.rx(405, 197), Archipelago.rz(405, 197), DungeonTheme.FORT, 6, 1, 2),
            // Mirefen Bayou
            new Dungeon(14, "Sunken Plantation Cellar", Archipelago.rx(-338, -63), Archipelago.rz(-338, -63), DungeonTheme.BAYOU, 7, 0, 2),
            new Dungeon(15, "Gator Warrens", Archipelago.rx(-296, -16), Archipelago.rz(-296, -16), DungeonTheme.BAYOU, 8, 1, 2),
            new Dungeon(16, "Root Hollow", Archipelago.rx(-345, -13), Archipelago.rz(-345, -13), DungeonTheme.BAYOU, 9, 2, 3),
            // A third for Saltmarrow, under its southern cliffs
            new Dungeon(17, "Smugglers' Sea Cave", Archipelago.rx(216, 20), Archipelago.rz(216, 20), DungeonTheme.GROTTO, 6, 2, 2),
            // Port Meridian
            new Dungeon(18, "Governor's Wine Cellars", Archipelago.rx(446, -232), Archipelago.rz(446, -232), DungeonTheme.FORT, 7, 0, 2),
            new Dungeon(19, "Meridian Sewers", Archipelago.rx(392, -178), Archipelago.rz(392, -178), DungeonTheme.GROTTO, 8, 1, 2),
            // Blackthorn Isle
            new Dungeon(20, "Blackthorn Smuggler Caves", Archipelago.rx(168, -312), Archipelago.rz(168, -312), DungeonTheme.SMUGGLER, 8, 0, 3),
            new Dungeon(21, "Drowned Cove", Archipelago.rx(131, -349), Archipelago.rz(131, -349), DungeonTheme.SEA_CAVE, 7, 1, 3),
            // Emerald Cay
            new Dungeon(22, "Serpent Stair", Archipelago.rx(-80, 398), Archipelago.rz(-80, 398), DungeonTheme.JUNGLE, 8, 0, 3),
            new Dungeon(23, "Overgrown Vault", Archipelago.rx(-84, 446), Archipelago.rz(-84, 446), DungeonTheme.JUNGLE, 9, 1, 3),
            // Whispering Key
            new Dungeon(24, "Ashgrove Crypts", Archipelago.rx(-404, -298), Archipelago.rz(-404, -298), DungeonTheme.CRYPT, 8, 0, 3),
            new Dungeon(25, "Haunted Undercroft", Archipelago.rx(-438, -292), Archipelago.rz(-438, -292), DungeonTheme.CRYPT, 9, 1, 3),
            // Goldwater Cay
            new Dungeon(26, "The Deep Seam", Archipelago.rx(316, 416), Archipelago.rz(316, 416), DungeonTheme.SMUGGLER, 9, 0, 3),
            new Dungeon(27, "Collapsed Galleries", Archipelago.rx(282, 384), Archipelago.rz(282, 384), DungeonTheme.GROTTO, 8, 1, 2),
            // Kingsport
            new Dungeon(28, "Kingsport Gaol", Archipelago.rx(577, -43), Archipelago.rz(577, -43), DungeonTheme.PRISON, 8, 0, 2),
            new Dungeon(29, "Counting-House Vaults", Archipelago.rx(623, 3), Archipelago.rz(623, 3), DungeonTheme.FORT, 7, 1, 2),
            // Crooked Reef (flooded)
            new Dungeon(30, "The Hull Warrens", Archipelago.rx(196, 548), Archipelago.rz(196, 548), DungeonTheme.SEA_CAVE, 8, 0, 3),
            // Saint Vesper
            new Dungeon(31, "Reliquary Crypt", Archipelago.rx(-577, 137), Archipelago.rz(-577, 137), DungeonTheme.CRYPT, 8, 0, 3),
            new Dungeon(32, "Penitents' Tunnels", Archipelago.rx(-543, 103), Archipelago.rz(-543, 103), DungeonTheme.PRISON, 7, 1, 2),
            // Mariner's Rest
            new Dungeon(33, "Wreckers' Grotto", Archipelago.rx(36, -556), Archipelago.rz(36, -556), DungeonTheme.GROTTO, 6, 0, 1),
            // Widow's Rock
            new Dungeon(34, "Widow's Sea Cave", Archipelago.rx(-263, -487), Archipelago.rz(-263, -487), DungeonTheme.SEA_CAVE, 8, 0, 3),
            new Dungeon(35, "Wreckers' Cistern", Archipelago.rx(-237, -513), Archipelago.rz(-237, -513), DungeonTheme.SMUGGLER, 8, 1, 3),
            // Serpent's Hollow
            new Dungeon(36, "The Coiled Temple", Archipelago.rx(-311, 439), Archipelago.rz(-311, 439), DungeonTheme.JUNGLE, 9, 0, 3),
            new Dungeon(37, "Serpent's Gullet", Archipelago.rx(-345, 405), Archipelago.rz(-345, 405), DungeonTheme.JUNGLE, 9, 1, 4),
            // Bloodwake Cay
            new Dungeon(38, "Bloodwake Dungeons", Archipelago.rx(-57, -703), Archipelago.rz(-57, -703), DungeonTheme.PRISON, 9, 0, 3),
            new Dungeon(39, "Red Maddox's Hoard", Archipelago.rx(-23, -737), Archipelago.rz(-23, -737), DungeonTheme.SMUGGLER, 8, 1, 4),
            // Stormbreak Isle
            new Dungeon(40, "Thunderwell", Archipelago.rx(746, 344), Archipelago.rz(746, 344), DungeonTheme.GROTTO, 8, 0, 3),
            // Raven's Landing
            new Dungeon(41, "Contraband Cellars", Archipelago.rx(-636, -144), Archipelago.rz(-636, -144), DungeonTheme.SMUGGLER, 8, 0, 2),
            new Dungeon(42, "The Rookery", Archipelago.rx(-604, -176), Archipelago.rz(-604, -176), DungeonTheme.CRYPT, 8, 1, 3),
            // Emberfall
            new Dungeon(43, "Cinder Halls", Archipelago.rx(-725, 645), Archipelago.rz(-725, 645), DungeonTheme.VOLCANIC, 10, 0, 4),
            new Dungeon(44, "The Ashen Throne", Archipelago.rx(-675, 595), Archipelago.rz(-675, 595), DungeonTheme.VOLCANIC, 11, 1, 5),
            new Dungeon(45, "Obsidian Labyrinth", Archipelago.rx(-736, 620), Archipelago.rz(-736, 620), DungeonTheme.VOLCANIC, 10, 2, 5),
            // The Forgotten Keys
            new Dungeon(46, "Halls of the First Tide", Archipelago.rx(452, 728), Archipelago.rz(452, 728), DungeonTheme.ANCIENT, 10, 0, 4),
            new Dungeon(47, "The Drowned Library", Archipelago.rx(496, 716), Archipelago.rz(496, 716), DungeonTheme.ANCIENT, 9, 1, 4),
            // Blackveil Atoll
            new Dungeon(48, "The Veiled Deep", Archipelago.rx(943, -400), Archipelago.rz(943, -400), DungeonTheme.SPECTRAL, 10, 0, 5),
            // Crownwatch
            new Dungeon(49, "Admiralty Magazine", Archipelago.rx(840, -80), Archipelago.rz(840, -80), DungeonTheme.FORT, 9, 0, 3),
            new Dungeon(50, "Crownwatch Hulks", Archipelago.rx(800, -40), Archipelago.rz(800, -40), DungeonTheme.PRISON, 9, 1, 4),
            // The Last Horizon
            new Dungeon(51, "Stair of Ages", Archipelago.rx(22, 1122), Archipelago.rz(22, 1122), DungeonTheme.ANCIENT, 11, 0, 5),
            new Dungeon(52, "The Sunset Vault", Archipelago.rx(-32, 1100), Archipelago.rz(-32, 1100), DungeonTheme.SPECTRAL, 12, 1, 5)
    );

    public static Dungeon byName(String name) {
        for (Dungeon d : ALL) {
            if (d.name.equals(name)) return d;
        }
        return null;
    }

    /** Pre-0.9 record (first 32 dungeons). Read once and folded into CLEARED_ALL. */
    public static final AttachmentType<Integer> CLEARED = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT)
            .copyOnDeath()
            .initializer(() -> 0)
            .buildAndRegister(CursedTides.id("dungeons_cleared"));

    /** Bit i set = vault of dungeon i opened by this player (up to 64 dungeons). */
    public static final AttachmentType<Long> CLEARED_ALL = AttachmentRegistry.<Long>builder()
            .persistent(Codec.LONG)
            .copyOnDeath()
            .initializer(() -> 0L)
            .buildAndRegister(CursedTides.id("dungeons_cleared_all"));

    public static void init() {
        CLEARED.identifier();
        CLEARED_ALL.identifier();
    }

    public static long mask(PlayerEntity player) {
        return player.getAttachedOrElse(CLEARED_ALL, 0L) | (player.getAttachedOrElse(CLEARED, 0) & 0xFFFFFFFFL);
    }

    public static int clearedCount(PlayerEntity player) {
        return Long.bitCount(mask(player));
    }

    public static boolean isCleared(PlayerEntity player, Dungeon d) {
        return (mask(player) & (1L << d.index)) != 0;
    }

    public static void onVaultOpened(ServerPlayerEntity player, BlockPos pos) {
        for (Dungeon d : ALL) {
            if (!d.contains(pos)) continue;
            com.cursedtides.quest.HorizonArc.onVault(player, d);
            long mask = mask(player);
            if ((mask & (1L << d.index)) != 0) return;
            player.setAttached(CLEARED_ALL, mask | (1L << d.index));
            player.sendMessage(Text.literal("Vault opened: " + d.name + " (" + clearedCount(player) + "/" + ALL.size() + ")")
                    .formatted(Formatting.GOLD), false);
            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 0.7f, 1.1f);
            PirateData.addXp(player, Weapon.EXPLORATION, 40 + d.tier * 30);
            com.cursedtides.activity.Achievements.grant(player, "first_vault");
            if (clearedCount(player) >= 10) com.cursedtides.activity.Achievements.grant(player, "ten_vaults");
            com.cursedtides.activity.Challenges.progress(player, "vault", 1);
            net.minecraft.item.ItemStack arm = com.cursedtides.combat.GearRarity.randomWeapon(player.getServerWorld(), player.getRandom(), d.tier);
            if (com.cursedtides.combat.GearRarity.isLegendary(arm)) com.cursedtides.activity.Achievements.grant(player, "legendary_find");
            if (!player.getInventory().insertStack(arm)) player.dropItem(arm, false);
            if (player.getRandom().nextBoolean()) player.giveItemStack(com.cursedtides.explore.Collectibles.randomFor(player, player.getRandom().nextBoolean() ? com.cursedtides.explore.Collectibles.Kind.LOG : com.cursedtides.explore.Collectibles.Kind.PAINTING));
            if (player.getRandom().nextInt(3) < d.tier) {
                net.minecraft.item.ItemStack trinket = new net.minecraft.item.ItemStack(
                        com.cursedtides.appearance.Accessories.random(player.getRandom(), d.tier >= 2));
                if (!player.getInventory().insertStack(trinket)) player.dropItem(trinket, false);
                player.sendMessage(Text.literal("Among the treasure: ").append(trinket.getName()).formatted(Formatting.LIGHT_PURPLE), false);
            }
            return;
        }
    }
}
