package com.cursedtides.mixin;

import com.cursedtides.client.AppearanceClient;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Pirates who've sat at the looking glass wear the skin they made there. */
@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {
    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void cursedtides$pirateLook(CallbackInfoReturnable<PlayerSkin> cir) {
        AbstractClientPlayer self = (AbstractClientPlayer) (Object) this;
        Identifier look = AppearanceClient.texture(self.getUUID());
        if (look == null) return;
        PlayerSkin base = cir.getReturnValue();
        cir.setReturnValue(new PlayerSkin(new net.minecraft.core.ClientAsset.ResourceTexture(look, look), base.cape(), base.elytra(),
                AppearanceClient.slim(self.getUUID()) ? net.minecraft.world.entity.player.PlayerModelType.SLIM
                        : net.minecraft.world.entity.player.PlayerModelType.WIDE, base.secure()));
    }
}
