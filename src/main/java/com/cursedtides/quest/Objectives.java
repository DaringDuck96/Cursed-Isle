package com.cursedtides.quest;

import com.cursedtides.CursedTides;
import com.cursedtides.entity.StoryNpcEntity;
import com.cursedtides.item.ModItems;
import com.cursedtides.world.Archipelago;
import com.cursedtides.world.Builds;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

/**
 * Where your story wants you next, for the radar (a marker, or an arrow on its rim) and for the "!" floating over
 * whoever you should talk to.
 */
public final class Objectives {
    private Objectives() {}

    /** has = false when there's nowhere in particular to point. */
    public record Target(boolean has, int x, int z, String label, List<Integer> marked) implements CustomPayload {
        public static final CustomPayload.Id<Target> ID = new CustomPayload.Id<>(CursedTides.id("objective"));
        public static final PacketCodec<RegistryByteBuf, Target> CODEC = PacketCodec.tuple(
                PacketCodecs.BOOL, Target::has, PacketCodecs.VAR_INT, Target::x, PacketCodecs.VAR_INT, Target::z,
                PacketCodecs.STRING, Target::label, PacketCodecs.VAR_INT.collect(PacketCodecs.toList()), Target::marked, Target::new);

        @Override
        public Id<? extends CustomPayload> getId() { return ID; }
    }

    private record Goal(int x, int z, String label, StoryNpcEntity.Role role) {}

    private static Goal npc(StoryNpcEntity.Role role, String label) {
        for (Builds.NpcSpot s : Builds.NPC_SPOTS) if (s.role() == role) return new Goal((int) s.x(), (int) s.z(), label, role);
        return null;
    }

    private static Goal goal(ServerPlayerEntity p) {
        int stage = QuestData.stage(p);
        switch (stage) {
            case QuestData.START -> {
                return switch (Lessons.step(p)) {
                    case TALK -> npc(StoryNpcEntity.Role.KEEPER, "Keeper Wren");
                    case LAUNCH -> new Goal(0, 40, "The pier", null);
                    case SAIL -> new Goal(Lessons.BUOY_X, Lessons.BUOY_Z, "Marker buoy", null);
                    case GUNS -> new Goal(Lessons.HULK_X, Lessons.HULK_Z, "The old hulk", null);
                    default -> null;
                };
            }
            case QuestData.FIND_YSOLDE -> {
                return npc(StoryNpcEntity.Role.HARBORMASTER, "Ysolde Vane");
            }
            case QuestData.FIND_SHARDS -> {
                if (!QuestData.has(p, ModItems.REEF_SHARD)) {
                    return new Goal(Archipelago.SUNKEN_REACH.cx(), Archipelago.SUNKEN_REACH.cz(), "Reef Shard", null);
                }
                if (!QuestData.has(p, ModItems.EMBER_SHARD)) {
                    return new Goal(Archipelago.ASHFALL_ISLAND.cx() - 35, Archipelago.ASHFALL_ISLAND.cz(), "Ember Shard", null);
                }
                return npc(StoryNpcEntity.Role.BELLWRIGHT, "Tamsin Coil");
            }
            case QuestData.RING_BELL -> {
                return new Goal(Archipelago.ALTAR_X, Archipelago.ALTAR_Z, "The Stilling Bell", null);
            }
            default -> {
                return side(p);
            }
        }
    }

    /** With the main story done (or waiting), the next side thing: Act Four, a letter, then a contract. */
    private static Goal side(ServerPlayerEntity p) {
        int[] t = LanternArc.target(p);
        if (t != null) return new Goal(t[0], t[1], LanternArc.targetLabel(p), null);
        t = Letters.target(p);
        if (t != null) return new Goal(t[0], t[1], Letters.targetLabel(p), null);
        t = Contracts.target(p);
        if (t != null) return new Goal(t[0], t[1], Contracts.targetLabel(p), null);
        return null;
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(Target.ID, Target.CODEC);
        ServerTickEvents.END_SERVER_TICK.register(Objectives::tick);
    }

    private static void tick(MinecraftServer server) {
        if (server.getTicks() % 20 != 13) return;
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            Goal g = goal(p);
            List<Integer> marked = new ArrayList<>();
            if (g != null && g.role() != null) {
                for (StoryNpcEntity npc : p.getServerWorld().getEntitiesByClass(StoryNpcEntity.class, new Box(p.getBlockPos()).expand(72),
                        n -> n.getRole() == g.role())) {
                    marked.add(npc.getId());
                }
            }
            ServerPlayNetworking.send(p, g == null ? new Target(false, 0, 0, "", marked) : new Target(true, g.x(), g.z(), g.label(), marked));
        }
    }
}
