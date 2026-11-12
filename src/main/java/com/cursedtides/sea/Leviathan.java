package com.cursedtides.sea;

import com.cursedtides.CursedTides;
import com.cursedtides.entity.ShipEntity;
import com.cursedtides.item.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Old Hundred-Arms, the thing under the deep water. It rises beneath a ship, wraps it in tentacles and holds it fast.
 * Cut every tentacle (they slam anyone on deck) before the hull gives way, or she goes down with all hands.
 */
public final class Leviathan {
    private Leviathan() {}

    public static class TentacleEntity extends HostileEntity {
        UUID ship;
        int slamCooldown = 40;

        public TentacleEntity(EntityType<? extends TentacleEntity> type, World world) {
            super(type, world);
            this.setNoGravity(true);
            this.experiencePoints = 20;
        }

        public static DefaultAttributeContainer.Builder attributes() {
            return HostileEntity.createHostileAttributes()
                    .add(EntityAttributes.GENERIC_MAX_HEALTH, 60)
                    .add(EntityAttributes.GENERIC_ARMOR, 4)
                    .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0)
                    .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.0)
                    .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8);
        }

        @Override
        protected void initGoals() { }

        @Override
        public boolean isPushable() { return false; }

        @Override
        public boolean canImmediatelyDespawn(double distanceSquared) { return false; }

        @Override
        public void tick() {
            super.tick();
            this.setVelocity(Vec3d.ZERO);
            if (!(this.getWorld() instanceof ServerWorld w)) return;
            if (ship == null || !(w.getEntity(ship) instanceof ShipEntity s) || !s.isAlive() || this.age > 20 * 240) {
                sink(w);
                return;
            }
            if (--slamCooldown <= 0) {
                slamCooldown = 50 + this.random.nextInt(30);
                List<PlayerEntity> near = w.getEntitiesByClass(PlayerEntity.class, this.getBoundingBox().expand(5, 3, 5), p -> p.isAlive() && !p.isSpectator());
                if (!near.isEmpty()) {
                    PlayerEntity victim = near.get(this.random.nextInt(near.size()));
                    this.swingHand(net.minecraft.util.Hand.MAIN_HAND);
                    victim.damage(w.getDamageSources().mobAttack(this), 8f);
                    Vec3d push = victim.getPos().subtract(this.getPos()).multiply(1, 0, 1).normalize();
                    victim.addVelocity(push.x * 0.9, 0.5, push.z * 0.9);
                    victim.velocityModified = true;
                    w.playSound(null, getBlockPos(), SoundEvents.ENTITY_GUARDIAN_ATTACK, SoundCategory.HOSTILE, 1.5f, 0.5f);
                    w.spawnParticles(ParticleTypes.SPLASH, victim.getX(), victim.getY() + 0.5, victim.getZ(), 30, 0.6, 0.3, 0.6, 0.2);
                }
            }
            if (this.age % 10 == 0) w.spawnParticles(ParticleTypes.BUBBLE_POP, getX(), getY() + 0.3, getZ(), 6, 0.6, 0.1, 0.6, 0.02);
        }

        private void sink(ServerWorld w) {
            w.spawnParticles(ParticleTypes.SPLASH, getX(), getY() + 1, getZ(), 40, 0.6, 1, 0.6, 0.2);
            this.discard();
        }

        @Override
        public boolean damage(DamageSource source, float amount) {
            if (source.isOf(net.minecraft.entity.damage.DamageTypes.DROWN) || source.isOf(net.minecraft.entity.damage.DamageTypes.IN_WALL)) return false;
            return super.damage(source, amount);
        }

        @Override
        public void writeCustomDataToNbt(NbtCompound nbt) {
            super.writeCustomDataToNbt(nbt);
            if (ship != null) nbt.putUuid("Ship", ship);
        }

        @Override
        public void readCustomDataFromNbt(NbtCompound nbt) {
            super.readCustomDataFromNbt(nbt);
            if (nbt.containsUuid("Ship")) ship = nbt.getUuid("Ship");
        }
    }

    public static final EntityType<TentacleEntity> TENTACLE = Registry.register(Registries.ENTITY_TYPE, CursedTides.id("tentacle"),
            EntityType.Builder.<TentacleEntity>create(TentacleEntity::new, SpawnGroup.MONSTER).dimensions(1.4f, 6.5f)
                    .maxTrackingRange(10).build("tentacle"));

    /** Ships in its grip, and when the grip began. */
    private static final Map<UUID, Long> GRIPPED = new HashMap<>();

    public static void register() {
        FabricDefaultAttributeRegistry.register(TENTACLE, TentacleEntity.attributes());
        ServerTickEvents.END_SERVER_TICK.register(Leviathan::tick);
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AFTER_DEATH.register((victim, source) -> {
            if (!(victim instanceof TentacleEntity t) || !(victim.getWorld() instanceof ServerWorld w) || t.ship == null) return;
            if (w.getEntitiesByClass(TentacleEntity.class, victim.getBoundingBox().expand(30), e -> e.isAlive() && t.ship.equals(e.ship)).isEmpty()) {
                GRIPPED.remove(t.ship);
                w.playSound(null, victim.getBlockPos(), SoundEvents.ENTITY_ELDER_GUARDIAN_DEATH, SoundCategory.HOSTILE, 3f, 0.4f);
                for (ServerPlayerEntity p : w.getPlayers(p -> p.squaredDistanceTo(victim) < 40 * 40)) {
                    p.sendMessage(Text.literal("The last arm slides back into the black water. Old Hundred-Arms lets you go... for now.")
                            .formatted(Formatting.DARK_AQUA, Formatting.BOLD), false);
                    p.giveItemStack(new ItemStack(ModItems.DOUBLOON, 15));
                    com.cursedtides.pirate.PirateData.addXp(p, com.cursedtides.pirate.Weapon.SAILING, 150);
                    com.cursedtides.activity.Achievements.grant(p, "hundred_arms");
                }
                victim.dropStack(new ItemStack(ModItems.ANCIENT_RELIC));
                victim.dropStack(new ItemStack(net.minecraft.item.Items.INK_SAC, 12));
            }
        });
    }

    /** Rise under this ship. Returns false if it's not on open water. */
    public static boolean rise(ServerWorld w, ShipEntity ship) {
        if (GRIPPED.containsKey(ship.getUuid())) return false;
        int count = 4 + w.random.nextInt(3);
        for (int i = 0; i < count; i++) {
            double a = Math.PI * 2 * i / count + w.random.nextDouble() * 0.4;
            double r = 5 + ship.shipClass().scale() * 2;
            TentacleEntity t = TENTACLE.create(w);
            if (t == null) continue;
            t.ship = ship.getUuid();
            t.refreshPositionAndAngles(ship.getX() + Math.cos(a) * r, ship.getY() - 3, ship.getZ() + Math.sin(a) * r, w.random.nextFloat() * 360, 0);
            t.setCustomName(Text.literal("Tentacle").formatted(Formatting.DARK_PURPLE));
            w.spawnEntity(t);
        }
        GRIPPED.put(ship.getUuid(), w.getTime());
        w.playSound(null, ship.getBlockPos(), SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.HOSTILE, 3f, 0.4f);
        for (Entity passenger : ship.getPassengerList()) {
            passenger.sendMessage(Text.literal("The sea heaves. Arms as thick as masts wrap the hull! Cut them loose before she cracks!")
                    .formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        }
        return true;
    }

    private static void tick(MinecraftServer server) {
        ServerWorld w = server.getOverworld();
        if (!GRIPPED.isEmpty()) {
            for (var e : Map.copyOf(GRIPPED).entrySet()) {
                if (!(w.getEntity(e.getKey()) instanceof ShipEntity ship) || !ship.isAlive()) { GRIPPED.remove(e.getKey()); continue; }
                ship.setVelocity(ship.getVelocity().multiply(0.1));
                long held = w.getTime() - e.getValue();
                if (held > 20 * 45 && held % 40 == 0) ship.seaDamage(w, 6f, "The tentacles squeeze. Planks burst!");
            }
        }
        // Now and then, far out in deep water, it comes up under someone
        if (server.getTicks() % 6000 != 3000 || !com.cursedtides.world.ModWorld.isArchipelago()) return;
        for (ServerPlayerEntity p : w.getPlayers()) {
            if (!(p.getVehicle() instanceof ShipEntity ship) || ship.getControllingPassenger() != p) continue;
            if (com.cursedtides.world.Archipelago.column(p.getBlockX(), p.getBlockZ()).island() != null) continue;
            if (w.random.nextFloat() < (w.isNight() ? 0.12f : 0.05f)) rise(w, ship);
        }
    }

    @SuppressWarnings("unused")
    private static Box around(LivingEntity e, double r) { return e.getBoundingBox().expand(r); }
}
