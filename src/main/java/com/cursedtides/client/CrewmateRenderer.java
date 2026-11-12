package com.cursedtides.client;

import com.cursedtides.CursedTides;
import com.cursedtides.entity.CrewmateEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

/** Hired hands, dressed for their trade. */
public class CrewmateRenderer extends BipedEntityRenderer<CrewmateEntity, BipedEntityModel<CrewmateEntity>> {
    private static final Identifier[] TEXTURES = {
            CursedTides.id("textures/entity/crew_gunner.png"),
            CursedTides.id("textures/entity/crew_bosun.png"),
            CursedTides.id("textures/entity/crew_navigator.png")
    };

    public CrewmateRenderer(EntityRendererFactory.Context context) {
        super(context, new BipedEntityModel<>(context.getPart(EntityModelLayers.ZOMBIE)), 0.5f);
    }

    @Override
    public Identifier getTexture(CrewmateEntity entity) {
        return TEXTURES[entity.getRole()];
    }
}
