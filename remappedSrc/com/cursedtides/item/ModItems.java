package com.cursedtides.item;

import com.cursedtides.CursedTides;
import com.cursedtides.entity.ModEntities;
import com.cursedtides.ship.ShipClass;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

public final class ModItems {
    private ModItems() {}

    // Weapons
    public static final Item CUTLASS = register("cutlass",
            new CutlassItem(Tiers.IRON, new Item.Properties()
                    .attributes(SwordItem.createAttributes(Tiers.IRON, 3, -2.2f))));

    public static final Item FLINTLOCK_PISTOL = register("flintlock_pistol",
            new FlintlockPistolItem(new Item.Properties().stacksTo(1).durability(250)));

    public static final Item RAPIER = register("rapier",
            new ParryingBladeItem(Tiers.IRON, 14, new Item.Properties()
                    .attributes(SwordItem.createAttributes(Tiers.IRON, 2, -1.8f))));

    public static final Item DAGGER = register("dagger",
            new DaggerItem(Tiers.IRON, new Item.Properties()
                    .attributes(SwordItem.createAttributes(Tiers.IRON, 1, -1.2f))));

    public static final Item BOARDING_AXE = register("boarding_axe",
            new BoardingAxeItem(Tiers.IRON, new Item.Properties()
                    .attributes(net.minecraft.world.item.AxeItem.createAttributes(Tiers.IRON, 7.0f, -3.2f))));

    public static final Item THROWING_KNIFE = register("throwing_knife",
            new ThrowingKnifeItem(new Item.Properties().stacksTo(16)));

    public static final Item MUSKET = register("musket",
            new FlintlockPistolItem(new Item.Properties().stacksTo(1).durability(400).rarity(Rarity.RARE), FlintlockPistolItem.MUSKET));

    public static final Item BLUNDERBUSS = register("blunderbuss",
            new FlintlockPistolItem(new Item.Properties().stacksTo(1).durability(300).rarity(Rarity.RARE), FlintlockPistolItem.BLUNDERBUSS));

    public static final Item SABER = register("saber",
            new CutlassItem(Tiers.IRON, new Item.Properties().rarity(Rarity.UNCOMMON)
                    .attributes(SwordItem.createAttributes(Tiers.IRON, 3, -1.9f))));

    public static final Item DUAL_BLADES = register("dual_blades",
            new DualBladesItem(Tiers.IRON, new Item.Properties().rarity(Rarity.UNCOMMON)
                    .attributes(SwordItem.createAttributes(Tiers.IRON, 1, -1.0f))));

    public static final Item BROADSWORD = register("broadsword",
            new HeavyWeaponItem(Tiers.IRON, false, new Item.Properties().rarity(Rarity.UNCOMMON)
                    .attributes(SwordItem.createAttributes(Tiers.IRON, 7, -3.0f))));

    public static final Item WAR_HAMMER = register("war_hammer",
            new HeavyWeaponItem(Tiers.IRON, true, new Item.Properties().rarity(Rarity.UNCOMMON)
                    .attributes(SwordItem.createAttributes(Tiers.IRON, 8, -3.3f))));

    public static final Item REPEATER_PISTOL = register("repeater_pistol",
            new FlintlockPistolItem(new Item.Properties().stacksTo(1).durability(350).rarity(Rarity.RARE), FlintlockPistolItem.REPEATER));
    public static final Item BAYONET_MUSKET = register("bayonet_musket",
            new FlintlockPistolItem(new Item.Properties().stacksTo(1).durability(450).rarity(Rarity.RARE)
                    .attributes(net.minecraft.world.item.component.ItemAttributeModifiers.builder()
                            .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,
                                    new net.minecraft.world.entity.ai.attributes.AttributeModifier(com.cursedtides.CursedTides.id("bayonet_damage"), 6.0,
                                            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),
                                    net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
                            .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED,
                                    new net.minecraft.world.entity.ai.attributes.AttributeModifier(com.cursedtides.CursedTides.id("bayonet_speed"), -2.9,
                                            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),
                                    net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
                            .build()), FlintlockPistolItem.BAYONET));

    public static final Item DOUBLE_BARREL_PISTOL = register("double_barrel_pistol",
            new FlintlockPistolItem(new Item.Properties().stacksTo(1).durability(300).rarity(Rarity.UNCOMMON), FlintlockPistolItem.DOUBLE_BARREL));

