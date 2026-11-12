package com.cursedtides.social;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * A friends list, as in the old pirate game: add a pirate (they must accept), see who's about and where, whisper
 * them, and go to them (not mid-fight, not from the brig, and not onto open water). Friends are told when you come
 * aboard or leave. Saved with the world.
 *
 *   /friend add <player>, /friend accept <name>, /friend remove <name>, /friend list (or /friends),
 *   /friend goto <name>, and /w <name> <message> to whisper.
 */
public class Friends extends PersistentState {
    public static final int MAX = 100;

    private final Map<UUID, Set<UUID>> friends = new HashMap<>();
    private final Map<UUID, String> names = new HashMap<>();
    private final Map<UUID, Set<UUID>> requests = new HashMap<>();

    private static final Type<Friends> TYPE = new Type<>(Friends::new, Friends::fromNbt, null);

    public static Friends get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, "cursedtides_friends");
    }

    public Set<UUID> of(UUID id) { return friends.getOrDefault(id, Set.of()); }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        NbtList list = new NbtList();
        for (var e : friends.entrySet()) {
            NbtCompound c = new NbtCompound();
            c.putUuid("Id", e.getKey());
            c.putString("Name", names.getOrDefault(e.getKey(), "?"));
            NbtList fl = new NbtList();
            for (UUID f : e.getValue()) {
                NbtCompound fc = new NbtCompound();
                fc.putUuid("Id", f);
                fl.add(fc);
            }
            c.put("Friends", fl);
            list.add(c);
        }
        nbt.put("Pirates", list);
        return nbt;
    }

    private static Friends fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        Friends s = new Friends();
        for (NbtElement el : nbt.getList("Pirates", NbtElement.COMPOUND_TYPE)) {
            NbtCompound c = (NbtCompound) el;
            UUID id = c.getUuid("Id");
            s.names.put(id, c.getString("Name"));
            Set<UUID> set = new LinkedHashSet<>();
            for (NbtElement fe : c.getList("Friends", NbtElement.COMPOUND_TYPE)) set.add(((NbtCompound) fe).getUuid("Id"));
            s.friends.put(id, set);
        }
        return s;
    }

    private UUID byName(String name) {
        for (var e : names.entrySet()) if (e.getValue().equalsIgnoreCase(name)) return e.getKey();
        return null;
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((d, a, env) -> {
            d.register(literal("friend")
                    .then(literal("add").then(argument("player", EntityArgumentType.player()).executes(Friends::add)))
                    .then(literal("accept").then(argument("name", StringArgumentType.word()).executes(Friends::accept)))
                    .then(literal("remove").then(argument("name", StringArgumentType.word()).executes(Friends::remove)))
                    .then(literal("list").executes(Friends::list))
                    .then(literal("goto").then(argument("name", StringArgumentType.word()).executes(Friends::go))));
            d.register(literal("friends").executes(Friends::list));
            d.register(literal("w").then(argument("name", StringArgumentType.word())
                    .then(argument("message", StringArgumentType.greedyString()).executes(Friends::whisper))));
        });
        ServerPlayConnectionEvents.JOIN.register((h, sender, server) -> server.execute(() -> {
            Friends s = get(server);
            s.names.put(h.player.getUuid(), h.player.getName().getString());
            s.markDirty();
            announce(server, h.player, true);
        }));
        ServerPlayConnectionEvents.DISCONNECT.register((h, server) -> announce(server, h.player, false));
    }

    private static void announce(MinecraftServer server, ServerPlayerEntity p, boolean on) {
        Friends s = get(server);
        for (UUID f : s.of(p.getUuid())) {
            ServerPlayerEntity q = server.getPlayerManager().getPlayer(f);
            if (q != null && q != p) q.sendMessage(Text.literal("Your friend " + p.getName().getString() + (on ? " has come aboard." : " has gone ashore."))
                    .formatted(on ? Formatting.GREEN : Formatting.GRAY), false);
        }
    }

    private static void tell(ServerPlayerEntity p, String s, Formatting f) { p.sendMessage(Text.literal(s).formatted(f), false); }

    private static int add(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        ServerPlayerEntity t = EntityArgumentType.getPlayer(ctx, "player");
        Friends s = get(ctx.getSource().getServer());
        if (t == p) { tell(p, "You're already your own best friend.", Formatting.GRAY); return 0; }
        if (s.of(p.getUuid()).contains(t.getUuid())) { tell(p, t.getName().getString() + " is already your friend.", Formatting.GRAY); return 0; }
        if (s.of(p.getUuid()).size() >= MAX) { tell(p, "Your friends list is full.", Formatting.RED); return 0; }
        // If they'd already asked you, that's a yes
        if (s.requests.getOrDefault(p.getUuid(), Set.of()).contains(t.getUuid())) return befriend(s, p, t);
        s.requests.computeIfAbsent(t.getUuid(), k -> new LinkedHashSet<>()).add(p.getUuid());
        tell(p, "Friend request sent to " + t.getName().getString() + ".", Formatting.GREEN);
        t.sendMessage(Text.literal(p.getName().getString() + " would like to be friends. ").formatted(Formatting.GREEN)
                .append(Text.literal("[Accept]").styled(st -> st.withColor(Formatting.GOLD)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend accept " + p.getName().getString())))), false);
        return 1;
    }

    private static int befriend(Friends s, ServerPlayerEntity a, ServerPlayerEntity b) {
        s.requests.getOrDefault(a.getUuid(), new LinkedHashSet<>()).remove(b.getUuid());
        s.requests.getOrDefault(b.getUuid(), new LinkedHashSet<>()).remove(a.getUuid());
        s.friends.computeIfAbsent(a.getUuid(), k -> new LinkedHashSet<>()).add(b.getUuid());
        s.friends.computeIfAbsent(b.getUuid(), k -> new LinkedHashSet<>()).add(a.getUuid());
        s.names.put(a.getUuid(), a.getName().getString());
        s.names.put(b.getUuid(), b.getName().getString());
        s.markDirty();
        tell(a, "You and " + b.getName().getString() + " are now friends.", Formatting.GREEN);
        tell(b, "You and " + a.getName().getString() + " are now friends.", Formatting.GREEN);
        return 1;
    }

    private static int accept(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        Friends s = get(ctx.getSource().getServer());
        String name = StringArgumentType.getString(ctx, "name");
        ServerPlayerEntity t = ctx.getSource().getServer().getPlayerManager().getPlayer(name);
        if (t == null || !s.requests.getOrDefault(p.getUuid(), Set.of()).contains(t.getUuid())) {
            tell(p, "No friend request from " + name + " (they need to be aboard).", Formatting.GRAY);
            return 0;
        }
        return befriend(s, p, t);
    }

    private static int remove(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        Friends s = get(ctx.getSource().getServer());
        UUID id = s.byName(StringArgumentType.getString(ctx, "name"));
        if (id == null || !s.of(p.getUuid()).contains(id)) { tell(p, "They're not on your friends list.", Formatting.GRAY); return 0; }
        s.friends.getOrDefault(p.getUuid(), new LinkedHashSet<>()).remove(id);
        s.friends.getOrDefault(id, new LinkedHashSet<>()).remove(p.getUuid());
        s.markDirty();
        tell(p, s.names.get(id) + " is off your friends list.", Formatting.GRAY);
        return 1;
    }

    /** Where someone is, in words: an island, or the open sea. */
    private static String where(ServerPlayerEntity q) {
        var col = com.cursedtides.world.Archipelago.column(q.getBlockX(), q.getBlockZ());
        if (q.getVehicle() instanceof com.cursedtides.entity.ShipEntity) return col.island() == null ? "at sea" : "sailing off " + col.island().name();
        if (col.island() != null) return "on " + col.island().name();
        for (var is : com.cursedtides.world.Archipelago.ISLANDS) if (is.edgeDistance(q.getX(), q.getZ()) < 60) return "near " + is.name();
        return "at sea";
    }

    private static int list(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        Friends s = get(ctx.getSource().getServer());
        Set<UUID> mine = s.of(p.getUuid());
        if (mine.isEmpty()) { tell(p, "No friends yet. /friend add <player> to ask someone.", Formatting.GRAY); return 1; }
        p.sendMessage(Text.literal("Friends (" + mine.size() + ")").formatted(Formatting.GREEN, Formatting.BOLD), false);
        for (UUID f : mine) {
            String name = s.names.getOrDefault(f, "?");
            ServerPlayerEntity q = ctx.getSource().getServer().getPlayerManager().getPlayer(f);
            MutableText line = Text.literal("  " + name).formatted(q != null ? Formatting.WHITE : Formatting.DARK_GRAY);
            if (q != null) {
                line.append(Text.literal(" Lv " + com.cursedtides.pirate.PirateData.notoriety(q) + ", " + where(q) + "  ").formatted(Formatting.GRAY))
                        .append(Text.literal("[Whisper]").styled(st -> st.withColor(Formatting.AQUA)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/w " + name + " "))))
                        .append(Text.literal(" [Go to]").styled(st -> st.withColor(Formatting.GOLD)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend goto " + name))));
            } else {
                line.append(Text.literal(" (ashore)").formatted(Formatting.DARK_GRAY));
            }
            p.sendMessage(line, false);
        }
        return 1;
    }

    private static int go(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        Friends s = get(ctx.getSource().getServer());
        UUID id = s.byName(StringArgumentType.getString(ctx, "name"));
        ServerPlayerEntity q = id == null ? null : ctx.getSource().getServer().getPlayerManager().getPlayer(id);
        if (q == null || !s.of(p.getUuid()).contains(id)) { tell(p, "They're not a friend who's aboard right now.", Formatting.GRAY); return 0; }
        if (!p.getServerWorld().getEntitiesByClass(MobEntity.class, p.getBoundingBox().expand(16), m -> m.getTarget() == p).isEmpty()) {
            tell(p, "Not in the middle of a fight!", Formatting.RED);
            return 0;
        }
        if (com.cursedtides.living.Brig.jailed(p)) { tell(p, "Not from the brig, you don't.", Formatting.RED); return 0; }
        if (q.getVehicle() instanceof com.cursedtides.entity.ShipEntity || q.isTouchingWater() || q.getWorld() != p.getWorld()) {
            tell(p, q.getName().getString() + " is at sea or somewhere you can't reach. Try again when they're ashore.", Formatting.GRAY);
            return 0;
        }
        p.stopRiding();
        p.teleport(q.getServerWorld(), q.getX(), q.getY(), q.getZ(), Set.of(), p.getYaw(), p.getPitch());
        tell(p, "You find your way to " + q.getName().getString() + ".", Formatting.GREEN);
        tell(q, p.getName().getString() + " has come to find you.", Formatting.GREEN);
        return 1;
    }

    private static int whisper(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        String name = StringArgumentType.getString(ctx, "name");
        ServerPlayerEntity q = ctx.getSource().getServer().getPlayerManager().getPlayer(name);
        if (q == null) { tell(p, name + " isn't aboard.", Formatting.GRAY); return 0; }
        String msg = StringArgumentType.getString(ctx, "message");
        q.sendMessage(Text.literal(p.getName().getString() + " whispers: ").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC)
                .append(Text.literal(msg).formatted(Formatting.WHITE))
                .styled(st -> st.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/w " + p.getName().getString() + " "))), false);
        tell(p, "You whisper to " + q.getName().getString() + ": " + msg, Formatting.LIGHT_PURPLE);
        return 1;
    }
}
