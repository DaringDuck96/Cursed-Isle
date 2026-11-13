package com.cursedtides.mixin;

import com.cursedtides.client.AreaMusic;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.Music;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Lets the archipelago choose its own music by place and by fight (see AreaMusic). */
@Mixin(Minecraft.class)
public abstract class MinecraftClientMusicMixin {
    @Inject(method = "getSituationalMusic", at = @At("HEAD"), cancellable = true)
    private void cursedtides$areaMusic(CallbackInfoReturnable<Music> cir) {
        Music m = AreaMusic.choose((Minecraft) (Object) this);
        if (m != null) cir.setReturnValue(m);
    }
}
