package com.cursedtides.dialogue;

import com.cursedtides.CursedTides;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** The conversation window's traffic. */
public final class DialoguePayloads {
    private DialoguePayloads() {}

    /** Server to client: show a line and its replies. Empty compound = close the window. */
    public record Show(CompoundTag data) implements CustomPacketPayload {
        public static final Type<Show> ID = new Type<>(CursedTides.id("dialogue_show"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Show> CODEC = StreamCodec.composite(ByteBufCodecs.COMPOUND_TAG, Show::data, Show::new);
        @Override public Type<? extends CustomPacketPayload> type() { return ID; }
    }

    /** Client to server: the player picked reply number {@code index} (-1 = walked away). */
    public record Choose(int index) implements CustomPacketPayload {
        public static final Type<Choose> ID = new Type<>(CursedTides.id("dialogue_choose"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Choose> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, Choose::index, Choose::new);
        @Override public Type<? extends CustomPacketPayload> type() { return ID; }
    }
}
