package com.cursedtides.living;

import net.minecraft.world.entity.EntityTypes;
import com.cursedtides.entity.DrownedCaptainEntity;
import com.cursedtides.entity.ModEntities;
import com.cursedtides.item.ModItems;
import com.cursedtides.ship.ShipEvents;
import com.cursedtides.ship.ShipFaction;
import com.cursedtides.world.Archipelago;
import com.cursedtides.world.ModWorld;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.commands.Commands.literal;

/**
 * The weekly siege. Every Saturday at 8pm (server time) the cursed fleet comes for one of the great ports, a different
 * one each week: six waves landing from three beaches, a ghost ship shelling the harbour, and the Dread Captain
 * himself at the end. Everyone in town fights. If enough of them reach the plaza, the town's morale breaks and the
 * port falls; hold it and every defender is paid by the town, the best of them handsomely.
 */
public final class Siege {
    private Siege() {}

    public static final String TAG = "cursedtides_siege";
    private static final Archipelago.Island[] PORTS = {Archipelago.SALTMARROW, Archipelago.PORT_MERIDIAN, Archipelago.KINGSPORT, Archipelago.SAINT_VESPER};
    private static final int WAVES = 6;

    private static Archipelago.Island town;
    private static int wave, countdown = -1, morale, waveTimer;
    private static boolean bossOut;
    private static ServerBossEvent bar;
    private static final Map<UUID, Integer> KILLS = new HashMap<>();
    private static final List<UUID> ALIVE = new ArrayList<>();
    private static String lastAnnounced = "";

    public static boolean running() { return town != null && countdown <= 0 && wave > 0; }

