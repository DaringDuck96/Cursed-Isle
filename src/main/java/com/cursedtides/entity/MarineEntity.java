package com.cursedtides.entity;

import com.cursedtides.faction.FactionData;
import com.cursedtides.faction.FactionEvents;
import com.cursedtides.item.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * A Tidewatch marine. Leaves law-abiding players alone, fires on wanted ones and on
 * the cursed dead, and defends itself (and its squad) when attacked.
 */
public class MarineEntity extends PathfinderMob implements RangedAttackMob {

    public MarineEntity(EntityType<? extends PathfinderMob> type, Level world) {
        super(type, world);
    }

    public static AttributeSupplier.Builder createMarineAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.ARMOR, 3.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 40, 20.0f));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.6, 240));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(MarineEntity.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                (target, l__) -> target instanceof Player p && !p.isCreative() && FactionData.isWanted(p)));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, CursedPirateEntity.class, true));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, EntitySpawnReason spawnReason,
                                 @Nullable SpawnGroupData entityData) {
        SpawnGroupData data = super.finalizeSpawn(world, difficulty, spawnReason, entityData);
        this.populateDefaultEquipmentSlots(world.getRandom(), difficulty);
        return data;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance localDifficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.MUSKET));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.04f);
    }

    /** One musket ball: blocked by walls, hits about two times in three. */
    @Override
    public void performRangedAttack(LivingEntity target, float pullProgress) {
        if (!(this.level() instanceof ServerLevel world)) {
            return;
        }
        Vec3 start = this.getEyePosition();
        Vec3 end = target.position().add(0, target.getBbHeight() * 0.6, 0);
        BlockHitResult block = world.clip(new ClipContext(start, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        boolean clear = block.getType() == HitResult.Type.MISS;
        if (clear && this.random.nextFloat() < 0.65f) {
            target.hurt(this.damageSources().mobAttack(this), 6.0f);
        }
        Vec3 dir = end.subtract(start).normalize();
        Vec3 muzzle = start.add(dir.scale(0.9));
        world.sendParticles(ParticleTypes.LARGE_SMOKE, muzzle.x, muzzle.y, muzzle.z, 4, 0.05, 0.05, 0.05, 0.01);
        world.sendParticles(ParticleTypes.FLAME, muzzle.x, muzzle.y, muzzle.z, 2, 0.03, 0.03, 0.03, 0.01);
        world.playSound(null, getX(), getY(), getZ(), SoundEvents.FIREWORK_ROCKET_LARGE_BLAST,
                SoundSource.HOSTILE, 1.0f, 0.65f);
    }

    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel level__, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level__, source, amount);
        if (hurt && source.getEntity() instanceof ServerPlayer player && !FactionData.isWanted(player)) {
            FactionEvents.onAttackedNavy(player);
        }
        return hurt;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PILLAGER_DEATH;
    }
}
