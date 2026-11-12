package com.cursedtides.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.List;

/** The captain's journal: story, skills, standing, contracts, maps, dungeons, islands. Scrolls with the wheel. */
public class JournalScreen extends Screen {
    private static final int PARCHMENT = 0xF0E8DCC0;
    private static final int EDGE = 0xFF6B4A2B;
    private static final int LINE_H = 11;

    private final List<String> lines;
    private int scroll;

    public JournalScreen(List<String> lines) {
        super(Text.literal("Captain's Journal"));
        this.lines = lines;
    }

    private int panelLeft() { return Math.max(10, this.width / 2 - 170); }
    private int panelRight() { return Math.min(this.width - 10, this.width / 2 + 170); }
    private int visibleLines() { return (this.height - 70) / LINE_H; }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        int left = panelLeft(), right = panelRight();
        context.fill(left - 2, 18, right + 2, this.height - 18, EDGE);
        context.fill(left, 20, right, this.height - 20, PARCHMENT);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§6§lCaptain's Journal"), this.width / 2, 26, 0xFFFFFF);

        int y = 44;
        int end = Math.min(lines.size(), scroll + visibleLines());
        for (int i = scroll; i < end; i++) {
            context.drawText(this.textRenderer, Text.literal(lines.get(i)), left + 10, y, 0x3B2A1A, false);
            y += LINE_H;
        }
        if (lines.size() > visibleLines()) {
            context.drawText(this.textRenderer, Text.literal("§8Scroll for more  (" + (scroll + 1) + "/" + lines.size() + ")"),
                    left + 10, this.height - 32, 0x5A4630, false);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scroll = MathHelper.clamp(scroll - (int) Math.signum(verticalAmount) * 3, 0, Math.max(0, lines.size() - visibleLines()));
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    /** Screen.render() draws the background itself, and 1.21 allows only one blur per frame: we draw it first, by hand. */
    @Override
    public void renderBackground(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
    }
}
