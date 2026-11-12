package com.cursedtides.activity;

import com.cursedtides.entity.ModEntities;
import com.cursedtides.living.Residents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

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
        final UUID player; final Vec3d centre; int wave; final List<UUID> foes = new ArrayList<>(); int pause;
        Bout(UUID player, Vec3d centre) { this.player = player; this.centre = centre; }
    }
    private static final Map<UUID, Bout> BOUTS = new HashMap<>();

    public static boolean busy(ServerPlayerEntity p) { return BOUTS.containsKey(p.getUuid()); }

    public static List<String> start(ServerPlayerEntity p, Entity master) {
        if (busy(p)) return List.of("You're already in the pit!");
        if (!com.cursedtides.trade.Wallet.pay(p, 30)) return List.of("Three doubloons to enter. Fighters pay, the crowd watches free.");
        Bout b = new Bout(p.getUuid(), master.getPos());
        b.pause = 60;
        BOUTS.put(p.getUuid(), b);
        p.getServerWorld().playSound(null, master.getBlockPos(), SoundEvents.BLOCK_BELL_USE, SoundCategory.PLAYERS, 1.5f, 1.0f);
        return List.of("Into the ring, then! Five waves. Last one standing takes the purse.",
                "Stay inside the posts. Step out and you forfeit. Good luck, {player}.");
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(Arena::tick);
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((victim, source, amount) -> {
            if (!(victim instanceof ServerPlayerEntity p)) return true;
            Bout b = BOUTS.get(p.getUuid());
            if (b == null || amount < p.getHealth()) return true;
            p.setHealth(4f);
            finish(p, b, false, "You're carried from the pit, battered but breathing.");
            return false;
        });
    }

    private static void tick(MinecraftServer server) {
        if (BOUTS.isEmpty() || server.getTicks() % 5 != 0) return;
        for (Bout b : new ArrayList<>(BOUTS.values())) {
            ServerPlayerEntity p = server.getPlayerManager().getPlayer(b.player);
            if (p == null) { cleanup(server.getOverworld(), b); BOUTS.remove(b.player); continue; }
            ServerWorld world = p.getServerWorld();
            if (p.getPos().squaredDistanceTo(b.centre) > RING * RING) { finish(p, b, false, "You left the ring. The crowd jeers."); continue; }
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
                p.sendMessage(Text.literal("Wave " + b.wave + " down! The next comes in five seconds.").formatted(Formatting.GOLD), true);
                world.playSound(null, p.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.5f, 1.5f);
                b.pause = 100;
            }
        }
    }

    private static void spawnWave(ServerWorld world, ServerPlayerEntity p, Bout b) {
        b.wave++;
        int count = 1 + b.wave;
        for (int i = 0; i < count; i++) {
            double a = Math.PI * 2 * i / count;
            BlockPos at = Residents.openGround(world, (int) (b.centre.x + Math.cos(a) * 9), (int) (b.centre.z + Math.sin(a) * 9), 4);
            if (at == null) continue;
            MobEntity foe = (MobEntity) (b.wave >= 4 && i == 0 ? EntityType.VINDICATOR.create(world)
                    : b.wave >= 3 ? ModEntities.CURSED_PIRATE.create(world) : EntityType.ZOMBIE.create(world));
            if (foe == null) continue;
            foe.refreshPositionAndAngles(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0, 0);
            foe.initialize(world, world.getLocalDifficulty(at), SpawnReason.EVENT, null);
            foe.setCustomName(Text.literal(b.wave >= 4 && i == 0 ? "Pit Champion" : "Challenger").formatted(Formatting.RED));
            foe.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
            foe.addCommandTag(TAG);
            foe.setTarget(p);
            Residents.markTransient(foe, world.getTime() + 6000);
            world.spawnEntity(foe);
            b.foes.add(foe.getUuid());
        }
        p.sendMessage(Text.literal("Wave " + b.wave + " of " + WAVES + "!").formatted(Formatting.RED, Formatting.BOLD), true);
        world.playSound(null, p.getBlockPos(), SoundEvents.EVENT_RAID_HORN.value(), SoundCategory.PLAYERS, 0.4f, 1.6f);
    }

    private static void finish(ServerPlayerEntity p, Bout b, boolean won, String why) {
        BOUTS.remove(b.player);
        cleanup(p.getServerWorld(), b);
        int survived = won ? WAVES : b.wave - 1;
        boolean best = Leaderboards.submit(p, "arena", survived);
        if (won) {
            p.giveItemStack(new ItemStack(com.cursedtides.item.ModItems.DOUBLOON, 25));
            Achievements.grant(p, "arena_champion");
            p.getServer().getPlayerManager().broadcast(Text.literal(p.getName().getString() + " has won the sword tournament!")
                    .formatted(Formatting.GOLD), false);
        } else {
            p.sendMessage(Text.literal(why + " Waves survived: " + survived + ".").formatted(Formatting.GRAY), false);
            if (survived > 0) p.giveItemStack(new ItemStack(com.cursedtides.item.ModItems.DOUBLOON, survived * 3));
        }
        if (best && survived > 0) p.sendMessage(Text.literal("A personal best!").formatted(Formatting.YELLOW), false);
        com.cursedtides.pirate.PirateData.addXp(p, com.cursedtides.pirate.Weapon.CUTLASS, 20 * survived);
    }

    private static void cleanup(ServerWorld world, Bout b) {
        for (UUID id : b.foes) {
            Entity e = world.getEntity(id);
            if (e != null) e.discard();
        }
    }
}
