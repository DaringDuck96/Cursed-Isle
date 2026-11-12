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

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("Accessory (" + slot.name().toLowerCase() + ")").formatted(Formatting.GOLD));
        tooltip.add(Text.literal(effectText).formatted(Formatting.BLUE));
        tooltip.add(Text.literal("Use to wear it. See what you're wearing with /pirate accessories.").formatted(Formatting.DARK_GRAY));
    }
}