    public static final Item HAND_CANNON = register("hand_cannon",
            new FlintlockPistolItem(new Item.Properties().stacksTo(1).durability(200).rarity(Rarity.RARE), FlintlockPistolItem.HAND_CANNON));

    public static final Item WANTED_POSTER = register("wanted_poster", new WantedPosterItem(new Item.Properties().stacksTo(16)));

    public static final Item SHIPS_CANNON = register("ships_cannon",
            new ShipsCannonItem(new Item.Properties().stacksTo(1).durability(120).rarity(Rarity.UNCOMMON)));

    public static final Item HEX_STAFF = register("hex_staff",
            new HexStaffItem(new Item.Properties().stacksTo(1).durability(300).rarity(Rarity.RARE)));

    // Ammunition
    public static final Item PISTOL_SHOT = register("pistol_shot", new Item(new Item.Properties()));
    // Special shot: hold it in the off hand and your gun loads it first
    public static final Item VENOM_SHOT = register("venom_shot", new LoreItem(new Item.Properties(), "tooltip.cursedtides.venom_shot"));
    public static final Item HOT_SHOT = register("hot_shot", new LoreItem(new Item.Properties(), "tooltip.cursedtides.hot_shot"));
    public static final Item SILVER_SHOT = register("silver_shot", new LoreItem(new Item.Properties(), "tooltip.cursedtides.silver_shot"));
    public static final Item FIREBRAND_SHOT = register("firebrand_shot", new LoreItem(new Item.Properties().stacksTo(16), "tooltip.cursedtides.firebrand_shot"));
    public static final Item BANE_SHOT = register("bane_shot", new LoreItem(new Item.Properties().stacksTo(16), "tooltip.cursedtides.bane_shot"));
    public static final Item LIGHT_REFIT = register("light_refit", new LoreItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), "tooltip.cursedtides.light_refit"));
    public static final Item WAR_REFIT = register("war_refit", new LoreItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), "tooltip.cursedtides.war_refit"));
    public static final Item FESTIVAL_TOKEN = register("festival_token", new LoreItem(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON), "tooltip.cursedtides.festival_token"));
    public static final Item CANNONBALL = register("cannonball", new Item(new Item.Properties().stacksTo(16)));

    /** Visual only: what a flying hex bolt looks like. Not obtainable. */
    public static final Item HEX_ORB = register("hex_orb", new Item(new Item.Properties()));

    // Treasure
    public static final Item DOUBLOON = register("doubloon", new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    // Ships
    public static final Item SLOOP_DEED = register("sloop_deed",
            new ShipDeedItem(ShipClass.SLOOP, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final Item BRIGANTINE_DEED = register("brigantine_deed",
            new ShipDeedItem(ShipClass.BRIGANTINE, new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final Item GALLEON_DEED = register("galleon_deed",
            new ShipDeedItem(ShipClass.GALLEON, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    // Ship upgrades and crew
    public static final Item SCHOONER_DEED = register("schooner_deed",
            new ShipDeedItem(ShipClass.SCHOONER, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final Item BRIG_DEED = register("brig_deed",
            new ShipDeedItem(ShipClass.BRIG, new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final Item FRIGATE_DEED = register("frigate_deed",
            new ShipDeedItem(ShipClass.FRIGATE, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final Item SHIP_OF_THE_LINE_DEED = register("ship_of_the_line_deed",
            new ShipDeedItem(ShipClass.SHIP_OF_THE_LINE, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final Item SWIVEL_GUN = register("swivel_gun",
            new ShipUpgradeItem(ShipUpgradeItem.Kind.SWIVEL, new Item.Properties().stacksTo(4)));
    public static final Item HULL_PLATING = register("hull_plating",
            new ShipUpgradeItem(ShipUpgradeItem.Kind.PLATING, new Item.Properties().stacksTo(16)));
    public static final Item GUN_CARRIAGE = register("gun_carriage",
            new ShipUpgradeItem(ShipUpgradeItem.Kind.GUNS, new Item.Properties().stacksTo(16)));
    public static final Item FINE_CANVAS = register("fine_canvas",
            new ShipUpgradeItem(ShipUpgradeItem.Kind.CANVAS, new Item.Properties().stacksTo(16)));
    public static final Item CARGO_BULKHEAD = register("cargo_bulkhead",
            new ShipUpgradeItem(ShipUpgradeItem.Kind.HOLD, new Item.Properties().stacksTo(16)));
    public static final Item GUNNER_ARTICLES = register("gunner_articles",
            new CrewArticlesItem(0, new Item.Properties().stacksTo(16)));
    public static final Item BOSUN_ARTICLES = register("bosun_articles",
            new CrewArticlesItem(1, new Item.Properties().stacksTo(16)));
    public static final Item NAVIGATOR_ARTICLES = register("navigator_articles",
            new CrewArticlesItem(2, new Item.Properties().stacksTo(16)));

    // Special shot
    public static final Item CHAIN_SHOT = register("chain_shot", new LoreItem(new Item.Properties().stacksTo(16),
            "tooltip.cursedtides.chain_shot"));
    public static final Item GRAPESHOT = register("grapeshot", new LoreItem(new Item.Properties().stacksTo(16),
            "tooltip.cursedtides.grapeshot"));
    public static final Item EXPLOSIVE_SHELL = register("explosive_shell", new LoreItem(new Item.Properties().stacksTo(16),
            "tooltip.cursedtides.explosive_shell"));

    // Treasure hunting and dungeons
    public static final Item TREASURE_MAP = register("treasure_map",
            new TreasureMapItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final Item MAP_FRAGMENT = register("map_fragment",
            new MapFragmentItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));
    public static final Item RUSTED_KEY = register("rusted_key",
            new LoreItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON), "tooltip.cursedtides.rusted_key"));

    // Side quests
    public static final Item GOVERNORS_LEDGER = register("governors_ledger",
            new LoreItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), "tooltip.cursedtides.governors_ledger"));

    // The Seven Tides: relics and story items
    public static final java.util.List<Item> RELICS = java.util.stream.Stream.of("dawn", "reef", "serpent", "ember", "veil", "sovereign", "lantern")
            .map(n -> register("relic_" + n, new LoreItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), "tooltip.cursedtides.relic_" + n)))
            .toList();
    public static final Item FLEET_LOG = register("fleet_log",
            new com.cursedtides.quest.FleetLogItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final Item LAMPWRIGHT_SEAL = register("lampwright_seal",
            new LoreItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), "tooltip.cursedtides.lampwright_seal"));
    // Act five, and boarding
    public static final Item GRAPPLING_HOOK = register("grappling_hook",
            new com.cursedtides.ship.Boarding.GrapplingHookItem(new Item.Properties().stacksTo(1).durability(64)));
    public static final Item BLACK_PENNANT = register("black_pennant",
            new LoreItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON), "tooltip.cursedtides.black_pennant"));
    public static final Item ADMIRALS_COMMISSION = register("admirals_commission",
            new LoreItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), "tooltip.cursedtides.admirals_commission"));
    public static final Item ACCORD_TOKEN = register("accord_token",
            new LoreItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), "tooltip.cursedtides.accord_token"));

    // Coin: ten silver pieces to the doubloon
    public static final Item SILVER_PIECE = register("silver_piece",
            new LoreItem(new Item.Properties().stacksTo(64), "tooltip.cursedtides.silver_piece"));

    // Collectibles and wildlife spoils
    public static final Item ANCIENT_COIN = register("ancient_coin",
            new LoreItem(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON), "tooltip.cursedtides.ancient_coin"));
    public static final Item ANCIENT_RELIC = register("ancient_relic",
            new LoreItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE), "tooltip.cursedtides.ancient_relic"));
    public static final Item SHARK_TOOTH = register("shark_tooth",
            new LoreItem(new Item.Properties().stacksTo(64), "tooltip.cursedtides.shark_tooth"));

    // Story: "The Drowned Bell"
    public static final Item SEA_CHART = register("sea_chart",
            new SeaChartItem(new Item.Properties().stacksTo(1)));
    public static final Item REEF_SHARD = register("reef_shard",
            new LoreItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), "tooltip.cursedtides.reef_shard"));
    public static final Item EMBER_SHARD = register("ember_shard",
            new LoreItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant(), "tooltip.cursedtides.ember_shard"));
    public static final Item STILLING_BELL = register("stilling_bell",
            new StillingBellItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));

    // Spawn eggs
    public static final Item CURSED_PIRATE_SPAWN_EGG = register("cursed_pirate_spawn_egg",
            new SpawnEggItem(ModEntities.CURSED_PIRATE, 0xD8D2B8, 0x1F7A6E, new Item.Properties()));
    public static final Item QUARTERMASTER_SPAWN_EGG = register("quartermaster_spawn_egg",
            new SpawnEggItem(ModEntities.QUARTERMASTER, 0x1E2C4A, 0xD4A93C, new Item.Properties()));
    public static final Item DROWNED_CAPTAIN_SPAWN_EGG = register("drowned_captain_spawn_egg",
            new SpawnEggItem(ModEntities.DROWNED_CAPTAIN, 0x1B3B3A, 0x4FE3C8, new Item.Properties()));

    public static final CreativeModeTab GROUP = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CursedTides.id("main"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(CUTLASS))
                    .title(Component.translatable("itemGroup.cursedtides.main"))
                    .displayItems((context, entries) -> {
                        entries.accept(CUTLASS);
                        entries.accept(FLINTLOCK_PISTOL);
                        entries.accept(SABER);
                        entries.accept(DUAL_BLADES);
                        entries.accept(BROADSWORD);
                        entries.accept(WAR_HAMMER);
                        entries.accept(DOUBLE_BARREL_PISTOL);
                        entries.accept(REPEATER_PISTOL);
                        entries.accept(BAYONET_MUSKET);
                        entries.accept(HAND_CANNON);
                        entries.accept(WANTED_POSTER);
                        entries.accept(MUSKET);
                        entries.accept(BLUNDERBUSS);
                        entries.accept(PISTOL_SHOT);
                        entries.accept(VENOM_SHOT);
                        entries.accept(HOT_SHOT);
                        entries.accept(SILVER_SHOT);
                        entries.accept(FIREBRAND_SHOT);
                        entries.accept(BANE_SHOT);
                        entries.accept(LIGHT_REFIT);
                        entries.accept(WAR_REFIT);
                        entries.accept(FESTIVAL_TOKEN);
                        entries.accept(SHIPS_CANNON);
                        entries.accept(CANNONBALL);
                        entries.accept(HEX_STAFF);
                        entries.accept(DOUBLOON);
                        entries.accept(SLOOP_DEED);
                        entries.accept(BRIGANTINE_DEED);
                        entries.accept(GALLEON_DEED);
                        entries.accept(SCHOONER_DEED);
                        entries.accept(BRIG_DEED);
                        entries.accept(FRIGATE_DEED);
                        entries.accept(SHIP_OF_THE_LINE_DEED);
                        entries.accept(SWIVEL_GUN);
                        entries.accept(HULL_PLATING);
                        entries.accept(GUN_CARRIAGE);
                        entries.accept(FINE_CANVAS);
                        entries.accept(CARGO_BULKHEAD);
                        entries.accept(GUNNER_ARTICLES);
                        entries.accept(BOSUN_ARTICLES);
                        entries.accept(NAVIGATOR_ARTICLES);
                        entries.accept(CHAIN_SHOT);
                        entries.accept(GRAPESHOT);
                        entries.accept(EXPLOSIVE_SHELL);
                        entries.accept(TREASURE_MAP);
                        entries.accept(MAP_FRAGMENT);
                        entries.accept(RUSTED_KEY);
                        ModClothing.all().forEach(entries::accept);
                        entries.accept(RAPIER);
                        entries.accept(DAGGER);
                        entries.accept(BOARDING_AXE);
                        entries.accept(THROWING_KNIFE);
                        entries.accept(SEA_CHART);
                        entries.accept(REEF_SHARD);
                        entries.accept(EMBER_SHARD);
                        entries.accept(STILLING_BELL);
                        entries.accept(CURSED_PIRATE_SPAWN_EGG);
                        entries.accept(QUARTERMASTER_SPAWN_EGG);
                        entries.accept(DROWNED_CAPTAIN_SPAWN_EGG);
                        entries.accept(ANCIENT_COIN);
                        entries.accept(ANCIENT_RELIC);
                        entries.accept(SHARK_TOOTH);
                        entries.accept(SILVER_PIECE);
                        RELICS.forEach(entries::accept);
                        entries.accept(FLEET_LOG);
                        entries.accept(LAMPWRIGHT_SEAL);
                        entries.accept(ACCORD_TOKEN);
                        entries.accept(GRAPPLING_HOOK);
                        entries.accept(BLACK_PENNANT);
                        entries.accept(ADMIRALS_COMMISSION);
                    })
                    .build());

    private static Item register(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, CursedTides.id(name), item);
    }

    /** Called from the mod initializer to force class loading. */
    public static void register() {
        ModClothing.all();
        CursedTides.LOGGER.debug("Registered Cursed Tides items");
    }
}
