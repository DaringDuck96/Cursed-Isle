package com.cursedtides.item;

import com.cursedtides.entity.ThrowingKnifeEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** Thrown with a flick. Pick them back up where they land. */
public class ThrowingKnifeItem extends Item {
    public ThrowingKnifeItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.SNOWBALL_THROW,
                SoundSource.PLAYERS, 0.6f, 1.6f);
        user.getCooldowns().addCooldown(this, 8);
        if (!world.isClientSide) {
            ThrowingKnifeEntity knife = new ThrowingKnifeEntity(world, user);
            knife.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0f, 2.2f, 0.5f);
            world.addFreshEntity(knife);
        }
        if (!user.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        tooltip.add(Component.literal("Right-click to throw. Trains Blades.").withStyle(ChatFormatting.GRAY));
    }
}
