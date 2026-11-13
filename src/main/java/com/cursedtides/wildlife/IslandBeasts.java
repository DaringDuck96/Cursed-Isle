package com.cursedtides.wildlife;

import com.cursedtides.CursedTides;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * The island creatures the old pirate game was known for, as our own: giant wasps in the jungles, cave bats that
 * come out at night, scorpions in the dunes and the volcanic badlands, snapjaw flytraps rooted in the undergrowth,
 * and living stumps that stand very still until you walk past.
 */
public final class IslandBeasts {
    private IslandBeasts() {}

    // ------------------------------------------------------------------ fliers: wasps and bats

    /** Hovers, drifts about its home, and dives at whatever it's angry with. */
    public static class FlierEntity extends Monster {
        private final boolean wasp;
        private Vec3 home;
        private int stingCooldown;

        public FlierEntity(EntityType<? extends FlierEntity> type, Level world, boolean wasp) {
            super(type, world);
            this.wasp = wasp;
            this.setNoGravity(true);
            this.xpReward = 8;
        }

        @Override
        protected void registerGoals() {
            this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                    (p, l__) -> wasp || this.level().isDarkOutside() || this.level().getMaxLocalRawBrightness(this.blockPosition()) < 8));
        }

        @Override
        public boolean causeFallDamage(double fallDistance, float damageMultiplier, DamageSource source) { return false; }

        @Override
        public void tick() {
            super.tick();
            if (this.level().isClientSide()) return;
            if (home == null) home = this.position();
            if (stingCooldown > 0) stingCooldown--;
            LivingEntity t = this.getTarget();
            Vec3 want;
            double speed;
            if (t != null && t.isAlive() && t.distanceToSqr(this) < 32 * 32) {
                want = t.position().add(0, t.getBbHeight() * (wasp ? 0.8 : 0.6), 0);
                speed = wasp ? 0.32 : 0.36;
                if (this.distanceToSqr(t) < 2.2 * 2.2 && stingCooldown <= 0) {
                    stingCooldown = wasp ? 30 : 24;
                    com.cursedtides.util.Compat.swing(this, InteractionHand.MAIN_HAND);
                    if (this.doHurtTarget((net.minecraft.server.level.ServerLevel) this.level(), t)) {
                        if (wasp) t.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0));
                        else {
                            t.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
                            this.heal(2);
                        }
                    }
                    // Fly back out after a strike
                    Vec3 away = this.position().subtract(t.position()).normalize().scale(0.6);
                    this.push(away.x, 0.3, away.z);
                }
            } else {
                this.setTarget(null);
                double a = (this.tickCount + this.getId() * 37) * 0.03;
                want = home.add(Math.cos(a) * 5, 2 + Math.sin(a * 1.7) * 1.5, Math.sin(a) * 5);
                speed = 0.12;
            }
            Vec3 to = want.subtract(this.position());
            if (to.lengthSqr() > 0.01) {
                Vec3 v = this.getDeltaMovement().scale(0.8).add(to.normalize().scale(speed * 0.25));
                this.setDeltaMovement(v);
                this.setYRot((float) Math.toDegrees(Math.atan2(-to.x, to.z)));
                this.yBodyRot = this.getYRot();
                this.yHeadRot = this.getYRot();
            }
            if (wasp && this.tickCount % 8 == 0) this.playSound(SoundEvents.BEE_LOOP_AGGRESSIVE, 0.4f, 0.7f);
        }

        @Override
        public void travel(Vec3 movementInput) {
            this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.91));
        }

        public static AttributeSupplier.Builder waspAttributes() {
            return Monster.createMonsterAttributes()
                    .add(Attributes.MAX_HEALTH, 12)
                    .add(Attributes.ATTACK_DAMAGE, 3)
                    .add(Attributes.FOLLOW_RANGE, 24)
                    .add(Attributes.FLYING_SPEED, 0.6);
        }

        public static AttributeSupplier.Builder batAttributes() {
            return Monster.createMonsterAttributes()
                    .add(Attributes.MAX_HEALTH, 14)
                    .add(Attributes.ATTACK_DAMAGE, 3.5)
                    .add(Attributes.FOLLOW_RANGE, 24)
                    .add(Attributes.FLYING_SPEED, 0.7);
        }
    }

    // ------------------------------------------------------------------ the scorpion

    public static class ScorpionEntity extends Monster {
        public ScorpionEntity(EntityType<? extends ScorpionEntity> type, Level world) {
            super(type, world);
            this.xpReward = 8;
        }

        @Override
        protected void registerGoals() {
            this.goalSelector.addGoal(0, new FloatGoal(this));
            this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, false));
            this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
            this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0f));
            this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
            this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        }

        @Override
        public boolean doHurtTarget(net.minecraft.server.level.ServerLevel level__, Entity target) {
            boolean hit = super.doHurtTarget((net.minecraft.server.level.ServerLevel) this.level(), target);
            if (hit && target instanceof LivingEntity l && this.random.nextInt(3) == 0) {
                l.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
            }
            return hit;
        }

        public static AttributeSupplier.Builder attributes() {
            return Monster.createMonsterAttributes()
                    .add(Attributes.MAX_HEALTH, 22)
                    .add(Attributes.ARMOR, 6)
                    .add(Attributes.MOVEMENT_SPEED, 0.26)
                    .add(Attributes.ATTACK_DAMAGE, 4);
        }
    }

    // ------------------------------------------------------------------ the snapjaw flytrap

    /** Rooted to the spot. Bites anything that comes within reach, and pulls it closer. */
    public static class FlytrapEntity extends Monster {
        private int biteCooldown;

        public FlytrapEntity(EntityType<? extends FlytrapEntity> type, Level world) {
            super(type, world);
            this.xpReward = 6;
        }

        @Override
        protected void registerGoals() {
            this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0f));
            this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
        }

        @Override
        public boolean isPushable() { return false; }

        @Override
        public void tick() {
            super.tick();
            this.setDeltaMovement(0, Math.min(0, this.getDeltaMovement().y), 0);
            if (this.level().isClientSide()) return;
            if (biteCooldown > 0) biteCooldown--;
            LivingEntity t = this.getTarget();
            if (t == null || !t.isAlive()) return;
            double d = this.distanceToSqr(t);
            if (d < 4.2 * 4.2) {
                this.getLookControl().setLookAt(t, 30, 30);
                if (biteCooldown <= 0) {
                    biteCooldown = 28;
                    com.cursedtides.util.Compat.swing(this, InteractionHand.MAIN_HAND);
                    if (this.doHurtTarget((net.minecraft.server.level.ServerLevel) this.level(), t)) {
                        Vec3 pull = this.position().subtract(t.position()).multiply(1, 0, 1).normalize().scale(0.45);
                        t.push(pull.x, 0.1, pull.z);
                        t.syncVelocity = true;
                        t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1));
                    }
                    this.playSound(SoundEvents.EVOKER_FANGS_ATTACK, 1f, 1.4f);
                }
            }
        }

        public static AttributeSupplier.Builder attributes() {
            return Monster.createMonsterAttributes()
                    .add(Attributes.MAX_HEALTH, 26)
                    .add(Attributes.MOVEMENT_SPEED, 0.0)
                    .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                    .add(Attributes.ATTACK_DAMAGE, 5)
                    .add(Attributes.FOLLOW_RANGE, 6);
        }
    }

    // ------------------------------------------------------------------ the living stump

    /** A stump, until you're close enough to grab. Slow, tough, and it hits like a falling tree. */
    public static class StumpEntity extends Monster {
        private boolean awake;

        public StumpEntity(EntityType<? extends StumpEntity> type, Level world) {
            super(type, world);
            this.xpReward = 10;
        }

        @Override
        protected void registerGoals() {
            this.goalSelector.addGoal(0, new FloatGoal(this));
            this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
            this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
            this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                    (p, l__) -> this.awake || p.distanceToSqr(this) < 4.5 * 4.5));
        }

        @Override
        public void tick() {
            super.tick();
            if (this.level().isClientSide()) return;
            if (!awake) {
                this.setDeltaMovement(0, Math.min(0, this.getDeltaMovement().y), 0);
                if (this.getTarget() != null) {
                    awake = true;
                    this.playSound(SoundEvents.WOOD_BREAK, 1.5f, 0.5f);
                    if (this.level() instanceof ServerLevel w) {
                        w.sendParticles(ParticleTypes.COMPOSTER, getX(), getY() + 1, getZ(), 20, 0.5, 0.8, 0.5, 0.05);
                    }
                }
            }
        }

        @Override
        public boolean hurtServer(net.minecraft.server.level.ServerLevel level__, DamageSource source, float amount) {
            awake = true;
            if (source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) amount *= 2; // dry wood
            return super.hurtServer(level__, source, amount);
        }

        @Override
        public boolean doHurtTarget(net.minecraft.server.level.ServerLevel level__, Entity target) {
            boolean hit = super.doHurtTarget((net.minecraft.server.level.ServerLevel) this.level(), target);
            if (hit && target instanceof LivingEntity l) {
                Vec3 push = l.position().subtract(this.position()).multiply(1, 0, 1).normalize();
                l.push(push.x * 0.9, 0.35, push.z * 0.9);
                l.syncVelocity = true;
            }
            return hit;
        }

        public static AttributeSupplier.Builder attributes() {
            return Monster.createMonsterAttributes()
                    .add(Attributes.MAX_HEALTH, 40)
                    .add(Attributes.ARMOR, 8)
                    .add(Attributes.MOVEMENT_SPEED, 0.2)
                    .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                    .add(Attributes.ATTACK_DAMAGE, 7);
        }
    }

    // ------------------------------------------------------------------ registration

    private static <T extends Entity> EntityType<T> type(String name, EntityType.EntityFactory<T> factory, float w, float h) {
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, CursedTides.id(name),
                EntityType.Builder.of(factory, MobCategory.MONSTER).sized(w, h).clientTrackingRange(8).build(com.cursedtides.util.Reg.entity(name)));
    }

    public static final EntityType<FlierEntity> GIANT_WASP = type("giant_wasp", (t, w) -> new FlierEntity(t, w, true), 0.8f, 0.8f);
    public static final EntityType<FlierEntity> CAVE_BAT = type("cave_bat", (t, w) -> new FlierEntity(t, w, false), 1.2f, 0.9f);
    public static final EntityType<ScorpionEntity> DUNE_SCORPION = type("dune_scorpion", ScorpionEntity::new, 1.1f, 0.7f);
    public static final EntityType<FlytrapEntity> SNAPJAW = type("snapjaw", FlytrapEntity::new, 0.9f, 1.6f);
    public static final EntityType<StumpEntity> LIVING_STUMP = type("living_stump", StumpEntity::new, 0.9f, 1.4f);

    private static Item egg(String name, EntityType<? extends Mob> type, int a, int b) {
        return Registry.register(BuiltInRegistries.ITEM, com.cursedtides.util.Reg.itemId(name + "_spawn_egg"), new SpawnEggItem(com.cursedtides.util.Reg.props().spawnEgg(type)));
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
        CreativeModeTabEvents.modifyOutputEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB, CursedTides.id("main"))).register(entries -> {
            for (Item egg : new Item[]{WASP_EGG, BAT_EGG, SCORPION_EGG, SNAPJAW_EGG, STUMP_EGG}) entries.accept(egg);
        });
    }
}
