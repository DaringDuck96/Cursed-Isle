package com.cursedtides.quest;

import com.cursedtides.CursedTides;
import com.cursedtides.combat.Levels;
import com.cursedtides.dungeon.Delves;
import com.cursedtides.dungeon.Dungeons;
import com.cursedtides.entity.CursedPirateEntity;
import com.cursedtides.entity.StoryNpcEntity;
import com.cursedtides.entity.StoryNpcEntity.Role;
import com.cursedtides.item.ModItems;
import com.cursedtides.world.Archipelago;
import com.mojang.serialization.Codec;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/**
 * Errands: hand-written side quests for the people in the towns. Talk to someone with work going and they'll ask
 * you; accept, do it, and come back. Each has its own small story: lost dice, a siren's comb, a dead brother's
 * ship's bell, a letter that mustn't be read. Bring things, hunt things, find places, settle scores, carry letters.
 */
public final class Errands {
    private Errands() {}

    // ------------------------------------------------------------------ what an errand asks

    public sealed interface Step permits Fetch, Hunt, Slay, Find, Deliver {}

    /** Bring so many of something. */
    public record Fetch(Supplier<Item> item, int count, String what) implements Step {}

    /** Kill so many of something, anywhere. */
    public record Hunt(Predicate<LivingEntity> which, int count, String what) implements Step {}

    /** Kill one named enemy (and his friends), who turns up at a place when you get near. */
    public record Slay(String name, Supplier<EntityType<? extends Mob>> type, Supplier<int[]> where, String place) implements Step {}

    /** Go somewhere (within so many blocks; a y of Integer.MIN_VALUE means any height) and see what's there. */
    public record Find(Supplier<int[]> where, int radius, String place, String found) implements Step {}

    /** Carry something to someone, who has something to say about it. */
    public record Deliver(Role to, String parcel, String[] lines) implements Step {}

    public record Errand(String id, Role giver, String title, String[] offer, String reminder, Step step, String[] done,
                         int doubloons, Supplier<ItemStack> extra) {}

    // ------------------------------------------------------------------ where things are

    /** A point out in an island's wild hinterland (or its core, for islands without one), on land. */
    static int[] wild(Archipelago.Island is, double angleDeg, double frac) {
        int cx = is.hasLobe() ? is.lobeX() : is.cx(), cz = is.hasLobe() ? is.lobeZ() : is.cz();
        int r = is.hasLobe() ? is.lobeR() : is.radius();
        double a = Math.toRadians(angleDeg);
        for (double f = frac; f >= 0; f -= 0.05) {
            int x = cx + (int) (Math.cos(a) * r * f), z = cz + (int) (Math.sin(a) * r * f);
            if (Archipelago.column(x, z).isLand()) return new int[]{x, Integer.MIN_VALUE, z};
        }
        return new int[]{cx, Integer.MIN_VALUE, cz};
    }

    private static Supplier<int[]> delveHold(String name) {
        return () -> {
            com.cursedtides.world.Builds.groundY(0, 0);
            Delves.Delve d = Delves.byName(name);
            if (d == null) return null;
            Delves.Floor f = d.floors.get(0);
            return new int[]{d.x, f.y1(), d.z};
        };
    }

    private static Supplier<int[]> delveTop(String name) {
        return () -> {
            com.cursedtides.world.Builds.groundY(0, 0);
            Delves.Delve d = Delves.byName(name);
            if (d == null) return null;
            Delves.Floor f = d.floors.get(d.floors.size() - 1);
            return new int[]{d.x, f.y1(), d.z};
        };
    }

    private static Predicate<LivingEntity> named(String prefix) {
        return e -> e.hasCustomName() && e.getCustomName().getString().startsWith(prefix);
    }

    private static ItemStack named(Item item, int n, String name) {
        ItemStack s = new ItemStack(item, n);
        s.set(DataComponents.CUSTOM_NAME, Component.literal(name).withStyle(ChatFormatting.GOLD));
        return s;
    }

    // ------------------------------------------------------------------ the errands

