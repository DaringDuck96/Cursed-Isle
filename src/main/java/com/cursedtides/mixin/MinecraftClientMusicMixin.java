package com.cursedtides.mixin;

import com.cursedtides.client.AreaMusic;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.MusicSound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Lets the archipelago choose its own music by place and by fight (see AreaMusic). */
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMusicMixin {
    @Inject(method = "getMusicType", at = @At("HEAD"), cancellable = true)
    private void cursedtides$areaMusic(CallbackInfoReturnable<MusicSound> cir) {
        MusicSound m = AreaMusic.choose((MinecraftClient) (Object) this);
        if (m != null) cir.setReturnValue(m);
    }
}
