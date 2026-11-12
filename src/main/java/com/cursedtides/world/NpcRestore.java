package com.cursedtides.world;

import com.cursedtides.entity.StoryNpcEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;

/**
 * Puts named story characters back at their posts. Useful for worlds created before a
 * character existed (the Broker and Commodore arrived in 0.5.0) or if one went missing.
 * Only checks spots near the player, where entities are loaded, so it never duplicates.
 */
public final class NpcRestore {
    private NpcRestore() {}

    public static int restoreNear(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        int restored = 0;
        for (Builds.NpcSpot spot : Builds.NPC_SPOTS) {
            double dx = spot.x() - player.getX(), dz = spot.z() - player.getZ();
            if (dx * dx + dz * dz > 96 * 96) {
                continue;
            }
            Box around = new Box(spot.x() - 24, spot.y() - 12, spot.z() - 24, spot.x() + 24, spot.y() + 12, spot.z() + 24);
            boolean present = !world.getEntitiesByClass(StoryNpcEntity.class, around, npc -> npc.getRole() == spot.role()).isEmpty();
            if (present) {
                continue;
            }
            StoryNpcEntity npc = Builds.npc(world, spot.role());
            if (npc == null) {
                continue;
            }
            npc.refreshPositionAndAngles(spot.x(), spot.y(), spot.z(), spot.yaw(), 0.0f);
            npc.setPersistent();
            world.spawnEntity(npc);
            restored++;
        }
        return restored;
    }
}
