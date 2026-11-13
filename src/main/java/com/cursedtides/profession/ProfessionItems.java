package com.cursedtides.profession;

import com.cursedtides.CursedTides;
import com.cursedtides.pirate.PirateData;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
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
        return Registry.register(BuiltInRegistries.ITEM, com.cursedtides.util.Reg.itemId(name), item);
    }

    private static FoodProperties food(int nutrition, float saturation) {
        return new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).build();
    }

    private static FoodProperties meal(int nutrition, float saturation, boolean bowl) {
        FoodProperties.Builder b = new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).alwaysEdible();
        return b.build();
    }

    // ------------------------------------------------------------------ catches

    public static final CatchItem SILVERFIN_SNAPPER = reg(com.cursedtides.util.Reg.itemName("silverfin_snapper"), new CatchItem(2, 9, Rarity.COMMON, food(2, 0.2f)));
    public static final CatchItem REEF_PARROTFISH = reg(com.cursedtides.util.Reg.itemName("reef_parrotfish"), new CatchItem(3, 14, Rarity.COMMON, food(2, 0.2f)));
    public static final CatchItem BAYOU_CATFISH = reg(com.cursedtides.util.Reg.itemName("bayou_catfish"), new CatchItem(4, 30, Rarity.COMMON, food(3, 0.2f)));
    public static final CatchItem LANTERN_EEL = reg(com.cursedtides.util.Reg.itemName("lantern_eel"), new CatchItem(2, 12, Rarity.UNCOMMON, food(2, 0.3f)));
    public static final CatchItem STORM_SWORDFISH = reg(com.cursedtides.util.Reg.itemName("storm_swordfish"), new CatchItem(60, 260, Rarity.UNCOMMON, food(4, 0.4f)));
    public static final CatchItem GHOSTFIN = reg(com.cursedtides.util.Reg.itemName("ghostfin"), new CatchItem(3, 18, Rarity.RARE, food(1, 0.1f)));
    public static final CatchItem GOLDEN_MARLIN = reg(com.cursedtides.util.Reg.itemName("golden_marlin"), new CatchItem(200, 900, Rarity.EPIC, food(6, 0.6f)));

    // ------------------------------------------------------------------ meals (cooking)

    public static final DishItem HARDTACK = reg(com.cursedtides.util.Reg.itemName("hardtack"), new DishItem(Profession.COOKING, meal(5, 1.2f, false), 4,
            new Effect[]{}));
    public static final DishItem FISH_STEW = reg(com.cursedtides.util.Reg.itemName("fish_stew"), new DishItem(Profession.COOKING, meal(8, 0.8f, true), 8,
            new Effect[]{new Effect(MobEffects.REGENERATION, 20 * 20, 0)}));
    public static final DishItem SPICED_SNAPPER = reg(com.cursedtides.util.Reg.itemName("spiced_snapper"), new DishItem(Profession.COOKING, meal(6, 0.9f, false), 10,
            new Effect[]{new Effect(MobEffects.STRENGTH, 20 * 90, 0)}));
    public static final DishItem BAYOU_GUMBO = reg(com.cursedtides.util.Reg.itemName("bayou_gumbo"), new DishItem(Profession.COOKING, meal(9, 0.9f, true), 12,
            new Effect[]{new Effect(MobEffects.FIRE_RESISTANCE, 20 * 120, 0), new Effect(MobEffects.REGENERATION, 20 * 10, 0)}));
    public static final DishItem REEF_CEVICHE = reg(com.cursedtides.util.Reg.itemName("reef_ceviche"), new DishItem(Profession.COOKING, meal(5, 0.7f, false), 10,
            new Effect[]{new Effect(MobEffects.WATER_BREATHING, 20 * 180, 0), new Effect(MobEffects.SPEED, 20 * 60, 0)}));
    public static final DishItem MARLIN_STEAK = reg(com.cursedtides.util.Reg.itemName("marlin_steak"), new DishItem(Profession.COOKING, meal(10, 1.0f, false), 20,
            new Effect[]{new Effect(MobEffects.ABSORPTION, 20 * 120, 1), new Effect(MobEffects.STRENGTH, 20 * 120, 0)}));
    public static final DishItem CAPTAINS_FEAST = reg(com.cursedtides.util.Reg.itemName("captains_feast"), new DishItem(Profession.COOKING, meal(20, 1.2f, false), 40,
            new Effect[]{new Effect(MobEffects.REGENERATION, 20 * 30, 1), new Effect(MobEffects.STRENGTH, 20 * 240, 0),
                    new Effect(MobEffects.RESISTANCE, 20 * 240, 0), new Effect(MobEffects.SPEED, 20 * 240, 0)}));

    // ------------------------------------------------------------------ drinks (brewing)

    public static final DishItem GROG = reg(com.cursedtides.util.Reg.itemName("grog"), new DishItem(Profession.BREWING, meal(2, 0.3f, false), 6,
            new Effect[]{new Effect(MobEffects.RESISTANCE, 20 * 120, 0), new Effect(MobEffects.NAUSEA, 20 * 6, 0)}).drink());
    public static final DishItem SEA_LEGS_TONIC = reg(com.cursedtides.util.Reg.itemName("sea_legs_tonic"), new DishItem(Profession.BREWING, meal(0, 0f, false), 10,
            new Effect[]{new Effect(MobEffects.DOLPHINS_GRACE, 20 * 90, 0), new Effect(MobEffects.WATER_BREATHING, 20 * 180, 0)}).drink());
    public static final DishItem LANTERN_TONIC = reg(com.cursedtides.util.Reg.itemName("lantern_tonic"), new DishItem(Profession.BREWING, meal(0, 0f, false), 10,
            new Effect[]{new Effect(MobEffects.NIGHT_VISION, 20 * 300, 0)}).drink());
    public static final DishItem IRONHIDE_TONIC = reg(com.cursedtides.util.Reg.itemName("ironhide_tonic"), new DishItem(Profession.BREWING, meal(0, 0f, false), 14,
            new Effect[]{new Effect(MobEffects.RESISTANCE, 20 * 60, 1)}).drink());
    public static final DishItem POWDER_MONKEY_TONIC = reg(com.cursedtides.util.Reg.itemName("powder_monkey_tonic"), new DishItem(Profession.BREWING, meal(0, 0f, false), 12,
            new Effect[]{new Effect(MobEffects.HASTE, 20 * 180, 1), new Effect(MobEffects.SPEED, 20 * 120, 0)}).drink());
    public static final DishItem VOODOO_DRAUGHT = reg(com.cursedtides.util.Reg.itemName("voodoo_draught"), new DishItem(Profession.BREWING, meal(0, 0f, false), 16,
            new Effect[]{}).drink().voodoo(50));

    // ------------------------------------------------------------------ smithing kits and the trophy

    public static final Item WHETSTONE = reg(com.cursedtides.util.Reg.itemName("whetstone"), new Honing.KitItem(Honing.Kind.BLADE, com.cursedtides.util.Reg.props().stacksTo(16)));
    public static final Item GUNSMITH_KIT = reg(com.cursedtides.util.Reg.itemName("gunsmith_kit"), new Honing.KitItem(Honing.Kind.FIREARM, com.cursedtides.util.Reg.props().stacksTo(16)));
    public static final Item REINFORCING_KIT = reg(com.cursedtides.util.Reg.itemName("reinforcing_kit"), new Honing.KitItem(Honing.Kind.ARMOR, com.cursedtides.util.Reg.props().stacksTo(16)));
    public static final Item FISHING_TROPHY = reg(com.cursedtides.util.Reg.itemName("fishing_trophy"), new com.cursedtides.item.LoreItem(
            com.cursedtides.util.Reg.props().stacksTo(1).rarity(Rarity.EPIC), "tooltip.cursedtides.fishing_trophy"));

    public static void register() {
        CreativeModeTabEvents.modifyOutputEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB, CursedTides.id("main")))
                .register(entries -> ALL.forEach(entries::accept));
    }

    // ------------------------------------------------------------------ quality

    /** 0 plain, 1 Fine, 2 Masterwork. */
    public static int quality(ItemStack stack) {
        CustomData c = stack.get(DataComponents.CUSTOM_DATA);
        return c == null ? 0 : c.copyTag().getIntOr("Quality", 0);
    }

    public static void setInt(ItemStack stack, String key, int value) {
        CustomData c = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag data = c == null ? new CompoundTag() : c.copyTag();
        data.putInt(key, value);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    }

    public record Effect(Holder<MobEffect> effect, int ticks, int amplifier) {}
}
