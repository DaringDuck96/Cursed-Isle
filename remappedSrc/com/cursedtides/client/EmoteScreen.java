package com.cursedtides.client;

import com.cursedtides.activity.Emotes;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** A ring of emotes. Click one, or press its number. */
public class EmoteScreen extends Screen {
    public EmoteScreen() {
        super(Component.literal("Emotes"));
    }

    @Override
    protected void init() {
        Emotes.Emote[] all = Emotes.Emote.values();
        int cx = width / 2, cy = height / 2;
        for (int i = 0; i < all.length; i++) {
            final int id = i;
            double a = Math.PI * 2 * i / all.length - Math.PI / 2;
            int x = cx + (int) (Math.cos(a) * 100) - 35, y = cy + (int) (Math.sin(a) * 80) - 10;
            String name = all[i].name().charAt(0) + all[i].name().substring(1).toLowerCase();
            addRenderableWidget(Button.builder(Component.literal(name), b -> {
                ClientPlayNetworking.send(new Emotes.Do(id));
                onClose();
            }).bounds(x, y, 70, 20).build());
        }
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        super.renderBackground(ctx, mouseX, mouseY, delta);
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredString(font, Component.literal("§6Emotes"), width / 2, height / 2 - 4, 0xFFFFFF);
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
