package com.cursedtides.living;

import com.cursedtides.entity.QuartermasterEntity;
import net.minecraft.world.entity.Entity;

/** Opening hours. Honest shops keep daylight hours; fences and smugglers only deal after dark. Peddlers keep their own. */
public final class Hours {
    private Hours() {}

    public static boolean night(Entity e) {
        long t = e.level().getOverworldClockTime() % 24000;
        return t >= 13000 && t < 23000;
    }

    public static boolean isOpen(Entity npc) {
        if (!(npc instanceof QuartermasterEntity qm)) return true;
        String trade = qm.tradeName();
        if (trade.equals("peddler") || trade.isEmpty() || trade.equals("cook")) return true; // the general quartermaster and the cookhouse never close
        boolean nightTrade = trade.equals("fence") || trade.equals("smuggler");
        return nightTrade == night(npc);
    }

    public static String closedLine(Entity npc) {
        if (npc instanceof QuartermasterEntity qm && (qm.tradeName().equals("fence") || qm.tradeName().equals("smuggler"))) {
            return "Not in daylight, friend. Too many eyes. Come back after dark.";
        }
        return "We're shut for the night. Shutters open at dawn.";
    }
}
