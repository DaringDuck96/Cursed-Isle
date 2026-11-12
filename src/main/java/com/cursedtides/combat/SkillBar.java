package com.cursedtides.combat;

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
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

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

    public enum Family { BLADE, GUN, STAFF, DOLL }

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
        SHACKLES(Family.DOLL, "Shackles", () -> Items.CHAIN, 16, 14, 0, "Bind every attuned foe in place.");

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

    public static Family family(ItemStack held) {
        Item item = held.getItem();
        if (item instanceof FlintlockPistolItem) return Family.GUN;
        if (item instanceof HexStaffItem) return Family.STAFF;
        if (item == Grenades.VOODOO_DOLL) return Family.DOLL;
        if (item instanceof SwordItem || item instanceof AxeItem) return Family.BLADE;
        return null;
    }

    private static Weapon mastery(Family f) {
        return switch (f) {
            case BLADE -> Weapon.CUTLASS;
            case GUN -> Weapon.PISTOL;
            case STAFF, DOLL -> Weapon.HEX;
        };
    }

    // ------------------------------------------------------------------ network

    /** Client to server: fire skill slot 0-3. */
    public record Use(int slot) implements CustomPayload {
        public static final CustomPayload.Id<Use> ID = new CustomPayload.Id<>(CursedTides.id("skill_use"));
        public static final PacketCodec<RegistryByteBuf, Use> CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, Use::slot, Use::new);

        @Override
        public Id<? extends CustomPayload> getId() { return ID; }
    }

    /** Server to client: the bar for what's in hand (family -1 = none), which slots are open, and time left on each. */
    public record State(int family, int unlocked, List<Integer> cooldowns) implements CustomPayload {
        public static final CustomPayload.Id<State> ID = new CustomPayload.Id<>(CursedTides.id("skill_state"));
        public static final PacketCodec<RegistryByteBuf, State> CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_INT, State::family, PacketCodecs.VAR_INT, State::unlocked,
                PacketCodecs.VAR_INT.collect(PacketCodecs.toList()), State::cooldowns, State::new);

        @Override
        public Id<? extends CustomPayload> getId() { return ID; }
    }

    private static final Map<UUID, Map<Skill, Long>> READY = new HashMap<>();
    private static final Map<UUID, State> SENT = new HashMap<>();

    public static void register() {
        PayloadTypeRegistry.playC2S().register(Use.ID, Use.CODEC);
        PayloadTypeRegistry.playS2C().register(State.ID, State.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Use.ID, (payload, context) -> context.server().execute(() -> use(context.player(), payload.slot())));
        ServerTickEvents.END_SERVER_TICK.register(SkillBar::tick);
        ServerPlayConnectionEvents.DISCONNECT.register((h, s) -> {
            READY.remove(h.player.getUuid());
            SENT.remove(h.player.getUuid());
        });
    }

    private static void tick(MinecraftServer server) {
        if (server.getTicks() % 5 != 0) return;
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            State s = state(p);
            State last = SENT.get(p.getUuid());
            if (!s.equals(last)) {
                SENT.put(p.getUuid(), s);
                ServerPlayNetworking.send(p, s);
            }
        }
    }

    private static State state(ServerPlayerEntity p) {
        Family f = family(p.getMainHandStack());
        if (f == null) return new State(-1, 0, List.of(0, 0, 0, 0));
        int lvl = PirateData.level(p, mastery(f));
        long now = p.getServerWorld().getTime();
        int unlocked = 0;
        Integer[] cd = new Integer[4];
        Map<Skill, Long> ready = READY.getOrDefault(p.getUuid(), Map.of());
        for (int i = 0; i < 4; i++) {
            Skill s = Skill.of(f, i);
            if (s != null && s.level <= lvl) unlocked |= 1 << i;
            long r = s == null ? 0 : ready.getOrDefault(s, 0L);
            // Rounded to whole seconds so the bar is only re-sent as each second ticks by
            cd[i] = (int) Math.max(0, (r - now + 19) / 20) * 20;
        }
        return new State(f.ordinal(), unlocked, List.of(cd));
    }

    private static void use(ServerPlayerEntity p, int slot) {
        if (p.isSpectator() || p.hasVehicle() || slot < 0 || slot > 3) return;
        ItemStack held = p.getMainHandStack();
        Family f = family(held);
        if (f == null) {
            p.sendMessage(Text.literal("Draw a weapon to use its skills.").formatted(Formatting.GRAY), true);
            return;
        }
        Skill s = Skill.of(f, slot);
        if (s == null) return;
        if (PirateData.level(p, mastery(f)) < s.level) {
            p.sendMessage(Text.literal(s.title + " unlocks at " + mastery(f).name().toLowerCase() + " mastery " + s.level + ".").formatted(Formatting.GRAY), true);
            return;
        }
        long now = p.getServerWorld().getTime();
        Map<Skill, Long> ready = READY.computeIfAbsent(p.getUuid(), k -> new HashMap<>());
        if (ready.getOrDefault(s, 0L) > now) return;
        if (s.voodoo > 0 && !PirateData.spendVoodoo(p, s.voodoo)) {
            p.sendMessage(Text.translatable("message.cursedtides.no_voodoo").formatted(Formatting.DARK_AQUA), true);
            return;
        }
        if (!fire(p, s, held)) return;
        ready.put(s, now + s.cooldownTicks);
        p.sendMessage(Text.literal(s.title).formatted(Formatting.AQUA), true);
    }

    // ------------------------------------------------------------------ the skills

    private static boolean fire(ServerPlayerEntity p, Skill s, ItemStack held) {
        ServerWorld w = p.getServerWorld();
        Vec3d look = p.getRotationVec(1f);
        float base = (float) p.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_DAMAGE);
        switch (s) {
            case SWEEP -> {
                for (LivingEntity e : foes(p, 3.8)) {
                    if (inFront(p, e, 70)) {
                        e.damage(p.getDamageSources().playerAttack(p), base * 0.9f + 2);
                        e.takeKnockback(0.5, p.getX() - e.getX(), p.getZ() - e.getZ());
                    }
                }
                w.spawnParticles(ParticleTypes.SWEEP_ATTACK, p.getX() + look.x * 1.5, p.getBodyY(0.6), p.getZ() + look.z * 1.5, 3, 0.6, 0.1, 0.6, 0);
                w.playSound(null, p.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1f, 0.9f);
            }
            case LUNGE -> {
                p.setVelocity(look.x * 1.6, 0.15, look.z * 1.6);
                p.velocityModified = true;
                LivingEntity t = aim(p, 7, 30);
                if (t != null) t.damage(p.getDamageSources().playerAttack(p), base * 1.4f + 3);
                w.playSound(null, p.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.PLAYERS, 1f, 1.2f);
            }
            case HAMSTRING -> {
                LivingEntity t = aim(p, 4, 40);
                if (t == null) return nothing(p);
                t.damage(p.getDamageSources().playerAttack(p), base + 2);
                t.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 120, 2));
                t.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 120, 1));
                w.spawnParticles(ParticleTypes.CRIT, t.getX(), t.getBodyY(0.3), t.getZ(), 12, 0.3, 0.2, 0.3, 0.2);
            }
            case WHIRLWIND -> {
                for (LivingEntity e : foes(p, 4.6)) {
                    e.damage(p.getDamageSources().playerAttack(p), base * 1.3f + 4);
                    e.takeKnockback(0.9, p.getX() - e.getX(), p.getZ() - e.getZ());
                }
                for (int k = 0; k < 8; k++) {
                    double a = Math.PI * 2 * k / 8;
                    w.spawnParticles(ParticleTypes.SWEEP_ATTACK, p.getX() + Math.cos(a) * 2.2, p.getBodyY(0.5), p.getZ() + Math.sin(a) * 2.2, 1, 0, 0, 0, 0);
                }
                w.playSound(null, p.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.2f, 0.6f);
            }
            case TAKE_AIM, LEG_SHOT, VOLLEY -> {
                int shots = s == Skill.VOLLEY ? 3 : 1;
                if (!ammo(p, shots)) return false;
                LivingEntity t = aim(p, 40, 6);
                gunSmoke(p, w);
                if (t == null) return true;
                float dmg = s == Skill.TAKE_AIM ? 14 : s == Skill.LEG_SHOT ? 6 : 7;
                for (int k = 0; k < shots; k++) {
                    t.timeUntilRegen = 0;
                    t.damage(p.getDamageSources().playerAttack(p), dmg);
                }
                if (s == Skill.LEG_SHOT) t.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 140, 3));
            }
            case SCATTERSHOT -> {
                if (!ammo(p, 1)) return false;
                gunSmoke(p, w);
                for (LivingEntity e : foes(p, 9)) {
                    if (inFront(p, e, 35)) {
                        e.timeUntilRegen = 0;
                        e.damage(p.getDamageSources().playerAttack(p), 7);
                    }
                }
            }
            case HEX_BLAST -> {
                LivingEntity t = aim(p, 24, 8);
                if (t == null) return nothing(p);
                t.damage(p.getDamageSources().indirectMagic(p, p), 9);
                w.spawnParticles(ParticleTypes.WITCH, t.getX(), t.getBodyY(0.5), t.getZ(), 30, 0.4, 0.5, 0.4, 0.1);
                w.playSound(null, t.getBlockPos(), SoundEvents.ENTITY_EVOKER_CAST_SPELL, SoundCategory.PLAYERS, 1f, 1.2f);
            }
            case SPIRIT_WARD -> {
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 160, 1));
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 300, 1));
                w.spawnParticles(ParticleTypes.SOUL, p.getX(), p.getBodyY(0.5), p.getZ(), 30, 0.6, 0.8, 0.6, 0.02);
                w.playSound(null, p.getBlockPos(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 0.8f, 1.5f);
            }
            case WITHERING -> {
                LivingEntity t = aim(p, 24, 10);
                if (t == null) return nothing(p);
                for (LivingEntity e : w.getEntitiesByClass(LivingEntity.class, t.getBoundingBox().expand(4), e -> e != p && Levels.isFoe(e))) {
                    e.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 120, 1));
                    e.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 160, 1));
                }
                w.spawnParticles(ParticleTypes.SQUID_INK, t.getX(), t.getBodyY(0.5), t.getZ(), 40, 2, 0.6, 2, 0.02);
            }
            case SIPHON -> {
                LivingEntity t = aim(p, 20, 8);
                if (t == null) return nothing(p);
                float before = t.getHealth();
                t.damage(p.getDamageSources().indirectMagic(p, p), 11);
                p.heal(Math.max(0, before - t.getHealth()));
                w.spawnParticles(ParticleTypes.DAMAGE_INDICATOR, t.getX(), t.getBodyY(0.6), t.getZ(), 10, 0.3, 0.3, 0.3, 0.1);
                w.spawnParticles(ParticleTypes.HEART, p.getX(), p.getBodyY(1), p.getZ(), 4, 0.3, 0.3, 0.3, 0.1);
            }
            case POKE, MEND, SWARM, SHACKLES -> {
                if (p.isSneaking()) return false;
                String working = switch (s) { case POKE -> "POKE"; case MEND -> "HEAL"; case SWARM -> "SWARM"; default -> "SHACKLES"; };
                NbtCompound n = held.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA, net.minecraft.component.type.NbtComponent.DEFAULT).copyNbt();
                n.putInt("Working", VoodooDollItem.Working.valueOf(working).ordinal());
                held.set(net.minecraft.component.DataComponentTypes.CUSTOM_DATA, net.minecraft.component.type.NbtComponent.of(n));
                return held.getItem().use(w, p, Hand.MAIN_HAND).getResult().isAccepted();
            }
        }
        return true;
    }

    private static boolean nothing(ServerPlayerEntity p) {
        p.sendMessage(Text.literal("No foe in your sights.").formatted(Formatting.GRAY), true);
        return false;
    }

    private static boolean ammo(ServerPlayerEntity p, int n) {
        if (p.getAbilities().creativeMode) return true;
        if (p.getInventory().count(ModItems.PISTOL_SHOT) < n) {
            p.sendMessage(Text.literal("Out of shot.").formatted(Formatting.GRAY), true);
            return false;
        }
        for (int k = 0; k < n; k++) {
            var inv = p.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                if (inv.getStack(i).isOf(ModItems.PISTOL_SHOT)) { inv.getStack(i).decrement(1); break; }
            }
        }
        return true;
    }

    private static void gunSmoke(ServerPlayerEntity p, ServerWorld w) {
        Vec3d l = p.getRotationVec(1f);
        w.spawnParticles(ParticleTypes.LARGE_SMOKE, p.getX() + l.x, p.getEyeY() - 0.2 + l.y, p.getZ() + l.z, 6, 0.1, 0.1, 0.1, 0.02);
        w.playSound(null, p.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, 0.6f, 1.7f);
    }

    private static List<LivingEntity> foes(ServerPlayerEntity p, double r) {
        Box box = p.getBoundingBox().expand(r);
        return p.getServerWorld().getEntitiesByClass(LivingEntity.class, box,
                e -> e != p && e.isAlive() && Levels.isFoe(e) && e.squaredDistanceTo(p) <= r * r);
    }

    private static boolean inFront(ServerPlayerEntity p, Entity e, double halfAngle) {
        Vec3d to = e.getPos().subtract(p.getPos()).multiply(1, 0, 1).normalize();
        Vec3d look = p.getRotationVec(1f).multiply(1, 0, 1).normalize();
        return to.dotProduct(look) >= Math.cos(Math.toRadians(halfAngle));
    }

    /** The foe closest to where you're looking, within range and a narrow cone, that you can see. */
    private static LivingEntity aim(ServerPlayerEntity p, double range, double cone) {
        Vec3d eye = p.getEyePos(), look = p.getRotationVec(1f);
        LivingEntity best = null;
        double bestDot = Math.cos(Math.toRadians(cone));
        for (LivingEntity e : p.getServerWorld().getEntitiesByClass(LivingEntity.class, p.getBoundingBox().expand(range),
                e -> e != p && e.isAlive() && !(e instanceof ServerPlayerEntity) && !(e instanceof net.minecraft.entity.passive.MerchantEntity))) {
            Vec3d to = e.getBoundingBox().getCenter().subtract(eye);
            double d = to.length();
            if (d > range) continue;
            double dot = to.normalize().dotProduct(look);
            if (dot > bestDot && p.canSee(e)) { bestDot = dot; best = e; }
        }
        return best;
    }
}
