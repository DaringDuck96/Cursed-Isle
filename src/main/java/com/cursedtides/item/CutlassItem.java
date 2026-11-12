package com.cursedtides.item;

import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**
 * A sword with a right-click skill that grows with Cutlass mastery.
 * Mastery 0-4: Lunge (dash forward, strike everything in front).
 * Mastery 5+: Whirling Blade (strike everything around you).
 */
public class CutlassItem extends SwordItem {
    public static final int WHIRL_LEVEL = 5;

    public CutlassItem(ToolMaterial material, Settings settings) {
        super(material, settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (hand != Hand.MAIN_HAND) {
            return TypedActionResult.pass(stack);
        }
        // Sneak + right-click parries instead of lunging
        if (user.isSneaking()) {
            if (user instanceof net.minecraft.server.network.ServerPlayerEntity player) {
                com.cursedtides.combat.CombatData.startParry(player, 10);
            }
            return TypedActionResult.success(stack, world.isClient);
        }

        int level = PirateData.level(user, Weapon.CUTLASS);
        boolean whirl = level >= WHIRL_LEVEL;
        float damage = 5.0f + level * 0.4f;
        int cooldown = Math.max(30, (whirl ? 90 : 70) - level * 2);
        user.getItemCooldownManager().set(this, cooldown);

        Vec3d look = user.getRotationVec(1.0f);
        Vec3d flat = new Vec3d(look.x, 0, look.z).normalize();

        if (!whirl) {
            // Dash forward.
            user.addVelocity(flat.x * 1.1, 0.15, flat.z * 1.1);
            user.velocityModified = true;
        }

        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.0f, whirl ? 0.7f : 1.1f);

        if (world instanceof ServerWorld serverWorld) {
            Box area = user.getBoundingBox().expand(whirl ? 3.5 : 4.0, 1.0, whirl ? 3.5 : 4.0);
            List<Entity> targets = world.getOtherEntities(user, area, e -> isValidTarget(user, e));
            for (Entity target : targets) {
                Vec3d toTarget = target.getPos().subtract(user.getPos());
                Vec3d toTargetFlat = new Vec3d(toTarget.x, 0, toTarget.z);
                if (!whirl && (toTargetFlat.lengthSquared() < 1.0e-4
                        || toTargetFlat.normalize().dotProduct(flat) < 0.5)) {
                    continue; // Lunge only hits things in front.
                }
                target.damage(world.getDamageSources().playerAttack(user), damage);
                if (target instanceof LivingEntity living && toTargetFlat.lengthSquared() > 1.0e-4) {
                    Vec3d push = toTargetFlat.normalize();
                    living.takeKnockback(whirl ? 0.8 : 0.5, -push.x, -push.z);
                }
            }

            if (whirl) {
                for (int i = 0; i < 16; i++) {
                    double angle = Math.PI * 2 * i / 16;
                    serverWorld.spawnParticles(ParticleTypes.SWEEP_ATTACK,
                            user.getX() + Math.cos(angle) * 2.0, user.getY() + 1.0,
                            user.getZ() + Math.sin(angle) * 2.0, 1, 0, 0, 0, 0);
                }
            } else {
                Vec3d front = user.getPos().add(flat.multiply(2.0)).add(0, 1.0, 0);
                serverWorld.spawnParticles(ParticleTypes.SWEEP_ATTACK, front.x, front.y, front.z, 3, 0.6, 0.2, 0.6, 0);
            }
            stack.damage(1, user, LivingEntity.getSlotForHand(hand));
        }
        return TypedActionResult.success(stack, world.isClient);
    }

    static boolean isValidTarget(PlayerEntity user, Entity e) {
        return e instanceof LivingEntity && e.isAlive() && !e.isSpectator()
                && !(e instanceof PlayerEntity)
                && !(e instanceof MerchantEntity)
                && !(e instanceof VillagerEntity)
                && !e.isTeammate(user);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("tooltip.cursedtides.cutlass.1").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.cursedtides.cutlass.2").formatted(Formatting.DARK_GRAY));
    }
}
