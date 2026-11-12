package com.cursedtides.quest;

import com.cursedtides.entity.StoryNpcEntity;
import com.cursedtides.faction.ContractType;
import com.cursedtides.faction.Faction;
import com.cursedtides.faction.FactionData;
import com.cursedtides.item.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** What each story character says, depending on how far the player has got. */
public final class StoryDialogue {
    private StoryDialogue() {}

    public static void talk(StoryNpcEntity npc, ServerPlayerEntity player) {
        int stage = QuestData.stage(player);
        switch (npc.getRole()) {
            case KEEPER -> keeper(npc, player, stage);
            case HARBORMASTER -> harbormaster(npc, player, stage);
            case BELLWRIGHT -> bellwright(npc, player, stage);
            case COMMODORE -> commodore(npc, player);
            case BROKER -> broker(npc, player);
            case DEALER -> {
                say(npc, player, "Hold'em, blinds one and two. Sit down and buy in with what you're carrying. Anyone else who sits at my table plays against you.");
                com.cursedtides.poker.PokerTables.join(player, npc);
            }
            case GAMBLER -> gambler(npc, player);
            case GOVERNOR -> governor(npc, player);
            case ROOK -> rook(npc, player);
            case NATURALIST -> naturalist(npc, player);
            case MEDIUM -> medium(npc, player);
            case FOREMAN -> foreman(npc, player);
            case BANKER -> OuterDialogue.banker(npc, player);
            case ABBESS -> OuterDialogue.abbess(npc, player);
            case ADMIRAL -> OuterDialogue.admiral(npc, player);
            case RACEMASTER -> OuterDialogue.racemaster(npc, player);
            case WAYFARER -> HorizonArc.wayfarer(npc, player);
            case SCHOLAR -> HorizonArc.scholar(npc, player);
            case SMUGGLER_QUEEN -> OuterDialogue.smugglerQueen(npc, player);
            case CONTEST_JUDGE -> com.cursedtides.profession.FishingContest.talk(npc, player);
            case AUCTIONEER -> {
                say(npc, player, "Lots on the board, captain! To sell, hold it up and name your price: /auction sell <doubloons>.");
                com.cursedtides.trade.AuctionHouse.open(player);
            }
        }
        player.getWorld().playSound(null, npc.getX(), npc.getY(), npc.getZ(),
                SoundEvents.ENTITY_VILLAGER_AMBIENT, SoundCategory.NEUTRAL, 0.6f, 0.9f);
    }

    private static void keeper(StoryNpcEntity npc, ServerPlayerEntity player, int stage) {
        if (stage == QuestData.START) {
            // The castaway's lessons (see Lessons): blade, guard, pistol, doll, firebomb, then a sloop of your own
            Lessons.talk(player);
        } else if (stage < QuestData.DONE) {
            say(npc, player, "Your chart shows the way. East for Saltmarrow, and mind the bell after dark.");
            hint(player, stage);
        } else {
            say(npc, player, "Forty years I've listened for that bell. Tonight I'll sleep. Thank you, captain.");
        }
    }

    private static void harbormaster(StoryNpcEntity npc, ServerPlayerEntity player, int stage) {
        if (stage == QuestData.START) {
            say(npc, player, "I don't hire strangers. If old Wren on Driftwood Island vouches for you, come back.");
        } else if (stage == QuestData.FIND_YSOLDE) {
            say(npc, player,
                    "Wren's stray, is it? Then listen close.",
                    "Thirty years ago a captain named Ashmere Thorne bargained with the deep: a crew that could never die, called by a bell that could never break.",
                    "The bellfounders who cast it cracked it out of spite. Three shards. Thorne kept the largest and rings it still.",
                    "One shard sank with the belltower at The Sunken Reach, south of here. Smugglers on Ashfall Island hide the other in their forge.",
                    "Bring both to Tamsin Coil, the last bellfounder. She lives on Ashfall's east shore. She can cast a bell to answer his.",
                    "Here's coin for powder and shot. The Quartermasters in the square will take it.",
                    "Name that sloop of yours at an anvil if you like. Hollis Pike at the shipyard can trade you up to something bigger.",
                    "Sell what you plunder to the Quartermasters, and the market buys trade goods. Bigger guns, better shot: it all costs doubloons.");
            QuestData.give(player, new ItemStack(ModItems.DOUBLOON, 15));
            // Wren's lessons already gave you a sloop; anyone who skipped them gets the harbour's
            if (Lessons.step(player) != Lessons.Step.DONE && player.getInventory().count(ModItems.SLOOP_DEED) == 0) {
                QuestData.give(player, new ItemStack(ModItems.SLOOP_DEED));
            }
            player.sendMessage(Text.literal("Ship controls: R/V raise or lower sails, Z/X fire port or starboard guns, "
                    + "B cargo hold. Sneak-punch your ship to stow her in the deed.").formatted(Formatting.AQUA), false);
            QuestData.setStage(player, QuestData.FIND_SHARDS);
            advanced(player);
        } else if (stage == QuestData.FIND_SHARDS) {
            boolean reef = QuestData.has(player, ModItems.REEF_SHARD);
            boolean ember = QuestData.has(player, ModItems.EMBER_SHARD);
            say(npc, player, reef && ember
                    ? "Both shards! Don't show them to me, take them to Tamsin on Ashfall's east shore."
                    : "Still missing " + (reef ? "the Ember Shard, in the smugglers' forge on Ashfall Island."
                    : ember ? "the Reef Shard, in the drowned belltower at The Sunken Reach."
                    : "both shards. The Sunken Reach to the south, and the forge on Ashfall Island to the west."));
        } else if (stage == QuestData.RING_BELL) {
            say(npc, player, "Hollowgrave lies far to the north-west. Ring that bell at Thorne's altar, and bring every shot you own.");
        } else {
            say(npc, player, "The harbor's never been busier. Ships sail at night again. Saltmarrow owes you, captain.");
        }
    }

