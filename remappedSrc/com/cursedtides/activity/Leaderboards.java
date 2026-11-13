package com.cursedtides.activity;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/** Best scores, saved with the world: /leaderboard arena|range|course|drinking|regatta|duels|fishing|wealth. */
public class Leaderboards extends SavedData {
    public record Board(String id, String title, boolean lowerIsBetter, String unit) {}

    public static final List<Board> BOARDS = List.of(
            new Board("arena", "Sword Tournament (waves survived)", false, "waves"),
            new Board("range", "Shooting Range (points)", false, "pts"),
            new Board("course", "Stack Run (fastest)", true, "s"),
            new Board("drinking", "Drinking Contests won", false, "wins"),
            new Board("duels", "Duels won", false, "wins"),
            new Board("regatta", "Regatta (fastest)", true, "s"),
            new Board("fishing", "Fish landed", false, "fish"),
            new Board("sinkings", "Ships sunk", false, "ships"),
            new Board("rival_kills", "Rivals bested", false, "kills"),
            new Board("defense", "Cannon Defense (waves held)", false, "waves"),
            new Board("privateer", "Privateer War points", false, "pts"),
            new Board("siege", "Siege defence points", false, "pts"));

    private final Map<String, Map<UUID, Integer>> scores = new HashMap<>();
    private final Map<UUID, String> names = new HashMap<>();

    private static final Factory<Leaderboards> TYPE = new Factory<>(Leaderboards::new, Leaderboards::fromNbt, null);

    public static Leaderboards get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE, "cursedtides_leaderboards");
    }

    private static Board board(String id) {
        for (Board b : BOARDS) if (b.id().equals(id)) return b;
        return null;
    }

    /** Record a result; keeps the better of old and new. Returns true on a personal best. */
    public static boolean submit(ServerPlayer p, String id, int value) {
        if (p.getServer() == null) return false;
        Leaderboards s = get(p.getServer());
        Board b = board(id);
        Map<UUID, Integer> m = s.scores.computeIfAbsent(id, k -> new HashMap<>());
        Integer old = m.get(p.getUUID());
        boolean better = old == null || (b != null && b.lowerIsBetter() ? value < old : value > old);
        if (better) m.put(p.getUUID(), value);
        s.names.put(p.getUUID(), p.getName().getString());
        s.setDirty();
        return better;
    }

    /** Add to a running total (wins, fish, sinkings). */
    public static void add(ServerPlayer p, String id, int amount) {
        if (p.getServer() == null) return;
        Leaderboards s = get(p.getServer());
        s.scores.computeIfAbsent(id, k -> new HashMap<>()).merge(p.getUUID(), amount, Integer::sum);
        s.names.put(p.getUUID(), p.getName().getString());
        s.setDirty();
    }

    public static int score(ServerPlayer p, String id) {
        if (p.getServer() == null) return 0;
        return get(p.getServer()).scores.getOrDefault(id, Map.of()).getOrDefault(p.getUUID(), 0);
    }

    public static List<String> top(MinecraftServer server, String id, int n) {
        Leaderboards s = get(server);
        Board b = board(id);
        List<Map.Entry<UUID, Integer>> list = new ArrayList<>(s.scores.getOrDefault(id, Map.of()).entrySet());
        list.sort((x, y) -> b != null && b.lowerIsBetter() ? Integer.compare(x.getValue(), y.getValue()) : Integer.compare(y.getValue(), x.getValue()));
        List<String> out = new ArrayList<>();
        for (int i = 0; i < Math.min(n, list.size()); i++) {
            var e = list.get(i);
            String v = b != null && b.unit().equals("s") ? String.format("%.1f", e.getValue() / 20.0) : String.valueOf(e.getValue());
            out.add((i + 1) + ". " + s.names.getOrDefault(e.getKey(), "?") + " (" + v + " " + (b == null ? "" : b.unit()) + ")");
        }
        return out;
    }

    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider lookup) {
        CompoundTag boards = new CompoundTag();
        scores.forEach((id, m) -> {
            CompoundTag c = new CompoundTag();
            m.forEach((u, v) -> c.putInt(u.toString(), v));
            boards.put(id, c);
        });
        nbt.put("Boards", boards);
        CompoundTag n = new CompoundTag();
        names.forEach((u, v) -> n.putString(u.toString(), v));
        nbt.put("Names", n);
        return nbt;
    }

    private static Leaderboards fromNbt(CompoundTag nbt, HolderLookup.Provider lookup) {
        Leaderboards s = new Leaderboards();
        CompoundTag boards = nbt.getCompound("Boards");
        for (String id : boards.getAllKeys()) {
            CompoundTag c = boards.getCompound(id);
            Map<UUID, Integer> m = new HashMap<>();
            for (String u : c.getAllKeys()) m.put(UUID.fromString(u), c.getInt(u));
            s.scores.put(id, m);
        }
        CompoundTag n = nbt.getCompound("Names");
        for (String u : n.getAllKeys()) s.names.put(UUID.fromString(u), n.getString(u));
        return s;
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> dispatcher.register(literal("leaderboard")
                .executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    p.displayClientMessage(Component.literal("Boards: " + String.join(", ", BOARDS.stream().map(Board::id).toList())).withStyle(ChatFormatting.GRAY), false);
                    return 1;
                })
                .then(argument("board", StringArgumentType.word()).suggests((c, b) -> {
                    BOARDS.forEach(x -> b.suggest(x.id()));
                    return b.buildFuture();
                }).executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    String id = StringArgumentType.getString(ctx, "board");
                    Board b = board(id);
                    if (b == null) return 0;
                    p.displayClientMessage(Component.literal(b.title()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
                    List<String> top = top(ctx.getSource().getServer(), id, 10);
                    if (top.isEmpty()) p.displayClientMessage(Component.literal("  Nobody yet. Be the first.").withStyle(ChatFormatting.GRAY), false);
                    for (String line : top) p.displayClientMessage(Component.literal("  " + line), false);
                    return 1;
                }))));
    }
}
