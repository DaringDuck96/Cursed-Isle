package com.cursedtides.client;

import com.cursedtides.entity.ShipEntity;
import com.cursedtides.sound.ModSounds;
import com.cursedtides.world.Archipelago;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;

/**
 * Music by place, as the old pirate games had it: a march in the navy towns, a hornpipe or a reel in the free
 * ports, drums under the jungle canopy, a bell over the cursed isles, a fiddle under full sail, something low and
 * strange in the caverns and the deep, and battle music the moment steel comes out.
 */
public final class AreaMusic {
    private AreaMusic() {}

    private static final int GAP_MIN = 20 * 20, GAP_MAX = 20 * 70;

    private static Music calm(Holder<SoundEvent> s) {
        return new Music(s, GAP_MIN, GAP_MAX, false);
    }

    /** What should play now, or null to leave it to the game. */
    public static Music choose(Minecraft mc) {
        if (mc.player == null || mc.level == null || !CombatHud.enabled()) return null;
        if (System.currentTimeMillis() - CombatHud.lastFight < 8000) return new Music(ModSounds.STEEL_AND_POWDER, 0, 0, true);
        double x = mc.player.getX(), z = mc.player.getZ();
        if (mc.player.getY() < 30) return calm(ModSounds.LANTERNS_DROWN); // the caverns and dungeons
        Archipelago.Island near = null;
        double best = Double.MAX_VALUE;
        for (Archipelago.Island is : Archipelago.ISLANDS) {
            double d = is.edgeDistance(x, z);
            if (d < best) { best = d; near = is; }
        }
        boolean aboard = mc.player.getVehicle() instanceof ShipEntity;
        if (near == null || best > 60 || (aboard && best > 10)) {
            return calm(best > 2500 ? ModSounds.NINTH_WAVE : ModSounds.FULL_SAIL);
        }
        double fromTown = Math.hypot(x - near.cx(), z - near.cz());
        if (near == Archipelago.KINGSPORT || near == Archipelago.IRONWATCH_ISLE || near == Archipelago.CROWNWATCH) {
            if (fromTown < 110) return calm(ModSounds.ADMIRALTY_MARCH);
        }
        if (near.style() == Archipelago.Style.TOWN || near == Archipelago.RAVENS_LANDING || near == Archipelago.BLACKTHORN_ISLE) {
            if (fromTown < 110) return calm(near == Archipelago.SALTMARROW || near.style() == Archipelago.Style.TOWN
                    ? ModSounds.SALTMARROW_HORNPIPE : ModSounds.WRECKERS_REEL);
        }
        return switch (near.style()) {
            case CURSED -> calm(ModSounds.HOLLOW_BONES);
            case JUNGLE, BAYOU, ANCIENT -> calm(ModSounds.UNDER_THE_CANOPY);
            case VOLCANIC, CLIFFS, CRAGS -> calm(ModSounds.LANTERNS_DROWN);
            case REEF, ATOLL, KEYS -> calm(ModSounds.NINTH_WAVE);
            default -> calm(ModSounds.UNDER_THE_CANOPY);
        };
    }
}
