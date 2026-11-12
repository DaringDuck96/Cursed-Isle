package com.cursedtides.profession;

import com.cursedtides.entity.StoryNpcEntity;
import com.cursedtides.item.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.PersistentState;

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
            if (server.getTicks() % 200 == 0) State.get(server).rollOver(server);
        });
    }

    private static long contestId(MinecraftServer server) {
        return server.getOverworld().getTimeOfDay() / LENGTH;
    }

    public static void talk(StoryNpcEntity npc, ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        State s = State.get(server);
        s.rollOver(server);
        List<ItemStack> prizes = s.prizes.remove(player.getUuid());
        if (prizes != null) {
            for (ItemStack prize : prizes) if (!player.getInventory().insertStack(prize)) player.dropItem(prize, false);
            say(npc, player, "Your winnings from the last contest! Well fished.");
            s.markDirty();
        }
        ItemStack held = player.getMainHandStack();
        if (held.getItem() instanceof CatchItem) {
            int w = CatchItem.weight(held);
            Entry best = s.entries.get(player.getUuid());
            if (best != null && best.weight >= w) {
                say(npc, player, String.format("%.1f lb. A fine fish, but your %s at %.1f lb is still your best.",
                        w / 10.0, best.species, best.weight / 10.0));
            } else {
                s.entries.put(player.getUuid(), new Entry(player.getName().getString(), held.getName().getString(), w));
                s.markDirty();
                say(npc, player, String.format("Let's see... %.1f lb! That's on the board.", w / 10.0));
            }
        } else {
            say(npc, player, "The contest runs two days. Bring me your heaviest catch (hold it up) and I'll weigh it.",
                    "Snapper, swordfish, marlin if you've the skill for it. Biggest single fish wins.");
        }
        List<Entry> board = s.ranked();
        long left = (contestId(server) + 1) * LENGTH - server.getOverworld().getTimeOfDay();
        StringBuilder sb = new StringBuilder("Board (" + (left / 1000) + " hours left):");
        if (board.isEmpty()) sb.append(" nothing yet.");
        for (int i = 0; i < Math.min(5, board.size()); i++) {
            Entry e = board.get(i);
            sb.append(String.format(" %d. %s, %s %.1f lb", i + 1, e.name, e.species, e.weight / 10.0)).append(i < 4 ? ";" : "");
        }
        say(npc, player, sb.toString());
    }

    private static void say(StoryNpcEntity npc, ServerPlayerEntity p, String... lines) {
        com.cursedtides.quest.StoryDialogue.say(npc, p, lines);
    }

    record Entry(String name, String species, int weight) {}

    static final class State extends PersistentState {
        long contest = -1;
        final Map<UUID, Entry> entries = new HashMap<>();
        final Map<UUID, List<ItemStack>> prizes = new HashMap<>();
        private static final Type<State> TYPE = new Type<>(State::new, State::fromNbt, null);

        static State get(MinecraftServer server) {
            return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, "cursedtides_fishing_contest");
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
                    ServerPlayerEntity p = server.getPlayerManager().getPlayer(id);
                    if (p != null) {
                        for (ItemStack s : prize) if (!p.getInventory().insertStack(s)) p.dropItem(s, false);
                        ProfessionData.addXp(p, Profession.FISHING, 60 - i * 20);
                    } else {
                        prizes.computeIfAbsent(id, k -> new ArrayList<>()).addAll(prize);
                    }
                    news.append(String.format("%d. %s (%s, %.1f lb) ", i + 1, e.name, e.species, e.weight / 10.0));
                }
                server.getPlayerManager().broadcast(Text.literal(news.toString().trim()).formatted(Formatting.AQUA), false);
            }
            entries.clear();
            contest = now;
            markDirty();
        }

        @Override
        public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
            nbt.putLong("Contest", contest);
            NbtList list = new NbtList();
            entries.forEach((id, e) -> {
                NbtCompound c = new NbtCompound();
                c.putUuid("Id", id);
                c.putString("Name", e.name);
                c.putString("Species", e.species);
                c.putInt("Weight", e.weight);
                list.add(c);
            });
            nbt.put("Entries", list);
            NbtList pz = new NbtList();
            prizes.forEach((id, stacks) -> {
                for (ItemStack s : stacks) {
                    NbtCompound c = new NbtCompound();
                    c.putUuid("Id", id);
                    c.put("Item", s.encode(lookup));
                    pz.add(c);
                }
            });
            nbt.put("Prizes", pz);
            return nbt;
        }

        private static State fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
            State s = new State();
            s.contest = nbt.contains("Contest") ? nbt.getLong("Contest") : -1;
            NbtList list = nbt.getList("Entries", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < list.size(); i++) {
                NbtCompound c = list.getCompound(i);
                s.entries.put(c.getUuid("Id"), new Entry(c.getString("Name"), c.getString("Species"), c.getInt("Weight")));
            }
            NbtList pz = nbt.getList("Prizes", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < pz.size(); i++) {
                NbtCompound c = pz.getCompound(i);
                ItemStack.fromNbt(lookup, c.get("Item")).ifPresent(st ->
                        s.prizes.computeIfAbsent(c.getUuid("Id"), k -> new ArrayList<>()).add(st));
            }
            return s;
        }
    }
}
