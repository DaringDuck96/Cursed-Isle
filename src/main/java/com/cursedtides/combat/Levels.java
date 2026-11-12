package com.cursedtides.combat;

import com.cursedtides.CursedTides;
import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import com.cursedtides.world.Archipelago;
import com.cursedtides.world.ModWorld;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Levels, the way the old pirate games did them. Every enemy in the archipelago has a level set by where it
 * lives (the starting beaches are gentle, the far reaches are deadly), shown after its name, and its health
 * and hits scale with it. Health is shown in big numbers (internal hit points times {@link #HP_SCALE}).
 * Hurting an enemy earns reputation in the weapon you used, more for tougher foes and next to nothing for
 * ones far beneath you; all of a pirate's reputation together sets their notoriety level, which raises
 * their health.
 */
public final class Levels {
    private Levels() {}

    /** One internal hit point (half a heart) is shown as this many. */
    public static final int HP_SCALE = 5;
    public static final String TAG_PREFIX = "ct_lv:";

    private static final Identifier HEALTH_MOD = CursedTides.id("level_health");
    private static final Identifier PLAYER_HEALTH = CursedTides.id("notoriety_health");

    // ------------------------------------------------------------------ where things live

    private static final Map<String, int[]> BANDS = new HashMap<>();

    private static void band(Archipelago.Island is, int lo, int hi) {
        BANDS.put(is.name(), new int[]{lo, hi});
    }

    static {
        band(Archipelago.DRIFTWOOD_ISLAND, 1, 3);
        band(Archipelago.SALTMARROW, 2, 5);
        band(Archipelago.MARINERS_REST, 3, 6);
        band(Archipelago.SUNKEN_REACH, 4, 7);
        band(Archipelago.MIREFEN_BAYOU, 5, 9);
        band(Archipelago.PORT_MERIDIAN, 6, 10);
        band(Archipelago.ASHFALL_ISLAND, 7, 11);
        band(Archipelago.HOLLOWGRAVE_ISLAND, 8, 12);
        band(Archipelago.IRONWATCH_ISLE, 9, 13);
        band(Archipelago.BLACKTHORN_ISLE, 10, 14);
        band(Archipelago.KINGSPORT, 10, 14);
        band(Archipelago.EMERALD_CAY, 11, 15);
        band(Archipelago.GOLDWATER_CAY, 12, 16);
        band(Archipelago.SAINT_VESPER, 12, 16);
        band(Archipelago.WHISPERING_KEY, 13, 17);
        band(Archipelago.CROOKED_REEF, 14, 18);
        band(Archipelago.RAVENS_LANDING, 15, 19);
        band(Archipelago.WIDOWS_ROCK, 16, 20);
        band(Archipelago.SERPENTS_HOLLOW, 18, 22);
        band(Archipelago.BLOODWAKE_CAY, 20, 24);
        band(Archipelago.STORMBREAK_ISLE, 22, 26);
        band(Archipelago.CROWNWATCH, 25, 29);
        band(Archipelago.GULLSCREAM_ROCK, 27, 31);
        band(Archipelago.EMBERFALL, 30, 34);
        band(Archipelago.FORGOTTEN_KEYS, 32, 36);
        band(Archipelago.ISLE_THAT_ISNT, 35, 39);
        band(Archipelago.BLACKVEIL_ATOLL, 38, 42);
        band(Archipelago.DROWNED_CITY, 40, 44);
        band(Archipelago.KINGSREST, 42, 46);
        band(Archipelago.LAST_HORIZON, 45, 50);
    }

    public static int[] band(Archipelago.Island is) {
        return BANDS.getOrDefault(is.name(), new int[]{1, 3});
    }

    private static Archipelago.Island nearest(int x, int z) {
        Archipelago.Island best = Archipelago.DRIFTWOOD_ISLAND;
        double bd = Double.MAX_VALUE;
        for (Archipelago.Island is : Archipelago.ISLANDS) {
            double d = is.edgeDistance(x, z);
            if (d < bd) { bd = d; best = is; }
        }
        return best;
    }

    /** The level range of a place: its island's (or the nearest island's, at sea), a few higher in the caverns. */
    public static int[] bandAt(BlockPos pos) {
        com.cursedtides.dungeon.Cavern cv = com.cursedtides.dungeon.Caverns.atQuiet(pos);
        if (cv != null) {
            int[] b = band(com.cursedtides.dungeon.Caverns.SITES.get(cv.index).island());
            return new int[]{Math.min(50, b[0] + 3), Math.min(50, b[1] + 3)};
        }
        return band(nearest(pos.getX(), pos.getZ()));
    }

    // ------------------------------------------------------------------ enemies

    public static int levelOf(Entity e) {
        for (String t : e.getCommandTags()) {
            if (t.startsWith(TAG_PREFIX)) {
                try { return Integer.parseInt(t.substring(TAG_PREFIX.length())); } catch (NumberFormatException ignored) { }
            }
        }
        return 0;
    }

    public static boolean isFoe(Entity e) {
        return e instanceof MobEntity && (e instanceof Monster || e.getType().getSpawnGroup() == SpawnGroup.MONSTER);
    }

    /** On the client, where tags aren't sent: a leveled foe wears its level in its name. */
    public static boolean isFoeClient(LivingEntity e) {
        return e.hasCustomName() && e.getCustomName().getString().contains("[Lv ");
    }

    public static double healthFactor(int level) {
        return 1 + 0.07 * (level - 1);
    }

    public static double damageFactor(int level) {
        return 1 + 0.055 * (level - 1);
    }

    /**
     * Give a creature its level. Ordinary foes get health to match; named ones (bosses and the like, which
     * set their own health) only get the level shown.
     */
    public static void apply(MobEntity mob, int level) {
        if (levelOf(mob) > 0) return;
        level = Math.max(1, Math.min(50, level));
        boolean named = mob.hasCustomName();
        mob.addCommandTag(TAG_PREFIX + level);
        Text base = named ? mob.getCustomName() : mob.getType().getName();
        MutableText name = base.copy().append(Text.literal(" [Lv " + level + "]").formatted(color(level)));
        mob.setCustomName(name);
        if (!named) {
            EntityAttributeInstance hp = mob.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
            if (hp != null && !hp.hasModifier(HEALTH_MOD)) {
                hp.addPersistentModifier(new EntityAttributeModifier(HEALTH_MOD, healthFactor(level) - 1,
                        EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                mob.setHealth(mob.getMaxHealth());
            }
        }
    }

    private static Formatting color(int level) {
        return level < 10 ? Formatting.GRAY : level < 20 ? Formatting.YELLOW : level < 30 ? Formatting.GOLD
                : level < 40 ? Formatting.RED : Formatting.DARK_RED;
    }

    private static void onLoad(Entity e, ServerWorld world) {
        if (!(e instanceof MobEntity mob) || !isFoe(e) || levelOf(e) > 0) return;
        if (world != world.getServer().getOverworld() || !ModWorld.isArchipelago()) return;
        int[] b = bandAt(e.getBlockPos());
        int level = mob.hasCustomName() ? b[1] + 2 : b[0] + world.random.nextInt(b[1] - b[0] + 1);
        apply(mob, level);
    }

    // ------------------------------------------------------------------ damage (called from LivingEntityMixin)

    /** Foes hit harder with level; pirates hit harder as they master the weapon in hand. */
    public static float scaleIncoming(LivingEntity victim, DamageSource source, float amount) {
        if (victim.getWorld().isClient) return amount;
        Entity attacker = source.getAttacker();
        if (attacker == null || attacker == victim) return amount;
        int lv = levelOf(attacker);
        if (lv > 0 && !(victim instanceof MobEntity && levelOf(victim) > 0)) amount *= (float) damageFactor(lv);
        if (attacker instanceof ServerPlayerEntity p && levelOf(victim) > 0) {
            Weapon w = com.cursedtides.pirate.PirateEvents.weaponFor(p, source.getSource());
            if (w != null) amount *= 1f + 0.015f * PirateData.level(p, w);
        }
        return amount;
    }

    private static final Map<UUID, Float> REP_REMAINDER = new HashMap<>();

    /** After a hit has landed: show the number, and earn reputation in the weapon that struck it. */
    public static void afterDamage(LivingEntity victim, DamageSource source, float dealt) {
        if (dealt <= 0 || victim.getWorld().isClient) return;
        Entity attacker = source.getAttacker();
        boolean crit = source.getSource() instanceof ServerPlayerEntity sp && sp.fallDistance > 0 && !sp.isOnGround();
        int shown = Math.max(1, Math.round(dealt * HP_SCALE));
        if (attacker instanceof ServerPlayerEntity p) {
            ServerPlayNetworking.send(p, new DamageNumber(victim.getId(), shown, crit ? 1 : 0));
            com.cursedtides.quest.Lessons.onHit(p, com.cursedtides.pirate.PirateEvents.weaponFor(p, source.getSource()), victim);
            int lv = levelOf(victim);
            if (lv > 0) {
                Weapon w = com.cursedtides.pirate.PirateEvents.weaponFor(p, source.getSource());
                if (w != null) {
                    double rel = 1 + (lv - PirateData.notoriety(p)) * 0.1;
                    rel = Math.max(0.1, Math.min(2.0, rel));
                    float rep = (float) (dealt * 0.6 * rel) + REP_REMAINDER.getOrDefault(p.getUuid(), 0f);
                    int whole = (int) rep;
                    REP_REMAINDER.put(p.getUuid(), rep - whole);
                    if (whole > 0) PirateData.addXp(p, w, whole);
                }
            }
        }
        if (victim instanceof ServerPlayerEntity p && attacker != null) {
            ServerPlayNetworking.send(p, new DamageNumber(victim.getId(), shown, 2));
        }
    }

    /** The bonus for finishing a foe: worth more the tougher it was. */
    public static int killReputation(ServerPlayerEntity p, LivingEntity victim) {
        int lv = levelOf(victim);
        if (lv <= 0) return Math.max(2, Math.round(victim.getMaxHealth() / 4f));
        double rel = Math.max(0.1, Math.min(2.0, 1 + (lv - PirateData.notoriety(p)) * 0.1));
        return Math.max(1, (int) Math.round((4 + lv * 1.5) * rel));
    }

    // ------------------------------------------------------------------ players

    public static double playerBonusHealth(int notoriety) {
        return (notoriety - 1) * 0.8;
    }

    public static void refreshPlayer(ServerPlayerEntity p) {
        EntityAttributeInstance hp = p.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (hp == null) return;
        double want = playerBonusHealth(PirateData.notoriety(p));
        EntityAttributeModifier cur = hp.getModifier(PLAYER_HEALTH);
        if (cur != null && cur.value() == want) return;
        boolean full = p.getHealth() >= p.getMaxHealth() - 0.01f;
        hp.removeModifier(PLAYER_HEALTH);
        if (want > 0) hp.addPersistentModifier(new EntityAttributeModifier(PLAYER_HEALTH, want, EntityAttributeModifier.Operation.ADD_VALUE));
        if (full) p.setHealth(p.getMaxHealth());
        sendHud(p);
    }

    public static void sendHud(ServerPlayerEntity p) {
        int total = PirateData.totalXp(p);
        int lv = PirateData.notoriety(p);
        int from = PirateData.totalXpForNotoriety(lv), to = PirateData.totalXpForNotoriety(lv + 1);
        ServerPlayNetworking.send(p, new Hud(lv, total - from, Math.max(1, to - from), PirateData.voodoo(p), PirateData.maxVoodoo(p)));
    }

    private static final Map<UUID, Integer> LAST_HUD = new HashMap<>();

    private static void tick(MinecraftServer server) {
        if (server.getTicks() % 10 != 0) return;
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            int key = PirateData.totalXp(p) * 31 + PirateData.voodoo(p);
            Integer last = LAST_HUD.put(p.getUuid(), key);
            if (last == null || last != key) sendHud(p);
        }
    }

    // ------------------------------------------------------------------ payloads

    /** kind: 0 = you hit something, 1 = a critical hit, 2 = you were hit. */
    public record DamageNumber(int entity, int amount, int kind) implements CustomPayload {
        public static final CustomPayload.Id<DamageNumber> ID = new CustomPayload.Id<>(CursedTides.id("damage_number"));
        public static final PacketCodec<RegistryByteBuf, DamageNumber> CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_INT, DamageNumber::entity, PacketCodecs.VAR_INT, DamageNumber::amount,
                PacketCodecs.VAR_INT, DamageNumber::kind, DamageNumber::new);

        @Override
        public Id<? extends CustomPayload> getId() { return ID; }
    }

    public record Hud(int notoriety, int rep, int repNeeded, int voodoo, int maxVoodoo) implements CustomPayload {
        public static final CustomPayload.Id<Hud> ID = new CustomPayload.Id<>(CursedTides.id("hud"));
        public static final PacketCodec<RegistryByteBuf, Hud> CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_INT, Hud::notoriety, PacketCodecs.VAR_INT, Hud::rep, PacketCodecs.VAR_INT, Hud::repNeeded,
                PacketCodecs.VAR_INT, Hud::voodoo, PacketCodecs.VAR_INT, Hud::maxVoodoo, Hud::new);

        @Override
        public Id<? extends CustomPayload> getId() { return ID; }
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(DamageNumber.ID, DamageNumber.CODEC);
        PayloadTypeRegistry.playS2C().register(Hud.ID, Hud.CODEC);
        ServerEntityEvents.ENTITY_LOAD.register(Levels::onLoad);
        ServerTickEvents.END_SERVER_TICK.register(Levels::tick);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> server.execute(() -> refreshPlayer(handler.player)));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldP, p, alive) -> refreshPlayer(p));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            LAST_HUD.remove(handler.player.getUuid());
            REP_REMAINDER.remove(handler.player.getUuid());
        });
    }
}
