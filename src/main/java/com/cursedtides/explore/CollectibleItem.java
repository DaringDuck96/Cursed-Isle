package com.cursedtides.explore;

import net.minecraft.world.InteractionResult;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

/** A curio: read it, study it, keep it or sell it to the archivist. */
public class CollectibleItem extends Item {
    public CollectibleItem(Properties settings) {
        super(settings);
    }

    public static Collectibles.Entry entry(ItemStack stack) {
        String id = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getStringOr("Curio", "");
        return Collectibles.byId(id);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        Collectibles.Entry e = entry(stack);
        if (e != null && user instanceof ServerPlayer p) Collectibles.study(p, e);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display__, java.util.function.Consumer<Component> adder__, TooltipFlag type) {
        java.util.List<Component> tooltip = new java.util.ArrayList<>();
        tooltip.add(Component.literal("Use to read or study it.").withStyle(ChatFormatting.GRAY));
        tooltip.forEach(adder__);
    }
}
