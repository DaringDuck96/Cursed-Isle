package com.cursedtides.explore;

import com.cursedtides.entity.ShipEntity;
import com.cursedtides.world.ModWorld;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * What the sea does to ships: whirlpools that drag and spin, currents that carry you (or fight you),
 * fog banks that swallow the horizon, and rocks that tear the bottom out of a ship sailed too fast into shallows.
 */
public final class SeaHazards {
    private SeaHazards() {}

    public record Whirlpool(String name, int x, int z, int radius) {}
    /** A current: a band from (x1,z1) to (x2,z2), so wide, pushing that way. */
    public record Current(String name, int x1, int z1, int x2, int z2, int width, double strength) {}
    /** A fog bank: thick fog within the radius (the client draws it; see FogZones). */
    public record FogBank(String name, int x, int z, int radius) {}

    public static final List<Whirlpool> WHIRLPOOLS = List.of(
            new Whirlpool("the Drain", 330, 180, 26),
            new Whirlpool("Widow's Gullet", -180, -420, 24),
            new Whirlpool("the Churn", 620, 520, 30),
            new Whirlpool("Old Mother's Eye", -560, 360, 28),
            new Whirlpool("the Throat", -330, 980, 32));

    public static final List<Current> CURRENTS = List.of(
            new Current("the Trade Wind Run", -200, -60, 520, -60, 40, 0.035),
            new Current("the Southern Drift", 100, 300, 100, 900, 36, 0.03),
            new Current("the Cold Reach", 700, -400, 300, -700, 34, 0.03),
            new Current("the Bloodwake Rip", -500, -400, -100, -760, 30, 0.04));

    public static final List<FogBank> FOG = List.of(
            new FogBank("the Murk", -380, -360, 140),
            new FogBank("the Grey Veil", 900, -400, 120),
            new FogBank("the Shroud", -470, 860, 150),
            new FogBank("Lanternless Water", 0, 1100, 160),
            new FogBank("the Nowhere Mist", -940, -720, 130),
            new FogBank("the Crown's Veil", 1140, 320, 110));

