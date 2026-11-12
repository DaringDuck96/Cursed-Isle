package com.cursedtides.item;

import com.cursedtides.entity.HexBoltEntity;
import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/** Casts hex bolts using voodoo energy, which refills on its own over time. */
public class HexStaffItem extends Item {
    public static final int COST = 15;

    public HexStaffItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack staff = user.getStackInHand(hand);

        int cost = (int) Math.round(COST * (1.0 - com.cursedtides.pirate.PerkData.stat(user,
                com.cursedtides.pirate.Perk.Stat.HEX_COST)));
        if (!world.isClient && !PirateData.spendVoodoo(user, cost)) {
            user.sendMessage(Text.translatable("message.cursedtides.no_voodoo").formatted(Formatting.DARK_AQUA), true);
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.5f, 1.6f);
            return TypedActionResult.fail(staff);
        }

        int level = PirateData.level(user, Weapon.HEX);
        user.getItemCooldownManager().set(this, Math.max(8, 20 - level / 2));
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ENTITY_EVOKER_CAST_SPELL, SoundCategory.PLAYERS, 0.8f, 1.3f);

        if (!world.isClient) {
            Vec3d dir = user.getRotationVec(1.0f);
            HexBoltEntity bolt = new HexBoltEntity(world, user, level);
            bolt.setBonusDamage((float) com.cursedtides.pirate.PerkData.stat(user, com.cursedtides.pirate.Perk.Stat.HEX_DAMAGE));
            bolt.setVelocity(dir.x, dir.y, dir.z, 1.6f, 0.0f);
            world.spawnEntity(bolt);
            staff.damage(1, user, LivingEntity.getSlotForHand(hand));
        }
        return TypedActionResult.success(staff, world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("tooltip.cursedtides.hex_staff.1").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.cursedtides.hex_staff.2").formatted(Formatting.DARK_GRAY));
    }
}
