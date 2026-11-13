package com.cursedtides.quest;

import com.cursedtides.entity.DrownedCaptainEntity;
import com.cursedtides.item.ModItems;
import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class QuestEvents {
    private QuestEvents() {}

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!(entity instanceof DrownedCaptainEntity) || !(entity.level() instanceof ServerLevel world)) {
                return;
            }
            world.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.BELL_RESONATE, SoundSource.HOSTILE, 3.0f, 0.4f);
            for (ServerPlayer player : world.getPlayers(p -> p.distanceToSqr(entity) < 64 * 64)) {
                if (QuestData.stage(player) != QuestData.RING_BELL) {
                    continue;
                }
                QuestData.setStage(player, QuestData.DONE);
                player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("The Bell Is Silent").withStyle(ChatFormatting.GOLD)));
                player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("The Drowned Bell: complete").withStyle(ChatFormatting.GRAY)));
                player.displayClientMessage(Component.literal("Thorne's half-bell cracks apart, and the night falls quiet. "
                        + "The Shattered Reach will remember your name.").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC), false);
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
