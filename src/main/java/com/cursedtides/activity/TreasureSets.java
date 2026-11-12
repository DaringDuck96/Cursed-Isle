package com.cursedtides.activity;

import com.cursedtides.CursedTides;
import com.cursedtides.item.ModItems;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Treasure collections: pearls, jewels, old coins and Brethren keepsakes, found on the fallen and in chests.
 * Use a piece to add it to your collection; finish a set for a reward. Spares are worth silver.
 */
public final class TreasureSets {
    private TreasureSets() {}

    public record Set(String id, String title, String[] pieces, String reward) {}

    public static final List<Set> SETS = List.of(
            new Set("pearls", "Pearls of the Reach", new String[]{"White Pearl", "Black Pearl", "Rose Pearl", "Golden Pearl", "Blue Pearl", "Moon Pearl"},
                    "a Lucky Coin and 60 doubloons"),
            new Set("jewels", "Jewels of the Drowned Court", new String[]{"Court Ruby", "Court Sapphire", "Court Emerald", "Court Topaz", "Court Diamond"},
                    "a Masterwork Ruby Ring and 80 doubloons"),
            new Set("coins", "Coins of the Old Kings", new String[]{"Copper Farthing", "Silver Crown", "Gold Sovereign", "Square Penny",
                    "Holed Coin", "The Last King's Mark"}, "a Legendary blade and 50 doubloons"),
            new Set("keepsakes", "Brethren Keepsakes", new String[]{"Captain's Ring", "Tarnished Locket", "Lucky Dice", "Bosun's Pipe", "Scrimshaw Tooth"},
                    "a Compass Pendant, a Hidden Chart and 70 doubloons"));

    public static final AttachmentType<List<String>> FOUND = AttachmentRegistry.<List<String>>builder()
            .persistent(Codec.STRING.listOf()).copyOnDeath().initializer(List::of).buildAndRegister(CursedTides.id("treasure_sets"));

    public static class PieceItem extends Item {
        public PieceItem(Settings s) { super(s); }

        @Override
        public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
            ItemStack stack = user.getStackInHand(hand);
            if (user instanceof ServerPlayerEntity p) {
                String key = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getString("Piece");
                if (!key.isEmpty()) { add(p, key); stack.decrement(1); }
            }
            return TypedActionResult.success(stack, world.isClient);
        }

        @Override
        public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
            String key = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getString("Piece");
            int dot = key.indexOf('.');
            if (dot > 0) for (Set s : SETS) if (s.id().equals(key.substring(0, dot))) tooltip.add(Text.literal("Part of: " + s.title()).formatted(Formatting.GOLD));
            tooltip.add(Text.literal("Use to add it to your collection (/collections).").formatted(Formatting.DARK_GRAY));
        }
    }

    public static final Item PIECE = Registry.register(Registries.ITEM, CursedTides.id("treasure_piece"), new PieceItem(new Item.Settings().rarity(Rarity.RARE)));

    public static ItemStack piece(int set, int index) {
        Set s = SETS.get(set);
        ItemStack stack = new ItemStack(PIECE);
        NbtCompound n = new NbtCompound();
        n.putString("Piece", s.id() + "." + index);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(n));
        stack.set(DataComponentTypes.ITEM_NAME, Text.literal(s.pieces()[index]));
        stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(set + 1));
        return stack;
    }

    public static ItemStack random(net.minecraft.util.math.random.Random r) {
        int set = r.nextInt(SETS.size());
        return piece(set, r.nextInt(SETS.get(set).pieces().length));
    }

    static void add(ServerPlayerEntity p, String key) {
        List<String> found = new ArrayList<>(p.getAttachedOrElse(FOUND, List.of()));
        if (found.contains(key)) {
            com.cursedtides.trade.Wallet.give(p, 25);
            p.sendMessage(Text.literal("You already have that one. A collector pays you 2 doubloons and change for the spare.").formatted(Formatting.GRAY), true);
            return;
        }
        found.add(key);
        p.setAttached(FOUND, found);
        String setId = key.substring(0, key.indexOf('.'));
        Set set = SETS.stream().filter(s -> s.id().equals(setId)).findFirst().orElse(null);
        if (set == null) return;
        long have = found.stream().filter(k -> k.startsWith(setId + ".")).count();
        p.getWorld().playSound(null, p.getBlockPos(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.8f, 1.4f);
        p.sendMessage(Text.literal(set.title() + ": " + have + "/" + set.pieces().length).formatted(Formatting.GOLD), false);
        if (have == set.pieces().length) complete(p, set);
    }

    private static void complete(ServerPlayerEntity p, Set set) {
        p.sendMessage(Text.literal("Collection complete: " + set.title() + "! You receive " + set.reward() + ".").formatted(Formatting.GOLD, Formatting.BOLD), false);
        p.getWorld().playSound(null, p.getBlockPos(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1f, 1f);
        Achievements.grant(p, "collector_of_sets");
        switch (set.id()) {
            case "pearls" -> { p.giveItemStack(new ItemStack(com.cursedtides.appearance.Accessories.LUCKY_COIN)); p.giveItemStack(new ItemStack(ModItems.DOUBLOON, 60)); }
            case "jewels" -> {
                ItemStack ring = new ItemStack(com.cursedtides.appearance.Accessories.RUBY_RING);
                com.cursedtides.appearance.AccessoryItem.setQuality(ring, 2);
                p.giveItemStack(ring);
                p.giveItemStack(new ItemStack(ModItems.DOUBLOON, 64));
                p.giveItemStack(new ItemStack(ModItems.DOUBLOON, 16));
            }
            case "coins" -> { p.giveItemStack(com.cursedtides.combat.GearRarity.legendaryOf(p.getServerWorld(), ModItems.SABER, p.getRandom())); p.giveItemStack(new ItemStack(ModItems.DOUBLOON, 50)); }
            default -> {
                p.giveItemStack(new ItemStack(com.cursedtides.appearance.Accessories.COMPASS_PENDANT));
                p.giveItemStack(new ItemStack(com.cursedtides.explore.Cartography.HIDDEN_CHART));
                p.giveItemStack(new ItemStack(ModItems.DOUBLOON, 64));
                p.giveItemStack(new ItemStack(ModItems.DOUBLOON, 6));
            }
        }
    }

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((victim, source) -> {
            if (!(source.getAttacker() instanceof ServerPlayerEntity) || !(victim instanceof Monster)) return;
            float chance = victim.getCommandTags().contains("cursedtides_boss") ? 1f : 0.025f;
            if (victim.getRandom().nextFloat() < chance) victim.dropStack(random(victim.getRandom()));
        });
        CommandRegistrationCallback.EVENT.register((d, a, e) -> d.register(literal("collections").executes(ctx -> {
            ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
            List<String> found = p.getAttachedOrElse(FOUND, List.of());
            for (int i = 0; i < SETS.size(); i++) {
                Set s = SETS.get(i);
                StringBuilder line = new StringBuilder();
                for (int k = 0; k < s.pieces().length; k++) {
                    boolean have = found.contains(s.id() + "." + k);
                    line.append(have ? "§a" : "§8").append(have ? s.pieces()[k] : "???").append("§7, ");
                }
                p.sendMessage(Text.literal("§6" + s.title() + ": " + line.substring(0, line.length() - 4)), false);
            }
            return 1;
        })));
    }
}
