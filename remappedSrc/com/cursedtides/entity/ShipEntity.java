package com.cursedtides.entity;

import com.cursedtides.faction.FactionData;
import com.cursedtides.faction.FactionEvents;
import com.cursedtides.item.CrewArticlesItem;
import com.cursedtides.item.ModItems;
import com.cursedtides.item.ShipUpgradeItem;
import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import com.cursedtides.ship.ShipAmmo;
import com.cursedtides.ship.ShipClass;
import com.cursedtides.ship.ShipFaction;
import com.cursedtides.ship.Wind;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * An ownable sailing ship on top of vanilla boat physics: hull health, sails that catch the
 * world wind (and suffer in storms), a cargo hold, crew seats and hired hands, upgrades,
 * dyed sails, four kinds of shot, repairs, sinking and capture. Ghost ships and Tidewatch
 * patrols use the same class and steer themselves.
 */
public class ShipEntity extends Boat {
    private static final EntityDataAccessor<Float> HULL = SynchedEntityData.defineId(ShipEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> SAIL = SynchedEntityData.defineId(ShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CLASS = SynchedEntityData.defineId(ShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FACTION = SynchedEntityData.defineId(ShipEntity.class, EntityDataSerializers.INT);
    /** 0 = merchant timber, 1 = ghost, 2 = Tidewatch colours. Kept when a ship is captured. */
    private static final EntityDataAccessor<Integer> LOOK = SynchedEntityData.defineId(ShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SAIL_SKILL = SynchedEntityData.defineId(ShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> OWNER = SynchedEntityData.defineId(ShipEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    /** Packed upgrade levels: 2 bits each for plating, guns, canvas, hold. */
    private static final EntityDataAccessor<Integer> UPGRADES = SynchedEntityData.defineId(ShipEntity.class, EntityDataSerializers.INT);
    /** RGB sail tint, or -1 for natural canvas. */
    private static final EntityDataAccessor<Integer> SAIL_COLOR = SynchedEntityData.defineId(ShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FIGUREHEAD = SynchedEntityData.defineId(ShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> EMBLEM = SynchedEntityData.defineId(ShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> AMMO = SynchedEntityData.defineId(ShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> NAVIGATORS = SynchedEntityData.defineId(ShipEntity.class, EntityDataSerializers.INT);
    /** Ticks of torn rigging left (from chain shot or lightning). */
    private static final EntityDataAccessor<Integer> TORN = SynchedEntityData.defineId(ShipEntity.class, EntityDataSerializers.INT);
    /** Which flag flies from the masthead (0 = the plain pennant). */
    private static final EntityDataAccessor<Integer> FLAG = SynchedEntityData.defineId(ShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ANCHORED = SynchedEntityData.defineId(ShipEntity.class, EntityDataSerializers.BOOLEAN);
    /** 0 regular, 1 light (fast, fragile, fewer guns), 2 war (slow, tough, more guns), as the old game's ships came. */
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(ShipEntity.class, EntityDataSerializers.INT);
    public static final int REGULAR = 0, LIGHT = 1, WAR = 2;

    public static final int LOOK_PLAIN = 0, LOOK_GHOST = 1, LOOK_NAVY = 2;

    private final SimpleContainer cargo = new SimpleContainer(54) {
        @Override
        public boolean stillValid(Player player) {
            return !ShipEntity.this.isRemoved() && player.distanceToSqr(ShipEntity.this) < 100.0;
        }
    };

    private int portCooldown;
    private int starboardCooldown;
    private int aiFireCooldown = 60;
    private int gunnerCooldown;
    @Nullable private UUID lastAttacker;
    @Nullable private Vec3 lastServerPos;
    private double speedBlocksPerSecond;
    private int lonelyTicks;
    @Nullable private UUID provoker;
    private int provokedTicks;
    private float patrolYaw = Float.NaN;
    @Nullable private Entity cachedTarget;
    private long lastWageDay = -1;
    /** Crew roles waiting to come aboard after launching from a deed. */
    private int[] pendingCrew = new int[0];

    public ShipEntity(EntityType<? extends Boat> type, Level world) {
        super(type, world);
        this.setCustomNameVisible(true);
    }

    // ================================================================== data

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HULL, (float) ShipClass.SLOOP.maxHull());
        builder.define(VARIANT, 0);
        builder.define(SAIL, 0);
        builder.define(CLASS, 0);
        builder.define(FACTION, 0);
        builder.define(LOOK, 0);
        builder.define(SAIL_SKILL, 0);
        builder.define(OWNER, Optional.empty());
        builder.define(UPGRADES, 0);
        builder.define(SAIL_COLOR, -1);
        builder.define(FIGUREHEAD, 0);
        builder.define(EMBLEM, 0);
        builder.define(AMMO, 0);
        builder.define(NAVIGATORS, 0);
        builder.define(TORN, 0);
        builder.define(FLAG, 0);
        builder.define(ANCHORED, false);
    }

    public com.cursedtides.ship.FigureheadItem.Kind getFigurehead() {
        return com.cursedtides.ship.FigureheadItem.Kind.byIndex(this.entityData.get(FIGUREHEAD));
    }
    public void setFigurehead(com.cursedtides.ship.FigureheadItem.Kind kind) { this.entityData.set(FIGUREHEAD, kind.ordinal()); }
    public int getFlag() { return this.entityData.get(FLAG); }
    /** Flag design painted on the mainsail, plus one; 0 for plain canvas. */
    public int getEmblem() { return this.entityData.get(EMBLEM); }
    /** Spotted by a sea eagle: glows for ten seconds. */
    public void addStatusEffectGlow() {
        this.setGlowingTag(true);
        spottedUntil = this.tickCount + 200;
    }
    private int spottedUntil = -1;
    public void setFlag(int flag) { this.entityData.set(FLAG, flag); }
    public boolean isAnchored() { return this.entityData.get(ANCHORED); }
    public void setAnchored(boolean anchored) { this.entityData.set(ANCHORED, anchored); }

    private int lastSwivel = -100;

    /** A named legend of the deep: armoured hull, extra guns, and half damage from everything. */
    public void makeLegendary(String name) {
        setShipClass(ShipClass.GALLEON);
        setUpgrade(ShipUpgradeItem.Kind.PLATING, 3);
        setUpgrade(ShipUpgradeItem.Kind.GUNS, 2);
        setHull(maxHull());
        setFlag(com.cursedtides.ship.FlagItem.Design.KRAKEN.ordinal());
        setCustomName(Component.literal(name).withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD));
        refreshDimensions();
        addTag("cursedtides_legendary");
    }

    /** Drops or weighs anchor. Only the owner (or crew) may. */
    public void toggleAnchor(ServerPlayer player) {
        boolean down = !isAnchored();
        setAnchored(down);
        if (down) setSail(0);
        this.level().playSound(null, getX(), getY(), getZ(), down ? SoundEvents.CHAIN_PLACE : SoundEvents.CHAIN_BREAK,
                SoundSource.PLAYERS, 1.2f, 0.6f);
        player.displayClientMessage(Component.literal(down ? "Anchor's down. She'll hold here." : "Anchor's aweigh!").withStyle(ChatFormatting.GRAY), true);
    }

    /** A rail-mounted swivel gun: a cone of grapeshot along the gunner's aim. Uses one grapeshot. */
    public void fireSwivel(ServerPlayer gunner) {
        if (upgrade(ShipUpgradeItem.Kind.SWIVEL) == 0) {
            gunner.displayClientMessage(Component.literal("No swivel gun is mounted. A shipwright sells them.").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        if (this.tickCount - lastSwivel < 30) return;
        boolean paid = false;
        if (gunner.getAbilities().instabuild) paid = true;
        for (net.minecraft.world.Container inv : new net.minecraft.world.Container[]{gunner.getInventory(), cargo}) {
            for (int i = 0; i < inv.getContainerSize() && !paid; i++) {
                if (inv.getItem(i).is(ModItems.GRAPESHOT)) {
                    inv.getItem(i).shrink(1);
                    paid = true;
                }
            }
        }
        if (!paid) {
            gunner.displayClientMessage(Component.literal("The swivel needs grapeshot (in your pack or the hold).").withStyle(ChatFormatting.RED), true);
            return;
        }
        lastSwivel = this.tickCount;
        ServerLevel world = (ServerLevel) this.level();
        net.minecraft.world.phys.Vec3 eye = gunner.getEyePosition();
        net.minecraft.world.phys.Vec3 look = gunner.getViewVector(1.0f);
        world.playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.8f, 1.6f);
        for (int i = 1; i < 14; i++) {
            net.minecraft.world.phys.Vec3 p = eye.add(look.scale(i * 2.0));
            world.sendParticles(ParticleTypes.SMOKE, p.x, p.y, p.z, 2, 0.3 * i / 6, 0.2, 0.3 * i / 6, 0.01);
        }
        var box = gunner.getBoundingBox().expandTowards(look.scale(28)).inflate(6);
        for (Entity e : world.getEntities(gunner, box, e -> e.isAlive() && e != this && !this.hasPassenger(e))) {
            net.minecraft.world.phys.Vec3 to = e.getBoundingBox().getCenter().subtract(eye);
            double dist = to.length();
            if (dist > 28 || dist < 0.5) continue;
            double angle = Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, to.normalize().dot(look)))));
            if (angle > 20) continue;
            if (e instanceof Player other && com.cursedtides.social.CrewState.shipmates(world.getServer(), gunner.getUUID(), other.getUUID())) continue;
            if (!(e instanceof ShipEntity) && !gunner.hasLineOfSight(e)) continue;
            float dmg = angle < 10 ? 7.0f : 4.0f;
            if (dist > 18) dmg *= 0.6f;
            e.hurt(world.damageSources().playerAttack(gunner), e instanceof ShipEntity ? dmg * 1.5f : dmg);
        }
        com.cursedtides.pirate.PirateData.addXp(gunner, com.cursedtides.pirate.Weapon.CANNON, 2);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
        super.onSyncedDataUpdated(data);
        if (CLASS.equals(data)) {
            this.refreshDimensions();
        }
    }

    public ShipClass shipClass() { return ShipClass.byIndex(this.entityData.get(CLASS)); }
    public void setShipClass(ShipClass cls) { this.entityData.set(CLASS, cls.ordinal()); }
    public float getHull() { return this.entityData.get(HULL); }
    public void setHull(float hull) { this.entityData.set(HULL, Mth.clamp(hull, 0.0f, maxHull())); }

    /** Damage from the sea itself (whirlpools, reefs, running aground). Tells the crew and sinks her if it must. */
    public void seaDamage(ServerLevel world, float amount, String message) {
        setHull(getHull() - amount);
        for (Entity passenger : this.getPassengers()) passenger.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.YELLOW));
        world.playSound(null, blockPosition(), net.minecraft.sounds.SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, net.minecraft.sounds.SoundSource.NEUTRAL, 1.0f, 0.6f);
        if (getHull() <= 0) sink(world);
    }
    public int getSail() { return this.entityData.get(SAIL); }
    public void setSail(int sail) { this.entityData.set(SAIL, Mth.clamp(sail, 0, 2)); }
    public ShipFaction getFaction() { return ShipFaction.byIndex(this.entityData.get(FACTION)); }
    public void setFaction(ShipFaction faction) { this.entityData.set(FACTION, faction.ordinal()); }
    /** Any self-steering ship (ghost or navy). */
    public boolean isHostile() { return getFaction() != ShipFaction.PLAYER; }
    public int getLook() { return this.entityData.get(LOOK); }
    public void setLook(int look) { this.entityData.set(LOOK, look); }
    public boolean isGhost() { return getLook() == LOOK_GHOST; }
    public int getSailSkill() { return this.entityData.get(SAIL_SKILL); }
    @Nullable public UUID getOwner() { return this.entityData.get(OWNER).orElse(null); }
    public void setOwner(@Nullable UUID owner) { this.entityData.set(OWNER, Optional.ofNullable(owner)); }
    public int getSailColor() { return this.entityData.get(SAIL_COLOR); }
    public ShipAmmo getAmmo() { return ShipAmmo.byIndex(this.entityData.get(AMMO)); }
    public boolean isTorn() { return this.entityData.get(TORN) > 0; }
    public SimpleContainer cargo() { return cargo; }

    public void tearRigging(int ticks) {
        this.entityData.set(TORN, Math.max(this.entityData.get(TORN), ticks));
        if (getSail() > 1) setSail(1);
    }

    // ---- upgrades: 2 bits each
    public int upgrade(ShipUpgradeItem.Kind kind) {
        return (this.entityData.get(UPGRADES) >> (kind.ordinal() * 2)) & 3;
    }

    private void setUpgrade(ShipUpgradeItem.Kind kind, int level) {
        int shift = kind.ordinal() * 2;
        int packed = this.entityData.get(UPGRADES) & ~(3 << shift);
        this.entityData.set(UPGRADES, packed | ((level & 3) << shift));
    }

    public int getRefit() { return this.entityData.get(VARIANT); }

    public void setRefit(int v) {
        float frac = getHull() / Math.max(1, maxHull());
        this.entityData.set(VARIANT, Math.floorMod(v, 3));
        setHull(frac * maxHull());
    }

    private float variantHull() { return getRefit() == LIGHT ? 0.8f : getRefit() == WAR ? 1.25f : 1.0f; }

    public int maxHull() {
        return Math.round(shipClass().maxHull() * variantHull() * (1.0f + 0.3f * upgrade(ShipUpgradeItem.Kind.PLATING))
                * (getFigurehead() == com.cursedtides.ship.FigureheadItem.Kind.LION ? 1.15f : 1.0f));
    }

    public int guns() {
        return Math.max(1, shipClass().cannonsPerSide() + (getRefit() == LIGHT ? -1 : getRefit() == WAR ? 1 : 0)) + upgrade(ShipUpgradeItem.Kind.GUNS)
                + (getFigurehead() == com.cursedtides.ship.FigureheadItem.Kind.SKULL ? 1 : 0);
    }

    public int cargoRows() {
        return Math.min(6, shipClass().cargoRows() + upgrade(ShipUpgradeItem.Kind.HOLD));
    }

    private double speedFactor() {
        return shipClass().speed() * (getRefit() == LIGHT ? 1.2 : getRefit() == WAR ? 0.88 : 1.0) * (1.0 + getSailSkill() * 0.01) * (1.0 + 0.08 * upgrade(ShipUpgradeItem.Kind.CANVAS))
                * (1.0 + 0.10 * this.entityData.get(NAVIGATORS))
                * (getFigurehead() == com.cursedtides.ship.FigureheadItem.Kind.MERMAID ? 1.10 : 1.0);
    }

    public boolean isOwner(Player player) {
        UUID owner = getOwner();
        return owner == null || owner.equals(player.getUUID());
    }

    public Component shipName() {
        return this.hasCustomName() ? this.getCustomName() : Component.literal("Unnamed " + variantName() + shipClass().displayName());
    }

    public String variantName() {
        return getRefit() == LIGHT ? "Light " : getRefit() == WAR ? "War " : "";
    }

    // ================================================================== size and seats

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return super.getDimensions(pose).scale(shipClass().scale());
    }

    @Override
    protected int getMaxPassengers() {
        return isHostile() ? 4 : shipClass().seats();
    }

    /** The owner steers from any seat. Hostile ships steer themselves. */
    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        if (isHostile()) {
            return null;
        }
        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof Player player && isOwner(player)) {
                return player;
            }
        }
        return null;
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scaleFactor) {
        float s = shipClass().scale();
        double along, side;
        if (passenger == getControllingPassenger()) {
            along = -0.9;
            side = 0.0;
        } else {
            int index = 0;
            for (Entity other : this.getPassengers()) {
                if (other == passenger) break;
                if (other != getControllingPassenger()) index++;
            }
            along = 0.7 - (index / 2) * 0.6;
            side = index % 2 == 0 ? 0.35 : -0.35;
        }
        return new Vec3(side * s, 0.2 * s, along * s).yRot(-this.getYRot() * Mth.DEG_TO_RAD);
    }

    @Override
    public Item getDropItem() {
        return shipClass().deed();
    }

    // ================================================================== movement

    private boolean onWater() {
        Level world = this.level();
        return world.getFluidState(this.blockPosition()).is(FluidTags.WATER)
                || world.getFluidState(this.blockPosition().below()).is(FluidTags.WATER);
    }

    @Override
    public void tick() {
        if (spottedUntil > 0 && this.tickCount > spottedUntil) {
            spottedUntil = -1;
            this.setGlowingTag(false);
        }
        super.tick();
        Level world = this.level();

        if (this.isControlledByLocalInstance() && onWater()) {
            if (isHostile()) {
                if (world instanceof ServerLevel serverWorld) {
                    hostileAi(serverWorld);
                }
            } else if (isAnchored()) {
                // Riding at anchor: she swings but doesn't go anywhere
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.0, 1.0, 0.0));
            } else if (getSail() > 0 && getControllingPassenger() != null) {
                applySail(getSail() == 1 ? 0.55 : 1.0);
                if (world.isThundering() && this.random.nextInt(4) == 0) {
                    this.setYRot(this.getYRot() + (this.random.nextFloat() - 0.5f) * 3.0f); // storm gusts
                }
            }
        }

        if (world.isClientSide) {
            if (getHull() < maxHull() * 0.3f && this.random.nextInt(3) == 0) {
                world.addParticle(ParticleTypes.LARGE_SMOKE, getX() + random.nextGaussian() * 0.5,
                        getY() + 0.6 * shipClass().scale(), getZ() + random.nextGaussian() * 0.5, 0, 0.05, 0);
            }
            return;
        }
        serverTick((ServerLevel) world);
    }

    private void applySail(double sailAmount) {
        Level world = this.level();
        long time = world.getGameTime();
        double weather = world.isThundering() ? 1.35 : world.isRaining() ? 1.1 : 1.0;
        double torn = isTorn() ? 0.4 : 1.0;
        // Water drag takes a tenth of her way each tick, so she settles at about nine times this push: for a sloop
        // under full sail in a fair wind, some 25-30 blocks a second, as fast as the old pirate games' ships
        double boost = active(rammingUntil) ? 1.9 : active(fullSailUntil) ? 1.5 : 1.0;
        double accel = 0.2 * sailAmount * Wind.efficiency(this.getYRot(), time) * Wind.strength(time)
                * speedFactor() * weather * torn * boost;
        Vec3 forward = Vec3.directionFromRotation(0.0f, this.getYRot());
        Vec3 v = this.getDeltaMovement().add(forward.scale(accel));
        double max = 1.2 * speedFactor() * torn * (active(rammingUntil) ? 1.5 : active(fullSailUntil) ? 1.3 : 1.0);
        double horizontal = Math.hypot(v.x, v.z);
        if (horizontal > max) {
            v = new Vec3(v.x / horizontal * max, v.y, v.z / horizontal * max);
        }
        this.setDeltaMovement(v);
    }

    // ================================================================== hostile AI

    /**
     * Ghost ships hunt any player, then Tidewatch ships. Tidewatch ships hunt whoever provoked
     * them and any wanted player, then ghost ships; otherwise they patrol.
     */
    @Nullable
    private Entity chooseTarget(ServerLevel world) {
        if (getFaction() == ShipFaction.DROWNED || getFaction() == ShipFaction.PIRATE) {
            boolean ghost = getFaction() == ShipFaction.DROWNED;
            Player player = world.getNearestPlayer(getX(), getY(), getZ(), 90.0,
                    e -> e instanceof Player p && !p.isSpectator() && !p.isCreative()
                            && !(ghost && com.cursedtides.quest.HorizonArc.ending(p) == com.cursedtides.quest.HorizonArc.CLAIMED));
            return player != null ? player : nearestShip(world, s -> s.getFaction() == ShipFaction.NAVY, 70.0);
        }
        if (provokedTicks > 0 && provoker != null && world.getPlayerByUUID(provoker) instanceof Player angry
                && angry.distanceToSqr(this) < 120 * 120) {
            return angry;
        }
        Player outlaw = world.getNearestPlayer(getX(), getY(), getZ(), 90.0,
                e -> e instanceof Player p && !p.isSpectator() && !p.isCreative() && FactionData.isWanted(p));
        return outlaw != null ? outlaw : nearestShip(world,
                s -> s.getFaction() == ShipFaction.DROWNED || s.getFaction() == ShipFaction.PIRATE, 80.0);
    }

    @Nullable
    private ShipEntity nearestShip(ServerLevel world, java.util.function.Predicate<ShipEntity> filter, double range) {
        ShipEntity best = null;
        double bestDist = range * range;
        for (ShipEntity other : world.getEntitiesOfClass(ShipEntity.class, getBoundingBox().inflate(range),
                s -> s != this && filter.test(s))) {
            double d = other.distanceToSqr(this);
            if (d < bestDist) {
                bestDist = d;
                best = other;
            }
        }
        return best;
    }

    /** If set, a hostile ship makes for this point, fighting what it meets on the way (Cannon Defense). */
    public Vec3 forcedDestination;

    private void hostileAi(ServerLevel world) {
        if (com.cursedtides.ship.Boarding.isLashed(this)) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0, 1, 0));
            return; // lashed alongside: no sailing, no broadsides
        }
        if (forcedDestination != null) {
            double dx = forcedDestination.x - getX(), dz = forcedDestination.z - getZ();
            float desired = (float) Math.toDegrees(Math.atan2(-dx, dz));
            float diff = Mth.wrapDegrees(desired - this.getYRot());
            this.setYRot(this.getYRot() + Mth.clamp(diff, -2.5f, 2.5f));
            setSail(2);
            applySail(0.7);
            if (--aiFireCooldown <= 0 && hasLivingCrew()) {
                Player near = world.getNearestPlayer(getX(), getY(), getZ(), 40, false);
                if (near != null) {
                    int side = rightVector().dot(near.position().subtract(position())) >= 0 ? 1 : -1;
                    fireBroadside(world, side, null, near.position(), ShipAmmo.ROUND);
                }
                aiFireCooldown = 120 + this.random.nextInt(40);
            }
            return;
        }
        if (getFaction() == ShipFaction.MERCHANT) {
            merchantAi(world);
            return;
        }
        if (provokedTicks > 0) provokedTicks--;
        Entity target = (this.tickCount % 20 == 0 || cachedTarget == null || cachedTarget.isRemoved())
                ? (cachedTarget = chooseTarget(world)) : cachedTarget;
        float desired;
        if (target == null) {
            if (getFaction() == ShipFaction.NAVY) {
                if (Float.isNaN(patrolYaw)) patrolYaw = this.random.nextFloat() * 360.0f;
                patrolYaw += (this.random.nextFloat() - 0.5f) * 0.6f;
                desired = patrolYaw;
            } else {
                desired = Wind.yaw(world.getGameTime());
            }
            setSail(1);
        } else {
            setSail(2);
            double dx = target.getX() - getX();
            double dz = target.getZ() - getZ();
            double dist = Math.hypot(dx, dz);
            desired = (float) Math.toDegrees(Math.atan2(-dx, dz));
            if (dist < 24) {
                desired += 90.0f; // turn to bring a broadside to bear
            }
            if (--aiFireCooldown <= 0 && dist < 42 && hasLivingCrew()) {
                int side = rightVector().dot(new Vec3(dx, 0, dz)) >= 0 ? 1 : -1;
                ShipAmmo shot = getFaction() == ShipFaction.NAVY && this.random.nextInt(3) == 0 ? ShipAmmo.CHAIN : ShipAmmo.ROUND;
                fireBroadside(world, side, null, target.position().add(target.getDeltaMovement().scale(10)), shot);
                aiFireCooldown = 100 + this.random.nextInt(40);
            }
        }
        float diff = Mth.wrapDegrees(desired - this.getYRot());
        this.setYRot(this.getYRot() + Mth.clamp(diff, -2.0f, 2.0f));
        applySail(target == null ? 0.4 : 0.8);
    }

    /** Traders run before the wind, away from anyone who comes close. */
    private void merchantAi(ServerLevel world) {
        Player near = world.getNearestPlayer(getX(), getY(), getZ(), 48.0,
                e -> e instanceof Player p && !p.isSpectator());
        float desired;
        if (near != null) {
            desired = (float) Math.toDegrees(Math.atan2(-(getX() - near.getX()), getZ() - near.getZ()));
            setSail(2);
        } else {
            if (Float.isNaN(patrolYaw)) patrolYaw = this.random.nextFloat() * 360.0f;
            desired = patrolYaw;
            setSail(1);
        }
        float diff = Mth.wrapDegrees(desired - this.getYRot());
        this.setYRot(this.getYRot() + Mth.clamp(diff, -1.5f, 1.5f));
        applySail(near != null ? 0.7 : 0.4);
    }

    // ================================================================== server upkeep

    // ------------------------------------------------------------------ sailing skills (see SkillBar)

    /** Game time until which each sailing skill's effect lasts. */
    private long fullSailUntil, rammingUntil, openFireUntil, coverUntil;
    private int comeAboutTicks;
    private float comeAboutStep;

    private long burningUntil;

    public void fullSail(long until) { fullSailUntil = until; }
    public void setBurning(long until) { burningUntil = Math.max(burningUntil, until); }
    public void rammingSpeed(long until) { rammingUntil = until; }
    public void openFire(long until) { openFireUntil = until; }
    public void takeCover(long until) { coverUntil = until; }
    public void comeAbout(int ticks, float degrees) { comeAboutTicks = ticks; comeAboutStep = degrees / ticks; }
    private boolean active(long until) { return this.level().getGameTime() < until; }

    private void sailingSkills(ServerLevel world) {
        if (active(burningUntil)) {
            if (this.tickCount % 5 == 0) world.sendParticles(ParticleTypes.FLAME, getX(), getY() + 1, getZ(), 6, 1.2 * shipClass().scale(), 0.4, 1.2 * shipClass().scale(), 0.02);
            if (this.tickCount % 20 == 0) seaDamage(world, 3f, "Fire on deck!");
        }
        if (comeAboutTicks > 0) {
            comeAboutTicks--;
            this.setYRot(this.getYRot() + comeAboutStep);
            this.setDeltaMovement(this.getDeltaMovement().scale(0.97));
        }
        if (active(fullSailUntil) && this.tickCount % 4 == 0) {
            world.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 2 * shipClass().scale(), getZ(), 2, 1, 0.5, 1, 0.02);
        }
        if (active(rammingUntil)) {
            Vec3 v = this.getDeltaMovement();
            if (Math.hypot(v.x, v.z) > 0.4) {
                for (ShipEntity other : world.getEntitiesOfClass(ShipEntity.class, getBoundingBox().inflate(1.5), o -> o != this && o.isHostile())) {
                    if (other.getTags().contains("ct_rammed_" + this.tickCount / 40)) continue;
                    other.addTag("ct_rammed_" + this.tickCount / 40);
                    LivingEntity cap = getControllingPassenger();
                    float dmg = 35f + shipClass().ordinal() * 8f;
                    other.hurt(cap != null ? world.damageSources().mobAttack(cap) : world.damageSources().generic(), dmg);
                    this.setHull(this.getHull() - dmg * 0.2f);
                    this.setDeltaMovement(v.scale(0.3));
                    world.playSound(null, blockPosition(), SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.NEUTRAL, 2f, 0.6f);
                    world.sendParticles(ParticleTypes.EXPLOSION, other.getX(), other.getY() + 1, other.getZ(), 4, 1, 0.5, 1, 0);
                    rammingUntil = 0;
                    break;
                }
            }
        }
        if (active(coverUntil) && this.tickCount % 20 == 0) {
            for (Entity e : getPassengers()) {
                if (e instanceof LivingEntity l) l.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 30, 1, false, false));
            }
        }
    }

    private void serverTick(ServerLevel world) {
        sailingSkills(world);
        if (portCooldown > 0) portCooldown--;
        if (starboardCooldown > 0) starboardCooldown--;
        if (gunnerCooldown > 0) gunnerCooldown--;
        int torn = this.entityData.get(TORN);
        if (torn > 0) this.entityData.set(TORN, torn - 1);

        if (lastServerPos != null) {
            speedBlocksPerSecond = speedBlocksPerSecond * 0.8
                    + 0.2 * Math.hypot(getX() - lastServerPos.x, getZ() - lastServerPos.z) * 20.0;
        }
        lastServerPos = position();

        if (pendingCrew.length > 0) {
            boardPendingCrew(world);
        }

        if (this.tickCount % 20 == 0) {
            if (getControllingPassenger() instanceof Player captain) {
                this.entityData.set(SAIL_SKILL, PirateData.level(captain, Weapon.SAILING)
                        + (int) com.cursedtides.pirate.PerkData.stat(captain, com.cursedtides.pirate.Perk.Stat.SAIL_SPEED));
            }
            this.entityData.set(NAVIGATORS, crewCount(CrewmateEntity.NAVIGATOR));
        }

        if (!isHostile()) {
            crewDuties(world);
            payWages(world);
        }

        if (this.tickCount % 10 == 0) {
            for (Entity passenger : this.getPassengers()) {
                if (passenger instanceof ServerPlayer player) {
                    player.displayClientMessage(hud(world), true);
                }
            }
        }

        // Storms: lightning finds tall masts
        if (this.tickCount % 200 == 0 && world.isThundering() && this.isVehicle() && this.random.nextInt(8) == 0) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(world);
            if (bolt != null) {
                bolt.moveTo(position().add(this.random.nextGaussian() * 6, 0, this.random.nextGaussian() * 6));
                bolt.setVisualOnly(true);
                world.addFreshEntity(bolt);
            }
            if (this.random.nextInt(3) == 0) {
                setHull(getHull() - 12.0f);
                tearRigging(300);
                for (Entity passenger : this.getPassengers()) {
                    passenger.sendSystemMessage(Component.literal("Lightning splinters the mast!").withStyle(ChatFormatting.YELLOW));
                }
                if (getHull() <= 0) {
                    sink(world);
                    return;
                }
            }
        }

        if (isHostile() && this.tickCount % 100 == 0) {
            boolean playerNear = world.getNearestPlayer(this, 160.0) != null;
            lonelyTicks = playerNear ? 0 : lonelyTicks + 100;
            if (lonelyTicks >= 1200) {
                this.getPassengers().forEach(Entity::discard);
                this.discard();
            }
        }
    }

    private Component hud(ServerLevel world) {
        long t = world.getGameTime();
        int max = maxHull();
        float hull = getHull();
        ChatFormatting hullColor = hull > max * 0.6f ? ChatFormatting.GREEN : hull > max * 0.3f ? ChatFormatting.YELLOW : ChatFormatting.RED;
        String sails = switch (getSail()) {
            case 0 -> "Furled";
            case 1 -> "Half";
            default -> "Full";
        };
        if (isTorn()) sails += " (torn)";
        String weather = world.isThundering() ? "Storm" : world.isRaining() ? "Rain" : "";
        double knots = speedBlocksPerSecond * 1.94;
        return Component.empty()
                .append(shipName().copy().withStyle(ChatFormatting.GOLD))
                .append(Component.literal("  Hull ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal((int) Math.ceil(hull) + "/" + max).withStyle(hullColor))
                .append(Component.literal("  Sails ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(sails).withStyle(isTorn() ? ChatFormatting.RED : ChatFormatting.WHITE))
                .append(Component.literal("  Shot ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(getAmmo().displayName()).withStyle(ChatFormatting.WHITE))
                .append(Component.literal("  Wind ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(Wind.fromName(t) + " " + Wind.strengthName(t) + ", " + Wind.pointOfSail(getYRot(), t))
                        .withStyle(ChatFormatting.AQUA))
                .append(Component.literal(weather.isEmpty() ? "" : "  " + weather).withStyle(ChatFormatting.DARK_AQUA))
                .append(Component.literal(String.format("  %.1f kn", knots)).withStyle(ChatFormatting.WHITE));
    }

    // ================================================================== crew

    private int crewCount(int role) {
        int n = 0;
        for (Entity e : this.getPassengers()) {
            if (e instanceof CrewmateEntity c && c.isAlive() && c.getRole() == role) n++;
        }
        return n;
    }

    private List<CrewmateEntity> crew() {
        List<CrewmateEntity> list = new ArrayList<>();
        for (Entity e : this.getPassengers()) {
            if (e instanceof CrewmateEntity c && c.isAlive()) list.add(c);
        }
        return list;
    }

    private void crewDuties(ServerLevel world) {
        // Bosuns patch the hull from planks in the hold
        int bosuns = crewCount(CrewmateEntity.BOSUN);
        if (bosuns > 0 && this.tickCount % 100 == 0 && getHull() < maxHull()) {
            for (int i = 0; i < cargo.getContainerSize(); i++) {
                ItemStack stack = cargo.getItem(i);
                if (stack.is(ItemTags.PLANKS)) {
                    stack.shrink(1);
                    setHull(getHull() + 6.0f * bosuns);
                    world.playSound(null, getX(), getY(), getZ(), SoundEvents.WOOD_PLACE, SoundSource.NEUTRAL, 0.6f, 1.2f);
                    break;
                }
            }
        }
        // Gunners fire on enemy ships by themselves
        int gunners = crewCount(CrewmateEntity.GUNNER);
        if (gunners > 0 && gunnerCooldown <= 0 && this.tickCount % 20 == 0) {
            boolean ownerWanted = getOwner() != null && world.getPlayerByUUID(getOwner()) instanceof Player owner
                    && FactionData.isWanted(owner);
            ShipEntity enemy = nearestShip(world, s -> s.getFaction() == ShipFaction.DROWNED
                    || s.getFaction() == ShipFaction.PIRATE
                    || (s.getFaction() == ShipFaction.NAVY && (ownerWanted || s.provoker != null && s.provoker.equals(getOwner()))), 36.0);
            if (enemy != null && takeAmmo(null, getAmmo(), Math.max(1, guns() / 2), true)) {
                int side = rightVector().dot(enemy.position().subtract(position())) >= 0 ? 1 : -1;
                Player owner = getOwner() == null ? null : world.getPlayerByUUID(getOwner());
                fireBroadside(world, side, owner, enemy.position(), getAmmo());
                gunnerCooldown = Math.max(40, 110 - gunners * 15);
            }
        }
    }

    /** Two doubloons a day per hand, from the hold. Unpaid crew walk off the ship. */
    private void payWages(ServerLevel world) {
        long day = world.getDayTime() / 24000L;
        if (lastWageDay < 0) {
            lastWageDay = day;
            return;
        }
        if (day == lastWageDay) {
            return;
        }
        lastWageDay = day;
        Player owner = getOwner() == null ? null : world.getPlayerByUUID(getOwner());
        for (CrewmateEntity hand : crew()) {
            if (cargo.countItem(ModItems.DOUBLOON) >= 2) {
                takeFromCargo(ModItems.DOUBLOON, 2);
            } else {
                if (owner != null) {
                    owner.displayClientMessage(Component.literal("Unpaid, ").append(hand.getName())
                            .append(Component.literal(" has left the crew of the ").append(shipName()).append(Component.literal(".")))
                            .withStyle(ChatFormatting.RED), false);
                }
                hand.discard();
            }
        }
    }

    private void boardPendingCrew(ServerLevel world) {
        int[] roles = pendingCrew;
        pendingCrew = new int[0];
        for (int role : roles) {
            signOn(world, role);
        }
    }

    private boolean signOn(ServerLevel world, int role) {
        if (this.getPassengers().size() >= getMaxPassengers()) {
            return false;
        }
        CrewmateEntity hand = ModEntities.CREWMATE.create(world);
        if (hand == null) return false;
        hand.moveTo(getX(), getY() + 0.5, getZ(), getYRot(), 0.0f);
        hand.finalizeSpawn(world, world.getCurrentDifficultyAt(blockPosition()), MobSpawnType.EVENT, null);
        hand.setRole(role);
        hand.setPersistenceRequired();
        world.addFreshEntity(hand);
        hand.startRiding(this, true);
        return true;
    }

    // ================================================================== cannons

    /** Starboard (right-hand) direction for the current heading. */
    private Vec3 rightVector() {
        Vec3 forward = Vec3.directionFromRotation(0.0f, this.getYRot());
        return new Vec3(-forward.z, 0.0, forward.x);
    }

    /**
     * Fires every gun on one side. side = +1 starboard, -1 port.
     * aimAt = null fires straight out; otherwise guns lay toward that point.
     */
    public void fireBroadside(ServerLevel world, int side, @Nullable Player gunner, @Nullable Vec3 aimAt, ShipAmmo ammo) {
        int guns = isHostile() ? shipClass().cannonsPerSide() : guns();
        float s = shipClass().scale();
        Vec3 forward = Vec3.directionFromRotation(0.0f, this.getYRot());
        Vec3 out = rightVector().scale(side);
        int pellets = ammo == ShipAmmo.GRAPE ? 3 : 1;
        for (int i = 0; i < guns; i++) {
            double along = (guns == 1 ? 0.0 : (i / (double) (guns - 1) - 0.5)) * 1.8 * s;
            Vec3 origin = this.position().add(forward.scale(along)).add(out.scale(1.0 * s + 0.5)).add(0, 0.55 * s, 0);
            for (int p = 0; p < pellets; p++) {
                Vec3 dir;
                float spread;
                if (aimAt != null) {
                    Vec3 d = aimAt.subtract(origin);
                    double dist = Math.max(1.0, Math.hypot(d.x, d.z));
                    dir = new Vec3(d.x / dist, 0.05 + dist * 0.0035, d.z / dist);
                    spread = 4.0f;
                } else {
                    dir = new Vec3(out.x, 0.1, out.z);
                    spread = 2.0f;
                }
                if (ammo == ShipAmmo.GRAPE) spread += 8.0f;
                CannonballEntity ball = new CannonballEntity(world, origin.x, origin.y, origin.z);
                ball.setAmmo(ammo);
                ball.setOwner(gunner != null ? gunner : this);
                ball.shoot(dir.x, dir.y, dir.z, ammo == ShipAmmo.GRAPE ? 1.5f : 1.75f, spread);
                world.addFreshEntity(ball);
            }
            world.sendParticles(ParticleTypes.LARGE_SMOKE, origin.x, origin.y, origin.z, 8, 0.25, 0.25, 0.25, 0.02);
            world.sendParticles(ParticleTypes.FLAME, origin.x, origin.y, origin.z, 3, 0.1, 0.1, 0.1, 0.01);
        }
        world.playSound(null, getX(), getY(), getZ(), com.cursedtides.sound.ModSounds.CANNON_BOOM.value(), SoundSource.PLAYERS, 2.0f, 0.9f + this.random.nextFloat() * 0.2f);
        world.playSound(null, getX(), getY(), getZ(), SoundEvents.FIREWORK_ROCKET_LARGE_BLAST,
                SoundSource.NEUTRAL, 3.0f, 0.35f);
    }

    /** Broadside ordered by a player aboard (any crew member can serve as gunner). */
    public void playerBroadside(ServerPlayer gunner, int side) {
        if (isHostile() || gunner.getVehicle() != this || !(this.level() instanceof ServerLevel world)) {
            return;
        }
        int cooldown = side > 0 ? starboardCooldown : portCooldown;
        if (cooldown > 0) {
            gunner.displayClientMessage(Component.literal((side > 0 ? "Starboard" : "Port") + " guns reloading... "
                    + (cooldown / 20 + 1) + "s").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        ShipAmmo ammo = getAmmo();
        boolean free = this.random.nextDouble() < com.cursedtides.pirate.PerkData.stat(gunner,
                com.cursedtides.pirate.Perk.Stat.BROADSIDE_SAVE);
        if (!free && !takeAmmo(gunner, ammo, Math.max(1, guns() / 2), false)) {
            gunner.displayClientMessage(Component.literal("No " + ammo.displayName().toLowerCase() + " in the hold or your pockets!")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }
        fireBroadside(world, side, gunner, null, ammo);
        int reload = Math.max(30, 80 - PirateData.level(gunner, Weapon.CANNON) * 2);
        reload = (int) Math.max(15, Math.round(reload * (1.0 - com.cursedtides.pirate.PerkData.stat(gunner,
                com.cursedtides.pirate.Perk.Stat.CANNON_RELOAD))));
        if (active(openFireUntil)) reload = Math.max(10, reload / 2);
        if (side > 0) starboardCooldown = reload; else portCooldown = reload;
    }

    public void cycleAmmo(ServerPlayer player) {
        ShipAmmo next = ShipAmmo.byIndex((getAmmo().ordinal() + 1) % ShipAmmo.values().length);
        this.entityData.set(AMMO, next.ordinal());
        player.displayClientMessage(Component.literal("Guns loaded with " + next.displayName()).withStyle(ChatFormatting.GOLD), true);
    }

    private boolean takeAmmo(@Nullable Player player, ShipAmmo ammo, int count, boolean holdOnly) {
        if (player != null && player.getAbilities().instabuild) {
            return true;
        }
        Item item = ammo.item();
        int available = cargo.countItem(item) + (holdOnly || player == null ? 0 : player.getInventory().countItem(item));
        if (available < count) {
            return false;
        }
        int left = takeFromCargo(item, count);
        if (left > 0 && player != null) {
            var inv = player.getInventory();
            for (int i = 0; i < inv.getContainerSize() && left > 0; i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.is(item)) {
                    int take = Math.min(left, stack.getCount());
                    stack.shrink(take);
                    left -= take;
                }
            }
        }
        return true;
    }

    /** Removes up to count of item from the hold; returns how many it couldn't find. */
    private int takeFromCargo(Item item, int count) {
        int left = count;
        for (int i = 0; i < cargo.getContainerSize() && left > 0; i++) {
            ItemStack stack = cargo.getItem(i);
            if (stack.is(item)) {
                int take = Math.min(left, stack.getCount());
                stack.shrink(take);
                left -= take;
            }
        }
        return left;
    }

    // ================================================================== damage, sinking, capture

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source) || this.isRemoved()) {
            return false;
        }
        if (this.level().isClientSide) {
            return true;
        }
        if (this.getTags().contains("cursedtides_legendary")) {
            amount *= 0.5f;
        }
        Entity attacker = source.getEntity();
        // Leveled crews: a tough ship shrugs off more, and a tough ship's guns bite harder
        int myLevel = com.cursedtides.combat.Levels.levelOf(this);
        if (myLevel > 0) amount /= (float) (1 + 0.05 * (myLevel - 1));
        else if (attacker != null && com.cursedtides.combat.Levels.levelOf(attacker) > 0) {
            amount *= (float) com.cursedtides.combat.Levels.damageFactor(com.cursedtides.combat.Levels.levelOf(attacker));
        }
        if (attacker == this || (attacker != null && attacker.getVehicle() == this)) {
            return false; // your own crew and your own guns don't hole your hull
        }
        if (attacker instanceof Player player) {
            lastAttacker = player.getUUID();
            if (getFaction() == ShipFaction.NAVY) {
                provoker = player.getUUID();
                provokedTicks = 2400;
                cachedTarget = player;
                if (player instanceof ServerPlayer sp && !FactionData.isWanted(sp)) {
                    FactionEvents.onAttackedNavy(sp);
                }
            }
        }
        this.setHurtDir(-this.getHurtDir());
        this.setHurtTime(10);
        this.setDamage(this.getDamage() + amount * 2.0f);
        float hullDamage = source.is(DamageTypeTags.IS_EXPLOSION) ? amount * 1.5f : amount;
        if (active(coverUntil)) hullDamage *= 0.5f;
        if (com.cursedtides.quest.MusterArc.isFlagship(this) && getHull() - hullDamage < maxHull() * 0.2f) {
            setHull(maxHull() * 0.2f);
            if (attacker instanceof Player pl) pl.displayClientMessage(Component.literal("The curse holds her timbers together. Grapple her and board!")
                    .withStyle(ChatFormatting.DARK_AQUA), true);
            return true;
        }
        setHull(getHull() - hullDamage);
        this.level().playSound(null, getX(), getY(), getZ(), SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR,
                SoundSource.NEUTRAL, 0.7f, 0.7f + this.random.nextFloat() * 0.3f);
        if (getHull() <= 0.0f) {
            sink((ServerLevel) this.level());
        }
        return true;
    }

    private void sink(ServerLevel world) {
        if (this.isRemoved()) return;
        world.playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_SPLASH, SoundSource.NEUTRAL, 2.0f, 0.5f);
        world.sendParticles(ParticleTypes.SPLASH, getX(), getY() + 0.5, getZ(), 60, 1.5, 0.4, 1.5, 0.1);
        world.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, getX(), getY(), getZ(), 40, 1.0, 0.3, 1.0, 0.1);
        Containers.dropContents(world, this, cargo);
        this.spawnAtLocation(new ItemStack(Items.OAK_PLANKS, 4 + shipClass().ordinal() * 3));

        if (isHostile()) {
            this.spawnAtLocation(new ItemStack(ModItems.DOUBLOON, 10 + this.random.nextInt(15)));
            this.spawnAtLocation(new ItemStack(ModItems.CANNONBALL, 2 + this.random.nextInt(4)));
            if (this.random.nextInt(4) == 0) this.spawnAtLocation(new ItemStack(Items.HEART_OF_THE_SEA));
            if (this.random.nextInt(3) == 0) this.spawnAtLocation(new ItemStack(ModItems.TREASURE_MAP));
            if (this.random.nextInt(5) == 0) this.spawnAtLocation(new ItemStack(ModItems.MAP_FRAGMENT));
            if (getFaction() == ShipFaction.DROWNED && this.random.nextInt(3) == 0) {
                this.spawnAtLocation(new ItemStack(com.cursedtides.trade.TradeGood.CURSED_IDOL.item()));
            }
            if (getFaction() == ShipFaction.NAVY) {
                this.spawnAtLocation(new ItemStack(ModItems.PISTOL_SHOT, 6 + this.random.nextInt(8)));
                this.spawnAtLocation(new ItemStack(ModItems.CHAIN_SHOT, 1 + this.random.nextInt(3)));
                if (this.random.nextInt(5) == 0) this.spawnAtLocation(new ItemStack(ModItems.MUSKET));
            }
            if (lastAttacker != null && world.getPlayerByUUID(lastAttacker) instanceof ServerPlayer sinker) {
                PirateData.addXp(sinker, Weapon.CANNON, 60);
                FactionEvents.onShipSunk(sinker, getFaction());
                com.cursedtides.quest.HorizonArc.onShipSunk(sinker, this);
                com.cursedtides.quest.Contracts.onShipSunk(sinker, com.cursedtides.combat.Levels.levelOf(this));
                com.cursedtides.sea.SeaEvents.onSunk(world, this, sinker);
                com.cursedtides.quest.MusterArc.onSunk(sinker, this);
                com.cursedtides.living.FestivalEvents.onShipSunk(sinker, this);
            }
            for (ServerPlayer p : world.getPlayers(p -> p.distanceToSqr(this) < 80 * 80)) {
                p.displayClientMessage(Component.literal("The ").append(shipName()).append(Component.literal(" slips beneath the waves!"))
                        .withStyle(ChatFormatting.AQUA), false);
            }
            this.getPassengers().forEach(Entity::discard);
        } else {
            UUID owner = getOwner();
            // Sinking another pirate's ship: sea infamy
            if (lastAttacker != null && !lastAttacker.equals(owner) && world.getPlayerByUUID(lastAttacker) instanceof ServerPlayer raider
                    && !com.cursedtides.social.CrewState.allied(world.getServer(), raider.getUUID(), owner == null ? raider.getUUID() : owner)) {
                com.cursedtides.social.Infamy.sea(raider, 20 + shipClass().ordinal() * 5);
            }
            if (owner != null && world.getPlayerByUUID(owner) instanceof ServerPlayer captain) {
                captain.displayClientMessage(Component.literal("Your ship ").append(shipName())
                        .append(Component.literal(" has gone down. Her cargo is adrift.")).withStyle(ChatFormatting.RED), false);
            }
            if (insured && owner != null) {
                ItemStack replacement = new ItemStack(shipClass().deed());
                CompoundTag fittings = new CompoundTag();
                fittings.putInt("Look", getLook());
                fittings.putInt("Upgrades", this.entityData.get(UPGRADES));
                fittings.putInt("SailColor", getSailColor());
                fittings.putInt("Flag", getFlag());
                fittings.putInt("Figurehead", this.entityData.get(FIGUREHEAD));
                fittings.putInt("Emblem", this.entityData.get(EMBLEM));
                replacement.set(DataComponents.CUSTOM_DATA, CustomData.of(fittings));
                if (this.hasCustomName()) replacement.set(DataComponents.CUSTOM_NAME, this.getCustomName());
                com.cursedtides.trade.Insurance.payOut(world.getServer(), owner, replacement);
                insured = false;
            }
            for (CrewmateEntity hand : crew()) hand.discard();
            this.ejectPassengers();
        }
        this.discard();
    }

    private boolean hasLivingCrew() {
        return this.getPassengers().stream().anyMatch(e -> e instanceof Mob && !(e instanceof CrewmateEntity) && e.isAlive());
    }

    // ================================================================== interaction

    /** Owner sneak-punches the ship to stow her: she goes back into a deed with everything aboard. */
    @Override
    public boolean skipAttackInteraction(Entity attacker) {
        if (attacker instanceof Player player && player.isShiftKeyDown() && !isHostile() && isOwner(player)
                && getOwner() != null) {
            if (player instanceof ServerPlayer serverPlayer) {
                stow(serverPlayer);
            }
            return true;
        }
        return false;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        boolean mine = !isHostile() && isOwner(player);
        boolean server = !this.level().isClientSide;

        if (held.is(ItemTags.PLANKS) && !isHostile() && getHull() < maxHull()) {
            if (server) {
                setHull(getHull() + 10.0f + (float) com.cursedtides.pirate.PerkData.stat(player,
                        com.cursedtides.pirate.Perk.Stat.REPAIR));
                if (!player.getAbilities().instabuild) held.shrink(1);
                this.level().playSound(null, getX(), getY(), getZ(), SoundEvents.WOOD_PLACE, SoundSource.PLAYERS, 1.0f, 0.8f);
                player.displayClientMessage(Component.literal("Hull patched: " + (int) getHull() + "/" + maxHull()).withStyle(ChatFormatting.GREEN), true);
            }
            return InteractionResult.sidedSuccess(!server);
        }
        if (mine && held.getItem() instanceof com.cursedtides.ship.FigureheadItem fh) {
            if (server) {
                var old = getFigurehead();
                setFigurehead(fh.kind);
                if (!player.getAbilities().instabuild) held.shrink(1);
                if (old != com.cursedtides.ship.FigureheadItem.Kind.NONE) {
                    var back = com.cursedtides.ship.FigureheadItem.itemFor(old);
                    if (back != null) player.addItem(new ItemStack(back));
                }
                if (getHull() > maxHull()) setHull(maxHull());
                player.displayClientMessage(Component.literal("The " + fh.kind.title + " is fixed to the bow. " + fh.kind.effect + ".").withStyle(ChatFormatting.GOLD), true);
                this.level().playSound(null, getX(), getY(), getZ(), SoundEvents.WOOD_PLACE, SoundSource.PLAYERS, 1.0f, 0.7f);
            }
            return InteractionResult.sidedSuccess(!server);
        }
        if (mine && held.getItem() instanceof com.cursedtides.ship.FlagItem paint && player.isShiftKeyDown()) {
            if (server) {
                this.entityData.set(EMBLEM, paint.design().ordinal() + 1);
                player.displayClientMessage(Component.literal("The sailmaker paints the " + paint.design().displayName + " across your mainsail.").withStyle(ChatFormatting.GOLD), true);
                this.level().playSound(null, getX(), getY(), getZ(), SoundEvents.WOOL_PLACE, SoundSource.PLAYERS, 1.0f, 0.8f);
            }
            return InteractionResult.sidedSuccess(!server);
        }
        if (mine && held.getItem() instanceof com.cursedtides.ship.FlagItem flagItem) {
            if (server) {
                setFlag(flagItem.design().ordinal());
                player.displayClientMessage(Component.literal("You run up the " + flagItem.design().displayName + ".").withStyle(ChatFormatting.GOLD), true);
                this.level().playSound(null, getX(), getY(), getZ(), SoundEvents.WOOL_PLACE, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
            return InteractionResult.sidedSuccess(!server);
        }
        if (mine && (held.is(com.cursedtides.item.ModItems.LIGHT_REFIT) || held.is(com.cursedtides.item.ModItems.WAR_REFIT))) {
            if (server) {
                int v = held.is(com.cursedtides.item.ModItems.LIGHT_REFIT) ? LIGHT : WAR;
                if (getRefit() == v) {
                    player.displayClientMessage(Component.literal("She's already a " + variantName().trim().toLowerCase() + " ship.").withStyle(ChatFormatting.GRAY), true);
                } else {
                    setRefit(v);
                    if (!player.getAbilities().instabuild) held.shrink(1);
                    player.displayClientMessage(Component.literal(v == LIGHT ? "Refitted light: stripped down for speed. Faster, but a thinner hull and fewer guns."
                            : "Refitted for war: heavier timbers and an extra gun a side. Tougher, but slower.").withStyle(ChatFormatting.GOLD), false);
                    this.level().playSound(null, getX(), getY(), getZ(), SoundEvents.SMITHING_TABLE_USE, SoundSource.PLAYERS, 1.0f, 0.8f);
                }
            }
            return InteractionResult.sidedSuccess(!server);
        }
        if (mine && held.getItem() instanceof ShipUpgradeItem upgradeItem) {
            if (server) fitUpgrade(player, held, upgradeItem.kind());
            return InteractionResult.sidedSuccess(!server);
        }
        if (mine && held.getItem() instanceof CrewArticlesItem articles) {
            if (server && this.level() instanceof ServerLevel world) {
                if (signOn(world, articles.role())) {
                    if (!player.getAbilities().instabuild) held.shrink(1);
                    player.displayClientMessage(Component.literal("A new " + CrewmateEntity.ROLE_NAMES[articles.role()].toLowerCase()
                            + " signs aboard the ").append(shipName()).withStyle(ChatFormatting.GOLD), false);
                } else {
                    player.displayClientMessage(Component.literal("No free seat aboard for more crew.").withStyle(ChatFormatting.RED), true);
                }
            }
            return InteractionResult.sidedSuccess(!server);
        }
        if (mine && held.getItem() instanceof DyeItem dye) {
            if (server) {
                this.entityData.set(SAIL_COLOR, dye.getDyeColor().getTextureDiffuseColor());
                if (!player.getAbilities().instabuild) held.shrink(1);
                player.displayClientMessage(Component.literal("Fresh-dyed sails!").withStyle(ChatFormatting.GOLD), true);
            }
            return InteractionResult.sidedSuccess(!server);
        }

        if (player.isSecondaryUseActive()) {
            if (server && player instanceof ServerPlayer serverPlayer) {
                if (isHostile()) {
                    tryCapture(serverPlayer);
                } else if (isOwner(player) || player.getVehicle() == this
                        || com.cursedtides.social.CrewState.shipmates(serverPlayer.getServer(), getOwner(), player.getUUID())) {
                    openCargo(serverPlayer);
                } else {
                    player.displayClientMessage(Component.literal("That's not your hold, sailor.").withStyle(ChatFormatting.RED), true);
                }
            }
            return InteractionResult.sidedSuccess(!server);
        }

        if (isHostile()) {
            return InteractionResult.PASS; // climb aboard and fight, but you can't take a seat
        }
        return super.interact(player, hand);
    }

    private void fitUpgrade(Player player, ItemStack held, ShipUpgradeItem.Kind kind) {
        int level = upgrade(kind);
        int max = kind == ShipUpgradeItem.Kind.HOLD ? Math.min(kind.maxLevel(), 6 - shipClass().cargoRows()) : kind.maxLevel();
        if (level >= max) {
            player.displayClientMessage(Component.literal("She can't take any more of that.").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        float hullFraction = getHull() / maxHull();
        setUpgrade(kind, level + 1);
        if (kind == ShipUpgradeItem.Kind.PLATING) {
            setHull(hullFraction * maxHull());
        }
        if (!player.getAbilities().instabuild) held.shrink(1);
        this.level().playSound(null, getX(), getY(), getZ(), SoundEvents.SMITHING_TABLE_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
        player.displayClientMessage(Component.literal("Fitted: " + kind.effect() + " (" + (level + 1) + "/" + max + ")").withStyle(ChatFormatting.GOLD), false);
    }

    public void openCargo(ServerPlayer player) {
        int rows = cargoRows();
        Component title = shipName().copy().append(Component.literal(" - Cargo Hold"));
        player.openMenu(new SimpleMenuProvider((syncId, inventory, p) -> switch (rows) {
            case 3 -> ChestMenu.threeRows(syncId, inventory, cargo);
            case 4 -> new ChestMenu(MenuType.GENERIC_9x4, syncId, inventory, cargo, 4);
            case 5 -> new ChestMenu(MenuType.GENERIC_9x5, syncId, inventory, cargo, 5);
            default -> ChestMenu.sixRows(syncId, inventory, cargo);
        }, title));
    }

    /** Take her as a prize (after a boarding action). */
    public void captureBy(ServerPlayer player) {
        tryCapture(player);
    }

    /** Strip her and send her down, credited to whoever did it. */
    public void scuttle(ServerLevel world, ServerPlayer by) {
        lastAttacker = by.getUUID();
        sink(world);
    }

    private void tryCapture(ServerPlayer player) {
        if (com.cursedtides.ship.Boarding.defending(this)) {
            player.displayClientMessage(Component.literal("Her boarders still hold the deck!").withStyle(ChatFormatting.RED), true);
            return;
        }
        if (com.cursedtides.quest.MusterArc.isFlagship(this)) {
            player.displayClientMessage(Component.literal("Nobody can sail the Last Muster. Strip her and let her sink.").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        if (hasLivingCrew()) {
            player.displayClientMessage(Component.literal("Her crew still holds the deck!").withStyle(ChatFormatting.RED), true);
            return;
        }
        ShipFaction former = getFaction();
        setFaction(ShipFaction.PLAYER);
        setOwner(player.getUUID());
        FactionEvents.onShipCaptured(player, former);
        setSail(0);
        setHull(Math.max(getHull(), maxHull() * 0.25f));
        this.getPassengers().forEach(Entity::discard);
        player.displayClientMessage(Component.literal("You've taken the ").append(shipName())
                .append(Component.literal("! She's yours now. Patch her hull with planks.")).withStyle(ChatFormatting.GOLD), false);
        this.level().playSound(null, getX(), getY(), getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 0.8f, 1.0f);
        PirateData.addXp(player, Weapon.SAILING, 40);
    }

    /** Insured by the Bank of Kingsport: a sinking pays out a replacement deed. */
    private boolean insured;

    private CompoundTag saveShip(CompoundTag data) {
        if (insured) data.putBoolean(com.cursedtides.trade.Insurance.KEY, true);
        data.putFloat("Hull", getHull());
        data.putInt("Look", getLook());
        data.putInt("Upgrades", this.entityData.get(UPGRADES));
        data.putInt("SailColor", getSailColor());
        data.putInt("Flag", getFlag());
        data.putInt("Figurehead", this.entityData.get(FIGUREHEAD));
        data.putInt("Emblem", this.entityData.get(EMBLEM));
        data.putBoolean("Anchored", isAnchored());
        data.putInt("Ammo", getAmmo().ordinal());
        data.putInt("Variant", getRefit());
        data.put("Cargo", cargo.createTag(this.registryAccess()));
        return data;
    }

    private void loadShip(CompoundTag data) {
        insured = data.getBoolean(com.cursedtides.trade.Insurance.KEY);
        if (data.contains("Upgrades")) this.entityData.set(UPGRADES, data.getInt("Upgrades"));
        this.entityData.set(VARIANT, data.getInt("Variant"));
        if (data.contains("Hull")) setHull(data.getFloat("Hull"));
        setLook(data.contains("Look") ? data.getInt("Look") : (data.getBoolean("Ghost") ? LOOK_GHOST : LOOK_PLAIN));
        if (data.contains("SailColor")) this.entityData.set(SAIL_COLOR, data.getInt("SailColor"));
        setFlag(data.getInt("Flag"));
        this.entityData.set(FIGUREHEAD, data.getInt("Figurehead"));
        this.entityData.set(EMBLEM, data.getInt("Emblem"));
        setAnchored(data.getBoolean("Anchored"));
        if (data.contains("Ammo")) this.entityData.set(AMMO, data.getInt("Ammo"));
        if (data.contains("Cargo", Tag.TAG_LIST)) {
            cargo.fromTag(data.getList("Cargo", Tag.TAG_COMPOUND), this.registryAccess());
        }
    }

    private void stow(ServerPlayer player) {
        ItemStack deed = new ItemStack(shipClass().deed());
        CompoundTag data = saveShip(new CompoundTag());
        List<CrewmateEntity> hands = crew();
        int[] roles = new int[hands.size()];
        for (int i = 0; i < roles.length; i++) {
            roles[i] = hands.get(i).getRole();
            hands.get(i).discard();
        }
        data.put("Crew", new IntArrayTag(roles));
        deed.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        if (this.hasCustomName()) {
            deed.set(DataComponents.CUSTOM_NAME, this.getCustomName());
        }
        this.ejectPassengers();
        if (!player.getInventory().add(deed)) {
            player.drop(deed, false);
        }
        this.level().playSound(null, getX(), getY(), getZ(), SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 1.0f, 0.6f);
        player.displayClientMessage(Component.literal("Anchored and stowed: ").append(shipName()).withStyle(ChatFormatting.GOLD), true);
        this.discard();
    }

    /** Restores a ship (and, next tick, her crew) from a deed's saved data. */
    public void loadFromDeed(CompoundTag data) {
        loadShip(data);
        pendingCrew = data.getIntArray("Crew");
    }

    // ================================================================== save / load

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("ShipClass", this.entityData.get(CLASS));
        nbt.putInt("Sail", getSail());
        nbt.putInt("Faction", getFaction().ordinal());
        if (getOwner() != null) {
            nbt.putUUID("Owner", getOwner());
        }
        nbt.putLong("WageDay", lastWageDay);
        saveShip(nbt);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        this.entityData.set(CLASS, nbt.getInt("ShipClass"));
        setSail(nbt.getInt("Sail"));
        if (nbt.contains("Faction")) {
            setFaction(ShipFaction.byIndex(nbt.getInt("Faction")));
        } else { // ships saved by 0.4.0
            setFaction(nbt.getBoolean("Hostile") ? ShipFaction.DROWNED : ShipFaction.PLAYER);
        }
        setOwner(nbt.hasUUID("Owner") ? nbt.getUUID("Owner") : null);
        lastWageDay = nbt.contains("WageDay") ? nbt.getLong("WageDay") : -1;
        loadShip(nbt);
        if (!nbt.contains("Hull")) setHull(maxHull());
    }
}