    private static void bellwright(StoryNpcEntity npc, ServerPlayerEntity player, int stage) {
        if (stage < QuestData.FIND_SHARDS) {
            say(npc, player, "Bells, bells... I don't cast for strangers. Not since the last one.");
        } else if (stage == QuestData.FIND_SHARDS) {
            boolean reef = QuestData.has(player, ModItems.REEF_SHARD);
            boolean ember = QuestData.has(player, ModItems.EMBER_SHARD);
            if (reef && ember) {
                say(npc, player,
                        "Reef salt and forge soot. Those are his shards, no mistaking the ring of them. Hand them here.",
                        "...There. A bell cast from his own bronze, tuned to still it rather than wake it. The Stilling Bell.",
                        "Ring it before the altar on Hollowgrave. Thorne will have to answer in person. Be ready when he does.");
                QuestData.take(player, ModItems.REEF_SHARD);
                QuestData.take(player, ModItems.EMBER_SHARD);
                QuestData.give(player, new ItemStack(ModItems.STILLING_BELL));
                QuestData.setStage(player, QuestData.RING_BELL);
                player.getWorld().playSound(null, npc.getX(), npc.getY(), npc.getZ(),
                        SoundEvents.BLOCK_ANVIL_USE, SoundCategory.NEUTRAL, 0.8f, 0.8f);
                advanced(player);
            } else {
                say(npc, player, "I need both shards to work with. "
                        + (reef ? "You have the reef one. The smugglers' forge is just over the ridge, west."
                        : ember ? "You have the ember one. The other lies in the drowned tower at The Sunken Reach."
                        : "One lies in the Sunken Reach, one in the smugglers' forge on this very island."));
            }
        } else if (stage == QuestData.RING_BELL) {
            say(npc, player, "Hollowgrave, at the altar. Don't lose that bell; I won't live to cast another.");
        } else {
            say(npc, player, "I heard it from here, the moment his bell broke. The sweetest sound I ever made.");
        }
    }

    // ------------------------------------------------------------------ tavern dice

    /** Hold doubloons and talk to him: two dice each, high roll doubles your stake (up to 16). */
    private static void gambler(StoryNpcEntity npc, ServerPlayerEntity player) {
        ItemStack held = player.getMainHandStack();
        if (!held.isOf(ModItems.DOUBLOON)) {
            say(npc, player, "Bones, friend! Hold your doubloons out (up to sixteen) and we'll roll two dice apiece. High roll takes it.");
            return;
        }
        int stake = Math.min(16, held.getCount());
        var rng = player.getRandom();
        int mine = rng.nextInt(6) + 1 + rng.nextInt(6) + 1;
        int his = rng.nextInt(6) + 1 + rng.nextInt(6) + 1;
        player.getWorld().playSound(null, npc.getX(), npc.getY(), npc.getZ(), SoundEvents.BLOCK_WOOD_HIT, SoundCategory.NEUTRAL, 1.0f, 1.4f);
        if (mine > his) {
            QuestData.give(player, new ItemStack(ModItems.DOUBLOON, stake));
            say(npc, player, "You roll " + mine + ", I roll " + his + ". Curse it! Take your " + stake + ".");
        } else if (mine < his) {
            held.decrement(stake);
            say(npc, player, "You roll " + mine + ", I roll " + his + ". The bones love me tonight.");
        } else {
            say(npc, player, "Both " + mine + "! Push. Roll again?");
        }
    }

