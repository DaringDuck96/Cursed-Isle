package com.cursedtides.item;

import com.cursedtides.world.Archipelago;
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

/**
 * Keeper Wren's chart of the Shattered Reach. Hover for island coordinates;
 * right-click for the bearing and distance from where you stand to each island.
 */
public class SeaChartItem extends Item {
    public SeaChartItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (!world.isClientSide) {
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 1.0f);
            user.displayClientMessage(Component.literal("~ The Shattered Reach ~").withStyle(ChatFormatting.GOLD), false);
            for (Archipelago.Island is : Archipelago.ISLANDS) {
                double dx = is.cx() - user.getX();
                double dz = is.cz() - user.getZ();
                int dist = (int) Math.round(Math.hypot(dx, dz));
                user.displayClientMessage(Component.literal("  " + is.name() + ": ").withStyle(ChatFormatting.YELLOW)
                        .append(Component.literal(dist + " blocks " + bearing(dx, dz)).withStyle(ChatFormatting.WHITE)), false);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
    }

    private static String bearing(double dx, double dz) {
        if (Math.hypot(dx, dz) < 20) {
            return "(you are here)";
        }
        // Minecraft: -Z is north, +X is east
        double angle = Math.toDegrees(Math.atan2(dx, -dz));
        String[] names = {"north", "north-east", "east", "south-east", "south", "south-west", "west", "north-west"};
        int index = (int) Math.round(((angle % 360) + 360) % 360 / 45.0) % 8;
        return names[index];
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        tooltip.add(Component.translatable("tooltip.cursedtides.sea_chart").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        for (Archipelago.Island is : Archipelago.ISLANDS) {
            tooltip.add(Component.literal(is.name() + "  " + is.cx() + ", " + is.cz()).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
