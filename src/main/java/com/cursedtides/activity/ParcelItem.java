package com.cursedtides.activity;

import com.cursedtides.trade.Ports;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/** A sealed parcel for someone in another port. Use it once you're there to hand it over. */
public class ParcelItem extends Item {
    public ParcelItem(Settings settings) {
        super(settings.maxCount(1));
    }

    public static ItemStack make(ServerPlayerEntity p, Entity from) {
        Ports.Port here = com.cursedtides.living.Prosperity.nearestPort(from.getBlockX(), from.getBlockZ(), 200);
        List<Ports.Port> honest = Ports.ALL.stream().filter(x -> !x.blackMarket() && x != here).toList();
        Ports.Port dest = honest.get(p.getRandom().nextInt(honest.size()));
        double dist = here == null ? 400 : Math.hypot(dest.island().cx() - here.island().cx(), dest.island().cz() - here.island().cz());
        ItemStack stack = new ItemStack(Deliveries.PARCEL);
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("Dest", Ports.ALL.indexOf(dest));
        nbt.putLong("Due", p.getServerWorld().getTime() + (long) (dist * 6) + 2400);
        nbt.putInt("Pay", 4 + (int) (dist / 60));
        String[] who = {"a clerk at the harbour office", "the tavern keeper", "a widow called Mrs. Pell", "the quartermaster",
                "a sailor named Gaff", "whoever answers at the blue door"};
        nbt.putString("For", who[p.getRandom().nextInt(who.length)]);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        return stack;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!(user instanceof ServerPlayerEntity p)) return TypedActionResult.success(stack, true);
        NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        int i = nbt.getInt("Dest");
        if (i < 0 || i >= Ports.ALL.size()) return TypedActionResult.fail(stack);
        Ports.Port dest = Ports.ALL.get(i);
        double dx = p.getX() - dest.island().cx(), dz = p.getZ() - dest.island().cz();
        if (dx * dx + dz * dz > 80 * 80) {
            p.sendMessage(Text.literal("This is for " + dest.island().name() + ". You're not there yet.").formatted(Formatting.GRAY), true);
            return TypedActionResult.fail(stack);
        }
        boolean late = p.getServerWorld().getTime() > nbt.getLong("Due");
        int pay = late ? Math.max(1, nbt.getInt("Pay") / 2) : nbt.getInt("Pay");
        stack.decrement(1);
        p.giveItemStack(new ItemStack(com.cursedtides.item.ModItems.DOUBLOON, pay));
        p.sendMessage(Text.literal(late ? "Delivered, late. " + pay + " doubloons, and a sour look." : "Delivered on time! " + pay + " doubloons.")
                .formatted(late ? Formatting.YELLOW : Formatting.GREEN), false);
        com.cursedtides.living.Prosperity.adjust(p.getServer(), dest.island().cx(), dest.island().cz(), 1, null);
        Challenges.progress(p, "parcel", 1);
        if (!late) Achievements.grant(p, "delivery");
        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        int i = nbt.getInt("Dest");
        if (i >= 0 && i < Ports.ALL.size()) {
            tooltip.add(Text.literal("For " + nbt.getString("For") + ", " + Ports.ALL.get(i).island().name()).formatted(Formatting.GRAY));
            tooltip.add(Text.literal("Pays " + nbt.getInt("Pay") + " doubloons on time").formatted(Formatting.DARK_GRAY));
        }
        tooltip.add(Text.literal("Use it when you arrive.").formatted(Formatting.DARK_GRAY));
    }
}
