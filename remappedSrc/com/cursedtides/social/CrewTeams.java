package com.cursedtides.social;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.PlayerTeam;
import java.util.ArrayList;

/** Each crew gets a scoreboard team: its colours on members' names, its tag in the player list, and no friendly fire. */
public final class CrewTeams {
    private CrewTeams() {}

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(CrewTeams::sync);
    }

    public static void sync(MinecraftServer server) {
        ServerScoreboard sb = server.getScoreboard();
        for (PlayerTeam t : new ArrayList<>(sb.getPlayerTeams())) {
            if (t.getName().startsWith("ct_")) sb.removePlayerTeam(t); // the old one-team-per-crew nameplates
        }
        Nameplates.syncAll(server);
    }
}
