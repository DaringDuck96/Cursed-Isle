package com.cursedtides.mixin;

import com.cursedtides.client.BossLooks;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Named bosses get their own Mob Workshop look (see BossLooks). */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "getRenderer(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/client/renderer/entity/EntityRenderer;",
            at = @At("HEAD"), cancellable = true)
    private void cursedtides$bossLook(Entity entity, CallbackInfoReturnable cir) {
        EntityRenderer<?, ?> boss = BossLooks.forEntity(entity);
        if (boss != null) cir.setReturnValue(boss);
    }

    /** Remember which renderer drew up the state, so the same one draws it. */
    @Inject(method = "extractEntity", at = @At("RETURN"))
    private void cursedtides$markBoss(Entity entity, float partialTicks, CallbackInfoReturnable<EntityRenderState> cir) {
        EntityRenderer<?, ?> boss = BossLooks.forEntity(entity);
        if (boss != null && cir.getReturnValue() != null) cir.getReturnValue().setData(BossLooks.RENDERER, boss);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "getRenderer(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;)Lnet/minecraft/client/renderer/entity/EntityRenderer;",
            at = @At("HEAD"), cancellable = true)
    private void cursedtides$bossState(EntityRenderState state, CallbackInfoReturnable cir) {
        EntityRenderer<?, ?> boss = state.getData(BossLooks.RENDERER);
        if (boss != null) cir.setReturnValue(boss);
    }
}
