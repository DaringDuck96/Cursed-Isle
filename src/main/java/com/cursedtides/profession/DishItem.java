package com.cursedtides.profession;

import com.cursedtides.CursedTides;
import com.cursedtides.pirate.PirateData;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/** A cooked dish or brewed drink. Quality is rolled when crafted, from the crafter's skill. */
public class DishItem extends Item {
    private final Profession trade;
    private final int xp;
    private final ProfessionItems.Effect[] effects;
    private boolean drink;
    private int voodoo;

    DishItem(Profession trade, FoodComponent food, int xp, ProfessionItems.Effect[] effects) {
        super(new Item.Settings().maxCount(16).food(food));
        this.trade = trade;
        this.xp = xp;
        this.effects = effects;
    }

    DishItem drink() { this.drink = true; return this; }
    DishItem voodoo(int amount) { this.voodoo = amount; return this; }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return drink ? UseAction.DRINK : UseAction.EAT;
    }

    @Override
    public SoundEvent getEatSound() {
        return drink ? SoundEvents.ENTITY_GENERIC_DRINK : super.getEatSound();
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        // Drinks and tonics can be taken any time, even on a full stomach
        user.setCurrentHand(hand);
        return TypedActionResult.consume(user.getStackInHand(hand));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient) {
            double scale = switch (ProfessionItems.quality(stack)) { case 2 -> 2.0; case 1 -> 1.5; default -> 1.0; };
            for (ProfessionItems.Effect e : effects) {
                boolean harmful = e.effect() == StatusEffects.NAUSEA;
                int ticks = harmful ? e.ticks() : (int) (e.ticks() * scale);
                user.addStatusEffect(new StatusEffectInstance(e.effect(), ticks, e.amplifier()));
            }
            if (voodoo > 0 && user instanceof PlayerEntity p) {
                PirateData.regenVoodoo(p, (int) (voodoo * scale));
            }
        }
        ItemStack rest = super.finishUsing(stack, world, user);
        if (drink && user instanceof PlayerEntity p && !p.getAbilities().creativeMode) {
            ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
            if (rest.isEmpty()) return bottle;
            if (!p.getInventory().insertStack(bottle)) p.dropItem(bottle, false);
        }
        return rest;
    }

    @Override
    public void onCraftByPlayer(ItemStack stack, World world, PlayerEntity player) {
        if (world.isClient || !(player instanceof ServerPlayerEntity sp)) return;
        int level = ProfessionData.level(sp, trade);
        float roll = sp.getRandom().nextFloat();
        int q = roll < Math.max(0, level - 15) * 0.03f ? 2 : roll < level * 0.03f ? 1 : 0;
        if (q > 0) ProfessionItems.setInt(stack, "Quality", q);
        ProfessionData.addXp(sp, trade, xp);
        if (trade == Profession.COOKING) com.cursedtides.activity.Challenges.progress(sp, "cook", stack.getCount());
    }

    @Override
    public Text getName(ItemStack stack) {
        Text base = super.getName(stack);
        return switch (ProfessionItems.quality(stack)) {
            case 2 -> Text.literal("Masterwork ").append(base).formatted(Formatting.LIGHT_PURPLE);
            case 1 -> Text.literal("Fine ").append(base).formatted(Formatting.AQUA);
            default -> base;
        };
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        double scale = switch (ProfessionItems.quality(stack)) { case 2 -> 2.0; case 1 -> 1.5; default -> 1.0; };
        for (ProfessionItems.Effect e : effects) {
            int secs = (int) (e.ticks() * (e.effect() == StatusEffects.NAUSEA ? 1.0 : scale) / 20);
            tooltip.add(Text.translatable(e.effect().value().getTranslationKey())
                    .append(Text.literal((e.amplifier() > 0 ? " II" : "") + " (" + secs / 60 + ":" + String.format("%02d", secs % 60) + ")"))
                    .formatted(e.effect().value().isBeneficial() ? Formatting.BLUE : Formatting.RED));
        }
        if (voodoo > 0) tooltip.add(Text.literal("Restores " + (int) (voodoo * scale) + " voodoo energy").formatted(Formatting.DARK_AQUA));
        tooltip.add(Text.literal((trade == Profession.COOKING ? "Cooked" : "Brewed") + " at a crafting table").formatted(Formatting.DARK_GRAY));
    }
}
