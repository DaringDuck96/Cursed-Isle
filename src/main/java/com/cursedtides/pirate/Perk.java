package com.cursedtides.pirate;

/**
 * The skill trees: every proficiency has three tiers (levels 5, 10 and 20), and at each tier
 * you pick one of two perks. Each perk raises one stat.
 */
public enum Perk {
    // Blades
    RIPOSTE(Weapon.CUTLASS, 0, 0, "Riposte", "Parry window +0.3s", Stat.PARRY_TICKS, 6),
    SHARPENED_EDGE(Weapon.CUTLASS, 0, 1, "Sharpened Edge", "+1.5 melee damage", Stat.MELEE, 1.5),
    FLEET_FOOTED(Weapon.CUTLASS, 1, 0, "Fleet-Footed", "Dodge roll recovers 0.6s faster", Stat.DODGE_CD, 12),
    HEAVY_HAND(Weapon.CUTLASS, 1, 1, "Heavy Hand", "+2 melee damage", Stat.MELEE, 2),
    DUELISTS_GRACE(Weapon.CUTLASS, 2, 0, "Duelist's Grace", "Parry window +0.5s", Stat.PARRY_TICKS, 10),
    REAVER(Weapon.CUTLASS, 2, 1, "Reaver", "+6 max health", Stat.MAX_HEALTH, 6),
    // Firearms
    QUICK_HANDS(Weapon.PISTOL, 0, 0, "Quick Hands", "Reload 20% faster", Stat.RELOAD, 0.2),
    HEAVY_BALL(Weapon.PISTOL, 0, 1, "Heavy Ball", "+15% firearm damage", Stat.GUN_DAMAGE, 0.15),
    FRUGAL_POWDER(Weapon.PISTOL, 1, 0, "Frugal Powder", "25% chance to keep your shot", Stat.AMMO_SAVE, 0.25),
    MARKSMAN(Weapon.PISTOL, 1, 1, "Marksman", "+20% firearm damage", Stat.GUN_DAMAGE, 0.2),
    GUNSLINGER(Weapon.PISTOL, 2, 0, "Gunslinger", "Reload 35% faster", Stat.RELOAD, 0.35),
    DEADEYE(Weapon.PISTOL, 2, 1, "Deadeye", "+35% firearm damage", Stat.GUN_DAMAGE, 0.35),
    // Cannon
    GUN_CREW_DRILL(Weapon.CANNON, 0, 0, "Gun Crew Drill", "Broadsides reload 15% faster", Stat.CANNON_RELOAD, 0.15),
    BALL_SAVER(Weapon.CANNON, 0, 1, "Ball Saver", "20% chance a broadside costs no shot", Stat.BROADSIDE_SAVE, 0.2),
    RAPID_FIRE(Weapon.CANNON, 1, 0, "Rapid Fire", "Broadsides reload 20% faster", Stat.CANNON_RELOAD, 0.2),
    THRIFTY_GUNNER(Weapon.CANNON, 1, 1, "Thrifty Gunner", "30% chance a broadside costs no shot", Stat.BROADSIDE_SAVE, 0.3),
    MASTER_GUNNER(Weapon.CANNON, 2, 0, "Master Gunner", "Broadsides reload 30% faster", Stat.CANNON_RELOAD, 0.3),
    POWDER_MONKEY(Weapon.CANNON, 2, 1, "Powder Monkey", "45% chance a broadside costs no shot", Stat.BROADSIDE_SAVE, 0.45),
    // Hexes
    DEEP_WELL(Weapon.HEX, 0, 0, "Deep Well", "Voodoo refills faster", Stat.VOODOO_REGEN, 1),
    SHARPER_CURSE(Weapon.HEX, 0, 1, "Sharper Curse", "+2 hex damage", Stat.HEX_DAMAGE, 2),
    THRIFT_OF_SPIRITS(Weapon.HEX, 1, 0, "Thrift of Spirits", "Hexes cost 25% less voodoo", Stat.HEX_COST, 0.25),
    WITHERING_WORD(Weapon.HEX, 1, 1, "Withering Word", "+3 hex damage", Stat.HEX_DAMAGE, 3),
    ENDLESS_TIDE(Weapon.HEX, 2, 0, "Endless Tide", "Voodoo refills much faster", Stat.VOODOO_REGEN, 3),
    DOOM(Weapon.HEX, 2, 1, "Doom", "+5 hex damage", Stat.HEX_DAMAGE, 5),
    // Sailing
    TRIM_THE_SHEETS(Weapon.SAILING, 0, 0, "Trim the Sheets", "+5% ship speed", Stat.SAIL_SPEED, 5),
    SHIPWRIGHTS_EYE(Weapon.SAILING, 0, 1, "Shipwright's Eye", "Planks patch 6 more hull", Stat.REPAIR, 6),
    FAIR_WINDS(Weapon.SAILING, 1, 0, "Fair Winds", "+8% ship speed", Stat.SAIL_SPEED, 8),
    SEA_LEGS(Weapon.SAILING, 1, 1, "Sea Legs", "Walk 10% faster", Stat.SPEED, 0.01),
    WIND_CALLER(Weapon.SAILING, 2, 0, "Wind Caller", "+15% ship speed", Stat.SAIL_SPEED, 15),
    IRON_KEEL(Weapon.SAILING, 2, 1, "Iron Keel", "Planks patch 15 more hull", Stat.REPAIR, 15),
    // Exploration
    KEEN_EYE(Weapon.EXPLORATION, 0, 0, "Keen Eye", "+1 luck (better loot)", Stat.LUCK, 1),
    COIN_SENSE(Weapon.EXPLORATION, 0, 1, "Coin Sense", "+5 doubloons for each discovery and dig", Stat.FINDERS_FEE, 5),
    TREASURE_NOSE(Weapon.EXPLORATION, 1, 0, "Treasure Nose", "+2 luck", Stat.LUCK, 2),
    TOUGH_AS_ROPE(Weapon.EXPLORATION, 1, 1, "Tough as Old Rope", "+4 max health", Stat.MAX_HEALTH, 4),
    FORTUNES_FAVOUR(Weapon.EXPLORATION, 2, 0, "Fortune's Favour", "+3 luck", Stat.LUCK, 3),
    SCHOLAR(Weapon.EXPLORATION, 2, 1, "Scholar of the Reach", "+25% proficiency XP", Stat.XP, 0.25);

