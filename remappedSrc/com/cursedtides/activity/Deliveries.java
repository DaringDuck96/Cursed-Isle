package com.cursedtides.activity;

import com.cursedtides.CursedTides;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

/** Odd jobs: carry a sealed parcel to another port before it's due. */
public final class Deliveries {
    private Deliveries() {}

    public static final Item PARCEL = Registry.register(BuiltInRegistries.ITEM, CursedTides.id("parcel"), new ParcelItem(new Item.Properties()));

    public static void init() { }
}
