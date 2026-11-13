package com.cursedtides.appearance;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** Worn, not held: use it to put it on (whatever was in that slot comes back to your pack). */
public class AccessoryItem extends Item {
    public enum Slot { HEAD, EYES, EAR, NECK, RING, TRINKET, GLOVES, BELT, CLOAK }

    public final Slot slot;
    /** Attribute and amount it grants, or null for a purely special effect. */
    public final Holder<Attribute> attribute;
    public final double amount;
    /** Special effect key read by other systems ("crit", "explore", "voodoo"), or null. */
    public final String special;
    public final int visual;
    private final String effectText;

    public AccessoryItem(Properties settings, Slot slot, Holder<Attribute> attribute, double amount,
                         String special, int visual, String effectText) {
        super(settings.stacksTo(1));
        this.slot = slot;
        this.attribute = attribute;
        this.amount = amount;
        this.special = special;
        this.visual = visual;
        this.effectText = effectText;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (!world.isClientSide && user instanceof ServerPlayer sp) {
            Appearance.wear(sp, stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
    }

    public static int quality(ItemStack stack) {
        return stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY)
                .copyTag().getInt("Quality");
    }

    public static void setQuality(ItemStack stack, int q) {
        var n = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        n.putInt("Quality", q);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(n));
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
    public void onCraftedBy(ItemStack stack, Level world, Player player) {
        if (world.isClientSide || !(player instanceof ServerPlayer sp)) return;
        int level = com.cursedtides.profession.ProfessionData.level(sp, trade());
        float roll = sp.getRandom().nextFloat();
        int q = level >= 20 && roll < 0.35f ? 2 : level >= 10 && roll < 0.6f ? 1 : 0;
        if (q > 0) {
            setQuality(stack, q);
            sp.displayClientMessage(Component.literal(q == 2 ? "Masterwork!" : "Fine work.").withStyle(ChatFormatting.GOLD), true);
        }
        com.cursedtides.profession.ProfessionData.addXp(sp, trade(), 20 + 10 * stack.getRarity().ordinal());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        int q = quality(stack);
        tooltip.add(Component.literal((q == 2 ? "Masterwork " : q == 1 ? "Fine " : "") + "Accessory (" + slot.name().toLowerCase() + ")").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal(effectText + (q > 0 ? "  (x" + factor(q) + ")" : "")).withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.literal("Use to wear it. See what you're wearing with /pirate accessories.").withStyle(ChatFormatting.DARK_GRAY));
    }
}
