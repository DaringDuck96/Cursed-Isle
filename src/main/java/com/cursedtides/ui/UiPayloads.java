package com.cursedtides.ui;

import com.cursedtides.CursedTides;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

import java.util.List;

/** Server to client: data for the journal and chart screens. */
public final class UiPayloads {
    private UiPayloads() {}

    /** Pre-formatted journal lines (legacy § colour codes). */
    public record Journal(List<String> lines) implements CustomPayload {
        public static final CustomPayload.Id<Journal> ID = new CustomPayload.Id<>(CursedTides.id("journal"));
        public static final PacketCodec<RegistryByteBuf, Journal> CODEC = PacketCodec.tuple(
                PacketCodecs.STRING.collect(PacketCodecs.toList()), Journal::lines, Journal::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    /**
     * What the chart may show: which islands and dungeons you've found, and the X of any
     * treasure maps you carry (as x,z pairs).
     */
    public record Chart(boolean archipelago, int islands, long dungeons, List<Integer> marks) implements CustomPayload {
        public static final CustomPayload.Id<Chart> ID = new CustomPayload.Id<>(CursedTides.id("chart"));
        public static final PacketCodec<RegistryByteBuf, Chart> CODEC = PacketCodec.tuple(
                PacketCodecs.BOOL, Chart::archipelago,
                PacketCodecs.VAR_INT, Chart::islands,
                PacketCodecs.VAR_LONG, Chart::dungeons,
                PacketCodecs.VAR_INT.collect(PacketCodecs.toList()), Chart::marks,
                Chart::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}
