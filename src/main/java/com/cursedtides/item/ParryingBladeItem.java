package com.cursedtides.item;

import com.cursedtides.combat.CombatData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/**
 * A light blade whose right-click is a parry: for a moment, the next melee blow aimed at you
 * is turned aside and the attacker is left reeling. The rapier's window is the longest.
 */
public class ParryingBladeItem extends SwordItem {
    private final int parryTicks;

    public ParryingBladeItem(ToolMaterial material, int parryTicks, Settings settings) {
        super(material, settings);
        this.parryTicks = parryTicks;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (hand != Hand.MAIN_HAND) {
            return TypedActionResult.pass(stack);
        }
        if (user instanceof ServerPlayerEntity player) {
            CombatData.startParry(player, parryTicks);
        }
        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("Right-click: parry (" + String.format("%.1f", parryTicks / 20.0) + "s window).")
                .formatted(Formatting.GRAY));
        tooltip.add(Text.literal("A parried attacker staggers, and your next blows land harder.").formatted(Formatting.DARK_GRAY));
    }
}
