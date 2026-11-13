package com.cursedtides.combat;

import com.cursedtides.CursedTides;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
    public record Lock(int entityId) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<Lock> ID = new CustomPacketPayload.Type<>(CursedTides.id("lock_on"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Lock> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, Lock::entityId, Lock::new);

        @Override
        public Type<? extends CustomPacketPayload> type() { return ID; }
    }

    private static final Map<UUID, Integer> LOCKED = new HashMap<>();

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(Lock.ID, Lock.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Lock.ID, (payload, ctx) -> ctx.server().execute(() -> {
            if (payload.entityId() < 0) LOCKED.remove(ctx.player().getUUID());
            else LOCKED.put(ctx.player().getUUID(), payload.entityId());
        }));
        ServerPlayConnectionEvents.DISCONNECT.register((h, s) -> LOCKED.remove(h.player.getUUID()));
    }

    /** The foe this player has locked, if it's still alive and within 40 blocks. */
    public static LivingEntity target(ServerPlayer p) {
        Integer id = LOCKED.get(p.getUUID());
        if (id == null) return null;
        Entity e = p.level().getEntity(id);
        if (e instanceof LivingEntity l && l.isAlive() && l.distanceToSqr(p) < 40 * 40) return l;
        LOCKED.remove(p.getUUID());
        return null;
    }
}
