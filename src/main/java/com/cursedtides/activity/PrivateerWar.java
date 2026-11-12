package com.cursedtides.activity;

import com.cursedtides.item.ModItems;
import com.cursedtides.living.Residents;
import com.cursedtides.world.Archipelago;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * The Privateer War: two letters of marque, Crimson and Azure, fight for Bloodwake Cay. Points for besting the other
 * side and for holding the old signal mast in the middle. Eight minutes; the higher score takes the purse.
 */
public final class PrivateerWar {
    private PrivateerWar() {}

    private static final int LENGTH = 20 * 60 * 8, HOLD_RADIUS = 7;
    private static final Set<UUID> QUEUE = new LinkedHashSet<>();
    private static final Map<UUID, Integer> TEAM = new HashMap<>(); // 0 crimson, 1 azure
    private static final Map<UUID, Integer> SCORE = new HashMap<>();
    private static int[] points = new int[2];
    private static int ticks = -1, countdown = -1;
    private static ServerBossBar bar;

    private static Archipelago.Island isle() { return Archipelago.BLOODWAKE_CAY; }

    public static boolean running() { return ticks >= 0; }

    public static void join(ServerPlayerEntity p) {
        if (running()) {
            if (!TEAM.containsKey(p.getUuid())) enlist(p.getServer(), p);
            return;
        }
        QUEUE.add(p.getUuid());
        p.sendMessage(Text.literal("You sign a letter of marque. " + QUEUE.size() + " privateers waiting (two needed).").formatted(Formatting.GOLD), false);
        if (QUEUE.size() >= 2 && countdown < 0) {
            countdown = 600;
            p.getServer().getPlayerManager().broadcast(Text.literal("A Privateer War begins at Bloodwake Cay in 30 seconds! /privateer join").formatted(Formatting.GOLD), false);
        }
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((d, a, e) -> d.register(literal("privateer")
                .then(literal("join").executes(ctx -> { join(ctx.getSource().getPlayerOrThrow()); return 1; }))
                .then(literal("leave").executes(ctx -> {
                    ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                    QUEUE.remove(p.getUuid());
                    TEAM.remove(p.getUuid());
                    if (bar != null) bar.removePlayer(p);
                    return 1;
                }))
                .then(literal("start").requires(s -> s.hasPermissionLevel(2)).executes(ctx -> { countdown = 20; return 1; }))));
        ServerTickEvents.END_SERVER_TICK.register(PrivateerWar::tick);
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((victim, source, amount) -> {
            if (!running() || !(victim instanceof ServerPlayerEntity v) || !(source.getAttacker() instanceof ServerPlayerEntity a)) return true;
            Integer tv = TEAM.get(v.getUuid()), ta = TEAM.get(a.getUuid());
            return tv == null || ta == null || !tv.equals(ta);
        });
        ServerLivingEntityEvents.AFTER_DEATH.register((victim, source) -> {
            if (!running() || !(victim instanceof ServerPlayerEntity v) || !(source.getAttacker() instanceof ServerPlayerEntity a)) return;
            Integer tv = TEAM.get(v.getUuid()), ta = TEAM.get(a.getUuid());
            if (tv == null || ta == null || tv.equals(ta)) return;
            points[ta] += 3;
            SCORE.merge(a.getUuid(), 3, Integer::sum);
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldP, p, alive) -> {
            if (running() && TEAM.containsKey(p.getUuid())) sendToSide(p, TEAM.get(p.getUuid()));
        });
    }

    private static void enlist(MinecraftServer server, ServerPlayerEntity p) {
        int team = count(0) <= count(1) ? 0 : 1;
        TEAM.put(p.getUuid(), team);
        bar.addPlayer(p);
        sendToSide(p, team);
        p.sendMessage(Text.literal("You fight under the " + (team == 0 ? "Crimson" : "Azure") + " letter!").formatted(team == 0 ? Formatting.RED : Formatting.BLUE, Formatting.BOLD), false);
    }

    private static int count(int team) {
        int n = 0;
        for (int t : TEAM.values()) if (t == team) n++;
        return n;
    }

