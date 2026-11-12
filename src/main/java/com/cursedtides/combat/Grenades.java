package com.cursedtides.combat;

import com.cursedtides.CursedTides;
import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Grenades: a firebomb, a stinkpot, a smoke bomb and a blast charge. Throw with use (hold sneak for a long lob).
 * They train Gunnery. Blast charges hurt ships, too.
 */
public final class Grenades {
    private Grenades() {}

    public enum Kind {
        FIREBOMB("Firebomb", "Bursts into flame: burns everything nearby."),
        STINKPOT("Stinkpot", "A cloud of foul gas: poisons and sickens."),
        SMOKE("Smoke Bomb", "A thick cloud: you vanish; foes inside are blinded."),
        BLAST("Blast Charge", "A heavy explosion that staggers and hurts ships.");
        public final String title, text;
        Kind(String title, String text) { this.title = title; this.text = text; }
    }

    public static class GrenadeItem extends Item {
        public final Kind kind;
        public GrenadeItem(Settings s, Kind kind) { super(s); this.kind = kind; }

        @Override
        public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
            ItemStack stack = user.getStackInHand(hand);
            world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.PLAYERS, 0.8f, 0.6f);
            if (!world.isClient) {
                GrenadeEntity g = new GrenadeEntity(world, user, kind);
                g.setItem(new ItemStack(this));
                float speed = user.isSneaking() ? 1.5f : 1.0f;
                g.setVelocity(user, user.getPitch() - (user.isSneaking() ? 12 : 4), user.getYaw(), 0f, speed, 1.0f);
                world.spawnEntity(g);
                if (user instanceof net.minecraft.server.network.ServerPlayerEntity sp) com.cursedtides.quest.Lessons.onThrow(sp);
                if (!user.getAbilities().creativeMode) stack.decrement(1);
                int level = PirateData.level(user, Weapon.CANNON);
                user.getItemCooldownManager().set(this, Math.max(10, 30 - level));
            }
            return TypedActionResult.success(stack, world.isClient);
        }

        @Override
        public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
            tooltip.add(Text.literal(kind.text).formatted(Formatting.GRAY));
            tooltip.add(Text.literal("Use to throw; sneak for a long lob. Trains Gunnery.").formatted(Formatting.DARK_GRAY));
        }
    }

    public static class GrenadeEntity extends ThrownItemEntity {
        private Kind kind = Kind.FIREBOMB;

        public GrenadeEntity(EntityType<? extends GrenadeEntity> type, World world) { super(type, world); }

        public GrenadeEntity(World world, LivingEntity owner, Kind kind) {
            super(TYPE, owner, world);
            this.kind = kind;
        }

        @Override
        protected Item getDefaultItem() { return ITEMS.get(0); }

        @Override
        protected void onCollision(HitResult hit) {
            super.onCollision(hit);
            if (!(this.getWorld() instanceof ServerWorld w)) return;
            Vec3d at = hit.getPos();
            Entity owner = this.getOwner();
            int level = owner instanceof PlayerEntity p ? PirateData.level(p, Weapon.CANNON) : 0;
            float power = 1f + level * 0.03f;
            Box area = new Box(at, at).expand(kind == Kind.BLAST ? 4 : 3);
            List<LivingEntity> hitList = w.getEntitiesByClass(LivingEntity.class, area, e -> e.isAlive() && e != owner
                    && !(owner instanceof PlayerEntity p && e instanceof PlayerEntity q && com.cursedtides.social.CrewState.allied(w.getServer(), p.getUuid(), q.getUuid())));
            switch (kind) {
                case FIREBOMB -> {
                    for (LivingEntity e : hitList) { e.setOnFireFor(6); e.damage(w.getDamageSources().thrown(this, owner), 5f * power); }
                    w.spawnParticles(ParticleTypes.FLAME, at.x, at.y + 0.3, at.z, 60, 1.5, 0.4, 1.5, 0.05);
                    w.spawnParticles(ParticleTypes.LAVA, at.x, at.y + 0.3, at.z, 10, 1, 0.2, 1, 0);
                    w.playSound(null, at.x, at.y, at.z, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 1.2f, 0.8f);
                }
                case STINKPOT -> {
                    AreaEffectCloudEntity cloud = new AreaEffectCloudEntity(w, at.x, at.y, at.z);
                    cloud.setRadius(3.5f);
                    cloud.setDuration(160);
                    cloud.setRadiusGrowth(-0.01f);
                    cloud.addEffect(new StatusEffectInstance(StatusEffects.POISON, 120, 1));
                    cloud.addEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 160, 0));
                    if (owner instanceof LivingEntity lo) cloud.setOwner(lo);
                    w.spawnEntity(cloud);
                    w.playSound(null, at.x, at.y, at.z, SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.PLAYERS, 1f, 0.5f);
                }
                case SMOKE -> {
                    w.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, at.x, at.y + 0.5, at.z, 80, 2, 1, 2, 0.01);
                    for (LivingEntity e : hitList) {
                        e.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0));
                        if (e instanceof net.minecraft.entity.mob.MobEntity m && m.getTarget() == owner) m.setTarget(null);
                    }
                    if (owner instanceof LivingEntity lo && lo.squaredDistanceTo(at) < 36) {
                        lo.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 120, 0));
                        lo.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 120, 1));
                    }
                    w.playSound(null, at.x, at.y, at.z, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 1.2f, 0.6f);
                }
                case BLAST -> {
                    for (LivingEntity e : hitList) {
                        e.damage(w.getDamageSources().thrown(this, owner), 10f * power);
                        Styles.stagger(e, 40);
                    }
                    for (var ship : w.getEntitiesByClass(com.cursedtides.entity.ShipEntity.class, area.expand(2), s -> true)) {
                        ship.damage(w.getDamageSources().thrown(this, owner), 15f * power);
                    }
                    w.createExplosion(this, at.x, at.y, at.z, 1.5f, World.ExplosionSourceType.NONE);
                }
            }
            if (owner instanceof ServerPlayerEntity p && !hitList.isEmpty()) PirateData.addXp(p, Weapon.CANNON, 3 * hitList.size());
            this.discard();
        }

        @Override
        public void writeCustomDataToNbt(NbtCompound nbt) {
            super.writeCustomDataToNbt(nbt);
            nbt.putInt("Kind", kind.ordinal());
        }

        @Override
        public void readCustomDataFromNbt(NbtCompound nbt) {
            super.readCustomDataFromNbt(nbt);
            kind = Kind.values()[Math.floorMod(nbt.getInt("Kind"), Kind.values().length)];
        }
    }

    public static final EntityType<GrenadeEntity> TYPE = Registry.register(Registries.ENTITY_TYPE, CursedTides.id("grenade"),
            EntityType.Builder.<GrenadeEntity>create(GrenadeEntity::new, SpawnGroup.MISC).dimensions(0.3f, 0.3f)
                    .maxTrackingRange(6).trackingTickInterval(5).build("grenade"));

    public static final List<Item> ITEMS = new ArrayList<>();

    private static Item reg(Kind k) {
        String id = switch (k) { case SMOKE -> "smoke_bomb"; case BLAST -> "blast_charge"; default -> k.name().toLowerCase(); };
        Item item = Registry.register(Registries.ITEM, CursedTides.id(id),
                new GrenadeItem(new Item.Settings().maxCount(16).rarity(k == Kind.BLAST ? Rarity.UNCOMMON : Rarity.COMMON), k));
        ITEMS.add(item);
        return item;
    }

    public static final Item FIREBOMB = reg(Kind.FIREBOMB);
    public static final Item STINKPOT = reg(Kind.STINKPOT);
    public static final Item SMOKE_BOMB = reg(Kind.SMOKE);
    public static final Item BLAST_CHARGE = reg(Kind.BLAST);

    public static final Item VOODOO_DOLL = Registry.register(Registries.ITEM, CursedTides.id("voodoo_doll"),
            new VoodooDollItem(new Item.Settings().maxCount(1).maxDamage(250).rarity(Rarity.RARE)));

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(RegistryKey.of(RegistryKeys.ITEM_GROUP, CursedTides.id("main"))).register(e -> {
            ITEMS.forEach(e::add);
            e.add(VOODOO_DOLL);
        });
    }
}
