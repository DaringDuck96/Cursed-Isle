package com.cursedtides.entity;

import com.cursedtides.item.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

/**
 * A cursed skeleton deckhand. Fights up close with a cutlass (holding a non-bow item
 * makes skeleton AI switch to melee), burns in daylight like any undead.
 */
public class CursedPirateEntity extends SkeletonEntity {
    /** What the sea made of them, by the island they rose on. */
    public enum Kind { DECKHAND("Cursed"), BONE("Bone"), BARNACLE("Barnacle"), EMBER("Ember");
        public final String word; Kind(String word) { this.word = word; } }
    /** Their place in the dead crew. */
    public enum Rank { GRUNT("Grunt"), BRUTE("Brute"), CAPTAIN("Captain");
        public final String word; Rank(String word) { this.word = word; } }

    private static final net.minecraft.entity.data.TrackedData<Integer> VARIANT =
            net.minecraft.entity.data.DataTracker.registerData(CursedPirateEntity.class, net.minecraft.entity.data.TrackedDataHandlerRegistry.INTEGER);

    public CursedPirateEntity(EntityType<? extends SkeletonEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected void initDataTracker(net.minecraft.entity.data.DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(VARIANT, 0);
    }

    public Kind kind() { return Kind.values()[Math.floorMod(this.dataTracker.get(VARIANT) & 0xF, Kind.values().length)]; }
    public Rank rank() { return Rank.values()[Math.floorMod(this.dataTracker.get(VARIANT) >> 4, Rank.values().length)]; }

    /** "Barnacle Brute", "Ember Captain", "Cursed Grunt". */
    public net.minecraft.text.Text title() {
        return net.minecraft.text.Text.literal(kind().word + " " + rank().word);
    }

    public void setVariant(Kind kind, Rank rank) {
        this.dataTracker.set(VARIANT, kind.ordinal() | (rank.ordinal() << 4));
        var scale = this.getAttributeInstance(EntityAttributes.GENERIC_SCALE);
        var hp = this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        var dmg = this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        var speed = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (rank == Rank.BRUTE) {
            if (scale != null) scale.setBaseValue(1.3);
            if (hp != null) hp.setBaseValue(42);
            if (dmg != null) dmg.setBaseValue(5);
            if (speed != null) speed.setBaseValue(0.25);
            this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(this.random.nextBoolean() ? ModItems.BOARDING_AXE : ModItems.BROADSWORD));
        } else if (rank == Rank.CAPTAIN) {
            if (scale != null) scale.setBaseValue(1.1);
            if (hp != null) hp.setBaseValue(36);
            if (dmg != null) dmg.setBaseValue(4);
            // (the captain's coat and tricorn are part of his model)
        }
        this.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.03f);
        this.setHealth(this.getMaxHealth());
    }

    @Override
    public net.minecraft.entity.EntityData initialize(net.minecraft.world.ServerWorldAccess world, LocalDifficulty difficulty,
                                                     SpawnReason reason, net.minecraft.entity.EntityData data) {
        net.minecraft.entity.EntityData out = super.initialize(world, difficulty, reason, data);
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
        if (!this.getWorld().isClient && rank() == Rank.CAPTAIN && this.age % 40 == 0 && this.getTarget() != null) {
            for (CursedPirateEntity mate : this.getWorld().getEntitiesByClass(CursedPirateEntity.class, this.getBoundingBox().expand(8), m -> m != this)) {
                mate.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.STRENGTH, 60, 0));
            }
        }
    }

    @Override
    public void writeCustomDataToNbt(net.minecraft.nbt.NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Variant", this.dataTracker.get(VARIANT));
    }

    @Override
    public void readCustomDataFromNbt(net.minecraft.nbt.NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dataTracker.set(VARIANT, nbt.getInt("Variant"));
    }

    public static DefaultAttributeContainer.Builder createPirateAttributes() {
        return AbstractSkeletonEntity.createAbstractSkeletonAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 26.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.28)
                .add(EntityAttributes.GENERIC_ARMOR, 2.0);
    }

    @Override
    protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
        this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(ModItems.CUTLASS));
        this.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.05f);
    }

    /**
     * Natural spawns: dark, solid ground, not in water. Done here instead of a
     * spawn-restriction registration so it works without access wideners.
     */
    @Override
    public boolean canSpawn(WorldAccess world, SpawnReason reason) {
        if (reason != SpawnReason.NATURAL && reason != SpawnReason.CHUNK_GENERATION) {
            return super.canSpawn(world, reason);
        }
        if (world.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        BlockPos pos = this.getBlockPos();
        BlockPos below = pos.down();
        if (!world.getFluidState(pos).isEmpty()) {
            return false;
        }
        if (!world.getBlockState(below).isSolidBlock(world, below)) {
            return false;
        }
        return world instanceof ServerWorldAccess serverWorld
                && HostileEntity.isSpawnDark(serverWorld, pos, this.random);
    }
}
