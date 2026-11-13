package com.cursedtides.client;

import com.cursedtides.CursedTides;
import com.cursedtides.entity.ShipEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import java.util.HashMap;
import java.util.Map;

/** Draws every ship with its class's own Mob Workshop hull, plus sails, flag, emblem and figurehead. */
public class ShipRenderer extends EntityRenderer<ShipEntity, ShipRenderer.State> {
    public static class State extends EntityRenderState {
        public String cls;
        public float yaw, rockZ, swellZ, swellX;
        public int sail, sailTint, flag, emblem;
        public boolean figureItem;
        public Identifier texture;
        public final ItemStackRenderState figure = new ItemStackRenderState();
    }

    /** Each class's own hull from the Mob Workshop, by class name. */
    private final Map<String, WorkshopShipModel> hulls = new HashMap<>();
    private final ItemModelResolver items;
    /** Where each class's bow carries its figurehead (model pixels: height, distance forward). */
    private static final Map<String, float[]> BOWS = Map.ofEntries(Map.entry("sloop", new float[]{-5f, 37.5f}), Map.entry("schooner", new float[]{-5f, 41.5f}), Map.entry("brigantine", new float[]{-7f, 44.5f}), Map.entry("brig", new float[]{-7f, 46.5f}), Map.entry("frigate", new float[]{-9f, 51.5f}), Map.entry("galleon", new float[]{-12f, 53.5f}), Map.entry("ship_of_the_line", new float[]{-16f, 59.5f}));

    public ShipRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 1.2f;
        this.items = context.getItemModelResolver();
        for (String cls : BOWS.keySet()) hulls.put(cls, new WorkshopShipModel(context.bakeLayer(WorkshopModels.layer("ship_" + cls))));
    }

    private static String cls(ShipEntity ship) {
        return ship.shipClass().name().toLowerCase();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ShipEntity ship, State s, float tickDelta) {
        super.extractRenderState(ship, s, tickDelta);
        s.cls = cls(ship);
        s.yaw = ship.getYRot(tickDelta);
        // Rock when hit, and roll gently with the swell
        float wobble = ship.getHurtTime() - tickDelta;
        float strength = Math.max(0.0f, ship.getDamage() - tickDelta);
        s.rockZ = wobble > 0.0f ? Mth.sin(wobble) * wobble * strength / 40.0f * ship.getHurtDir() : 0.0f;
        float swell = (ship.tickCount + tickDelta) * 0.05f;
        // Heavier seas in rain, heavier still in a storm
        float rough = 1.0f + 1.5f * ship.level().getRainLevel(tickDelta) + 2.0f * ship.level().getThunderLevel(tickDelta);
        s.swellZ = Mth.sin(swell * rough) * 1.5f * rough;
        s.swellX = Mth.sin(swell * 0.7f * rough) * 0.8f * rough;
        s.sail = ship.getSail();
        s.sailTint = ship.getSailColor() < 0 ? 0xFFFFFFFF : 0xFF000000 | ship.getSailColor();
        s.flag = ship.getFlag();
        s.emblem = ship.getEmblem();
        String look = switch (ship.getLook()) {
            case ShipEntity.LOOK_GHOST -> "ghost";
            case ShipEntity.LOOK_NAVY -> "navy";
            default -> "plain";
        };
        s.texture = CursedTides.id("textures/entity/workshop/ship_" + s.cls + "_" + look + ".png");
        var fig = ship.getFigurehead();
        var figItem = fig != com.cursedtides.ship.FigureheadItem.Kind.NONE ? com.cursedtides.ship.FigureheadItem.itemFor(fig) : null;
        s.figureItem = figItem != null;
        if (figItem != null) items.updateForNonLiving(s.figure, new ItemStack(figItem), ItemDisplayContext.FIXED, ship);
        else s.figure.clear();
    }

    private static Identifier flagTexture(int index) {
        return CursedTides.id("textures/entity/flags/" + com.cursedtides.ship.FlagItem.Design.byIndex(index).name().toLowerCase() + ".png");
    }

    private static PoseStack stack(PoseStack.Pose pose) {
        PoseStack ps = new PoseStack();
        ps.last().set(pose);
        return ps;
    }

    @Override
    public void submit(State s, PoseStack matrices, SubmitNodeCollector collector, CameraRenderState camera) {
        WorkshopShipModel hull = hulls.get(s.cls);
        if (hull != null) {
            matrices.pushPose();
            matrices.translate(0.0, 0.1, 0.0);
            matrices.rotate(Axis.YP.rotationDegrees(-s.yaw));
            if (s.rockZ != 0) matrices.rotate(Axis.ZP.rotationDegrees(s.rockZ));
            matrices.rotate(Axis.ZP.rotationDegrees(s.swellZ));
            matrices.rotate(Axis.XP.rotationDegrees(s.swellX));
            matrices.scale(-1.0f, -1.0f, 1.0f);
            final int light = s.lightCoords;
            final int sail = s.sail, tint = s.sailTint;
            final float age = s.ageInTicks;
            final boolean customFlag = s.flag > 0, figureItem = s.figureItem;
            collector.submitCustomGeometry(matrices, RenderTypes.entityCutout(s.texture), (pose, buffer) -> {
                PoseStack ps = stack(pose);
                hull.pose(sail, age);
                hull.setCustomFlag(customFlag);
                hull.renderHull(ps, buffer, light, OverlayTexture.NO_OVERLAY, figureItem);
                hull.renderSails(ps, buffer, light, OverlayTexture.NO_OVERLAY, tint);
                hull.setCustomFlag(false);
            });
            if (customFlag) {
                collector.submitCustomGeometry(matrices, RenderTypes.entityCutout(flagTexture(s.flag)), (pose, buffer) -> {
                    hull.pose(sail, age);
                    hull.renderFlag(stack(pose), buffer, light, OverlayTexture.NO_OVERLAY);
                });
            }
            if (s.emblem > 0) {
                collector.submitCustomGeometry(matrices, RenderTypes.entityCutout(flagTexture(s.emblem - 1)), (pose, buffer) ->
                        hull.renderEmblem(stack(pose), buffer, light, OverlayTexture.NO_OVERLAY));
            }
            if (figureItem) {
                float[] bow = BOWS.get(s.cls);
                matrices.pushPose();
                matrices.translate(0.0f, bow[0] / 16.0f, bow[1] / 16.0f);
                matrices.scale(-1.0f, -1.0f, 1.0f);
                matrices.rotate(Axis.XP.rotationDegrees(-20));
                matrices.scale(1.4f, 1.4f, 1.4f);
                s.figure.submit(matrices, collector, light, OverlayTexture.NO_OVERLAY, 0);
                matrices.popPose();
            }
            matrices.popPose();
        }
        super.submit(s, matrices, collector, camera);
    }
}
