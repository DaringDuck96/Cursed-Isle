package com.cursedtides.faction;

import net.minecraft.ChatFormatting;

/** The powers of the Shattered Reach that remember what you do. */
public enum Faction {
    NAVY("Tidewatch Navy", ChatFormatting.BLUE),
    BRETHREN("Saltmarrow Brethren", ChatFormatting.DARK_RED),
    SMUGGLERS("Raven's Landing Smugglers", ChatFormatting.DARK_PURPLE);

    private final String displayName;
    private final ChatFormatting color;

    Faction(String displayName, ChatFormatting color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String displayName() { return displayName; }
    public ChatFormatting color() { return color; }

    public Faction rival() {
        return this == NAVY ? BRETHREN : NAVY; // smugglers and Brethren both answer to the Navy's guns
    }
}
