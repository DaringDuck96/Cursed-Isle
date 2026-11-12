package com.cursedtides.entity;

import com.cursedtides.item.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradedItem;
import net.minecraft.world.World;

import java.util.Optional;

/**
 * Saltmarrow's shipwright. Sells sloops outright and trades up: hand in your old deed
 * plus doubloons for a bigger hull. Also sells guns, shot and patching timber.
 */
public class ShipwrightEntity extends QuartermasterEntity {
    private static final int UNLIMITED = 9999;

    public ShipwrightEntity(EntityType<? extends WanderingTraderEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected void fillRecipes() {
        TradeOfferList offers = this.getOffers();
        offers.add(new TradeOffer(new TradedItem(ModItems.DOUBLOON, 48), new ItemStack(ModItems.SLOOP_DEED), UNLIMITED, 5, 0.0f));
        offers.add(new TradeOffer(new TradedItem(ModItems.DOUBLOON, 64), Optional.of(new TradedItem(ModItems.SLOOP_DEED, 1)),
                new ItemStack(ModItems.BRIGANTINE_DEED), UNLIMITED, 10, 0.0f));
        offers.add(new TradeOffer(new TradedItem(ModItems.DOUBLOON, 64), Optional.of(new TradedItem(ModItems.BRIGANTINE_DEED, 1)),
                new ItemStack(ModItems.GALLEON_DEED), UNLIMITED, 20, 0.0f));
        offers.add(new TradeOffer(new TradedItem(ModItems.DOUBLOON, 32), Optional.of(new TradedItem(ModItems.SLOOP_DEED, 1)),
                new ItemStack(ModItems.SCHOONER_DEED), UNLIMITED, 10, 0.0f));
        offers.add(new TradeOffer(new TradedItem(ModItems.DOUBLOON, 40), Optional.of(new TradedItem(ModItems.BRIGANTINE_DEED, 1)),
                new ItemStack(ModItems.BRIG_DEED), UNLIMITED, 15, 0.0f));
        offers.add(new TradeOffer(new TradedItem(ModItems.DOUBLOON, 64), Optional.of(new TradedItem(ModItems.BRIG_DEED, 1)),
                new ItemStack(ModItems.FRIGATE_DEED), UNLIMITED, 25, 0.0f));
        offers.add(new TradeOffer(new TradedItem(ModItems.DOUBLOON, 64), Optional.of(new TradedItem(ModItems.FRIGATE_DEED, 1)),
                new ItemStack(ModItems.SHIP_OF_THE_LINE_DEED), UNLIMITED, 40, 0.0f));
        // Fittings
        offers.add(sell(24, ModItems.SWIVEL_GUN, 1));
        offers.add(sell(8, com.cursedtides.activity.MiniGames.CARPENTERS_MALLET, 1));
        offers.add(sell(35, com.cursedtides.ship.FigureheadItem.MERMAID, 1));
        offers.add(sell(35, com.cursedtides.ship.FigureheadItem.LION, 1));
        offers.add(sell(45, com.cursedtides.ship.FigureheadItem.SKULL, 1));
        offers.add(sell(40, com.cursedtides.ship.FigureheadItem.EAGLE, 1));
        offers.add(sell(20, ModItems.HULL_PLATING, 1));
        offers.add(sell(30, ModItems.GUN_CARRIAGE, 1));
        offers.add(sell(16, ModItems.FINE_CANVAS, 1));
        offers.add(sell(18, ModItems.CARGO_BULKHEAD, 1));
        // Hands
        offers.add(sell(20, ModItems.GUNNER_ARTICLES, 1));
        offers.add(sell(15, ModItems.BOSUN_ARTICLES, 1));
        offers.add(sell(25, ModItems.NAVIGATOR_ARTICLES, 1));
        // Shot
        offers.add(sell(8, ModItems.CHAIN_SHOT, 4));
        offers.add(sell(6, ModItems.GRAPESHOT, 4));
        offers.add(sell(12, ModItems.EXPLOSIVE_SHELL, 4));
        offers.add(sell(6, ModItems.CANNONBALL, 4));
        offers.add(sell(2, Items.OAK_PLANKS, 16));
        offers.add(sell(40, ModItems.SHIPS_CANNON, 1));
        offers.add(sell(4, Items.SPYGLASS, 1));
        offers.add(sell(3, Items.COMPASS, 1));
        // Colours to fly
        for (com.cursedtides.ship.FlagItem.Design d : com.cursedtides.ship.FlagItem.Design.values()) {
            if (d.price > 0) offers.add(sell(d.price, com.cursedtides.ship.FlagItem.item(d), 1));
        }
    }

    private static TradeOffer sell(int doubloons, Item item, int count) {
        return new TradeOffer(new TradedItem(ModItems.DOUBLOON, doubloons), new ItemStack(item, count), UNLIMITED, 2, 0.0f);
    }

    @Override
    public String dialogueFile() {
        return "shipwright";
    }

    /** Sneak-click holding a deed to sell the ship back to the yard for half her value. */
    @Override
    public net.minecraft.util.ActionResult interactMob(net.minecraft.entity.player.PlayerEntity player, net.minecraft.util.Hand hand) {
        ItemStack held = player.getStackInHand(hand);
        if (player.isSneaking() && held.getItem() instanceof com.cursedtides.item.ShipDeedItem deed) {
            if (!this.getWorld().isClient) {
                var data = held.get(net.minecraft.component.DataComponentTypes.CUSTOM_DATA);
                boolean loaded = data != null && !data.copyNbt().getList("Cargo", net.minecraft.nbt.NbtElement.COMPOUND_TYPE).isEmpty()
                        && data.copyNbt().getList("Cargo", net.minecraft.nbt.NbtElement.COMPOUND_TYPE).stream()
                        .anyMatch(e -> e instanceof net.minecraft.nbt.NbtCompound c && !c.getString("id").isEmpty());
                if (loaded) {
                    player.sendMessage(net.minecraft.text.Text.literal("Empty her hold first. I buy hulls, not cargo.")
                            .formatted(net.minecraft.util.Formatting.GRAY), false);
                } else {
                    int price = com.cursedtides.trade.Insurance.value(deed.shipClass()) / 2;
                    held.decrement(1);
                    com.cursedtides.trade.Wallet.give(player, price * com.cursedtides.trade.Wallet.SILVER_PER_DOUBLOON);
                    player.sendMessage(net.minecraft.text.Text.literal("Sold to the yard for " + price + " doubloons. She'll find a new captain.")
                            .formatted(net.minecraft.util.Formatting.GOLD), false);
                }
            }
            return net.minecraft.util.ActionResult.success(this.getWorld().isClient);
        }
        return super.interactMob(player, hand);
    }
}
