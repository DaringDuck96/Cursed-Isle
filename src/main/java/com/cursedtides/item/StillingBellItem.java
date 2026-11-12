package com.cursedtides.item;

import com.cursedtides.entity.DrownedCaptainEntity;
import com.cursedtides.entity.ModEntities;
import com.cursedtides.quest.QuestData;
import com.cursedtides.world.Archipelago;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/** Rung at Hollowgrave's altar, it summons Captain Thorne for the final battle. */
public class StillingBellItem extends net.minecraft.item.Item {
    private static final double ALTAR_RANGE = 12.0;

    public StillingBellItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.BLOCK_BELL_USE, SoundCategory.PLAYERS, 2.0f, 1.4f);
        user.getItemCooldownManager().set(this, 60);
        if (!(world instanceof ServerWorld serverWorld)) {
            return TypedActionResult.success(stack, true);
        }

        int stage = QuestData.stage(user);
        double dx = user.getX() - (Archipelago.ALTAR_X + 0.5);
        double dz = user.getZ() - (Archipelago.ALTAR_Z + 0.5);
        if (stage >= QuestData.DONE) {
            user.sendMessage(Text.literal("The bell's clear note fades. Its work is done.").formatted(Formatting.GRAY), true);
        } else if (Math.hypot(dx, dz) > ALTAR_RANGE || world.getRegistryKey() != World.OVERWORLD) {
            user.sendMessage(Text.literal("The bell hums, waiting. It must be rung before Thorne's altar on Hollowgrave Island.")
                    .formatted(Formatting.GRAY), true);
        } else if (!serverWorld.getEntitiesByClass(DrownedCaptainEntity.class, user.getBoundingBox().expand(64), e -> true).isEmpty()) {
            user.sendMessage(Text.literal("Thorne is already here!").formatted(Formatting.DARK_AQUA), true);
        } else {
            summonThorne(serverWorld);
            user.sendMessage(Text.literal("The Stilling Bell's note rolls across Hollowgrave... and something answers from beneath the altar.")
                    .formatted(Formatting.DARK_AQUA, Formatting.ITALIC), false);
        }
        return TypedActionResult.success(stack, false);
    }

    private static void summonThorne(ServerWorld world) {
        int y = Archipelago.templeFloorY();
        BlockPos pos = new BlockPos(Archipelago.ALTAR_X, y, Archipelago.ALTAR_Z + 3);
        LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(world);
        if (bolt != null) {
            bolt.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(pos));
            bolt.setCosmetic(true);
            world.spawnEntity(bolt);
        }
        DrownedCaptainEntity thorne = ModEntities.DROWNED_CAPTAIN.create(world);
        if (thorne == null) {
            return;
        }
        thorne.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 180, 0);
        thorne.initialize(world, world.getLocalDifficulty(pos), SpawnReason.EVENT, null);
        thorne.setPersistent();
        world.spawnEntity(thorne);
        DrownedCaptainEntity.callCrew(world, thorne, 2);
        Vec3d at = Vec3d.ofBottomCenter(pos).add(0, 1.6, 0);
        com.cursedtides.quest.Cutscenes.playNear(world, at, 60, "Captain Ashmere Thorne", java.util.List.of(
                com.cursedtides.quest.Cutscenes.orbit(at, 9, 3, 200, 250, 70, "Lightning splits the temple. Where it struck, something stands up out of the water and the dark."),
                new com.cursedtides.quest.Cutscenes.Shot(at.add(0, 1, -6), at.add(0, 0.3, -3), at, 70,
                        "Ashmere Thorne. Thirty years drowned, and his bell still rings for him. His crew rise behind him.")));
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("tooltip.cursedtides.stilling_bell").formatted(Formatting.GRAY, Formatting.ITALIC));
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}
