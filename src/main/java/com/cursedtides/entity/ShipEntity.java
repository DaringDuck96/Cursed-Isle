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
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * An ownable sailing ship on top of vanilla boat physics: hull health, sails that catch the
 * world wind (and suffer in storms), a cargo hold, crew seats and hired hands, upgrades,
 * dyed sails, four kinds of shot, repairs, sinking and capture. Ghost ships and Tidewatch
 * patrols use the same class and steer themselves.
 */
public class ShipEntity extends BoatEntity {
    private static final TrackedData<Float> HULL = DataTracker.registerData(ShipEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Integer> SAIL = DataTracker.registerData(ShipEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> CLASS = DataTracker.registerData(ShipEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> FACTION = DataTracker.registerData(ShipEntity.class, TrackedDataHandlerRegistry.INTEGER);
    /** 0 = merchant timber, 1 = ghost, 2 = Tidewatch colours. Kept when a ship is captured. */
    private static final TrackedData<Integer> LOOK = DataTracker.registerData(ShipEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> SAIL_SKILL = DataTracker.registerData(ShipEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Optional<UUID>> OWNER = DataTracker.registerData(ShipEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    /** Packed upgrade levels: 2 bits each for plating, guns, canvas, hold. */
    private static final TrackedData<Integer> UPGRADES = DataTracker.registerData(ShipEntity.class, TrackedDataHandlerRegistry.INTEGER);
    /** RGB sail tint, or -1 for natural canvas. */
    private static final TrackedData<Integer> SAIL_COLOR = DataTracker.registerData(ShipEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> FIGUREHEAD = DataTracker.registerData(ShipEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> EMBLEM = DataTracker.registerData(ShipEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> AMMO = DataTracker.registerData(ShipEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> NAVIGATORS = DataTracker.registerData(ShipEntity.class, TrackedDataHandlerRegistry.INTEGER);
    /** Ticks of torn rigging left (from chain shot or lightning). */
    private static final TrackedData<Integer> TORN = DataTracker.registerData(ShipEntity.class, TrackedDataHandlerRegistry.INTEGER);
    /** Which flag flies from the masthead (0 = the plain pennant). */
    private static final TrackedData<Integer> FLAG = DataTracker.registerData(ShipEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> ANCHORED = DataTracker.registerData(ShipEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    public static final int LOOK_PLAIN = 0, LOOK_GHOST = 1, LOOK_NAVY = 2;

    private final SimpleInventory cargo = new SimpleInventory(54) {
        @Override
        public boolean canPlayerUse(PlayerEntity player) {
            return !ShipEntity.this.isRemoved() && player.squaredDistanceTo(ShipEntity.this) < 100.0;
        }
    };

    private int portCooldown;
    private int starboardCooldown;
    private int aiFireCooldown = 60;
    private int gunnerCooldown;
    @Nullable private UUID lastAttacker;
    @Nullable private Vec3d lastServerPos;
    private double speedBlocksPerSecond;
    private int lonelyTicks;
    @Nullable private UUID provoker;
    private int provokedTicks;
    private float patrolYaw = Float.NaN;
    @Nullable private Entity cachedTarget;
    private long lastWageDay = -1;
    /** Crew roles waiting to come aboard after launching from a deed. */
    private int[] pendingCrew = new int[0];

    public ShipEntity(EntityType<? extends BoatEntity> type, World world) {
        super(type, world);
        this.setCustomNameVisible(true);
    }

    // ================================================================== data

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(HULL, (float) ShipClass.SLOOP.maxHull());
        builder.add(SAIL, 0);
        builder.add(CLASS, 0);
        builder.add(FACTION, 0);
        builder.add(LOOK, 0);
        builder.add(SAIL_SKILL, 0);
        builder.add(OWNER, Optional.empty());
        builder.add(UPGRADES, 0);
        builder.add(SAIL_COLOR, -1);
        builder.add(FIGUREHEAD, 0);
        builder.add(EMBLEM, 0);
        builder.add(AMMO, 0);
        builder.add(NAVIGATORS, 0);
        builder.add(TORN, 0);
        builder.add(FLAG, 0);
        builder.add(ANCHORED, false);
    }

    public com.cursedtides.ship.FigureheadItem.Kind getFigurehead() {
        return com.cursedtides.ship.FigureheadItem.Kind.byIndex(this.dataTracker.get(FIGUREHEAD));
    }
    public void setFigurehead(com.cursedtides.ship.FigureheadItem.Kind kind) { this.dataTracker.set(FIGUREHEAD, kind.ordinal()); }
    public int getFlag() { return this.dataTracker.get(FLAG); }
    /** Flag design painted on the mainsail, plus one; 0 for plain canvas. */
    public int getEmblem() { return this.dataTracker.get(EMBLEM); }
    /** Spotted by a sea eagle: glows for ten seconds. */
    public void addStatusEffectGlow() {
        this.setGlowing(true);
        spottedUntil = this.age + 200;
    }
    private int spottedUntil = -1;
    public void setFlag(int flag) { this.dataTracker.set(FLAG, flag); }
    public boolean isAnchored() { return this.dataTracker.get(ANCHORED); }
    public void setAnchored(boolean anchored) { this.dataTracker.set(ANCHORED, anchored); }

    private int lastSwivel = -100;

    /** A named legend of the deep: armoured hull, extra guns, and half damage from everything. */
    public void makeLegendary(String name) {
        setShipClass(ShipClass.GALLEON);
        setUpgrade(ShipUpgradeItem.Kind.PLATING, 3);
        setUpgrade(ShipUpgradeItem.Kind.GUNS, 2);
        setHull(maxHull());
        setFlag(com.cursedtides.ship.FlagItem.Design.KRAKEN.ordinal());
        setCustomName(Text.literal(name).formatted(Formatting.DARK_AQUA, Formatting.BOLD));
        calculateDimensions();
        addCommandTag("cursedtides_legendary");
    }

    /** Drops or weighs anchor. Only the owner (or crew) may. */
    public void toggleAnchor(ServerPlayerEntity player) {
        boolean down = !isAnchored();
        setAnchored(down);
        if (down) setSail(0);
        this.getWorld().playSound(null, getX(), getY(), getZ(), down ? SoundEvents.BLOCK_CHAIN_PLACE : SoundEvents.BLOCK_CHAIN_BREAK,
                SoundCategory.PLAYERS, 1.2f, 0.6f);
        player.sendMessage(Text.literal(down ? "Anchor's down. She'll hold here." : "Anchor's aweigh!").formatted(Formatting.GRAY), true);
    }

    /** A rail-mounted swivel gun: a cone of grapeshot along the gunner's aim. Uses one grapeshot. */
    public void fireSwivel(ServerPlayerEntity gunner) {
        if (upgrade(ShipUpgradeItem.Kind.SWIVEL) == 0) {
            gunner.sendMessage(Text.literal("No swivel gun is mounted. A shipwright sells them.").formatted(Formatting.GRAY), true);
            return;
        }
        if (this.age - lastSwivel < 30) return;
        boolean paid = false;
        if (gunner.getAbilities().creativeMode) paid = true;
        for (net.minecraft.inventory.Inventory inv : new net.minecraft.inventory.Inventory[]{gunner.getInventory(), cargo}) {
            for (int i = 0; i < inv.size() && !paid; i++) {
                if (inv.getStack(i).isOf(ModItems.GRAPESHOT)) {
                    inv.getStack(i).decrement(1);
                    paid = true;
                }
            }
        }
        if (!paid) {
            gunner.sendMessage(Text.literal("The swivel needs grapeshot (in your pack or the hold).").formatted(Formatting.RED), true);
            return;
        }
        lastSwivel = this.age;
        ServerWorld world = (ServerWorld) this.getWorld();
        net.minecraft.util.math.Vec3d eye = gunner.getEyePos();
        net.minecraft.util.math.Vec3d look = gunner.getRotationVec(1.0f);
        world.playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, 0.8f, 1.6f);
        for (int i = 1; i < 14; i++) {
            net.minecraft.util.math.Vec3d p = eye.add(look.multiply(i * 2.0));
            world.spawnParticles(ParticleTypes.SMOKE, p.x, p.y, p.z, 2, 0.3 * i / 6, 0.2, 0.3 * i / 6, 0.01);
        }
        var box = gunner.getBoundingBox().stretch(look.multiply(28)).expand(6);
        for (Entity e : world.getOtherEntities(gunner, box, e -> e.isAlive() && e != this && !this.hasPassenger(e))) {
            net.minecraft.util.math.Vec3d to = e.getBoundingBox().getCenter().subtract(eye);
            double dist = to.length();
            if (dist > 28 || dist < 0.5) continue;
            double angle = Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, to.normalize().dotProduct(look)))));
            if (angle > 20) continue;
            if (e instanceof PlayerEntity other && com.cursedtides.social.CrewState.shipmates(world.getServer(), gunner.getUuid(), other.getUuid())) continue;
            if (!(e instanceof ShipEntity) && !gunner.canSee(e)) continue;
            float dmg = angle < 10 ? 7.0f : 4.0f;
            if (dist > 18) dmg *= 0.6f;
            e.damage(world.getDamageSources().playerAttack(gunner), e instanceof ShipEntity ? dmg * 1.5f : dmg);
        }
        com.cursedtides.pirate.PirateData.addXp(gunner, com.cursedtides.pirate.Weapon.CANNON, 2);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (CLASS.equals(data)) {
            this.calculateDimensions();
        }
    }

    public ShipClass shipClass() { return ShipClass.byIndex(this.dataTracker.get(CLASS)); }
    public void setShipClass(ShipClass cls) { this.dataTracker.set(CLASS, cls.ordinal()); }
    public float getHull() { return this.dataTracker.get(HULL); }
    public void setHull(float hull) { this.dataTracker.set(HULL, MathHelper.clamp(hull, 0.0f, maxHull())); }

    /** Damage from the sea itself (whirlpools, reefs, running aground). Tells the crew and sinks her if it must. */
    public void seaDamage(ServerWorld world, float amount, String message) {
        setHull(getHull() - amount);
        for (Entity passenger : this.getPassengerList()) passenger.sendMessage(Text.literal(message).formatted(Formatting.YELLOW));
        world.playSound(null, getBlockPos(), net.minecraft.sound.SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, net.minecraft.sound.SoundCategory.NEUTRAL, 1.0f, 0.6f);
        if (getHull() <= 0) sink(world);
    }
    public int getSail() { return this.dataTracker.get(SAIL); }
    public void setSail(int sail) { this.dataTracker.set(SAIL, MathHelper.clamp(sail, 0, 2)); }
    public ShipFaction getFaction() { return ShipFaction.byIndex(this.dataTracker.get(FACTION)); }
    public void setFaction(ShipFaction faction) { this.dataTracker.set(FACTION, faction.ordinal()); }
    /** Any self-steering ship (ghost or navy). */
    public boolean isHostile() { return getFaction() != ShipFaction.PLAYER; }
    public int getLook() { return this.dataTracker.get(LOOK); }
    public void setLook(int look) { this.dataTracker.set(LOOK, look); }
    public boolean isGhost() { return getLook() == LOOK_GHOST; }
    public int getSailSkill() { return this.dataTracker.get(SAIL_SKILL); }
    @Nullable public UUID getOwner() { return this.dataTracker.get(OWNER).orElse(null); }
    public void setOwner(@Nullable UUID owner) { this.dataTracker.set(OWNER, Optional.ofNullable(owner)); }
    public int getSailColor() { return this.dataTracker.get(SAIL_COLOR); }
    public ShipAmmo getAmmo() { return ShipAmmo.byIndex(this.dataTracker.get(AMMO)); }
    public boolean isTorn() { return this.dataTracker.get(TORN) > 0; }
    public SimpleInventory cargo() { return cargo; }

    public void tearRigging(int ticks) {
        this.dataTracker.set(TORN, Math.max(this.dataTracker.get(TORN), ticks));
        if (getSail() > 1) setSail(1);
    }

    // ---- upgrades: 2 bits each
    public int upgrade(ShipUpgradeItem.Kind kind) {
        return (this.dataTracker.get(UPGRADES) >> (kind.ordinal() * 2)) & 3;
    }

    private void setUpgrade(ShipUpgradeItem.Kind kind, int level) {
        int shift = kind.ordinal() * 2;
        int packed = this.dataTracker.get(UPGRADES) & ~(3 << shift);
        this.dataTracker.set(UPGRADES, packed | ((level & 3) << shift));
    }

    public int maxHull() {
        return Math.round(shipClass().maxHull() * (1.0f + 0.3f * upgrade(ShipUpgradeItem.Kind.PLATING))
                * (getFigurehead() == com.cursedtides.ship.FigureheadItem.Kind.LION ? 1.15f : 1.0f));
    }

    public int guns() {
        return shipClass().cannonsPerSide() + upgrade(ShipUpgradeItem.Kind.GUNS)
                + (getFigurehead() == com.cursedtides.ship.FigureheadItem.Kind.SKULL ? 1 : 0);
    }

    public int cargoRows() {
        return Math.min(6, shipClass().cargoRows() + upgrade(ShipUpgradeItem.Kind.HOLD));
    }

    private double speedFactor() {
        return shipClass().speed() * (1.0 + getSailSkill() * 0.01) * (1.0 + 0.08 * upgrade(ShipUpgradeItem.Kind.CANVAS))
                * (1.0 + 0.10 * this.dataTracker.get(NAVIGATORS))
                * (getFigurehead() == com.cursedtides.ship.FigureheadItem.Kind.MERMAID ? 1.10 : 1.0);
    }

    public boolean isOwner(PlayerEntity player) {
        UUID owner = getOwner();
        return owner == null || owner.equals(player.getUuid());
    }

    public Text shipName() {
        return this.hasCustomName() ? this.getCustomName() : Text.literal("Unnamed " + shipClass().displayName());
    }

    // ================================================================== size and seats

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        return super.getDimensions(pose).scaled(shipClass().scale());
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
        for (Entity passenger : this.getPassengerList()) {
            if (passenger instanceof PlayerEntity player && isOwner(player)) {
                return player;
            }
        }
        return null;
    }

    @Override
    protected Vec3d getPassengerAttachmentPos(Entity passenger, EntityDimensions dimensions, float scaleFactor) {
        float s = shipClass().scale();
        double along, side;
        if (passenger == getControllingPassenger()) {
            along = -0.9;
            side = 0.0;
        } else {
            int index = 0;
            for (Entity other : this.getPassengerList()) {
                if (other == passenger) break;
                if (other != getControllingPassenger()) index++;
            }
            along = 0.7 - (index / 2) * 0.6;
            side = index % 2 == 0 ? 0.35 : -0.35;
        }
        return new Vec3d(side * s, 0.2 * s, along * s).rotateY(-this.getYaw() * MathHelper.RADIANS_PER_DEGREE);
    }

    @Override
    public Item asItem() {
        return shipClass().deed();
    }

    // ================================================================== movement

    private boolean onWater() {
        World world = this.getWorld();
        return world.getFluidState(this.getBlockPos()).isIn(FluidTags.WATER)
                || world.getFluidState(this.getBlockPos().down()).isIn(FluidTags.WATER);
    }

    @Override
    public void tick() {
        if (spottedUntil > 0 && this.age > spottedUntil) {
            spottedUntil = -1;
            this.setGlowing(false);
        }
        super.tick();
        World world = this.getWorld();

        if (this.isLogicalSideForUpdatingMovement() && onWater()) {
            if (isHostile()) {
                if (world instanceof ServerWorld serverWorld) {
                    hostileAi(serverWorld);
                }
            } else if (isAnchored()) {
                // Riding at anchor: she swings but doesn't go anywhere
                this.setVelocity(this.getVelocity().multiply(0.0, 1.0, 0.0));
            } else if (getSail() > 0 && getControllingPassenger() != null) {
                applySail(getSail() == 1 ? 0.55 : 1.0);
                if (world.isThundering() && this.random.nextInt(4) == 0) {
                    this.setYaw(this.getYaw() + (this.random.nextFloat() - 0.5f) * 3.0f); // storm gusts
                }
            }
        }

        if (world.isClient) {
            if (getHull() < maxHull() * 0.3f && this.random.nextInt(3) == 0) {
                world.addParticle(ParticleTypes.LARGE_SMOKE, getX() + random.nextGaussian() * 0.5,
                        getY() + 0.6 * shipClass().scale(), getZ() + random.nextGaussian() * 0.5, 0, 0.05, 0);
            }
            return;
        }
        serverTick((ServerWorld) world);
    }

    private void applySail(double sailAmount) {
        World world = this.getWorld();
        long time = world.getTime();
        double weather = world.isThundering() ? 1.35 : world.isRaining() ? 1.1 : 1.0;
        double torn = isTorn() ? 0.4 : 1.0;
        // Water drag takes a tenth of her way each tick, so she settles at about nine times this push: for a sloop
        // under full sail in a fair wind, some 25-30 blocks a second, as fast as the old pirate games' ships
        double accel = 0.2 * sailAmount * Wind.efficiency(this.getYaw(), time) * Wind.strength(time)
                * speedFactor() * weather * torn;
        Vec3d forward = Vec3d.fromPolar(0.0f, this.getYaw());
        Vec3d v = this.getVelocity().add(forward.multiply(accel));
        double max = 1.2 * speedFactor() * torn;
        double horizontal = Math.hypot(v.x, v.z);
        if (horizontal > max) {
            v = new Vec3d(v.x / horizontal * max, v.y, v.z / horizontal * max);
        }
        this.setVelocity(v);
    }

    // ================================================================== hostile AI

    /**
     * Ghost ships hunt any player, then Tidewatch ships. Tidewatch ships hunt whoever provoked
     * them and any wanted player, then ghost ships; otherwise they patrol.
     */
    @Nullable
    private Entity chooseTarget(ServerWorld world) {
        if (getFaction() == ShipFaction.DROWNED || getFaction() == ShipFaction.PIRATE) {
            boolean ghost = getFaction() == ShipFaction.DROWNED;
            PlayerEntity player = world.getClosestPlayer(getX(), getY(), getZ(), 90.0,
                    e -> e instanceof PlayerEntity p && !p.isSpectator() && !p.isCreative()
                            && !(ghost && com.cursedtides.quest.HorizonArc.ending(p) == com.cursedtides.quest.HorizonArc.CLAIMED));
            return player != null ? player : nearestShip(world, s -> s.getFaction() == ShipFaction.NAVY, 70.0);
        }
        if (provokedTicks > 0 && provoker != null && world.getPlayerByUuid(provoker) instanceof PlayerEntity angry
                && angry.squaredDistanceTo(this) < 120 * 120) {
            return angry;
        }
        PlayerEntity outlaw = world.getClosestPlayer(getX(), getY(), getZ(), 90.0,
                e -> e instanceof PlayerEntity p && !p.isSpectator() && !p.isCreative() && FactionData.isWanted(p));
        return outlaw != null ? outlaw : nearestShip(world,
                s -> s.getFaction() == ShipFaction.DROWNED || s.getFaction() == ShipFaction.PIRATE, 80.0);
    }

    @Nullable
    private ShipEntity nearestShip(ServerWorld world, java.util.function.Predicate<ShipEntity> filter, double range) {
        ShipEntity best = null;
        double bestDist = range * range;
        for (ShipEntity other : world.getEntitiesByClass(ShipEntity.class, getBoundingBox().expand(range),
                s -> s != this && filter.test(s))) {
            double d = other.squaredDistanceTo(this);
            if (d < bestDist) {
                bestDist = d;
                best = other;
            }
        }
        return best;
    }

    /** If set, a hostile ship makes for this point, fighting what it meets on the way (Cannon Defense). */
    public Vec3d forcedDestination;

    private void hostileAi(ServerWorld world) {
        if (com.cursedtides.ship.Boarding.isLashed(this)) {
            this.setVelocity(this.getVelocity().multiply(0, 1, 0));
            return; // lashed alongside: no sailing, no broadsides
        }
        if (forcedDestination != null) {
            double dx = forcedDestination.x - getX(), dz = forcedDestination.z - getZ();
            float desired = (float) Math.toDegrees(Math.atan2(-dx, dz));
            float diff = MathHelper.wrapDegrees(desired - this.getYaw());
            this.setYaw(this.getYaw() + MathHelper.clamp(diff, -2.5f, 2.5f));
            setSail(2);
            applySail(0.7);
            if (--aiFireCooldown <= 0 && hasLivingCrew()) {
                PlayerEntity near = world.getClosestPlayer(getX(), getY(), getZ(), 40, false);
                if (near != null) {
                    int side = rightVector().dotProduct(near.getPos().subtract(getPos())) >= 0 ? 1 : -1;
                    fireBroadside(world, side, null, near.getPos(), ShipAmmo.ROUND);
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
        Entity target = (this.age % 20 == 0 || cachedTarget == null || cachedTarget.isRemoved())
                ? (cachedTarget = chooseTarget(world)) : cachedTarget;
        float desired;
        if (target == null) {
            if (getFaction() == ShipFaction.NAVY) {
                if (Float.isNaN(patrolYaw)) patrolYaw = this.random.nextFloat() * 360.0f;
                patrolYaw += (this.random.nextFloat() - 0.5f) * 0.6f;
                desired = patrolYaw;
            } else {
                desired = Wind.yaw(world.getTime());
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
                int side = rightVector().dotProduct(new Vec3d(dx, 0, dz)) >= 0 ? 1 : -1;
                ShipAmmo shot = getFaction() == ShipFaction.NAVY && this.random.nextInt(3) == 0 ? ShipAmmo.CHAIN : ShipAmmo.ROUND;
                fireBroadside(world, side, null, target.getPos().add(target.getVelocity().multiply(10)), shot);
                aiFireCooldown = 100 + this.random.nextInt(40);
            }
        }
        float diff = MathHelper.wrapDegrees(desired - this.getYaw());
        this.setYaw(this.getYaw() + MathHelper.clamp(diff, -2.0f, 2.0f));
        applySail(target == null ? 0.4 : 0.8);
    }

    /** Traders run before the wind, away from anyone who comes close. */
    private void merchantAi(ServerWorld world) {
        PlayerEntity near = world.getClosestPlayer(getX(), getY(), getZ(), 48.0,
                e -> e instanceof PlayerEntity p && !p.isSpectator());
        float desired;
        if (near != null) {
            desired = (float) Math.toDegrees(Math.atan2(-(getX() - near.getX()), getZ() - near.getZ()));
            setSail(2);
        } else {
            if (Float.isNaN(patrolYaw)) patrolYaw = this.random.nextFloat() * 360.0f;
            desired = patrolYaw;
            setSail(1);
        }
        float diff = MathHelper.wrapDegrees(desired - this.getYaw());
        this.setYaw(this.getYaw() + MathHelper.clamp(diff, -1.5f, 1.5f));
        applySail(near != null ? 0.7 : 0.4);
    }

    // ================================================================== server upkeep

    private void serverTick(ServerWorld world) {
        if (portCooldown > 0) portCooldown--;
        if (starboardCooldown > 0) starboardCooldown--;
        if (gunnerCooldown > 0) gunnerCooldown--;
        int torn = this.dataTracker.get(TORN);
        if (torn > 0) this.dataTracker.set(TORN, torn - 1);

        if (lastServerPos != null) {
            speedBlocksPerSecond = speedBlocksPerSecond * 0.8
                    + 0.2 * Math.hypot(getX() - lastServerPos.x, getZ() - lastServerPos.z) * 20.0;
        }
        lastServerPos = getPos();

        if (pendingCrew.length > 0) {
            boardPendingCrew(world);
        }

        if (this.age % 20 == 0) {
            if (getControllingPassenger() instanceof PlayerEntity captain) {
                this.dataTracker.set(SAIL_SKILL, PirateData.level(captain, Weapon.SAILING)
                        + (int) com.cursedtides.pirate.PerkData.stat(captain, com.cursedtides.pirate.Perk.Stat.SAIL_SPEED));
            }
            this.dataTracker.set(NAVIGATORS, crewCount(CrewmateEntity.NAVIGATOR));
        }

        if (!isHostile()) {
            crewDuties(world);
            payWages(world);
        }

        if (this.age % 10 == 0) {
            for (Entity passenger : this.getPassengerList()) {
                if (passenger instanceof ServerPlayerEntity player) {
                    player.sendMessage(hud(world), true);
                }
            }
        }

        // Storms: lightning finds tall masts
        if (this.age % 200 == 0 && world.isThundering() && this.hasPassengers() && this.random.nextInt(8) == 0) {
            LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(world);
            if (bolt != null) {
                bolt.refreshPositionAfterTeleport(getPos().add(this.random.nextGaussian() * 6, 0, this.random.nextGaussian() * 6));
                bolt.setCosmetic(true);
                world.spawnEntity(bolt);
            }
            if (this.random.nextInt(3) == 0) {
                setHull(getHull() - 12.0f);
                tearRigging(300);
                for (Entity passenger : this.getPassengerList()) {
                    passenger.sendMessage(Text.literal("Lightning splinters the mast!").formatted(Formatting.YELLOW));
                }
                if (getHull() <= 0) {
                    sink(world);
                    return;
                }
            }
        }

        if (isHostile() && this.age % 100 == 0) {
            boolean playerNear = world.getClosestPlayer(this, 160.0) != null;
            lonelyTicks = playerNear ? 0 : lonelyTicks + 100;
            if (lonelyTicks >= 1200) {
                this.getPassengerList().forEach(Entity::discard);
                this.discard();
            }
        }
    }

    private Text hud(ServerWorld world) {
        long t = world.getTime();
        int max = maxHull();
        float hull = getHull();
        Formatting hullColor = hull > max * 0.6f ? Formatting.GREEN : hull > max * 0.3f ? Formatting.YELLOW : Formatting.RED;
        String sails = switch (getSail()) {
            case 0 -> "Furled";
            case 1 -> "Half";
            default -> "Full";
        };
        if (isTorn()) sails += " (torn)";
        String weather = world.isThundering() ? "Storm" : world.isRaining() ? "Rain" : "";
        double knots = speedBlocksPerSecond * 1.94;
        return Text.empty()
                .append(shipName().copy().formatted(Formatting.GOLD))
                .append(Text.literal("  Hull ").formatted(Formatting.GRAY))
                .append(Text.literal((int) Math.ceil(hull) + "/" + max).formatted(hullColor))
                .append(Text.literal("  Sails ").formatted(Formatting.GRAY))
                .append(Text.literal(sails).formatted(isTorn() ? Formatting.RED : Formatting.WHITE))
                .append(Text.literal("  Shot ").formatted(Formatting.GRAY))
                .append(Text.literal(getAmmo().displayName()).formatted(Formatting.WHITE))
                .append(Text.literal("  Wind ").formatted(Formatting.GRAY))
                .append(Text.literal(Wind.fromName(t) + " " + Wind.strengthName(t) + ", " + Wind.pointOfSail(getYaw(), t))
                        .formatted(Formatting.AQUA))
                .append(Text.literal(weather.isEmpty() ? "" : "  " + weather).formatted(Formatting.DARK_AQUA))
                .append(Text.literal(String.format("  %.1f kn", knots)).formatted(Formatting.WHITE));
    }

    // ================================================================== crew

    private int crewCount(int role) {
        int n = 0;
        for (Entity e : this.getPassengerList()) {
            if (e instanceof CrewmateEntity c && c.isAlive() && c.getRole() == role) n++;
        }
        return n;
    }

    private List<CrewmateEntity> crew() {
        List<CrewmateEntity> list = new ArrayList<>();
        for (Entity e : this.getPassengerList()) {
            if (e instanceof CrewmateEntity c && c.isAlive()) list.add(c);
        }
        return list;
    }

    private void crewDuties(ServerWorld world) {
        // Bosuns patch the hull from planks in the hold
        int bosuns = crewCount(CrewmateEntity.BOSUN);
        if (bosuns > 0 && this.age % 100 == 0 && getHull() < maxHull()) {
            for (int i = 0; i < cargo.size(); i++) {
                ItemStack stack = cargo.getStack(i);
                if (stack.isIn(ItemTags.PLANKS)) {
                    stack.decrement(1);
                    setHull(getHull() + 6.0f * bosuns);
                    world.playSound(null, getX(), getY(), getZ(), SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.NEUTRAL, 0.6f, 1.2f);
                    break;
                }
            }
        }
        // Gunners fire on enemy ships by themselves
        int gunners = crewCount(CrewmateEntity.GUNNER);
        if (gunners > 0 && gunnerCooldown <= 0 && this.age % 20 == 0) {
            boolean ownerWanted = getOwner() != null && world.getPlayerByUuid(getOwner()) instanceof PlayerEntity owner
                    && FactionData.isWanted(owner);
            ShipEntity enemy = nearestShip(world, s -> s.getFaction() == ShipFaction.DROWNED
                    || s.getFaction() == ShipFaction.PIRATE
                    || (s.getFaction() == ShipFaction.NAVY && (ownerWanted || s.provoker != null && s.provoker.equals(getOwner()))), 36.0);
            if (enemy != null && takeAmmo(null, getAmmo(), Math.max(1, guns() / 2), true)) {
                int side = rightVector().dotProduct(enemy.getPos().subtract(getPos())) >= 0 ? 1 : -1;
                PlayerEntity owner = getOwner() == null ? null : world.getPlayerByUuid(getOwner());
                fireBroadside(world, side, owner, enemy.getPos(), getAmmo());
                gunnerCooldown = Math.max(40, 110 - gunners * 15);
            }
        }
    }

    /** Two doubloons a day per hand, from the hold. Unpaid crew walk off the ship. */
    private void payWages(ServerWorld world) {
        long day = world.getTimeOfDay() / 24000L;
        if (lastWageDay < 0) {
            lastWageDay = day;
            return;
        }
        if (day == lastWageDay) {
            return;
        }
        lastWageDay = day;
        PlayerEntity owner = getOwner() == null ? null : world.getPlayerByUuid(getOwner());
        for (CrewmateEntity hand : crew()) {
            if (cargo.count(ModItems.DOUBLOON) >= 2) {
                takeFromCargo(ModItems.DOUBLOON, 2);
            } else {
                if (owner != null) {
                    owner.sendMessage(Text.literal("Unpaid, ").append(hand.getName())
                            .append(Text.literal(" has left the crew of the ").append(shipName()).append(Text.literal(".")))
                            .formatted(Formatting.RED), false);
                }
                hand.discard();
            }
        }
    }

    private void boardPendingCrew(ServerWorld world) {
        int[] roles = pendingCrew;
        pendingCrew = new int[0];
        for (int role : roles) {
            signOn(world, role);
        }
    }

    private boolean signOn(ServerWorld world, int role) {
        if (this.getPassengerList().size() >= getMaxPassengers()) {
            return false;
        }
        CrewmateEntity hand = ModEntities.CREWMATE.create(world);
        if (hand == null) return false;
        hand.refreshPositionAndAngles(getX(), getY() + 0.5, getZ(), getYaw(), 0.0f);
        hand.initialize(world, world.getLocalDifficulty(getBlockPos()), SpawnReason.EVENT, null);
        hand.setRole(role);
        hand.setPersistent();
        world.spawnEntity(hand);
        hand.startRiding(this, true);
        return true;
    }

    // ================================================================== cannons

    /** Starboard (right-hand) direction for the current heading. */
    private Vec3d rightVector() {
        Vec3d forward = Vec3d.fromPolar(0.0f, this.getYaw());
        return new Vec3d(-forward.z, 0.0, forward.x);
    }

    /**
     * Fires every gun on one side. side = +1 starboard, -1 port.
     * aimAt = null fires straight out; otherwise guns lay toward that point.
     */
    public void fireBroadside(ServerWorld world, int side, @Nullable PlayerEntity gunner, @Nullable Vec3d aimAt, ShipAmmo ammo) {
        int guns = isHostile() ? shipClass().cannonsPerSide() : guns();
        float s = shipClass().scale();
        Vec3d forward = Vec3d.fromPolar(0.0f, this.getYaw());
        Vec3d out = rightVector().multiply(side);
        int pellets = ammo == ShipAmmo.GRAPE ? 3 : 1;
        for (int i = 0; i < guns; i++) {
            double along = (guns == 1 ? 0.0 : (i / (double) (guns - 1) - 0.5)) * 1.8 * s;
            Vec3d origin = this.getPos().add(forward.multiply(along)).add(out.multiply(1.0 * s + 0.5)).add(0, 0.55 * s, 0);
            for (int p = 0; p < pellets; p++) {
                Vec3d dir;
                float spread;
                if (aimAt != null) {
                    Vec3d d = aimAt.subtract(origin);
                    double dist = Math.max(1.0, Math.hypot(d.x, d.z));
                    dir = new Vec3d(d.x / dist, 0.05 + dist * 0.0035, d.z / dist);
                    spread = 4.0f;
                } else {
                    dir = new Vec3d(out.x, 0.1, out.z);
                    spread = 2.0f;
                }
                if (ammo == ShipAmmo.GRAPE) spread += 8.0f;
                CannonballEntity ball = new CannonballEntity(world, origin.x, origin.y, origin.z);
                ball.setAmmo(ammo);
                ball.setOwner(gunner != null ? gunner : this);
                ball.setVelocity(dir.x, dir.y, dir.z, ammo == ShipAmmo.GRAPE ? 1.5f : 1.75f, spread);
                world.spawnEntity(ball);
            }
            world.spawnParticles(ParticleTypes.LARGE_SMOKE, origin.x, origin.y, origin.z, 8, 0.25, 0.25, 0.25, 0.02);
            world.spawnParticles(ParticleTypes.FLAME, origin.x, origin.y, origin.z, 3, 0.1, 0.1, 0.1, 0.01);
        }
        world.playSound(null, getX(), getY(), getZ(), com.cursedtides.sound.ModSounds.CANNON_BOOM.value(), SoundCategory.PLAYERS, 2.0f, 0.9f + this.random.nextFloat() * 0.2f);
        world.playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST,
                SoundCategory.NEUTRAL, 3.0f, 0.35f);
    }

    /** Broadside ordered by a player aboard (any crew member can serve as gunner). */
    public void playerBroadside(ServerPlayerEntity gunner, int side) {
        if (isHostile() || gunner.getVehicle() != this || !(this.getWorld() instanceof ServerWorld world)) {
            return;
        }
        int cooldown = side > 0 ? starboardCooldown : portCooldown;
        if (cooldown > 0) {
            gunner.sendMessage(Text.literal((side > 0 ? "Starboard" : "Port") + " guns reloading... "
                    + (cooldown / 20 + 1) + "s").formatted(Formatting.GRAY), true);
            return;
        }
        ShipAmmo ammo = getAmmo();
        boolean free = this.random.nextDouble() < com.cursedtides.pirate.PerkData.stat(gunner,
                com.cursedtides.pirate.Perk.Stat.BROADSIDE_SAVE);
        if (!free && !takeAmmo(gunner, ammo, Math.max(1, guns() / 2), false)) {
            gunner.sendMessage(Text.literal("No " + ammo.displayName().toLowerCase() + " in the hold or your pockets!")
                    .formatted(Formatting.RED), true);
            return;
        }
        fireBroadside(world, side, gunner, null, ammo);
        int reload = Math.max(30, 80 - PirateData.level(gunner, Weapon.CANNON) * 2);
        reload = (int) Math.max(15, Math.round(reload * (1.0 - com.cursedtides.pirate.PerkData.stat(gunner,
                com.cursedtides.pirate.Perk.Stat.CANNON_RELOAD))));
        if (side > 0) starboardCooldown = reload; else portCooldown = reload;
    }

    public void cycleAmmo(ServerPlayerEntity player) {
        ShipAmmo next = ShipAmmo.byIndex((getAmmo().ordinal() + 1) % ShipAmmo.values().length);
        this.dataTracker.set(AMMO, next.ordinal());
        player.sendMessage(Text.literal("Guns loaded with " + next.displayName()).formatted(Formatting.GOLD), true);
    }

    private boolean takeAmmo(@Nullable PlayerEntity player, ShipAmmo ammo, int count, boolean holdOnly) {
        if (player != null && player.getAbilities().creativeMode) {
            return true;
        }
        Item item = ammo.item();
        int available = cargo.count(item) + (holdOnly || player == null ? 0 : player.getInventory().count(item));
        if (available < count) {
            return false;
        }
        int left = takeFromCargo(item, count);
        if (left > 0 && player != null) {
            var inv = player.getInventory();
            for (int i = 0; i < inv.size() && left > 0; i++) {
                ItemStack stack = inv.getStack(i);
                if (stack.isOf(item)) {
                    int take = Math.min(left, stack.getCount());
                    stack.decrement(take);
                    left -= take;
                }
            }
        }
        return true;
    }

    /** Removes up to count of item from the hold; returns how many it couldn't find. */
    private int takeFromCargo(Item item, int count) {
        int left = count;
        for (int i = 0; i < cargo.size() && left > 0; i++) {
            ItemStack stack = cargo.getStack(i);
            if (stack.isOf(item)) {
                int take = Math.min(left, stack.getCount());
                stack.decrement(take);
                left -= take;
            }
        }
        return left;
    }

    // ================================================================== damage, sinking, capture

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source) || this.isRemoved()) {
            return false;
        }
        if (this.getWorld().isClient) {
            return true;
        }
        if (this.getCommandTags().contains("cursedtides_legendary")) {
            amount *= 0.5f;
        }
        Entity attacker = source.getAttacker();
        // Leveled crews: a tough ship shrugs off more, and a tough ship's guns bite harder
        int myLevel = com.cursedtides.combat.Levels.levelOf(this);
        if (myLevel > 0) amount /= (float) (1 + 0.05 * (myLevel - 1));
        else if (attacker != null && com.cursedtides.combat.Levels.levelOf(attacker) > 0) {
            amount *= (float) com.cursedtides.combat.Levels.damageFactor(com.cursedtides.combat.Levels.levelOf(attacker));
        }
        if (attacker == this || (attacker != null && attacker.getVehicle() == this)) {
            return false; // your own crew and your own guns don't hole your hull
        }
        if (attacker instanceof PlayerEntity player) {
            lastAttacker = player.getUuid();
            if (getFaction() == ShipFaction.NAVY) {
                provoker = player.getUuid();
                provokedTicks = 2400;
                cachedTarget = player;
                if (player instanceof ServerPlayerEntity sp && !FactionData.isWanted(sp)) {
                    FactionEvents.onAttackedNavy(sp);
                }
            }
        }
        this.setDamageWobbleSide(-this.getDamageWobbleSide());
        this.setDamageWobbleTicks(10);
        this.setDamageWobbleStrength(this.getDamageWobbleStrength() + amount * 2.0f);
        float hullDamage = source.isIn(DamageTypeTags.IS_EXPLOSION) ? amount * 1.5f : amount;
        if (com.cursedtides.quest.MusterArc.isFlagship(this) && getHull() - hullDamage < maxHull() * 0.2f) {
            setHull(maxHull() * 0.2f);
            if (attacker instanceof PlayerEntity pl) pl.sendMessage(Text.literal("The curse holds her timbers together. Grapple her and board!")
                    .formatted(Formatting.DARK_AQUA), true);
            return true;
        }
        setHull(getHull() - hullDamage);
        this.getWorld().playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR,
                SoundCategory.NEUTRAL, 0.7f, 0.7f + this.random.nextFloat() * 0.3f);
        if (getHull() <= 0.0f) {
            sink((ServerWorld) this.getWorld());
        }
        return true;
    }

    private void sink(ServerWorld world) {
        if (this.isRemoved()) return;
        world.playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.NEUTRAL, 2.0f, 0.5f);
        world.spawnParticles(ParticleTypes.SPLASH, getX(), getY() + 0.5, getZ(), 60, 1.5, 0.4, 1.5, 0.1);
        world.spawnParticles(ParticleTypes.BUBBLE_COLUMN_UP, getX(), getY(), getZ(), 40, 1.0, 0.3, 1.0, 0.1);
        ItemScatterer.spawn(world, this, cargo);
        this.dropStack(new ItemStack(Items.OAK_PLANKS, 4 + shipClass().ordinal() * 3));

        if (isHostile()) {
            this.dropStack(new ItemStack(ModItems.DOUBLOON, 10 + this.random.nextInt(15)));
            this.dropStack(new ItemStack(ModItems.CANNONBALL, 2 + this.random.nextInt(4)));
            if (this.random.nextInt(4) == 0) this.dropStack(new ItemStack(Items.HEART_OF_THE_SEA));
            if (this.random.nextInt(3) == 0) this.dropStack(new ItemStack(ModItems.TREASURE_MAP));
            if (this.random.nextInt(5) == 0) this.dropStack(new ItemStack(ModItems.MAP_FRAGMENT));
            if (getFaction() == ShipFaction.DROWNED && this.random.nextInt(3) == 0) {
                this.dropStack(new ItemStack(com.cursedtides.trade.TradeGood.CURSED_IDOL.item()));
            }
            if (getFaction() == ShipFaction.NAVY) {
                this.dropStack(new ItemStack(ModItems.PISTOL_SHOT, 6 + this.random.nextInt(8)));
                this.dropStack(new ItemStack(ModItems.CHAIN_SHOT, 1 + this.random.nextInt(3)));
                if (this.random.nextInt(5) == 0) this.dropStack(new ItemStack(ModItems.MUSKET));
            }
            if (lastAttacker != null && world.getPlayerByUuid(lastAttacker) instanceof ServerPlayerEntity sinker) {
                PirateData.addXp(sinker, Weapon.CANNON, 60);
                FactionEvents.onShipSunk(sinker, getFaction());
                com.cursedtides.quest.HorizonArc.onShipSunk(sinker, this);
                com.cursedtides.quest.Contracts.onShipSunk(sinker, com.cursedtides.combat.Levels.levelOf(this));
                com.cursedtides.sea.SeaEvents.onSunk(world, this, sinker);
                com.cursedtides.quest.MusterArc.onSunk(sinker, this);
            }
            for (ServerPlayerEntity p : world.getPlayers(p -> p.squaredDistanceTo(this) < 80 * 80)) {
                p.sendMessage(Text.literal("The ").append(shipName()).append(Text.literal(" slips beneath the waves!"))
                        .formatted(Formatting.AQUA), false);
            }
            this.getPassengerList().forEach(Entity::discard);
        } else {
            UUID owner = getOwner();
            if (owner != null && world.getPlayerByUuid(owner) instanceof ServerPlayerEntity captain) {
                captain.sendMessage(Text.literal("Your ship ").append(shipName())
                        .append(Text.literal(" has gone down. Her cargo is adrift.")).formatted(Formatting.RED), false);
            }
            if (insured && owner != null) {
                ItemStack replacement = new ItemStack(shipClass().deed());
                NbtCompound fittings = new NbtCompound();
                fittings.putInt("Look", getLook());
                fittings.putInt("Upgrades", this.dataTracker.get(UPGRADES));
                fittings.putInt("SailColor", getSailColor());
                fittings.putInt("Flag", getFlag());
                fittings.putInt("Figurehead", this.dataTracker.get(FIGUREHEAD));
                fittings.putInt("Emblem", this.dataTracker.get(EMBLEM));
                replacement.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(fittings));
                if (this.hasCustomName()) replacement.set(DataComponentTypes.CUSTOM_NAME, this.getCustomName());
                com.cursedtides.trade.Insurance.payOut(world.getServer(), owner, replacement);
                insured = false;
            }
            for (CrewmateEntity hand : crew()) hand.discard();
            this.removeAllPassengers();
        }
        this.discard();
    }

    private boolean hasLivingCrew() {
        return this.getPassengerList().stream().anyMatch(e -> e instanceof MobEntity && !(e instanceof CrewmateEntity) && e.isAlive());
    }

    // ================================================================== interaction

    /** Owner sneak-punches the ship to stow her: she goes back into a deed with everything aboard. */
    @Override
    public boolean handleAttack(Entity attacker) {
        if (attacker instanceof PlayerEntity player && player.isSneaking() && !isHostile() && isOwner(player)
                && getOwner() != null) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                stow(serverPlayer);
            }
            return true;
        }
        return false;
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        ItemStack held = player.getStackInHand(hand);
        boolean mine = !isHostile() && isOwner(player);
        boolean server = !this.getWorld().isClient;

