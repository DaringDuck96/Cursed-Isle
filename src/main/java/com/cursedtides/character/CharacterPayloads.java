package com.cursedtides.character;

import com.cursedtides.CursedTides;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public final class CharacterPayloads {
    private CharacterPayloads() {}

    /** Server to client: open the character screen. firstTime = full creation; otherwise rename only. */
    public record Open(boolean firstTime, String currentName, int background) implements CustomPayload {
        public static final CustomPayload.Id<Open> ID = new CustomPayload.Id<>(CursedTides.id("character_open"));
        public static final PacketCodec<RegistryByteBuf, Open> CODEC = PacketCodec.tuple(
                PacketCodecs.BOOL, Open::firstTime,
                PacketCodecs.STRING, Open::currentName,
                PacketCodecs.VAR_INT, Open::background,
                Open::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    /** Client to server: the player's choices. color is a dye colour index (0-15). */
    public record Create(int background, int color, String name) implements CustomPayload {
        public static final CustomPayload.Id<Create> ID = new CustomPayload.Id<>(CursedTides.id("character_create"));
        public static final PacketCodec<RegistryByteBuf, Create> CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_INT, Create::background,
                PacketCodecs.VAR_INT, Create::color,
                PacketCodecs.STRING, Create::name,
                Create::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}
