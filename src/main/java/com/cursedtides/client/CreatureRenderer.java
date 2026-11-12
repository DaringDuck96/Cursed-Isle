package com.cursedtides.client;

import com.cursedtides.CursedTides;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

import java.util.function.Function;

/** Renders a wild creature with its generated model and a texture chosen per entity. */
public class CreatureRenderer<T extends MobEntity> extends MobEntityRenderer<T, CreatureModel<T>> {
    private final Function<T, String> texture;
    private final float scale;
    private final String folder;

    public CreatureRenderer(EntityRendererFactory.Context ctx, EntityModelLayer layer, String[] parts, float shadow,
                            float scale, Function<T, String> texture) {
        this(ctx, layer, parts, shadow, scale, texture, "wildlife");
    }

    /** As above, with textures from textures/entity/&lt;folder&gt;/ (the workshop's bosses use "workshop"). */
    public CreatureRenderer(EntityRendererFactory.Context ctx, EntityModelLayer layer, String[] parts, float shadow,
                            float scale, Function<T, String> texture, String folder) {
        super(ctx, new CreatureModel<>(ctx.getPart(layer), parts), shadow * scale);
        this.texture = texture;
        this.scale = scale;
        this.folder = folder;
    }

    public static EntityModelLayer layer(String name) {
        return new EntityModelLayer(CursedTides.id(name), "main");
    }

    @Override
    protected void scale(T entity, MatrixStack matrices, float amount) {
        matrices.scale(scale, scale, scale);
    }

    @Override
    public Identifier getTexture(T entity) {
        return CursedTides.id("textures/entity/" + folder + "/" + texture.apply(entity) + ".png");
    }
}
