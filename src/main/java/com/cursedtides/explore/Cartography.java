package com.cursedtides.explore;

import net.minecraft.world.InteractionResult;
import com.cursedtides.CursedTides;
import com.cursedtides.dialogue.Specials;
import com.cursedtides.profession.Profession;
import com.cursedtides.profession.ProfessionData;
import com.cursedtides.world.Archipelago;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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
        public CraftedItem(Properties settings, Profession trade, int xp, String hint) {
            super(settings);
            this.trade = trade; this.xp = xp; this.hint = hint;
        }

        @Override
        public void onCraftedBy(ItemStack stack, Player player) {
        Level world = player.level();
            if (!world.isClientSide() && player instanceof ServerPlayer sp) ProfessionData.addXp(sp, trade, xp * Math.max(1, stack.getCount()));
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display__, java.util.function.Consumer<Component> adder__, TooltipFlag type) {
        java.util.List<Component> tooltip = new java.util.ArrayList<>();
            if (!hint.isEmpty()) tooltip.add(Component.literal(hint).withStyle(ChatFormatting.GRAY));
        tooltip.forEach(adder__);
    }
    }

    public static class BlankChartItem extends CraftedItem {
        public BlankChartItem(Properties s) { super(s, Profession.CARTOGRAPHY, 4, "Use on an island you haven't surveyed to chart it."); }

        @Override
        public InteractionResult use(Level world, Player user, InteractionHand hand) {
            ItemStack stack = user.getItemInHand(hand);
            if (!(user instanceof ServerPlayer p)) return InteractionResult.SUCCESS;
            int i = Archipelago.islandIndexAt(p.getBlockX(), p.getBlockZ());
            if (i < 0) {
                p.sendSystemMessage(Component.literal("Nothing but water to draw here.").withStyle(ChatFormatting.GRAY), true);
                return InteractionResult.FAIL;
            }
            int mask = p.getAttachedOrElse(SURVEYED, 0);
            if ((mask & (1 << i)) != 0) {
                p.sendSystemMessage(Component.literal("You've already surveyed " + Archipelago.ISLANDS.get(i).name() + ".").withStyle(ChatFormatting.GRAY), true);
                return InteractionResult.FAIL;
            }
            p.setAttached(SURVEYED, mask | (1 << i));
            stack.shrink(1);
            ItemStack survey = new ItemStack(SURVEY);
            CompoundTag n = new CompoundTag();
            n.putInt("Island", i);
            survey.set(DataComponents.CUSTOM_DATA, CustomData.of(n));
            survey.set(DataComponents.ITEM_NAME, Component.literal("Survey of " + Archipelago.ISLANDS.get(i).name()));
            p.addItem(survey);
            int level = ProfessionData.level(p, Profession.CARTOGRAPHY);
            ProfessionData.addXp(p, Profession.CARTOGRAPHY, 30);
            if (level >= 15 && p.getRandom().nextFloat() < 0.3f) {
                p.addItem(new ItemStack(com.cursedtides.item.ModItems.TREASURE_MAP));
                p.sendSystemMessage(Component.literal("Your sharp eye picks out a likely spot for buried treasure. You mark it on a second sheet.").withStyle(ChatFormatting.GOLD), false);
            }
            world.playSound(null, p.blockPosition(), SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.PLAYERS, 1f, 1f);
            p.sendSystemMessage(Component.literal("You chart " + Archipelago.ISLANDS.get(i).name() + ".").withStyle(ChatFormatting.GREEN), true);
            return InteractionResult.SUCCESS;
        }
    }

    public static class HiddenChartItem extends CraftedItem {
        public HiddenChartItem(Properties s) { super(s, Profession.CARTOGRAPHY, 25, "Needs Cartography 10. Use to reveal an island that isn't on any chart."); }

        @Override
        public InteractionResult use(Level world, Player user, InteractionHand hand) {
            ItemStack stack = user.getItemInHand(hand);
            if (!(user instanceof ServerPlayer p)) return InteractionResult.SUCCESS;
            int level = ProfessionData.level(p, Profession.CARTOGRAPHY);
            if (level < 10) {
                p.sendSystemMessage(Component.literal("The lines swim. You'll need more skill at cartography (10) to read this.").withStyle(ChatFormatting.GRAY), true);
                return InteractionResult.FAIL;
            }
            for (Archipelago.Island is : Archipelago.HIDDEN) {
                if (is == Archipelago.KINGSREST && level < 25 && com.cursedtides.quest.CrownArc.stage(p) < com.cursedtides.quest.CrownArc.CONFRONT) continue;
                if (revealed(p, is)) continue;
                stack.shrink(1);
                reveal(p, is);
                ProfessionData.addXp(p, Profession.CARTOGRAPHY, 60);
                return InteractionResult.SUCCESS;
            }
            p.sendSystemMessage(Component.literal("Every secret this chart could tell, you already know.").withStyle(ChatFormatting.GRAY), true);
            return InteractionResult.FAIL;
        }
    }

    public static final Item BLANK_CHART = Registry.register(BuiltInRegistries.ITEM, com.cursedtides.util.Reg.itemId("blank_chart"), new BlankChartItem(com.cursedtides.util.Reg.props()));
    public static final Item SURVEY = Registry.register(BuiltInRegistries.ITEM, com.cursedtides.util.Reg.itemId("island_survey"),
            new CraftedItem(com.cursedtides.util.Reg.props().stacksTo(16), Profession.CARTOGRAPHY, 0, "The archivist buys surveys."));
    public static final Item HIDDEN_CHART = Registry.register(BuiltInRegistries.ITEM, com.cursedtides.util.Reg.itemId("hidden_chart"),
            new HiddenChartItem(com.cursedtides.util.Reg.props().stacksTo(1).rarity(Rarity.RARE)));
    public static final Item ROUGH_GEM = Registry.register(BuiltInRegistries.ITEM, com.cursedtides.util.Reg.itemId("rough_gem"),
            new Item(com.cursedtides.util.Reg.props().rarity(Rarity.UNCOMMON)));
    public static final Item CUT_GEM = Registry.register(BuiltInRegistries.ITEM, com.cursedtides.util.Reg.itemId("cut_gem"),
            new CraftedItem(com.cursedtides.util.Reg.props().rarity(Rarity.RARE), Profession.JEWELCRAFTING, 15, "Set it in a ring or a pendant."));
    public static final Item SAILCLOTH = Registry.register(BuiltInRegistries.ITEM, com.cursedtides.util.Reg.itemId("sailcloth"),
            new CraftedItem(com.cursedtides.util.Reg.props(), Profession.TAILORING, 3, "Good for bandanas, cloaks and sails."));

    public static boolean revealed(Player p, Archipelago.Island is) {
        int i = Archipelago.ISLANDS.indexOf(is);
        return (p.getAttachedOrElse(REVEALED, 0) & (1 << i)) != 0;
    }

    public static int revealedMask(Player p) {
        return p.getAttachedOrElse(REVEALED, 0);
    }

    public static void reveal(ServerPlayer p, Archipelago.Island is) {
        int i = Archipelago.ISLANDS.indexOf(is);
        p.setAttached(REVEALED, revealedMask(p) | (1 << i));
        p.sendSystemMessage(Component.literal("A new island on your chart: " + is.name() + ", near " + is.cx() + ", " + is.cz() + ".")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), false);
        p.level().playSound(null, p.blockPosition(), SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.PLAYERS, 1f, 0.8f);
        com.cursedtides.activity.Achievements.grant(p, "hidden_isle");
    }

    public static void register() {
        net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents.modifyOutputEvent(net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, CursedTides.id("main"))).register(e -> {
            e.accept(BLANK_CHART);
            e.accept(HIDDEN_CHART);
            e.accept(ROUGH_GEM);
            e.accept(CUT_GEM);
            e.accept(SAILCLOTH);
        });
        // Rough gems turn up in rock now and then
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, be) -> {
            if (world.isClientSide() || !stony(state) || world.getRandom().nextFloat() > 0.006f) return;
            ItemEntity gem = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ROUGH_GEM));
            world.addFreshEntity(gem);
            if (player instanceof ServerPlayer sp) sp.sendSystemMessage(Component.literal("Something glitters in the rubble.").withStyle(ChatFormatting.AQUA), true);
        });
        Specials.register("sell_surveys", (p, npc) -> {
            int n = p.getInventory().countItem(SURVEY);
            if (n == 0) return List.of("No surveys? Take a blank chart to an island you haven't drawn, and use it there.");
            com.cursedtides.util.Compat.take(p, s -> s.is(SURVEY), n);
            com.cursedtides.trade.Wallet.give(p, n * 50);
            ProfessionData.addXp(p, Profession.CARTOGRAPHY, n * 5);
            return List.of(n + " survey" + (n == 1 ? "" : "s") + "! The archive thanks you. " + (n * 5) + " doubloons.");
        });
        Specials.register("buy_charts", (p, npc) -> {
            if (!com.cursedtides.trade.Wallet.pay(p, 20)) return List.of("Two doubloons for a sheaf of blank charts.");
            p.addItem(new ItemStack(BLANK_CHART, 4));
            return List.of("Four blank charts. Fill them with something I haven't seen.");
        });
    }

    private static boolean stony(BlockState s) {
        return s.is(Blocks.STONE) || s.is(Blocks.DEEPSLATE) || s.is(Blocks.ANDESITE) || s.is(Blocks.DIORITE) || s.is(Blocks.GRANITE)
                || s.is(Blocks.TUFF) || s.is(Blocks.BASALT) || s.is(Blocks.BLACKSTONE) || s.is(Blocks.SANDSTONE) || s.is(Blocks.CALCITE);
    }

    @SuppressWarnings("unused")
    private static boolean serverSide(Level w) { return w instanceof ServerLevel; }
}
