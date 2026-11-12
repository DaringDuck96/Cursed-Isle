package com.cursedtides.client;

import com.cursedtides.combat.Levels;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The combat display: a health bar with numbers where the hearts were, a voodoo bar beneath it, your notoriety
 * level beside it, the health of whatever you're fighting in the corner, and damage numbers that float up
 * off whatever gets hit.
 */
public final class CombatHud {
    private CombatHud() {}

    private static boolean active;
    private static Levels.Hud hud = new Levels.Hud(1, 0, 1, 100, 100);
    private static int targetId = -1;
    private static long targetUntil;

    private record Floater(Vec3d pos, String text, int color, long born) {}

    private static final List<Floater> FLOATERS = new ArrayList<>();

    /** Only once the server has told us it runs Cursed Tides (otherwise hearts stay). */
    public static boolean enabled() {
        return active;
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(Levels.Hud.ID, (payload, context) -> context.client().execute(() -> {
            hud = payload;
            active = true;
        }));
        ClientPlayNetworking.registerGlobalReceiver(Levels.DamageNumber.ID, (payload, context) -> context.client().execute(() -> {
            MinecraftClient mc = context.client();
            if (mc.world == null) return;
            Entity e = mc.world.getEntityById(payload.entity());
            if (e == null) return;
            if (payload.kind() != 2) {
                targetId = e.getId();
                targetUntil = System.currentTimeMillis() + 6000;
            }
            double jx = (Math.random() - 0.5) * 0.6, jz = (Math.random() - 0.5) * 0.6;
            Vec3d at = e.getPos().add(jx, e.getHeight() + 0.25, jz);
            String text = switch (payload.kind()) {
                case 1 -> payload.amount() + "!";
                case 2 -> "-" + payload.amount();
                default -> String.valueOf(payload.amount());
            };
            int color = switch (payload.kind()) {
                case 1 -> 0xFFAA00;
                case 2 -> 0xFF5555;
                default -> 0xFFFFFF;
            };
            synchronized (FLOATERS) {
                FLOATERS.add(new Floater(at, text, color, System.currentTimeMillis()));
                if (FLOATERS.size() > 60) FLOATERS.remove(0);
            }
        }));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            active = false;
            synchronized (FLOATERS) { FLOATERS.clear(); }
        });
        HudRenderCallback.EVENT.register(CombatHud::renderHud);
        WorldRenderEvents.AFTER_ENTITIES.register(CombatHud::renderWorld);
    }

    // ------------------------------------------------------------------ screen

    private static void renderHud(DrawContext ctx, RenderTickCounter tick) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!active || mc.player == null || mc.options.hudHidden) return;
        TextRenderer font = mc.textRenderer;
        int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
        PlayerEntity p = mc.player;
        if (mc.interactionManager != null && mc.interactionManager.hasStatusBars()) {
            int x = sw / 2 - 91, y = sh - 40, w = 81;
            float frac = Math.max(0, Math.min(1, p.getHealth() / p.getMaxHealth()));
            ctx.fill(x - 1, y - 1, x + w + 1, y + 8, 0xC0000000);
            int col = frac > 0.5f ? 0xFF2E9E3A : frac > 0.25f ? 0xFFD4A21E : 0xFFC0261E;
            ctx.fill(x, y, x + Math.round(w * frac), y + 7, col);
            String hp = Math.round(p.getHealth() * Levels.HP_SCALE) + " / " + Math.round(p.getMaxHealth() * Levels.HP_SCALE);
            small(ctx, font, hp, x + w / 2f, y + 1.5f, 0xFFFFFFFF, true);
            // Voodoo, under the health
            float vf = hud.maxVoodoo() <= 0 ? 0 : Math.min(1f, hud.voodoo() / (float) hud.maxVoodoo());
            ctx.fill(x - 1, y + 8, x + w + 1, y + 11, 0xC0000000);
            ctx.fill(x, y + 8, x + Math.round(w * vf), y + 10, 0xFF3FB8D8);
            // Notoriety badge, to the left
            int bx = x - 26;
            ctx.fill(bx, y - 1, bx + 23, y + 11, 0xC0000000);
            String lv = String.valueOf(hud.notoriety());
            ctx.drawText(font, lv, bx + 12 - font.getWidth(lv) / 2, y, 0xFFFFD24A, true);
            float rf = Math.min(1f, hud.rep() / (float) hud.repNeeded());
            ctx.fill(bx + 1, y + 9, bx + 1 + Math.round(21 * rf), y + 10, 0xFFFFD24A);
        }
        // The foe you're fighting
        Entity target = mc.targetedEntity instanceof LivingEntity le && Levels.isFoeClient(le) ? le : null;
        if (target == null && targetId >= 0 && System.currentTimeMillis() < targetUntil && mc.world != null) {
            Entity e = mc.world.getEntityById(targetId);
            if (e instanceof LivingEntity le && le.isAlive()) target = le;
        }
        if (target instanceof LivingEntity le) {
            int x = 6, y = 6, w = 120;
            Text name = le.getDisplayName();
            ctx.fill(x - 2, y - 2, x + w + 2, y + 21, 0x90000000);
            ctx.drawText(font, name, x, y, 0xFFFFFFFF, true);
            float frac = Math.max(0, Math.min(1, le.getHealth() / le.getMaxHealth()));
            ctx.fill(x, y + 11, x + w, y + 18, 0xFF3A1010);
            ctx.fill(x, y + 11, x + Math.round(w * frac), y + 18, 0xFFC0261E);
            String hp = Math.round(le.getHealth() * Levels.HP_SCALE) + " / " + Math.round(le.getMaxHealth() * Levels.HP_SCALE);
            small(ctx, font, hp, x + w / 2f, y + 12.5f, 0xFFFFFFFF, true);
        }
    }

    private static void small(DrawContext ctx, TextRenderer font, String s, float cx, float y, int color, boolean shadow) {
        MatrixStack m = ctx.getMatrices();
        m.push();
        m.translate(cx, y, 0);
        m.scale(0.6f, 0.6f, 1f);
        ctx.drawText(font, s, -font.getWidth(s) / 2, 0, color, shadow);
        m.pop();
    }

    // ------------------------------------------------------------------ world

    private static void renderWorld(WorldRenderContext context) {
        if (FLOATERS.isEmpty()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        Camera camera = context.camera();
        Vec3d cam = camera.getPos();
        MatrixStack matrices = context.matrixStack() != null ? context.matrixStack() : new MatrixStack();
        VertexConsumerProvider.Immediate consumers = mc.getBufferBuilders().getEntityVertexConsumers();
        long now = System.currentTimeMillis();
        TextRenderer font = mc.textRenderer;
        synchronized (FLOATERS) {
            Iterator<Floater> it = FLOATERS.iterator();
            while (it.hasNext()) {
                Floater f = it.next();
                float age = (now - f.born()) / 1000f;
                if (age > 1.2f) { it.remove(); continue; }
                matrices.push();
                matrices.translate(f.pos().x - cam.x, f.pos().y - cam.y + age * 0.9, f.pos().z - cam.z);
                matrices.multiply(camera.getRotation());
                float s = 0.03f * (age < 0.15f ? 1.4f - age * 2.6f : 1f);
                matrices.scale(s, -s, s);
                int alpha = (int) (255 * Math.max(0.1f, Math.min(1f, (1.2f - age) / 0.5f)));
                Matrix4f m = matrices.peek().getPositionMatrix();
                font.draw(f.text(), -font.getWidth(f.text()) / 2f, 0, (alpha << 24) | f.color(), true, m, consumers,
                        TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
                matrices.pop();
            }
        }
        consumers.draw();
    }
}
