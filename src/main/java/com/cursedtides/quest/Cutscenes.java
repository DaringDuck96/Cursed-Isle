package com.cursedtides.quest;

import com.cursedtides.CursedTides;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import java.util.List;
import java.util.Locale;

/**
 * Story cutscenes, as the old pirate game did them at its big moments: the camera leaves you, swings along a path
 * looking at what matters, with black bars and a line or two of narration, then hands you back. Esc skips. The
 * server sends the shots; the client (CutsceneClient) flies the camera. You're shielded while it plays.
 */
public final class Cutscenes {
    private Cutscenes() {}

    /** One camera move: from, to, what to look at, how long, and what's said. */
    public record Shot(Vec3 from, Vec3 to, Vec3 look, int ticks, String caption) {}

    /** Server to client: a whole scene, as text so the codec stays simple. */
    public record Play(String title, String script) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<Play> ID = new CustomPacketPayload.Type<>(CursedTides.id("cutscene"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Play> CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, Play::title, ByteBufCodecs.STRING_UTF8, Play::script, Play::new);

        @Override
        public Type<? extends CustomPacketPayload> type() { return ID; }
    }

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(Play.ID, Play.CODEC);
    }

    public static void play(ServerPlayer p, String title, List<Shot> shots) {
        StringBuilder b = new StringBuilder();
        int total = 0;
        for (Shot s : shots) {
            if (!b.isEmpty()) b.append('\u001e');
            b.append(String.format(Locale.ROOT, "%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%d\u001f%s",
                    s.from().x, s.from().y, s.from().z, s.to().x, s.to().y, s.to().z, s.look().x, s.look().y, s.look().z,
                    s.ticks(), s.caption() == null ? "" : s.caption()));
            total += s.ticks();
        }
        // Nothing can hurt you while you're watching
        p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, total + 40, 4, false, false));
        ServerPlayNetworking.send(p, new Play(title, b.toString()));
    }

    /** A sweep: circle round a point at a height and distance, looking at it. */
    public static Shot orbit(Vec3 at, double radius, double height, double fromDeg, double toDeg, int ticks, String caption) {
        Vec3 a = at.add(Math.cos(Math.toRadians(fromDeg)) * radius, height, Math.sin(Math.toRadians(fromDeg)) * radius);
        Vec3 c = at.add(Math.cos(Math.toRadians(toDeg)) * radius, height, Math.sin(Math.toRadians(toDeg)) * radius);
        return new Shot(a, c, at, ticks, caption);
    }

    /** For everyone near a point (e.g. a boss rising). */
    public static void playNear(ServerLevel w, Vec3 at, double range, String title, List<Shot> shots) {
        for (ServerPlayer p : w.getPlayers(q -> q.distanceToSqr(at) < range * range)) play(p, title, shots);
    }
}
