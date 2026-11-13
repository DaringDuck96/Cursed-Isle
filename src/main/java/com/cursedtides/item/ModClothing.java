package com.cursedtides.item;

import net.minecraft.world.item.equipment.ArmorType;
import com.cursedtides.CursedTides;
import java.util.EnumMap;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Wearable pirate clothing, built on the armour system so it shows on your character.
 * It's clothing first: light protection only. Sailor's garb can be dyed any colour.
 */
public final class ModClothing {
    private ModClothing() {}

    private static ArmorMaterial material(String name, int chest) {
        java.util.Map<ArmorType, Integer> defense = new java.util.EnumMap<>(ArmorType.class);
        defense.put(ArmorType.HELMET, 1);
        defense.put(ArmorType.CHESTPLATE, chest);
        defense.put(ArmorType.LEGGINGS, 1);
        defense.put(ArmorType.BOOTS, 1);
        defense.put(ArmorType.BODY, 2);
        return new ArmorMaterial(10, defense, 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0f, 0.0f,
                net.minecraft.tags.ItemTags.REPAIRS_LEATHER_ARMOR,
                net.minecraft.resources.ResourceKey.create(net.minecraft.world.item.equipment.EquipmentAssets.ROOT_ID, CursedTides.id(name)));
    }

    public static final ArmorMaterial SAILOR = material("sailor", 2);
    public static final ArmorMaterial CAPTAIN = material("captain", 3);
    public static final ArmorMaterial NAVY = material("navy_uniform", 3);
    public static final ArmorMaterial BAYOU = material("bayou", 2);

    private static Item piece(String name, ArmorMaterial material, ArmorType type, Rarity rarity) {
        return Registry.register(BuiltInRegistries.ITEM, com.cursedtides.util.Reg.itemId(name),
                new Item(com.cursedtides.util.Reg.props().humanoidArmor(material, type).rarity(rarity)));
    }

    // Sailor's garb (dyeable)
    public static final Item SAILOR_BANDANA = piece("sailor_bandana", SAILOR, ArmorType.HELMET, Rarity.COMMON);
    public static final Item SAILOR_SHIRT = piece("sailor_shirt", SAILOR, ArmorType.CHESTPLATE, Rarity.COMMON);
    public static final Item SAILOR_BREECHES = piece("sailor_breeches", SAILOR, ArmorType.LEGGINGS, Rarity.COMMON);
    public static final Item SAILOR_BOOTS = piece("sailor_boots", SAILOR, ArmorType.BOOTS, Rarity.COMMON);
    // Captain's attire
    public static final Item CAPTAIN_HAT = piece("captain_hat", CAPTAIN, ArmorType.HELMET, Rarity.UNCOMMON);
    public static final Item CAPTAIN_COAT = piece("captain_coat", CAPTAIN, ArmorType.CHESTPLATE, Rarity.UNCOMMON);
    public static final Item CAPTAIN_BREECHES = piece("captain_breeches", CAPTAIN, ArmorType.LEGGINGS, Rarity.UNCOMMON);
    public static final Item CAPTAIN_BOOTS = piece("captain_boots", CAPTAIN, ArmorType.BOOTS, Rarity.UNCOMMON);
    // Tidewatch uniform
    public static final Item NAVY_CAP = piece("navy_cap", NAVY, ArmorType.HELMET, Rarity.UNCOMMON);
    public static final Item NAVY_COAT = piece("navy_coat", NAVY, ArmorType.CHESTPLATE, Rarity.UNCOMMON);
    public static final Item NAVY_TROUSERS = piece("navy_trousers", NAVY, ArmorType.LEGGINGS, Rarity.UNCOMMON);
    public static final Item NAVY_BOOTS = piece("navy_boots", NAVY, ArmorType.BOOTS, Rarity.UNCOMMON);
    // Bayou oilskins
    public static final Item BAYOU_HAT = piece("bayou_hat", BAYOU, ArmorType.HELMET, Rarity.COMMON);
    public static final Item BAYOU_OILSKIN = piece("bayou_oilskin", BAYOU, ArmorType.CHESTPLATE, Rarity.COMMON);
    public static final Item BAYOU_WADERS = piece("bayou_waders", BAYOU, ArmorType.LEGGINGS, Rarity.COMMON);
    public static final Item BAYOU_BOOTS = piece("bayou_boots", BAYOU, ArmorType.BOOTS, Rarity.COMMON);

    public static final List<Item> SAILOR_SET = List.of(SAILOR_BANDANA, SAILOR_SHIRT, SAILOR_BREECHES, SAILOR_BOOTS);
    public static final List<Item> CAPTAIN_SET = List.of(CAPTAIN_HAT, CAPTAIN_COAT, CAPTAIN_BREECHES, CAPTAIN_BOOTS);
    public static final List<Item> NAVY_SET = List.of(NAVY_CAP, NAVY_COAT, NAVY_TROUSERS, NAVY_BOOTS);
    public static final List<Item> BAYOU_SET = List.of(BAYOU_HAT, BAYOU_OILSKIN, BAYOU_WADERS, BAYOU_BOOTS);

    public static List<Item> all() {
        return List.of(SAILOR_BANDANA, SAILOR_SHIRT, SAILOR_BREECHES, SAILOR_BOOTS,
                CAPTAIN_HAT, CAPTAIN_COAT, CAPTAIN_BREECHES, CAPTAIN_BOOTS,
                NAVY_CAP, NAVY_COAT, NAVY_TROUSERS, NAVY_BOOTS,
                BAYOU_HAT, BAYOU_OILSKIN, BAYOU_WADERS, BAYOU_BOOTS);
    }
}
