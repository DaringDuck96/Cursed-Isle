package com.cursedtides.faction;

import net.minecraft.util.Formatting;

/** The powers of the Shattered Reach that remember what you do. */
public enum Faction {
    NAVY("Tidewatch Navy", Formatting.BLUE),
    BRETHREN("Saltmarrow Brethren", Formatting.DARK_RED),
    SMUGGLERS("Raven's Landing Smugglers", Formatting.DARK_PURPLE);

    private final String displayName;
    private final Formatting color;

    Faction(String displayName, Formatting color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String displayName() { return displayName; }
    public Formatting color() { return color; }

    public Faction rival() {
        return this == NAVY ? BRETHREN : NAVY; // smugglers and Brethren both answer to the Navy's guns
    }
}
