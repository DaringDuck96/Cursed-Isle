package com.cursedtides.dialogue;

import com.cursedtides.CursedTides;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** The conversation window's traffic. */
public final class DialoguePayloads {
    private DialoguePayloads() {}

    /** Server to client: show a line and its replies. Empty compound = close the window. */
    public record Show(NbtCompound data) implements CustomPayload {
        public static final Id<Show> ID = new Id<>(CursedTides.id("dialogue_show"));
        public static final PacketCodec<RegistryByteBuf, Show> CODEC = PacketCodec.tuple(PacketCodecs.NBT_COMPOUND, Show::data, Show::new);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    /** Client to server: the player picked reply number {@code index} (-1 = walked away). */
    public record Choose(int index) implements CustomPayload {
        public static final Id<Choose> ID = new Id<>(CursedTides.id("dialogue_choose"));
        public static final PacketCodec<RegistryByteBuf, Choose> CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, Choose::index, Choose::new);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }
}
