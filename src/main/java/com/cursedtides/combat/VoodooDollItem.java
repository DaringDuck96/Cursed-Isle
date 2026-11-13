package com.cursedtides.combat;

import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

/**
 * The voodoo doll. Use it on a creature or a person to attune to them (up to three at once). Then use the doll in
 * the air to work on everyone you're attuned to at a distance: foes are hurt, bound and drained; friends are healed and
 * cured. Sneak and use to choose the working. Trains Hexes; costs voodoo.
 */
public class VoodooDollItem extends Item {
    public enum Working {
        POKE("Poke", 0, 8, "Stab the doll: hurts every attuned foe."),
        HEAL("Mend", 0, 12, "Stitch the doll: heals every attuned friend."),
        SWARM("Swarm", 8, 18, "Insects pour from the doll: poison and slow attuned foes."),
        CURE("Cure", 12, 14, "Unpick the curse: clears ill effects from attuned friends."),
        SHACKLES("Shackles", 16, 22, "Bind the doll's limbs: attuned foes can barely move."),
        DRAIN("Life Drain", 22, 26, "Squeeze the doll: drain attuned foes and heal yourself.");

        public final String title; public final int level, cost; public final String text;
        Working(String title, int level, int cost, String text) { this.title = title; this.level = level; this.cost = cost; this.text = text; }
    }

    private static final Map<UUID, List<UUID>> ATTUNED = new HashMap<>();
    private static final int RANGE = 32;

    public VoodooDollItem(Properties settings) {
        super(settings);
    }

    static Working working(ItemStack doll) {
        int i = doll.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getIntOr("Working", 0);
        Working[] all = Working.values();
        return all[Math.floorMod(i, all.length)];
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
        if (user.level().isClientSide()) return InteractionResult.SUCCESS;
        List<UUID> list = ATTUNED.computeIfAbsent(user.getUUID(), k -> new ArrayList<>());
        if (list.contains(entity.getUUID())) {
            list.remove(entity.getUUID());
            com.cursedtides.util.Compat.tell(user, Component.literal("You loosen the thread to " + entity.getName().getString() + ".").withStyle(ChatFormatting.GRAY), true);
            return InteractionResult.SUCCESS;
        }
        if (list.size() >= 3) list.remove(0);
        list.add(entity.getUUID());
        if (user.level() instanceof ServerLevel sw) {
            sw.sendParticles(ParticleTypes.WITCH, entity.getX(), entity.getY(0.7), entity.getZ(), 16, 0.3, 0.4, 0.3, 0.05);
        }
        user.level().playSound(null, user.blockPosition(), SoundEvents.ILLUSIONER_PREPARE_MIRROR, SoundSource.PLAYERS, 0.7f, 1.4f);
        com.cursedtides.util.Compat.tell(user, Component.literal("Attuned to " + entity.getName().getString() + " (" + list.size() + "/3).").withStyle(ChatFormatting.DARK_PURPLE), true);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack doll = user.getItemInHand(hand);
        int level = PirateData.level(user, Weapon.HEX);
        if (user.isShiftKeyDown()) {
            Working[] all = Working.values();
            Working next = working(doll);
            for (int k = 1; k <= all.length; k++) {
                Working w = all[(working(doll).ordinal() + k) % all.length];
                if (w.level <= level) { next = w; break; }
            }
            CompoundTag n = doll.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            n.putInt("Working", next.ordinal());
            doll.set(DataComponents.CUSTOM_DATA, CustomData.of(n));
            if (!world.isClientSide()) com.cursedtides.util.Compat.tell(user, Component.literal("Working: " + next.title + " (" + next.cost + " voodoo)").withStyle(ChatFormatting.DARK_PURPLE), true);
            return InteractionResult.SUCCESS;
        }
        if (!(world instanceof ServerLevel sw) || !(user instanceof ServerPlayer p)) return InteractionResult.SUCCESS;
        Working w = working(doll);
        if (w.level > level) w = Working.POKE;
        List<LivingEntity> targets = new ArrayList<>();
        for (UUID id : ATTUNED.getOrDefault(p.getUUID(), List.of())) {
            if (sw.getEntity(id) instanceof LivingEntity l && l.isAlive() && l.distanceToSqr(p) < RANGE * RANGE) targets.add(l);
        }
        boolean friendly = w == Working.HEAL || w == Working.CURE;
        targets.removeIf(t -> friendly == hostile(p, t));
        if (targets.isEmpty()) {
            p.sendSystemMessage(Component.literal(friendly ? "You're not attuned to any friend in reach. Use the doll on someone first."
                    : "You're not attuned to any foe in reach. Use the doll on one first.").withStyle(ChatFormatting.GRAY), true);
            return InteractionResult.FAIL;
        }
        if (!PirateData.spendVoodoo(p, w.cost)) {
            p.sendSystemMessage(Component.translatable("message.cursedtides.no_voodoo").withStyle(ChatFormatting.DARK_AQUA), true);
            return InteractionResult.FAIL;
        }
        if (!friendly) com.cursedtides.quest.Lessons.onDollWorked(p);
        float power = 1f + level * 0.03f;
        for (LivingEntity t : targets) {
            switch (w) {
                case POKE -> { t.setInvulnerableTime(0); t.hurt(sw.damageSources().indirectMagic(p, p), 4f * power); }
                case HEAL -> t.heal(4f * power);
                case SWARM -> {
                    t.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1), p);
                    t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1), p);
                }
                case CURE -> {
                    for (var e : List.copyOf(t.getActiveEffects())) {
                        if (e.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) t.removeEffect(e.getEffect());
                    }
                    t.setRemainingFireTicks(0);
                }
                case SHACKLES -> {
                    t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, (int) (80 * power), 5), p);
                    if (t instanceof Mob m) m.getNavigation().stop();
                }
                case DRAIN -> {
                    t.setInvulnerableTime(0);
                    float d = 5f * power;
                    t.hurt(sw.damageSources().indirectMagic(p, p), d);
                    p.heal(d * 0.6f);
                }
            }
            sw.sendParticles(friendly ? ParticleTypes.HEART : ParticleTypes.WITCH, t.getX(), t.getY(0.7), t.getZ(), friendly ? 4 : 10, 0.3, 0.3, 0.3, 0.05);
        }
        p.getCooldowns().addCooldown(new ItemStack(this), Math.max(10, 30 - level / 2));
        sw.playSound(null, p.blockPosition(), friendly ? SoundEvents.AMETHYST_BLOCK_CHIME : SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 0.8f, 1.2f);
        PirateData.addXp(p, Weapon.HEX, 2 * targets.size());
        doll.hurtAndBreak(1, p, hand.asEquipmentSlot());
        return InteractionResult.SUCCESS;
    }

    private static boolean hostile(Player p, Entity e) {
        if (e instanceof Player other) {
            return com.cursedtides.social.Duels.inDuel(p.getUUID()) && com.cursedtides.social.Duels.inDuel(other.getUUID());
        }
        return e instanceof Enemy || (e instanceof Mob m && m.getTarget() == p);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display__, java.util.function.Consumer<Component> adder__, TooltipFlag type) {
        java.util.List<Component> tooltip = new java.util.ArrayList<>();
        Working w = working(stack);
        tooltip.add(Component.literal("Working: " + w.title + " (" + w.cost + " voodoo)").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.literal(w.text).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Use on someone to attune. Use in the air to work. Sneak-use to change working.").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.forEach(adder__);
    }
}