    private static void sendToSide(ServerPlayerEntity p, int team) {
        ServerWorld w = p.getServer().getOverworld();
        int off = (int) (isle().radius() * 0.6) * (team == 0 ? -1 : 1);
        BlockPos at = Residents.openGround(w, isle().cx() + off, isle().cz(), 12);
        if (at == null) at = new BlockPos(isle().cx() + off, com.cursedtides.world.Builds.groundY(isle().cx() + off, isle().cz()), isle().cz());
        p.teleport(w, at.getX() + 0.5, at.getY(), at.getZ() + 0.5, team == 0 ? -90 : 90, 0);
    }

    private static void tick(MinecraftServer server) {
        if (countdown > 0 && --countdown == 0) start(server);
        if (!running()) return;
        ticks++;
        ServerWorld w = server.getOverworld();
        if (ticks % 100 == 0) {
            int[] near = new int[2];
            for (var e : TEAM.entrySet()) {
                ServerPlayerEntity p = server.getPlayerManager().getPlayer(e.getKey());
                if (p != null && p.isAlive() && p.squaredDistanceTo(isle().cx(), p.getY(), isle().cz()) < HOLD_RADIUS * HOLD_RADIUS) near[e.getValue()]++;
            }
            if (near[0] > 0 && near[1] == 0) points[0]++;
            if (near[1] > 0 && near[0] == 0) points[1]++;
            int gy = com.cursedtides.world.Builds.groundY(isle().cx(), isle().cz());
            w.spawnParticles(near[0] > near[1] ? ParticleTypes.FLAME : ParticleTypes.SOUL_FIRE_FLAME, isle().cx() + 0.5, gy + 4, isle().cz() + 0.5, 20, 0.2, 2, 0.2, 0.01);
        }
        bar.setName(Text.literal("Crimson " + points[0] + "  vs  " + points[1] + " Azure   (" + Math.max(0, (LENGTH - ticks) / 20) + "s)"));
        bar.setPercent(points[0] + points[1] == 0 ? 0.5f : points[0] / (float) (points[0] + points[1]));
        if (ticks >= LENGTH) finish(server);
    }

    private static void start(MinecraftServer server) {
        if (QUEUE.size() < 2) {
            countdown = -1;
            return;
        }
        ticks = 0;
        points = new int[2];
        SCORE.clear();
        TEAM.clear();
        bar = new ServerBossBar(Text.literal("Privateer War"), BossBar.Color.RED, BossBar.Style.PROGRESS);
        for (UUID id : QUEUE) {
            ServerPlayerEntity p = server.getPlayerManager().getPlayer(id);
            if (p != null) enlist(server, p);
        }
        QUEUE.clear();
        countdown = -1;
        server.getPlayerManager().broadcast(Text.literal("The Privateer War has begun at Bloodwake Cay! Hold the signal mast, best the other letter.").formatted(Formatting.GOLD, Formatting.BOLD), false);
    }

    private static void finish(MinecraftServer server) {
        int winner = points[0] == points[1] ? -1 : points[0] > points[1] ? 0 : 1;
        server.getPlayerManager().broadcast(Text.literal("The Privateer War is over: Crimson " + points[0] + ", Azure " + points[1] + ". "
                + (winner < 0 ? "A draw!" : (winner == 0 ? "Crimson" : "Azure") + " takes the purse!")).formatted(Formatting.GOLD, Formatting.BOLD), false);
        for (var e : TEAM.entrySet()) {
            ServerPlayerEntity p = server.getPlayerManager().getPlayer(e.getKey());
            if (p == null) continue;
            boolean won = e.getValue() == winner;
            p.giveItemStack(new ItemStack(ModItems.DOUBLOON, won ? 30 : 10));
            Leaderboards.add(p, "privateer", SCORE.getOrDefault(p.getUuid(), 0));
            if (won) Achievements.grant(p, "privateer");
        }
        if (bar != null) bar.clearPlayers();
        bar = null;
        TEAM.clear();
        ticks = -1;
    }
}
