package com.cursedtides.item;

import net.minecraft.world.item.ToolMaterial;
import com.cursedtides.CursedTides;
import com.cursedtides.entity.ModEntities;
import com.cursedtides.ship.ShipClass;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;

public final class ModItems {
    private ModItems() {}

    // Weapons
    public static final Item CUTLASS = register(com.cursedtides.util.Reg.itemName("cutlass"),
            new CutlassItem(ToolMaterial.IRON, com.cursedtides.util.Reg.props()
                    .sword(ToolMaterial.IRON, 3, -2.2f)));

    public static final Item FLINTLOCK_PISTOL = register(com.cursedtides.util.Reg.itemName("flintlock_pistol"),
            new FlintlockPistolItem(com.cursedtides.util.Reg.props().stacksTo(1).durability(250)));

    public static final Item RAPIER = register(com.cursedtides.util.Reg.itemName("rapier"),
            new ParryingBladeItem(ToolMaterial.IRON, 14, com.cursedtides.util.Reg.props()
                    .sword(ToolMaterial.IRON, 2, -1.8f)));

    public static final Item DAGGER = register(com.cursedtides.util.Reg.itemName("dagger"),
            new DaggerItem(ToolMaterial.IRON, com.cursedtides.util.Reg.props()
                    .sword(ToolMaterial.IRON, 1, -1.2f)));

    public static final Item BOARDING_AXE = register(com.cursedtides.util.Reg.itemName("boarding_axe"),
            new BoardingAxeItem(ToolMaterial.IRON, com.cursedtides.util.Reg.props()
                    .axe(ToolMaterial.IRON, 7.0f, -3.2f)));

    public static final Item THROWING_KNIFE = register(com.cursedtides.util.Reg.itemName("throwing_knife"),
            new ThrowingKnifeItem(com.cursedtides.util.Reg.props().stacksTo(16)));

    public static final Item MUSKET = register(com.cursedtides.util.Reg.itemName("musket"),
            new FlintlockPistolItem(com.cursedtides.util.Reg.props().stacksTo(1).durability(400).rarity(Rarity.RARE), FlintlockPistolItem.MUSKET));

    public static final Item BLUNDERBUSS = register(com.cursedtides.util.Reg.itemName("blunderbuss"),
            new FlintlockPistolItem(com.cursedtides.util.Reg.props().stacksTo(1).durability(300).rarity(Rarity.RARE), FlintlockPistolItem.BLUNDERBUSS));

    public static final Item SABER = register(com.cursedtides.util.Reg.itemName("saber"),
            new CutlassItem(ToolMaterial.IRON, com.cursedtides.util.Reg.props().rarity(Rarity.UNCOMMON)
                    .sword(ToolMaterial.IRON, 3, -1.9f)));

    public static final Item DUAL_BLADES = register(com.cursedtides.util.Reg.itemName("dual_blades"),
            new DualBladesItem(ToolMaterial.IRON, com.cursedtides.util.Reg.props().rarity(Rarity.UNCOMMON)
                    .sword(ToolMaterial.IRON, 1, -1.0f)));

    public static final Item BROADSWORD = register(com.cursedtides.util.Reg.itemName("broadsword"),
            new HeavyWeaponItem(ToolMaterial.IRON, false, com.cursedtides.util.Reg.props().rarity(Rarity.UNCOMMON)
                    .sword(ToolMaterial.IRON, 7, -3.0f)));

    public static final Item WAR_HAMMER = register(com.cursedtides.util.Reg.itemName("war_hammer"),
            new HeavyWeaponItem(ToolMaterial.IRON, true, com.cursedtides.util.Reg.props().rarity(Rarity.UNCOMMON)
                    .sword(ToolMaterial.IRON, 8, -3.3f)));

