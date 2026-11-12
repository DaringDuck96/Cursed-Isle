package com.cursedtides.profession;

/** Peacetime trades, levelled apart from the fighting proficiencies. */
public enum Profession {
    FISHING("Fishing", "More rare catches; double catches from 15; the legendary marlin from 25"),
    COOKING("Cooking", "Better meals: Fine dishes last half again as long, Masterwork twice"),
    BREWING("Brewing", "Better tonics and grog, as with cooking"),
    SMITHING("Smithing", "Hone and reinforce gear: tier II from 10, tier III from 20"),
    HUNTING("Hunting", "Extra hides, teeth and meat from wild creatures"),
    LEADERSHIP("Leadership", "Crewmates and shipmates near you fight harder: strength from 5, resistance from 15, regeneration from 25");

    public final String displayName;
    public final String perk;

    Profession(String displayName, String perk) {
        this.displayName = displayName;
        this.perk = perk;
    }
}
