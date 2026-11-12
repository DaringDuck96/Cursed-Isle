package com.cursedtides.trade;

import com.cursedtides.item.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** Coin in the pocket: silver pieces, ten to the doubloon. Pays exactly and gives change. */
public final class Wallet {
    private Wallet() {}

    public static final int SILVER_PER_DOUBLOON = 10;

    public static int total(PlayerEntity player) {
        var inv = player.getInventory();
        return inv.count(ModItems.SILVER_PIECE) + inv.count(ModItems.DOUBLOON) * SILVER_PER_DOUBLOON;
    }

    /** Takes {@code silver} worth of coin, silver first, breaking doubloons as needed. */
    public static boolean pay(PlayerEntity player, int silver) {
        if (silver <= 0) return true;
        if (total(player) < silver) return false;
        int fromSilver = Math.min(silver, player.getInventory().count(ModItems.SILVER_PIECE));
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
    public static void give(PlayerEntity player, int silver, boolean preferDoubloons) {
        if (silver <= 0) return;
        int doubloons = preferDoubloons ? silver / SILVER_PER_DOUBLOON : 0;
        int pieces = silver - doubloons * SILVER_PER_DOUBLOON;
        insert(player, ModItems.DOUBLOON, doubloons);
        insert(player, ModItems.SILVER_PIECE, pieces);
    }

    public static void give(PlayerEntity player, int silver) {
        give(player, silver, true);
    }

    private static void insert(PlayerEntity player, Item item, int count) {
        while (count > 0) {
            int n = Math.min(count, item.getMaxCount());
            ItemStack stack = new ItemStack(item, n);
            if (!player.getInventory().insertStack(stack)) player.dropItem(stack, false);
            count -= n;
        }
    }

    static void remove(PlayerEntity player, Item item, int count) {
        var inv = player.getInventory();
        for (int i = 0; i < inv.size() && count > 0; i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isOf(item)) {
                int take = Math.min(count, stack.getCount());
                stack.decrement(take);
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
