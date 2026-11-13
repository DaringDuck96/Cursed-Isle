package com.cursedtides.explore;

import com.cursedtides.CursedTides;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Things worth keeping: messages in bottles, the logs of lost ships, paintings and instruments. Each one you read or
 * study goes into your Encyclopedia of the Reach for good.
 */
public final class Collectibles {
    private Collectibles() {}

    public enum Kind { BOTTLE("Message in a Bottle"), LOG("Ship's Log"), PAINTING("Painting"), INSTRUMENT("Instrument");
        public final String title;
        Kind(String title) { this.title = title; }
    }

    public record Entry(String id, Kind kind, String title, String text) {}

    public static final List<Entry> ALL = List.of(
            // Bottles
            new Entry("bottle_tide", Kind.BOTTLE, "\"To whoever finds this\"", "Marooned on a sandbar no bigger than a dinner table. I've eaten the last gull. If you're reading this, I was right about the current and wrong about everything else. — T."),
            new Entry("bottle_love", Kind.BOTTLE, "A love letter, smudged", "Ada, the ship's turning for home at last. Keep the lamp lit in the window. If I'm late, it's the wind's fault. If I'm very late, it's the rum's."),
            new Entry("bottle_map", Kind.BOTTLE, "A scrap of chart", "Three palms in a row on the eastern key. Pace twenty from the middle one, toward the sunrise. Dig until you hit the box, then keep digging; the box is a decoy."),
            new Entry("bottle_warning", Kind.BOTTLE, "A warning, in a shaking hand", "Do not answer the bell. Whatever you hear out on the water at night, do not answer the bell."),
            new Entry("bottle_recipe", Kind.BOTTLE, "Grandmother's grog", "One part rum, one part lime, two parts whatever's left in the bottom of the barrel. Stir with a cutlass. Serve to enemies first."),
            new Entry("bottle_will", Kind.BOTTLE, "A last will", "I leave my boots to the cabin boy, my debts to the captain, and my parrot to whoever can catch him."),
            new Entry("bottle_child", Kind.BOTTLE, "A child's drawing", "A ship with too many sails, a fish with teeth, and a sun with a face. Underneath, in careful letters: DADDY'S BOAT."),
            new Entry("bottle_mutiny", Kind.BOTTLE, "Plans for a mutiny", "Thursday, at the change of watch. Hobbs takes the powder room, Mags takes the wheel. Nobody touch the cook. We need the cook."),
            new Entry("bottle_apology", Kind.BOTTLE, "An apology", "I'm sorry I sank your ship. In my defence, it was a very easy ship to sink."),
            new Entry("bottle_lantern", Kind.BOTTLE, "The lamplighter's note", "Seven lamps on seven tides. When the last goes dark, the sea remembers what it swallowed."),
            new Entry("bottle_blank", Kind.BOTTLE, "An empty page", "Nothing is written. But it smells, faintly and unmistakably, of oranges."),
            new Entry("bottle_bet", Kind.BOTTLE, "A wager", "I bet Jory Bones a doubloon this bottle reaches Saltmarrow before he does. If you're Jory: pay up."),
            // Logs
            new Entry("log_merrow", Kind.LOG, "Log of the Merrow Lass", "Day 41. Becalmed. The men have started naming the clouds. Day 43. Still becalmed. Cook has named one of the men."),
            new Entry("log_tidewatch", Kind.LOG, "Tidewatch patrol log", "Pursued the pirate sloop into the fog off Blackveil. She vanished. Our soundings say the water there is two hundred fathoms deep, and yet we heard her keel scrape stone."),
            new Entry("log_whaler", Kind.LOG, "Whaler's log", "Saw something larger than the ship pass beneath us at dawn. Harpooner refused to throw. I did not insist."),
            new Entry("log_smuggler", Kind.LOG, "A smuggler's ledger", "Forty casks 'salt fish' to Raven's Landing. Salt fish does not slosh. The customs man did not ask."),
            new Entry("log_ghost", Kind.LOG, "Log, water-stained", "We drowned on the ninth. I am writing this on the tenth. The ink keeps running."),
            new Entry("log_explorer", Kind.LOG, "Explorer's log", "The terraces at the Last Horizon were built by hands. There are steps for feet smaller than ours, and doors for something much larger."),
            new Entry("log_storm", Kind.LOG, "Log of the Gull's Wager", "Stormbreak in sight. Wind backing west, glass falling like a stone. Ordered every hand to pray. The bosun asked to whom. Fair question."),
            new Entry("log_prize", Kind.LOG, "A prize captain's log", "Took the merchantman without a shot. Turns out her cargo was cats. Four hundred cats. We are no longer a pirate ship. We are a cat ship."),
            // Paintings
            new Entry("painting_harbour", Kind.PAINTING, "Saltmarrow at Dusk", "Oils on sailcloth. The harbour glows orange, and a tiny figure on the pier is waving at a ship that isn't in the picture."),
            new Entry("painting_admiral", Kind.PAINTING, "Portrait of an Admiral", "Stern, medalled, magnificent. Someone has very carefully painted a moustache on her in a slightly different blue."),
            new Entry("painting_storm", Kind.PAINTING, "The Wreck of the Coldwake", "A ship on her beam ends in a green sea. Look closely and the waves have faces."),
            new Entry("painting_city", Kind.PAINTING, "The City Before", "Towers of pale stone and bridges over clear water. On the back: 'As it was, before the sea came up to see.'"),
            new Entry("painting_still", Kind.PAINTING, "Still Life with Rum", "A bottle, a lime, a pistol. The bottle is empty. The pistol is not."),
            new Entry("painting_map", Kind.PAINTING, "The Archipelago, Imagined", "Every island drawn as an animal: a crab, a turtle, a sleeping dog. Driftwood is a very small, very confused crab."),
            // Instruments
            new Entry("instrument_fiddle", Kind.INSTRUMENT, "A battered fiddle", "Three strings left and a crack down the belly. It still plays the Wrecker's Reel faster than anyone can dance it."),
            new Entry("instrument_concertina", Kind.INSTRUMENT, "Squeezebox", "A sailor's concertina. Wheezes like an old dog, sings like a young one."),
            new Entry("instrument_drum", Kind.INSTRUMENT, "A boarding drum", "Painted with a skull. Beat it slow to make a crew brave, fast to make another crew nervous."),
            new Entry("instrument_whistle", Kind.INSTRUMENT, "Bosun's whistle", "Silver, worn smooth. Blow it on deck and every sailor within earshot looks guilty."),
            new Entry("instrument_bell", Kind.INSTRUMENT, "A drowned bell", "No bigger than your fist, green with age. When you shake it there's no sound at all, and then, a second later, there is."),
            new Entry("instrument_horn", Kind.INSTRUMENT, "A conch horn", "Blown at the Last Horizon to call the ferry. Nobody has seen the ferry in four hundred years. Blow it anyway.")
    );

