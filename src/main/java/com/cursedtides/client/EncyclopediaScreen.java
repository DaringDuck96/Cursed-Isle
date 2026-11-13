package com.cursedtides.client;

import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/** The Encyclopedia of the Reach: one page per curio and island, leafed through like a book. */
public class EncyclopediaScreen extends Screen {
    private final ListTag pages;
    private final int found, total;
    private int page;
    private static final int W = 260, H = 190;

    public EncyclopediaScreen(CompoundTag data) {
        super(Component.literal("Encyclopedia of the Reach"));
        this.pages = data.getListOrEmpty("Pages");
        this.found = data.getIntOr("Found", 0);
        this.total = data.getIntOr("Total", 0);
    }

    @Override
    protected void init() {
        int left = (width - W) / 2, top = (height - H) / 2;
        addRenderableWidget(Button.builder(Component.literal("<"), b -> turn(-1)).bounds(left + 8, top + H - 28, 20, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"), b -> turn(1)).bounds(left + W - 28, top + H - 28, 20, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Next section"), b -> nextSection()).bounds(left + W / 2 - 45, top + H - 28, 90, 20).build());
    }

    private void turn(int d) {
        if (pages.isEmpty()) return;
        page = Math.floorMod(page + d, pages.size());
    }

    private void nextSection() {
        if (pages.isEmpty()) return;
        String here = pages.getCompoundOrEmpty(page).getStringOr("Section", "");
        for (int i = 1; i <= pages.size(); i++) {
            int p = (page + i) % pages.size();
            if (!pages.getCompoundOrEmpty(p).getStringOr("Section", "").equals(here)) {
                page = p;
                return;
            }
        }
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent key__) {
        int keyCode = key__.key(), scanCode = key__.keycode(), modifiers = key__.modifiers();
        if (keyCode == 262) { turn(1); return true; }   // right arrow
        if (keyCode == 263) { turn(-1); return true; }  // left arrow
        return super.keyPressed(key__);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        super.extractBackground(ctx, mouseX, mouseY, delta);
        int left = (width - W) / 2, top = (height - H) / 2;
        ctx.fill(left, top, left + W, top + H, 0xF0E8DCC0);
        ctx.fill(left + 3, top + 3, left + W - 3, top + H - 3, 0xF0F4EAD2);
        super.extractRenderState(ctx, mouseX, mouseY, delta);
        ctx.centeredText(font, Component.literal("§6Encyclopedia of the Reach §7(" + found + "/" + total + " curios)"), width / 2, top - 12, 0xFFFFFFFF);
        if (pages.isEmpty()) return;
        CompoundTag p = pages.getCompoundOrEmpty(page);
        boolean have = p.getBooleanOr("Found", false);
        ctx.text(font, Component.literal(p.getStringOr("Section", "") + "  ·  " + (page + 1) + "/" + pages.size()), left + 10, top + 8, 0xFF7A6040, false);
        ctx.text(font, Component.literal(p.getStringOr("Title", "")), left + 10, top + 22, have ? 0xFF3A2410 : 0xFF9A8A70, false);
        List<FormattedCharSequence> lines = font.split(Component.literal(p.getStringOr("Text", "")), W - 20);
        int y = top + 40;
        for (FormattedCharSequence line : lines) {
            ctx.text(font, line, left + 10, y, have ? 0xFF2A2018 : 0xFF8A7A60, false);
            y += 10;
            if (y > top + H - 36) break;
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
