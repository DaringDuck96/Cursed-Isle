package com.cursedtides.social;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/** Player crews: a name, a captain, and shipmates who share holds and a chat channel. Saved with the world. */
public class CrewState extends PersistentState {
    public static final int MAX_MEMBERS = 8;

    public static final class Crew {
        public final String name;
        public UUID captain;
        public final Set<UUID> members = new LinkedHashSet<>();
        public final Map<UUID, String> names = new HashMap<>();
        /** 0 hand, 1 bosun, 2 officer; the captain outranks them all. */
        public final Map<UUID, Integer> ranks = new HashMap<>();
        public int bank;
        public final Set<String> allies = new LinkedHashSet<>();
        public final Set<String> rivals = new LinkedHashSet<>();
        public String color = "gold";

        public int rank(UUID id) {
            return id.equals(captain) ? 3 : ranks.getOrDefault(id, 0);
        }

        public String rankName(UUID id) {
            return switch (rank(id)) { case 3 -> "Captain"; case 2 -> "Officer"; case 1 -> "Bosun"; default -> "Hand"; };
        }

        public Formatting formatting() {
            Formatting f = Formatting.byName(color);
            return f == null || !f.isColor() ? Formatting.GOLD : f;
        }

        Crew(String name, UUID captain) {
            this.name = name;
            this.captain = captain;
        }
    }

    private final Map<String, Crew> crews = new HashMap<>();
    private final Map<UUID, Crew> byMember = new HashMap<>();
    /** Invitations aren't saved: they lapse on restart, like a sailor's promise. */
    private final Map<UUID, String> invites = new HashMap<>();

    private static final Type<CrewState> TYPE = new Type<>(CrewState::new, CrewState::fromNbt, null);

