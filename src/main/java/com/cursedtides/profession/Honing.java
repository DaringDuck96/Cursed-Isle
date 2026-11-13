package com.cursedtides.profession;

import net.minecraft.world.InteractionResult;
import com.cursedtides.CursedTides;
import com.cursedtides.item.FlintlockPistolItem;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.Level;

/**
 * Smithing: whetstones hone blades (+1 attack damage a tier), gunsmith kits rifle firearms (+10% damage
 * a tier), reinforcing kits stiffen clothing and armour (+1 armour a tier). Tier caps rise with Smithing.
 */
public final class Honing {
    private Honing() {}

    public enum Kind { BLADE, FIREARM, ARMOR }

    private static final Identifier MOD_ID = CursedTides.id("honed");
    private static final String[] NUMERALS = {"", "I", "II", "III"};

    public static int tier(ItemStack stack) {
        CustomData c = stack.get(DataComponents.CUSTOM_DATA);
        return c == null ? 0 : c.copyTag().getIntOr("Honed", 0);
    }

    /** Damage multiplier for a rifled firearm. */
    public static float gunBonus(ItemStack gun) {
        return 1.0f + 0.10f * tier(gun);
    }

    static Kind kindOf(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof FlintlockPistolItem) return Kind.FIREARM;
        if (com.cursedtides.util.Compat.isBlade(stack)) return Kind.BLADE;
        if (stack.has(DataComponents.EQUIPPABLE) && stack.get(DataComponents.EQUIPPABLE).slot().isArmor()) return Kind.ARMOR;
        return null;
    }

    public static int maxTier(Player player) {
        int level = ProfessionData.level(player, Profession.SMITHING);
        return level >= 20 ? 3 : level >= 10 ? 2 : 1;
    }

    static void apply(ServerPlayer player, ItemStack kitStack, Kind kind, ItemStack target) {
        if (kindOf(target) != kind) {
            String what = switch (kind) { case BLADE -> "a blade"; case FIREARM -> "a firearm"; case ARMOR -> "clothing or armour"; };
            player.sendSystemMessage(Component.literal("Hold " + what + " in your other hand.").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        int tier = tier(target);
        int cap = maxTier(player);
        if (tier >= 3) {
            player.sendSystemMessage(Component.literal("That's as fine as it'll ever be.").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        if (tier >= cap) {
            player.sendSystemMessage(Component.literal("Tier " + NUMERALS[tier + 1] + " needs Smithing " + (tier == 1 ? 10 : 20) + ".")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }
        int next = tier + 1;
        ProfessionItems.setInt(target, "Honed", next);
        if (kind != Kind.FIREARM) {
            ItemAttributeModifiers base = target.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
            if (base.modifiers().isEmpty()) base = target.getItem().components().getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
            ItemAttributeModifiers.Builder b = ItemAttributeModifiers.builder();
            for (ItemAttributeModifiers.Entry e : base.modifiers()) {
                if (!e.modifier().id().equals(MOD_ID)) b.add(e.attribute(), e.modifier(), e.slot());
            }
            if (kind == Kind.BLADE) {
                b.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(MOD_ID, next,
                        AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
            } else {
                b.add(Attributes.ARMOR, new AttributeModifier(MOD_ID, next,
                        AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.bySlot(target.get(DataComponents.EQUIPPABLE).slot()));
            }
            target.set(DataComponents.ATTRIBUTE_MODIFIERS, b.build());
        }
        String label = switch (kind) { case BLADE -> "Honed "; case FIREARM -> "Rifled "; case ARMOR -> "Reinforced "; };
        target.set(DataComponents.LORE, new ItemLore(List.of(Component.literal(label + NUMERALS[next])
                .withStyle(ChatFormatting.AQUA).withStyle(s -> s.withItalic(false)))));
        if (!player.getAbilities().instabuild) kitStack.shrink(1);
        player.level().playSound(null, player.blockPosition(), SoundEvents.GRINDSTONE_USE, SoundSource.PLAYERS, 0.8f, 1.1f);
        player.sendSystemMessage(Component.literal(label + NUMERALS[next] + ": ").append(target.getHoverName()).withStyle(ChatFormatting.GOLD), true);
        ProfessionData.addXp(player, Profession.SMITHING, 10 + next * 10);
    }

    /** Use with the item to improve held in the other hand. */
    public static class KitItem extends Item {
        private final Kind kind;

        public KitItem(Kind kind, Properties settings) {
            super(settings);
            this.kind = kind;
        }

        @Override
        public InteractionResult use(Level world, Player user, InteractionHand hand) {
            ItemStack kit = user.getItemInHand(hand);
            ItemStack target = user.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
            if (!world.isClientSide() && user instanceof ServerPlayer sp) {
                apply(sp, kit, kind, target);
            }
            return InteractionResult.SUCCESS;
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display__, java.util.function.Consumer<Component> adder__, TooltipFlag type) {
        java.util.List<Component> tooltip = new java.util.ArrayList<>();
            String text = switch (kind) {
                case BLADE -> "Hold a blade in your other hand and use: +1 attack damage per tier";
                case FIREARM -> "Hold a firearm in your other hand and use: +10% damage per tier";
                case ARMOR -> "Hold clothing or armour in your other hand and use: +1 armour per tier";
            };
            tooltip.add(Component.literal(text).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Up to tier III; higher tiers need more Smithing").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.forEach(adder__);
    }
    }
}
