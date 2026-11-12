package com.cursedtides.faction;

/** Repeatable jobs offered by the Commodore (Navy) and the Broker (Brethren). One active at a time. */
public enum ContractType {
    NONE(0, null, "", 0, 0, 0),
    NAVY_UNDEAD(1, Faction.NAVY, "Put down 10 cursed pirates", 10, 25, 40),
    NAVY_GHOST(2, Faction.NAVY, "Sink a ghost ship", 1, 50, 80),
    BRETHREN_MARINES(3, Faction.BRETHREN, "Kill 5 Tidewatch marines", 5, 30, 50),
    BRETHREN_PATROL(4, Faction.BRETHREN, "Sink a Tidewatch patrol ship", 1, 60, 100),
    BRETHREN_UNDEAD(5, Faction.BRETHREN, "Clear 8 cursed pirates off the trade lanes", 8, 20, 35),
    SMUGGLER_RUN(6, Faction.SMUGGLERS, "Sell 6 crates of contraband to a black market", 6, 35, 50),
    SMUGGLER_SLIP(7, Faction.SMUGGLERS, "Give 2 Tidewatch inspections the slip", 2, 40, 60),
    SMUGGLER_IDOLS(8, Faction.SMUGGLERS, "Fence 3 cursed idols on the black market", 3, 45, 60),
    SMUGGLER_CONVOY(9, Faction.SMUGGLERS, "Capture a merchantman", 1, 50, 70);

    private final int id;
    private final Faction giver;
    private final String description;
    private final int goal;
    private final int doubloons;
    private final int reputation;

    ContractType(int id, Faction giver, String description, int goal, int doubloons, int reputation) {
        this.id = id;
        this.giver = giver;
        this.description = description;
        this.goal = goal;
        this.doubloons = doubloons;
        this.reputation = reputation;
    }

    public int id() { return id; }
    public Faction giver() { return giver; }
    public String description() { return description; }
    public int goal() { return goal; }
    public int doubloons() { return doubloons; }
    public int reputation() { return reputation; }

    public static ContractType byId(int id) {
        for (ContractType t : values()) {
            if (t.id == id) return t;
        }
        return NONE;
    }
}
