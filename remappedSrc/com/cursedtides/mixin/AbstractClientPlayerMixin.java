package com.cursedtides.mixin;

import com.cursedtides.client.AppearanceClient;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Pirates who've sat at the looking glass wear the skin they made there. */
@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {
    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true)
    private void cursedtides$pirateLook(CallbackInfoReturnable<PlayerSkin> cir) {
        AbstractClientPlayer self = (AbstractClientPlayer) (Object) this;
        ResourceLocation look = AppearanceClient.texture(self.getUUID());
        if (look == null) return;
        PlayerSkin base = cir.getReturnValue();
        cir.setReturnValue(new PlayerSkin(look, null, base.capeTexture(), base.elytraTexture(),
                AppearanceClient.slim(self.getUUID()) ? PlayerSkin.Model.SLIM : PlayerSkin.Model.WIDE, base.secure()));
    }
}
