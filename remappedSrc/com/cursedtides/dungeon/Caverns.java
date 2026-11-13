package com.cursedtides.dungeon;

import com.cursedtides.CursedTides;
import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import com.cursedtides.world.Archipelago;
import com.cursedtides.world.Builds;
import com.cursedtides.world.Canvas;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * The grand caverns: one vast cave system under each of seven islands. You climb down an old rope from a
 * cave mouth on the surface into a camp, then push through chamber after chamber (packs of the dead, spiders,
 * claim-jumpers, whatever lives down there come at you in waves) to the great hall at the bottom, where
 * something big has made its lair. Beat it for the cavern's hoard. It returns in time.
 */
public final class Caverns {
    private Caverns() {}

    public record Site(String name, Archipelago.Island island, CavernTheme theme, int chambers,
                       String bossName, Supplier<EntityType<?>> boss, int health, int tier) {}

    public static final List<Site> SITES = List.of(
            new Site("The Weeping Catacombs", Archipelago.HOLLOWGRAVE_ISLAND, CavernTheme.CURSED, 8,
                    "The Bone Abbot", () -> EntityType.WITHER_SKELETON, 220, 3),
            new Site("Cinderdeep", Archipelago.ASHFALL_ISLAND, CavernTheme.VOLCANIC, 7,
                    "The Cinder Matron", () -> EntityType.BLAZE, 180, 3),
            new Site("The Serpent's Throat", Archipelago.SERPENTS_HOLLOW, CavernTheme.JUNGLE, 8,
                    "Old Gnasher", () -> com.cursedtides.wildlife.Wildlife.CROCODILE, 260, 4),
            new Site("The Motherlode", Archipelago.GOLDWATER_CAY, CavernTheme.MINE, 7,
                    "Claim-Jumper Grubb", () -> EntityType.VINDICATOR, 200, 3),
            new Site("The Drowned Grotto", Archipelago.WHISPERING_KEY, CavernTheme.DROWNED, 7,
                    "Mother Brine", () -> EntityType.DROWNED, 240, 4),
            new Site("The Furnace Below", Archipelago.EMBERFALL, CavernTheme.VOLCANIC, 9,
                    "The Slag Beast", () -> EntityType.RAVAGER, 320, 5),
            new Site("Hall of the First Light", Archipelago.LAST_HORIZON, CavernTheme.CRYSTAL, 9,
                    "The Lantern Warden", () -> EntityType.EVOKER, 300, 5)
    );

    private static volatile List<Cavern> built;

    /** All caverns, laid out once the island's buildings are known (which is when the world generator first runs). */
    public static List<Cavern> all() {
        if (built == null) Builds.groundY(0, 0); // runs the Builds setup, which calls addBuilds
        return built == null ? List.of() : built;
    }

    // ------------------------------------------------------------------ worldgen hookup

    /** Called at the end of Builds' setup: picks each cave mouth, lays out the caverns and adds them. */
    public static synchronized void addBuilds(List<Builds.Build> out) {
        if (built != null) return;
        List<Cavern> list = new ArrayList<>();
        for (int i = 0; i < SITES.size(); i++) {
            Site s = SITES.get(i);
            int[] m = findMouth(s.island());
            Cavern cv = null;
            for (int v = 0; v < 12; v++) {
                Cavern attempt = new Cavern(i, s.name(), m[0], m[1], s.theme(), s.chambers(), v);
                boolean clash = false;
                for (Cavern o : list) if (attempt.overlaps(o, 12)) clash = true;
                if (!clash) { cv = attempt; break; }
                if (cv == null) cv = attempt;
            }
            cv.mouthY = Builds.groundY(m[0], m[1]);
            list.add(cv);
            final Cavern c = cv;
            out.add(new Builds.Build(c.minX, c.minZ, c.maxX, c.maxZ, false, c::place));
            out.add(new Builds.Build(m[0] - 5, m[1] - 5, m[0] + 5, m[1] + 5, true, canvas -> mouth(canvas, c)));
            Cavern.Chamber arena = c.arena();
            CursedTides.LOGGER.info("Grand cavern {}: mouth {} {} {}, great hall {} {} {}", c.name, m[0], c.mouthY, m[1],
                    arena.cx(), arena.floor(), arena.cz());
        }
        built = List.copyOf(list);
    }

