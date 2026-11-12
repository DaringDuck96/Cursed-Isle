package com.cursedtides.explore;

import com.cursedtides.CursedTides;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import java.util.EnumMap;
import java.util.List;

/**
 * The brass diving helmet. It holds a few minutes of air, breathed only while your head's under water, and
 * refills the moment you surface. Weighted boots (any boots, worn with it) let you walk the bottom faster.
 */
public final class Diving {
    private Diving() {}

    public static final int MAX_AIR = 240; // seconds

    private static RegistryEntry<ArmorMaterial> material() {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        for (ArmorItem.Type t : ArmorItem.Type.values()) defense.put(t, 2);
        Identifier id = CursedTides.id("diving");
        return Registry.registerReference(Registries.ARMOR_MATERIAL, id, new ArmorMaterial(defense, 8,
                SoundEvents.ITEM_ARMOR_EQUIP_IRON, () -> Ingredient.ofItems(Items.COPPER_INGOT), List.of(new ArmorMaterial.Layer(id)), 0f, 0f));
    }

    public static final RegistryEntry<ArmorMaterial> MATERIAL = material();

    public static final Item HELMET = Registry.register(Registries.ITEM, CursedTides.id("diving_helmet"),
            new ArmorItem(MATERIAL, ArmorItem.Type.HELMET, new Item.Settings().maxDamage(300).rarity(Rarity.UNCOMMON)) {
                @Override
                public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
                    tooltip.add(Text.literal("Air: " + air(stack) + "s of " + MAX_AIR + "s").formatted(Formatting.AQUA));
                    tooltip.add(Text.literal("Breathe under water; refills when you surface.").formatted(Formatting.GRAY));
                }
            });

    static int air(ItemStack stack) {
        NbtCompound n = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        return n.contains("Air") ? n.getInt("Air") : MAX_AIR;
    }

    static void setAir(ItemStack stack, int air) {
        NbtCompound n = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        n.putInt("Air", air);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(n));
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(Diving::tick);
    }

    private static void tick(MinecraftServer server) {
        if (server.getTicks() % 20 != 0) return;
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            ItemStack head = p.getEquippedStack(EquipmentSlot.HEAD);
            if (!head.isOf(HELMET)) continue;
            int air = air(head);
            if (p.isSubmergedInWater()) {
                if (air > 0) {
                    air--;
                    p.setAir(p.getMaxAir());
                    p.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 260, 0, true, false));
                    if (!p.getEquippedStack(EquipmentSlot.FEET).isEmpty()) {
                        p.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, 40, 0, true, false));
                    }
                    if (air % 30 == 0 || air <= 10) {
                        p.sendMessage(Text.literal("Air: " + air + "s").formatted(air <= 30 ? Formatting.RED : Formatting.AQUA), true);
                    }
                    if (air == 30) p.getWorld().playSound(null, p.getBlockPos(), SoundEvents.BLOCK_BELL_USE, SoundCategory.PLAYERS, 0.4f, 2f);
                    if (p.getY() < 40) com.cursedtides.activity.Achievements.grant(p, "deep_diver");
                }
            } else if (air < MAX_AIR) {
                air = Math.min(MAX_AIR, air + 15);
            }
            setAir(head, air);
        }
    }
}
