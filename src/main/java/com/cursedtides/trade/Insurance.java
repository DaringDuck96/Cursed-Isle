package com.cursedtides.trade;

import com.cursedtides.item.ShipDeedItem;
import com.cursedtides.ship.ShipClass;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Ship insurance, written by the Bank of Kingsport. An insured ship that sinks is replaced with a
 * fresh deed (her name, sails and fittings kept; her cargo lost). The policy is used up when it pays.
 */
public final class Insurance {
    private Insurance() {}

    public static final String KEY = "Insured";

    /** What a ship of this class is worth, in doubloons. */
    public static int value(ShipClass cls) {
        return switch (cls) {
            case SLOOP -> 48;
            case BRIGANTINE -> 112;
            case GALLEON -> 176;
            case SCHOONER -> 80;
            case BRIG -> 140;
            case FRIGATE -> 220;
            case SHIP_OF_THE_LINE -> 300;
        };
    }

    public static int premium(ShipClass cls) {
        return Math.max(8, value(cls) / 5);
    }

    public static boolean isInsured(ItemStack deed) {
        CustomData c = deed.get(DataComponents.CUSTOM_DATA);
        return c != null && c.copyTag().getBooleanOr(KEY, false);
    }

    /** At the banker, holding a deed: buys a policy. */
    public static void insure(ServerPlayer player, ItemStack deed, ShipDeedItem item) {
        if (isInsured(deed)) {
            player.sendSystemMessage(Component.literal("That ship is already insured.").withStyle(ChatFormatting.GRAY), false);
            return;
        }
        int premium = premium(item.shipClass());
        if (!Wallet.pay(player, premium * Wallet.SILVER_PER_DOUBLOON)) {
            player.sendSystemMessage(Component.literal("A policy on a " + item.shipClass().displayName() + " costs " + premium
                    + " doubloons. Come back with the coin.").withStyle(ChatFormatting.RED), false);
            return;
        }
        CustomData c = deed.get(DataComponents.CUSTOM_DATA);
        CompoundTag data = c == null ? new CompoundTag() : c.copyTag();
        data.putBoolean(KEY, true);
        deed.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        player.sendSystemMessage(Component.literal("Insured for " + premium + " doubloons. If she goes down, the bank will replace her hull"
                + " (though not her cargo).").withStyle(ChatFormatting.GOLD), false);
    }

    /** Called when an insured player ship sinks. */
    public static void payOut(MinecraftServer server, UUID owner, ItemStack replacement) {
        ServerPlayer p = server.getPlayerList().getPlayer(owner);
        if (p != null) {
            if (!p.getInventory().add(replacement)) p.drop(replacement, false, net.minecraft.util.Prediction.SERVER_ONLY);
            p.sendSystemMessage(Component.literal("The Bank of Kingsport honours your policy: a replacement deed for ")
                    .append(replacement.getHoverName()).append(Component.literal(".")).withStyle(ChatFormatting.GOLD), false);
            return;
        }
        Claims claims = Claims.get(server);
        claims.pending.computeIfAbsent(owner, k -> new ArrayList<>()).add(replacement);
        claims.setDirty();
    }

    /** Hands over any replacement deeds waiting for this player. */
    public static boolean collect(ServerPlayer p) {
        Claims claims = Claims.get(p.level().getServer());
        List<ItemStack> deeds = claims.pending.remove(p.getUUID());
        if (deeds == null || deeds.isEmpty()) return false;
        for (ItemStack d : deeds) if (!p.getInventory().add(d)) p.drop(d, false, net.minecraft.util.Prediction.SERVER_ONLY);
        claims.setDirty();
        p.sendSystemMessage(Component.literal("Your insurance claim is settled: " + deeds.size() + " replacement deed(s).").withStyle(ChatFormatting.GOLD), false);
        return true;
    }

    static final class Claims extends SavedData {
        final Map<UUID, List<ItemStack>> pending = new HashMap<>();
        private static final net.minecraft.world.level.saveddata.SavedDataType<Claims> TYPE = com.cursedtides.util.Saved.type("cursedtides_insurance", Claims::new, Claims::fromNbt, (st__, l__) -> st__.save(new CompoundTag(), l__));

        static Claims get(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(TYPE);
        }

                public CompoundTag save(CompoundTag nbt, HolderLookup.Provider lookup) {
            ListTag list = new ListTag();
            pending.forEach((id, deeds) -> {
                for (ItemStack d : deeds) {
                    CompoundTag c = new CompoundTag();
                    c.store("Owner", net.minecraft.core.UUIDUtil.CODEC, id);
                    c.put("Deed", com.cursedtides.util.Compat.saveStack(d, lookup));
                    list.add(c);
                }
            });
            nbt.put("Claims", list);
            return nbt;
        }

        private static Claims fromNbt(CompoundTag nbt, HolderLookup.Provider lookup) {
            Claims c = new Claims();
            ListTag list = nbt.getListOrEmpty("Claims");
            for (int i = 0; i < list.size(); i++) {
                CompoundTag e = list.getCompoundOrEmpty(i);
                com.cursedtides.util.Compat.loadStack(lookup, e.get("Deed")).ifPresent(d ->
                        c.pending.computeIfAbsent(e.read("Owner", net.minecraft.core.UUIDUtil.CODEC).orElse(null), k -> new ArrayList<>()).add(d));
            }
            return c;
        }
    }
}
