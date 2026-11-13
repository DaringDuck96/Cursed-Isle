package com.cursedtides.social;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/**
 * Guilds, as in the old pirate game: big, lasting companies of pirates (crews are for sailing together; a guild is
 * who you belong to). A name over your head, ranks from Member up to Guildmaster, a message of the day and a guild
 * chat that reaches every member wherever they are. Saved with the world.
 *
 *   /guild create <name>, /guild invite <player>, /guild accept, /guild leave, /guild info, /guild list,
 *   /guild promote|demote|kick <player>, /guild motd <text>, /guild disband; and /g <message> to talk.
 */
public class Guilds extends SavedData {
    public static final int MAX_MEMBERS = 500;
    public static final String[] RANKS = {"Member", "Veteran", "Officer", "Guildmaster"};

    public static final class Guild {
        public final String name;
        public final Map<UUID, Integer> ranks = new LinkedHashMap<>();
        public final Map<UUID, String> names = new HashMap<>();
        public String motd = "";

        Guild(String name) { this.name = name; }

        public UUID master() {
            for (var e : ranks.entrySet()) if (e.getValue() == 3) return e.getKey();
            return null;
        }
    }

    private final Map<String, Guild> guilds = new LinkedHashMap<>();
    private final Map<UUID, Guild> byMember = new HashMap<>();
    private final Map<UUID, String> invites = new HashMap<>();

    private static final Factory<Guilds> TYPE = new Factory<>(Guilds::new, Guilds::fromNbt, null);

