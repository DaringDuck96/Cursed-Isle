package com.cursedtides.client;

import net.minecraft.client.input.MouseButtonEvent;

/** Small bridges from the old input conventions our screens were written against. */
public final class UiCompat {
    private UiCompat() {}

    /** The button as it used to be numbered: 0 left, 1 right, 2 middle. */
    public static int button(MouseButtonEvent e) {
        return switch (e.button()) {
            case 1 -> 0;
            case 3 -> 1;
            case 2 -> 2;
            default -> e.button() - 1;
        };
    }
}
