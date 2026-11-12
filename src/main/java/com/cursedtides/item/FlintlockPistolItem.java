package com.cursedtides.item;

import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Any black-powder firearm: pistol, musket or blunderbuss. Hitscan, one Pistol Shot per
 * firing. Pellets above 1 make a spread (blunderbuss); damage per target is summed so
 * every pellet counts. All firearms train the Firearms proficiency.
 */
public class FlintlockPistolItem extends Item {
    public record Stats(double range, float damage, int reloadTicks, int pellets, float spreadDegrees, String tooltipKey, int barrels, boolean blast) {
        public Stats(double range, float damage, int reloadTicks, int pellets, float spreadDegrees, String tooltipKey) {
            this(range, damage, reloadTicks, pellets, spreadDegrees, tooltipKey, 1, false);
        }
    }

    public static final Stats PISTOL = new Stats(32.0, 9.0f, 30, 1, 0.0f, "tooltip.cursedtides.flintlock_pistol");
    public static final Stats MUSKET = new Stats(64.0, 15.0f, 55, 1, 0.0f, "tooltip.cursedtides.musket");
    public static final Stats BLUNDERBUSS = new Stats(12.0, 3.0f, 45, 7, 9.0f, "tooltip.cursedtides.blunderbuss");
    /** Two shots before a long reload. */
    public static final Stats DOUBLE_BARREL = new Stats(28.0, 8.0f, 50, 1, 0.0f, "tooltip.cursedtides.double_barrel", 2, false);
    /** A stubby cannon fired from the hip: the shot bursts where it lands. */
    public static final Stats HAND_CANNON = new Stats(24.0, 14.0f, 80, 1, 0.0f, "tooltip.cursedtides.hand_cannon", 1, true);

    private final Stats stats;

    public FlintlockPistolItem(Settings settings) {
        this(settings, PISTOL);
    }

    public FlintlockPistolItem(Settings settings, Stats stats) {
        super(settings);
        this.stats = stats;
    }

