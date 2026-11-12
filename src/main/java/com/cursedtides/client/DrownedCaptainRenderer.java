package com.cursedtides.client;

import com.cursedtides.CursedTides;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.SkeletonEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.util.Identifier;

/** The skeleton model, a quarter bigger, in the captain's own texture. */
public class DrownedCaptainRenderer extends SkeletonEntityRenderer<AbstractSkeletonEntity> {
    private static final Identifier TEXTURE = CursedTides.id("textures/entity/drowned_captain.png");

    public DrownedCaptainRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.7f;
    }

    @Override
    public Identifier getTexture(AbstractSkeletonEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(AbstractSkeletonEntity entity, MatrixStack matrices, float amount) {
        matrices.scale(1.25f, 1.25f, 1.25f);
    }
}
