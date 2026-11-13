package com.cursedtides.util;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Small helpers that smooth over API changes between Minecraft versions. */
public final class Compat {
    private Compat() {}

    /** Drop a stack where an entity stands (server side only). */
    public static ItemEntity drop(Entity e, ItemStack stack) {
        if (e.level() instanceof ServerLevel sl) return e.spawnAtLocation(sl, stack);
        return null;
    }

    /** The old three-argument knockback. */
    public static void knock(net.minecraft.world.entity.LivingEntity e, double power, double x, double z) {
        e.knockback(power, x, z, e.damageSources().generic(), 0f);
    }

    /** Swing an arm, for anyone. */
    public static void swing(net.minecraft.world.entity.LivingEntity e, net.minecraft.world.InteractionHand hand) {
        e.swing(hand, net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
    }

    public static void swing(net.minecraft.world.entity.LivingEntity e, net.minecraft.world.InteractionHand hand, boolean ignored) {
        swing(e, hand);
    }

    public static void teleport(Entity e, ServerLevel level, double x, double y, double z, java.util.Set<?> ignored, float yaw, float pitch) {
        teleport(e, level, x, y, z, yaw, pitch);
    }

    /** Take up to n matching items from a player's inventory. */
    public static int take(net.minecraft.world.entity.player.Player p, java.util.function.Predicate<net.minecraft.world.item.ItemStack> pred, int n) {
        return p.getInventory().clearOrCountMatchingItems(pred, false, n, p.inventoryMenu.getCraftSlots());
    }

    public static net.minecraft.nbt.Tag saveStack(net.minecraft.world.item.ItemStack s, net.minecraft.core.HolderLookup.Provider lookup) {
        return net.minecraft.world.item.ItemStack.CODEC.encodeStart(lookup.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), s).getOrThrow();
    }

    public static java.util.Optional<net.minecraft.world.item.ItemStack> loadStack(net.minecraft.core.HolderLookup.Provider lookup, net.minecraft.nbt.Tag t) {
        if (t == null) return java.util.Optional.empty();
        return net.minecraft.world.item.ItemStack.CODEC.parse(lookup.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), t).result();
    }

    /** Teleport, keeping things simple. */
    public static void teleport(Entity e, ServerLevel level, double x, double y, double z, float yaw, float pitch) {
        e.teleportTo(level, x, y, z, java.util.Set.of(), yaw, pitch, false);
    }

    /** Make a sound at an entity, heard by everyone near. */
    public static void sound(Entity e, net.minecraft.sounds.SoundEvent sound, net.minecraft.sounds.SoundSource source, float vol, float pitch) {
        e.level().playSound(null, e.getX(), e.getY(), e.getZ(), sound, source, vol, pitch);
    }

    /** A container's stacks as a list tag (slot numbers kept), for deeds and saves. */
    public static net.minecraft.nbt.ListTag saveItems(net.minecraft.world.Container c, net.minecraft.core.HolderLookup.Provider reg) {
        net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
        var ops = reg.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);
        for (int i = 0; i < c.getContainerSize(); i++) {
            ItemStack st = c.getItem(i);
            if (st.isEmpty()) continue;
            final int slot = i;
            ItemStack.CODEC.encodeStart(ops, st).result().ifPresent(t -> {
                net.minecraft.nbt.CompoundTag e = new net.minecraft.nbt.CompoundTag();
                e.putInt("Slot", slot);
                e.put("Item", t);
                list.add(e);
            });
        }
        return list;
    }

    public static void loadItems(net.minecraft.world.Container c, net.minecraft.nbt.ListTag list, net.minecraft.core.HolderLookup.Provider reg) {
        var ops = reg.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);
        for (int i = 0; i < c.getContainerSize(); i++) c.setItem(i, ItemStack.EMPTY);
        for (var t : list) {
            if (!(t instanceof net.minecraft.nbt.CompoundTag e)) continue;
            int slot = e.getIntOr("Slot", -1);
            if (slot < 0 || slot >= c.getContainerSize()) continue;
            ItemStack.CODEC.parse(ops, e.get("Item")).result().ifPresent(st -> c.setItem(slot, st));
        }
    }

    /** Hurt something (server side), returning whether it took the hit. */
    public static boolean hurt(Entity e, net.minecraft.world.damagesource.DamageSource src, float amount) {
        return e.level() instanceof ServerLevel sl && e.hurtServer(sl, src, amount);
    }

    /** A chat colour by its lower-case name (red, dark_aqua...), or null. */
    public static net.minecraft.ChatFormatting colorByName(String name) {
        for (net.minecraft.ChatFormatting f : net.minecraft.ChatFormatting.values()) if (f.name().equalsIgnoreCase(name)) return f;
        return null;
    }

    /** Whether this formatting is a colour (the first sixteen are). */
    public static boolean isColor(net.minecraft.ChatFormatting f) {
        return f != null && f.ordinal() < 16;
    }

    public static String nameOf(net.minecraft.ChatFormatting f) {
        return f.name().toLowerCase(java.util.Locale.ROOT);
    }

    /** Swords, axes and our own blades. */
    public static boolean isBlade(ItemStack stack) {
        var i = stack.getItem();
        return stack.is(net.minecraft.tags.ItemTags.SWORDS) || stack.is(net.minecraft.tags.ItemTags.AXES)
                || i instanceof com.cursedtides.item.CutlassItem || i instanceof com.cursedtides.item.DualBladesItem
                || i instanceof com.cursedtides.item.ParryingBladeItem || i instanceof com.cursedtides.item.HeavyWeaponItem
                || i instanceof com.cursedtides.item.BoardingAxeItem;
    }

    /** A chat line (or, with overlay, the action-bar line) for any player. */
    public static void tell(Player p, Component msg, boolean overlay) {
        if (p instanceof ServerPlayer sp) sp.sendSystemMessage(msg, overlay);
        else if (overlay) p.sendOverlayMessage(msg);
        else p.sendSystemMessage(msg);
    }
}
