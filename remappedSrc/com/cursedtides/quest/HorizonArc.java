package com.cursedtides.quest;

import com.cursedtides.CursedTides;
import com.cursedtides.dungeon.Dungeon;
import com.cursedtides.dungeon.Dungeons;
import com.cursedtides.entity.ShipEntity;
import com.cursedtides.entity.StoryNpcEntity;
import com.cursedtides.faction.Faction;
import com.cursedtides.faction.FactionData;
import com.cursedtides.item.ModItems;
import com.cursedtides.ship.FlagItem;
import com.cursedtides.world.Archipelago;
import com.cursedtides.world.Builds;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Story arc two: "The Seven Tides". Seven relics of the drowned people who built the Horizon Gate,
 * scattered across the Reach; a secret society (the Lantern Court) that wants them; a pirate coalition
 * (Rook Varga's Free Accord) that means to take them at the last; the lost flagship of the Meridian Fleet;
 * and three endings.
 */
public final class HorizonArc {
    private HorizonArc() {}

    public enum Relic {
        DAWN("Dawn Relic", "The Wayfarer, at the Last Horizon"),
        REEF("Reef Relic", "The vault of the Hull Warrens, Crooked Reef"),
        SERPENT("Serpent Relic", "The vault of the Coiled Temple, Serpent's Hollow"),
        EMBER("Ember Relic", "The Cinder King, in Emberfall's crater"),
        VEIL("Veil Relic", "The Veil Witch, on Blackveil Atoll"),
        SOVEREIGN("Sovereign Relic", "The ghost of the Sovereign Dawn, over Crooked Reef at night"),
        LANTERN("Lantern Relic", "The Lantern Court's hand in Port Meridian");

        public final String displayName;
        public final String where;

        Relic(String displayName, String where) {
            this.displayName = displayName;
            this.where = where;
        }

        public Item item() {
            return ModItems.RELICS.get(ordinal());
        }
    }

    // Stages
    public static final int NONE = 0, SEEK_SCHOLAR = 1, GATHER = 2, TO_GATE = 3, TURNCOAT = 4, OPEN_GATE = 5,
            SOVEREIGN_FIGHT = 6, CHOOSE = 7, DONE = 8;
    // Flags
    private static final int COURT = 1, EXPOSED = 2, MET_ROOK = 4, OFFERED = 8, ADMIRAL_THANKED = 16, END_OFFERED = 32;
    // Endings
    public static final int SEALED = 1, CLAIMED = 2, COURT_ENDING = 3;

    private static final String ROOK_TAG = "cursedtides_arc_rook", SOVEREIGN_TAG = "cursedtides_arc_sovereign",
            HAND_TAG = "cursedtides_arc_hand", SHIP_TAG = "cursedtides_sovereign_dawn";

    private static AttachmentType<Integer> attachment(String name) {
        return AttachmentRegistry.<Integer>builder().persistent(Codec.INT).copyOnDeath().initializer(() -> 0)
                .buildAndRegister(CursedTides.id(name));
    }

    public static final AttachmentType<Integer> STAGE = attachment("horizon_stage");
    public static final AttachmentType<Integer> RELICS = attachment("horizon_relics");
    public static final AttachmentType<Integer> FLAGS = attachment("horizon_flags");
    public static final AttachmentType<Integer> ENDING = attachment("horizon_ending");

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register(HorizonArc::onDeath);
        ServerTickEvents.END_SERVER_TICK.register(HorizonArc::tick);
    }

    public static int stage(Player p) { return p.getAttachedOrElse(STAGE, 0); }
    public static int relicCount(Player p) { return Integer.bitCount(p.getAttachedOrElse(RELICS, 0)); }

    public static boolean hasRelic(Player p, String name) {
        for (Relic r : Relic.values()) if (r.name().equalsIgnoreCase(name)) return hasRelic(p, r);
        return false;
    }

    public static boolean hasFlag(Player p, String name) {
        int f = switch (name) {
            case "court" -> COURT;
            case "exposed" -> EXPOSED;
            case "met_rook" -> MET_ROOK;
            case "offered" -> OFFERED;
            case "end_offered" -> END_OFFERED;
            default -> 0;
        };
        return f != 0 && flag(p, f);
    }

    public static String endingName(Player p) {
        return switch (ending(p)) {
            case SEALED -> "sealed";
            case CLAIMED -> "claimed";
            case COURT_ENDING -> "court";
            default -> "none";
        };
    }

    public static boolean gateOpened(MinecraftServer server) {
        return server != null && GateState.get(server).gateOpened;
    }
    public static int ending(Player p) { return p.getAttachedOrElse(ENDING, 0); }
    private static int relics(Player p) { return p.getAttachedOrElse(RELICS, 0); }
    private static boolean hasRelic(Player p, Relic r) { return (relics(p) & (1 << r.ordinal())) != 0; }
    private static int count(Player p) { return Integer.bitCount(relics(p)); }
    private static boolean flag(Player p, int f) { return (p.getAttachedOrElse(FLAGS, 0) & f) != 0; }
    private static void setFlag(Player p, int f) { p.setAttached(FLAGS, p.getAttachedOrElse(FLAGS, 0) | f); }
    private static void setStage(Player p, int s) { p.setAttached(STAGE, s); }

    // ------------------------------------------------------------------ relics

    static void grant(ServerPlayer p, Relic r) {
        if (stage(p) != GATHER && !(r == Relic.DAWN && stage(p) <= SEEK_SCHOLAR)) return;
        if (hasRelic(p, r)) return;
        p.setAttached(RELICS, relics(p) | (1 << r.ordinal()));
        QuestData.give(p, new ItemStack(r.item()));
        int n = count(p);
        if (n >= 7) com.cursedtides.activity.Achievements.grant(p, "seven_relics");
        p.displayClientMessage(Component.literal("Relic recovered: " + r.displayName + " (" + n + "/7)").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        p.level().playSound(null, p.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8f, 1.3f);
        if (n == 4) {
            p.displayClientMessage(Component.literal("The sea feels wrong tonight. The curse stirs as the relics come together.")
                    .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC), false);
        }
        if (n == 7) {
            setStage(p, TO_GATE);
            p.displayClientMessage(Component.literal("All seven. Take them to the Wayfarer at the Last Horizon.").withStyle(ChatFormatting.YELLOW), false);
        }
    }

    /** Arc players near an event share in it: relics are for the whole crew. */
    private static List<ServerPlayer> nearby(Entity at, double range) {
        List<ServerPlayer> out = new ArrayList<>();
        if (at.level() instanceof ServerLevel w) {
            for (ServerPlayer p : w.getPlayers(p -> p.distanceToSqr(at) < range * range)) out.add(p);
        }
        return out;
    }

    /** Called whenever a vault is opened, even one opened before. */
    public static void onVault(ServerPlayer opener, Dungeon d) {
        Relic r = d.index == 30 ? Relic.REEF : d.index == 36 ? Relic.SERPENT : null;
        if (r == null) return;
        for (ServerPlayer p : nearby(opener, 32)) grant(p, r);
    }

    public static void onShipSunk(ServerPlayer sinker, ShipEntity ship) {
        if (!ship.getTags().contains(SHIP_TAG)) return;
        for (ServerPlayer p : nearby(ship, 96)) grant(p, Relic.SOVEREIGN);
        grant(sinker, Relic.SOVEREIGN);
        ship.spawnAtLocation(new ItemStack(FlagItem.item(FlagItem.Design.KRAKEN)));
        if (ship.getServer() != null) {
            ship.getServer().getPlayerList().broadcastSystemMessage(Component.literal("The Sovereign Dawn, flagship of the lost Meridian Fleet, sinks a second time.")
                    .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), false);
        }
    }

    /** The waterlogged log of the Meridian Fleet, read aloud at night over Crooked Reef. */
    public static boolean summonSovereign(ServerPlayer p) {
        ServerLevel world = p.serverLevel();
        var reef = Archipelago.CROOKED_REEF;
        if (Math.hypot(p.getX() - reef.cx(), p.getZ() - reef.cz()) > 150) {
            p.displayClientMessage(Component.literal("The log's last entry: \"...going down over the reef they call Crooked...\" Read it there.")
                    .withStyle(ChatFormatting.GRAY), false);
            return false;
        }
        if (!world.isNight()) {
            p.displayClientMessage(Component.literal("The ink only shows by moonlight. Wait for night.").withStyle(ChatFormatting.GRAY), false);
            return false;
        }
        if (!world.getEntitiesOfClass(ShipEntity.class, p.getBoundingBox().inflate(200), s -> s.getTags().contains(SHIP_TAG)).isEmpty()) {
            p.displayClientMessage(Component.literal("She's already out there.").withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        BlockPos at = com.cursedtides.ship.ShipEvents.findSeaSpot(world, p, 50, 75);
        if (at == null) {
            p.displayClientMessage(Component.literal("Get out onto open water first.").withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        ShipEntity ship = com.cursedtides.ship.ShipEvents.spawnShip(world, com.cursedtides.ship.ShipFaction.DROWNED,
                at.getX() + 0.5, at.getY() - 0.1, at.getZ() + 0.5, p);
        if (ship == null) return false;
        ship.makeLegendary("The Sovereign Dawn");
        ship.addTag(SHIP_TAG);
        world.playSound(null, at, SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 3.0f, 0.5f);
        p.getServer().getPlayerList().broadcastSystemMessage(Component.literal("A ghost galleon rises over Crooked Reef: the Sovereign Dawn, lost these forty years.")
                .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), false);
        return true;
    }

    // ------------------------------------------------------------------ bosses

    private static Mob boss(ServerLevel w, double x, double y, double z, Function<ServerLevel, ? extends Mob> f,
                                  String name, float health, double scale, String tag, ItemStack weapon) {
        Mob mob = f.apply(w);
        if (mob == null) return null;
        mob.moveTo(x, y, z, 0, 0);
        mob.finalizeSpawn(w, w.getCurrentDifficultyAt(BlockPos.containing(x, y, z)), MobSpawnType.EVENT, null);
        mob.setCustomName(Component.literal(name).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        mob.setCustomNameVisible(true);
        mob.setPersistenceRequired();
        mob.addTag(tag);
        var hp = mob.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null) hp.setBaseValue(health);
        mob.setHealth(health);
        var size = mob.getAttribute(Attributes.SCALE);
        if (size != null) size.setBaseValue(scale);
        var armor = mob.getAttribute(Attributes.ARMOR);
        if (armor != null) armor.setBaseValue(12);
        if (weapon != null) {
            mob.setItemSlot(EquipmentSlot.MAINHAND, weapon);
            mob.setDropChance(EquipmentSlot.MAINHAND, 0.0f);
        }
        w.addFreshEntity(mob);
        return mob;
    }

    private static void minion(ServerLevel w, double x, double y, double z, Function<ServerLevel, ? extends Mob> f, String name) {
        Mob mob = f.apply(w);
        if (mob == null) return;
        mob.moveTo(x, y, z, 0, 0);
        mob.finalizeSpawn(w, w.getCurrentDifficultyAt(BlockPos.containing(x, y, z)), MobSpawnType.EVENT, null);
        mob.setCustomName(Component.literal(name));
        mob.setPersistenceRequired();
        mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.TURTLE_HELMET));
        w.addFreshEntity(mob);
    }

    private static boolean bossNear(Entity at, String tag) {
        return !at.level().getEntitiesOfClass(Mob.class, at.getBoundingBox().inflate(64),
                m -> m.isAlive() && m.getTags().contains(tag)).isEmpty();
    }

    private static int gateY() {
        return Builds.groundY(Archipelago.LAST_HORIZON.cx(), Archipelago.LAST_HORIZON.cz());
    }

    private static void ambush(ServerPlayer p) {
        ServerLevel w = p.serverLevel();
        int gx = Archipelago.LAST_HORIZON.cx(), gz = Archipelago.LAST_HORIZON.cz(), y = gateY();
        boss(w, gx + 0.5, y, gz - 9.5, EntityType.VINDICATOR::create, "Rook Varga, the Turncoat", 220, 1.2, ROOK_TAG,
                new ItemStack(Items.NETHERITE_AXE));
        for (int[] o : new int[][]{{-4, -8}, {4, -8}, {-6, -5}, {6, -5}}) {
            minion(w, gx + o[0] + 0.5, y, gz + o[1] + 0.5, o[0] < 0 ? EntityType.VINDICATOR::create : EntityType.PILLAGER::create,
                    "Accord Cutthroat");
        }
    }

    private static void openGate(ServerPlayer p) {
        ServerLevel w = p.serverLevel();
        int gx = Archipelago.LAST_HORIZON.cx(), gz = Archipelago.LAST_HORIZON.cz(), y = gateY();
        for (int x = gx - 4; x <= gx + 4; x++) {
            for (int yy = y; yy <= y + 12; yy++) w.setBlockAndUpdate(new BlockPos(x, yy, gz), Blocks.AIR.defaultBlockState());
        }
        for (int i = 0; i < 7; i++) {
            w.setBlockAndUpdate(new BlockPos(gx - 3 + i, y - 1, gz + 3), Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.HAS_EYE, true));
        }
        GateState.get(w.getServer()).gateOpened = true;
        GateState.get(w.getServer()).setDirty();
        w.playSound(null, new BlockPos(gx, y, gz), SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 2.0f, 0.7f);
        Mob sovereign = boss(w, gx + 0.5, y, gz - 4.5, EntityType.DROWNED::create, "The Tide Sovereign", 420, 2.4, SOVEREIGN_TAG,
                new ItemStack(Items.TRIDENT));
        if (sovereign != null) sovereign.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.TURTLE_HELMET)); // the sun won't save you
        for (int[] o : new int[][]{{-5, -2}, {5, -2}, {-3, -7}, {3, -7}}) {
            minion(w, gx + o[0] + 0.5, y, gz + o[1] + 0.5, EntityType.DROWNED::create, "Drowned Herald");
        }
        net.minecraft.world.phys.Vec3 at = new net.minecraft.world.phys.Vec3(gx + 0.5, y + 2, gz - 4.5);
        Cutscenes.playNear(w, at, 80, "The Tide Sovereign", java.util.List.of(
                new Cutscenes.Shot(at.add(0, 6, 16), at.add(0, 3, 9), at, 80, "The seven relics blaze in the gate, and the Last Horizon opens."),
                Cutscenes.orbit(at, 7, 1, 60, 120, 80, "Out of it steps the thing the old tides served: the Tide Sovereign, crowned in coral.")));
    }

    private static void onDeath(LivingEntity victim, DamageSource source) {
        var tags = victim.getTags();
        String name = victim.getCustomName() == null ? "" : victim.getCustomName().getString();
        if (tags.contains("cursedtides_boss") && name.contains("Cinder King")) {
            for (ServerPlayer p : nearby(victim, 64)) grant(p, Relic.EMBER);
        } else if (tags.contains("cursedtides_boss") && name.contains("Veil Witch")) {
            for (ServerPlayer p : nearby(victim, 64)) grant(p, Relic.VEIL);
        } else if (tags.contains(HAND_TAG)) {
            for (ServerPlayer p : nearby(victim, 64)) {
                if (flag(p, EXPOSED)) grant(p, Relic.LANTERN);
            }
        } else if (tags.contains(ROOK_TAG)) {
            for (ServerPlayer p : nearby(victim, 80)) {
                if (stage(p) == TURNCOAT) {
                    setStage(p, OPEN_GATE);
                    p.displayClientMessage(Component.literal("Rook falls, cursing the Accord that followed him. The relics are still yours. "
                            + "Speak to the Wayfarer to set them in the gate.").withStyle(ChatFormatting.YELLOW), false);
                }
            }
        } else if (tags.contains(SOVEREIGN_TAG)) {
            victim.spawnAtLocation(new ItemStack(ModItems.DOUBLOON, 64));
            victim.spawnAtLocation(new ItemStack(FlagItem.item(FlagItem.Design.HORIZON)));
            for (ServerPlayer p : nearby(victim, 80)) {
                if (stage(p) == SOVEREIGN_FIGHT) {
                    setStage(p, CHOOSE);
                    p.displayClientMessage(Component.literal("The Tide Sovereign breaks apart into seawater. Beyond the gate, the sea is still. "
                            + "The Wayfarer is waiting for your decision.").withStyle(ChatFormatting.YELLOW), false);
                }
            }
            if (victim.getServer() != null) {
                victim.getServer().getPlayerList().broadcastSystemMessage(Component.literal("The Tide Sovereign has fallen at the Last Horizon!")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
            }
        }
    }

    // ------------------------------------------------------------------ conversations

    public static void wayfarer(StoryNpcEntity npc, ServerPlayer p) {
        switch (stage(p)) {
            case NONE -> {
                StoryDialogue.say(npc, p,
                        "You sailed off the edge of every chart in Saltmarrow to get here. Good. Most turn back at Stormbreak.",
                        "That gate on the summit was old when the first ships came. The people who built it lived on the Keys, before the sea rose.",
                        "Seven sockets around its threshold. Seven relics, scattered across the Reach. And someone else is collecting them.",
                        "Here is the first. I dug it out of the observatory. Take it to the scholar camped on the Forgotten Keys; he can read the marks.");
                setStage(p, SEEK_SCHOLAR);
                grant(p, Relic.DAWN);
                p.displayClientMessage(Component.literal("New story: The Seven Tides").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD), false);
            }
            case SEEK_SCHOLAR -> StoryDialogue.say(npc, p, "The Forgotten Keys, south-east of here past Crooked Reef. Find the scholar's camp.");
            case GATHER -> StoryDialogue.say(npc, p, "You have " + count(p) + " of the seven. Keep going, and watch your back.");
            case TO_GATE -> {
                StoryDialogue.say(npc, p, "All seven! Then let's... wait. Sails on the horizon. Black ones.",
                        "Rook Varga. He told you he'd help at the gate, didn't he? Help himself, more like.");
                if (!bossNear(npc, ROOK_TAG)) ambush(p);
                setStage(p, TURNCOAT);
                p.displayClientMessage(Component.literal("Betrayal! Defeat Rook Varga and his Accord.").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), false);
            }
            case TURNCOAT -> {
                StoryDialogue.say(npc, p, "Rook first! He won't let you near that gate while he's breathing.");
                if (!bossNear(npc, ROOK_TAG)) ambush(p);
            }
            case OPEN_GATE -> {
                StoryDialogue.say(npc, p, "Now. Set them in the sockets, one by one. Dawn, Reef, Serpent, Ember, Veil, Sovereign, Lantern...",
                        "...the bars are lifting. Something is coming through. Stand fast!");
                setStage(p, SOVEREIGN_FIGHT);
                openGate(p);
            }
            case SOVEREIGN_FIGHT -> {
                StoryDialogue.say(npc, p, "It's the drowned king of the First Tide! Bring it down!");
                if (!bossNear(npc, SOVEREIGN_TAG)) openGate(p);
            }
            case CHOOSE -> choose(npc, p);
            default -> epilogue(npc, p);
        }
    }

    private static void choose(StoryNpcEntity npc, ServerPlayer p) {
        int pick = com.cursedtides.dialogue.Dialogue.choice(p);
        boolean courtAvailable = flag(p, COURT) && QuestData.has(p, ModItems.LAMPWRIGHT_SEAL);
        boolean court = courtAvailable && pick == 2;
        if (!flag(p, END_OFFERED)) {
            setFlag(p, END_OFFERED);
            List<String> lines = new ArrayList<>(List.of(
                    "The gate stands open, and beyond it the tide that drowned the First People waits to answer whoever holds the relics.",
                    "Seal it, and the curse that walks the Reach loses its hold. Claim it, and the sea will answer to you, and it will want things.",
                    "(Choose: seal the gate, or claim the tide."));
            if (flag(p, COURT)) lines.add("(Or press the Lampwright's Seal to the gate, and give it to the Lantern Court.)");
            lines.add("This choice is permanent.)");
            StoryDialogue.say(npc, p, lines.toArray(new String[0]));
            return;
        }
        int ending = court ? COURT_ENDING : pick == 1 ? CLAIMED : SEALED;
        p.setAttached(ENDING, ending);
        com.cursedtides.activity.Achievements.grant(p, "horizon");
        setStage(p, DONE);
        MinecraftServer server = p.getServer();
        switch (ending) {
            case SEALED -> {
                StoryDialogue.say(npc, p, "You let them go. The relics sink into the threshold and the gate grinds shut.",
                        "Listen. No bells under the water. The curse is quieter already. Warden of the Horizon, they'll call you.");
                FactionData.adjust(p, Faction.NAVY, 200);
                FactionData.adjust(p, Faction.BRETHREN, 100);
                QuestData.give(p, new ItemStack(ModItems.DOUBLOON, 200));
            }
            case CLAIMED -> {
                StoryDialogue.say(npc, p, "The tide pours through you. Your lungs will never fear the sea again, and the drowned will not raise a hand to you.",
                        "The Tidewatch will hunt you for this. The Brethren will crown you. Sovereign of the Seventh Tide.");
                FactionData.adjust(p, Faction.BRETHREN, 300);
                FactionData.adjust(p, Faction.NAVY, -400);
                QuestData.give(p, new ItemStack(ModItems.DOUBLOON, 150));
            }
            default -> {
                QuestData.take(p, ModItems.LAMPWRIGHT_SEAL);
                StoryDialogue.say(npc, p, "You press the Lampwright's seal into the lintel. Lanterns kindle all down the gate.",
                        "Somewhere in Port Meridian, a governor is smiling. The Court owns the gate now, and you are its Lantern-Bearer.");
                FactionData.adjust(p, Faction.NAVY, -150);
                FactionData.adjust(p, Faction.BRETHREN, -150);
                FactionData.adjust(p, Faction.SMUGGLERS, 150);
                QuestData.give(p, new ItemStack(ModItems.DOUBLOON, 500));
            }
        }
        p.level().playSound(null, p.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 0.8f);
        if (server != null) {
            server.getPlayerList().broadcastSystemMessage(Component.literal(p.getName().getString() + " " + switch (ending) {
                case SEALED -> "has sealed the Horizon Gate. The Reach breathes easier.";
                case CLAIMED -> "has claimed the Seventh Tide. The drowned bow to a new sovereign.";
                default -> "has given the Horizon Gate to the Lantern Court.";
            }).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        }
    }

    private static void epilogue(StoryNpcEntity npc, ServerPlayer p) {
        StoryDialogue.say(npc, p, switch (ending(p)) {
            case SEALED -> "The gate is quiet. Thank you, Warden.";
            case CLAIMED -> "The sea moves when you walk past it, Sovereign. Be careful what it asks of you.";
            case COURT_ENDING -> "The Court's lanterns burn on the gate every night now. I hope they were worth it.";
            default -> "The horizon is clear today.";
        });
    }

    public static String title(Player p) {
        String crown = CrownArc.title(p);
        if (crown != null) return crown;
        return horizonTitle(p);
    }

    /** The title from this act's ending alone. */
    public static String horizonTitle(Player p) {
        return switch (ending(p)) {
            case SEALED -> "Warden of the Horizon";
            case CLAIMED -> "Sovereign of the Seventh Tide";
            case COURT_ENDING -> "Lantern-Bearer of the Court";
            default -> null;
        };
    }

    /** The scholar on the Forgotten Keys reads the relics and sends you after the rest. */
    public static void scholar(StoryNpcEntity npc, ServerPlayer p) {
        if (LanternArc.scholar(npc, p)) return;
        int s = stage(p);
        if (s == NONE) {
            StoryDialogue.say(npc, p, "The First Tide built these ruins before the sea rose. Fascinating people. Terrible taste in gods.");
            return;
        }
        if (s == SEEK_SCHOLAR) {
            StoryDialogue.say(npc, p,
                    "Let me see that. Oh. Oh my. The glyph for dawn, and the mark of the Seventh Socket.",
                    "Seven relics opened the Horizon Gate. The tablets name where they went: a reef vault, a serpent temple, a fire king's crater,",
                    "a witch's shrine, a fleet's flagship, and a lantern held by men in the capital who call themselves the Lantern Court.",
                    "The flagship is the Sovereign Dawn, the Meridian Fleet's pride, lost at Crooked Reef. Take this log; the salvagers brought it up.",
                    "Read it at night over the reef. And captain... the Court has been buying every scrap of the First Tide for years. Be careful.");
            setStage(p, GATHER);
            QuestData.give(p, new ItemStack(ModItems.FLEET_LOG));
            return;
        }
        if (s == GATHER) {
            // Anyone who emptied those vaults before the hunt began brought the relics out without knowing it
            for (Dungeon d : Dungeons.ALL) {
                if (d.index == 30 && Dungeons.isCleared(p, d)) grant(p, Relic.REEF);
                if (d.index == 36 && Dungeons.isCleared(p, d)) grant(p, Relic.SERPENT);
            }
            List<String> lines = new ArrayList<>();
            lines.add("You have " + count(p) + " of seven. Still missing:");
            for (Relic r : Relic.values()) if (!hasRelic(p, r)) lines.add("  " + r.displayName + ": " + r.where);
            if (!hasRelic(p, Relic.SOVEREIGN) && !QuestData.has(p, ModItems.FLEET_LOG)) {
                QuestData.give(p, new ItemStack(ModItems.FLEET_LOG));
                lines.add("Lost the fleet log? Here's my copy.");
            }
            StoryDialogue.say(npc, p, lines.toArray(new String[0]));
            return;
        }
        StoryDialogue.say(npc, p, "The gate! Go, go. I'll write it all down.");
    }

    /** Governor Harrow is the Lantern Court's hand. Returns true when the arc took over the conversation. */
    static boolean governor(StoryNpcEntity npc, ServerPlayer p) {
        if (stage(p) != GATHER || hasRelic(p, Relic.LANTERN)) {
            if (flag(p, EXPOSED)) {
                StoryDialogue.say(npc, p, "You've cost me everything, captain. Get out before I call the guard.");
                return true;
            }
            return false;
        }
        if (count(p) < 4) return false;
        if (flag(p, EXPOSED)) {
            StoryDialogue.say(npc, p, "My Hand will see you dead before you take that lantern.");
            if (!bossNear(npc, HAND_TAG)) {
                boss(p.serverLevel(), npc.getX() + 2, npc.getY(), npc.getZ(), EntityType.EVOKER::create,
                        "The Lampwright's Hand", 180, 1.15, HAND_TAG, null);
            }
            return true;
        }
        if (!flag(p, OFFERED)) {
            setFlag(p, OFFERED);
            StoryDialogue.say(npc, p,
                    "Four relics. Five? You've done the Court's work for it, captain. Yes, I am the Lampwright's hand. Sit down.",
                    "The seventh relic is the Lantern, and it is mine. You may have it, if you promise the gate to the Court when it opens.",
                    "Or you may run to the Admiral with tales about a governor. My Hand would enjoy that.",
                    "(Make the bargain, or expose her. This choice is permanent.)");
            return true;
        }
        if (com.cursedtides.dialogue.Dialogue.choice(p) == 1) {
            setFlag(p, COURT);
            FactionData.adjust(p, Faction.NAVY, -50);
            FactionData.adjust(p, Faction.SMUGGLERS, 50);
            QuestData.give(p, new ItemStack(ModItems.LAMPWRIGHT_SEAL));
            StoryDialogue.say(npc, p, "A sensible captain. The Lantern, and the Lampwright's seal. When the gate opens, press the seal to it.");
            grant(p, Relic.LANTERN);
        } else {
            setFlag(p, EXPOSED);
            FactionData.adjust(p, Faction.NAVY, 50);
            StoryDialogue.say(npc, p, "Then you'll take it from my Hand's corpse. Kill them!");
            boss(p.serverLevel(), npc.getX() + 2, npc.getY(), npc.getZ(), EntityType.EVOKER::create,
                    "The Lampwright's Hand", 180, 1.15, HAND_TAG, null);
        }
        return true;
    }

    /** Rook Varga's Free Accord: a pirate coalition that wants the gate. */
    static boolean rook(StoryNpcEntity npc, ServerPlayer p) {
        if (stage(p) < GATHER || stage(p) >= DONE || flag(p, MET_ROOK)) return false;
        setFlag(p, MET_ROOK);
        StoryDialogue.say(npc, p,
                "Word travels, relic-hunter. Half the Reach knows what you're after and the other half's lying about it.",
                "I'm putting together an Accord: every free captain from here to Raven's Landing, sailing as one. No Navy, no Court.",
                "Wear this. When you go to the gate, the Accord will be there to watch your back. You have my word.",
                "(Rook gives you an Accord token.)");
        QuestData.give(p, new ItemStack(ModItems.ACCORD_TOKEN));
        return true;
    }

    /** The Admiral thanks those who exposed the Court. */
    static boolean admiral(StoryNpcEntity npc, ServerPlayer p) {
        if (!flag(p, EXPOSED) || !hasRelic(p, Relic.LANTERN) || flag(p, ADMIRAL_THANKED)) return false;
        setFlag(p, ADMIRAL_THANKED);
        StoryDialogue.say(npc, p, "Harrow, a Court agent. Under my nose. The Admiralty owes you, captain: a hundred doubloons, and our thanks.");
        QuestData.give(p, new ItemStack(ModItems.DOUBLOON, 100));
        FactionData.adjust(p, Faction.NAVY, 150);
        return true;
    }

    // ------------------------------------------------------------------ journal

    public static List<String> journal(Player p) {
        List<String> lines = new ArrayList<>();
        int s = stage(p);
        String objective = switch (s) {
            case NONE -> "Sail to the Last Horizon, at the far southern edge of the chart, and find the Wayfarer.";
            case SEEK_SCHOLAR -> "Take the Dawn Relic to the scholar camped on the Forgotten Keys.";
            case GATHER -> "Recover the seven relics (" + count(p) + "/7).";
            case TO_GATE -> "Bring all seven relics to the Wayfarer at the Last Horizon.";
            case TURNCOAT -> "Defeat Rook Varga, the Turncoat, at the Horizon Gate.";
            case OPEN_GATE -> "Speak to the Wayfarer to set the relics in the gate.";
            case SOVEREIGN_FIGHT -> "Defeat the Tide Sovereign.";
            case CHOOSE -> "Decide the fate of the Horizon Gate (speak to the Wayfarer).";
            default -> "Complete. " + title(p) + ".";
        };
        lines.add("§f" + objective);
        if (s == GATHER) {
            for (Relic r : Relic.values()) {
                lines.add((hasRelic(p, r) ? "§a✔ " : "§8• ") + r.displayName + " §7(" + r.where + ")");
            }
        }
        return lines;
    }

    // ------------------------------------------------------------------ the lasting effects of each ending

    private static void tick(MinecraftServer server) {
        if (server.getTickCount() % 40 != 0) return;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (ending(p) == CLAIMED && p.isInWater()) {
                p.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 220, 0, true, false));
            }
        }
    }

    /** Server-wide: has the gate been opened? */
    static final class GateState extends SavedData {
        boolean gateOpened;
        private static final Factory<GateState> TYPE = new Factory<>(GateState::new, GateState::fromNbt, null);

        static GateState get(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(TYPE, "cursedtides_horizon");
        }

        @Override
        public CompoundTag save(CompoundTag nbt, HolderLookup.Provider lookup) {
            nbt.putBoolean("GateOpened", gateOpened);
            return nbt;
        }

        private static GateState fromNbt(CompoundTag nbt, HolderLookup.Provider lookup) {
            GateState w = new GateState();
            w.gateOpened = nbt.getBoolean("GateOpened");
            return w;
        }
    }
}
