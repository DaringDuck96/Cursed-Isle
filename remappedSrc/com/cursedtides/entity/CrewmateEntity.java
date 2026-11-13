package com.cursedtides.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * A hired hand who sits aboard your ship and does a job: gunner, bosun or navigator.
 * The ship reads their roles each tick. Sneak-right-click as the owner to dismiss.
 */
public class CrewmateEntity extends PathfinderMob {
    public static final int GUNNER = 0, BOSUN = 1, NAVIGATOR = 2;
    public static final String[] ROLE_NAMES = {"Gunner", "Bosun", "Navigator"};
    private static final String[] FIRST = {"Ned", "Bree", "Obi", "Wil", "Rosa", "Fenn", "Ivo", "Kit", "Lark", "Moss", "Pip", "Juno"};
    private static final String[] LAST = {"Brine", "Tarr", "Gale", "Hook", "Salt", "Rudd", "Keel", "Rook", "Shoal", "Cray"};

    private static final EntityDataAccessor<Integer> ROLE = SynchedEntityData.defineId(CrewmateEntity.class, EntityDataSerializers.INT);

    public CrewmateEntity(EntityType<? extends PathfinderMob> type, Level world) {
        super(type, world);
    }

    public static AttributeSupplier.Builder createCrewAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ROLE, 0);
    }

    public int getRole() {
        return Mth.clamp(this.entityData.get(ROLE), 0, 2);
    }

    public void setRole(int role) {
        this.entityData.set(ROLE, role);
        String name = FIRST[this.random.nextInt(FIRST.length)] + " " + LAST[this.random.nextInt(LAST.length)];
        this.setCustomName(Component.literal(name + ", " + ROLE_NAMES[getRole()]).withStyle(ChatFormatting.AQUA));
        this.setCustomNameVisible(true);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND && player.isShiftKeyDown() && this.getVehicle() instanceof ShipEntity ship
                && ship.isOwner(player) && ship.getOwner() != null) {
            if (!this.level().isClientSide) {
                player.displayClientMessage(Component.literal("Dismissed: ").append(this.getName()).withStyle(ChatFormatting.GRAY), false);
                this.discard();
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("CrewRole", getRole());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        this.entityData.set(ROLE, nbt.getInt("CrewRole"));
    }
}
