package com.cursedtides.client;

import com.cursedtides.CursedTides;
import com.cursedtides.entity.ShipEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class ShipRenderer extends EntityRenderer<ShipEntity> {
    public static final EntityModelLayer LAYER = new EntityModelLayer(CursedTides.id("ship"), "main");
    private static final Identifier TEXTURE = CursedTides.id("textures/entity/ship.png");
    private static final Identifier GHOST_TEXTURE = CursedTides.id("textures/entity/ghost_ship.png");
    private static final Identifier NAVY_TEXTURE = CursedTides.id("textures/entity/navy_ship.png");

    private final ShipModel model;

    public ShipRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 1.2f;
        this.model = new ShipModel(context.getPart(LAYER));
    }

    @Override
    public void render(ShipEntity ship, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.translate(0.0, 0.1, 0.0);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw));

        // Rock when hit, and roll gently with the swell
        float wobble = ship.getDamageWobbleTicks() - tickDelta;
        float strength = Math.max(0.0f, ship.getDamageWobbleStrength() - tickDelta);
        if (wobble > 0.0f) {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(
                    MathHelper.sin(wobble) * wobble * strength / 40.0f * ship.getDamageWobbleSide()));
        }
        float swell = (ship.age + tickDelta) * 0.05f;
        // Heavier seas in rain, heavier still in a storm
        float rough = 1.0f + 1.5f * ship.getWorld().getRainGradient(tickDelta) + 2.0f * ship.getWorld().getThunderGradient(tickDelta);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.sin(swell * rough) * 1.5f * rough));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(MathHelper.sin(swell * 0.7f * rough) * 0.8f * rough));

        float s = ship.shipClass().scale();
        matrices.scale(s, s, s);
        matrices.scale(-1.0f, -1.0f, 1.0f);

        this.model.setAngles(ship, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        boolean customFlag = ship.getFlag() > 0;
        this.model.setCustomFlag(customFlag);
        VertexConsumer vertices = vertexConsumers.getBuffer(this.model.getLayer(getTexture(ship)));
        this.model.renderHull(matrices, vertices, light, OverlayTexture.DEFAULT_UV);
        int tint = ship.getSailColor() < 0 ? 0xFFFFFFFF : 0xFF000000 | ship.getSailColor();
        this.model.renderSail(matrices, vertices, light, OverlayTexture.DEFAULT_UV, tint);
        // Extra masts: aft for a two-master, aft and fore for a three-master
        int masts = ship.shipClass().masts();
        float[] offsets = masts == 3 ? new float[]{-17.0f, 16.0f} : masts == 2 ? new float[]{-17.0f} : new float[0];
        for (float dz : offsets) {
            matrices.push();
            matrices.translate(0.0f, 0.0f, dz / 16.0f);
            float shorter = dz > 0 ? 0.85f : 0.92f;
            matrices.translate(0.0f, -4.0f / 16.0f * (1 - shorter), 0.0f);
            matrices.scale(1.0f, shorter, 1.0f);
            this.model.renderRig(matrices, vertices, light, OverlayTexture.DEFAULT_UV, tint);
            matrices.pop();
        }
        if (customFlag) {
            Identifier flagTexture = CursedTides.id("textures/entity/flags/" + com.cursedtides.ship.FlagItem.Design.byIndex(ship.getFlag())
                    .name().toLowerCase() + ".png");
            VertexConsumer flagVertices = vertexConsumers.getBuffer(net.minecraft.client.render.RenderLayer.getEntityCutoutNoCull(flagTexture));
            this.model.renderFlag(matrices, flagVertices, light, OverlayTexture.DEFAULT_UV);
        }
        this.model.setCustomFlag(false);
        var fig = ship.getFigurehead();
        if (fig != com.cursedtides.ship.FigureheadItem.Kind.NONE) {
            var item = com.cursedtides.ship.FigureheadItem.itemFor(fig);
            if (item != null) {
                matrices.push();
                matrices.translate(0.0f, -4.0f / 16.0f, 37.5f / 16.0f); // just ahead of the bow tip
                matrices.scale(-1.0f, -1.0f, 1.0f);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-20));
                matrices.scale(1.4f, 1.4f, 1.4f);
                net.minecraft.client.MinecraftClient.getInstance().getItemRenderer().renderItem(new net.minecraft.item.ItemStack(item),
                        net.minecraft.client.render.model.json.ModelTransformationMode.FIXED, light, OverlayTexture.DEFAULT_UV,
                        matrices, vertexConsumers, ship.getWorld(), ship.getId());
                matrices.pop();
            }
        }
        matrices.pop();

        super.render(ship, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(ShipEntity ship) {
        return switch (ship.getLook()) {
            case ShipEntity.LOOK_GHOST -> GHOST_TEXTURE;
            case ShipEntity.LOOK_NAVY -> NAVY_TEXTURE;
            default -> TEXTURE;
        };
    }
}
