package com.cursedtides.living;

import com.cursedtides.combat.Levels;
import com.cursedtides.entity.ModEntities;
import com.cursedtides.wildlife.Wildlife;
import com.cursedtides.world.Archipelago;
import com.cursedtides.world.Builds;
import com.cursedtides.world.ModWorld;
import com.cursedtides.world.Noise;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Where the enemies live. Instead of things spawning at random in the dark, every island has fixed camps out in
 * the wilds (never in town) with a few foes of the island's level each. Kill them and they come back to the same
 * spot after a short while; lure them too far and they give up and walk home. Camps only wake while a pirate is
 * near, and empty out when everyone has gone.
 */
public final class Camps {
    private Camps() {}

    public static final String TAG = "ct_camp";

    public static final class Camp {
        final int x, z, size, level;
        final Archipelago.Island island;
        final List<Supplier<EntityType<?>>> kinds;
        final List<UUID> members = new ArrayList<>();
        long nextSpawn;

        Camp(Archipelago.Island island, int x, int z, int size, int level, List<Supplier<EntityType<?>>> kinds) {
            this.island = island;
            this.x = x;
            this.z = z;
            this.size = size;
            this.level = level;
            this.kinds = kinds;
        }
    }

    private static List<Camp> camps;
    private static final Map<UUID, Camp> OWNER = new HashMap<>();

    private static List<Supplier<EntityType<?>>> kinds(Archipelago.Style style) {
        return switch (style) {
            case SANDY -> List.of(() -> ModEntities.CURSED_PIRATE, () -> EntityType.ZOMBIE);
            case TOWN -> List.of(() -> ModEntities.CURSED_PIRATE, () -> EntityType.PILLAGER);
            case REEF, KEYS -> List.of(() -> EntityType.DROWNED, () -> ModEntities.CURSED_PIRATE);
            case VOLCANIC -> List.of(() -> EntityType.HUSK, () -> EntityType.SKELETON, () -> EntityType.MAGMA_CUBE);
            case CURSED -> List.of(() -> EntityType.SKELETON, () -> EntityType.ZOMBIE, () -> ModEntities.CURSED_PIRATE, () -> EntityType.STRAY);
            case FORTRESS -> List.of(() -> EntityType.PILLAGER, () -> EntityType.VINDICATOR, () -> ModEntities.CURSED_PIRATE);
            case BAYOU -> List.of(() -> Wildlife.CROCODILE, () -> Wildlife.SNAKE, () -> EntityType.DROWNED);
            case JUNGLE -> List.of(() -> Wildlife.JAGUAR, () -> Wildlife.SNAKE, () -> EntityType.SPIDER);
            case CLIFFS, CRAGS -> List.of(() -> EntityType.SPIDER, () -> EntityType.SKELETON, () -> ModEntities.CURSED_PIRATE);
            case DESERT -> List.of(() -> EntityType.HUSK, () -> ModEntities.CURSED_PIRATE);
            case ATOLL -> List.of(() -> EntityType.STRAY, () -> EntityType.DROWNED);
            case ANCIENT -> List.of(() -> EntityType.SKELETON, () -> EntityType.STRAY, () -> EntityType.VINDICATOR);
        };
    }

    /** Laid out once, from the islands and what's built on them. */
    static synchronized List<Camp> all() {
        if (camps != null) return camps;
        Builds.groundY(0, 0); // make sure the buildings are known
        List<Camp> out = new ArrayList<>();
        for (Archipelago.Island is : Archipelago.ISLANDS) {
            int[] band = Levels.band(is);
            int want = Math.max(3, is.radius() / 7);
            int made = 0;
            for (int k = 0; k < want * 8 && made < want; k++) {
                double a = Noise.rand(is.seed(), k, 61) * Math.PI * 2;
                double r = is.radius() * (0.2 + Noise.rand(is.seed(), k, 62) * 0.65);
                int x = is.cx() + (int) (Math.cos(a) * r), z = is.cz() + (int) (Math.sin(a) * r);
                if (!wild(x, z)) continue;
                boolean crowded = false;
                for (Camp c : out) if ((c.x - x) * (c.x - x) + (c.z - z) * (c.z - z) < 22 * 22) crowded = true;
                if (crowded) continue;
                int level = band[0] + (int) (Noise.rand(x, z, 63) * (band[1] - band[0] + 1));
                int size = 2 + (int) (Noise.rand(x, z, 64) * 3);
                List<Supplier<EntityType<?>>> all = kinds(is.style());
                // Each camp is mostly one kind of foe, with now and then another mixed in
                int main = (int) (Noise.rand(x, z, 65) * all.size());
                List<Supplier<EntityType<?>>> mine = List.of(all.get(main), all.get(main), all.get((main + 1) % all.size()));
                out.add(new Camp(is, x, z, size, Math.min(50, level), mine));
                made++;
            }
        }
        camps = List.copyOf(out);
        com.cursedtides.CursedTides.LOGGER.info("Cursed Tides: {} enemy camps across the archipelago", camps.size());
        return camps;
    }

