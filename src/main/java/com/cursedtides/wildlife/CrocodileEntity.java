package com.cursedtides.wildlife;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Lies still in the shallows and lunges at anything that comes close. Bayou ones are alligators. */
public class CrocodileEntity extends Monster {
    private static final EntityDataAccessor<Boolean> ALLIGATOR = SynchedEntityData.defineId(CrocodileEntity.class, EntityDataSerializers.BOOLEAN);

    public CrocodileEntity(EntityType<? extends CrocodileEntity> type, Level world) {
        super(type, world);
    }

    public static AttributeSupplier.Builder createCrocAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ALLIGATOR, false);
    }

    public boolean isAlligator() { return this.entityData.get(ALLIGATOR); }
    public void setAlligator(boolean value) { this.entityData.set(ALLIGATOR, value); }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.3, false));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // An ambush hunter: only strikes at what wanders within ten blocks
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                (p, l__) -> p.distanceToSqr(this) < 100));
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput out__) {
        CompoundTag nbt = new CompoundTag();
        super.addAdditionalSaveData(out__);
        nbt.putBoolean("Alligator", isAlligator());
        out__.store("ct", CompoundTag.CODEC, nbt);
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput in__) {
        CompoundTag nbt = in__.read("ct", CompoundTag.CODEC).orElseGet(CompoundTag::new);
        super.readAdditionalSaveData(in__);
        setAlligator(nbt.getBooleanOr("Alligator", false));
    }
}
