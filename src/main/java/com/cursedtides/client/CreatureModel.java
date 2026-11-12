package com.cursedtides.client;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * One model class for every wild creature. Parts are animated by name: legs and arms swing,
 * tails sway, snake segments ripple, claws pinch, jaws open, and the head follows the gaze.
 */
public class CreatureModel<T extends LivingEntity> extends EntityModel<T> {
    private final ModelPart root;
    private final Map<String, ModelPart> parts = new HashMap<>();

    public CreatureModel(ModelPart root, String[] paths) {
        this.root = root;
        for (String path : paths) {
            ModelPart part = root;
            for (String step : path.split("/")) {
                part = part.getChild(step);
            }
            parts.put(path.substring(path.lastIndexOf('/') + 1), part);
        }
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch) {
        root.traverse().forEach(ModelPart::resetTransform);
        float swing = limbAngle * 0.6662f;
        for (Map.Entry<String, ModelPart> e : parts.entrySet()) {
            String name = e.getKey();
            ModelPart p = e.getValue();
            switch (name) {
                case "head" -> {
                    p.yaw += headYaw * MathHelper.RADIANS_PER_DEGREE * 0.7f;
                    p.pitch += headPitch * MathHelper.RADIANS_PER_DEGREE * 0.7f;
                }
                case "leg_fl", "leg_br", "arm_r", "leg_mr" -> p.pitch += MathHelper.cos(swing) * 1.2f * limbDistance;
                case "leg_fr", "leg_bl", "arm_l", "leg_ml" -> p.pitch += MathHelper.cos(swing + MathHelper.PI) * 1.2f * limbDistance;
                case "jaw" -> p.pitch += 0.08f + Math.max(0, MathHelper.sin(age * 0.06f)) * 0.25f * (this.handSwingProgress > 0 ? 3 : 1);
                case "claw_l" -> p.yaw += MathHelper.sin(age * 0.2f) * 0.2f;
                case "claw_r" -> p.yaw -= MathHelper.sin(age * 0.2f) * 0.2f;
                case "fin_l", "fin_r" -> p.roll += MathHelper.sin(age * 0.15f) * 0.12f * (name.endsWith("l") ? 1 : -1);
                default -> {
                    if (name.startsWith("tail")) {
                        int k = name.length() > 4 && Character.isDigit(name.charAt(name.length() - 1)) ? name.charAt(name.length() - 1) - '0' : 3;
                        p.yaw += MathHelper.sin(age * 0.18f - k * 0.7f) * (0.18f + limbDistance * 0.4f);
                    } else if (name.startsWith("tent")) {
                        int k = name.charAt(4) - '0';
                        p.pitch += MathHelper.sin(age * 0.09f - k * 0.6f) * 0.16f;
                        p.roll += MathHelper.cos(age * 0.07f - k * 0.5f) * 0.14f;
                    } else if (name.startsWith("seg")) {
                        int k = name.charAt(3) - '0';
                        p.yaw += MathHelper.sin(age * 0.25f + limbAngle * 0.8f - k * 0.9f) * 0.42f;
                    }
                }
            }
        }
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        root.render(matrices, vertices, light, overlay, color);
    }
}
