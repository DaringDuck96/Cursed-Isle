package com.cursedtides.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** A plain item with lore lines in its tooltip (quest items, relics). */
public class LoreItem extends Item {
    private final String[] loreKeys;

    public LoreItem(Properties settings, String... loreKeys) {
        super(settings);
        this.loreKeys = loreKeys;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display__, java.util.function.Consumer<Component> adder__, TooltipFlag type) {
        java.util.List<Component> tooltip = new java.util.ArrayList<>();
        for (String key : loreKeys) {
            tooltip.add(Component.translatable(key).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
        tooltip.forEach(adder__);
    }
}
