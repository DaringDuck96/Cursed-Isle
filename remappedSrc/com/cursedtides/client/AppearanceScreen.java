package com.cursedtides.client;

import com.cursedtides.appearance.Appearance;
import com.cursedtides.appearance.Look;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;

/** The looking glass: build your pirate's face, hair and clothes, and see them turn in the mirror. */
public class AppearanceScreen extends Screen {
    private boolean enabled, slim;
    private final int[] values = new int[6]; // tone, hair, hair colour, facial, marking, outfit
    private static final String[] LABELS = {"Skin", "Hair", "Hair colour", "Facial hair", "Marks", "Clothes"};
    private static final String[][] OPTIONS = {Look.TONES, Look.HAIR, Look.HAIR_COLOURS, Look.FACIAL, Look.MARKINGS, Look.OUTFITS};
    private final int accessories;

    public AppearanceScreen(int code) {
        super(Component.literal("The Looking Glass"));
        this.enabled = true;
        this.slim = Look.slim(code);
        values[0] = Look.tone(code);
        values[1] = Look.enabled(code) ? Look.hair(code) : 1;
        values[2] = Look.hairColour(code);
        values[3] = Look.facial(code);
        values[4] = Look.marking(code);
        values[5] = Look.outfit(code);
        this.accessories = Look.accessories(code);
        updatePreview();
    }

    private int pack() {
        return Look.pack(enabled, slim, values[0], values[1], values[2], values[3], values[4], values[5], accessories);
    }

    private void updatePreview() {
        AppearanceClient.preview = pack();
    }

    @Override
    protected void init() {
        int x = width / 2 + 10, y = height / 2 - 92;
        for (int i = 0; i < 6; i++) {
            final int idx = i;
            addRenderableWidget(Button.builder(Component.literal("<"), b -> cycle(idx, -1)).bounds(x, y + i * 24, 20, 20).build());
            addRenderableWidget(Button.builder(Component.literal(">"), b -> cycle(idx, 1)).bounds(x + 140, y + i * 24, 20, 20).build());
        }
        addRenderableWidget(Button.builder(Component.literal(slim ? "Build: slight" : "Build: broad"), b -> {
            slim = !slim;
            updatePreview();
            rebuildWidgets();
        }).bounds(x, y + 6 * 24, 160, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Use my own skin"), b -> {
            ClientPlayNetworking.send(new Appearance.Set(0));
            onClose();
        }).bounds(x, y + 7 * 24, 78, 20).build());
        addRenderableWidget(Button.builder(Component.literal("§6Done"), b -> {
            ClientPlayNetworking.send(new Appearance.Set(pack()));
            onClose();
        }).bounds(x + 82, y + 7 * 24, 78, 20).build());
    }

    private void cycle(int idx, int dir) {
        int n = OPTIONS[idx].length;
        values[idx] = Math.floorMod(values[idx] + dir, n);
        updatePreview();
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        super.renderBackground(ctx, mouseX, mouseY, delta);
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredString(font, Component.literal("§6§lThe Looking Glass"), width / 2, height / 2 - 112, 0xFFFFFF);
        int x = width / 2 + 10, y = height / 2 - 92;
        for (int i = 0; i < 6; i++) {
            ctx.drawCenteredString(font, Component.literal("§7" + LABELS[i] + ": §f" + OPTIONS[i][values[i]]),
                    x + 80, y + i * 24 + 6, 0xFFFFFF);
        }
        if (minecraft != null && minecraft.player != null) {
            int px = width / 2 - 140, py = height / 2 - 100;
            ctx.fill(px, py, px + 120, py + 190, 0x88202020);
            InventoryScreen.renderEntityInInventoryFollowsMouse(ctx, px, py, px + 120, py + 190, 70, 0.0625f, mouseX, mouseY, minecraft.player);
        }
        ctx.drawCenteredString(font, Component.literal("§8Rings, patches and bandanas show once worn."),
                width / 2, height / 2 + 104, 0xFFFFFF);
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void removed() {
        AppearanceClient.preview = null;
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
