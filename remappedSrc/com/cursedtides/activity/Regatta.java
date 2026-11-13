package com.cursedtides.activity;

import com.cursedtides.entity.StoryNpcEntity;
import com.cursedtides.item.ModItems;
import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import com.cursedtides.world.Archipelago;
import com.cursedtides.world.ModWorld;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The Stormbreak regatta: one lap of eight buoys around the island, threading the sea stacks,
 * against the clock. Best times are kept for the whole server. Also brews Stormbreak's weather.
 */
public final class Regatta {
    private Regatta() {}

    /** Buoy offsets from the island centre, in course order. Must match OuterBuilds.BUOYS. */
    private static final int[][] BUOYS = {{0, -80}, {41, -41}, {80, 0}, {41, 41}, {0, 80}, {-41, 41}, {-80, 0}, {-41, -41}};
    private static final double REACH = 11.0;
    private static final int TIME_LIMIT = 20 * 60 * 6;

    private record Run(int startTick, int next) {}
    private static final Map<UUID, Run> RUNS = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(Regatta::tick);
    }

    public static void talk(StoryNpcEntity npc, ServerPlayer player) {
        Records records = Records.get(player.getServer());
        List<String> lines = new ArrayList<>();
        if (RUNS.containsKey(player.getUUID())) {
            RUNS.remove(player.getUUID());
            lines.add("Giving up? Fair enough. The stacks have humbled better sailors. Talk to me when you want another go.");
        } else {
            RUNS.put(player.getUUID(), new Run(player.getServer().getTickCount(), 0));
            lines.add("One lap, eight buoys, yellow first, then sunwise round the island. Mind the stacks.");
            lines.add("The clock's running the moment you leave this hut. Get to your boat!");
        }
        lines.add("Record: " + records.describeBest());
        Integer mine = records.best.get(player.getUUID());
        if (mine != null) lines.add("Your best: " + seconds(mine));
        com.cursedtides.quest.StoryDialogue.say(npc, player, lines.toArray(new String[0]));
    }

    private static String seconds(int ticks) {
        return String.format("%.1fs", ticks / 20.0);
    }

    private static void tick(MinecraftServer server) {
        if (!ModWorld.isArchipelago()) return;
        ServerLevel world = server.overworld();
        int now = server.getTickCount();
        var isle = Archipelago.STORMBREAK_ISLE;

        // Stormbreak lives up to its name: lightning over the water around it
        if (now % 50 == 0) {
            for (ServerPlayer p : world.players()) {
                double dx = p.getX() - isle.cx(), dz = p.getZ() - isle.cz();
                if (dx * dx + dz * dz > 220 * 220 || world.random.nextFloat() > 0.5f) continue;
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(world);
                if (bolt == null) continue;
                double a = world.random.nextDouble() * Math.PI * 2, d = 30 + world.random.nextDouble() * 60;
                bolt.moveTo(p.getX() + Math.cos(a) * d, Archipelago.WATER_TOP + 1, p.getZ() + Math.sin(a) * d);
                bolt.setVisualOnly(true);
                world.addFreshEntity(bolt);
            }
        }

        if (now % 4 != 0 || RUNS.isEmpty()) return;
        for (var it = RUNS.entrySet().iterator(); it.hasNext(); ) {
            var e = it.next();
            ServerPlayer p = server.getPlayerList().getPlayer(e.getKey());
            Run run = e.getValue();
            if (p == null) { it.remove(); continue; }
            int elapsed = now - run.startTick();
            if (elapsed > TIME_LIMIT) {
                p.displayClientMessage(Component.literal("Out of time! Talk to the Regatta Master to try again.").withStyle(ChatFormatting.RED), false);
                it.remove();
                continue;
            }
            int[] b = BUOYS[run.next() % BUOYS.length];
            double bx = isle.cx() + b[0] + 0.5, bz = isle.cz() + b[1] + 0.5;
            boolean afloat = p.getVehicle() instanceof Boat;
            if (now % 20 == 0) {
                p.displayClientMessage(Component.literal("Regatta " + seconds(elapsed) + "  |  buoy " + Math.min(run.next() + 1, 9) + "/9"
                        + (afloat ? "" : "  (get in a boat!)")).withStyle(ChatFormatting.AQUA), true);
            }
            if (!afloat || Math.hypot(p.getX() - bx, p.getZ() - bz) > REACH) continue;
            int next = run.next() + 1;
            world.playSound(null, p.blockPosition(), SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.PLAYERS, 1.0f, 0.8f + next * 0.08f);
            if (next < 9) {
                e.setValue(new Run(run.startTick(), next));
                continue;
            }
            // Back at the yellow buoy: finished
            it.remove();
            finish(server, p, elapsed);
        }
    }

    private static void finish(MinecraftServer server, ServerPlayer p, int ticks) {
        Records records = Records.get(server);
        boolean serverRecord = records.recordTicks == 0 || ticks < records.recordTicks;
        Integer best = records.best.get(p.getUUID());
        boolean personal = best == null || ticks < best;
        if (personal) records.best.put(p.getUUID(), ticks);
        records.names.put(p.getUUID(), p.getName().getString());
        if (serverRecord) {
            records.recordTicks = ticks;
            records.recordHolder = p.getName().getString();
        }
        records.setDirty();
        com.cursedtides.activity.Leaderboards.submit(p, "regatta", ticks);
        int purse = ticks < 20 * 75 ? 30 : ticks < 20 * 100 ? 18 : ticks < 20 * 140 ? 10 : 5;
        p.addItem(new ItemStack(ModItems.DOUBLOON, purse));
        PirateData.addXp(p, Weapon.SAILING, 40 + purse * 2);
        p.displayClientMessage(Component.literal("Finished in " + seconds(ticks) + "! Purse: " + purse + " doubloons"
                + (personal ? "  (personal best)" : "")).withStyle(ChatFormatting.GOLD), false);
        p.level().playSound(null, p.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 0.8f, 1.2f);
        if (serverRecord) {
            server.getPlayerList().broadcastSystemMessage(Component.literal(p.getName().getString() + " has set a new Stormbreak regatta record: "
                    + seconds(ticks) + "!").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        }
    }

    /** Server-wide regatta times. */
    public static final class Records extends SavedData {
        final Map<UUID, Integer> best = new HashMap<>();
        final Map<UUID, String> names = new HashMap<>();
        int recordTicks;
        String recordHolder = "";

        private static final Factory<Records> TYPE = new Factory<>(Records::new, Records::fromNbt, null);

        static Records get(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(TYPE, "cursedtides_regatta");
        }

        String describeBest() {
            if (recordTicks == 0) return "none yet. Be the first.";
            List<Map.Entry<UUID, Integer>> top = new ArrayList<>(best.entrySet());
            top.sort(Map.Entry.comparingByValue());
            StringBuilder sb = new StringBuilder(seconds(recordTicks) + " by " + recordHolder);
            if (top.size() > 1) {
                sb.append(". Leaderboard:");
                for (int i = 0; i < Math.min(5, top.size()); i++) {
                    sb.append(" ").append(i + 1).append(". ").append(names.getOrDefault(top.get(i).getKey(), "?"))
                            .append(" ").append(seconds(top.get(i).getValue()));
                }
            }
            return sb.toString();
        }

        @Override
        public CompoundTag save(CompoundTag nbt, HolderLookup.Provider lookup) {
            nbt.putInt("Record", recordTicks);
            nbt.putString("Holder", recordHolder);
            ListTag list = new ListTag();
            for (var e : best.entrySet()) {
                CompoundTag c = new CompoundTag();
                c.putUUID("Id", e.getKey());
                c.putInt("Ticks", e.getValue());
                c.putString("Name", names.getOrDefault(e.getKey(), "?"));
                list.add(c);
            }
            nbt.put("Times", list);
            return nbt;
        }

        private static Records fromNbt(CompoundTag nbt, HolderLookup.Provider lookup) {
            Records r = new Records();
            r.recordTicks = nbt.getInt("Record");
            r.recordHolder = nbt.getString("Holder");
            ListTag list = nbt.getList("Times", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag c = list.getCompound(i);
                r.best.put(c.getUUID("Id"), c.getInt("Ticks"));
                r.names.put(c.getUUID("Id"), c.getString("Name"));
            }
            return r;
        }
    }
}