    public static final List<Errand> ALL = List.of(
            new Errand("lamp_oil", Role.KEEPER, "Oil for the Lamp",
                    new String[]{"Captain, a small thing, and I'm ashamed to ask it. The lamp burns fish oil, and these old hands can't work a line like they used to.",
                            "Ten cod would see me through the month. You'll find them anywhere off the reef."},
                    "Ten cod, captain, when you've a moment. The lamp's burning low.",
                    new Fetch(() -> Items.COD, 10, "raw cod"),
                    new String[]{"Bless you. Smell that? That's a month of light.", "Here. It isn't much, but it's honest."},
                    20, () -> new ItemStack(Items.LANTERN, 2)),

            new Errand("anchor_rats", Role.HARBORMASTER, "Rats on the Anchor Chains",
                    new String[]{"The cursed have taken to climbing the anchor chains at night. Two of my night watch quit, and one won't stop screaming.",
                            "Put down ten of them. Anywhere in the Reach. Fewer dead men walking means fewer of them up my chains."},
                    "Ten of the cursed, captain. My watchmen are counting.",
                    new Hunt(e -> e instanceof CursedPirateEntity, 10, "the cursed"),
                    new String[]{"Last night, nobody came up the chains. First quiet night since spring.", "The harbour owes you. So do I, which is rarer."},
                    35, () -> new ItemStack(ModItems.PISTOL_SHOT, 16)),

            new Errand("dues_unpaid", Role.BROKER, "Dues Unpaid",
                    new String[]{"Corwen Pike. Calls himself a gentleman. Owes the Brethren eighteen months of dues and a ship he 'borrowed'.",
                            "He's camped in the scrub on Mariner's Rest with a few lads who think he's paying them. He isn't.",
                            "Bring him to account. Permanently, if he insists."},
                    "Gentleman Corwen Pike, Mariner's Rest. The Brethren don't forget, captain, and neither do I.",
                    new Slay("Gentleman Corwen Pike", () -> EntityType.PILLAGER, () -> wild(Archipelago.MARINERS_REST, 300, 0.5), "the scrub on Mariner's Rest"),
                    new String[]{"His signet ring? Lovely. I'll have it melted into something he'd have hated.", "The Brethren pay their debts too. Here's yours."},
                    45, () -> null),

            new Errand("lucky_bones", Role.GAMBLER, "Jory's Lucky Bones",
                    new String[]{"Lost 'em. My lucky dice. Carved from a drowned man's knuckles. Don't make that face.",
                            "Took a girl to see the Tide Caves on Driftwood. She was very impressed. I dropped 'em on the way out, near the mouth.",
                            "Fetch 'em back and I'll cut you in on my next sure thing."},
                    "Tide Caves, Driftwood Island, by the mouth. Two little yellow dice. Please.",
                    new Find(() -> { var d = Dungeons.byName("Tide Caves"); return d == null ? null : new int[]{d.ex, Integer.MIN_VALUE, d.ez}; }, 8,
                            "the mouth of the Tide Caves on Driftwood",
                            "Wedged between two rocks by the cave mouth: a pair of yellowed dice. You roll them once. Sixes."),
                    new String[]{"My babies! Sixes every time. Don't tell anyone.", "My next sure thing's a horse called Barnacle. Put your shirt on it. Actually, here's your share in advance."},
                    25, () -> null),

            new Errand("pit_beasts", Role.ARENA_MASTER, "Beasts for the Pit",
                    new String[]{"The crowd's bored of men hitting men. They want claws.",
                            "I can't ship a jaguar here alive, so prove to me you can kill three. Emerald Cay's jungle is crawling with them."},
                    "Three jaguars, captain. The crowd's restless.",
                    new Hunt(e -> e.getType() == com.cursedtides.wildlife.Wildlife.JAGUAR, 3, "jaguars"),
                    new String[]{"Three, and not a scratch on you. Well. A few scratches. The crowd'll love them.", "Your name's on tonight's bill. Try not to die before then."},
                    30, () -> new ItemStack(ModItems.CUTLASS)),

            new Errand("bone_rangers", Role.RANGE_MASTER, "The Bone Rangers",
                    new String[]{"Skeletons with bows, out on Hollowgrave. Somebody's taught them to shoot, and they shoot better than my recruits.",
                            "Go and show them what a real marksman looks like. Eight of them."},
                    "Eight skeletons. Aim small, miss small.",
                    new Hunt(e -> e.getType() == EntityType.SKELETON, 8, "skeletons"),
                    new String[]{"Eight! I'll tell the recruits you did it with your eyes shut. Builds character.", "Take these. Better shot than the stuff they sell in the square."},
                    25, () -> new ItemStack(ModItems.PISTOL_SHOT, 24)),

            new Errand("dry_still", Role.TAVERN_KEEPER, "Sugar for the Still",
                    new String[]{"The still's dry. A dry tavern is a quiet tavern, and a quiet tavern is a broke tavern.",
                            "Sixteen sugar cane. Bring 'em, and the first cask off the still has your name on it."},
                    "Sixteen sugar cane, love. I can hear the regulars getting sober.",
                    new Fetch(() -> Items.SUGAR_CANE, 16, "sugar cane"),
                    new String[]{"Lovely. Listen to that still bubble.", "And here's that cask, as promised. Don't drink it all at once. Or do, I'm not your mother."},
                    15, () -> new ItemStack(com.cursedtides.trade.TradeGood.RUM.item())),

            new Errand("scuttler_quinn", Role.BOUNTY_CLERK, "Wanted: Scuttler Quinn",
                    new String[]{"Scuttler Quinn. Sank four merchantmen by drilling their hulls in harbour, then bought the salvage rights for pennies.",
                            "Last seen ashore on Blackthorn Isle, in the hills behind the fort. Dead or alive pays the same, and dead is less paperwork."},
                    "Scuttler Quinn, Blackthorn's hills. The poster's still up.",
                    new Slay("Scuttler Quinn", () -> EntityType.VINDICATOR, () -> wild(Archipelago.BLACKTHORN_ISLE, 240, 0.55), "the hills of Blackthorn Isle"),
                    new String[]{"Quinn's drill. Nasty thing. I'll file it with the evidence.", "Bounty's paid in full. Sign here. And here. And here."},
                    50, () -> null),

            new Errand("mirefen_survey", Role.LAND_AGENT, "A Survey of Mirefen",
                    new String[]{"Someone has offered to buy the whole of Mirefen Bayou, sight unseen. I'd like to see it first.",
                            "Walk out into the bayou's interior and tell me what's there. Somewhere solid. Anywhere solid."},
                    "The heart of Mirefen Bayou, captain. Boots you don't mind losing.",
                    new Find(() -> wild(Archipelago.MIREFEN_BAYOU, 270, 0.3), 10, "the interior of Mirefen Bayou",
                            "Under a half-sunk cypress you find a surveyor's stake already driven in, with a Navy brand burned into the wood. Somebody's been here first."),
                    new String[]{"The Navy? Oh, that's interesting. That's very interesting.", "Thank you, captain. You saw nothing. Your fee includes the nothing."},
                    40, () -> null),

            new Errand("powder_short", Role.GUNNERY_SERGEANT, "Powder Shortage",
                    new String[]{"The harbour batteries are down to their last six charges. The quartermaster says the shipment's 'delayed'. It's been delayed since spring.",
                            "Twelve measures of powder. Creepers carry it, if you're the sort to go looking."},
                    "Twelve measures of gunpowder, captain. The guns are hungry.",
                    new Fetch(() -> Items.GUNPOWDER, 12, "gunpowder"),
                    new String[]{"Now that's a smell I've missed. The batteries thank you, and so do I.", "Have some shot from the Navy stores. Nobody counts it."},
                    25, () -> new ItemStack(ModItems.CANNONBALL, 12)),

            new Errand("siren_comb", Role.BARBER, "The Siren's Comb",
                    new String[]{"Do you know what they say a siren's comb does? Makes your hair grow back thick as rope.",
                            "I don't need it, obviously. A customer does. A rich, bald customer.",
                            "They sing on the open water, mostly at night. Mind your ears, and mind your helm."},
                    "One siren's comb. Out on the open sea, at night. Plug your ears.",
                    new Hunt(named("Siren of the Shoals"), 1, "a Siren of the Shoals"),
                    new String[]{"Oh, it's beautiful. It's humming. Is it supposed to hum?", "Anyway. My customer pays well. Here's your cut, and a free trim whenever you like."},
                    45, () -> null),

            new Errand("deep_ink", Role.TATTOOIST, "Ink From the Deep",
                    new String[]{"Every tattoo I do with ordinary ink fades to grey in a year of salt.",
                            "The glow squid down in the deep caves leave an ink that never fades. Six sacs, and your next tattoo's on me."},
                    "Six glow ink sacs. The deep caves, where the light comes from the walls.",
                    new Fetch(() -> Items.GLOW_INK_SAC, 6, "glow ink sacs"),
                    new String[]{"Look at that colour. It'll outlive both of us.", "Here. For your trouble, before the free tattoo."},
                    30, () -> null),

            new Errand("discreet_letter", Role.BANKER, "A Discreet Letter",
                    new String[]{"I need a courier who can't read. Failing that, one who won't.",
                            "This letter goes to Governor Harrow in Port Meridian. Into her hand and nobody else's."},
                    "Governor Harrow, Port Meridian. Into her hand.",
                    new Deliver(Role.GOVERNOR, "Sealed Letter for Governor Harrow",
                            new String[]{"From Fairweather? Give it here.", "...I see. Tell him the answer is yes. And tell him I said it through my teeth."}),
                    new String[]{"Yes? Through her teeth? Excellent. That's the best kind of yes.", "Your fee, captain. And my discretion, which is worth more."},
                    40, () -> null),

            new Errand("lot_77", Role.AUCTIONEER, "Lot Seventy-Seven",
                    new String[]{"Lot seventy-seven: a captain's sword from the Weeping Duchess, the wreck off the Sunken Reach. Sold, paid for, never delivered.",
                            "Because the captain's still holding it. Down in the hold. Dead, but holding it.",
                            "Pry it loose. I keep my reputation, you keep a commission."},
                    "Captain Saltbones, the Weeping Duchess, off the Sunken Reach. Lot seventy-seven awaits.",
                    new Hunt(named("Captain Saltbones"), 1, "Captain Saltbones"),
                    new String[]{"Going once, going twice... delivered! The buyer will be thrilled.", "Your commission, captain, less my commission on your commission."},
                    60, () -> null),

            new Errand("signal_log", Role.ARCHIVIST, "The Keeper's Logbook",
                    new String[]{"The Old Signal Tower on Ironwatch kept a log of every ship that passed for eighty years. Then the keeper went strange and barred the door.",
                            "If the log survives, it'll be at the top, by his fire. I'd give a great deal to read it."},
                    "The top of the Old Signal Tower, Ironwatch Isle. Mind the stairs. And whatever's on them.",
                    new Find(delveTop("The Old Signal Tower"), 7, "the top of the Old Signal Tower on Ironwatch",
                            "In a lead box by the cold signal fire: a swollen logbook. The last forty pages say the same thing, over and over. THEY ARE STILL PASSING."),
                    new String[]{"'They are still passing.' Oh. Oh, how wonderful. How horrible.", "Thank you, captain. Here: the archive's thanks, and a copy of anything you like from the stacks."},
                    40, () -> new ItemStack(ModItems.TREASURE_MAP)),

            new Errand("drowned_candles", Role.ABBESS, "Candles for the Drowned",
                    new String[]{"Every year on the Night of Lanterns we light a candle for each soul the sea has taken. This year there are more souls than wax.",
                            "Eight honeycombs, and the sisters will do the rest."},
                    "Eight honeycombs, child. Wild hives, on the green islands.",
                    new Fetch(() -> Items.HONEYCOMB, 8, "honeycombs"),
                    new String[]{"There will be enough now. Every one of them remembered.", "Take this blessing with you. The sea is kinder to those who remember it."},
                    20, () -> new ItemStack(Items.GOLDEN_APPLE)),

            new Errand("bloodfin_problem", Role.CONTEST_JUDGE, "The Bloodfin Problem",
                    new String[]{"The fishing contest is in ruins. Bloodfin packs have eaten three entries and a judge. Not me. The other judge.",
                            "Thin them out, would you? Five should teach them manners. They hunt out in open water, around ships."},
                    "Five Bloodfins. Open water. Keep your fingers inside the boat.",
                    new Hunt(named("Bloodfin"), 5, "Bloodfin sharks"),
                    new String[]{"Five! The contest is saved. The other judge is still eaten, but you can't have everything.", "A prize for you, then. The judges' decision is final."},
                    30, () -> new ItemStack(ModItems.SHARK_TOOTH, 4)),

            new Errand("mary_clare_bell", Role.DIVER, "The Bell of the Mary Clare",
                    new String[]{"My brother Amos went down with the Mary Clare off Crooked Reef. I've dived that reef a hundred times and never found her.",
                            "Folk say she's lying out past the shoals now, upright, as if she's waiting. If you find her, bring me her bell. It's all I want."},
                    "The Mary Clare, out past Crooked Reef. Her bell. Please.",
                    new Find(delveHold("The Mary Clare"), 8, "the hold of the Mary Clare, off Crooked Reef",
                            "In the hold, wrapped in rotted sailcloth as if someone had put it to bed: the ship's bell, MARY CLARE engraved around the lip."),
                    new String[]{"...That's her. That's her bell.", "Thank you. I'll hang it over his empty grave. Maybe he'll hear it."},
                    45, () -> null),

            new Errand("deserters", Role.COMMODORE, "Deserters",
                    new String[]{"Six of my marines deserted to join a pirate crew. They took their muskets, and my second-best hat.",
                            "By now they'll be among the pillagers raiding the outer isles. I don't need them back. I need them stopped."},
                    "Six pillagers, captain. Any six. They all look alike in the end.",
                    new Hunt(e -> e.getType() == EntityType.PILLAGER, 6, "pillagers"),
                    new String[]{"Six. The Tidewatch is in your debt. Officially, quietly.", "If you find my hat, I'd like it back."},
                    40, () -> null),

            new Errand("forge_copper", Role.BELLWRIGHT, "Copper for the Forge",
                    new String[]{"Since the Stilling Bell, every captain in the Reach wants a bell of their own. I'm out of copper.",
                            "Sixteen ingots. I'll cast you something that rings true."},
                    "Sixteen copper ingots. The forge is waiting.",
                    new Fetch(() -> Items.COPPER_INGOT, 16, "copper ingots"),
                    new String[]{"That'll make three bells and a spare. And this one's for you.", "Ring it before a battle. The cursed hate it."},
                    20, () -> new ItemStack(Items.BELL))
    );

