package com.cursedtides.mixin;

import com.cursedtides.combat.Levels;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The archipelago's undead are cursed, not fragile: they walk in daylight. */
@Mixin(Mob.class)
public abstract class MobEntityMixin {
    @Inject(method = "isAffectedByDaylight", at = @At("HEAD"), cancellable = true)
    private void cursedtides$noSunburn(CallbackInfoReturnable<Boolean> cir) {
        if (Levels.levelOf((Mob) (Object) this) > 0) cir.setReturnValue(false);
    }
}
