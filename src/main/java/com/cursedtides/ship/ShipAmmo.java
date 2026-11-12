package com.cursedtides.ship;

import com.cursedtides.item.ModItems;
import net.minecraft.item.Item;

/** What the ship's guns are loaded with. Cycle with C while aboard. */
public enum ShipAmmo {
    /** Solid shot: hull damage and a small blast. */
    ROUND("Round shot"),
    /** Two balls on a chain: little hull damage, but tears the target's sails for a while. */
    CHAIN("Chain shot"),
    /** A spray of small balls: shreds crews and anyone on deck, barely scratches hulls. */
    GRAPE("Grapeshot"),
    /** A shell that bursts on impact: the heaviest hull damage. */
    EXPLOSIVE("Explosive shell");

    private final String displayName;

    ShipAmmo(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public Item item() {
        return switch (this) {
            case ROUND -> ModItems.CANNONBALL;
            case CHAIN -> ModItems.CHAIN_SHOT;
            case GRAPE -> ModItems.GRAPESHOT;
            case EXPLOSIVE -> ModItems.EXPLOSIVE_SHELL;
        };
    }

    public static ShipAmmo byIndex(int i) {
        ShipAmmo[] all = values();
        return all[Math.floorMod(i, all.length)];
    }
}
