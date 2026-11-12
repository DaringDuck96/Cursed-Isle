package com.cursedtides.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * A hired hand who sits aboard your ship and does a job: gunner, bosun or navigator.
 * The ship reads their roles each tick. Sneak-right-click as the owner to dismiss.
 */
public class CrewmateEntity extends PathAwareEntity {
    public static final int GUNNER = 0, BOSUN = 1, NAVIGATOR = 2;
    public static final String[] ROLE_NAMES = {"Gunner", "Bosun", "Navigator"};
    private static final String[] FIRST = {"Ned", "Bree", "Obi", "Wil", "Rosa", "Fenn", "Ivo", "Kit", "Lark", "Moss", "Pip", "Juno"};
    private static final String[] LAST = {"Brine", "Tarr", "Gale", "Hook", "Salt", "Rudd", "Keel", "Rook", "Shoal", "Cray"};

    private static final TrackedData<Integer> ROLE = DataTracker.registerData(CrewmateEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public CrewmateEntity(EntityType<? extends PathAwareEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createCrewAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(ROLE, 0);
    }

    public int getRole() {
        return MathHelper.clamp(this.dataTracker.get(ROLE), 0, 2);
    }

    public void setRole(int role) {
        this.dataTracker.set(ROLE, role);
        String name = FIRST[this.random.nextInt(FIRST.length)] + " " + LAST[this.random.nextInt(LAST.length)];
        this.setCustomName(Text.literal(name + ", " + ROLE_NAMES[getRole()]).formatted(Formatting.AQUA));
        this.setCustomNameVisible(true);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(2, new LookAroundGoal(this));
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (hand == Hand.MAIN_HAND && player.isSneaking() && this.getVehicle() instanceof ShipEntity ship
                && ship.isOwner(player) && ship.getOwner() != null) {
            if (!this.getWorld().isClient) {
                player.sendMessage(Text.literal("Dismissed: ").append(this.getName()).formatted(Formatting.GRAY), false);
                this.discard();
            }
            return ActionResult.success(this.getWorld().isClient);
        }
        return super.interactMob(player, hand);
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("CrewRole", getRole());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dataTracker.set(ROLE, nbt.getInt("CrewRole"));
    }
}
