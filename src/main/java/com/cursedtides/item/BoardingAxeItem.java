package com.cursedtides.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Slow, heavy and brutal. Every hit staggers, and like any axe it knocks shields aside. */
public class BoardingAxeItem extends Item {
    public BoardingAxeItem(ToolMaterial material, Properties settings) {
        super(settings);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 2), attacker);
        super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display__, java.util.function.Consumer<Component> adder__, TooltipFlag type) {
        java.util.List<Component> tooltip = new java.util.ArrayList<>();
        tooltip.add(Component.literal("Heavy blows stagger whatever they hit.").withStyle(ChatFormatting.GRAY));
        tooltip.forEach(adder__);
    }
}
