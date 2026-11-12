package com.cursedtides.social;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

import java.util.ArrayList;

/** Each crew gets a scoreboard team: its colours on members' names, its tag in the player list, and no friendly fire. */
public final class CrewTeams {
    private CrewTeams() {}

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(CrewTeams::sync);
    }

    public static void sync(MinecraftServer server) {
        ServerScoreboard sb = server.getScoreboard();
        for (Team t : new ArrayList<>(sb.getTeams())) {
            if (t.getName().startsWith("ct_")) sb.removeTeam(t);
        }
        for (CrewState.Crew c : CrewState.get(server).all()) {
            String id = "ct_" + Integer.toHexString(c.name.toLowerCase().hashCode());
            Team t = sb.addTeam(id);
            t.setColor(c.formatting());
            t.setPrefix(Text.literal("[" + c.name + "] ").formatted(c.formatting()));
            t.setFriendlyFireAllowed(false);
            t.setShowFriendlyInvisibles(true);
            for (String name : c.names.values()) sb.addScoreHolderToTeam(name, t);
        }
    }
}
