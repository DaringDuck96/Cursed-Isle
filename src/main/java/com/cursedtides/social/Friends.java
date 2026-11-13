package com.cursedtides.social;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/**
 * A friends list, as in the old pirate game: add a pirate (they must accept), see who's about and where, whisper
 * them, and go to them (not mid-fight, not from the brig, and not onto open water). Friends are told when you come
 * aboard or leave. Saved with the world.
 *
 *   /friend add <player>, /friend accept <name>, /friend remove <name>, /friend list (or /friends),
 *   /friend goto <name>, and /w <name> <message> to whisper.
 */
public class Friends extends SavedData {
    public static final int MAX = 100;

    private final Map<UUID, Set<UUID>> friends = new HashMap<>();
    private final Map<UUID, String> names = new HashMap<>();
    private final Map<UUID, Set<UUID>> requests = new HashMap<>();

    private static final net.minecraft.world.level.saveddata.SavedDataType<Friends> TYPE = com.cursedtides.util.Saved.type("cursedtides_friends", Friends::new, Friends::fromNbt, (st__, l__) -> st__.save(new CompoundTag(), l__));

    public static Friends get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public Set<UUID> of(UUID id) { return friends.getOrDefault(id, Set.of()); }

        public CompoundTag save(CompoundTag nbt, HolderLookup.Provider lookup) {
        ListTag list = new ListTag();
        for (var e : friends.entrySet()) {
            CompoundTag c = new CompoundTag();
            c.store("Id", net.minecraft.core.UUIDUtil.CODEC, e.getKey());
            c.putString("Name", names.getOrDefault(e.getKey(), "?"));
            ListTag fl = new ListTag();
            for (UUID f : e.getValue()) {
                CompoundTag fc = new CompoundTag();
                fc.store("Id", net.minecraft.core.UUIDUtil.CODEC, f);
                fl.add(fc);
            }
            c.put("Friends", fl);
            list.add(c);
        }
        nbt.put("Pirates", list);
        return nbt;
    }

