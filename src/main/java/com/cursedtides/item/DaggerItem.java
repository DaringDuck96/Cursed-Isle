package com.cursedtides.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/** Quick and light. Strike from behind for a vicious backstab. Right-click to parry. */
public class DaggerItem extends ParryingBladeItem {
    private static final float BACKSTAB = 7.0f;

    public DaggerItem(ToolMaterial material, Settings settings) {
        super(material, 8, settings);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.postHit(stack, target, attacker);
        if (attacker.getWorld() instanceof ServerWorld world && attacker instanceof PlayerEntity player && target.isAlive()) {
            Vec3d facing = target.getRotationVector().multiply(1, 0, 1).normalize();
            Vec3d toAttacker = attacker.getPos().subtract(target.getPos()).multiply(1, 0, 1).normalize();
            if (facing.dotProduct(toAttacker) < -0.4) { // attacker is behind the target
                target.timeUntilRegen = 0;
                target.damage(world.getDamageSources().playerAttack(player), BACKSTAB);
                world.spawnParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getBodyY(0.6), target.getZ(),
                        6, 0.2, 0.2, 0.2, 0.1);
                world.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT,
                        SoundCategory.PLAYERS, 1.0f, 1.3f);
            }
        }
        return result;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("Backstab: +" + (int) BACKSTAB + " damage when you strike from behind.").formatted(Formatting.GRAY));
        super.appendTooltip(stack, context, tooltip, type);
    }
}
