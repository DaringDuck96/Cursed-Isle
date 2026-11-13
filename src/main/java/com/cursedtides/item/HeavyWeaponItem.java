package com.cursedtides.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.InteractionResult;
import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Slow, brutal weapons. Every blow staggers. The broadsword's skill is a cleaving arc in front;
 * the war hammer's is a ground slam that knocks everything around you off its feet.
 */
public class HeavyWeaponItem extends Item {
    public final boolean hammer;

    public HeavyWeaponItem(ToolMaterial material, boolean hammer, Properties settings) {
        super(settings);
        this.hammer = hammer;
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (user.isShiftKeyDown()) {
            // Taunt: every foe nearby turns on you, and you brace for it
            if (world instanceof ServerLevel sw) {
                int n = 0;
                for (net.minecraft.world.entity.Mob m : world.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
                        user.getBoundingBox().inflate(14), m -> m instanceof net.minecraft.world.entity.monster.Enemy && m.isAlive())) {
                    m.setTarget(user);
                    n++;
                }
                user.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.RESISTANCE, 160, 1));
                user.getCooldowns().addCooldown(new ItemStack(this), 300);
                sw.playSound(null, user.blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 0.6f, 1.4f);
                com.cursedtides.util.Compat.tell(user, Component.literal("\"Come on, then!\" " + n + " foes turn on you.").withStyle(ChatFormatting.GOLD), true);
            }
            return InteractionResult.SUCCESS;
        }
        if (hammer && !user.onGround()) return InteractionResult.PASS;
        int level = PirateData.level(user, Weapon.CUTLASS);
        user.getCooldowns().addCooldown(new ItemStack(this), Math.max(50, 110 - level * 2));
        if (world instanceof ServerLevel sw) {
            Vec3 look = user.getViewVector(1f);
            Vec3 flat = new Vec3(look.x, 0, look.z).normalize();
            double reach = hammer ? 4.5 : 4.0;
            List<Entity> targets = world.getEntities(user, user.getBoundingBox().inflate(reach, 1.5, reach),
                    e -> CutlassItem.isValidTarget(user, e));
            float damage = (hammer ? 7f : 9f) + level * 0.35f;
            for (Entity t : targets) {
                Vec3 to = t.position().subtract(user.position()).multiply(1, 0, 1);
                if (!hammer && (to.lengthSqr() < 1e-4 || to.normalize().dot(flat) < 0.3)) continue;
                t.hurt(world.damageSources().playerAttack(user), damage);
                if (t instanceof LivingEntity living) {
                    com.cursedtides.combat.Styles.stagger(living, hammer ? 50 : 35);
                    if (hammer) living.push(0, 0.55, 0);
                    else if (to.lengthSqr() > 1e-4) com.cursedtides.util.Compat.knock(living, 1.0, -to.normalize().x, -to.normalize().z);
                    living.syncVelocity = true;
                }
            }
            if (hammer) {
                var below = world.getBlockState(user.blockPosition().below());
                sw.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, below), user.getX(), user.getY() + 0.1, user.getZ(),
                        60, 2.0, 0.1, 2.0, 0.2);
                sw.sendParticles(ParticleTypes.EXPLOSION, user.getX(), user.getY() + 0.3, user.getZ(), 1, 0, 0, 0, 0);
                world.playSound(null, user.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.6f, 1.4f);
                world.playSound(null, user.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.8f, 0.6f);
            } else {
                for (int i = -3; i <= 3; i++) {
                    Vec3 dir = flat.yRot((float) Math.toRadians(i * 20));
                    Vec3 p = user.position().add(dir.scale(2.5)).add(0, 1, 0);
                    sw.sendParticles(ParticleTypes.SWEEP_ATTACK, p.x, p.y, p.z, 1, 0, 0, 0, 0);
                }
                world.playSound(null, user.blockPosition(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1f, 0.6f);
            }
            user.causeFoodExhaustion(2f);
            stack.hurtAndBreak(2, user, hand.asEquipmentSlot());
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display__, java.util.function.Consumer<Component> adder__, TooltipFlag type) {
        java.util.List<Component> tooltip = new java.util.ArrayList<>();
        tooltip.add(Component.literal("Heavy: every blow staggers the foe.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(hammer ? "Use: Ground Slam, knocking down everything around you."
                : "Use: Cleave, a wide arc that hurls foes back.").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("Sneak + use: Taunt, drawing every foe nearby onto you.").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.forEach(adder__);
    }
}
