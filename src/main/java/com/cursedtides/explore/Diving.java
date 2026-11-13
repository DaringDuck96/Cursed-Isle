package com.cursedtides.explore;

import net.minecraft.world.item.equipment.ArmorType;
import com.cursedtides.CursedTides;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import java.util.EnumMap;
import java.util.List;

/**
 * The brass diving helmet. It holds a few minutes of air, breathed only while your head's under water, and
 * refills the moment you surface. Weighted boots (any boots, worn with it) let you walk the bottom faster.
 */
public final class Diving {
    private Diving() {}

    public static final int MAX_AIR = 240; // seconds

    private static ArmorMaterial material() {
        java.util.Map<ArmorType, Integer> defense = new java.util.EnumMap<>(ArmorType.class);
        for (ArmorType t : ArmorType.values()) defense.put(t, 2);
        return new ArmorMaterial(20, defense, 8, SoundEvents.ARMOR_EQUIP_IRON, 0f, 0f, net.minecraft.tags.ItemTags.REPAIRS_IRON_ARMOR,
                net.minecraft.resources.ResourceKey.create(net.minecraft.world.item.equipment.EquipmentAssets.ROOT_ID, CursedTides.id("diving")));
    }

    public static final ArmorMaterial MATERIAL = material();

    public static final Item HELMET = Registry.register(BuiltInRegistries.ITEM, com.cursedtides.util.Reg.itemId("diving_helmet"),
            new Item(com.cursedtides.util.Reg.props().humanoidArmor(MATERIAL, ArmorType.HELMET).durability(300).rarity(Rarity.UNCOMMON)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display__, java.util.function.Consumer<Component> adder__, TooltipFlag type) {
        java.util.List<Component> tooltip = new java.util.ArrayList<>();
                    tooltip.add(Component.literal("Air: " + air(stack) + "s of " + MAX_AIR + "s").withStyle(ChatFormatting.AQUA));
                    tooltip.add(Component.literal("Breathe under water; refills when you surface.").withStyle(ChatFormatting.GRAY));
        tooltip.forEach(adder__);
    }
            });

    static int air(ItemStack stack) {
        CompoundTag n = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return n.contains("Air") ? n.getIntOr("Air", 0) : MAX_AIR;
    }

    static void setAir(ItemStack stack, int air) {
        CompoundTag n = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        n.putInt("Air", air);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(n));
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(Diving::tick);
    }

    private static void tick(MinecraftServer server) {
        if (server.getTickCount() % 20 != 0) return;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            ItemStack head = p.getItemBySlot(EquipmentSlot.HEAD);
            if (!head.is(HELMET)) continue;
            int air = air(head);
            if (p.isUnderWater()) {
                if (air > 0) {
                    air--;
                    p.setAirSupply(p.getMaxAirSupply());
                    p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, true, false));
                    if (!p.getItemBySlot(EquipmentSlot.FEET).isEmpty()) {
                        p.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 40, 0, true, false));
                    }
                    if (air % 30 == 0 || air <= 10) {
                        p.sendSystemMessage(Component.literal("Air: " + air + "s").withStyle(air <= 30 ? ChatFormatting.RED : ChatFormatting.AQUA), true);
                    }
                    if (air == 30) p.level().playSound(null, p.blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 0.4f, 2f);
                    if (p.getY() < 40) com.cursedtides.activity.Achievements.grant(p, "deep_diver");
                }
            } else if (air < MAX_AIR) {
                air = Math.min(MAX_AIR, air + 15);
            }
            setAir(head, air);
        }
    }
}
