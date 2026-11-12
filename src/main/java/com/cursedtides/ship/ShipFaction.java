package com.cursedtides.ship;

/** Who sails a ship. PLAYER ships are steered by their owner; the others steer themselves. */
public enum ShipFaction {
    PLAYER, DROWNED, NAVY,
    /** Unarmed traders under Tidewatch protection: they flee, and can be taken. */
    MERCHANT,
    /** Rival pirates outside the Brethren: they attack anyone, and fight the Navy. */
    PIRATE;

    public static ShipFaction byIndex(int i) {
        ShipFaction[] all = values();
        return all[Math.max(0, Math.min(all.length - 1, i))];
    }
}
