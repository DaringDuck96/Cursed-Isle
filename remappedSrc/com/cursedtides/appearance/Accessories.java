package com.cursedtides.appearance;

import com.cursedtides.CursedTides;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import java.util.ArrayList;
import java.util.List;

import static com.cursedtides.appearance.AccessoryItem.Slot.*;

/** Every accessory in the Reach. */
public final class Accessories {
    private Accessories() {}

    public static final List<Item> ALL = new ArrayList<>();

    private static Item reg(String name, Rarity rarity, AccessoryItem.Slot slot, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr,
                            double amount, String special, int visual, String text) {
        Item item = Registry.register(BuiltInRegistries.ITEM, CursedTides.id(name),
                new AccessoryItem(new Item.Properties().rarity(rarity), slot, attr, amount, special, visual, text));
        ALL.add(item);
        return item;
    }

    public static final Item RED_BANDANA = reg("red_bandana", Rarity.COMMON, HEAD, Attributes.LUCK, 1, null, Look.ACC_BANDANA_RED, "+1 luck");
    public static final Item BLACK_BANDANA = reg("black_bandana", Rarity.COMMON, HEAD, Attributes.LUCK, 1, null, Look.ACC_BANDANA_BLACK, "+1 luck");
    public static final Item EYEPATCH = reg("eyepatch", Rarity.UNCOMMON, EYES, null, 0.05, "crit", Look.ACC_EYEPATCH, "+5% critical hit chance");
    public static final Item GOLD_EARRING = reg("gold_earring", Rarity.UNCOMMON, EAR, Attributes.LUCK, 2, null, Look.ACC_EARRING, "+2 luck");
    public static final Item SHARK_TOOTH_NECKLACE = reg("shark_tooth_necklace", Rarity.UNCOMMON, NECK, Attributes.MAX_HEALTH, 2, null, 0, "+1 heart");
    public static final Item COMPASS_PENDANT = reg("compass_pendant", Rarity.RARE, NECK, null, 0.15, "explore", 0, "+15% Exploration and Sailing xp");
    public static final Item SILVER_RING = reg("silver_ring", Rarity.UNCOMMON, RING, Attributes.ARMOR, 1, null, 0, "+1 armour");
    public static final Item RUBY_RING = reg("ruby_ring", Rarity.RARE, RING, Attributes.ATTACK_DAMAGE, 1, null, 0, "+1 attack damage");
    public static final Item SIGNET_RING = reg("signet_ring", Rarity.RARE, RING, Attributes.MOVEMENT_SPEED, 0.005, null, 0, "+5% speed");
    public static final Item LUCKY_COIN = reg("lucky_coin", Rarity.RARE, TRINKET, Attributes.LUCK, 3, null, 0, "+3 luck");
    public static final Item VOODOO_CHARM = reg("voodoo_charm", Rarity.RARE, TRINKET, null, 1, "voodoo", 0, "Voodoo energy returns faster");
    public static final Item LEATHER_GLOVES = reg("leather_gloves", Rarity.COMMON, GLOVES, Attributes.ATTACK_SPEED, 0.25, null, 0, "+0.25 attack speed");
    public static final Item CAPTAINS_BELT = reg("captains_belt", Rarity.UNCOMMON, BELT, Attributes.KNOCKBACK_RESISTANCE, 0.2, null, 0, "+20% knockback resistance");
    public static final Item STORM_CLOAK = reg("storm_cloak", Rarity.RARE, CLOAK, Attributes.ARMOR_TOUGHNESS, 2, null, 0, "+2 armour toughness");

    /** A random accessory; rare=true favours the better pieces. */
    public static Item random(net.minecraft.util.RandomSource r, boolean rare) {
        List<Item> pool = new ArrayList<>();
        for (Item i : ALL) {
            Rarity q = i.getDefaultInstance().getRarity();
            int weight = q == Rarity.COMMON ? (rare ? 1 : 4) : q == Rarity.UNCOMMON ? (rare ? 2 : 3) : (rare ? 3 : 1);
            for (int k = 0; k < weight; k++) pool.add(i);
        }
        return pool.get(r.nextInt(pool.size()));
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB, CursedTides.id("main")))
                .register(entries -> ALL.forEach(entries::accept));
    }
}
