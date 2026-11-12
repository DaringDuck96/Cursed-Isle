package com.cursedtides.item;

import com.cursedtides.entity.CannonballEntity;
import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**
 * Aboard a boat: fires a full broadside, three cannonballs out of each side.
 * On foot: fires a single cannonball where you're looking.
 * Either way, one cannonball is used per shot.
 */
public class ShipsCannonItem extends Item {
    private static final int BROADSIDE_COOLDOWN = 60;
    private static final int SINGLE_COOLDOWN = 40;

    public ShipsCannonItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack cannon = user.getStackInHand(hand);

        if (!AmmoHelper.consume(user, ModItems.CANNONBALL)) {
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.PLAYERS, 0.8f, 0.8f);
            if (!world.isClient) {
                user.sendMessage(Text.translatable("message.cursedtides.no_cannonball"), true);
            }
            return TypedActionResult.fail(cannon);
        }

        boolean broadside = user.getVehicle() instanceof BoatEntity;
        int level = PirateData.level(user, Weapon.CANNON);
        int base = broadside ? BROADSIDE_COOLDOWN : SINGLE_COOLDOWN;
        user.getItemCooldownManager().set(this, Math.max(20, base - level * 2));
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, SoundCategory.PLAYERS, 2.0f, 0.3f);

        if (world instanceof ServerWorld serverWorld) {
            if (user.getVehicle() instanceof BoatEntity boat) {
                fireBroadside(serverWorld, user, boat);
            } else {
                Vec3d dir = user.getRotationVec(1.0f);
                CannonballEntity ball = new CannonballEntity(world, user);
                ball.setVelocity(dir.x, dir.y + 0.05, dir.z, 1.6f, 1.0f);
                world.spawnEntity(ball);
                Vec3d m = user.getEyePos().add(dir);
                serverWorld.spawnParticles(ParticleTypes.LARGE_SMOKE, m.x, m.y, m.z, 8, 0.15, 0.15, 0.15, 0.02);
            }
            cannon.damage(1, user, LivingEntity.getSlotForHand(hand));
        }

        return TypedActionResult.success(cannon, world.isClient);
    }

    private static void fireBroadside(ServerWorld world, PlayerEntity user, BoatEntity boat) {
        Vec3d forward = Vec3d.fromPolar(0.0f, boat.getYaw());
        Vec3d side = new Vec3d(-forward.z, 0.0, forward.x);

        for (int s = -1; s <= 1; s += 2) {
            Vec3d out = side.multiply(s);
            for (int i = -1; i <= 1; i++) {
                Vec3d origin = boat.getPos()
                        .add(forward.multiply(i * 0.9))
                        .add(out.multiply(1.3))
                        .add(0.0, 0.8, 0.0);
                CannonballEntity ball = new CannonballEntity(world, user);
                ball.setPosition(origin);
                ball.setVelocity(out.x, 0.12, out.z, 1.8f, 2.5f);
                world.spawnEntity(ball);
                world.spawnParticles(ParticleTypes.LARGE_SMOKE, origin.x, origin.y, origin.z, 6, 0.2, 0.2, 0.2, 0.02);
                world.spawnParticles(ParticleTypes.FLAME, origin.x, origin.y, origin.z, 3, 0.1, 0.1, 0.1, 0.01);
            }
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("tooltip.cursedtides.ships_cannon.1").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.cursedtides.ships_cannon.2").formatted(Formatting.GRAY));
    }
}
