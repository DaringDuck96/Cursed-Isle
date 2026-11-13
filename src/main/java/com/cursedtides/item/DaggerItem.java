package com.cursedtides.item;

import net.minecraft.world.item.ToolMaterial;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.Vec3;

/** Quick and light. Strike from behind for a vicious backstab. Right-click to parry. */
public class DaggerItem extends ParryingBladeItem {
    private static final float BACKSTAB = 7.0f;

    public DaggerItem(ToolMaterial material, Properties settings) {
        super(material, 8, settings);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (attacker.level() instanceof ServerLevel world && attacker instanceof Player player && target.isAlive()) {
            Vec3 facing = target.getLookAngle().multiply(1, 0, 1).normalize();
            Vec3 toAttacker = attacker.position().subtract(target.position()).multiply(1, 0, 1).normalize();
            if (facing.dot(toAttacker) < -0.4) { // attacker is behind the target
                target.setInvulnerableTime(0);
                target.hurtServer(world, world.damageSources().playerAttack(player), BACKSTAB);
                world.sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY(0.6), target.getZ(),
                        6, 0.2, 0.2, 0.2, 0.1);
                world.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_CRIT,
                        SoundSource.PLAYERS, 1.0f, 1.3f);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display__, java.util.function.Consumer<Component> adder__, TooltipFlag type) {
        adder__.accept(Component.literal("Backstab: +" + (int) BACKSTAB + " damage when you strike from behind.").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, display__, adder__, type);
    }
}
