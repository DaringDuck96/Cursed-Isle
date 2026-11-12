package com.cursedtides.client;

import com.cursedtides.dialogue.DialoguePayloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * The conversation window: the speaker's portrait and name, their words typed out a letter at a time,
 * and up to six numbered replies. Click a reply or press its number; click the text to skip ahead.
 */
public class DialogueScreen extends Screen {
    private static final int PANEL_W = 380, PORTRAIT_W = 70;
    private NbtCompound data;
    private float shown;
    private int fullLength;

    public DialogueScreen(NbtCompound data) {
        super(Text.literal("Conversation"));
        this.data = data;
    }

    public void update(NbtCompound data) {
        this.data = data;
        this.shown = 0;
        this.clearAndInit();
    }

    private NbtList options() {
        return data.getList("Options", NbtElement.STRING_TYPE);
    }

    private List<String> lines() {
        List<String> out = new ArrayList<>();
        NbtList t = data.getList("Text", NbtElement.STRING_TYPE);
        for (int i = 0; i < t.size(); i++) out.add(t.getString(i));
        return out;
    }

    private int panelLeft() { return (this.width - PANEL_W) / 2; }

    private int panelTop() {
        return this.height - 18 - textHeight() - 24 * options().size() - 30;
    }

    private int textHeight() {
        int h = 0;
        for (String line : lines()) h += textRenderer.wrapLines(StringVisitable.plain(line), PANEL_W - PORTRAIT_W - 20).size() * 10 + 4;
        return Math.max(h, 50);
    }

    @Override
    protected void init() {
        fullLength = 0;
        for (String l : lines()) fullLength += l.length();
        NbtList opts = options();
        int top = panelTop() + 30 + textHeight();
        for (int i = 0; i < opts.size(); i++) {
            int index = i;
            String label = (i + 1) + ". " + opts.getString(i);
            addDrawableChild(ButtonWidget.builder(Text.literal(label), b -> pick(index))
                    .dimensions(panelLeft() + 10, top + i * 24, PANEL_W - 20, 20).build());
        }
    }

    private void pick(int index) {
        ClientPlayNetworking.send(new DialoguePayloads.Choose(index));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        shown = Math.min(fullLength, shown + delta * 1.6f);
        int left = panelLeft(), top = panelTop();
        int bottom = this.height - 10;
        ctx.fillGradient(0, top - 30, this.width, this.height, 0x00000000, 0xC0000000);
        ctx.fill(left, top, left + PANEL_W, bottom, 0xE0141018);
        ctx.drawBorder(left, top, PANEL_W, bottom - top, 0xFF8A6A2E);
        ctx.drawBorder(left + 2, top + 2, PANEL_W - 4, bottom - top - 4, 0xFF3A2C14);
        // Portrait: the speaker themselves
        Entity e = this.client != null && this.client.world != null ? this.client.world.getEntityById(data.getInt("Npc")) : null;
        if (e instanceof LivingEntity living) {
            ctx.fill(left + 8, top + 8, left + 8 + PORTRAIT_W - 12, top + 8 + 76, 0xFF241C10);
            InventoryScreen.drawEntity(ctx, left + 8, top + 8, left + PORTRAIT_W - 4, top + 84, 34, 0.0625f,
                    mouseX, mouseY, living);
        }
        ctx.drawTextWithShadow(textRenderer, Text.literal("§6§l" + data.getString("Name")), left + PORTRAIT_W + 6, top + 10, 0xFFFFFF);
        // The words, typed out
        int budget = (int) shown;
        int y = top + 24;
        for (String line : lines()) {
            String visible = budget >= line.length() ? line : line.substring(0, Math.max(0, budget));
            budget -= line.length();
            for (OrderedText row : textRenderer.wrapLines(StringVisitable.plain(visible), PANEL_W - PORTRAIT_W - 20)) {
                ctx.drawTextWithShadow(textRenderer, row, left + PORTRAIT_W + 6, y, 0xEDE6D6);
                y += 10;
            }
            y += 4;
            if (budget <= 0) break;
        }
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (shown < fullLength && mouseY < panelTop() + 30 + textHeight()) {
            shown = fullLength;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode >= GLFW.GLFW_KEY_1 && keyCode <= GLFW.GLFW_KEY_6) {
            int i = keyCode - GLFW.GLFW_KEY_1;
            if (i < options().size()) {
                pick(i);
                return true;
            }
        }
        if (keyCode == GLFW.GLFW_KEY_SPACE && shown < fullLength) {
            shown = fullLength;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        ClientPlayNetworking.send(new DialoguePayloads.Choose(-1));
        super.close();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // No blur: the world stays in view behind the conversation
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
