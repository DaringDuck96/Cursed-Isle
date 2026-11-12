package com.cursedtides.item;

import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/** A pair of short blades. Quick strikes build combos twice as long; right-click is a three-cut flurry on whatever's in front. */
public class DualBladesItem extends SwordItem {
    public DualBladesItem(ToolMaterial material, Settings settings) {
        super(material, settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (hand != Hand.MAIN_HAND) return TypedActionResult.pass(stack);
        int level = PirateData.level(user, Weapon.CUTLASS);
        if (user.isSneaking()) {
            // Brawl: a boot to the chest of whoever is in front of you
            if (world instanceof ServerWorld sw) {
                Vec3d f = user.getRotationVec(1f).multiply(1, 0, 1).normalize();
                for (Entity t : world.getOtherEntities(user, user.getBoundingBox().expand(2.5, 1, 2.5), e -> CutlassItem.isValidTarget(user, e))) {
                    Vec3d to = t.getPos().subtract(user.getPos()).multiply(1, 0, 1);
                    if (to.lengthSquared() < 1e-4 || to.normalize().dotProduct(f) < 0.5) continue;
                    t.damage(world.getDamageSources().playerAttack(user), 2f);
                    if (t instanceof LivingEntity l) {
                        l.takeKnockback(1.6, -f.x, -f.z);
                        com.cursedtides.combat.Styles.stagger(l, 40);
                    }
                }
                sw.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.PLAYERS, 1f, 0.8f);
                user.getItemCooldownManager().set(this, 60);
            }
            return TypedActionResult.success(stack, world.isClient);
        }
        user.getItemCooldownManager().set(this, Math.max(25, 60 - level));
        if (world instanceof ServerWorld sw) {
            Vec3d look = user.getRotationVec(1f);
            Vec3d flat = new Vec3d(look.x, 0, look.z).normalize();
            List<Entity> targets = world.getOtherEntities(user, user.getBoundingBox().expand(3.2, 1, 3.2),
                    e -> CutlassItem.isValidTarget(user, e));
            for (Entity t : targets) {
                Vec3d to = t.getPos().subtract(user.getPos()).multiply(1, 0, 1);
                if (to.lengthSquared() < 1e-4 || to.normalize().dotProduct(flat) < 0.6) continue;
                float per = 2.5f + level * 0.15f;
                for (int i = 0; i < 3; i++) {
                    t.timeUntilRegen = 0;
                    t.damage(world.getDamageSources().playerAttack(user), per);
                }
                sw.spawnParticles(ParticleTypes.SWEEP_ATTACK, t.getX(), t.getBodyY(0.5), t.getZ(), 3, 0.3, 0.3, 0.3, 0);
                if (t instanceof LivingEntity living) com.cursedtides.combat.Styles.stagger(living, 20);
            }
            world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1f, 1.5f);
            world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.PLAYERS, 0.7f, 1.7f);
            stack.damage(1, user, LivingEntity.getSlotForHand(hand));
        }
        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("Combos last twice as long with paired blades.").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("Use: Flurry, three quick cuts on the foe in front.").formatted(Formatting.DARK_GRAY));
        tooltip.add(Text.literal("Sneak + use: Brawl, a kick that sends a foe sprawling.").formatted(Formatting.DARK_GRAY));
    }
}
