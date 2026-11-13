package com.cursedtides.profession;

import com.cursedtides.CursedTides;
import com.cursedtides.pirate.PirateData;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import java.util.ArrayList;
import java.util.List;

/** A caught fish: has a weight, for bragging and for the Mariner's Rest contest. */
public class CatchItem extends Item {
    public final int minWeight, maxWeight; // tenths of a pound

    CatchItem(int minLb, int maxLb, Rarity rarity, FoodProperties food) {
        super(new Item.Properties().stacksTo(16).rarity(rarity).food(food));
        this.minWeight = minLb * 10;
        this.maxWeight = maxLb * 10;
    }

    public static int weight(ItemStack stack) {
        CustomData c = stack.get(DataComponents.CUSTOM_DATA);
        return c == null ? 0 : c.copyTag().getInt("Weight");
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        int w = weight(stack);
        if (w > 0) tooltip.add(Component.literal(String.format("%.1f lb", w / 10.0)).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("Cook it, or enter it in the Mariner's Rest contest").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }
}
