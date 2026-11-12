package com.cursedtides.item;

import com.cursedtides.entity.ModEntities;
import com.cursedtides.entity.ShipEntity;
import com.cursedtides.ship.ShipClass;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.List;

/**
 * Proof of ownership. Use it on open water to launch your ship; sneak-punch your ship
 * to stow her back into the deed. Rename the deed in an anvil to name your ship.
 */
public class ShipDeedItem extends Item {
    private final ShipClass shipClass;

    public ShipClass shipClass() {
        return shipClass;
    }

    public ShipDeedItem(ShipClass shipClass, Settings settings) {
        super(settings);
        this.shipClass = shipClass;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack deed = user.getStackInHand(hand);
        BlockHitResult hit = raycast(world, user, RaycastContext.FluidHandling.ANY);
        if (hit.getType() != HitResult.Type.BLOCK || !world.getFluidState(hit.getBlockPos()).isIn(FluidTags.WATER)) {
            if (!world.isClient) {
                user.sendMessage(Text.literal("Launch her on open water.").formatted(Formatting.GRAY), true);
            }
            return TypedActionResult.pass(deed);
        }
        if (world.isClient) {
            return TypedActionResult.success(deed);
        }

        ShipEntity ship = ModEntities.SHIP.create(world);
        if (ship == null) {
            return TypedActionResult.fail(deed);
        }
        Vec3d pos = hit.getPos();
        ship.setShipClass(shipClass);
        ship.setHull(shipClass.maxHull());
        ship.refreshPositionAndAngles(pos.x, pos.y, pos.z, user.getYaw(), 0.0f);
        ship.calculateDimensions();
        if (!world.isSpaceEmpty(ship, ship.getBoundingBox())) {
            user.sendMessage(Text.literal("Not enough open water here for a " + shipClass.displayName() + ".")
                    .formatted(Formatting.RED), true);
            return TypedActionResult.fail(deed);
        }
        NbtComponent saved = deed.get(DataComponentTypes.CUSTOM_DATA);
        if (saved != null) {
            ship.loadFromDeed(saved.copyNbt());
        }
        Text name = deed.get(DataComponentTypes.CUSTOM_NAME);
        ship.setCustomName(name != null ? name : Text.literal("Unnamed " + shipClass.displayName()));
        ship.setOwner(user.getUuid());
        world.spawnEntity(ship);
        world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.PLAYERS, 1.0f, 0.7f);
        user.sendMessage(Text.literal("Launched: ").append(ship.shipName()).formatted(Formatting.GOLD), true);
        if (!user.getAbilities().creativeMode) {
            deed.decrement(1);
        }
        return TypedActionResult.success(deed);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal(shipClass.displayName() + ": hull " + shipClass.maxHull() + ", "
                + shipClass.cannonsPerSide() + " guns a side, " + (shipClass.cargoRows() * 9) + " cargo slots, "
                + shipClass.seats() + " crew").formatted(Formatting.GRAY));
        NbtComponent saved = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (saved != null) {
            NbtCompound data = saved.copyNbt();
            if (data.contains("Hull")) {
                tooltip.add(Text.literal("Hull condition: " + (int) data.getFloat("Hull") + "/" + shipClass.maxHull())
                        .formatted(Formatting.DARK_AQUA));
            }
            int packed = data.getInt("Upgrades");
            if (packed != 0) {
                tooltip.add(Text.literal("Fitted: plating " + (packed & 3) + ", guns +" + ((packed >> 2) & 3)
                        + ", canvas " + ((packed >> 4) & 3) + ", hold +" + ((packed >> 6) & 3)).formatted(Formatting.DARK_AQUA));
            }
            int[] crew = data.getIntArray("Crew");
            if (crew.length > 0) {
                tooltip.add(Text.literal("Crew aboard: " + crew.length).formatted(Formatting.DARK_AQUA));
            }
            int look = data.contains("Look") ? data.getInt("Look") : (data.getBoolean("Ghost") ? ShipEntity.LOOK_GHOST : 0);
            if (look == ShipEntity.LOOK_GHOST) {
                tooltip.add(Text.literal("A captured ghost ship.").formatted(Formatting.DARK_AQUA, Formatting.ITALIC));
            } else if (look == ShipEntity.LOOK_NAVY) {
                tooltip.add(Text.literal("A captured Tidewatch warship.").formatted(Formatting.BLUE, Formatting.ITALIC));
            }
        }
        tooltip.add(Text.literal("Use on water to launch. Sneak-punch your ship to stow her.").formatted(Formatting.DARK_GRAY));
        tooltip.add(Text.literal("Rename this deed in an anvil to name your ship.").formatted(Formatting.DARK_GRAY));
    }
}
