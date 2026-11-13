package com.cursedtides.profession;

import com.cursedtides.CursedTides;
import com.cursedtides.pirate.PirateData;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.CustomData;
import java.util.ArrayList;
import java.util.List;

/** Fish you can catch, meals you can cook, tonics you can brew, and the kits that improve gear. */
public final class ProfessionItems {
    private ProfessionItems() {}

    private static final List<Item> ALL = new ArrayList<>();

    private static <T extends Item> T reg(String name, T item) {
        ALL.add(item);
        return Registry.register(BuiltInRegistries.ITEM, CursedTides.id(name), item);
    }

    private static FoodProperties food(int nutrition, float saturation) {
        return new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).build();
    }

    private static FoodProperties meal(int nutrition, float saturation, boolean bowl) {
        FoodProperties.Builder b = new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).alwaysEdible();
        if (bowl) b.usingConvertsTo(Items.BOWL);
        return b.build();
    }

    // ------------------------------------------------------------------ catches

    public static final CatchItem SILVERFIN_SNAPPER = reg("silverfin_snapper", new CatchItem(2, 9, Rarity.COMMON, food(2, 0.2f)));
    public static final CatchItem REEF_PARROTFISH = reg("reef_parrotfish", new CatchItem(3, 14, Rarity.COMMON, food(2, 0.2f)));
    public static final CatchItem BAYOU_CATFISH = reg("bayou_catfish", new CatchItem(4, 30, Rarity.COMMON, food(3, 0.2f)));
    public static final CatchItem LANTERN_EEL = reg("lantern_eel", new CatchItem(2, 12, Rarity.UNCOMMON, food(2, 0.3f)));
    public static final CatchItem STORM_SWORDFISH = reg("storm_swordfish", new CatchItem(60, 260, Rarity.UNCOMMON, food(4, 0.4f)));
    public static final CatchItem GHOSTFIN = reg("ghostfin", new CatchItem(3, 18, Rarity.RARE, food(1, 0.1f)));
    public static final CatchItem GOLDEN_MARLIN = reg("golden_marlin", new CatchItem(200, 900, Rarity.EPIC, food(6, 0.6f)));

    // ------------------------------------------------------------------ meals (cooking)

    public static final DishItem HARDTACK = reg("hardtack", new DishItem(Profession.COOKING, meal(5, 1.2f, false), 4,
            new Effect[]{}));
    public static final DishItem FISH_STEW = reg("fish_stew", new DishItem(Profession.COOKING, meal(8, 0.8f, true), 8,
            new Effect[]{new Effect(MobEffects.REGENERATION, 20 * 20, 0)}));
    public static final DishItem SPICED_SNAPPER = reg("spiced_snapper", new DishItem(Profession.COOKING, meal(6, 0.9f, false), 10,
            new Effect[]{new Effect(MobEffects.DAMAGE_BOOST, 20 * 90, 0)}));
    public static final DishItem BAYOU_GUMBO = reg("bayou_gumbo", new DishItem(Profession.COOKING, meal(9, 0.9f, true), 12,
            new Effect[]{new Effect(MobEffects.FIRE_RESISTANCE, 20 * 120, 0), new Effect(MobEffects.REGENERATION, 20 * 10, 0)}));
    public static final DishItem REEF_CEVICHE = reg("reef_ceviche", new DishItem(Profession.COOKING, meal(5, 0.7f, false), 10,
            new Effect[]{new Effect(MobEffects.WATER_BREATHING, 20 * 180, 0), new Effect(MobEffects.MOVEMENT_SPEED, 20 * 60, 0)}));
    public static final DishItem MARLIN_STEAK = reg("marlin_steak", new DishItem(Profession.COOKING, meal(10, 1.0f, false), 20,
            new Effect[]{new Effect(MobEffects.ABSORPTION, 20 * 120, 1), new Effect(MobEffects.DAMAGE_BOOST, 20 * 120, 0)}));
    public static final DishItem CAPTAINS_FEAST = reg("captains_feast", new DishItem(Profession.COOKING, meal(20, 1.2f, false), 40,
            new Effect[]{new Effect(MobEffects.REGENERATION, 20 * 30, 1), new Effect(MobEffects.DAMAGE_BOOST, 20 * 240, 0),
                    new Effect(MobEffects.DAMAGE_RESISTANCE, 20 * 240, 0), new Effect(MobEffects.MOVEMENT_SPEED, 20 * 240, 0)}));

    // ------------------------------------------------------------------ drinks (brewing)

    public static final DishItem GROG = reg("grog", new DishItem(Profession.BREWING, meal(2, 0.3f, false), 6,
            new Effect[]{new Effect(MobEffects.DAMAGE_RESISTANCE, 20 * 120, 0), new Effect(MobEffects.CONFUSION, 20 * 6, 0)}).drink());
    public static final DishItem SEA_LEGS_TONIC = reg("sea_legs_tonic", new DishItem(Profession.BREWING, meal(0, 0f, false), 10,
            new Effect[]{new Effect(MobEffects.DOLPHINS_GRACE, 20 * 90, 0), new Effect(MobEffects.WATER_BREATHING, 20 * 180, 0)}).drink());
    public static final DishItem LANTERN_TONIC = reg("lantern_tonic", new DishItem(Profession.BREWING, meal(0, 0f, false), 10,
            new Effect[]{new Effect(MobEffects.NIGHT_VISION, 20 * 300, 0)}).drink());
    public static final DishItem IRONHIDE_TONIC = reg("ironhide_tonic", new DishItem(Profession.BREWING, meal(0, 0f, false), 14,
            new Effect[]{new Effect(MobEffects.DAMAGE_RESISTANCE, 20 * 60, 1)}).drink());
    public static final DishItem POWDER_MONKEY_TONIC = reg("powder_monkey_tonic", new DishItem(Profession.BREWING, meal(0, 0f, false), 12,
            new Effect[]{new Effect(MobEffects.DIG_SPEED, 20 * 180, 1), new Effect(MobEffects.MOVEMENT_SPEED, 20 * 120, 0)}).drink());
    public static final DishItem VOODOO_DRAUGHT = reg("voodoo_draught", new DishItem(Profession.BREWING, meal(0, 0f, false), 16,
            new Effect[]{}).drink().voodoo(50));

    // ------------------------------------------------------------------ smithing kits and the trophy

    public static final Item WHETSTONE = reg("whetstone", new Honing.KitItem(Honing.Kind.BLADE, new Item.Properties().stacksTo(16)));
    public static final Item GUNSMITH_KIT = reg("gunsmith_kit", new Honing.KitItem(Honing.Kind.FIREARM, new Item.Properties().stacksTo(16)));
    public static final Item REINFORCING_KIT = reg("reinforcing_kit", new Honing.KitItem(Honing.Kind.ARMOR, new Item.Properties().stacksTo(16)));
    public static final Item FISHING_TROPHY = reg("fishing_trophy", new com.cursedtides.item.LoreItem(
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), "tooltip.cursedtides.fishing_trophy"));

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB, CursedTides.id("main")))
                .register(entries -> ALL.forEach(entries::accept));
    }

    // ------------------------------------------------------------------ quality

    /** 0 plain, 1 Fine, 2 Masterwork. */
    public static int quality(ItemStack stack) {
        CustomData c = stack.get(DataComponents.CUSTOM_DATA);
        return c == null ? 0 : c.copyTag().getInt("Quality");
    }

    public static void setInt(ItemStack stack, String key, int value) {
        CustomData c = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag data = c == null ? new CompoundTag() : c.copyTag();
        data.putInt(key, value);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    }

    public record Effect(Holder<MobEffect> effect, int ticks, int amplifier) {}
}