    // ------------------------------------------------------------------ progress

    private static final int NONE = 0, ACTIVE = 1, READY = 2, DONE = 3;

    public static final AttachmentType<Map<String, Integer>> STATE = AttachmentRegistry.<Map<String, Integer>>builder()
            .persistent(Codec.unboundedMap(Codec.STRING, Codec.INT)).copyOnDeath().initializer(HashMap::new)
            .buildAndRegister(CursedTides.id("errands"));

    private static int get(ServerPlayer p, String key) {
        return p.getAttachedOrElse(STATE, Map.of()).getOrDefault(key, 0);
    }

    private static void put(ServerPlayer p, String key, int v) {
        Map<String, Integer> m = new HashMap<>(p.getAttachedOrElse(STATE, Map.of()));
        m.put(key, v);
        p.setAttached(STATE, m);
    }

    private static Errand byId(String id) {
        for (Errand e : ALL) if (e.id().equals(id)) return e;
        return null;
    }

    private static boolean ready(ServerPlayer p) {
        return Lessons.step(p) == Lessons.Step.DONE;
    }

    /** When each person last offered work to each player, so a second word gets their usual business. */
    /** For the marker over a person's head: 0 nothing, 1 has work for you, 2 waiting for you to come back. */
    public static int marker(ServerPlayer p, Role role) {
        if (!ready(p)) return 0;
        int best = 0;
        for (Errand e : ALL) {
            int s = get(p, e.id());
            if (s == ACTIVE && e.step() instanceof Deliver d && d.to() == role) return 2;
            if (e.giver() != role) continue;
            if (s == READY || (s == ACTIVE && e.step() instanceof Fetch f && count(p, f.item().get()) >= f.count())) return 2;
            if (s == NONE) best = 1;
        }
        return best;
    }

