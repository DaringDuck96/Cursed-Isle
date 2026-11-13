package com.cursedtides.ship;

import com.cursedtides.CursedTides;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client to server: a ship order from the keyboard. */
public record ShipControlPayload(int action) implements CustomPacketPayload {
    public static final int SAILS_UP = 0;
    public static final int SAILS_DOWN = 1;
    public static final int FIRE_PORT = 2;
    public static final int FIRE_STARBOARD = 3;
    public static final int OPEN_HOLD = 4;
    public static final int CYCLE_AMMO = 5;
    /** Not ship orders: requests for the journal and chart screens. */
    public static final int OPEN_JOURNAL = 6;
    public static final int OPEN_MAP = 7;
    /** On foot: dodge rolls. */
    public static final int DODGE_LEFT = 8;
    public static final int DODGE_RIGHT = 9;
    public static final int DODGE_BACK = 10;
    public static final int OPEN_SKILLS = 11;
    /** Go below deck, or come back up. */
    public static final int BELOW_DECK = 12;
    public static final int FIRE_SWIVEL = 13;
    public static final int ANCHOR = 14;
    public static final int ULTIMATE = 15;

    public static final CustomPacketPayload.Type<ShipControlPayload> ID = new CustomPacketPayload.Type<>(CursedTides.id("ship_control"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShipControlPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, ShipControlPayload::action, ShipControlPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
