package com.cursedtides.combat;

import com.cursedtides.CursedTides;
import com.cursedtides.item.ModItems;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.List;

/**
 * Plunder the old way: a fallen foe leaves a loot container (a pouch, a sea chest, or for the very tough a skull
 * chest) instead of a scatter of junk. Open it and a loot window shows what's inside; take what you want, and
 * whatever's left goes into your pack when you close it.
 */
public final class Loot {
    private Loot() {}

    public enum Tier {
        POUCH("loot_pouch", "Loot Pouch", Rarity.COMMON, 2),
        CHEST("sea_chest", "Sea Chest", Rarity.UNCOMMON, 4),
        SKULL("skull_chest", "Skull Chest", Rarity.RARE, 6);

        public final String id, title;
        public final Rarity rarity;
        public final int rolls;

        Tier(String id, String title, Rarity rarity, int rolls) {
            this.id = id;
            this.title = title;
            this.rarity = rarity;
            this.rolls = rolls;
        }
    }

    public static class ContainerItem extends Item {
        final Tier tier;

        public ContainerItem(Settings s, Tier tier) {
            super(s);
            this.tier = tier;
        }

        @Override
        public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
            ItemStack stack = user.getStackInHand(hand);
            if (world.isClient || !(user instanceof ServerPlayerEntity p)) return TypedActionResult.success(stack, true);
            int level = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getInt("Level");
            stack.decrement(1);
            open(p, tier, Math.max(1, level));
            return TypedActionResult.success(stack, false);
        }

        @Override
        public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
            int level = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getInt("Level");
            if (level > 0) tooltip.add(Text.literal("Taken from a level " + level + " foe").formatted(Formatting.GRAY));
            tooltip.add(Text.literal("Use to open").formatted(Formatting.DARK_GRAY));
        }
    }

    public static final Item POUCH = reg(Tier.POUCH);
    public static final Item CHEST = reg(Tier.CHEST);
    public static final Item SKULL = reg(Tier.SKULL);

    private static Item reg(Tier t) {
        return Registry.register(Registries.ITEM, CursedTides.id(t.id), new ContainerItem(new Item.Settings().maxCount(16).rarity(t.rarity), t));
    }

    public static ItemStack container(Tier t, int level) {
        ItemStack s = new ItemStack(t == Tier.POUCH ? POUCH : t == Tier.CHEST ? CHEST : SKULL);
        NbtCompound n = new NbtCompound();
        n.putInt("Level", level);
        s.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(n));
        return s;
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(RegistryKey.of(RegistryKeys.ITEM_GROUP, CursedTides.id("main"))).register(e -> {
            e.add(POUCH);
            e.add(CHEST);
            e.add(SKULL);
        });
        ServerLivingEntityEvents.AFTER_DEATH.register(Loot::onDeath);
    }

    private static void onDeath(LivingEntity victim, DamageSource source) {
        if (!(source.getAttacker() instanceof ServerPlayerEntity) || !(victim.getWorld() instanceof ServerWorld w)) return;
        int lv = Levels.levelOf(victim);
        if (lv <= 0) return;
        boolean big = victim.hasCustomName() && victim.isCustomNameVisible(); // elites and bosses wear their names openly
        Random r = w.random;
        Tier t = null;
        if (big) t = lv >= 15 ? Tier.SKULL : Tier.CHEST;
        else if (lv >= 20 && r.nextFloat() < 0.03f) t = Tier.SKULL;
        else if (lv >= 6 && r.nextFloat() < 0.10f) t = Tier.CHEST;
        else if (r.nextFloat() < 0.40f) t = Tier.POUCH;
        if (t != null) victim.dropStack(container(t, lv));
    }

    /** Fill a loot window and show it; anything not taken is put in the pack when it closes. */
    private static void open(ServerPlayerEntity p, Tier tier, int level) {
        SimpleInventory inv = new SimpleInventory(9);
        Random r = p.getRandom();
        int coins = (1 + level / 3) * tier.rolls + r.nextInt(3 + level);
        inv.addStack(new ItemStack(ModItems.DOUBLOON, Math.min(64, Math.max(1, coins / 3))));
        for (int i = 0; i < tier.rolls; i++) {
            float roll = r.nextFloat();
            ItemStack s;
            if (roll < 0.30f) s = new ItemStack(ModItems.PISTOL_SHOT, 2 + r.nextInt(6));
            else if (roll < 0.45f) s = new ItemStack(Grenades.FIREBOMB, 1 + r.nextInt(2));
            else if (roll < 0.58f) s = com.cursedtides.explore.Collectibles.randomFor(p, null);
            else if (roll < 0.72f) s = new ItemStack(com.cursedtides.appearance.Accessories.random(r, tier != Tier.POUCH));
            else if (roll < 0.86f || tier == Tier.SKULL) s = GearRarity.randomWeapon(p.getServerWorld(), r, Math.min(5, 1 + level / 10 + tier.ordinal()));
            else s = new ItemStack(ModItems.DOUBLOON, 1 + r.nextInt(4));
            if (s != null && !s.isEmpty()) inv.addStack(s);
        }
        p.getWorld().playSound(null, p.getBlockPos(), SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.PLAYERS, 0.6f, 1.2f);
        p.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, playerInv, player) ->
                new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X1, syncId, playerInv, inv, 1) {
                    @Override
                    public void onClosed(PlayerEntity who) {
                        super.onClosed(who);
                        for (int i = 0; i < inv.size(); i++) {
                            ItemStack left = inv.removeStack(i);
                            if (!left.isEmpty()) who.getInventory().offerOrDrop(left);
                        }
                    }
                }, Text.literal(tier.title + " (Lv " + level + ")")));
    }
}
