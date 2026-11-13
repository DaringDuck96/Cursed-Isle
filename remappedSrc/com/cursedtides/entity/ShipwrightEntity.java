package com.cursedtides.entity;

import com.cursedtides.item.ModItems;
import java.util.Optional;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;

/**
 * Saltmarrow's shipwright. Sells sloops outright and trades up: hand in your old deed
 * plus doubloons for a bigger hull. Also sells guns, shot and patching timber.
 */
public class ShipwrightEntity extends QuartermasterEntity {
    private static final int UNLIMITED = 9999;

    public ShipwrightEntity(EntityType<? extends WanderingTrader> type, Level world) {
        super(type, world);
    }

    @Override
    protected void updateTrades() {
        MerchantOffers offers = this.getOffers();
        offers.add(new MerchantOffer(new ItemCost(ModItems.DOUBLOON, 48), new ItemStack(ModItems.SLOOP_DEED), UNLIMITED, 5, 0.0f));
        offers.add(new MerchantOffer(new ItemCost(ModItems.DOUBLOON, 64), Optional.of(new ItemCost(ModItems.SLOOP_DEED, 1)),
                new ItemStack(ModItems.BRIGANTINE_DEED), UNLIMITED, 10, 0.0f));
        offers.add(new MerchantOffer(new ItemCost(ModItems.DOUBLOON, 64), Optional.of(new ItemCost(ModItems.BRIGANTINE_DEED, 1)),
                new ItemStack(ModItems.GALLEON_DEED), UNLIMITED, 20, 0.0f));
        offers.add(new MerchantOffer(new ItemCost(ModItems.DOUBLOON, 32), Optional.of(new ItemCost(ModItems.SLOOP_DEED, 1)),
                new ItemStack(ModItems.SCHOONER_DEED), UNLIMITED, 10, 0.0f));
        offers.add(new MerchantOffer(new ItemCost(ModItems.DOUBLOON, 40), Optional.of(new ItemCost(ModItems.BRIGANTINE_DEED, 1)),
                new ItemStack(ModItems.BRIG_DEED), UNLIMITED, 15, 0.0f));
        offers.add(new MerchantOffer(new ItemCost(ModItems.DOUBLOON, 64), Optional.of(new ItemCost(ModItems.BRIG_DEED, 1)),
                new ItemStack(ModItems.FRIGATE_DEED), UNLIMITED, 25, 0.0f));
        offers.add(new MerchantOffer(new ItemCost(ModItems.DOUBLOON, 64), Optional.of(new ItemCost(ModItems.FRIGATE_DEED, 1)),
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

    private static MerchantOffer sell(int doubloons, Item item, int count) {
        return new MerchantOffer(new ItemCost(ModItems.DOUBLOON, doubloons), new ItemStack(item, count), UNLIMITED, 2, 0.0f);
    }

    @Override
    public String dialogueFile() {
        return "shipwright";
    }

    /** Sneak-click holding a deed to sell the ship back to the yard for half her value. */
    @Override
    public net.minecraft.world.InteractionResult mobInteract(net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && held.getItem() instanceof com.cursedtides.item.ShipDeedItem deed) {
            if (!this.level().isClientSide) {
                var data = held.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
                boolean loaded = data != null && !data.copyTag().getList("Cargo", net.minecraft.nbt.Tag.TAG_COMPOUND).isEmpty()
                        && data.copyTag().getList("Cargo", net.minecraft.nbt.Tag.TAG_COMPOUND).stream()
                        .anyMatch(e -> e instanceof net.minecraft.nbt.CompoundTag c && !c.getString("id").isEmpty());
                if (loaded) {
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("Empty her hold first. I buy hulls, not cargo.")
                            .withStyle(net.minecraft.ChatFormatting.GRAY), false);
                } else {
                    int price = com.cursedtides.trade.Insurance.value(deed.shipClass()) / 2;
                    held.shrink(1);
                    com.cursedtides.trade.Wallet.give(player, price * com.cursedtides.trade.Wallet.SILVER_PER_DOUBLOON);
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("Sold to the yard for " + price + " doubloons. She'll find a new captain.")
                            .withStyle(net.minecraft.ChatFormatting.GOLD), false);
                }
            }
            return net.minecraft.world.InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }
}
