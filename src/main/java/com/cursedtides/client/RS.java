package com.cursedtides.client;

import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

/** Extra data our renderers carry on vanilla render states: the texture to use, and the entity itself. */
public final class RS {
    private RS() {}

    public static final RenderStateDataKey<Identifier> TEXTURE = RenderStateDataKey.create(() -> "cursedtides:texture");
    public static final RenderStateDataKey<Entity> ENTITY = RenderStateDataKey.create(() -> "cursedtides:entity");

    public static final RenderStateDataKey<String> MODEL = RenderStateDataKey.create(() -> "cursedtides:model");
    public static final RenderStateDataKey<net.minecraft.network.chat.Component> MARK = RenderStateDataKey.create(() -> "cursedtides:mark");
    public static final RenderStateDataKey<net.minecraft.world.phys.Vec3> MARK_AT = RenderStateDataKey.create(() -> "cursedtides:mark_at");

    public static Identifier tex(EntityRenderState s, Identifier fallback) {
        Identifier t = s.getData(TEXTURE);
        return t == null ? fallback : t;
    }

    public static Entity entity(EntityRenderState s) {
        return s.getData(ENTITY);
    }
}
