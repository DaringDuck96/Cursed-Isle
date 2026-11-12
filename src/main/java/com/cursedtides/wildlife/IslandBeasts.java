package com.cursedtides.wildlife;

import com.cursedtides.CursedTides;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * The island creatures the old pirate game was known for, as our own: giant wasps in the jungles, cave bats that
 * come out at night, scorpions in the dunes and the volcanic badlands, snapjaw flytraps rooted in the undergrowth,
 * and living stumps that stand very still until you walk past.
 */
public final class IslandBeasts {
    private IslandBeasts() {}

    // ------------------------------------------------------------------ fliers: wasps and bats

    /** Hovers, drifts about its home, and dives at whatever it's angry with. */
    public static class FlierEntity extends HostileEntity {
        private final boolean wasp;
        private Vec3d home;
        private int stingCooldown;

        public FlierEntity(EntityType<? extends FlierEntity> type, World world, boolean wasp) {
            super(type, world);
            this.wasp = wasp;
            this.setNoGravity(true);
            this.experiencePoints = 8;
        }

        @Override
        protected void initGoals() {
            this.targetSelector.add(1, new RevengeGoal(this));
            this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, 10, true, false,
                    p -> wasp || !this.getWorld().isDay() || this.getWorld().getLightLevel(this.getBlockPos()) < 8));
        }

        @Override
        public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource source) { return false; }

        @Override
        public void tick() {
            super.tick();
            if (this.getWorld().isClient) return;
            if (home == null) home = this.getPos();
            if (stingCooldown > 0) stingCooldown--;
            LivingEntity t = this.getTarget();
            Vec3d want;
            double speed;
            if (t != null && t.isAlive() && t.squaredDistanceTo(this) < 32 * 32) {
                want = t.getPos().add(0, t.getHeight() * (wasp ? 0.8 : 0.6), 0);
                speed = wasp ? 0.32 : 0.36;
                if (this.squaredDistanceTo(t) < 2.2 * 2.2 && stingCooldown <= 0) {
                    stingCooldown = wasp ? 30 : 24;
                    this.swingHand(Hand.MAIN_HAND);
                    if (this.tryAttack(t)) {
                        if (wasp) t.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 80, 0));
                        else {
                            t.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 80, 0));
                            this.heal(2);
                        }
                    }
                    // Fly back out after a strike
                    Vec3d away = this.getPos().subtract(t.getPos()).normalize().multiply(0.6);
                    this.addVelocity(away.x, 0.3, away.z);
                }
            } else {
                this.setTarget(null);
                double a = (this.age + this.getId() * 37) * 0.03;
                want = home.add(Math.cos(a) * 5, 2 + Math.sin(a * 1.7) * 1.5, Math.sin(a) * 5);
                speed = 0.12;
            }
            Vec3d to = want.subtract(this.getPos());
            if (to.lengthSquared() > 0.01) {
                Vec3d v = this.getVelocity().multiply(0.8).add(to.normalize().multiply(speed * 0.25));
                this.setVelocity(v);
                this.setYaw((float) Math.toDegrees(Math.atan2(-to.x, to.z)));
                this.bodyYaw = this.getYaw();
                this.headYaw = this.getYaw();
            }
            if (wasp && this.age % 8 == 0) this.playSound(SoundEvents.ENTITY_BEE_LOOP_AGGRESSIVE, 0.4f, 0.7f);
        }

        @Override
        public void travel(Vec3d movementInput) {
            this.move(net.minecraft.entity.MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(0.91));
        }

        public static DefaultAttributeContainer.Builder waspAttributes() {
            return HostileEntity.createHostileAttributes()
                    .add(EntityAttributes.GENERIC_MAX_HEALTH, 12)
                    .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3)
                    .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24)
                    .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.6);
        }

        public static DefaultAttributeContainer.Builder batAttributes() {
            return HostileEntity.createHostileAttributes()
                    .add(EntityAttributes.GENERIC_MAX_HEALTH, 14)
                    .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.5)
                    .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24)
                    .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.7);
        }
    }

    // ------------------------------------------------------------------ the scorpion

    public static class ScorpionEntity extends HostileEntity {
        public ScorpionEntity(EntityType<? extends ScorpionEntity> type, World world) {
            super(type, world);
            this.experiencePoints = 8;
        }

        @Override
        protected void initGoals() {
            this.goalSelector.add(0, new SwimGoal(this));
            this.goalSelector.add(2, new MeleeAttackGoal(this, 1.2, false));
            this.goalSelector.add(6, new WanderAroundFarGoal(this, 0.8));
            this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
            this.goalSelector.add(8, new LookAroundGoal(this));
            this.targetSelector.add(1, new RevengeGoal(this));
            this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        }

        @Override
        public boolean tryAttack(Entity target) {
            boolean hit = super.tryAttack(target);
            if (hit && target instanceof LivingEntity l && this.random.nextInt(3) == 0) {
                l.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 1));
            }
            return hit;
        }

        public static DefaultAttributeContainer.Builder attributes() {
            return HostileEntity.createHostileAttributes()
                    .add(EntityAttributes.GENERIC_MAX_HEALTH, 22)
                    .add(EntityAttributes.GENERIC_ARMOR, 6)
                    .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.26)
                    .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4);
        }
    }

    // ------------------------------------------------------------------ the snapjaw flytrap

    /** Rooted to the spot. Bites anything that comes within reach, and pulls it closer. */
    public static class FlytrapEntity extends HostileEntity {
        private int biteCooldown;

        public FlytrapEntity(EntityType<? extends FlytrapEntity> type, World world) {
            super(type, world);
            this.experiencePoints = 6;
        }

        @Override
        protected void initGoals() {
            this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
            this.targetSelector.add(1, new RevengeGoal(this));
            this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, false));
        }

        @Override
        public boolean isPushable() { return false; }

        @Override
        public void tick() {
            super.tick();
            this.setVelocity(0, Math.min(0, this.getVelocity().y), 0);
            if (this.getWorld().isClient) return;
            if (biteCooldown > 0) biteCooldown--;
            LivingEntity t = this.getTarget();
            if (t == null || !t.isAlive()) return;
            double d = this.squaredDistanceTo(t);
            if (d < 4.2 * 4.2) {
                this.getLookControl().lookAt(t, 30, 30);
                if (biteCooldown <= 0) {
                    biteCooldown = 28;
                    this.swingHand(Hand.MAIN_HAND);
                    if (this.tryAttack(t)) {
                        Vec3d pull = this.getPos().subtract(t.getPos()).multiply(1, 0, 1).normalize().multiply(0.45);
                        t.addVelocity(pull.x, 0.1, pull.z);
                        t.velocityModified = true;
                        t.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 1));
                    }
                    this.playSound(SoundEvents.ENTITY_EVOKER_FANGS_ATTACK, 1f, 1.4f);
                }
            }
        }

        public static DefaultAttributeContainer.Builder attributes() {
            return HostileEntity.createHostileAttributes()
                    .add(EntityAttributes.GENERIC_MAX_HEALTH, 26)
                    .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.0)
                    .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0)
                    .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5)
                    .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 6);
        }
    }

    // ------------------------------------------------------------------ the living stump

    /** A stump, until you're close enough to grab. Slow, tough, and it hits like a falling tree. */
    public static class StumpEntity extends HostileEntity {
        private boolean awake;

        public StumpEntity(EntityType<? extends StumpEntity> type, World world) {
            super(type, world);
            this.experiencePoints = 10;
        }

        @Override
        protected void initGoals() {
            this.goalSelector.add(0, new SwimGoal(this));
            this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0, true));
            this.goalSelector.add(8, new LookAroundGoal(this));
            this.targetSelector.add(1, new RevengeGoal(this));
            this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, 10, true, false,
                    p -> this.awake || p.squaredDistanceTo(this) < 4.5 * 4.5));
        }

        @Override
        public void tick() {
            super.tick();
            if (this.getWorld().isClient) return;
            if (!awake) {
                this.setVelocity(0, Math.min(0, this.getVelocity().y), 0);
                if (this.getTarget() != null) {
                    awake = true;
                    this.playSound(SoundEvents.BLOCK_WOOD_BREAK, 1.5f, 0.5f);
                    if (this.getWorld() instanceof ServerWorld w) {
                        w.spawnParticles(ParticleTypes.COMPOSTER, getX(), getY() + 1, getZ(), 20, 0.5, 0.8, 0.5, 0.05);
                    }
                }
            }
        }

        @Override
        public boolean damage(DamageSource source, float amount) {
            awake = true;
            if (source.isIn(net.minecraft.registry.tag.DamageTypeTags.IS_FIRE)) amount *= 2; // dry wood
            return super.damage(source, amount);
        }

        @Override
        public boolean tryAttack(Entity target) {
            boolean hit = super.tryAttack(target);
            if (hit && target instanceof LivingEntity l) {
                Vec3d push = l.getPos().subtract(this.getPos()).multiply(1, 0, 1).normalize();
                l.addVelocity(push.x * 0.9, 0.35, push.z * 0.9);
                l.velocityModified = true;
            }
            return hit;
        }

        public static DefaultAttributeContainer.Builder attributes() {
            return HostileEntity.createHostileAttributes()
                    .add(EntityAttributes.GENERIC_MAX_HEALTH, 40)
                    .add(EntityAttributes.GENERIC_ARMOR, 8)
                    .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2)
                    .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.8)
                    .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 7);
        }
    }

    // ------------------------------------------------------------------ registration

    private static <T extends Entity> EntityType<T> type(String name, EntityType.EntityFactory<T> factory, float w, float h) {
        return Registry.register(Registries.ENTITY_TYPE, CursedTides.id(name),
                EntityType.Builder.create(factory, SpawnGroup.MONSTER).dimensions(w, h).maxTrackingRange(8).build(name));
    }

    public static final EntityType<FlierEntity> GIANT_WASP = type("giant_wasp", (t, w) -> new FlierEntity(t, w, true), 0.8f, 0.8f);
    public static final EntityType<FlierEntity> CAVE_BAT = type("cave_bat", (t, w) -> new FlierEntity(t, w, false), 1.2f, 0.9f);
    public static final EntityType<ScorpionEntity> DUNE_SCORPION = type("dune_scorpion", ScorpionEntity::new, 1.1f, 0.7f);
    public static final EntityType<FlytrapEntity> SNAPJAW = type("snapjaw", FlytrapEntity::new, 0.9f, 1.6f);
    public static final EntityType<StumpEntity> LIVING_STUMP = type("living_stump", StumpEntity::new, 0.9f, 1.4f);

    private static Item egg(String name, EntityType<? extends MobEntity> type, int a, int b) {
        return Registry.register(Registries.ITEM, CursedTides.id(name + "_spawn_egg"), new SpawnEggItem(type, a, b, new Item.Settings()));
    }

    public static final Item WASP_EGG = egg("giant_wasp", GIANT_WASP, 0xE8B020, 0x1A140C);
    public static final Item BAT_EGG = egg("cave_bat", CAVE_BAT, 0x3A2E2A, 0xFF3A3A);
    public static final Item SCORPION_EGG = egg("dune_scorpion", DUNE_SCORPION, 0xB0803A, 0x4A3218);
    public static final Item SNAPJAW_EGG = egg("snapjaw", SNAPJAW, 0x4A7A2A, 0xC03A4A);
    public static final Item STUMP_EGG = egg("living_stump", LIVING_STUMP, 0x5A4228, 0xFFD23A);

    public static void register() {
        FabricDefaultAttributeRegistry.register(GIANT_WASP, FlierEntity.waspAttributes());
        FabricDefaultAttributeRegistry.register(CAVE_BAT, FlierEntity.batAttributes());
        FabricDefaultAttributeRegistry.register(DUNE_SCORPION, ScorpionEntity.attributes());
        FabricDefaultAttributeRegistry.register(SNAPJAW, FlytrapEntity.attributes());
        FabricDefaultAttributeRegistry.register(LIVING_STUMP, StumpEntity.attributes());
        ItemGroupEvents.modifyEntriesEvent(RegistryKey.of(RegistryKeys.ITEM_GROUP, CursedTides.id("main"))).register(entries -> {
            for (Item egg : new Item[]{WASP_EGG, BAT_EGG, SCORPION_EGG, SNAPJAW_EGG, STUMP_EGG}) entries.add(egg);
        });
    }
}
