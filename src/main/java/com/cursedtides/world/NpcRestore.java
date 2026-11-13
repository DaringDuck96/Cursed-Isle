package com.cursedtides.world;

import com.cursedtides.entity.StoryNpcEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

/**
 * Puts named story characters back at their posts. Useful for worlds created before a
 * character existed (the Broker and Commodore arrived in 0.5.0) or if one went missing.
 * Only checks spots near the player, where entities are loaded, so it never duplicates.
 */
public final class NpcRestore {
    private NpcRestore() {}

    public static int restoreNear(ServerPlayer player) {
        ServerLevel world = player.level();
        int restored = 0;
        for (Builds.NpcSpot spot : Builds.NPC_SPOTS) {
            double dx = spot.x() - player.getX(), dz = spot.z() - player.getZ();
            if (dx * dx + dz * dz > 96 * 96) {
                continue;
            }
            AABB around = new AABB(spot.x() - 24, spot.y() - 12, spot.z() - 24, spot.x() + 24, spot.y() + 12, spot.z() + 24);
            boolean present = !world.getEntitiesOfClass(StoryNpcEntity.class, around, npc -> npc.getRole() == spot.role()).isEmpty();
            if (present) {
                continue;
            }
            StoryNpcEntity npc = Builds.npc(world, spot.role());
            if (npc == null) {
                continue;
            }
            npc.snapTo(spot.x(), spot.y(), spot.z(), spot.yaw(), 0.0f);
            npc.setPersistenceRequired();
            world.addFreshEntity(npc);
            restored++;
        }
        return restored;
    }
}
