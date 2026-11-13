package com.cursedtides.client;

import com.cursedtides.dungeon.Dungeon;
import com.cursedtides.dungeon.Dungeons;
import com.cursedtides.ui.UiPayloads;
import com.cursedtides.world.Archipelago;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * A sea chart of the Shattered Reach drawn from the island list: discovered islands are
 * named and coloured, the rest are faint question marks. Shows you, your treasure X's
 * and the dungeon entrances you know of.
 */
public class ChartScreen extends Screen {
    private static final int SEA = 0xFF2A5D7A;
    private static final int SEA_EDGE = 0xFF6B4A2B;

    private final UiPayloads.Chart chart;
    private int minX, minZ, maxX, maxZ;

    public ChartScreen(UiPayloads.Chart chart) {
        super(Component.literal("Sea Chart"));
        this.chart = chart;
        minX = Integer.MAX_VALUE; minZ = Integer.MAX_VALUE; maxX = Integer.MIN_VALUE; maxZ = Integer.MIN_VALUE;
        for (Archipelago.Island is : Archipelago.ISLANDS) {
            int e = is.extent() + 200;
            minX = Math.min(minX, is.cx() - e);
            maxX = Math.max(maxX, is.cx() + e);
            minZ = Math.min(minZ, is.cz() - e);
            maxZ = Math.max(maxZ, is.cz() + e);
        }
    }

    private static void disc(GuiGraphics context, int cx, int cz, int r, int color) {
        for (int dz = -r; dz <= r; dz++) {
            int half = (int) Math.sqrt(r * r - dz * dz);
            context.fill(cx - half, cz + dz, cx + half + 1, cz + dz + 1, color);
        }
    }

    private int size() { return Math.min(this.width - 40, this.height - 60); }
    private int left() { return (this.width - size()) / 2; }
    private int top() { return 36; }
    private double scale() { return size() / (double) Math.max(maxX - minX, maxZ - minZ); }
    private int sx(double x) { return left() + (int) ((x - minX) * scale()); }
    private int sz(double z) { return top() + (int) ((z - minZ) * scale()); }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        int l = left(), t = top(), s = size();
        context.drawCenteredString(this.font, Component.literal("§6§lThe Shattered Reach"), this.width / 2, 20, 0xFFFFFF);
        context.fill(l - 3, t - 3, l + s + 3, t + s + 3, SEA_EDGE);
        context.fill(l, t, l + s, t + s, SEA);

        if (!chart.archipelago()) {
            context.drawCenteredString(this.font,
                    Component.literal("This world isn't the Cursed Archipelago, so there's no chart of it."), this.width / 2, t + s / 2, 0xFFFFFF);
            super.render(context, mouseX, mouseY, delta);
            return;
        }

        for (int i = 0; i < Archipelago.ISLANDS.size(); i++) {
            Archipelago.Island is = Archipelago.ISLANDS.get(i);
            boolean known = (chart.islands() & (1 << i)) != 0;
            if (!known && Archipelago.isHidden(i)) continue; // off the charts until someone finds it
            int color = known ? colorFor(is.style()) : 0x553C5A6E;
            int r = Math.max(2, (int) (is.radius() * 0.85 * scale()));
            int cx = sx(is.cx()), cz = sz(is.cz());
            disc(context, cx, cz, r, color);
            if (is.hasLobe()) disc(context, sx(is.lobeX()), sz(is.lobeZ()), Math.max(2, (int) (is.lobeR() * 0.85 * scale())), color);
            String label = known ? is.name() : "?";
            context.drawCenteredString(this.font, Component.literal(label), cx, cz + r + 3, known ? 0xFFF2D9 : 0x9FB3C8);
        }

        // Dungeon entrances on islands you've found
        for (Dungeon d : Dungeons.ALL) {
            int island = Archipelago.islandIndexAt(d.ex, d.ez);
            if (island < 0 || (chart.islands() & (1 << island)) == 0) continue;
            boolean cleared = (chart.dungeons() & (1L << d.index)) != 0;
            int x = sx(d.ex), z = sz(d.ez);
            context.fill(x - 1, z - 1, x + 2, z + 2, cleared ? 0xFF5BC85B : 0xFF1A1A1A);
        }

        // Whirlpools and fog banks
        for (var w : com.cursedtides.explore.SeaHazards.WHIRLPOOLS) {
            context.drawCenteredString(this.font, Component.literal("§3@"), sx(w.x()), sz(w.z()) - 4, 0xFFFFFF);
        }
        for (var f : com.cursedtides.explore.SeaHazards.FOG) {
            int r = Math.max(3, (int) (f.radius() * scale())), fx = sx(f.x()), fz = sz(f.z());
            context.fill(fx - r, fz - r / 3, fx + r, fz + r / 3, 0x30FFFFFF);
        }

        // Treasure X's
        for (int i = 0; i + 1 < chart.marks().size(); i += 2) {
            int x = sx(chart.marks().get(i)), z = sz(chart.marks().get(i + 1));
            context.drawCenteredString(this.font, Component.literal("§c§lX"), x, z - 4, 0xFFFFFF);
        }

        // You
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            int px = sx(client.player.getX()), pz = sz(client.player.getZ());
            context.fill(px - 2, pz - 2, px + 3, pz + 3, 0xFFFFFFFF);
            context.fill(px - 1, pz - 1, px + 2, pz + 2, 0xFFE03C3C);
        }

        context.drawCenteredString(this.font,
                Component.literal("§fwhite/red: you   §8■§f dungeon   §a■§f vault opened   §cX§f treasure   §3@§f whirlpool"),
                this.width / 2, t + s + 8, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    private static int colorFor(Archipelago.Style style) {
        return switch (style) {
            case SANDY -> 0xFFE6D59A;
            case TOWN -> 0xFF8DBF6A;
            case REEF -> 0xFF4FB3A9;
            case VOLCANIC -> 0xFF4A4040;
            case CURSED -> 0xFF5E6B55;
            case FORTRESS -> 0xFF8E9AA3;
            case BAYOU -> 0xFF5C6B3A;
            case JUNGLE -> 0xFF2F7D32;
            case CLIFFS -> 0xFF9A9A8E;
            case CRAGS -> 0xFF5D646B;
            case DESERT -> 0xFFD9B26A;
            case KEYS -> 0xFF9ED6B0;
            case ATOLL -> 0xFFB9B2C9;
            case ANCIENT -> 0xFFA7A07A;
        };
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /** Screen.render() draws the background itself, and 1.21 allows only one blur per frame: we draw it first, by hand. */
    @Override
    public void renderBackground(net.minecraft.client.gui.GuiGraphics context, int mouseX, int mouseY, float delta) {
    }
}
