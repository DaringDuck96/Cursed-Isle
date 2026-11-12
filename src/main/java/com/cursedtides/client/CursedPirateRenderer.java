package com.cursedtides.client;

import com.cursedtides.CursedTides;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.SkeletonEntityRenderer;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.util.Identifier;

/** Reuses the vanilla skeleton model with the mod's own pirate texture. */
public class CursedPirateRenderer extends SkeletonEntityRenderer {
    private static final Identifier TEXTURE = CursedTides.id("textures/entity/cursed_pirate.png");
    private static final Identifier BONE = CursedTides.id("textures/entity/cursed_pirate_bone.png");
    private static final Identifier BARNACLE = CursedTides.id("textures/entity/cursed_pirate_barnacle.png");
    private static final Identifier EMBER = CursedTides.id("textures/entity/cursed_pirate_ember.png");

    public CursedPirateRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(AbstractSkeletonEntity entity) {
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
