package com.cursedtides.explore;

import com.cursedtides.dialogue.Specials;
import com.cursedtides.world.Archipelago;
import com.cursedtides.world.ModWorld;
import com.cursedtides.living.Residents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import java.util.List;

final class CursedTidesInit {
    private CursedTidesInit() {}
    static void touch(Object o) { java.util.Objects.requireNonNull(o); }
}

/** Exploration systems: diving, sea hazards, curios washing ashore, the sunken city and the encyclopedia. */
public final class Exploration {
    private Exploration() {}

    public static void register() {
        CursedTidesInit.touch(Collectibles.ITEM); // make sure the curio item registers during start-up
        Cartography.register();
        Diving.register();
        SeaHazards.register();
        Encyclopedia.register();
        ServerTickEvents.END_SERVER_TICK.register(Exploration::tick);

        Specials.register("encyclopedia", (p, npc) -> {
            Encyclopedia.open(p);
            return null;
        });
        Specials.register("sell_curios", (p, npc) -> {
            int sold = 0;
            var inv = p.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack s = inv.getItem(i);
                if (s.is(Collectibles.ITEM)) {
                    Collectibles.Entry e = CollectibleItem.entry(s);
                    if (e != null) Collectibles.study(p, e);
                    sold += s.getCount();
                    inv.setItem(i, ItemStack.EMPTY);
                }
            }
            if (sold == 0) return List.of("You've nothing for the archive today. Bring me bottles, logs, paintings, instruments.");
            com.cursedtides.trade.Wallet.give(p, sold * 35);
            return List.of("Wonderful. Wonderful! " + sold + " piece" + (sold == 1 ? "" : "s") + " for the archive. Here's " + (sold * 35 / 10) + " doubloons and change for your trouble.");
        });
        Specials.register("buy_helmet", (p, npc) -> {
            if (!com.cursedtides.trade.Wallet.pay(p, 220)) return List.of("Twenty-two doubloons for the helmet. Brass doesn't grow on palms.");
            p.addItem(new ItemStack(Diving.HELMET));
            return List.of("There. Four minutes of air in her, refills when you come up. Don't hold your breath; she does that for you.");
        });
    }

    private static void tick(MinecraftServer server) {
        if (!ModWorld.isArchipelago()) return;
        ServerLevel world = server.overworld();
        int t = server.getTickCount();
        // Bottles wash up on beaches near players every few minutes
        if (t % 3600 == 0) {
            for (ServerPlayer p : world.players()) {
                if (world.random.nextFloat() > 0.35f) continue;
                double a = world.random.nextDouble() * Math.PI * 2;
                BlockPos at = Residents.openGround(world, (int) (p.getX() + Math.cos(a) * 20), (int) (p.getZ() + Math.sin(a) * 20), 10);
                if (at == null || !world.getBlockState(at.below()).is(Blocks.SAND)) continue;
                ItemEntity bottle = new ItemEntity(world, at.getX() + 0.5, at.getY() + 0.2, at.getZ() + 0.5,
                        Collectibles.randomFor(p, Collectibles.Kind.BOTTLE));
                bottle.setGlowingTag(true);
                world.addFreshEntity(bottle);
            }
        }
        // Reaching the heart of the drowned city
        if (t % 40 == 0) {
            Archipelago.Island city = com.cursedtides.world.SunkenCity.ISLAND;
            for (ServerPlayer p : world.players()) {
                double dx = p.getX() - city.cx(), dz = p.getZ() - city.cz();
                if (dx * dx + dz * dz < 10 * 10 && p.getY() < Archipelago.WATER_TOP) {
                    com.cursedtides.activity.Achievements.grant(p, "sunken_city");
                }
            }
        }
    }
}
