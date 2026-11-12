package com.cursedtides.item;

import com.cursedtides.world.Archipelago;
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

/**
 * Keeper Wren's chart of the Shattered Reach. Hover for island coordinates;
 * right-click for the bearing and distance from where you stand to each island.
 */
public class SeaChartItem extends Item {
    public SeaChartItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient) {
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.PLAYERS, 1.0f, 1.0f);
            user.sendMessage(Text.literal("~ The Shattered Reach ~").formatted(Formatting.GOLD), false);
            for (Archipelago.Island is : Archipelago.ISLANDS) {
                double dx = is.cx() - user.getX();
                double dz = is.cz() - user.getZ();
                int dist = (int) Math.round(Math.hypot(dx, dz));
                user.sendMessage(Text.literal("  " + is.name() + ": ").formatted(Formatting.YELLOW)
                        .append(Text.literal(dist + " blocks " + bearing(dx, dz)).formatted(Formatting.WHITE)), false);
            }
        }
        return TypedActionResult.success(stack, world.isClient);
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
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("tooltip.cursedtides.sea_chart").formatted(Formatting.GRAY, Formatting.ITALIC));
        for (Archipelago.Island is : Archipelago.ISLANDS) {
            tooltip.add(Text.literal(is.name() + "  " + is.cx() + ", " + is.cz()).formatted(Formatting.DARK_GRAY));
        }
    }
}
