package com.cursedtides.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

final class AmmoHelper {
    private AmmoHelper() {}

    /** Removes one of {@code ammo} from the player's inventory. Creative players never run dry. */
    static boolean consume(PlayerEntity player, Item ammo) {
        if (player.getAbilities().creativeMode) {
            return true;
        }
        var inventory = player.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isOf(ammo)) {
                stack.decrement(1);
                return true;
            }
        }
        return false;
    }
}
