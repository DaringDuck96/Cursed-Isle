package com.cursedtides.social;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Guilds, as in the old pirate game: big, lasting companies of pirates (crews are for sailing together; a guild is
 * who you belong to). A name over your head, ranks from Member up to Guildmaster, a message of the day and a guild
 * chat that reaches every member wherever they are. Saved with the world.
 *
 *   /guild create <name>, /guild invite <player>, /guild accept, /guild leave, /guild info, /guild list,
 *   /guild promote|demote|kick <player>, /guild motd <text>, /guild disband; and /g <message> to talk.
 */
public class Guilds extends PersistentState {
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

    private static final Type<Guilds> TYPE = new Type<>(Guilds::new, Guilds::fromNbt, null);

    public static Guilds get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, "cursedtides_guilds");
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
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        NbtList list = new NbtList();
        for (Guild g : guilds.values()) {
            NbtCompound c = new NbtCompound();
            c.putString("Name", g.name);
            c.putString("Motd", g.motd);
            NbtList members = new NbtList();
            for (var e : g.ranks.entrySet()) {
                NbtCompound m = new NbtCompound();
                m.putUuid("Id", e.getKey());
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

    private static Guilds fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        Guilds s = new Guilds();
        for (NbtElement el : nbt.getList("Guilds", NbtElement.COMPOUND_TYPE)) {
            NbtCompound c = (NbtCompound) el;
            Guild g = new Guild(c.getString("Name"));
            g.motd = c.getString("Motd");
            for (NbtElement me : c.getList("Members", NbtElement.COMPOUND_TYPE)) {
                NbtCompound m = (NbtCompound) me;
                UUID id = m.getUuid("Id");
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
                    .then(literal("invite").then(argument("player", EntityArgumentType.player()).executes(Guilds::invite)))
                    .then(literal("accept").executes(Guilds::accept))
                    .then(literal("leave").executes(Guilds::leave))
                    .then(literal("info").executes(Guilds::info))
                    .then(literal("list").executes(Guilds::list))
                    .then(literal("promote").then(argument("player", EntityArgumentType.player()).executes(c -> rank(c, 1))))
                    .then(literal("demote").then(argument("player", EntityArgumentType.player()).executes(c -> rank(c, -1))))
                    .then(literal("kick").then(argument("player", EntityArgumentType.player()).executes(Guilds::kick)))
                    .then(literal("motd").then(argument("text", StringArgumentType.greedyString()).executes(Guilds::motd)))
                    .then(literal("disband").executes(Guilds::disband)));
            d.register(literal("g").then(argument("message", StringArgumentType.greedyString()).executes(Guilds::chat)));
        });
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register((h, sender, server) -> {
            Guild g = get(server).of(h.player.getUuid());
            if (g != null) {
                g.names.put(h.player.getUuid(), h.player.getName().getString());
                if (!g.motd.isEmpty()) h.player.sendMessage(Text.literal("[" + g.name + "] ").formatted(Formatting.DARK_GREEN)
                        .append(Text.literal(g.motd).formatted(Formatting.GREEN)), false);
            }
        });
    }

    private static void tell(ServerPlayerEntity p, String s, Formatting f) { p.sendMessage(Text.literal(s).formatted(f), false); }

    private void broadcast(MinecraftServer server, Guild g, Text msg) {
        for (UUID id : g.ranks.keySet()) {
            ServerPlayerEntity m = server.getPlayerManager().getPlayer(id);
            if (m != null) m.sendMessage(msg, false);
        }
    }

    private static int create(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        Guilds s = get(ctx.getSource().getServer());
        String name = StringArgumentType.getString(ctx, "name").trim();
        if (s.of(p.getUuid()) != null) { tell(p, "You're already in a guild. /guild leave first.", Formatting.RED); return 0; }
        if (name.length() < 3 || name.length() > 24 || !name.matches("[A-Za-z0-9' -]+")) {
            tell(p, "A guild name is 3 to 24 letters, numbers, spaces or apostrophes.", Formatting.RED);
            return 0;
        }
        if (s.guilds.containsKey(name.toLowerCase())) { tell(p, "There's already a guild by that name.", Formatting.RED); return 0; }
        Guild g = new Guild(name);
        g.ranks.put(p.getUuid(), 3);
        g.names.put(p.getUuid(), p.getName().getString());
        s.guilds.put(name.toLowerCase(), g);
        s.byMember.put(p.getUuid(), g);
        s.markDirty();
        tell(p, "You've founded the guild \"" + name + "\". Invite pirates with /guild invite <player>; talk with /g <message>.", Formatting.GREEN);
        Nameplates.refresh(p);
        return 1;
    }

    private static int invite(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        ServerPlayerEntity t = EntityArgumentType.getPlayer(ctx, "player");
        Guilds s = get(ctx.getSource().getServer());
        Guild g = s.of(p.getUuid());
        if (g == null || g.ranks.get(p.getUuid()) < 2) { tell(p, "Only a guild's officers can invite.", Formatting.RED); return 0; }
        if (s.of(t.getUuid()) != null) { tell(p, t.getName().getString() + " is already in a guild.", Formatting.RED); return 0; }
        if (g.ranks.size() >= MAX_MEMBERS) { tell(p, "The guild is full.", Formatting.RED); return 0; }
        s.invites.put(t.getUuid(), g.name.toLowerCase());
        tell(p, "Invitation sent to " + t.getName().getString() + ".", Formatting.GREEN);
        t.sendMessage(Text.literal(p.getName().getString() + " invites you to join the guild \"" + g.name + "\". ").formatted(Formatting.GREEN)
                .append(Text.literal("[Join]").styled(st -> st.withColor(Formatting.GOLD).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/guild accept")))), false);
        return 1;
    }

    private static int accept(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        Guilds s = get(ctx.getSource().getServer());
        String key = s.invites.remove(p.getUuid());
        Guild g = key == null ? null : s.guilds.get(key);
        if (g == null) { tell(p, "No guild invitation is waiting for you.", Formatting.GRAY); return 0; }
        if (s.of(p.getUuid()) != null) { tell(p, "You're already in a guild.", Formatting.RED); return 0; }
        g.ranks.put(p.getUuid(), 0);
        g.names.put(p.getUuid(), p.getName().getString());
        s.byMember.put(p.getUuid(), g);
        s.markDirty();
        s.broadcast(ctx.getSource().getServer(), g, Text.literal(p.getName().getString() + " has joined " + g.name + "!").formatted(Formatting.GREEN));
        Nameplates.refresh(p);
        return 1;
    }

    private static int leave(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        Guilds s = get(ctx.getSource().getServer());
        Guild g = s.of(p.getUuid());
        if (g == null) { tell(p, "You're not in a guild.", Formatting.GRAY); return 0; }
        boolean master = g.ranks.get(p.getUuid()) == 3;
        g.ranks.remove(p.getUuid());
        s.byMember.remove(p.getUuid());
        if (g.ranks.isEmpty()) s.guilds.remove(g.name.toLowerCase());
        else if (master) {
            // The longest-serving officer (or member) takes the helm
            UUID heir = null;
            int best = -1;
            for (var e : g.ranks.entrySet()) if (e.getValue() > best) { best = e.getValue(); heir = e.getKey(); }
            g.ranks.put(heir, 3);
            s.broadcast(ctx.getSource().getServer(), g, Text.literal(g.names.getOrDefault(heir, "Someone") + " is the new Guildmaster.").formatted(Formatting.GOLD));
        }
        s.markDirty();
        tell(p, "You've left \"" + g.name + "\".", Formatting.GRAY);
        s.broadcast(ctx.getSource().getServer(), g, Text.literal(p.getName().getString() + " has left the guild.").formatted(Formatting.GRAY));
        Nameplates.refresh(p);
        return 1;
    }

    private static int info(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        Guild g = get(ctx.getSource().getServer()).of(p.getUuid());
        if (g == null) { tell(p, "You're not in a guild. /guild create <name>, or wait for an invitation.", Formatting.GRAY); return 0; }
        p.sendMessage(Text.literal("Guild \"" + g.name + "\" (" + g.ranks.size() + " members)").formatted(Formatting.DARK_GREEN, Formatting.BOLD), false);
        if (!g.motd.isEmpty()) tell(p, "Message of the day: " + g.motd, Formatting.GREEN);
        List<UUID> ids = new ArrayList<>(g.ranks.keySet());
        ids.sort((a, b) -> g.ranks.get(b) - g.ranks.get(a));
        for (UUID id : ids) {
            boolean online = ctx.getSource().getServer().getPlayerManager().getPlayer(id) != null;
            p.sendMessage(Text.literal("  " + RANKS[g.ranks.get(id)] + ": ").formatted(Formatting.GRAY)
                    .append(Text.literal(g.names.getOrDefault(id, "?")).formatted(online ? Formatting.WHITE : Formatting.DARK_GRAY)), false);
        }
        return 1;
    }

    private static int list(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        Guilds s = get(ctx.getSource().getServer());
        if (s.guilds.isEmpty()) { tell(p, "No guilds yet. Found the first with /guild create <name>.", Formatting.GRAY); return 1; }
        for (Guild g : s.guilds.values()) tell(p, "  " + g.name + " (" + g.ranks.size() + ")", Formatting.GREEN);
        return 1;
    }

    private static int rank(CommandContext<ServerCommandSource> ctx, int delta) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        ServerPlayerEntity t = EntityArgumentType.getPlayer(ctx, "player");
        Guilds s = get(ctx.getSource().getServer());
        Guild g = s.of(p.getUuid());
        if (g == null || s.of(t.getUuid()) != g || t == p) { tell(p, "They're not in your guild.", Formatting.RED); return 0; }
        int mine = g.ranks.get(p.getUuid()), theirs = g.ranks.get(t.getUuid());
        if (mine < 2 || theirs >= mine) { tell(p, "You can only change the rank of those below you.", Formatting.RED); return 0; }
        int now = Math.max(0, Math.min(mine == 3 ? 2 : 1, theirs + delta));
        g.ranks.put(t.getUuid(), now);
        s.markDirty();
        s.broadcast(ctx.getSource().getServer(), g, Text.literal(t.getName().getString() + " is now " + RANKS[now] + ".").formatted(Formatting.GOLD));
        return 1;
    }

    private static int kick(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        ServerPlayerEntity t = EntityArgumentType.getPlayer(ctx, "player");
        Guilds s = get(ctx.getSource().getServer());
        Guild g = s.of(p.getUuid());
        if (g == null || s.of(t.getUuid()) != g || t == p) { tell(p, "They're not in your guild.", Formatting.RED); return 0; }
        if (g.ranks.get(p.getUuid()) < 2 || g.ranks.get(t.getUuid()) >= g.ranks.get(p.getUuid())) { tell(p, "You can't remove them.", Formatting.RED); return 0; }
        g.ranks.remove(t.getUuid());
        s.byMember.remove(t.getUuid());
        s.markDirty();
        tell(t, "You've been removed from \"" + g.name + "\".", Formatting.RED);
        s.broadcast(ctx.getSource().getServer(), g, Text.literal(t.getName().getString() + " has been removed from the guild.").formatted(Formatting.GRAY));
        Nameplates.refresh(t);
        return 1;
    }

    private static int motd(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        Guilds s = get(ctx.getSource().getServer());
        Guild g = s.of(p.getUuid());
        if (g == null || g.ranks.get(p.getUuid()) < 2) { tell(p, "Only officers can set the message of the day.", Formatting.RED); return 0; }
        g.motd = StringArgumentType.getString(ctx, "text");
        s.markDirty();
        s.broadcast(ctx.getSource().getServer(), g, Text.literal("[" + g.name + "] ").formatted(Formatting.DARK_GREEN).append(Text.literal(g.motd).formatted(Formatting.GREEN)));
        return 1;
    }

    private static int disband(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        Guilds s = get(ctx.getSource().getServer());
        Guild g = s.of(p.getUuid());
        if (g == null || g.ranks.get(p.getUuid()) != 3) { tell(p, "Only the Guildmaster can disband the guild.", Formatting.RED); return 0; }
        s.broadcast(ctx.getSource().getServer(), g, Text.literal("The guild \"" + g.name + "\" has been disbanded.").formatted(Formatting.RED));
        for (UUID id : g.ranks.keySet()) {
            s.byMember.remove(id);
            ServerPlayerEntity m = ctx.getSource().getServer().getPlayerManager().getPlayer(id);
            if (m != null) Nameplates.refresh(m);
        }
        s.guilds.remove(g.name.toLowerCase());
        s.markDirty();
        return 1;
    }

    private static int chat(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        Guilds s = get(ctx.getSource().getServer());
        Guild g = s.of(p.getUuid());
        if (g == null) { tell(p, "You're not in a guild.", Formatting.GRAY); return 0; }
        s.broadcast(ctx.getSource().getServer(), g, Text.literal("[" + g.name + "] ").formatted(Formatting.DARK_GREEN)
                .append(Text.literal(p.getName().getString() + ": ").formatted(Formatting.GREEN))
                .append(Text.literal(StringArgumentType.getString(ctx, "message")).formatted(Formatting.WHITE)));
        return 1;
    }
}
