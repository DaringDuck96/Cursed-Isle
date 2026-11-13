package com.cursedtides.combat;

import net.minecraft.world.item.DyeColor;
import com.cursedtides.CursedTides;
import com.cursedtides.item.FlintlockPistolItem;
import com.cursedtides.item.HexStaffItem;
import com.cursedtides.item.ModItems;
import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Weapon skills, the old pirate games' way: whatever's in your hand gives you a bar of four skills, fired with
 * Z, X, C and V (on foot; aboard ship those keys still work the guns). Each unlocks as you master that weapon and
 * recharges after use. Blades sweep, lunge, hamstring and whirl; firearms take aimed shots, scatter, cripple and
 * volley; a hex staff blasts, wards, curses and siphons; the voodoo doll's slots are its workings.
 */
public final class SkillBar {
    private SkillBar() {}

    public enum Family { BLADE, GUN, STAFF, DOLL, DAGGER, GRENADE, SHIP }

    public enum Skill {
        SWEEP(Family.BLADE, "Sweep", () -> Items.IRON_SWORD, 0, 6, 0, "Cut every foe in front of you."),
        LUNGE(Family.BLADE, "Lunge", () -> Items.FEATHER, 3, 8, 0, "Dash forward and run the first foe through."),
        HAMSTRING(Family.BLADE, "Hamstring", () -> Items.SHEARS, 7, 12, 0, "A low cut: the foe can barely walk or swing."),
        WHIRLWIND(Family.BLADE, "Whirlwind", () -> Items.TRIDENT, 12, 20, 0, "Spin, cutting everything around you."),
        TAKE_AIM(Family.GUN, "Take Aim", () -> Items.SPYGLASS, 0, 10, 0, "A slow, careful shot for heavy damage."),
        SCATTERSHOT(Family.GUN, "Scattershot", () -> Items.GUNPOWDER, 3, 12, 0, "A spray of shot into everything ahead."),
        LEG_SHOT(Family.GUN, "Leg Shot", () -> Items.LEATHER_BOOTS, 7, 10, 0, "Cripple a foe so it can't chase you."),
        VOLLEY(Family.GUN, "Volley", () -> Items.FIREWORK_STAR, 12, 22, 0, "Three shots as fast as you can load."),
        HEX_BLAST(Family.STAFF, "Hex Blast", () -> Items.FIRE_CHARGE, 0, 6, 10, "A bolt of raw hex at your target."),
        SPIRIT_WARD(Family.STAFF, "Spirit Ward", () -> Items.TOTEM_OF_UNDYING, 3, 20, 15, "Spirits shield you from harm."),
        WITHERING(Family.STAFF, "Withering Curse", () -> Items.WITHER_ROSE, 7, 12, 18, "Curse your target and all around it."),
        SIPHON(Family.STAFF, "Soul Siphon", () -> Items.GHAST_TEAR, 12, 18, 25, "Draw out a foe's life into your own."),
        POKE(Family.DOLL, "Poke", () -> Items.STICK, 0, 3, 0, "Stab the doll: hurt every attuned foe."),
        MEND(Family.DOLL, "Mend", () -> Items.STRING, 0, 6, 0, "Stitch the doll: heal every attuned friend."),
        SWARM(Family.DOLL, "Swarm", () -> Items.SPIDER_EYE, 8, 10, 0, "Poison and slow every attuned foe."),
        SHACKLES(Family.DOLL, "Shackles", () -> Items.IRON_CHAIN, 16, 14, 0, "Bind every attuned foe in place."),
        GOUGE(Family.DAGGER, "Gouge", () -> ModItems.DAGGER, 0, 5, 0, "A quick, dirty stab that leaves the foe bleeding and slow."),
        FAN_OF_KNIVES(Family.DAGGER, "Fan of Knives", () -> ModItems.THROWING_KNIFE, 3, 10, 0, "Throw five knives in a spread."),
        BACKSTAB(Family.DAGGER, "Backstab", () -> Items.ENDER_PEARL, 7, 12, 0, "Slip behind a foe and strike where it hurts most."),
        VENOM_EDGE(Family.DAGGER, "Venom Edge", () -> Items.SPIDER_EYE, 12, 20, 0, "Poison everyone close, and dance away quick."),
        LONG_TOSS(Family.GRENADE, "Long Toss", () -> Items.SNOWBALL, 0, 3, 0, "Hurl the grenade in hand twice as far."),
        CLUSTER(Family.GRENADE, "Cluster", () -> Items.FIRE_CHARGE, 4, 14, 0, "Throw three at once, in a spread."),
        QUICK_FUSE(Family.GRENADE, "Quick Fuse", () -> Items.FLINT_AND_STEEL, 8, 10, 0, "A flat, fast throw that bursts on the first thing it hits."),
        POWDER_KEG(Family.GRENADE, "Powder Keg", () -> Items.TNT, 13, 25, 0, "Two grenades lashed to a keg of powder. Stand back."),
        FULL_SAIL(Family.SHIP, "Full Sail", () -> Items.BANNER.pick(DyeColor.WHITE), 0, 30, 0, "Every stitch of canvas: much faster for ten seconds."),
        COME_ABOUT(Family.SHIP, "Come About", () -> Items.COMPASS, 3, 20, 0, "Swing her round to the opposite heading in a second."),
        OPEN_FIRE(Family.SHIP, "Open Fire", () -> Items.FIRE_CHARGE, 6, 40, 0, "Gun crews load double-quick: broadsides reload twice as fast for 15 seconds."),
        RAMMING_SPEED(Family.SHIP, "Ramming Speed", () -> Items.IRON_BLOCK, 10, 45, 0, "Charge! A burst of speed, and the first enemy you hit takes a terrible blow."),
        TAKE_COVER(Family.SHIP, "Take Cover", () -> Items.SHIELD, 14, 45, 0, "Everyone down: half damage to hull and crew for 12 seconds.");

