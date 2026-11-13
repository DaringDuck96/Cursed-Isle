package com.cursedtides.activity;

import com.cursedtides.living.Residents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
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
        final UUID player; final Vec3 spot; int ticks; int score; int hits; UUID target; int targetAge;
        Round(UUID player, Vec3 spot) { this.player = player; this.spot = spot; }
    }
    private static final Map<UUID, Round> ROUNDS = new HashMap<>();

    public static List<String> start(ServerPlayer p, Entity master) {
        if (ROUNDS.containsKey(p.getUUID())) return List.of("You're mid-round. Shoot!");
        if (p.getInventory().countItem(com.cursedtides.item.ModItems.PISTOL_SHOT) < 10) {
            p.addItem(new ItemStack(com.cursedtides.item.ModItems.PISTOL_SHOT, 16));
        }
        ROUNDS.put(p.getUUID(), new Round(p.getUUID(), p.position()));
        return List.of("Sixty seconds on the clock. Targets pop up downrange; the farther they stand and the faster you drop them, the more they're worth.",
                "Any firearm will do. I've put some shot in your pouch. Go!");
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(ShootingRange::tick);
    }

    /** Called by firearms with everything a shot struck. */
    public static void onShot(ServerPlayer p, Collection<Entity> struck) {
        Round r = ROUNDS.get(p.getUUID());
        if (r == null || r.target == null) return;
        for (Entity e : struck) {
            if (!e.getUUID().equals(r.target)) continue;
            double dist = p.distanceTo(e);
            int points = (int) Math.round(dist / 2.0) + Math.max(0, 6 - r.targetAge / 10);
            r.score += points;
            r.hits++;
            Challenges.progress(p, "range", 1);
            ServerLevel w = p.serverLevel();
            w.sendParticles(ParticleTypes.CRIT, e.getX(), e.getY() + 1.4, e.getZ(), 20, 0.2, 0.3, 0.2, 0.3);
            w.playSound(null, p.blockPosition(), SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.PLAYERS, 1f, 1.6f);
            p.displayClientMessage(Component.literal("+" + points + "  (" + r.score + ")").withStyle(ChatFormatting.GOLD), true);
            e.discard();
            r.target = null;
            return;
        }
    }

    private static void tick(MinecraftServer server) {
        if (ROUNDS.isEmpty()) return;
        for (Round r : List.copyOf(ROUNDS.values())) {
            ServerPlayer p = server.getPlayerList().getPlayer(r.player);
            ServerLevel w = p == null ? server.overworld() : p.serverLevel();
            r.ticks++;
            r.targetAge++;
            if (p == null || r.ticks >= LENGTH || p.position().distanceToSqr(r.spot) > 16 * 16) {
                removeTarget(w, r);
                ROUNDS.remove(r.player);
                if (p != null) end(p, r);
                continue;
            }
            if (r.target == null || r.targetAge > 80) {
                removeTarget(w, r);
                spawnTarget(w, p, r);
            }
            if (r.ticks % 200 == 0) p.displayClientMessage(Component.literal((LENGTH - r.ticks) / 20 + "s left, " + r.score + " points").withStyle(ChatFormatting.GRAY), true);
        }
    }

    private static void spawnTarget(ServerLevel w, ServerPlayer p, Round r) {
        Vec3 look = p.getViewVector(1f).multiply(1, 0, 1).normalize();
        double dist = 10 + w.random.nextInt(18);
        double side = (w.random.nextDouble() - 0.5) * 16;
        Vec3 aim = r.spot.add(look.scale(dist)).add(look.z * side, 0, -look.x * side);
        BlockPos at = Residents.openGround(w, (int) aim.x, (int) aim.z, 4);
        if (at == null) return;
        ArmorStand stand = EntityType.ARMOR_STAND.create(w);
        if (stand == null) return;
        stand.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, p.getYRot() + 180, 0);
        stand.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, new ItemStack(Items.HAY_BLOCK));
        stand.setInvulnerable(true);
        stand.addTag(TAG);
        Residents.markTransient(stand, w.getGameTime() + 400);
        w.addFreshEntity(stand);
        w.sendParticles(ParticleTypes.POOF, stand.getX(), stand.getY() + 1, stand.getZ(), 8, 0.2, 0.5, 0.2, 0.01);
        r.target = stand.getUUID();
        r.targetAge = 0;
    }

    private static void removeTarget(ServerLevel w, Round r) {
        if (r.target == null) return;
        Entity e = w.getEntity(r.target);
        if (e != null) e.discard();
        r.target = null;
    }

    private static void end(ServerPlayer p, Round r) {
        p.displayClientMessage(Component.literal("Cease fire! " + r.hits + " hits, " + r.score + " points.").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        if (Leaderboards.submit(p, "range", r.score) && r.score > 0) p.displayClientMessage(Component.literal("A personal best!").withStyle(ChatFormatting.YELLOW), false);
        if (r.score >= 100) Achievements.grant(p, "sharpshooter");
        if (r.score > 0) p.addItem(new ItemStack(com.cursedtides.item.ModItems.DOUBLOON, Math.min(20, r.score / 10)));
        com.cursedtides.pirate.PirateData.addXp(p, com.cursedtides.pirate.Weapon.PISTOL, r.score);
    }
}
