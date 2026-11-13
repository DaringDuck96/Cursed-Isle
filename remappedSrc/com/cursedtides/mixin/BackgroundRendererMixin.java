package com.cursedtides.mixin;

import com.cursedtides.explore.SeaHazards;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fog banks: inside one, the horizon closes in to a few dozen blocks. */
@Mixin(FogRenderer.class)
public abstract class BackgroundRendererMixin {
    @Inject(method = "applyFog", at = @At("TAIL"), require = 0)
    private static void cursedtides$fogBanks(Camera camera, FogRenderer.FogMode fogType, float viewDistance, boolean thickFog,
                                            float tickDelta, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || camera.getFluidInCamera() != net.minecraft.world.level.material.FogType.NONE) return;
        float density = SeaHazards.fogDensity(camera.getPosition().x, camera.getPosition().z);
        if (density <= 0) return;
        float end = Math.max(18f, viewDistance * (1 - density * 0.88f));
        RenderSystem.setShaderFogStart(fogType == FogRenderer.FogMode.FOG_SKY ? 0 : end * 0.15f);
        RenderSystem.setShaderFogEnd(end);
    }
}
