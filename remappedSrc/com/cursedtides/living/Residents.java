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
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
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
            new Home(Role.DIVER, Archipelago.SUNKEN_REACH, -16, 14),
            new Home(Role.GUNNERY_SERGEANT, Archipelago.SALTMARROW, -6, 44),
            new Home(Role.BARBER, Archipelago.SALTMARROW, 22, -26),
            new Home(Role.TATTOOIST, Archipelago.SALTMARROW, 26, -20),
            new Home(Role.BARBER, Archipelago.KINGSPORT, 20, 14),
            new Home(Role.TATTOOIST, Archipelago.KINGSPORT, -14, -18)
    );

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(Residents::tick);
    }

    private static void tick(MinecraftServer server) {
        if (!ModWorld.isArchipelago() || server.getTickCount() % 100 != 0) return;
        ServerLevel world = server.overworld();
        for (ServerPlayer p : world.players()) {
            for (Home h : HOMES) {
                int x = h.island().cx() + h.dx(), z = h.island().cz() + h.dz();
                if (p.distanceToSqr(x, p.getY(), z) > 72 * 72 || !world.hasChunkAt(new BlockPos(x, 0, z))) continue;
                AABB near = new AABB(x - 40, world.getMinBuildHeight(), z - 40, x + 40, world.getMaxBuildHeight(), z + 40);
                if (!world.getEntitiesOfClass(StoryNpcEntity.class, near, n -> n.getRole() == h.role()).isEmpty()) continue;
                BlockPos at = openGround(world, x, z, 10);
                if (at == null) continue;
                StoryNpcEntity npc = Builds.npc(world, h.role());
                if (npc == null) continue;
                npc.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, world.random.nextFloat() * 360, 0);
                world.addFreshEntity(npc);
            }
        }
        if (server.getTickCount() % 1200 == 0) expire(world);
        if (server.getTickCount() % 6000 == 0) visitors(world);
    }

    /** Somewhere a person could stand: natural ground with two blocks of air above, near the wanted spot. */
    @Nullable
    public static BlockPos openGround(ServerLevel world, int x, int z, int search) {
        for (int r = 0; r <= search; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue;
                    int top = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x + dx, z + dz);
                    BlockPos ground = new BlockPos(x + dx, top - 1, z + dz);
                    BlockState s = world.getBlockState(ground);
                    if (!natural(s) || !world.getFluidState(ground.above()).isEmpty()) continue;
                    if (!world.isEmptyBlock(ground.above(2))) continue;
                    return ground.above();
                }
            }
        }
        return null;
    }

    private static boolean natural(BlockState s) {
        return s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.SAND) || s.is(Blocks.DIRT) || s.is(Blocks.COARSE_DIRT) || s.is(Blocks.DIRT_PATH)
                || s.is(Blocks.GRAVEL) || s.is(Blocks.PODZOL) || s.is(Blocks.RED_SAND) || s.is(Blocks.STONE) || s.is(Blocks.COBBLESTONE)
                || s.is(Blocks.MUD) || s.is(Blocks.SANDSTONE) || s.is(Blocks.ANDESITE) || s.is(Blocks.TUFF) || s.is(Blocks.MOSS_BLOCK)
                || s.is(Blocks.STONE_BRICKS) || s.is(Blocks.MOSSY_COBBLESTONE) || s.is(Blocks.ROOTED_DIRT) || s.is(Blocks.CALCITE);
    }

    public static void markTransient(Entity e, long until) {
        e.addTag(TRANSIENT);
        e.addTag(EXPIRES + until);
    }

    private static void expire(ServerLevel world) {
        long now = world.getGameTime();
        List<Entity> tagged = new java.util.ArrayList<>();
        for (Entity e : world.getAllEntities()) if (e.getTags().contains(TRANSIENT)) tagged.add(e);
        for (Entity e : tagged) {
            for (String tag : e.getTags()) {
                if (!tag.startsWith(EXPIRES)) continue;
                try {
                    if (Long.parseLong(tag.substring(EXPIRES.length())) < now) {
                        world.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF, e.getX(), e.getY() + 1, e.getZ(), 12, 0.3, 0.5, 0.3, 0.02);
                        e.discard();
                    }
                } catch (NumberFormatException ignored) { }
                break;
            }
        }
    }

    /** Every five minutes, a chance of a peddler in a town or the Old Mariner on a beach at night. */
    private static void visitors(ServerLevel world) {
        List<ServerPlayer> players = world.getPlayers(p -> !p.isSpectator());
        if (players.isEmpty()) return;
        ServerPlayer p = players.get(world.random.nextInt(players.size()));
        long t = world.getDayTime() % 24000;
        boolean night = t >= 13000 && t < 23000;
        if (night && world.random.nextFloat() < 0.15f) {
            spawnMariner(world, p);
        } else if (!night && world.random.nextFloat() < 0.3f) {
            spawnPeddler(world, p);
        }
    }

    public static boolean spawnPeddler(ServerLevel world, ServerPlayer p) {
        var port = Prosperity.nearestPort(p.getBlockX(), p.getBlockZ(), 120);
        if (port == null) return false;
        BlockPos at = openGround(world, port.island().cx() + world.random.nextInt(21) - 10, port.island().cz() + world.random.nextInt(21) - 10, 12);
        if (at == null) return false;
        QuartermasterEntity peddler = ModEntities.QUARTERMASTER.create(world);
        if (peddler == null) return false;
        String[] names = {"Jonquil", "Fennick", "Old Saffron", "Hettie Brass", "Mendel", "Quince"};
        peddler.setCustomName(Component.literal(names[world.random.nextInt(names.length)] + ", Wandering Peddler"));
        peddler.setCustomNameVisible(true);
        peddler.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0, 0);
        markTransient(peddler, world.getGameTime() + 12000);
        world.addFreshEntity(peddler);
        world.getServer().getPlayerList().broadcastSystemMessage(Component.literal("A wandering peddler has set out a blanket of curios at "
                + port.island().name() + ". They'll be gone by nightfall.").withStyle(ChatFormatting.YELLOW), false);
        return true;
    }

    public static boolean spawnMariner(ServerLevel world, ServerPlayer p) {
        for (int i = 0; i < 12; i++) {
            double a = world.random.nextDouble() * Math.PI * 2;
            int x = (int) (p.getX() + Math.cos(a) * 24), z = (int) (p.getZ() + Math.sin(a) * 24);
            BlockPos at = openGround(world, x, z, 6);
            if (at == null || !world.getBlockState(at.below()).is(Blocks.SAND)) continue;
            StoryNpcEntity m = Builds.npc(world, Role.OLD_MARINER);
            if (m == null) return false;
            m.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0, 0);
            markTransient(m, world.getGameTime() + 3600);
            world.addFreshEntity(m);
            p.displayClientMessage(Component.literal("Down on the sand, someone is humming an old sea song.").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC), false);
            return true;
        }
        return false;
    }
}
