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
    /** When we last hit or were hit (for the battle music). */
    public static long lastFight;
    private static Levels.Hud hud = new Levels.Hud(1, 0, 1, 100, 100);
    private static int targetId = -1;
    private static long targetUntil;

    private record Floater(Vec3d pos, String text, int color, long born) {}

    private static final List<Floater> FLOATERS = new ArrayList<>();
    private static com.cursedtides.combat.SkillBar.State skills = new com.cursedtides.combat.SkillBar.State(-1, 0, List.of(0, 0, 0, 0));
    private static long skillsAt;
    private static com.cursedtides.quest.Objectives.Target objective = new com.cursedtides.quest.Objectives.Target(false, 0, 0, "", List.of());

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
            lastFight = System.currentTimeMillis();
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
        ClientPlayNetworking.registerGlobalReceiver(com.cursedtides.combat.SkillBar.State.ID, (payload, context) ->
                context.client().execute(() -> { skills = payload; skillsAt = System.currentTimeMillis(); }));
        ClientPlayNetworking.registerGlobalReceiver(com.cursedtides.quest.Objectives.Target.ID, (payload, context) ->
                context.client().execute(() -> objective = payload));
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
        // At the helm: the nearest landfall, how far and which way (the sea is wide, and land is out of sight)
        if (p.getVehicle() instanceof com.cursedtides.entity.ShipEntity) {
            com.cursedtides.world.Archipelago.Island near = null;
            double best = Double.MAX_VALUE;
            for (com.cursedtides.world.Archipelago.Island is : com.cursedtides.world.Archipelago.ISLANDS) {
                if (com.cursedtides.world.Archipelago.HIDDEN.contains(is)) continue;
                double d = is.edgeDistance(p.getX(), p.getZ());
                if (d < best) { best = d; near = is; }
            }
            if (near != null && best > 40) {
                double dx = near.cx() - p.getX(), dz = near.cz() - p.getZ();
                double bearing = (Math.toDegrees(Math.atan2(dx, -dz)) + 360) % 360;
                String[] pts = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
                String dir = pts[(int) Math.round(bearing / 45) % 8];
                String line = near.name() + "  " + String.format("%,d", (int) best) + " blocks " + dir;
                ctx.drawCenteredTextWithShadow(font, line, sw / 2, sh - 54, 0xFFE8D9A8);
            }
        }
        radar(ctx, mc, p, sw);
        if (p.getVehicle() == null) skillBar(ctx, mc, sw, sh);
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

    // ------------------------------------------------------------------ skill bar

    private static final String[] KEYS = {"Z", "X", "C", "V"};

    /** Four skill slots to the right of the hotbar, for the weapon in hand. */
    private static void skillBar(DrawContext ctx, MinecraftClient mc, int sw, int sh) {
        if (skills.family() < 0) return;
        com.cursedtides.combat.SkillBar.Family f = com.cursedtides.combat.SkillBar.Family.values()[skills.family()];
        long since = System.currentTimeMillis() - skillsAt;
        for (int i = 0; i < 4; i++) {
            com.cursedtides.combat.SkillBar.Skill s = com.cursedtides.combat.SkillBar.Skill.of(f, i);
            if (s == null) continue;
            int x = sw / 2 + 98 + i * 22, y = sh - 22;
            ctx.fill(x, y, x + 20, y + 20, 0xC0000000);
            ctx.fill(x + 1, y + 1, x + 19, y + 19, 0xC03A2C1C);
            ctx.drawItem(new net.minecraft.item.ItemStack(s.icon.get()), x + 2, y + 2);
            boolean open = (skills.unlocked() & (1 << i)) != 0;
            if (!open) {
                ctx.fill(x + 1, y + 1, x + 19, y + 19, 0xB0000000);
                small(ctx, mc.textRenderer, "Lv" + s.level, x + 10, y + 7, 0xFFAAAAAA, true);
            } else {
                double left = skills.cooldowns().get(i) * 50.0 - since;
                if (left > 0) {
                    int h = (int) Math.round(18 * Math.min(1, left / (s.cooldownTicks * 50.0)));
                    ctx.fill(x + 1, y + 19 - h, x + 19, y + 19, 0xA0000000);
                    small(ctx, mc.textRenderer, String.valueOf((int) Math.ceil(left / 1000)), x + 10, y + 7, 0xFFFFFFFF, true);
                }
            }
            small(ctx, mc.textRenderer, KEYS[i], x + 3, y + 1, 0xFFFFD24A, true);
        }
    }

    // ------------------------------------------------------------------ radar

    private static final int RADAR_R = 46;
    private static final double RADAR_RANGE = 112;

    /** A north-up radar in the top-right corner: coast, foes, folk, ships, and where your story points. */
    private static void radar(DrawContext ctx, MinecraftClient mc, PlayerEntity p, int sw) {
        int cx = sw - RADAR_R - 8, cy = RADAR_R + 8, r = RADAR_R;
        double scale = r / RADAR_RANGE;
        for (int dy = -r - 2; dy <= r + 2; dy++) {
            int hw = (int) Math.sqrt(Math.max(0, (r + 2) * (r + 2) - dy * dy));
            ctx.fill(cx - hw, cy + dy, cx + hw + 1, cy + dy + 1, 0xFF2A1E10);
        }
        for (int dy = -r; dy <= r; dy++) {
            int hw = (int) Math.sqrt(r * r - dy * dy);
            ctx.fill(cx - hw, cy + dy, cx + hw + 1, cy + dy + 1, 0xE01C4A66);
        }
        // Land
        for (com.cursedtides.world.Archipelago.Island is : com.cursedtides.world.Archipelago.ISLANDS) {
            if (is.edgeDistance(p.getX(), p.getZ()) > RADAR_RANGE * 1.5) continue;
            landDisc(ctx, cx, cy, r, (is.cx() - p.getX()) * scale, (is.cz() - p.getZ()) * scale, is.radius() * 0.92 * scale);
            if (is.hasLobe()) landDisc(ctx, cx, cy, r, (is.lobeX() - p.getX()) * scale, (is.lobeZ() - p.getZ()) * scale, is.lobeR() * 0.92 * scale);
        }
        // Things nearby
        if (mc.world != null) {
            for (Entity e : mc.world.getEntities()) {
                if (e == p) continue;
                double dx = (e.getX() - p.getX()) * scale, dz = (e.getZ() - p.getZ()) * scale;
                if (dx * dx + dz * dz > (r - 2) * (r - 2)) continue;
                int color;
                int size = 1;
                if (e instanceof com.cursedtides.entity.ShipEntity) { color = 0xFF7FB2FF; size = 2; }
                else if (e instanceof PlayerEntity) color = 0xFFFFFFFF;
                else if (e instanceof com.cursedtides.entity.StoryNpcEntity) color = objective.marked().contains(e.getId()) ? 0xFFFFD24A : 0xFF6BE36B;
                else if (e instanceof LivingEntity le && Levels.isFoeClient(le)) color = 0xFFE04040;
                else continue;
                int ex = cx + (int) dx, ey = cy + (int) dz;
                ctx.fill(ex - size, ey - size, ex + size + 1, ey + size + 1, color);
            }
        }
        // The story's next step
        if (objective.has()) {
            double dx = (objective.x() - p.getX()) * scale, dz = (objective.z() - p.getZ()) * scale;
            double d = Math.sqrt(dx * dx + dz * dz);
            if (d > r - 4) { dx = dx / d * (r - 4); dz = dz / d * (r - 4); }
            int ox = cx + (int) dx, oy = cy + (int) dz;
            ctx.fill(ox - 3, oy - 3, ox + 4, oy + 4, 0xFF000000);
            ctx.fill(ox - 2, oy - 2, ox + 3, oy + 3, 0xFFFFD24A);
            double blocks = Math.hypot(objective.x() - p.getX(), objective.z() - p.getZ());
            String label = objective.label() + "  " + (blocks > 999 ? String.format("%.1fk", blocks / 1000) : String.valueOf((int) blocks));
            small(ctx, mc.textRenderer, label, cx, cy + r + 6, 0xFFFFE8A0, true);
        }
        // You, and which way you face
        double yaw = Math.toRadians(p.getYaw());
        double fx = -Math.sin(yaw), fz = Math.cos(yaw);
        for (int k = 0; k <= 5; k++) {
            int px = cx + (int) Math.round(fx * k), py = cy + (int) Math.round(fz * k);
            ctx.fill(px, py, px + 1, py + 1, 0xFFFFFFFF);
        }
        ctx.fill(cx - 1, cy - 1, cx + 2, cy + 2, 0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(mc.textRenderer, "N", cx, cy - r - 1, 0xFFE8D9A8);
    }

    private static void landDisc(DrawContext ctx, int cx, int cy, int r, double ox, double oz, double rr) {
        if (rr < 0.5) return;
        for (int dy = (int) Math.floor(oz - rr); dy <= (int) Math.ceil(oz + rr); dy++) {
            if (dy < -r || dy > r) continue;
            double inner = rr * rr - (dy - oz) * (dy - oz);
            if (inner < 0) continue;
            double half = Math.sqrt(inner);
            int hw = (int) Math.sqrt(r * r - dy * dy);
            int a = (int) Math.max(-hw, Math.round(ox - half)), b = (int) Math.min(hw, Math.round(ox + half));
            if (a <= b) ctx.fill(cx + a, cy + dy, cx + b + 1, cy + dy + 1, 0xFFC9B27A);
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
        MinecraftClient mc = MinecraftClient.getInstance();
        if (FLOATERS.isEmpty() && objective.marked().isEmpty()) return;
        if (mc.world != null) {
            synchronized (FLOATERS) {
                for (int id : objective.marked()) {
                    Entity e = mc.world.getEntityById(id);
                    if (e == null) continue;
                    double bob = Math.sin(System.currentTimeMillis() / 300.0) * 0.08;
                    // A marker shows for one frame (born now), so it never floats away or fades
                    FLOATERS.add(new Floater(e.getLerpedPos(context.tickCounter().getTickDelta(false)).add(0, e.getHeight() + 0.75 + bob, 0),
                            "!", 0xFFD24A, System.currentTimeMillis() + 100000));
                }
            }
        }
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
                boolean marker = f.born() > now;
                float age = marker ? 0.6f : (now - f.born()) / 1000f;
                if (marker) it.remove();
                else if (age > 1.2f) { it.remove(); continue; }
                matrices.push();
                matrices.translate(f.pos().x - cam.x, f.pos().y - cam.y + (marker ? 0 : age * 0.9), f.pos().z - cam.z);
                matrices.multiply(camera.getRotation());
                float s = marker ? 0.06f : 0.03f * (age < 0.15f ? 1.4f - age * 2.6f : 1f);
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
