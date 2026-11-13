package com.cursedtides.item;

import com.cursedtides.entity.ModEntities;
import com.cursedtides.entity.ShipEntity;
import com.cursedtides.ship.ShipClass;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Proof of ownership. Use it on open water to launch your ship; sneak-punch your ship
 * to stow her back into the deed. Rename the deed in an anvil to name your ship.
 */
public class ShipDeedItem extends Item {
    private final ShipClass shipClass;

    public ShipClass shipClass() {
        return shipClass;
    }

    public ShipDeedItem(ShipClass shipClass, Properties settings) {
        super(settings);
        this.shipClass = shipClass;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack deed = user.getItemInHand(hand);
        BlockHitResult hit = getPlayerPOVHitResult(world, user, ClipContext.Fluid.ANY);
        if (hit.getType() != HitResult.Type.BLOCK || !world.getFluidState(hit.getBlockPos()).is(FluidTags.WATER)) {
            if (!world.isClientSide) {
                user.displayClientMessage(Component.literal("Launch her on open water.").withStyle(ChatFormatting.GRAY), true);
            }
            return InteractionResultHolder.pass(deed);
        }
        if (world.isClientSide) {
            return InteractionResultHolder.success(deed);
        }

        ShipEntity ship = ModEntities.SHIP.create(world);
        if (ship == null) {
            return InteractionResultHolder.fail(deed);
        }
        Vec3 pos = hit.getLocation();
        ship.setShipClass(shipClass);
        ship.setHull(shipClass.maxHull());
        ship.moveTo(pos.x, pos.y, pos.z, user.getYRot(), 0.0f);
        ship.refreshDimensions();
        if (!world.noCollision(ship, ship.getBoundingBox())) {
            user.displayClientMessage(Component.literal("Not enough open water here for a " + shipClass.displayName() + ".")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(deed);
        }
        CustomData saved = deed.get(DataComponents.CUSTOM_DATA);
        if (saved != null) {
            ship.loadFromDeed(saved.copyTag());
        }
        Component name = deed.get(DataComponents.CUSTOM_NAME);
        ship.setCustomName(name != null ? name : Component.literal("Unnamed " + shipClass.displayName()));
        ship.setOwner(user.getUUID());
        world.addFreshEntity(ship);
        world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.0f, 0.7f);
        user.displayClientMessage(Component.literal("Launched: ").append(ship.shipName()).withStyle(ChatFormatting.GOLD), true);
        if (!user.getAbilities().instabuild) {
            deed.shrink(1);
        }
        return InteractionResultHolder.success(deed);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        tooltip.add(Component.literal(shipClass.displayName() + ": hull " + shipClass.maxHull() + ", "
                + shipClass.cannonsPerSide() + " guns a side, " + (shipClass.cargoRows() * 9) + " cargo slots, "
                + shipClass.seats() + " crew").withStyle(ChatFormatting.GRAY));
        CustomData saved = stack.get(DataComponents.CUSTOM_DATA);
        if (saved != null) {
            CompoundTag data = saved.copyTag();
            if (data.contains("Hull")) {
                tooltip.add(Component.literal("Hull condition: " + (int) data.getFloat("Hull") + "/" + shipClass.maxHull())
                        .withStyle(ChatFormatting.DARK_AQUA));
            }
            int packed = data.getInt("Upgrades");
            if (packed != 0) {
                tooltip.add(Component.literal("Fitted: plating " + (packed & 3) + ", guns +" + ((packed >> 2) & 3)
                        + ", canvas " + ((packed >> 4) & 3) + ", hold +" + ((packed >> 6) & 3)).withStyle(ChatFormatting.DARK_AQUA));
            }
            int[] crew = data.getIntArray("Crew");
            if (crew.length > 0) {
                tooltip.add(Component.literal("Crew aboard: " + crew.length).withStyle(ChatFormatting.DARK_AQUA));
            }
            int look = data.contains("Look") ? data.getInt("Look") : (data.getBoolean("Ghost") ? ShipEntity.LOOK_GHOST : 0);
            if (look == ShipEntity.LOOK_GHOST) {
                tooltip.add(Component.literal("A captured ghost ship.").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC));
            } else if (look == ShipEntity.LOOK_NAVY) {
                tooltip.add(Component.literal("A captured Tidewatch warship.").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
            }
        }
        tooltip.add(Component.literal("Use on water to launch. Sneak-punch your ship to stow her.").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("Rename this deed in an anvil to name your ship.").withStyle(ChatFormatting.DARK_GRAY));
    }
}
