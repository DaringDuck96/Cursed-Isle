package com.cursedtides.combat;

import com.cursedtides.CursedTides;
import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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
        public GrenadeItem(Properties s, Kind kind) { super(s); this.kind = kind; }

        @Override
        public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
            ItemStack stack = user.getItemInHand(hand);
            world.playSound(null, user.blockPosition(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.8f, 0.6f);
            if (!world.isClientSide) {
                GrenadeEntity g = new GrenadeEntity(world, user, kind);
                g.setItem(new ItemStack(this));
                float speed = user.isShiftKeyDown() ? 1.5f : 1.0f;
                g.shootFromRotation(user, user.getXRot() - (user.isShiftKeyDown() ? 12 : 4), user.getYRot(), 0f, speed, 1.0f);
                world.addFreshEntity(g);
                if (user instanceof net.minecraft.server.level.ServerPlayer sp) com.cursedtides.quest.Lessons.onThrow(sp);
                if (!user.getAbilities().instabuild) stack.shrink(1);
                int level = PirateData.level(user, Weapon.CANNON);
                user.getCooldowns().addCooldown(this, Math.max(10, 30 - level));
            }
            return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
            tooltip.add(Component.literal(kind.text).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Use to throw; sneak for a long lob. Trains Gunnery.").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    public static class GrenadeEntity extends ThrowableItemProjectile {
        private Kind kind = Kind.FIREBOMB;

        public GrenadeEntity(EntityType<? extends GrenadeEntity> type, Level world) { super(type, world); }

        public GrenadeEntity(Level world, LivingEntity owner, Kind kind) {
            super(TYPE, owner, world);
            this.kind = kind;
        }

        @Override
        protected Item getDefaultItem() { return ITEMS.get(0); }

        @Override
        protected void onHit(HitResult hit) {
            super.onHit(hit);
            if (!(this.level() instanceof ServerLevel w)) return;
            Vec3 at = hit.getLocation();
            Entity owner = this.getOwner();
            int level = owner instanceof Player p ? PirateData.level(p, Weapon.CANNON) : 0;
            float power = 1f + level * 0.03f;
            AABB area = new AABB(at, at).inflate(kind == Kind.BLAST ? 4 : 3);
            List<LivingEntity> hitList = w.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner
                    && !(owner instanceof Player p && e instanceof Player q && com.cursedtides.social.CrewState.allied(w.getServer(), p.getUUID(), q.getUUID())));
            switch (kind) {
                case FIREBOMB -> {
                    for (LivingEntity e : hitList) { e.igniteForSeconds(6); e.hurt(w.damageSources().thrown(this, owner), 5f * power); }
                    w.sendParticles(ParticleTypes.FLAME, at.x, at.y + 0.3, at.z, 60, 1.5, 0.4, 1.5, 0.05);
                    w.sendParticles(ParticleTypes.LAVA, at.x, at.y + 0.3, at.z, 10, 1, 0.2, 1, 0);
                    w.playSound(null, at.x, at.y, at.z, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.2f, 0.8f);
                }
                case STINKPOT -> {
                    AreaEffectCloud cloud = new AreaEffectCloud(w, at.x, at.y, at.z);
                    cloud.setRadius(3.5f);
                    cloud.setDuration(160);
                    cloud.setRadiusPerTick(-0.01f);
                    cloud.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1));
                    cloud.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 0));
                    if (owner instanceof LivingEntity lo) cloud.setOwner(lo);
                    w.addFreshEntity(cloud);
                    w.playSound(null, at.x, at.y, at.z, SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 1f, 0.5f);
                }
                case SMOKE -> {
                    w.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, at.x, at.y + 0.5, at.z, 80, 2, 1, 2, 0.01);
                    for (LivingEntity e : hitList) {
                        e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                        if (e instanceof net.minecraft.world.entity.Mob m && m.getTarget() == owner) m.setTarget(null);
                    }
                    if (owner instanceof LivingEntity lo && lo.distanceToSqr(at) < 36) {
                        lo.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 120, 0));
                        lo.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 120, 1));
                    }
                    w.playSound(null, at.x, at.y, at.z, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.2f, 0.6f);
                }
                case BLAST -> {
                    for (LivingEntity e : hitList) {
                        e.hurt(w.damageSources().thrown(this, owner), 10f * power);
                        Styles.stagger(e, 40);
                    }
                    for (var ship : w.getEntitiesOfClass(com.cursedtides.entity.ShipEntity.class, area.inflate(2), s -> true)) {
                        ship.hurt(w.damageSources().thrown(this, owner), 15f * power);
                    }
                    w.explode(this, at.x, at.y, at.z, 1.5f, Level.ExplosionInteraction.NONE);
                }
            }
            if (owner instanceof ServerPlayer p && !hitList.isEmpty()) PirateData.addXp(p, Weapon.CANNON, 3 * hitList.size());
            this.discard();
        }

        @Override
        public void addAdditionalSaveData(CompoundTag nbt) {
            super.addAdditionalSaveData(nbt);
            nbt.putInt("Kind", kind.ordinal());
        }

        @Override
        public void readAdditionalSaveData(CompoundTag nbt) {
            super.readAdditionalSaveData(nbt);
            kind = Kind.values()[Math.floorMod(nbt.getInt("Kind"), Kind.values().length)];
        }
    }

    public static final EntityType<GrenadeEntity> TYPE = Registry.register(BuiltInRegistries.ENTITY_TYPE, CursedTides.id("grenade"),
            EntityType.Builder.<GrenadeEntity>of(GrenadeEntity::new, MobCategory.MISC).sized(0.3f, 0.3f)
                    .clientTrackingRange(6).updateInterval(5).build("grenade"));

    public static final List<Item> ITEMS = new ArrayList<>();

    private static Item reg(Kind k) {
        String id = switch (k) { case SMOKE -> "smoke_bomb"; case BLAST -> "blast_charge"; default -> k.name().toLowerCase(); };
        Item item = Registry.register(BuiltInRegistries.ITEM, CursedTides.id(id),
                new GrenadeItem(new Item.Properties().stacksTo(16).rarity(k == Kind.BLAST ? Rarity.UNCOMMON : Rarity.COMMON), k));
        ITEMS.add(item);
        return item;
    }

    public static final Item FIREBOMB = reg(Kind.FIREBOMB);
    public static final Item STINKPOT = reg(Kind.STINKPOT);
    public static final Item SMOKE_BOMB = reg(Kind.SMOKE);
    public static final Item BLAST_CHARGE = reg(Kind.BLAST);

    public static final Item VOODOO_DOLL = Registry.register(BuiltInRegistries.ITEM, CursedTides.id("voodoo_doll"),
            new VoodooDollItem(new Item.Properties().stacksTo(1).durability(250).rarity(Rarity.RARE)));

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB, CursedTides.id("main"))).register(e -> {
            ITEMS.forEach(e::accept);
            e.accept(VOODOO_DOLL);
        });
    }
}