    public static CrewState get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, "cursedtides_crews");
    }

    @Nullable
    public Crew crewOf(UUID player) {
        return byMember.get(player);
    }

    /** True when both belong to the same crew (or are the same person). */
    public static boolean shipmates(MinecraftServer server, @Nullable UUID a, @Nullable UUID b) {
        if (a == null || b == null) return false;
        if (a.equals(b)) return true;
        Crew crew = get(server).crewOf(a);
        return crew != null && crew.members.contains(b);
    }

    // ------------------------------------------------------------------ save / load

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        NbtList list = new NbtList();
        for (Crew crew : crews.values()) {
            NbtCompound c = new NbtCompound();
            c.putString("Name", crew.name);
            c.putUuid("Captain", crew.captain);
            NbtList members = new NbtList();
            for (UUID id : crew.members) {
                NbtCompound m = new NbtCompound();
                m.put("Id", NbtHelper.fromUuid(id));
                m.putString("Name", crew.names.getOrDefault(id, "?"));
                members.add(m);
            }
            c.put("Members", members);
            NbtCompound ranks = new NbtCompound();
            crew.ranks.forEach((id, r) -> ranks.putInt(id.toString(), r));
            c.put("Ranks", ranks);
            c.putInt("Bank", crew.bank);
            c.putString("Color", crew.color);
            c.putString("Allies", String.join("\n", crew.allies));
            c.putString("Rivals", String.join("\n", crew.rivals));
            list.add(c);
        }
        nbt.put("Crews", list);
        return nbt;
    }

    private static CrewState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        CrewState state = new CrewState();
        NbtList list = nbt.getList("Crews", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound c = list.getCompound(i);
            Crew crew = new Crew(c.getString("Name"), c.getUuid("Captain"));
            NbtList members = c.getList("Members", NbtElement.COMPOUND_TYPE);
            for (int j = 0; j < members.size(); j++) {
                NbtCompound m = members.getCompound(j);
                UUID id = NbtHelper.toUuid(m.get("Id"));
                crew.members.add(id);
                crew.names.put(id, m.getString("Name"));
                state.byMember.put(id, crew);
            }
            NbtCompound ranks = c.getCompound("Ranks");
            for (String k : ranks.getKeys()) crew.ranks.put(UUID.fromString(k), ranks.getInt(k));
            crew.bank = c.getInt("Bank");
            if (c.contains("Color")) crew.color = c.getString("Color");
            for (String a : c.getString("Allies").split("\n")) if (!a.isEmpty()) crew.allies.add(a);
            for (String r : c.getString("Rivals").split("\n")) if (!r.isEmpty()) crew.rivals.add(r);
            state.crews.put(crew.name.toLowerCase(), crew);
        }
        return state;
    }

    // ------------------------------------------------------------------ commands

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> dispatcher.register(literal("crew")
                .executes(ctx -> info(ctx))
                .then(literal("create").then(argument("name", StringArgumentType.greedyString()).executes(CrewState::create)))
                .then(literal("invite").then(argument("player", EntityArgumentType.player()).executes(CrewState::invite)))
                .then(literal("join").executes(CrewState::join))
                .then(literal("leave").executes(CrewState::leave))
                .then(literal("info").executes(CrewState::info))
                .then(literal("say").then(argument("message", StringArgumentType.greedyString()).executes(CrewState::say)))
                .then(literal("promote").then(argument("player", EntityArgumentType.player()).executes(ctx -> setRank(ctx, 1))))
                .then(literal("demote").then(argument("player", EntityArgumentType.player()).executes(ctx -> setRank(ctx, -1))))
                .then(literal("kick").then(argument("player", EntityArgumentType.player()).executes(CrewState::kick)))
                .then(literal("bank").executes(CrewState::bank))
                .then(literal("deposit").then(argument("silver", IntegerArgumentType.integer(1)).executes(ctx -> moveMoney(ctx, true))))
                .then(literal("withdraw").then(argument("silver", IntegerArgumentType.integer(1)).executes(ctx -> moveMoney(ctx, false))))
                .then(literal("ally").then(argument("crew", StringArgumentType.greedyString()).executes(ctx -> relation(ctx, 1))))
                .then(literal("rival").then(argument("crew", StringArgumentType.greedyString()).executes(ctx -> relation(ctx, -1))))
                .then(literal("neutral").then(argument("crew", StringArgumentType.greedyString()).executes(ctx -> relation(ctx, 0))))
                .then(literal("color").then(argument("color", StringArgumentType.word()).suggests((c, b) -> {
                    for (Formatting f : Formatting.values()) if (f.isColor()) b.suggest(f.getName());
                    return b.buildFuture();
                }).executes(CrewState::color)))));
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.ALLOW_DAMAGE.register((victim, source, amount) -> {
            if (!(victim instanceof ServerPlayerEntity v) || !(source.getAttacker() instanceof ServerPlayerEntity a) || a == v) return true;
            if (Duels.inDuel(v.getUuid())) return true;
            return !allied(v.getServer(), a.getUuid(), v.getUuid());
        });
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AFTER_DEATH.register((victim, source) -> {
            if (!(victim instanceof ServerPlayerEntity v) || !(source.getAttacker() instanceof ServerPlayerEntity a)) return;
            if (rivals(v.getServer(), a.getUuid(), v.getUuid())) {
                com.cursedtides.profession.ProfessionData.addXp(a, com.cursedtides.profession.Profession.LEADERSHIP, 15);
                com.cursedtides.activity.Leaderboards.add(a, "rival_kills", 1);
                a.sendMessage(Text.literal("A blow struck against a rival crew!").formatted(Formatting.RED), true);
            }
        });
    }

    private static int create(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        CrewState s = get(ctx.getSource().getServer());
        String name = StringArgumentType.getString(ctx, "name").trim();
        if (s.crewOf(p.getUuid()) != null) return fail(p, "You already sail with a crew. /crew leave first.");
        if (name.isEmpty() || name.length() > 24) return fail(p, "A crew name needs 1 to 24 characters.");
        if (s.crews.containsKey(name.toLowerCase())) return fail(p, "A crew by that name already sails these waters.");
        Crew crew = new Crew(name, p.getUuid());
        s.crews.put(name.toLowerCase(), crew);
        s.add(crew, p);
        com.cursedtides.activity.Achievements.grant(p, "crew_founded");
        p.sendMessage(Text.literal("You've founded the crew \"" + name + "\". Invite hands with /crew invite <player>.")
                .formatted(Formatting.GOLD), false);
        return 1;
    }

    private static int invite(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
        CrewState s = get(ctx.getSource().getServer());
        Crew crew = s.crewOf(p.getUuid());
        if (crew == null) return fail(p, "You have no crew. /crew create <name> first.");
        if (crew.rank(p.getUuid()) < 2) return fail(p, "Only the captain and officers sign on new hands.");
        if (crew.members.size() >= MAX_MEMBERS) return fail(p, "Your crew is full (" + MAX_MEMBERS + ").");
        if (s.crewOf(target.getUuid()) != null) return fail(p, target.getName().getString() + " already sails with a crew.");
        s.invites.put(target.getUuid(), crew.name.toLowerCase());
        p.sendMessage(Text.literal("Invitation sent to " + target.getName().getString() + ".").formatted(Formatting.GRAY), false);
        target.sendMessage(Text.literal(p.getName().getString() + " invites you to join the crew \"" + crew.name
                + "\". Type /crew join to sign the articles.").formatted(Formatting.GOLD), false);
        return 1;
    }

    private static int join(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        CrewState s = get(ctx.getSource().getServer());
        String key = s.invites.remove(p.getUuid());
        Crew crew = key == null ? null : s.crews.get(key);
        if (crew == null) return fail(p, "Nobody has invited you aboard.");
        if (s.crewOf(p.getUuid()) != null) return fail(p, "You already sail with a crew.");
        if (crew.members.size() >= MAX_MEMBERS) return fail(p, "That crew filled up before you got there.");
        s.add(crew, p);
        s.tell(ctx.getSource().getServer(), crew, Text.literal(p.getName().getString() + " has signed the articles!").formatted(Formatting.GOLD));
        return 1;
    }

    private static int leave(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        CrewState s = get(ctx.getSource().getServer());
        Crew crew = s.crewOf(p.getUuid());
        if (crew == null) return fail(p, "You're not in a crew.");
        crew.members.remove(p.getUuid());
        crew.names.remove(p.getUuid());
        s.byMember.remove(p.getUuid());
        crew.ranks.remove(p.getUuid());
        p.sendMessage(Text.literal("You've left \"" + crew.name + "\".").formatted(Formatting.GRAY), false);
        if (crew.members.isEmpty()) {
            s.crews.remove(crew.name.toLowerCase());
        } else {
            if (crew.captain.equals(p.getUuid())) {
                crew.captain = crew.members.iterator().next();
            }
            s.tell(ctx.getSource().getServer(), crew, Text.literal(p.getName().getString() + " has left the crew. Captain: "
                    + crew.names.getOrDefault(crew.captain, "?")).formatted(Formatting.GRAY));
        }
        s.markDirty();
        CrewTeams.sync(ctx.getSource().getServer());
        return 1;
    }

    private static int info(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        Crew crew = get(ctx.getSource().getServer()).crewOf(p.getUuid());
        if (crew == null) {
            p.sendMessage(Text.literal("No crew. /crew create <name>, or wait for an invitation.").formatted(Formatting.GRAY), false);
            return 1;
        }
        p.sendMessage(Text.literal("Crew \"" + crew.name + "\"").formatted(crew.formatting(), Formatting.BOLD), false);
        p.sendMessage(Text.literal("Chest: " + com.cursedtides.trade.Wallet.format(crew.bank)
                + (crew.allies.isEmpty() ? "" : "  Allies: " + String.join(", ", crew.allies))
                + (crew.rivals.isEmpty() ? "" : "  Rivals: " + String.join(", ", crew.rivals))).formatted(Formatting.GRAY), false);
        for (UUID id : crew.members) {
            boolean online = ctx.getSource().getServer().getPlayerManager().getPlayer(id) != null;
            p.sendMessage(Text.literal((id.equals(crew.captain) ? " ⚓ " : " • ") + crew.rankName(id) + " " + crew.names.getOrDefault(id, "?")
                    + (online ? "" : " (ashore)")).formatted(online ? Formatting.WHITE : Formatting.DARK_GRAY), false);
        }
        return 1;
    }

    private static int say(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        CrewState s = get(ctx.getSource().getServer());
        Crew crew = s.crewOf(p.getUuid());
        if (crew == null) return fail(p, "You're not in a crew.");
        s.tell(ctx.getSource().getServer(), crew, Text.literal("[" + crew.name + "] ").formatted(Formatting.GOLD)
                .append(Text.literal(p.getName().getString() + ": " + StringArgumentType.getString(ctx, "message")).formatted(Formatting.WHITE)));
        return 1;
    }

    // ------------------------------------------------------------------ helpers

    /** Shipmates, or sailing with an allied crew. Friendly fire between them is held. */
    public static boolean allied(MinecraftServer server, UUID a, UUID b) {
        if (shipmates(server, a, b)) return true;
        CrewState s = get(server);
        Crew ca = s.crewOf(a), cb = s.crewOf(b);
        return ca != null && cb != null && ca.allies.contains(cb.name.toLowerCase()) && cb.allies.contains(ca.name.toLowerCase());
    }

    public static boolean rivals(MinecraftServer server, UUID a, UUID b) {
        CrewState s = get(server);
        Crew ca = s.crewOf(a), cb = s.crewOf(b);
        return ca != null && cb != null && ca != cb && (ca.rivals.contains(cb.name.toLowerCase()) || cb.rivals.contains(ca.name.toLowerCase()));
    }

    private static int setRank(CommandContext<ServerCommandSource> ctx, int delta) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        ServerPlayerEntity t = EntityArgumentType.getPlayer(ctx, "player");
        CrewState s = get(ctx.getSource().getServer());
        Crew crew = s.crewOf(p.getUuid());
        if (crew == null || !crew.members.contains(t.getUuid())) return fail(p, "They're not in your crew.");
        if (!crew.captain.equals(p.getUuid())) return fail(p, "Only the captain sets ranks.");
        if (t.getUuid().equals(crew.captain)) return fail(p, "You're already the captain.");
        int rank = Math.max(0, Math.min(2, crew.rank(t.getUuid()) + delta));
        crew.ranks.put(t.getUuid(), rank);
        s.markDirty();
        s.tell(ctx.getSource().getServer(), crew, Text.literal(t.getName().getString() + " is now " + crew.rankName(t.getUuid()) + ".").formatted(Formatting.GOLD));
        return 1;
    }

    private static int kick(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        ServerPlayerEntity t = EntityArgumentType.getPlayer(ctx, "player");
        CrewState s = get(ctx.getSource().getServer());
        Crew crew = s.crewOf(p.getUuid());
        if (crew == null || !crew.members.contains(t.getUuid()) || t == p) return fail(p, "They're not someone you can put ashore.");
        if (crew.rank(p.getUuid()) < 2 || crew.rank(t.getUuid()) >= crew.rank(p.getUuid())) return fail(p, "You don't outrank them.");
        crew.members.remove(t.getUuid());
        crew.names.remove(t.getUuid());
        crew.ranks.remove(t.getUuid());
        s.byMember.remove(t.getUuid());
        s.markDirty();
        t.sendMessage(Text.literal("You've been put ashore by \"" + crew.name + "\".").formatted(Formatting.RED), false);
        s.tell(ctx.getSource().getServer(), crew, Text.literal(t.getName().getString() + " has been put ashore.").formatted(Formatting.GRAY));
        CrewTeams.sync(ctx.getSource().getServer());
        return 1;
    }

    private static int bank(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        Crew crew = get(ctx.getSource().getServer()).crewOf(p.getUuid());
        if (crew == null) return fail(p, "You're not in a crew.");
        p.sendMessage(Text.literal("The crew chest holds " + com.cursedtides.trade.Wallet.format(crew.bank) + ".").formatted(Formatting.GOLD), false);
        return 1;
    }

    private static int moveMoney(CommandContext<ServerCommandSource> ctx, boolean deposit) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        CrewState s = get(ctx.getSource().getServer());
        Crew crew = s.crewOf(p.getUuid());
        if (crew == null) return fail(p, "You're not in a crew.");
        int silver = IntegerArgumentType.getInteger(ctx, "silver");
        if (deposit) {
            if (!com.cursedtides.trade.Wallet.pay(p, silver)) return fail(p, "You haven't that much on you.");
            crew.bank += silver;
        } else {
            if (crew.rank(p.getUuid()) < 2) return fail(p, "Only officers and the captain draw from the crew chest.");
            if (crew.bank < silver) return fail(p, "The chest holds only " + com.cursedtides.trade.Wallet.format(crew.bank) + ".");
            crew.bank -= silver;
            com.cursedtides.trade.Wallet.give(p, silver);
        }
        s.markDirty();
        s.tell(ctx.getSource().getServer(), crew, Text.literal(p.getName().getString() + (deposit ? " paid " : " drew ")
                + com.cursedtides.trade.Wallet.format(silver) + (deposit ? " into" : " from") + " the crew chest (" + com.cursedtides.trade.Wallet.format(crew.bank) + ").")
                .formatted(Formatting.GRAY));
        return 1;
    }

    private static int relation(CommandContext<ServerCommandSource> ctx, int how) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        CrewState s = get(ctx.getSource().getServer());
        Crew crew = s.crewOf(p.getUuid());
        if (crew == null || crew.rank(p.getUuid()) < 3) return fail(p, "Only a captain makes treaties and declares rivalries.");
        String other = StringArgumentType.getString(ctx, "crew").trim().toLowerCase();
        Crew them = s.crews.get(other);
        if (them == null || them == crew) return fail(p, "No such crew sails these waters.");
        crew.allies.remove(other);
        crew.rivals.remove(other);
        if (how > 0) crew.allies.add(other);
        if (how < 0) crew.rivals.add(other);
        s.markDirty();
        Text msg = Text.literal(how > 0 ? "\"" + crew.name + "\" offers friendship to \"" + them.name + "\"" + (them.allies.contains(crew.name.toLowerCase()) ? ". The alliance is sealed!" : ". (They must /crew ally back.)")
                : how < 0 ? "\"" + crew.name + "\" declares \"" + them.name + "\" rivals!" : "\"" + crew.name + "\" is neutral toward \"" + them.name + "\".")
                .formatted(how > 0 ? Formatting.GREEN : how < 0 ? Formatting.RED : Formatting.GRAY);
        s.tell(ctx.getSource().getServer(), crew, msg);
        s.tell(ctx.getSource().getServer(), them, msg);
        return 1;
    }

    private static int color(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
        CrewState s = get(ctx.getSource().getServer());
        Crew crew = s.crewOf(p.getUuid());
        if (crew == null || crew.rank(p.getUuid()) < 3) return fail(p, "Only the captain picks the colours.");
        Formatting f = Formatting.byName(StringArgumentType.getString(ctx, "color"));
        if (f == null || !f.isColor()) return fail(p, "That's not a colour.");
        crew.color = f.getName();
        s.markDirty();
        CrewTeams.sync(ctx.getSource().getServer());
        s.tell(ctx.getSource().getServer(), crew, Text.literal("The crew flies new colours.").formatted(f));
        return 1;
    }

    public java.util.Collection<Crew> all() {
        return crews.values();
    }

    private void add(Crew crew, ServerPlayerEntity p) {
        crew.members.add(p.getUuid());
        crew.names.put(p.getUuid(), p.getName().getString());
        byMember.put(p.getUuid(), crew);
        markDirty();
        if (p.getServer() != null) CrewTeams.sync(p.getServer());
    }

    private void tell(MinecraftServer server, Crew crew, Text text) {
        for (UUID id : crew.members) {
            ServerPlayerEntity member = server.getPlayerManager().getPlayer(id);
            if (member != null) member.sendMessage(text, false);
        }
    }

    private static int fail(ServerPlayerEntity p, String msg) {
        p.sendMessage(Text.literal(msg).formatted(Formatting.RED), false);
        return 0;
    }
}
