package com.cursedtides.client;

import com.cursedtides.CursedTides;
import com.cursedtides.entity.MarineEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/** A humanoid in Tidewatch blue, holding a musket. Uses the standard 64x64 humanoid model. */
public class MarineRenderer extends HumanoidMobRenderer<MarineEntity, HumanoidModel<MarineEntity>> {
    private static final ResourceLocation TEXTURE = CursedTides.id("textures/entity/marine.png");

    public MarineRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5f);
    }

    @Override
    public ResourceLocation getTexture(MarineEntity entity) {
        return TEXTURE;
    }
}
