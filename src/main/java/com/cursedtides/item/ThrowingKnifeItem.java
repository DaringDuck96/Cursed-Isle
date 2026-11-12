package com.cursedtides.item;

import com.cursedtides.entity.ThrowingKnifeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/** Thrown with a flick. Pick them back up where they land. */
public class ThrowingKnifeItem extends Item {
    public ThrowingKnifeItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW,
                SoundCategory.PLAYERS, 0.6f, 1.6f);
        user.getItemCooldownManager().set(this, 8);
        if (!world.isClient) {
            ThrowingKnifeEntity knife = new ThrowingKnifeEntity(world, user);
            knife.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, 2.2f, 0.5f);
            world.spawnEntity(knife);
        }
        if (!user.getAbilities().creativeMode) {
            stack.decrement(1);
        }
        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("Right-click to throw. Trains Blades.").formatted(Formatting.GRAY));
    }
}
