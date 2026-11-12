package com.cursedtides.quest;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/** The waterlogged log of the Meridian Fleet. Read it at night over Crooked Reef. */
public class FleetLogItem extends Item {
    public FleetLogItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient && user instanceof ServerPlayerEntity p) {
            if (HorizonArc.stage(p) != HorizonArc.GATHER) {
                p.sendMessage(Text.literal("Salt-stained pages. The last entry is about a reef, and a light under the water.")
                        .formatted(Formatting.GRAY), false);
            } else {
                HorizonArc.summonSovereign(p);
            }
            user.getItemCooldownManager().set(this, 100);
        }
        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("Log of the Sovereign Dawn, flagship of the Meridian Fleet").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("Read it at night over Crooked Reef").formatted(Formatting.DARK_AQUA, Formatting.ITALIC));
    }
}
