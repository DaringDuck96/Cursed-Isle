package com.cursedtides.social;

import com.cursedtides.CursedTides;
import com.cursedtides.entity.StoryNpcEntity;
import com.cursedtides.pirate.PirateData;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.PlayerTeam;
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
    public record Markers(List<Integer> ids, List<Integer> kinds) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<Markers> ID = new CustomPacketPayload.Type<>(CursedTides.id("npc_markers"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Markers> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), Markers::ids,
                ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), Markers::kinds, Markers::new);

        @Override
        public Type<? extends CustomPacketPayload> type() { return ID; }
    }

    private static final Map<UUID, String> LAST = new HashMap<>();
    private static final Map<UUID, Markers> SENT = new HashMap<>();

    public static void register() {
        PayloadTypeRegistry.playS2C().register(Markers.ID, Markers.CODEC);
        ServerPlayConnectionEvents.JOIN.register((h, s, server) -> server.execute(() -> refresh(h.player)));
        ServerPlayConnectionEvents.DISCONNECT.register((h, s) -> { LAST.remove(h.player.getUUID()); SENT.remove(h.player.getUUID()); });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 100 == 37) for (ServerPlayer p : server.getPlayerList().getPlayers()) refresh(p);
            if (server.getTickCount() % 20 == 3) for (ServerPlayer p : server.getPlayerList().getPlayers()) markers(p);
        });
        // Crewmates can't hurt each other (this used to be the crew team's job)
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((victim, source, amount) -> {
            if (victim instanceof ServerPlayer v && source.getEntity() instanceof ServerPlayer a && a != v
                    && CrewState.allied(v.getServer(), a.getUUID(), v.getUUID())) return false;
            return true;
        });
    }

    public static void syncAll(MinecraftServer server) {
        LAST.clear();
        for (ServerPlayer p : server.getPlayerList().getPlayers()) refresh(p);
    }

    private static ChatFormatting levelColor(int lv) {
        return lv < 10 ? ChatFormatting.GRAY : lv < 20 ? ChatFormatting.YELLOW : lv < 30 ? ChatFormatting.GOLD : lv < 40 ? ChatFormatting.RED : ChatFormatting.DARK_RED;
    }

    /** Puts this pirate on their own scoreboard team, whose prefix and suffix make their nameplate. */
    public static void refresh(ServerPlayer p) {
        MinecraftServer server = p.getServer();
        if (server == null) return;
        CrewState.Crew crew = CrewState.get(server).crewOf(p.getUUID());
        String guild = Guilds.nameOf(server, p.getUUID());
        int lv = PirateData.notoriety(p);
        String badges = Infamy.badges(p), title = Titles.worn(p);
        String key = (crew == null ? "" : crew.name + "|" + crew.color) + "|" + guild + "|" + lv + "|" + badges + "|" + title;
        if (key.equals(LAST.get(p.getUUID()))) return;
        LAST.put(p.getUUID(), key);
        ServerScoreboard sb = server.getScoreboard();
        String id = "ctp_" + p.getUUID().toString().replace("-", "").substring(0, 12);
        PlayerTeam t = sb.getPlayerTeam(id);
        if (t == null) t = sb.addPlayerTeam(id);
        t.setPlayerPrefix(crew == null ? Component.empty() : Component.literal("[" + crew.name + "] ").withStyle(crew.formatting()));
        MutableComponent suffix = Component.literal(" Lv " + lv).withStyle(levelColor(lv));
        if (!badges.isEmpty()) suffix.append(Component.literal(badges).withStyle(ChatFormatting.DARK_RED));
        if (title != null) suffix.append(Component.literal(" ‹" + title + "›").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        if (guild != null) suffix.append(Component.literal(" «" + guild + "»").withStyle(ChatFormatting.DARK_GREEN));
        t.setPlayerSuffix(suffix);
        t.setColor(crew == null ? ChatFormatting.WHITE : crew.formatting());
        t.setSeeFriendlyInvisibles(true);
        sb.addPlayerToTeam(p.getScoreboardName(), t);
    }

    private static void markers(ServerPlayer p) {
        List<Integer> ids = new ArrayList<>(), kinds = new ArrayList<>();
        StoryNpcEntity.Role story = com.cursedtides.quest.Objectives.storyRole(p);
        for (StoryNpcEntity npc : p.serverLevel().getEntitiesOfClass(StoryNpcEntity.class, new AABB(p.blockPosition()).inflate(48), n -> true)) {
            int e = com.cursedtides.quest.Errands.marker(p, npc.getRole());
            int k = npc.getRole() == story ? MARK_STORY : e == 2 ? MARK_RETURN : e == 1 ? MARK_WORK : 0;
            if (k > 0) { ids.add(npc.getId()); kinds.add(k); }
        }
        Markers m = new Markers(ids, kinds);
        if (m.equals(SENT.get(p.getUUID()))) return;
        SENT.put(p.getUUID(), m);
        ServerPlayNetworking.send(p, m);
    }
}
