package com.cursedtides.client;

import com.cursedtides.CursedTides;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.WanderingTraderEntityRenderer;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.util.Identifier;

/** Reuses the vanilla trader model with the mod's own coat-and-hat texture. */
public class QuartermasterRenderer extends WanderingTraderEntityRenderer {
    private static final Identifier TEXTURE = CursedTides.id("textures/entity/quartermaster.png");

    public QuartermasterRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(WanderingTraderEntity entity) {
        return TEXTURE;
    }
}
