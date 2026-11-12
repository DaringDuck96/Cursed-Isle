package com.cursedtides.sea;

import com.cursedtides.entity.ShipEntity;
import com.cursedtides.item.ModItems;
import com.cursedtides.ship.ShipEvents;
import com.cursedtides.ship.ShipFaction;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

/** Named boss ships and treasure fleets: big prizes at sea. */
public final class SeaEvents {
    private SeaEvents() {}

    public static final String TREASURE = "cursedtides_treasure_ship", BOSS = "cursedtides_boss_ship";

    private static final String[] BOSS_SHIPS = {"The Crimson Widow", "Blackwater Jack's Revenge", "The Hungry Dark", "Gallows Bride",
            "The Iron Maw", "Bloody Mary Kettle", "The Salt Tyrant"};
    private static final String[] TREASURE_SHIPS = {"The Golden Hind of Kingsport", "Sovereign's Purse", "The Silver Duchess", "Crown Bounty"};

    public static boolean bossShip(ServerWorld world, ServerPlayerEntity player) {
        BlockPos at = ShipEvents.findSeaSpot(world, player, 80, 110);
        if (at == null) return false;
        ShipEntity ship = ShipEvents.spawnShip(world, ShipFaction.PIRATE, at.getX() + 0.5, at.getY() - 0.1, at.getZ() + 0.5, player);
        if (ship == null) return false;
        String name = BOSS_SHIPS[world.random.nextInt(BOSS_SHIPS.length)];
        ship.makeLegendary(name);
        ship.setCustomName(Text.literal(name).formatted(Formatting.DARK_RED, Formatting.BOLD));
        ship.setFlag(com.cursedtides.ship.FlagItem.Design.BLACK_SKULL.ordinal());
        ship.addCommandTag(BOSS);
        world.getServer().getPlayerManager().broadcast(Text.literal("The dread ship " + name + " has been sighted! Her captain has sunk "
                + (20 + world.random.nextInt(60)) + " ships. Sink her and her hold is yours.").formatted(Formatting.DARK_RED, Formatting.BOLD), false);
        return true;
    }

    public static boolean treasureFleet(ServerWorld world, ServerPlayerEntity player) {
        BlockPos at = ShipEvents.findSeaSpot(world, player, 80, 110);
        if (at == null) return false;
        ShipEntity galleon = ShipEvents.spawnShip(world, ShipFaction.MERCHANT, at.getX() + 0.5, at.getY() - 0.1, at.getZ() + 0.5, player);
        if (galleon == null) return false;
        galleon.setShipClass(com.cursedtides.ship.ShipClass.GALLEON);
        galleon.setHull(galleon.maxHull());
        String name = TREASURE_SHIPS[world.random.nextInt(TREASURE_SHIPS.length)];
        galleon.setCustomName(Text.literal(name).formatted(Formatting.GOLD, Formatting.BOLD));
        galleon.addCommandTag(TREASURE);
        galleon.cargo().addStack(new ItemStack(Items.GOLD_INGOT, 32));
        galleon.cargo().addStack(new ItemStack(ModItems.DOUBLOON, 64));
        galleon.cargo().addStack(new ItemStack(ModItems.DOUBLOON, 32));
        galleon.cargo().addStack(new ItemStack(ModItems.ANCIENT_COIN, 6));
        galleon.cargo().addStack(new ItemStack(com.cursedtides.explore.Cartography.CUT_GEM, 4));
        for (int i = 0; i < 2; i++) {
            BlockPos e = new BlockPos(at.getX() + (i == 0 ? 18 : -18), at.getY(), at.getZ() + 10);
            ShipEvents.spawnShip(world, ShipFaction.NAVY, e.getX() + 0.5, e.getY() - 0.1, e.getZ() + 0.5, player);
        }
        world.getServer().getPlayerManager().broadcast(Text.literal("A treasure fleet is crossing the Reach: the " + name
                + ", riding low with gold, under Tidewatch escort. Fortune favours the bold.").formatted(Formatting.GOLD), false);
        return true;
    }

    /** Called as a ship goes down. */
    public static void onSunk(ServerWorld world, ShipEntity ship, ServerPlayerEntity sinker) {
        if (ship.getCommandTags().contains(BOSS)) {
            ship.dropStack(com.cursedtides.combat.GearRarity.randomWeapon(world, world.random, 4));
            ship.dropStack(new ItemStack(ModItems.DOUBLOON, 60));
            ship.dropStack(new ItemStack(com.cursedtides.appearance.Accessories.random(world.random, true)));
            if (sinker != null) {
                com.cursedtides.activity.Achievements.grant(sinker, "dread_sunk");
                world.getServer().getPlayerManager().broadcast(Text.literal(sinker.getName().getString() + " has sent "
                        + ship.getName().getString() + " to the bottom!").formatted(Formatting.GOLD, Formatting.BOLD), false);
            }
        }
        if (ship.getCommandTags().contains(TREASURE) && sinker != null) {
            com.cursedtides.faction.FactionData.adjust(sinker, com.cursedtides.faction.Faction.NAVY, -80);
            com.cursedtides.faction.FactionData.adjust(sinker, com.cursedtides.faction.Faction.BRETHREN, 60);
        }
    }
}
