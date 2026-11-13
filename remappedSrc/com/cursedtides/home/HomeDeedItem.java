package com.cursedtides.home;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

/** Use on the ground where you want the building. The deed is spent. */
public class HomeDeedItem extends Item {
    private final Homes.Kind kind;

    public HomeDeedItem(Properties settings, Homes.Kind kind) {
        super(settings);
        this.kind = kind;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getPlayer() instanceof ServerPlayer p)) return InteractionResult.SUCCESS;
        if (Homes.place(p, kind, context.getClickedPos())) {
            context.getItemInHand().shrink(1);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        Blueprints.Size s = Blueprints.size(kind);
        tooltip.add(Component.literal(kind.title + ", " + s.w() + " by " + s.d()).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Use on open ground to build it there and claim the plot.").withStyle(ChatFormatting.GRAY));
        if (kind == Homes.Kind.TAVERN) tooltip.add(Component.literal("Earns a share of the port's trade every day.").withStyle(ChatFormatting.DARK_GRAY));
        if (kind == Homes.Kind.HQ) tooltip.add(Component.literal("Your whole crew can use it, and /home go takes them there.").withStyle(ChatFormatting.DARK_GRAY));
    }
}
