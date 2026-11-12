package com.cursedtides;

import com.cursedtides.entity.ModEntities;
import com.cursedtides.faction.FactionData;
import com.cursedtides.faction.FactionEvents;
import com.cursedtides.item.ModItems;
import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.PirateEvents;
import com.cursedtides.quest.QuestData;
import com.cursedtides.quest.QuestEvents;
import com.cursedtides.ship.ShipEvents;
import com.cursedtides.world.ModWorld;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CursedTides implements ModInitializer {
    public static final String MOD_ID = "cursedtides";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        PirateData.init();
        QuestData.init();
        FactionData.init();
        com.cursedtides.dungeon.Dungeons.init();
        com.cursedtides.block.ModBlocks.register();
        com.cursedtides.ui.UiData.register();
        ModEntities.register();
        ModItems.register();
        com.cursedtides.wildlife.Wildlife.register();
        com.cursedtides.sound.ModSounds.register();
        com.cursedtides.appearance.Accessories.register();
        com.cursedtides.appearance.Appearance.register();
        com.cursedtides.trade.TradeGood.register();
        com.cursedtides.trade.Markets.register();
        com.cursedtides.trade.Smuggling.register();
        com.cursedtides.trade.AuctionHouse.register();
        com.cursedtides.trade.PlayerTrade.register();
        com.cursedtides.profession.ProfessionItems.register();
        com.cursedtides.ship.FlagItem.register();
        com.cursedtides.ship.BelowDeck.register();
        com.cursedtides.quest.HorizonArc.register();
        com.cursedtides.dialogue.Dialogue.register();
        com.cursedtides.profession.ProfessionData.register();
        ModEntities.registerSpawns();
        ModWorld.register();
        PirateEvents.register();
        QuestEvents.register();
        ShipEvents.register();
        FactionEvents.register();
        com.cursedtides.combat.CombatData.register();
        com.cursedtides.combat.Styles.register();
        com.cursedtides.combat.Ultimates.register();
        com.cursedtides.character.CharacterData.register();
        com.cursedtides.pirate.PerkData.register();
        com.cursedtides.poker.PokerTables.register();
        com.cursedtides.quest.SideQuests.register();
        com.cursedtides.event.WorldEvents.register();
        com.cursedtides.social.CrewState.register();
        com.cursedtides.social.CrewTeams.register();
        com.cursedtides.social.Duels.register();
        com.cursedtides.living.LivingWorld.register();
        com.cursedtides.activity.Activities.register();
        com.cursedtides.explore.Exploration.register();
        com.cursedtides.home.Homes.register();
        com.cursedtides.activity.Regatta.register();
        LOGGER.info("Cursed Tides: hoist the colours!");
    }
}
