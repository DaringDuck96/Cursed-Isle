package com.cursedtides.item;

import com.cursedtides.entity.HexBoltEntity;
import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The voodoo staff. Sneak and use to choose a hex; use to cast it. New hexes come with Hex mastery:
 * Hex Bolt (0), Withering Curse (5), Spirit Ward (10), Drowned Grasp (15), Soul Siphon (20), Ghost Walk (25), Storm Hex (30).
 */
public class HexStaffItem extends Item {
    public static final int COST = 15;

    public enum Hex {
        BOLT("Hex Bolt", 0, 15, "A slowing bolt of curse-fire."),
        WITHER("Withering Curse", 5, 20, "Rot the foe you look at: wither and weakness."),
        WARD("Spirit Ward", 10, 30, "Ancestor spirits shield you: absorption and resistance."),
        GRASP("Drowned Grasp", 15, 30, "Drowned hands hold every foe around you in place."),
        SIPHON("Soul Siphon", 20, 25, "Drain the foe you look at and heal by half."),
        GHOST("Ghost Walk", 25, 40, "Slip out of sight: invisible and swift, and foes lose you."),
        STORM("Storm Hex", 30, 45, "Call lightning down on the foe you look at.");

        public final String title; public final int level; public final int cost; public final String text;
        Hex(String title, int level, int cost, String text) { this.title = title; this.level = level; this.cost = cost; this.text = text; }
    }

    public HexStaffItem(Properties settings) {
        super(settings);
    }

    public static Hex selected(ItemStack staff) {
        int i = staff.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("Hex");
        Hex[] all = Hex.values();
        return all[Math.floorMod(i, all.length)];
    }

    private static void select(ItemStack staff, Hex hex) {
        CompoundTag n = staff.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        n.putInt("Hex", hex.ordinal());
        staff.set(DataComponents.CUSTOM_DATA, CustomData.of(n));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack staff = user.getItemInHand(hand);
        int level = PirateData.level(user, Weapon.HEX);

        if (user.isShiftKeyDown()) {
            Hex[] all = Hex.values();
            Hex next = selected(staff);
            for (int k = 1; k <= all.length; k++) {
                Hex h = all[(selected(staff).ordinal() + k) % all.length];
                if (h.level <= level) { next = h; break; }
            }
            select(staff, next);
            if (!world.isClientSide) {
                user.displayClientMessage(Component.literal("Hex: " + next.title + " (" + next.cost + " voodoo)").withStyle(ChatFormatting.DARK_AQUA), true);
            }
            return InteractionResultHolder.sidedSuccess(staff, world.isClientSide);
        }

        // Hold to charge, as the old staffs did: a tap casts, half a second makes it Charged, a second and a half Fully Charged
        user.startUsingItem(hand);
        return InteractionResultHolder.consume(staff);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) { return 72000; }

    @Override
    public net.minecraft.world.item.UseAnim getUseAnimation(ItemStack stack) { return net.minecraft.world.item.UseAnim.BOW; }

