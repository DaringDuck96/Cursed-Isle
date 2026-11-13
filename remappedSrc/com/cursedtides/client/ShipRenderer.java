package com.cursedtides.client;

import com.cursedtides.CursedTides;
import com.cursedtides.entity.ShipEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ShipRenderer extends EntityRenderer<ShipEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(CursedTides.id("ship"), "main");
    private static final ResourceLocation TEXTURE = CursedTides.id("textures/entity/ship.png");
    private static final ResourceLocation GHOST_TEXTURE = CursedTides.id("textures/entity/ghost_ship.png");
    private static final ResourceLocation NAVY_TEXTURE = CursedTides.id("textures/entity/navy_ship.png");

    private final ShipModel model;
    /** Each class's own hull from the Mob Workshop, by class name. */
    private final java.util.Map<String, WorkshopShipModel> hulls = new java.util.HashMap<>();
    /** Where each class's bow carries its figurehead (model pixels: height, distance forward). */
    private static final java.util.Map<String, float[]> BOWS = java.util.Map.ofEntries(java.util.Map.entry("sloop", new float[]{-5f, 37.5f}), java.util.Map.entry("schooner", new float[]{-5f, 41.5f}), java.util.Map.entry("brigantine", new float[]{-7f, 44.5f}), java.util.Map.entry("brig", new float[]{-7f, 46.5f}), java.util.Map.entry("frigate", new float[]{-9f, 51.5f}), java.util.Map.entry("galleon", new float[]{-12f, 53.5f}), java.util.Map.entry("ship_of_the_line", new float[]{-16f, 59.5f}));

    public ShipRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 1.2f;
        this.model = new ShipModel(context.bakeLayer(LAYER));
        for (String cls : BOWS.keySet()) hulls.put(cls, new WorkshopShipModel(context.bakeLayer(WorkshopModels.layer("ship_" + cls))));
    }

    private static String cls(ShipEntity ship) {
        return ship.shipClass().name().toLowerCase();
    }

    @Override
    public void render(ShipEntity ship, float yaw, float tickDelta, PoseStack matrices,
                       MultiBufferSource vertexConsumers, int light) {
        matrices.pushPose();
        matrices.translate(0.0, 0.1, 0.0);
        matrices.mulPose(Axis.YP.rotationDegrees(-yaw));

        // Rock when hit, and roll gently with the swell
        float wobble = ship.getHurtTime() - tickDelta;
        float strength = Math.max(0.0f, ship.getDamage() - tickDelta);
        if (wobble > 0.0f) {
            matrices.mulPose(Axis.ZP.rotationDegrees(
                    Mth.sin(wobble) * wobble * strength / 40.0f * ship.getHurtDir()));
        }
        float swell = (ship.tickCount + tickDelta) * 0.05f;
        // Heavier seas in rain, heavier still in a storm
        float rough = 1.0f + 1.5f * ship.level().getRainLevel(tickDelta) + 2.0f * ship.level().getThunderLevel(tickDelta);
        matrices.mulPose(Axis.ZP.rotationDegrees(Mth.sin(swell * rough) * 1.5f * rough));
        matrices.mulPose(Axis.XP.rotationDegrees(Mth.sin(swell * 0.7f * rough) * 0.8f * rough));

        WorkshopShipModel hull = hulls.get(cls(ship));
        if (hull != null) {
            renderWorkshop(ship, hull, matrices, vertexConsumers, light);
            matrices.popPose();
            super.render(ship, yaw, tickDelta, matrices, vertexConsumers, light);
            return;
        }
        float s = ship.shipClass().scale();
        matrices.scale(s, s, s);
        matrices.scale(-1.0f, -1.0f, 1.0f);

        this.model.setAngles(ship, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        boolean customFlag = ship.getFlag() > 0;
        this.model.setCustomFlag(customFlag);
        VertexConsumer vertices = vertexConsumers.getBuffer(this.model.renderType(getTexture(ship)));
        this.model.renderHull(matrices, vertices, light, OverlayTexture.NO_OVERLAY);
        int tint = ship.getSailColor() < 0 ? 0xFFFFFFFF : 0xFF000000 | ship.getSailColor();
        this.model.renderSail(matrices, vertices, light, OverlayTexture.NO_OVERLAY, tint);
        // Extra masts: aft for a two-master, aft and fore for a three-master
        int masts = ship.shipClass().masts();
        float[] offsets = masts == 3 ? new float[]{-17.0f, 16.0f} : masts == 2 ? new float[]{-17.0f} : new float[0];
        for (float dz : offsets) {
            matrices.pushPose();
            matrices.translate(0.0f, 0.0f, dz / 16.0f);
            float shorter = dz > 0 ? 0.85f : 0.92f;
            matrices.translate(0.0f, -4.0f / 16.0f * (1 - shorter), 0.0f);
            matrices.scale(1.0f, shorter, 1.0f);
            this.model.renderRig(matrices, vertices, light, OverlayTexture.NO_OVERLAY, tint);
            matrices.popPose();
        }
        if (customFlag) {
            ResourceLocation flagTexture = CursedTides.id("textures/entity/flags/" + com.cursedtides.ship.FlagItem.Design.byIndex(ship.getFlag())
                    .name().toLowerCase() + ".png");
            VertexConsumer flagVertices = vertexConsumers.getBuffer(net.minecraft.client.renderer.RenderType.entityCutoutNoCull(flagTexture));
            this.model.renderFlag(matrices, flagVertices, light, OverlayTexture.NO_OVERLAY);
        }
        this.model.setCustomFlag(false);
        if (ship.getEmblem() > 0) {
            ResourceLocation emblemTexture = CursedTides.id("textures/entity/flags/" + com.cursedtides.ship.FlagItem.Design.byIndex(ship.getEmblem() - 1)
                    .name().toLowerCase() + ".png");
            this.model.renderEmblem(matrices, vertexConsumers.getBuffer(net.minecraft.client.renderer.RenderType.entityCutoutNoCull(emblemTexture)),
                    light, OverlayTexture.NO_OVERLAY);
        }
        var fig = ship.getFigurehead();
        if (fig != com.cursedtides.ship.FigureheadItem.Kind.NONE) {
            var item = com.cursedtides.ship.FigureheadItem.itemFor(fig);
            if (item != null) {
                matrices.pushPose();
                matrices.translate(0.0f, -4.0f / 16.0f, 37.5f / 16.0f); // just ahead of the bow tip
                matrices.scale(-1.0f, -1.0f, 1.0f);
                matrices.mulPose(Axis.XP.rotationDegrees(-20));
                matrices.scale(1.4f, 1.4f, 1.4f);
                net.minecraft.client.Minecraft.getInstance().getItemRenderer().renderStatic(new net.minecraft.world.item.ItemStack(item),
                        net.minecraft.world.item.ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY,
                        matrices, vertexConsumers, ship.level(), ship.getId());
                matrices.popPose();
            }
        }
        matrices.popPose();

        super.render(ship, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    /** The class's own hull is drawn at its true size (no class scaling); flags, emblem and figurehead as before. */
    private void renderWorkshop(ShipEntity ship, WorkshopShipModel hull, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
        matrices.scale(-1.0f, -1.0f, 1.0f);
        hull.setAngles(ship, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        boolean customFlag = ship.getFlag() > 0;
        hull.setCustomFlag(customFlag);
        var fig = ship.getFigurehead();
        var figItem = fig != com.cursedtides.ship.FigureheadItem.Kind.NONE ? com.cursedtides.ship.FigureheadItem.itemFor(fig) : null;
        VertexConsumer vertices = vertexConsumers.getBuffer(hull.renderType(getTexture(ship)));
        hull.renderHull(matrices, vertices, light, OverlayTexture.NO_OVERLAY, figItem != null);
        int tint = ship.getSailColor() < 0 ? 0xFFFFFFFF : 0xFF000000 | ship.getSailColor();
        hull.renderSails(matrices, vertices, light, OverlayTexture.NO_OVERLAY, tint);
        if (customFlag) {
            ResourceLocation flagTexture = CursedTides.id("textures/entity/flags/" + com.cursedtides.ship.FlagItem.Design.byIndex(ship.getFlag())
                    .name().toLowerCase() + ".png");
            hull.renderFlag(matrices, vertexConsumers.getBuffer(net.minecraft.client.renderer.RenderType.entityCutoutNoCull(flagTexture)), light, OverlayTexture.NO_OVERLAY);
        }
        hull.setCustomFlag(false);
        if (ship.getEmblem() > 0) {
            ResourceLocation emblemTexture = CursedTides.id("textures/entity/flags/" + com.cursedtides.ship.FlagItem.Design.byIndex(ship.getEmblem() - 1)
                    .name().toLowerCase() + ".png");
            hull.renderEmblem(matrices, vertexConsumers.getBuffer(net.minecraft.client.renderer.RenderType.entityCutoutNoCull(emblemTexture)),
                    light, OverlayTexture.NO_OVERLAY);
        }
        if (figItem != null) {
            float[] bow = BOWS.get(cls(ship));
            matrices.pushPose();
            matrices.translate(0.0f, bow[0] / 16.0f, bow[1] / 16.0f);
            matrices.scale(-1.0f, -1.0f, 1.0f);
            matrices.mulPose(Axis.XP.rotationDegrees(-20));
            matrices.scale(1.4f, 1.4f, 1.4f);
            net.minecraft.client.Minecraft.getInstance().getItemRenderer().renderStatic(new net.minecraft.world.item.ItemStack(figItem),
                    net.minecraft.world.item.ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY,
                    matrices, vertexConsumers, ship.level(), ship.getId());
            matrices.popPose();
        }
    }

    @Override
    public ResourceLocation getTexture(ShipEntity ship) {
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
