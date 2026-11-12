package com.cursedtides.living;

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
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.literal;

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
    private static ServerBossBar bar;
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
            if (!victim.getCommandTags().contains(TAG)) return;
            ALIVE.remove(victim.getUuid());
            if (source.getAttacker() instanceof ServerPlayerEntity p) KILLS.merge(p.getUuid(), victim instanceof DrownedCaptainEntity ? 25 : 1, Integer::sum);
            if (victim instanceof DrownedCaptainEntity && victim.getServer() != null) end(victim.getServer(), true);
        });
        CommandRegistrationCallback.EVENT.register((d, a, e) -> d.register(literal("cursedtides").requires(s -> s.hasPermissionLevel(2))
                .then(literal("siege").executes(ctx -> {
                    ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                    Archipelago.Island nearest = PORTS[0];
                    for (Archipelago.Island is : PORTS) if (p.squaredDistanceTo(is.cx(), p.getY(), is.cz()) < p.squaredDistanceTo(nearest.cx(), p.getY(), nearest.cz())) nearest = is;
                    announce(ctx.getSource().getServer(), nearest, 60 * 20);
                    return 1;
                }))));
        CommandRegistrationCallback.EVENT.register((d, a, e) -> d.register(literal("siege").executes(ctx -> {
            ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
            if (town != null) p.sendMessage(Text.literal("The siege of " + town.name() + " is " + (running() ? "under way!" : "coming!")).formatted(Formatting.RED), false);
            else p.sendMessage(Text.literal("The next siege falls on Saturday at 8pm (server time). This week the cursed are expected at "
                    + thisWeeksPort().name() + ".").formatted(Formatting.GOLD), false);
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
        server.getPlayerManager().broadcast(Text.literal("☠ The cursed fleet is sighted off " + where.name() + "! The siege begins in "
                + (ticks / 20 >= 60 ? ticks / 1200 + " minutes" : ticks / 20 + " seconds") + ". Every blade to the walls!").formatted(Formatting.DARK_RED, Formatting.BOLD), false);
    }

    private static void tick(MinecraftServer server) {
        if (!ModWorld.isArchipelago()) return;
        // The weekly schedule, by the real clock: warnings at 7:50 and 7:59, the attack at 8:00 on Saturdays
        if (server.getTicks() % 200 == 0 && town == null) {
            LocalDateTime now = LocalDateTime.now();
            String key = now.toLocalDate() + "-" + now.getHour() + ":" + now.getMinute() / 10;
            if (now.getDayOfWeek() == DayOfWeek.SATURDAY && now.getHour() == 19 && now.getMinute() >= 50 && !key.equals(lastAnnounced)) {
                lastAnnounced = key;
                int secondsLeft = (60 - now.getMinute()) * 60 - now.getSecond();
                announce(server, thisWeeksPort(), Math.max(20 * 30, secondsLeft * 20));
            }
        }
        if (town == null) return;
        ServerWorld w = server.getOverworld();
        if (countdown > 0) {
            countdown--;
            if (countdown == 20 * 60) server.getPlayerManager().broadcast(Text.literal("One minute until the cursed land at " + town.name() + "!").formatted(Formatting.RED), false);
            if (countdown == 0) begin(server, w);
            return;
        }
        if (!running()) return;
        if (server.getTicks() % 20 != 0) return;
        // Keep everyone in town in the fight: the bar, and the plaza count
        int px = town.cx(), pz = town.cz();
        for (ServerPlayerEntity p : w.getPlayers()) {
            if (p.squaredDistanceTo(px, p.getY(), pz) < 160 * 160) bar.addPlayer(p); else bar.removePlayer(p);
        }
        int inPlaza = 0;
        for (UUID id : List.copyOf(ALIVE)) {
            if (!(w.getEntity(id) instanceof MobEntity m) || !m.isAlive()) { ALIVE.remove(id); continue; }
            if (m.squaredDistanceTo(px, m.getY(), pz) < 12 * 12) inPlaza++;
            if (m.getTarget() == null || !m.getTarget().isAlive()) {
                ServerPlayerEntity near = (ServerPlayerEntity) w.getClosestPlayer(m, 24);
                if (near != null && !near.isCreative()) m.setTarget(near);
                else m.getNavigation().startMovingTo(px + 0.5, com.cursedtides.world.Builds.groundY(px, pz), pz + 0.5, 1.0);
            }
        }
        if (inPlaza > 0) {
            morale -= inPlaza;
            if (morale <= 0) { end(server, false); return; }
        }
        bar.setName(Text.literal("Siege of " + town.name() + ": wave " + wave + "/" + WAVES + (bossOut ? " - the Dread Captain!" : "") + "   morale " + morale));
        bar.setPercent(Math.max(0, morale) / 100f);
        waveTimer--;
        if (!bossOut && (ALIVE.size() <= 3 || waveTimer <= 0)) {
            if (wave < WAVES) spawnWave(server, w);
            else spawnBoss(w);
        }
    }

    private static void begin(MinecraftServer server, ServerWorld w) {
        bar = new ServerBossBar(Text.literal("Siege"), BossBar.Color.RED, BossBar.Style.NOTCHED_10);
        w.playSound(null, town.cx(), com.cursedtides.world.Builds.groundY(town.cx(), town.cz()), town.cz(),
                com.cursedtides.sound.ModSounds.BELL_TOLL.value(), SoundCategory.HOSTILE, 6f, 0.7f);
        server.getPlayerManager().broadcast(Text.literal("The cursed are ashore at " + town.name() + "! Hold the plaza!").formatted(Formatting.DARK_RED, Formatting.BOLD), false);
        spawnWave(server, w);
    }

    private static void spawnWave(MinecraftServer server, ServerWorld w) {
        wave++;
        waveTimer = 50;
        int defenders = Math.max(1, w.getPlayers(p -> p.squaredDistanceTo(town.cx(), p.getY(), town.cz()) < 160 * 160).size());
        int count = 5 + wave * 2 + defenders * 2;
        for (int beach = 0; beach < 3; beach++) {
            double a = Math.PI * 2 * beach / 3 + wave * 0.4;
            int bx = town.cx() + (int) (Math.cos(a) * town.radius() * 0.75), bz = town.cz() + (int) (Math.sin(a) * town.radius() * 0.75);
            for (int i = 0; i < count / 3 + 1; i++) {
                BlockPos at = Residents.openGround(w, bx + w.random.nextInt(7) - 3, bz + w.random.nextInt(7) - 3, 8);
                if (at == null || !w.isChunkLoaded(at)) continue;
                MobEntity m = (MobEntity) (wave >= 5 && i % 3 == 0 ? EntityType.WITHER_SKELETON.create(w)
                        : wave >= 3 && i % 2 == 0 ? EntityType.DROWNED.create(w) : ModEntities.CURSED_PIRATE.create(w));
                if (m == null) continue;
                m.refreshPositionAndAngles(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, w.random.nextFloat() * 360, 0);
                m.initialize(w, w.getLocalDifficulty(at), SpawnReason.EVENT, null);
                m.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
                m.setEquipmentDropChance(EquipmentSlot.HEAD, 0f);
                m.addCommandTag(TAG);
                m.setPersistent();
                w.spawnEntity(m);
                ALIVE.add(m.getUuid());
            }
        }
        if (wave == 4) {
            // A ghost ship stands off and shells the harbour
            for (ServerPlayerEntity p : w.getPlayers(p -> p.squaredDistanceTo(town.cx(), p.getY(), town.cz()) < 160 * 160)) {
                BlockPos sea = ShipEvents.findSeaSpot(w, p, 40, 70);
                if (sea != null) {
                    var ship = ShipEvents.spawnShip(w, ShipFaction.DROWNED, sea.getX() + 0.5, sea.getY() - 0.1, sea.getZ() + 0.5, p);
                    if (ship != null) ship.addCommandTag(TAG);
                    break;
                }
            }
        }
        server.getPlayerManager().broadcast(Text.literal("Siege of " + town.name() + ": wave " + wave + " lands!").formatted(Formatting.RED), false);
    }

    private static void spawnBoss(ServerWorld w) {
        bossOut = true;
        BlockPos at = Residents.openGround(w, town.cx() + (int) (town.radius() * 0.6), town.cz(), 10);
        if (at == null) at = new BlockPos(town.cx(), com.cursedtides.world.Builds.groundY(town.cx(), town.cz()), town.cz());
        DrownedCaptainEntity boss = ModEntities.DROWNED_CAPTAIN.create(w);
        if (boss == null) return;
        boss.refreshPositionAndAngles(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0, 0);
        boss.initialize(w, w.getLocalDifficulty(at), SpawnReason.EVENT, null);
        boss.setCustomName(Text.literal("The Dread Captain").formatted(Formatting.DARK_RED, Formatting.BOLD));
        boss.setCustomNameVisible(true);
        var hp = boss.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (hp != null) hp.setBaseValue(450);
        boss.setHealth(450);
        boss.addCommandTag(TAG);
        boss.addCommandTag("cursedtides_boss");
        boss.setPersistent();
        w.spawnEntity(boss);
        ALIVE.add(boss.getUuid());
        w.getServer().getPlayerManager().broadcast(Text.literal("The Dread Captain himself wades ashore at " + town.name() + "!").formatted(Formatting.DARK_RED, Formatting.BOLD), false);
    }

    private static void end(MinecraftServer server, boolean held) {
        if (town == null) return;
        ServerWorld w = server.getOverworld();
        Archipelago.Island where = town;
        for (UUID id : ALIVE) if (w.getEntity(id) instanceof MobEntity m) m.discard();
        for (var ship : w.getEntitiesByClass(com.cursedtides.entity.ShipEntity.class, new Box(where.cx() - 200, w.getBottomY(), where.cz() - 200,
                where.cx() + 200, w.getTopY(), where.cz() + 200), s -> s.getCommandTags().contains(TAG))) {
            ship.getPassengerList().forEach(net.minecraft.entity.Entity::discard);
            ship.discard();
        }
        if (held) {
            Prosperity.adjust(server, where.cx(), where.cz(), 20, "it threw back the siege");
            server.getPlayerManager().broadcast(Text.literal(where.name() + " holds! The cursed fleet limps away. Defenders, collect your pay.").formatted(Formatting.GOLD, Formatting.BOLD), false);
        } else {
            Prosperity.adjust(server, where.cx(), where.cz(), -40, "it fell to the siege");
            server.getPlayerManager().broadcast(Text.literal(where.name() + " has fallen! The cursed loot the warehouses before the tide turns.").formatted(Formatting.DARK_RED, Formatting.BOLD), false);
        }
        List<Map.Entry<UUID, Integer>> ranked = new ArrayList<>(KILLS.entrySet());
        ranked.sort((a, b) -> b.getValue() - a.getValue());
        for (int i = 0; i < ranked.size(); i++) {
            ServerPlayerEntity p = server.getPlayerManager().getPlayer(ranked.get(i).getKey());
            if (p == null) continue;
            int kills = ranked.get(i).getValue();
            int pay = (held ? 10 : 3) + kills / 2 + (held && i == 0 ? 40 : held && i < 3 ? 20 : 0);
            p.giveItemStack(new ItemStack(ModItems.DOUBLOON, Math.min(64, pay)));
            com.cursedtides.activity.Leaderboards.add(p, "siege", kills);
            if (held) {
                com.cursedtides.activity.Achievements.grant(p, "siege_breaker");
                if (i == 0) p.giveItemStack(com.cursedtides.combat.GearRarity.randomWeapon(w, w.random, 4));
            }
            p.sendMessage(Text.literal("Siege tally: " + kills + " points" + (i < 3 ? ", #" + (i + 1) + " among the defenders" : "") + ".").formatted(Formatting.GOLD), false);
        }
        if (bar != null) bar.clearPlayers();
        bar = null;
        town = null;
        wave = 0;
        countdown = -1;
        ALIVE.clear();
    }
}
