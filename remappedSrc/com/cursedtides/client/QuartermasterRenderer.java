package com.cursedtides.client;

import com.cursedtides.CursedTides;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WanderingTraderRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.WanderingTrader;

/** Reuses the vanilla trader model with the mod's own coat-and-hat texture. */
public class QuartermasterRenderer extends WanderingTraderRenderer {
    private static final ResourceLocation TEXTURE = CursedTides.id("textures/entity/quartermaster.png");

    public QuartermasterRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(WanderingTrader entity) {
        return TEXTURE;
    }
}
