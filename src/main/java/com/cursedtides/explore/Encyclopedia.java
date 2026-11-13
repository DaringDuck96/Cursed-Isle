package com.cursedtides.explore;

import com.cursedtides.CursedTides;
import com.cursedtides.world.Archipelago;
import com.cursedtides.world.ModWorld;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import java.util.List;

import static net.minecraft.commands.Commands.literal;

/** The Encyclopedia of the Reach: every curio you've studied, plus the islands you've charted. */
public final class Encyclopedia {
    private Encyclopedia() {}

    public record Open(CompoundTag data) implements CustomPacketPayload {
        public static final Type<Open> ID = new Type<>(CursedTides.id("encyclopedia"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Open> CODEC = StreamCodec.composite(ByteBufCodecs.COMPOUND_TAG, Open::data, Open::new);
        @Override public Type<? extends CustomPacketPayload> type() { return ID; }
    }

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(Open.ID, Open.CODEC);
        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> dispatcher.register(literal("encyclopedia").executes(ctx -> {
            open(ctx.getSource().getPlayerOrException());
            return 1;
        })));
    }

    public static void open(ServerPlayer p) {
        CompoundTag data = new CompoundTag();
        ListTag pages = new ListTag();
        List<String> found = p.getAttachedOrElse(Collectibles.FOUND, List.of());
        for (Collectibles.Kind kind : Collectibles.Kind.values()) {
            for (Collectibles.Entry e : Collectibles.ALL) {
                if (e.kind() != kind) continue;
                CompoundTag page = new CompoundTag();
                boolean have = found.contains(e.id());
                page.putString("Section", kind.title + "s");
                page.putString("Title", have ? e.title() : "???");
                page.putString("Text", have ? e.text() : "Not yet found. " + hint(kind));
                page.putBoolean("Found", have);
                pages.add(page);
            }
        }
        int mask = p.getAttachedOrElse(ModWorld.DISCOVERED, 0);
        for (int i = 0; i < Archipelago.ISLANDS.size(); i++) {
            CompoundTag page = new CompoundTag();
            boolean have = (mask & (1 << i)) != 0;
            var isle = Archipelago.ISLANDS.get(i);
            page.putString("Section", "Islands");
            page.putString("Title", have ? isle.name() : "Uncharted");
            page.putString("Text", have ? "Charted. " + describe(isle.style()) + " Centre near " + isle.cx() + ", " + isle.cz() + "."
                    : "Somewhere out there. Sail until you find it.");
            page.putBoolean("Found", have);
            pages.add(page);
        }
        data.put("Pages", pages);
        data.putInt("Found", found.size());
        data.putInt("Total", Collectibles.ALL.size());
        ServerPlayNetworking.send(p, new Open(data));
    }

    private static String hint(Collectibles.Kind k) {
        return switch (k) {
            case BOTTLE -> "Bottles wash up on beaches, and sometimes on a fishing line.";
            case LOG -> "Logs turn up in sunken ships' holds and dungeon vaults.";
            case PAINTING -> "Paintings hang in vaults and drowned halls.";
            case INSTRUMENT -> "Instruments are carried by the great and terrible.";
        };
    }

    private static String describe(Archipelago.Style s) {
        return switch (s) {
            case TOWN -> "A port town.";
            case SANDY -> "Sand and palms.";
            case REEF -> "Coral shallows.";
            case VOLCANIC -> "Ash and fire.";
            case CURSED -> "Cursed ground.";
            case FORTRESS -> "Walls and guns.";
            case BAYOU -> "Swamp and black water.";
            case JUNGLE -> "Deep jungle.";
            case CLIFFS -> "Sheer cliffs.";
            case CRAGS -> "Jagged crags.";
            case DESERT -> "Dry red rock.";
            case KEYS -> "A scatter of keys.";
            case ATOLL -> "A ring atoll.";
            case ANCIENT -> "Ancient terraces.";
        };
    }
}