    /** The flattest open bit of the island that no building claims; failing that, the flattest bit of land at all. */
    public static int[] findMouth(Archipelago.Island is) {
        int[] best = null, fallback = null;
        int bestSlope = Integer.MAX_VALUE, fallbackSlope = Integer.MAX_VALUE;
        for (int r = 8; r < is.radius() * 0.9; r += 3) {
            for (int k = 0; k < 32; k++) {
                double a = k * Math.PI * 2 / 32 + r * 0.37;
                int x = is.cx() + (int) (Math.cos(a) * r), z = is.cz() + (int) (Math.sin(a) * r);
                int slope = slope(x, z);
                if (slope < 0) continue;
                boolean claimed = false;
                for (int dx = -5; dx <= 5 && !claimed; dx += 5)
                    for (int dz = -5; dz <= 5 && !claimed; dz += 5)
                        if (Builds.blocksPlants(x + dx, z + dz)) claimed = true;
                if (!claimed && slope < bestSlope) { best = new int[]{x, z}; bestSlope = slope; }
                if (claimed && !Builds.blocksPlants(x, z) && slope < fallbackSlope) { fallback = new int[]{x, z}; fallbackSlope = slope; }
            }
        }
        if (best != null) return best;
        if (fallback != null) return fallback;
        return new int[]{is.cx() + is.radius() / 2, is.cz()};
    }

    /** Height spread of the ground round a spot, or -1 if any of it is sea, crater or too near the shore. */
    private static int slope(int x, int z) {
        int lo = Integer.MAX_VALUE, hi = Integer.MIN_VALUE;
        for (int dx = -5; dx <= 5; dx += 5) {
            for (int dz = -5; dz <= 5; dz += 5) {
                Archipelago.Column col = Archipelago.column(x + dx, z + dz);
                if (!col.isLand() || col.crater() || col.lavaTop() > 0 || col.top() < Archipelago.WATER_TOP + 3) return -1;
                lo = Math.min(lo, col.top());
                hi = Math.max(hi, col.top());
            }
        }
        return hi - lo;
    }

