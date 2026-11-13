package com.cursedtides.client;

import com.cursedtides.CursedTides;
import com.cursedtides.entity.CrewmateEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Hired hands, dressed for their trade. */
public class CrewmateRenderer extends HumanoidMobRenderer<CrewmateEntity, HumanoidModel<CrewmateEntity>> {
    private static final ResourceLocation[] TEXTURES = {
            CursedTides.id("textures/entity/crew_gunner.png"),
            CursedTides.id("textures/entity/crew_bosun.png"),
            CursedTides.id("textures/entity/crew_navigator.png")
    };

    public CrewmateRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5f);
    }

    @Override
    public ResourceLocation getTexture(CrewmateEntity entity) {
        return TEXTURES[entity.getRole()];
    }
}