    public static Archipelago.Island thisWeeksPort() {
        int week = LocalDateTime.now().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        return PORTS[week % PORTS.length];
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(Siege::tick);
        ServerLivingEntityEvents.AFTER_DEATH.register((victim, source) -> {
            if (!victim.entityTags().contains(TAG)) return;
            ALIVE.remove(victim.getUUID());
            if (source.getEntity() instanceof ServerPlayer p) KILLS.merge(p.getUUID(), victim instanceof DrownedCaptainEntity ? 25 : 1, Integer::sum);
            if (victim instanceof DrownedCaptainEntity && victim.level().getServer() != null) end(victim.level().getServer(), true);
        });
        CommandRegistrationCallback.EVENT.register((d, a, e) -> d.register(literal("cursedtides").requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
                .then(literal("siege").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    Archipelago.Island nearest = PORTS[0];
                    for (Archipelago.Island is : PORTS) if (p.distanceToSqr(is.cx(), p.getY(), is.cz()) < p.distanceToSqr(nearest.cx(), p.getY(), nearest.cz())) nearest = is;
                    announce(ctx.getSource().getServer(), nearest, 60 * 20);
                    return 1;
                }))));
        CommandRegistrationCallback.EVENT.register((d, a, e) -> d.register(literal("siege").executes(ctx -> {
            ServerPlayer p = ctx.getSource().getPlayerOrException();
            if (town != null) p.sendSystemMessage(Component.literal("The siege of " + town.name() + " is " + (running() ? "under way!" : "coming!")).withStyle(ChatFormatting.RED), false);
            else p.sendSystemMessage(Component.literal("The next siege falls on Saturday at 8pm (server time). This week the cursed are expected at "
                    + thisWeeksPort().name() + ".").withStyle(ChatFormatting.GOLD), false);
            return 1;
        })));
    }

    private static void announce(MinecraftServer server, Archipelago.Island where, int ticks) {
        if (town != null) return;
        town = where;
        countdown = ticks;
        wave = 0;
        morale = 100;
        KILLS.clear();
        ALIVE.clear();
        bossOut = false;
        server.getPlayerList().broadcastSystemMessage(Component.literal("☠ The cursed fleet is sighted off " + where.name() + "! The siege begins in "
                + (ticks / 20 >= 60 ? ticks / 1200 + " minutes" : ticks / 20 + " seconds") + ". Every blade to the walls!").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
    }

    private static void tick(MinecraftServer server) {
        if (!ModWorld.isArchipelago()) return;
        // The weekly schedule, by the real clock: warnings at 7:50 and 7:59, the attack at 8:00 on Saturdays
        if (server.getTickCount() % 200 == 0 && town == null) {
            LocalDateTime now = LocalDateTime.now();
            String key = now.toLocalDate() + "-" + now.getHour() + ":" + now.getMinute() / 10;
            if (now.getDayOfWeek() == DayOfWeek.SATURDAY && now.getHour() == 19 && now.getMinute() >= 50 && !key.equals(lastAnnounced)) {
                lastAnnounced = key;
                int secondsLeft = (60 - now.getMinute()) * 60 - now.getSecond();
                announce(server, thisWeeksPort(), Math.max(20 * 30, secondsLeft * 20));
            }
        }
        if (town == null) return;
        ServerLevel w = server.overworld();
        if (countdown > 0) {
            countdown--;
            if (countdown == 20 * 60) server.getPlayerList().broadcastSystemMessage(Component.literal("One minute until the cursed land at " + town.name() + "!").withStyle(ChatFormatting.RED), false);
            if (countdown == 0) begin(server, w);
            return;
        }
        if (!running()) return;
        if (server.getTickCount() % 20 != 0) return;
        // Keep everyone in town in the fight: the bar, and the plaza count
        int px = town.cx(), pz = town.cz();
        for (ServerPlayer p : w.players()) {
            if (p.distanceToSqr(px, p.getY(), pz) < 160 * 160) bar.addPlayer(p); else bar.removePlayer(p);
        }
        int inPlaza = 0;
        for (UUID id : List.copyOf(ALIVE)) {
            if (!(w.getEntity(id) instanceof Mob m) || !m.isAlive()) { ALIVE.remove(id); continue; }
            if (m.distanceToSqr(px, m.getY(), pz) < 12 * 12) inPlaza++;
            if (m.getTarget() == null || !m.getTarget().isAlive()) {
                ServerPlayer near = (ServerPlayer) w.getNearestPlayer(m, 24);
                if (near != null && !near.isCreative()) m.setTarget(near);
                else m.getNavigation().moveTo(px + 0.5, com.cursedtides.world.Builds.groundY(px, pz), pz + 0.5, 1.0);
            }
        }
        if (inPlaza > 0) {
            morale -= inPlaza;
            if (morale <= 0) { end(server, false); return; }
        }
        bar.setName(Component.literal("Siege of " + town.name() + ": wave " + wave + "/" + WAVES + (bossOut ? " - the Dread Captain!" : "") + "   morale " + morale));
        bar.setProgress(Math.max(0, morale) / 100f);
        waveTimer--;
        if (!bossOut && (ALIVE.size() <= 3 || waveTimer <= 0)) {
            if (wave < WAVES) spawnWave(server, w);
            else spawnBoss(w);
        }
    }

    private static void begin(MinecraftServer server, ServerLevel w) {
        bar = new ServerBossEvent(java.util.UUID.randomUUID(), Component.literal("Siege"), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);
        w.playSound(null, town.cx(), com.cursedtides.world.Builds.groundY(town.cx(), town.cz()), town.cz(),
                com.cursedtides.sound.ModSounds.BELL_TOLL.value(), SoundSource.HOSTILE, 6f, 0.7f);
        server.getPlayerList().broadcastSystemMessage(Component.literal("The cursed are ashore at " + town.name() + "! Hold the plaza!").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
        spawnWave(server, w);
    }

    private static void spawnWave(MinecraftServer server, ServerLevel w) {
        wave++;
        waveTimer = 50;
        int defenders = Math.max(1, w.getPlayers(p -> p.distanceToSqr(town.cx(), p.getY(), town.cz()) < 160 * 160).size());
        int count = 5 + wave * 2 + defenders * 2;
        for (int beach = 0; beach < 3; beach++) {
            double a = Math.PI * 2 * beach / 3 + wave * 0.4;
            int bx = town.cx() + (int) (Math.cos(a) * town.radius() * 0.75), bz = town.cz() + (int) (Math.sin(a) * town.radius() * 0.75);
            for (int i = 0; i < count / 3 + 1; i++) {
                BlockPos at = Residents.openGround(w, bx + w.getRandom().nextInt(7) - 3, bz + w.getRandom().nextInt(7) - 3, 8);
                if (at == null || !w.hasChunkAt(at)) continue;
                Mob m = (Mob) (wave >= 5 && i % 3 == 0 ? EntityTypes.WITHER_SKELETON.create(w, EntitySpawnReason.EVENT)
                        : wave >= 3 && i % 2 == 0 ? EntityTypes.DROWNED.create(w, EntitySpawnReason.EVENT) : ModEntities.CURSED_PIRATE.create(w, EntitySpawnReason.EVENT));
                if (m == null) continue;
                m.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, w.getRandom().nextFloat() * 360, 0);
                m.finalizeSpawn(w, w.getCurrentDifficultyAt(at), EntitySpawnReason.EVENT, null);
                m.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
                m.setDropChance(EquipmentSlot.HEAD, 0f);
                m.addTag(TAG);
                m.setPersistenceRequired();
                w.addFreshEntity(m);
                ALIVE.add(m.getUUID());
            }
        }
        if (wave == 4) {
            // A ghost ship stands off and shells the harbour
            for (ServerPlayer p : w.getPlayers(p -> p.distanceToSqr(town.cx(), p.getY(), town.cz()) < 160 * 160)) {
                BlockPos sea = ShipEvents.findSeaSpot(w, p, 40, 70);
                if (sea != null) {
                    var ship = ShipEvents.spawnShip(w, ShipFaction.DROWNED, sea.getX() + 0.5, sea.getY() - 0.1, sea.getZ() + 0.5, p);
                    if (ship != null) ship.addTag(TAG);
                    break;
                }
            }
        }
        server.getPlayerList().broadcastSystemMessage(Component.literal("Siege of " + town.name() + ": wave " + wave + " lands!").withStyle(ChatFormatting.RED), false);
    }

    private static void spawnBoss(ServerLevel w) {
        bossOut = true;
        BlockPos at = Residents.openGround(w, town.cx() + (int) (town.radius() * 0.6), town.cz(), 10);
        if (at == null) at = new BlockPos(town.cx(), com.cursedtides.world.Builds.groundY(town.cx(), town.cz()), town.cz());
        DrownedCaptainEntity boss = ModEntities.DROWNED_CAPTAIN.create(w, EntitySpawnReason.EVENT);
        if (boss == null) return;
        boss.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0, 0);
        boss.finalizeSpawn(w, w.getCurrentDifficultyAt(at), EntitySpawnReason.EVENT, null);
        boss.setCustomName(Component.literal("The Dread Captain").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        boss.setCustomNameVisible(true);
        var hp = boss.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null) hp.setBaseValue(450);
        boss.setHealth(450);
        boss.addTag(TAG);
        boss.addTag("cursedtides_boss");
        boss.setPersistenceRequired();
        w.addFreshEntity(boss);
        ALIVE.add(boss.getUUID());
        w.getServer().getPlayerList().broadcastSystemMessage(Component.literal("The Dread Captain himself wades ashore at " + town.name() + "!").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
    }

    private static void end(MinecraftServer server, boolean held) {
        if (town == null) return;
        ServerLevel w = server.overworld();
        Archipelago.Island where = town;
        for (UUID id : ALIVE) if (w.getEntity(id) instanceof Mob m) m.discard();
        for (var ship : w.getEntitiesOfClass(com.cursedtides.entity.ShipEntity.class, new AABB(where.cx() - 200, w.getMinY(), where.cz() - 200,
                where.cx() + 200, w.getMaxY(), where.cz() + 200), s -> s.entityTags().contains(TAG))) {
            ship.getPassengers().forEach(net.minecraft.world.entity.Entity::discard);
            ship.discard();
        }
        if (held) {
            Prosperity.adjust(server, where.cx(), where.cz(), 20, "it threw back the siege");
            server.getPlayerList().broadcastSystemMessage(Component.literal(where.name() + " holds! The cursed fleet limps away. Defenders, collect your pay.").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        } else {
            Prosperity.adjust(server, where.cx(), where.cz(), -40, "it fell to the siege");
            server.getPlayerList().broadcastSystemMessage(Component.literal(where.name() + " has fallen! The cursed loot the warehouses before the tide turns.").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
        }
        List<Map.Entry<UUID, Integer>> ranked = new ArrayList<>(KILLS.entrySet());
        ranked.sort((a, b) -> b.getValue() - a.getValue());
        for (int i = 0; i < ranked.size(); i++) {
            ServerPlayer p = server.getPlayerList().getPlayer(ranked.get(i).getKey());
            if (p == null) continue;
            int kills = ranked.get(i).getValue();
            int pay = (held ? 10 : 3) + kills / 2 + (held && i == 0 ? 40 : held && i < 3 ? 20 : 0);
            p.addItem(new ItemStack(ModItems.DOUBLOON, Math.min(64, pay)));
            com.cursedtides.activity.Leaderboards.add(p, "siege", kills);
            if (held) {
                com.cursedtides.activity.Achievements.grant(p, "siege_breaker");
                if (i == 0) p.addItem(com.cursedtides.combat.GearRarity.randomWeapon(w, w.getRandom(), 4));
            }
            p.sendSystemMessage(Component.literal("Siege tally: " + kills + " points" + (i < 3 ? ", #" + (i + 1) + " among the defenders" : "") + ".").withStyle(ChatFormatting.GOLD), false);
        }
        if (bar != null) bar.removeAllPlayers();
        bar = null;
        town = null;
        wave = 0;
        countdown = -1;
        ALIVE.clear();
    }
}
