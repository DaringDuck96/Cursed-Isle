package com.cursedtides.item;

import com.cursedtides.entity.HexBoltEntity;
import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

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

    public HexStaffItem(Settings settings) {
        super(settings);
    }

    public static Hex selected(ItemStack staff) {
        int i = staff.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getInt("Hex");
        Hex[] all = Hex.values();
        return all[Math.floorMod(i, all.length)];
    }

    private static void select(ItemStack staff, Hex hex) {
        NbtCompound n = staff.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        n.putInt("Hex", hex.ordinal());
        staff.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(n));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack staff = user.getStackInHand(hand);
        int level = PirateData.level(user, Weapon.HEX);

        if (user.isSneaking()) {
            Hex[] all = Hex.values();
            Hex next = selected(staff);
            for (int k = 1; k <= all.length; k++) {
                Hex h = all[(selected(staff).ordinal() + k) % all.length];
                if (h.level <= level) { next = h; break; }
            }
            select(staff, next);
            if (!world.isClient) {
                user.sendMessage(Text.literal("Hex: " + next.title + " (" + next.cost + " voodoo)").formatted(Formatting.DARK_AQUA), true);
            }
            return TypedActionResult.success(staff, world.isClient);
        }

        Hex hex = selected(staff);
        if (hex.level > level) hex = Hex.BOLT;
        int cost = (int) Math.round(hex.cost * (1.0 - com.cursedtides.pirate.PerkData.stat(user,
                com.cursedtides.pirate.Perk.Stat.HEX_COST)));
        if (!world.isClient && !PirateData.spendVoodoo(user, cost)) {
            user.sendMessage(Text.translatable("message.cursedtides.no_voodoo").formatted(Formatting.DARK_AQUA), true);
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.5f, 1.6f);
            return TypedActionResult.fail(staff);
        }

        user.getItemCooldownManager().set(this, Math.max(8, (hex == Hex.BOLT ? 20 : 40) - level / 2));
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ENTITY_EVOKER_CAST_SPELL, SoundCategory.PLAYERS, 0.8f, 1.3f);
        if (world.isClient || !(world instanceof ServerWorld sw)) return TypedActionResult.success(staff, true);

        float power = 1.0f + level * 0.03f + (float) com.cursedtides.pirate.PerkData.stat(user, com.cursedtides.pirate.Perk.Stat.HEX_DAMAGE);
        switch (hex) {
            case BOLT -> {
                Vec3d dir = user.getRotationVec(1.0f);
                HexBoltEntity bolt = new HexBoltEntity(world, user, level);
                bolt.setBonusDamage((float) com.cursedtides.pirate.PerkData.stat(user, com.cursedtides.pirate.Perk.Stat.HEX_DAMAGE));
                bolt.setVelocity(dir.x, dir.y, dir.z, 1.6f, 0.0f);
                world.spawnEntity(bolt);
            }
            case WITHER -> {
                LivingEntity t = target(user, 24);
                if (t != null) {
                    t.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, (int) (100 * power), 1), user);
                    t.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 160, 1), user);
                    sw.spawnParticles(ParticleTypes.SQUID_INK, t.getX(), t.getBodyY(0.6), t.getZ(), 20, 0.3, 0.4, 0.3, 0.02);
                }
            }
            case WARD -> {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 400, 1 + level / 20));
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 200, 0));
                sw.spawnParticles(ParticleTypes.SOUL, user.getX(), user.getY() + 1, user.getZ(), 24, 0.6, 0.6, 0.6, 0.02);
            }
            case GRASP -> {
                for (Entity e : world.getOtherEntities(user, user.getBoundingBox().expand(8), e -> e instanceof LivingEntity && foe(user, e))) {
                    LivingEntity l = (LivingEntity) e;
                    l.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, (int) (60 * power), 6), user);
                    l.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 60, 2), user);
                    if (l instanceof MobEntity m) m.getNavigation().stop();
                    sw.spawnParticles(ParticleTypes.FALLING_WATER, l.getX(), l.getY() + 0.2, l.getZ(), 30, 0.4, 0.1, 0.4, 0);
                    sw.spawnParticles(ParticleTypes.BUBBLE_POP, l.getX(), l.getY() + 0.5, l.getZ(), 10, 0.3, 0.3, 0.3, 0.02);
                }
                world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_DROWNED_AMBIENT_WATER, SoundCategory.PLAYERS, 1f, 0.6f);
            }
            case SIPHON -> {
                LivingEntity t = target(user, 20);
                if (t != null) {
                    float dmg = 8f * power;
                    t.damage(world.getDamageSources().indirectMagic(user, user), dmg);
                    user.heal(dmg / 2);
                    Vec3d from = t.getPos().add(0, t.getHeight() * 0.6, 0), to = user.getEyePos();
                    Vec3d step = to.subtract(from).normalize();
                    for (double d = 0; d < from.distanceTo(to); d += 0.6) {
                        Vec3d q = from.add(step.multiply(d));
                        sw.spawnParticles(ParticleTypes.DAMAGE_INDICATOR, q.x, q.y, q.z, 1, 0, 0, 0, 0);
                    }
                }
            }
            case GHOST -> {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 160, 0));
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 160, 1));
                for (MobEntity m : world.getEntitiesByClass(MobEntity.class, user.getBoundingBox().expand(24), m -> m.getTarget() == user)) {
                    m.setTarget(null);
                }
                sw.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, user.getX(), user.getY() + 1, user.getZ(), 30, 0.4, 0.8, 0.4, 0.02);
            }
            case STORM -> {
                LivingEntity t = target(user, 40);
                if (t != null) {
                    LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(world);
                    if (bolt != null) {
                        bolt.refreshPositionAfterTeleport(t.getPos());
                        if (user instanceof net.minecraft.server.network.ServerPlayerEntity sp) bolt.setChanneler(sp);
                        world.spawnEntity(bolt);
                    }
                    t.damage(world.getDamageSources().indirectMagic(user, user), 6f * power);
                }
            }
        }
        staff.damage(1, user, LivingEntity.getSlotForHand(hand));
        return TypedActionResult.success(staff, false);
    }

    private static boolean foe(PlayerEntity user, Entity e) {
        return e.isAlive() && (e instanceof Monster || (e instanceof MobEntity m && m.getTarget() == user));
    }

    /** The living thing under your crosshair, within range. */
    private static LivingEntity target(PlayerEntity user, double range) {
        Vec3d start = user.getEyePos();
        Vec3d end = start.add(user.getRotationVec(1f).multiply(range));
        Box box = user.getBoundingBox().stretch(user.getRotationVec(1f).multiply(range)).expand(1);
        EntityHitResult hit = ProjectileUtil.raycast(user, start, end, box, e -> e instanceof LivingEntity && !e.isSpectator() && e.canHit(), range * range);
        if (hit == null) {
            user.sendMessage(Text.literal("No one there to hex.").formatted(Formatting.GRAY), true);
            return null;
        }
        return (LivingEntity) hit.getEntity();
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        Hex h = selected(stack);
        tooltip.add(Text.literal("Hex: " + h.title + " (" + h.cost + " voodoo)").formatted(Formatting.DARK_AQUA));
        tooltip.add(Text.literal(h.text).formatted(Formatting.GRAY));
        tooltip.add(Text.literal("Sneak and use to choose another hex.").formatted(Formatting.DARK_GRAY));
    }
}
