package com.cursedtides.trade;

import com.cursedtides.CursedTides;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;

import java.util.List;

/** Crated cargo for buying cheap in one port and selling dear in another. Prices are in silver. */
public enum TradeGood {
    RUM("rum_cask", "Cask of Rum", 40, false),
    SUGAR("sugar_sack", "Sack of Sugar", 25, false),
    SPICES("spice_chest", "Chest of Spices", 70, false),
    SILK("silk_bolt", "Bolt of Silk", 90, false),
    COFFEE("coffee_sack", "Sack of Coffee", 45, false),
    TIMBER("timber_bundle", "Bundle of Timber", 20, false),
    COTTON("cotton_bale", "Bale of Cotton", 30, false),
    SALT_FISH("salt_fish_barrel", "Barrel of Salt Fish", 18, false),
    IRON("iron_crate", "Crate of Ironware", 50, false),
    UNTAXED_RUM("untaxed_rum", "Untaxed Rum", 60, true),
    POWDER("smuggled_powder", "Smuggled Powder Kegs", 80, true),
    CURSED_IDOL("cursed_idol", "Cursed Idol", 150, true),
    STOLEN_PLATE("stolen_plate", "Stolen Silver Plate", 120, true);

    public final String id;
    public final String displayName;
    public final int basePrice;
    public final boolean contraband;
    private Item item;

    TradeGood(String id, String displayName, int basePrice, boolean contraband) {
        this.id = id;
        this.displayName = displayName;
        this.basePrice = basePrice;
        this.contraband = contraband;
    }

    public Item item() {
        return item;
    }

    public static TradeGood of(ItemStack stack) {
        for (TradeGood g : values()) {
            if (g.item != null && stack.isOf(g.item)) return g;
        }
        return null;
    }

    public static boolean isContraband(ItemStack stack) {
        TradeGood g = of(stack);
        return g != null && g.contraband;
    }

    public static void register() {
        for (TradeGood g : values()) {
            g.item = Registry.register(Registries.ITEM, CursedTides.id(g.id), new GoodItem(g,
                    new Item.Settings().maxCount(16).rarity(g.contraband ? Rarity.UNCOMMON : Rarity.COMMON)));
        }
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.registry.RegistryKey.of(
                net.minecraft.registry.RegistryKeys.ITEM_GROUP, CursedTides.id("main"))).register(entries -> {
            for (TradeGood g : values()) entries.add(g.item);
        });
    }

    /** Where this good is cheap and where it sells dear, for the tooltip. */
    public String tradeHint() {
        StringBuilder cheap = new StringBuilder(), dear = new StringBuilder();
        for (Ports.Port p : Ports.ALL) {
            Ports.Kind k = p.kind(this);
            if (k == Ports.Kind.PRODUCES) cheap.append(cheap.isEmpty() ? "" : ", ").append(p.island().name());
            if (k == Ports.Kind.DEMANDS) dear.append(dear.isEmpty() ? "" : ", ").append(p.island().name());
        }
        return (cheap.isEmpty() ? "" : "Cheap in " + cheap + ". ") + (dear.isEmpty() ? "" : "Wanted in " + dear + ".");
    }

    static class GoodItem extends Item {
        private final TradeGood good;

        GoodItem(TradeGood good, Settings settings) {
            super(settings);
            this.good = good;
        }

        /** Distilling rum from sugar is a brewer's trade. */
        @Override
        public void onCraftByPlayer(ItemStack stack, net.minecraft.world.World world, net.minecraft.entity.player.PlayerEntity player) {
            if (good == RUM && !world.isClient && player instanceof net.minecraft.server.network.ServerPlayerEntity sp) {
                com.cursedtides.profession.ProfessionData.addXp(sp, com.cursedtides.profession.Profession.BREWING, 10);
            }
        }

        @Override
        public Text getName(ItemStack stack) {
            return Text.literal(good.displayName);
        }

        @Override
        public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
            tooltip.add(Text.literal(good.contraband ? "Contraband: the Tidewatch will seize it" : "Trade good")
                    .formatted(good.contraband ? Formatting.RED : Formatting.GOLD));
            tooltip.add(Text.literal("Worth about " + Wallet.format(good.basePrice)).formatted(Formatting.GRAY));
            String hint = good.tradeHint();
            if (!hint.isEmpty()) tooltip.add(Text.literal(hint).formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        }
    }
}
