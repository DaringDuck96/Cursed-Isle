package com.cursedtides.activity;

import com.cursedtides.item.ModItems;
import com.cursedtides.living.Residents;
import com.cursedtides.world.Archipelago;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.item.ItemStack;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static net.minecraft.commands.Commands.literal;

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
    private static ServerBossEvent bar;

    private static Archipelago.Island isle() { return Archipelago.BLOODWAKE_CAY; }

    public static boolean running() { return ticks >= 0; }

    public static void join(ServerPlayer p) {
        if (running()) {
            if (!TEAM.containsKey(p.getUUID())) enlist(p.level().getServer(), p);
            return;
        }
        QUEUE.add(p.getUUID());
        p.sendSystemMessage(Component.literal("You sign a letter of marque. " + QUEUE.size() + " privateers waiting (two needed).").withStyle(ChatFormatting.GOLD), false);
        if (QUEUE.size() >= 2 && countdown < 0) {
            countdown = 600;
            p.level().getServer().getPlayerList().broadcastSystemMessage(Component.literal("A Privateer War begins at Bloodwake Cay in 30 seconds! /privateer join").withStyle(ChatFormatting.GOLD), false);
        }
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((d, a, e) -> d.register(literal("privateer")
                .then(literal("join").executes(ctx -> { join(ctx.getSource().getPlayerOrException()); return 1; }))
                .then(literal("leave").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    QUEUE.remove(p.getUUID());
                    TEAM.remove(p.getUUID());
                    if (bar != null) bar.removePlayer(p);
                    return 1;
                }))
                .then(literal("start").requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)).executes(ctx -> { countdown = 20; return 1; }))));
        ServerTickEvents.END_SERVER_TICK.register(PrivateerWar::tick);
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((victim, source, amount) -> {
            if (!running() || !(victim instanceof ServerPlayer v) || !(source.getEntity() instanceof ServerPlayer a)) return true;
            Integer tv = TEAM.get(v.getUUID()), ta = TEAM.get(a.getUUID());
            return tv == null || ta == null || !tv.equals(ta);
        });
        ServerLivingEntityEvents.AFTER_DEATH.register((victim, source) -> {
            if (!running() || !(victim instanceof ServerPlayer v) || !(source.getEntity() instanceof ServerPlayer a)) return;
            Integer tv = TEAM.get(v.getUUID()), ta = TEAM.get(a.getUUID());
            if (tv == null || ta == null || tv.equals(ta)) return;
            points[ta] += 3;
            SCORE.merge(a.getUUID(), 3, Integer::sum);
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldP, p, alive) -> {
            if (running() && TEAM.containsKey(p.getUUID())) sendToSide(p, TEAM.get(p.getUUID()));
        });
    }

    private static void enlist(MinecraftServer server, ServerPlayer p) {
        int team = count(0) <= count(1) ? 0 : 1;
        TEAM.put(p.getUUID(), team);
        bar.addPlayer(p);
        sendToSide(p, team);
        p.sendSystemMessage(Component.literal("You fight under the " + (team == 0 ? "Crimson" : "Azure") + " letter!").withStyle(team == 0 ? ChatFormatting.RED : ChatFormatting.BLUE, ChatFormatting.BOLD), false);
    }

    private static int count(int team) {
        int n = 0;
        for (int t : TEAM.values()) if (t == team) n++;
        return n;
    }

    private static void sendToSide(ServerPlayer p, int team) {
        ServerLevel w = p.level().getServer().overworld();
        int off = (int) (isle().radius() * 0.6) * (team == 0 ? -1 : 1);
        BlockPos at = Residents.openGround(w, isle().cx() + off, isle().cz(), 12);
        if (at == null) at = new BlockPos(isle().cx() + off, com.cursedtides.world.Builds.groundY(isle().cx() + off, isle().cz()), isle().cz());
        com.cursedtides.util.Compat.teleport(p, w, at.getX() + 0.5, at.getY(), at.getZ() + 0.5, team == 0 ? -90 : 90, 0);
    }

    private static void tick(MinecraftServer server) {
        if (countdown > 0 && --countdown == 0) start(server);
        if (!running()) return;
        ticks++;
        ServerLevel w = server.overworld();
        if (ticks % 100 == 0) {
            int[] near = new int[2];
            for (var e : TEAM.entrySet()) {
                ServerPlayer p = server.getPlayerList().getPlayer(e.getKey());
                if (p != null && p.isAlive() && p.distanceToSqr(isle().cx(), p.getY(), isle().cz()) < HOLD_RADIUS * HOLD_RADIUS) near[e.getValue()]++;
            }
            if (near[0] > 0 && near[1] == 0) points[0]++;
            if (near[1] > 0 && near[0] == 0) points[1]++;
            int gy = com.cursedtides.world.Builds.groundY(isle().cx(), isle().cz());
            w.sendParticles(near[0] > near[1] ? ParticleTypes.FLAME : ParticleTypes.SOUL_FIRE_FLAME, isle().cx() + 0.5, gy + 4, isle().cz() + 0.5, 20, 0.2, 2, 0.2, 0.01);
        }
        bar.setName(Component.literal("Crimson " + points[0] + "  vs  " + points[1] + " Azure   (" + Math.max(0, (LENGTH - ticks) / 20) + "s)"));
        bar.setProgress(points[0] + points[1] == 0 ? 0.5f : points[0] / (float) (points[0] + points[1]));
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
        bar = new ServerBossEvent(java.util.UUID.randomUUID(), Component.literal("Privateer War"), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
        for (UUID id : QUEUE) {
            ServerPlayer p = server.getPlayerList().getPlayer(id);
            if (p != null) enlist(server, p);
        }
        QUEUE.clear();
        countdown = -1;
        server.getPlayerList().broadcastSystemMessage(Component.literal("The Privateer War has begun at Bloodwake Cay! Hold the signal mast, best the other letter.").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
    }

    private static void finish(MinecraftServer server) {
        int winner = points[0] == points[1] ? -1 : points[0] > points[1] ? 0 : 1;
        server.getPlayerList().broadcastSystemMessage(Component.literal("The Privateer War is over: Crimson " + points[0] + ", Azure " + points[1] + ". "
                + (winner < 0 ? "A draw!" : (winner == 0 ? "Crimson" : "Azure") + " takes the purse!")).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        for (var e : TEAM.entrySet()) {
            ServerPlayer p = server.getPlayerList().getPlayer(e.getKey());
            if (p == null) continue;
            boolean won = e.getValue() == winner;
            p.addItem(new ItemStack(ModItems.DOUBLOON, won ? 30 : 10));
            Leaderboards.add(p, "privateer", SCORE.getOrDefault(p.getUUID(), 0));
            if (won) Achievements.grant(p, "privateer");
        }
        if (bar != null) bar.removeAllPlayers();
        bar = null;
        TEAM.clear();
        ticks = -1;
    }
}