        public final Family family;
        public final String title, text;
        public final Supplier<Item> icon;
        public final int level, cooldownTicks, voodoo;

        Skill(Family family, String title, Supplier<Item> icon, int level, int cooldownSeconds, int voodoo, String text) {
            this.family = family;
            this.title = title;
            this.icon = icon;
            this.level = level;
            this.cooldownTicks = cooldownSeconds * 20;
            this.voodoo = voodoo;
            this.text = text;
        }

        public static Skill of(Family f, int slot) {
            int i = 0;
            for (Skill s : values()) {
                if (s.family != f) continue;
                if (i++ == slot) return s;
            }
            return null;
        }
    }

    /** The bar for this player: their ship's, if they're aboard one of their own, else their weapon's. */
    public static Family family(ServerPlayer p) {
        if (p.getVehicle() instanceof com.cursedtides.entity.ShipEntity s && !s.isHostile()) return Family.SHIP;
        if (p.isPassenger()) return null;
        return family(p.getMainHandItem());
    }

    public static Family family(ItemStack held) {
        Item item = held.getItem();
        if (item == ModItems.DAGGER || item == ModItems.THROWING_KNIFE) return Family.DAGGER;
        if (item instanceof Grenades.GrenadeItem) return Family.GRENADE;
        if (item instanceof FlintlockPistolItem) return Family.GUN;
        if (item instanceof HexStaffItem) return Family.STAFF;
        if (item == Grenades.VOODOO_DOLL) return Family.DOLL;
        if (com.cursedtides.util.Compat.isBlade(held)) return Family.BLADE;
        return null;
    }

    private static Weapon mastery(Family f) {
        return switch (f) {
            case BLADE -> Weapon.CUTLASS;
            case GUN -> Weapon.PISTOL;
            case STAFF, DOLL -> Weapon.HEX;
            case DAGGER -> Weapon.CUTLASS;
            case GRENADE -> Weapon.CANNON;
            case SHIP -> Weapon.SAILING;
        };
    }

    // ------------------------------------------------------------------ network

    /** Client to server: fire skill slot 0-3. */
    public record Use(int slot) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<Use> ID = new CustomPacketPayload.Type<>(CursedTides.id("skill_use"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Use> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, Use::slot, Use::new);

        @Override
        public Type<? extends CustomPacketPayload> type() { return ID; }
    }

