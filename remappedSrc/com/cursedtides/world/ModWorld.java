package com.cursedtides.world;

import com.cursedtides.CursedTides;
import com.cursedtides.item.ModItems;
import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import com.cursedtides.quest.QuestData;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.phys.Vec3;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** World type wiring: the generator feature, spawn point, island discovery and sailing XP. */
public final class ModWorld {
    private ModWorld() {}

    public static final Feature<NoneFeatureConfiguration> ARCHIPELAGO = Registry.register(BuiltInRegistries.FEATURE,
            CursedTides.id("archipelago"), new ArchipelagoFeature(NoneFeatureConfiguration.CODEC));

    /** Bit i set = island i of Archipelago.ISLANDS discovered. */
    public static final AttachmentType<Integer> DISCOVERED = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT)
            .copyOnDeath()
            .initializer(() -> 0)
            .buildAndRegister(CursedTides.id("discovered_islands"));

    private static boolean archipelago = false;
    private static final Map<UUID, Vec3> lastBoatPos = new HashMap<>();
    private static final Map<UUID, Double> sailedMeters = new HashMap<>();

    public static boolean isArchipelago() {
        return archipelago;
    }

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ServerLevel overworld = server.overworld();
            archipelago = overworld.getChunkSource().getGenerator() instanceof FlatLevelSource flat
                    && flat.settings().getBiome().is(Archipelago.BIOME);
            if (archipelago) {
                BlockPos spawn = new BlockPos(Archipelago.SPAWN_X,
                        Archipelago.standY(Archipelago.SPAWN_X, Archipelago.SPAWN_Z), Archipelago.SPAWN_Z);
                overworld.setDefaultSpawnPos(spawn, 0.0f);
                CursedTides.LOGGER.info("Cursed Archipelago detected; spawn set to Driftwood Island");
            }
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            archipelago = false;
            lastBoatPos.clear();
            sailedMeters.clear();
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            if (archipelago && QuestData.stage(player) == QuestData.START) {
                player.displayClientMessage(Component.literal("You wake on warm sand, salt in your mouth. Somewhere nearby, a lighthouse...")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
                player.displayClientMessage(Component.literal("Find the keeper of Driftwood Island. (Type /pirate any time to see your goal.)")
                        .withStyle(ChatFormatting.YELLOW), false);
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(ModWorld::tick);
    }

    private static void tick(MinecraftServer server) {
        if (server.getTickCount() % 20 != 0) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            trackSailing(player);
            if (archipelago && player.level() == server.overworld()) {
                checkDiscovery(player);
            }
        }
    }

    /** Every 25 blocks sailed earns 1 Sailing XP (any world, any boat). */
    private static void trackSailing(ServerPlayer player) {
        UUID id = player.getUUID();
        if (!(player.getVehicle() instanceof Boat boat)) {
            lastBoatPos.remove(id);
            return;
        }
        Vec3 now = boat.position();
        Vec3 before = lastBoatPos.put(id, now);
        if (before == null) {
            return;
        }
        double moved = Math.hypot(now.x - before.x, now.z - before.z);
        if (moved > 40) {
            return; // teleported, not sailed
        }
        double total = sailedMeters.getOrDefault(id, 0.0) + moved;
        int xp = (int) (total / 25.0);
        sailedMeters.put(id, total - xp * 25.0);
        if (xp > 0) {
            PirateData.addXp(player, Weapon.SAILING, xp);
        }
    }

    private static void checkDiscovery(ServerPlayer player) {
        int index = Archipelago.islandIndexAt(player.getBlockX(), player.getBlockZ());
        if (index < 0) {
            return;
        }
        int mask = player.getAttachedOrElse(DISCOVERED, 0);
        if ((mask & (1 << index)) != 0) {
            return;
        }
        player.setAttached(DISCOVERED, mask | (1 << index));
        if (Integer.bitCount(mask | (1 << index)) >= Archipelago.ISLANDS.size()) com.cursedtides.activity.Achievements.grant(player, "all_islands");
        Archipelago.Island island = Archipelago.ISLANDS.get(index);
        player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(island.name()).withStyle(ChatFormatting.GOLD)));
        player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("Island discovered").withStyle(ChatFormatting.GRAY)));
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 0.6f, 1.0f);
        int discovered = Integer.bitCount(mask | (1 << index));
        player.displayClientMessage(Component.literal("Discovered " + island.name() + " (" + discovered + "/" + Archipelago.ISLANDS.size()
                + "). +5 doubloons").withStyle(ChatFormatting.GREEN), false);
        ItemStack coins = new ItemStack(ModItems.DOUBLOON, 5 + (int) com.cursedtides.pirate.PerkData.stat(player,
                com.cursedtides.pirate.Perk.Stat.FINDERS_FEE));
        if (!player.getInventory().add(coins)) {
            player.drop(coins, false);
        }
        PirateData.addXp(player, Weapon.EXPLORATION, 60);
    }

    public static boolean hasDiscovered(ServerPlayer player, int index) {
        return (player.getAttachedOrElse(DISCOVERED, 0) & (1 << index)) != 0;
    }
}
