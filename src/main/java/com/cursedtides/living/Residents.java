package com.cursedtides.living;

import com.cursedtides.entity.ModEntities;
import com.cursedtides.entity.QuartermasterEntity;
import com.cursedtides.entity.StoryNpcEntity;
import com.cursedtides.entity.StoryNpcEntity.Role;
import com.cursedtides.world.Archipelago;
import com.cursedtides.world.Archipelago.Island;
import com.cursedtides.world.Builds;
import com.cursedtides.world.ModWorld;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.Heightmap;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * People who move into the archipelago after it's made: the arena and range masters, the tavern keeper, the bounty
 * office, land agents, the archivist, the course master and the salvage diver. Each takes up a spot near their
 * island the first time a player comes by, on open ground. Plus the ones who come and go: a peddler who sails in for
 * a while, and the Old Mariner, who is only ever seen on a beach at night.
 */
public final class Residents {
    private Residents() {}

    public static final String TRANSIENT = "cursedtides_transient";
    private static final String EXPIRES = "cursedtides_expires_";

    public record Home(Role role, Island island, int dx, int dz) {}

    public static final List<Home> HOMES = List.of(
            new Home(Role.ARENA_MASTER, Archipelago.SALTMARROW, 34, 26),
            new Home(Role.RANGE_MASTER, Archipelago.SALTMARROW, -30, 24),
            new Home(Role.TAVERN_KEEPER, Archipelago.SALTMARROW, 14, -14),
            new Home(Role.TAVERN_KEEPER, Archipelago.KINGSPORT, 16, -10),
            new Home(Role.BOUNTY_CLERK, Archipelago.SALTMARROW, 4, 32),
            new Home(Role.BOUNTY_CLERK, Archipelago.KINGSPORT, -6, 28),
            new Home(Role.LAND_AGENT, Archipelago.SALTMARROW, -18, -28),
            new Home(Role.LAND_AGENT, Archipelago.PORT_MERIDIAN, 12, 26),
            new Home(Role.ARCHIVIST, Archipelago.KINGSPORT, -24, 18),
            new Home(Role.COURSE_MASTER, Archipelago.STORMBREAK_ISLE, -12, 16),
            new Home(Role.DIVER, Archipelago.CROOKED_REEF, 20, -18),
            new Home(Role.DIVER, Archipelago.SUNKEN_REACH, -16, 14)
    );

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(Residents::tick);
    }

    private static void tick(MinecraftServer server) {
        if (!ModWorld.isArchipelago() || server.getTicks() % 100 != 0) return;
        ServerWorld world = server.getOverworld();
        for (ServerPlayerEntity p : world.getPlayers()) {
            for (Home h : HOMES) {
                int x = h.island().cx() + h.dx(), z = h.island().cz() + h.dz();
                if (p.squaredDistanceTo(x, p.getY(), z) > 72 * 72 || !world.isChunkLoaded(new BlockPos(x, 0, z))) continue;
                Box near = new Box(x - 40, world.getBottomY(), z - 40, x + 40, world.getTopY(), z + 40);
                if (!world.getEntitiesByClass(StoryNpcEntity.class, near, n -> n.getRole() == h.role()).isEmpty()) continue;
                BlockPos at = openGround(world, x, z, 10);
                if (at == null) continue;
                StoryNpcEntity npc = Builds.npc(world, h.role());
                if (npc == null) continue;
                npc.refreshPositionAndAngles(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, world.random.nextFloat() * 360, 0);
                world.spawnEntity(npc);
            }
        }
        if (server.getTicks() % 1200 == 0) expire(world);
        if (server.getTicks() % 6000 == 0) visitors(world);
    }

    /** Somewhere a person could stand: natural ground with two blocks of air above, near the wanted spot. */
    @Nullable
    public static BlockPos openGround(ServerWorld world, int x, int z, int search) {
        for (int r = 0; r <= search; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue;
                    int top = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x + dx, z + dz);
                    BlockPos ground = new BlockPos(x + dx, top - 1, z + dz);
                    BlockState s = world.getBlockState(ground);
                    if (!natural(s) || !world.getFluidState(ground.up()).isEmpty()) continue;
                    if (!world.isAir(ground.up(2))) continue;
                    return ground.up();
                }
            }
        }
        return null;
    }

    private static boolean natural(BlockState s) {
        return s.isOf(Blocks.GRASS_BLOCK) || s.isOf(Blocks.SAND) || s.isOf(Blocks.DIRT) || s.isOf(Blocks.COARSE_DIRT) || s.isOf(Blocks.DIRT_PATH)
                || s.isOf(Blocks.GRAVEL) || s.isOf(Blocks.PODZOL) || s.isOf(Blocks.RED_SAND) || s.isOf(Blocks.STONE) || s.isOf(Blocks.COBBLESTONE)
                || s.isOf(Blocks.MUD) || s.isOf(Blocks.SANDSTONE) || s.isOf(Blocks.ANDESITE) || s.isOf(Blocks.TUFF) || s.isOf(Blocks.MOSS_BLOCK)
                || s.isOf(Blocks.STONE_BRICKS) || s.isOf(Blocks.MOSSY_COBBLESTONE) || s.isOf(Blocks.ROOTED_DIRT) || s.isOf(Blocks.CALCITE);
    }

    public static void markTransient(Entity e, long until) {
        e.addCommandTag(TRANSIENT);
        e.addCommandTag(EXPIRES + until);
    }

    private static void expire(ServerWorld world) {
        long now = world.getTime();
        List<Entity> tagged = new java.util.ArrayList<>();
        for (Entity e : world.iterateEntities()) if (e.getCommandTags().contains(TRANSIENT)) tagged.add(e);
        for (Entity e : tagged) {
            for (String tag : e.getCommandTags()) {
                if (!tag.startsWith(EXPIRES)) continue;
                try {
                    if (Long.parseLong(tag.substring(EXPIRES.length())) < now) {
                        world.spawnParticles(net.minecraft.particle.ParticleTypes.POOF, e.getX(), e.getY() + 1, e.getZ(), 12, 0.3, 0.5, 0.3, 0.02);
                        e.discard();
                    }
                } catch (NumberFormatException ignored) { }
                break;
            }
        }
    }

    /** Every five minutes, a chance of a peddler in a town or the Old Mariner on a beach at night. */
    private static void visitors(ServerWorld world) {
        List<ServerPlayerEntity> players = world.getPlayers(p -> !p.isSpectator());
        if (players.isEmpty()) return;
        ServerPlayerEntity p = players.get(world.random.nextInt(players.size()));
        long t = world.getTimeOfDay() % 24000;
        boolean night = t >= 13000 && t < 23000;
        if (night && world.random.nextFloat() < 0.15f) {
            spawnMariner(world, p);
        } else if (!night && world.random.nextFloat() < 0.3f) {
            spawnPeddler(world, p);
        }
    }

    public static boolean spawnPeddler(ServerWorld world, ServerPlayerEntity p) {
        var port = Prosperity.nearestPort(p.getBlockX(), p.getBlockZ(), 120);
        if (port == null) return false;
        BlockPos at = openGround(world, port.island().cx() + world.random.nextInt(21) - 10, port.island().cz() + world.random.nextInt(21) - 10, 12);
        if (at == null) return false;
        QuartermasterEntity peddler = ModEntities.QUARTERMASTER.create(world);
        if (peddler == null) return false;
        String[] names = {"Jonquil", "Fennick", "Old Saffron", "Hettie Brass", "Mendel", "Quince"};
        peddler.setCustomName(Text.literal(names[world.random.nextInt(names.length)] + ", Wandering Peddler"));
        peddler.setCustomNameVisible(true);
        peddler.refreshPositionAndAngles(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0, 0);
        markTransient(peddler, world.getTime() + 12000);
        world.spawnEntity(peddler);
        world.getServer().getPlayerManager().broadcast(Text.literal("A wandering peddler has set out a blanket of curios at "
                + port.island().name() + ". They'll be gone by nightfall.").formatted(Formatting.YELLOW), false);
        return true;
    }

    public static boolean spawnMariner(ServerWorld world, ServerPlayerEntity p) {
        for (int i = 0; i < 12; i++) {
            double a = world.random.nextDouble() * Math.PI * 2;
            int x = (int) (p.getX() + Math.cos(a) * 24), z = (int) (p.getZ() + Math.sin(a) * 24);
            BlockPos at = openGround(world, x, z, 6);
            if (at == null || !world.getBlockState(at.down()).isOf(Blocks.SAND)) continue;
            StoryNpcEntity m = Builds.npc(world, Role.OLD_MARINER);
            if (m == null) return false;
            m.refreshPositionAndAngles(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0, 0);
            markTransient(m, world.getTime() + 3600);
            world.spawnEntity(m);
            p.sendMessage(Text.literal("Down on the sand, someone is humming an old sea song.").formatted(Formatting.DARK_AQUA, Formatting.ITALIC), false);
            return true;
        }
        return false;
    }
}
