package com.cursedtides.client;

import com.cursedtides.CursedTides;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * People and the walking dead, drawn with the Mob Workshop's models. Each entity picks a model (its build: a coat
 * and tricorn, a hood and robe, a brute's bulk...) and a texture; models are swapped in per entity as it's drawn.
 */
public class WorkshopBipedRenderer<T extends MobEntity> extends BipedEntityRenderer<T, BipedEntityModel<T>> {
    private final Map<String, BipedEntityModel<T>> models = new HashMap<>();
    private final Function<T, String> modelFor;
    private final Function<T, String> textureFor;
    private final float scale;

    public WorkshopBipedRenderer(EntityRendererFactory.Context ctx, String[] modelIds, Function<T, String> modelFor,
                                 Function<T, String> textureFor, float scale) {
        super(ctx, new BipedEntityModel<>(ctx.getPart(WorkshopModels.layer(modelIds[0]))), 0.5f * scale);
        for (String id : modelIds) models.put(id, new BipedEntityModel<>(ctx.getPart(WorkshopModels.layer(id))));
        this.modelFor = modelFor;
        this.textureFor = textureFor;
        this.scale = scale;
    }

    /** One model for everyone of this kind. */
    public static <E extends MobEntity> WorkshopBipedRenderer<E> single(EntityRendererFactory.Context ctx, String id) {
        return new WorkshopBipedRenderer<>(ctx, new String[]{id}, e -> id, e -> id, 1.0f);
    }

    @Override
    public void render(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider consumers, int light) {
        BipedEntityModel<T> m = models.get(modelFor.apply(entity));
        if (m != null) this.model = m;
        super.render(entity, yaw, tickDelta, matrices, consumers, light);
    }

    @Override
    protected void scale(T entity, MatrixStack matrices, float amount) {
        if (scale != 1.0f) matrices.scale(scale, scale, scale);
    }

    @Override
    public Identifier getTexture(T entity) {
        return CursedTides.id("textures/entity/workshop/" + textureFor.apply(entity) + ".png");
    }
}
