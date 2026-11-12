package com.cursedtides.wildlife;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/** A jungle monkey. Feed it melon or cocoa until it trusts you, and it becomes your companion. */
public class MonkeyEntity extends TameableEntity {
    public MonkeyEntity(EntityType<? extends MonkeyEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createMonkeyAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new EscapeDangerGoal(this, 1.5));
        this.goalSelector.add(2, new SitGoal(this));
        this.goalSelector.add(3, new FollowOwnerGoal(this, 1.1, 8.0f, 2.0f));
        this.goalSelector.add(6, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(8, new LookAroundGoal(this));
    }

    private static boolean isTreat(ItemStack stack) {
        return stack.isOf(Items.MELON_SLICE) || stack.isOf(Items.COCOA_BEANS) || stack.isOf(Items.SWEET_BERRIES);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        boolean client = this.getWorld().isClient;
        if (!this.isTamed() && isTreat(stack)) {
            stack.decrementUnlessCreative(1, player);
            if (!client) {
                if (this.random.nextInt(3) == 0) {
                    this.setOwner(player);
                    this.setPersistent();
                    this.navigation.stop();
                    this.setSitting(true);
                    this.getWorld().sendEntityStatus(this, (byte) 7);
                } else {
                    this.getWorld().sendEntityStatus(this, (byte) 6);
                }
            }
            return ActionResult.success(client);
        }
        if (this.isTamed() && this.isOwner(player) && hand == Hand.MAIN_HAND) {
            if (isTreat(stack) && this.getHealth() < this.getMaxHealth()) {
                stack.decrementUnlessCreative(1, player);
                this.heal(4.0f);
                return ActionResult.success(client);
            }
            if (!client) {
                this.setSitting(!this.isSitting());
                this.jumping = false;
                this.navigation.stop();
            }
            return ActionResult.success(client);
        }
        return super.interactMob(player, hand);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return false;
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }
}
