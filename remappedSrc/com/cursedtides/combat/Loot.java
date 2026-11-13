package com.cursedtides.combat;

import com.cursedtides.CursedTides;
import com.cursedtides.item.ModItems;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
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

        public ContainerItem(Properties s, Tier tier) {
            super(s);
            this.tier = tier;
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
            ItemStack stack = user.getItemInHand(hand);
            if (world.isClientSide || !(user instanceof ServerPlayer p)) return InteractionResultHolder.sidedSuccess(stack, true);
            int level = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("Level");
            stack.shrink(1);
            open(p, tier, Math.max(1, level));
            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
            int level = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("Level");
            if (level > 0) tooltip.add(Component.literal("Taken from a level " + level + " foe").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Use to open").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    public static final Item POUCH = reg(Tier.POUCH);
    public static final Item CHEST = reg(Tier.CHEST);
    public static final Item SKULL = reg(Tier.SKULL);

    private static Item reg(Tier t) {
        return Registry.register(BuiltInRegistries.ITEM, CursedTides.id(t.id), new ContainerItem(new Item.Properties().stacksTo(16).rarity(t.rarity), t));
    }

    public static ItemStack container(Tier t, int level) {
        ItemStack s = new ItemStack(t == Tier.POUCH ? POUCH : t == Tier.CHEST ? CHEST : SKULL);
        CompoundTag n = new CompoundTag();
        n.putInt("Level", level);
        s.set(DataComponents.CUSTOM_DATA, CustomData.of(n));
        return s;
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB, CursedTides.id("main"))).register(e -> {
            e.accept(POUCH);
            e.accept(CHEST);
            e.accept(SKULL);
        });
        ServerLivingEntityEvents.AFTER_DEATH.register(Loot::onDeath);
    }

    private static void onDeath(LivingEntity victim, DamageSource source) {
        if (!(source.getEntity() instanceof ServerPlayer) || !(victim.level() instanceof ServerLevel w)) return;
        int lv = Levels.levelOf(victim);
        if (lv <= 0) return;
        boolean big = victim.hasCustomName() && victim.isCustomNameVisible(); // elites and bosses wear their names openly
        RandomSource r = w.random;
        Tier t = null;
        if (big) t = lv >= 15 ? Tier.SKULL : Tier.CHEST;
        else if (lv >= 20 && r.nextFloat() < 0.03f) t = Tier.SKULL;
        else if (lv >= 6 && r.nextFloat() < 0.10f) t = Tier.CHEST;
        else if (r.nextFloat() < 0.40f) t = Tier.POUCH;
        if (t != null) victim.spawnAtLocation(container(t, lv));
    }

    /** Fill a loot window and show it; anything not taken is put in the pack when it closes. */
    private static void open(ServerPlayer p, Tier tier, int level) {
        SimpleContainer inv = new SimpleContainer(9);
        RandomSource r = p.getRandom();
        int coins = (1 + level / 3) * tier.rolls + r.nextInt(3 + level);
        inv.addItem(new ItemStack(ModItems.DOUBLOON, Math.min(64, Math.max(1, coins / 3))));
        for (int i = 0; i < tier.rolls; i++) {
            float roll = r.nextFloat();
            ItemStack s;
            if (roll < 0.30f) s = new ItemStack(ModItems.PISTOL_SHOT, 2 + r.nextInt(6));
            else if (roll < 0.45f) s = new ItemStack(Grenades.FIREBOMB, 1 + r.nextInt(2));
            else if (roll < 0.58f) s = com.cursedtides.explore.Collectibles.randomFor(p, null);
            else if (roll < 0.72f) s = new ItemStack(com.cursedtides.appearance.Accessories.random(r, tier != Tier.POUCH));
            else if (roll < 0.86f || tier == Tier.SKULL) s = GearRarity.randomWeapon(p.serverLevel(), r, Math.min(5, 1 + level / 10 + tier.ordinal()));
            else s = new ItemStack(ModItems.DOUBLOON, 1 + r.nextInt(4));
            if (s != null && !s.isEmpty()) inv.addItem(s);
        }
        // Now and then, a letter: the start of one of the found tales
        if (r.nextFloat() < (tier == Tier.POUCH ? 0.03f : 0.2f)) {
            ItemStack letter = com.cursedtides.quest.Letters.forLoot(p);
            if (!letter.isEmpty()) inv.addItem(letter);
        }
        p.level().playSound(null, p.blockPosition(), SoundEvents.CHEST_OPEN, SoundSource.PLAYERS, 0.6f, 1.2f);
        p.openMenu(new SimpleMenuProvider((syncId, playerInv, player) ->
                new ChestMenu(MenuType.GENERIC_9x1, syncId, playerInv, inv, 1) {
                    @Override
                    public void removed(Player who) {
                        super.removed(who);
                        for (int i = 0; i < inv.getContainerSize(); i++) {
                            ItemStack left = inv.removeItemNoUpdate(i);
                            if (!left.isEmpty()) who.getInventory().placeItemBackInInventory(left);
                        }
                    }
                }, Component.literal(tier.title + " (Lv " + level + ")")));
    }
}
