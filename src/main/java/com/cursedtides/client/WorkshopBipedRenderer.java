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
        // A marker over the head of anyone with something for you (see Nameplates)
        int mark = MARKERS.getOrDefault(entity.getId(), 0);
        if (mark > 0) {
            net.minecraft.text.Text t = mark == 2 ? net.minecraft.text.Text.literal("?").formatted(net.minecraft.util.Formatting.GREEN, net.minecraft.util.Formatting.BOLD)
                    : net.minecraft.text.Text.literal("!").formatted(mark == 3 ? net.minecraft.util.Formatting.GOLD : net.minecraft.util.Formatting.YELLOW, net.minecraft.util.Formatting.BOLD);
            matrices.push();
            matrices.translate(0, 0.45 + Math.sin((entity.age + tickDelta) * 0.1) * 0.06, 0);
            this.renderLabelIfPresent(entity, t, matrices, consumers, light, tickDelta);
            matrices.pop();
        }
    }

    /** Entity id to marker kind, from the server (1 work, 2 come back, 3 story). */
    public static final Map<Integer, Integer> MARKERS = new HashMap<>();

    @Override
    protected void scale(T entity, MatrixStack matrices, float amount) {
        if (scale != 1.0f) matrices.scale(scale, scale, scale);
    }

    @Override
    public Identifier getTexture(T entity) {
        return CursedTides.id("textures/entity/workshop/" + textureFor.apply(entity) + ".png");
    }
}
