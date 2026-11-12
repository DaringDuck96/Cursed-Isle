package com.cursedtides.social;

import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

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
                .then(argument("player", EntityArgumentType.player()).executes(Duels::challenge))));

        ServerLivingEntityEvents.ALLOW_DAMAGE.register(Duels::allowDamage);
        ServerTickEvents.END_SERVER_TICK.register(Duels::tick);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID id = handler.getPlayer().getUuid();
            CHALLENGES.remove(id);
            Fight fight = FIGHTS.get(id);
            if (fight != null) finish(server, fight.other(id), id, "left the island");
        });
    }

    private static int challenge(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
        if (target == p) return fail(p, "You can't cross blades with your own reflection.");
        if (inDuel(p.getUuid()) || inDuel(target.getUuid())) return fail(p, "One of you is already in a duel.");
        if (p.distanceTo(target) > MAX_DISTANCE) return fail(p, "Get within " + (int) MAX_DISTANCE + " blocks to issue a challenge.");
        CHALLENGES.put(target.getUuid(), new Challenge(p.getUuid(), ctx.getSource().getServer().getTicks() + CHALLENGE_TICKS));
        p.sendMessage(Text.literal("You throw down the gauntlet to " + target.getName().getString() + ".").formatted(Formatting.GOLD), false);
        target.sendMessage(Text.literal(p.getName().getString() + " challenges you to a duel! /duel accept or /duel decline.")
                .formatted(Formatting.GOLD), false);
        return 1;
    }

    private static int accept(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        MinecraftServer server = ctx.getSource().getServer();
        Challenge c = CHALLENGES.remove(p.getUuid());
        ServerPlayerEntity challenger = c == null ? null : server.getPlayerManager().getPlayer(c.challenger());
        if (challenger == null) return fail(p, "There's no challenge waiting for you.");
        if (inDuel(p.getUuid()) || inDuel(challenger.getUuid())) return fail(p, "One of you is already in a duel.");
        Fight fight = new Fight(challenger.getUuid(), p.getUuid(), server.getTicks() + FIGHT_TICKS);
        FIGHTS.put(fight.a(), fight);
        FIGHTS.put(fight.b(), fight);
        Text start = Text.literal("En garde! " + challenger.getName().getString() + " and " + p.getName().getString()
                + " duel. First to fall yields.").formatted(Formatting.GOLD, Formatting.BOLD);
        challenger.sendMessage(start, false);
        p.sendMessage(start, false);
        for (ServerPlayerEntity fighter : new ServerPlayerEntity[]{challenger, p}) {
            fighter.getWorld().playSound(null, fighter.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 0.6f, 1.4f);
        }
        return 1;
    }

    private static int decline(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        Challenge c = CHALLENGES.remove(p.getUuid());
        if (c == null) return fail(p, "There's no challenge to decline.");
        ServerPlayerEntity challenger = ctx.getSource().getServer().getPlayerManager().getPlayer(c.challenger());
        if (challenger != null) challenger.sendMessage(Text.literal(p.getName().getString() + " declines your challenge.").formatted(Formatting.GRAY), false);
        p.sendMessage(Text.literal("Challenge declined.").formatted(Formatting.GRAY), false);
        return 1;
    }

    private static int yieldFight(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        Fight fight = FIGHTS.get(p.getUuid());
        if (fight == null) return fail(p, "You're not in a duel.");
        finish(ctx.getSource().getServer(), fight.other(p.getUuid()), p.getUuid(), "yielded");
        return 1;
    }

    /** The blow that would kill a duellist instead ends the duel. */
    private static boolean allowDamage(LivingEntity entity, DamageSource source, float amount) {
        if (!(entity instanceof ServerPlayerEntity victim)) return true;
        Fight fight = FIGHTS.get(victim.getUuid());
        if (fight == null) return true;
        UUID opponent = fight.other(victim.getUuid());
        boolean fromOpponent = source.getAttacker() instanceof ServerPlayerEntity attacker && attacker.getUuid().equals(opponent);
        if (fromOpponent && amount >= victim.getHealth()) {
            victim.setHealth(1.0f);
            finish(victim.getServer(), opponent, victim.getUuid(), "was bested");
            return false;
        }
        return true;
    }

    private static void tick(MinecraftServer server) {
        if (server.getTicks() % 20 != 0) return;
        int now = server.getTicks();
        CHALLENGES.values().removeIf(c -> c.expires() < now);
        Iterator<Map.Entry<UUID, Fight>> it = Map.copyOf(FIGHTS).entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Fight> e = it.next();
            Fight f = e.getValue();
            if (!e.getKey().equals(f.a()) || !FIGHTS.containsKey(f.a())) continue; // handle each fight once
            ServerPlayerEntity a = server.getPlayerManager().getPlayer(f.a());
            ServerPlayerEntity b = server.getPlayerManager().getPlayer(f.b());
            if (a == null || b == null) continue; // disconnect handler deals with it
            if (a.isDead()) { finish(server, f.b(), f.a(), "fell to something else"); continue; }
            if (b.isDead()) { finish(server, f.a(), f.b(), "fell to something else"); continue; }
            if (a.getWorld() != b.getWorld() || a.distanceTo(b) > MAX_DISTANCE) {
                UUID runner = a.getWorld() != b.getWorld() ? f.a() : (a.getVelocity().lengthSquared() > b.getVelocity().lengthSquared() ? f.a() : f.b());
                finish(server, f.other(runner), runner, "fled the field");
                continue;
            }
            if (f.ends() < now) {
                FIGHTS.remove(f.a());
                FIGHTS.remove(f.b());
                Text draw = Text.literal("Time! The duel ends in a draw, and both of you still standing.").formatted(Formatting.GRAY);
                a.sendMessage(draw, false);
                b.sendMessage(draw, false);
            }
        }
    }

    private static void finish(MinecraftServer server, UUID winnerId, UUID loserId, String how) {
        Fight f = FIGHTS.remove(winnerId);
        FIGHTS.remove(loserId);
        if (f == null || server == null) return;
        ServerPlayerEntity winner = server.getPlayerManager().getPlayer(winnerId);
        ServerPlayerEntity loser = server.getPlayerManager().getPlayer(loserId);
        String loserName = loser != null ? loser.getName().getString() : "Their opponent";
        String winnerName = winner != null ? winner.getName().getString() : "their opponent";
        server.getPlayerManager().broadcast(Text.literal(loserName + " " + how + "; " + winnerName + " wins the duel!")
                .formatted(Formatting.GOLD), false);
        if (winner != null) {
            PirateData.addXp(winner, Weapon.CUTLASS, 25);
            com.cursedtides.activity.Achievements.grant(winner, "duel_won");
            com.cursedtides.activity.Challenges.progress(winner, "duel", 1);
            com.cursedtides.activity.Leaderboards.add(winner, "duels", 1);
            winner.getWorld().playSound(null, winner.getBlockPos(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 0.7f, 1.2f);
        }
        if (loser != null) {
            PirateData.addXp(loser, Weapon.CUTLASS, 8);
        }
    }

    private static int fail(ServerPlayerEntity p, String msg) {
        p.sendMessage(Text.literal(msg).formatted(Formatting.RED), false);
        return 0;
    }
}
