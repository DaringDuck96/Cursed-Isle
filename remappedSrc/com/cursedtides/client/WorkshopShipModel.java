package com.cursedtides.client;

import com.cursedtides.entity.ShipEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;

/** One ship class from the Mob Workshop: its own hull, masts, a sail per mast, guns, flag and emblem board. */
public class WorkshopShipModel extends EntityModel<ShipEntity> {
    private final ModelPart root, flag, emblem, figurehead;
    private final List<ModelPart> sails = new ArrayList<>();
    private boolean customFlag;

    public WorkshopShipModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        for (int i = 1; root.hasChild("sail" + i); i++) sails.add(root.getChild("sail" + i));
        this.flag = root.getChild("flag");
        this.emblem = root.getChild("emblem");
        this.figurehead = root.getChild("figurehead");
    }

    public void setCustomFlag(boolean custom) { this.customFlag = custom; }

    @Override
    public void setAngles(ShipEntity ship, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        int setting = ship.getSail();
        float billow = (float) Math.sin((ship.tickCount + animationProgress) * 0.08) * 0.02f;
        for (ModelPart s : sails) {
            s.visible = setting > 0;
            s.yScale = setting == 1 ? 0.5f : 1.0f;
            s.xRot = setting > 0 ? -0.06f + billow : 0.0f;
        }
    }

    @Override
    public void renderToBuffer(PoseStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        root.render(matrices, vertices, light, overlay, color);
    }

    /** Everything but the canvas, the emblem board and (when an item stands in for it) the figurehead. */
    public void renderHull(PoseStack matrices, VertexConsumer vertices, int light, int overlay, boolean figureItem) {
        boolean[] was = new boolean[sails.size()];
        for (int i = 0; i < sails.size(); i++) { was[i] = sails.get(i).visible; sails.get(i).visible = false; }
        flag.visible = !customFlag;
        emblem.visible = false;
        figurehead.visible = !figureItem;
        root.render(matrices, vertices, light, overlay, 0xFFFFFFFF);
        for (int i = 0; i < sails.size(); i++) sails.get(i).visible = was[i];
        flag.visible = true;
        emblem.visible = true;
        figurehead.visible = true;
    }

    public void renderSails(PoseStack matrices, VertexConsumer vertices, int light, int overlay, int tint) {
        for (ModelPart s : sails) if (s.visible) s.render(matrices, vertices, light, overlay, tint);
    }

    public void renderFlag(PoseStack matrices, VertexConsumer vertices, int light, int overlay) {
        flag.render(matrices, vertices, light, overlay, 0xFFFFFFFF);
    }

    public void renderEmblem(PoseStack matrices, VertexConsumer vertices, int light, int overlay) {
        emblem.render(matrices, vertices, light, overlay, 0xFFFFFFFF);
    }
}
