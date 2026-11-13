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
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
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
    public record Skills(List<Integer> levels, long chosen) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<Skills> ID = new CustomPacketPayload.Type<>(CursedTides.id("skills"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Skills> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), Skills::levels,
                ByteBufCodecs.VAR_LONG, Skills::chosen,
                Skills::new);
        @Override public Type<? extends CustomPacketPayload> type() { return ID; }
    }

    public record Choose(int tree, int tier, int option) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<Choose> ID = new CustomPacketPayload.Type<>(CursedTides.id("choose_perk"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Choose> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Choose::tree, ByteBufCodecs.VAR_INT, Choose::tier, ByteBufCodecs.VAR_INT, Choose::option,
                Choose::new);
        @Override public Type<? extends CustomPacketPayload> type() { return ID; }
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

    public static long chosen(Player player) {
        return player.getAttachedOrElse(CHOSEN, 0L);
    }

    public static List<Perk> perks(Player player) {
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
    public static double stat(Player player, Perk.Stat stat) {
        double total = 0;
        for (Perk p : perks(player)) {
            if (p.stat() == stat) total += p.amount();
        }
        return total;
    }

    // ---- choosing

    public static void sendSkills(ServerPlayer player) {
        List<Integer> levels = new ArrayList<>();
        for (Weapon w : Weapon.values()) levels.add(PirateData.level(player, w));
        ServerPlayNetworking.send(player, new Skills(levels, chosen(player)));
    }

    private static void choose(ServerPlayer player, Choose c) {
        if (c.tree() < 0 || c.tree() >= Weapon.values().length || c.tier() < 0 || c.tier() > 2 || c.option() < 0 || c.option() > 1) {
            return;
        }
        Weapon tree = Weapon.values()[c.tree()];
        long chosen = chosen(player);
        if (choice(chosen, tree, c.tier()) != 0) {
            player.displayClientMessage(Component.literal("You've already chosen at that tier. (/pirate respec to start over)").withStyle(ChatFormatting.GRAY), true);
        } else if (PirateData.level(player, tree) < Perk.TIER_LEVELS[c.tier()]) {
            player.displayClientMessage(Component.literal("Reach level " + Perk.TIER_LEVELS[c.tier()] + " first.").withStyle(ChatFormatting.GRAY), true);
        } else {
            long updated = chosen | ((long) (c.option() + 1) << (slot(tree, c.tier()) * 2));
            player.setAttached(CHOSEN, updated);
            Perk perk = Perk.of(tree, c.tier(), c.option());
            applyAttributes(player);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6f, 1.5f);
            if (perk != null) {
                player.displayClientMessage(Component.literal("Learned " + perk.title() + ": " + perk.effect()).withStyle(ChatFormatting.GOLD), false);
            }
        }
        sendSkills(player);
    }

    public static boolean respec(ServerPlayer player) {
        if (chosen(player) == 0L) return false;
        if (!player.getAbilities().instabuild) {
            if (player.getInventory().countItem(ModItems.DOUBLOON) < RESPEC_COST) return false;
            int left = RESPEC_COST;
            var inv = player.getInventory();
            for (int i = 0; i < inv.getContainerSize() && left > 0; i++) {
                var s = inv.getItem(i);
                if (s.is(ModItems.DOUBLOON)) {
                    int take = Math.min(left, s.getCount());
                    s.shrink(take);
                    left -= take;
                }
            }
        }
        player.setAttached(CHOSEN, 0L);
        applyAttributes(player);
        return true;
    }

    // ---- attribute-type perks

    public static void applyAttributes(ServerPlayer player) {
        set(player, Attributes.ATTACK_DAMAGE, "perk_melee", stat(player, Perk.Stat.MELEE));
        set(player, Attributes.MAX_HEALTH, "perk_health", stat(player, Perk.Stat.MAX_HEALTH));
        set(player, Attributes.MOVEMENT_SPEED, "perk_speed", stat(player, Perk.Stat.SPEED));
        set(player, Attributes.LUCK, "perk_luck", stat(player, Perk.Stat.LUCK));
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void set(Player player, Holder<Attribute> attribute, String key, double value) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        var id = CursedTides.id(key);
        instance.removeModifier(id);
        if (value != 0) {
            instance.addPermanentModifier(new AttributeModifier(id, value, AttributeModifier.Operation.ADD_VALUE));
        }
    }
}
