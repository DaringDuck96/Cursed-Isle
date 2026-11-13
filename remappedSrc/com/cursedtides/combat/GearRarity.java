package com.cursedtides.combat;

import com.cursedtides.item.FlintlockPistolItem;
import com.cursedtides.item.ModItems;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * Found weapons come in grades: Crude, Common, Fine, Rare, Famed and Legendary. Better grades carry enchantments
 * and a maker's name; the grade shows in the item's colour and name.
 */
public final class GearRarity {
    private GearRarity() {}

    public enum Grade {
        CRUDE("Crude", ChatFormatting.DARK_GRAY, 0), COMMON("", ChatFormatting.WHITE, 0), FINE("Fine", ChatFormatting.GREEN, 1),
        RARE("Rare", ChatFormatting.BLUE, 2), FAMED("Famed", ChatFormatting.LIGHT_PURPLE, 3), LEGENDARY("Legendary", ChatFormatting.GOLD, 4);
        public final String prefix; public final ChatFormatting colour; public final int power;
        Grade(String prefix, ChatFormatting colour, int power) { this.prefix = prefix; this.colour = colour; this.power = power; }
    }

    private static final String[] MAKERS = {"Oriel Vance", "the Foundry at Kingsport", "Hask of Crooked Reef", "Mother Dace",
            "the Saint Vesper armoury", "a nameless Emberfall smith", "Tobias Wren", "the Brethren's forge"};
    private static final String[] LEGENDS = {"Widowmaker", "Saltbite", "The Last Word", "Gullscream", "Kingsbane", "Drowned Oath",
            "Stormcaller", "Moonhook", "Ninth Wave", "Old Faithful"};

    private static final List<Item> MELEE = List.of(ModItems.CUTLASS, ModItems.SABER, ModItems.RAPIER, ModItems.DAGGER,
            ModItems.DUAL_BLADES, ModItems.BROADSWORD, ModItems.WAR_HAMMER, ModItems.BOARDING_AXE);
    private static final List<Item> GUNS = List.of(ModItems.FLINTLOCK_PISTOL, ModItems.DOUBLE_BARREL_PISTOL, ModItems.MUSKET, ModItems.REPEATER_PISTOL, ModItems.BAYONET_MUSKET,
            ModItems.BLUNDERBUSS, ModItems.HAND_CANNON);

    public static ItemStack legendaryOf(ServerLevel world, net.minecraft.world.item.Item item, RandomSource r) {
        ItemStack stack = new ItemStack(item);
        apply(world, stack, Grade.LEGENDARY, r);
        return stack;
    }

    public static boolean isLegendary(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY)
                .copyTag().getString("Grade").equals("LEGENDARY");
    }

    /** Roll a grade; luck shifts the odds upward (0 for a common chest, 2-3 for a boss). */
    public static Grade roll(RandomSource r, int luck) {
        int x = r.nextInt(100) + luck * 12;
        if (x >= 118) return Grade.LEGENDARY;
        if (x >= 100) return Grade.FAMED;
        if (x >= 82) return Grade.RARE;
        if (x >= 55) return Grade.FINE;
        if (x >= 12) return Grade.COMMON;
        return Grade.CRUDE;
    }

    public static ItemStack randomWeapon(ServerLevel world, RandomSource r, int luck) {
        boolean gun = r.nextInt(3) == 0;
        List<Item> pool = gun ? GUNS : MELEE;
        ItemStack stack = new ItemStack(pool.get(r.nextInt(pool.size())));
        apply(world, stack, roll(r, luck), r);
        return stack;
    }

    public static void apply(ServerLevel world, ItemStack stack, Grade g, RandomSource r) {
        boolean gun = stack.getItem() instanceof FlintlockPistolItem;
        var reg = world.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        java.util.function.BiConsumer<ResourceKey<Enchantment>, Integer> ench = (key, lvl) ->
                reg.getHolder(key).ifPresent(e -> stack.enchant(e, lvl));
        if (g.power >= 1) ench.accept(Enchantments.UNBREAKING, Math.min(3, g.power));
        if (!gun && g.power >= 1) ench.accept(Enchantments.SHARPNESS, g.power + (g == Grade.LEGENDARY ? 1 : 0));
        if (!gun && g.power >= 3) ench.accept(r.nextBoolean() ? Enchantments.FIRE_ASPECT : Enchantments.LOOTING, g.power >= 4 ? 2 : 1);
        if (g.power >= 4) ench.accept(Enchantments.MENDING, 1);
        if (g == Grade.CRUDE && stack.isDamageableItem()) stack.setDamageValue(stack.getMaxDamage() / 2);

        Rarity rarity = switch (g) { case LEGENDARY -> Rarity.EPIC; case FAMED, RARE -> Rarity.RARE; case FINE -> Rarity.UNCOMMON; default -> Rarity.COMMON; };
        stack.set(DataComponents.RARITY, rarity);
        net.minecraft.nbt.CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        tag.putString("Grade", g.name());
        stack.set(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
        String base = stack.getItem().getDescription().getString();
        String name = g == Grade.LEGENDARY ? LEGENDS[r.nextInt(LEGENDS.length)] + " (" + base + ")"
                : g.prefix.isEmpty() ? base : g.prefix + " " + base;
        if (g != Grade.COMMON) stack.set(DataComponents.CUSTOM_NAME, Component.literal(name).withStyle(g.colour).withStyle(s -> s.withItalic(false)));
        if (g.power >= 2) {
            stack.set(DataComponents.LORE, new ItemLore(List.of(
                    Component.literal(g.name().charAt(0) + g.name().substring(1).toLowerCase() + " grade").withStyle(g.colour),
                    Component.literal("Made by " + MAKERS[r.nextInt(MAKERS.length)]).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC))));
        }
    }
}
