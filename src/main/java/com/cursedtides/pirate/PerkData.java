package com.cursedtides.pirate;

import com.cursedtides.CursedTides;
import com.cursedtides.item.ModItems;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/**
 * Which perks a player has chosen: 2 bits per (proficiency, tier) slot in one long
 * (0 = unchosen, 1 = option A, 2 = option B). Stat-type perks are applied as attribute
 * modifiers; the rest are read where they matter.
 */
public final class PerkData {
    private PerkData() {}

    public static final int RESPEC_COST = 32;

    public static final AttachmentType<Long> CHOSEN = AttachmentRegistry.<Long>builder()
            .persistent(Codec.LONG).copyOnDeath().initializer(() -> 0L)
            .buildAndRegister(CursedTides.id("perks"));

    // ---- payloads
    public record Skills(List<Integer> levels, long chosen) implements CustomPayload {
        public static final CustomPayload.Id<Skills> ID = new CustomPayload.Id<>(CursedTides.id("skills"));
        public static final PacketCodec<RegistryByteBuf, Skills> CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_INT.collect(PacketCodecs.toList()), Skills::levels,
                PacketCodecs.VAR_LONG, Skills::chosen,
                Skills::new);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public record Choose(int tree, int tier, int option) implements CustomPayload {
        public static final CustomPayload.Id<Choose> ID = new CustomPayload.Id<>(CursedTides.id("choose_perk"));
        public static final PacketCodec<RegistryByteBuf, Choose> CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_INT, Choose::tree, PacketCodecs.VAR_INT, Choose::tier, PacketCodecs.VAR_INT, Choose::option,
                Choose::new);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(Skills.ID, Skills.CODEC);
        PayloadTypeRegistry.playC2S().register(Choose.ID, Choose.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Choose.ID, (payload, context) ->
                context.server().execute(() -> choose(context.player(), payload)));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> applyAttributes(handler.getPlayer()));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> applyAttributes(newPlayer));
    }

    // ---- reading

    private static int slot(Weapon tree, int tier) {
        return tree.ordinal() * 3 + tier;
    }

    public static int choice(long chosen, Weapon tree, int tier) {
        return (int) ((chosen >>> (slot(tree, tier) * 2)) & 3L);
    }

    public static long chosen(PlayerEntity player) {
        return player.getAttachedOrElse(CHOSEN, 0L);
    }

    public static List<Perk> perks(PlayerEntity player) {
        long chosen = chosen(player);
        List<Perk> list = new ArrayList<>();
        for (Weapon tree : Weapon.values()) {
            for (int tier = 0; tier < 3; tier++) {
                int c = choice(chosen, tree, tier);
                if (c > 0) {
                    Perk p = Perk.of(tree, tier, c - 1);
                    if (p != null) list.add(p);
                }
            }
        }
        return list;
    }

    /** Sum of a stat across the player's chosen perks. */
    public static double stat(PlayerEntity player, Perk.Stat stat) {
        double total = 0;
        for (Perk p : perks(player)) {
            if (p.stat() == stat) total += p.amount();
        }
        return total;
    }

    // ---- choosing

    public static void sendSkills(ServerPlayerEntity player) {
        List<Integer> levels = new ArrayList<>();
        for (Weapon w : Weapon.values()) levels.add(PirateData.level(player, w));
        ServerPlayNetworking.send(player, new Skills(levels, chosen(player)));
    }

    private static void choose(ServerPlayerEntity player, Choose c) {
        if (c.tree() < 0 || c.tree() >= Weapon.values().length || c.tier() < 0 || c.tier() > 2 || c.option() < 0 || c.option() > 1) {
            return;
        }
        Weapon tree = Weapon.values()[c.tree()];
        long chosen = chosen(player);
        if (choice(chosen, tree, c.tier()) != 0) {
            player.sendMessage(Text.literal("You've already chosen at that tier. (/pirate respec to start over)").formatted(Formatting.GRAY), true);
        } else if (PirateData.level(player, tree) < Perk.TIER_LEVELS[c.tier()]) {
            player.sendMessage(Text.literal("Reach level " + Perk.TIER_LEVELS[c.tier()] + " first.").formatted(Formatting.GRAY), true);
        } else {
            long updated = chosen | ((long) (c.option() + 1) << (slot(tree, c.tier()) * 2));
            player.setAttached(CHOSEN, updated);
            Perk perk = Perk.of(tree, c.tier(), c.option());
            applyAttributes(player);
            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.6f, 1.5f);
            if (perk != null) {
                player.sendMessage(Text.literal("Learned " + perk.title() + ": " + perk.effect()).formatted(Formatting.GOLD), false);
            }
        }
        sendSkills(player);
    }

    public static boolean respec(ServerPlayerEntity player) {
        if (chosen(player) == 0L) return false;
        if (!player.getAbilities().creativeMode) {
            if (player.getInventory().count(ModItems.DOUBLOON) < RESPEC_COST) return false;
            int left = RESPEC_COST;
            var inv = player.getInventory();
            for (int i = 0; i < inv.size() && left > 0; i++) {
                var s = inv.getStack(i);
                if (s.isOf(ModItems.DOUBLOON)) {
                    int take = Math.min(left, s.getCount());
                    s.decrement(take);
                    left -= take;
                }
            }
        }
        player.setAttached(CHOSEN, 0L);
        applyAttributes(player);
        return true;
    }

    // ---- attribute-type perks

    public static void applyAttributes(ServerPlayerEntity player) {
        set(player, EntityAttributes.GENERIC_ATTACK_DAMAGE, "perk_melee", stat(player, Perk.Stat.MELEE));
        set(player, EntityAttributes.GENERIC_MAX_HEALTH, "perk_health", stat(player, Perk.Stat.MAX_HEALTH));
        set(player, EntityAttributes.GENERIC_MOVEMENT_SPEED, "perk_speed", stat(player, Perk.Stat.SPEED));
        set(player, EntityAttributes.GENERIC_LUCK, "perk_luck", stat(player, Perk.Stat.LUCK));
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void set(PlayerEntity player, RegistryEntry<EntityAttribute> attribute, String key, double value) {
        EntityAttributeInstance instance = player.getAttributeInstance(attribute);
        if (instance == null) return;
        var id = CursedTides.id(key);
        instance.removeModifier(id);
        if (value != 0) {
            instance.addPersistentModifier(new EntityAttributeModifier(id, value, EntityAttributeModifier.Operation.ADD_VALUE));
        }
    }
}