    /** Server to client: the bar for what's in hand (family -1 = none), which slots are open, and time left on each. */
    public record State(int family, int unlocked, List<Integer> cooldowns) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<State> ID = new CustomPacketPayload.Type<>(CursedTides.id("skill_state"));
        public static final StreamCodec<RegistryFriendlyByteBuf, State> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, State::family, ByteBufCodecs.VAR_INT, State::unlocked,
                ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), State::cooldowns, State::new);

        @Override
        public Type<? extends CustomPacketPayload> type() { return ID; }
    }

    private static final Map<UUID, Map<Skill, Long>> READY = new HashMap<>();
    private static final Map<UUID, State> SENT = new HashMap<>();

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(Use.ID, Use.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(State.ID, State.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Use.ID, (payload, context) -> context.server().execute(() -> use(context.player(), payload.slot())));
        ServerTickEvents.END_SERVER_TICK.register(SkillBar::tick);
        ServerPlayConnectionEvents.DISCONNECT.register((h, s) -> {
            READY.remove(h.player.getUUID());
            SENT.remove(h.player.getUUID());
        });
    }

    private static void tick(MinecraftServer server) {
        if (server.getTickCount() % 5 != 0) return;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            State s = state(p);
            State last = SENT.get(p.getUUID());
            if (!s.equals(last)) {
                SENT.put(p.getUUID(), s);
                ServerPlayNetworking.send(p, s);
            }
        }
    }

    private static State state(ServerPlayer p) {
        Family f = family(p);
        if (f == null) return new State(-1, 0, List.of(0, 0, 0, 0));
        int slots = f == Family.SHIP ? 5 : 4;
        int lvl = PirateData.level(p, mastery(f));
        long now = p.level().getGameTime();
        int unlocked = 0;
        Integer[] cd = new Integer[slots];
        Map<Skill, Long> ready = READY.getOrDefault(p.getUUID(), Map.of());
        for (int i = 0; i < slots; i++) {
            Skill s = Skill.of(f, i);
            if (s != null && s.level <= lvl) unlocked |= 1 << i;
            long r = s == null ? 0 : ready.getOrDefault(s, 0L);
            // Rounded to whole seconds so the bar is only re-sent as each second ticks by
            cd[i] = (int) Math.max(0, (r - now + 19) / 20) * 20;
        }
        return new State(f.ordinal(), unlocked, List.of(cd));
    }

    private static void use(ServerPlayer p, int slot) {
        if (p.isSpectator() || slot < 0 || slot > 4) return;
        ItemStack held = p.getMainHandItem();
        Family f = family(p);
        if (f == null) {
            p.sendSystemMessage(Component.literal("Draw a weapon to use its skills.").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        Skill s = Skill.of(f, slot);
        if (s == null) return;
        if (PirateData.level(p, mastery(f)) < s.level) {
            p.sendSystemMessage(Component.literal(s.title + " unlocks at " + mastery(f).name().toLowerCase() + " mastery " + s.level + ".").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        long now = p.level().getGameTime();
        Map<Skill, Long> ready = READY.computeIfAbsent(p.getUUID(), k -> new HashMap<>());
        if (ready.getOrDefault(s, 0L) > now) return;
        if (s.voodoo > 0 && !PirateData.spendVoodoo(p, s.voodoo)) {
            p.sendSystemMessage(Component.translatable("message.cursedtides.no_voodoo").withStyle(ChatFormatting.DARK_AQUA), true);
            return;
        }
        if (!fire(p, s, held)) return;
        ready.put(s, now + s.cooldownTicks);
        p.sendSystemMessage(Component.literal(s.title).withStyle(ChatFormatting.AQUA), true);
    }

    // ------------------------------------------------------------------ the skills

    private static boolean fire(ServerPlayer p, Skill s, ItemStack held) {
        ServerLevel w = p.level();
        Vec3 look = p.getViewVector(1f);
        float base = (float) p.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        switch (s) {
            case SWEEP -> {
                for (LivingEntity e : foes(p, 3.8)) {
                    if (inFront(p, e, 70)) {
                        e.hurt(p.damageSources().playerAttack(p), base * 0.9f + 2);
                        com.cursedtides.util.Compat.knock(e, 0.5, p.getX() - e.getX(), p.getZ() - e.getZ());
                    }
                }
                w.sendParticles(ParticleTypes.SWEEP_ATTACK, p.getX() + look.x * 1.5, p.getY(0.6), p.getZ() + look.z * 1.5, 3, 0.6, 0.1, 0.6, 0);
                w.playSound(null, p.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1f, 0.9f);
            }
            case LUNGE -> {
                p.setDeltaMovement(look.x * 1.6, 0.15, look.z * 1.6);
                p.syncVelocity = true;
                LivingEntity t = aim(p, 7, 30);
                if (t != null) t.hurt(p.damageSources().playerAttack(p), base * 1.4f + 3);
                w.playSound(null, p.blockPosition(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.PLAYERS, 1f, 1.2f);
            }
            case HAMSTRING -> {
                LivingEntity t = aim(p, 4, 40);
                if (t == null) return nothing(p);
                t.hurt(p.damageSources().playerAttack(p), base + 2);
                t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 2));
                t.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
                w.sendParticles(ParticleTypes.CRIT, t.getX(), t.getY(0.3), t.getZ(), 12, 0.3, 0.2, 0.3, 0.2);
            }
            case WHIRLWIND -> {
                for (LivingEntity e : foes(p, 4.6)) {
                    e.hurt(p.damageSources().playerAttack(p), base * 1.3f + 4);
                    com.cursedtides.util.Compat.knock(e, 0.9, p.getX() - e.getX(), p.getZ() - e.getZ());
                }
                for (int k = 0; k < 8; k++) {
                    double a = Math.PI * 2 * k / 8;
                    w.sendParticles(ParticleTypes.SWEEP_ATTACK, p.getX() + Math.cos(a) * 2.2, p.getY(0.5), p.getZ() + Math.sin(a) * 2.2, 1, 0, 0, 0, 0);
                }
                w.playSound(null, p.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2f, 0.6f);
            }
            case TAKE_AIM, LEG_SHOT, VOLLEY -> {
                int shots = s == Skill.VOLLEY ? 3 : 1;
                if (!ammo(p, shots)) return false;
                LivingEntity t = aim(p, 40, 6);
                gunSmoke(p, w);
                if (t == null) return true;
                float dmg = s == Skill.TAKE_AIM ? 14 : s == Skill.LEG_SHOT ? 6 : 7;
                for (int k = 0; k < shots; k++) {
                    t.setInvulnerableTime(0);
                    t.hurt(p.damageSources().playerAttack(p), dmg);
                }
                if (s == Skill.LEG_SHOT) t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 140, 3));
            }
            case SCATTERSHOT -> {
                if (!ammo(p, 1)) return false;
                gunSmoke(p, w);
                for (LivingEntity e : foes(p, 9)) {
                    if (inFront(p, e, 35)) {
                        e.setInvulnerableTime(0);
                        e.hurt(p.damageSources().playerAttack(p), 7);
                    }
                }
            }
            case HEX_BLAST -> {
                LivingEntity t = aim(p, 24, 8);
                if (t == null) return nothing(p);
                t.hurt(p.damageSources().indirectMagic(p, p), 9);
                w.sendParticles(ParticleTypes.WITCH, t.getX(), t.getY(0.5), t.getZ(), 30, 0.4, 0.5, 0.4, 0.1);
                w.playSound(null, t.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1f, 1.2f);
            }
            case SPIRIT_WARD -> {
                p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 160, 1));
                p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 1));
                w.sendParticles(ParticleTypes.SOUL, p.getX(), p.getY(0.5), p.getZ(), 30, 0.6, 0.8, 0.6, 0.02);
                w.playSound(null, p.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.8f, 1.5f);
            }
            case WITHERING -> {
                LivingEntity t = aim(p, 24, 10);
                if (t == null) return nothing(p);
                for (LivingEntity e : w.getEntitiesOfClass(LivingEntity.class, t.getBoundingBox().inflate(4), e -> e != p && Levels.isFoe(e))) {
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 1));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 1));
                }
                w.sendParticles(ParticleTypes.SQUID_INK, t.getX(), t.getY(0.5), t.getZ(), 40, 2, 0.6, 2, 0.02);
            }
            case SIPHON -> {
                LivingEntity t = aim(p, 20, 8);
                if (t == null) return nothing(p);
                float before = t.getHealth();
                t.hurt(p.damageSources().indirectMagic(p, p), 11);
                p.heal(Math.max(0, before - t.getHealth()));
                w.sendParticles(ParticleTypes.DAMAGE_INDICATOR, t.getX(), t.getY(0.6), t.getZ(), 10, 0.3, 0.3, 0.3, 0.1);
                w.sendParticles(ParticleTypes.HEART, p.getX(), p.getY(1), p.getZ(), 4, 0.3, 0.3, 0.3, 0.1);
            }
            case GOUGE -> {
                LivingEntity t = aim(p, 4, 40);
                if (t == null) return nothing(p);
                t.hurt(p.damageSources().playerAttack(p), base + 3);
                t.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0));
                t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
                w.sendParticles(ParticleTypes.DAMAGE_INDICATOR, t.getX(), t.getY(0.5), t.getZ(), 6, 0.2, 0.2, 0.2, 0.1);
                w.playSound(null, p.blockPosition(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1f, 1.4f);
            }
            case FAN_OF_KNIVES -> {
                for (int k = -2; k <= 2; k++) {
                    com.cursedtides.entity.ThrowingKnifeEntity knife = new com.cursedtides.entity.ThrowingKnifeEntity(w, p);
                    knife.shootFromRotation(p, p.getXRot(), p.getYRot() + k * 9, 0f, 2.1f, 0.4f);
                    w.addFreshEntity(knife);
                }
                w.playSound(null, p.blockPosition(), SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1f, 1.6f);
            }
            case BACKSTAB -> {
                LivingEntity t = aim(p, 8, 25);
                if (t == null) return nothing(p);
                Vec3 behind = t.position().subtract(t.getLookAngle().multiply(1, 0, 1).normalize().scale(1.3));
                w.sendParticles(ParticleTypes.LARGE_SMOKE, p.getX(), p.getY(0.5), p.getZ(), 12, 0.3, 0.5, 0.3, 0.02);
                com.cursedtides.util.Compat.teleport(p, w, behind.x, t.getY(), behind.z, java.util.Set.of(), t.getYRot(), p.getXRot());
                t.hurt(p.damageSources().playerAttack(p), base * 2.5f + 4);
                w.playSound(null, t.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1f, 0.8f);
            }
            case VENOM_EDGE -> {
                for (LivingEntity e : foes(p, 3.5)) e.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1));
                p.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 1));
                w.sendParticles(ParticleTypes.ITEM_SLIME, p.getX(), p.getY(0.5), p.getZ(), 30, 1.5, 0.4, 1.5, 0.05);
                w.playSound(null, p.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1f, 1.5f);
            }
            case LONG_TOSS, CLUSTER, QUICK_FUSE -> {
                if (!(held.getItem() instanceof Grenades.GrenadeItem g)) return false;
                int n = s == Skill.CLUSTER ? Math.min(3, p.getAbilities().instabuild ? 3 : held.getCount()) : 1;
                for (int k = 0; k < n; k++) {
                    Grenades.GrenadeEntity e = new Grenades.GrenadeEntity(w, p, g.kind);
                    e.setItem(new ItemStack(g));
                    float spread = n == 1 ? 0 : (k - 1) * 14;
                    float pitch = s == Skill.QUICK_FUSE ? p.getXRot() : p.getXRot() - 10;
                    float speed = s == Skill.LONG_TOSS ? 2.0f : s == Skill.QUICK_FUSE ? 3.2f : 1.3f;
                    e.shootFromRotation(p, pitch, p.getYRot() + spread, 0f, speed, 0.5f);
                    w.addFreshEntity(e);
                }
                if (!p.getAbilities().instabuild) held.shrink(n);
                w.playSound(null, p.blockPosition(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 1f, 0.5f);
            }
            case POWDER_KEG -> {
                if (!(held.getItem() instanceof Grenades.GrenadeItem)) return false;
                if (!p.getAbilities().instabuild && held.getCount() < 2) {
                    p.sendSystemMessage(Component.literal("You need two grenades for a powder keg.").withStyle(ChatFormatting.GRAY), true);
                    return false;
                }
                var hit = p.pick(16, 1f, false);
                Vec3 at = hit.getLocation();
                for (LivingEntity e : w.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(5), e -> e != p && e.isAlive() && Levels.isFoe(e))) {
                    e.hurt(p.damageSources().explosion(p, p), 16f);
                    Styles.stagger(e, 60);
                }
                w.explode(p, at.x, at.y, at.z, 2.5f, net.minecraft.world.level.Level.ExplosionInteraction.NONE);
                if (!p.getAbilities().instabuild) held.shrink(2);
            }
            case FULL_SAIL, COME_ABOUT, OPEN_FIRE, RAMMING_SPEED, TAKE_COVER -> {
                if (!(p.getVehicle() instanceof com.cursedtides.entity.ShipEntity ship)) return false;
                long now = w.getGameTime();
                switch (s) {
                    case FULL_SAIL -> { ship.fullSail(now + 200); shout(ship, "Full sail! Every stitch of canvas!"); }
                    case COME_ABOUT -> { ship.comeAbout(20, 180); shout(ship, "Come about! Hard over!"); }
                    case OPEN_FIRE -> { ship.openFire(now + 300); shout(ship, "Open fire! Load and run out, double quick!"); }
                    case RAMMING_SPEED -> { ship.rammingSpeed(now + 120); shout(ship, "Ramming speed! Brace for impact!"); }
                    default -> { ship.takeCover(now + 240); shout(ship, "Take cover! Everybody down!"); }
                }
                w.playSound(null, ship.blockPosition(), SoundEvents.RAID_HORN.value(), SoundSource.PLAYERS, 0.6f, 1.4f);
            }
            case POKE, MEND, SWARM, SHACKLES -> {
                if (p.isShiftKeyDown()) return false;
                String working = switch (s) { case POKE -> "POKE"; case MEND -> "HEAL"; case SWARM -> "SWARM"; default -> "SHACKLES"; };
                CompoundTag n = held.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
                n.putInt("Working", VoodooDollItem.Working.valueOf(working).ordinal());
                held.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(n));
                return held.getItem().use(w, p, InteractionHand.MAIN_HAND).consumesAction();
            }
        }
        return true;
    }

    private static void shout(com.cursedtides.entity.ShipEntity ship, String order) {
        for (Entity e : ship.getPassengers()) {
            if (e instanceof ServerPlayer q) q.sendSystemMessage(Component.literal(order).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), true);
        }
    }

    private static boolean nothing(ServerPlayer p) {
        p.sendSystemMessage(Component.literal("No foe in your sights.").withStyle(ChatFormatting.GRAY), true);
        return false;
    }

    private static boolean ammo(ServerPlayer p, int n) {
        if (p.getAbilities().instabuild) return true;
        if (p.getInventory().countItem(ModItems.PISTOL_SHOT) < n) {
            p.sendSystemMessage(Component.literal("Out of shot.").withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        for (int k = 0; k < n; k++) {
            var inv = p.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).is(ModItems.PISTOL_SHOT)) { inv.getItem(i).shrink(1); break; }
            }
        }
        return true;
    }

    private static void gunSmoke(ServerPlayer p, ServerLevel w) {
        Vec3 l = p.getViewVector(1f);
        w.sendParticles(ParticleTypes.LARGE_SMOKE, p.getX() + l.x, p.getEyeY() - 0.2 + l.y, p.getZ() + l.z, 6, 0.1, 0.1, 0.1, 0.02);
        w.playSound(null, p.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.6f, 1.7f);
    }

    private static List<LivingEntity> foes(ServerPlayer p, double r) {
        AABB box = p.getBoundingBox().inflate(r);
        return p.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e != p && e.isAlive() && Levels.isFoe(e) && e.distanceToSqr(p) <= r * r);
    }

    private static boolean inFront(ServerPlayer p, Entity e, double halfAngle) {
        Vec3 to = e.position().subtract(p.position()).multiply(1, 0, 1).normalize();
        Vec3 look = p.getViewVector(1f).multiply(1, 0, 1).normalize();
        return to.dot(look) >= Math.cos(Math.toRadians(halfAngle));
    }

    /** The foe closest to where you're looking, within range and a narrow cone, that you can see. */
    private static LivingEntity aim(ServerPlayer p, double range, double cone) {
        LivingEntity locked = LockOn.target(p);
        if (locked != null && locked.distanceToSqr(p) <= range * range && p.hasLineOfSight(locked)) return locked;
        Vec3 eye = p.getEyePosition(), look = p.getViewVector(1f);
        LivingEntity best = null;
        double bestDot = Math.cos(Math.toRadians(cone));
        for (LivingEntity e : p.level().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(range),
                e -> e != p && e.isAlive() && !(e instanceof ServerPlayer) && !(e instanceof net.minecraft.world.entity.npc.villager.AbstractVillager))) {
            Vec3 to = e.getBoundingBox().getCenter().subtract(eye);
            double d = to.length();
            if (d > range) continue;
            double dot = to.normalize().dot(look);
            if (dot > bestDot && p.hasLineOfSight(e)) { bestDot = dot; best = e; }
        }
        return best;
    }
}
