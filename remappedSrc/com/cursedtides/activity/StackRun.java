package com.cursedtides.activity;

import com.cursedtides.world.Archipelago;
import com.cursedtides.world.Builds;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
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

    public static Vec3 flag(int i) {
        var isle = Archipelago.STORMBREAK_ISLE;
        int x = isle.cx() + FLAGS[i][0], z = isle.cz() + FLAGS[i][1];
        return new Vec3(x + 0.5, Builds.groundY(x, z), z + 0.5);
    }

    public static List<String> start(ServerPlayer p, Entity master) {
        RUNS.put(p.getUUID(), new Run(0, p.serverLevel().getGameTime()));
        return List.of("Ten flags, in order, over the top and back down. The next one's burning gold. The clock's already running!",
                "Fall in the sea and you start again. Mind the edges, they crumble.");
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(StackRun::tick);
    }

    private static void tick(MinecraftServer server) {
        ServerLevel world = server.overworld();
        if (server.getTickCount() % 10 == 0 && !RUNS.isEmpty()) {
            for (int i = 0; i < FLAGS.length; i++) {
                Vec3 f = flag(i);
                world.sendParticles(new DustParticleOptions(new Vector3f(0.8f, 0.1f, 0.1f), 1.5f), f.x, f.y + 1.5, f.z, 3, 0.2, 0.6, 0.2, 0);
            }
        }
        for (Map.Entry<UUID, Run> e : List.copyOf(RUNS.entrySet())) {
            ServerPlayer p = server.getPlayerList().getPlayer(e.getKey());
            Run run = e.getValue();
            if (p == null || p.isPassenger() || p.getAbilities().flying || p.isFallFlying()) { RUNS.remove(e.getKey()); continue; }
            if (p.isInWater()) {
                RUNS.remove(e.getKey());
                p.displayClientMessage(Component.literal("Into the drink! Run abandoned.").withStyle(ChatFormatting.RED), true);
                continue;
            }
            Vec3 f = flag(run.next());
            if (server.getTickCount() % 4 == 0) {
                world.sendParticles(p, ParticleTypes.WAX_OFF, true, f.x, f.y + 1.2, f.z, 6, 0.3, 0.8, 0.3, 0);
            }
            if (p.position().distanceToSqr(f) < TOUCH * TOUCH) {
                world.playSound(null, p.blockPosition(), SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 1f, 1f + run.next() * 0.08f);
                if (run.next() + 1 >= FLAGS.length) {
                    RUNS.remove(e.getKey());
                    finish(p, (int) (world.getGameTime() - run.started()));
                } else {
                    RUNS.put(e.getKey(), new Run(run.next() + 1, run.started()));
                    p.displayClientMessage(Component.literal("Flag " + (run.next() + 1) + "/" + FLAGS.length + "  "
                            + String.format("%.1fs", (world.getGameTime() - run.started()) / 20.0)).withStyle(ChatFormatting.YELLOW), true);
                }
            }
        }
    }

    private static void finish(ServerPlayer p, int ticks) {
        p.displayClientMessage(Component.literal("Stack Run complete in " + String.format("%.1f", ticks / 20.0) + " seconds!").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        if (Leaderboards.submit(p, "course", ticks)) p.displayClientMessage(Component.literal("A personal best!").withStyle(ChatFormatting.YELLOW), false);
        if (ticks < 90 * 20) Achievements.grant(p, "stack_runner");
        p.addItem(new ItemStack(com.cursedtides.item.ModItems.DOUBLOON, ticks < 90 * 20 ? 12 : 5));
        com.cursedtides.pirate.PirateData.addXp(p, com.cursedtides.pirate.Weapon.EXPLORATION, 40);
    }
}
