package com.cursedtides.client;

import com.cursedtides.quest.Cutscenes;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

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
                shots.add(new Cutscenes.Shot(new Vec3d(v[0], v[1], v[2]), new Vec3d(v[3], v[4], v[5]), new Vec3d(v[6], v[7], v[8]),
                        Integer.parseInt(n[9]), two.length > 1 ? two[1] : ""));
            }
            if (!shots.isEmpty()) context.client().setScreen(new CutsceneScreen(payload.title(), shots));
        }));
    }

    public static class CutsceneScreen extends Screen {
        private final String title;
        private final List<Cutscenes.Shot> shots;
        private int shot, t;
        private ArmorStandEntity cam;
        private Perspective savedPerspective;
        private boolean savedHud;

        CutsceneScreen(String title, List<Cutscenes.Shot> shots) {
            super(Text.literal(title));
            this.title = title;
            this.shots = shots;
        }

        @Override
        protected void init() {
            MinecraftClient mc = this.client;
            if (cam != null || mc == null || mc.world == null) return;
            cam = new ArmorStandEntity(mc.world, 0, 0, 0);
            savedPerspective = mc.options.getPerspective();
            savedHud = mc.options.hudHidden;
            mc.options.setPerspective(Perspective.FIRST_PERSON);
            mc.options.hudHidden = true;
            place(0f, true);
            mc.setCameraEntity(cam);
        }

        private void place(float ease, boolean snap) {
            Cutscenes.Shot s = shots.get(shot);
            double k = s.ticks() <= 0 ? 1 : MathHelper.clamp((t + ease) / s.ticks(), 0, 1);
            k = k * k * (3 - 2 * k); // ease in and out
            Vec3d pos = s.from().lerp(s.to(), k);
            double eye = cam.getStandingEyeHeight();
            Vec3d to = s.look().subtract(pos);
            float yaw = (float) Math.toDegrees(Math.atan2(-to.x, to.z));
            float pitch = (float) -Math.toDegrees(Math.atan2(to.y, Math.hypot(to.x, to.z)));
            if (snap) {
                cam.prevX = pos.x; cam.prevY = pos.y - eye; cam.prevZ = pos.z;
                cam.prevYaw = yaw; cam.prevPitch = pitch;
            } else {
                cam.prevX = cam.getX(); cam.prevY = cam.getY(); cam.prevZ = cam.getZ();
                cam.prevYaw = cam.getYaw(); cam.prevPitch = cam.getPitch();
            }
            cam.setPosition(pos.x, pos.y - eye, pos.z);
            cam.lastRenderX = cam.prevX; cam.lastRenderY = cam.prevY; cam.lastRenderZ = cam.prevZ;
            cam.setYaw(yaw);
            cam.setPitch(pitch);
            cam.setHeadYaw(yaw);
        }

        @Override
        public void tick() {
            if (cam == null) return;
            t++;
            if (t > shots.get(shot).ticks()) {
                shot++;
                t = 0;
                if (shot >= shots.size()) { close(); return; }
                place(0f, true);
                return;
            }
            place(0f, false);
        }

        @Override
        public void close() {
            MinecraftClient mc = this.client;
            if (mc != null) {
                mc.setCameraEntity(mc.player);
                if (savedPerspective != null) mc.options.setPerspective(savedPerspective);
                mc.options.hudHidden = savedHud;
            }
            super.close();
        }

        @Override
        public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
            int bar = this.height / 8;
            ctx.fill(0, 0, this.width, bar, 0xFF000000);
            ctx.fill(0, this.height - bar, this.width, this.height, 0xFF000000);
            if (shot < shots.size()) {
                Cutscenes.Shot s = shots.get(shot);
                float a = MathHelper.clamp(Math.min(t, s.ticks() - t) / 10f, 0, 1);
                int alpha = (int) (a * 255) << 24;
                if (shot == 0 && !title.isEmpty()) {
                    ctx.getMatrices().push();
                    ctx.getMatrices().scale(2f, 2f, 1f);
                    ctx.drawCenteredTextWithShadow(this.textRenderer, title, this.width / 4, bar / 2 - 4, 0xFFD24A | alpha);
                    ctx.getMatrices().pop();
                }
                if (!s.caption().isEmpty() && a > 0.05f) {
                    List<net.minecraft.text.OrderedText> lines = this.textRenderer.wrapLines(Text.literal(s.caption()), this.width - 80);
                    int y = this.height - bar + Math.max(4, (bar - lines.size() * 10) / 2);
                    for (var l : lines) {
                        ctx.drawCenteredTextWithShadow(this.textRenderer, l, this.width / 2, y, 0xF0E6D2 | alpha);
                        y += 10;
                    }
                }
            }
            ctx.drawTextWithShadow(this.textRenderer, "Esc to skip", this.width - 64, this.height - 10, 0x80FFFFFF);
        }

        @Override
        public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) { }

        @Override
        public boolean shouldPause() { return false; }
    }
}
