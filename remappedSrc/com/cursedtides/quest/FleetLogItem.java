package com.cursedtides.quest;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** The waterlogged log of the Meridian Fleet. Read it at night over Crooked Reef. */
public class FleetLogItem extends Item {
    public FleetLogItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (!world.isClientSide && user instanceof ServerPlayer p) {
            if (HorizonArc.stage(p) != HorizonArc.GATHER) {
                p.displayClientMessage(Component.literal("Salt-stained pages. The last entry is about a reef, and a light under the water.")
                        .withStyle(ChatFormatting.GRAY), false);
            } else {
                HorizonArc.summonSovereign(p);
            }
            user.getCooldowns().addCooldown(this, 100);
        }
        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        tooltip.add(Component.literal("Log of the Sovereign Dawn, flagship of the Meridian Fleet").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Read it at night over Crooked Reef").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC));
    }
}