    // ------------------------------------------------------------------ side quests

    private static void governor(StoryNpcEntity npc, ServerPlayerEntity player) {
        if (HorizonArc.governor(npc, player)) return;
        int q = SideQuests.get(player, SideQuests.LEDGER);
        if (q == 0) {
            say(npc, player,
                    "A captain, are you? Then you may be of use. Port Meridian's ledger has been stolen: every debt, every bribe, every name.",
                    "Captain Rook Varga took it to Blackthorn Isle, north of Hollowgrave. Bring it back to me, discreetly.");
            SideQuests.set(player, SideQuests.LEDGER, 1);
            player.sendMessage(Text.literal("New side quest: The Governor's Ledger").formatted(Formatting.YELLOW), false);
        } else if (q == 1) {
            if (QuestData.has(player, ModItems.GOVERNORS_LEDGER)) {
                QuestData.take(player, ModItems.GOVERNORS_LEDGER);
                SideQuests.set(player, SideQuests.LEDGER, SideQuests.LEDGER_RETURNED);
                QuestData.give(player, new ItemStack(ModItems.DOUBLOON, 40));
                FactionData.adjust(player, Faction.NAVY, 80);
                FactionData.adjust(player, Faction.BRETHREN, -30);
                say(npc, player, "My ledger, unopened I trust. Port Meridian thanks you, and so does the Tidewatch. Forty doubloons.");
            } else {
                say(npc, player, "Blackthorn Isle, captain. Rook keeps it in one of the tents. Don't read it.");
            }
        } else if (q == SideQuests.LEDGER_RETURNED) {
            say(npc, player, "My door is open to you, captain. Port Meridian remembers its friends.");
        } else {
            say(npc, player, "I know where my ledger went, captain. Every page of it. Get out of my house.");
        }
    }

    private static void rook(StoryNpcEntity npc, ServerPlayerEntity player) {
        if (HorizonArc.rook(npc, player)) return;
        int q = SideQuests.get(player, SideQuests.LEDGER);
        if (q == 1 && QuestData.has(player, ModItems.GOVERNORS_LEDGER)) {
            say(npc, player,
                    "Found my little book, did you? You could run it back to Harrow and be her errand-dog.",
                    "Or sell it to me: 60 doubloons, and every pirate in the Reach will know you're one of us.",
                    "(Sell it to Rook, or take it back to Governor Harrow. This choice is permanent.)");
            if (com.cursedtides.dialogue.Dialogue.choice(player) == 1) {
                QuestData.take(player, ModItems.GOVERNORS_LEDGER);
                SideQuests.set(player, SideQuests.LEDGER, SideQuests.LEDGER_SOLD);
                QuestData.give(player, new ItemStack(ModItems.DOUBLOON, 60));
                FactionData.adjust(player, Faction.BRETHREN, 80);
                FactionData.adjust(player, Faction.NAVY, -40);
                say(npc, player, "Ha! A pleasure. Harrow's name will be mud by the next tide.");
            } else {
                player.sendMessage(Text.literal("Decide what to do with the ledger.").formatted(Formatting.GRAY), true);
            }
        } else if (q == SideQuests.LEDGER_SOLD) {
            say(npc, player, "Always welcome by my fire, friend.");
        } else if (q == SideQuests.LEDGER_RETURNED) {
            say(npc, player, "Harrow's lapdog. Drink your rum and keep your hands where I can see them.");
        } else {
            say(npc, player, "Welcome to Blackthorn. Mind the fire, mind your purse, and don't touch my tents.");
        }
    }

