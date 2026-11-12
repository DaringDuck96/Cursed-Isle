package com.cursedtides.client;

import com.cursedtides.combat.Levels;
import com.cursedtides.combat.LockOn;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The client half of lock-on: R (on foot) locks the foe nearest your crosshair, R again cycles to the next one
 * across your view, sneak + R lets go. While locked, your view eases round to keep it in front of you.
 */
public final class LockOnClient {
    private LockOnClient() {}

    private static int locked = -1;

    public static void cycle(MinecraftClient mc, boolean release) {
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.world == null) return;
        if (release) { set(-1); return; }
        Vec3d eye = p.getEyePos(), look = p.getRotationVec(1f);
        List<LivingEntity> foes = new ArrayList<>();
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity l) || l == p || !l.isAlive() || !Levels.isFoeClient(l)) continue;
            if (l.squaredDistanceTo(p) > 32 * 32 || !p.canSee(l)) continue;
            Vec3d to = l.getBoundingBox().getCenter().subtract(eye).normalize();
            if (to.dotProduct(look) < Math.cos(Math.toRadians(70))) continue;
            foes.add(l);
        }
        if (foes.isEmpty()) {
            p.sendMessage(net.minecraft.text.Text.literal("No foe to lock on to.").formatted(Formatting.GRAY), true);
            set(-1);
            return;
        }
        // Sorted left to right across the view, so R walks along the line of foes
        double yaw = Math.toRadians(p.getYaw());
        foes.sort(Comparator.comparingDouble(l -> {
            Vec3d to = l.getPos().subtract(p.getPos());
            return Math.atan2(to.x * Math.cos(yaw) + to.z * Math.sin(yaw), -to.x * Math.sin(yaw) + to.z * Math.cos(yaw));
        }));
        int current = -1;
        for (int i = 0; i < foes.size(); i++) if (foes.get(i).getId() == locked) current = i;
        LivingEntity next;
        if (current < 0) {
            next = foes.stream().max(Comparator.comparingDouble(l -> l.getBoundingBox().getCenter().subtract(eye).normalize().dotProduct(look))).orElse(foes.get(0));
        } else {
            next = foes.get((current + 1) % foes.size());
        }
        set(next.getId());
    }

    private static void set(int id) {
        if (id == locked) return;
        locked = id;
        ClientPlayNetworking.send(new LockOn.Lock(id));
    }

    public static LivingEntity target(MinecraftClient mc) {
        if (locked < 0 || mc.world == null || mc.player == null) return null;
        Entity e = mc.world.getEntityById(locked);
        if (e instanceof LivingEntity l && l.isAlive() && l.squaredDistanceTo(mc.player) < 40 * 40) return l;
        set(-1);
        return null;
    }

    /** Each tick: ease the view toward the locked foe. */
    public static void tick(MinecraftClient mc) {
        LivingEntity t = target(mc);
        ClientPlayerEntity p = mc.player;
        if (t == null || p == null || mc.currentScreen != null || p.hasVehicle()) return;
        Vec3d to = t.getBoundingBox().getCenter().subtract(p.getEyePos());
        float wantYaw = (float) Math.toDegrees(Math.atan2(-to.x, to.z));
        float wantPitch = (float) -Math.toDegrees(Math.atan2(to.y, Math.hypot(to.x, to.z)));
        float dy = MathHelper.wrapDegrees(wantYaw - p.getYaw());
        float dp = wantPitch - p.getPitch();
        p.setYaw(p.getYaw() + dy * 0.25f);
        p.setPitch(MathHelper.clamp(p.getPitch() + dp * 0.2f, -90, 90));
    }

    /** A frame under the crosshair: who's locked, their level and health. */
    public static void render(DrawContext ctx, MinecraftClient mc, int sw, int sh) {
        LivingEntity t = target(mc);
        if (t == null) return;
        int x = sw / 2, y = sh / 2 + 14;
        String name = t.getDisplayName().getString();
        int w = Math.max(80, mc.textRenderer.getWidth(name) + 10);
        ctx.fill(x - w / 2, y, x + w / 2, y + 18, 0x90000000);
        ctx.drawCenteredTextWithShadow(mc.textRenderer, "» " + name + " «", x, y + 2, 0xFFFF5A3A);
        float frac = MathHelper.clamp(t.getHealth() / t.getMaxHealth(), 0, 1);
        ctx.fill(x - w / 2 + 4, y + 13, x + w / 2 - 4, y + 16, 0xFF3A0A0A);
        ctx.fill(x - w / 2 + 4, y + 13, x - w / 2 + 4 + (int) ((w - 8) * frac), y + 16, 0xFFD83A2A);
        // Corner brackets round the crosshair
        int c = 0xFFFF5A3A;
        ctx.fill(x - 9, sh / 2 - 9, x - 5, sh / 2 - 8, c); ctx.fill(x - 9, sh / 2 - 9, x - 8, sh / 2 - 5, c);
        ctx.fill(x + 5, sh / 2 - 9, x + 9, sh / 2 - 8, c); ctx.fill(x + 8, sh / 2 - 9, x + 9, sh / 2 - 5, c);
        ctx.fill(x - 9, sh / 2 + 8, x - 5, sh / 2 + 9, c); ctx.fill(x - 9, sh / 2 + 5, x - 8, sh / 2 + 9, c);
        ctx.fill(x + 5, sh / 2 + 8, x + 9, sh / 2 + 9, c); ctx.fill(x + 8, sh / 2 + 5, x + 9, sh / 2 + 9, c);
    }
}
