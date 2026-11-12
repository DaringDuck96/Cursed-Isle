package com.cursedtides.mixin;

import com.cursedtides.client.AppearanceClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Pirates who've sat at the looking glass wear the skin they made there. */
@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerMixin {
    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true)
    private void cursedtides$pirateLook(CallbackInfoReturnable<SkinTextures> cir) {
        AbstractClientPlayerEntity self = (AbstractClientPlayerEntity) (Object) this;
        Identifier look = AppearanceClient.texture(self.getUuid());
        if (look == null) return;
        SkinTextures base = cir.getReturnValue();
        cir.setReturnValue(new SkinTextures(look, null, base.capeTexture(), base.elytraTexture(),
                AppearanceClient.slim(self.getUuid()) ? SkinTextures.Model.SLIM : SkinTextures.Model.WIDE, base.secure()));
    }
}
