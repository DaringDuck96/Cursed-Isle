package com.cursedtides.client;

import com.cursedtides.CursedTides;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.AbstractSkeleton;

/** The skeleton model, a quarter bigger, in the captain's own texture. */
public class DrownedCaptainRenderer extends SkeletonRenderer<AbstractSkeleton> {
    private static final ResourceLocation TEXTURE = CursedTides.id("textures/entity/drowned_captain.png");

    public DrownedCaptainRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.7f;
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractSkeleton entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(AbstractSkeleton entity, PoseStack matrices, float amount) {
        matrices.scale(1.25f, 1.25f, 1.25f);
    }
}
