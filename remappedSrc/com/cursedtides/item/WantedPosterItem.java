package com.cursedtides.item;

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

/** A Tidewatch wanted poster. Read it to see who's worth hunting today. */
public class WantedPosterItem extends Item {
    public WantedPosterItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        if (user instanceof ServerPlayer p && p.getServer() != null) {
            for (Component line : com.cursedtides.living.Bounties.board(p.getServer())) p.displayClientMessage(line, false);
        }
        return InteractionResultHolder.sidedSuccess(user.getItemInHand(hand), world.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        tooltip.add(Component.literal("Use to read the current wanted list.").withStyle(ChatFormatting.GRAY));
    }
}
