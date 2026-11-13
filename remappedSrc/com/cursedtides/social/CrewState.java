package com.cursedtides.social;

import com.mojang.brigadier.arguments.IntegerArgumentType;
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
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/** Player crews: a name, a captain, and shipmates who share holds and a chat channel. Saved with the world. */
public class CrewState extends SavedData {
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

        public ChatFormatting formatting() {
            ChatFormatting f = ChatFormatting.getByName(color);
            return f == null || !f.isColor() ? ChatFormatting.GOLD : f;
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

    private static final Factory<CrewState> TYPE = new Factory<>(CrewState::new, CrewState::fromNbt, null);

    public static CrewState get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE, "cursedtides_crews");
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
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider lookup) {
        ListTag list = new ListTag();
        for (Crew crew : crews.values()) {
            CompoundTag c = new CompoundTag();
            c.putString("Name", crew.name);
            c.putUUID("Captain", crew.captain);
            ListTag members = new ListTag();
            for (UUID id : crew.members) {
                CompoundTag m = new CompoundTag();
                m.put("Id", NbtUtils.createUUID(id));
                m.putString("Name", crew.names.getOrDefault(id, "?"));
                members.add(m);
            }
            c.put("Members", members);
            CompoundTag ranks = new CompoundTag();
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

    private static CrewState fromNbt(CompoundTag nbt, HolderLookup.Provider lookup) {
        CrewState state = new CrewState();
        ListTag list = nbt.getList("Crews", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag c = list.getCompound(i);
            Crew crew = new Crew(c.getString("Name"), c.getUUID("Captain"));
            ListTag members = c.getList("Members", Tag.TAG_COMPOUND);
            for (int j = 0; j < members.size(); j++) {
                CompoundTag m = members.getCompound(j);
                UUID id = NbtUtils.loadUUID(m.get("Id"));
                crew.members.add(id);
                crew.names.put(id, m.getString("Name"));
                state.byMember.put(id, crew);
            }
            CompoundTag ranks = c.getCompound("Ranks");
            for (String k : ranks.getAllKeys()) crew.ranks.put(UUID.fromString(k), ranks.getInt(k));
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
                .then(literal("invite").then(argument("player", EntityArgument.player()).executes(CrewState::invite)))
                .then(literal("join").executes(CrewState::join))
                .then(literal("leave").executes(CrewState::leave))
                .then(literal("info").executes(CrewState::info))
                .then(literal("say").then(argument("message", StringArgumentType.greedyString()).executes(CrewState::say)))
                .then(literal("promote").then(argument("player", EntityArgument.player()).executes(ctx -> setRank(ctx, 1))))
                .then(literal("demote").then(argument("player", EntityArgument.player()).executes(ctx -> setRank(ctx, -1))))
                .then(literal("kick").then(argument("player", EntityArgument.player()).executes(CrewState::kick)))
                .then(literal("bank").executes(CrewState::bank))
                .then(literal("deposit").then(argument("silver", IntegerArgumentType.integer(1)).executes(ctx -> moveMoney(ctx, true))))
                .then(literal("withdraw").then(argument("silver", IntegerArgumentType.integer(1)).executes(ctx -> moveMoney(ctx, false))))
                .then(literal("ally").then(argument("crew", StringArgumentType.greedyString()).executes(ctx -> relation(ctx, 1))))
                .then(literal("rival").then(argument("crew", StringArgumentType.greedyString()).executes(ctx -> relation(ctx, -1))))
                .then(literal("neutral").then(argument("crew", StringArgumentType.greedyString()).executes(ctx -> relation(ctx, 0))))
                .then(literal("color").then(argument("color", StringArgumentType.word()).suggests((c, b) -> {
                    for (ChatFormatting f : ChatFormatting.values()) if (f.isColor()) b.suggest(f.getName());
                    return b.buildFuture();
                }).executes(CrewState::color)))));
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.ALLOW_DAMAGE.register((victim, source, amount) -> {
            if (!(victim instanceof ServerPlayer v) || !(source.getEntity() instanceof ServerPlayer a) || a == v) return true;
            if (Duels.inDuel(v.getUUID())) return true;
            return !allied(v.getServer(), a.getUUID(), v.getUUID());
        });
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AFTER_DEATH.register((victim, source) -> {
            if (!(victim instanceof ServerPlayer v) || !(source.getEntity() instanceof ServerPlayer a)) return;
            if (rivals(v.getServer(), a.getUUID(), v.getUUID())) {
                com.cursedtides.profession.ProfessionData.addXp(a, com.cursedtides.profession.Profession.LEADERSHIP, 15);
                com.cursedtides.activity.Leaderboards.add(a, "rival_kills", 1);
                a.displayClientMessage(Component.literal("A blow struck against a rival crew!").withStyle(ChatFormatting.RED), true);
            }
        });
    }

    private static int create(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        CrewState s = get(ctx.getSource().getServer());
        String name = StringArgumentType.getString(ctx, "name").trim();
        if (s.crewOf(p.getUUID()) != null) return fail(p, "You already sail with a crew. /crew leave first.");
        if (name.isEmpty() || name.length() > 24) return fail(p, "A crew name needs 1 to 24 characters.");
        if (s.crews.containsKey(name.toLowerCase())) return fail(p, "A crew by that name already sails these waters.");
        Crew crew = new Crew(name, p.getUUID());
        s.crews.put(name.toLowerCase(), crew);
        s.add(crew, p);
        com.cursedtides.activity.Achievements.grant(p, "crew_founded");
        p.displayClientMessage(Component.literal("You've founded the crew \"" + name + "\". Invite hands with /crew invite <player>.")
                .withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private static int invite(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        CrewState s = get(ctx.getSource().getServer());
        Crew crew = s.crewOf(p.getUUID());
        if (crew == null) return fail(p, "You have no crew. /crew create <name> first.");
        if (crew.rank(p.getUUID()) < 2) return fail(p, "Only the captain and officers sign on new hands.");
        if (crew.members.size() >= MAX_MEMBERS) return fail(p, "Your crew is full (" + MAX_MEMBERS + ").");
        if (s.crewOf(target.getUUID()) != null) return fail(p, target.getName().getString() + " already sails with a crew.");
        s.invites.put(target.getUUID(), crew.name.toLowerCase());
        p.displayClientMessage(Component.literal("Invitation sent to " + target.getName().getString() + ".").withStyle(ChatFormatting.GRAY), false);
        target.displayClientMessage(Component.literal(p.getName().getString() + " invites you to join the crew \"" + crew.name
                + "\". Type /crew join to sign the articles.").withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private static int join(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        CrewState s = get(ctx.getSource().getServer());
        String key = s.invites.remove(p.getUUID());
        Crew crew = key == null ? null : s.crews.get(key);
        if (crew == null) return fail(p, "Nobody has invited you aboard.");
        if (s.crewOf(p.getUUID()) != null) return fail(p, "You already sail with a crew.");
        if (crew.members.size() >= MAX_MEMBERS) return fail(p, "That crew filled up before you got there.");
        s.add(crew, p);
        s.tell(ctx.getSource().getServer(), crew, Component.literal(p.getName().getString() + " has signed the articles!").withStyle(ChatFormatting.GOLD));
        return 1;
    }

    private static int leave(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        CrewState s = get(ctx.getSource().getServer());
        Crew crew = s.crewOf(p.getUUID());
        if (crew == null) return fail(p, "You're not in a crew.");
        crew.members.remove(p.getUUID());
        crew.names.remove(p.getUUID());
        s.byMember.remove(p.getUUID());
        crew.ranks.remove(p.getUUID());
        p.displayClientMessage(Component.literal("You've left \"" + crew.name + "\".").withStyle(ChatFormatting.GRAY), false);
        if (crew.members.isEmpty()) {
            s.crews.remove(crew.name.toLowerCase());
        } else {
            if (crew.captain.equals(p.getUUID())) {
                crew.captain = crew.members.iterator().next();
            }
            s.tell(ctx.getSource().getServer(), crew, Component.literal(p.getName().getString() + " has left the crew. Captain: "
                    + crew.names.getOrDefault(crew.captain, "?")).withStyle(ChatFormatting.GRAY));
        }
        s.setDirty();
        CrewTeams.sync(ctx.getSource().getServer());
        return 1;
    }

    private static int info(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        Crew crew = get(ctx.getSource().getServer()).crewOf(p.getUUID());
        if (crew == null) {
            p.displayClientMessage(Component.literal("No crew. /crew create <name>, or wait for an invitation.").withStyle(ChatFormatting.GRAY), false);
            return 1;
        }
        p.displayClientMessage(Component.literal("Crew \"" + crew.name + "\"").withStyle(crew.formatting(), ChatFormatting.BOLD), false);
        p.displayClientMessage(Component.literal("Chest: " + com.cursedtides.trade.Wallet.format(crew.bank)
                + (crew.allies.isEmpty() ? "" : "  Allies: " + String.join(", ", crew.allies))
                + (crew.rivals.isEmpty() ? "" : "  Rivals: " + String.join(", ", crew.rivals))).withStyle(ChatFormatting.GRAY), false);
        for (UUID id : crew.members) {
            boolean online = ctx.getSource().getServer().getPlayerList().getPlayer(id) != null;
            p.displayClientMessage(Component.literal((id.equals(crew.captain) ? " ⚓ " : " • ") + crew.rankName(id) + " " + crew.names.getOrDefault(id, "?")
                    + (online ? "" : " (ashore)")).withStyle(online ? ChatFormatting.WHITE : ChatFormatting.DARK_GRAY), false);
        }
        return 1;
    }

    private static int say(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        CrewState s = get(ctx.getSource().getServer());
        Crew crew = s.crewOf(p.getUUID());
        if (crew == null) return fail(p, "You're not in a crew.");
        s.tell(ctx.getSource().getServer(), crew, Component.literal("[" + crew.name + "] ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(p.getName().getString() + ": " + StringArgumentType.getString(ctx, "message")).withStyle(ChatFormatting.WHITE)));
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

    private static int setRank(CommandContext<CommandSourceStack> ctx, int delta) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        ServerPlayer t = EntityArgument.getPlayer(ctx, "player");
        CrewState s = get(ctx.getSource().getServer());
        Crew crew = s.crewOf(p.getUUID());
        if (crew == null || !crew.members.contains(t.getUUID())) return fail(p, "They're not in your crew.");
        if (!crew.captain.equals(p.getUUID())) return fail(p, "Only the captain sets ranks.");
        if (t.getUUID().equals(crew.captain)) return fail(p, "You're already the captain.");
        int rank = Math.max(0, Math.min(2, crew.rank(t.getUUID()) + delta));
        crew.ranks.put(t.getUUID(), rank);
        s.setDirty();
        s.tell(ctx.getSource().getServer(), crew, Component.literal(t.getName().getString() + " is now " + crew.rankName(t.getUUID()) + ".").withStyle(ChatFormatting.GOLD));
        return 1;
    }

    private static int kick(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        ServerPlayer t = EntityArgument.getPlayer(ctx, "player");
        CrewState s = get(ctx.getSource().getServer());
        Crew crew = s.crewOf(p.getUUID());
        if (crew == null || !crew.members.contains(t.getUUID()) || t == p) return fail(p, "They're not someone you can put ashore.");
        if (crew.rank(p.getUUID()) < 2 || crew.rank(t.getUUID()) >= crew.rank(p.getUUID())) return fail(p, "You don't outrank them.");
        crew.members.remove(t.getUUID());
        crew.names.remove(t.getUUID());
        crew.ranks.remove(t.getUUID());
        s.byMember.remove(t.getUUID());
        s.setDirty();
        t.displayClientMessage(Component.literal("You've been put ashore by \"" + crew.name + "\".").withStyle(ChatFormatting.RED), false);
        s.tell(ctx.getSource().getServer(), crew, Component.literal(t.getName().getString() + " has been put ashore.").withStyle(ChatFormatting.GRAY));
        CrewTeams.sync(ctx.getSource().getServer());
        return 1;
    }

    private static int bank(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        Crew crew = get(ctx.getSource().getServer()).crewOf(p.getUUID());
        if (crew == null) return fail(p, "You're not in a crew.");
        p.displayClientMessage(Component.literal("The crew chest holds " + com.cursedtides.trade.Wallet.format(crew.bank) + ".").withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private static int moveMoney(CommandContext<CommandSourceStack> ctx, boolean deposit) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        CrewState s = get(ctx.getSource().getServer());
        Crew crew = s.crewOf(p.getUUID());
        if (crew == null) return fail(p, "You're not in a crew.");
        int silver = IntegerArgumentType.getInteger(ctx, "silver");
        if (deposit) {
            if (!com.cursedtides.trade.Wallet.pay(p, silver)) return fail(p, "You haven't that much on you.");
            crew.bank += silver;
        } else {
            if (crew.rank(p.getUUID()) < 2) return fail(p, "Only officers and the captain draw from the crew chest.");
            if (crew.bank < silver) return fail(p, "The chest holds only " + com.cursedtides.trade.Wallet.format(crew.bank) + ".");
            crew.bank -= silver;
            com.cursedtides.trade.Wallet.give(p, silver);
        }
        s.setDirty();
        s.tell(ctx.getSource().getServer(), crew, Component.literal(p.getName().getString() + (deposit ? " paid " : " drew ")
                + com.cursedtides.trade.Wallet.format(silver) + (deposit ? " into" : " from") + " the crew chest (" + com.cursedtides.trade.Wallet.format(crew.bank) + ").")
                .withStyle(ChatFormatting.GRAY));
        return 1;
    }

    private static int relation(CommandContext<CommandSourceStack> ctx, int how) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        CrewState s = get(ctx.getSource().getServer());
        Crew crew = s.crewOf(p.getUUID());
        if (crew == null || crew.rank(p.getUUID()) < 3) return fail(p, "Only a captain makes treaties and declares rivalries.");
        String other = StringArgumentType.getString(ctx, "crew").trim().toLowerCase();
        Crew them = s.crews.get(other);
        if (them == null || them == crew) return fail(p, "No such crew sails these waters.");
        crew.allies.remove(other);
        crew.rivals.remove(other);
        if (how > 0) crew.allies.add(other);
        if (how < 0) crew.rivals.add(other);
        s.setDirty();
        Component msg = Component.literal(how > 0 ? "\"" + crew.name + "\" offers friendship to \"" + them.name + "\"" + (them.allies.contains(crew.name.toLowerCase()) ? ". The alliance is sealed!" : ". (They must /crew ally back.)")
                : how < 0 ? "\"" + crew.name + "\" declares \"" + them.name + "\" rivals!" : "\"" + crew.name + "\" is neutral toward \"" + them.name + "\".")
                .withStyle(how > 0 ? ChatFormatting.GREEN : how < 0 ? ChatFormatting.RED : ChatFormatting.GRAY);
        s.tell(ctx.getSource().getServer(), crew, msg);
        s.tell(ctx.getSource().getServer(), them, msg);
        return 1;
    }

    private static int color(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        CrewState s = get(ctx.getSource().getServer());
        Crew crew = s.crewOf(p.getUUID());
        if (crew == null || crew.rank(p.getUUID()) < 3) return fail(p, "Only the captain picks the colours.");
        ChatFormatting f = ChatFormatting.getByName(StringArgumentType.getString(ctx, "color"));
        if (f == null || !f.isColor()) return fail(p, "That's not a colour.");
        crew.color = f.getName();
        s.setDirty();
        CrewTeams.sync(ctx.getSource().getServer());
        s.tell(ctx.getSource().getServer(), crew, Component.literal("The crew flies new colours.").withStyle(f));
        return 1;
    }

    public java.util.Collection<Crew> all() {
        return crews.values();
    }

    private void add(Crew crew, ServerPlayer p) {
        crew.members.add(p.getUUID());
        crew.names.put(p.getUUID(), p.getName().getString());
        byMember.put(p.getUUID(), crew);
        setDirty();
        if (p.getServer() != null) CrewTeams.sync(p.getServer());
    }

    private void tell(MinecraftServer server, Crew crew, Component text) {
        for (UUID id : crew.members) {
            ServerPlayer member = server.getPlayerList().getPlayer(id);
            if (member != null) member.displayClientMessage(text, false);
        }
    }

    private static int fail(ServerPlayer p, String msg) {
        p.displayClientMessage(Component.literal(msg).withStyle(ChatFormatting.RED), false);
        return 0;
    }
}
