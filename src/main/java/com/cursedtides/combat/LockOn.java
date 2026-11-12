package com.cursedtides.combat;

import com.cursedtides.CursedTides;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Lock-on targeting, as in the old pirate game: lock a foe (R on foot) and your aimed skills, guns, staffs and dolls
 * go for it, and your view follows it. The client picks and cycles the target; the server remembers it per player.
 */
public final class LockOn {
    private LockOn() {}

    /** Client to server: the entity id now locked, or -1 for none. */
    public record Lock(int entityId) implements CustomPayload {
        public static final CustomPayload.Id<Lock> ID = new CustomPayload.Id<>(CursedTides.id("lock_on"));
        public static final PacketCodec<RegistryByteBuf, Lock> CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, Lock::entityId, Lock::new);

        @Override
        public Id<? extends CustomPayload> getId() { return ID; }
    }

    private static final Map<UUID, Integer> LOCKED = new HashMap<>();

    public static void register() {
        PayloadTypeRegistry.playC2S().register(Lock.ID, Lock.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Lock.ID, (payload, ctx) -> ctx.server().execute(() -> {
            if (payload.entityId() < 0) LOCKED.remove(ctx.player().getUuid());
            else LOCKED.put(ctx.player().getUuid(), payload.entityId());
        }));
        ServerPlayConnectionEvents.DISCONNECT.register((h, s) -> LOCKED.remove(h.player.getUuid()));
    }

    /** The foe this player has locked, if it's still alive and within 40 blocks. */
    public static LivingEntity target(ServerPlayerEntity p) {
        Integer id = LOCKED.get(p.getUuid());
        if (id == null) return null;
        Entity e = p.getServerWorld().getEntityById(id);
        if (e instanceof LivingEntity l && l.isAlive() && l.squaredDistanceTo(p) < 40 * 40) return l;
        LOCKED.remove(p.getUuid());
        return null;
    }
}
