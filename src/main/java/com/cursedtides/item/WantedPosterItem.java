package com.cursedtides.item;

import net.minecraft.world.InteractionResult;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
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
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (user instanceof ServerPlayer p && p.level().getServer() != null) {
            for (Component line : com.cursedtides.living.Bounties.board(p.level().getServer())) p.sendSystemMessage(line, false);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display__, java.util.function.Consumer<Component> adder__, TooltipFlag type) {
        java.util.List<Component> tooltip = new java.util.ArrayList<>();
        tooltip.add(Component.literal("Use to read the current wanted list.").withStyle(ChatFormatting.GRAY));
        tooltip.forEach(adder__);
    }
}
