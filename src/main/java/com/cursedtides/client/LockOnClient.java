package com.cursedtides.client;

import com.cursedtides.combat.Levels;
import com.cursedtides.combat.LockOn;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
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

    public static void cycle(Minecraft mc, boolean release) {
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null) return;
        if (release) { set(-1); return; }
        Vec3 eye = p.getEyePosition(), look = p.getViewVector(1f);
        List<LivingEntity> foes = new ArrayList<>();
        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof LivingEntity l) || l == p || !l.isAlive() || !Levels.isFoeClient(l)) continue;
            if (l.distanceToSqr(p) > 32 * 32 || !p.hasLineOfSight(l)) continue;
            Vec3 to = l.getBoundingBox().getCenter().subtract(eye).normalize();
            if (to.dot(look) < Math.cos(Math.toRadians(70))) continue;
            foes.add(l);
        }
        if (foes.isEmpty()) {
            p.sendOverlayMessage(net.minecraft.network.chat.Component.literal("No foe to lock on to.").withStyle(ChatFormatting.GRAY));
            set(-1);
            return;
        }
        // Sorted left to right across the view, so R walks along the line of foes
        double yaw = Math.toRadians(p.getYRot());
        foes.sort(Comparator.comparingDouble(l -> {
            Vec3 to = l.position().subtract(p.position());
            return Math.atan2(to.x * Math.cos(yaw) + to.z * Math.sin(yaw), -to.x * Math.sin(yaw) + to.z * Math.cos(yaw));
        }));
        int current = -1;
        for (int i = 0; i < foes.size(); i++) if (foes.get(i).getId() == locked) current = i;
        LivingEntity next;
        if (current < 0) {
            next = foes.stream().max(Comparator.comparingDouble(l -> l.getBoundingBox().getCenter().subtract(eye).normalize().dot(look))).orElse(foes.get(0));
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

    public static LivingEntity target(Minecraft mc) {
        if (locked < 0 || mc.level == null || mc.player == null) return null;
        Entity e = mc.level.getEntity(locked);
        if (e instanceof LivingEntity l && l.isAlive() && l.distanceToSqr(mc.player) < 40 * 40) return l;
        set(-1);
        return null;
    }

    /** Each tick: ease the view toward the locked foe. */
    public static void tick(Minecraft mc) {
        LivingEntity t = target(mc);
        LocalPlayer p = mc.player;
        if (t == null || p == null || mc.gui.screen() != null || p.isPassenger()) return;
        Vec3 to = t.getBoundingBox().getCenter().subtract(p.getEyePosition());
        float wantYaw = (float) Math.toDegrees(Math.atan2(-to.x, to.z));
        float wantPitch = (float) -Math.toDegrees(Math.atan2(to.y, Math.hypot(to.x, to.z)));
        float dy = Mth.wrapDegrees(wantYaw - p.getYRot());
        float dp = wantPitch - p.getXRot();
        p.setYRot(p.getYRot() + dy * 0.25f);
        p.setXRot(Mth.clamp(p.getXRot() + dp * 0.2f, -90, 90));
    }

    /** A frame under the crosshair: who's locked, their level and health. */
    public static void render(GuiGraphicsExtractor ctx, Minecraft mc, int sw, int sh) {
        LivingEntity t = target(mc);
        if (t == null) return;
        int x = sw / 2, y = sh / 2 + 14;
        String name = t.getDisplayName().getString();
        int w = Math.max(80, mc.font.width(name) + 10);
        ctx.fill(x - w / 2, y, x + w / 2, y + 18, 0x90000000);
        ctx.centeredText(mc.font, "» " + name + " «", x, y + 2, 0xFFFF5A3A);
        float frac = Mth.clamp(t.getHealth() / t.getMaxHealth(), 0, 1);
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
