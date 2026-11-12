package com.cursedtides.entity;

import com.cursedtides.item.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

/**
 * Captain Ashmere Thorne, final boss of "The Drowned Bell". Immune to sunlight,
 * shows a boss bar, and calls up his crew once he's badly hurt.
 */
public class DrownedCaptainEntity extends CursedPirateEntity {
    public static final Text NAME = Text.literal("Captain Ashmere Thorne").formatted(Formatting.DARK_AQUA);

    private final ServerBossBar bossBar = (ServerBossBar) new ServerBossBar(NAME, BossBar.Color.BLUE, BossBar.Style.NOTCHED_10)
            .setDarkenSky(true);
    private boolean calledCrew = false;

    public DrownedCaptainEntity(EntityType<? extends SkeletonEntity> type, World world) {
        super(type, world);
        this.setCustomName(NAME);
        this.experiencePoints = 80;
    }

    public static DefaultAttributeContainer.Builder createCaptainAttributes() {
        return AbstractSkeletonEntity.createAbstractSkeletonAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 220.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 9.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_ARMOR, 10.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.7)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
        this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(ModItems.CUTLASS));
        this.equipStack(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
        this.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.0f);
        this.setEquipmentDropChance(EquipmentSlot.CHEST, 0.0f);
    }

    @Override
    protected boolean isAffectedByDaylight() {
        return false;
    }

    @Override
    public boolean canSpawn(WorldAccess world, SpawnReason reason) {
        return reason != SpawnReason.NATURAL && reason != SpawnReason.CHUNK_GENERATION;
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        bossBar.setPercent(this.getHealth() / this.getMaxHealth());
        if (!calledCrew && this.getHealth() < this.getMaxHealth() / 2 && this.getWorld() instanceof ServerWorld world) {
            calledCrew = true;
            callCrew(world, this, 3);
            world.playSound(null, getX(), getY(), getZ(), SoundEvents.BLOCK_BELL_RESONATE, SoundCategory.HOSTILE, 2.0f, 0.5f);
            for (ServerPlayerEntity p : bossBar.getPlayers()) {
                p.sendMessage(Text.literal("Thorne strikes his half-bell. The dead answer!").formatted(Formatting.DARK_AQUA), false);
            }
        }
    }

    /** Raises cursed pirates in a ring around the captain. */
    public static void callCrew(ServerWorld world, CursedPirateEntity around, int count) {
        for (int i = 0; i < count; i++) {
            double a = Math.PI * 2 * i / count;
            CursedPirateEntity mate = ModEntities.CURSED_PIRATE.create(world);
            if (mate == null) continue;
            mate.refreshPositionAndAngles(around.getX() + Math.cos(a) * 3, around.getY(), around.getZ() + Math.sin(a) * 3, 0, 0);
            mate.initialize(world, world.getLocalDifficulty(mate.getBlockPos()), SpawnReason.EVENT, null);
            world.spawnEntity(mate);
        }
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        bossBar.addPlayer(player);
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        bossBar.removePlayer(player);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("CalledCrew", calledCrew);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        calledCrew = nbt.getBoolean("CalledCrew");
    }
}
