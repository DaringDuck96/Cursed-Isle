package com.cursedtides.mixin;

import com.cursedtides.client.BossLooks;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Named bosses get their own Mob Workshop look (see BossLooks). */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "getRenderer", at = @At("HEAD"), cancellable = true)
    private void cursedtides$bossLook(Entity entity, CallbackInfoReturnable cir) {
        EntityRenderer<?> boss = BossLooks.forEntity(entity);
        if (boss != null) cir.setReturnValue(boss);
    }
}
