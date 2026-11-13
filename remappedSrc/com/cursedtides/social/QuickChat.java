package com.cursedtides.social;

import com.cursedtides.CursedTides;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Quick chat, the old pirate game's way of talking: pick a phrase from a menu (Y) and your pirate says it aloud, in a
 * speech bubble over your head and in the chat of everyone nearby. Quick to use mid-fight, and it can't be rude.
 */
public final class QuickChat {
    private QuickChat() {}

    public record Category(String name, String[] phrases) {}

    public static final List<Category> CATEGORIES = List.of(
            new Category("Greetings", new String[]{"Ahoy!", "Ahoy, matey!", "Fair winds to ye!", "Good to see ye again!",
                    "Welcome aboard!", "Farewell, and mind the reefs.", "Thank ye kindly!", "Ye're welcome!"}),
            new Category("Battle", new String[]{"To arms!", "Charge!", "Behind ye!", "Help! I'm in trouble!", "Fall back!",
                    "Watch my back!", "Take that, ye bilge rat!", "Huzzah! Victory!", "Heal me, quick!", "Get the big one!"}),
            new Category("Sailing", new String[]{"All hands on deck!", "Hoist the sails!", "Fire the cannons!", "Hard to port!",
                    "Hard to starboard!", "Enemy ship off the bow!", "We're taking on water!", "Board her!", "Ramming speed!", "Land ho!"}),
            new Category("Crew", new String[]{"Want to join my crew?", "Need another hand?", "Follow me!", "Wait for me!",
                    "Where to, captain?", "Meet me at the docks.", "Let's sail together!", "I'm ready to set sail."}),
            new Category("Treasure", new String[]{"Look at this loot!", "Want to trade?", "What're ye selling?", "Shiny!",
                    "Where's the treasure?", "X marks the spot!", "That's a fine blade.", "Gold and glory!"}),
            new Category("Moods", new String[]{"Yo ho ho!", "Arrr!", "Shiver me timbers!", "Blow me down!", "Aye!", "Nay!",
                    "Ha! Ha! Ha!", "Blast it all!", "Well met, pirate!", "Dead men tell no tales..."})
    );

    /** Client to server: say phrase [phrase] from category [category]. */
    public record Say(int category, int phrase) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<Say> ID = new CustomPacketPayload.Type<>(CursedTides.id("quick_chat"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Say> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Say::category, ByteBufCodecs.VAR_INT, Say::phrase, Say::new);

        @Override
        public Type<? extends CustomPacketPayload> type() { return ID; }
    }

    private static final Map<UUID, Long> LAST = new HashMap<>();
    private record Bubble(Display.TextDisplay display, long until) {}
    private static final List<Bubble> BUBBLES = new ArrayList<>();

    public static void register() {
        PayloadTypeRegistry.playC2S().register(Say.ID, Say.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Say.ID, (payload, ctx) -> ctx.server().execute(() -> say(ctx.player(), payload)));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (BUBBLES.isEmpty()) return;
            long now = server.overworld().getGameTime();
            BUBBLES.removeIf(b -> {
                if (now < b.until() && b.display().isAlive() && b.display().isPassenger()) return false;
                b.display().discard();
                return true;
            });
        });
    }

    private static void say(ServerPlayer p, Say s) {
        if (s.category() < 0 || s.category() >= CATEGORIES.size()) return;
        String[] ph = CATEGORIES.get(s.category()).phrases();
        if (s.phrase() < 0 || s.phrase() >= ph.length) return;
        ServerLevel w = p.serverLevel();
        long now = w.getGameTime();
        if (now - LAST.getOrDefault(p.getUUID(), -100L) < 30) return;
        LAST.put(p.getUUID(), now);
        String line = ph[s.phrase()];
        Component msg = Component.literal(p.getName().getString() + ": ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(line).withStyle(ChatFormatting.WHITE));
        for (ServerPlayer q : w.getPlayers(q -> q.distanceToSqr(p) < 48 * 48)) q.displayClientMessage(msg, false);
        bubble(w, p, line);
    }

    /** A speech bubble that rides above the speaker's head for four seconds. */
    private static void bubble(ServerLevel w, ServerPlayer p, String line) {
        BUBBLES.removeIf(b -> {
            if (b.display().getVehicle() == p) { b.display().discard(); return true; }
            return false;
        });
        Display.TextDisplay d = EntityType.TEXT_DISPLAY.create(w);
        if (d == null) return;
        CompoundTag nbt = new CompoundTag();
        nbt.putString("text", Component.Serializer.toJson(Component.literal(line).withStyle(ChatFormatting.BLACK), w.registryAccess()));
        nbt.putString("billboard", "center");
        nbt.putInt("background", 0xE8F4ECD8);
        nbt.putInt("line_width", 120);
        CompoundTag t = new CompoundTag();
        t.put("translation", floats(0, 0.55f, 0));
        t.put("left_rotation", floats(0, 0, 0, 1));
        t.put("right_rotation", floats(0, 0, 0, 1));
        t.put("scale", floats(0.8f, 0.8f, 0.8f));
        nbt.put("transformation", t);
        d.load(mergeId(nbt));
        d.moveTo(p.getX(), p.getY() + p.getBbHeight(), p.getZ(), 0, 0);
        w.addFreshEntity(d);
        d.startRiding(p, true);
        BUBBLES.add(new Bubble(d, w.getGameTime() + 80));
    }

    private static net.minecraft.nbt.ListTag floats(float... v) {
        net.minecraft.nbt.ListTag l = new net.minecraft.nbt.ListTag();
        for (float f : v) l.add(net.minecraft.nbt.FloatTag.valueOf(f));
        return l;
    }

    /** readNbt wants the entity's base fields too; give it a fresh position block so it doesn't complain. */
    private static CompoundTag mergeId(CompoundTag nbt) {
        net.minecraft.nbt.ListTag pos = new net.minecraft.nbt.ListTag();
        for (int i = 0; i < 3; i++) pos.add(net.minecraft.nbt.DoubleTag.valueOf(0));
        nbt.put("Pos", pos);
        return nbt;
    }
}