    private static void naturalist(StoryNpcEntity npc, ServerPlayerEntity player) {
        int q = SideQuests.get(player, SideQuests.SPECIMENS);
        if (q == 0) {
            say(npc, player, "Oh! A visitor! I'm cataloguing the Reach. Would you fetch me three specimens?",
                    "A bucket of tropical fish, a sea pickle from the reefs, and a blue orchid from the bayou mud.");
            SideQuests.set(player, SideQuests.SPECIMENS, 1);
            player.sendMessage(Text.literal("New side quest: Specimens of the Reach").formatted(Formatting.YELLOW), false);
        } else if (q == 1) {
            boolean fish = QuestData.has(player, Items.TROPICAL_FISH_BUCKET);
            boolean pickle = QuestData.has(player, Items.SEA_PICKLE);
            boolean orchid = QuestData.has(player, Items.BLUE_ORCHID);
            if (fish && pickle && orchid) {
                QuestData.take(player, Items.TROPICAL_FISH_BUCKET);
                QuestData.take(player, Items.SEA_PICKLE);
                QuestData.take(player, Items.BLUE_ORCHID);
                SideQuests.set(player, SideQuests.SPECIMENS, 2);
                QuestData.give(player, new ItemStack(ModItems.DOUBLOON, 25));
                QuestData.give(player, new ItemStack(ModItems.MAP_FRAGMENT));
                com.cursedtides.pirate.PirateData.addXp(player, com.cursedtides.pirate.Weapon.EXPLORATION, 80);
                say(npc, player, "Marvellous! Splendid! For you: doubloons, and a scrap of an old map I found in the temple.");
            } else {
                say(npc, player, "Still need: " + (fish ? "" : "tropical fish (in a bucket) ") + (pickle ? "" : "a sea pickle ")
                        + (orchid ? "" : "a blue orchid") + ".");
            }
        } else {
            say(npc, player, "Your specimens are pressed and labelled. Science thanks you!");
        }
    }

    private static void medium(StoryNpcEntity npc, ServerPlayerEntity player) {
        int q = SideQuests.get(player, SideQuests.SHADE);
        if (q == 0) {
            say(npc, player, "You hear her too, don't you? Lady Morrow, in the house on the hill. Sixty years dead and still pacing.",
                    "She won't rest while she can still hold a blade. Go and... persuade her.");
            SideQuests.set(player, SideQuests.SHADE, 1);
            player.sendMessage(Text.literal("New side quest: The Restless Shade").formatted(Formatting.YELLOW), false);
        } else if (q == 1) {
            say(npc, player, "She's still there. I can feel her pacing. The big house, on the hill.");
        } else if (q == 2) {
            SideQuests.set(player, SideQuests.SHADE, 3);
            QuestData.give(player, new ItemStack(ModItems.DOUBLOON, 30));
            com.cursedtides.pirate.PirateData.addXp(player, com.cursedtides.pirate.Weapon.HEX, 120);
            say(npc, player, "Quiet at last. Thank you. Take this, and a little of what she taught me about the old words.");
        } else {
            say(npc, player, "The house is only a house now. Mostly.");
        }
    }

    private static void foreman(StoryNpcEntity npc, ServerPlayerEntity player) {
        int q = SideQuests.get(player, SideQuests.SEAM);
        var seam = com.cursedtides.dungeon.Dungeons.byName("The Deep Seam");
        if (q == 0) {
            say(npc, player, "Gold in these hills, and something down the Deep Seam that's killed three of my diggers.",
                    "Clear it to the bottom and open that old vault, and I'll pay you a share.");
            SideQuests.set(player, SideQuests.SEAM, 1);
            player.sendMessage(Text.literal("New side quest: Down the Deep Seam").formatted(Formatting.YELLOW), false);
        } else if (q == 1) {
            if (seam != null && com.cursedtides.dungeon.Dungeons.isCleared(player, seam)) {
                SideQuests.set(player, SideQuests.SEAM, 2);
                QuestData.give(player, new ItemStack(ModItems.DOUBLOON, 35));
                QuestData.give(player, new ItemStack(Items.GOLD_INGOT, 12));
                say(npc, player, "You did it! The diggers go back down tomorrow. Here's your share.");
            } else {
                say(npc, player, "The Deep Seam's entrance is just east of camp. Bring a key out of there and open the vault.");
            }
        } else {
            say(npc, player, "Best yield in years, thanks to you.");
        }
    }

    // ------------------------------------------------------------------ factions

    private static void commodore(StoryNpcEntity npc, ServerPlayerEntity player) {
        int navy = FactionData.rep(player, Faction.NAVY);
        int brethren = FactionData.rep(player, Faction.BRETHREN);

        if (FactionData.isWanted(player)) {
            int bounty = FactionData.bounty(player);
            if (player.getInventory().count(ModItems.DOUBLOON) >= bounty) {
                takeDoubloons(player, bounty);
                FactionData.set(player, Faction.NAVY, -50);
                say(npc, player,
                        "Bold, walking into my fortress with a price on your head. Bolder still to pay it.",
                        bounty + " doubloons. The ledger's square. Cross the Tidewatch again and it won't be.");
            } else {
                say(npc, player,
                        "You're wanted, and I've a brig with your name on it. The price of a pardon is "
                                + bounty + " doubloons.",
                        "Come back with the coin. Or don't come back.");
            }
            return;
        }
        if (brethren >= 300 && navy < 100) {
            say(npc, player, "I know whose tables you drink at in Saltmarrow. The Tidewatch has no work for Brethren.");
            return;
        }
        if (navy < 0) {
            say(npc, player, "Your record is... uneven. Bring me proof of good conduct before you ask for work.",
                    "(Put down cursed pirates or sink ghost ships to raise your standing with the Tidewatch.)");
            return;
        }
        contractBoard(npc, player, Faction.NAVY);
    }

