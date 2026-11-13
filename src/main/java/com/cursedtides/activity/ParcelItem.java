package com.cursedtides.activity;

import net.minecraft.world.InteractionResult;
import com.cursedtides.trade.Ports;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

/** A sealed parcel for someone in another port. Use it once you're there to hand it over. */
public class ParcelItem extends Item {
    public ParcelItem(Properties settings) {
        super(settings.stacksTo(1));
    }

    public static ItemStack make(ServerPlayer p, Entity from) {
        Ports.Port here = com.cursedtides.living.Prosperity.nearestPort(from.getBlockX(), from.getBlockZ(), 200);
        List<Ports.Port> honest = Ports.ALL.stream().filter(x -> !x.blackMarket() && x != here).toList();
        Ports.Port dest = honest.get(p.getRandom().nextInt(honest.size()));
        double dist = here == null ? 400 : Math.hypot(dest.island().cx() - here.island().cx(), dest.island().cz() - here.island().cz());
        ItemStack stack = new ItemStack(Deliveries.PARCEL);
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("Dest", Ports.ALL.indexOf(dest));
        nbt.putLong("Due", p.level().getGameTime() + (long) (dist * 6) + 2400);
        nbt.putInt("Pay", 4 + (int) (dist / 60));
        String[] who = {"a clerk at the harbour office", "the tavern keeper", "a widow called Mrs. Pell", "the quartermaster",
                "a sailor named Gaff", "whoever answers at the blue door"};
        nbt.putString("For", who[p.getRandom().nextInt(who.length)]);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        return stack;
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (!(user instanceof ServerPlayer p)) return InteractionResult.SUCCESS;
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        int i = nbt.getIntOr("Dest", 0);
        if (i < 0 || i >= Ports.ALL.size()) return InteractionResult.FAIL;
        Ports.Port dest = Ports.ALL.get(i);
        double dx = p.getX() - dest.island().cx(), dz = p.getZ() - dest.island().cz();
        if (dx * dx + dz * dz > 80 * 80) {
            p.sendSystemMessage(Component.literal("This is for " + dest.island().name() + ". You're not there yet.").withStyle(ChatFormatting.GRAY), true);
            return InteractionResult.FAIL;
        }
        boolean late = p.level().getGameTime() > nbt.getLongOr("Due", 0L);
        int pay = late ? Math.max(1, nbt.getIntOr("Pay", 0) / 2) : nbt.getIntOr("Pay", 0);
        stack.shrink(1);
        p.addItem(new ItemStack(com.cursedtides.item.ModItems.DOUBLOON, pay));
        p.sendSystemMessage(Component.literal(late ? "Delivered, late. " + pay + " doubloons, and a sour look." : "Delivered on time! " + pay + " doubloons.")
                .withStyle(late ? ChatFormatting.YELLOW : ChatFormatting.GREEN), false);
        com.cursedtides.living.Prosperity.adjust(p.level().getServer(), dest.island().cx(), dest.island().cz(), 1, null);
        Challenges.progress(p, "parcel", 1);
        if (!late) Achievements.grant(p, "delivery");
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display__, java.util.function.Consumer<Component> adder__, TooltipFlag type) {
        java.util.List<Component> tooltip = new java.util.ArrayList<>();
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        int i = nbt.getIntOr("Dest", 0);
        if (i >= 0 && i < Ports.ALL.size()) {
            tooltip.add(Component.literal("For " + nbt.getStringOr("For", "") + ", " + Ports.ALL.get(i).island().name()).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Pays " + nbt.getIntOr("Pay", 0) + " doubloons on time").withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltip.add(Component.literal("Use it when you arrive.").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.forEach(adder__);
    }
}
