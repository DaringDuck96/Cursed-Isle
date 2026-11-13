package com.cursedtides.util;

import com.cursedtides.CursedTides;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Registration helpers. Items and blocks must know their own id before they're built, so each registration names
 * its id first (Reg.itemId / Reg.itemName / Reg.blockId), and the properties made right after pick it up.
 */
public final class Reg {
    private Reg() {}

    private static ResourceKey<Item> item;
    private static ResourceKey<Block> block;

    public static String itemName(String name) {
        item = ResourceKey.create(Registries.ITEM, CursedTides.id(name));
        return name;
    }

    public static Identifier itemId(String name) {
        itemName(name);
        return CursedTides.id(name);
    }

    public static Item.Properties props() {
        if (item == null) throw new IllegalStateException("Item properties made before the item's id was named");
        return new Item.Properties().setId(item);
    }

    public static Identifier blockId(String name) {
        block = ResourceKey.create(Registries.BLOCK, CursedTides.id(name));
        return CursedTides.id(name);
    }

    public static ResourceKey<Block> blockKey() {
        return block;
    }

    public static ResourceKey<EntityType<?>> entity(String name) {
        return ResourceKey.create(Registries.ENTITY_TYPE, CursedTides.id(name));
    }
}