        if (held.isIn(ItemTags.PLANKS) && !isHostile() && getHull() < maxHull()) {
            if (server) {
                setHull(getHull() + 10.0f + (float) com.cursedtides.pirate.PerkData.stat(player,
                        com.cursedtides.pirate.Perk.Stat.REPAIR));
                if (!player.getAbilities().creativeMode) held.decrement(1);
                this.getWorld().playSound(null, getX(), getY(), getZ(), SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.PLAYERS, 1.0f, 0.8f);
                player.sendMessage(Text.literal("Hull patched: " + (int) getHull() + "/" + maxHull()).formatted(Formatting.GREEN), true);
            }
            return ActionResult.success(!server);
        }
        if (mine && held.getItem() instanceof com.cursedtides.ship.FigureheadItem fh) {
            if (server) {
                var old = getFigurehead();
                setFigurehead(fh.kind);
                if (!player.getAbilities().creativeMode) held.decrement(1);
                if (old != com.cursedtides.ship.FigureheadItem.Kind.NONE) {
                    var back = com.cursedtides.ship.FigureheadItem.itemFor(old);
                    if (back != null) player.giveItemStack(new ItemStack(back));
                }
                if (getHull() > maxHull()) setHull(maxHull());
                player.sendMessage(Text.literal("The " + fh.kind.title + " is fixed to the bow. " + fh.kind.effect + ".").formatted(Formatting.GOLD), true);
                this.getWorld().playSound(null, getX(), getY(), getZ(), SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.PLAYERS, 1.0f, 0.7f);
            }
            return ActionResult.success(!server);
        }
        if (mine && held.getItem() instanceof com.cursedtides.ship.FlagItem paint && player.isSneaking()) {
            if (server) {
                this.dataTracker.set(EMBLEM, paint.design().ordinal() + 1);
                player.sendMessage(Text.literal("The sailmaker paints the " + paint.design().displayName + " across your mainsail.").formatted(Formatting.GOLD), true);
                this.getWorld().playSound(null, getX(), getY(), getZ(), SoundEvents.BLOCK_WOOL_PLACE, SoundCategory.PLAYERS, 1.0f, 0.8f);
            }
            return ActionResult.success(!server);
        }
        if (mine && held.getItem() instanceof com.cursedtides.ship.FlagItem flagItem) {
            if (server) {
                setFlag(flagItem.design().ordinal());
                player.sendMessage(Text.literal("You run up the " + flagItem.design().displayName + ".").formatted(Formatting.GOLD), true);
                this.getWorld().playSound(null, getX(), getY(), getZ(), SoundEvents.BLOCK_WOOL_PLACE, SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
            return ActionResult.success(!server);
        }
        if (mine && held.getItem() instanceof ShipUpgradeItem upgradeItem) {
            if (server) fitUpgrade(player, held, upgradeItem.kind());
            return ActionResult.success(!server);
        }
        if (mine && held.getItem() instanceof CrewArticlesItem articles) {
            if (server && this.getWorld() instanceof ServerWorld world) {
                if (signOn(world, articles.role())) {
                    if (!player.getAbilities().creativeMode) held.decrement(1);
                    player.sendMessage(Text.literal("A new " + CrewmateEntity.ROLE_NAMES[articles.role()].toLowerCase()
                            + " signs aboard the ").append(shipName()).formatted(Formatting.GOLD), false);
                } else {
                    player.sendMessage(Text.literal("No free seat aboard for more crew.").formatted(Formatting.RED), true);
                }
            }
            return ActionResult.success(!server);
        }
        if (mine && held.getItem() instanceof DyeItem dye) {
            if (server) {
                this.dataTracker.set(SAIL_COLOR, dye.getColor().getEntityColor());
                if (!player.getAbilities().creativeMode) held.decrement(1);
                player.sendMessage(Text.literal("Fresh-dyed sails!").formatted(Formatting.GOLD), true);
            }
            return ActionResult.success(!server);
        }

        if (player.shouldCancelInteraction()) {
            if (server && player instanceof ServerPlayerEntity serverPlayer) {
                if (isHostile()) {
                    tryCapture(serverPlayer);
                } else if (isOwner(player) || player.getVehicle() == this
                        || com.cursedtides.social.CrewState.shipmates(serverPlayer.getServer(), getOwner(), player.getUuid())) {
                    openCargo(serverPlayer);
                } else {
                    player.sendMessage(Text.literal("That's not your hold, sailor.").formatted(Formatting.RED), true);
                }
            }
            return ActionResult.success(!server);
        }

        if (isHostile()) {
            return ActionResult.PASS; // climb aboard and fight, but you can't take a seat
        }
        return super.interact(player, hand);
    }

    private void fitUpgrade(PlayerEntity player, ItemStack held, ShipUpgradeItem.Kind kind) {
        int level = upgrade(kind);
        int max = kind == ShipUpgradeItem.Kind.HOLD ? Math.min(kind.maxLevel(), 6 - shipClass().cargoRows()) : kind.maxLevel();
        if (level >= max) {
            player.sendMessage(Text.literal("She can't take any more of that.").formatted(Formatting.GRAY), true);
            return;
        }
        float hullFraction = getHull() / maxHull();
        setUpgrade(kind, level + 1);
        if (kind == ShipUpgradeItem.Kind.PLATING) {
            setHull(hullFraction * maxHull());
        }
        if (!player.getAbilities().creativeMode) held.decrement(1);
        this.getWorld().playSound(null, getX(), getY(), getZ(), SoundEvents.BLOCK_SMITHING_TABLE_USE, SoundCategory.PLAYERS, 1.0f, 1.0f);
        player.sendMessage(Text.literal("Fitted: " + kind.effect() + " (" + (level + 1) + "/" + max + ")").formatted(Formatting.GOLD), false);
    }

    public void openCargo(ServerPlayerEntity player) {
        int rows = cargoRows();
        Text title = shipName().copy().append(Text.literal(" - Cargo Hold"));
        player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inventory, p) -> switch (rows) {
            case 3 -> GenericContainerScreenHandler.createGeneric9x3(syncId, inventory, cargo);
            case 4 -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X4, syncId, inventory, cargo, 4);
            case 5 -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X5, syncId, inventory, cargo, 5);
            default -> GenericContainerScreenHandler.createGeneric9x6(syncId, inventory, cargo);
        }, title));
    }

    /** Take her as a prize (after a boarding action). */
    public void captureBy(ServerPlayerEntity player) {
        tryCapture(player);
    }

    /** Strip her and send her down, credited to whoever did it. */
    public void scuttle(ServerWorld world, ServerPlayerEntity by) {
        lastAttacker = by.getUuid();
        sink(world);
    }

    private void tryCapture(ServerPlayerEntity player) {
        if (com.cursedtides.ship.Boarding.defending(this)) {
            player.sendMessage(Text.literal("Her boarders still hold the deck!").formatted(Formatting.RED), true);
            return;
        }
        if (com.cursedtides.quest.MusterArc.isFlagship(this)) {
            player.sendMessage(Text.literal("Nobody can sail the Last Muster. Strip her and let her sink.").formatted(Formatting.GRAY), true);
            return;
        }
        if (hasLivingCrew()) {
            player.sendMessage(Text.literal("Her crew still holds the deck!").formatted(Formatting.RED), true);
            return;
        }
        ShipFaction former = getFaction();
        setFaction(ShipFaction.PLAYER);
        setOwner(player.getUuid());
        FactionEvents.onShipCaptured(player, former);
        setSail(0);
        setHull(Math.max(getHull(), maxHull() * 0.25f));
        this.getPassengerList().forEach(Entity::discard);
        player.sendMessage(Text.literal("You've taken the ").append(shipName())
                .append(Text.literal("! She's yours now. Patch her hull with planks.")).formatted(Formatting.GOLD), false);
        this.getWorld().playSound(null, getX(), getY(), getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 0.8f, 1.0f);
        PirateData.addXp(player, Weapon.SAILING, 40);
    }

    /** Insured by the Bank of Kingsport: a sinking pays out a replacement deed. */
    private boolean insured;

    private NbtCompound saveShip(NbtCompound data) {
        if (insured) data.putBoolean(com.cursedtides.trade.Insurance.KEY, true);
        data.putFloat("Hull", getHull());
        data.putInt("Look", getLook());
        data.putInt("Upgrades", this.dataTracker.get(UPGRADES));
        data.putInt("SailColor", getSailColor());
        data.putInt("Flag", getFlag());
        data.putInt("Figurehead", this.dataTracker.get(FIGUREHEAD));
        data.putInt("Emblem", this.dataTracker.get(EMBLEM));
        data.putBoolean("Anchored", isAnchored());
        data.putInt("Ammo", getAmmo().ordinal());
        data.put("Cargo", cargo.toNbtList(this.getRegistryManager()));
        return data;
    }

    private void loadShip(NbtCompound data) {
        insured = data.getBoolean(com.cursedtides.trade.Insurance.KEY);
        if (data.contains("Upgrades")) this.dataTracker.set(UPGRADES, data.getInt("Upgrades"));
        if (data.contains("Hull")) setHull(data.getFloat("Hull"));
        setLook(data.contains("Look") ? data.getInt("Look") : (data.getBoolean("Ghost") ? LOOK_GHOST : LOOK_PLAIN));
        if (data.contains("SailColor")) this.dataTracker.set(SAIL_COLOR, data.getInt("SailColor"));
        setFlag(data.getInt("Flag"));
        this.dataTracker.set(FIGUREHEAD, data.getInt("Figurehead"));
        this.dataTracker.set(EMBLEM, data.getInt("Emblem"));
        setAnchored(data.getBoolean("Anchored"));
        if (data.contains("Ammo")) this.dataTracker.set(AMMO, data.getInt("Ammo"));
        if (data.contains("Cargo", NbtElement.LIST_TYPE)) {
            cargo.readNbtList(data.getList("Cargo", NbtElement.COMPOUND_TYPE), this.getRegistryManager());
        }
    }

    private void stow(ServerPlayerEntity player) {
        ItemStack deed = new ItemStack(shipClass().deed());
        NbtCompound data = saveShip(new NbtCompound());
        List<CrewmateEntity> hands = crew();
        int[] roles = new int[hands.size()];
        for (int i = 0; i < roles.length; i++) {
            roles[i] = hands.get(i).getRole();
            hands.get(i).discard();
        }
        data.put("Crew", new NbtIntArray(roles));
        deed.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(data));
        if (this.hasCustomName()) {
            deed.set(DataComponentTypes.CUSTOM_NAME, this.getCustomName());
        }
        this.removeAllPassengers();
        if (!player.getInventory().insertStack(deed)) {
            player.dropItem(deed, false);
        }
        this.getWorld().playSound(null, getX(), getY(), getZ(), SoundEvents.BLOCK_CHAIN_PLACE, SoundCategory.PLAYERS, 1.0f, 0.6f);
        player.sendMessage(Text.literal("Anchored and stowed: ").append(shipName()).formatted(Formatting.GOLD), true);
        this.discard();
    }

    /** Restores a ship (and, next tick, her crew) from a deed's saved data. */
    public void loadFromDeed(NbtCompound data) {
        loadShip(data);
        pendingCrew = data.getIntArray("Crew");
    }

    // ================================================================== save / load

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("ShipClass", this.dataTracker.get(CLASS));
        nbt.putInt("Sail", getSail());
        nbt.putInt("Faction", getFaction().ordinal());
        if (getOwner() != null) {
            nbt.putUuid("Owner", getOwner());
        }
        nbt.putLong("WageDay", lastWageDay);
        saveShip(nbt);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dataTracker.set(CLASS, nbt.getInt("ShipClass"));
        setSail(nbt.getInt("Sail"));
        if (nbt.contains("Faction")) {
            setFaction(ShipFaction.byIndex(nbt.getInt("Faction")));
        } else { // ships saved by 0.4.0
            setFaction(nbt.getBoolean("Hostile") ? ShipFaction.DROWNED : ShipFaction.PLAYER);
        }
        setOwner(nbt.containsUuid("Owner") ? nbt.getUuid("Owner") : null);
        lastWageDay = nbt.contains("WageDay") ? nbt.getLong("WageDay") : -1;
        loadShip(nbt);
        if (!nbt.contains("Hull")) setHull(maxHull());
    }
}