    public static final AttachmentType<List<String>> FOUND = AttachmentRegistry.<List<String>>builder()
            .persistent(Codec.STRING.listOf()).copyOnDeath().initializer(List::of).buildAndRegister(CursedTides.id("collectibles"));

    public static final Item ITEM = Registry.register(BuiltInRegistries.ITEM, com.cursedtides.util.Reg.itemId("curio"), new CollectibleItem(com.cursedtides.util.Reg.props().stacksTo(1).rarity(Rarity.UNCOMMON)));

    @Nullable
    public static Entry byId(String id) {
        for (Entry e : ALL) if (e.id().equals(id)) return e;
        return null;
    }

    public static boolean has(ServerPlayer p, String id) {
        return p.getAttachedOrElse(FOUND, List.of()).contains(id);
    }

    public static int count(ServerPlayer p) {
        return p.getAttachedOrElse(FOUND, List.of()).size();
    }

    public static ItemStack stack(Entry e) {
        ItemStack s = new ItemStack(ITEM);
        CompoundTag n = new CompoundTag();
        n.putString("Curio", e.id());
        s.set(DataComponents.CUSTOM_DATA, CustomData.of(n));
        s.set(DataComponents.ITEM_NAME, Component.literal(e.kind().title + ": " + e.title()));
        s.set(DataComponents.CUSTOM_MODEL_DATA, new net.minecraft.world.item.component.CustomModelData(java.util.List.of((float) (e.kind().ordinal() + 1)), java.util.List.of(), java.util.List.of(), java.util.List.of()));
        return s;
    }

    /** Something the player hasn't found yet (of that kind, or any kind if null); a repeat if they've found them all. */
    public static ItemStack randomFor(ServerPlayer p, @Nullable Kind kind) {
        Set<String> found = new HashSet<>(p.getAttachedOrElse(FOUND, List.of()));
        List<Entry> pool = new ArrayList<>(), any = new ArrayList<>();
        for (Entry e : ALL) {
            if (kind != null && e.kind() != kind) continue;
            any.add(e);
            if (!found.contains(e.id())) pool.add(e);
        }
        List<Entry> from = pool.isEmpty() ? any : pool;
        return stack(from.get(p.getRandom().nextInt(from.size())));
    }

    /** Reading a curio records it. */
    static void study(ServerPlayer p, Entry e) {
        p.sendSystemMessage(Component.literal(e.kind().title + ": " + e.title()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        p.sendSystemMessage(Component.literal(e.text()).withStyle(ChatFormatting.ITALIC), false);
        if (has(p, e.id())) return;
        List<String> found = new ArrayList<>(p.getAttachedOrElse(FOUND, List.of()));
        found.add(e.id());
        p.setAttached(FOUND, found);
        p.sendSystemMessage(Component.literal("Added to your Encyclopedia (" + found.size() + "/" + ALL.size() + "). Open it with /encyclopedia.")
                .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        p.level().playSound(null, p.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1f, 1f);
        com.cursedtides.pirate.PirateData.addXp(p, com.cursedtides.pirate.Weapon.EXPLORATION, 15);
        com.cursedtides.activity.Challenges.progress(p, "bottle", 1);
        if (found.size() >= 10) com.cursedtides.activity.Achievements.grant(p, "collector");
        if (found.size() >= ALL.size()) com.cursedtides.activity.Achievements.grant(p, "encyclopedist");
    }
}
