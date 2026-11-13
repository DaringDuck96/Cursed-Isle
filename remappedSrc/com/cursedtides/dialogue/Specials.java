package com.cursedtides.dialogue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * Named bits of business a conversation can start: "action": "special:arena" and so on.
 * A handler returns lines for the NPC to say (then the conversation continues at "then"),
 * or null/empty when it has opened something else or there's nothing to add.
 */
public final class Specials {
    private Specials() {}

    public interface Handler {
        List<String> run(ServerPlayer player, Entity npc);
    }

    private static final Map<String, Handler> HANDLERS = new HashMap<>();

    public static void register(String id, Handler handler) {
        HANDLERS.put(id, handler);
    }

    static List<String> run(String id, ServerPlayer player, Entity npc) {
        Handler h = HANDLERS.get(id);
        return h == null ? List.of("(Nothing comes of it.)") : h.run(player, npc);
    }
}
