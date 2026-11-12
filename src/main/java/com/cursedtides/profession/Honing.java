package com.cursedtides.profession;

import com.cursedtides.CursedTides;
import com.cursedtides.item.FlintlockPistolItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

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
        NbtComponent c = stack.get(DataComponentTypes.CUSTOM_DATA);
        return c == null ? 0 : c.copyNbt().getInt("Honed");
    }

    /** Damage multiplier for a rifled firearm. */
    public static float gunBonus(ItemStack gun) {
        return 1.0f + 0.10f * tier(gun);
    }

    static Kind kindOf(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof FlintlockPistolItem) return Kind.FIREARM;
        if (item instanceof SwordItem || item instanceof AxeItem) return Kind.BLADE;
        if (item instanceof ArmorItem) return Kind.ARMOR;
        return null;
    }

    public static int maxTier(PlayerEntity player) {
        int level = ProfessionData.level(player, Profession.SMITHING);
        return level >= 20 ? 3 : level >= 10 ? 2 : 1;
    }

    static void apply(ServerPlayerEntity player, ItemStack kitStack, Kind kind, ItemStack target) {
        if (kindOf(target) != kind) {
            String what = switch (kind) { case BLADE -> "a blade"; case FIREARM -> "a firearm"; case ARMOR -> "clothing or armour"; };
            player.sendMessage(Text.literal("Hold " + what + " in your other hand.").formatted(Formatting.GRAY), true);
            return;
        }
        int tier = tier(target);
        int cap = maxTier(player);
        if (tier >= 3) {
            player.sendMessage(Text.literal("That's as fine as it'll ever be.").formatted(Formatting.GRAY), true);
            return;
        }
        if (tier >= cap) {
            player.sendMessage(Text.literal("Tier " + NUMERALS[tier + 1] + " needs Smithing " + (tier == 1 ? 10 : 20) + ".")
                    .formatted(Formatting.RED), true);
            return;
        }
        int next = tier + 1;
        ProfessionItems.setInt(target, "Honed", next);
        if (kind != Kind.FIREARM) {
            AttributeModifiersComponent base = target.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
            if (base.modifiers().isEmpty()) base = target.getItem().getAttributeModifiers();
            AttributeModifiersComponent.Builder b = AttributeModifiersComponent.builder();
            for (AttributeModifiersComponent.Entry e : base.modifiers()) {
                if (!e.modifier().id().equals(MOD_ID)) b.add(e.attribute(), e.modifier(), e.slot());
            }
            if (kind == Kind.BLADE) {
                b.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(MOD_ID, next,
                        EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND);
            } else {
                ArmorItem armor = (ArmorItem) target.getItem();
                b.add(EntityAttributes.GENERIC_ARMOR, new EntityAttributeModifier(MOD_ID, next,
                        EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.forEquipmentSlot(armor.getSlotType()));
            }
            target.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, b.build());
        }
        String label = switch (kind) { case BLADE -> "Honed "; case FIREARM -> "Rifled "; case ARMOR -> "Reinforced "; };
        target.set(DataComponentTypes.LORE, new LoreComponent(List.of(Text.literal(label + NUMERALS[next])
                .formatted(Formatting.AQUA).styled(s -> s.withItalic(false)))));
        if (!player.getAbilities().creativeMode) kitStack.decrement(1);
        player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.PLAYERS, 0.8f, 1.1f);
        player.sendMessage(Text.literal(label + NUMERALS[next] + ": ").append(target.getName()).formatted(Formatting.GOLD), true);
        ProfessionData.addXp(player, Profession.SMITHING, 10 + next * 10);
    }

    /** Use with the item to improve held in the other hand. */
    public static class KitItem extends Item {
        private final Kind kind;

        public KitItem(Kind kind, Settings settings) {
            super(settings);
            this.kind = kind;
        }

        @Override
        public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
            ItemStack kit = user.getStackInHand(hand);
            ItemStack target = user.getStackInHand(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND);
            if (!world.isClient && user instanceof ServerPlayerEntity sp) {
                apply(sp, kit, kind, target);
            }
            return TypedActionResult.success(kit, world.isClient);
        }

        @Override
        public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
            String text = switch (kind) {
                case BLADE -> "Hold a blade in your other hand and use: +1 attack damage per tier";
                case FIREARM -> "Hold a firearm in your other hand and use: +10% damage per tier";
                case ARMOR -> "Hold clothing or armour in your other hand and use: +1 armour per tier";
            };
            tooltip.add(Text.literal(text).formatted(Formatting.GRAY));
            tooltip.add(Text.literal("Up to tier III; higher tiers need more Smithing").formatted(Formatting.DARK_GRAY));
        }
    }
}
