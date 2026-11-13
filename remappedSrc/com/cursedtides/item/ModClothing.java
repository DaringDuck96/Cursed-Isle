package com.cursedtides.item;

import com.cursedtides.CursedTides;
import java.util.EnumMap;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
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

    private static Holder<ArmorMaterial> material(String name, boolean dyeable, int chest) {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.HELMET, 1);
        defense.put(ArmorItem.Type.CHESTPLATE, chest);
        defense.put(ArmorItem.Type.LEGGINGS, 1);
        defense.put(ArmorItem.Type.BOOTS, 1);
        defense.put(ArmorItem.Type.BODY, 2);
        ResourceLocation id = CursedTides.id(name);
        List<ArmorMaterial.Layer> layers = dyeable
                ? List.of(new ArmorMaterial.Layer(id, "", true), new ArmorMaterial.Layer(id, "_overlay", false))
                : List.of(new ArmorMaterial.Layer(id));
        return Registry.registerForHolder(BuiltInRegistries.ARMOR_MATERIAL, id, new ArmorMaterial(defense, 15,
                SoundEvents.ARMOR_EQUIP_LEATHER, () -> Ingredient.of(Items.LEATHER), layers, 0.0f, 0.0f));
    }

    public static final Holder<ArmorMaterial> SAILOR = material("sailor", true, 2);
    public static final Holder<ArmorMaterial> CAPTAIN = material("captain", false, 3);
    public static final Holder<ArmorMaterial> NAVY = material("navy_uniform", false, 3);
    public static final Holder<ArmorMaterial> BAYOU = material("bayou", false, 2);

    private static Item piece(String name, Holder<ArmorMaterial> material, ArmorItem.Type type, Rarity rarity) {
        return Registry.register(BuiltInRegistries.ITEM, CursedTides.id(name),
                new ArmorItem(material, type, new Item.Properties().durability(type.getDurability(10)).rarity(rarity)));
    }

    // Sailor's garb (dyeable)
    public static final Item SAILOR_BANDANA = piece("sailor_bandana", SAILOR, ArmorItem.Type.HELMET, Rarity.COMMON);
    public static final Item SAILOR_SHIRT = piece("sailor_shirt", SAILOR, ArmorItem.Type.CHESTPLATE, Rarity.COMMON);
    public static final Item SAILOR_BREECHES = piece("sailor_breeches", SAILOR, ArmorItem.Type.LEGGINGS, Rarity.COMMON);
    public static final Item SAILOR_BOOTS = piece("sailor_boots", SAILOR, ArmorItem.Type.BOOTS, Rarity.COMMON);
    // Captain's attire
    public static final Item CAPTAIN_HAT = piece("captain_hat", CAPTAIN, ArmorItem.Type.HELMET, Rarity.UNCOMMON);
    public static final Item CAPTAIN_COAT = piece("captain_coat", CAPTAIN, ArmorItem.Type.CHESTPLATE, Rarity.UNCOMMON);
    public static final Item CAPTAIN_BREECHES = piece("captain_breeches", CAPTAIN, ArmorItem.Type.LEGGINGS, Rarity.UNCOMMON);
    public static final Item CAPTAIN_BOOTS = piece("captain_boots", CAPTAIN, ArmorItem.Type.BOOTS, Rarity.UNCOMMON);
    // Tidewatch uniform
    public static final Item NAVY_CAP = piece("navy_cap", NAVY, ArmorItem.Type.HELMET, Rarity.UNCOMMON);
    public static final Item NAVY_COAT = piece("navy_coat", NAVY, ArmorItem.Type.CHESTPLATE, Rarity.UNCOMMON);
    public static final Item NAVY_TROUSERS = piece("navy_trousers", NAVY, ArmorItem.Type.LEGGINGS, Rarity.UNCOMMON);
    public static final Item NAVY_BOOTS = piece("navy_boots", NAVY, ArmorItem.Type.BOOTS, Rarity.UNCOMMON);
    // Bayou oilskins
    public static final Item BAYOU_HAT = piece("bayou_hat", BAYOU, ArmorItem.Type.HELMET, Rarity.COMMON);
    public static final Item BAYOU_OILSKIN = piece("bayou_oilskin", BAYOU, ArmorItem.Type.CHESTPLATE, Rarity.COMMON);
    public static final Item BAYOU_WADERS = piece("bayou_waders", BAYOU, ArmorItem.Type.LEGGINGS, Rarity.COMMON);
    public static final Item BAYOU_BOOTS = piece("bayou_boots", BAYOU, ArmorItem.Type.BOOTS, Rarity.COMMON);

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