    public static final Item REPEATER_PISTOL = register(com.cursedtides.util.Reg.itemName("repeater_pistol"),
            new FlintlockPistolItem(com.cursedtides.util.Reg.props().stacksTo(1).durability(350).rarity(Rarity.RARE), FlintlockPistolItem.REPEATER));
    public static final Item BAYONET_MUSKET = register(com.cursedtides.util.Reg.itemName("bayonet_musket"),
            new FlintlockPistolItem(com.cursedtides.util.Reg.props().stacksTo(1).durability(450).rarity(Rarity.RARE)
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

    public static final Item DOUBLE_BARREL_PISTOL = register(com.cursedtides.util.Reg.itemName("double_barrel_pistol"),
            new FlintlockPistolItem(com.cursedtides.util.Reg.props().stacksTo(1).durability(300).rarity(Rarity.UNCOMMON), FlintlockPistolItem.DOUBLE_BARREL));

    public static final Item HAND_CANNON = register(com.cursedtides.util.Reg.itemName("hand_cannon"),
            new FlintlockPistolItem(com.cursedtides.util.Reg.props().stacksTo(1).durability(200).rarity(Rarity.RARE), FlintlockPistolItem.HAND_CANNON));

    public static final Item WANTED_POSTER = register(com.cursedtides.util.Reg.itemName("wanted_poster"), new WantedPosterItem(com.cursedtides.util.Reg.props().stacksTo(16)));

    public static final Item SHIPS_CANNON = register(com.cursedtides.util.Reg.itemName("ships_cannon"),
            new ShipsCannonItem(com.cursedtides.util.Reg.props().stacksTo(1).durability(120).rarity(Rarity.UNCOMMON)));

    public static final Item HEX_STAFF = register(com.cursedtides.util.Reg.itemName("hex_staff"),
            new HexStaffItem(com.cursedtides.util.Reg.props().stacksTo(1).durability(300).rarity(Rarity.RARE)));

    // Ammunition
    public static final Item PISTOL_SHOT = register(com.cursedtides.util.Reg.itemName("pistol_shot"), new Item(com.cursedtides.util.Reg.props()));
    // Special shot: hold it in the off hand and your gun loads it first
    public static final Item VENOM_SHOT = register(com.cursedtides.util.Reg.itemName("venom_shot"), new LoreItem(com.cursedtides.util.Reg.props(), "tooltip.cursedtides.venom_shot"));
    public static final Item HOT_SHOT = register(com.cursedtides.util.Reg.itemName("hot_shot"), new LoreItem(com.cursedtides.util.Reg.props(), "tooltip.cursedtides.hot_shot"));
    public static final Item SILVER_SHOT = register(com.cursedtides.util.Reg.itemName("silver_shot"), new LoreItem(com.cursedtides.util.Reg.props(), "tooltip.cursedtides.silver_shot"));
    public static final Item FIREBRAND_SHOT = register(com.cursedtides.util.Reg.itemName("firebrand_shot"), new LoreItem(com.cursedtides.util.Reg.props().stacksTo(16), "tooltip.cursedtides.firebrand_shot"));
    public static final Item BANE_SHOT = register(com.cursedtides.util.Reg.itemName("bane_shot"), new LoreItem(com.cursedtides.util.Reg.props().stacksTo(16), "tooltip.cursedtides.bane_shot"));
    public static final Item LIGHT_REFIT = register(com.cursedtides.util.Reg.itemName("light_refit"), new LoreItem(com.cursedtides.util.Reg.props().stacksTo(1).rarity(Rarity.UNCOMMON), "tooltip.cursedtides.light_refit"));
    public static final Item WAR_REFIT = register(com.cursedtides.util.Reg.itemName("war_refit"), new LoreItem(com.cursedtides.util.Reg.props().stacksTo(1).rarity(Rarity.UNCOMMON), "tooltip.cursedtides.war_refit"));
    public static final Item FESTIVAL_TOKEN = register(com.cursedtides.util.Reg.itemName("festival_token"), new LoreItem(com.cursedtides.util.Reg.props().stacksTo(64).rarity(Rarity.UNCOMMON), "tooltip.cursedtides.festival_token"));
    public static final Item CANNONBALL = register(com.cursedtides.util.Reg.itemName("cannonball"), new Item(com.cursedtides.util.Reg.props().stacksTo(16)));

    /** Visual only: what a flying hex bolt looks like. Not obtainable. */
    public static final Item HEX_ORB = register(com.cursedtides.util.Reg.itemName("hex_orb"), new Item(com.cursedtides.util.Reg.props()));

    // Treasure
    public static final Item DOUBLOON = register(com.cursedtides.util.Reg.itemName("doubloon"), new Item(com.cursedtides.util.Reg.props().rarity(Rarity.UNCOMMON)));

    // Ships
    public static final Item SLOOP_DEED = register(com.cursedtides.util.Reg.itemName("sloop_deed"),
            new ShipDeedItem(ShipClass.SLOOP, com.cursedtides.util.Reg.props().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final Item BRIGANTINE_DEED = register(com.cursedtides.util.Reg.itemName("brigantine_deed"),
            new ShipDeedItem(ShipClass.BRIGANTINE, com.cursedtides.util.Reg.props().stacksTo(1).rarity(Rarity.RARE)));
    public static final Item GALLEON_DEED = register(com.cursedtides.util.Reg.itemName("galleon_deed"),
            new ShipDeedItem(ShipClass.GALLEON, com.cursedtides.util.Reg.props().stacksTo(1).rarity(Rarity.EPIC)));

    // Ship upgrades and crew
    public static final Item SCHOONER_DEED = register(com.cursedtides.util.Reg.itemName("schooner_deed"),
            new ShipDeedItem(ShipClass.SCHOONER, com.cursedtides.util.Reg.props().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final Item BRIG_DEED = register(com.cursedtides.util.Reg.itemName("brig_deed"),
            new ShipDeedItem(ShipClass.BRIG, com.cursedtides.util.Reg.props().stacksTo(1).rarity(Rarity.RARE)));
    public static final Item FRIGATE_DEED = register(com.cursedtides.util.Reg.itemName("frigate_deed"),
            new ShipDeedItem(ShipClass.FRIGATE, com.cursedtides.util.Reg.props().stacksTo(1).rarity(Rarity.EPIC)));
    public static final Item SHIP_OF_THE_LINE_DEED = register(com.cursedtides.util.Reg.itemName("ship_of_the_line_deed"),
            new ShipDeedItem(ShipClass.SHIP_OF_THE_LINE, com.cursedtides.util.Reg.props().stacksTo(1).rarity(Rarity.EPIC)));
    public static final Item SWIVEL_GUN = register(com.cursedtides.util.Reg.itemName("swivel_gun"),
            new ShipUpgradeItem(ShipUpgradeItem.Kind.SWIVEL, com.cursedtides.util.Reg.props().stacksTo(4)));
    public static final Item HULL_PLATING = register(com.cursedtides.util.Reg.itemName("hull_plating"),
            new ShipUpgradeItem(ShipUpgradeItem.Kind.PLATING, com.cursedtides.util.Reg.props().stacksTo(16)));
    public static final Item GUN_CARRIAGE = register(com.cursedtides.util.Reg.itemName("gun_carriage"),
            new ShipUpgradeItem(ShipUpgradeItem.Kind.GUNS, com.cursedtides.util.Reg.props().stacksTo(16)));
    public static final Item FINE_CANVAS = register(com.cursedtides.util.Reg.itemName("fine_canvas"),
            new ShipUpgradeItem(ShipUpgradeItem.Kind.CANVAS, com.cursedtides.util.Reg.props().stacksTo(16)));
    public static final Item CARGO_BULKHEAD = register(com.cursedtides.util.Reg.itemName("cargo_bulkhead"),
            new ShipUpgradeItem(ShipUpgradeItem.Kind.HOLD, com.cursedtides.util.Reg.props().stacksTo(16)));
    public static final Item GUNNER_ARTICLES = register(com.cursedtides.util.Reg.itemName("gunner_articles"),
            new CrewArticlesItem(0, com.cursedtides.util.Reg.props().stacksTo(16)));
    public static final Item BOSUN_ARTICLES = register(com.cursedtides.util.Reg.itemName("bosun_articles"),
            new CrewArticlesItem(1, com.cursedtides.util.Reg.props().stacksTo(16)));
    public static final Item NAVIGATOR_ARTICLES = register(com.cursedtides.util.Reg.itemName("navigator_articles"),
            new CrewArticlesItem(2, com.cursedtides.util.Reg.props().stacksTo(16)));

    // Special shot
    public static final Item CHAIN_SHOT = register(com.cursedtides.util.Reg.itemName("chain_shot"), new LoreItem(com.cursedtides.util.Reg.props().stacksTo(16),
            "tooltip.cursedtides.chain_shot"));
    public static final Item GRAPESHOT = register(com.cursedtides.util.Reg.itemName("grapeshot"), new LoreItem(com.cursedtides.util.Reg.props().stacksTo(16),
            "tooltip.cursedtides.grapeshot"));
    public static final Item EXPLOSIVE_SHELL = register(com.cursedtides.util.Reg.itemName("explosive_shell"), new LoreItem(com.cursedtides.util.Reg.props().stacksTo(16),
            "tooltip.cursedtides.explosive_shell"));

    // Treasure hunting and dungeons
    public static final Item TREASURE_MAP = register(com.cursedtides.util.Reg.itemName("treasure_map"),
            new TreasureMapItem(com.cursedtides.util.Reg.props().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final Item MAP_FRAGMENT = register(com.cursedtides.util.Reg.itemName("map_fragment"),
            new MapFragmentItem(com.cursedtides.util.Reg.props().stacksTo(16).rarity(Rarity.RARE)));
    public static final Item RUSTED_KEY = register(com.cursedtides.util.Reg.itemName("rusted_key"),
            new LoreItem(com.cursedtides.util.Reg.props().stacksTo(16).rarity(Rarity.UNCOMMON), "tooltip.cursedtides.rusted_key"));

    // Side quests
    public static final Item GOVERNORS_LEDGER = register(com.cursedtides.util.Reg.itemName("governors_ledger"),
            new LoreItem(com.cursedtides.util.Reg.props().stacksTo(1).rarity(Rarity.RARE), "tooltip.cursedtides.governors_ledger"));

    // The Seven Tides: relics and story items
    public static final java.util.List<Item> RELICS = java.util.stream.Stream.of("dawn", "reef", "serpent", "ember", "veil", "sovereign", "lantern")
            .map(n -> register("relic_" + n, new LoreItem(com.cursedtides.util.Reg.props().stacksTo(1).rarity(Rarity.EPIC), "tooltip.cursedtides.relic_" + n)))
            .toList();
    public static final Item FLEET_LOG = register(com.cursedtides.util.Reg.itemName("fleet_log"),
            new com.cursedtides.quest.FleetLogItem(com.cursedtides.util.Reg.props().stacksTo(1).rarity(Rarity.RARE)));
    public static final Item LAMPWRIGHT_SEAL = register(com.cursedtides.util.Reg.itemName("lampwright_seal"),
            new LoreItem(com.cursedtides.util.Reg.props().stacksTo(1).rarity(Rarity.RARE), "tooltip.cursedtides.lampwright_seal"));
    // Act five, and boarding
    public static final Item GRAPPLING_HOOK = register(com.cursedtides.util.Reg.itemName("grappling_hook"),
            new com.cursedtides.ship.Boarding.GrapplingHookItem(com.cursedtides.util.Reg.props().stacksTo(1).durability(64)));
    public static final Item BLACK_PENNANT = register(com.cursedtides.util.Reg.itemName("black_pennant"),
            new LoreItem(com.cursedtides.util.Reg.props().stacksTo(16).rarity(Rarity.UNCOMMON), "tooltip.cursedtides.black_pennant"));
    public static final Item ADMIRALS_COMMISSION = register(com.cursedtides.util.Reg.itemName("admirals_commission"),
            new LoreItem(com.cursedtides.util.Reg.props().stacksTo(1).rarity(Rarity.EPIC), "tooltip.cursedtides.admirals_commission"));
    public static final Item ACCORD_TOKEN = register(com.cursedtides.util.Reg.itemName("accord_token"),
            new LoreItem(com.cursedtides.util.Reg.props().stacksTo(1).rarity(Rarity.UNCOMMON), "tooltip.cursedtides.accord_token"));

    // Coin: ten silver pieces to the doubloon
    public static final Item SILVER_PIECE = register(com.cursedtides.util.Reg.itemName("silver_piece"),
            new LoreItem(com.cursedtides.util.Reg.props().stacksTo(64), "tooltip.cursedtides.silver_piece"));

    // Collectibles and wildlife spoils
    public static final Item ANCIENT_COIN = register(com.cursedtides.util.Reg.itemName("ancient_coin"),
            new LoreItem(com.cursedtides.util.Reg.props().stacksTo(64).rarity(Rarity.UNCOMMON), "tooltip.cursedtides.ancient_coin"));
    public static final Item ANCIENT_RELIC = register(com.cursedtides.util.Reg.itemName("ancient_relic"),
            new LoreItem(com.cursedtides.util.Reg.props().stacksTo(16).rarity(Rarity.RARE), "tooltip.cursedtides.ancient_relic"));
    public static final Item SHARK_TOOTH = register(com.cursedtides.util.Reg.itemName("shark_tooth"),
            new LoreItem(com.cursedtides.util.Reg.props().stacksTo(64), "tooltip.cursedtides.shark_tooth"));

    // Story: "The Drowned Bell"
    public static final Item SEA_CHART = register(com.cursedtides.util.Reg.itemName("sea_chart"),
            new SeaChartItem(com.cursedtides.util.Reg.props().stacksTo(1)));
    public static final Item REEF_SHARD = register(com.cursedtides.util.Reg.itemName("reef_shard"),
            new LoreItem(com.cursedtides.util.Reg.props().stacksTo(1).rarity(Rarity.RARE), "tooltip.cursedtides.reef_shard"));
    public static final Item EMBER_SHARD = register(com.cursedtides.util.Reg.itemName("ember_shard"),
            new LoreItem(com.cursedtides.util.Reg.props().stacksTo(1).rarity(Rarity.RARE).fireResistant(), "tooltip.cursedtides.ember_shard"));
    public static final Item STILLING_BELL = register(com.cursedtides.util.Reg.itemName("stilling_bell"),
            new StillingBellItem(com.cursedtides.util.Reg.props().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));

    // Spawn eggs
    public static final Item CURSED_PIRATE_SPAWN_EGG = register(com.cursedtides.util.Reg.itemName("cursed_pirate_spawn_egg"),
            new SpawnEggItem(com.cursedtides.util.Reg.props().spawnEgg(ModEntities.CURSED_PIRATE)));
    public static final Item QUARTERMASTER_SPAWN_EGG = register(com.cursedtides.util.Reg.itemName("quartermaster_spawn_egg"),
            new SpawnEggItem(com.cursedtides.util.Reg.props().spawnEgg(ModEntities.QUARTERMASTER)));
    public static final Item DROWNED_CAPTAIN_SPAWN_EGG = register(com.cursedtides.util.Reg.itemName("drowned_captain_spawn_egg"),
            new SpawnEggItem(com.cursedtides.util.Reg.props().spawnEgg(ModEntities.DROWNED_CAPTAIN)));

    public static final CreativeModeTab GROUP = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CursedTides.id("main"),
            FabricCreativeModeTab.builder()
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
        return Registry.register(BuiltInRegistries.ITEM, com.cursedtides.util.Reg.itemId(name), item);
    }

    /** Called from the mod initializer to force class loading. */
    public static void register() {
        ModClothing.all();
        CursedTides.LOGGER.debug("Registered Cursed Tides items");
    }
}
