package com.cursedtides.sea;

import com.cursedtides.CursedTides;
import com.cursedtides.entity.ShipEntity;
import com.cursedtides.item.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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

    public static class TentacleEntity extends Monster {
        UUID ship;
        int slamCooldown = 40;

        public TentacleEntity(EntityType<? extends TentacleEntity> type, Level world) {
            super(type, world);
            this.setNoGravity(true);
            this.xpReward = 20;
        }

        public static AttributeSupplier.Builder attributes() {
            return Monster.createMonsterAttributes()
                    .add(Attributes.MAX_HEALTH, 60)
                    .add(Attributes.ARMOR, 4)
                    .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.0)
                    .add(Attributes.ATTACK_DAMAGE, 8);
        }

        @Override
        protected void registerGoals() { }

        @Override
        public boolean isPushable() { return false; }

        @Override
        public boolean removeWhenFarAway(double distanceSquared) { return false; }

        @Override
        public void tick() {
            super.tick();
            this.setDeltaMovement(Vec3.ZERO);
            if (!(this.level() instanceof ServerLevel w)) return;
            if (ship == null || !(w.getEntity(ship) instanceof ShipEntity s) || !s.isAlive() || this.tickCount > 20 * 240) {
                sink(w);
                return;
            }
            if (--slamCooldown <= 0) {
                slamCooldown = 50 + this.random.nextInt(30);
                List<Player> near = w.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(5, 3, 5), p -> p.isAlive() && !p.isSpectator());
                if (!near.isEmpty()) {
                    Player victim = near.get(this.random.nextInt(near.size()));
                    this.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                    victim.hurt(w.damageSources().mobAttack(this), 8f);
                    Vec3 push = victim.position().subtract(this.position()).multiply(1, 0, 1).normalize();
                    victim.push(push.x * 0.9, 0.5, push.z * 0.9);
                    victim.hurtMarked = true;
                    w.playSound(null, blockPosition(), SoundEvents.GUARDIAN_ATTACK, SoundSource.HOSTILE, 1.5f, 0.5f);
                    w.sendParticles(ParticleTypes.SPLASH, victim.getX(), victim.getY() + 0.5, victim.getZ(), 30, 0.6, 0.3, 0.6, 0.2);
                }
            }
            if (this.tickCount % 10 == 0) w.sendParticles(ParticleTypes.BUBBLE_POP, getX(), getY() + 0.3, getZ(), 6, 0.6, 0.1, 0.6, 0.02);
        }

        private void sink(ServerLevel w) {
            w.sendParticles(ParticleTypes.SPLASH, getX(), getY() + 1, getZ(), 40, 0.6, 1, 0.6, 0.2);
            this.discard();
        }

        @Override
        public boolean hurt(DamageSource source, float amount) {
            if (source.is(net.minecraft.world.damagesource.DamageTypes.DROWN) || source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL)) return false;
            return super.hurt(source, amount);
        }

        @Override
        public void addAdditionalSaveData(CompoundTag nbt) {
            super.addAdditionalSaveData(nbt);
            if (ship != null) nbt.putUUID("Ship", ship);
        }

        @Override
        public void readAdditionalSaveData(CompoundTag nbt) {
            super.readAdditionalSaveData(nbt);
            if (nbt.hasUUID("Ship")) ship = nbt.getUUID("Ship");
        }
    }

    public static final EntityType<TentacleEntity> TENTACLE = Registry.register(BuiltInRegistries.ENTITY_TYPE, CursedTides.id("tentacle"),
            EntityType.Builder.<TentacleEntity>of(TentacleEntity::new, MobCategory.MONSTER).sized(1.4f, 6.5f)
                    .clientTrackingRange(10).build("tentacle"));

    /** Ships in its grip, and when the grip began. */
    private static final Map<UUID, Long> GRIPPED = new HashMap<>();

    public static void register() {
        FabricDefaultAttributeRegistry.register(TENTACLE, TentacleEntity.attributes());
        ServerTickEvents.END_SERVER_TICK.register(Leviathan::tick);
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AFTER_DEATH.register((victim, source) -> {
            if (!(victim instanceof TentacleEntity t) || !(victim.level() instanceof ServerLevel w) || t.ship == null) return;
            if (w.getEntitiesOfClass(TentacleEntity.class, victim.getBoundingBox().inflate(30), e -> e.isAlive() && t.ship.equals(e.ship)).isEmpty()) {
                GRIPPED.remove(t.ship);
                w.playSound(null, victim.blockPosition(), SoundEvents.ELDER_GUARDIAN_DEATH, SoundSource.HOSTILE, 3f, 0.4f);
                for (ServerPlayer p : w.getPlayers(p -> p.distanceToSqr(victim) < 40 * 40)) {
                    p.displayClientMessage(Component.literal("The last arm slides back into the black water. Old Hundred-Arms lets you go... for now.")
                            .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), false);
                    p.addItem(new ItemStack(ModItems.DOUBLOON, 15));
                    com.cursedtides.pirate.PirateData.addXp(p, com.cursedtides.pirate.Weapon.SAILING, 150);
                    com.cursedtides.activity.Achievements.grant(p, "hundred_arms");
                }
                victim.spawnAtLocation(new ItemStack(ModItems.ANCIENT_RELIC));
                victim.spawnAtLocation(new ItemStack(net.minecraft.world.item.Items.INK_SAC, 12));
            }
        });
    }

    /** Rise under this ship. Returns false if it's not on open water. */
    public static boolean rise(ServerLevel w, ShipEntity ship) {
        if (GRIPPED.containsKey(ship.getUUID())) return false;
        int count = 4 + w.random.nextInt(3);
        for (int i = 0; i < count; i++) {
            double a = Math.PI * 2 * i / count + w.random.nextDouble() * 0.4;
            double r = 5 + ship.shipClass().scale() * 2;
            TentacleEntity t = TENTACLE.create(w);
            if (t == null) continue;
            t.ship = ship.getUUID();
            t.moveTo(ship.getX() + Math.cos(a) * r, ship.getY() - 3, ship.getZ() + Math.sin(a) * r, w.random.nextFloat() * 360, 0);
            t.setCustomName(Component.literal("Tentacle").withStyle(ChatFormatting.DARK_PURPLE));
            w.addFreshEntity(t);
        }
        GRIPPED.put(ship.getUUID(), w.getGameTime());
        w.playSound(null, ship.blockPosition(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 3f, 0.4f);
        for (Entity passenger : ship.getPassengers()) {
            passenger.sendSystemMessage(Component.literal("The sea heaves. Arms as thick as masts wrap the hull! Cut them loose before she cracks!")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        }
        return true;
    }

    private static void tick(MinecraftServer server) {
        ServerLevel w = server.overworld();
        if (!GRIPPED.isEmpty()) {
            for (var e : Map.copyOf(GRIPPED).entrySet()) {
                if (!(w.getEntity(e.getKey()) instanceof ShipEntity ship) || !ship.isAlive()) { GRIPPED.remove(e.getKey()); continue; }
                ship.setDeltaMovement(ship.getDeltaMovement().scale(0.1));
                long held = w.getGameTime() - e.getValue();
                if (held > 20 * 45 && held % 40 == 0) ship.seaDamage(w, 6f, "The tentacles squeeze. Planks burst!");
            }
        }
        // Now and then, far out in deep water, it comes up under someone
        if (server.getTickCount() % 6000 != 3000 || !com.cursedtides.world.ModWorld.isArchipelago()) return;
        for (ServerPlayer p : w.players()) {
            if (!(p.getVehicle() instanceof ShipEntity ship) || ship.getControllingPassenger() != p) continue;
            if (com.cursedtides.world.Archipelago.column(p.getBlockX(), p.getBlockZ()).island() != null) continue;
            if (w.random.nextFloat() < (w.isNight() ? 0.12f : 0.05f)) rise(w, ship);
        }
    }

    @SuppressWarnings("unused")
    private static AABB around(LivingEntity e, double r) { return e.getBoundingBox().inflate(r); }
}