    /** Players steadying their aim, and until when. */
    private static final java.util.Map<java.util.UUID, Long> AIMING = new java.util.HashMap<>();

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack gun = user.getStackInHand(hand);
        if (user.isSneaking() && !world.isClient) {
            // Take Aim: steady yourself; the next shot within five seconds hits half again as hard
            AIMING.put(user.getUuid(), world.getTime() + 100);
            user.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.SLOWNESS, 100, 2, false, false));
            user.sendMessage(Text.literal("You steady your aim...").formatted(Formatting.GOLD), true);
            world.playSound(null, user.getBlockPos(), SoundEvents.ITEM_CROSSBOW_LOADING_MIDDLE.value(), SoundCategory.PLAYERS, 0.8f, 1.2f);
            return TypedActionResult.success(gun);
        }
        if (user.isSneaking()) return TypedActionResult.success(gun, true);

        double save = com.cursedtides.pirate.PerkData.stat(user, com.cursedtides.pirate.Perk.Stat.AMMO_SAVE);
        boolean keepShot = !world.isClient && user.getRandom().nextDouble() < save
                && user.getInventory().count(ModItems.PISTOL_SHOT) > 0;
        if (!keepShot && !AmmoHelper.consume(user, ModItems.PISTOL_SHOT)) {
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.PLAYERS, 0.8f, 1.4f);
            if (!world.isClient) {
                user.sendMessage(Text.translatable("message.cursedtides.no_pistol_shot"), true);
            }
            return TypedActionResult.fail(gun);
        }

        int level = PirateData.level(user, Weapon.PISTOL);
        float damage = stats.damage() * (1.0f + level * 0.04f
                + (float) com.cursedtides.pirate.PerkData.stat(user, com.cursedtides.pirate.Perk.Stat.GUN_DAMAGE))
                * com.cursedtides.profession.Honing.gunBonus(gun);
        double faster = com.cursedtides.pirate.PerkData.stat(user, com.cursedtides.pirate.Perk.Stat.RELOAD);
        Long aimed = AIMING.remove(user.getUuid());
        if (aimed != null && aimed >= world.getTime()) {
            damage *= 1.5f;
            user.removeStatusEffect(net.minecraft.entity.effect.StatusEffects.SLOWNESS);
        }

        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, SoundCategory.PLAYERS, 1.0f,
                stats.pellets() > 1 ? 0.4f : stats.range() > 40 ? 0.7f : 0.55f);
        int reload = Math.max(stats.reloadTicks() / 2, stats.reloadTicks() - level);
        boolean secondBarrel = false;
        if (stats.barrels() > 1) {
            var data = gun.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA, net.minecraft.component.type.NbtComponent.DEFAULT).copyNbt();
            int fired = data.getInt("Fired") + 1;
            secondBarrel = fired < stats.barrels();
            data.putInt("Fired", secondBarrel ? fired : 0);
            gun.set(net.minecraft.component.DataComponentTypes.CUSTOM_DATA, net.minecraft.component.type.NbtComponent.of(data));
        }
        user.getItemCooldownManager().set(this, secondBarrel ? 6 : Math.max(6, (int) Math.round(reload * (1.0 - faster))));

        if (world instanceof ServerWorld serverWorld) {
            Vec3d start = user.getEyePos();
            Vec3d aim = user.getRotationVec(1.0f);
            Vec3d muzzle = start.add(aim.multiply(0.8)).add(0, -0.15, 0);
            Map<Entity, Float> hits = new HashMap<>();

            for (int i = 0; i < stats.pellets(); i++) {
                Vec3d dir = stats.pellets() > 1 ? jitter(aim, stats.spreadDegrees(), user) : aim;
                Vec3d end = start.add(dir.multiply(stats.range()));
                BlockHitResult blockHit = world.raycast(new RaycastContext(start, end,
                        RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user));
                if (blockHit.getType() != HitResult.Type.MISS) {
                    end = blockHit.getPos();
                }
                Box searchBox = user.getBoundingBox().stretch(dir.multiply(stats.range())).expand(1.0);
                EntityHitResult entityHit = ProjectileUtil.raycast(user, start, end, searchBox,
                        e -> !e.isSpectator() && e.canHit() && e != user && e != user.getVehicle(),
                        start.squaredDistanceTo(end));
                if (entityHit != null) {
                    end = entityHit.getPos();
                    hits.merge(entityHit.getEntity(), damage, Float::sum);
                }
                double length = muzzle.distanceTo(end);
                Vec3d step = end.subtract(muzzle).normalize();
                for (double d = 0; d < length; d += stats.pellets() > 1 ? 1.5 : 0.75) {
                    Vec3d p = muzzle.add(step.multiply(d));
                    serverWorld.spawnParticles(ParticleTypes.SMOKE, p.x, p.y, p.z, 1, 0, 0, 0, 0);
                }
                serverWorld.spawnParticles(ParticleTypes.CRIT, end.x, end.y, end.z, 3, 0.1, 0.1, 0.1, 0.1);
            }

            if (stats.blast()) {
                Vec3d end = start.add(aim.multiply(stats.range()));
                BlockHitResult bh = world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user));
                Vec3d at = hits.isEmpty() ? (bh.getType() != HitResult.Type.MISS ? bh.getPos() : null) : hits.keySet().iterator().next().getPos();
                if (at != null) {
                    serverWorld.spawnParticles(ParticleTypes.EXPLOSION, at.x, at.y + 0.5, at.z, 2, 0.3, 0.3, 0.3, 0);
                    world.playSound(null, at.x, at.y, at.z, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, 0.9f, 1.2f);
                    for (Entity near : world.getOtherEntities(user, new Box(at, at).expand(2.5), e -> e instanceof LivingEntity && e.isAlive())) {
                        hits.merge(near, damage * 0.5f, Float::sum);
                        if (near instanceof LivingEntity l) com.cursedtides.combat.Styles.stagger(l, 30);
                    }
                }
                user.addVelocity(-aim.x * 0.5, 0.1, -aim.z * 0.5);
                user.velocityModified = true;
            }
            if (user instanceof net.minecraft.server.network.ServerPlayerEntity sp) {
                com.cursedtides.activity.ShootingRange.onShot(sp, hits.keySet());
            }
            for (Map.Entry<Entity, Float> hit : hits.entrySet()) {
                Entity target = hit.getKey();
                target.damage(world.getDamageSources().playerAttack(user), hit.getValue());
                if (target instanceof LivingEntity living) {
                    living.takeKnockback(stats.pellets() > 1 ? 0.8 : 0.4, -aim.x, -aim.z);
                }
            }

            serverWorld.spawnParticles(ParticleTypes.LARGE_SMOKE, muzzle.x, muzzle.y, muzzle.z,
                    stats.pellets() > 1 ? 10 : 4, 0.08, 0.08, 0.08, 0.02);
            serverWorld.spawnParticles(ParticleTypes.FLAME, muzzle.x, muzzle.y, muzzle.z, 3, 0.03, 0.03, 0.03, 0.01);
            gun.damage(1, user, LivingEntity.getSlotForHand(hand));
        }

        return TypedActionResult.success(gun, world.isClient);
    }

    private static Vec3d jitter(Vec3d dir, float degrees, PlayerEntity user) {
        double spread = Math.toRadians(degrees);
        var random = user.getRandom();
        double yaw = (random.nextDouble() - 0.5) * 2 * spread;
        double pitch = (random.nextDouble() - 0.5) * 2 * spread;
        return dir.rotateY((float) yaw).rotateX((float) pitch).normalize();
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable(stats.tooltipKey()).formatted(Formatting.GRAY));
        tooltip.add(Text.literal("Sneak + use: Take Aim (next shot +50%).").formatted(Formatting.DARK_GRAY));
        if (stats.barrels() > 1) tooltip.add(Text.literal("Two barrels: fire twice, then reload.").formatted(Formatting.DARK_GRAY));
    }
}
