package com.cursedtides.quest;

import com.cursedtides.CursedTides;
import com.cursedtides.combat.Levels;
import com.cursedtides.entity.ShipEntity;
import com.cursedtides.entity.StoryNpcEntity;
import com.cursedtides.faction.Faction;
import com.cursedtides.faction.FactionData;
import com.cursedtides.item.ModItems;
import com.cursedtides.ship.ShipClass;
import com.cursedtides.ship.ShipEvents;
import com.cursedtides.ship.ShipFaction;
import com.cursedtides.world.Archipelago;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.List;

import static net.minecraft.commands.Commands.literal;

/**
 * Act five, "The Black Muster". With the Abyssal settled, Tidewatch patrols start vanishing, and are seen again
 * sailing with dead crews under a black pennant. Admiral Galloway wants proof; the pennants carry the seal of
 * Corvin Hale, the hanging admiral who sailed out after Ashmere Thorne thirty years ago and never came back. Every
 * ship the Reach has ever lost is mustering under him at the Graveyard of Ships. Only the Brethren know the way
 * there, so Isadora Vance, whose father Hale hanged, has to be asked. Sink his escorts, board his flagship (the
 * curse won't let her sink), cut Hale down, and decide who gets his commission: the Admiralty, or the Brethren.
 */
public final class MusterArc {
    private MusterArc() {}

    public static final int NONE = 0, SEE_ADMIRAL = 1, PENNANTS = 2, BACK_TO_ADMIRAL = 3, SEE_QUEEN = 4, GRAVEYARD = 5, CHOOSE = 6, DONE = 7;
    public static final int NAVY_ENDING = 1, BRETHREN_ENDING = 2;

    private static AttachmentType<Integer> attach(String name) {
        return AttachmentRegistry.<Integer>builder().persistent(Codec.INT).copyOnDeath().initializer(() -> 0)
                .buildAndRegister(CursedTides.id(name));
    }

    public static final AttachmentType<Integer> STAGE = attach("muster_stage");
    public static final AttachmentType<Integer> PENNANT_COUNT = attach("muster_pennants");
    public static final AttachmentType<Integer> ESCORTS_SUNK = attach("muster_escorts");
    public static final AttachmentType<Integer> ENDING = attach("muster_ending");

    public static final String MUSTER = "ct_muster", ESCORT = "ct_muster_escort", FLAGSHIP = "ct_muster_flag", HALE = "ct_hale";
    public static final String HALE_NAME = "Dread Admiral Corvin Hale";

    public static int stage(ServerPlayer p) { return p.getAttachedOrElse(STAGE, 0); }
    public static int ending(ServerPlayer p) { return p.getAttachedOrElse(ENDING, 0); }

    /** The Graveyard of Ships: open water far to the south-west, between Vesh Amaru and the Last Horizon. */
    public static int graveX() { return Archipelago.seaX(-240); }
    public static int graveZ() { return Archipelago.seaZ(1010); }

    public static boolean isFlagship(ShipEntity s) { return s.getTags().contains(FLAGSHIP); }

    // ------------------------------------------------------------------ talking

    private static void say(String who, ServerPlayer p, String... lines) {
        for (String l : lines) {
            p.displayClientMessage(Component.literal("[" + who + "] ").withStyle(ChatFormatting.GOLD).append(Component.literal(l).withStyle(ChatFormatting.WHITE)), false);
        }
    }