    private static final Map<String, Long> OFFERED = new HashMap<>();

    // ------------------------------------------------------------------ talking

    private static void say(StoryNpcEntity npc, ServerPlayer p, String... lines) {
        String name = npc.getRole().displayName();
        for (String l : lines) {
            p.displayClientMessage(Component.literal("[" + name + "] ").withStyle(ChatFormatting.GOLD).append(Component.literal(l).withStyle(ChatFormatting.WHITE)), false);
        }
    }

    /**
     * Called before an NPC's usual conversation. Returns true when the errand business took the turn: handing in, a
     * delivery, or offering new work (once every few minutes; talk again for the usual business).
     */
    public static boolean interact(StoryNpcEntity npc, ServerPlayer p) {
        if (!ready(p)) return false;
        Role role = npc.getRole();
        // A delivery for this person?
        for (Errand e : ALL) {
            if (get(p, e.id()) == ACTIVE && e.step() instanceof Deliver d && d.to() == role) {
                if (take(p, d.parcel())) {
                    say(npc, p, d.lines());
                    put(p, e.id(), READY);
                    p.displayClientMessage(Component.literal("Delivered. Go back to " + e.giver().displayName() + ".").withStyle(ChatFormatting.GREEN), false);
                    return true;
                }
            }
        }
        for (Errand e : ALL) {
            if (e.giver() != role) continue;
            int s = get(p, e.id());
            if (s == READY || (s == ACTIVE && e.step() instanceof Fetch f && count(p, f.item().get()) >= f.count())) {
                if (e.step() instanceof Fetch f) remove(p, f.item().get(), f.count());
                complete(npc, p, e);
                return true;
            }
            if (s == ACTIVE) {
                say(npc, p, e.reminder());
                return false; // and on to their usual business
            }
            if (s == NONE) {
                long now = p.serverLevel().getGameTime();
                String key = p.getUUID() + "|" + role.name();
                Long last = OFFERED.get(key);
                if (last != null && now - last < 20 * 60 * 3) return false;
                OFFERED.put(key, now);
                p.displayClientMessage(Component.literal("Errand: " + e.title()).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD), false);
                say(npc, p, e.offer());
                p.displayClientMessage(Component.literal("  [Accept]").withStyle(st -> st.withColor(ChatFormatting.GREEN)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/errand accept " + e.id())))
                        .append(Component.literal("   [Not now]").withStyle(st -> st.withColor(ChatFormatting.GRAY)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/errand later " + e.id()))))
                        .append(Component.literal("   (talk again for their usual business)").withStyle(ChatFormatting.DARK_GRAY)), false);
                return true;
            }
        }
        return false;
    }

    private static void accept(ServerPlayer p, String id) {
        Errand e = byId(id);
        if (e == null || get(p, id) != NONE || !ready(p)) return;
        put(p, id, ACTIVE);
        put(p, id + "#n", 0);
        p.displayClientMessage(Component.literal("Errand accepted: " + e.title()).withStyle(ChatFormatting.GREEN), false);
        p.displayClientMessage(Component.literal("  " + objective(p, e)).withStyle(ChatFormatting.GRAY), false);
        if (e.step() instanceof Deliver d) {
            ItemStack parcel = new ItemStack(Items.PAPER);
            parcel.set(DataComponents.CUSTOM_NAME, Component.literal(d.parcel()).withStyle(ChatFormatting.GOLD));
            QuestData.give(p, parcel);
        }
        p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1f, 1f);
    }

    private static void complete(StoryNpcEntity npc, ServerPlayer p, Errand e) {
        put(p, e.id(), DONE);
        say(npc, p, e.done());
        com.cursedtides.trade.Wallet.give(p, e.doubloons() * com.cursedtides.trade.Wallet.SILVER_PER_DOUBLOON);
        ItemStack extra = e.extra().get();
        if (extra != null && !extra.isEmpty()) QuestData.give(p, extra);
        com.cursedtides.pirate.PirateData.addXp(p, com.cursedtides.pirate.Weapon.SAILING, 40);
        p.displayClientMessage(Component.literal("Errand complete: " + e.title() + " (+" + e.doubloons() + " doubloons)").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 0.6f, 1.2f);
        com.cursedtides.activity.Achievements.grant(p, "errand_runner");
        if (doneCount(p) >= ALL.size()) com.cursedtides.activity.Achievements.grant(p, "pillar_of_the_reach");
    }

    private static int doneCount(ServerPlayer p) {
        int n = 0;
        for (Errand e : ALL) if (get(p, e.id()) == DONE) n++;
        return n;
    }

    // ------------------------------------------------------------------ inventory helpers

    private static int count(ServerPlayer p, Item item) {
        return p.getInventory().countItem(item);
    }

    private static void remove(ServerPlayer p, Item item, int n) {
        var inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize() && n > 0; i++) {
            ItemStack s = inv.getItem(i);
            if (!s.is(item)) continue;
            int t = Math.min(n, s.getCount());
            s.shrink(t);
            n -= t;
        }
    }

