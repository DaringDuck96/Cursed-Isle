package com.cursedtides.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.InteractionResult;
import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * A sword with a right-click skill that grows with Cutlass mastery.
 * Mastery 0-4: Lunge (dash forward, strike everything in front).
 * Mastery 5+: Whirling Blade (strike everything around you).
 */
public class CutlassItem extends Item {
    public static final int WHIRL_LEVEL = 5;

    public CutlassItem(ToolMaterial material, Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        // Sneak + right-click parries instead of lunging
        if (user.isShiftKeyDown()) {
            if (user instanceof net.minecraft.server.level.ServerPlayer player) {
                com.cursedtides.combat.CombatData.startParry(player, 10);
            }
            return InteractionResult.SUCCESS;
        }

        int level = PirateData.level(user, Weapon.CUTLASS);
        boolean whirl = level >= WHIRL_LEVEL;
        float damage = 5.0f + level * 0.4f;
        int cooldown = Math.max(30, (whirl ? 90 : 70) - level * 2);
        user.getCooldowns().addCooldown(new ItemStack(this), cooldown);

        Vec3 look = user.getViewVector(1.0f);
        Vec3 flat = new Vec3(look.x, 0, look.z).normalize();

        if (!whirl) {
            // Dash forward.
            user.push(flat.x * 1.1, 0.15, flat.z * 1.1);
            user.syncVelocity = true;
        }

        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, whirl ? 0.7f : 1.1f);

        if (world instanceof ServerLevel serverWorld) {
            AABB area = user.getBoundingBox().inflate(whirl ? 3.5 : 4.0, 1.0, whirl ? 3.5 : 4.0);
            List<Entity> targets = world.getEntities(user, area, e -> isValidTarget(user, e));
            for (Entity target : targets) {
                Vec3 toTarget = target.position().subtract(user.position());
                Vec3 toTargetFlat = new Vec3(toTarget.x, 0, toTarget.z);
                if (!whirl && (toTargetFlat.lengthSqr() < 1.0e-4
                        || toTargetFlat.normalize().dot(flat) < 0.5)) {
                    continue; // Lunge only hits things in front.
                }
                target.hurt(world.damageSources().playerAttack(user), damage);
                if (target instanceof LivingEntity living && toTargetFlat.lengthSqr() > 1.0e-4) {
                    Vec3 push = toTargetFlat.normalize();
                    com.cursedtides.util.Compat.knock(living, whirl ? 0.8 : 0.5, -push.x, -push.z);
                }
            }

            if (whirl) {
                for (int i = 0; i < 16; i++) {
                    double angle = Math.PI * 2 * i / 16;
                    serverWorld.sendParticles(ParticleTypes.SWEEP_ATTACK,
                            user.getX() + Math.cos(angle) * 2.0, user.getY() + 1.0,
                            user.getZ() + Math.sin(angle) * 2.0, 1, 0, 0, 0, 0);
                }
            } else {
                Vec3 front = user.position().add(flat.scale(2.0)).add(0, 1.0, 0);
                serverWorld.sendParticles(ParticleTypes.SWEEP_ATTACK, front.x, front.y, front.z, 3, 0.6, 0.2, 0.6, 0);
            }
            stack.hurtAndBreak(1, user, hand.asEquipmentSlot());
        }
        return InteractionResult.SUCCESS;
    }

    static boolean isValidTarget(Player user, Entity e) {
        return e instanceof LivingEntity && e.isAlive() && !e.isSpectator()
                && !(e instanceof Player)
                && !(e instanceof AbstractVillager)
                && !(e instanceof Villager)
                && !e.isAlliedTo(user);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display__, java.util.function.Consumer<Component> adder__, TooltipFlag type) {
        java.util.List<Component> tooltip = new java.util.ArrayList<>();
        tooltip.add(Component.translatable("tooltip.cursedtides.cutlass.1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.cursedtides.cutlass.2").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.forEach(adder__);
    }
}
