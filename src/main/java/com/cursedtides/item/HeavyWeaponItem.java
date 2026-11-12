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
import net.minecraft.particle.BlockStateParticleEffect;
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
 * Slow, brutal weapons. Every blow staggers. The broadsword's skill is a cleaving arc in front;
 * the war hammer's is a ground slam that knocks everything around you off its feet.
 */
public class HeavyWeaponItem extends SwordItem {
    public final boolean hammer;

    public HeavyWeaponItem(ToolMaterial material, boolean hammer, Settings settings) {
        super(material, settings);
        this.hammer = hammer;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (hand != Hand.MAIN_HAND) return TypedActionResult.pass(stack);
        if (hammer && !user.isOnGround()) return TypedActionResult.pass(stack);
        int level = PirateData.level(user, Weapon.CUTLASS);
        user.getItemCooldownManager().set(this, Math.max(50, 110 - level * 2));
        if (world instanceof ServerWorld sw) {
            Vec3d look = user.getRotationVec(1f);
            Vec3d flat = new Vec3d(look.x, 0, look.z).normalize();
            double reach = hammer ? 4.5 : 4.0;
            List<Entity> targets = world.getOtherEntities(user, user.getBoundingBox().expand(reach, 1.5, reach),
                    e -> CutlassItem.isValidTarget(user, e));
            float damage = (hammer ? 7f : 9f) + level * 0.35f;
            for (Entity t : targets) {
                Vec3d to = t.getPos().subtract(user.getPos()).multiply(1, 0, 1);
                if (!hammer && (to.lengthSquared() < 1e-4 || to.normalize().dotProduct(flat) < 0.3)) continue;
                t.damage(world.getDamageSources().playerAttack(user), damage);
                if (t instanceof LivingEntity living) {
                    com.cursedtides.combat.Styles.stagger(living, hammer ? 50 : 35);
                    if (hammer) living.addVelocity(0, 0.55, 0);
                    else if (to.lengthSquared() > 1e-4) living.takeKnockback(1.0, -to.normalize().x, -to.normalize().z);
                    living.velocityModified = true;
                }
            }
            if (hammer) {
                var below = world.getBlockState(user.getBlockPos().down());
                sw.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, below), user.getX(), user.getY() + 0.1, user.getZ(),
                        60, 2.0, 0.1, 2.0, 0.2);
                sw.spawnParticles(ParticleTypes.EXPLOSION, user.getX(), user.getY() + 0.3, user.getZ(), 1, 0, 0, 0, 0);
                world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, 0.6f, 1.4f);
                world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.8f, 0.6f);
            } else {
                for (int i = -3; i <= 3; i++) {
                    Vec3d dir = flat.rotateY((float) Math.toRadians(i * 20));
                    Vec3d p = user.getPos().add(dir.multiply(2.5)).add(0, 1, 0);
                    sw.spawnParticles(ParticleTypes.SWEEP_ATTACK, p.x, p.y, p.z, 1, 0, 0, 0, 0);
                }
                world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.PLAYERS, 1f, 0.6f);
            }
            user.addExhaustion(2f);
            stack.damage(2, user, LivingEntity.getSlotForHand(hand));
        }
        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("Heavy: every blow staggers the foe.").formatted(Formatting.GRAY));
        tooltip.add(Text.literal(hammer ? "Use: Ground Slam, knocking down everything around you."
                : "Use: Cleave, a wide arc that hurls foes back.").formatted(Formatting.DARK_GRAY));
    }
}
