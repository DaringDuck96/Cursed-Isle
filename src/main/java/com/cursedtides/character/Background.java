package com.cursedtides.character;

/** Where your pirate came from: shapes your starting skills, kit, clothes and standing. */
public enum Background {
    CASTAWAY("Castaway Sailor",
            "Twenty years before the mast, until the ship went down under you.",
            "+Sailing, a full sailor's outfit in your colour, a spyglass"),
    DESERTER("Tidewatch Deserter",
            "You wore the blue once. You walked away with a pistol and the coat.",
            "+Firearms, a flintlock and shot, a Tidewatch coat. The Navy remembers you; the Brethren like you for it"),
    SMUGGLER("Smuggler's Child",
            "Raised in the coves, running contraband past the patrols.",
            "+Blades, a dagger and throwing knives, extra doubloons. The Brethren know your family"),
    HEXER("Bayou-born Hexer",
            "You grew up in the mangroves, where old words still hold power.",
            "+Hexes, a hex staff and bayou oilskins");

    private final String title;
    private final String story;
    private final String perks;

    Background(String title, String story, String perks) {
        this.title = title;
        this.story = story;
        this.perks = perks;
    }

    public String title() { return title; }
    public String story() { return story; }
    public String perks() { return perks; }

    public static Background byIndex(int i) {
        Background[] all = values();
        return all[Math.max(0, Math.min(all.length - 1, i))];
    }
}
