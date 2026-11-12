package com.cursedtides.combat;

import com.cursedtides.item.FlintlockPistolItem;
import com.cursedtides.item.ModItems;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.random.Random;

import java.util.List;

/**
 * Found weapons come in grades: Crude, Common, Fine, Rare, Famed and Legendary. Better grades carry enchantments
 * and a maker's name; the grade shows in the item's colour and name.
 */
public final class GearRarity {
    private GearRarity() {}

    public enum Grade {
        CRUDE("Crude", Formatting.DARK_GRAY, 0), COMMON("", Formatting.WHITE, 0), FINE("Fine", Formatting.GREEN, 1),
        RARE("Rare", Formatting.BLUE, 2), FAMED("Famed", Formatting.LIGHT_PURPLE, 3), LEGENDARY("Legendary", Formatting.GOLD, 4);
        public final String prefix; public final Formatting colour; public final int power;
        Grade(String prefix, Formatting colour, int power) { this.prefix = prefix; this.colour = colour; this.power = power; }
    }

    private static final String[] MAKERS = {"Oriel Vance", "the Foundry at Kingsport", "Hask of Crooked Reef", "Mother Dace",
            "the Saint Vesper armoury", "a nameless Emberfall smith", "Tobias Wren", "the Brethren's forge"};
    private static final String[] LEGENDS = {"Widowmaker", "Saltbite", "The Last Word", "Gullscream", "Kingsbane", "Drowned Oath",
            "Stormcaller", "Moonhook", "Ninth Wave", "Old Faithful"};

    private static final List<Item> MELEE = List.of(ModItems.CUTLASS, ModItems.SABER, ModItems.RAPIER, ModItems.DAGGER,
            ModItems.DUAL_BLADES, ModItems.BROADSWORD, ModItems.WAR_HAMMER, ModItems.BOARDING_AXE);
    private static final List<Item> GUNS = List.of(ModItems.FLINTLOCK_PISTOL, ModItems.DOUBLE_BARREL_PISTOL, ModItems.MUSKET,
            ModItems.BLUNDERBUSS, ModItems.HAND_CANNON);

    public static ItemStack legendaryOf(ServerWorld world, net.minecraft.item.Item item, Random r) {
        ItemStack stack = new ItemStack(item);
        apply(world, stack, Grade.LEGENDARY, r);
        return stack;
    }

    public static boolean isLegendary(ItemStack stack) {
        return stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, net.minecraft.component.type.NbtComponent.DEFAULT)
                .copyNbt().getString("Grade").equals("LEGENDARY");
    }

    /** Roll a grade; luck shifts the odds upward (0 for a common chest, 2-3 for a boss). */
    public static Grade roll(Random r, int luck) {
        int x = r.nextInt(100) + luck * 12;
        if (x >= 118) return Grade.LEGENDARY;
        if (x >= 100) return Grade.FAMED;
        if (x >= 82) return Grade.RARE;
        if (x >= 55) return Grade.FINE;
        if (x >= 12) return Grade.COMMON;
        return Grade.CRUDE;
    }

    public static ItemStack randomWeapon(ServerWorld world, Random r, int luck) {
        boolean gun = r.nextInt(3) == 0;
        List<Item> pool = gun ? GUNS : MELEE;
        ItemStack stack = new ItemStack(pool.get(r.nextInt(pool.size())));
        apply(world, stack, roll(r, luck), r);
        return stack;
    }

    public static void apply(ServerWorld world, ItemStack stack, Grade g, Random r) {
        boolean gun = stack.getItem() instanceof FlintlockPistolItem;
        var reg = world.getRegistryManager().get(RegistryKeys.ENCHANTMENT);
        java.util.function.BiConsumer<RegistryKey<Enchantment>, Integer> ench = (key, lvl) ->
                reg.getEntry(key).ifPresent(e -> stack.addEnchantment(e, lvl));
        if (g.power >= 1) ench.accept(Enchantments.UNBREAKING, Math.min(3, g.power));
        if (!gun && g.power >= 1) ench.accept(Enchantments.SHARPNESS, g.power + (g == Grade.LEGENDARY ? 1 : 0));
        if (!gun && g.power >= 3) ench.accept(r.nextBoolean() ? Enchantments.FIRE_ASPECT : Enchantments.LOOTING, g.power >= 4 ? 2 : 1);
        if (g.power >= 4) ench.accept(Enchantments.MENDING, 1);
        if (g == Grade.CRUDE && stack.isDamageable()) stack.setDamage(stack.getMaxDamage() / 2);

        Rarity rarity = switch (g) { case LEGENDARY -> Rarity.EPIC; case FAMED, RARE -> Rarity.RARE; case FINE -> Rarity.UNCOMMON; default -> Rarity.COMMON; };
        stack.set(DataComponentTypes.RARITY, rarity);
        net.minecraft.nbt.NbtCompound tag = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, net.minecraft.component.type.NbtComponent.DEFAULT).copyNbt();
        tag.putString("Grade", g.name());
        stack.set(DataComponentTypes.CUSTOM_DATA, net.minecraft.component.type.NbtComponent.of(tag));
        String base = stack.getItem().getName().getString();
        String name = g == Grade.LEGENDARY ? LEGENDS[r.nextInt(LEGENDS.length)] + " (" + base + ")"
                : g.prefix.isEmpty() ? base : g.prefix + " " + base;
        if (g != Grade.COMMON) stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name).formatted(g.colour).styled(s -> s.withItalic(false)));
        if (g.power >= 2) {
            stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                    Text.literal(g.name().charAt(0) + g.name().substring(1).toLowerCase() + " grade").formatted(g.colour),
                    Text.literal("Made by " + MAKERS[r.nextInt(MAKERS.length)]).formatted(Formatting.DARK_GRAY, Formatting.ITALIC))));
        }
    }
}
