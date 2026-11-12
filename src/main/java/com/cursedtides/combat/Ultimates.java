package com.cursedtides.combat;

import com.cursedtides.CursedTides;
import com.cursedtides.entity.CrewmateEntity;
import com.cursedtides.entity.ShipEntity;
import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import com.cursedtides.profession.Profession;
import com.cursedtides.profession.ProfessionData;
import com.cursedtides.social.CrewState;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * Ultimate abilities (key U): one signature move, chosen by your best proficiency once it reaches 20,
 * on a five-minute cooldown. Also home to Leadership, the officer's trade.
 */
public final class Ultimates {
    private Ultimates() {}

    public static final int UNLOCK_LEVEL = 20;
    public static final long COOLDOWN = 20 * 60 * 5;

    public static final AttachmentType<Long> READY_AT = AttachmentRegistry.<Long>builder()
            .persistent(Codec.LONG).copyOnDeath().initializer(() -> 0L).buildAndRegister(CursedTides.id("ultimate_ready"));

    private static final class Job {
        final ServerPlayerEntity player; final int every; int left; final IntConsumer step; int t;
        Job(ServerPlayerEntity p, int every, int times, IntConsumer step) { this.player = p; this.every = every; this.left = times; this.step = step; }
    }
    private static final List<Job> JOBS = new ArrayList<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(Ultimates::tick);
        ServerLivingEntityEvents.AFTER_DEATH.register((victim, source) -> {
            if (!(victim instanceof Monster) || !(victim.getWorld() instanceof ServerWorld world)) return;
            Entity killer = source.getAttacker();
            if (killer == null) return;
            // An officer learns from every foe their people put down nearby
            for (ServerPlayerEntity officer : world.getPlayers(p -> p.squaredDistanceTo(victim) < 24 * 24)) {
                if (killer == officer) continue;
                boolean ours = killer instanceof CrewmateEntity
                        || (killer instanceof PlayerEntity kp && CrewState.shipmates(world.getServer(), officer.getUuid(), kp.getUuid()));
                if (ours) ProfessionData.addXp(officer, Profession.LEADERSHIP, 4);
            }
        });
    }

    public static String nameFor(Weapon w) {
        return switch (w) {
            case CUTLASS -> "Blade Storm";
            case PISTOL -> "Dead Eye Volley";
            case CANNON -> "Barrage";
            case HEX -> "Cursed Tide";
            case SAILING -> "Full Canvas";
            case EXPLORATION -> "Treasure Sense";
        };
    }

    /** The proficiency whose ultimate you'd use, or null if none has reached the mark. */
    public static Weapon best(PlayerEntity p) {
        Weapon best = null;
        int lvl = UNLOCK_LEVEL - 1;
        for (Weapon w : Weapon.values()) {
            int l = PirateData.level(p, w);
            if (l > lvl) { lvl = l; best = w; }
        }
        return best;
    }

    public static void use(ServerPlayerEntity p) {
        Weapon w = best(p);
        if (w == null) {
            p.sendMessage(Text.literal("Your ultimate unlocks when any proficiency reaches " + UNLOCK_LEVEL + ".").formatted(Formatting.GRAY), true);
            return;
        }
        long now = p.getServerWorld().getTime();
        long ready = p.getAttachedOrElse(READY_AT, 0L);
        if (ready > now) {
            p.sendMessage(Text.literal(nameFor(w) + " ready in " + ((ready - now) / 20) + "s").formatted(Formatting.GRAY), true);
            return;
        }
        p.setAttached(READY_AT, now + COOLDOWN);
        com.cursedtides.activity.Achievements.grant(p, "ultimate");
        ServerWorld world = p.getServerWorld();
        p.sendMessage(Text.literal("⚔ " + nameFor(w) + "!").formatted(Formatting.GOLD, Formatting.BOLD), true);
        world.playSound(null, p.getBlockPos(), com.cursedtides.sound.ModSounds.ULTIMATE.value(), SoundCategory.PLAYERS, 1f, 1f);
        switch (w) {
            case CUTLASS -> {
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 120, 1));
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 120, 1));
                JOBS.add(new Job(p, 10, 12, i -> {
                    for (LivingEntity t : foes(p, 4.5)) {
                        t.timeUntilRegen = 0;
                        t.damage(world.getDamageSources().playerAttack(p), 4f);
                        Styles.stagger(t, 15);
                    }
                    for (int k = 0; k < 10; k++) {
                        double a = Math.PI * 2 * k / 10 + i;
                        world.spawnParticles(ParticleTypes.SWEEP_ATTACK, p.getX() + Math.cos(a) * 2.5, p.getY() + 1, p.getZ() + Math.sin(a) * 2.5, 1, 0, 0, 0, 0);
                    }
                    world.playSound(null, p.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 0.8f, 0.8f + i * 0.05f);
                }));
            }
            case PISTOL -> {
                List<LivingEntity> targets = foes(p, 28);
                targets.sort(Comparator.comparingDouble(p::squaredDistanceTo));
                List<LivingEntity> marked = targets.subList(0, Math.min(6, targets.size()));
                JOBS.add(new Job(p, 5, marked.size(), i -> {
                    LivingEntity t = marked.get(i);
                    if (!t.isAlive()) return;
                    Vec3d from = p.getEyePos(), to = t.getPos().add(0, t.getHeight() * 0.6, 0);
                    Vec3d step = to.subtract(from).normalize();
                    for (double d = 0; d < from.distanceTo(to); d += 0.8) {
                        Vec3d q = from.add(step.multiply(d));
                        world.spawnParticles(ParticleTypes.SMOKE, q.x, q.y, q.z, 1, 0, 0, 0, 0);
                    }
                    t.timeUntilRegen = 0;
                    t.damage(world.getDamageSources().playerAttack(p), 14f);
                    world.playSound(null, p.getBlockPos(), SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, SoundCategory.PLAYERS, 1f, 0.6f);
                }));
            }
            case CANNON -> {
                HitResult hit = p.raycast(64, 0, false);
                Vec3d centre = hit.getPos();
                JOBS.add(new Job(p, 6, 10, i -> {
                    Vec3d at = centre.add((p.getRandom().nextDouble() - 0.5) * 10, 0, (p.getRandom().nextDouble() - 0.5) * 10);
                    world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, at.x, at.y + 0.5, at.z, 1, 0, 0, 0, 0);
                    world.playSound(null, at.x, at.y, at.z, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, 1.2f, 0.9f);
                    for (Entity e : world.getOtherEntities(p, new Box(at, at).expand(3), e -> e instanceof LivingEntity && e.isAlive() && isFoe(p, e))) {
                        e.timeUntilRegen = 0;
                        e.damage(world.getDamageSources().playerAttack(p), 10f);
                    }
                }));
            }
            case HEX -> {
                for (LivingEntity t : foes(p, 10)) {
                    t.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 140, 1), p);
                    t.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 30, 0), p);
                    Styles.stagger(t, 80);
                }
                p.heal(8);
                world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, p.getX(), p.getY() + 0.5, p.getZ(), 120, 5, 0.5, 5, 0.02);
                world.playSound(null, p.getBlockPos(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 0.6f, 1.3f);
            }
            case SAILING -> {
                if (p.getVehicle() instanceof ShipEntity ship) {
                    JOBS.add(new Job(p, 5, 40, i -> {
                        Vec3d fwd = Vec3d.fromPolar(0, ship.getYaw());
                        ship.addVelocity(fwd.x * 0.08, 0, fwd.z * 0.08);
                        world.spawnParticles(ParticleTypes.CLOUD, ship.getX(), ship.getY() + 6, ship.getZ(), 4, 2, 1, 2, 0.05);
                    }));
                    p.sendMessage(Text.literal("Every stitch of canvas aloft!").formatted(Formatting.AQUA), false);
                } else {
                    p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 400, 2));
                    p.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, 400, 0));
                    p.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 600, 0));
                }
            }
            case EXPLORATION -> {
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 900, 0));
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, 1200, 2));
                for (LivingEntity t : foes(p, 48)) t.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 600, 0));
                p.sendMessage(Text.literal("Your eye for trouble sharpens: every foe nearby is marked, and luck sits on your shoulder.")
                        .formatted(Formatting.GREEN), false);
            }
        }
    }

    private static boolean isFoe(ServerPlayerEntity p, Entity e) {
        if (e == p || e instanceof CrewmateEntity) return false;
        if (e instanceof PlayerEntity other) return com.cursedtides.social.Duels.inDuel(p.getUuid()) && com.cursedtides.social.Duels.inDuel(other.getUuid());
        return e instanceof Monster || (e instanceof net.minecraft.entity.mob.MobEntity m && m.getTarget() == p);
    }

    private static List<LivingEntity> foes(ServerPlayerEntity p, double r) {
        List<LivingEntity> out = new ArrayList<>();
        for (Entity e : p.getWorld().getOtherEntities(p, p.getBoundingBox().expand(r), e -> e instanceof LivingEntity && e.isAlive() && isFoe(p, e))) {
            out.add((LivingEntity) e);
        }
        return out;
    }

    private static void tick(MinecraftServer server) {
        Iterator<Job> it = JOBS.iterator();
        while (it.hasNext()) {
            Job j = it.next();
            if (j.player.isRemoved() || j.player.isDead()) { it.remove(); continue; }
            if (j.t++ % j.every != 0) continue;
            int i = (j.t - 1) / j.every;
            j.step.accept(i);
            if (--j.left <= 0) it.remove();
        }
        if (server.getTicks() % 200 == 0) leadershipAura(server);
    }

    /** Officers steady the people around them: crewmates and shipmates within 16 blocks. */
    private static void leadershipAura(MinecraftServer server) {
        for (ServerPlayerEntity officer : server.getPlayerManager().getPlayerList()) {
            int lvl = ProfessionData.level(officer, Profession.LEADERSHIP);
            if (lvl < 5) continue;
            Box area = officer.getBoundingBox().expand(16);
            List<LivingEntity> people = new ArrayList<>(officer.getWorld().getEntitiesByClass(CrewmateEntity.class, area, Entity::isAlive));
            for (ServerPlayerEntity mate : officer.getServerWorld().getPlayers(m -> m != officer && m.squaredDistanceTo(officer) < 256
                    && CrewState.shipmates(server, officer.getUuid(), m.getUuid()))) people.add(mate);
            for (LivingEntity l : people) {
                l.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 240, 0, true, false));
                if (lvl >= 15) l.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 240, 0, true, false));
                if (lvl >= 25) l.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 0, true, false));
            }
            if (!people.isEmpty() && server.getTicks() % 1200 == 0) ProfessionData.addXp(officer, Profession.LEADERSHIP, 2 + people.size());
        }
    }
}
