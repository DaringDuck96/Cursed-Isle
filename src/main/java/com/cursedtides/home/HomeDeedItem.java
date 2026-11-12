package com.cursedtides.home;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

import java.util.List;

/** Use on the ground where you want the building. The deed is spent. */
public class HomeDeedItem extends Item {
    private final Homes.Kind kind;

    public HomeDeedItem(Settings settings, Homes.Kind kind) {
        super(settings);
        this.kind = kind;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!(context.getPlayer() instanceof ServerPlayerEntity p)) return ActionResult.SUCCESS;
        if (Homes.place(p, kind, context.getBlockPos())) {
            context.getStack().decrement(1);
            return ActionResult.SUCCESS;
        }
        return ActionResult.FAIL;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        Blueprints.Size s = Blueprints.size(kind);
        tooltip.add(Text.literal(kind.title + ", " + s.w() + " by " + s.d()).formatted(Formatting.GOLD));
        tooltip.add(Text.literal("Use on open ground to build it there and claim the plot.").formatted(Formatting.GRAY));
        if (kind == Homes.Kind.TAVERN) tooltip.add(Text.literal("Earns a share of the port's trade every day.").formatted(Formatting.DARK_GRAY));
        if (kind == Homes.Kind.HQ) tooltip.add(Text.literal("Your whole crew can use it, and /home go takes them there.").formatted(Formatting.DARK_GRAY));
    }
}
