package com.cursedtides.quest;

import com.cursedtides.CursedTides;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

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
    public record Shot(Vec3d from, Vec3d to, Vec3d look, int ticks, String caption) {}

    /** Server to client: a whole scene, as text so the codec stays simple. */
    public record Play(String title, String script) implements CustomPayload {
        public static final CustomPayload.Id<Play> ID = new CustomPayload.Id<>(CursedTides.id("cutscene"));
        public static final PacketCodec<RegistryByteBuf, Play> CODEC = PacketCodec.tuple(
                PacketCodecs.STRING, Play::title, PacketCodecs.STRING, Play::script, Play::new);

        @Override
        public Id<? extends CustomPayload> getId() { return ID; }
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(Play.ID, Play.CODEC);
    }

    public static void play(ServerPlayerEntity p, String title, List<Shot> shots) {
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
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, total + 40, 4, false, false));
        ServerPlayNetworking.send(p, new Play(title, b.toString()));
    }

    /** A sweep: circle round a point at a height and distance, looking at it. */
    public static Shot orbit(Vec3d at, double radius, double height, double fromDeg, double toDeg, int ticks, String caption) {
        Vec3d a = at.add(Math.cos(Math.toRadians(fromDeg)) * radius, height, Math.sin(Math.toRadians(fromDeg)) * radius);
        Vec3d c = at.add(Math.cos(Math.toRadians(toDeg)) * radius, height, Math.sin(Math.toRadians(toDeg)) * radius);
        return new Shot(a, c, at, ticks, caption);
    }

    /** For everyone near a point (e.g. a boss rising). */
    public static void playNear(ServerWorld w, Vec3d at, double range, String title, List<Shot> shots) {
        for (ServerPlayerEntity p : w.getPlayers(q -> q.squaredDistanceTo(at) < range * range)) play(p, title, shots);
    }
}
