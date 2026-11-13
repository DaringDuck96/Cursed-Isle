package com.cursedtides.mixin;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hearts give way to a health bar with numbers (drawn by CombatHud). */
@Mixin(Gui.class)
public abstract class InGameHudMixin {
    @Inject(method = "renderHealthBar", at = @At("HEAD"), cancellable = true)
    private void cursedtides$noHearts(GuiGraphics context, Player player, int x, int y, int lines, int regeneratingHeartIndex,
                                      float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci) {
        if (com.cursedtides.client.CombatHud.enabled()) ci.cancel();
    }
}
