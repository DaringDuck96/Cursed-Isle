package com.cursedtides.client;

import com.cursedtides.CursedTides;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import java.util.function.Function;

/** Renders a wild creature with its generated model and a texture chosen per entity. */
public class CreatureRenderer<T extends Mob> extends MobRenderer<T, CreatureRenderer.State, CreatureModel> {
    /** What the creature model needs to pose itself. */
    public static class State extends LivingEntityRenderState {
        public float attackTime;
        public Identifier texture;
    }

    private final Function<T, String> texture;
    private final float scale;
    private final String folder;

    public CreatureRenderer(EntityRendererProvider.Context ctx, ModelLayerLocation layer, String[] parts, float shadow,
                            float scale, Function<T, String> texture) {
        this(ctx, layer, parts, shadow, scale, texture, "wildlife");
    }

    /** As above, with textures from textures/entity/&lt;folder&gt;/ (the workshop's bosses use "workshop"). */
    public CreatureRenderer(EntityRendererProvider.Context ctx, ModelLayerLocation layer, String[] parts, float shadow,
                            float scale, Function<T, String> texture, String folder) {
        super(ctx, new CreatureModel(ctx.bakeLayer(layer), parts), shadow * scale);
        this.texture = texture;
        this.scale = scale;
        this.folder = folder;
    }

    public static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(CursedTides.id(name), "main");
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(T entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.attackTime = entity.getSwingAnimation(partialTicks);
        state.texture = CursedTides.id("textures/entity/" + folder + "/" + texture.apply(entity) + ".png");
    }

    @Override
    protected void scale(State state, PoseStack matrices) {
        matrices.scale(scale, scale, scale);
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return state.texture;
    }
}
