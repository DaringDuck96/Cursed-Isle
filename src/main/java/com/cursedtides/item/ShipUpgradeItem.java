package com.cursedtides.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/** Fitted to your own ship by right-clicking her with it. The ship does the work (ShipEntity.interact). */
public class ShipUpgradeItem extends Item {
    public enum Kind {
        PLATING("+30% hull strength", 3),
        GUNS("+1 gun on each side", 2),
        CANVAS("+8% speed under sail", 3),
        HOLD("+1 row of cargo space", 2),
        SWIVEL("A swivel gun on the rail: fire grapeshot where you aim (G)", 1);

        private final String effect;
        private final int maxLevel;

        Kind(String effect, int maxLevel) {
            this.effect = effect;
            this.maxLevel = maxLevel;
        }

        public String effect() { return effect; }
        public int maxLevel() { return maxLevel; }
    }

    private final Kind kind;

    public ShipUpgradeItem(Kind kind, Settings settings) {
        super(settings);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal(kind.effect() + " (up to " + kind.maxLevel() + " per ship)").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("Right-click your ship to fit it.").formatted(Formatting.DARK_GRAY));
    }
}
