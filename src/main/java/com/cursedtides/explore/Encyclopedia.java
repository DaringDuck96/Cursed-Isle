package com.cursedtides.explore;

import com.cursedtides.CursedTides;
import com.cursedtides.world.Archipelago;
import com.cursedtides.world.ModWorld;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

import static net.minecraft.server.command.CommandManager.literal;

/** The Encyclopedia of the Reach: every curio you've studied, plus the islands you've charted. */
public final class Encyclopedia {
    private Encyclopedia() {}

    public record Open(NbtCompound data) implements CustomPayload {
        public static final Id<Open> ID = new Id<>(CursedTides.id("encyclopedia"));
        public static final PacketCodec<RegistryByteBuf, Open> CODEC = PacketCodec.tuple(PacketCodecs.NBT_COMPOUND, Open::data, Open::new);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(Open.ID, Open.CODEC);
        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> dispatcher.register(literal("encyclopedia").executes(ctx -> {
            open(ctx.getSource().getPlayerOrThrow());
            return 1;
        })));
    }

    public static void open(ServerPlayerEntity p) {
        NbtCompound data = new NbtCompound();
        NbtList pages = new NbtList();
        List<String> found = p.getAttachedOrElse(Collectibles.FOUND, List.of());
        for (Collectibles.Kind kind : Collectibles.Kind.values()) {
            for (Collectibles.Entry e : Collectibles.ALL) {
                if (e.kind() != kind) continue;
                NbtCompound page = new NbtCompound();
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
            NbtCompound page = new NbtCompound();
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
