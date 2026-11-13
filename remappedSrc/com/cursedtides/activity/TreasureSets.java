package com.cursedtides.activity;

import com.cursedtides.CursedTides;
import com.cursedtides.item.ModItems;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import java.util.ArrayList;
import java.util.List;

import static net.minecraft.commands.Commands.literal;

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
        public PieceItem(Properties s) { super(s); }

        @Override
        public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
            ItemStack stack = user.getItemInHand(hand);
            if (user instanceof ServerPlayer p) {
                String key = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString("Piece");
                if (!key.isEmpty()) { add(p, key); stack.shrink(1); }
            }
            return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
            String key = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString("Piece");
            int dot = key.indexOf('.');
            if (dot > 0) for (Set s : SETS) if (s.id().equals(key.substring(0, dot))) tooltip.add(Component.literal("Part of: " + s.title()).withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.literal("Use to add it to your collection (/collections).").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    public static final Item PIECE = Registry.register(BuiltInRegistries.ITEM, CursedTides.id("treasure_piece"), new PieceItem(new Item.Properties().rarity(Rarity.RARE)));

    public static ItemStack piece(int set, int index) {
        Set s = SETS.get(set);
        ItemStack stack = new ItemStack(PIECE);
        CompoundTag n = new CompoundTag();
        n.putString("Piece", s.id() + "." + index);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(n));
        stack.set(DataComponents.ITEM_NAME, Component.literal(s.pieces()[index]));
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(set + 1));
        return stack;
    }

    public static ItemStack random(net.minecraft.util.RandomSource r) {
        int set = r.nextInt(SETS.size());
        return piece(set, r.nextInt(SETS.get(set).pieces().length));
    }

    static void add(ServerPlayer p, String key) {
        List<String> found = new ArrayList<>(p.getAttachedOrElse(FOUND, List.of()));
        if (found.contains(key)) {
            com.cursedtides.trade.Wallet.give(p, 25);
            p.displayClientMessage(Component.literal("You already have that one. A collector pays you 2 doubloons and change for the spare.").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        found.add(key);
        p.setAttached(FOUND, found);
        String setId = key.substring(0, key.indexOf('.'));
        Set set = SETS.stream().filter(s -> s.id().equals(setId)).findFirst().orElse(null);
        if (set == null) return;
        long have = found.stream().filter(k -> k.startsWith(setId + ".")).count();
        p.level().playSound(null, p.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8f, 1.4f);
        p.displayClientMessage(Component.literal(set.title() + ": " + have + "/" + set.pieces().length).withStyle(ChatFormatting.GOLD), false);
        if (have == set.pieces().length) complete(p, set);
    }

    private static void complete(ServerPlayer p, Set set) {
        p.displayClientMessage(Component.literal("Collection complete: " + set.title() + "! You receive " + set.reward() + ".").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        p.level().playSound(null, p.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1f, 1f);
        Achievements.grant(p, "collector_of_sets");
        switch (set.id()) {
            case "pearls" -> { p.addItem(new ItemStack(com.cursedtides.appearance.Accessories.LUCKY_COIN)); p.addItem(new ItemStack(ModItems.DOUBLOON, 60)); }
            case "jewels" -> {
                ItemStack ring = new ItemStack(com.cursedtides.appearance.Accessories.RUBY_RING);
                com.cursedtides.appearance.AccessoryItem.setQuality(ring, 2);
                p.addItem(ring);
                p.addItem(new ItemStack(ModItems.DOUBLOON, 64));
                p.addItem(new ItemStack(ModItems.DOUBLOON, 16));
            }
            case "coins" -> { p.addItem(com.cursedtides.combat.GearRarity.legendaryOf(p.serverLevel(), ModItems.SABER, p.getRandom())); p.addItem(new ItemStack(ModItems.DOUBLOON, 50)); }
            default -> {
                p.addItem(new ItemStack(com.cursedtides.appearance.Accessories.COMPASS_PENDANT));
                p.addItem(new ItemStack(com.cursedtides.explore.Cartography.HIDDEN_CHART));
                p.addItem(new ItemStack(ModItems.DOUBLOON, 64));
                p.addItem(new ItemStack(ModItems.DOUBLOON, 6));
            }
        }
    }

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((victim, source) -> {
            if (!(source.getEntity() instanceof ServerPlayer) || !(victim instanceof Enemy)) return;
            float chance = victim.getTags().contains("cursedtides_boss") ? 1f : 0.025f;
            if (victim.getRandom().nextFloat() < chance) victim.spawnAtLocation(random(victim.getRandom()));
        });
        CommandRegistrationCallback.EVENT.register((d, a, e) -> d.register(literal("collections").executes(ctx -> {
            ServerPlayer p = ctx.getSource().getPlayerOrException();
            List<String> found = p.getAttachedOrElse(FOUND, List.of());
            for (int i = 0; i < SETS.size(); i++) {
                Set s = SETS.get(i);
                StringBuilder line = new StringBuilder();
                for (int k = 0; k < s.pieces().length; k++) {
                    boolean have = found.contains(s.id() + "." + k);
                    line.append(have ? "§a" : "§8").append(have ? s.pieces()[k] : "???").append("§7, ");
                }
                p.displayClientMessage(Component.literal("§6" + s.title() + ": " + line.substring(0, line.length() - 4)), false);
            }
            return 1;
        })));
    }
}
