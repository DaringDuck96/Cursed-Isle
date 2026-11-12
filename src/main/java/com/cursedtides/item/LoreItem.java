package com.cursedtides.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/** A plain item with lore lines in its tooltip (quest items, relics). */
public class LoreItem extends Item {
    private final String[] loreKeys;

    public LoreItem(Settings settings, String... loreKeys) {
        super(settings);
        this.loreKeys = loreKeys;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        for (String key : loreKeys) {
            tooltip.add(Text.translatable(key).formatted(Formatting.GRAY, Formatting.ITALIC));
        }
    }
}
