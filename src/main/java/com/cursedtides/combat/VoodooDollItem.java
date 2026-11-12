package com.cursedtides.combat;

import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    public VoodooDollItem(Settings settings) {
        super(settings);
    }

    static Working working(ItemStack doll) {
        int i = doll.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getInt("Working");
        Working[] all = Working.values();
        return all[Math.floorMod(i, all.length)];
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (user.getWorld().isClient) return ActionResult.SUCCESS;
        List<UUID> list = ATTUNED.computeIfAbsent(user.getUuid(), k -> new ArrayList<>());
        if (list.contains(entity.getUuid())) {
            list.remove(entity.getUuid());
            user.sendMessage(Text.literal("You loosen the thread to " + entity.getName().getString() + ".").formatted(Formatting.GRAY), true);
            return ActionResult.SUCCESS;
        }
        if (list.size() >= 3) list.remove(0);
        list.add(entity.getUuid());
        if (user.getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.WITCH, entity.getX(), entity.getBodyY(0.7), entity.getZ(), 16, 0.3, 0.4, 0.3, 0.05);
        }
        user.getWorld().playSound(null, user.getBlockPos(), SoundEvents.ENTITY_ILLUSIONER_PREPARE_MIRROR, SoundCategory.PLAYERS, 0.7f, 1.4f);
        user.sendMessage(Text.literal("Attuned to " + entity.getName().getString() + " (" + list.size() + "/3).").formatted(Formatting.DARK_PURPLE), true);
        return ActionResult.SUCCESS;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack doll = user.getStackInHand(hand);
        int level = PirateData.level(user, Weapon.HEX);
        if (user.isSneaking()) {
            Working[] all = Working.values();
            Working next = working(doll);
            for (int k = 1; k <= all.length; k++) {
                Working w = all[(working(doll).ordinal() + k) % all.length];
                if (w.level <= level) { next = w; break; }
            }
            NbtCompound n = doll.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
            n.putInt("Working", next.ordinal());
            doll.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(n));
            if (!world.isClient) user.sendMessage(Text.literal("Working: " + next.title + " (" + next.cost + " voodoo)").formatted(Formatting.DARK_PURPLE), true);
            return TypedActionResult.success(doll, world.isClient);
        }
        if (!(world instanceof ServerWorld sw) || !(user instanceof ServerPlayerEntity p)) return TypedActionResult.success(doll, true);
        Working w = working(doll);
        if (w.level > level) w = Working.POKE;
        List<LivingEntity> targets = new ArrayList<>();
        for (UUID id : ATTUNED.getOrDefault(p.getUuid(), List.of())) {
            if (sw.getEntity(id) instanceof LivingEntity l && l.isAlive() && l.squaredDistanceTo(p) < RANGE * RANGE) targets.add(l);
        }
        boolean friendly = w == Working.HEAL || w == Working.CURE;
        targets.removeIf(t -> friendly == hostile(p, t));
        if (targets.isEmpty()) {
            p.sendMessage(Text.literal(friendly ? "You're not attuned to any friend in reach. Use the doll on someone first."
                    : "You're not attuned to any foe in reach. Use the doll on one first.").formatted(Formatting.GRAY), true);
            return TypedActionResult.fail(doll);
        }
        if (!PirateData.spendVoodoo(p, w.cost)) {
            p.sendMessage(Text.translatable("message.cursedtides.no_voodoo").formatted(Formatting.DARK_AQUA), true);
            return TypedActionResult.fail(doll);
        }
        float power = 1f + level * 0.03f;
        for (LivingEntity t : targets) {
            switch (w) {
                case POKE -> { t.timeUntilRegen = 0; t.damage(sw.getDamageSources().indirectMagic(p, p), 4f * power); }
                case HEAL -> t.heal(4f * power);
                case SWARM -> {
                    t.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 1), p);
                    t.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 1), p);
                }
                case CURE -> {
                    for (var e : List.copyOf(t.getStatusEffects())) {
                        if (e.getEffectType().value().getCategory() == StatusEffectCategory.HARMFUL) t.removeStatusEffect(e.getEffectType());
                    }
                    t.setFireTicks(0);
                }
                case SHACKLES -> {
                    t.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, (int) (80 * power), 5), p);
                    if (t instanceof MobEntity m) m.getNavigation().stop();
                }
                case DRAIN -> {
                    t.timeUntilRegen = 0;
                    float d = 5f * power;
                    t.damage(sw.getDamageSources().indirectMagic(p, p), d);
                    p.heal(d * 0.6f);
                }
            }
            sw.spawnParticles(friendly ? ParticleTypes.HEART : ParticleTypes.WITCH, t.getX(), t.getBodyY(0.7), t.getZ(), friendly ? 4 : 10, 0.3, 0.3, 0.3, 0.05);
        }
        p.getItemCooldownManager().set(this, Math.max(10, 30 - level / 2));
        sw.playSound(null, p.getBlockPos(), friendly ? SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME : SoundEvents.ENTITY_EVOKER_CAST_SPELL, SoundCategory.PLAYERS, 0.8f, 1.2f);
        PirateData.addXp(p, Weapon.HEX, 2 * targets.size());
        doll.damage(1, p, LivingEntity.getSlotForHand(hand));
        return TypedActionResult.success(doll);
    }

    private static boolean hostile(PlayerEntity p, Entity e) {
        if (e instanceof PlayerEntity other) {
            return com.cursedtides.social.Duels.inDuel(p.getUuid()) && com.cursedtides.social.Duels.inDuel(other.getUuid());
        }
        return e instanceof Monster || (e instanceof MobEntity m && m.getTarget() == p);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        Working w = working(stack);
        tooltip.add(Text.literal("Working: " + w.title + " (" + w.cost + " voodoo)").formatted(Formatting.DARK_PURPLE));
        tooltip.add(Text.literal(w.text).formatted(Formatting.GRAY));
        tooltip.add(Text.literal("Use on someone to attune. Use in the air to work. Sneak-use to change working.").formatted(Formatting.DARK_GRAY));
    }
}
