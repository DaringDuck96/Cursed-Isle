package com.cursedtides.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

/**
 * One model class for every wild creature. Parts are animated by name: legs and arms swing,
 * tails sway, snake segments ripple, claws pinch, jaws open, and the head follows the gaze.
 */
public class CreatureModel extends EntityModel<CreatureRenderer.State> {
    private final Map<String, ModelPart> parts = new HashMap<>();

    public CreatureModel(ModelPart root, String[] paths) {
        super(root);
        for (String path : paths) {
            ModelPart part = root;
            for (String step : path.split("/")) {
                part = part.getChild(step);
            }
            parts.put(path.substring(path.lastIndexOf('/') + 1), part);
        }
    }

    @Override
    public void setupAnim(CreatureRenderer.State state) {
        super.setupAnim(state);
        float limbAngle = state.walkAnimationPos, limbDistance = state.walkAnimationSpeed, age = state.ageInTicks;
        float headYaw = state.yRot, headPitch = state.xRot;
        float swing = limbAngle * 0.6662f;
        for (Map.Entry<String, ModelPart> e : parts.entrySet()) {
            String name = e.getKey();
            ModelPart p = e.getValue();
            switch (name) {
                case "head" -> {
                    p.yRot += headYaw * Mth.DEG_TO_RAD * 0.7f;
                    p.xRot += headPitch * Mth.DEG_TO_RAD * 0.7f;
                }
                case "leg_fl", "leg_br", "arm_r", "leg_mr" -> p.xRot += Mth.cos(swing) * 1.2f * limbDistance;
                case "leg_fr", "leg_bl", "arm_l", "leg_ml" -> p.xRot += Mth.cos(swing + Mth.PI) * 1.2f * limbDistance;
                case "jaw" -> p.xRot += 0.08f + Math.max(0, Mth.sin(age * 0.06f)) * 0.25f * (state.attackTime > 0 ? 3 : 1);
                case "claw_l" -> p.yRot += Mth.sin(age * 0.2f) * 0.2f;
                case "claw_r" -> p.yRot -= Mth.sin(age * 0.2f) * 0.2f;
                case "fin_l", "fin_r" -> p.zRot += Mth.sin(age * 0.15f) * 0.12f * (name.endsWith("l") ? 1 : -1);
                // Wings beat hard and fast; a flytrap's leaves and a wasp's wings are told apart by name
                case "wing_l" -> p.zRot += Mth.sin(age * 1.3f) * 0.75f;
                case "wing_r" -> p.zRot -= Mth.sin(age * 1.3f) * 0.75f;
                default -> {
                    if (name.startsWith("tail")) {
                        int k = name.length() > 4 && Character.isDigit(name.charAt(name.length() - 1)) ? name.charAt(name.length() - 1) - '0' : 3;
                        p.yRot += Mth.sin(age * 0.18f - k * 0.7f) * (0.18f + limbDistance * 0.4f);
                    } else if (name.startsWith("tent")) {
                        int k = name.charAt(4) - '0';
                        p.xRot += Mth.sin(age * 0.09f - k * 0.6f) * 0.16f;
                        p.zRot += Mth.cos(age * 0.07f - k * 0.5f) * 0.14f;
                    } else if (name.startsWith("seg")) {
                        int k = name.charAt(3) - '0';
                        p.yRot += Mth.sin(age * 0.25f + limbAngle * 0.8f - k * 0.9f) * 0.42f;
                    }
                }
            }
        }
    }
}
