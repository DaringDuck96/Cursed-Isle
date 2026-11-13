package com.cursedtides.activity;

import com.cursedtides.CursedTides;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import com.mojang.brigadier.arguments.StringArgumentType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/** Waves, bows, jigs and drinks: the little things that make a tavern feel full. Key P, or /emote <name>. */
public final class Emotes {
    private Emotes() {}

    public enum Emote {
        WAVE("waves"), BOW("bows with a flourish"), SALUTE("snaps a salute"), CHEER("raises a cheer"), LAUGH("laughs heartily"),
        POINT("points out to sea"), SHRUG("shrugs"), DANCE("dances a jig"), DRINK("knocks back a drink"), SIT("sits down"),
        SING("strikes up a shanty"), TOAST("raises a toast"), SPIT("spits on the deck"), SLEEP("dozes off"), FLEX("flexes"), CRY("weeps openly");
        public final String verb;
        Emote(String verb) { this.verb = verb; }
    }

    public record Do(int id) implements CustomPacketPayload {
        public static final Type<Do> ID = new Type<>(CursedTides.id("emote"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Do> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, Do::id, Do::new);
        @Override public Type<? extends CustomPacketPayload> type() { return ID; }
    }

    public static final String SEAT = "cursedtides_seat";
    private static final Map<UUID, Integer> DANCING = new HashMap<>();
    private static final Map<UUID, Long> LAST = new HashMap<>();
    private static final Map<UUID, Long> LAST_SONG = new HashMap<>();

    public static void register() {
        PayloadTypeRegistry.playC2S().register(Do.ID, Do.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Do.ID, (payload, ctx) -> ctx.server().execute(() -> {
            Emote[] all = Emote.values();
            if (payload.id() >= 0 && payload.id() < all.length) perform(ctx.player(), all[payload.id()]);
        }));
        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> dispatcher.register(literal("emote")
                .then(argument("name", StringArgumentType.word()).suggests((c, b) -> {
                    for (Emote e : Emote.values()) b.suggest(e.name().toLowerCase());
                    return b.buildFuture();
                }).executes(ctx -> {
                    try {
                        perform(ctx.getSource().getPlayerOrException(), Emote.valueOf(StringArgumentType.getString(ctx, "name").toUpperCase()));
                        return 1;
                    } catch (IllegalArgumentException e) {
                        return 0;
                    }
                }))));
        ServerTickEvents.END_SERVER_TICK.register(Emotes::tick);
    }