    private static void title(ServerPlayer p, String t, String sub) {
        p.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));
        p.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(sub).withStyle(ChatFormatting.GRAY)));
        p.connection.send(new ClientboundSetTitleTextPacket(Component.literal(t).withStyle(ChatFormatting.DARK_AQUA)));
    }

    private static final String GALLOWAY = "Admiral Hester Galloway", VANCE = "Isadora Vance";

    /** Admiral Galloway at Crownwatch. True if she had something to say about the Muster. */
    public static boolean admiral(StoryNpcEntity npc, ServerPlayer p) {
        switch (stage(p)) {
            case SEE_ADMIRAL -> {
                say(GALLOWAY, p, "Captain. Shut the door. No, leave the guard outside, he talks.",
                        "Three patrols in a month. The Resolute, the Kestrel, the Lady Anne. No wreckage, no survivors, no storms on the log.",
                        "Then a fisherman swears he saw the Kestrel off Mariner's Rest, under full sail, with skeletons on the yards and a black pennant at the masthead.",
                        "I don't believe in ghost stories. I believe in evidence. Bring me three of those pennants.",
                        "Our gunners can't get close enough, but you've a pirate's habits. Here: a grappling hook from the prize stores.",
                        "Batter one of those ships to half, throw this, and go over the side. Take the pennant off her mast yourself.");
                ItemStack hook = new ItemStack(ModItems.GRAPPLING_HOOK);
                QuestData.give(p, hook);
                p.setAttached(STAGE, PENNANTS);
                title(p, "The Black Muster", "Bring Galloway three black pennants");
                return true;
            }
            case PENNANTS -> {
                say(GALLOWAY, p, "Three pennants, captain. You have " + p.getAttachedOrElse(PENNANT_COUNT, 0)
                        + ". The dead ships are out on open water, mostly at night. Board them or sink them.");
                return true;
            }
            case BACK_TO_ADMIRAL -> {
                say(GALLOWAY, p, "...Every one stitched with the same seal. A noose around an anchor.",
                        "That was Corvin Hale's mark. Admiral of the Tidewatch before my time. He hanged forty pirates in a single morning at Bloodwake.",
                        "Thirty years ago he sailed after Ashmere Thorne with nine ships, and none of them came home. The Admiralty called it a storm.",
                        "If Hale's mustering the dead, he's doing it somewhere we've never charted. And I know exactly who has charts we don't.",
                        "Isadora Vance, on Raven's Landing. She'll spit when she hears the name. Hale hanged her father. Go anyway.");
                QuestData.give(p, new ItemStack(ModItems.DOUBLOON, 40));
                p.setAttached(STAGE, SEE_QUEEN);
                return true;
            }
            case SEE_QUEEN -> { say(GALLOWAY, p, "Raven's Landing, captain. Isadora Vance. Mind your purse."); return true; }
            case GRAVEYARD -> { say(GALLOWAY, p, "The Graveyard of Ships. God help you. Sink his escorts and he'll have to come out himself."); return true; }
            case CHOOSE -> {
                say(GALLOWAY, p, "Hale's commission. Signed by a king two kings ago.",
                        "Give it to me and the Admiralty buries this properly: a pardon for whatever you've done, and a commission of your own.",
                        "Or take it to Vance, I suppose. I can't stop you. I'd rather you didn't.");
                choices(p);
                return true;
            }
            case DONE -> {
                say(GALLOWAY, p, ending(p) == NAVY_ENDING ? "Commodore. The Reach is quieter for what you did. Don't let it go to your head."
                        : "The Brethren's admiral, walking into my office. Times change. Wipe your feet.");
                return true;
            }
            default -> { return false; }
        }
    }

    /** Isadora Vance at Raven's Landing. */
    public static boolean queen(StoryNpcEntity npc, ServerPlayer p) {
        switch (stage(p)) {
            case SEE_QUEEN -> {
                say(VANCE, p, "Corvin Hale. Say it again and I'll have your tongue for a bookmark.",
                        "He hanged my father on a dry morning and had his band play under the gallows. I was eight.",
                        "...Fine. Every ship the Reach has lost drifts the same way when the curse gets into her: south-west, past Vesh Amaru.",
                        "There's a stretch of water out there the Brethren call the Graveyard of Ships. Hulls stacked on the shoals like cordwood.",
                        "If Hale's mustering anywhere, he's mustering there. I've marked it on your chart.",
                        "He'll keep his flagship back behind his escorts. Sink enough of them and his pride will bring him out. It always did.",
                        "And when you've got him on his knees, tell him whose daughter sent you.");
                p.setAttached(STAGE, GRAVEYARD);
                title(p, "The Graveyard of Ships", "Sail south-west, past Vesh Amaru");
                return true;
            }
            case GRAVEYARD -> { say(VANCE, p, "The Graveyard's south-west of Vesh Amaru. Go. You're making my lads nervous."); return true; }
            case CHOOSE -> {
                say(VANCE, p, "Is he dead? Properly dead, this time?",
                        "Give me that commission and I'll burn it on the Brethren's table, and the whole Reach will know a pirate ended the hanging admiral.",
                        "And his flagship's escort... well. A good captain could do with a ship nobody else dares sail.",
                        "Or give it to Galloway and go and be a Navy pet. Your choice. Choose right.");
                choices(p);
                return true;
            }
            case DONE -> {
                say(VANCE, p, ending(p) == BRETHREN_ENDING ? "My father would've liked you. He liked anyone who frightened the Navy."
                        : "Commodore. Should I curtsey? I won't.");
                return true;
            }
            default -> { return false; }
        }
    }

    private static void choices(ServerPlayer p) {
        p.displayClientMessage(Component.literal("  [Give the commission to the Admiralty]").withStyle(s -> s.withColor(ChatFormatting.BLUE)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/blackmuster navy")))
                .append(Component.literal("   [Give it to the Brethren]").withStyle(s -> s.withColor(ChatFormatting.DARK_RED)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/blackmuster brethren")))), false);
    }

    public static String objective(ServerPlayer p) {
        return switch (stage(p)) {
            case SEE_ADMIRAL -> "A dead ship flying a black pennant crossed your bow. Report it to Admiral Galloway at Crownwatch.";
            case PENNANTS -> "Take black pennants from the dead ships at sea (board or sink them): " + p.getAttachedOrElse(PENNANT_COUNT, 0) + "/3.";
            case BACK_TO_ADMIRAL -> "Bring the three black pennants to Admiral Galloway at Crownwatch.";
            case SEE_QUEEN -> "Ask Isadora Vance on Raven's Landing where the dead muster.";
            case GRAVEYARD -> p.getAttachedOrElse(ESCORTS_SUNK, 0) < 3
                    ? "Sail to the Graveyard of Ships and sink Hale's escorts: " + p.getAttachedOrElse(ESCORTS_SUNK, 0) + "/3."
                    : "Board the Last Muster and kill Dread Admiral Corvin Hale.";
            case CHOOSE -> "Decide who gets Hale's commission: Admiral Galloway (Crownwatch) or Isadora Vance (Raven's Landing).";
            case DONE -> ending(p) == NAVY_ENDING ? "Hale is dead and his commission is with the Admiralty. Commodore of the Reach."
                    : "Hale is dead and his commission burned on the Brethren's table.";
            default -> "Not yet begun. (Comes after the Drowned Lights.)";
        };
    }

    public static List<String> journal(ServerPlayer p) {
        return List.of("§f" + objective(p));
    }

    public static int[] target(ServerPlayer p) {
        return switch (stage(p)) {
            case SEE_ADMIRAL, BACK_TO_ADMIRAL -> spot(StoryNpcEntity.Role.ADMIRAL);
            case SEE_QUEEN -> spot(StoryNpcEntity.Role.SMUGGLER_QUEEN);
            case GRAVEYARD -> new int[]{graveX(), graveZ()};
            case CHOOSE -> spot(StoryNpcEntity.Role.ADMIRAL);
            default -> null;
        };
    }

    public static String targetLabel(ServerPlayer p) {
        return switch (stage(p)) {
            case SEE_ADMIRAL, BACK_TO_ADMIRAL, CHOOSE -> "Admiral Galloway";
            case SEE_QUEEN -> "Isadora Vance";
            case GRAVEYARD -> "The Graveyard of Ships";
            default -> "";
        };
    }

    private static int[] spot(StoryNpcEntity.Role role) {
        for (com.cursedtides.world.Builds.NpcSpot s : com.cursedtides.world.Builds.NPC_SPOTS) if (s.role() == role) return new int[]{(int) s.x(), (int) s.z()};
        return null;
    }

    // ------------------------------------------------------------------ the dead fleet

    public static void register() {
        STAGE.identifier();
        PENNANT_COUNT.identifier();
        ESCORTS_SUNK.identifier();
        ENDING.identifier();
        ServerTickEvents.END_SERVER_TICK.register(MusterArc::tick);
        ServerLivingEntityEvents.AFTER_DEATH.register((victim, source) -> {
            if (!victim.getTags().contains(HALE) || !(victim.level() instanceof ServerLevel w)) return;
            for (ServerPlayer p : w.getPlayers(q -> q.distanceToSqr(victim) < 96 * 96 && stage(q) == GRAVEYARD)) {
                p.setAttached(STAGE, CHOOSE);
                ItemStack commission = new ItemStack(ModItems.ADMIRALS_COMMISSION);
                QuestData.give(p, commission);
                title(p, "Corvin Hale falls", "The Black Muster breaks");
                p.displayClientMessage(Component.literal("Hale crumbles into salt and old lace. In his coat, sealed in wax, is his commission. Galloway wants it. So will Isadora Vance.")
                        .withStyle(ChatFormatting.AQUA), false);
            }
            w.playSound(null, victim.blockPosition(), SoundEvents.WITHER_DEATH, SoundSource.HOSTILE, 2f, 0.7f);
            w.setWeatherParameters(6000, 0, false, false);
        });
        CommandRegistrationCallback.EVENT.register((d, a, e) -> d.register(literal("blackmuster")
                .then(literal("navy").executes(ctx -> { choose(ctx.getSource().getPlayerOrException(), NAVY_ENDING); return 1; }))
                .then(literal("brethren").executes(ctx -> { choose(ctx.getSource().getPlayerOrException(), BRETHREN_ENDING); return 1; }))));
    }

    private static void tick(MinecraftServer server) {
        if (server.getTickCount() % 20 != 9) return;
        ServerLevel w = server.overworld();
        for (ServerPlayer p : w.players()) {
            int st = stage(p);
            boolean sailing = p.getVehicle() instanceof ShipEntity;
            if (st == NONE) {
                if (LanternArc.stage(p) == LanternArc.DONE && sailing && server.getTickCount() % 400 == 9 && w.random.nextFloat() < 0.5f) {
                    ShipEntity ghost = spawnMuster(w, p, 90, 120, false);
                    if (ghost != null) {
                        p.setAttached(STAGE, SEE_ADMIRAL);
                        p.displayClientMessage(Component.literal("Out of a bank of grey fog sails a Tidewatch frigate, gunports shut, with a black pennant at her masthead and dead men on her yards. Nobody on deck turns to look at you.")
                                .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC), false);
                        title(p, "The Black Muster", "A new tale begins");
                    }
                }
                continue;
            }
            if (st == PENNANTS && sailing && server.getTickCount() % 1800 == 9
                    && Archipelago.column(p.getBlockX(), p.getBlockZ()).island() == null
                    && w.getEntitiesOfClass(ShipEntity.class, p.getBoundingBox().inflate(160), s -> s.getTags().contains(MUSTER)).isEmpty()
                    && w.random.nextFloat() < (w.isNight() ? 0.7f : 0.4f)) {
                spawnMuster(w, p, 70, 110, false);
            }
            if (st == GRAVEYARD && p.distanceToSqr(graveX(), p.getY(), graveZ()) < 110 * 110) {
                AABB area = new AABB(graveX() - 260, 0, graveZ() - 260, graveX() + 260, 320, graveZ() + 260);
                boolean escorts = !w.getEntitiesOfClass(ShipEntity.class, area, s -> s.getTags().contains(ESCORT)).isEmpty();
                boolean flag = !w.getEntitiesOfClass(ShipEntity.class, area, s -> s.getTags().contains(FLAGSHIP)).isEmpty();
                if (escorts || flag) continue;
                int sunk = p.getAttachedOrElse(ESCORTS_SUNK, 0);
                if (sunk < 3) {
                    for (int i = sunk; i < 3; i++) spawnMuster(w, p, 40, 70, true);
                    title(p, "The Graveyard of Ships", "Hale's escorts close in");
                    w.playSound(null, p.blockPosition(), SoundEvents.BELL_RESONATE, SoundSource.HOSTILE, 3f, 0.5f);
                    w.setWeatherParameters(0, 6000, true, false);
                } else {
                    spawnFlagship(w, p);
                }
            }
        }
    }

    private static ShipEntity spawnMuster(ServerLevel w, ServerPlayer p, double min, double max, boolean escort) {
        BlockPos at = ShipEvents.findSeaSpot(w, p, min, max);
        if (at == null) return null;
        ShipEntity s = ShipEvents.spawnShip(w, ShipFaction.DROWNED, at.getX() + 0.5, at.getY(), at.getZ() + 0.5, p);
        if (s == null) return null;
        s.addTag(MUSTER);
        if (escort) {
            s.addTag(ESCORT);
            s.setShipClass(w.random.nextBoolean() ? ShipClass.FRIGATE : ShipClass.BRIG);
            s.setHull(s.maxHull());
            s.refreshDimensions();
        }
        s.setFlag(com.cursedtides.ship.FlagItem.Design.CURSED_TIDE.ordinal());
        return s;
    }

    private static void spawnFlagship(ServerLevel w, ServerPlayer p) {
        BlockPos at = ShipEvents.findSeaSpot(w, p, 45, 70);
        if (at == null) return;
        ShipEntity s = ShipEvents.spawnShip(w, ShipFaction.DROWNED, at.getX() + 0.5, at.getY(), at.getZ() + 0.5, p);
        if (s == null) return;
        s.addTag(MUSTER);
        s.addTag(FLAGSHIP);
        s.setShipClass(ShipClass.SHIP_OF_THE_LINE);
        s.setHull(s.maxHull());
        s.refreshDimensions();
        s.setCustomName(Component.literal("The Last Muster").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD)
                .append(Component.literal(" [Lv 50]").withStyle(ChatFormatting.DARK_RED)));
        title(p, "The Last Muster", "Hale's flagship comes about");
        p.displayClientMessage(Component.literal("A great black ship of the line slides out from behind the wrecks. Her timbers glow at the seams. On her quarterdeck, a tall figure in an admiral's coat raises a hand, and every gun on her side runs out.")
                .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC), false);
        w.playSound(null, p.blockPosition(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 3f, 0.6f);
        Vec3 ship = s.position().add(0, 3, 0);
        Vec3 side = ship.subtract(p.position()).multiply(1, 0, 1).normalize();
        Cutscenes.play(p, "The Last Muster", List.of(
                new Cutscenes.Shot(p.getEyePosition().add(0, 8, 0), ship.subtract(side.scale(25)).add(0, 6, 0), ship, 90,
                        "Out from behind the wrecks of the Graveyard comes a black ship of the line, her seams glowing like coals."),
                Cutscenes.orbit(ship, 14, 4, 0, 70, 80, "On her quarterdeck, Dread Admiral Corvin Hale. Every gun on her side runs out at once.")));
    }

    /** Hale himself, at the head of his flagship's boarders. */
    public static void spawnHale(ServerLevel w, Vec3 around, int level, String tag) {
        Mob hale = EntityType.WITHER_SKELETON.create(w);
        if (hale == null) return;
        hale.moveTo(around.x, Archipelago.WATER_TOP + 1, around.z, 0, 0);
        hale.finalizeSpawn(w, w.getCurrentDifficultyAt(hale.blockPosition()), MobSpawnType.EVENT, null);
        hale.setCustomName(Component.literal(HALE_NAME).withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD));
        hale.setCustomNameVisible(true);
        hale.setPersistenceRequired();
        Levels.apply(hale, 50);
        AttributeInstance hp = hale.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null) { hp.setBaseValue(hp.getBaseValue() * 8 * Levels.healthFactor(50)); hale.setHealth(hale.getMaxHealth()); }
        AttributeInstance size = hale.getAttribute(Attributes.SCALE);
        if (size != null) size.setBaseValue(1.25);
        hale.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.CUTLASS));
        hale.setDropChance(EquipmentSlot.MAINHAND, 0f);
        hale.addTag(HALE);
        hale.addTag("cursedtides_boss");
        hale.addTag(tag);
        w.addFreshEntity(hale);
        for (ServerPlayer p : w.getPlayers(q -> q.distanceToSqr(around) < 48 * 48)) {
            p.displayClientMessage(Component.literal("[" + HALE_NAME + "] ").withStyle(ChatFormatting.DARK_AQUA)
                    .append(Component.literal("Another pirate on my deck. I hanged better than you before breakfast. Come and be hanged.").withStyle(ChatFormatting.WHITE)), false);
            if (stage(p) == GRAVEYARD) p.displayClientMessage(Component.literal("(Isadora Vance asked you to tell him whose daughter sent you. You do. He hesitates, just for a moment.)")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
        }
    }

    // ------------------------------------------------------------------ hooks from ships

    /** A dead ship sunk by a player. */
    public static void onSunk(ServerPlayer p, ShipEntity s) {
        if (s.getFaction() != ShipFaction.DROWNED) return;
        credit(p, s);
    }

    /** A ship boarded and beaten by a player. */
    public static void onBoarded(ServerPlayer p, ShipEntity s) {
        if (s.getFaction() != ShipFaction.DROWNED) return;
        if (stage(p) == PENNANTS && !s.getTags().contains("ct_pennant_taken")) {
            s.addTag("ct_pennant_taken");
            pennant(p);
        }
        if (s.getTags().contains(ESCORT) && !s.getTags().contains("ct_escort_counted")) {
            s.addTag("ct_escort_counted");
            escort(p);
        }
    }

    private static void credit(ServerPlayer p, ShipEntity s) {
        if (stage(p) == PENNANTS && !s.getTags().contains("ct_pennant_taken")) pennant(p);
        if (s.getTags().contains(ESCORT) && !s.getTags().contains("ct_escort_counted")) escort(p);
    }

    private static void pennant(ServerPlayer p) {
        int n = p.getAttachedOrElse(PENNANT_COUNT, 0) + 1;
        p.setAttached(PENNANT_COUNT, n);
        QuestData.give(p, new ItemStack(ModItems.BLACK_PENNANT));
        p.displayClientMessage(Component.literal("You cut the black pennant from her masthead (" + n + "/3). A noose around an anchor, stitched in grey thread.")
                .withStyle(ChatFormatting.DARK_AQUA), false);
        if (n >= 3) {
            p.setAttached(STAGE, BACK_TO_ADMIRAL);
            title(p, "Three black pennants", "Take them to Admiral Galloway");
        }
    }

    private static void escort(ServerPlayer p) {
        if (stage(p) != GRAVEYARD) return;
        int n = Math.min(3, p.getAttachedOrElse(ESCORTS_SUNK, 0) + 1);
        p.setAttached(ESCORTS_SUNK, n);
        p.displayClientMessage(Component.literal("Hale's escort is broken (" + n + "/3).").withStyle(ChatFormatting.DARK_AQUA), false);
    }

    private static void choose(ServerPlayer p, int which) {
        if (stage(p) != CHOOSE) return;
        if (p.getInventory().countItem(ModItems.ADMIRALS_COMMISSION) > 0) {
            int slot = p.getInventory().findSlotMatchingItem(new ItemStack(ModItems.ADMIRALS_COMMISSION));
            if (slot >= 0) p.getInventory().removeItem(slot, 1);
        }
        p.setAttached(ENDING, which);
        p.setAttached(STAGE, DONE);
        if (which == NAVY_ENDING) {
            say(GALLOWAY, p, "It's done, then. I'll see him buried in the Admiralty records where he belongs.",
                    "Kneel. Oh, don't look like that, it's a formality. By my hand and the Crown's seal: Commodore of the Reach.");
            FactionData.adjust(p, Faction.NAVY, 250);
            com.cursedtides.trade.Wallet.give(p, 400 * com.cursedtides.trade.Wallet.SILVER_PER_DOUBLOON);
            QuestData.give(p, com.cursedtides.combat.GearRarity.randomWeapon(p.serverLevel(), p.getRandom(), 5));
            title(p, "Commodore of the Reach", "The Black Muster: complete");
        } else {
            say(VANCE, p, "Watch.", "...There. Thirty years I've waited to smell that.",
                    "Now. One of Hale's ships came out of the Graveyard on her own and anchored off my landing this morning. No crew. She won't take orders from me.",
                    "She'll take them from you. Don't ask me why. Here's her papers.");
            FactionData.adjust(p, Faction.BRETHREN, 200);
            FactionData.adjust(p, Faction.SMUGGLERS, 200);
            ItemStack deed = new ItemStack(ShipClass.GALLEON.deed());
            CompoundTag fit = new CompoundTag();
            fit.putInt("Look", ShipEntity.LOOK_GHOST);
            fit.putInt("Flag", com.cursedtides.ship.FlagItem.Design.CURSED_TIDE.ordinal());
            deed.set(DataComponents.CUSTOM_DATA, CustomData.of(fit));
            deed.set(DataComponents.CUSTOM_NAME, Component.literal("Hale's Regret").withStyle(ChatFormatting.DARK_AQUA));
            QuestData.give(p, deed);
            title(p, "Scourge of the Muster", "The Black Muster: complete");
        }
        QuestData.give(p, new ItemStack(Items.TOTEM_OF_UNDYING));
        com.cursedtides.activity.Achievements.grant(p, "black_muster");
    }
}
