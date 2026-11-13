package com.cursedtides.character;

import com.cursedtides.CursedTides;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public final class CharacterPayloads {
    private CharacterPayloads() {}

    /** Server to client: open the character screen. firstTime = full creation; otherwise rename only. */
    public record Open(boolean firstTime, String currentName, int background) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<Open> ID = new CustomPacketPayload.Type<>(CursedTides.id("character_open"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Open> CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, Open::firstTime,
                ByteBufCodecs.STRING_UTF8, Open::currentName,
                ByteBufCodecs.VAR_INT, Open::background,
                Open::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    /** Client to server: the player's choices. color is a dye colour index (0-15). */
    public record Create(int background, int color, String name) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<Create> ID = new CustomPacketPayload.Type<>(CursedTides.id("character_create"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Create> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Create::background,
                ByteBufCodecs.VAR_INT, Create::color,
                ByteBufCodecs.STRING_UTF8, Create::name,
                Create::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }
}
