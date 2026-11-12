package com.cursedtides.quest;

import com.cursedtides.entity.DrownedCaptainEntity;
import com.cursedtides.item.ModItems;
import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class QuestEvents {
    private QuestEvents() {}

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!(entity instanceof DrownedCaptainEntity) || !(entity.getWorld() instanceof ServerWorld world)) {
                return;
            }
            world.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.BLOCK_BELL_RESONATE, SoundCategory.HOSTILE, 3.0f, 0.4f);
            for (ServerPlayerEntity player : world.getPlayers(p -> p.squaredDistanceTo(entity) < 64 * 64)) {
                if (QuestData.stage(player) != QuestData.RING_BELL) {
                    continue;
                }
                QuestData.setStage(player, QuestData.DONE);
                player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("The Bell Is Silent").formatted(Formatting.GOLD)));
                player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal("The Drowned Bell: complete").formatted(Formatting.GRAY)));
                player.sendMessage(Text.literal("Thorne's half-bell cracks apart, and the night falls quiet. "
                        + "The Shattered Reach will remember your name.").formatted(Formatting.AQUA, Formatting.ITALIC), false);
                QuestData.give(player, new ItemStack(ModItems.DOUBLOON, 60));
                QuestData.give(player, new ItemStack(Items.HEART_OF_THE_SEA));
                for (Weapon w : new Weapon[]{Weapon.CUTLASS, Weapon.PISTOL, Weapon.HEX}) {
                    PirateData.addXp(player, w, 150);
                }
                PirateData.addXp(player, Weapon.EXPLORATION, 150);
            }
        });
    }
}
