package com.cursedtides.quest;

import com.cursedtides.CursedTides;
import com.cursedtides.combat.Levels;
import com.cursedtides.entity.ModEntities;
import com.cursedtides.entity.ShipEntity;
import com.cursedtides.item.ModItems;
import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.function.Supplier;

/**
 * The castaway's first lessons. Before Keeper Wren sends you to Saltmarrow she makes sure you can look after
 * yourself, one thing at a time, the way the old pirate games taught you by playing: a blade against the crabs
 * on her beach, a parry and a dodge against the first cursed deckhand out of the surf, a pistol, the voodoo doll,
 * a firebomb, and then a sloop of your own, a buoy to round and a hulk to put a broadside into. Each lesson hands
 * over the tool it teaches and spells out the keys; the next starts when it's done.
 */
public final class Lessons {
    private Lessons() {}

    public enum Step {
        TALK("Talk to Keeper Wren by the lighthouse.", null, null),
        BLADE("Clear the crabs off Wren's beach with your cutlass (3).",
                "Left-click to swing", "Swing again and again to chain a combo; the fourth blow is a finisher"),
        GUARD("Cut down the cursed deckhand.",
                "Sneak + right-click to parry, Left Alt to dodge-roll", "A parry turns a blow and staggers them; a roll slips everything"),
        PISTOL("Shoot the deckhands with your flintlock (3 hits).",
                "Right-click to fire", "Each shot needs a moment to reload; keep shot in your pack"),
        DOLL("Hex a deckhand with the voodoo doll.",
                "Use the doll ON a foe to attune, then use it in the air", "Sneak + use the doll to choose another working"),
        BOMB("Throw a firebomb at the deckhands.",
                "Right-click to throw (sneak to throw it further)", "Firebombs, stinkpots and smoke bombs are sold in every port"),
        LAUNCH("Launch your sloop from the pier and take the helm.",
                "Use the deed on open water, then use the ship", "Sneak-punch your ship to stow her back in the deed"),
        SAIL("Raise sail and round the marker buoy south of Driftwood.",
                "R raises sail, V lowers it, W/A/S/D steer", "Running before the wind is fastest; N drops anchor"),
        GUNS("Fire your guns at the old hulk by the buoy (3 hits).",
                "Z fires the port guns, X the starboard guns", "C changes shot; G fires the swivel gun"),
        DONE(null, null, null);

        public final String goal, keys, tip;

        Step(String goal, String keys, String tip) {
            this.goal = goal;
            this.keys = keys;
            this.tip = tip;
        }
    }

