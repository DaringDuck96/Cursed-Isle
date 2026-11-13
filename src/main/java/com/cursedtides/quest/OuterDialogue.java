package com.cursedtides.quest;

import com.cursedtides.activity.Regatta;
import com.cursedtides.entity.StoryNpcEntity;
import com.cursedtides.faction.Faction;
import com.cursedtides.faction.FactionData;
import com.cursedtides.item.ModClothing;
import com.cursedtides.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** The characters of the outer reaches. */
public final class OuterDialogue {
    private OuterDialogue() {}

    private static final String COMMISSION = "cursedtides_commission";
    private static final String WAYFARER_GIFT = "cursedtides_wayfarer";
    private static final String BLESSED = "cursedtides_blessed_";

    /** The Bank of Kingsport keeps your strongbox: the same one your ender chests open. */
    static void banker(StoryNpcEntity npc, ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        if (held.getItem() instanceof com.cursedtides.item.ShipDeedItem deed) {
            int premium = com.cursedtides.trade.Insurance.premium(deed.shipClass());
            StoryDialogue.say(npc, player, "A policy on a " + deed.shipClass().displayName().toLowerCase() + "? " + premium
                    + " doubloons, and if she sinks the bank stands you a new hull.");
            com.cursedtides.trade.Insurance.insure(player, held, deed);
            return;
        }
        if (com.cursedtides.trade.Insurance.collect(player)) {
            StoryDialogue.say(npc, player, "A sad business, losing a ship. Here are your replacement papers, as the policy promised.");
        }
        StoryDialogue.say(npc, player, "Your strongbox, captain. Whatever you leave with the Bank of Kingsport is safe from thieves and storms alike.",
                "(Hold a ship's deed when you talk to me to insure her.)");
        player.openMenu(new SimpleMenuProvider(
                (syncId, inventory, p) -> ChestMenu.threeRows(syncId, inventory, p.getEnderChestInventory()),
                Component.literal("Strongbox, Bank of Kingsport")));
    }

    /** Mends anyone who asks, once in a while: full health, and poison or worse drawn out. */
    static void abbess(StoryNpcEntity npc, ServerPlayer player) {
        long day = player.level().getOverworldClockTime() / 6000; // a quarter-day
        String tag = BLESSED + day;
        if (player.entityTags().contains(tag)) {
            StoryDialogue.say(npc, player, "Rest a while, child. I've done what I can for today. Come back after the bells.");
            return;
        }
        player.entityTags().removeIf(t -> t.startsWith(BLESSED));
        player.addTag(tag);
        player.setHealth(player.getMaxHealth());
        player.getFoodData().setFoodLevel(20);
        player.removeEffect(MobEffects.POISON);
        player.removeEffect(MobEffects.WITHER);
        player.removeEffect(MobEffects.WEAKNESS);
        player.removeEffect(MobEffects.SLOWNESS);
        player.removeEffect(MobEffects.NAUSEA);
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 30, 0));
        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 20 * 120, 0));
        player.level().playSound(null, player.blockPosition(), SoundEvents.BELL_RESONATE, SoundSource.NEUTRAL, 0.5f, 1.4f);
        if (FactionData.isWanted(player)) {
            StoryDialogue.say(npc, player, "I know that face from the posters. Sanctuary is sanctuary. Be mended, and go gently.");
        } else {
            StoryDialogue.say(npc, player, "Hold still. There. The sea has enough sorrow without us adding wounds to it.");
        }
    }

    /** The head of the Tidewatch: contracts like the Commodore, and a commission for the loyal. */
    static void admiral(StoryNpcEntity npc, ServerPlayer player) {
        if (HorizonArc.admiral(npc, player)) return;
        if (MusterArc.admiral(npc, player)) return;
        int navy = FactionData.rep(player, Faction.NAVY);
        if (FactionData.isWanted(player)) {
            StoryDialogue.say(npc, player, "A wanted pirate in Crownwatch? Pay your bounty to Commodore Vey at Ironwatch, then we'll talk. Guards!");
            return;
        }
        if (navy >= 600 && !player.entityTags().contains(COMMISSION)) {
            player.addTag(COMMISSION);
            StoryDialogue.say(npc, player,
                    "Your record reached my desk, captain. Ghost ships sunk, the cursed driven off, contracts fulfilled.",
                    "By order of the Admiralty you hold a Tidewatch commission. Take this deed: a galleon of the line.",
                    "And dress the part. The Tidewatch looks after its own.");
            QuestData.give(player, new ItemStack(ModItems.GALLEON_DEED));
            for (Item piece : ModClothing.NAVY_SET) QuestData.give(player, new ItemStack(piece));
            player.level().playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 0.8f, 1.0f);
            return;
        }
        if (navy < 100) {
            StoryDialogue.say(npc, player, "The Admiralty deals with proven officers. Prove yourself to Commodore Vey at Ironwatch first.",
                    "(Reach 600 Tidewatch standing and the Admiral will grant you a commission.)");
            return;
        }
        StoryDialogue.contractBoard(npc, player, Faction.NAVY);
    }

    static void racemaster(StoryNpcEntity npc, ServerPlayer player) {
        Regatta.talk(npc, player);
    }

    /** At the edge of the chart: the gate, and what it waits for. */
    static void wayfarer(StoryNpcEntity npc, ServerPlayer player) {
        if (!player.entityTags().contains(WAYFARER_GIFT)) {
            player.addTag(WAYFARER_GIFT);
            StoryDialogue.say(npc, player,
                    "You sailed off the edge of every chart in Saltmarrow to get here. Good. Most turn back at Stormbreak.",
                    "That gate on the summit was old when the first ships came. The people who built it lived on the Keys, before the sea rose.",
                    "Seven sockets around its threshold. Seven relics, scattered across the Reach. Someone has been collecting them.",
                    "Take this. I found it in the observatory. Keep it close, and keep an eye on who asks about it.");
            QuestData.give(player, new ItemStack(ModItems.ANCIENT_RELIC));
        } else {
            StoryDialogue.say(npc, player,
                    "The gate stays shut. Seven relics, seven sockets. I'll be here, watching the horizon.",
                    "(The story of the Horizon Gate continues in a future chapter.)");
        }
    }

    /** Raven's Landing's queen of contraband: smuggling contracts for those the Tidewatch doesn't own. */
    static void smugglerQueen(StoryNpcEntity npc, ServerPlayer player) {
        if (MusterArc.queen(npc, player)) return;
        int navy = FactionData.rep(player, Faction.NAVY);
        int smugglers = FactionData.rep(player, Faction.SMUGGLERS);
        if (navy >= 300) {
            StoryDialogue.say(npc, player, "A Tidewatch favourite in my warehouse. You can find the door, or my lads can show you it.");
            return;
        }
        if (smugglers < -100) {
            StoryDialogue.say(npc, player, "Informers drink alone in Raven's Landing. Earn back some trust on the black market first.");
            return;
        }
        if (smugglers < 50 && FactionData.contract(player).type() == 0) {
            StoryDialogue.say(npc, player,
                    "New faces make me nervous. Here's how it works: the fence in the square sells rum without the Crown's stamp and powder without papers.",
                    "Carry it somewhere it's wanted, Blackthorn or back here, and sell it on the black market. Keep clear of Tidewatch sails.",
                    "If a patrol hails you, you can heave to and lose the lot, or run. Running is more fun.");
        }
        StoryDialogue.contractBoard(npc, player, Faction.SMUGGLERS);
    }
}
