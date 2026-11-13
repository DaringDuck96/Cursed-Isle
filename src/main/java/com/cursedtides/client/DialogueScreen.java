package com.cursedtides.client;

import com.cursedtides.dialogue.DialoguePayloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * The conversation window: the speaker's portrait and name, their words typed out a letter at a time,
 * and up to six numbered replies. Click a reply or press its number; click the text to skip ahead.
 */
public class DialogueScreen extends Screen {
    private static final int PANEL_W = 380, PORTRAIT_W = 70;
    private CompoundTag data;
    private float shown;
    private int fullLength;

    public DialogueScreen(CompoundTag data) {
        super(Component.literal("Conversation"));
        this.data = data;
    }

    public void update(CompoundTag data) {
        this.data = data;
        this.shown = 0;
        this.rebuildWidgets();
    }

    private ListTag options() {
        return data.getListOrEmpty("Options");
    }

    private List<String> lines() {
        List<String> out = new ArrayList<>();
        ListTag t = data.getListOrEmpty("Text");
        for (int i = 0; i < t.size(); i++) out.add(t.getStringOr(i, ""));
        return out;
    }

    private int panelLeft() { return (this.width - PANEL_W) / 2; }

    private int panelTop() {
        return this.height - 18 - textHeight() - 24 * options().size() - 30;
    }

    private int textHeight() {
        int h = 0;
        for (String line : lines()) h += font.split(FormattedText.of(line), PANEL_W - PORTRAIT_W - 20).size() * 10 + 4;
        return Math.max(h, 50);
    }

    @Override
    protected void init() {
        fullLength = 0;
        for (String l : lines()) fullLength += l.length();
        ListTag opts = options();
        int top = panelTop() + 30 + textHeight();
        for (int i = 0; i < opts.size(); i++) {
            int index = i;
            String label = (i + 1) + ". " + opts.getString(i);
            addRenderableWidget(Button.builder(Component.literal(label), b -> pick(index))
                    .bounds(panelLeft() + 10, top + i * 24, PANEL_W - 20, 20).build());
        }
    }

    private void pick(int index) {
        ClientPlayNetworking.send(new DialoguePayloads.Choose(index));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        shown = Math.min(fullLength, shown + delta * 1.6f);
        int left = panelLeft(), top = panelTop();
        int bottom = this.height - 10;
        ctx.fillGradient(0, top - 30, this.width, this.height, 0x00000000, 0xC0000000);
        ctx.fill(left, top, left + PANEL_W, bottom, 0xE0141018);
        ctx.outline(left, top, PANEL_W, bottom - top, 0xFF8A6A2E);
        ctx.outline(left + 2, top + 2, PANEL_W - 4, bottom - top - 4, 0xFF3A2C14);
        // Portrait: the speaker themselves
        Entity e = this.minecraft != null && this.minecraft.level != null ? this.minecraft.level.getEntity(data.getIntOr("Npc", 0)) : null;
        if (e instanceof LivingEntity living) {
            ctx.fill(left + 8, top + 8, left + 8 + PORTRAIT_W - 12, top + 8 + 76, 0xFF241C10);
            InventoryScreen.extractEntityInInventoryFollowsMouse(ctx, left + 8, top + 8, left + PORTRAIT_W - 4, top + 84, 34, 0.0625f,
                    mouseX, mouseY, living);
        }
        ctx.text(font, Component.literal("§6§l" + data.getStringOr("Name", "")), left + PORTRAIT_W + 6, top + 10, 0xFFFFFFFF);
        // The words, typed out
        int budget = (int) shown;
        int y = top + 24;
        for (String line : lines()) {
            String visible = budget >= line.length() ? line : line.substring(0, Math.max(0, budget));
            budget -= line.length();
            for (FormattedCharSequence row : font.split(FormattedText.of(visible), PANEL_W - PORTRAIT_W - 20)) {
                ctx.text(font, row, left + PORTRAIT_W + 6, y, 0xFFEDE6D6);
                y += 10;
            }
            y += 4;
            if (budget <= 0) break;
        }
        super.extractRenderState(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event__, boolean dbl__) {
        double mouseX = event__.x(), mouseY = event__.y(); int button = UiCompat.button(event__);
        if (shown < fullLength && mouseY < panelTop() + 30 + textHeight()) {
            shown = fullLength;
            return true;
        }
        return super.mouseClicked(event__, dbl__);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent key__) {
        int keyCode = key__.key(), scanCode = key__.keycode(), modifiers = key__.modifiers();
        if (keyCode >= com.mojang.blaze3d.platform.InputConstants.KEY_1 && keyCode <= com.mojang.blaze3d.platform.InputConstants.KEY_6) {
            int i = keyCode - com.mojang.blaze3d.platform.InputConstants.KEY_1;
            if (i < options().size()) {
                pick(i);
                return true;
            }
        }
        if (keyCode == com.mojang.blaze3d.platform.InputConstants.KEY_SPACE && shown < fullLength) {
            shown = fullLength;
            return true;
        }
        return super.keyPressed(key__);
    }

    @Override
    public void onClose() {
        ClientPlayNetworking.send(new DialoguePayloads.Choose(-1));
        super.onClose();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        // No blur: the world stays in view behind the conversation
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
