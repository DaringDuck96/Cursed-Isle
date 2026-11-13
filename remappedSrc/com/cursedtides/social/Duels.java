package com.cursedtides.social;

import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/**
 * Honourable duels: first to be beaten down yields. Nobody dies; the loser is left at half a heart.
 * Challenges lapse after a minute, fights after three, and walking off forfeits.
 */
public final class Duels {
    private Duels() {}

    private static final int CHALLENGE_TICKS = 20 * 60;
    private static final int FIGHT_TICKS = 20 * 180;
    private static final double MAX_DISTANCE = 48;

    private record Challenge(UUID challenger, int expires) {}
    private record Fight(UUID a, UUID b, int ends) {
        UUID other(UUID id) { return id.equals(a) ? b : a; }
    }

    /** Keyed by the challenged player. */
    private static final Map<UUID, Challenge> CHALLENGES = new HashMap<>();
    /** Each fight is stored under both fighters. */
    private static final Map<UUID, Fight> FIGHTS = new HashMap<>();

    public static boolean inDuel(UUID player) {
        return FIGHTS.containsKey(player);
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> dispatcher.register(literal("duel")
                .then(literal("accept").executes(Duels::accept))
                .then(literal("decline").executes(Duels::decline))
                .then(literal("yield").executes(Duels::yieldFight))
                .then(argument("player", EntityArgument.player()).executes(Duels::challenge))));

