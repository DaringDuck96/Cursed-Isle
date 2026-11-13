package com.cursedtides.activity;

import net.minecraft.world.entity.EntityTypes;
import com.cursedtides.entity.ModEntities;
import com.cursedtides.living.Residents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The sword tournament: five waves of challengers in a ring around the arena master. Nobody dies in the pit;
 * a fighter who would fall is carried out instead. Range weapons are frowned on but not forbidden.
 */
public final class Arena {
    private Arena() {}

    public static final String TAG = "cursedtides_arena";
    public static final int WAVES = 5;
    private static final double RING = 22;

    private static final class Bout {
        final UUID player; final Vec3 centre; int wave; final List<UUID> foes = new ArrayList<>(); int pause;
        Bout(UUID player, Vec3 centre) { this.player = player; this.centre = centre; }
    }
    private static final Map<UUID, Bout> BOUTS = new HashMap<>();

    public static boolean busy(ServerPlayer p) { return BOUTS.containsKey(p.getUUID()); }

    public static List<String> start(ServerPlayer p, Entity master) {
        if (busy(p)) return List.of("You're already in the pit!");
        if (!com.cursedtides.trade.Wallet.pay(p, 30)) return List.of("Three doubloons to enter. Fighters pay, the crowd watches free.");
        Bout b = new Bout(p.getUUID(), master.position());
        b.pause = 60;
        BOUTS.put(p.getUUID(), b);
        p.level().playSound(null, master.blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.5f, 1.0f);
        return List.of("Into the ring, then! Five waves. Last one standing takes the purse.",
                "Stay inside the posts. Step out and you forfeit. Good luck, {player}.");
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(Arena::tick);
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((victim, source, amount) -> {
            if (!(victim instanceof ServerPlayer p)) return true;
            Bout b = BOUTS.get(p.getUUID());
            if (b == null || amount < p.getHealth()) return true;
            p.setHealth(4f);
            finish(p, b, false, "You're carried from the pit, battered but breathing.");
            return false;
        });
    }

    private static void tick(MinecraftServer server) {
        if (BOUTS.isEmpty() || server.getTickCount() % 5 != 0) return;
        for (Bout b : new ArrayList<>(BOUTS.values())) {
            ServerPlayer p = server.getPlayerList().getPlayer(b.player);
            if (p == null) { cleanup(server.overworld(), b); BOUTS.remove(b.player); continue; }
            ServerLevel world = p.level();
            if (p.position().distanceToSqr(b.centre) > RING * RING) { finish(p, b, false, "You left the ring. The crowd jeers."); continue; }
            if (b.pause > 0) {
                b.pause -= 5;
                if (b.pause <= 0) spawnWave(world, p, b);
                continue;
            }
            b.foes.removeIf(id -> {
                Entity e = world.getEntity(id);
                return e == null || !e.isAlive();
            });
            if (b.foes.isEmpty()) {
                Challenges.progress(p, "arena", 1);
                if (b.wave >= WAVES) { finish(p, b, true, null); continue; }
                p.sendSystemMessage(Component.literal("Wave " + b.wave + " down! The next comes in five seconds.").withStyle(ChatFormatting.GOLD), true);
                world.playSound(null, p.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5f, 1.5f);
                b.pause = 100;
            }
        }
    }

    private static void spawnWave(ServerLevel world, ServerPlayer p, Bout b) {
        b.wave++;
        int count = 1 + b.wave;
        for (int i = 0; i < count; i++) {
            double a = Math.PI * 2 * i / count;
            BlockPos at = Residents.openGround(world, (int) (b.centre.x + Math.cos(a) * 9), (int) (b.centre.z + Math.sin(a) * 9), 4);
            if (at == null) continue;
            Mob foe = (Mob) (b.wave >= 4 && i == 0 ? EntityTypes.VINDICATOR.create(world, EntitySpawnReason.EVENT)
                    : b.wave >= 3 ? ModEntities.CURSED_PIRATE.create(world, EntitySpawnReason.EVENT) : EntityTypes.ZOMBIE.create(world, EntitySpawnReason.EVENT));
            if (foe == null) continue;
            foe.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0, 0);
            foe.finalizeSpawn(world, world.getCurrentDifficultyAt(at), EntitySpawnReason.EVENT, null);
            foe.setCustomName(Component.literal(b.wave >= 4 && i == 0 ? "Pit Champion" : "Challenger").withStyle(ChatFormatting.RED));
            foe.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
            foe.addTag(TAG);
            foe.setTarget(p);
            Residents.markTransient(foe, world.getGameTime() + 6000);
            world.addFreshEntity(foe);
            b.foes.add(foe.getUUID());
        }
        p.sendSystemMessage(Component.literal("Wave " + b.wave + " of " + WAVES + "!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
        world.playSound(null, p.blockPosition(), SoundEvents.RAID_HORN.value(), SoundSource.PLAYERS, 0.4f, 1.6f);
    }

    private static void finish(ServerPlayer p, Bout b, boolean won, String why) {
        BOUTS.remove(b.player);
        cleanup(p.level(), b);
        int survived = won ? WAVES : b.wave - 1;
        boolean best = Leaderboards.submit(p, "arena", survived);
        if (won) {
            p.addItem(new ItemStack(com.cursedtides.item.ModItems.DOUBLOON, 25));
            Achievements.grant(p, "arena_champion");
            p.level().getServer().getPlayerList().broadcastSystemMessage(Component.literal(p.getName().getString() + " has won the sword tournament!")
                    .withStyle(ChatFormatting.GOLD), false);
        } else {
            p.sendSystemMessage(Component.literal(why + " Waves survived: " + survived + ".").withStyle(ChatFormatting.GRAY), false);
            if (survived > 0) p.addItem(new ItemStack(com.cursedtides.item.ModItems.DOUBLOON, survived * 3));
        }
        if (best && survived > 0) p.sendSystemMessage(Component.literal("A personal best!").withStyle(ChatFormatting.YELLOW), false);
        com.cursedtides.pirate.PirateData.addXp(p, com.cursedtides.pirate.Weapon.CUTLASS, 20 * survived);
    }

    private static void cleanup(ServerLevel world, Bout b) {
        for (UUID id : b.foes) {
            Entity e = world.getEntity(id);
            if (e != null) e.discard();
        }
    }
}
