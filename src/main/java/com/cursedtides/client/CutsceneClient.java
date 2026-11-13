package com.cursedtides.client;

import com.cursedtides.quest.Cutscenes;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

/**
 * Plays a cutscene: a camera that isn't in the world (an armour stand only we know about) flies the shots while
 * a screen draws the black bars and narration. The screen also keeps your pirate still. Esc skips.
 */
public final class CutsceneClient {
    private CutsceneClient() {}

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(Cutscenes.Play.ID, (payload, context) -> context.client().execute(() -> {
            List<Cutscenes.Shot> shots = new ArrayList<>();
            for (String part : payload.script().split("\u001e")) {
                String[] two = part.split("\u001f", -1);
                String[] n = two[0].split(",");
                if (n.length < 10) continue;
                double[] v = new double[9];
                for (int i = 0; i < 9; i++) v[i] = Double.parseDouble(n[i]);
                shots.add(new Cutscenes.Shot(new Vec3(v[0], v[1], v[2]), new Vec3(v[3], v[4], v[5]), new Vec3(v[6], v[7], v[8]),
                        Integer.parseInt(n[9]), two.length > 1 ? two[1] : ""));
            }
            if (!shots.isEmpty()) context.client().gui.setScreen(new CutsceneScreen(payload.title(), shots));
        }));
    }

    public static class CutsceneScreen extends Screen {
        private final String title;
        private final List<Cutscenes.Shot> shots;
        private int shot, t;
        private ArmorStand cam;
        private CameraType savedPerspective;
        private boolean savedHud;

        CutsceneScreen(String title, List<Cutscenes.Shot> shots) {
            super(Component.literal(title));
            this.title = title;
            this.shots = shots;
        }

        @Override
        protected void init() {
            Minecraft mc = this.minecraft;
            if (cam != null || mc == null || mc.level == null) return;
            cam = new ArmorStand(mc.level, 0, 0, 0);
            savedPerspective = mc.options.getCameraType();
            savedHud = mc.gui.hud.isHidden();
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            if (!mc.gui.hud.isHidden()) mc.gui.hud.toggle();
            place(0f, true);
            mc.setCameraEntity(cam);
        }

        private void place(float ease, boolean snap) {
            Cutscenes.Shot s = shots.get(shot);
            double k = s.ticks() <= 0 ? 1 : Mth.clamp((t + ease) / s.ticks(), 0, 1);
            k = k * k * (3 - 2 * k); // ease in and out
            Vec3 pos = s.from().lerp(s.to(), k);
            double eye = cam.getEyeHeight();
            Vec3 to = s.look().subtract(pos);
            float yaw = (float) Math.toDegrees(Math.atan2(-to.x, to.z));
            float pitch = (float) -Math.toDegrees(Math.atan2(to.y, Math.hypot(to.x, to.z)));
            if (snap) {
                cam.xo = pos.x; cam.yo = pos.y - eye; cam.zo = pos.z;
                cam.yRotO = yaw; cam.xRotO = pitch;
            } else {
                cam.xo = cam.getX(); cam.yo = cam.getY(); cam.zo = cam.getZ();
                cam.yRotO = cam.getYRot(); cam.xRotO = cam.getXRot();
            }
            cam.setPos(pos.x, pos.y - eye, pos.z);
            cam.xOld = cam.xo; cam.yOld = cam.yo; cam.zOld = cam.zo;
            cam.setYRot(yaw);
            cam.setXRot(pitch);
            cam.setYHeadRot(yaw);
        }

        @Override
        public void tick() {
            if (cam == null) return;
            t++;
            if (t > shots.get(shot).ticks()) {
                shot++;
                t = 0;
                if (shot >= shots.size()) { onClose(); return; }
                place(0f, true);
                return;
            }
            place(0f, false);
        }

        @Override
        public void onClose() {
            Minecraft mc = this.minecraft;
            if (mc != null) {
                mc.setCameraEntity(mc.player);
                if (savedPerspective != null) mc.options.setCameraType(savedPerspective);
                if (mc.gui.hud.isHidden() != savedHud) mc.gui.hud.toggle();
            }
            super.onClose();
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
            int bar = this.height / 8;
            ctx.fill(0, 0, this.width, bar, 0xFF000000);
            ctx.fill(0, this.height - bar, this.width, this.height, 0xFF000000);
            if (shot < shots.size()) {
                Cutscenes.Shot s = shots.get(shot);
                float a = Mth.clamp(Math.min(t, s.ticks() - t) / 10f, 0, 1);
                int alpha = (int) (a * 255) << 24;
                if (shot == 0 && !title.isEmpty()) {
                    ctx.pose().pushMatrix();
                    ctx.pose().scale(2f, 2f);
                    ctx.centeredText(this.font, title, this.width / 4, bar / 2 - 4, 0xFFD24A | alpha);
                    ctx.pose().popMatrix();
                }
                if (!s.caption().isEmpty() && a > 0.05f) {
                    List<net.minecraft.util.FormattedCharSequence> lines = this.font.split(Component.literal(s.caption()), this.width - 80);
                    int y = this.height - bar + Math.max(4, (bar - lines.size() * 10) / 2);
                    for (var l : lines) {
                        ctx.centeredText(this.font, l, this.width / 2, y, 0xF0E6D2 | alpha);
                        y += 10;
                    }
                }
            }
            ctx.text(this.font, "Esc to skip", this.width - 64, this.height - 10, 0x80FFFFFF);
        }

        @Override
        public void extractBackground(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) { }

        @Override
        public boolean isPauseScreen() { return false; }
    }
}
