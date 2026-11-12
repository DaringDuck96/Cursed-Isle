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
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

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
    private static int[] findMouth(Archipelago.Island is) {
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
                for (int yy = y; yy <= Math.max(y, ground) + 3; yy++) c.set(x, yy, z, Blocks.AIR.getDefaultState());
                for (int yy = Math.min(y, ground) - 1; yy < y; yy++) c.set(x, yy, z, cv.theme.wall(x, yy, z));
                c.set(x, y - 1, z, d < 4.5 ? cv.theme.floor(x, y - 1, z) : Blocks.COARSE_DIRT.getDefaultState());
                boolean opening = dx >= 2 && Math.abs(dz) <= 1;
                if (d >= 3.2 && d <= 4.6 && !opening) {
                    int h = 1 + (int) (com.cursedtides.world.Noise.rand(x, z, 4321) * 3);
                    for (int k = 0; k < h; k++) c.set(x, y + k, z, cv.theme.wall(x, y + k, z));
                }
                if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {
                    for (int yy = y - 6; yy < y; yy++) c.set(x, yy, z, Blocks.CAVE_AIR.getDefaultState());
                    c.set(x, y - 7, z, Blocks.BLACK_CONCRETE_POWDER.getDefaultState());
                }
            }
        }
        // The beam and its posts
        for (int dx = -2; dx <= 2; dx += 4) {
            if (!c.contains(mx + dx, mz)) continue;
            c.fill(mx + dx, y, mz, mx + dx, y + 2, mz, Blocks.SPRUCE_FENCE.getDefaultState());
        }
        BlockState beam = Blocks.STRIPPED_SPRUCE_LOG.getDefaultState().with(net.minecraft.block.PillarBlock.AXIS, Direction.Axis.X);
        for (int dx = -2; dx <= 2; dx++) if (c.contains(mx + dx, mz)) c.set(mx + dx, y + 3, mz, beam);
        if (c.contains(mx, mz)) {
            for (int yy = y - 6; yy <= y + 2; yy++) c.set(mx, yy, mz, Cavern.CaveRopeHolder.rope());
        }
        if (c.contains(mx + 2, mz + 1)) c.set(mx + 2, y + 3, mz + 1,
                Blocks.LANTERN.getDefaultState().with(net.minecraft.block.LanternBlock.HANGING, false));
        if (c.contains(mx - 2, mz - 1)) c.set(mx - 2, y + 3, mz - 1, Blocks.LANTERN.getDefaultState());
    }

    // ------------------------------------------------------------------ the rope

    public static Cavern at(BlockPos p) {
        for (Cavern c : all()) if (c.contains(p)) return c;
        return null;
    }

    public static void climb(ServerPlayerEntity player, BlockPos ropePos) {
        for (Cavern c : all()) {
            if (ropePos.getY() > 20 && Math.abs(ropePos.getX() - c.mouthX) <= 3 && Math.abs(ropePos.getZ() - c.mouthZ) <= 3) {
                BlockPos land = c.ropeLanding();
                player.sendMessage(Text.literal("You climb down into the dark...").formatted(Formatting.GRAY), true);
                travel(player, land.getX() + 1.5, land.getY(), land.getZ() + 0.5);
                return;
            }
            if (c.contains(ropePos)) {
                player.sendMessage(Text.literal("You haul yourself back up to daylight.").formatted(Formatting.GRAY), true);
                travel(player, c.mouthX + 3.5, c.mouthY, c.mouthZ + 0.5);
                return;
            }
        }
    }

    private static void travel(ServerPlayerEntity player, double x, double y, double z) {
        if (player.hasVehicle()) player.stopRiding();
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 30, 0, false, false));
        player.requestTeleport(x, y, z);
        player.getWorld().playSound(null, x, y, z, SoundEvents.BLOCK_LADDER_STEP, SoundCategory.PLAYERS, 1f, 0.8f);
    }

    // ------------------------------------------------------------------ players' records

    /** Bit i = cavern i found. */
    public static final AttachmentType<Integer> FOUND = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT).copyOnDeath().initializer(() -> 0).buildAndRegister(CursedTides.id("caverns_found"));
    /** Bit i = the master of cavern i beaten (at least once). */
    public static final AttachmentType<Integer> BEATEN = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT).copyOnDeath().initializer(() -> 0).buildAndRegister(CursedTides.id("caverns_beaten"));

    public static int beatenCount(ServerPlayerEntity p) {
        return Integer.bitCount(p.getAttachedOrElse(BEATEN, 0));
    }

    // ------------------------------------------------------------------ runtime

    private static final String TAG = "ct_cavern_boss";
    private static final int PACK_COOLDOWN = 20 * 150;
    private static final int BOSS_COOLDOWN = 20 * 60 * 15;

    private static final class State {
        final Map<Cavern.Chamber, Long> packAt = new HashMap<>();
        long bossReady;
        MobEntity boss;
        ServerBossBar bar;
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
                dispatcher.register(CommandManager.literal("cursedtides")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("cavern")
                                .then(CommandManager.argument("index", IntegerArgumentType.integer(0, SITES.size() - 1))
                                        .executes(ctx -> {
                                            ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                                            Cavern c = all().get(IntegerArgumentType.getInteger(ctx, "index"));
                                            p.requestTeleport(c.mouthX + 3.5, c.mouthY, c.mouthZ + 0.5);
                                            return 1;
                                        })
                                        .then(CommandManager.literal("arena").executes(ctx -> {
                                            ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                                            Cavern c = all().get(IntegerArgumentType.getInteger(ctx, "index"));
                                            Cavern.Chamber a = c.arena();
                                            int x = a.cx() + a.rx() / 2;
                                            p.requestTeleport(x + 0.5, c.floorAt(a, x, a.cz()), a.cz() + 0.5);
                                            return 1;
                                        }))))));
    }

    private static void tick(MinecraftServer server) {
        if (server.getTicks() % 20 != 0) return;
        ServerWorld world = server.getOverworld();
        long now = world.getTime();
        List<Cavern> caverns = all();
        if (caverns.isEmpty()) return;
        Map<Integer, List<ServerPlayerEntity>> inside = new HashMap<>();
        for (ServerPlayerEntity p : world.getPlayers()) {
            Cavern c = at(p.getBlockPos());
            Integer before = WHERE.get(p.getUuid());
            if (c == null) {
                WHERE.remove(p.getUuid());
                for (Cavern m : caverns) {
                    if (p.squaredDistanceTo(m.mouthX + 0.5, m.mouthY, m.mouthZ + 0.5) < 36) {
                        p.sendMessage(Text.literal(m.name + " — use the rope to climb down").formatted(Formatting.GOLD), true);
                    }
                }
                continue;
            }
            inside.computeIfAbsent(c.index, k -> new ArrayList<>()).add(p);
            if (before == null || before != c.index) {
                WHERE.put(p.getUuid(), c.index);
                enter(p, c);
            }
            if (p.isSpectator() || p.isCreative()) continue;
            Cavern.Chamber ch = c.chamberAt(p.getBlockPos());
            if (ch != null) stir(world, c, ch, p, now);
        }
        for (int i = 0; i < caverns.size(); i++) {
            State st = STATES.get(i);
            List<ServerPlayerEntity> here = inside.getOrDefault(i, List.of());
            if (st != null && st.boss != null) tendBoss(st, here);
        }
    }

    private static void enter(ServerPlayerEntity p, Cavern c) {
        Site s = SITES.get(c.index);
        p.networkHandler.sendPacket(new TitleFadeS2CPacket(10, 60, 20));
        p.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal("Beneath " + s.island().name()).formatted(Formatting.GRAY)));
        p.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(c.name).formatted(Formatting.GOLD)));
        int found = p.getAttachedOrElse(FOUND, 0);
        if ((found & (1 << c.index)) == 0) {
            p.setAttached(FOUND, found | (1 << c.index));
            p.sendMessage(Text.literal("Grand cavern discovered: " + c.name + " (" + Integer.bitCount(found | (1 << c.index))
                    + "/" + SITES.size() + ")").formatted(Formatting.GOLD), false);
            PirateData.addXp(p, Weapon.EXPLORATION, 60);
            com.cursedtides.activity.Achievements.grant(p, "spelunker");
        }
    }

    /** Wake the chamber: a pack comes out of the dark, or in the great hall, its master. */
    private static void stir(ServerWorld world, Cavern c, Cavern.Chamber ch, ServerPlayerEntity p, long now) {
        State st = state(c.index);
        if (ch.kind() == Cavern.Kind.ARENA) {
            if (st.boss == null && now >= st.bossReady && c.reachOf(ch, p.getBlockX(), p.getBlockZ()) < 0.8) summonBoss(world, c, ch);
            return;
        }
        if (ch.kind() == Cavern.Kind.ENTRY) return;
        Long last = st.packAt.get(ch);
        if (last != null && now - last < PACK_COOLDOWN) return;
        Box box = new Box(ch.cx() - ch.rx(), ch.floor() - 4, ch.cz() - ch.rz(), ch.cx() + ch.rx() + 1, ch.floor() + ch.h(), ch.cz() + ch.rz() + 1);
        if (world.getEntitiesByClass(HostileEntity.class, box, e -> true).size() >= 4) return;
        st.packAt.put(ch, now);
        Site s = SITES.get(c.index);
        int n = 3 + world.random.nextInt(2) + (s.tier() >= 4 ? 1 : 0) + (ch.kind() == Cavern.Kind.GROTTO ? 1 : 0);
        int spawned = 0;
        for (int k = 0; k < n * 3 && spawned < n; k++) {
            BlockPos at = c.standable(ch, 0.35 + world.random.nextDouble() * 0.5, world.random.nextDouble() * Math.PI * 2);
            if (at == null || at.getSquaredDistance(p.getBlockPos()) < 49) continue;
            if (!world.getBlockState(at).isAir() || !world.getBlockState(at.up()).isAir()) continue;
            Entity e = c.theme.pack(spawned + (int) (now / 20)).create(world);
            if (!(e instanceof MobEntity mob)) continue;
            mob.refreshPositionAndAngles(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, world.random.nextFloat() * 360, 0);
            mob.initialize(world, world.getLocalDifficulty(at), SpawnReason.EVENT, null);
            world.spawnEntity(mob);
            spawned++;
        }
        if (spawned > 0) {
            world.playSound(null, p.getBlockPos(), SoundEvents.AMBIENT_CAVE.value(), SoundCategory.HOSTILE, 1f, 0.8f);
        }
    }

    private static void summonBoss(ServerWorld world, Cavern c, Cavern.Chamber ch) {
        State st = state(c.index);
        Box hall = new Box(ch.cx() - ch.rx(), ch.floor() - 4, ch.cz() - ch.rz(), ch.cx() + ch.rx() + 1, ch.floor() + ch.h(), ch.cz() + ch.rz() + 1);
        // One that was already here (saved with the chunk)? Take it back up.
        List<MobEntity> old = world.getEntitiesByClass(MobEntity.class, hall, e -> e.getCommandTags().contains(TAG + c.index));
        Site s = SITES.get(c.index);
        if (!old.isEmpty()) {
            st.boss = old.get(0);
        } else {
            Entity e = s.boss().get().create(world);
            if (!(e instanceof MobEntity mob)) return;
            int y = c.floorAt(ch, ch.cx(), ch.cz());
            BlockPos at = new BlockPos(ch.cx(), y, ch.cz());
            mob.refreshPositionAndAngles(at.getX() + 0.5, y, at.getZ() + 0.5, 0, 0);
            mob.initialize(world, world.getLocalDifficulty(at), SpawnReason.EVENT, null);
            mob.setCustomName(Text.literal(s.bossName()).formatted(Formatting.DARK_RED, Formatting.BOLD));
            mob.setCustomNameVisible(true);
            mob.setPersistent();
            scale(mob, EntityAttributes.GENERIC_MAX_HEALTH, s.health());
            mob.setHealth(s.health());
            EntityAttributeInstance atk = mob.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            if (atk != null) atk.setBaseValue(atk.getBaseValue() * (1.3 + s.tier() * 0.15));
            scale(mob, EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.8);
            if (mob.getType() == EntityType.DROWNED) mob.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.TRIDENT));
            if (mob.getType() == EntityType.WITHER_SKELETON) mob.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
            if (mob.getType() == EntityType.VINDICATOR) mob.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
            for (EquipmentSlot slot : EquipmentSlot.values()) mob.setEquipmentDropChance(slot, 0f);
            mob.addCommandTag(TAG);
            mob.addCommandTag(TAG + c.index);
            world.spawnEntity(mob);
            // Its retinue
            for (int k = 0; k < 2 + s.tier() / 2; k++) {
                BlockPos g = c.standable(ch, 0.3 + world.random.nextDouble() * 0.3, Math.PI * 2 * k / 4.0);
                if (g == null) continue;
                Entity ge = c.theme.pack(k).create(world);
                if (!(ge instanceof MobEntity guard)) continue;
                guard.refreshPositionAndAngles(g.getX() + 0.5, g.getY(), g.getZ() + 0.5, 0, 0);
                guard.initialize(world, world.getLocalDifficulty(g), SpawnReason.EVENT, null);
                world.spawnEntity(guard);
            }
            for (ServerPlayerEntity p : world.getPlayers(p -> hall.contains(p.getPos()))) {
                p.networkHandler.sendPacket(new TitleFadeS2CPacket(5, 50, 15));
                p.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal("stirs in the deep").formatted(Formatting.GRAY)));
                p.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(s.bossName()).formatted(Formatting.DARK_RED)));
            }
            world.playSound(null, at, SoundEvents.ENTITY_WARDEN_EMERGE, SoundCategory.HOSTILE, 2f, 0.7f);
        }
        st.bar = new ServerBossBar(Text.literal(s.bossName()), BossBar.Color.RED, BossBar.Style.NOTCHED_10);
        st.bar.setThickenFog(true);
        st.bar.setDarkenSky(true);
    }

    private static void scale(MobEntity mob, net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attr, double value) {
        EntityAttributeInstance inst = mob.getAttributeInstance(attr);
        if (inst != null) inst.setBaseValue(value);
    }

    private static void tendBoss(State st, List<ServerPlayerEntity> here) {
        if (st.boss.isRemoved() || !st.boss.isAlive()) {
            // Killed (onDeath handles it) or unloaded with the chunk: either way, stop tracking it here.
            if (st.bar != null) st.bar.clearPlayers();
            st.bar = null;
            st.boss = null;
            return;
        }
        if (st.bar == null) return;
        st.bar.setPercent(Math.max(0f, st.boss.getHealth() / st.boss.getMaxHealth()));
        for (ServerPlayerEntity p : List.copyOf(st.bar.getPlayers())) if (!here.contains(p)) st.bar.removePlayer(p);
        for (ServerPlayerEntity p : here) {
            if (p.squaredDistanceTo(st.boss) < 48 * 48) st.bar.addPlayer(p);
            else st.bar.removePlayer(p);
        }
    }

    private static void onDeath(LivingEntity entity, DamageSource source) {
        if (!entity.getCommandTags().contains(TAG) || !(entity.getWorld() instanceof ServerWorld world)) return;
        int index = -1;
        for (int i = 0; i < SITES.size(); i++) if (entity.getCommandTags().contains(TAG + i)) index = i;
        if (index < 0) return;
        Cavern c = all().get(index);
        Site s = SITES.get(index);
        State st = state(index);
        st.bossReady = world.getTime() + BOSS_COOLDOWN;
        if (st.bar != null) st.bar.clearPlayers();
        st.bar = null;
        st.boss = null;
        world.playSound(null, entity.getBlockPos(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1f, 0.9f);
        for (ServerPlayerEntity p : world.getPlayers(p -> c.contains(p.getBlockPos()) && p.squaredDistanceTo(entity) < 64 * 64)) {
            p.sendMessage(Text.literal(s.bossName() + " is beaten! The hoard of " + c.name + " is yours.").formatted(Formatting.GOLD), false);
            com.cursedtides.trade.Wallet.give(p, (20 + s.tier() * 15) * com.cursedtides.trade.Wallet.SILVER_PER_DOUBLOON);
            PirateData.addXp(p, Weapon.EXPLORATION, 80 + s.tier() * 40);
            ItemStack arm = com.cursedtides.combat.GearRarity.randomWeapon(world, p.getRandom(), s.tier());
            if (com.cursedtides.combat.GearRarity.isLegendary(arm)) com.cursedtides.activity.Achievements.grant(p, "legendary_find");
            if (!p.getInventory().insertStack(arm)) p.dropItem(arm, false);
            if (p.getRandom().nextInt(4) < s.tier() - 1) {
                ItemStack trinket = new ItemStack(com.cursedtides.appearance.Accessories.random(p.getRandom(), true));
                if (!p.getInventory().insertStack(trinket)) p.dropItem(trinket, false);
            }
            int beaten = p.getAttachedOrElse(BEATEN, 0) | (1 << index);
            p.setAttached(BEATEN, beaten);
            com.cursedtides.activity.Achievements.grant(p, "cavern_master");
            if (Integer.bitCount(beaten) >= SITES.size()) com.cursedtides.activity.Achievements.grant(p, "deep_delver");
            com.cursedtides.activity.Challenges.progress(p, "vault", 1);
        }
    }
}
