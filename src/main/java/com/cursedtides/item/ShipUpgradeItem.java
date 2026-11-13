package com.cursedtides.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

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

    public ShipUpgradeItem(Kind kind, Properties settings) {
        super(settings);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display__, java.util.function.Consumer<Component> adder__, TooltipFlag type) {
        java.util.List<Component> tooltip = new java.util.ArrayList<>();
        tooltip.add(Component.literal(kind.effect() + " (up to " + kind.maxLevel() + " per ship)").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Right-click your ship to fit it.").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.forEach(adder__);
    }
}
