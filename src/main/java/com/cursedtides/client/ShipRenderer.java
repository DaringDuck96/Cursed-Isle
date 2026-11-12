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
    /** Each class's own hull from the Mob Workshop, by class name. */
    private final java.util.Map<String, WorkshopShipModel> hulls = new java.util.HashMap<>();
    /** Where each class's bow carries its figurehead (model pixels: height, distance forward). */
    private static final java.util.Map<String, float[]> BOWS = java.util.Map.ofEntries(java.util.Map.entry("sloop", new float[]{-5f, 37.5f}), java.util.Map.entry("schooner", new float[]{-5f, 41.5f}), java.util.Map.entry("brigantine", new float[]{-7f, 44.5f}), java.util.Map.entry("brig", new float[]{-7f, 46.5f}), java.util.Map.entry("frigate", new float[]{-9f, 51.5f}), java.util.Map.entry("galleon", new float[]{-12f, 53.5f}), java.util.Map.entry("ship_of_the_line", new float[]{-16f, 59.5f}));

    public ShipRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 1.2f;
        this.model = new ShipModel(context.getPart(LAYER));
        for (String cls : BOWS.keySet()) hulls.put(cls, new WorkshopShipModel(context.getPart(WorkshopModels.layer("ship_" + cls))));
    }

    private static String cls(ShipEntity ship) {
        return ship.shipClass().name().toLowerCase();
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

        WorkshopShipModel hull = hulls.get(cls(ship));
        if (hull != null) {
            renderWorkshop(ship, hull, matrices, vertexConsumers, light);
            matrices.pop();
            super.render(ship, yaw, tickDelta, matrices, vertexConsumers, light);
            return;
        }
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
        if (ship.getEmblem() > 0) {
            Identifier emblemTexture = CursedTides.id("textures/entity/flags/" + com.cursedtides.ship.FlagItem.Design.byIndex(ship.getEmblem() - 1)
                    .name().toLowerCase() + ".png");
            this.model.renderEmblem(matrices, vertexConsumers.getBuffer(net.minecraft.client.render.RenderLayer.getEntityCutoutNoCull(emblemTexture)),
                    light, OverlayTexture.DEFAULT_UV);
        }
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

    /** The class's own hull is drawn at its true size (no class scaling); flags, emblem and figurehead as before. */
    private void renderWorkshop(ShipEntity ship, WorkshopShipModel hull, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.scale(-1.0f, -1.0f, 1.0f);
        hull.setAngles(ship, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        boolean customFlag = ship.getFlag() > 0;
        hull.setCustomFlag(customFlag);
        var fig = ship.getFigurehead();
        var figItem = fig != com.cursedtides.ship.FigureheadItem.Kind.NONE ? com.cursedtides.ship.FigureheadItem.itemFor(fig) : null;
        VertexConsumer vertices = vertexConsumers.getBuffer(hull.getLayer(getTexture(ship)));
        hull.renderHull(matrices, vertices, light, OverlayTexture.DEFAULT_UV, figItem != null);
        int tint = ship.getSailColor() < 0 ? 0xFFFFFFFF : 0xFF000000 | ship.getSailColor();
        hull.renderSails(matrices, vertices, light, OverlayTexture.DEFAULT_UV, tint);
        if (customFlag) {
            Identifier flagTexture = CursedTides.id("textures/entity/flags/" + com.cursedtides.ship.FlagItem.Design.byIndex(ship.getFlag())
                    .name().toLowerCase() + ".png");
            hull.renderFlag(matrices, vertexConsumers.getBuffer(net.minecraft.client.render.RenderLayer.getEntityCutoutNoCull(flagTexture)), light, OverlayTexture.DEFAULT_UV);
        }
        hull.setCustomFlag(false);
        if (ship.getEmblem() > 0) {
            Identifier emblemTexture = CursedTides.id("textures/entity/flags/" + com.cursedtides.ship.FlagItem.Design.byIndex(ship.getEmblem() - 1)
                    .name().toLowerCase() + ".png");
            hull.renderEmblem(matrices, vertexConsumers.getBuffer(net.minecraft.client.render.RenderLayer.getEntityCutoutNoCull(emblemTexture)),
                    light, OverlayTexture.DEFAULT_UV);
        }
        if (figItem != null) {
            float[] bow = BOWS.get(cls(ship));
            matrices.push();
            matrices.translate(0.0f, bow[0] / 16.0f, bow[1] / 16.0f);
            matrices.scale(-1.0f, -1.0f, 1.0f);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-20));
            matrices.scale(1.4f, 1.4f, 1.4f);
            net.minecraft.client.MinecraftClient.getInstance().getItemRenderer().renderItem(new net.minecraft.item.ItemStack(figItem),
                    net.minecraft.client.render.model.json.ModelTransformationMode.FIXED, light, OverlayTexture.DEFAULT_UV,
                    matrices, vertexConsumers, ship.getWorld(), ship.getId());
            matrices.pop();
        }
    }

    @Override
    public Identifier getTexture(ShipEntity ship) {
        if (hulls.containsKey(cls(ship))) {
            String look = switch (ship.getLook()) {
                case ShipEntity.LOOK_GHOST -> "ghost";
                case ShipEntity.LOOK_NAVY -> "navy";
                default -> "plain";
            };
            return CursedTides.id("textures/entity/workshop/ship_" + cls(ship) + "_" + look + ".png");
        }
        return switch (ship.getLook()) {
            case ShipEntity.LOOK_GHOST -> GHOST_TEXTURE;
            case ShipEntity.LOOK_NAVY -> NAVY_TEXTURE;
            default -> TEXTURE;
        };
    }
}