    /** Lesson index, and how far into it (hits, kills). */
    public static final AttachmentType<Integer> STEP = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT).copyOnDeath().initializer(() -> 0).buildAndRegister(CursedTides.id("lesson_step"));
    public static final AttachmentType<Integer> COUNT = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT).copyOnDeath().initializer(() -> 0).buildAndRegister(CursedTides.id("lesson_count"));

    private static final String TAG = "ct_lesson";
    /** The marker buoy and the gunnery hulk, off Driftwood's south shore. */
    public static final int BUOY_X = 46, BUOY_Z = 176, HULK_X = -34, HULK_Z = 196;

    public static Step step(ServerPlayerEntity p) {
        Step[] all = Step.values();
        return all[Math.max(0, Math.min(all.length - 1, p.getAttachedOrElse(STEP, 0)))];
    }

    public static boolean active(ServerPlayerEntity p) {
        return QuestData.stage(p) == QuestData.START && step(p) != Step.TALK && step(p) != Step.DONE;
    }

    public static Text objective(ServerPlayerEntity p) {
        Step s = step(p);
        if (s.goal == null) return Text.literal(Step.TALK.goal);
        int need = need(s), have = p.getAttachedOrElse(COUNT, 0);
        return Text.literal(need > 1 ? s.goal + " " + Math.min(have, need) + "/" + need : s.goal);
    }

    private static int need(Step s) {
        return switch (s) {
            case BLADE, PISTOL, GUNS -> 3;
            default -> 1;
        };
    }

    public static void register() {
        STEP.identifier();
        COUNT.identifier();
        ServerTickEvents.END_SERVER_TICK.register(Lessons::tick);
    }

    // ------------------------------------------------------------------ Wren

    private static void wren(ServerPlayerEntity p, String... lines) {
        for (String line : lines) {
            p.sendMessage(Text.literal("[Keeper Wren] ").formatted(Formatting.GOLD).append(Text.literal(line).formatted(Formatting.WHITE)), false);
        }
    }

    /** Wren's side of it: the first talk starts the lessons; later talks repeat the current one. */
    public static void talk(ServerPlayerEntity p) {
        Step s = step(p);
        if (s == Step.TALK) {
            wren(p, "Easy, easy. The tide spat you onto Driftwood Island, same as it did me forty years back.",
                    "You hear that, after dark? That's the Drowned Bell, tolling out on Hollowgrave. Every toll pulls another dead crew up from the deep.",
                    "Before I send you anywhere, you'll learn to look after yourself. Here: my old cutlass.");
            begin(p, Step.BLADE);
            return;
        }
        if (s == Step.DONE) return;
        wren(p, "Where were we? " + s.goal);
        begin(p, s); // hands back the tools and wakes the targets again, in case they were lost
    }

    // ------------------------------------------------------------------ the lessons

    private static void begin(ServerPlayerEntity p, Step s) {
        p.setAttached(STEP, s.ordinal());
        p.setAttached(COUNT, 0);
        ServerWorld w = p.getServerWorld();
        switch (s) {
            case BLADE -> {
                ensure(p, ModItems.CUTLASS, 1);
                wren(p, "Crabs have been at my nets all week. Clear a few off the beach for me, and get the feel of that blade.");
                for (int i = 0; i < 4; i++) spawnNear(w, p, () -> com.cursedtides.wildlife.Wildlife.CRAB, 7, 12, false);
            }
            case GUARD -> {
                wren(p, "Look out, in the surf! One of the Bell's dead. They hit hard: turn its blows aside, roll clear, then cut it down.");
                spawnNear(w, p, () -> ModEntities.CURSED_PIRATE, 9, 13, true);
            }
            case PISTOL -> {
                ensure(p, ModItems.FLINTLOCK_PISTOL, 1);
                ensure(p, ModItems.PISTOL_SHOT, 16);
                wren(p, "More of them coming up the rocks. Take my flintlock: put some shot in them before they're on you.");
                for (int i = 0; i < 2; i++) spawnNear(w, p, () -> ModEntities.CURSED_PIRATE, 14, 18, true);
            }
            case DOLL -> {
                ensure(p, com.cursedtides.combat.Grenades.VOODOO_DOLL, 1);
                wren(p, "My mother's doll. Ugly thing, but it works. Bind it to one of the dead, then stick it. The hex follows the thread.");
                spawnNear(w, p, () -> ModEntities.CURSED_PIRATE, 10, 14, true);
            }
            case BOMB -> {
                ensure(p, com.cursedtides.combat.Grenades.FIREBOMB, 3);
                wren(p, "A whole knot of them now! Here, firebombs. Throw, don't hold.");
                for (int i = 0; i < 3; i++) spawnNear(w, p, () -> ModEntities.CURSED_PIRATE, 12, 15, true);
            }
            case LAUNCH -> {
                ensure(p, ModItems.SLOOP_DEED, 1);
                wren(p, "You'll do. And you'll not reach anywhere worth going on foot.",
                        "This is the deed to the sloop my husband never came back for. She's sound. Take her down to the pier and put her in the water.");
            }
            case SAIL -> wren(p, "Now get some wind in her. There's a marker buoy off the south shore: sail out and round it.");
            case GUNS -> wren(p, "See that old hulk past the buoy? Give her a broadside. Turn side-on to her first: the guns fire out of the sides.");
            case DONE -> {
                wren(p, "Ha! You'll make a captain yet.",
                        "Now: Saltmarrow lies east, a short sail. Open your chart (M) and you'll see it. Ask for Ysolde Vane, the harbormistress.",
                        "And keep your journal (J) about you. When your skills grow, K lets you choose what you learn.");
                ensure(p, ModItems.SEA_CHART, 1);
                QuestData.give(p, new ItemStack(net.minecraft.item.Items.BREAD, 8));
                QuestData.setStage(p, QuestData.FIND_YSOLDE);
                title(p, "Lessons done", "Sail east to Saltmarrow");
                p.getWorld().playSound(null, p.getBlockPos(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 0.7f, 1.1f);
                return;
            }
            default -> { }
        }
        if (s.keys != null) {
            title(p, s.keys, s.tip);
            p.sendMessage(Text.literal("Lesson: ").formatted(Formatting.AQUA, Formatting.BOLD)
                    .append(Text.literal(s.goal + "  ").formatted(Formatting.WHITE))
                    .append(Text.literal("(" + s.keys + ")").formatted(Formatting.GRAY)), false);
        }
    }

    private static void title(ServerPlayerEntity p, String title, String sub) {
        p.networkHandler.sendPacket(new TitleFadeS2CPacket(10, 90, 20));
        p.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal(sub == null ? "" : sub).formatted(Formatting.GRAY)));
        p.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(title).formatted(Formatting.AQUA)));
    }

    private static void ensure(ServerPlayerEntity p, net.minecraft.item.Item item, int count) {
        int have = p.getInventory().count(item);
        if (have < count) QuestData.give(p, new ItemStack(item, count - have));
    }

    private static void spawnNear(ServerWorld w, ServerPlayerEntity p, Supplier<EntityType<?>> type, int min, int max, boolean leveled) {
        for (int tries = 0; tries < 10; tries++) {
            double a = w.random.nextDouble() * Math.PI * 2;
            int d = min + w.random.nextInt(max - min + 1);
            BlockPos at = com.cursedtides.living.Residents.openGround(w, (int) (p.getX() + Math.cos(a) * d), (int) (p.getZ() + Math.sin(a) * d), 4);
            if (at == null) continue;
            Entity e = type.get().create(w);
            if (!(e instanceof MobEntity mob)) return;
            mob.refreshPositionAndAngles(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, w.random.nextFloat() * 360, 0);
            mob.initialize(w, w.getLocalDifficulty(at), SpawnReason.EVENT, null);
            mob.addCommandTag(TAG);
            if (leveled) {
                Levels.apply(mob, 1);
                mob.setTarget(p);
            }
            w.spawnEntity(mob);
            return;
        }
    }

    private static void progress(ServerPlayerEntity p, Step s) {
        if (step(p) != s || QuestData.stage(p) != QuestData.START) return;
        int have = p.getAttachedOrElse(COUNT, 0) + 1;
        p.setAttached(COUNT, have);
        if (have < need(s)) {
            p.sendMessage(Text.literal(s.goal + " " + have + "/" + need(s)).formatted(Formatting.AQUA), true);
            return;
        }
        p.sendMessage(Text.literal("Lesson learned!").formatted(Formatting.GREEN, Formatting.BOLD), true);
        p.getWorld().playSound(null, p.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.6f, 1.4f);
        Weapon w = switch (s) {
            case BLADE, GUARD -> Weapon.CUTLASS;
            case PISTOL -> Weapon.PISTOL;
            case DOLL, BOMB -> Weapon.HEX;
            case LAUNCH, SAIL -> Weapon.SAILING;
            case GUNS -> Weapon.CANNON;
            default -> null;
        };
        if (w != null) PirateData.addXp(p, w, 25);
        begin(p, Step.values()[s.ordinal() + 1]);
    }

    // ------------------------------------------------------------------ hooks from the rest of the game

    /** A foe died; the weapon is what landed the killing blow (or null). */
    public static void onKill(ServerPlayerEntity p, Weapon weapon, LivingEntity victim) {
        if (!active(p)) return;
        Step s = step(p);
        if (s == Step.BLADE && weapon == Weapon.CUTLASS) progress(p, s);
        else if (s == Step.GUARD && weapon == Weapon.CUTLASS && Levels.levelOf(victim) > 0) progress(p, s);
    }

    /** A hit landed with a weapon. */
    public static void onHit(ServerPlayerEntity p, Weapon weapon, LivingEntity victim) {
        if (active(p) && step(p) == Step.PISTOL && weapon == Weapon.PISTOL) progress(p, Step.PISTOL);
    }

    public static void onDollWorked(ServerPlayerEntity p) {
        if (active(p) && step(p) == Step.DOLL) progress(p, Step.DOLL);
    }

    public static void onThrow(ServerPlayerEntity p) {
        if (active(p) && step(p) == Step.BOMB) progress(p, Step.BOMB);
    }

    public static void onCannonImpact(Entity owner, Vec3d at) {
        ServerPlayerEntity p = owner instanceof ServerPlayerEntity sp ? sp
                : owner instanceof ShipEntity ship && ship.getControllingPassenger() instanceof ServerPlayerEntity sp2 ? sp2 : null;
        if (p == null || !active(p) || step(p) != Step.GUNS) return;
        double dx = at.x - HULK_X, dz = at.z - HULK_Z;
        if (dx * dx + dz * dz < 16 * 16) progress(p, Step.GUNS);
    }

    private static void tick(MinecraftServer server) {
        if (server.getTicks() % 10 != 3) return;
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            if (!active(p)) continue;
            Step s = step(p);
            boolean aboard = p.getVehicle() instanceof ShipEntity ship && ship.isOwner(p);
            if (s == Step.LAUNCH && aboard) progress(p, s);
            else if (s == Step.SAIL && aboard) {
                double dx = p.getX() - BUOY_X, dz = p.getZ() - BUOY_Z;
                if (dx * dx + dz * dz < 26 * 26) progress(p, s);
            }
        }
    }
}
