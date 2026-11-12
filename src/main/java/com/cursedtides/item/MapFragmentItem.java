package com.cursedtides.item;

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

/** One torn corner of a legendary map. Three together make the whole thing. */
public class MapFragmentItem extends Item {
    public static final int NEEDED = 3;

    public MapFragmentItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack held = user.getStackInHand(hand);
        int have = user.getInventory().count(this);
        if (world.isClient) {
            return TypedActionResult.success(held, true);
        }
        if (have < NEEDED && !user.getAbilities().creativeMode) {
            user.sendMessage(Text.literal("The edges don't meet yet. You have " + have + " of " + NEEDED + " pieces.")
                    .formatted(Formatting.GRAY), true);
            return TypedActionResult.fail(held);
        }
        if (!user.getAbilities().creativeMode) {
            int left = NEEDED;
            var inv = user.getInventory();
            for (int i = 0; i < inv.size() && left > 0; i++) {
                ItemStack s = inv.getStack(i);
                if (s.isOf(this)) {
                    int take = Math.min(left, s.getCount());
                    s.decrement(take);
                    left -= take;
                }
            }
        }
        ItemStack map = TreasureMapItem.legendary();
        if (!user.getInventory().insertStack(map)) {
            user.dropItem(map, false);
        }
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_BOOK_PUT, SoundCategory.PLAYERS, 1.0f, 0.7f);
        user.sendMessage(Text.literal("The pieces fit. A Legendary Treasure Map!").formatted(Formatting.GOLD), false);
        return TypedActionResult.success(held, false);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("Collect " + NEEDED + " and use them to piece together a Legendary Treasure Map.")
                .formatted(Formatting.GRAY));
    }
}
