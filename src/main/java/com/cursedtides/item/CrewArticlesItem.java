package com.cursedtides.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/** Signed articles for one hand. Right-click your ship and they come aboard. */
public class CrewArticlesItem extends Item {
    private static final String[] ROLES = {"Gunner", "Bosun", "Navigator"};
    private static final String[] DUTIES = {
            "Fires your broadsides at enemy ships on their own.",
            "Patches the hull with planks from the cargo hold.",
            "Reads the wind: +10% speed under sail."
    };

    private final int role;

    public CrewArticlesItem(int role, Settings settings) {
        super(settings);
        this.role = role;
    }

    public int role() {
        return role;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("Hires a " + ROLES[role] + ". " + DUTIES[role]).formatted(Formatting.GRAY));
        tooltip.add(Text.literal("Wages: 2 doubloons a day from the cargo hold.").formatted(Formatting.DARK_GRAY));
        tooltip.add(Text.literal("Right-click your ship to sign them on.").formatted(Formatting.DARK_GRAY));
    }
}
