package com.cursedtides.item;

import com.cursedtides.CursedTides;
import com.cursedtides.entity.ModEntities;
import com.cursedtides.ship.ShipClass;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;

public final class ModItems {
    private ModItems() {}

    // Weapons
    public static final Item CUTLASS = register("cutlass",
            new CutlassItem(ToolMaterials.IRON, new Item.Settings()
                    .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.IRON, 3, -2.2f))));

    public static final Item FLINTLOCK_PISTOL = register("flintlock_pistol",
            new FlintlockPistolItem(new Item.Settings().maxCount(1).maxDamage(250)));

    public static final Item RAPIER = register("rapier",
            new ParryingBladeItem(ToolMaterials.IRON, 14, new Item.Settings()
                    .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.IRON, 2, -1.8f))));

    public static final Item DAGGER = register("dagger",
            new DaggerItem(ToolMaterials.IRON, new Item.Settings()
                    .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.IRON, 1, -1.2f))));

    public static final Item BOARDING_AXE = register("boarding_axe",
            new BoardingAxeItem(ToolMaterials.IRON, new Item.Settings()
                    .attributeModifiers(net.minecraft.item.AxeItem.createAttributeModifiers(ToolMaterials.IRON, 7.0f, -3.2f))));

    public static final Item THROWING_KNIFE = register("throwing_knife",
            new ThrowingKnifeItem(new Item.Settings().maxCount(16)));

    public static final Item MUSKET = register("musket",
            new FlintlockPistolItem(new Item.Settings().maxCount(1).maxDamage(400).rarity(Rarity.RARE), FlintlockPistolItem.MUSKET));

    public static final Item BLUNDERBUSS = register("blunderbuss",
            new FlintlockPistolItem(new Item.Settings().maxCount(1).maxDamage(300).rarity(Rarity.RARE), FlintlockPistolItem.BLUNDERBUSS));

    public static final Item SABER = register("saber",
            new CutlassItem(ToolMaterials.IRON, new Item.Settings().rarity(Rarity.UNCOMMON)
                    .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.IRON, 3, -1.9f))));

    public static final Item DUAL_BLADES = register("dual_blades",
            new DualBladesItem(ToolMaterials.IRON, new Item.Settings().rarity(Rarity.UNCOMMON)
                    .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.IRON, 1, -1.0f))));

    public static final Item BROADSWORD = register("broadsword",
            new HeavyWeaponItem(ToolMaterials.IRON, false, new Item.Settings().rarity(Rarity.UNCOMMON)
                    .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.IRON, 7, -3.0f))));

    public static final Item WAR_HAMMER = register("war_hammer",
            new HeavyWeaponItem(ToolMaterials.IRON, true, new Item.Settings().rarity(Rarity.UNCOMMON)
                    .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.IRON, 8, -3.3f))));

    public static final Item REPEATER_PISTOL = register("repeater_pistol",
            new FlintlockPistolItem(new Item.Settings().maxCount(1).maxDamage(350).rarity(Rarity.RARE), FlintlockPistolItem.REPEATER));
    public static final Item BAYONET_MUSKET = register("bayonet_musket",
            new FlintlockPistolItem(new Item.Settings().maxCount(1).maxDamage(450).rarity(Rarity.RARE)
                    .attributeModifiers(net.minecraft.component.type.AttributeModifiersComponent.builder()
                            .add(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_DAMAGE,
                                    new net.minecraft.entity.attribute.EntityAttributeModifier(com.cursedtides.CursedTides.id("bayonet_damage"), 6.0,
                                            net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE),
                                    net.minecraft.component.type.AttributeModifierSlot.MAINHAND)
                            .add(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_SPEED,
                                    new net.minecraft.entity.attribute.EntityAttributeModifier(com.cursedtides.CursedTides.id("bayonet_speed"), -2.9,
                                            net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE),
                                    net.minecraft.component.type.AttributeModifierSlot.MAINHAND)
                            .build()), FlintlockPistolItem.BAYONET));

    public static final Item DOUBLE_BARREL_PISTOL = register("double_barrel_pistol",
            new FlintlockPistolItem(new Item.Settings().maxCount(1).maxDamage(300).rarity(Rarity.UNCOMMON), FlintlockPistolItem.DOUBLE_BARREL));

    public static final Item HAND_CANNON = register("hand_cannon",
            new FlintlockPistolItem(new Item.Settings().maxCount(1).maxDamage(200).rarity(Rarity.RARE), FlintlockPistolItem.HAND_CANNON));

    public static final Item WANTED_POSTER = register("wanted_poster", new WantedPosterItem(new Item.Settings().maxCount(16)));

    public static final Item SHIPS_CANNON = register("ships_cannon",
            new ShipsCannonItem(new Item.Settings().maxCount(1).maxDamage(120).rarity(Rarity.UNCOMMON)));

    public static final Item HEX_STAFF = register("hex_staff",
            new HexStaffItem(new Item.Settings().maxCount(1).maxDamage(300).rarity(Rarity.RARE)));

    // Ammunition
    public static final Item PISTOL_SHOT = register("pistol_shot", new Item(new Item.Settings()));
    public static final Item CANNONBALL = register("cannonball", new Item(new Item.Settings().maxCount(16)));

    /** Visual only: what a flying hex bolt looks like. Not obtainable. */
    public static final Item HEX_ORB = register("hex_orb", new Item(new Item.Settings()));

    // Treasure
    public static final Item DOUBLOON = register("doubloon", new Item(new Item.Settings().rarity(Rarity.UNCOMMON)));

    // Ships
    public static final Item SLOOP_DEED = register("sloop_deed",
            new ShipDeedItem(ShipClass.SLOOP, new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON)));
    public static final Item BRIGANTINE_DEED = register("brigantine_deed",
            new ShipDeedItem(ShipClass.BRIGANTINE, new Item.Settings().maxCount(1).rarity(Rarity.RARE)));
    public static final Item GALLEON_DEED = register("galleon_deed",
            new ShipDeedItem(ShipClass.GALLEON, new Item.Settings().maxCount(1).rarity(Rarity.EPIC)));

    // Ship upgrades and crew
    public static final Item SCHOONER_DEED = register("schooner_deed",
            new ShipDeedItem(ShipClass.SCHOONER, new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON)));
    public static final Item BRIG_DEED = register("brig_deed",
            new ShipDeedItem(ShipClass.BRIG, new Item.Settings().maxCount(1).rarity(Rarity.RARE)));
    public static final Item FRIGATE_DEED = register("frigate_deed",
            new ShipDeedItem(ShipClass.FRIGATE, new Item.Settings().maxCount(1).rarity(Rarity.EPIC)));
    public static final Item SHIP_OF_THE_LINE_DEED = register("ship_of_the_line_deed",
            new ShipDeedItem(ShipClass.SHIP_OF_THE_LINE, new Item.Settings().maxCount(1).rarity(Rarity.EPIC)));
    public static final Item SWIVEL_GUN = register("swivel_gun",
            new ShipUpgradeItem(ShipUpgradeItem.Kind.SWIVEL, new Item.Settings().maxCount(4)));
    public static final Item HULL_PLATING = register("hull_plating",
            new ShipUpgradeItem(ShipUpgradeItem.Kind.PLATING, new Item.Settings().maxCount(16)));
    public static final Item GUN_CARRIAGE = register("gun_carriage",
            new ShipUpgradeItem(ShipUpgradeItem.Kind.GUNS, new Item.Settings().maxCount(16)));
    public static final Item FINE_CANVAS = register("fine_canvas",
            new ShipUpgradeItem(ShipUpgradeItem.Kind.CANVAS, new Item.Settings().maxCount(16)));
    public static final Item CARGO_BULKHEAD = register("cargo_bulkhead",
            new ShipUpgradeItem(ShipUpgradeItem.Kind.HOLD, new Item.Settings().maxCount(16)));
    public static final Item GUNNER_ARTICLES = register("gunner_articles",
            new CrewArticlesItem(0, new Item.Settings().maxCount(16)));
    public static final Item BOSUN_ARTICLES = register("bosun_articles",
            new CrewArticlesItem(1, new Item.Settings().maxCount(16)));
    public static final Item NAVIGATOR_ARTICLES = register("navigator_articles",
            new CrewArticlesItem(2, new Item.Settings().maxCount(16)));

    // Special shot
    public static final Item CHAIN_SHOT = register("chain_shot", new LoreItem(new Item.Settings().maxCount(16),
            "tooltip.cursedtides.chain_shot"));
    public static final Item GRAPESHOT = register("grapeshot", new LoreItem(new Item.Settings().maxCount(16),
            "tooltip.cursedtides.grapeshot"));
    public static final Item EXPLOSIVE_SHELL = register("explosive_shell", new LoreItem(new Item.Settings().maxCount(16),
            "tooltip.cursedtides.explosive_shell"));

    // Treasure hunting and dungeons
    public static final Item TREASURE_MAP = register("treasure_map",
            new TreasureMapItem(new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON)));
    public static final Item MAP_FRAGMENT = register("map_fragment",
            new MapFragmentItem(new Item.Settings().maxCount(16).rarity(Rarity.RARE)));
    public static final Item RUSTED_KEY = register("rusted_key",
            new LoreItem(new Item.Settings().maxCount(16).rarity(Rarity.UNCOMMON), "tooltip.cursedtides.rusted_key"));

    // Side quests
    public static final Item GOVERNORS_LEDGER = register("governors_ledger",
            new LoreItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE), "tooltip.cursedtides.governors_ledger"));

    // The Seven Tides: relics and story items
    public static final java.util.List<Item> RELICS = java.util.stream.Stream.of("dawn", "reef", "serpent", "ember", "veil", "sovereign", "lantern")
            .map(n -> register("relic_" + n, new LoreItem(new Item.Settings().maxCount(1).rarity(Rarity.EPIC), "tooltip.cursedtides.relic_" + n)))
            .toList();
    public static final Item FLEET_LOG = register("fleet_log",
            new com.cursedtides.quest.FleetLogItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE)));
    public static final Item LAMPWRIGHT_SEAL = register("lampwright_seal",
            new LoreItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE), "tooltip.cursedtides.lampwright_seal"));
    public static final Item ACCORD_TOKEN = register("accord_token",
            new LoreItem(new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON), "tooltip.cursedtides.accord_token"));

    // Coin: ten silver pieces to the doubloon
    public static final Item SILVER_PIECE = register("silver_piece",
            new LoreItem(new Item.Settings().maxCount(64), "tooltip.cursedtides.silver_piece"));

    // Collectibles and wildlife spoils
    public static final Item ANCIENT_COIN = register("ancient_coin",
            new LoreItem(new Item.Settings().maxCount(64).rarity(Rarity.UNCOMMON), "tooltip.cursedtides.ancient_coin"));
    public static final Item ANCIENT_RELIC = register("ancient_relic",
            new LoreItem(new Item.Settings().maxCount(16).rarity(Rarity.RARE), "tooltip.cursedtides.ancient_relic"));
    public static final Item SHARK_TOOTH = register("shark_tooth",
            new LoreItem(new Item.Settings().maxCount(64), "tooltip.cursedtides.shark_tooth"));

    // Story: "The Drowned Bell"
    public static final Item SEA_CHART = register("sea_chart",
            new SeaChartItem(new Item.Settings().maxCount(1)));
    public static final Item REEF_SHARD = register("reef_shard",
            new LoreItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE), "tooltip.cursedtides.reef_shard"));
    public static final Item EMBER_SHARD = register("ember_shard",
            new LoreItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE).fireproof(), "tooltip.cursedtides.ember_shard"));
    public static final Item STILLING_BELL = register("stilling_bell",
            new StillingBellItem(new Item.Settings().maxCount(1).rarity(Rarity.EPIC).fireproof()));

    // Spawn eggs
    public static final Item CURSED_PIRATE_SPAWN_EGG = register("cursed_pirate_spawn_egg",
            new SpawnEggItem(ModEntities.CURSED_PIRATE, 0xD8D2B8, 0x1F7A6E, new Item.Settings()));
    public static final Item QUARTERMASTER_SPAWN_EGG = register("quartermaster_spawn_egg",
            new SpawnEggItem(ModEntities.QUARTERMASTER, 0x1E2C4A, 0xD4A93C, new Item.Settings()));
    public static final Item DROWNED_CAPTAIN_SPAWN_EGG = register("drowned_captain_spawn_egg",
            new SpawnEggItem(ModEntities.DROWNED_CAPTAIN, 0x1B3B3A, 0x4FE3C8, new Item.Settings()));

    public static final ItemGroup GROUP = Registry.register(Registries.ITEM_GROUP, CursedTides.id("main"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(CUTLASS))
                    .displayName(Text.translatable("itemGroup.cursedtides.main"))
                    .entries((context, entries) -> {
                        entries.add(CUTLASS);
                        entries.add(FLINTLOCK_PISTOL);
                        entries.add(SABER);
                        entries.add(DUAL_BLADES);
                        entries.add(BROADSWORD);
                        entries.add(WAR_HAMMER);
                        entries.add(DOUBLE_BARREL_PISTOL);
                        entries.add(REPEATER_PISTOL);
                        entries.add(BAYONET_MUSKET);
                        entries.add(HAND_CANNON);
                        entries.add(WANTED_POSTER);
                        entries.add(MUSKET);
                        entries.add(BLUNDERBUSS);
                        entries.add(PISTOL_SHOT);
                        entries.add(SHIPS_CANNON);
                        entries.add(CANNONBALL);
                        entries.add(HEX_STAFF);
                        entries.add(DOUBLOON);
                        entries.add(SLOOP_DEED);
                        entries.add(BRIGANTINE_DEED);
                        entries.add(GALLEON_DEED);
                        entries.add(SCHOONER_DEED);
                        entries.add(BRIG_DEED);
                        entries.add(FRIGATE_DEED);
                        entries.add(SHIP_OF_THE_LINE_DEED);
                        entries.add(SWIVEL_GUN);
                        entries.add(HULL_PLATING);
                        entries.add(GUN_CARRIAGE);
                        entries.add(FINE_CANVAS);
                        entries.add(CARGO_BULKHEAD);
                        entries.add(GUNNER_ARTICLES);
                        entries.add(BOSUN_ARTICLES);
                        entries.add(NAVIGATOR_ARTICLES);
                        entries.add(CHAIN_SHOT);
                        entries.add(GRAPESHOT);
                        entries.add(EXPLOSIVE_SHELL);
                        entries.add(TREASURE_MAP);
                        entries.add(MAP_FRAGMENT);
                        entries.add(RUSTED_KEY);
                        ModClothing.all().forEach(entries::add);
                        entries.add(RAPIER);
                        entries.add(DAGGER);
                        entries.add(BOARDING_AXE);
                        entries.add(THROWING_KNIFE);
                        entries.add(SEA_CHART);
                        entries.add(REEF_SHARD);
                        entries.add(EMBER_SHARD);
                        entries.add(STILLING_BELL);
                        entries.add(CURSED_PIRATE_SPAWN_EGG);
                        entries.add(QUARTERMASTER_SPAWN_EGG);
                        entries.add(DROWNED_CAPTAIN_SPAWN_EGG);
                        entries.add(ANCIENT_COIN);
                        entries.add(ANCIENT_RELIC);
                        entries.add(SHARK_TOOTH);
                        entries.add(SILVER_PIECE);
                        RELICS.forEach(entries::add);
                        entries.add(FLEET_LOG);
                        entries.add(LAMPWRIGHT_SEAL);
                        entries.add(ACCORD_TOKEN);
                    })
                    .build());

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, CursedTides.id(name), item);
    }

    /** Called from the mod initializer to force class loading. */
    public static void register() {
        ModClothing.all();
        CursedTides.LOGGER.debug("Registered Cursed Tides items");
    }
}
