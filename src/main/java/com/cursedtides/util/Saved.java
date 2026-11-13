package com.cursedtides.util;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.function.BiFunction;
import java.util.function.Supplier;

/** World-saved state kept as a compound tag, the way the mod has always stored it. */
public final class Saved {
    private Saved() {}

    private static MinecraftServer server;

    public static void setServer(MinecraftServer s) { server = s; }

    private static HolderLookup.Provider lookup() {
        return server != null ? server.registryAccess() : RegistryAccess.EMPTY;
    }

    public static <T extends SavedData> SavedDataType<T> type(String name, Supplier<T> ctor,
                                                             BiFunction<CompoundTag, HolderLookup.Provider, T> load,
                                                             BiFunction<T, HolderLookup.Provider, CompoundTag> save) {
        Codec<T> codec = CompoundTag.CODEC.xmap(tag -> load.apply(tag, lookup()), t -> save.apply(t, lookup()));
        return new SavedDataType<>(com.cursedtides.CursedTides.id(name), ctor, codec, null);
    }
}
