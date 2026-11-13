package com.cursedtides.client;

import com.cursedtides.CursedTides;
import com.cursedtides.appearance.Appearance;
import com.cursedtides.appearance.Look;
import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Builds a skin for each look by stacking painted layers (tone, outfit, markings, hair, beard, accessories)
 * and keeps one texture per distinct look, shared by every pirate who wears it.
 */
public final class AppearanceClient {
    private AppearanceClient() {}

    private static final Map<UUID, Integer> CODES = new HashMap<>();
    private static final Map<Integer, Identifier> BUILT = new HashMap<>();
    private static final Map<String, NativeImage> LAYERS = new HashMap<>();
    /** While the looking glass is open, the local player previews this look instead. */
    @Nullable static Integer preview;

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(Appearance.Sync.ID, (payload, context) -> context.client().execute(() -> {
            for (Appearance.Entry e : payload.entries()) CODES.put(e.id(), e.code());
        }));
        ClientPlayNetworking.registerGlobalReceiver(Appearance.Open.ID, (payload, context) ->
                context.client().execute(() -> context.client().gui.setScreen(new AppearanceScreen(payload.code()))));
        ClientPlayNetworking.registerGlobalReceiver(com.cursedtides.explore.Encyclopedia.Open.ID, (payload, context) ->
                context.client().execute(() -> context.client().gui.setScreen(new EncyclopediaScreen(payload.data()))));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> CODES.clear());
    }

    public static int code(UUID id) {
        Minecraft mc = Minecraft.getInstance();
        if (preview != null && mc.player != null && mc.player.getUUID().equals(id)) return preview;
        return CODES.getOrDefault(id, 0);
    }

    /** The composed skin for a player, or null to keep their ordinary one. */
    @Nullable
    public static Identifier texture(UUID id) {
        int code = code(id);
        if (!Look.enabled(code)) return null;
        return BUILT.computeIfAbsent(code, AppearanceClient::build);
    }

    public static boolean slim(UUID id) {
        return Look.slim(code(id));
    }

    private static Identifier build(int code) {
        NativeImage out = new NativeImage(64, 64, true);
        layer(out, "tone_" + Look.tone(code), -1);
        layer(out, "outfit_" + Look.outfit(code), -1);
        if (Look.marking(code) > 0) layer(out, "marking_" + Look.marking(code), -1);
        int tint = Look.HAIR_TINT[Look.hairColour(code) % Look.HAIR_TINT.length];
        if (Look.hair(code) > 0) layer(out, "hair_" + Look.hair(code), tint);
        if (Look.facial(code) > 0) layer(out, "facial_" + Look.facial(code), tint);
        int acc = Look.accessories(code);
        if ((acc & Look.ACC_BANDANA_RED) != 0) layer(out, "acc_bandana_red", -1);
        else if ((acc & Look.ACC_BANDANA_BLACK) != 0) layer(out, "acc_bandana_black", -1);
        if ((acc & Look.ACC_EYEPATCH) != 0) layer(out, "acc_eyepatch", -1);
        if ((acc & Look.ACC_EARRING) != 0) layer(out, "acc_earring", -1);
        Identifier id = CursedTides.id("look/built_" + Integer.toHexString(code));
        Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(id::toString, out));
        return id;
    }

    /** Alpha-blend one layer over the canvas. Colours in NativeImage are ABGR; tint is 0xRRGGBB or -1. */
    private static void layer(NativeImage out, String name, int tint) {
        NativeImage src = load(name);
        if (src == null) return;
        int tr = (tint >> 16) & 255, tg = (tint >> 8) & 255, tb = tint & 255;
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 64; y++) {
                int s = net.minecraft.util.ARGB.toABGR(src.getPixel(x, y));
                int sa = (s >>> 24) & 255;
                if (sa == 0) continue;
                int sr = s & 255, sg = (s >> 8) & 255, sb = (s >> 16) & 255;
                if (tint != -1) {
                    sr = sr * tr / 255;
                    sg = sg * tg / 255;
                    sb = sb * tb / 255;
                }
                int d = net.minecraft.util.ARGB.toABGR(out.getPixel(x, y));
                int da = (d >>> 24) & 255;
                int dr = d & 255, dg = (d >> 8) & 255, db = (d >> 16) & 255;
                int oa = sa + da * (255 - sa) / 255;
                int or = oa == 0 ? 0 : (sr * sa + dr * da * (255 - sa) / 255) / oa;
                int og = oa == 0 ? 0 : (sg * sa + dg * da * (255 - sa) / 255) / oa;
                int ob = oa == 0 ? 0 : (sb * sa + db * da * (255 - sa) / 255) / oa;
                out.setPixelABGR(x, y, (oa << 24) | (ob << 16) | (og << 8) | or);
            }
        }
    }

    @Nullable
    private static NativeImage load(String name) {
        return LAYERS.computeIfAbsent(name, n -> {
            Identifier id = CursedTides.id("textures/look/" + n + ".png");
            Optional<Resource> res = Minecraft.getInstance().getResourceManager().getResource(id);
            if (res.isEmpty()) return null;
            try (InputStream in = res.get().open()) {
                return NativeImage.read(in);
            } catch (Exception e) {
                CursedTides.LOGGER.warn("Couldn't read look layer {}", id, e);
                return null;
            }
        });
    }
}
