package com.cursedtides.appearance;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/** Worn, not held: use it to put it on (whatever was in that slot comes back to your pack). */
public class AccessoryItem extends Item {
    public enum Slot { HEAD, EYES, EAR, NECK, RING, TRINKET, GLOVES, BELT, CLOAK }

    public final Slot slot;
    /** Attribute and amount it grants, or null for a purely special effect. */
    public final RegistryEntry<EntityAttribute> attribute;
    public final double amount;
    /** Special effect key read by other systems ("crit", "explore", "voodoo"), or null. */
    public final String special;
    public final int visual;
    private final String effectText;

    public AccessoryItem(Settings settings, Slot slot, RegistryEntry<EntityAttribute> attribute, double amount,
                         String special, int visual, String effectText) {
        super(settings.maxCount(1));
        this.slot = slot;
        this.attribute = attribute;
        this.amount = amount;
        this.special = special;
        this.visual = visual;
        this.effectText = effectText;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient && user instanceof ServerPlayerEntity sp) {
            Appearance.wear(sp, stack);
        }
        return TypedActionResult.success(stack, world.isClient);
    }

    public static int quality(ItemStack stack) {
        return stack.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA, net.minecraft.component.type.NbtComponent.DEFAULT)
                .copyNbt().getInt("Quality");
    }

    public static void setQuality(ItemStack stack, int q) {
        var n = stack.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA, net.minecraft.component.type.NbtComponent.DEFAULT).copyNbt();
        n.putInt("Quality", q);
        stack.set(net.minecraft.component.DataComponentTypes.CUSTOM_DATA, net.minecraft.component.type.NbtComponent.of(n));
    }

    /** Fine pieces are a quarter stronger, Masterwork half again. */
    public static double factor(int quality) {
        return 1.0 + 0.25 * quality;
    }

    /** Which trade makes this: cloth and leather are tailoring; metal and stones are jewelcrafting. */
    public com.cursedtides.profession.Profession trade() {
        return switch (slot) {
            case HEAD, EYES, GLOVES, BELT, CLOAK -> com.cursedtides.profession.Profession.TAILORING;
            default -> com.cursedtides.profession.Profession.JEWELCRAFTING;
        };
    }

    @Override
    public void onCraftByPlayer(ItemStack stack, World world, PlayerEntity player) {
        if (world.isClient || !(player instanceof ServerPlayerEntity sp)) return;
        int level = com.cursedtides.profession.ProfessionData.level(sp, trade());
        float roll = sp.getRandom().nextFloat();
        int q = level >= 20 && roll < 0.35f ? 2 : level >= 10 && roll < 0.6f ? 1 : 0;
        if (q > 0) {
            setQuality(stack, q);
            sp.sendMessage(Text.literal(q == 2 ? "Masterwork!" : "Fine work.").formatted(Formatting.GOLD), true);
        }
        com.cursedtides.profession.ProfessionData.addXp(sp, trade(), 20 + 10 * stack.getRarity().ordinal());
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        int q = quality(stack);
        tooltip.add(Text.literal((q == 2 ? "Masterwork " : q == 1 ? "Fine " : "") + "Accessory (" + slot.name().toLowerCase() + ")").formatted(Formatting.GOLD));
        tooltip.add(Text.literal(effectText + (q > 0 ? "  (x" + factor(q) + ")" : "")).formatted(Formatting.BLUE));
        tooltip.add(Text.literal("Use to wear it. See what you're wearing with /pirate accessories.").formatted(Formatting.DARK_GRAY));
    }
}
