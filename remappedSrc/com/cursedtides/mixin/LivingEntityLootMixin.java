package com.cursedtides.mixin;

import com.cursedtides.combat.Levels;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Foes with a level leave a loot container (see Loot) instead of their usual scatter of bones and rotten flesh. */
@Mixin(LivingEntity.class)
public abstract class LivingEntityLootMixin {
    @Inject(method = "dropLoot", at = @At("HEAD"), cancellable = true)
    private void cursedtides$noJunk(DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        if (causedByPlayer && Levels.levelOf((LivingEntity) (Object) this) > 0) ci.cancel();
    }
}
