package com.cursedtides.entity;

import com.cursedtides.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * A cursed skeleton deckhand. Fights up close with a cutlass (holding a non-bow item
 * makes skeleton AI switch to melee), burns in daylight like any undead.
 */
public class CursedPirateEntity extends Skeleton {
    /** What the sea made of them, by the island they rose on. */
    public enum Kind { DECKHAND("Cursed"), BONE("Bone"), BARNACLE("Barnacle"), EMBER("Ember");
        public final String word; Kind(String word) { this.word = word; } }
    /** Their place in the dead crew. */
    public enum Rank { GRUNT("Grunt"), BRUTE("Brute"), CAPTAIN("Captain");
        public final String word; Rank(String word) { this.word = word; } }

    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> VARIANT =
            net.minecraft.network.syncher.SynchedEntityData.defineId(CursedPirateEntity.class, net.minecraft.network.syncher.EntityDataSerializers.INT);

    public CursedPirateEntity(EntityType<? extends Skeleton> type, Level world) {
        super(type, world);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, 0);
    }

    public Kind kind() { return Kind.values()[Math.floorMod(this.entityData.get(VARIANT) & 0xF, Kind.values().length)]; }
    public Rank rank() { return Rank.values()[Math.floorMod(this.entityData.get(VARIANT) >> 4, Rank.values().length)]; }

    /** "Barnacle Brute", "Ember Captain", "Cursed Grunt". */
    public net.minecraft.network.chat.Component title() {
        return net.minecraft.network.chat.Component.literal(kind().word + " " + rank().word);
    }

    public void setVariant(Kind kind, Rank rank) {
        this.entityData.set(VARIANT, kind.ordinal() | (rank.ordinal() << 4));
        var scale = this.getAttribute(Attributes.SCALE);
        var hp = this.getAttribute(Attributes.MAX_HEALTH);
        var dmg = this.getAttribute(Attributes.ATTACK_DAMAGE);
        var speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (rank == Rank.BRUTE) {
            if (scale != null) scale.setBaseValue(1.3);
            if (hp != null) hp.setBaseValue(42);
            if (dmg != null) dmg.setBaseValue(5);
            if (speed != null) speed.setBaseValue(0.25);
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(this.random.nextBoolean() ? ModItems.BOARDING_AXE : ModItems.BROADSWORD));
        } else if (rank == Rank.CAPTAIN) {
            if (scale != null) scale.setBaseValue(1.1);
            if (hp != null) hp.setBaseValue(36);
            if (dmg != null) dmg.setBaseValue(4);
            // (the captain's coat and tricorn are part of his model)
        }
        this.setDropChance(EquipmentSlot.MAINHAND, 0.03f);
        this.setHealth(this.getMaxHealth());
    }

    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor world, DifficultyInstance difficulty,
                                                     MobSpawnType reason, net.minecraft.world.entity.SpawnGroupData data) {
        net.minecraft.world.entity.SpawnGroupData out = super.finalizeSpawn(world, difficulty, reason, data);
        if (this.getClass() != CursedPirateEntity.class) return out; // bosses built on this (Thorne) set themselves up
        // The island decides what they're made of; the dice decide their rank
        com.cursedtides.world.Archipelago.Column col = com.cursedtides.world.Archipelago.column(this.getBlockX(), this.getBlockZ());
        Kind kind = Kind.DECKHAND;
        if (col.island() != null) {
            kind = switch (col.island().style()) {
                case CURSED, ANCIENT, CLIFFS, CRAGS -> Kind.BONE;
                case REEF, KEYS, ATOLL, BAYOU, SANDY -> Kind.BARNACLE;
                case VOLCANIC, DESERT -> Kind.EMBER;
                default -> Kind.DECKHAND;
            };
        }
        float roll = this.random.nextFloat();
        setVariant(kind, roll < 0.07f ? Rank.CAPTAIN : roll < 0.25f ? Rank.BRUTE : Rank.GRUNT);
        return out;
    }

    @Override
    public void tick() {
        super.tick();
        // A captain rallies the dead around him
        if (!this.level().isClientSide && rank() == Rank.CAPTAIN && this.tickCount % 40 == 0 && this.getTarget() != null) {
            for (CursedPirateEntity mate : this.level().getEntitiesOfClass(CursedPirateEntity.class, this.getBoundingBox().inflate(8), m -> m != this)) {
                mate.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, 60, 0));
            }
        }
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("Variant", this.entityData.get(VARIANT));
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        this.entityData.set(VARIANT, nbt.getInt("Variant"));
    }

    public static AttributeSupplier.Builder createPirateAttributes() {
        return AbstractSkeleton.createAttributes()
                .add(Attributes.MAX_HEALTH, 26.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ARMOR, 2.0);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance localDifficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.CUTLASS));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.05f);
    }

    /**
     * Natural spawns: dark, solid ground, not in water. Done here instead of a
     * spawn-restriction registration so it works without access wideners.
     */
    @Override
    public boolean checkSpawnRules(LevelAccessor world, MobSpawnType reason) {
        if (reason != MobSpawnType.NATURAL && reason != MobSpawnType.CHUNK_GENERATION) {
            return super.checkSpawnRules(world, reason);
        }
        if (world.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        BlockPos pos = this.blockPosition();
        BlockPos below = pos.below();
        if (!world.getFluidState(pos).isEmpty()) {
            return false;
        }
        if (!world.getBlockState(below).isRedstoneConductor(world, below)) {
            return false;
        }
        return world instanceof ServerLevelAccessor serverWorld
                && Monster.isDarkEnoughToSpawn(serverWorld, pos, this.random);
    }
}