    @Override
    public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        int held = getUseDuration(stack, user) - remainingUseTicks;
        if (world.isClientSide && held > 8) {
            Vec3 at = user.getEyePosition().add(user.getViewVector(1f).scale(0.8));
            world.addParticle(held >= 30 ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.WITCH, at.x, at.y - 0.2, at.z, 0, 0.02, 0);
        }
        if (!world.isClientSide && (held == 10 || held == 30)) {
            world.playSound(null, user.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1f, held == 30 ? 1.6f : 1.1f);
            if (user instanceof Player p) p.displayClientMessage(Component.literal(held == 30 ? "Fully charged!" : "Charged...").withStyle(ChatFormatting.DARK_AQUA), true);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof Player p)) return;
        int held = getUseDuration(stack, user) - remainingUseTicks;
        float charge = held >= 30 ? 3.0f : held >= 10 ? 1.8f : 1.0f;
        cast(world, p, p.getUsedItemHand(), stack, charge);
    }

    private InteractionResultHolder<ItemStack> cast(Level world, Player user, InteractionHand hand, ItemStack staff, float charge) {
        int level = PirateData.level(user, Weapon.HEX);
        Hex hex = selected(staff);
        if (hex.level > level) hex = Hex.BOLT;
        int cost = (int) Math.round(hex.cost * (charge >= 3 ? 2.0 : charge > 1 ? 1.4 : 1.0) * (1.0 - com.cursedtides.pirate.PerkData.stat(user,
                com.cursedtides.pirate.Perk.Stat.HEX_COST)));
        if (!world.isClientSide && !PirateData.spendVoodoo(user, cost)) {
            user.displayClientMessage(Component.translatable("message.cursedtides.no_voodoo").withStyle(ChatFormatting.DARK_AQUA), true);
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.5f, 1.6f);
            return InteractionResultHolder.fail(staff);
        }

        user.getCooldowns().addCooldown(this, Math.max(8, (hex == Hex.BOLT ? 20 : 40) - level / 2));
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 0.8f, 1.3f);
        if (world.isClientSide || !(world instanceof ServerLevel sw)) return InteractionResultHolder.sidedSuccess(staff, true);

        float power = (1.0f + level * 0.03f + (float) com.cursedtides.pirate.PerkData.stat(user, com.cursedtides.pirate.Perk.Stat.HEX_DAMAGE)) * charge;
        switch (hex) {
            case BOLT -> {
                Vec3 dir = user.getViewVector(1.0f);
                int bolts = charge >= 3 ? 3 : 1;
                for (int k = 0; k < bolts; k++) {
                    HexBoltEntity bolt = new HexBoltEntity(world, user, level);
                    bolt.setBonusDamage((float) com.cursedtides.pirate.PerkData.stat(user, com.cursedtides.pirate.Perk.Stat.HEX_DAMAGE)
                            + (charge - 1) * 7f);
                    Vec3 d = bolts == 1 ? dir : dir.yRot((k - 1) * 0.12f);
                    bolt.shoot(d.x, d.y, d.z, 1.6f + (charge - 1) * 0.2f, 0.0f);
                    world.addFreshEntity(bolt);
                }
                if (charge > 1) sw.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, user.getX(), user.getEyeY(), user.getZ(), 12, 0.3, 0.3, 0.3, 0.05);
            }
            case WITHER -> {
                LivingEntity t = target(user, 24);
                if (t != null) {
                    t.addEffect(new MobEffectInstance(MobEffects.WITHER, (int) (100 * power), 1), user);
                    t.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 1), user);
                    sw.sendParticles(ParticleTypes.SQUID_INK, t.getX(), t.getY(0.6), t.getZ(), 20, 0.3, 0.4, 0.3, 0.02);
                }
            }
            case WARD -> {
                user.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 1 + level / 20));
                user.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 0));
                sw.sendParticles(ParticleTypes.SOUL, user.getX(), user.getY() + 1, user.getZ(), 24, 0.6, 0.6, 0.6, 0.02);
            }
            case GRASP -> {
                for (Entity e : world.getEntities(user, user.getBoundingBox().inflate(8), e -> e instanceof LivingEntity && foe(user, e))) {
                    LivingEntity l = (LivingEntity) e;
                    l.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (int) (60 * power), 6), user);
                    l.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, 2), user);
                    if (l instanceof Mob m) m.getNavigation().stop();
                    sw.sendParticles(ParticleTypes.FALLING_WATER, l.getX(), l.getY() + 0.2, l.getZ(), 30, 0.4, 0.1, 0.4, 0);
                    sw.sendParticles(ParticleTypes.BUBBLE_POP, l.getX(), l.getY() + 0.5, l.getZ(), 10, 0.3, 0.3, 0.3, 0.02);
                }
                world.playSound(null, user.blockPosition(), SoundEvents.DROWNED_AMBIENT_WATER, SoundSource.PLAYERS, 1f, 0.6f);
            }
            case SIPHON -> {
                LivingEntity t = target(user, 20);
                if (t != null) {
                    float dmg = 8f * power;
                    t.hurt(world.damageSources().indirectMagic(user, user), dmg);
                    user.heal(dmg / 2);
                    Vec3 from = t.position().add(0, t.getBbHeight() * 0.6, 0), to = user.getEyePosition();
                    Vec3 step = to.subtract(from).normalize();
                    for (double d = 0; d < from.distanceTo(to); d += 0.6) {
                        Vec3 q = from.add(step.scale(d));
                        sw.sendParticles(ParticleTypes.DAMAGE_INDICATOR, q.x, q.y, q.z, 1, 0, 0, 0, 0);
                    }
                }
            }
            case GHOST -> {
                user.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 160, 0));
                user.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 1));
                for (Mob m : world.getEntitiesOfClass(Mob.class, user.getBoundingBox().inflate(24), m -> m.getTarget() == user)) {
                    m.setTarget(null);
                }
                sw.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, user.getX(), user.getY() + 1, user.getZ(), 30, 0.4, 0.8, 0.4, 0.02);
            }
            case STORM -> {
                LivingEntity t = target(user, 40);
                if (t != null) {
                    LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(world);
                    if (bolt != null) {
                        bolt.moveTo(t.position());
                        if (user instanceof net.minecraft.server.level.ServerPlayer sp) bolt.setCause(sp);
                        world.addFreshEntity(bolt);
                    }
                    t.hurt(world.damageSources().indirectMagic(user, user), 6f * power);
                    if (user instanceof net.minecraft.server.level.ServerPlayer sp) com.cursedtides.activity.Achievements.grant(sp, "storm_caller");
                }
            }
        }
        staff.hurtAndBreak(1, user, LivingEntity.getSlotForHand(hand));
        return InteractionResultHolder.sidedSuccess(staff, false);
    }

    private static boolean foe(Player user, Entity e) {
        return e.isAlive() && (e instanceof Enemy || (e instanceof Mob m && m.getTarget() == user));
    }

    /** The living thing under your crosshair, within range. */
    private static LivingEntity target(Player user, double range) {
        Vec3 start = user.getEyePosition();
        Vec3 end = start.add(user.getViewVector(1f).scale(range));
        AABB box = user.getBoundingBox().expandTowards(user.getViewVector(1f).scale(range)).inflate(1);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(user, start, end, box, e -> e instanceof LivingEntity && !e.isSpectator() && e.isPickable(), range * range);
        if (hit == null) {
            user.displayClientMessage(Component.literal("No one there to hex.").withStyle(ChatFormatting.GRAY), true);
            return null;
        }
        return (LivingEntity) hit.getEntity();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        Hex h = selected(stack);
        tooltip.add(Component.literal("Hex: " + h.title + " (" + h.cost + " voodoo)").withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(Component.literal(h.text).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Sneak and use to choose another hex.").withStyle(ChatFormatting.DARK_GRAY));
    }
}