    private static Friends fromNbt(CompoundTag nbt, HolderLookup.Provider lookup) {
        Friends s = new Friends();
        for (Tag el : nbt.getListOrEmpty("Pirates")) {
            CompoundTag c = (CompoundTag) el;
            UUID id = c.read("Id", net.minecraft.core.UUIDUtil.CODEC).orElse(null);
            s.names.put(id, c.getStringOr("Name", ""));
            Set<UUID> set = new LinkedHashSet<>();
            for (Tag fe : c.getListOrEmpty("Friends")) set.add(((CompoundTag) fe).read("Id", net.minecraft.core.UUIDUtil.CODEC).orElse(null));
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
                    .then(literal("add").then(argument("player", EntityArgument.player()).executes(Friends::add)))
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
            s.names.put(h.player.getUUID(), h.player.getName().getString());
            s.setDirty();
            announce(server, h.player, true);
        }));
        ServerPlayConnectionEvents.DISCONNECT.register((h, server) -> announce(server, h.player, false));
    }

    private static void announce(MinecraftServer server, ServerPlayer p, boolean on) {
        Friends s = get(server);
        for (UUID f : s.of(p.getUUID())) {
            ServerPlayer q = server.getPlayerList().getPlayer(f);
            if (q != null && q != p) q.sendSystemMessage(Component.literal("Your friend " + p.getName().getString() + (on ? " has come aboard." : " has gone ashore."))
                    .withStyle(on ? ChatFormatting.GREEN : ChatFormatting.GRAY), false);
        }
    }

    private static void tell(ServerPlayer p, String s, ChatFormatting f) { p.sendSystemMessage(Component.literal(s).withStyle(f), false); }

    private static int add(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        ServerPlayer t = EntityArgument.getPlayer(ctx, "player");
        Friends s = get(ctx.getSource().getServer());
        if (t == p) { tell(p, "You're already your own best friend.", ChatFormatting.GRAY); return 0; }
        if (s.of(p.getUUID()).contains(t.getUUID())) { tell(p, t.getName().getString() + " is already your friend.", ChatFormatting.GRAY); return 0; }
        if (s.of(p.getUUID()).size() >= MAX) { tell(p, "Your friends list is full.", ChatFormatting.RED); return 0; }
        // If they'd already asked you, that's a yes
        if (s.requests.getOrDefault(p.getUUID(), Set.of()).contains(t.getUUID())) return befriend(s, p, t);
        s.requests.computeIfAbsent(t.getUUID(), k -> new LinkedHashSet<>()).add(p.getUUID());
        tell(p, "Friend request sent to " + t.getName().getString() + ".", ChatFormatting.GREEN);
        t.sendSystemMessage(Component.literal(p.getName().getString() + " would like to be friends. ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal("[Accept]").withStyle(st -> st.withColor(ChatFormatting.GOLD)
                        .withClickEvent(new ClickEvent.RunCommand("/friend accept " + p.getName().getString())))), false);
        return 1;
    }

    private static int befriend(Friends s, ServerPlayer a, ServerPlayer b) {
        s.requests.getOrDefault(a.getUUID(), new LinkedHashSet<>()).remove(b.getUUID());
        s.requests.getOrDefault(b.getUUID(), new LinkedHashSet<>()).remove(a.getUUID());
        s.friends.computeIfAbsent(a.getUUID(), k -> new LinkedHashSet<>()).add(b.getUUID());
        s.friends.computeIfAbsent(b.getUUID(), k -> new LinkedHashSet<>()).add(a.getUUID());
        s.names.put(a.getUUID(), a.getName().getString());
        s.names.put(b.getUUID(), b.getName().getString());
        s.setDirty();
        tell(a, "You and " + b.getName().getString() + " are now friends.", ChatFormatting.GREEN);
        tell(b, "You and " + a.getName().getString() + " are now friends.", ChatFormatting.GREEN);
        return 1;
    }

    private static int accept(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        Friends s = get(ctx.getSource().getServer());
        String name = StringArgumentType.getString(ctx, "name");
        ServerPlayer t = ctx.getSource().getServer().getPlayerList().getPlayerByName(name);
        if (t == null || !s.requests.getOrDefault(p.getUUID(), Set.of()).contains(t.getUUID())) {
            tell(p, "No friend request from " + name + " (they need to be aboard).", ChatFormatting.GRAY);
            return 0;
        }
        return befriend(s, p, t);
    }

    private static int remove(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        Friends s = get(ctx.getSource().getServer());
        UUID id = s.byName(StringArgumentType.getString(ctx, "name"));
        if (id == null || !s.of(p.getUUID()).contains(id)) { tell(p, "They're not on your friends list.", ChatFormatting.GRAY); return 0; }
        s.friends.getOrDefault(p.getUUID(), new LinkedHashSet<>()).remove(id);
        s.friends.getOrDefault(id, new LinkedHashSet<>()).remove(p.getUUID());
        s.setDirty();
        tell(p, s.names.get(id) + " is off your friends list.", ChatFormatting.GRAY);
        return 1;
    }

    /** Where someone is, in words: an island, or the open sea. */
    private static String where(ServerPlayer q) {
        var col = com.cursedtides.world.Archipelago.column(q.getBlockX(), q.getBlockZ());
        if (q.getVehicle() instanceof com.cursedtides.entity.ShipEntity) return col.island() == null ? "at sea" : "sailing off " + col.island().name();
        if (col.island() != null) return "on " + col.island().name();
        for (var is : com.cursedtides.world.Archipelago.ISLANDS) if (is.edgeDistance(q.getX(), q.getZ()) < 60) return "near " + is.name();
        return "at sea";
    }

    private static int list(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        Friends s = get(ctx.getSource().getServer());
        Set<UUID> mine = s.of(p.getUUID());
        if (mine.isEmpty()) { tell(p, "No friends yet. /friend add <player> to ask someone.", ChatFormatting.GRAY); return 1; }
        p.sendSystemMessage(Component.literal("Friends (" + mine.size() + ")").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), false);
        for (UUID f : mine) {
            String name = s.names.getOrDefault(f, "?");
            ServerPlayer q = ctx.getSource().getServer().getPlayerList().getPlayer(f);
            MutableComponent line = Component.literal("  " + name).withStyle(q != null ? ChatFormatting.WHITE : ChatFormatting.DARK_GRAY);
            if (q != null) {
                line.append(Component.literal(" Lv " + com.cursedtides.pirate.PirateData.notoriety(q) + ", " + where(q) + "  ").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal("[Whisper]").withStyle(st -> st.withColor(ChatFormatting.AQUA)
                                .withClickEvent(new ClickEvent.SuggestCommand("/w " + name + " "))))
                        .append(Component.literal(" [Go to]").withStyle(st -> st.withColor(ChatFormatting.GOLD)
                                .withClickEvent(new ClickEvent.RunCommand("/friend goto " + name))));
            } else {
                line.append(Component.literal(" (ashore)").withStyle(ChatFormatting.DARK_GRAY));
            }
            p.sendSystemMessage(line, false);
        }
        return 1;
    }

    private static int go(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        Friends s = get(ctx.getSource().getServer());
        UUID id = s.byName(StringArgumentType.getString(ctx, "name"));
        ServerPlayer q = id == null ? null : ctx.getSource().getServer().getPlayerList().getPlayer(id);
        if (q == null || !s.of(p.getUUID()).contains(id)) { tell(p, "They're not a friend who's aboard right now.", ChatFormatting.GRAY); return 0; }
        if (!p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(16), m -> m.getTarget() == p).isEmpty()) {
            tell(p, "Not in the middle of a fight!", ChatFormatting.RED);
            return 0;
        }
        if (com.cursedtides.living.Brig.jailed(p)) { tell(p, "Not from the brig, you don't.", ChatFormatting.RED); return 0; }
        if (q.getVehicle() instanceof com.cursedtides.entity.ShipEntity || q.isInWater() || q.level() != p.level()) {
            tell(p, q.getName().getString() + " is at sea or somewhere you can't reach. Try again when they're ashore.", ChatFormatting.GRAY);
            return 0;
        }
        p.stopRiding();
        com.cursedtides.util.Compat.teleport(p, q.level(), q.getX(), q.getY(), q.getZ(), Set.of(), p.getYRot(), p.getXRot());
        tell(p, "You find your way to " + q.getName().getString() + ".", ChatFormatting.GREEN);
        tell(q, p.getName().getString() + " has come to find you.", ChatFormatting.GREEN);
        return 1;
    }

    private static int whisper(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        String name = StringArgumentType.getString(ctx, "name");
        ServerPlayer q = ctx.getSource().getServer().getPlayerList().getPlayerByName(name);
        if (q == null) { tell(p, name + " isn't aboard.", ChatFormatting.GRAY); return 0; }
        String msg = StringArgumentType.getString(ctx, "message");
        q.sendSystemMessage(Component.literal(p.getName().getString() + " whispers: ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)
                .append(Component.literal(msg).withStyle(ChatFormatting.WHITE))
                .withStyle(st -> st.withClickEvent(new ClickEvent.SuggestCommand("/w " + p.getName().getString() + " "))), false);
        tell(p, "You whisper to " + q.getName().getString() + ": " + msg, ChatFormatting.LIGHT_PURPLE);
        return 1;
    }
}
