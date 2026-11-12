package com.cursedtides.appearance;

/**
 * A pirate's look, packed into one int so it travels cheaply:
 * bit 0 enabled, bit 1 slim build, then 3 bits each for skin tone, hair style, hair colour, facial hair,
 * marking and outfit, and the visible accessories (eyepatch, earring, bandana) in the top bits.
 */
public final class Look {
    private Look() {}

    public static final String[] TONES = {"Fair", "Rosy", "Olive", "Tan", "Brown", "Deep"};
    public static final String[] HAIR = {"Bald", "Cropped", "Long", "Ponytail", "Braids", "Topknot", "Wild"};
    public static final String[] HAIR_COLOURS = {"Black", "Dark brown", "Brown", "Auburn", "Ginger", "Blonde", "Grey", "White"};
    public static final String[] FACIAL = {"None", "Stubble", "Full beard", "Goatee", "Moustache", "Braided beard"};
    public static final String[] MARKINGS = {"None", "Cheek scar", "Eye scar", "Face tattoo", "Arm tattoos", "Freckles"};
    public static final String[] OUTFITS = {"Linen shirt", "Striped shirt", "Dark vest", "Red sash"};
    public static final int[] HAIR_TINT = {0x1C1A18, 0x3A2618, 0x6A4428, 0x8A3A1E, 0xC0602A, 0xE0C070, 0x9A9A9A, 0xE8E8E4};

    public static final int ACC_EYEPATCH = 1, ACC_EARRING = 2, ACC_BANDANA_RED = 4, ACC_BANDANA_BLACK = 8;

    public static boolean enabled(int c) { return (c & 1) != 0; }
    public static boolean slim(int c) { return (c & 2) != 0; }
    public static int tone(int c) { return (c >> 2) & 7; }
    public static int hair(int c) { return (c >> 5) & 7; }
    public static int hairColour(int c) { return (c >> 8) & 7; }
    public static int facial(int c) { return (c >> 11) & 7; }
    public static int marking(int c) { return (c >> 14) & 7; }
    public static int outfit(int c) { return (c >> 17) & 3; }
    public static int accessories(int c) { return (c >> 20) & 15; }

    public static int pack(boolean enabled, boolean slim, int tone, int hair, int hairColour, int facial, int marking, int outfit, int acc) {
        return (enabled ? 1 : 0) | (slim ? 2 : 0) | (clamp(tone, TONES.length) << 2) | (clamp(hair, HAIR.length) << 5)
                | (clamp(hairColour, HAIR_COLOURS.length) << 8) | (clamp(facial, FACIAL.length) << 11)
                | (clamp(marking, MARKINGS.length) << 14) | (clamp(outfit, OUTFITS.length) << 17) | ((acc & 15) << 20);
    }

    public static int withAccessories(int code, int acc) {
        return (code & ~(15 << 20)) | ((acc & 15) << 20);
    }

    private static int clamp(int v, int n) {
        return Math.floorMod(v, n);
    }
}
