package com.cursedtides.trade;

import com.cursedtides.item.ShipDeedItem;
import com.cursedtides.ship.ShipClass;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.PersistentState;

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
        NbtComponent c = deed.get(DataComponentTypes.CUSTOM_DATA);
        return c != null && c.copyNbt().getBoolean(KEY);
    }

    /** At the banker, holding a deed: buys a policy. */
    public static void insure(ServerPlayerEntity player, ItemStack deed, ShipDeedItem item) {
        if (isInsured(deed)) {
            player.sendMessage(Text.literal("That ship is already insured.").formatted(Formatting.GRAY), false);
            return;
        }
        int premium = premium(item.shipClass());
        if (!Wallet.pay(player, premium * Wallet.SILVER_PER_DOUBLOON)) {
            player.sendMessage(Text.literal("A policy on a " + item.shipClass().displayName() + " costs " + premium
                    + " doubloons. Come back with the coin.").formatted(Formatting.RED), false);
            return;
        }
        NbtComponent c = deed.get(DataComponentTypes.CUSTOM_DATA);
        NbtCompound data = c == null ? new NbtCompound() : c.copyNbt();
        data.putBoolean(KEY, true);
        deed.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(data));
        player.sendMessage(Text.literal("Insured for " + premium + " doubloons. If she goes down, the bank will replace her hull"
                + " (though not her cargo).").formatted(Formatting.GOLD), false);
    }

    /** Called when an insured player ship sinks. */
    public static void payOut(MinecraftServer server, UUID owner, ItemStack replacement) {
        ServerPlayerEntity p = server.getPlayerManager().getPlayer(owner);
        if (p != null) {
            if (!p.getInventory().insertStack(replacement)) p.dropItem(replacement, false);
            p.sendMessage(Text.literal("The Bank of Kingsport honours your policy: a replacement deed for ")
                    .append(replacement.getName()).append(Text.literal(".")).formatted(Formatting.GOLD), false);
            return;
        }
        Claims claims = Claims.get(server);
        claims.pending.computeIfAbsent(owner, k -> new ArrayList<>()).add(replacement);
        claims.markDirty();
    }

    /** Hands over any replacement deeds waiting for this player. */
    public static boolean collect(ServerPlayerEntity p) {
        Claims claims = Claims.get(p.getServer());
        List<ItemStack> deeds = claims.pending.remove(p.getUuid());
        if (deeds == null || deeds.isEmpty()) return false;
        for (ItemStack d : deeds) if (!p.getInventory().insertStack(d)) p.dropItem(d, false);
        claims.markDirty();
        p.sendMessage(Text.literal("Your insurance claim is settled: " + deeds.size() + " replacement deed(s).").formatted(Formatting.GOLD), false);
        return true;
    }

    static final class Claims extends PersistentState {
        final Map<UUID, List<ItemStack>> pending = new HashMap<>();
        private static final Type<Claims> TYPE = new Type<>(Claims::new, Claims::fromNbt, null);

        static Claims get(MinecraftServer server) {
            return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, "cursedtides_insurance");
        }

        @Override
        public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
            NbtList list = new NbtList();
            pending.forEach((id, deeds) -> {
                for (ItemStack d : deeds) {
                    NbtCompound c = new NbtCompound();
                    c.putUuid("Owner", id);
                    c.put("Deed", d.encode(lookup));
                    list.add(c);
                }
            });
            nbt.put("Claims", list);
            return nbt;
        }

        private static Claims fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
            Claims c = new Claims();
            NbtList list = nbt.getList("Claims", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < list.size(); i++) {
                NbtCompound e = list.getCompound(i);
                ItemStack.fromNbt(lookup, e.get("Deed")).ifPresent(d ->
                        c.pending.computeIfAbsent(e.getUuid("Owner"), k -> new ArrayList<>()).add(d));
            }
            return c;
        }
    }
}