    private static boolean take(ServerPlayer p, String parcelName) {
        var inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(Items.PAPER) && s.has(DataComponents.CUSTOM_NAME) && s.getHoverName().getString().equals(parcelName)) {
                s.shrink(1);
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ hunting, slaying, finding

    public static void register() {
        STATE.identifier();
        ServerLivingEntityEvents.AFTER_DEATH.register((victim, source) -> {
            if (source.getEntity() instanceof ServerPlayer p) onKill(p, victim);
            for (String t : victim.getTags()) {
                if (!t.startsWith(SLAY_TAG) || !(victim.level() instanceof ServerLevel w)) continue;
                String id = t.substring(SLAY_TAG.length());
                Errand e = byId(id);
                if (e == null || !(e.step() instanceof Slay sl) || !victim.hasCustomName() || !victim.getCustomName().getString().startsWith(sl.name())) continue;
                for (ServerPlayer p : w.getPlayers(q -> q.distanceToSqr(victim) < 64 * 64 && get(q, id) == ACTIVE)) {
                    put(p, id, READY);
                    p.displayClientMessage(Component.literal(sl.name() + " is dead. Go back to " + e.giver().displayName() + ".").withStyle(ChatFormatting.GREEN), false);
                }
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(Errands::tick);
        CommandRegistrationCallback.EVENT.register((d, a, env) -> d.register(literal("errand")
                .then(literal("accept").then(argument("id", StringArgumentType.word()).executes(ctx -> {
                    accept(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "id"));
                    return 1;
                })))
                .then(literal("later").then(argument("id", StringArgumentType.word()).executes(ctx -> {
                    ctx.getSource().getPlayerOrException().displayClientMessage(Component.literal("Maybe another time. (They'll ask again later.)").withStyle(ChatFormatting.GRAY), false);
                    return 1;
                })))));
    }

    /** Any kill by a player: counts for Hunt errands. */
    public static void onKill(ServerPlayer p, LivingEntity victim) {
        for (Errand e : ALL) {
            if (get(p, e.id()) != ACTIVE || !(e.step() instanceof Hunt h) || !h.which().test(victim)) continue;
            int n = get(p, e.id() + "#n") + 1;
            put(p, e.id() + "#n", n);
            if (n >= h.count()) {
                put(p, e.id(), READY);
                p.displayClientMessage(Component.literal(e.title() + ": done (" + h.count() + "/" + h.count() + " " + h.what() + "). Go back to "
                        + e.giver().displayName() + ".").withStyle(ChatFormatting.GREEN), false);
            } else {
                p.displayClientMessage(Component.literal(e.title() + ": " + n + "/" + h.count() + " " + h.what()).withStyle(ChatFormatting.GRAY), true);
            }
        }
    }

    private static final String SLAY_TAG = "ct_errand:";

    private static void tick(MinecraftServer server) {
        if (server.getTickCount() % 40 != 23) return;
        ServerLevel w = server.overworld();
        for (ServerPlayer p : w.players()) {
            for (Errand e : ALL) {
                if (get(p, e.id()) != ACTIVE) continue;
                if (e.step() instanceof Find f) {
                    int[] at = f.where().get();
                    if (at == null) continue;
                    double dx = p.getX() - at[0], dz = p.getZ() - at[2];
                    boolean yok = at[1] == Integer.MIN_VALUE || Math.abs(p.getY() - at[1]) < 6;
                    if (dx * dx + dz * dz < f.radius() * f.radius() && yok) {
                        put(p, e.id(), READY);
                        p.displayClientMessage(Component.literal(f.found()).withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC), false);
                        p.displayClientMessage(Component.literal("Go back to " + e.giver().displayName() + ".").withStyle(ChatFormatting.GREEN), false);
                        w.playSound(null, p.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1f, 0.8f);
                    }
                } else if (e.step() instanceof Slay sl) {
                    int[] at = sl.where().get();
                    if (at == null || p.distanceToSqr(at[0], p.getY(), at[2]) > 48 * 48) continue;
                    String tag = SLAY_TAG + e.id();
                    AABB box = new AABB(at[0] - 64, w.getMinBuildHeight(), at[2] - 64, at[0] + 64, w.getMaxBuildHeight(), at[2] + 64);
                    if (!w.getEntitiesOfClass(Mob.class, box, m -> m.getTags().contains(tag)).isEmpty()) continue;
                    spawnMark(w, e, sl, at, tag, p);
                }
            }
        }
    }

    private static void spawnMark(ServerLevel w, Errand e, Slay sl, int[] at, String tag, ServerPlayer p) {
        int level = Math.min(50, Levels.bandAt(new BlockPos(at[0], 64, at[2]))[1] + 2);
        for (int i = 0; i < 3; i++) {
            BlockPos pos = com.cursedtides.living.Residents.openGround(w, at[0] + (i == 0 ? 0 : (i == 1 ? 3 : -3)), at[2] + (i == 0 ? 0 : 2), 8);
            if (pos == null) continue;
            Mob m = sl.type().get().create(w);
            if (m == null) continue;
            m.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, w.random.nextFloat() * 360, 0);
            m.finalizeSpawn(w, w.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null);
            if (i == 0) {
                m.setCustomName(Component.literal(sl.name()).withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                m.setCustomNameVisible(true);
            }
            Levels.apply(m, i == 0 ? level + 2 : level);
            if (i == 0) {
                AttributeInstance hp = m.getAttribute(Attributes.MAX_HEALTH);
                if (hp != null) { hp.setBaseValue(hp.getBaseValue() * 3 * Levels.healthFactor(level + 2)); m.setHealth(m.getMaxHealth()); }
            }
            m.addTag(tag);
            m.setPersistenceRequired();
            w.addFreshEntity(m);
            m.setTarget(p);
        }
        p.displayClientMessage(Component.literal(sl.name() + " is here, and not alone.").withStyle(ChatFormatting.RED), false);
    }

    // ------------------------------------------------------------------ the journal and the chart

    private static String objective(ServerPlayer p, Errand e) {
        Step s = e.step();
        if (get(p, e.id()) == READY) return "Return to " + e.giver().displayName() + ".";
        if (s instanceof Fetch f) return "Bring " + f.count() + " " + f.what() + " to " + e.giver().displayName()
                + " (" + Math.min(f.count(), count(p, f.item().get())) + "/" + f.count() + ").";
        if (s instanceof Hunt h) return "Kill " + h.count() + " " + h.what() + " (" + get(p, e.id() + "#n") + "/" + h.count() + ").";
        if (s instanceof Slay sl) return "Find and kill " + sl.name() + " in " + sl.place() + ".";
        if (s instanceof Find f) return "Search " + f.place() + ".";
        if (s instanceof Deliver d) return "Take the " + d.parcel() + " to " + d.to().displayName() + ".";
        return "";
    }

    public static List<String> journal(ServerPlayer p) {
        List<String> lines = new ArrayList<>();
        for (Errand e : ALL) {
            int s = get(p, e.id());
            if (s == ACTIVE || s == READY) lines.add("§f" + e.title() + ": §7" + objective(p, e));
        }
        lines.add("§7Errands done: " + doneCount(p) + "/" + ALL.size() + (lines.isEmpty() ? " (townsfolk with work will ask when you talk to them)" : ""));
        return lines;
    }

    private static int[] npcSpot(Role role) {
        for (com.cursedtides.world.Builds.NpcSpot s : com.cursedtides.world.Builds.NPC_SPOTS) if (s.role() == role) return new int[]{(int) s.x(), (int) s.z()};
        for (com.cursedtides.living.Residents.Home h : com.cursedtides.living.Residents.HOMES) {
            if (h.role() == role) return new int[]{h.island().cx() + h.dx(), h.island().cz() + h.dz()};
        }
        return null;
    }

    private static Errand firstActive(ServerPlayer p) {
        for (Errand e : ALL) {
            int s = get(p, e.id());
            if (s == ACTIVE || s == READY) return e;
        }
        return null;
    }

    public static int[] target(ServerPlayer p) {
        Errand e = firstActive(p);
        if (e == null) return null;
        if (get(p, e.id()) == READY) return npcSpot(e.giver());
        Step s = e.step();
        if (s instanceof Slay sl) { int[] a = sl.where().get(); return a == null ? null : new int[]{a[0], a[2]}; }
        if (s instanceof Find f) { int[] a = f.where().get(); return a == null ? null : new int[]{a[0], a[2]}; }
        if (s instanceof Deliver d) return npcSpot(d.to());
        if (s instanceof Fetch f && count(p, f.item().get()) >= f.count()) return npcSpot(e.giver());
        return null;
    }

    public static String targetLabel(ServerPlayer p) {
        Errand e = firstActive(p);
        return e == null ? "" : e.title();
    }
}
