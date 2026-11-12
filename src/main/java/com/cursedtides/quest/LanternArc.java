package com.cursedtides.quest;

import com.cursedtides.CursedTides;
import com.cursedtides.combat.Levels;
import com.cursedtides.entity.StoryNpcEntity;
import com.cursedtides.world.Archipelago;
import com.cursedtides.world.Builds;
import com.cursedtides.world.Canvas;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Act four, "The Drowned Lights". With the crown settled, every lighthouse in the Reach flickers red at once.
 * Keeper Wren knows the old story: the First People kept watch-fires on the far rocks, not for ships but to keep
 * something asleep beneath the Reach. The fires went out when the sea rose. Something is waking. Relight the three
 * Drowned Lights on Stormbreak, Widow's Rock and Gullscream Rock (each has a keeper who no longer wants it lit),
 * follow where their beams cross out to open sea, and face the Abyssal when it rises. Then decide what to do with
 * its heart.
 */
public final class LanternArc {
    private LanternArc() {}

    public static final int NONE = 0, SEE_WREN = 1, SEE_SCHOLAR = 2, LIGHTS = 3, THE_DEEP = 4, CHOOSE = 5, DONE = 6;
    public static final int RETURNED = 1, KEPT = 2;

    public static final AttachmentType<Integer> STAGE = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT).copyOnDeath().initializer(() -> 0).buildAndRegister(CursedTides.id("lantern_stage"));
    public static final AttachmentType<Integer> LIT = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT).copyOnDeath().initializer(() -> 0).buildAndRegister(CursedTides.id("lantern_lit"));
    public static final AttachmentType<Integer> ENDING = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT).copyOnDeath().initializer(() -> 0).buildAndRegister(CursedTides.id("lantern_ending"));

    public static int stage(ServerPlayerEntity p) { return p.getAttachedOrElse(STAGE, 0); }

    // ------------------------------------------------------------------ the three lights

    public record Light(String name, Archipelago.Island island, String keeper, Supplier<EntityType<?>> keeperType, int bit) {}

    public static final List<Light> LIGHTS_LIST = List.of(
            new Light("the Storm Light", Archipelago.STORMBREAK_ISLE, "Brannoch, Who Drowned the Fire", () -> EntityType.DROWNED, 1),
            new Light("the Widow's Light", Archipelago.WIDOWS_ROCK, "The Grey Lamplighter", () -> EntityType.WITHER_SKELETON, 2),
            new Light("the Gull Light", Archipelago.GULLSCREAM_ROCK, "Mother of Gulls", () -> EntityType.EVOKER, 4));

    private static volatile List<BlockPos> towers;

    public static List<BlockPos> towers() {
        if (towers == null) Builds.groundY(0, 0);
        return towers == null ? List.of() : towers;
    }

    /** Where the three beams cross: open water south of the Forgotten Keys. */
    public static int deepX() { return Archipelago.seaX(330); }
    public static int deepZ() { return Archipelago.seaZ(900); }

    /** Called at the end of Builds' setup: a ruined beacon tower on each of the three rocks. */
    public static synchronized void addBuilds(List<Builds.Build> out) {
        if (towers != null) return;
        List<BlockPos> list = new ArrayList<>();
        for (Light l : LIGHTS_LIST) {
            int[] at = com.cursedtides.dungeon.Caverns.findMouth(l.island());
            BlockPos base = new BlockPos(at[0], Builds.groundY(at[0], at[1]), at[1]);
            list.add(base);
            out.add(new Builds.Build(at[0] - 4, at[1] - 4, at[0] + 4, at[1] + 4, true, c -> tower(c, base)));
        }
        towers = List.copyOf(list);
    }

    private static BlockState s(net.minecraft.block.Block b) { return b.getDefaultState(); }

    private static void tower(Canvas c, BlockPos base) {
        int x = base.getX(), y = base.getY(), z = base.getZ();
        c.foundation(x - 3, z - 3, x + 3, z + 3, y, s(Blocks.COBBLESTONE));
        for (int k = 0; k < 16; k++) {
            int r = k < 10 ? 3 : 2;
            for (int dx = -r; dx <= r; dx++) for (int dz = -r; dz <= r; dz++) {
                boolean edge = Math.abs(dx) == r || Math.abs(dz) == r;
                if (!edge) { c.set(x + dx, y + k, z + dz, s(Blocks.AIR)); continue; }
                double n = com.cursedtides.world.Noise.rand(x + dx, y + k, z + dz, 4401);
                if (k > 6 && n < 0.12) continue; // broken stones
                c.set(x + dx, y + k, z + dz, s(n < 0.35 ? Blocks.MOSSY_STONE_BRICKS : n < 0.5 ? Blocks.CRACKED_STONE_BRICKS : Blocks.STONE_BRICKS));
            }
        }
        c.set(x, y, z + 3, s(Blocks.AIR));
        c.set(x, y + 1, z + 3, s(Blocks.AIR));
        BlockState ladder = s(Blocks.LADDER).with(net.minecraft.block.LadderBlock.FACING, net.minecraft.util.math.Direction.SOUTH);
        for (int k = 0; k <= 16; k++) c.set(x, y + k, z - 2 + (k >= 10 ? 1 : 0), ladder);
        c.fill(x - 2, y + 16, z - 2, x + 2, y + 16, z + 2, s(Blocks.STONE_BRICKS));
        c.set(x, y + 17, z, s(Blocks.CAULDRON)); // the cold brazier
    }

    // ------------------------------------------------------------------ talking

    private static void say(String who, ServerPlayerEntity p, String... lines) {
        for (String l : lines) {
            p.sendMessage(Text.literal("[" + who + "] ").formatted(Formatting.GOLD).append(Text.literal(l).formatted(Formatting.WHITE)), false);
        }
    }

    /** Wren's part; true if she had something to say about it. */
    public static boolean wren(StoryNpcEntity npc, ServerPlayerEntity p) {
        int st = stage(p);
        if (st == SEE_WREN) {
            say("Keeper Wren", p, "You saw them too. Every light in the Reach, red, all at once. My own lamp did it. I've never been so frightened.",
                    "My grandmother told it as a children's tale. Before the sea rose, the First People kept watch-fires on the far rocks.",
                    "Not for ships. For something underneath. To keep it sleeping. The fires drowned with them.",
                    "Professor Thwaite on the Forgotten Keys will know if there's truth in it. Please. Go and ask him.");
            p.setAttached(STAGE, SEE_SCHOLAR);
            return true;
        }
        if (st == CHOOSE) {
            say("Keeper Wren", p, "You're alive! And that... that's its heart, isn't it. Still beating.",
                    "Give it back to the deep and it sleeps again, for a thousand years maybe. Keep it, and gods know what it gives you.");
            p.sendMessage(Text.literal("  [Return the heart to the deep]").styled(s -> s.withColor(Formatting.AQUA)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/drownedlights return")))
                    .append(Text.literal("   [Keep the heart]").styled(s -> s.withColor(Formatting.DARK_RED)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/drownedlights keep")))), false);
            return true;
        }
        if (st == DONE) {
            say("Keeper Wren", p, ending(p) == RETURNED ? "The lamp burns gold again. Every night I thank you for it, Lightkeeper."
                    : "The sea goes very quiet when you walk past it now. I don't think it's peace.");
            return true;
        }
        if (st > SEE_WREN) {
            say("Keeper Wren", p, objective(p));
            return true;
        }
        return false;
    }

    public static boolean scholar(StoryNpcEntity npc, ServerPlayerEntity p) {
        if (stage(p) != SEE_SCHOLAR) return false;
        say("Professor Ansel Thwaite", p, "Red lights? Oh dear. Oh, dear, dear. Sit down. No, stand, there isn't time.",
                "The tablets call it the Abyssal. Not a god; a hunger. The First People sank it under the Reach and lit three fires to keep it dreaming.",
                "The Storm Light on Stormbreak, the Widow's Light on Widow's Rock, the Gull Light on Gullscream Rock. Ruins now.",
                "Light them again. Take this: First-Wick, from the observatory stores. It'll take a flame where nothing else will.",
                "And captain: whoever let them go out had reasons. They may still be there, keeping them dark.");
        ItemStack wick = new ItemStack(Items.BLAZE_POWDER);
        wick.set(net.minecraft.component.DataComponentTypes.CUSTOM_NAME, Text.literal("First-Wick").formatted(Formatting.GOLD));
        QuestData.give(p, wick);
        p.setAttached(STAGE, LIGHTS);
        title(p, "The Drowned Lights", "Relight the three fires");
        return true;
    }

    private static void title(ServerPlayerEntity p, String t, String sub) {
        p.networkHandler.sendPacket(new TitleFadeS2CPacket(10, 70, 20));
        p.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal(sub).formatted(Formatting.GRAY)));
        p.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(t).formatted(Formatting.GOLD)));
    }

    public static String objective(ServerPlayerEntity p) {
        return switch (stage(p)) {
            case SEE_WREN -> "Every light in the Reach burned red. Ask Keeper Wren on Driftwood what it means.";
            case SEE_SCHOLAR -> "Ask Professor Thwaite on the Forgotten Keys about the First People's watch-fires.";
            case LIGHTS -> {
                int lit = p.getAttachedOrElse(LIT, 0);
                StringBuilder b = new StringBuilder("Relight the Drowned Lights:");
                for (Light l : LIGHTS_LIST) b.append((lit & l.bit()) != 0 ? " §a" : " §7").append(l.island().name()).append("§f");
                yield b.toString();
            }
            case THE_DEEP -> "The three beams cross over open water south of the Forgotten Keys. Sail there and face what rises.";
            case CHOOSE -> "Bring the Abyssal's heart to Keeper Wren on Driftwood, and decide what becomes of it.";
            case DONE -> ending(p) == RETURNED ? "The Abyssal sleeps again. You are the Lightkeeper of the Reach."
                    : "You keep the Abyssal's heart. The sea knows it.";
            default -> "Not yet begun. (Comes after the Hollow Crown.)";
        };
    }

    public static List<String> journal(ServerPlayerEntity p) {
        return List.of("§f" + objective(p));
    }

    public static int ending(ServerPlayerEntity p) { return p.getAttachedOrElse(ENDING, 0); }

    public static int[] target(ServerPlayerEntity p) {
        switch (stage(p)) {
            case SEE_WREN, CHOOSE -> { return spot(StoryNpcEntity.Role.KEEPER); }
            case SEE_SCHOLAR -> { return spot(StoryNpcEntity.Role.SCHOLAR); }
            case LIGHTS -> {
                int lit = p.getAttachedOrElse(LIT, 0);
                List<BlockPos> t = towers();
                for (int i = 0; i < LIGHTS_LIST.size() && i < t.size(); i++) {
                    if ((lit & LIGHTS_LIST.get(i).bit()) == 0) return new int[]{t.get(i).getX(), t.get(i).getZ()};
                }
                return null;
            }
            case THE_DEEP -> { return new int[]{deepX(), deepZ()}; }
            default -> { return null; }
        }
    }

    public static String targetLabel(ServerPlayerEntity p) {
        return switch (stage(p)) {
            case SEE_WREN, CHOOSE -> "Keeper Wren";
            case SEE_SCHOLAR -> "Professor Thwaite";
            case LIGHTS -> "A Drowned Light";
            case THE_DEEP -> "Where the beams cross";
            default -> "";
        };
    }

    private static int[] spot(StoryNpcEntity.Role role) {
        for (Builds.NpcSpot s : Builds.NPC_SPOTS) if (s.role() == role) return new int[]{(int) s.x(), (int) s.z()};
        return null;
    }

    // ------------------------------------------------------------------ the fights

    private static final String KEEPER_TAG = "ct_light_keeper:";
    private static final String ABYSSAL_TAG = "ct_abyssal";

    public static void register() {
        STAGE.identifier();
        LIT.identifier();
        ENDING.identifier();
        ServerTickEvents.END_SERVER_TICK.register(LanternArc::tick);
        ServerLivingEntityEvents.AFTER_DEATH.register(LanternArc::onDeath);
        CommandRegistrationCallback.EVENT.register((d, a, e) -> d.register(literal("drownedlights")
                .then(literal("return").executes(ctx -> { choose(ctx.getSource().getPlayerOrThrow(), RETURNED); return 1; }))
                .then(literal("keep").executes(ctx -> { choose(ctx.getSource().getPlayerOrThrow(), KEPT); return 1; }))));
    }

    private static void tick(MinecraftServer server) {
        if (server.getTicks() % 20 != 17) return;
        ServerWorld w = server.getOverworld();
        for (ServerPlayerEntity p : w.getPlayers()) {
            int st = stage(p);
            if (st == NONE) {
                if (CrownArc.stage(p) == CrownArc.DONE && w.isNight()) {
                    p.setAttached(STAGE, SEE_WREN);
                    p.sendMessage(Text.literal("Out across the water, every lighthouse in sight flares red at the same moment, and goes dark.")
                            .formatted(Formatting.DARK_RED, Formatting.ITALIC), false);
                    title(p, "The Drowned Lights", "A new tale begins");
                }
                continue;
            }
            if (st == LIGHTS) {
                List<BlockPos> t = towers();
                int lit = p.getAttachedOrElse(LIT, 0);
                for (int i = 0; i < LIGHTS_LIST.size() && i < t.size(); i++) {
                    Light l = LIGHTS_LIST.get(i);
                    if ((lit & l.bit()) != 0) continue;
                    BlockPos base = t.get(i);
                    if (p.squaredDistanceTo(base.getX() + 0.5, base.getY(), base.getZ() + 0.5) > 28 * 28) continue;
                    final String tag = KEEPER_TAG + i;
                    if (w.getEntitiesByClass(MobEntity.class, new Box(base).expand(48), m -> m.getCommandTags().contains(tag)).isEmpty()) {
                        spawnKeeper(w, p, l, i, base);
                    }
                }
            }
            if (st == THE_DEEP && p.squaredDistanceTo(deepX(), p.getY(), deepZ()) < 70 * 70) {
                boolean risen = !w.getEntitiesByClass(MobEntity.class, new Box(new BlockPos(deepX(), 50, deepZ())).expand(120),
                        m -> m.getCommandTags().contains(ABYSSAL_TAG)).isEmpty();
                if (!risen) rise(w, p);
            }
        }
    }

    private static MobEntity boss(ServerWorld w, Entity e, double x, double y, double z, String name, int level, double hpMult) {
        if (!(e instanceof MobEntity m)) return null;
        m.refreshPositionAndAngles(x, y, z, w.random.nextFloat() * 360, 0);
        m.initialize(w, w.getLocalDifficulty(BlockPos.ofFloored(x, y, z)), SpawnReason.EVENT, null);
        m.setCustomName(Text.literal(name).formatted(Formatting.DARK_RED, Formatting.BOLD));
        m.setCustomNameVisible(true);
        m.setPersistent();
        Levels.apply(m, level);
        EntityAttributeInstance hp = m.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (hp != null) {
            hp.setBaseValue(hp.getBaseValue() * hpMult * Levels.healthFactor(level));
            m.setHealth(m.getMaxHealth());
        }
        return m;
    }

    private static void spawnKeeper(ServerWorld w, ServerPlayerEntity p, Light l, int i, BlockPos base) {
        int level = Math.min(50, Levels.band(l.island())[1] + 3);
        BlockPos at = com.cursedtides.living.Residents.openGround(w, base.getX() + 6, base.getZ() + 2, 6);
        if (at == null) at = base.up();
        MobEntity m = boss(w, l.keeperType().get().create(w), at.getX() + 0.5, at.getY(), at.getZ() + 0.5, l.keeper(), level, 6);
        if (m == null) return;
        m.addCommandTag(KEEPER_TAG + i);
        m.setTarget(p);
        w.spawnEntity(m);
        say(l.keeper(), p, i == 0 ? "The fire is out because I put it out. Let him sleep, let him sleep, LET HIM SLEEP."
                : i == 1 ? "Every widow on this rock lit that lamp for a man who never came home. No more. No more lights."
                : "My children nest in the cold brazier. You will not burn them out.");
    }

    private static void rise(ServerWorld w, ServerPlayerEntity p) {
        double x = deepX() + 0.5, z = deepZ() + 0.5;
        MobEntity maw = boss(w, EntityType.ELDER_GUARDIAN.create(w), x, Archipelago.WATER_TOP - 6, z, "The Abyssal", 50, 4);
        if (maw == null) return;
        maw.addCommandTag(ABYSSAL_TAG);
        w.spawnEntity(maw);
        for (int k = 0; k < 6; k++) {
            double a = Math.PI * 2 * k / 6;
            Entity t = com.cursedtides.sea.Leviathan.TENTACLE.create(w);
            if (t instanceof MobEntity tm) {
                tm.refreshPositionAndAngles(x + Math.cos(a) * 14, Archipelago.WATER_TOP - 4, z + Math.sin(a) * 14, 0, 0);
                tm.addCommandTag(ABYSSAL_TAG + "_arm");
                w.spawnEntity(tm);
            }
        }
        for (ServerPlayerEntity near : w.getPlayers(q -> q.squaredDistanceTo(x, q.getY(), z) < 150 * 150)) {
            title(near, "The Abyssal", "rises from the deep");
        }
        w.playSound(null, BlockPos.ofFloored(x, 62, z), SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.HOSTILE, 3f, 0.5f);
        w.setWeather(0, 6000, true, true);
    }

    private static void onDeath(LivingEntity victim, DamageSource source) {
        if (!(victim.getWorld() instanceof ServerWorld w)) return;
        for (String tag : victim.getCommandTags()) {
            if (tag.startsWith(KEEPER_TAG)) {
                int i = Integer.parseInt(tag.substring(KEEPER_TAG.length()));
                Light l = LIGHTS_LIST.get(i);
                BlockPos base = towers().get(i);
                w.setBlockState(base.up(17), Blocks.CAMPFIRE.getDefaultState());
                w.setBlockState(base.up(16), Blocks.MAGMA_BLOCK.getDefaultState());
                for (ServerPlayerEntity p : w.getPlayers(q -> q.squaredDistanceTo(victim) < 64 * 64 && stage(q) == LIGHTS)) {
                    int lit = p.getAttachedOrElse(LIT, 0) | l.bit();
                    p.setAttached(LIT, lit);
                    p.sendMessage(Text.literal("The First-Wick catches. " + cap(l.name()) + " burns again, and its beam swings out to sea.")
                            .formatted(Formatting.GOLD), false);
                    if (lit == 7) {
                        p.setAttached(STAGE, THE_DEEP);
                        title(p, "Three lights burn", "Their beams cross far to the south");
                    }
                }
                return;
            }
            if (tag.equals(ABYSSAL_TAG)) {
                for (Entity arm : w.getEntitiesByClass(MobEntity.class, victim.getBoundingBox().expand(60), m -> m.getCommandTags().contains(ABYSSAL_TAG + "_arm"))) {
                    arm.discard();
                }
                for (ServerPlayerEntity p : w.getPlayers(q -> q.squaredDistanceTo(victim) < 120 * 120 && stage(q) == THE_DEEP)) {
                    p.setAttached(STAGE, CHOOSE);
                    title(p, "The Abyssal falls", "Its heart still beats");
                    ItemStack heart = new ItemStack(Items.HEART_OF_THE_SEA);
                    heart.set(net.minecraft.component.DataComponentTypes.CUSTOM_NAME, Text.literal("Heart of the Abyssal").formatted(Formatting.DARK_AQUA));
                    QuestData.give(p, heart);
                    p.sendMessage(Text.literal("Something rises in the churned water: a heart the size of a barrel, still beating. Wren will know what to do.")
                            .formatted(Formatting.AQUA), false);
                }
                w.setWeather(6000, 0, false, false);
                return;
            }
        }
    }

    private static String cap(String s) { return Character.toUpperCase(s.charAt(0)) + s.substring(1); }

    private static void choose(ServerPlayerEntity p, int ending) {
        if (stage(p) != CHOOSE) return;
        p.setAttached(ENDING, ending);
        p.setAttached(STAGE, DONE);
        if (ending == RETURNED) {
            say("Keeper Wren", p, "Then let's do it properly. Row it out past the reef with me. My first trip in forty years.",
                    "...There. It's gone down. Listen. The lamp's burning gold again.");
            QuestData.give(p, new ItemStack(com.cursedtides.appearance.Accessories.random(p.getRandom(), true)));
            com.cursedtides.trade.Wallet.give(p, 300 * com.cursedtides.trade.Wallet.SILVER_PER_DOUBLOON);
            com.cursedtides.faction.FactionData.adjust(p, com.cursedtides.faction.Faction.NAVY, 150);
            title(p, "Lightkeeper of the Reach", "The Drowned Lights: complete");
        } else {
            say("Keeper Wren", p, "You're keeping it. Of course you are.",
                    "Hold it close, then. The sea will move for you now. Just remember that it's always listening.");
            p.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.CONDUIT_POWER, 20 * 60 * 20, 0));
            com.cursedtides.faction.FactionData.adjust(p, com.cursedtides.faction.Faction.BRETHREN, 150);
            QuestData.give(p, com.cursedtides.combat.GearRarity.randomWeapon(p.getServerWorld(), p.getRandom(), 5));
            title(p, "Heart of the Abyss", "The Drowned Lights: complete");
        }
        com.cursedtides.activity.Achievements.grant(p, "drowned_lights");
    }
}
