package com.cursedtides.ui;

import com.cursedtides.CursedTides;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server to client: data for the journal and chart screens. */
public final class UiPayloads {
    private UiPayloads() {}

    /** Pre-formatted journal lines (legacy § colour codes). */
    public record Journal(List<String> lines) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<Journal> ID = new CustomPacketPayload.Type<>(CursedTides.id("journal"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Journal> CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), Journal::lines, Journal::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    /**
     * What the chart may show: which islands and dungeons you've found, and the X of any
     * treasure maps you carry (as x,z pairs).
     */
    public record Chart(boolean archipelago, int islands, long dungeons, List<Integer> marks) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<Chart> ID = new CustomPacketPayload.Type<>(CursedTides.id("chart"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Chart> CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, Chart::archipelago,
                ByteBufCodecs.VAR_INT, Chart::islands,
                ByteBufCodecs.VAR_LONG, Chart::dungeons,
                ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), Chart::marks,
                Chart::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }
}