    public static Guilds get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE, "cursedtides_guilds");
    }

    @Nullable
    public Guild of(UUID player) { return byMember.get(player); }

    @Nullable
    public static String nameOf(MinecraftServer server, UUID player) {
        Guild g = get(server).of(player);
        return g == null ? null : g.name;
    }

    // ------------------------------------------------------------------ saving

    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider lookup) {
        ListTag list = new ListTag();
        for (Guild g : guilds.values()) {
            CompoundTag c = new CompoundTag();
            c.putString("Name", g.name);
            c.putString("Motd", g.motd);
            ListTag members = new ListTag();
            for (var e : g.ranks.entrySet()) {
                CompoundTag m = new CompoundTag();
                m.putUUID("Id", e.getKey());
                m.putInt("Rank", e.getValue());
                m.putString("Name", g.names.getOrDefault(e.getKey(), "?"));
                members.add(m);
            }
            c.put("Members", members);
            list.add(c);
        }
        nbt.put("Guilds", list);
        return nbt;
    }

    private static Guilds fromNbt(CompoundTag nbt, HolderLookup.Provider lookup) {
        Guilds s = new Guilds();
        for (Tag el : nbt.getList("Guilds", Tag.TAG_COMPOUND)) {
            CompoundTag c = (CompoundTag) el;
            Guild g = new Guild(c.getString("Name"));
            g.motd = c.getString("Motd");
            for (Tag me : c.getList("Members", Tag.TAG_COMPOUND)) {
                CompoundTag m = (CompoundTag) me;
                UUID id = m.getUUID("Id");
                g.ranks.put(id, m.getInt("Rank"));
                g.names.put(id, m.getString("Name"));
                s.byMember.put(id, g);
            }
            s.guilds.put(g.name.toLowerCase(), g);
        }
        return s;
    }

    // ------------------------------------------------------------------ commands

    public static void register() {
        CommandRegistrationCallback.EVENT.register((d, access, env) -> {
            d.register(literal("guild")
                    .then(literal("create").then(argument("name", StringArgumentType.greedyString()).executes(Guilds::create)))
                    .then(literal("invite").then(argument("player", EntityArgument.player()).executes(Guilds::invite)))
                    .then(literal("accept").executes(Guilds::accept))
                    .then(literal("leave").executes(Guilds::leave))
                    .then(literal("info").executes(Guilds::info))
                    .then(literal("list").executes(Guilds::list))
                    .then(literal("promote").then(argument("player", EntityArgument.player()).executes(c -> rank(c, 1))))
                    .then(literal("demote").then(argument("player", EntityArgument.player()).executes(c -> rank(c, -1))))
                    .then(literal("kick").then(argument("player", EntityArgument.player()).executes(Guilds::kick)))
                    .then(literal("motd").then(argument("text", StringArgumentType.greedyString()).executes(Guilds::motd)))
                    .then(literal("disband").executes(Guilds::disband)));
            d.register(literal("g").then(argument("message", StringArgumentType.greedyString()).executes(Guilds::chat)));
        });
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register((h, sender, server) -> {
            Guild g = get(server).of(h.player.getUUID());
            if (g != null) {
                g.names.put(h.player.getUUID(), h.player.getName().getString());
                if (!g.motd.isEmpty()) h.player.displayClientMessage(Component.literal("[" + g.name + "] ").withStyle(ChatFormatting.DARK_GREEN)
                        .append(Component.literal(g.motd).withStyle(ChatFormatting.GREEN)), false);
            }
        });
    }

    private static void tell(ServerPlayer p, String s, ChatFormatting f) { p.displayClientMessage(Component.literal(s).withStyle(f), false); }

    private void broadcast(MinecraftServer server, Guild g, Component msg) {
        for (UUID id : g.ranks.keySet()) {
            ServerPlayer m = server.getPlayerList().getPlayer(id);
            if (m != null) m.displayClientMessage(msg, false);
        }
    }

    private static int create(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        Guilds s = get(ctx.getSource().getServer());
        String name = StringArgumentType.getString(ctx, "name").trim();
        if (s.of(p.getUUID()) != null) { tell(p, "You're already in a guild. /guild leave first.", ChatFormatting.RED); return 0; }
        if (name.length() < 3 || name.length() > 24 || !name.matches("[A-Za-z0-9' -]+")) {
            tell(p, "A guild name is 3 to 24 letters, numbers, spaces or apostrophes.", ChatFormatting.RED);
            return 0;
        }
        if (s.guilds.containsKey(name.toLowerCase())) { tell(p, "There's already a guild by that name.", ChatFormatting.RED); return 0; }
        Guild g = new Guild(name);
        g.ranks.put(p.getUUID(), 3);
        g.names.put(p.getUUID(), p.getName().getString());
        s.guilds.put(name.toLowerCase(), g);
        s.byMember.put(p.getUUID(), g);
        s.setDirty();
        tell(p, "You've founded the guild \"" + name + "\". Invite pirates with /guild invite <player>; talk with /g <message>.", ChatFormatting.GREEN);
        Nameplates.refresh(p);
        return 1;
    }

    private static int invite(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        ServerPlayer t = EntityArgument.getPlayer(ctx, "player");
        Guilds s = get(ctx.getSource().getServer());
        Guild g = s.of(p.getUUID());
        if (g == null || g.ranks.get(p.getUUID()) < 2) { tell(p, "Only a guild's officers can invite.", ChatFormatting.RED); return 0; }
        if (s.of(t.getUUID()) != null) { tell(p, t.getName().getString() + " is already in a guild.", ChatFormatting.RED); return 0; }
        if (g.ranks.size() >= MAX_MEMBERS) { tell(p, "The guild is full.", ChatFormatting.RED); return 0; }
        s.invites.put(t.getUUID(), g.name.toLowerCase());
        tell(p, "Invitation sent to " + t.getName().getString() + ".", ChatFormatting.GREEN);
        t.displayClientMessage(Component.literal(p.getName().getString() + " invites you to join the guild \"" + g.name + "\". ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal("[Join]").withStyle(st -> st.withColor(ChatFormatting.GOLD).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/guild accept")))), false);
        return 1;
    }

    private static int accept(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        Guilds s = get(ctx.getSource().getServer());
        String key = s.invites.remove(p.getUUID());
        Guild g = key == null ? null : s.guilds.get(key);
        if (g == null) { tell(p, "No guild invitation is waiting for you.", ChatFormatting.GRAY); return 0; }
        if (s.of(p.getUUID()) != null) { tell(p, "You're already in a guild.", ChatFormatting.RED); return 0; }
        g.ranks.put(p.getUUID(), 0);
        g.names.put(p.getUUID(), p.getName().getString());
        s.byMember.put(p.getUUID(), g);
        s.setDirty();
        s.broadcast(ctx.getSource().getServer(), g, Component.literal(p.getName().getString() + " has joined " + g.name + "!").withStyle(ChatFormatting.GREEN));
        Nameplates.refresh(p);
        return 1;
    }

    private static int leave(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        Guilds s = get(ctx.getSource().getServer());
        Guild g = s.of(p.getUUID());
        if (g == null) { tell(p, "You're not in a guild.", ChatFormatting.GRAY); return 0; }
        boolean master = g.ranks.get(p.getUUID()) == 3;
        g.ranks.remove(p.getUUID());
        s.byMember.remove(p.getUUID());
        if (g.ranks.isEmpty()) s.guilds.remove(g.name.toLowerCase());
        else if (master) {
            // The longest-serving officer (or member) takes the helm
            UUID heir = null;
            int best = -1;
            for (var e : g.ranks.entrySet()) if (e.getValue() > best) { best = e.getValue(); heir = e.getKey(); }
            g.ranks.put(heir, 3);
            s.broadcast(ctx.getSource().getServer(), g, Component.literal(g.names.getOrDefault(heir, "Someone") + " is the new Guildmaster.").withStyle(ChatFormatting.GOLD));
        }
        s.setDirty();
        tell(p, "You've left \"" + g.name + "\".", ChatFormatting.GRAY);
        s.broadcast(ctx.getSource().getServer(), g, Component.literal(p.getName().getString() + " has left the guild.").withStyle(ChatFormatting.GRAY));
        Nameplates.refresh(p);
        return 1;
    }

    private static int info(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        Guild g = get(ctx.getSource().getServer()).of(p.getUUID());
        if (g == null) { tell(p, "You're not in a guild. /guild create <name>, or wait for an invitation.", ChatFormatting.GRAY); return 0; }
        p.displayClientMessage(Component.literal("Guild \"" + g.name + "\" (" + g.ranks.size() + " members)").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD), false);
        if (!g.motd.isEmpty()) tell(p, "Message of the day: " + g.motd, ChatFormatting.GREEN);
        List<UUID> ids = new ArrayList<>(g.ranks.keySet());
        ids.sort((a, b) -> g.ranks.get(b) - g.ranks.get(a));
        for (UUID id : ids) {
            boolean online = ctx.getSource().getServer().getPlayerList().getPlayer(id) != null;
            p.displayClientMessage(Component.literal("  " + RANKS[g.ranks.get(id)] + ": ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(g.names.getOrDefault(id, "?")).withStyle(online ? ChatFormatting.WHITE : ChatFormatting.DARK_GRAY)), false);
        }
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        Guilds s = get(ctx.getSource().getServer());
        if (s.guilds.isEmpty()) { tell(p, "No guilds yet. Found the first with /guild create <name>.", ChatFormatting.GRAY); return 1; }
        for (Guild g : s.guilds.values()) tell(p, "  " + g.name + " (" + g.ranks.size() + ")", ChatFormatting.GREEN);
        return 1;
    }

    private static int rank(CommandContext<CommandSourceStack> ctx, int delta) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        ServerPlayer t = EntityArgument.getPlayer(ctx, "player");
        Guilds s = get(ctx.getSource().getServer());
        Guild g = s.of(p.getUUID());
        if (g == null || s.of(t.getUUID()) != g || t == p) { tell(p, "They're not in your guild.", ChatFormatting.RED); return 0; }
        int mine = g.ranks.get(p.getUUID()), theirs = g.ranks.get(t.getUUID());
        if (mine < 2 || theirs >= mine) { tell(p, "You can only change the rank of those below you.", ChatFormatting.RED); return 0; }
        int now = Math.max(0, Math.min(mine == 3 ? 2 : 1, theirs + delta));
        g.ranks.put(t.getUUID(), now);
        s.setDirty();
        s.broadcast(ctx.getSource().getServer(), g, Component.literal(t.getName().getString() + " is now " + RANKS[now] + ".").withStyle(ChatFormatting.GOLD));
        return 1;
    }

    private static int kick(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        ServerPlayer t = EntityArgument.getPlayer(ctx, "player");
        Guilds s = get(ctx.getSource().getServer());
        Guild g = s.of(p.getUUID());
        if (g == null || s.of(t.getUUID()) != g || t == p) { tell(p, "They're not in your guild.", ChatFormatting.RED); return 0; }
        if (g.ranks.get(p.getUUID()) < 2 || g.ranks.get(t.getUUID()) >= g.ranks.get(p.getUUID())) { tell(p, "You can't remove them.", ChatFormatting.RED); return 0; }
        g.ranks.remove(t.getUUID());
        s.byMember.remove(t.getUUID());
        s.setDirty();
        tell(t, "You've been removed from \"" + g.name + "\".", ChatFormatting.RED);
        s.broadcast(ctx.getSource().getServer(), g, Component.literal(t.getName().getString() + " has been removed from the guild.").withStyle(ChatFormatting.GRAY));
        Nameplates.refresh(t);
        return 1;
    }

    private static int motd(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        Guilds s = get(ctx.getSource().getServer());
        Guild g = s.of(p.getUUID());
        if (g == null || g.ranks.get(p.getUUID()) < 2) { tell(p, "Only officers can set the message of the day.", ChatFormatting.RED); return 0; }
        g.motd = StringArgumentType.getString(ctx, "text");
        s.setDirty();
        s.broadcast(ctx.getSource().getServer(), g, Component.literal("[" + g.name + "] ").withStyle(ChatFormatting.DARK_GREEN).append(Component.literal(g.motd).withStyle(ChatFormatting.GREEN)));
        return 1;
    }

    private static int disband(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        Guilds s = get(ctx.getSource().getServer());
        Guild g = s.of(p.getUUID());
        if (g == null || g.ranks.get(p.getUUID()) != 3) { tell(p, "Only the Guildmaster can disband the guild.", ChatFormatting.RED); return 0; }
        s.broadcast(ctx.getSource().getServer(), g, Component.literal("The guild \"" + g.name + "\" has been disbanded.").withStyle(ChatFormatting.RED));
        for (UUID id : g.ranks.keySet()) {
            s.byMember.remove(id);
            ServerPlayer m = ctx.getSource().getServer().getPlayerList().getPlayer(id);
            if (m != null) Nameplates.refresh(m);
        }
        s.guilds.remove(g.name.toLowerCase());
        s.setDirty();
        return 1;
    }

    private static int chat(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        Guilds s = get(ctx.getSource().getServer());
        Guild g = s.of(p.getUUID());
        if (g == null) { tell(p, "You're not in a guild.", ChatFormatting.GRAY); return 0; }
        s.broadcast(ctx.getSource().getServer(), g, Component.literal("[" + g.name + "] ").withStyle(ChatFormatting.DARK_GREEN)
                .append(Component.literal(p.getName().getString() + ": ").withStyle(ChatFormatting.GREEN))
                .append(Component.literal(StringArgumentType.getString(ctx, "message")).withStyle(ChatFormatting.WHITE)));
        return 1;
    }
}
