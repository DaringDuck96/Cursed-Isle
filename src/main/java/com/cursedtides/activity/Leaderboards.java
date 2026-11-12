package com.cursedtides.activity;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.PersistentState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/** Best scores, saved with the world: /leaderboard arena|range|course|drinking|regatta|duels|fishing|wealth. */
public class Leaderboards extends PersistentState {
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
            new Board("rival_kills", "Rivals bested", false, "kills"));

    private final Map<String, Map<UUID, Integer>> scores = new HashMap<>();
    private final Map<UUID, String> names = new HashMap<>();

    private static final Type<Leaderboards> TYPE = new Type<>(Leaderboards::new, Leaderboards::fromNbt, null);

    public static Leaderboards get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, "cursedtides_leaderboards");
    }

    private static Board board(String id) {
        for (Board b : BOARDS) if (b.id().equals(id)) return b;
        return null;
    }

    /** Record a result; keeps the better of old and new. Returns true on a personal best. */
    public static boolean submit(ServerPlayerEntity p, String id, int value) {
        if (p.getServer() == null) return false;
        Leaderboards s = get(p.getServer());
        Board b = board(id);
        Map<UUID, Integer> m = s.scores.computeIfAbsent(id, k -> new HashMap<>());
        Integer old = m.get(p.getUuid());
        boolean better = old == null || (b != null && b.lowerIsBetter() ? value < old : value > old);
        if (better) m.put(p.getUuid(), value);
        s.names.put(p.getUuid(), p.getName().getString());
        s.markDirty();
        return better;
    }

    /** Add to a running total (wins, fish, sinkings). */
    public static void add(ServerPlayerEntity p, String id, int amount) {
        if (p.getServer() == null) return;
        Leaderboards s = get(p.getServer());
        s.scores.computeIfAbsent(id, k -> new HashMap<>()).merge(p.getUuid(), amount, Integer::sum);
        s.names.put(p.getUuid(), p.getName().getString());
        s.markDirty();
    }

    public static int score(ServerPlayerEntity p, String id) {
        if (p.getServer() == null) return 0;
        return get(p.getServer()).scores.getOrDefault(id, Map.of()).getOrDefault(p.getUuid(), 0);
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
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        NbtCompound boards = new NbtCompound();
        scores.forEach((id, m) -> {
            NbtCompound c = new NbtCompound();
            m.forEach((u, v) -> c.putInt(u.toString(), v));
            boards.put(id, c);
        });
        nbt.put("Boards", boards);
        NbtCompound n = new NbtCompound();
        names.forEach((u, v) -> n.putString(u.toString(), v));
        nbt.put("Names", n);
        return nbt;
    }

    private static Leaderboards fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        Leaderboards s = new Leaderboards();
        NbtCompound boards = nbt.getCompound("Boards");
        for (String id : boards.getKeys()) {
            NbtCompound c = boards.getCompound(id);
            Map<UUID, Integer> m = new HashMap<>();
            for (String u : c.getKeys()) m.put(UUID.fromString(u), c.getInt(u));
            s.scores.put(id, m);
        }
        NbtCompound n = nbt.getCompound("Names");
        for (String u : n.getKeys()) s.names.put(UUID.fromString(u), n.getString(u));
        return s;
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> dispatcher.register(literal("leaderboard")
                .executes(ctx -> {
                    ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                    p.sendMessage(Text.literal("Boards: " + String.join(", ", BOARDS.stream().map(Board::id).toList())).formatted(Formatting.GRAY), false);
                    return 1;
                })
                .then(argument("board", StringArgumentType.word()).suggests((c, b) -> {
                    BOARDS.forEach(x -> b.suggest(x.id()));
                    return b.buildFuture();
                }).executes(ctx -> {
                    ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                    String id = StringArgumentType.getString(ctx, "board");
                    Board b = board(id);
                    if (b == null) return 0;
                    p.sendMessage(Text.literal(b.title()).formatted(Formatting.GOLD, Formatting.BOLD), false);
                    List<String> top = top(ctx.getSource().getServer(), id, 10);
                    if (top.isEmpty()) p.sendMessage(Text.literal("  Nobody yet. Be the first.").formatted(Formatting.GRAY), false);
                    for (String line : top) p.sendMessage(Text.literal("  " + line), false);
                    return 1;
                }))));
    }
}
