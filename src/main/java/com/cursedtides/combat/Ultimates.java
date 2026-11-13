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
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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
        final ServerPlayer player; final int every; int left; final IntConsumer step; int t;
        Job(ServerPlayer p, int every, int times, IntConsumer step) { this.player = p; this.every = every; this.left = times; this.step = step; }
    }
    private static final List<Job> JOBS = new ArrayList<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(Ultimates::tick);
        ServerLivingEntityEvents.AFTER_DEATH.register((victim, source) -> {
            if (!(victim instanceof Enemy) || !(victim.level() instanceof ServerLevel world)) return;
            Entity killer = source.getEntity();
            if (killer == null) return;
            // An officer learns from every foe their people put down nearby
            for (ServerPlayer officer : world.getPlayers(p -> p.distanceToSqr(victim) < 24 * 24)) {
                if (killer == officer) continue;
                boolean ours = killer instanceof CrewmateEntity
                        || (killer instanceof Player kp && CrewState.shipmates(world.getServer(), officer.getUUID(), kp.getUUID()));
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
    public static Weapon best(Player p) {
        Weapon best = null;
        int lvl = UNLOCK_LEVEL - 1;
        for (Weapon w : Weapon.values()) {
            int l = PirateData.level(p, w);
            if (l > lvl) { lvl = l; best = w; }
        }
        return best;
    }

    public static void use(ServerPlayer p) {
        Weapon w = best(p);
        if (w == null) {
            p.sendSystemMessage(Component.literal("Your ultimate unlocks when any proficiency reaches " + UNLOCK_LEVEL + ".").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        long now = p.level().getGameTime();
        long ready = p.getAttachedOrElse(READY_AT, 0L);
        if (ready > now) {
            p.sendSystemMessage(Component.literal(nameFor(w) + " ready in " + ((ready - now) / 20) + "s").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        p.setAttached(READY_AT, now + COOLDOWN);
        com.cursedtides.activity.Achievements.grant(p, "ultimate");
        ServerLevel world = p.level();
        p.sendSystemMessage(Component.literal("⚔ " + nameFor(w) + "!").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), true);
        world.playSound(null, p.blockPosition(), com.cursedtides.sound.ModSounds.ULTIMATE.value(), SoundSource.PLAYERS, 1f, 1f);
        switch (w) {
            case CUTLASS -> {
                p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 120, 1));
                p.addEffect(new MobEffectInstance(MobEffects.SPEED, 120, 1));
                JOBS.add(new Job(p, 10, 12, i -> {
                    for (LivingEntity t : foes(p, 4.5)) {
                        t.setInvulnerableTime(0);
                        t.hurt(world.damageSources().playerAttack(p), 4f);
                        Styles.stagger(t, 15);
                    }
                    for (int k = 0; k < 10; k++) {
                        double a = Math.PI * 2 * k / 10 + i;
                        world.sendParticles(ParticleTypes.SWEEP_ATTACK, p.getX() + Math.cos(a) * 2.5, p.getY() + 1, p.getZ() + Math.sin(a) * 2.5, 1, 0, 0, 0, 0);
                    }
                    world.playSound(null, p.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8f, 0.8f + i * 0.05f);
                }));
            }
            case PISTOL -> {
                List<LivingEntity> targets = foes(p, 28);
                targets.sort(Comparator.comparingDouble(p::distanceToSqr));
                List<LivingEntity> marked = targets.subList(0, Math.min(6, targets.size()));
                JOBS.add(new Job(p, 5, marked.size(), i -> {
                    LivingEntity t = marked.get(i);
                    if (!t.isAlive()) return;
                    Vec3 from = p.getEyePosition(), to = t.position().add(0, t.getBbHeight() * 0.6, 0);
                    Vec3 step = to.subtract(from).normalize();
                    for (double d = 0; d < from.distanceTo(to); d += 0.8) {
                        Vec3 q = from.add(step.scale(d));
                        world.sendParticles(ParticleTypes.SMOKE, q.x, q.y, q.z, 1, 0, 0, 0, 0);
                    }
                    t.setInvulnerableTime(0);
                    t.hurt(world.damageSources().playerAttack(p), 14f);
                    world.playSound(null, p.blockPosition(), SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, SoundSource.PLAYERS, 1f, 0.6f);
                }));
            }
            case CANNON -> {
                HitResult hit = p.pick(64, 0, false);
                Vec3 centre = hit.getLocation();
                JOBS.add(new Job(p, 6, 10, i -> {
                    Vec3 at = centre.add((p.getRandom().nextDouble() - 0.5) * 10, 0, (p.getRandom().nextDouble() - 0.5) * 10);
                    world.sendParticles(ParticleTypes.EXPLOSION_EMITTER, at.x, at.y + 0.5, at.z, 1, 0, 0, 0, 0);
                    world.playSound(null, at.x, at.y, at.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.2f, 0.9f);
                    for (Entity e : world.getEntities(p, new AABB(at, at).inflate(3), e -> e instanceof LivingEntity && e.isAlive() && isFoe(p, e))) {
                        e.setInvulnerableTime(0);
                        e.hurt(world.damageSources().playerAttack(p), 10f);
                    }
                }));
            }
            case HEX -> {
                for (LivingEntity t : foes(p, 10)) {
                    t.addEffect(new MobEffectInstance(MobEffects.WITHER, 140, 1), p);
                    t.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 30, 0), p);
                    Styles.stagger(t, 80);
                }
                p.heal(8);
                world.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.getX(), p.getY() + 0.5, p.getZ(), 120, 5, 0.5, 5, 0.02);
                world.playSound(null, p.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.6f, 1.3f);
            }
            case SAILING -> {
                if (p.getVehicle() instanceof ShipEntity ship) {
                    JOBS.add(new Job(p, 5, 40, i -> {
                        Vec3 fwd = Vec3.directionFromRotation(0, ship.getYRot());
                        ship.push(fwd.x * 0.08, 0, fwd.z * 0.08);
                        world.sendParticles(ParticleTypes.CLOUD, ship.getX(), ship.getY() + 6, ship.getZ(), 4, 2, 1, 2, 0.05);
                    }));
                    p.sendSystemMessage(Component.literal("Every stitch of canvas aloft!").withStyle(ChatFormatting.AQUA), false);
                } else {
                    p.addEffect(new MobEffectInstance(MobEffects.SPEED, 400, 2));
                    p.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 400, 0));
                    p.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 600, 0));
                }
            }
            case EXPLORATION -> {
                p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 900, 0));
                p.addEffect(new MobEffectInstance(MobEffects.LUCK, 1200, 2));
                for (LivingEntity t : foes(p, 48)) t.addEffect(new MobEffectInstance(MobEffects.GLOWING, 600, 0));
                p.sendSystemMessage(Component.literal("Your eye for trouble sharpens: every foe nearby is marked, and luck sits on your shoulder.")
                        .withStyle(ChatFormatting.GREEN), false);
            }
        }
    }

    private static boolean isFoe(ServerPlayer p, Entity e) {
        if (e == p || e instanceof CrewmateEntity) return false;
        if (e instanceof Player other) return com.cursedtides.social.Duels.inDuel(p.getUUID()) && com.cursedtides.social.Duels.inDuel(other.getUUID());
        return e instanceof Enemy || (e instanceof net.minecraft.world.entity.Mob m && m.getTarget() == p);
    }

    private static List<LivingEntity> foes(ServerPlayer p, double r) {
        List<LivingEntity> out = new ArrayList<>();
        for (Entity e : p.level().getEntities(p, p.getBoundingBox().inflate(r), e -> e instanceof LivingEntity && e.isAlive() && isFoe(p, e))) {
            out.add((LivingEntity) e);
        }
        return out;
    }

    private static void tick(MinecraftServer server) {
        Iterator<Job> it = JOBS.iterator();
        while (it.hasNext()) {
            Job j = it.next();
            if (j.player.isRemoved() || j.player.isDeadOrDying()) { it.remove(); continue; }
            if (j.t++ % j.every != 0) continue;
            int i = (j.t - 1) / j.every;
            j.step.accept(i);
            if (--j.left <= 0) it.remove();
        }
        if (server.getTickCount() % 200 == 0) leadershipAura(server);
    }

    /** Officers steady the people around them: crewmates and shipmates within 16 blocks. */
    private static void leadershipAura(MinecraftServer server) {
        for (ServerPlayer officer : server.getPlayerList().getPlayers()) {
            int lvl = ProfessionData.level(officer, Profession.LEADERSHIP);
            if (lvl < 5) continue;
            AABB area = officer.getBoundingBox().inflate(16);
            List<LivingEntity> people = new ArrayList<>(officer.level().getEntitiesOfClass(CrewmateEntity.class, area, Entity::isAlive));
            for (ServerPlayer mate : officer.level().getPlayers(m -> m != officer && m.distanceToSqr(officer) < 256
                    && CrewState.shipmates(server, officer.getUUID(), m.getUUID()))) people.add(mate);
            for (LivingEntity l : people) {
                l.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 240, 0, true, false));
                if (lvl >= 15) l.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 240, 0, true, false));
                if (lvl >= 25) l.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, true, false));
            }
            if (!people.isEmpty() && server.getTickCount() % 1200 == 0) ProfessionData.addXp(officer, Profession.LEADERSHIP, 2 + people.size());
        }
    }
}
