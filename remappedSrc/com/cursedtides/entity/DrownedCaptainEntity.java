package com.cursedtides.entity;

import com.cursedtides.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

/**
 * Captain Ashmere Thorne, final boss of "The Drowned Bell". Immune to sunlight,
 * shows a boss bar, and calls up his crew once he's badly hurt.
 */
public class DrownedCaptainEntity extends CursedPirateEntity {
    public static final Component NAME = Component.literal("Captain Ashmere Thorne").withStyle(ChatFormatting.DARK_AQUA);

    private final ServerBossEvent bossBar = (ServerBossEvent) new ServerBossEvent(NAME, BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_10)
            .setDarkenScreen(true);
    private boolean calledCrew = false;

    public DrownedCaptainEntity(EntityType<? extends Skeleton> type, Level world) {
        super(type, world);
        this.setCustomName(NAME);
        this.xpReward = 80;
    }

    public static AttributeSupplier.Builder createCaptainAttributes() {
        return AbstractSkeleton.createAttributes()
                .add(Attributes.MAX_HEALTH, 220.0)
                .add(Attributes.ATTACK_DAMAGE, 9.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance localDifficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.CUTLASS));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0f);
        this.setDropChance(EquipmentSlot.CHEST, 0.0f);
    }

    @Override
    protected boolean isSunBurnTick() {
        return false;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor world, MobSpawnType reason) {
        return reason != MobSpawnType.NATURAL && reason != MobSpawnType.CHUNK_GENERATION;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        bossBar.setProgress(this.getHealth() / this.getMaxHealth());
        if (!calledCrew && this.getHealth() < this.getMaxHealth() / 2 && this.level() instanceof ServerLevel world) {
            calledCrew = true;
            callCrew(world, this, 3);
            world.playSound(null, getX(), getY(), getZ(), SoundEvents.BELL_RESONATE, SoundSource.HOSTILE, 2.0f, 0.5f);
            for (ServerPlayer p : bossBar.getPlayers()) {
                p.displayClientMessage(Component.literal("Thorne strikes his half-bell. The dead answer!").withStyle(ChatFormatting.DARK_AQUA), false);
            }
        }
    }

    /** Raises cursed pirates in a ring around the captain. */
    public static void callCrew(ServerLevel world, CursedPirateEntity around, int count) {
        for (int i = 0; i < count; i++) {
            double a = Math.PI * 2 * i / count;
            CursedPirateEntity mate = ModEntities.CURSED_PIRATE.create(world);
            if (mate == null) continue;
            mate.moveTo(around.getX() + Math.cos(a) * 3, around.getY(), around.getZ() + Math.sin(a) * 3, 0, 0);
            mate.finalizeSpawn(world, world.getCurrentDifficultyAt(mate.blockPosition()), MobSpawnType.EVENT, null);
            world.addFreshEntity(mate);
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossBar.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossBar.removePlayer(player);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("CalledCrew", calledCrew);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        calledCrew = nbt.getBoolean("CalledCrew");
    }
}
