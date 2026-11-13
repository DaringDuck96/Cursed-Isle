package com.cursedtides.trade;

import com.cursedtides.item.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Coin in the pocket: silver pieces, ten to the doubloon. Pays exactly and gives change. */
public final class Wallet {
    private Wallet() {}

    public static final int SILVER_PER_DOUBLOON = 10;

    public static int total(Player player) {
        var inv = player.getInventory();
        return inv.countItem(ModItems.SILVER_PIECE) + inv.countItem(ModItems.DOUBLOON) * SILVER_PER_DOUBLOON;
    }

    /** Takes {@code silver} worth of coin, silver first, breaking doubloons as needed. */
    public static boolean pay(Player player, int silver) {
        if (silver <= 0) return true;
        if (total(player) < silver) return false;
        int fromSilver = Math.min(silver, player.getInventory().countItem(ModItems.SILVER_PIECE));
        remove(player, ModItems.SILVER_PIECE, fromSilver);
        int rest = silver - fromSilver;
        if (rest > 0) {
            int doubloons = (rest + SILVER_PER_DOUBLOON - 1) / SILVER_PER_DOUBLOON;
            remove(player, ModItems.DOUBLOON, doubloons);
            give(player, doubloons * SILVER_PER_DOUBLOON - rest, false);
        }
        return true;
    }

    /** Pays out silver, as doubloons where it can (unless asked for silver only). */
    public static void give(Player player, int silver, boolean preferDoubloons) {
        if (silver <= 0) return;
        int doubloons = preferDoubloons ? silver / SILVER_PER_DOUBLOON : 0;
        int pieces = silver - doubloons * SILVER_PER_DOUBLOON;
        insert(player, ModItems.DOUBLOON, doubloons);
        insert(player, ModItems.SILVER_PIECE, pieces);
    }

    public static void give(Player player, int silver) {
        give(player, silver, true);
    }

    private static void insert(Player player, Item item, int count) {
        while (count > 0) {
            int n = Math.min(count, item.getDefaultMaxStackSize());
            ItemStack stack = new ItemStack(item, n);
            if (!player.getInventory().add(stack)) player.drop(stack, false);
            count -= n;
        }
    }

    static void remove(Player player, Item item, int count) {
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize() && count > 0; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(item)) {
                int take = Math.min(count, stack.getCount());
                stack.shrink(take);
                count -= take;
            }
        }
    }

    /** "3 doubloons 4 silver" */
    public static String format(int silver) {
        int d = silver / SILVER_PER_DOUBLOON, s = silver % SILVER_PER_DOUBLOON;
        if (d == 0) return s + " silver";
        return d + (d == 1 ? " doubloon" : " doubloons") + (s == 0 ? "" : " " + s + " silver");
    }
}