    public static void perform(ServerPlayer p, Emote e) {
        ServerLevel w = p.serverLevel();
        long now = w.getGameTime();
        if (LAST.getOrDefault(p.getUUID(), 0L) > now - 20) return;
        LAST.put(p.getUUID(), now);
        Component line = Component.literal("* " + com.cursedtides.character.CharacterData.pirateName(p) + " " + e.verb + ".").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
        for (ServerPlayer other : w.getPlayers(o -> o.distanceToSqr(p) < 32 * 32)) other.displayClientMessage(line, false);
        switch (e) {
            case WAVE, POINT, SALUTE -> { p.swing(InteractionHand.MAIN_HAND, true); sound(p, SoundEvents.VILLAGER_AMBIENT, 1.4f); }
            case BOW -> { p.setXRot(60); p.swing(InteractionHand.OFF_HAND, true); sound(p, SoundEvents.ARMOR_EQUIP_LEATHER.value(), 1f); }
            case CHEER -> { hop(p); p.swing(InteractionHand.MAIN_HAND, true); particles(p, ParticleTypes.HAPPY_VILLAGER, 10); sound(p, SoundEvents.VILLAGER_CELEBRATE, 1.2f); }
            case LAUGH -> { particles(p, ParticleTypes.NOTE, 3); sound(p, SoundEvents.WITCH_CELEBRATE, 1.4f); }
            case SHRUG -> sound(p, SoundEvents.VILLAGER_NO, 1.2f);
            case DANCE -> DANCING.put(p.getUUID(), 100);
            case SING -> {
                DANCING.put(p.getUUID(), -100);
                if (LAST_SONG.getOrDefault(p.getUUID(), -10000L) < now - 1200) {
                    LAST_SONG.put(p.getUUID(), now);
                    var songs = java.util.List.of(com.cursedtides.sound.ModSounds.SALTMARROW_HORNPIPE, com.cursedtides.sound.ModSounds.WRECKERS_REEL,
                            com.cursedtides.sound.ModSounds.LANTERNS_DROWN);
                    p.level().playSound(null, p.blockPosition(), songs.get(p.getRandom().nextInt(songs.size())).value(), SoundSource.RECORDS, 0.5f, 1f);
                }
            }
            case DRINK, TOAST -> {
                p.swing(InteractionHand.MAIN_HAND, true);
                sound(p, e == Emote.DRINK ? SoundEvents.GENERIC_DRINK : SoundEvents.GLASS_HIT, 1f);
                if (e == Emote.DRINK) p.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0, false, false));
            }
            case SPIT -> { particles(p, ParticleTypes.SPIT, 4); sound(p, SoundEvents.LLAMA_SPIT, 1f); }
            case SLEEP -> { particles(p, ParticleTypes.CLOUD, 3); sit(p); }
            case FLEX -> { p.swing(InteractionHand.MAIN_HAND, true); p.swing(InteractionHand.OFF_HAND, true); particles(p, ParticleTypes.CRIT, 8); }
            case CRY -> { particles(p, ParticleTypes.FALLING_WATER, 12); sound(p, SoundEvents.VILLAGER_HURT, 1.3f); }
            case SIT -> sit(p);
        }
    }

    private static void hop(ServerPlayer p) {
        if (!p.onGround()) return;
        p.push(0, 0.4, 0);
        p.hurtMarked = true;
    }

    private static void sound(ServerPlayer p, SoundEvent s, float pitch) {
        p.level().playSound(null, p.blockPosition(), s, SoundSource.PLAYERS, 0.7f, pitch);
    }

    private static void particles(ServerPlayer p, ParticleOptions type, int n) {
        p.serverLevel().sendParticles(type, p.getX(), p.getEyeY() + 0.3, p.getZ(), n, 0.3, 0.2, 0.3, 0.02);
    }

    /** Sit where you stand, on an invisible stool. Sneak to get up. */
    public static void sit(ServerPlayer p) {
        if (p.isPassenger() || !p.onGround()) return;
        ArmorStand seat = EntityType.ARMOR_STAND.create(p.serverLevel());
        if (seat == null) return;
        seat.moveTo(p.getX(), p.getY() - 1.7, p.getZ(), p.getYRot(), 0);
        seat.setInvisible(true);
        seat.setNoGravity(true);
        seat.setInvulnerable(true);
        seat.setSilent(true);
        seat.addTag(SEAT);
        p.serverLevel().addFreshEntity(seat);
        p.startRiding(seat, true);
    }

    private static void tick(MinecraftServer server) {
        if (server.getTickCount() % 20 == 0) {
            for (ServerLevel w : server.getAllLevels()) {
                for (Entity e : w.getEntities(EntityType.ARMOR_STAND, a -> a.getTags().contains(SEAT) && !a.isVehicle())) e.discard();
            }
        }
        if (DANCING.isEmpty()) return;
        for (Map.Entry<UUID, Integer> e : Map.copyOf(DANCING).entrySet()) {
            ServerPlayer p = server.getPlayerList().getPlayer(e.getKey());
            int left = e.getValue();
            boolean singing = left < 0;
            int abs = Math.abs(left);
            if (p == null || abs <= 0) { DANCING.remove(e.getKey()); continue; }
            if (abs % 10 == 0) {
                if (singing) {
                    particles(p, ParticleTypes.NOTE, 1);
                    p.level().playSound(null, p.blockPosition(), SoundEvents.NOTE_BLOCK_FLUTE.value(), SoundSource.PLAYERS, 0.6f,
                            new float[]{0.7f, 0.8f, 0.9f, 1.05f, 0.9f, 0.8f, 1.2f, 1.05f}[(abs / 10) % 8]);
                } else {
                    p.swing((abs / 10) % 2 == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, true);
                    if (p.onGround() && (abs / 10) % 2 == 0) hop(p);
                    p.setYRot(p.getYRot() + 45);
                    particles(p, ParticleTypes.NOTE, 1);
                }
            }
            DANCING.put(e.getKey(), singing ? -(abs - 1) : abs - 1);
        }
    }
}
