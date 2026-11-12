package com.cursedtides.entity;

import com.cursedtides.item.ModItems;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.LookAtCustomerGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.StopFollowingCustomerGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradedItem;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

/**
 * A port merchant who deals in doubloons. Sells weapons and ammunition and buys
 * gold and plunder. Never despawns and never restocks out.
 */
public class QuartermasterEntity extends WanderingTraderEntity {
    private static final int UNLIMITED = 9999;

    public QuartermasterEntity(EntityType<? extends WanderingTraderEntity> type, World world) {
        super(type, world);
    }

    /** Same as a wandering trader, minus the invisibility-potion habit. */
    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new StopFollowingCustomerGoal(this));
        this.goalSelector.add(1, new FleeEntityGoal<>(this, CursedPirateEntity.class, 8.0f, 0.5, 0.5));
        this.goalSelector.add(1, new EscapeDangerGoal(this, 0.5));
        this.goalSelector.add(1, new LookAtCustomerGoal(this));
        this.goalSelector.add(8, new WanderAroundFarGoal(this, 0.35));
        this.goalSelector.add(9, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
    }

    /** Specialist vendors are told apart by their trade, which is part of their name. */
    private String trade() {
        String n = this.hasCustomName() && this.getCustomName() != null ? this.getCustomName().getString().toLowerCase() : "";
        for (String t : new String[]{"clothier", "gunsmith", "armorer", "fishmonger", "cook", "salvager", "antiquarian",
                "fence", "assayer", "chandler", "apothecary", "smuggler", "merchant", "trader", "purser", "peddler"}) {
            if (n.contains(t)) return t;
        }
        return "";
    }

    public String tradeName() {
        return trade();
    }

    /** Merchants, traders, pursers and the black market deal in cargo: click for the market, sneak-click for the shop. */
    public boolean isMarket() {
        String t = trade();
        return t.equals("merchant") || t.equals("trader") || t.equals("purser") || t.equals("fence") || t.equals("smuggler");
    }

    /** Which conversation this shopkeeper has: by trade, or the general one. */
    public String dialogueFile() {
        String t = trade();
        return t.isEmpty() ? "vendor_general" : "vendor_" + t;
    }

    /** Opens the ordinary shop window. */
    public void openShop(PlayerEntity player) {
        if (this.getOffers().isEmpty()) return;
        if (!com.cursedtides.living.Hours.isOpen(this)) {
            player.sendMessage(net.minecraft.text.Text.literal("<" + this.getDisplayName().getString() + "> " + com.cursedtides.living.Hours.closedLine(this))
                    .formatted(net.minecraft.util.Formatting.GRAY), false);
            return;
        }
        this.setCustomer(player);
        this.sendOffers(player, this.getDisplayName(), 1);
    }

    @Override
    public net.minecraft.util.ActionResult interactMob(PlayerEntity player, net.minecraft.util.Hand hand) {
        // Click to talk; sneak-click goes straight to business
        if (hand == net.minecraft.util.Hand.MAIN_HAND && !player.isSneaking() && this.isAlive() && !this.hasCustomer()) {
            if (player instanceof net.minecraft.server.network.ServerPlayerEntity sp
                    && com.cursedtides.dialogue.Dialogue.open(sp, this, dialogueFile())) {
                return net.minecraft.util.ActionResult.SUCCESS;
            }
        }
        if (isMarket() && hand == net.minecraft.util.Hand.MAIN_HAND && this.isAlive()) {
            if (player instanceof net.minecraft.server.network.ServerPlayerEntity sp && !com.cursedtides.living.Hours.isOpen(this)) {
                openShop(sp); // just says it's shut
            } else if (player instanceof net.minecraft.server.network.ServerPlayerEntity sp) {
                com.cursedtides.trade.Markets.open(sp, this);
            }
            return net.minecraft.util.ActionResult.success(this.getWorld().isClient);
        }
        return super.interactMob(player, hand);
    }

    @Override
    protected void fillRecipes() {
        TradeOfferList offers = this.getOffers();
        switch (trade()) {
            case "clothier" -> {
                for (Item piece : com.cursedtides.item.ModClothing.SAILOR_SET) offers.add(sell(3, piece, 1));
                for (Item piece : com.cursedtides.item.ModClothing.BAYOU_SET) offers.add(sell(5, piece, 1));
                for (Item piece : com.cursedtides.item.ModClothing.CAPTAIN_SET) offers.add(sell(18, piece, 1));
                offers.add(sell(1, Items.WHITE_DYE, 2));
                offers.add(sell(1, Items.RED_DYE, 2));
                offers.add(sell(1, Items.BLACK_DYE, 2));
                offers.add(buy(Items.WHITE_WOOL, 8, 1));
                offers.add(buy(Items.LEATHER, 6, 1));
                offers.add(sell(4, com.cursedtides.appearance.Accessories.RED_BANDANA, 1));
                offers.add(sell(4, com.cursedtides.appearance.Accessories.BLACK_BANDANA, 1));
                offers.add(sell(6, com.cursedtides.appearance.Accessories.LEATHER_GLOVES, 1));
                offers.add(sell(10, com.cursedtides.appearance.Accessories.CAPTAINS_BELT, 1));
                offers.add(sell(8, com.cursedtides.appearance.Accessories.EYEPATCH, 1));
                return;
            }
            case "gunsmith" -> {
                offers.add(sell(16, ModItems.FLINTLOCK_PISTOL, 1));
                offers.add(sell(28, ModItems.DOUBLE_BARREL_PISTOL, 1));
                offers.add(sell(45, ModItems.HAND_CANNON, 1));
                offers.add(sell(28, ModItems.MUSKET, 1));
                offers.add(sell(34, ModItems.BLUNDERBUSS, 1));
                offers.add(sell(3, ModItems.PISTOL_SHOT, 8));
                offers.add(sell(4, Items.GUNPOWDER, 4));
                offers.add(sell(6, com.cursedtides.profession.ProfessionItems.GUNSMITH_KIT, 1));
                offers.add(buy(Items.IRON_INGOT, 4, 1));
                offers.add(buy(Items.GUNPOWDER, 8, 1));
                return;
            }
            case "armorer" -> {
                offers.add(sell(9, ModItems.CUTLASS, 1));
                offers.add(sell(13, ModItems.RAPIER, 1));
                offers.add(sell(7, ModItems.DAGGER, 1));
                offers.add(sell(15, ModItems.BOARDING_AXE, 1));
                offers.add(sell(14, ModItems.SABER, 1));
                offers.add(sell(18, ModItems.DUAL_BLADES, 1));
                offers.add(sell(24, ModItems.BROADSWORD, 1));
                offers.add(sell(26, ModItems.WAR_HAMMER, 1));
                offers.add(sell(4, ModItems.THROWING_KNIFE, 6));
                offers.add(sell(14, Items.SHIELD, 1));
                offers.add(sell(20, Items.CHAINMAIL_CHESTPLATE, 1));
                offers.add(sell(3, com.cursedtides.profession.ProfessionItems.WHETSTONE, 2));
                offers.add(sell(6, com.cursedtides.profession.ProfessionItems.REINFORCING_KIT, 1));
                offers.add(buy(Items.IRON_INGOT, 3, 1));
                return;
            }
            case "fishmonger", "cook" -> {
                offers.add(sell(1, Items.COOKED_COD, 4));
                offers.add(sell(1, Items.COOKED_SALMON, 3));
                offers.add(sell(2, Items.BREAD, 6));
                offers.add(sell(3, Items.PUMPKIN_PIE, 2));
                offers.add(sell(2, Items.FISHING_ROD, 1));
                offers.add(buy(Items.COD, 6, 1));
                offers.add(buy(Items.SALMON, 5, 1));
                offers.add(buy(Items.TROPICAL_FISH, 2, 1));
                offers.add(buy(Items.PUFFERFISH, 1, 2));
                offers.add(buy(ModItems.SHARK_TOOTH, 2, 3));
                offers.add(buy(com.cursedtides.profession.ProfessionItems.SILVERFIN_SNAPPER, 3, 1));
                offers.add(buy(com.cursedtides.profession.ProfessionItems.STORM_SWORDFISH, 1, 4));
                offers.add(buy(com.cursedtides.profession.ProfessionItems.GOLDEN_MARLIN, 1, 30));
                offers.add(sell(3, com.cursedtides.profession.ProfessionItems.FISH_STEW, 2));
                offers.add(sell(2, com.cursedtides.profession.ProfessionItems.HARDTACK, 4));
                return;
            }
            case "salvager" -> {
                offers.add(sell(10, ModItems.HULL_PLATING, 1));
                offers.add(sell(10, ModItems.FINE_CANVAS, 1));
                offers.add(sell(6, Items.SPYGLASS, 1));
                offers.add(potion(6, net.minecraft.potion.Potions.LONG_WATER_BREATHING));
                offers.add(buy(Items.IRON_NUGGET, 18, 1));
                offers.add(buy(Items.CHAIN, 4, 1));
                offers.add(buy(Items.NAUTILUS_SHELL, 1, 3));
                offers.add(buy(Items.HEART_OF_THE_SEA, 1, 26));
                return;
            }
            case "peddler" -> {
                net.minecraft.util.math.random.Random r = this.getRandom();
                for (int i = 0; i < 4; i++) offers.add(sell(10 + r.nextInt(16), com.cursedtides.appearance.Accessories.random(r, true), 1));
                offers.add(sell(30, com.cursedtides.ship.FlagItem.randomRare(r), 1));
                offers.add(sell(14, ModItems.TREASURE_MAP, 1));
                offers.add(sell(18, ModItems.DUAL_BLADES, 1));
                offers.add(sell(40, ModItems.HAND_CANNON, 1));
                offers.add(buy(ModItems.ANCIENT_COIN, 1, 4));
                offers.add(buy(ModItems.SHARK_TOOTH, 3, 1));
                return;
            }
            case "antiquarian" -> {
                offers.add(buy(ModItems.ANCIENT_COIN, 1, 3));
                offers.add(buy(ModItems.ANCIENT_RELIC, 1, 18));
                offers.add(buy(Items.GOLD_INGOT, 2, 1));
                offers.add(sell(12, ModItems.TREASURE_MAP, 1));
                offers.add(sell(20, ModItems.MAP_FRAGMENT, 1));
                offers.add(sell(4, Items.COMPASS, 1));
                offers.add(sell(14, com.cursedtides.appearance.Accessories.GOLD_EARRING, 1));
                offers.add(sell(16, com.cursedtides.appearance.Accessories.SILVER_RING, 1));
                offers.add(sell(30, com.cursedtides.appearance.Accessories.COMPASS_PENDANT, 1));
                return;
            }
            case "fence", "smuggler" -> {
                // The black market: better prices for plunder, and no questions about where it came from
                offers.add(buy(Items.GOLD_INGOT, 1, 1));
                offers.add(buy(Items.EMERALD, 1, 3));
                offers.add(buy(Items.DIAMOND, 1, 8));
                offers.add(buy(Items.HEART_OF_THE_SEA, 1, 30));
                offers.add(buy(ModItems.ANCIENT_RELIC, 1, 22));
                offers.add(sell(8, ModItems.EXPLOSIVE_SHELL, 2));
                offers.add(sell(10, ModItems.TREASURE_MAP, 1));
                offers.add(sell(5, Items.TNT, 1));
                offers.add(sell(14, ModItems.BLUNDERBUSS, 1));
                return;
            }
            case "assayer" -> {
                offers.add(buy(Items.RAW_GOLD, 2, 1));
                offers.add(buy(Items.GOLD_INGOT, 2, 1));
                offers.add(buy(Items.GOLD_BLOCK, 1, 5));
                offers.add(buy(Items.RAW_COPPER, 16, 1));
                offers.add(sell(4, Items.IRON_PICKAXE, 1));
                offers.add(sell(2, Items.TORCH, 16));
                return;
            }
            case "chandler" -> {
                offers.add(sell(5, ModItems.CANNONBALL, 4));
                offers.add(sell(7, ModItems.CHAIN_SHOT, 3));
                offers.add(sell(7, ModItems.GRAPESHOT, 3));
                offers.add(sell(12, ModItems.EXPLOSIVE_SHELL, 2));
                offers.add(sell(10, ModItems.HULL_PLATING, 1));
                offers.add(sell(10, ModItems.FINE_CANVAS, 1));
                offers.add(sell(2, Items.OAK_PLANKS, 16));
                offers.add(sell(3, Items.LEAD, 2));
                return;
            }
            case "apothecary" -> {
                offers.add(potion(4, net.minecraft.potion.Potions.HEALING));
                offers.add(potion(6, net.minecraft.potion.Potions.REGENERATION));
                offers.add(potion(6, net.minecraft.potion.Potions.LONG_WATER_BREATHING));
                offers.add(potion(5, net.minecraft.potion.Potions.FIRE_RESISTANCE));
                offers.add(potion(5, net.minecraft.potion.Potions.SWIFTNESS));
                offers.add(sell(2, Items.GLISTERING_MELON_SLICE, 1));
                offers.add(sell(5, com.cursedtides.profession.ProfessionItems.SEA_LEGS_TONIC, 1));
                offers.add(sell(4, com.cursedtides.profession.ProfessionItems.LANTERN_TONIC, 1));
                offers.add(buy(Items.NETHER_WART, 4, 1));
                offers.add(buy(Items.BLUE_ORCHID, 4, 1));
                return;
            }
            default -> { }
        }
        // Gear for doubloons
        offers.add(sell(4, ModItems.PISTOL_SHOT, 6));
        offers.add(sell(6, ModItems.CANNONBALL, 4));
        offers.add(sell(10, ModItems.CUTLASS, 1));
        offers.add(sell(18, ModItems.FLINTLOCK_PISTOL, 1));
        offers.add(sell(30, ModItems.HEX_STAFF, 1));
        offers.add(sell(40, ModItems.SHIPS_CANNON, 1));
        offers.add(sell(3, Items.OAK_BOAT, 1));
        offers.add(sell(5, Items.COOKED_COD, 8));
        offers.add(sell(12, ModItems.TREASURE_MAP, 1));
        // For the flooded dungeons of the Sunken Reach and Hollowgrave
        offers.add(new TradeOffer(new TradedItem(ModItems.DOUBLOON, 6),
                net.minecraft.component.type.PotionContentsComponent.createStack(Items.POTION,
                        net.minecraft.potion.Potions.LONG_WATER_BREATHING), UNLIMITED, 2, 0.0f));
        offers.add(sell(10, Items.TURTLE_HELMET, 1));
        // Clothes and blades
        for (Item piece : com.cursedtides.item.ModClothing.SAILOR_SET) offers.add(sell(4, piece, 1));
        for (Item piece : com.cursedtides.item.ModClothing.BAYOU_SET) offers.add(sell(6, piece, 1));
        offers.add(sell(14, ModItems.RAPIER, 1));
        offers.add(sell(8, ModItems.DAGGER, 1));
        offers.add(sell(16, ModItems.BOARDING_AXE, 1));
        offers.add(sell(5, ModItems.THROWING_KNIFE, 6));
        // Plunder for doubloons
        offers.add(buy(Items.GOLD_INGOT, 2, 1));
        offers.add(buy(Items.EMERALD, 1, 2));
        offers.add(buy(Items.BONE, 16, 1));
        offers.add(buy(Items.HEART_OF_THE_SEA, 1, 24));
    }

    private static TradeOffer potion(int doubloons, net.minecraft.registry.entry.RegistryEntry<net.minecraft.potion.Potion> potion) {
        return new TradeOffer(new TradedItem(ModItems.DOUBLOON, doubloons),
                net.minecraft.component.type.PotionContentsComponent.createStack(Items.POTION, potion), UNLIMITED, 2, 0.0f);
    }

    private static TradeOffer sell(int doubloons, Item item, int count) {
        return new TradeOffer(new TradedItem(ModItems.DOUBLOON, doubloons), new ItemStack(item, count), UNLIMITED, 2, 0.0f);
    }

    private static TradeOffer buy(Item item, int count, int doubloons) {
        return new TradeOffer(new TradedItem(item, count), new ItemStack(ModItems.DOUBLOON, doubloons), UNLIMITED, 2, 0.0f);
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason,
                                 @Nullable EntityData entityData) {
        this.setPersistent();
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    /** Natural spawns only on dry, solid ground. */
    @Override
    public boolean canSpawn(WorldAccess world, SpawnReason reason) {
        if (reason != SpawnReason.NATURAL && reason != SpawnReason.CHUNK_GENERATION) {
            return super.canSpawn(world, reason);
        }
        BlockPos pos = this.getBlockPos();
        BlockPos below = pos.down();
        return world.getFluidState(pos).isEmpty()
                && world.getBlockState(below).isSolidBlock(world, below);
    }
}
