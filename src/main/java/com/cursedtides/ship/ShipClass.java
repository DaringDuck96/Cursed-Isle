package com.cursedtides.ship;

import com.cursedtides.item.ModItems;
import net.minecraft.item.Item;

/** The three hulls a captain can own. Bigger ships are tougher, carry more and hit harder, but sail slower. */
public enum ShipClass {
    //          name          scale  hull  cargo rows  cannons/side  speed  crew seats
    SLOOP("Sloop", 1.0f, 120, 3, 2, 1.25, 3),
    BRIGANTINE("Brigantine", 1.3f, 220, 4, 3, 1.05, 4),
    GALLEON("Galleon", 1.65f, 380, 6, 4, 0.85, 6),
    /** Fast and handy: a trader's ship, light on guns. */
    SCHOONER("Schooner", 1.15f, 160, 4, 2, 1.35, 4),
    /** A two-masted fighting ship, heavier than a brigantine. */
    BRIG("Brig", 1.4f, 280, 4, 4, 1.0, 5),
    /** The best warship a captain can buy: strong, well gunned, and quick for her size. */
    FRIGATE("Frigate", 1.55f, 340, 5, 5, 1.02, 6),
    /** The heaviest ship afloat: a floating fortress, slow to turn and slower to sink. */
    SHIP_OF_THE_LINE("Ship of the Line", 1.9f, 560, 6, 7, 0.82, 8);

    private final String displayName;
    private final float scale;
    private final int maxHull;
    private final int cargoRows;
    private final int cannonsPerSide;
    private final double speed;
    private final int seats;

    ShipClass(String displayName, float scale, int maxHull, int cargoRows, int cannonsPerSide, double speed, int seats) {
        this.displayName = displayName;
        this.scale = scale;
        this.maxHull = maxHull;
        this.cargoRows = cargoRows;
        this.cannonsPerSide = cannonsPerSide;
        this.speed = speed;
        this.seats = seats;
    }

    public String displayName() { return displayName; }
    public float scale() { return scale; }
    public int maxHull() { return maxHull; }
    public int cargoRows() { return cargoRows; }
    public int cannonsPerSide() { return cannonsPerSide; }
    public double speed() { return speed; }
    public int seats() { return seats; }

    public Item deed() {
        return switch (this) {
            case SLOOP -> ModItems.SLOOP_DEED;
            case BRIGANTINE -> ModItems.BRIGANTINE_DEED;
            case GALLEON -> ModItems.GALLEON_DEED;
            case SCHOONER -> ModItems.SCHOONER_DEED;
            case BRIG -> ModItems.BRIG_DEED;
            case FRIGATE -> ModItems.FRIGATE_DEED;
            case SHIP_OF_THE_LINE -> ModItems.SHIP_OF_THE_LINE_DEED;
        };
    }

    /** How many masts she carries (for the model). */
    public int masts() {
        return switch (this) {
            case SLOOP -> 1;
            case GALLEON, FRIGATE, SHIP_OF_THE_LINE -> 3;
            default -> 2;
        };
    }

    public static ShipClass byIndex(int i) {
        ShipClass[] all = values();
        return all[Math.max(0, Math.min(all.length - 1, i))];
    }
}
