package com.cursedtides.ui;

import com.cursedtides.dungeon.Dungeon;
import com.cursedtides.dungeon.Dungeons;
import com.cursedtides.faction.ContractType;
import com.cursedtides.faction.Faction;
import com.cursedtides.faction.FactionData;
import com.cursedtides.item.ModItems;
import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import com.cursedtides.quest.QuestData;
import com.cursedtides.world.Archipelago;
import com.cursedtides.world.ModWorld;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import java.util.ArrayList;
import java.util.List;

/** Builds what the journal and chart screens show, on the server where the data lives. */
public final class UiData {
    private UiData() {}

    public static void register() {
        PayloadTypeRegistry.playS2C().register(UiPayloads.Journal.ID, UiPayloads.Journal.CODEC);
        PayloadTypeRegistry.playS2C().register(UiPayloads.Chart.ID, UiPayloads.Chart.CODEC);
    }

    public static void sendJournal(ServerPlayer player) {
        List<String> lines = new ArrayList<>();
        int notoriety = PirateData.notoriety(player);
        lines.add("§6§l" + com.cursedtides.character.CharacterData.pirateName(player) + "§r§6, "
                + PirateData.title(notoriety).getString() + "§r§7   Notoriety " + notoriety);
        String arcTitle = com.cursedtides.quest.HorizonArc.title(player);
        if (arcTitle != null) lines.add("§d§o" + arcTitle);
        if (com.cursedtides.character.CharacterData.isCreated(player)) {
            lines.add("§7" + com.cursedtides.character.CharacterData.background(player).title()
                    + ". (Rename with /pirate appearance)");
        }
        com.cursedtides.social.CrewState.Crew crew = player.getServer() == null ? null
                : com.cursedtides.social.CrewState.get(player.getServer()).crewOf(player.getUUID());
        if (crew != null) {
            lines.add("§7Crew: §f" + crew.name + " §7(" + crew.members.size() + " hands, captain "
                    + crew.names.getOrDefault(crew.captain, "?") + ")");
        } else {
            lines.add("§7No crew. Found one with /crew create <name>.");
        }
        lines.add("");

        lines.add("§e§lThe Drowned Bell");
        lines.add("§f" + QuestData.objective(player).getString());
        lines.add("");

        lines.add("§e§lThe Seven Tides");
        lines.addAll(com.cursedtides.quest.HorizonArc.journal(player));
        lines.add("");

        lines.add("§e§lThe Hollow Crown");
        lines.addAll(com.cursedtides.quest.CrownArc.journal(player));
        lines.add("");

        lines.add("§e§lThe Drowned Lights");
        lines.addAll(com.cursedtides.quest.LanternArc.journal(player));
        lines.add("");

        lines.add("§e§lThe Black Muster");
        lines.addAll(com.cursedtides.quest.MusterArc.journal(player));
        lines.add("");

        lines.add("§e§lErrands");
        lines.addAll(com.cursedtides.quest.Errands.journal(player));
        lines.add("");

        lines.add("§e§lWrecks and Spires");
        lines.addAll(com.cursedtides.dungeon.Delves.journal(player));
        lines.add("");

        lines.add("§e§lInfamy");
        lines.addAll(com.cursedtides.social.Infamy.journal(player));
        lines.add("");

        lines.add("§e§lThe Pirate Code");
        lines.addAll(com.cursedtides.ui.Tips.code());
        lines.add("");

        lines.add("§e§lContracts");
        lines.addAll(com.cursedtides.quest.Contracts.journal(player));
        lines.add("");

        lines.add("§e§lLetters");
        lines.addAll(com.cursedtides.quest.Letters.journal(player));
        lines.add("");

        lines.add("§e§lSide quests");
        lines.addAll(com.cursedtides.quest.SideQuests.journal(player));
        lines.add("");

        lines.add("§e§lProficiencies");
        for (Weapon w : Weapon.values()) {
            int level = PirateData.level(player, w);
            String bar = "|".repeat(Math.round(level / 30f * 20));
            lines.add("§f" + pad(Component.translatable(w.translationKey()).getString(), 12) + " §7Lv " + level + "  §b" + bar);
        }
        lines.add("§3Voodoo energy " + PirateData.voodoo(player) + "/" + PirateData.maxVoodoo(player));
        lines.add("");

        lines.add("§e§lProfessions");
        lines.addAll(com.cursedtides.profession.ProfessionData.journal(player));
        lines.add("");

        lines.add("§e§lStanding");
        for (Faction f : Faction.values()) {
            int rep = FactionData.rep(player, f);
            lines.add("§f" + f.displayName() + ": §7" + FactionData.rankName(f, rep) + " (" + rep + ")");
        }
        if (FactionData.isWanted(player)) {
            lines.add("§c§lWANTED§r§c  Bounty " + FactionData.bounty(player) + " doubloons (pay the Commodore at Ironwatch)");
        }
        FactionData.Contract contract = FactionData.contract(player);
        if (contract.type() != 0) {
            ContractType t = contract.contractType();
            lines.add("§fContract: §7" + t.description() + " (" + contract.progress() + "/" + t.goal() + ") for the "
                    + t.giver().displayName());
        } else {
            lines.add("§7No contract. Ask the Commodore at Ironwatch or the Broker in Saltmarrow's tavern.");
        }
        lines.add("");

        lines.add("§e§lTreasure maps carried");
        boolean anyMap = false;
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.is(ModItems.TREASURE_MAP)) continue;
            anyMap = true;
            CustomData c = stack.get(DataComponents.CUSTOM_DATA);
            CompoundTag data = c == null ? new CompoundTag() : c.copyTag();
            if (data.contains("TargetX")) {
                int tx = data.getInt("TargetX"), tz = data.getInt("TargetZ");
                int dist = (int) Math.hypot(tx - player.getX(), tz - player.getZ());
                lines.add("§f" + (data.getBoolean("Legendary") ? "§6Legendary §f" : "") + "X at " + tx + ", " + tz + " §7(" + dist + " blocks)");
            } else {
                lines.add("§7An unread map. Use it to study the markings.");
            }
        }
        int fragments = inv.countItem(ModItems.MAP_FRAGMENT);
        if (fragments > 0) lines.add("§7Map fragments: " + fragments + "/3");
        if (!anyMap && fragments == 0) lines.add("§7None. Look in dungeon vaults, wrecks and the Quartermasters' wares.");
        lines.add("");

        lines.add("§e§lDungeons §7(" + Dungeons.clearedCount(player) + "/" + Dungeons.ALL.size() + " vaults opened)");
        int discovered = player.getAttachedOrElse(ModWorld.DISCOVERED, 0);
        for (Dungeon d : Dungeons.ALL) {
            int island = Archipelago.islandIndexAt(d.ex, d.ez);
            boolean known = island >= 0 && (discovered & (1 << island)) != 0;
            String where = island >= 0 ? Archipelago.ISLANDS.get(island).name() : "?";
            if (Dungeons.isCleared(player, d)) {
                lines.add("§a✔ " + d.name + " §7(" + where + ")");
            } else if (known) {
                lines.add("§f• " + d.name + " §7(" + where + ", entrance " + d.ex + ", " + d.ez + ")");
            } else {
                lines.add("§8• ??? (somewhere on an undiscovered island)");
            }
        }
        lines.add("");

        lines.add("§e§lIslands §7(" + Integer.bitCount(discovered) + "/" + Archipelago.ISLANDS.size() + ")");
        for (int i = 0; i < Archipelago.ISLANDS.size(); i++) {
            Archipelago.Island is = Archipelago.ISLANDS.get(i);
            lines.add(((discovered & (1 << i)) != 0 ? "§a✔ " + is.name() : "§8• " + is.name()) + " §7(" + is.cx() + ", " + is.cz() + ")");
        }
        ServerPlayNetworking.send(player, new UiPayloads.Journal(lines));
    }

    public static void sendMap(ServerPlayer player) {
        List<Integer> marks = new ArrayList<>();
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            CustomData c = stack.is(ModItems.TREASURE_MAP) ? stack.get(DataComponents.CUSTOM_DATA) : null;
            if (c != null && c.copyTag().contains("TargetX")) {
                marks.add(c.copyTag().getInt("TargetX"));
                marks.add(c.copyTag().getInt("TargetZ"));
            }
        }
        long cleared = Dungeons.mask(player);
        ServerPlayNetworking.send(player, new UiPayloads.Chart(ModWorld.isArchipelago(),
                player.getAttachedOrElse(ModWorld.DISCOVERED, 0) | com.cursedtides.explore.Cartography.revealedMask(player), cleared, marks));
    }

    private static String pad(String s, int width) {
        return s.length() >= width ? s : s + " ".repeat(width - s.length());
    }
}
