package com.cursedtides.ship;

import com.cursedtides.CursedTides;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** A flag to fly from your masthead. Right-click your ship with it; you keep the flag. */
public class FlagItem extends Item {
    public enum Design {
        PENNANT("Plain Pennant", 0, Rarity.COMMON),
        BLACK_SKULL("Skull and Cutlasses", 12, Rarity.COMMON),
        BRETHREN("Brethren Red", 10, Rarity.COMMON),
        TIDEWATCH("Tidewatch Ensign", 10, Rarity.COMMON),
        SMUGGLER("Raven of the Landing", 10, Rarity.COMMON),
        MERCHANT("Merchant's Stripes", 6, Rarity.COMMON),
        CURSED_TIDE("Cursed Tide", 0, Rarity.RARE),
        GOLDEN_SUN("Golden Sun", 0, Rarity.RARE),
        KRAKEN("Kraken's Coil", 0, Rarity.EPIC),
        HORIZON("Last Horizon", 0, Rarity.EPIC);

        public final String displayName;
        /** Doubloons at a shipwright; 0 = not for sale (found, won, or looted). */
        public final int price;
        public final Rarity rarity;

        Design(String displayName, int price, Rarity rarity) {
            this.displayName = displayName;
            this.price = price;
            this.rarity = rarity;
        }

        public static Design byIndex(int i) {
            Design[] all = values();
            return all[Math.max(0, Math.min(all.length - 1, i))];
        }
    }

    private static final Map<Design, Item> ITEMS = new EnumMap<>(Design.class);

    private final Design design;

    public FlagItem(Design design, Settings settings) {
        super(settings);
        this.design = design;
    }

    public Design design() {
        return design;
    }

    public static Item item(Design d) {
        return ITEMS.get(d);
    }

    /** The rare flags, for treasure and bosses. */
    public static Item randomRare(net.minecraft.util.math.random.Random random) {
        Design[] rare = {Design.CURSED_TIDE, Design.GOLDEN_SUN, Design.KRAKEN, Design.HORIZON};
        return ITEMS.get(rare[random.nextInt(rare.length)]);
    }

    public static void register() {
        for (Design d : Design.values()) {
            if (d == Design.PENNANT) continue;
            ITEMS.put(d, Registry.register(Registries.ITEM, CursedTides.id("flag_" + d.name().toLowerCase()),
                    new FlagItem(d, new Item.Settings().maxCount(1).rarity(d.rarity))));
        }
        ITEMS.put(Design.PENNANT, null);
        ItemGroupEvents.modifyEntriesEvent(RegistryKey.of(RegistryKeys.ITEM_GROUP, CursedTides.id("main"))).register(entries -> {
            for (Design d : Design.values()) if (ITEMS.get(d) != null) entries.add(ITEMS.get(d));
        });
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.literal("Flag: " + design.displayName);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("Right-click your ship to fly it. You keep the flag.").formatted(Formatting.GRAY));
        if (design.price == 0) tooltip.add(Text.literal("A rare colour: not sold in any yard").formatted(Formatting.GOLD));
    }
}
