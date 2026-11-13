package com.cursedtides.client;

import com.cursedtides.CursedTides;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.Mob;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * People and the walking dead, drawn with the Mob Workshop's models. Each entity picks a model (its build: a coat
 * and tricorn, a hood and robe, a brute's bulk...) and a texture; models are swapped in per entity as it's drawn.
 */
public class WorkshopBipedRenderer<T extends Mob> extends HumanoidMobRenderer<T, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {
    private final Map<String, HumanoidModel<HumanoidRenderState>> models = new HashMap<>();
    private final HumanoidModel<HumanoidRenderState> first;
    private final Function<T, String> modelFor;
    private final Function<T, String> textureFor;
    private final float scale;

    public WorkshopBipedRenderer(EntityRendererProvider.Context ctx, String[] modelIds, Function<T, String> modelFor,
                                 Function<T, String> textureFor, float scale) {
        super(ctx, new HumanoidModel<>(ctx.bakeLayer(WorkshopModels.layer(modelIds[0]))), 0.5f * scale);
        this.first = this.model;
        models.put(modelIds[0], this.model);
        for (String id : modelIds) models.computeIfAbsent(id, k -> new HumanoidModel<>(ctx.bakeLayer(WorkshopModels.layer(k))));
        this.modelFor = modelFor;
        this.textureFor = textureFor;
        this.scale = scale;
    }

    /** One model for everyone of this kind. */
    public static <E extends Mob> WorkshopBipedRenderer<E> single(EntityRendererProvider.Context ctx, String id) {
        return new WorkshopBipedRenderer<>(ctx, new String[]{id}, e -> id, e -> id, 1.0f);
    }

    /** Entity id to marker kind, from the server (1 work, 2 come back, 3 story). */
    public static final Map<Integer, Integer> MARKERS = new HashMap<>();

    @Override
    public HumanoidRenderState createRenderState() {
        return new HumanoidRenderState();
    }

    @Override
    public void extractRenderState(T entity, HumanoidRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.setData(RS.MODEL, modelFor.apply(entity));
        state.setData(RS.TEXTURE, CursedTides.id("textures/entity/workshop/" + textureFor.apply(entity) + ".png"));
        int mark = MARKERS.getOrDefault(entity.getId(), 0);
        if (mark > 0) {
            Component t = mark == 2 ? Component.literal("?").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)
                    : Component.literal("!").withStyle(mark == 3 ? ChatFormatting.GOLD : ChatFormatting.YELLOW, ChatFormatting.BOLD);
            state.setData(RS.MARK, t);
            state.setData(RS.MARK_AT, entity.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, entity.getYRot(partialTicks)));
        } else {
            state.setData(RS.MARK, null);
        }
    }

    @Override
    public void submit(HumanoidRenderState state, PoseStack matrices, SubmitNodeCollector collector, CameraRenderState camera) {
        HumanoidModel<HumanoidRenderState> m = models.get(state.getData(RS.MODEL));
        this.model = m != null ? m : first;
        super.submit(state, matrices, collector, camera);
        // A marker over the head of anyone with something for you (see Nameplates)
        Component mark = state.getData(RS.MARK);
        if (mark != null && state.getData(RS.MARK_AT) != null) {
            matrices.pushPose();
            matrices.translate(0, 0.45 + Math.sin(state.ageInTicks * 0.1) * 0.06, 0);
            collector.submitNameTag(matrices, state.getData(RS.MARK_AT), 0, mark, true, state.lightCoords, camera);
            matrices.popPose();
        }
    }

    @Override
    protected void scale(HumanoidRenderState state, PoseStack matrices) {
        if (scale != 1.0f) matrices.scale(scale, scale, scale);
    }

    @Override
    public Identifier getTextureLocation(HumanoidRenderState state) {
        return RS.tex(state, CursedTides.id("textures/entity/workshop/missing.png"));
    }
}
