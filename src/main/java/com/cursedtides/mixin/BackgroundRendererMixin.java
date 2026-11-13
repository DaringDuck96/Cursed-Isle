package com.cursedtides.mixin;

import com.cursedtides.explore.SeaHazards;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Fog banks: inside one, the horizon closes in to a few dozen blocks. */
@Mixin(FogRenderer.class)
public abstract class BackgroundRendererMixin {
    @Inject(method = "setupFog", at = @At("RETURN"), require = 0)
    private void cursedtides$fogBanks(Camera camera, int renderDistanceInChunks, DeltaTracker deltaTracker, float darkenWorldAmount,
                                      ClientLevel level, CallbackInfoReturnable<FogData> cir) {
        if (level == null || camera.getFluidInCamera() != net.minecraft.world.level.material.FogType.NONE) return;
        FogData fog = cir.getReturnValue();
        if (fog == null) return;
        float density = SeaHazards.fogDensity(camera.position().x, camera.position().z);
        if (density <= 0) return;
        float view = renderDistanceInChunks * 16f;
        float end = Math.max(18f, view * (1 - density * 0.88f));
        fog.environmentalStart = Math.min(fog.environmentalStart, end * 0.15f);
        fog.environmentalEnd = Math.min(fog.environmentalEnd, end);
        fog.skyEnd = Math.min(fog.skyEnd, end);
        fog.cloudEnd = Math.min(fog.cloudEnd, end);
    }
}
