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

import java.util.ArrayList;
import java.util.List;

/** Carved figureheads for the bow. Use one on your own ship to mount it; each brings its own luck. */
public class FigureheadItem extends Item {
    public enum Kind {
        NONE("", ""),
        MERMAID("Mermaid", "+10% speed under sail"),
        LION("Golden Lion", "+15% hull strength"),
        SKULL("Grinning Skull", "+1 gun on each side"),
        KRAKEN("Kraken", "Whirlpools and currents barely touch her"),
        EAGLE("Sea Eagle", "Hostile ships within 150 blocks are spotted (they glow)");

        public final String title, effect;
        Kind(String title, String effect) { this.title = title; this.effect = effect; }

        public static Kind byIndex(int i) {
            Kind[] all = values();
            return all[Math.max(0, Math.min(all.length - 1, i))];
        }
    }

    public static final List<Item> ALL = new ArrayList<>();

    public final Kind kind;

    public FigureheadItem(Settings settings, Kind kind) {
        super(settings);
        this.kind = kind;
    }

    private static Item reg(Kind kind) {
        Item item = Registry.register(Registries.ITEM, CursedTides.id("figurehead_" + kind.name().toLowerCase()),
                new FigureheadItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE), kind));
        ALL.add(item);
        return item;
    }

    public static final Item MERMAID = reg(Kind.MERMAID);
    public static final Item LION = reg(Kind.LION);
    public static final Item SKULL = reg(Kind.SKULL);
    public static final Item KRAKEN = reg(Kind.KRAKEN);
    public static final Item EAGLE = reg(Kind.EAGLE);

    public static Item itemFor(Kind kind) {
        for (Item i : ALL) if (((FigureheadItem) i).kind == kind) return i;
        return null;
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(RegistryKey.of(RegistryKeys.ITEM_GROUP, CursedTides.id("main"))).register(e -> ALL.forEach(e::add));
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal(kind.effect).formatted(Formatting.BLUE));
        tooltip.add(Text.literal("Use on your own ship to mount it at the bow.").formatted(Formatting.DARK_GRAY));
    }
}