    public enum Stat {
        MELEE, MAX_HEALTH, SPEED, LUCK, PARRY_TICKS, DODGE_CD, GUN_DAMAGE, RELOAD, AMMO_SAVE,
        CANNON_RELOAD, BROADSIDE_SAVE, HEX_DAMAGE, HEX_COST, VOODOO_REGEN, SAIL_SPEED, REPAIR, FINDERS_FEE, XP
    }

    public static final int[] TIER_LEVELS = {5, 10, 20};

    private final Weapon tree;
    private final int tier;
    private final int option;
    private final String title;
    private final String effect;
    private final Stat stat;
    private final double amount;

    Perk(Weapon tree, int tier, int option, String title, String effect, Stat stat, double amount) {
        this.tree = tree;
        this.tier = tier;
        this.option = option;
        this.title = title;
        this.effect = effect;
        this.stat = stat;
        this.amount = amount;
    }

    public Weapon tree() { return tree; }
    public int tier() { return tier; }
    public int option() { return option; }
    public String title() { return title; }
    public String effect() { return effect; }
    public Stat stat() { return stat; }
    public double amount() { return amount; }

    public static Perk of(Weapon tree, int tier, int option) {
        for (Perk p : values()) {
            if (p.tree == tree && p.tier == tier && p.option == option) return p;
        }
        return null;
    }
}