    /** Open ground, away from every town, building and dungeon mouth. */
    private static boolean wild(int x, int z) {
        Archipelago.Column col = Archipelago.column(x, z);
        if (!col.isLand() || col.crater() || col.top() < Archipelago.WATER_TOP + 2) return false;
        for (int dx = -14; dx <= 14; dx += 7) {
            for (int dz = -14; dz <= 14; dz += 7) {
                if (Builds.blocksPlants(x + dx, z + dz)) return false;
            }
        }
        return true;
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(Camps::tick);
        // Camp foes left over from before a restart: clear them, the camp will raise fresh ones.
        ServerEntityEvents.ENTITY_LOAD.register((e, world) -> {
            if (e.getCommandTags().contains(TAG) && !OWNER.containsKey(e.getUuid())) e.discard();
        });
    }

    private static void tick(MinecraftServer server) {
        if (server.getTicks() % 20 != 7 || !ModWorld.isArchipelago()) return;
        ServerWorld world = server.getOverworld();
        List<ServerPlayerEntity> players = world.getPlayers(p -> !p.isSpectator());
        if (players.isEmpty() && OWNER.isEmpty()) return;
        long now = world.getTime();
        for (Camp c : all()) {
            double nearest = Double.MAX_VALUE;
            for (ServerPlayerEntity p : players) {
                double dx = p.getX() - c.x, dz = p.getZ() - c.z;
                nearest = Math.min(nearest, dx * dx + dz * dz);
            }
            // Forget the fallen, and bring strays home
            for (int i = c.members.size() - 1; i >= 0; i--) {
                Entity e = world.getEntity(c.members.get(i));
                if (e == null || !e.isAlive()) {
                    OWNER.remove(c.members.remove(i));
                    if (c.nextSpawn < now) c.nextSpawn = now + 20 * (25 + world.random.nextInt(25));
                    continue;
                }
                if (nearest > 112 * 112) {
                    e.discard();
                    OWNER.remove(c.members.remove(i));
                    continue;
                }
                if (e instanceof MobEntity mob) leash(mob, c);
            }
            if (nearest > 64 * 64 || nearest < 10 * 10) continue;
            if (c.members.size() < c.size && now >= c.nextSpawn) {
                if (spawn(world, c)) c.nextSpawn = c.members.size() < c.size ? now + 20 : now;
            }
        }
    }

    private static void leash(MobEntity mob, Camp c) {
        double dx = mob.getX() - c.x, dz = mob.getZ() - c.z;
        double d2 = dx * dx + dz * dz;
        if (d2 > 48 * 48) {
            // Too far: it loses heart and is back at camp, whole
            int y = Builds.groundY(c.x, c.z);
            mob.setTarget(null);
            mob.refreshPositionAndAngles(c.x + 0.5, y, c.z + 0.5, mob.getYaw(), 0);
            mob.setHealth(mob.getMaxHealth());
        } else if (d2 > 28 * 28 && mob.getTarget() == null) {
            mob.getNavigation().startMovingTo(c.x + 0.5, Builds.groundY(c.x, c.z), c.z + 0.5, 1.0);
        } else if (d2 > 12 * 12 && mob.getTarget() == null && mob.getNavigation().isIdle()) {
            mob.getNavigation().startMovingTo(c.x + 0.5, Builds.groundY(c.x, c.z), c.z + 0.5, 0.8);
        }
    }

    private static boolean spawn(ServerWorld world, Camp c) {
        for (int tries = 0; tries < 6; tries++) {
            int x = c.x + world.random.nextInt(9) - 4, z = c.z + world.random.nextInt(9) - 4;
            BlockPos at = new BlockPos(x, Builds.groundY(x, z), z);
            if (!world.isChunkLoaded(at.getX() >> 4, at.getZ() >> 4)) return false;
            if (!world.getBlockState(at).isAir() || !world.getBlockState(at.up()).isAir() || world.getBlockState(at.down()).isAir()) continue;
            Entity e = c.kinds.get(world.random.nextInt(c.kinds.size())).get().create(world);
            if (!(e instanceof MobEntity mob)) return false;
            mob.refreshPositionAndAngles(x + 0.5, at.getY(), z + 0.5, world.random.nextFloat() * 360, 0);
            mob.initialize(world, world.getLocalDifficulty(at), SpawnReason.EVENT, null);
            mob.addCommandTag(TAG);
            Levels.apply(mob, c.level - 1 + world.random.nextInt(3));
            OWNER.put(mob.getUuid(), c);
            c.members.add(mob.getUuid());
            world.spawnEntity(mob);
            return true;
        }
        return false;
    }
}
