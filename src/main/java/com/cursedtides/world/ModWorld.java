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
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** World type wiring: the generator feature, spawn point, island discovery and sailing XP. */
public final class ModWorld {
    private ModWorld() {}

    public static final Feature<DefaultFeatureConfig> ARCHIPELAGO = Registry.register(Registries.FEATURE,
            CursedTides.id("archipelago"), new ArchipelagoFeature(DefaultFeatureConfig.CODEC));

    /** Bit i set = island i of Archipelago.ISLANDS discovered. */
    public static final AttachmentType<Integer> DISCOVERED = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT)
            .copyOnDeath()
            .initializer(() -> 0)
            .buildAndRegister(CursedTides.id("discovered_islands"));

    private static boolean archipelago = false;
    private static final Map<UUID, Vec3d> lastBoatPos = new HashMap<>();
    private static final Map<UUID, Double> sailedMeters = new HashMap<>();

    public static boolean isArchipelago() {
        return archipelago;
    }

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ServerWorld overworld = server.getOverworld();
            archipelago = overworld.getChunkManager().getChunkGenerator() instanceof FlatChunkGenerator flat
                    && flat.getConfig().getBiome().matchesKey(Archipelago.BIOME);
            if (archipelago) {
                BlockPos spawn = new BlockPos(Archipelago.SPAWN_X,
                        Archipelago.standY(Archipelago.SPAWN_X, Archipelago.SPAWN_Z), Archipelago.SPAWN_Z);
                overworld.setSpawnPos(spawn, 0.0f);
                CursedTides.LOGGER.info("Cursed Archipelago detected; spawn set to Driftwood Island");
            }
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            archipelago = false;
            lastBoatPos.clear();
            sailedMeters.clear();
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            if (archipelago && QuestData.stage(player) == QuestData.START) {
                player.sendMessage(Text.literal("You wake on warm sand, salt in your mouth. Somewhere nearby, a lighthouse...")
                        .formatted(Formatting.GRAY, Formatting.ITALIC), false);
                player.sendMessage(Text.literal("Find the keeper of Driftwood Island. (Type /pirate any time to see your goal.)")
                        .formatted(Formatting.YELLOW), false);
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(ModWorld::tick);
    }

    private static void tick(MinecraftServer server) {
        if (server.getTicks() % 20 != 0) {
            return;
        }
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            trackSailing(player);
            if (archipelago && player.getWorld() == server.getOverworld()) {
                checkDiscovery(player);
            }
        }
    }

    /** Every 25 blocks sailed earns 1 Sailing XP (any world, any boat). */
    private static void trackSailing(ServerPlayerEntity player) {
        UUID id = player.getUuid();
        if (!(player.getVehicle() instanceof BoatEntity boat)) {
            lastBoatPos.remove(id);
            return;
        }
        Vec3d now = boat.getPos();
        Vec3d before = lastBoatPos.put(id, now);
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

    private static void checkDiscovery(ServerPlayerEntity player) {
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
        player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(island.name()).formatted(Formatting.GOLD)));
        player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal("Island discovered").formatted(Formatting.GRAY)));
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 0.6f, 1.0f);
        int discovered = Integer.bitCount(mask | (1 << index));
        player.sendMessage(Text.literal("Discovered " + island.name() + " (" + discovered + "/" + Archipelago.ISLANDS.size()
                + "). +5 doubloons").formatted(Formatting.GREEN), false);
        ItemStack coins = new ItemStack(ModItems.DOUBLOON, 5 + (int) com.cursedtides.pirate.PerkData.stat(player,
                com.cursedtides.pirate.Perk.Stat.FINDERS_FEE));
        if (!player.getInventory().insertStack(coins)) {
            player.dropItem(coins, false);
        }
        PirateData.addXp(player, Weapon.EXPLORATION, 60);
    }

    public static boolean hasDiscovered(ServerPlayerEntity player, int index) {
        return (player.getAttachedOrElse(DISCOVERED, 0) & (1 << index)) != 0;
    }
}
