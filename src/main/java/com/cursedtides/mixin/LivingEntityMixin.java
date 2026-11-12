package com.cursedtides.mixin;

import com.cursedtides.combat.Levels;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Level-scaled hits, and a hook after each hit lands (damage numbers, weapon reputation). */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Unique
    private float cursedtides$healthBefore;

    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private float cursedtides$scaleDamage(float amount, DamageSource source, float original) {
        return Levels.scaleIncoming((LivingEntity) (Object) this, source, amount);
    }

    @Inject(method = "applyDamage", at = @At("HEAD"))
    private void cursedtides$beforeHit(DamageSource source, float amount, CallbackInfo ci) {
        cursedtides$healthBefore = ((LivingEntity) (Object) this).getHealth();
    }

    @Inject(method = "applyDamage", at = @At("TAIL"))
    private void cursedtides$afterHit(DamageSource source, float amount, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        Levels.afterDamage(self, source, cursedtides$healthBefore - self.getHealth());
    }
}
