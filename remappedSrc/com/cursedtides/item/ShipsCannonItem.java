package com.cursedtides.item;

import com.cursedtides.entity.CannonballEntity;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Aboard a boat: fires a full broadside, three cannonballs out of each side.
 * On foot: fires a single cannonball where you're looking.
 * Either way, one cannonball is used per shot.
 */
public class ShipsCannonItem extends Item {
    private static final int BROADSIDE_COOLDOWN = 60;
    private static final int SINGLE_COOLDOWN = 40;

    public ShipsCannonItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack cannon = user.getItemInHand(hand);

        if (!AmmoHelper.consume(user, ModItems.CANNONBALL)) {
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8f, 0.8f);
            if (!world.isClientSide) {
                user.displayClientMessage(Component.translatable("message.cursedtides.no_cannonball"), true);
            }
            return InteractionResultHolder.fail(cannon);
        }

        boolean broadside = user.getVehicle() instanceof Boat;
        int level = PirateData.level(user, Weapon.CANNON);
        int base = broadside ? BROADSIDE_COOLDOWN : SINGLE_COOLDOWN;
        user.getCooldowns().addCooldown(this, Math.max(20, base - level * 2));
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, SoundSource.PLAYERS, 2.0f, 0.3f);

        if (world instanceof ServerLevel serverWorld) {
            if (user.getVehicle() instanceof Boat boat) {
                fireBroadside(serverWorld, user, boat);
            } else {
                Vec3 dir = user.getViewVector(1.0f);
                CannonballEntity ball = new CannonballEntity(world, user);
                ball.shoot(dir.x, dir.y + 0.05, dir.z, 1.6f, 1.0f);
                world.addFreshEntity(ball);
                Vec3 m = user.getEyePosition().add(dir);
                serverWorld.sendParticles(ParticleTypes.LARGE_SMOKE, m.x, m.y, m.z, 8, 0.15, 0.15, 0.15, 0.02);
            }
            cannon.hurtAndBreak(1, user, LivingEntity.getSlotForHand(hand));
        }

        return InteractionResultHolder.sidedSuccess(cannon, world.isClientSide);
    }

    private static void fireBroadside(ServerLevel world, Player user, Boat boat) {
        Vec3 forward = Vec3.directionFromRotation(0.0f, boat.getYRot());
        Vec3 side = new Vec3(-forward.z, 0.0, forward.x);

        for (int s = -1; s <= 1; s += 2) {
            Vec3 out = side.scale(s);
            for (int i = -1; i <= 1; i++) {
                Vec3 origin = boat.position()
                        .add(forward.scale(i * 0.9))
                        .add(out.scale(1.3))
                        .add(0.0, 0.8, 0.0);
                CannonballEntity ball = new CannonballEntity(world, user);
                ball.setPos(origin);
                ball.shoot(out.x, 0.12, out.z, 1.8f, 2.5f);
                world.addFreshEntity(ball);
                world.sendParticles(ParticleTypes.LARGE_SMOKE, origin.x, origin.y, origin.z, 6, 0.2, 0.2, 0.2, 0.02);
                world.sendParticles(ParticleTypes.FLAME, origin.x, origin.y, origin.z, 3, 0.1, 0.1, 0.1, 0.01);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        tooltip.add(Component.translatable("tooltip.cursedtides.ships_cannon.1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.cursedtides.ships_cannon.2").withStyle(ChatFormatting.GRAY));
    }
}