        ServerLivingEntityEvents.ALLOW_DAMAGE.register(Duels::allowDamage);
        ServerTickEvents.END_SERVER_TICK.register(Duels::tick);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID id = handler.getPlayer().getUUID();
            CHALLENGES.remove(id);
            Fight fight = FIGHTS.get(id);
            if (fight != null) finish(server, fight.other(id), id, "left the island");
        });
    }

    private static int challenge(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        if (target == p) return fail(p, "You can't cross blades with your own reflection.");
        if (inDuel(p.getUUID()) || inDuel(target.getUUID())) return fail(p, "One of you is already in a duel.");
        if (p.distanceTo(target) > MAX_DISTANCE) return fail(p, "Get within " + (int) MAX_DISTANCE + " blocks to issue a challenge.");
        CHALLENGES.put(target.getUUID(), new Challenge(p.getUUID(), ctx.getSource().getServer().getTickCount() + CHALLENGE_TICKS));
        p.displayClientMessage(Component.literal("You throw down the gauntlet to " + target.getName().getString() + ".").withStyle(ChatFormatting.GOLD), false);
        target.displayClientMessage(Component.literal(p.getName().getString() + " challenges you to a duel! /duel accept or /duel decline.")
                .withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private static int accept(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        MinecraftServer server = ctx.getSource().getServer();
        Challenge c = CHALLENGES.remove(p.getUUID());
        ServerPlayer challenger = c == null ? null : server.getPlayerList().getPlayer(c.challenger());
        if (challenger == null) return fail(p, "There's no challenge waiting for you.");
        if (inDuel(p.getUUID()) || inDuel(challenger.getUUID())) return fail(p, "One of you is already in a duel.");
        Fight fight = new Fight(challenger.getUUID(), p.getUUID(), server.getTickCount() + FIGHT_TICKS);
        FIGHTS.put(fight.a(), fight);
        FIGHTS.put(fight.b(), fight);
        Component start = Component.literal("En garde! " + challenger.getName().getString() + " and " + p.getName().getString()
                + " duel. First to fall yields.").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        challenger.displayClientMessage(start, false);
        p.displayClientMessage(start, false);
        for (ServerPlayer fighter : new ServerPlayer[]{challenger, p}) {
            fighter.level().playSound(null, fighter.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.6f, 1.4f);
        }
        return 1;
    }

    private static int decline(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        Challenge c = CHALLENGES.remove(p.getUUID());
        if (c == null) return fail(p, "There's no challenge to decline.");
        ServerPlayer challenger = ctx.getSource().getServer().getPlayerList().getPlayer(c.challenger());
        if (challenger != null) challenger.displayClientMessage(Component.literal(p.getName().getString() + " declines your challenge.").withStyle(ChatFormatting.GRAY), false);
        p.displayClientMessage(Component.literal("Challenge declined.").withStyle(ChatFormatting.GRAY), false);
        return 1;
    }

    private static int yieldFight(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        Fight fight = FIGHTS.get(p.getUUID());
        if (fight == null) return fail(p, "You're not in a duel.");
        finish(ctx.getSource().getServer(), fight.other(p.getUUID()), p.getUUID(), "yielded");
        return 1;
    }

    /** The blow that would kill a duellist instead ends the duel. */
    private static boolean allowDamage(LivingEntity entity, DamageSource source, float amount) {
        if (!(entity instanceof ServerPlayer victim)) return true;
        Fight fight = FIGHTS.get(victim.getUUID());
        if (fight == null) return true;
        UUID opponent = fight.other(victim.getUUID());
        boolean fromOpponent = source.getEntity() instanceof ServerPlayer attacker && attacker.getUUID().equals(opponent);
        if (fromOpponent && amount >= victim.getHealth()) {
            victim.setHealth(1.0f);
            finish(victim.getServer(), opponent, victim.getUUID(), "was bested");
            return false;
        }
        return true;
    }

    private static void tick(MinecraftServer server) {
        if (server.getTickCount() % 20 != 0) return;
        int now = server.getTickCount();
        CHALLENGES.values().removeIf(c -> c.expires() < now);
        Iterator<Map.Entry<UUID, Fight>> it = Map.copyOf(FIGHTS).entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Fight> e = it.next();
            Fight f = e.getValue();
            if (!e.getKey().equals(f.a()) || !FIGHTS.containsKey(f.a())) continue; // handle each fight once
            ServerPlayer a = server.getPlayerList().getPlayer(f.a());
            ServerPlayer b = server.getPlayerList().getPlayer(f.b());
            if (a == null || b == null) continue; // disconnect handler deals with it
            if (a.isDeadOrDying()) { finish(server, f.b(), f.a(), "fell to something else"); continue; }
            if (b.isDeadOrDying()) { finish(server, f.a(), f.b(), "fell to something else"); continue; }
            if (a.level() != b.level() || a.distanceTo(b) > MAX_DISTANCE) {
                UUID runner = a.level() != b.level() ? f.a() : (a.getDeltaMovement().lengthSqr() > b.getDeltaMovement().lengthSqr() ? f.a() : f.b());
                finish(server, f.other(runner), runner, "fled the field");
                continue;
            }
            if (f.ends() < now) {
                FIGHTS.remove(f.a());
                FIGHTS.remove(f.b());
                Component draw = Component.literal("Time! The duel ends in a draw, and both of you still standing.").withStyle(ChatFormatting.GRAY);
                a.displayClientMessage(draw, false);
                b.displayClientMessage(draw, false);
            }
        }
    }

    private static void finish(MinecraftServer server, UUID winnerId, UUID loserId, String how) {
        Fight f = FIGHTS.remove(winnerId);
        FIGHTS.remove(loserId);
        if (f == null || server == null) return;
        ServerPlayer winner = server.getPlayerList().getPlayer(winnerId);
        ServerPlayer loser = server.getPlayerList().getPlayer(loserId);
        String loserName = loser != null ? loser.getName().getString() : "Their opponent";
        String winnerName = winner != null ? winner.getName().getString() : "their opponent";
        server.getPlayerList().broadcastSystemMessage(Component.literal(loserName + " " + how + "; " + winnerName + " wins the duel!")
                .withStyle(ChatFormatting.GOLD), false);
        if (winner != null) {
            PirateData.addXp(winner, Weapon.CUTLASS, 25);
            Infamy.land(winner, 20);
            com.cursedtides.activity.Achievements.grant(winner, "duel_won");
            com.cursedtides.activity.Challenges.progress(winner, "duel", 1);
            com.cursedtides.activity.Leaderboards.add(winner, "duels", 1);
            winner.level().playSound(null, winner.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 0.7f, 1.2f);
        }
        if (loser != null) {
            PirateData.addXp(loser, Weapon.CUTLASS, 8);
            Infamy.land(loser, 3);
        }
    }

    private static int fail(ServerPlayer p, String msg) {
        p.displayClientMessage(Component.literal(msg).withStyle(ChatFormatting.RED), false);
        return 0;
    }
}
