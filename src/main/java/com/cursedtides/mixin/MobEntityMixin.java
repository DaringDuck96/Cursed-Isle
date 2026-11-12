package com.cursedtides.mixin;

import com.cursedtides.combat.Levels;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The archipelago's undead are cursed, not fragile: they walk in daylight. */
@Mixin(MobEntity.class)
public abstract class MobEntityMixin {
    @Inject(method = "isAffectedByDaylight", at = @At("HEAD"), cancellable = true)
    private void cursedtides$noSunburn(CallbackInfoReturnable<Boolean> cir) {
        if (Levels.levelOf((MobEntity) (Object) this) > 0) cir.setReturnValue(false);
    }
}