    private static final Map<UUID, Vec3d> LAST_VEL = new HashMap<>();
    private static final Map<UUID, String> LAST_ZONE = new HashMap<>();
    private static final Map<UUID, Long> RAM_READY = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(SeaHazards::tick);
    }

    private static void tick(MinecraftServer server) {
        if (!ModWorld.isArchipelago()) return;
        ServerWorld world = server.getOverworld();
        int t = server.getTicks();
        for (ServerPlayerEntity p : world.getPlayers()) {
            if (!(p.getVehicle() instanceof ShipEntity ship) || ship.getControllingPassenger() != p) {
                if (t % 40 == 0) announce(p);
                continue;
            }
            Vec3d pos = ship.getPos();
            var fig = ship.getFigurehead();
            if (fig == com.cursedtides.ship.FigureheadItem.Kind.EAGLE && t % 200 == 0) {
                for (ShipEntity other : world.getEntitiesByClass(ShipEntity.class, ship.getBoundingBox().expand(150), o -> o != ship && o.isHostile())) {
                    other.addStatusEffectGlow();
                }
            }
            boolean kraken = fig == com.cursedtides.ship.FigureheadItem.Kind.KRAKEN;
            for (Whirlpool w : WHIRLPOOLS) {
                if (kraken) break;
                double dx = w.x() - pos.x, dz = w.z() - pos.z;
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > w.radius() * 1.6) continue;
                double pull = 0.02 * (1 - d / (w.radius() * 1.6)) + 0.004;
                Vec3d in = new Vec3d(dx, 0, dz).normalize();
                Vec3d spin = new Vec3d(-in.z, 0, in.x);
                ship.addVelocity(in.x * pull + spin.x * pull * 1.4, 0, in.z * pull + spin.z * pull * 1.4);
                ship.setYaw(ship.getYaw() + 0.8f);
                if (d < 5 && t % 20 == 0) ship.seaDamage(world, 6f, "The whirlpool's heart grinds at the hull!");
            }
            for (Current c : CURRENTS) {
                if (kraken) break;
                Vec3d a = new Vec3d(c.x1(), 0, c.z1()), b = new Vec3d(c.x2(), 0, c.z2());
                Vec3d ab = b.subtract(a);
                double len = ab.length();
                Vec3d dir = ab.multiply(1 / len);
                Vec3d ap = new Vec3d(pos.x, 0, pos.z).subtract(a);
                double along = ap.dotProduct(dir);
                if (along < 0 || along > len) continue;
                double off = ap.subtract(dir.multiply(along)).length();
                if (off > c.width()) continue;
                ship.addVelocity(dir.x * c.strength(), 0, dir.z * c.strength());
            }
            // Running aground: a sudden stop against land at speed
            Vec3d before = LAST_VEL.getOrDefault(ship.getUuid(), Vec3d.ZERO);
            double speed = before.horizontalLength();
            // Ramming: drive your bow into another ship at speed
            if (speed > 0.3 && RAM_READY.getOrDefault(ship.getUuid(), 0L) < world.getTime()) {
                Vec3d fwd = Vec3d.fromPolar(0, ship.getYaw());
                net.minecraft.util.math.Box bow = ship.getBoundingBox().offset(fwd.multiply(2 * ship.shipClass().scale())).expand(1.5, 1, 1.5);
                for (ShipEntity other : world.getEntitiesByClass(ShipEntity.class, bow, o -> o != ship && o.isAlive())) {
                    float hit = (float) (speed * 60 * ship.shipClass().scale());
                    other.damage(world.getDamageSources().playerAttack(p), hit);
                    ship.seaDamage(world, hit * 0.25f, "Ramming speed! You stove in her side.");
                    other.setVelocity(other.getVelocity().add(fwd.multiply(speed * 0.8)));
                    ship.setVelocity(ship.getVelocity().multiply(0.2));
                    world.playSound(null, other.getBlockPos(), net.minecraft.sound.SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, SoundCategory.NEUTRAL, 2f, 0.4f);
                    world.spawnParticles(ParticleTypes.EXPLOSION, other.getX(), other.getY() + 1, other.getZ(), 2, 1, 0.5, 1, 0);
                    com.cursedtides.pirate.PirateData.addXp(p, com.cursedtides.pirate.Weapon.SAILING, 20);
                    RAM_READY.put(ship.getUuid(), world.getTime() + 60);
                    break;
                }
            }
            if (ship.horizontalCollision && speed > 0.35 && t % 5 == 0) {
                ship.seaDamage(world, (float) (speed * 30), "You've run aground! Timbers crack below the waterline.");
                ship.setVelocity(before.multiply(-0.3));
            }
            LAST_VEL.put(ship.getUuid(), ship.getVelocity());
            if (t % 40 == 0) announce(p);
        }
        if (t % 10 == 0) {
            for (Whirlpool w : WHIRLPOOLS) {
                if (world.getClosestPlayer(w.x(), 62, w.z(), 96, false) == null) continue;
                for (int i = 0; i < 6; i++) {
                    double a = (t / 10.0 + i) * 0.9, r = 3 + (i * 4) % w.radius();
                    world.spawnParticles(ParticleTypes.SPLASH, w.x() + Math.cos(a) * r, 63, w.z() + Math.sin(a) * r, 6, 0.6, 0, 0.6, 0.1);
                    world.spawnParticles(ParticleTypes.BUBBLE_COLUMN_UP, w.x() + Math.cos(a) * r * 0.5, 62, w.z() + Math.sin(a) * r * 0.5, 3, 0.3, 0.2, 0.3, 0.05);
                }
                if (t % 60 == 0) world.playSound(null, w.x(), 62, w.z(), com.cursedtides.sound.ModSounds.WHIRLPOOL.value(), SoundCategory.AMBIENT, 4f, 1f);
            }
        }
    }

    private static void announce(ServerPlayerEntity p) {
        String zone = null;
        for (Whirlpool w : WHIRLPOOLS) if (near(p, w.x(), w.z(), w.radius() * 2)) zone = "whirlpool:" + w.name();
        for (FogBank f : FOG) if (zone == null && near(p, f.x(), f.z(), f.radius())) zone = "fog:" + f.name();
        String last = LAST_ZONE.get(p.getUuid());
        if (zone != null && !zone.equals(last)) {
            String name = zone.substring(zone.indexOf(':') + 1);
            p.sendMessage(Text.literal(zone.startsWith("fog") ? "Fog closes in: " + name + "." : "The sea is turning! " + capital(name) + " is close.")
                    .formatted(zone.startsWith("fog") ? Formatting.GRAY : Formatting.DARK_AQUA, Formatting.ITALIC), true);
        }
        if (zone == null) LAST_ZONE.remove(p.getUuid());
        else LAST_ZONE.put(p.getUuid(), zone);
    }

    private static String capital(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static boolean near(Entity e, int x, int z, int r) {
        double dx = e.getX() - x, dz = e.getZ() - z;
        return dx * dx + dz * dz < (double) r * r;
    }

    /** How thick the fog is here, 0 (none) to 1 (a wall of white). Shared with the client renderer. */
    public static float fogDensity(double x, double z) {
        float best = 0;
        for (FogBank f : FOG) {
            double d = Math.sqrt((x - f.x()) * (x - f.x()) + (z - f.z()) * (z - f.z())) / f.radius();
            if (d < 1) best = Math.max(best, (float) Math.min(1, (1 - d) * 2.5));
        }
        return best;
    }

    @SuppressWarnings("unused")
    private static Box box(int x, int z, int r) {
        return new Box(x - r, 0, z - r, x + r, 256, z + r);
    }
}
