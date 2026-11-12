package com.cursedtides.activity;

import com.cursedtides.living.Residents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Sixty seconds, a pistol and a field of straw dummies that pop up and vanish. Farther and quicker scores more. */
public final class ShootingRange {
    private ShootingRange() {}

    public static final String TAG = "cursedtides_target";
    private static final int LENGTH = 20 * 60;

    private static final class Round {
        final UUID player; final Vec3d spot; int ticks; int score; int hits; UUID target; int targetAge;
        Round(UUID player, Vec3d spot) { this.player = player; this.spot = spot; }
    }
    private static final Map<UUID, Round> ROUNDS = new HashMap<>();

    public static List<String> start(ServerPlayerEntity p, Entity master) {
        if (ROUNDS.containsKey(p.getUuid())) return List.of("You're mid-round. Shoot!");
        if (p.getInventory().count(com.cursedtides.item.ModItems.PISTOL_SHOT) < 10) {
            p.giveItemStack(new ItemStack(com.cursedtides.item.ModItems.PISTOL_SHOT, 16));
        }
        ROUNDS.put(p.getUuid(), new Round(p.getUuid(), p.getPos()));
        return List.of("Sixty seconds on the clock. Targets pop up downrange; the farther they stand and the faster you drop them, the more they're worth.",
                "Any firearm will do. I've put some shot in your pouch. Go!");
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(ShootingRange::tick);
    }

    /** Called by firearms with everything a shot struck. */
    public static void onShot(ServerPlayerEntity p, Collection<Entity> struck) {
        Round r = ROUNDS.get(p.getUuid());
        if (r == null || r.target == null) return;
        for (Entity e : struck) {
            if (!e.getUuid().equals(r.target)) continue;
            double dist = p.distanceTo(e);
            int points = (int) Math.round(dist / 2.0) + Math.max(0, 6 - r.targetAge / 10);
            r.score += points;
            r.hits++;
            Challenges.progress(p, "range", 1);
            ServerWorld w = p.getServerWorld();
            w.spawnParticles(ParticleTypes.CRIT, e.getX(), e.getY() + 1.4, e.getZ(), 20, 0.2, 0.3, 0.2, 0.3);
            w.playSound(null, p.getBlockPos(), SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), SoundCategory.PLAYERS, 1f, 1.6f);
            p.sendMessage(Text.literal("+" + points + "  (" + r.score + ")").formatted(Formatting.GOLD), true);
            e.discard();
            r.target = null;
            return;
        }
    }

    private static void tick(MinecraftServer server) {
        if (ROUNDS.isEmpty()) return;
        for (Round r : List.copyOf(ROUNDS.values())) {
            ServerPlayerEntity p = server.getPlayerManager().getPlayer(r.player);
            ServerWorld w = p == null ? server.getOverworld() : p.getServerWorld();
            r.ticks++;
            r.targetAge++;
            if (p == null || r.ticks >= LENGTH || p.getPos().squaredDistanceTo(r.spot) > 16 * 16) {
                removeTarget(w, r);
                ROUNDS.remove(r.player);
                if (p != null) end(p, r);
                continue;
            }
            if (r.target == null || r.targetAge > 80) {
                removeTarget(w, r);
                spawnTarget(w, p, r);
            }
            if (r.ticks % 200 == 0) p.sendMessage(Text.literal((LENGTH - r.ticks) / 20 + "s left, " + r.score + " points").formatted(Formatting.GRAY), true);
        }
    }

    private static void spawnTarget(ServerWorld w, ServerPlayerEntity p, Round r) {
        Vec3d look = p.getRotationVec(1f).multiply(1, 0, 1).normalize();
        double dist = 10 + w.random.nextInt(18);
        double side = (w.random.nextDouble() - 0.5) * 16;
        Vec3d aim = r.spot.add(look.multiply(dist)).add(look.z * side, 0, -look.x * side);
        BlockPos at = Residents.openGround(w, (int) aim.x, (int) aim.z, 4);
        if (at == null) return;
        ArmorStandEntity stand = EntityType.ARMOR_STAND.create(w);
        if (stand == null) return;
        stand.refreshPositionAndAngles(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, p.getYaw() + 180, 0);
        stand.equipStack(net.minecraft.entity.EquipmentSlot.HEAD, new ItemStack(Items.HAY_BLOCK));
        stand.setInvulnerable(true);
        stand.addCommandTag(TAG);
        Residents.markTransient(stand, w.getTime() + 400);
        w.spawnEntity(stand);
        w.spawnParticles(ParticleTypes.POOF, stand.getX(), stand.getY() + 1, stand.getZ(), 8, 0.2, 0.5, 0.2, 0.01);
        r.target = stand.getUuid();
        r.targetAge = 0;
    }

    private static void removeTarget(ServerWorld w, Round r) {
        if (r.target == null) return;
        Entity e = w.getEntity(r.target);
        if (e != null) e.discard();
        r.target = null;
    }

    private static void end(ServerPlayerEntity p, Round r) {
        p.sendMessage(Text.literal("Cease fire! " + r.hits + " hits, " + r.score + " points.").formatted(Formatting.GOLD, Formatting.BOLD), false);
        if (Leaderboards.submit(p, "range", r.score) && r.score > 0) p.sendMessage(Text.literal("A personal best!").formatted(Formatting.YELLOW), false);
        if (r.score >= 100) Achievements.grant(p, "sharpshooter");
        if (r.score > 0) p.giveItemStack(new ItemStack(com.cursedtides.item.ModItems.DOUBLOON, Math.min(20, r.score / 10)));
        com.cursedtides.pirate.PirateData.addXp(p, com.cursedtides.pirate.Weapon.PISTOL, r.score);
    }
}
