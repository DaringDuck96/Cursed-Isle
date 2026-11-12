package com.cursedtides.activity;

import com.cursedtides.CursedTides;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import com.mojang.brigadier.arguments.StringArgumentType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

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

    public record Do(int id) implements CustomPayload {
        public static final Id<Do> ID = new Id<>(CursedTides.id("emote"));
        public static final PacketCodec<RegistryByteBuf, Do> CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, Do::id, Do::new);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
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
                        perform(ctx.getSource().getPlayerOrThrow(), Emote.valueOf(StringArgumentType.getString(ctx, "name").toUpperCase()));
                        return 1;
                    } catch (IllegalArgumentException e) {
                        return 0;
                    }
                }))));
        ServerTickEvents.END_SERVER_TICK.register(Emotes::tick);
    }

    public static void perform(ServerPlayerEntity p, Emote e) {
        ServerWorld w = p.getServerWorld();
        long now = w.getTime();
        if (LAST.getOrDefault(p.getUuid(), 0L) > now - 20) return;
        LAST.put(p.getUuid(), now);
        Text line = Text.literal("* " + com.cursedtides.character.CharacterData.pirateName(p) + " " + e.verb + ".").formatted(Formatting.GRAY, Formatting.ITALIC);
        for (ServerPlayerEntity other : w.getPlayers(o -> o.squaredDistanceTo(p) < 32 * 32)) other.sendMessage(line, false);
        switch (e) {
            case WAVE, POINT, SALUTE -> { p.swingHand(Hand.MAIN_HAND, true); sound(p, SoundEvents.ENTITY_VILLAGER_AMBIENT, 1.4f); }
            case BOW -> { p.setPitch(60); p.swingHand(Hand.OFF_HAND, true); sound(p, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER.value(), 1f); }
            case CHEER -> { hop(p); p.swingHand(Hand.MAIN_HAND, true); particles(p, ParticleTypes.HAPPY_VILLAGER, 10); sound(p, SoundEvents.ENTITY_VILLAGER_CELEBRATE, 1.2f); }
            case LAUGH -> { particles(p, ParticleTypes.NOTE, 3); sound(p, SoundEvents.ENTITY_WITCH_CELEBRATE, 1.4f); }
            case SHRUG -> sound(p, SoundEvents.ENTITY_VILLAGER_NO, 1.2f);
            case DANCE -> DANCING.put(p.getUuid(), 100);
            case SING -> {
                DANCING.put(p.getUuid(), -100);
                if (LAST_SONG.getOrDefault(p.getUuid(), -10000L) < now - 1200) {
                    LAST_SONG.put(p.getUuid(), now);
                    var songs = java.util.List.of(com.cursedtides.sound.ModSounds.SALTMARROW_HORNPIPE, com.cursedtides.sound.ModSounds.WRECKERS_REEL,
                            com.cursedtides.sound.ModSounds.LANTERNS_DROWN);
                    p.getWorld().playSound(null, p.getBlockPos(), songs.get(p.getRandom().nextInt(songs.size())).value(), SoundCategory.RECORDS, 0.5f, 1f);
                }
            }
            case DRINK, TOAST -> {
                p.swingHand(Hand.MAIN_HAND, true);
                sound(p, e == Emote.DRINK ? SoundEvents.ENTITY_GENERIC_DRINK : SoundEvents.BLOCK_GLASS_HIT, 1f);
                if (e == Emote.DRINK) p.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 60, 0, false, false));
            }
            case SPIT -> { particles(p, ParticleTypes.SPIT, 4); sound(p, SoundEvents.ENTITY_LLAMA_SPIT, 1f); }
            case SLEEP -> { particles(p, ParticleTypes.CLOUD, 3); sit(p); }
            case FLEX -> { p.swingHand(Hand.MAIN_HAND, true); p.swingHand(Hand.OFF_HAND, true); particles(p, ParticleTypes.CRIT, 8); }
            case CRY -> { particles(p, ParticleTypes.FALLING_WATER, 12); sound(p, SoundEvents.ENTITY_VILLAGER_HURT, 1.3f); }
            case SIT -> sit(p);
        }
    }

    private static void hop(ServerPlayerEntity p) {
        if (!p.isOnGround()) return;
        p.addVelocity(0, 0.4, 0);
        p.velocityModified = true;
    }

    private static void sound(ServerPlayerEntity p, SoundEvent s, float pitch) {
        p.getWorld().playSound(null, p.getBlockPos(), s, SoundCategory.PLAYERS, 0.7f, pitch);
    }

    private static void particles(ServerPlayerEntity p, ParticleEffect type, int n) {
        p.getServerWorld().spawnParticles(type, p.getX(), p.getEyeY() + 0.3, p.getZ(), n, 0.3, 0.2, 0.3, 0.02);
    }

    /** Sit where you stand, on an invisible stool. Sneak to get up. */
    public static void sit(ServerPlayerEntity p) {
        if (p.hasVehicle() || !p.isOnGround()) return;
        ArmorStandEntity seat = EntityType.ARMOR_STAND.create(p.getServerWorld());
        if (seat == null) return;
        seat.refreshPositionAndAngles(p.getX(), p.getY() - 1.7, p.getZ(), p.getYaw(), 0);
        seat.setInvisible(true);
        seat.setNoGravity(true);
        seat.setInvulnerable(true);
        seat.setSilent(true);
        seat.addCommandTag(SEAT);
        p.getServerWorld().spawnEntity(seat);
        p.startRiding(seat, true);
    }

    private static void tick(MinecraftServer server) {
        if (server.getTicks() % 20 == 0) {
            for (ServerWorld w : server.getWorlds()) {
                for (Entity e : w.getEntitiesByType(EntityType.ARMOR_STAND, a -> a.getCommandTags().contains(SEAT) && !a.hasPassengers())) e.discard();
            }
        }
        if (DANCING.isEmpty()) return;
        for (Map.Entry<UUID, Integer> e : Map.copyOf(DANCING).entrySet()) {
            ServerPlayerEntity p = server.getPlayerManager().getPlayer(e.getKey());
            int left = e.getValue();
            boolean singing = left < 0;
            int abs = Math.abs(left);
            if (p == null || abs <= 0) { DANCING.remove(e.getKey()); continue; }
            if (abs % 10 == 0) {
                if (singing) {
                    particles(p, ParticleTypes.NOTE, 1);
                    p.getWorld().playSound(null, p.getBlockPos(), SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), SoundCategory.PLAYERS, 0.6f,
                            new float[]{0.7f, 0.8f, 0.9f, 1.05f, 0.9f, 0.8f, 1.2f, 1.05f}[(abs / 10) % 8]);
                } else {
                    p.swingHand((abs / 10) % 2 == 0 ? Hand.MAIN_HAND : Hand.OFF_HAND, true);
                    if (p.isOnGround() && (abs / 10) % 2 == 0) hop(p);
                    p.setYaw(p.getYaw() + 45);
                    particles(p, ParticleTypes.NOTE, 1);
                }
            }
            DANCING.put(e.getKey(), singing ? -(abs - 1) : abs - 1);
        }
    }
}
