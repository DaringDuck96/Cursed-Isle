package com.cursedtides.profession;

import com.cursedtides.CursedTides;
import com.cursedtides.pirate.PirateData;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/** A caught fish: has a weight, for bragging and for the Mariner's Rest contest. */
public class CatchItem extends Item {
    public final int minWeight, maxWeight; // tenths of a pound

    CatchItem(int minLb, int maxLb, Rarity rarity, FoodComponent food) {
        super(new Item.Settings().maxCount(16).rarity(rarity).food(food));
        this.minWeight = minLb * 10;
        this.maxWeight = maxLb * 10;
    }

    public static int weight(ItemStack stack) {
        NbtComponent c = stack.get(DataComponentTypes.CUSTOM_DATA);
        return c == null ? 0 : c.copyNbt().getInt("Weight");
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        int w = weight(stack);
        if (w > 0) tooltip.add(Text.literal(String.format("%.1f lb", w / 10.0)).formatted(Formatting.AQUA));
        tooltip.add(Text.literal("Cook it, or enter it in the Mariner's Rest contest").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
    }
}
