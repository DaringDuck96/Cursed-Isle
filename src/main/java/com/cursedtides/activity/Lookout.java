package com.cursedtides.activity;

import com.cursedtides.entity.StoryNpcEntity.Role;
import com.cursedtides.living.Residents;
import com.cursedtides.world.Archipelago;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * The Lookout: put your name down for an activity, and when enough hands have signed up, everyone is carried there
 * together. /lookout, /lookout join <activity>, /lookout leave.
 */
public final class Lookout {
    private Lookout() {}

    public record Activity(String id, String title, int needed, Role host) {}

    public static final List<Activity> ACTIVITIES = List.of(
            new Activity("arena", "Sword tournament (Saltmarrow)", 2, Role.ARENA_MASTER),
            new Activity("range", "Shooting range (Saltmarrow)", 2, Role.RANGE_MASTER),
            new Activity("course", "Stack Run (Stormbreak)", 2, Role.COURSE_MASTER),
            new Activity("defense", "Cannon Defense (Saltmarrow harbour)", 2, Role.GUNNERY_SERGEANT),
            new Activity("drinking", "Tavern night (Saltmarrow)", 2, Role.TAVERN_KEEPER),
            new Activity("privateer", "Privateer War (Bloodwake Cay)", 2, null));

    private static final Map<String, Set<UUID>> QUEUES = new LinkedHashMap<>();

    public static void register() {
        CommandRegistrationCallback.EVENT.register((d, a, e) -> d.register(literal("lookout")
                .executes(ctx -> { show(ctx.getSource().getPlayerOrThrow()); return 1; })
                .then(literal("leave").executes(ctx -> {
                    UUID id = ctx.getSource().getPlayerOrThrow().getUuid();
                    QUEUES.values().forEach(q -> q.remove(id));
                    ctx.getSource().getPlayerOrThrow().sendMessage(Text.literal("You take your name off the lookout board.").formatted(Formatting.GRAY), false);
                    return 1;
                }))
                .then(literal("join").then(argument("activity", StringArgumentType.word()).suggests((c, b) -> {
                    ACTIVITIES.forEach(x -> b.suggest(x.id()));
                    return b.buildFuture();
                }).executes(ctx -> join(ctx.getSource().getPlayerOrThrow(), StringArgumentType.getString(ctx, "activity")))))));
    }

    private static void show(ServerPlayerEntity p) {
        p.sendMessage(Text.literal("The Lookout board").formatted(Formatting.GOLD, Formatting.BOLD), false);
        for (Activity a : ACTIVITIES) {
            int n = QUEUES.getOrDefault(a.id(), Set.of()).size();
            p.sendMessage(Text.literal("  " + a.id() + ": " + a.title() + "  (" + n + "/" + a.needed() + " waiting)"), false);
        }
        p.sendMessage(Text.literal("  /lookout join <activity>").formatted(Formatting.DARK_GRAY), false);
    }

    private static int join(ServerPlayerEntity p, String id) {
        Activity act = ACTIVITIES.stream().filter(x -> x.id().equals(id)).findFirst().orElse(null);
        if (act == null) {
            show(p);
            return 0;
        }
        if (act.id().equals("privateer")) {
            PrivateerWar.join(p);
            return 1;
        }
        QUEUES.values().forEach(q -> q.remove(p.getUuid()));
        Set<UUID> q = QUEUES.computeIfAbsent(act.id(), k -> new LinkedHashSet<>());
        q.add(p.getUuid());
        p.sendMessage(Text.literal("Your name's on the board for " + act.title() + " (" + q.size() + "/" + act.needed() + ").").formatted(Formatting.GOLD), false);
        if (q.size() < act.needed()) return 1;
        BlockPos where = location(p, act);
        for (UUID u : List.copyOf(q)) {
            ServerPlayerEntity m = p.getServer().getPlayerManager().getPlayer(u);
            if (m == null || where == null) continue;
            m.teleport(p.getServer().getOverworld(), where.getX() + 0.5, where.getY(), where.getZ() + 0.5, m.getYaw(), m.getPitch());
            m.sendMessage(Text.literal("The lookout calls: a crew's gathered for " + act.title() + "! Off you go.").formatted(Formatting.GOLD), false);
        }
        q.clear();
        return 1;
    }

    private static BlockPos location(ServerPlayerEntity p, Activity act) {
        for (Residents.Home h : Residents.HOMES) {
            if (h.role() != act.host()) continue;
            Archipelago.Island is = h.island();
            BlockPos at = Residents.openGround(p.getServer().getOverworld(), is.cx() + h.dx() + 2, is.cz() + h.dz() + 2, 8);
            if (at != null) return at;
            return new BlockPos(is.cx() + h.dx(), com.cursedtides.world.Builds.groundY(is.cx() + h.dx(), is.cz() + h.dz()), is.cz() + h.dz());
        }
        return null;
    }
}