    /** A ring of tumbled rock round a dark pit, a beam across it and the old rope going down. */
    private static void mouth(Canvas c, Cavern cv) {
        int mx = cv.mouthX, mz = cv.mouthZ, y = cv.mouthY;
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                int x = mx + dx, z = mz + dz;
                if (!c.contains(x, z)) continue;
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > 5.2) continue;
                int ground = Builds.groundY(x, z);
                // Level the ground into a little clearing
                for (int yy = y; yy <= Math.max(y, ground) + 3; yy++) c.set(x, yy, z, Blocks.AIR.defaultBlockState());
                for (int yy = Math.min(y, ground) - 1; yy < y; yy++) c.set(x, yy, z, cv.theme.wall(x, yy, z));
                c.set(x, y - 1, z, d < 4.5 ? cv.theme.floor(x, y - 1, z) : Blocks.COARSE_DIRT.defaultBlockState());
                boolean opening = dx >= 2 && Math.abs(dz) <= 1;
                if (d >= 3.2 && d <= 4.6 && !opening) {
                    int h = 1 + (int) (com.cursedtides.world.Noise.rand(x, z, 4321) * 3);
                    for (int k = 0; k < h; k++) c.set(x, y + k, z, cv.theme.wall(x, y + k, z));
                }
                if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {
                    for (int yy = y - 6; yy < y; yy++) c.set(x, yy, z, Blocks.CAVE_AIR.defaultBlockState());
                    c.set(x, y - 7, z, Blocks.BLACK_CONCRETE_POWDER.defaultBlockState());
                }
            }
        }
        // The beam and its posts
        for (int dx = -2; dx <= 2; dx += 4) {
            if (!c.contains(mx + dx, mz)) continue;
            c.fill(mx + dx, y, mz, mx + dx, y + 2, mz, Blocks.SPRUCE_FENCE.defaultBlockState());
        }
        BlockState beam = Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, Direction.Axis.X);
        for (int dx = -2; dx <= 2; dx++) if (c.contains(mx + dx, mz)) c.set(mx + dx, y + 3, mz, beam);
        if (c.contains(mx, mz)) {
            for (int yy = y - 6; yy <= y + 2; yy++) c.set(mx, yy, mz, Cavern.CaveRopeHolder.rope());
        }
        if (c.contains(mx + 2, mz + 1)) c.set(mx + 2, y + 3, mz + 1,
                Blocks.LANTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LanternBlock.HANGING, false));
        if (c.contains(mx - 2, mz - 1)) c.set(mx - 2, y + 3, mz - 1, Blocks.LANTERN.defaultBlockState());
    }

    // ------------------------------------------------------------------ the rope

    /** Like {@link #at}, but never forces the world layout to be worked out (null until it has been). */
    public static Cavern atQuiet(BlockPos p) {
        List<Cavern> b = built;
        if (b == null) return null;
        for (Cavern c : b) if (c.contains(p)) return c;
        return null;
    }

    public static Cavern at(BlockPos p) {
        for (Cavern c : all()) if (c.contains(p)) return c;
        return null;
    }

    public static void climb(ServerPlayer player, BlockPos ropePos) {
        for (Cavern c : all()) {
            if (ropePos.getY() > 20 && Math.abs(ropePos.getX() - c.mouthX) <= 3 && Math.abs(ropePos.getZ() - c.mouthZ) <= 3) {
                BlockPos land = c.ropeLanding();
                player.displayClientMessage(Component.literal("You climb down into the dark...").withStyle(ChatFormatting.GRAY), true);
                travel(player, land.getX() + 1.5, land.getY(), land.getZ() + 0.5);
                return;
            }
            if (c.contains(ropePos)) {
                player.displayClientMessage(Component.literal("You haul yourself back up to daylight.").withStyle(ChatFormatting.GRAY), true);
                travel(player, c.mouthX + 3.5, c.mouthY, c.mouthZ + 0.5);
                return;
            }
        }
    }

    private static void travel(ServerPlayer player, double x, double y, double z) {
        if (player.isPassenger()) player.stopRiding();
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0, false, false));
        player.teleportTo(x, y, z);
        player.level().playSound(null, x, y, z, SoundEvents.LADDER_STEP, SoundSource.PLAYERS, 1f, 0.8f);
    }

    // ------------------------------------------------------------------ players' records

    /** Bit i = cavern i found. */
    public static final AttachmentType<Integer> FOUND = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT).copyOnDeath().initializer(() -> 0).buildAndRegister(CursedTides.id("caverns_found"));
    /** Bit i = the master of cavern i beaten (at least once). */
    public static final AttachmentType<Integer> BEATEN = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT).copyOnDeath().initializer(() -> 0).buildAndRegister(CursedTides.id("caverns_beaten"));

    public static int beatenCount(ServerPlayer p) {
        return Integer.bitCount(p.getAttachedOrElse(BEATEN, 0));
    }

    // ------------------------------------------------------------------ runtime

    private static final String TAG = "ct_cavern_boss";
    private static final int PACK_COOLDOWN = 20 * 150;
    private static final int BOSS_COOLDOWN = 20 * 60 * 15;

    private static final class State {
        final Map<Cavern.Chamber, Long> packAt = new HashMap<>();
        long bossReady;
        Mob boss;
        ServerBossEvent bar;
    }

    private static final Map<Integer, State> STATES = new HashMap<>();
    private static final Map<UUID, Integer> WHERE = new HashMap<>();

    private static State state(int i) {
        return STATES.computeIfAbsent(i, k -> new State());
    }

    public static void register() {
        FOUND.identifier();
        BEATEN.identifier();
        ServerTickEvents.END_SERVER_TICK.register(Caverns::tick);
        ServerLivingEntityEvents.AFTER_DEATH.register(Caverns::onDeath);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal("cursedtides")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("cavern")
                                .then(Commands.argument("index", IntegerArgumentType.integer(0, SITES.size() - 1))
                                        .executes(ctx -> {
                                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                                            Cavern c = all().get(IntegerArgumentType.getInteger(ctx, "index"));
                                            p.teleportTo(c.mouthX + 3.5, c.mouthY, c.mouthZ + 0.5);
                                            return 1;
                                        })
                                        .then(Commands.literal("arena").executes(ctx -> {
                                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                                            Cavern c = all().get(IntegerArgumentType.getInteger(ctx, "index"));
                                            Cavern.Chamber a = c.arena();
                                            int x = a.cx() + a.rx() / 2;
                                            p.teleportTo(x + 0.5, c.floorAt(a, x, a.cz()), a.cz() + 0.5);
                                            return 1;
                                        }))))));
    }

    private static void tick(MinecraftServer server) {
        if (server.getTickCount() % 20 != 0) return;
        ServerLevel world = server.overworld();
        long now = world.getGameTime();
        List<Cavern> caverns = all();
        if (caverns.isEmpty()) return;
        Map<Integer, List<ServerPlayer>> inside = new HashMap<>();
        for (ServerPlayer p : world.players()) {
            Cavern c = at(p.blockPosition());
            Integer before = WHERE.get(p.getUUID());
            if (c == null) {
                WHERE.remove(p.getUUID());
                for (Cavern m : caverns) {
                    if (p.distanceToSqr(m.mouthX + 0.5, m.mouthY, m.mouthZ + 0.5) < 36) {
                        p.displayClientMessage(Component.literal(m.name + " — use the rope to climb down").withStyle(ChatFormatting.GOLD), true);
                    }
                }
                continue;
            }
            inside.computeIfAbsent(c.index, k -> new ArrayList<>()).add(p);
            if (before == null || before != c.index) {
                WHERE.put(p.getUUID(), c.index);
                enter(p, c);
            }
            if (p.isSpectator() || p.isCreative()) continue;
            Cavern.Chamber ch = c.chamberAt(p.blockPosition());
            if (ch != null) stir(world, c, ch, p, now);
        }
        for (int i = 0; i < caverns.size(); i++) {
            State st = STATES.get(i);
            List<ServerPlayer> here = inside.getOrDefault(i, List.of());
            if (st != null && st.boss != null) tendBoss(st, here);
        }
    }

    private static void enter(ServerPlayer p, Cavern c) {
        Site s = SITES.get(c.index);
        p.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 20));
        p.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("Beneath " + s.island().name()).withStyle(ChatFormatting.GRAY)));
        p.connection.send(new ClientboundSetTitleTextPacket(Component.literal(c.name).withStyle(ChatFormatting.GOLD)));
        int found = p.getAttachedOrElse(FOUND, 0);
        if ((found & (1 << c.index)) == 0) {
            p.setAttached(FOUND, found | (1 << c.index));
            p.displayClientMessage(Component.literal("Grand cavern discovered: " + c.name + " (" + Integer.bitCount(found | (1 << c.index))
                    + "/" + SITES.size() + ")").withStyle(ChatFormatting.GOLD), false);
            PirateData.addXp(p, Weapon.EXPLORATION, 60);
            com.cursedtides.activity.Achievements.grant(p, "spelunker");
        }
    }

    /** Wake the chamber: a pack comes out of the dark, or in the great hall, its master. */
    private static void stir(ServerLevel world, Cavern c, Cavern.Chamber ch, ServerPlayer p, long now) {
        State st = state(c.index);
        if (ch.kind() == Cavern.Kind.ARENA) {
            if (st.boss == null && now >= st.bossReady && c.reachOf(ch, p.getBlockX(), p.getBlockZ()) < 0.8) summonBoss(world, c, ch);
            return;
        }
        if (ch.kind() == Cavern.Kind.ENTRY) return;
        Long last = st.packAt.get(ch);
        if (last != null && now - last < PACK_COOLDOWN) return;
        AABB box = new AABB(ch.cx() - ch.rx(), ch.floor() - 4, ch.cz() - ch.rz(), ch.cx() + ch.rx() + 1, ch.floor() + ch.h(), ch.cz() + ch.rz() + 1);
        if (world.getEntitiesOfClass(Monster.class, box, e -> true).size() >= 4) return;
        st.packAt.put(ch, now);
        Site s = SITES.get(c.index);
        int n = 3 + world.random.nextInt(2) + (s.tier() >= 4 ? 1 : 0) + (ch.kind() == Cavern.Kind.GROTTO ? 1 : 0);
        int spawned = 0;
        for (int k = 0; k < n * 3 && spawned < n; k++) {
            BlockPos at = c.standable(ch, 0.35 + world.random.nextDouble() * 0.5, world.random.nextDouble() * Math.PI * 2);
            if (at == null || at.distSqr(p.blockPosition()) < 49) continue;
            if (!world.getBlockState(at).isAir() || !world.getBlockState(at.above()).isAir()) continue;
            Entity e = c.theme.pack(spawned + (int) (now / 20)).create(world);
            if (!(e instanceof Mob mob)) continue;
            mob.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, world.random.nextFloat() * 360, 0);
            mob.finalizeSpawn(world, world.getCurrentDifficultyAt(at), MobSpawnType.EVENT, null);
            world.addFreshEntity(mob);
            spawned++;
        }
        if (spawned > 0) {
            world.playSound(null, p.blockPosition(), SoundEvents.AMBIENT_CAVE.value(), SoundSource.HOSTILE, 1f, 0.8f);
        }
    }

    private static void summonBoss(ServerLevel world, Cavern c, Cavern.Chamber ch) {
        State st = state(c.index);
        AABB hall = new AABB(ch.cx() - ch.rx(), ch.floor() - 4, ch.cz() - ch.rz(), ch.cx() + ch.rx() + 1, ch.floor() + ch.h(), ch.cz() + ch.rz() + 1);
        // One that was already here (saved with the chunk)? Take it back up.
        List<Mob> old = world.getEntitiesOfClass(Mob.class, hall, e -> e.getTags().contains(TAG + c.index));
        Site s = SITES.get(c.index);
        if (!old.isEmpty()) {
            st.boss = old.get(0);
        } else {
            Entity e = s.boss().get().create(world);
            if (!(e instanceof Mob mob)) return;
            int y = c.floorAt(ch, ch.cx(), ch.cz());
            BlockPos at = new BlockPos(ch.cx(), y, ch.cz());
            mob.moveTo(at.getX() + 0.5, y, at.getZ() + 0.5, 0, 0);
            mob.finalizeSpawn(world, world.getCurrentDifficultyAt(at), MobSpawnType.EVENT, null);
            mob.setCustomName(Component.literal(s.bossName()).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
            mob.setCustomNameVisible(true);
            mob.setPersistenceRequired();
            scale(mob, Attributes.MAX_HEALTH, s.health());
            mob.setHealth(s.health());
            AttributeInstance atk = mob.getAttribute(Attributes.ATTACK_DAMAGE);
            if (atk != null) atk.setBaseValue(atk.getBaseValue() * (1.3 + s.tier() * 0.15));
            scale(mob, Attributes.KNOCKBACK_RESISTANCE, 0.8);
            if (mob.getType() == EntityType.DROWNED) mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.TRIDENT));
            if (mob.getType() == EntityType.WITHER_SKELETON) mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
            if (mob.getType() == EntityType.VINDICATOR) mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
            for (EquipmentSlot slot : EquipmentSlot.values()) mob.setDropChance(slot, 0f);
            mob.addTag(TAG);
            mob.addTag(TAG + c.index);
            world.addFreshEntity(mob);
            // Its retinue
            for (int k = 0; k < 2 + s.tier() / 2; k++) {
                BlockPos g = c.standable(ch, 0.3 + world.random.nextDouble() * 0.3, Math.PI * 2 * k / 4.0);
                if (g == null) continue;
                Entity ge = c.theme.pack(k).create(world);
                if (!(ge instanceof Mob guard)) continue;
                guard.moveTo(g.getX() + 0.5, g.getY(), g.getZ() + 0.5, 0, 0);
                guard.finalizeSpawn(world, world.getCurrentDifficultyAt(g), MobSpawnType.EVENT, null);
                world.addFreshEntity(guard);
            }
            for (ServerPlayer p : world.getPlayers(p -> hall.contains(p.position()))) {
                p.connection.send(new ClientboundSetTitlesAnimationPacket(5, 50, 15));
                p.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("stirs in the deep").withStyle(ChatFormatting.GRAY)));
                p.connection.send(new ClientboundSetTitleTextPacket(Component.literal(s.bossName()).withStyle(ChatFormatting.DARK_RED)));
            }
            world.playSound(null, at, SoundEvents.WARDEN_EMERGE, SoundSource.HOSTILE, 2f, 0.7f);
        }
        st.bar = new ServerBossEvent(Component.literal(s.bossName()), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);
        st.bar.setCreateWorldFog(true);
        st.bar.setDarkenScreen(true);
    }

    private static void scale(Mob mob, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr, double value) {
        AttributeInstance inst = mob.getAttribute(attr);
        if (inst != null) inst.setBaseValue(value);
    }

    private static void tendBoss(State st, List<ServerPlayer> here) {
        if (st.boss.isRemoved() || !st.boss.isAlive()) {
            // Killed (onDeath handles it) or unloaded with the chunk: either way, stop tracking it here.
            if (st.bar != null) st.bar.removeAllPlayers();
            st.bar = null;
            st.boss = null;
            return;
        }
        if (st.bar == null) return;
        st.bar.setProgress(Math.max(0f, st.boss.getHealth() / st.boss.getMaxHealth()));
        for (ServerPlayer p : List.copyOf(st.bar.getPlayers())) if (!here.contains(p)) st.bar.removePlayer(p);
        for (ServerPlayer p : here) {
            if (p.distanceToSqr(st.boss) < 48 * 48) st.bar.addPlayer(p);
            else st.bar.removePlayer(p);
        }
    }

    private static void onDeath(LivingEntity entity, DamageSource source) {
        if (!entity.getTags().contains(TAG) || !(entity.level() instanceof ServerLevel world)) return;
        int index = -1;
        for (int i = 0; i < SITES.size(); i++) if (entity.getTags().contains(TAG + i)) index = i;
        if (index < 0) return;
        Cavern c = all().get(index);
        Site s = SITES.get(index);
        State st = state(index);
        st.bossReady = world.getGameTime() + BOSS_COOLDOWN;
        if (st.bar != null) st.bar.removeAllPlayers();
        st.bar = null;
        st.boss = null;
        world.playSound(null, entity.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1f, 0.9f);
        for (ServerPlayer p : world.getPlayers(p -> c.contains(p.blockPosition()) && p.distanceToSqr(entity) < 64 * 64)) {
            p.displayClientMessage(Component.literal(s.bossName() + " is beaten! The hoard of " + c.name + " is yours.").withStyle(ChatFormatting.GOLD), false);
            com.cursedtides.trade.Wallet.give(p, (20 + s.tier() * 15) * com.cursedtides.trade.Wallet.SILVER_PER_DOUBLOON);
            PirateData.addXp(p, Weapon.EXPLORATION, 80 + s.tier() * 40);
            ItemStack arm = com.cursedtides.combat.GearRarity.randomWeapon(world, p.getRandom(), s.tier());
            if (com.cursedtides.combat.GearRarity.isLegendary(arm)) com.cursedtides.activity.Achievements.grant(p, "legendary_find");
            if (!p.getInventory().add(arm)) p.drop(arm, false);
            if (p.getRandom().nextInt(4) < s.tier() - 1) {
                ItemStack trinket = new ItemStack(com.cursedtides.appearance.Accessories.random(p.getRandom(), true));
                if (!p.getInventory().add(trinket)) p.drop(trinket, false);
            }
            int beaten = p.getAttachedOrElse(BEATEN, 0) | (1 << index);
            p.setAttached(BEATEN, beaten);
            com.cursedtides.activity.Achievements.grant(p, "cavern_master");
            if (Integer.bitCount(beaten) >= SITES.size()) com.cursedtides.activity.Achievements.grant(p, "deep_delver");
            com.cursedtides.activity.Challenges.progress(p, "vault", 1);
        }
    }
}
