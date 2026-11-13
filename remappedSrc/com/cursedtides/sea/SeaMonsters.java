package com.cursedtides.sea;

import com.cursedtides.combat.Levels;
import com.cursedtides.entity.ShipEntity;
import com.cursedtides.item.ModItems;
import com.cursedtides.ship.ShipEvents;
import com.cursedtides.world.Archipelago;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import java.util.List;

/**
 * What roams the open water between the islands. Out of sight of land, a sailor may meet a Reach Serpent (it rams
 * hulls and spits at anyone on deck), a Siren of the Shoals (her song turns your stomach and drags your ship
 * toward her), or a pack of Bloodfin sharks circling for anyone who goes over the side. They're levelled like
 * the sea itself: tougher the further out you are. Old Hundred-Arms (see Leviathan) still lurks under it all.
 */
public final class SeaMonsters {
    private SeaMonsters() {}

    public static final String TAG = "ct_seamonster";
    public static final String SERPENT = "Reach Serpent", SIREN = "Siren of the Shoals", BLOODFIN = "Bloodfin";

    public enum Kind { SERPENT, SIREN, BLOODFIN }

    /** The monsters about, so we needn't search the whole world for them. */
    private static final java.util.Set<java.util.UUID> LIVE = new java.util.HashSet<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(SeaMonsters::tick);
        ServerLivingEntityEvents.AFTER_DEATH.register(SeaMonsters::onDeath);
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_LOAD.register((e, world) -> {
            if (e.getTags().contains(TAG)) LIVE.add(e.getUUID());
        });
    }

    private static boolean openSea(int x, int z) {
        Archipelago.Column c = Archipelago.column(x, z);
        if (c.island() != null) return false;
        for (Archipelago.Island is : Archipelago.ISLANDS) if (is.edgeDistance(x, z) < 70) return false;
        return true;
    }

    private static Kind kindOf(Entity e) {
        for (String t : e.getTags()) if (t.startsWith(TAG + ":")) return Kind.valueOf(t.substring(TAG.length() + 1));
        return null;
    }

    private static void tick(MinecraftServer server) {
        if (!com.cursedtides.world.ModWorld.isArchipelago()) return;
        ServerLevel w = server.overworld();
        int t = server.getTickCount();
        if (t % 20 == 5) behave(w);
        if (t % 400 != 200) return;
        for (ServerPlayer p : w.players()) {
            if (!(p.getVehicle() instanceof ShipEntity) && !p.isInWater()) continue;
            if (!openSea(p.getBlockX(), p.getBlockZ())) continue;
            List<Mob> near = w.getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(200), m -> m.getTags().contains(TAG));
            if (near.size() >= 3) continue;
            if (w.random.nextFloat() > (w.isNight() ? 0.45f : 0.25f)) continue;
            BlockPos at = ShipEvents.findSeaSpot(w, p, 60, 95);
            if (at == null || !openSea(at.getX(), at.getZ())) continue;
            float roll = w.random.nextFloat();
            Kind kind = roll < 0.4f ? Kind.SERPENT : roll < 0.7f ? Kind.BLOODFIN : Kind.SIREN;
            if (kind == Kind.SIREN && !w.isNight() && w.random.nextBoolean()) kind = Kind.SERPENT; // sirens sing mostly at night
            spawn(w, kind, at, p);
        }
    }

    public static void spawn(ServerLevel w, Kind kind, BlockPos at, ServerPlayer p) {
        int[] band = Levels.bandAt(at);
        int level = Math.min(50, band[1] + 2);
        switch (kind) {
            case SERPENT -> {
                Mob m = make(w, EntityType.GUARDIAN, at.getX() + 0.5, Archipelago.WATER_TOP - 3, at.getZ() + 0.5, SERPENT, level, 9, 2.6);
                if (m != null) {
                    tag(m, kind);
                    w.addFreshEntity(m);
                    tell(p, "Something long and green breaks the water off your bow, and goes under again. A Reach Serpent.");
                }
            }
            case SIREN -> {
                Mob m = make(w, EntityType.DROWNED, at.getX() + 0.5, Archipelago.WATER_TOP - 1, at.getZ() + 0.5, SIREN, level, 5, 1.1);
                if (m != null) {
                    tag(m, kind);
                    m.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.TRIDENT));
                    m.setDropChance(EquipmentSlot.MAINHAND, 0.15f);
                    w.addFreshEntity(m);
                    tell(p, "A voice drifts over the water, sweet and wrong. Your helm pulls toward it by itself.");
                }
            }
            case BLOODFIN -> {
                int n = 3 + w.random.nextInt(2);
                for (int i = 0; i < n; i++) {
                    Mob m = make(w, com.cursedtides.wildlife.Wildlife.SHARK, at.getX() + w.random.nextGaussian() * 3, Archipelago.WATER_TOP - 2,
                            at.getZ() + w.random.nextGaussian() * 3, BLOODFIN, level - 2, 2.5, 1.3);
                    if (m == null) continue;
                    tag(m, kind);
                    w.addFreshEntity(m);
                }
                tell(p, "Fins. Lots of them, cutting circles in the water. Bloodfins. Don't go over the side.");
            }
        }
    }

    private static void tag(Mob m, Kind kind) {
        LIVE.add(m.getUUID());
        m.addTag(TAG);
        m.addTag(TAG + ":" + kind.name());
    }

    private static void tell(ServerPlayer p, String s) {
        p.displayClientMessage(Component.literal(s).withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC), false);
        p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.ELDER_GUARDIAN_AMBIENT, SoundSource.HOSTILE, 1.2f, 0.6f);
    }

    private static Mob make(ServerLevel w, EntityType<? extends Mob> type, double x, double y, double z,
                                  String name, int level, double hpMult, double scale) {
        Mob m = type.create(w);
        if (m == null) return null;
        m.moveTo(x, y, z, w.random.nextFloat() * 360, 0);
        m.finalizeSpawn(w, w.getCurrentDifficultyAt(m.blockPosition()), MobSpawnType.EVENT, null);
        m.setCustomName(Component.literal(name).withStyle(ChatFormatting.DARK_AQUA));
        Levels.apply(m, Math.max(1, level));
        AttributeInstance hp = m.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null) {
            hp.setBaseValue(hp.getBaseValue() * hpMult * Levels.healthFactor(level));
            m.setHealth(m.getMaxHealth());
        }
        AttributeInstance size = m.getAttribute(Attributes.SCALE);
        if (size != null) size.setBaseValue(scale);
        return m;
    }

    /** Once a second: hunt ships, sing, circle, and go back to the deep when nobody's about. */
    private static void behave(ServerLevel w) {
        for (java.util.UUID id : List.copyOf(LIVE)) {
            if (!(w.getEntity(id) instanceof Mob m) || !m.isAlive()) { LIVE.remove(id); continue; }
            ServerPlayer p = (ServerPlayer) w.getNearestPlayer(m, 240);
            if (p == null) { m.discard(); continue; }
            Kind kind = kindOf(m);
            if (kind == null) continue;
            int level = Math.max(1, Levels.levelOf(m));
            ShipEntity ship = nearestShip(w, m, 60);
            switch (kind) {
                case SERPENT -> {
                    if (ship != null) {
                        m.getNavigation().moveTo(ship.getX(), Archipelago.WATER_TOP - 2, ship.getZ(), 1.4);
                        if (m.distanceToSqr(ship) < 8 * 8 && w.getGameTime() % 60 < 20) {
                            ship.seaDamage(w, 6f + level / 6f, "The Reach Serpent rams the hull! Planks splinter below the waterline!");
                            ship.push(m.getLookAngle().x * 0.6, 0.1, m.getLookAngle().z * 0.6);
                            w.sendParticles(ParticleTypes.SPLASH, ship.getX(), ship.getY() + 0.5, ship.getZ(), 40, 1.5, 0.4, 1.5, 0.3);
                        }
                    }
                    if (m.getTarget() == null && p.distanceToSqr(m) < 40 * 40) m.setTarget(p);
                }
                case SIREN -> {
                    // Stay near the surface, and sing
                    if (m.getY() < Archipelago.WATER_TOP - 2) m.push(0, 0.08, 0);
                    if (w.getGameTime() % 60 < 20) {
                        w.playSound(null, m.blockPosition(), SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, SoundSource.HOSTILE, 2.5f, 0.5f);
                        w.sendParticles(ParticleTypes.NOTE, m.getX(), m.getY() + 2.2, m.getZ(), 4, 0.6, 0.3, 0.6, 1);
                        for (ServerPlayer q : w.getPlayers(q -> q.distanceToSqr(m) < 28 * 28)) {
                            q.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120, 0));
                            q.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
                        }
                    }
                    if (ship != null && m.distanceToSqr(ship) > 5 * 5 && m.distanceToSqr(ship) < 45 * 45) {
                        Vec3 pull = m.position().subtract(ship.position()).multiply(1, 0, 1).normalize().scale(0.05);
                        ship.push(pull.x, 0, pull.z);
                    }
                    if (m.getTarget() == null) m.setTarget(p);
                }
                case BLOODFIN -> {
                    LivingEntity swimmer = w.getNearestPlayer(m.getX(), m.getY(), m.getZ(), 30,
                            e -> e instanceof ServerPlayer sp && sp.isInWater() && !(sp.getVehicle() instanceof ShipEntity));
                    if (swimmer != null) m.setTarget(swimmer);
                    else if (ship != null) {
                        double a = (w.getGameTime() / 20.0) * 0.4 + m.getId();
                        m.getNavigation().moveTo(ship.getX() + Math.cos(a) * 7, Archipelago.WATER_TOP - 2, ship.getZ() + Math.sin(a) * 7, 1.2);
                    }
                }
            }
        }
    }

    private static ShipEntity nearestShip(ServerLevel w, Mob m, double r) {
        ShipEntity best = null;
        double bd = r * r;
        for (ShipEntity s : w.getEntitiesOfClass(ShipEntity.class, m.getBoundingBox().inflate(r), s -> !s.isHostile() && s.isVehicle())) {
            double d = s.distanceToSqr(m);
            if (d < bd) { bd = d; best = s; }
        }
        return best;
    }

    private static void onDeath(LivingEntity victim, DamageSource source) {
        Kind kind = kindOf(victim);
        if (kind == null || !(source.getEntity() instanceof ServerPlayer p)) return;
        int level = Math.max(1, Levels.levelOf(victim));
        switch (kind) {
            case SERPENT -> {
                victim.spawnAtLocation(com.cursedtides.combat.Loot.container(com.cursedtides.combat.Loot.Tier.CHEST, level));
                victim.spawnAtLocation(new ItemStack(ModItems.DOUBLOON, 12 + level / 2));
                victim.spawnAtLocation(new ItemStack(Items.PRISMARINE_SHARD, 4 + p.getRandom().nextInt(6)));
                if (p.getRandom().nextInt(3) == 0) victim.spawnAtLocation(new ItemStack(Items.HEART_OF_THE_SEA));
                p.displayClientMessage(Component.literal("The Reach Serpent rolls over and sinks, trailing green.").withStyle(ChatFormatting.DARK_AQUA), false);
            }
            case SIREN -> {
                victim.spawnAtLocation(com.cursedtides.combat.Loot.container(com.cursedtides.combat.Loot.Tier.CHEST, level));
                victim.spawnAtLocation(new ItemStack(Items.NAUTILUS_SHELL, 1 + p.getRandom().nextInt(2)));
                victim.spawnAtLocation(new ItemStack(ModItems.DOUBLOON, 10 + level / 2));
                p.displayClientMessage(Component.literal("The song stops mid-note. The sea is very quiet.").withStyle(ChatFormatting.DARK_AQUA), false);
            }
            case BLOODFIN -> {
                victim.spawnAtLocation(new ItemStack(ModItems.SHARK_TOOTH, 1 + p.getRandom().nextInt(2)));
                if (p.getRandom().nextInt(4) == 0) victim.spawnAtLocation(com.cursedtides.combat.Loot.container(com.cursedtides.combat.Loot.Tier.POUCH, level));
            }
        }
        com.cursedtides.pirate.PirateData.addXp(p, com.cursedtides.pirate.Weapon.SAILING, kind == Kind.BLOODFIN ? 10 : 70);
        com.cursedtides.activity.Achievements.grant(p, "sea_monster");
    }
}
