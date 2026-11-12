package com.cursedtides.activity;

import com.cursedtides.world.Archipelago;
import com.cursedtides.world.Builds;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The Stack Run: a foot race up and over the crags of Stormbreak Isle, touching every flag in order.
 * The flags are marked with red smoke; the next one you need burns gold. No ships, no mounts, no gliding.
 */
public final class StackRun {
    private StackRun() {}

    /** Checkpoints as offsets from the island's centre; the height is the ground there. */
    private static final int[][] FLAGS = {{-8, 14}, {4, 20}, {16, 12}, {22, -2}, {14, -16}, {0, -24}, {-14, -18}, {-22, -4}, {-10, 6}, {-12, 16}};
    private static final double TOUCH = 2.5;

    private record Run(int next, long started) {}
    private static final Map<UUID, Run> RUNS = new HashMap<>();

    public static Vec3d flag(int i) {
        var isle = Archipelago.STORMBREAK_ISLE;
        int x = isle.cx() + FLAGS[i][0], z = isle.cz() + FLAGS[i][1];
        return new Vec3d(x + 0.5, Builds.groundY(x, z), z + 0.5);
    }

    public static List<String> start(ServerPlayerEntity p, Entity master) {
        RUNS.put(p.getUuid(), new Run(0, p.getServerWorld().getTime()));
        return List.of("Ten flags, in order, over the top and back down. The next one's burning gold. The clock's already running!",
                "Fall in the sea and you start again. Mind the edges, they crumble.");
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(StackRun::tick);
    }

    private static void tick(MinecraftServer server) {
        ServerWorld world = server.getOverworld();
        if (server.getTicks() % 10 == 0 && !RUNS.isEmpty()) {
            for (int i = 0; i < FLAGS.length; i++) {
                Vec3d f = flag(i);
                world.spawnParticles(new DustParticleEffect(new Vector3f(0.8f, 0.1f, 0.1f), 1.5f), f.x, f.y + 1.5, f.z, 3, 0.2, 0.6, 0.2, 0);
            }
        }
        for (Map.Entry<UUID, Run> e : List.copyOf(RUNS.entrySet())) {
            ServerPlayerEntity p = server.getPlayerManager().getPlayer(e.getKey());
            Run run = e.getValue();
            if (p == null || p.hasVehicle() || p.getAbilities().flying || p.isFallFlying()) { RUNS.remove(e.getKey()); continue; }
            if (p.isTouchingWater()) {
                RUNS.remove(e.getKey());
                p.sendMessage(Text.literal("Into the drink! Run abandoned.").formatted(Formatting.RED), true);
                continue;
            }
            Vec3d f = flag(run.next());
            if (server.getTicks() % 4 == 0) {
                world.spawnParticles(p, ParticleTypes.WAX_OFF, true, f.x, f.y + 1.2, f.z, 6, 0.3, 0.8, 0.3, 0);
            }
            if (p.getPos().squaredDistanceTo(f) < TOUCH * TOUCH) {
                world.playSound(null, p.getBlockPos(), SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.PLAYERS, 1f, 1f + run.next() * 0.08f);
                if (run.next() + 1 >= FLAGS.length) {
                    RUNS.remove(e.getKey());
                    finish(p, (int) (world.getTime() - run.started()));
                } else {
                    RUNS.put(e.getKey(), new Run(run.next() + 1, run.started()));
                    p.sendMessage(Text.literal("Flag " + (run.next() + 1) + "/" + FLAGS.length + "  "
                            + String.format("%.1fs", (world.getTime() - run.started()) / 20.0)).formatted(Formatting.YELLOW), true);
                }
            }
        }
    }

    private static void finish(ServerPlayerEntity p, int ticks) {
        p.sendMessage(Text.literal("Stack Run complete in " + String.format("%.1f", ticks / 20.0) + " seconds!").formatted(Formatting.GOLD, Formatting.BOLD), false);
        if (Leaderboards.submit(p, "course", ticks)) p.sendMessage(Text.literal("A personal best!").formatted(Formatting.YELLOW), false);
        if (ticks < 90 * 20) Achievements.grant(p, "stack_runner");
        p.giveItemStack(new ItemStack(com.cursedtides.item.ModItems.DOUBLOON, ticks < 90 * 20 ? 12 : 5));
        com.cursedtides.pirate.PirateData.addXp(p, com.cursedtides.pirate.Weapon.EXPLORATION, 40);
    }
}
