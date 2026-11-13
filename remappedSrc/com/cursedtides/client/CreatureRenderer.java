package com.cursedtides.client;

import com.cursedtides.CursedTides;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Function;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

/** Renders a wild creature with its generated model and a texture chosen per entity. */
public class CreatureRenderer<T extends Mob> extends MobRenderer<T, CreatureModel<T>> {
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
        super(ctx, new CreatureModel<>(ctx.bakeLayer(layer), parts), shadow * scale);
        this.texture = texture;
        this.scale = scale;
        this.folder = folder;
    }

    public static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(CursedTides.id(name), "main");
    }

    @Override
    protected void scale(T entity, PoseStack matrices, float amount) {
        matrices.scale(scale, scale, scale);
    }

    @Override
    public ResourceLocation getTexture(T entity) {
        return CursedTides.id("textures/entity/" + folder + "/" + texture.apply(entity) + ".png");
    }
}
