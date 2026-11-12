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
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

import static net.minecraft.server.command.CommandManager.literal;

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

    public static int stage(ServerPlayerEntity p) { return p.getAttachedOrElse(STAGE, 0); }
    public static int ending(ServerPlayerEntity p) { return p.getAttachedOrElse(ENDING, 0); }

    /** The Graveyard of Ships: open water far to the south-west, between Vesh Amaru and the Last Horizon. */
    public static int graveX() { return Archipelago.seaX(-240); }
    public static int graveZ() { return Archipelago.seaZ(1010); }

    public static boolean isFlagship(ShipEntity s) { return s.getCommandTags().contains(FLAGSHIP); }

    // ------------------------------------------------------------------ talking

    private static void say(String who, ServerPlayerEntity p, String... lines) {
        for (String l : lines) {
            p.sendMessage(Text.literal("[" + who + "] ").formatted(Formatting.GOLD).append(Text.literal(l).formatted(Formatting.WHITE)), false);
        }
    }

    private static void title(ServerPlayerEntity p, String t, String sub) {
        p.networkHandler.sendPacket(new TitleFadeS2CPacket(10, 70, 20));
        p.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal(sub).formatted(Formatting.GRAY)));
        p.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(t).formatted(Formatting.DARK_AQUA)));
    }

    private static final String GALLOWAY = "Admiral Hester Galloway", VANCE = "Isadora Vance";

    /** Admiral Galloway at Crownwatch. True if she had something to say about the Muster. */
    public static boolean admiral(StoryNpcEntity npc, ServerPlayerEntity p) {
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
    public static boolean queen(StoryNpcEntity npc, ServerPlayerEntity p) {
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

    private static void choices(ServerPlayerEntity p) {
        p.sendMessage(Text.literal("  [Give the commission to the Admiralty]").styled(s -> s.withColor(Formatting.BLUE)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/blackmuster navy")))
                .append(Text.literal("   [Give it to the Brethren]").styled(s -> s.withColor(Formatting.DARK_RED)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/blackmuster brethren")))), false);
    }

    public static String objective(ServerPlayerEntity p) {
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

    public static List<String> journal(ServerPlayerEntity p) {
        return List.of("§f" + objective(p));
    }

    public static int[] target(ServerPlayerEntity p) {
        return switch (stage(p)) {
            case SEE_ADMIRAL, BACK_TO_ADMIRAL -> spot(StoryNpcEntity.Role.ADMIRAL);
            case SEE_QUEEN -> spot(StoryNpcEntity.Role.SMUGGLER_QUEEN);
            case GRAVEYARD -> new int[]{graveX(), graveZ()};
            case CHOOSE -> spot(StoryNpcEntity.Role.ADMIRAL);
            default -> null;
        };
    }

    public static String targetLabel(ServerPlayerEntity p) {
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
            if (!victim.getCommandTags().contains(HALE) || !(victim.getWorld() instanceof ServerWorld w)) return;
            for (ServerPlayerEntity p : w.getPlayers(q -> q.squaredDistanceTo(victim) < 96 * 96 && stage(q) == GRAVEYARD)) {
                p.setAttached(STAGE, CHOOSE);
                ItemStack commission = new ItemStack(ModItems.ADMIRALS_COMMISSION);
                QuestData.give(p, commission);
                title(p, "Corvin Hale falls", "The Black Muster breaks");
                p.sendMessage(Text.literal("Hale crumbles into salt and old lace. In his coat, sealed in wax, is his commission. Galloway wants it. So will Isadora Vance.")
                        .formatted(Formatting.AQUA), false);
            }
            w.playSound(null, victim.getBlockPos(), SoundEvents.ENTITY_WITHER_DEATH, SoundCategory.HOSTILE, 2f, 0.7f);
            w.setWeather(6000, 0, false, false);
        });
        CommandRegistrationCallback.EVENT.register((d, a, e) -> d.register(literal("blackmuster")
                .then(literal("navy").executes(ctx -> { choose(ctx.getSource().getPlayerOrThrow(), NAVY_ENDING); return 1; }))
                .then(literal("brethren").executes(ctx -> { choose(ctx.getSource().getPlayerOrThrow(), BRETHREN_ENDING); return 1; }))));
    }

    private static void tick(MinecraftServer server) {
        if (server.getTicks() % 20 != 9) return;
        ServerWorld w = server.getOverworld();
        for (ServerPlayerEntity p : w.getPlayers()) {
            int st = stage(p);
            boolean sailing = p.getVehicle() instanceof ShipEntity;
            if (st == NONE) {
                if (LanternArc.stage(p) == LanternArc.DONE && sailing && server.getTicks() % 400 == 9 && w.random.nextFloat() < 0.5f) {
                    ShipEntity ghost = spawnMuster(w, p, 90, 120, false);
                    if (ghost != null) {
                        p.setAttached(STAGE, SEE_ADMIRAL);
                        p.sendMessage(Text.literal("Out of a bank of grey fog sails a Tidewatch frigate, gunports shut, with a black pennant at her masthead and dead men on her yards. Nobody on deck turns to look at you.")
                                .formatted(Formatting.DARK_AQUA, Formatting.ITALIC), false);
                        title(p, "The Black Muster", "A new tale begins");
                    }
                }
                continue;
            }
            if (st == PENNANTS && sailing && server.getTicks() % 1800 == 9
                    && Archipelago.column(p.getBlockX(), p.getBlockZ()).island() == null
                    && w.getEntitiesByClass(ShipEntity.class, p.getBoundingBox().expand(160), s -> s.getCommandTags().contains(MUSTER)).isEmpty()
                    && w.random.nextFloat() < (w.isNight() ? 0.7f : 0.4f)) {
                spawnMuster(w, p, 70, 110, false);
            }
            if (st == GRAVEYARD && p.squaredDistanceTo(graveX(), p.getY(), graveZ()) < 110 * 110) {
                Box area = new Box(graveX() - 260, 0, graveZ() - 260, graveX() + 260, 320, graveZ() + 260);
                boolean escorts = !w.getEntitiesByClass(ShipEntity.class, area, s -> s.getCommandTags().contains(ESCORT)).isEmpty();
                boolean flag = !w.getEntitiesByClass(ShipEntity.class, area, s -> s.getCommandTags().contains(FLAGSHIP)).isEmpty();
                if (escorts || flag) continue;
                int sunk = p.getAttachedOrElse(ESCORTS_SUNK, 0);
                if (sunk < 3) {
                    for (int i = sunk; i < 3; i++) spawnMuster(w, p, 40, 70, true);
                    title(p, "The Graveyard of Ships", "Hale's escorts close in");
                    w.playSound(null, p.getBlockPos(), SoundEvents.BLOCK_BELL_RESONATE, SoundCategory.HOSTILE, 3f, 0.5f);
                    w.setWeather(0, 6000, true, false);
                } else {
                    spawnFlagship(w, p);
                }
            }
        }
    }

    private static ShipEntity spawnMuster(ServerWorld w, ServerPlayerEntity p, double min, double max, boolean escort) {
        BlockPos at = ShipEvents.findSeaSpot(w, p, min, max);
        if (at == null) return null;
        ShipEntity s = ShipEvents.spawnShip(w, ShipFaction.DROWNED, at.getX() + 0.5, at.getY(), at.getZ() + 0.5, p);
        if (s == null) return null;
        s.addCommandTag(MUSTER);
        if (escort) {
            s.addCommandTag(ESCORT);
            s.setShipClass(w.random.nextBoolean() ? ShipClass.FRIGATE : ShipClass.BRIG);
            s.setHull(s.maxHull());
            s.calculateDimensions();
        }
        s.setFlag(com.cursedtides.ship.FlagItem.Design.CURSED_TIDE.ordinal());
        return s;
    }

    private static void spawnFlagship(ServerWorld w, ServerPlayerEntity p) {
        BlockPos at = ShipEvents.findSeaSpot(w, p, 45, 70);
        if (at == null) return;
        ShipEntity s = ShipEvents.spawnShip(w, ShipFaction.DROWNED, at.getX() + 0.5, at.getY(), at.getZ() + 0.5, p);
        if (s == null) return;
        s.addCommandTag(MUSTER);
        s.addCommandTag(FLAGSHIP);
        s.setShipClass(ShipClass.SHIP_OF_THE_LINE);
        s.setHull(s.maxHull());
        s.calculateDimensions();
        s.setCustomName(Text.literal("The Last Muster").formatted(Formatting.DARK_AQUA, Formatting.BOLD)
                .append(Text.literal(" [Lv 50]").formatted(Formatting.DARK_RED)));
        title(p, "The Last Muster", "Hale's flagship comes about");
        p.sendMessage(Text.literal("A great black ship of the line slides out from behind the wrecks. Her timbers glow at the seams. On her quarterdeck, a tall figure in an admiral's coat raises a hand, and every gun on her side runs out.")
                .formatted(Formatting.DARK_AQUA, Formatting.ITALIC), false);
        w.playSound(null, p.getBlockPos(), SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.HOSTILE, 3f, 0.6f);
    }

    /** Hale himself, at the head of his flagship's boarders. */
    public static void spawnHale(ServerWorld w, Vec3d around, int level, String tag) {
        MobEntity hale = EntityType.WITHER_SKELETON.create(w);
        if (hale == null) return;
        hale.refreshPositionAndAngles(around.x, Archipelago.WATER_TOP + 1, around.z, 0, 0);
        hale.initialize(w, w.getLocalDifficulty(hale.getBlockPos()), SpawnReason.EVENT, null);
        hale.setCustomName(Text.literal(HALE_NAME).formatted(Formatting.DARK_AQUA, Formatting.BOLD));
        hale.setCustomNameVisible(true);
        hale.setPersistent();
        Levels.apply(hale, 50);
        EntityAttributeInstance hp = hale.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (hp != null) { hp.setBaseValue(hp.getBaseValue() * 8 * Levels.healthFactor(50)); hale.setHealth(hale.getMaxHealth()); }
        EntityAttributeInstance size = hale.getAttributeInstance(EntityAttributes.GENERIC_SCALE);
        if (size != null) size.setBaseValue(1.25);
        hale.equipStack(EquipmentSlot.MAINHAND, new ItemStack(ModItems.CUTLASS));
        hale.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0f);
        hale.addCommandTag(HALE);
        hale.addCommandTag("cursedtides_boss");
        hale.addCommandTag(tag);
        w.spawnEntity(hale);
        for (ServerPlayerEntity p : w.getPlayers(q -> q.squaredDistanceTo(around) < 48 * 48)) {
            p.sendMessage(Text.literal("[" + HALE_NAME + "] ").formatted(Formatting.DARK_AQUA)
                    .append(Text.literal("Another pirate on my deck. I hanged better than you before breakfast. Come and be hanged.").formatted(Formatting.WHITE)), false);
            if (stage(p) == GRAVEYARD) p.sendMessage(Text.literal("(Isadora Vance asked you to tell him whose daughter sent you. You do. He hesitates, just for a moment.)")
                    .formatted(Formatting.GRAY, Formatting.ITALIC), false);
        }
    }

    // ------------------------------------------------------------------ hooks from ships

    /** A dead ship sunk by a player. */
    public static void onSunk(ServerPlayerEntity p, ShipEntity s) {
        if (s.getFaction() != ShipFaction.DROWNED) return;
        credit(p, s);
    }

    /** A ship boarded and beaten by a player. */
    public static void onBoarded(ServerPlayerEntity p, ShipEntity s) {
        if (s.getFaction() != ShipFaction.DROWNED) return;
        if (stage(p) == PENNANTS && !s.getCommandTags().contains("ct_pennant_taken")) {
            s.addCommandTag("ct_pennant_taken");
            pennant(p);
        }
        if (s.getCommandTags().contains(ESCORT) && !s.getCommandTags().contains("ct_escort_counted")) {
            s.addCommandTag("ct_escort_counted");
            escort(p);
        }
    }

    private static void credit(ServerPlayerEntity p, ShipEntity s) {
        if (stage(p) == PENNANTS && !s.getCommandTags().contains("ct_pennant_taken")) pennant(p);
        if (s.getCommandTags().contains(ESCORT) && !s.getCommandTags().contains("ct_escort_counted")) escort(p);
    }

    private static void pennant(ServerPlayerEntity p) {
        int n = p.getAttachedOrElse(PENNANT_COUNT, 0) + 1;
        p.setAttached(PENNANT_COUNT, n);
        QuestData.give(p, new ItemStack(ModItems.BLACK_PENNANT));
        p.sendMessage(Text.literal("You cut the black pennant from her masthead (" + n + "/3). A noose around an anchor, stitched in grey thread.")
                .formatted(Formatting.DARK_AQUA), false);
        if (n >= 3) {
            p.setAttached(STAGE, BACK_TO_ADMIRAL);
            title(p, "Three black pennants", "Take them to Admiral Galloway");
        }
    }

    private static void escort(ServerPlayerEntity p) {
        if (stage(p) != GRAVEYARD) return;
        int n = Math.min(3, p.getAttachedOrElse(ESCORTS_SUNK, 0) + 1);
        p.setAttached(ESCORTS_SUNK, n);
        p.sendMessage(Text.literal("Hale's escort is broken (" + n + "/3).").formatted(Formatting.DARK_AQUA), false);
    }

    private static void choose(ServerPlayerEntity p, int which) {
        if (stage(p) != CHOOSE) return;
        if (p.getInventory().count(ModItems.ADMIRALS_COMMISSION) > 0) {
            int slot = p.getInventory().getSlotWithStack(new ItemStack(ModItems.ADMIRALS_COMMISSION));
            if (slot >= 0) p.getInventory().removeStack(slot, 1);
        }
        p.setAttached(ENDING, which);
        p.setAttached(STAGE, DONE);
        if (which == NAVY_ENDING) {
            say(GALLOWAY, p, "It's done, then. I'll see him buried in the Admiralty records where he belongs.",
                    "Kneel. Oh, don't look like that, it's a formality. By my hand and the Crown's seal: Commodore of the Reach.");
            FactionData.adjust(p, Faction.NAVY, 250);
            com.cursedtides.trade.Wallet.give(p, 400 * com.cursedtides.trade.Wallet.SILVER_PER_DOUBLOON);
            QuestData.give(p, com.cursedtides.combat.GearRarity.randomWeapon(p.getServerWorld(), p.getRandom(), 5));
            title(p, "Commodore of the Reach", "The Black Muster: complete");
        } else {
            say(VANCE, p, "Watch.", "...There. Thirty years I've waited to smell that.",
                    "Now. One of Hale's ships came out of the Graveyard on her own and anchored off my landing this morning. No crew. She won't take orders from me.",
                    "She'll take them from you. Don't ask me why. Here's her papers.");
            FactionData.adjust(p, Faction.BRETHREN, 200);
            FactionData.adjust(p, Faction.SMUGGLERS, 200);
            ItemStack deed = new ItemStack(ShipClass.GALLEON.deed());
            NbtCompound fit = new NbtCompound();
            fit.putInt("Look", ShipEntity.LOOK_GHOST);
            fit.putInt("Flag", com.cursedtides.ship.FlagItem.Design.CURSED_TIDE.ordinal());
            deed.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(fit));
            deed.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Hale's Regret").formatted(Formatting.DARK_AQUA));
            QuestData.give(p, deed);
            title(p, "Scourge of the Muster", "The Black Muster: complete");
        }
        QuestData.give(p, new ItemStack(Items.TOTEM_OF_UNDYING));
        com.cursedtides.activity.Achievements.grant(p, "black_muster");
    }
}
