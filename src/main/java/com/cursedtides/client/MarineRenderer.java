package com.cursedtides.client;

import com.cursedtides.CursedTides;
import com.cursedtides.entity.MarineEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

/** A humanoid in Tidewatch blue, holding a musket. Uses the standard 64x64 humanoid model. */
public class MarineRenderer extends BipedEntityRenderer<MarineEntity, BipedEntityModel<MarineEntity>> {
    private static final Identifier TEXTURE = CursedTides.id("textures/entity/marine.png");

    public MarineRenderer(EntityRendererFactory.Context context) {
        super(context, new BipedEntityModel<>(context.getPart(EntityModelLayers.ZOMBIE)), 0.5f);
    }

    @Override
    public Identifier getTexture(MarineEntity entity) {
        return TEXTURE;
    }
}
