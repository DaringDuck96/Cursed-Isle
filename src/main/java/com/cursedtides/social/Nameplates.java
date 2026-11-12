package com.cursedtides.social;

import com.cursedtides.CursedTides;
import com.cursedtides.entity.StoryNpcEntity;
import com.cursedtides.pirate.PirateData;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Nameplates the old pirate game's way. Over every pirate: their crew's tag, their name, their level, and their
 * guild beneath (as a suffix). Over the townsfolk: a yellow ! when they've work for you, a green ? when they're
 * waiting for you to come back, and a gold ! over whoever your story wants you to see next.
 */
public final class Nameplates {
    private Nameplates() {}

    public static final int MARK_WORK = 1, MARK_RETURN = 2, MARK_STORY = 3;

    /** Server to client: which nearby people get which marker. */
    public record Markers(List<Integer> ids, List<Integer> kinds) implements CustomPayload {
        public static final CustomPayload.Id<Markers> ID = new CustomPayload.Id<>(CursedTides.id("npc_markers"));
        public static final PacketCodec<RegistryByteBuf, Markers> CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_INT.collect(PacketCodecs.toList()), Markers::ids,
                PacketCodecs.VAR_INT.collect(PacketCodecs.toList()), Markers::kinds, Markers::new);

        @Override
        public Id<? extends CustomPayload> getId() { return ID; }
    }

    private static final Map<UUID, String> LAST = new HashMap<>();
    private static final Map<UUID, Markers> SENT = new HashMap<>();

    public static void register() {
        PayloadTypeRegistry.playS2C().register(Markers.ID, Markers.CODEC);
        ServerPlayConnectionEvents.JOIN.register((h, s, server) -> server.execute(() -> refresh(h.player)));
        ServerPlayConnectionEvents.DISCONNECT.register((h, s) -> { LAST.remove(h.player.getUuid()); SENT.remove(h.player.getUuid()); });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 100 == 37) for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) refresh(p);
            if (server.getTicks() % 20 == 3) for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) markers(p);
        });
        // Crewmates can't hurt each other (this used to be the crew team's job)
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((victim, source, amount) -> {
            if (victim instanceof ServerPlayerEntity v && source.getAttacker() instanceof ServerPlayerEntity a && a != v
                    && CrewState.allied(v.getServer(), a.getUuid(), v.getUuid())) return false;
            return true;
        });
    }

    public static void syncAll(MinecraftServer server) {
        LAST.clear();
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) refresh(p);
    }

    private static Formatting levelColor(int lv) {
        return lv < 10 ? Formatting.GRAY : lv < 20 ? Formatting.YELLOW : lv < 30 ? Formatting.GOLD : lv < 40 ? Formatting.RED : Formatting.DARK_RED;
    }

    /** Puts this pirate on their own scoreboard team, whose prefix and suffix make their nameplate. */
    public static void refresh(ServerPlayerEntity p) {
        MinecraftServer server = p.getServer();
        if (server == null) return;
        CrewState.Crew crew = CrewState.get(server).crewOf(p.getUuid());
        String guild = Guilds.nameOf(server, p.getUuid());
        int lv = PirateData.notoriety(p);
        String key = (crew == null ? "" : crew.name + "|" + crew.color) + "|" + guild + "|" + lv;
        if (key.equals(LAST.get(p.getUuid()))) return;
        LAST.put(p.getUuid(), key);
        ServerScoreboard sb = server.getScoreboard();
        String id = "ctp_" + p.getUuid().toString().replace("-", "").substring(0, 12);
        Team t = sb.getTeam(id);
        if (t == null) t = sb.addTeam(id);
        t.setPrefix(crew == null ? Text.empty() : Text.literal("[" + crew.name + "] ").formatted(crew.formatting()));
        MutableText suffix = Text.literal(" Lv " + lv).formatted(levelColor(lv));
        if (guild != null) suffix.append(Text.literal(" «" + guild + "»").formatted(Formatting.DARK_GREEN));
        t.setSuffix(suffix);
        t.setColor(crew == null ? Formatting.WHITE : crew.formatting());
        t.setShowFriendlyInvisibles(true);
        sb.addScoreHolderToTeam(p.getNameForScoreboard(), t);
    }

    private static void markers(ServerPlayerEntity p) {
        List<Integer> ids = new ArrayList<>(), kinds = new ArrayList<>();
        StoryNpcEntity.Role story = com.cursedtides.quest.Objectives.storyRole(p);
        for (StoryNpcEntity npc : p.getServerWorld().getEntitiesByClass(StoryNpcEntity.class, new Box(p.getBlockPos()).expand(48), n -> true)) {
            int e = com.cursedtides.quest.Errands.marker(p, npc.getRole());
            int k = npc.getRole() == story ? MARK_STORY : e == 2 ? MARK_RETURN : e == 1 ? MARK_WORK : 0;
            if (k > 0) { ids.add(npc.getId()); kinds.add(k); }
        }
        Markers m = new Markers(ids, kinds);
        if (m.equals(SENT.get(p.getUuid()))) return;
        SENT.put(p.getUuid(), m);
        ServerPlayNetworking.send(p, m);
    }
}
