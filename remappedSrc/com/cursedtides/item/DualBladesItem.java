package com.cursedtides.item;

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
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** A pair of short blades. Quick strikes build combos twice as long; right-click is a three-cut flurry on whatever's in front. */
public class DualBladesItem extends SwordItem {
    public DualBladesItem(Tier material, Properties settings) {
        super(material, settings);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(stack);
        int level = PirateData.level(user, Weapon.CUTLASS);
        if (user.isShiftKeyDown()) {
            // Brawl: a boot to the chest of whoever is in front of you
            if (world instanceof ServerLevel sw) {
                Vec3 f = user.getViewVector(1f).multiply(1, 0, 1).normalize();
                for (Entity t : world.getEntities(user, user.getBoundingBox().inflate(2.5, 1, 2.5), e -> CutlassItem.isValidTarget(user, e))) {
                    Vec3 to = t.position().subtract(user.position()).multiply(1, 0, 1);
                    if (to.lengthSqr() < 1e-4 || to.normalize().dot(f) < 0.5) continue;
                    t.hurt(world.damageSources().playerAttack(user), 2f);
                    if (t instanceof LivingEntity l) {
                        l.knockback(1.6, -f.x, -f.z);
                        com.cursedtides.combat.Styles.stagger(l, 40);
                    }
                }
                sw.playSound(null, user.blockPosition(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.PLAYERS, 1f, 0.8f);
                user.getCooldowns().addCooldown(this, 60);
            }
            return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
        }
        user.getCooldowns().addCooldown(this, Math.max(25, 60 - level));
        if (world instanceof ServerLevel sw) {
            Vec3 look = user.getViewVector(1f);
            Vec3 flat = new Vec3(look.x, 0, look.z).normalize();
            List<Entity> targets = world.getEntities(user, user.getBoundingBox().inflate(3.2, 1, 3.2),
                    e -> CutlassItem.isValidTarget(user, e));
            for (Entity t : targets) {
                Vec3 to = t.position().subtract(user.position()).multiply(1, 0, 1);
                if (to.lengthSqr() < 1e-4 || to.normalize().dot(flat) < 0.6) continue;
                float per = 2.5f + level * 0.15f;
                for (int i = 0; i < 3; i++) {
                    t.invulnerableTime = 0;
                    t.hurt(world.damageSources().playerAttack(user), per);
                }
                sw.sendParticles(ParticleTypes.SWEEP_ATTACK, t.getX(), t.getY(0.5), t.getZ(), 3, 0.3, 0.3, 0.3, 0);
                if (t instanceof LivingEntity living) com.cursedtides.combat.Styles.stagger(living, 20);
            }
            world.playSound(null, user.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1f, 1.5f);
            world.playSound(null, user.blockPosition(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 0.7f, 1.7f);
            stack.hurtAndBreak(1, user, LivingEntity.getSlotForHand(hand));
        }
        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        tooltip.add(Component.literal("Combos last twice as long with paired blades.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Use: Flurry, three quick cuts on the foe in front.").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("Sneak + use: Brawl, a kick that sends a foe sprawling.").withStyle(ChatFormatting.DARK_GRAY));
    }
}
