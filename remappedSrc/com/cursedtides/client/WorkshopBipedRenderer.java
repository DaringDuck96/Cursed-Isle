package com.cursedtides.client;

import com.cursedtides.CursedTides;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

/**
 * People and the walking dead, drawn with the Mob Workshop's models. Each entity picks a model (its build: a coat
 * and tricorn, a hood and robe, a brute's bulk...) and a texture; models are swapped in per entity as it's drawn.
 */
public class WorkshopBipedRenderer<T extends Mob> extends HumanoidMobRenderer<T, HumanoidModel<T>> {
    private final Map<String, HumanoidModel<T>> models = new HashMap<>();
    private final Function<T, String> modelFor;
    private final Function<T, String> textureFor;
    private final float scale;

    public WorkshopBipedRenderer(EntityRendererProvider.Context ctx, String[] modelIds, Function<T, String> modelFor,
                                 Function<T, String> textureFor, float scale) {
        super(ctx, new HumanoidModel<>(ctx.bakeLayer(WorkshopModels.layer(modelIds[0]))), 0.5f * scale);
        for (String id : modelIds) models.put(id, new HumanoidModel<>(ctx.bakeLayer(WorkshopModels.layer(id))));
        this.modelFor = modelFor;
        this.textureFor = textureFor;
        this.scale = scale;
    }

    /** One model for everyone of this kind. */
    public static <E extends Mob> WorkshopBipedRenderer<E> single(EntityRendererProvider.Context ctx, String id) {
        return new WorkshopBipedRenderer<>(ctx, new String[]{id}, e -> id, e -> id, 1.0f);
    }

    @Override
    public void render(T entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource consumers, int light) {
        HumanoidModel<T> m = models.get(modelFor.apply(entity));
        if (m != null) this.model = m;
        super.render(entity, yaw, tickDelta, matrices, consumers, light);
        // A marker over the head of anyone with something for you (see Nameplates)
        int mark = MARKERS.getOrDefault(entity.getId(), 0);
        if (mark > 0) {
            net.minecraft.network.chat.Component t = mark == 2 ? net.minecraft.network.chat.Component.literal("?").withStyle(net.minecraft.ChatFormatting.GREEN, net.minecraft.ChatFormatting.BOLD)
                    : net.minecraft.network.chat.Component.literal("!").withStyle(mark == 3 ? net.minecraft.ChatFormatting.GOLD : net.minecraft.ChatFormatting.YELLOW, net.minecraft.ChatFormatting.BOLD);
            matrices.pushPose();
            matrices.translate(0, 0.45 + Math.sin((entity.tickCount + tickDelta) * 0.1) * 0.06, 0);
            this.renderNameTag(entity, t, matrices, consumers, light, tickDelta);
            matrices.popPose();
        }
    }

    /** Entity id to marker kind, from the server (1 work, 2 come back, 3 story). */
    public static final Map<Integer, Integer> MARKERS = new HashMap<>();

    @Override
    protected void scale(T entity, PoseStack matrices, float amount) {
        if (scale != 1.0f) matrices.scale(scale, scale, scale);
    }

    @Override
    public ResourceLocation getTexture(T entity) {
        return CursedTides.id("textures/entity/workshop/" + textureFor.apply(entity) + ".png");
    }
}
