package com.cursedtides.profession;

import com.cursedtides.CursedTides;
import com.cursedtides.pirate.PirateData;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/** Fish you can catch, meals you can cook, tonics you can brew, and the kits that improve gear. */
public final class ProfessionItems {
    private ProfessionItems() {}

    private static final List<Item> ALL = new ArrayList<>();

    private static <T extends Item> T reg(String name, T item) {
        ALL.add(item);
        return Registry.register(Registries.ITEM, CursedTides.id(name), item);
    }

    private static FoodComponent food(int nutrition, float saturation) {
        return new FoodComponent.Builder().nutrition(nutrition).saturationModifier(saturation).build();
    }

    private static FoodComponent meal(int nutrition, float saturation, boolean bowl) {
        FoodComponent.Builder b = new FoodComponent.Builder().nutrition(nutrition).saturationModifier(saturation).alwaysEdible();
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
            new Effect[]{new Effect(StatusEffects.REGENERATION, 20 * 20, 0)}));
    public static final DishItem SPICED_SNAPPER = reg("spiced_snapper", new DishItem(Profession.COOKING, meal(6, 0.9f, false), 10,
            new Effect[]{new Effect(StatusEffects.STRENGTH, 20 * 90, 0)}));
    public static final DishItem BAYOU_GUMBO = reg("bayou_gumbo", new DishItem(Profession.COOKING, meal(9, 0.9f, true), 12,
            new Effect[]{new Effect(StatusEffects.FIRE_RESISTANCE, 20 * 120, 0), new Effect(StatusEffects.REGENERATION, 20 * 10, 0)}));
    public static final DishItem REEF_CEVICHE = reg("reef_ceviche", new DishItem(Profession.COOKING, meal(5, 0.7f, false), 10,
            new Effect[]{new Effect(StatusEffects.WATER_BREATHING, 20 * 180, 0), new Effect(StatusEffects.SPEED, 20 * 60, 0)}));
    public static final DishItem MARLIN_STEAK = reg("marlin_steak", new DishItem(Profession.COOKING, meal(10, 1.0f, false), 20,
            new Effect[]{new Effect(StatusEffects.ABSORPTION, 20 * 120, 1), new Effect(StatusEffects.STRENGTH, 20 * 120, 0)}));
    public static final DishItem CAPTAINS_FEAST = reg("captains_feast", new DishItem(Profession.COOKING, meal(20, 1.2f, false), 40,
            new Effect[]{new Effect(StatusEffects.REGENERATION, 20 * 30, 1), new Effect(StatusEffects.STRENGTH, 20 * 240, 0),
                    new Effect(StatusEffects.RESISTANCE, 20 * 240, 0), new Effect(StatusEffects.SPEED, 20 * 240, 0)}));

    // ------------------------------------------------------------------ drinks (brewing)

    public static final DishItem GROG = reg("grog", new DishItem(Profession.BREWING, meal(2, 0.3f, false), 6,
            new Effect[]{new Effect(StatusEffects.RESISTANCE, 20 * 120, 0), new Effect(StatusEffects.NAUSEA, 20 * 6, 0)}).drink());
    public static final DishItem SEA_LEGS_TONIC = reg("sea_legs_tonic", new DishItem(Profession.BREWING, meal(0, 0f, false), 10,
            new Effect[]{new Effect(StatusEffects.DOLPHINS_GRACE, 20 * 90, 0), new Effect(StatusEffects.WATER_BREATHING, 20 * 180, 0)}).drink());
    public static final DishItem LANTERN_TONIC = reg("lantern_tonic", new DishItem(Profession.BREWING, meal(0, 0f, false), 10,
            new Effect[]{new Effect(StatusEffects.NIGHT_VISION, 20 * 300, 0)}).drink());
    public static final DishItem IRONHIDE_TONIC = reg("ironhide_tonic", new DishItem(Profession.BREWING, meal(0, 0f, false), 14,
            new Effect[]{new Effect(StatusEffects.RESISTANCE, 20 * 60, 1)}).drink());
    public static final DishItem POWDER_MONKEY_TONIC = reg("powder_monkey_tonic", new DishItem(Profession.BREWING, meal(0, 0f, false), 12,
            new Effect[]{new Effect(StatusEffects.HASTE, 20 * 180, 1), new Effect(StatusEffects.SPEED, 20 * 120, 0)}).drink());
    public static final DishItem VOODOO_DRAUGHT = reg("voodoo_draught", new DishItem(Profession.BREWING, meal(0, 0f, false), 16,
            new Effect[]{}).drink().voodoo(50));

    // ------------------------------------------------------------------ smithing kits and the trophy

    public static final Item WHETSTONE = reg("whetstone", new Honing.KitItem(Honing.Kind.BLADE, new Item.Settings().maxCount(16)));
    public static final Item GUNSMITH_KIT = reg("gunsmith_kit", new Honing.KitItem(Honing.Kind.FIREARM, new Item.Settings().maxCount(16)));
    public static final Item REINFORCING_KIT = reg("reinforcing_kit", new Honing.KitItem(Honing.Kind.ARMOR, new Item.Settings().maxCount(16)));
    public static final Item FISHING_TROPHY = reg("fishing_trophy", new com.cursedtides.item.LoreItem(
            new Item.Settings().maxCount(1).rarity(Rarity.EPIC), "tooltip.cursedtides.fishing_trophy"));

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(RegistryKey.of(RegistryKeys.ITEM_GROUP, CursedTides.id("main")))
                .register(entries -> ALL.forEach(entries::add));
    }

    // ------------------------------------------------------------------ quality

    /** 0 plain, 1 Fine, 2 Masterwork. */
    public static int quality(ItemStack stack) {
        NbtComponent c = stack.get(DataComponentTypes.CUSTOM_DATA);
        return c == null ? 0 : c.copyNbt().getInt("Quality");
    }

    static void setInt(ItemStack stack, String key, int value) {
        NbtComponent c = stack.get(DataComponentTypes.CUSTOM_DATA);
        NbtCompound data = c == null ? new NbtCompound() : c.copyNbt();
        data.putInt(key, value);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(data));
    }

    public record Effect(RegistryEntry<StatusEffect> effect, int ticks, int amplifier) {}
}
