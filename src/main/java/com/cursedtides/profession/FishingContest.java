package com.cursedtides.profession;

import com.cursedtides.entity.StoryNpcEntity;
import com.cursedtides.item.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The Mariner's Rest fishing contest: every two days, the heaviest single fish shown to the judge wins.
 * Prizes: 40, 20 and 10 doubloons, and the trophy for first. Prizes wait with the judge if you're away.
 */
public final class FishingContest {
    private FishingContest() {}

    private static final long LENGTH = 48000;

    static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 200 == 0) State.get(server).rollOver(server);
        });
    }

    private static long contestId(MinecraftServer server) {
        return server.overworld().getOverworldClockTime() / LENGTH;
    }

    public static void talk(StoryNpcEntity npc, ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        if (server == null) return;
        State s = State.get(server);
        s.rollOver(server);
        List<ItemStack> prizes = s.prizes.remove(player.getUUID());
        if (prizes != null) {
            for (ItemStack prize : prizes) if (!player.getInventory().add(prize)) player.drop(prize, false, net.minecraft.util.Prediction.SERVER_ONLY);
            say(npc, player, "Your winnings from the last contest! Well fished.");
            s.setDirty();
        }
        ItemStack held = player.getMainHandItem();
        if (held.getItem() instanceof CatchItem) {
            int w = CatchItem.weight(held);
            Entry best = s.entries.get(player.getUUID());
            if (best != null && best.weight >= w) {
                say(npc, player, String.format("%.1f lb. A fine fish, but your %s at %.1f lb is still your best.",
                        w / 10.0, best.species, best.weight / 10.0));
            } else {
                s.entries.put(player.getUUID(), new Entry(player.getName().getString(), held.getHoverName().getString(), w));
                s.setDirty();
                say(npc, player, String.format("Let's see... %.1f lb! That's on the board.", w / 10.0));
            }
        } else {
            say(npc, player, "The contest runs two days. Bring me your heaviest catch (hold it up) and I'll weigh it.",
                    "Snapper, swordfish, marlin if you've the skill for it. Biggest single fish wins.");
        }
        List<Entry> board = s.ranked();
        long left = (contestId(server) + 1) * LENGTH - server.overworld().getOverworldClockTime();
        StringBuilder sb = new StringBuilder("Board (" + (left / 1000) + " hours left):");
        if (board.isEmpty()) sb.append(" nothing yet.");
        for (int i = 0; i < Math.min(5, board.size()); i++) {
            Entry e = board.get(i);
            sb.append(String.format(" %d. %s, %s %.1f lb", i + 1, e.name, e.species, e.weight / 10.0)).append(i < 4 ? ";" : "");
        }
        say(npc, player, sb.toString());
    }

    private static void say(StoryNpcEntity npc, ServerPlayer p, String... lines) {
        com.cursedtides.quest.StoryDialogue.say(npc, p, lines);
    }

    record Entry(String name, String species, int weight) {}

    static final class State extends SavedData {
        long contest = -1;
        final Map<UUID, Entry> entries = new HashMap<>();
        final Map<UUID, List<ItemStack>> prizes = new HashMap<>();
        private static final net.minecraft.world.level.saveddata.SavedDataType<State> TYPE = com.cursedtides.util.Saved.type("cursedtides_fishing_contest", State::new, State::fromNbt, (st__, l__) -> st__.save(new CompoundTag(), l__));

        static State get(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(TYPE);
        }

        List<Entry> ranked() {
            List<Entry> list = new ArrayList<>(entries.values());
            list.sort((a, b) -> Integer.compare(b.weight, a.weight));
            return list;
        }

        void rollOver(MinecraftServer server) {
            long now = contestId(server);
            if (contest == now) return;
            if (contest >= 0 && !entries.isEmpty()) {
                List<Map.Entry<UUID, Entry>> ranked = new ArrayList<>(entries.entrySet());
                ranked.sort((a, b) -> Integer.compare(b.getValue().weight, a.getValue().weight));
                int[] purse = {40, 20, 10};
                StringBuilder news = new StringBuilder("The Mariner's Rest fishing contest is over! ");
                for (int i = 0; i < Math.min(3, ranked.size()); i++) {
                    UUID id = ranked.get(i).getKey();
                    Entry e = ranked.get(i).getValue();
                    List<ItemStack> prize = new ArrayList<>();
                    prize.add(new ItemStack(ModItems.DOUBLOON, purse[i]));
                    if (i == 0) prize.add(new ItemStack(ProfessionItems.FISHING_TROPHY));
                    ServerPlayer p = server.getPlayerList().getPlayer(id);
                    if (p != null) {
                        for (ItemStack s : prize) if (!p.getInventory().add(s)) p.drop(s, false, net.minecraft.util.Prediction.SERVER_ONLY);
                        ProfessionData.addXp(p, Profession.FISHING, 60 - i * 20);
                    } else {
                        prizes.computeIfAbsent(id, k -> new ArrayList<>()).addAll(prize);
                    }
                    news.append(String.format("%d. %s (%s, %.1f lb) ", i + 1, e.name, e.species, e.weight / 10.0));
                }
                server.getPlayerList().broadcastSystemMessage(Component.literal(news.toString().trim()).withStyle(ChatFormatting.AQUA), false);
            }
            entries.clear();
            contest = now;
            setDirty();
        }

                public CompoundTag save(CompoundTag nbt, HolderLookup.Provider lookup) {
            nbt.putLong("Contest", contest);
            ListTag list = new ListTag();
            entries.forEach((id, e) -> {
                CompoundTag c = new CompoundTag();
                c.store("Id", net.minecraft.core.UUIDUtil.CODEC, id);
                c.putString("Name", e.name);
                c.putString("Species", e.species);
                c.putInt("Weight", e.weight);
                list.add(c);
            });
            nbt.put("Entries", list);
            ListTag pz = new ListTag();
            prizes.forEach((id, stacks) -> {
                for (ItemStack s : stacks) {
                    CompoundTag c = new CompoundTag();
                    c.store("Id", net.minecraft.core.UUIDUtil.CODEC, id);
                    c.put("Item", com.cursedtides.util.Compat.saveStack(s, lookup));
                    pz.add(c);
                }
            });
            nbt.put("Prizes", pz);
            return nbt;
        }

        private static State fromNbt(CompoundTag nbt, HolderLookup.Provider lookup) {
            State s = new State();
            s.contest = nbt.contains("Contest") ? nbt.getLongOr("Contest", 0L) : -1;
            ListTag list = nbt.getListOrEmpty("Entries");
            for (int i = 0; i < list.size(); i++) {
                CompoundTag c = list.getCompoundOrEmpty(i);
                s.entries.put(c.read("Id", net.minecraft.core.UUIDUtil.CODEC).orElse(null), new Entry(c.getStringOr("Name", ""), c.getStringOr("Species", ""), c.getIntOr("Weight", 0)));
            }
            ListTag pz = nbt.getListOrEmpty("Prizes");
            for (int i = 0; i < pz.size(); i++) {
                CompoundTag c = pz.getCompoundOrEmpty(i);
                com.cursedtides.util.Compat.loadStack(lookup, c.get("Item")).ifPresent(st ->
                        s.prizes.computeIfAbsent(c.read("Id", net.minecraft.core.UUIDUtil.CODEC).orElse(null), k -> new ArrayList<>()).add(st));
            }
            return s;
        }
    }
}
