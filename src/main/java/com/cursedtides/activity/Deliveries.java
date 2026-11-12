package com.cursedtides.activity;

import com.cursedtides.CursedTides;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/** Odd jobs: carry a sealed parcel to another port before it's due. */
public final class Deliveries {
    private Deliveries() {}

    public static final Item PARCEL = Registry.register(Registries.ITEM, CursedTides.id("parcel"), new ParcelItem(new Item.Settings()));

    public static void init() { }
}
