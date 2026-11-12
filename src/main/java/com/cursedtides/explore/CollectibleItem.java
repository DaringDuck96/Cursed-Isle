package com.cursedtides.explore;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
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

/** A curio: read it, study it, keep it or sell it to the archivist. */
public class CollectibleItem extends Item {
    public CollectibleItem(Settings settings) {
        super(settings);
    }

    public static Collectibles.Entry entry(ItemStack stack) {
        String id = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getString("Curio");
        return Collectibles.byId(id);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        Collectibles.Entry e = entry(stack);
        if (e != null && user instanceof ServerPlayerEntity p) Collectibles.study(p, e);
        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("Use to read or study it.").formatted(Formatting.GRAY));
    }
}
