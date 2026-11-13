package com.cursedtides.entity;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import com.cursedtides.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

/**
 * A port merchant who deals in doubloons. Sells weapons and ammunition and buys
 * gold and plunder. Never despawns and never restocks out.
 */
public class QuartermasterEntity extends WanderingTrader {
    private static final int UNLIMITED = 9999;

    public QuartermasterEntity(EntityType<? extends WanderingTrader> type, Level world) {
        super(type, world);
    }

    /** Same as a wandering trader, minus the invisibility-potion habit. */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, CursedPirateEntity.class, 8.0f, 0.5, 0.5));
        this.goalSelector.addGoal(1, new PanicGoal(this, 0.5));
        this.goalSelector.addGoal(1, new LookAtTradingPlayerGoal(this));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.35));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 6.0f));
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
    public void openShop(Player player) {
        if (this.getOffers().isEmpty()) return;
        if (!com.cursedtides.living.Hours.isOpen(this)) {
            com.cursedtides.util.Compat.tell(player, net.minecraft.network.chat.Component.literal("<" + this.getDisplayName().getString() + "> " + com.cursedtides.living.Hours.closedLine(this))
                    .withStyle(net.minecraft.ChatFormatting.GRAY), false);
            return;
        }
        this.setTradingPlayer(player);
        this.openTradingScreen(player, this.getDisplayName(), 1);
    }

    @Override
    public net.minecraft.world.InteractionResult mobInteract(Player player, net.minecraft.world.InteractionHand hand) {
        // Click to talk; sneak-click goes straight to business
        if (hand == net.minecraft.world.InteractionHand.MAIN_HAND && !player.isShiftKeyDown() && this.isAlive() && !this.isTrading()) {
            if (player instanceof net.minecraft.server.level.ServerPlayer sp
                    && com.cursedtides.dialogue.Dialogue.open(sp, this, dialogueFile())) {
                return net.minecraft.world.InteractionResult.SUCCESS;
            }
        }
        if (isMarket() && hand == net.minecraft.world.InteractionHand.MAIN_HAND && this.isAlive()) {
            if (player instanceof net.minecraft.server.level.ServerPlayer sp && !com.cursedtides.living.Hours.isOpen(this)) {
                openShop(sp); // just says it's shut
            } else if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
                com.cursedtides.trade.Markets.open(sp, this);
            }
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected void updateTrades(net.minecraft.server.level.ServerLevel level__) {
        MerchantOffers offers = this.getOffers();
        switch (trade()) {
            case "clothier" -> {
                for (Item piece : com.cursedtides.item.ModClothing.SAILOR_SET) offers.add(sell(3, piece, 1));
                for (Item piece : com.cursedtides.item.ModClothing.BAYOU_SET) offers.add(sell(5, piece, 1));
                for (Item piece : com.cursedtides.item.ModClothing.CAPTAIN_SET) offers.add(sell(18, piece, 1));
                offers.add(sell(1, Items.DYE.pick(DyeColor.WHITE), 2));
                offers.add(sell(1, Items.DYE.pick(DyeColor.RED), 2));
                offers.add(sell(1, Items.DYE.pick(DyeColor.BLACK), 2));
                offers.add(buy(Items.WOOL.pick(DyeColor.WHITE), 8, 1));
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
                offers.add(sell(3, com.cursedtides.combat.Grenades.FIREBOMB, 2));
                offers.add(sell(3, com.cursedtides.combat.Grenades.STINKPOT, 2));
                offers.add(sell(3, com.cursedtides.combat.Grenades.SMOKE_BOMB, 2));
                offers.add(sell(6, com.cursedtides.combat.Grenades.BLAST_CHARGE, 2));
                offers.add(sell(45, ModItems.HAND_CANNON, 1));
                offers.add(sell(28, ModItems.MUSKET, 1));
                offers.add(sell(44, ModItems.REPEATER_PISTOL, 1));
                offers.add(sell(40, ModItems.BAYONET_MUSKET, 1));
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
                offers.add(potion(6, net.minecraft.world.item.alchemy.Potions.LONG_WATER_BREATHING));
                offers.add(buy(Items.IRON_NUGGET, 18, 1));
                offers.add(buy(Items.IRON_CHAIN, 4, 1));
                offers.add(buy(Items.NAUTILUS_SHELL, 1, 3));
                offers.add(buy(Items.HEART_OF_THE_SEA, 1, 26));
                return;
            }
            case "peddler" -> {
                offers.add(sell(12, net.minecraft.world.item.Items.PARROT_SPAWN_EGG, 1));
                offers.add(sell(10, com.cursedtides.activity.MiniGames.BREWERS_KIT, 1));
                net.minecraft.util.RandomSource r = this.getRandom();
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
                offers.add(sell(4, com.cursedtides.living.Brig.LOCKPICK, 2));
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
                offers.add(sell(30, com.cursedtides.combat.Grenades.VOODOO_DOLL, 1));
                offers.add(sell(10, com.cursedtides.activity.MiniGames.BREWERS_KIT, 1));
                offers.add(potion(4, net.minecraft.world.item.alchemy.Potions.HEALING));
                offers.add(potion(6, net.minecraft.world.item.alchemy.Potions.REGENERATION));
                offers.add(potion(6, net.minecraft.world.item.alchemy.Potions.LONG_WATER_BREATHING));
                offers.add(potion(5, net.minecraft.world.item.alchemy.Potions.FIRE_RESISTANCE));
                offers.add(potion(5, net.minecraft.world.item.alchemy.Potions.SWIFTNESS));
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
        offers.add(new MerchantOffer(new ItemCost(ModItems.DOUBLOON, 6),
                net.minecraft.world.item.alchemy.PotionContents.createItemStack(Items.POTION,
                        net.minecraft.world.item.alchemy.Potions.LONG_WATER_BREATHING), UNLIMITED, 2, 0.0f));
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

    private static MerchantOffer potion(int doubloons, net.minecraft.core.Holder<net.minecraft.world.item.alchemy.Potion> potion) {
        return new MerchantOffer(new ItemCost(ModItems.DOUBLOON, doubloons),
                net.minecraft.world.item.alchemy.PotionContents.createItemStack(Items.POTION, potion), UNLIMITED, 2, 0.0f);
    }

    private static MerchantOffer sell(int doubloons, Item item, int count) {
        return new MerchantOffer(new ItemCost(ModItems.DOUBLOON, doubloons), new ItemStack(item, count), UNLIMITED, 2, 0.0f);
    }

    private static MerchantOffer buy(Item item, int count, int doubloons) {
        return new MerchantOffer(new ItemCost(item, count), new ItemStack(ModItems.DOUBLOON, doubloons), UNLIMITED, 2, 0.0f);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, EntitySpawnReason spawnReason,
                                 @Nullable SpawnGroupData entityData) {
        this.setPersistenceRequired();
        return super.finalizeSpawn(world, difficulty, spawnReason, entityData);
    }

    /** Natural spawns only on dry, solid ground. */
    @Override
    public boolean checkSpawnRules(LevelAccessor world, EntitySpawnReason reason) {
        if (reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION) {
            return super.checkSpawnRules(world, reason);
        }
        BlockPos pos = this.blockPosition();
        BlockPos below = pos.below();
        return world.getFluidState(pos).isEmpty()
                && world.getBlockState(below).isRedstoneConductor(world, below);
    }
}
