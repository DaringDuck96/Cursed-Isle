package com.cursedtides.explore;

import com.cursedtides.CursedTides;
import com.cursedtides.dialogue.Specials;
import com.cursedtides.profession.Profession;
import com.cursedtides.profession.ProfessionData;
import com.cursedtides.world.Archipelago;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/**
 * Cartography, jewelcrafting's raw stones, and the charts that reveal hidden islands.
 * Use a Blank Chart on an island you haven't surveyed to draw an Island Survey (sell them to the archivist).
 * A Hidden Chart (Cartography 10) reveals an island that isn't on any chart.
 */
public final class Cartography {
    private Cartography() {}

    public static final AttachmentType<Integer> REVEALED = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT).copyOnDeath().initializer(() -> 0).buildAndRegister(CursedTides.id("revealed_islands"));
    public static final AttachmentType<Integer> SURVEYED = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT).copyOnDeath().initializer(() -> 0).buildAndRegister(CursedTides.id("surveyed_islands"));

    /** An item that teaches its trade when crafted. */
    public static class CraftedItem extends Item {
        final Profession trade; final int xp; final String hint;
        public CraftedItem(Settings settings, Profession trade, int xp, String hint) {
            super(settings);
            this.trade = trade; this.xp = xp; this.hint = hint;
        }

        @Override
        public void onCraftByPlayer(ItemStack stack, World world, PlayerEntity player) {
            if (!world.isClient && player instanceof ServerPlayerEntity sp) ProfessionData.addXp(sp, trade, xp * Math.max(1, stack.getCount()));
        }

        @Override
        public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
            if (!hint.isEmpty()) tooltip.add(Text.literal(hint).formatted(Formatting.GRAY));
        }
    }

    public static class BlankChartItem extends CraftedItem {
        public BlankChartItem(Settings s) { super(s, Profession.CARTOGRAPHY, 4, "Use on an island you haven't surveyed to chart it."); }

        @Override
        public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
            ItemStack stack = user.getStackInHand(hand);
            if (!(user instanceof ServerPlayerEntity p)) return TypedActionResult.success(stack, true);
            int i = Archipelago.islandIndexAt(p.getBlockX(), p.getBlockZ());
            if (i < 0) {
                p.sendMessage(Text.literal("Nothing but water to draw here.").formatted(Formatting.GRAY), true);
                return TypedActionResult.fail(stack);
            }
            int mask = p.getAttachedOrElse(SURVEYED, 0);
            if ((mask & (1 << i)) != 0) {
                p.sendMessage(Text.literal("You've already surveyed " + Archipelago.ISLANDS.get(i).name() + ".").formatted(Formatting.GRAY), true);
                return TypedActionResult.fail(stack);
            }
            p.setAttached(SURVEYED, mask | (1 << i));
            stack.decrement(1);
            ItemStack survey = new ItemStack(SURVEY);
            NbtCompound n = new NbtCompound();
            n.putInt("Island", i);
            survey.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(n));
            survey.set(DataComponentTypes.ITEM_NAME, Text.literal("Survey of " + Archipelago.ISLANDS.get(i).name()));
            p.giveItemStack(survey);
            int level = ProfessionData.level(p, Profession.CARTOGRAPHY);
            ProfessionData.addXp(p, Profession.CARTOGRAPHY, 30);
            if (level >= 15 && p.getRandom().nextFloat() < 0.3f) {
                p.giveItemStack(new ItemStack(com.cursedtides.item.ModItems.TREASURE_MAP));
                p.sendMessage(Text.literal("Your sharp eye picks out a likely spot for buried treasure. You mark it on a second sheet.").formatted(Formatting.GOLD), false);
            }
            world.playSound(null, p.getBlockPos(), SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundCategory.PLAYERS, 1f, 1f);
            p.sendMessage(Text.literal("You chart " + Archipelago.ISLANDS.get(i).name() + ".").formatted(Formatting.GREEN), true);
            return TypedActionResult.success(stack);
        }
    }

    public static class HiddenChartItem extends CraftedItem {
        public HiddenChartItem(Settings s) { super(s, Profession.CARTOGRAPHY, 25, "Needs Cartography 10. Use to reveal an island that isn't on any chart."); }

        @Override
        public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
            ItemStack stack = user.getStackInHand(hand);
            if (!(user instanceof ServerPlayerEntity p)) return TypedActionResult.success(stack, true);
            int level = ProfessionData.level(p, Profession.CARTOGRAPHY);
            if (level < 10) {
                p.sendMessage(Text.literal("The lines swim. You'll need more skill at cartography (10) to read this.").formatted(Formatting.GRAY), true);
                return TypedActionResult.fail(stack);
            }
            for (Archipelago.Island is : Archipelago.HIDDEN) {
                if (is == Archipelago.KINGSREST && level < 25 && com.cursedtides.quest.CrownArc.stage(p) < com.cursedtides.quest.CrownArc.CONFRONT) continue;
                if (revealed(p, is)) continue;
                stack.decrement(1);
                reveal(p, is);
                ProfessionData.addXp(p, Profession.CARTOGRAPHY, 60);
                return TypedActionResult.success(stack);
            }
            p.sendMessage(Text.literal("Every secret this chart could tell, you already know.").formatted(Formatting.GRAY), true);
            return TypedActionResult.fail(stack);
        }
    }

    public static final Item BLANK_CHART = Registry.register(Registries.ITEM, CursedTides.id("blank_chart"), new BlankChartItem(new Item.Settings()));
    public static final Item SURVEY = Registry.register(Registries.ITEM, CursedTides.id("island_survey"),
            new CraftedItem(new Item.Settings().maxCount(16), Profession.CARTOGRAPHY, 0, "The archivist buys surveys."));
    public static final Item HIDDEN_CHART = Registry.register(Registries.ITEM, CursedTides.id("hidden_chart"),
            new HiddenChartItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE)));
    public static final Item ROUGH_GEM = Registry.register(Registries.ITEM, CursedTides.id("rough_gem"),
            new Item(new Item.Settings().rarity(Rarity.UNCOMMON)));
    public static final Item CUT_GEM = Registry.register(Registries.ITEM, CursedTides.id("cut_gem"),
            new CraftedItem(new Item.Settings().rarity(Rarity.RARE), Profession.JEWELCRAFTING, 15, "Set it in a ring or a pendant."));
    public static final Item SAILCLOTH = Registry.register(Registries.ITEM, CursedTides.id("sailcloth"),
            new CraftedItem(new Item.Settings(), Profession.TAILORING, 3, "Good for bandanas, cloaks and sails."));

    public static boolean revealed(PlayerEntity p, Archipelago.Island is) {
        int i = Archipelago.ISLANDS.indexOf(is);
        return (p.getAttachedOrElse(REVEALED, 0) & (1 << i)) != 0;
    }

    public static int revealedMask(PlayerEntity p) {
        return p.getAttachedOrElse(REVEALED, 0);
    }

    public static void reveal(ServerPlayerEntity p, Archipelago.Island is) {
        int i = Archipelago.ISLANDS.indexOf(is);
        p.setAttached(REVEALED, revealedMask(p) | (1 << i));
        p.sendMessage(Text.literal("A new island on your chart: " + is.name() + ", near " + is.cx() + ", " + is.cz() + ".")
                .formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD), false);
        p.getWorld().playSound(null, p.getBlockPos(), SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundCategory.PLAYERS, 1f, 0.8f);
        com.cursedtides.activity.Achievements.grant(p, "hidden_isle");
    }

    public static void register() {
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.registry.RegistryKey.of(
                net.minecraft.registry.RegistryKeys.ITEM_GROUP, CursedTides.id("main"))).register(e -> {
            e.add(BLANK_CHART);
            e.add(HIDDEN_CHART);
            e.add(ROUGH_GEM);
            e.add(CUT_GEM);
            e.add(SAILCLOTH);
        });
        // Rough gems turn up in rock now and then
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, be) -> {
            if (world.isClient || !stony(state) || world.random.nextFloat() > 0.006f) return;
            ItemEntity gem = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ROUGH_GEM));
            world.spawnEntity(gem);
            if (player instanceof ServerPlayerEntity sp) sp.sendMessage(Text.literal("Something glitters in the rubble.").formatted(Formatting.AQUA), true);
        });
        Specials.register("sell_surveys", (p, npc) -> {
            int n = p.getInventory().count(SURVEY);
            if (n == 0) return List.of("No surveys? Take a blank chart to an island you haven't drawn, and use it there.");
            p.getInventory().remove(s -> s.isOf(SURVEY), n, p.playerScreenHandler.getCraftingInput());
            com.cursedtides.trade.Wallet.give(p, n * 50);
            ProfessionData.addXp(p, Profession.CARTOGRAPHY, n * 5);
            return List.of(n + " survey" + (n == 1 ? "" : "s") + "! The archive thanks you. " + (n * 5) + " doubloons.");
        });
        Specials.register("buy_charts", (p, npc) -> {
            if (!com.cursedtides.trade.Wallet.pay(p, 20)) return List.of("Two doubloons for a sheaf of blank charts.");
            p.giveItemStack(new ItemStack(BLANK_CHART, 4));
            return List.of("Four blank charts. Fill them with something I haven't seen.");
        });
    }

    private static boolean stony(BlockState s) {
        return s.isOf(Blocks.STONE) || s.isOf(Blocks.DEEPSLATE) || s.isOf(Blocks.ANDESITE) || s.isOf(Blocks.DIORITE) || s.isOf(Blocks.GRANITE)
                || s.isOf(Blocks.TUFF) || s.isOf(Blocks.BASALT) || s.isOf(Blocks.BLACKSTONE) || s.isOf(Blocks.SANDSTONE) || s.isOf(Blocks.CALCITE);
    }

    @SuppressWarnings("unused")
    private static boolean serverSide(World w) { return w instanceof ServerWorld; }
}
