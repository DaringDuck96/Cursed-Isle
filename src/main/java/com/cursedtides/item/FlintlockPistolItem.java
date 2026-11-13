package com.cursedtides.item;

import net.minecraft.world.InteractionResult;
import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

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

    /** Four turning barrels: four quick shots, then a slow reload. */
    public static final Stats REPEATER = new Stats(26.0, 6.5f, 70, 1, 1.0f, "tooltip.cursedtides.repeater", 4, false);
    /** A long gun with a blade on the end, for when they get close. */
    public static final Stats BAYONET = new Stats(56.0, 13.0f, 60, 1, 0.0f, "tooltip.cursedtides.bayonet_musket");

    private final Stats stats;

    public FlintlockPistolItem(Properties settings) {
        this(settings, PISTOL);
    }

    public FlintlockPistolItem(Properties settings, Stats stats) {
        super(settings);
        this.stats = stats;
    }

    /** Players steadying their aim, and until when. */
    private static final java.util.Map<java.util.UUID, Long> AIMING = new java.util.HashMap<>();

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack gun = user.getItemInHand(hand);
        if (user.isShiftKeyDown() && !world.isClientSide()) {
            // Take Aim: steady yourself; the next shot within five seconds hits half again as hard
            AIMING.put(user.getUUID(), world.getGameTime() + 100);
            user.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOWNESS, 100, 2, false, false));
            com.cursedtides.util.Compat.tell(user, Component.literal("You steady your aim...").withStyle(ChatFormatting.GOLD), true);
            world.playSound(null, user.blockPosition(), SoundEvents.CROSSBOW_LOADING_MIDDLE.value(), SoundSource.PLAYERS, 0.8f, 1.2f);
            return InteractionResult.SUCCESS;
        }
        if (user.isShiftKeyDown()) return InteractionResult.SUCCESS;

        // Special shot in the off hand is loaded first: venom, hot or silver; otherwise plain lead
        net.minecraft.world.item.Item shot = ModItems.PISTOL_SHOT;
        ItemStack off = user.getOffhandItem();
        if (off.is(ModItems.VENOM_SHOT) || off.is(ModItems.HOT_SHOT) || off.is(ModItems.SILVER_SHOT)) shot = off.getItem();
        double save = com.cursedtides.pirate.PerkData.stat(user, com.cursedtides.pirate.Perk.Stat.AMMO_SAVE);
        boolean keepShot = !world.isClientSide() && user.getRandom().nextDouble() < save
                && user.getInventory().countItem(shot) > 0;
        if (!keepShot && !AmmoHelper.consume(user, shot)) {
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8f, 1.4f);
            if (!world.isClientSide()) {
                com.cursedtides.util.Compat.tell(user, Component.translatable("message.cursedtides.no_pistol_shot"), true);
            }
            return InteractionResult.FAIL;
        }

        int level = PirateData.level(user, Weapon.PISTOL);
        float damage = stats.damage() * (1.0f + level * 0.04f
                + (float) com.cursedtides.pirate.PerkData.stat(user, com.cursedtides.pirate.Perk.Stat.GUN_DAMAGE))
                * com.cursedtides.profession.Honing.gunBonus(gun);
        double faster = com.cursedtides.pirate.PerkData.stat(user, com.cursedtides.pirate.Perk.Stat.RELOAD);
        Long aimed = AIMING.remove(user.getUUID());
        if (aimed != null && aimed >= world.getGameTime()) {
            damage *= 1.5f;
            user.removeEffect(net.minecraft.world.effect.MobEffects.SLOWNESS);
        }

        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, SoundSource.PLAYERS, 1.0f,
                stats.pellets() > 1 ? 0.4f : stats.range() > 40 ? 0.7f : 0.55f);
        int reload = Math.max(stats.reloadTicks() / 2, stats.reloadTicks() - level);
        boolean secondBarrel = false;
        if (stats.barrels() > 1) {
            var data = gun.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
            int fired = data.getIntOr("Fired", 0) + 1;
            secondBarrel = fired < stats.barrels();
            data.putInt("Fired", secondBarrel ? fired : 0);
            gun.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(data));
        }
        user.getCooldowns().addCooldown(new ItemStack(this), secondBarrel ? 6 : Math.max(6, (int) Math.round(reload * (1.0 - faster))));

        if (world instanceof ServerLevel serverWorld) {
            Vec3 start = user.getEyePosition();
            Vec3 aim = user.getViewVector(1.0f);
            Vec3 muzzle = start.add(aim.scale(0.8)).add(0, -0.15, 0);
            Map<Entity, Float> hits = new HashMap<>();

            for (int i = 0; i < stats.pellets(); i++) {
                Vec3 dir = stats.pellets() > 1 ? jitter(aim, stats.spreadDegrees(), user) : aim;
                Vec3 end = start.add(dir.scale(stats.range()));
                BlockHitResult blockHit = world.clip(new ClipContext(start, end,
                        ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, user));
                if (blockHit.getType() != HitResult.Type.MISS) {
                    end = blockHit.getLocation();
                }
                AABB searchBox = user.getBoundingBox().expandTowards(dir.scale(stats.range())).inflate(1.0);
                EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(user, start, end, searchBox,
                        e -> !e.isSpectator() && e.isPickable() && e != user && e != user.getVehicle(),
                        start.distanceToSqr(end));
                if (entityHit != null) {
                    end = entityHit.getLocation();
                    hits.merge(entityHit.getEntity(), damage, Float::sum);
                }
                double length = muzzle.distanceTo(end);
                Vec3 step = end.subtract(muzzle).normalize();
                for (double d = 0; d < length; d += stats.pellets() > 1 ? 1.5 : 0.75) {
                    Vec3 p = muzzle.add(step.scale(d));
                    serverWorld.sendParticles(ParticleTypes.SMOKE, p.x, p.y, p.z, 1, 0, 0, 0, 0);
                }
                serverWorld.sendParticles(ParticleTypes.CRIT, end.x, end.y, end.z, 3, 0.1, 0.1, 0.1, 0.1);
            }

            if (stats.blast()) {
                Vec3 end = start.add(aim.scale(stats.range()));
                BlockHitResult bh = world.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, user));
                Vec3 at = hits.isEmpty() ? (bh.getType() != HitResult.Type.MISS ? bh.getLocation() : null) : hits.keySet().iterator().next().position();
                if (at != null) {
                    serverWorld.sendParticles(ParticleTypes.EXPLOSION, at.x, at.y + 0.5, at.z, 2, 0.3, 0.3, 0.3, 0);
                    world.playSound(null, at.x, at.y, at.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.9f, 1.2f);
                    for (Entity near : world.getEntities(user, new AABB(at, at).inflate(2.5), e -> e instanceof LivingEntity && e.isAlive())) {
                        hits.merge(near, damage * 0.5f, Float::sum);
                        if (near instanceof LivingEntity l) com.cursedtides.combat.Styles.stagger(l, 30);
                    }
                }
                user.push(-aim.x * 0.5, 0.1, -aim.z * 0.5);
                user.syncVelocity = true;
            }
            if (user instanceof net.minecraft.server.level.ServerPlayer sp) {
                com.cursedtides.activity.ShootingRange.onShot(sp, hits.keySet());
            }
            for (Map.Entry<Entity, Float> hit : hits.entrySet()) {
                Entity target = hit.getKey();
                float dealt = hit.getValue();
                if (shot == ModItems.SILVER_SHOT && target instanceof LivingEntity l && (l.getType().builtInRegistryHolder().is(net.minecraft.tags.EntityTypeTags.UNDEAD)
                        || l instanceof com.cursedtides.entity.CursedPirateEntity)) dealt *= 1.75f;
                if (shot == ModItems.HOT_SHOT) { dealt *= 1.15f; target.igniteForSeconds(5); }
                target.hurt(world.damageSources().playerAttack(user), dealt);
                if (shot == ModItems.VENOM_SHOT && target instanceof LivingEntity l) {
                    l.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON, 100, 1), user);
                }
                if (target instanceof LivingEntity living) {
                    com.cursedtides.util.Compat.knock(living, stats.pellets() > 1 ? 0.8 : 0.4, -aim.x, -aim.z);
                }
            }

            serverWorld.sendParticles(ParticleTypes.LARGE_SMOKE, muzzle.x, muzzle.y, muzzle.z,
                    stats.pellets() > 1 ? 10 : 4, 0.08, 0.08, 0.08, 0.02);
            serverWorld.sendParticles(ParticleTypes.FLAME, muzzle.x, muzzle.y, muzzle.z, 3, 0.03, 0.03, 0.03, 0.01);
            gun.hurtAndBreak(1, user, hand.asEquipmentSlot());
        }

        return InteractionResult.SUCCESS;
    }

    private static Vec3 jitter(Vec3 dir, float degrees, Player user) {
        double spread = Math.toRadians(degrees);
        var random = user.getRandom();
        double yaw = (random.nextDouble() - 0.5) * 2 * spread;
        double pitch = (random.nextDouble() - 0.5) * 2 * spread;
        return dir.yRot((float) yaw).xRot((float) pitch).normalize();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display__, java.util.function.Consumer<Component> adder__, TooltipFlag type) {
        java.util.List<Component> tooltip = new java.util.ArrayList<>();
        tooltip.add(Component.translatable(stats.tooltipKey()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Sneak + use: Take Aim (next shot +50%).").withStyle(ChatFormatting.DARK_GRAY));
        if (stats.barrels() > 1) tooltip.add(Component.literal("Two barrels: fire twice, then reload.").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.forEach(adder__);
    }
}
