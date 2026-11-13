package com.cursedtides.profession;

import com.cursedtides.CursedTides;
import com.cursedtides.pirate.PirateData;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import java.util.ArrayList;
import java.util.List;

/** A cooked dish or brewed drink. Quality is rolled when crafted, from the crafter's skill. */
public class DishItem extends Item {
    private final Profession trade;
    private final int xp;
    private final ProfessionItems.Effect[] effects;
    private boolean drink;
    private int voodoo;

    DishItem(Profession trade, FoodProperties food, int xp, ProfessionItems.Effect[] effects) {
        super(new Item.Properties().stacksTo(16).food(food));
        this.trade = trade;
        this.xp = xp;
        this.effects = effects;
    }

    DishItem drink() { this.drink = true; return this; }
    DishItem voodoo(int amount) { this.voodoo = amount; return this; }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return drink ? UseAnim.DRINK : UseAnim.EAT;
    }

    @Override
    public SoundEvent getEatingSound() {
        return drink ? SoundEvents.GENERIC_DRINK : super.getEatingSound();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        // Drinks and tonics can be taken any time, even on a full stomach
        user.startUsingItem(hand);
        return InteractionResultHolder.consume(user.getItemInHand(hand));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        if (!world.isClientSide) {
            double scale = switch (ProfessionItems.quality(stack)) { case 2 -> 2.0; case 1 -> 1.5; default -> 1.0; };
            for (ProfessionItems.Effect e : effects) {
                boolean harmful = e.effect() == MobEffects.CONFUSION;
                int ticks = harmful ? e.ticks() : (int) (e.ticks() * scale);
                user.addEffect(new MobEffectInstance(e.effect(), ticks, e.amplifier()));
            }
            if (voodoo > 0 && user instanceof Player p) {
                PirateData.regenVoodoo(p, (int) (voodoo * scale));
            }
        }
        ItemStack rest = super.finishUsingItem(stack, world, user);
        if (drink && user instanceof Player p && !p.getAbilities().instabuild) {
            ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
            if (rest.isEmpty()) return bottle;
            if (!p.getInventory().add(bottle)) p.drop(bottle, false);
        }
        return rest;
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level world, Player player) {
        if (world.isClientSide || !(player instanceof ServerPlayer sp)) return;
        int level = ProfessionData.level(sp, trade);
        float roll = sp.getRandom().nextFloat();
        int q = roll < Math.max(0, level - 15) * 0.03f ? 2 : roll < level * 0.03f ? 1 : 0;
        if (q > 0) ProfessionItems.setInt(stack, "Quality", q);
        ProfessionData.addXp(sp, trade, xp);
        if (trade == Profession.COOKING) com.cursedtides.activity.Challenges.progress(sp, "cook", stack.getCount());
    }

    @Override
    public Component getName(ItemStack stack) {
        Component base = super.getName(stack);
        return switch (ProfessionItems.quality(stack)) {
            case 2 -> Component.literal("Masterwork ").append(base).withStyle(ChatFormatting.LIGHT_PURPLE);
            case 1 -> Component.literal("Fine ").append(base).withStyle(ChatFormatting.AQUA);
            default -> base;
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        double scale = switch (ProfessionItems.quality(stack)) { case 2 -> 2.0; case 1 -> 1.5; default -> 1.0; };
        for (ProfessionItems.Effect e : effects) {
            int secs = (int) (e.ticks() * (e.effect() == MobEffects.CONFUSION ? 1.0 : scale) / 20);
            tooltip.add(Component.translatable(e.effect().value().getDescriptionId())
                    .append(Component.literal((e.amplifier() > 0 ? " II" : "") + " (" + secs / 60 + ":" + String.format("%02d", secs % 60) + ")"))
                    .withStyle(e.effect().value().isBeneficial() ? ChatFormatting.BLUE : ChatFormatting.RED));
        }
        if (voodoo > 0) tooltip.add(Component.literal("Restores " + (int) (voodoo * scale) + " voodoo energy").withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(Component.literal((trade == Profession.COOKING ? "Cooked" : "Brewed") + " at a crafting table").withStyle(ChatFormatting.DARK_GRAY));
    }
}
