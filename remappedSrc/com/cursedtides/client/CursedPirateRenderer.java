package com.cursedtides.client;

import com.cursedtides.CursedTides;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.AbstractSkeleton;

/** Reuses the vanilla skeleton model with the mod's own pirate texture. */
public class CursedPirateRenderer extends SkeletonRenderer {
    private static final ResourceLocation TEXTURE = CursedTides.id("textures/entity/cursed_pirate.png");
    private static final ResourceLocation BONE = CursedTides.id("textures/entity/cursed_pirate_bone.png");
    private static final ResourceLocation BARNACLE = CursedTides.id("textures/entity/cursed_pirate_barnacle.png");
    private static final ResourceLocation EMBER = CursedTides.id("textures/entity/cursed_pirate_ember.png");

    public CursedPirateRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractSkeleton entity) {
        if (entity instanceof com.cursedtides.entity.CursedPirateEntity cp) {
            return switch (cp.kind()) {
                case BONE -> BONE;
                case BARNACLE -> BARNACLE;
                case EMBER -> EMBER;
                default -> TEXTURE;
            };
        }
        return TEXTURE;
    }
}
