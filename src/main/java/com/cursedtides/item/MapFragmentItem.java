package com.cursedtides.item;

import net.minecraft.world.InteractionResult;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** One torn corner of a legendary map. Three together make the whole thing. */
public class MapFragmentItem extends Item {
    public static final int NEEDED = 3;

    public MapFragmentItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack held = user.getItemInHand(hand);
        int have = user.getInventory().countItem(this);
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (have < NEEDED && !user.getAbilities().instabuild) {
            com.cursedtides.util.Compat.tell(user, Component.literal("The edges don't meet yet. You have " + have + " of " + NEEDED + " pieces.")
                    .withStyle(ChatFormatting.GRAY), true);
            return InteractionResult.FAIL;
        }
        if (!user.getAbilities().instabuild) {
            int left = NEEDED;
            var inv = user.getInventory();
            for (int i = 0; i < inv.getContainerSize() && left > 0; i++) {
                ItemStack s = inv.getItem(i);
                if (s.is(this)) {
                    int take = Math.min(left, s.getCount());
                    s.shrink(take);
                    left -= take;
                }
            }
        }
        ItemStack map = TreasureMapItem.legendary();
        if (!user.getInventory().add(map)) {
            user.drop(map, false, net.minecraft.util.Prediction.SERVER_ONLY);
        }
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BOOK_PUT, SoundSource.PLAYERS, 1.0f, 0.7f);
        com.cursedtides.util.Compat.tell(user, Component.literal("The pieces fit. A Legendary Treasure Map!").withStyle(ChatFormatting.GOLD), false);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display__, java.util.function.Consumer<Component> adder__, TooltipFlag type) {
        java.util.List<Component> tooltip = new java.util.ArrayList<>();
        tooltip.add(Component.literal("Collect " + NEEDED + " and use them to piece together a Legendary Treasure Map.")
                .withStyle(ChatFormatting.GRAY));
        tooltip.forEach(adder__);
    }
}
