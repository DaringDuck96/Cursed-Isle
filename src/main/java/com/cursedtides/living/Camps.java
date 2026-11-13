package com.cursedtides.living;

import net.minecraft.world.entity.EntityTypes;
import com.cursedtides.combat.Levels;
import com.cursedtides.entity.ModEntities;
import com.cursedtides.wildlife.Wildlife;
import com.cursedtides.world.Archipelago;
import com.cursedtides.world.Builds;
import com.cursedtides.world.ModWorld;
import com.cursedtides.world.Noise;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
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
        /** A named elite who keeps one of the wild areas (null for an ordinary camp). */
        String elite;

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
    private static final Map<UUID, String> WHERE = new HashMap<>();

    private static List<Supplier<EntityType<?>>> kinds(Archipelago.Style style) {
        return switch (style) {
            case SANDY -> List.of(() -> ModEntities.CURSED_PIRATE, () -> Wildlife.CRAB);
            case TOWN -> List.of(() -> ModEntities.CURSED_PIRATE, () -> EntityTypes.PILLAGER);
            case REEF, KEYS -> List.of(() -> ModEntities.CURSED_PIRATE, () -> EntityTypes.DROWNED);
            case VOLCANIC -> List.of(() -> ModEntities.CURSED_PIRATE, () -> ModEntities.CURSED_PIRATE, () -> EntityTypes.MAGMA_CUBE);
            case CURSED -> List.of(() -> ModEntities.CURSED_PIRATE, () -> ModEntities.CURSED_PIRATE, () -> EntityTypes.STRAY);
            case FORTRESS -> List.of(() -> EntityTypes.PILLAGER, () -> EntityTypes.VINDICATOR, () -> ModEntities.CURSED_PIRATE);
            case BAYOU -> List.of(() -> Wildlife.CROCODILE, () -> Wildlife.SNAKE, () -> ModEntities.CURSED_PIRATE);
            case JUNGLE -> List.of(() -> Wildlife.JAGUAR, () -> Wildlife.SNAKE, () -> EntityTypes.SPIDER);
            case CLIFFS, CRAGS -> List.of(() -> ModEntities.CURSED_PIRATE, () -> EntityTypes.SPIDER);
            case DESERT -> List.of(() -> ModEntities.CURSED_PIRATE, () -> EntityTypes.HUSK);
            case ATOLL -> List.of(() -> ModEntities.CURSED_PIRATE, () -> EntityTypes.DROWNED);
            case ANCIENT -> List.of(() -> ModEntities.CURSED_PIRATE, () -> ModEntities.CURSED_PIRATE, () -> EntityTypes.VINDICATOR);
        };
    }

    /** Laid out once, from the islands and what's built on them. */
    static synchronized List<Camp> all() {
        if (camps != null) return camps;
        Builds.groundY(0, 0); // make sure the buildings are known
        List<Camp> out = new ArrayList<>();
        for (Archipelago.Island is : Archipelago.ISLANDS) {
            int[] band = Levels.band(is);
            double area = Math.PI * is.radius() * is.radius() + (is.hasLobe() ? Math.PI * is.lobeR() * is.lobeR() : 0);
            int want = Math.max(3, (int) Math.round(area / 2200));
            int made = 0;
            for (int k = 0; k < want * 8 && made < want; k++) {
                double a = Noise.rand(is.seed(), k, 61) * Math.PI * 2;
                boolean wilds = is.hasLobe() && Noise.rand(is.seed(), k, 66) < 0.85;
                int ccx = wilds ? is.lobeX() : is.cx(), ccz = wilds ? is.lobeZ() : is.cz();
                double r = (wilds ? is.lobeR() : is.radius()) * (0.1 + Noise.rand(is.seed(), k, 62) * 0.75);
                int x = ccx + (int) (Math.cos(a) * r), z = ccz + (int) (Math.sin(a) * r);
                if (!wild(x, z)) continue;
                boolean crowded = false;
                for (Camp c : out) if ((c.x - x) * (c.x - x) + (c.z - z) * (c.z - z) < 22 * 22) crowded = true;
                if (crowded) continue;
                int[] here = Levels.bandAt(new BlockPos(x, 64, z));
                int level = here[0] + (int) (Noise.rand(x, z, 63) * (here[1] - here[0] + 1));
                int size = 2 + (int) (Noise.rand(x, z, 64) * 3);
                List<Supplier<EntityType<?>>> all = kinds(is.style());
                // Each camp is mostly one kind of foe, with now and then another mixed in
                int main = (int) (Noise.rand(x, z, 65) * all.size());
                List<Supplier<EntityType<?>>> mine = List.of(all.get(main), all.get(main), all.get((main + 1) % all.size()));
                out.add(new Camp(is, x, z, size, Math.min(50, level), mine));
                made++;
            }
        }
        // Each named wild area is kept by an elite
        String[] titles = {"Gristle", "Old Rot", "the Ferryman", "Mother Maggot", "Rattlejaw", "Barnacle Bill", "the Hangman",
                "Sallow Jack", "Brine-Eye", "Captain Cinders", "the Weeping Bosun", "Gutterbones", "Pegleg Moss", "Crawling Kate",
                "the Tallyman", "Rustheart", "Hookfinger", "the Drowned Abbot", "Blackwater Bess", "Grimsby the Gaunt"};
        int n = 0;
        for (com.cursedtides.world.Areas.Area a : com.cursedtides.world.Areas.ALL) {
            int[] b = Levels.bandAt(new BlockPos(a.x(), 64, a.z()));
            List<Supplier<EntityType<?>>> all = kinds(a.island().style());
            Camp c = new Camp(a.island(), a.x() + 7, a.z() + 4, 1, Math.min(50, b[1] + 2), List.of(all.get(n % all.size())));
            c.elite = titles[n % titles.length];
            out.add(c);
            n++;
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
            if (e.entityTags().contains(TAG) && !OWNER.containsKey(e.getUUID())) e.discard();
        });
    }

    private static void tick(MinecraftServer server) {
        if (server.getTickCount() % 20 != 7 || !ModWorld.isArchipelago()) return;
        ServerLevel world = server.overworld();
        List<ServerPlayer> players = world.getPlayers(p -> !p.isSpectator());
        if (players.isEmpty() && OWNER.isEmpty()) return;
        long now = world.getGameTime();
        for (ServerPlayer p : players) {
            com.cursedtides.world.Areas.Area a = com.cursedtides.world.Areas.at(p.getX(), p.getZ());
            String name = a == null ? "" : a.name();
            String before = WHERE.put(p.getUUID(), name);
            if (a != null && !name.equals(before)) {
                int[] b = Levels.bandAt(p.blockPosition());
                String title = Character.toUpperCase(name.charAt(0)) + name.substring(1);
                p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(10, 50, 20));
                p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(
                        net.minecraft.network.chat.Component.literal(a.island().name() + "  ·  Lv " + b[0] + "-" + b[1]).withStyle(net.minecraft.ChatFormatting.GRAY)));
                p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(
                        net.minecraft.network.chat.Component.literal(title).withStyle(net.minecraft.ChatFormatting.GOLD)));
            }
        }
        for (Camp c : all()) {
            double nearest = Double.MAX_VALUE;
            for (ServerPlayer p : players) {
                double dx = p.getX() - c.x, dz = p.getZ() - c.z;
                nearest = Math.min(nearest, dx * dx + dz * dz);
            }
            // Forget the fallen, and bring strays home
            for (int i = c.members.size() - 1; i >= 0; i--) {
                Entity e = world.getEntity(c.members.get(i));
                if (e == null || !e.isAlive()) {
                    OWNER.remove(c.members.remove(i));
                    if (c.nextSpawn < now) c.nextSpawn = now + (c.elite != null ? 20 * 300 : 20 * (25 + world.getRandom().nextInt(25)));
                    continue;
                }
                if (nearest > 112 * 112) {
                    e.discard();
                    OWNER.remove(c.members.remove(i));
                    continue;
                }
                if (e instanceof Mob mob) leash(mob, c);
            }
            if (nearest > 64 * 64 || nearest < 10 * 10) continue;
            if (c.members.size() < c.size && now >= c.nextSpawn) {
                if (spawn(world, c)) c.nextSpawn = c.members.size() < c.size ? now + 20 : now;
            }
        }
    }

    private static void leash(Mob mob, Camp c) {
        double dx = mob.getX() - c.x, dz = mob.getZ() - c.z;
        double d2 = dx * dx + dz * dz;
        if (d2 > 48 * 48) {
            // Too far: it loses heart and is back at camp, whole
            int y = Builds.groundY(c.x, c.z);
            mob.setTarget(null);
            mob.snapTo(c.x + 0.5, y, c.z + 0.5, mob.getYRot(), 0);
            mob.setHealth(mob.getMaxHealth());
        } else if (d2 > 28 * 28 && mob.getTarget() == null) {
            mob.getNavigation().moveTo(c.x + 0.5, Builds.groundY(c.x, c.z), c.z + 0.5, 1.0);
        } else if (d2 > 12 * 12 && mob.getTarget() == null && mob.getNavigation().isDone()) {
            mob.getNavigation().moveTo(c.x + 0.5, Builds.groundY(c.x, c.z), c.z + 0.5, 0.8);
        }
    }

    private static boolean spawn(ServerLevel world, Camp c) {
        for (int tries = 0; tries < 6; tries++) {
            int x = c.x + world.getRandom().nextInt(9) - 4, z = c.z + world.getRandom().nextInt(9) - 4;
            BlockPos at = new BlockPos(x, Builds.groundY(x, z), z);
            if (!world.hasChunk(at.getX() >> 4, at.getZ() >> 4)) return false;
            if (!world.getBlockState(at).isAir() || !world.getBlockState(at.above()).isAir() || world.getBlockState(at.below()).isAir()) continue;
            Entity e = c.kinds.get(world.getRandom().nextInt(c.kinds.size())).get().create(world, EntitySpawnReason.EVENT);
            if (!(e instanceof Mob mob)) return false;
            mob.snapTo(x + 0.5, at.getY(), z + 0.5, world.getRandom().nextFloat() * 360, 0);
            mob.finalizeSpawn(world, world.getCurrentDifficultyAt(at), EntitySpawnReason.EVENT, null);
            mob.addTag(TAG);
            if (c.elite != null) {
                mob.setCustomName(net.minecraft.network.chat.Component.literal(c.elite).withStyle(net.minecraft.ChatFormatting.DARK_RED, net.minecraft.ChatFormatting.BOLD));
                Levels.apply(mob, c.level);
                var hp = mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
                if (hp != null) {
                    hp.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(com.cursedtides.CursedTides.id("elite"),
                            3 * Levels.healthFactor(c.level) - 1, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                    mob.setHealth(mob.getMaxHealth());
                }
                mob.setCustomNameVisible(true);
            } else {
                Levels.apply(mob, c.level - 1 + world.getRandom().nextInt(3));
            }
            OWNER.put(mob.getUUID(), c);
            c.members.add(mob.getUUID());
            world.addFreshEntity(mob);
            return true;
        }
        return false;
    }
}
