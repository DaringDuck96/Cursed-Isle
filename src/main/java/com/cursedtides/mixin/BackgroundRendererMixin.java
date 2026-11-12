package com.cursedtides.mixin;

import com.cursedtides.explore.SeaHazards;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fog banks: inside one, the horizon closes in to a few dozen blocks. */
@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin {
    @Inject(method = "applyFog", at = @At("TAIL"), require = 0)
    private static void cursedtides$fogBanks(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog,
                                            float tickDelta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || camera.getSubmersionType() != net.minecraft.block.enums.CameraSubmersionType.NONE) return;
        float density = SeaHazards.fogDensity(camera.getPos().x, camera.getPos().z);
        if (density <= 0) return;
        float end = Math.max(18f, viewDistance * (1 - density * 0.88f));
        RenderSystem.setShaderFogStart(fogType == BackgroundRenderer.FogType.FOG_SKY ? 0 : end * 0.15f);
        RenderSystem.setShaderFogEnd(end);
    }
}
