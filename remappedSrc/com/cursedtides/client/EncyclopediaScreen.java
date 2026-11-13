package com.cursedtides.client;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
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
        this.pages = data.getList("Pages", Tag.TAG_COMPOUND);
        this.found = data.getInt("Found");
        this.total = data.getInt("Total");
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
        String here = pages.getCompound(page).getString("Section");
        for (int i = 1; i <= pages.size(); i++) {
            int p = (page + i) % pages.size();
            if (!pages.getCompound(p).getString("Section").equals(here)) {
                page = p;
                return;
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 262) { turn(1); return true; }   // right arrow
        if (keyCode == 263) { turn(-1); return true; }  // left arrow
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        super.renderBackground(ctx, mouseX, mouseY, delta);
        int left = (width - W) / 2, top = (height - H) / 2;
        ctx.fill(left, top, left + W, top + H, 0xF0E8DCC0);
        ctx.fill(left + 3, top + 3, left + W - 3, top + H - 3, 0xF0F4EAD2);
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredString(font, Component.literal("§6Encyclopedia of the Reach §7(" + found + "/" + total + " curios)"), width / 2, top - 12, 0xFFFFFF);
        if (pages.isEmpty()) return;
        CompoundTag p = pages.getCompound(page);
        boolean have = p.getBoolean("Found");
        ctx.drawString(font, Component.literal(p.getString("Section") + "  ·  " + (page + 1) + "/" + pages.size()), left + 10, top + 8, 0x7A6040, false);
        ctx.drawString(font, Component.literal(p.getString("Title")), left + 10, top + 22, have ? 0x3A2410 : 0x9A8A70, false);
        List<FormattedCharSequence> lines = font.split(Component.literal(p.getString("Text")), W - 20);
        int y = top + 40;
        for (FormattedCharSequence line : lines) {
            ctx.drawString(font, line, left + 10, y, have ? 0x2A2018 : 0x8A7A60, false);
            y += 10;
            if (y > top + H - 36) break;
        }
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
