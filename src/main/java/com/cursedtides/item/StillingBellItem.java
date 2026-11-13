package com.cursedtides.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityTypes;
import com.cursedtides.entity.DrownedCaptainEntity;
import com.cursedtides.entity.ModEntities;
import com.cursedtides.quest.QuestData;
import com.cursedtides.world.Archipelago;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Rung at Hollowgrave's altar, it summons Captain Thorne for the final battle. */
public class StillingBellItem extends net.minecraft.world.item.Item {
    private static final double ALTAR_RANGE = 12.0;

    public StillingBellItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 2.0f, 1.4f);
        user.getCooldowns().addCooldown(new ItemStack(this), 60);
        if (!(world instanceof ServerLevel serverWorld)) {
            return InteractionResult.SUCCESS;
        }

        int stage = QuestData.stage(user);
        double dx = user.getX() - (Archipelago.ALTAR_X + 0.5);
        double dz = user.getZ() - (Archipelago.ALTAR_Z + 0.5);
        if (stage >= QuestData.DONE) {
            com.cursedtides.util.Compat.tell(user, Component.literal("The bell's clear note fades. Its work is done.").withStyle(ChatFormatting.GRAY), true);
        } else if (Math.hypot(dx, dz) > ALTAR_RANGE || world.dimension() != Level.OVERWORLD) {
            com.cursedtides.util.Compat.tell(user, Component.literal("The bell hums, waiting. It must be rung before Thorne's altar on Hollowgrave Island.")
                    .withStyle(ChatFormatting.GRAY), true);
        } else if (!serverWorld.getEntitiesOfClass(DrownedCaptainEntity.class, user.getBoundingBox().inflate(64), e -> true).isEmpty()) {
            com.cursedtides.util.Compat.tell(user, Component.literal("Thorne is already here!").withStyle(ChatFormatting.DARK_AQUA), true);
        } else {
            summonThorne(serverWorld);
            com.cursedtides.util.Compat.tell(user, Component.literal("The Stilling Bell's note rolls across Hollowgrave... and something answers from beneath the altar.")
                    .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC), false);
        }
        return InteractionResult.SUCCESS;
    }

    private static void summonThorne(ServerLevel world) {
        int y = Archipelago.templeFloorY();
        BlockPos pos = new BlockPos(Archipelago.ALTAR_X, y, Archipelago.ALTAR_Z + 3);
        LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(world, EntitySpawnReason.EVENT);
        if (bolt != null) {
            bolt.snapTo(Vec3.atBottomCenterOf(pos));
            bolt.setVisualOnly(true);
            world.addFreshEntity(bolt);
        }
        DrownedCaptainEntity thorne = ModEntities.DROWNED_CAPTAIN.create(world, EntitySpawnReason.EVENT);
        if (thorne == null) {
            return;
        }
        thorne.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 180, 0);
        thorne.finalizeSpawn(world, world.getCurrentDifficultyAt(pos), EntitySpawnReason.EVENT, null);
        thorne.setPersistenceRequired();
        world.addFreshEntity(thorne);
        DrownedCaptainEntity.callCrew(world, thorne, 2);
        Vec3 at = Vec3.atBottomCenterOf(pos).add(0, 1.6, 0);
        com.cursedtides.quest.Cutscenes.playNear(world, at, 60, "Captain Ashmere Thorne", java.util.List.of(
                com.cursedtides.quest.Cutscenes.orbit(at, 9, 3, 200, 250, 70, "Lightning splits the temple. Where it struck, something stands up out of the water and the dark."),
                new com.cursedtides.quest.Cutscenes.Shot(at.add(0, 1, -6), at.add(0, 0.3, -3), at, 70,
                        "Ashmere Thorne. Thirty years drowned, and his bell still rings for him. His crew rise behind him.")));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display__, java.util.function.Consumer<Component> adder__, TooltipFlag type) {
        java.util.List<Component> tooltip = new java.util.ArrayList<>();
        tooltip.add(Component.translatable("tooltip.cursedtides.stilling_bell").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltip.forEach(adder__);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