    private static void broker(StoryNpcEntity npc, ServerPlayerEntity player) {
        int navy = FactionData.rep(player, Faction.NAVY);
        int brethren = FactionData.rep(player, Faction.BRETHREN);
        if (navy >= 300) {
            say(npc, player, "A Tidewatch privateer, drinking in my tavern? Finish your cup and go. No work for you here.");
            return;
        }
        if (brethren < -100) {
            say(npc, player, "The Brethren remember a turncoat. Earn it back before you sit at my table.");
            return;
        }
        if (FactionData.isWanted(player) && FactionData.contract(player).type() == 0) {
            say(npc, player, "Wanted, are you? Good. The Tidewatch hates you, and I like the company you keep.");
        }
        contractBoard(npc, player, Faction.BRETHREN);
    }

    static void contractBoard(StoryNpcEntity npc, ServerPlayerEntity player, Faction giver) {
        FactionData.Contract current = FactionData.contract(player);
        ContractType type = current.contractType();

        if (type != ContractType.NONE) {
            if (type.giver() != giver) {
                say(npc, player, giver == Faction.NAVY
                        ? "You're already running an errand for someone in Saltmarrow. Finish it, or drop it (/pirate contract abandon)."
                        : "You're carrying Tidewatch papers. Finish their errand or drop it (/pirate contract abandon).");
                return;
            }
            if (current.progress() >= type.goal()) {
                FactionData.setContract(player, FactionData.Contract.NONE);
                QuestData.give(player, new ItemStack(ModItems.DOUBLOON, type.doubloons()));
                FactionData.adjust(player, giver, type.reputation());
                say(npc, player, giver == Faction.NAVY
                                ? "Good work. The Tidewatch pays its debts: " + type.doubloons() + " doubloons."
                                : "Ha! That'll make them squeal at Ironwatch. " + type.doubloons() + " doubloons, as promised.",
                        "Come back when you want more work.");
                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.8f, 0.8f);
            } else {
                say(npc, player, "Still waiting on that job: " + type.description() + " ("
                        + current.progress() + "/" + type.goal() + ").");
            }
            return;
        }

        ContractType offer = pickContract(player, giver);
        FactionData.setContract(player, new FactionData.Contract(offer.id(), 0));
        String rank = FactionData.rankName(giver, FactionData.rep(player, giver));
        say(npc, player, giver == Faction.NAVY
                        ? "The Tidewatch has work for a capable " + rank.toLowerCase() + ". " + offer.description() + "."
                        : "Got a job for you, " + rank.toLowerCase() + ". " + offer.description() + ".",
                "Pay is " + offer.doubloons() + " doubloons, and you'll be better thought of here.");
        player.sendMessage(Text.literal("New contract: " + offer.description()).formatted(Formatting.YELLOW), false);
    }

    private static ContractType pickContract(ServerPlayerEntity player, Faction giver) {
        java.util.List<ContractType> options = new java.util.ArrayList<>();
        for (ContractType t : ContractType.values()) {
            if (t.giver() == giver) options.add(t);
        }
        return options.get(player.getRandom().nextInt(options.size()));
    }

    static void takeDoubloons(ServerPlayerEntity player, int amount) {
        var inv = player.getInventory();
        int left = amount;
        for (int i = 0; i < inv.size() && left > 0; i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isOf(ModItems.DOUBLOON)) {
                int take = Math.min(left, stack.getCount());
                stack.decrement(take);
                left -= take;
            }
        }
    }

    public static void say(StoryNpcEntity npc, ServerPlayerEntity player, String... lines) {
        String name = npc.getRole().displayName();
        for (String line : lines) {
            if (com.cursedtides.dialogue.Dialogue.capture(player, line)) continue;
            player.sendMessage(Text.literal("[" + name + "] ").formatted(Formatting.GOLD)
                    .append(Text.literal(line).formatted(Formatting.WHITE)), false);
        }
    }

    private static void hint(ServerPlayerEntity player, int stage) {
        player.sendMessage(Text.literal("Goal: ").formatted(Formatting.YELLOW)
                .append(QuestData.objective(player).copy().formatted(Formatting.GRAY)), false);
    }

    private static void advanced(ServerPlayerEntity player) {
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 0.5f, 1.3f);
        hint(player, QuestData.stage(player));
    }
}
