package com.cursedtides.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Signed articles for one hand. Right-click your ship and they come aboard. */
public class CrewArticlesItem extends Item {
    private static final String[] ROLES = {"Gunner", "Bosun", "Navigator"};
    private static final String[] DUTIES = {
            "Fires your broadsides at enemy ships on their own.",
            "Patches the hull with planks from the cargo hold.",
            "Reads the wind: +10% speed under sail."
    };

    private final int role;

    public CrewArticlesItem(int role, Properties settings) {
        super(settings);
        this.role = role;
    }

    public int role() {
        return role;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display__, java.util.function.Consumer<Component> adder__, TooltipFlag type) {
        java.util.List<Component> tooltip = new java.util.ArrayList<>();
        tooltip.add(Component.literal("Hires a " + ROLES[role] + ". " + DUTIES[role]).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Wages: 2 doubloons a day from the cargo hold.").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("Right-click your ship to sign them on.").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.forEach(adder__);
    }
}
