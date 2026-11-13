package com.cursedtides.ship;

import com.cursedtides.CursedTides;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
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

    public FlagItem(Design design, Properties settings) {
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
    public static Item randomRare(net.minecraft.util.RandomSource random) {
        Design[] rare = {Design.CURSED_TIDE, Design.GOLDEN_SUN, Design.KRAKEN, Design.HORIZON};
        return ITEMS.get(rare[random.nextInt(rare.length)]);
    }

    public static void register() {
        for (Design d : Design.values()) {
            if (d == Design.PENNANT) continue;
            ITEMS.put(d, Registry.register(BuiltInRegistries.ITEM, com.cursedtides.util.Reg.itemId("flag_" + d.name().toLowerCase()),
                    new FlagItem(d, com.cursedtides.util.Reg.props().stacksTo(1).rarity(d.rarity))));
        }
        ITEMS.put(Design.PENNANT, null);
        CreativeModeTabEvents.modifyOutputEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB, CursedTides.id("main"))).register(entries -> {
            for (Design d : Design.values()) if (ITEMS.get(d) != null) entries.accept(ITEMS.get(d));
        });
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal("Flag: " + design.displayName);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display__, java.util.function.Consumer<Component> adder__, TooltipFlag type) {
        java.util.List<Component> tooltip = new java.util.ArrayList<>();
        tooltip.add(Component.literal("Right-click your ship to fly it. You keep the flag.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Sneak + right-click to paint it on your mainsail.").withStyle(ChatFormatting.DARK_GRAY));
        if (design.price == 0) tooltip.add(Component.literal("A rare colour: not sold in any yard").withStyle(ChatFormatting.GOLD));
        tooltip.forEach(adder__);
    }
}
