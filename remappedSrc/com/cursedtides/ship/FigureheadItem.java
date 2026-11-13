package com.cursedtides.ship;

import com.cursedtides.CursedTides;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
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

    public FigureheadItem(Properties settings, Kind kind) {
        super(settings);
        this.kind = kind;
    }

    private static Item reg(Kind kind) {
        Item item = Registry.register(BuiltInRegistries.ITEM, CursedTides.id("figurehead_" + kind.name().toLowerCase()),
                new FigureheadItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), kind));
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
        ItemGroupEvents.modifyEntriesEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB, CursedTides.id("main"))).register(e -> ALL.forEach(e::accept));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        tooltip.add(Component.literal(kind.effect).withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.literal("Use on your own ship to mount it at the bow.").withStyle(ChatFormatting.DARK_GRAY));
    }
}
