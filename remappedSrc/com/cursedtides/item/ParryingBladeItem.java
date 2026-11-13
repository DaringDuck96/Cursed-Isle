package com.cursedtides.item;

import com.cursedtides.combat.CombatData;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * A light blade whose right-click is a parry: for a moment, the next melee blow aimed at you
 * is turned aside and the attacker is left reeling. The rapier's window is the longest.
 */
public class ParryingBladeItem extends SwordItem {
    private final int parryTicks;

    public ParryingBladeItem(Tier material, int parryTicks, Properties settings) {
        super(material, settings);
        this.parryTicks = parryTicks;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }
        if (user instanceof ServerPlayer player) {
            CombatData.startParry(player, parryTicks);
        }
        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        tooltip.add(Component.literal("Right-click: parry (" + String.format("%.1f", parryTicks / 20.0) + "s window).")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("A parried attacker staggers, and your next blows land harder.").withStyle(ChatFormatting.DARK_GRAY));
    }
}
