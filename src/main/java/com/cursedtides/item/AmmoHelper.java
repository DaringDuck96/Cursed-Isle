package com.cursedtides.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

final class AmmoHelper {
    private AmmoHelper() {}

    /** Removes one of {@code ammo} from the player's inventory. Creative players never run dry. */
    static boolean consume(Player player, Item ammo) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        var inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(ammo)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }
}
