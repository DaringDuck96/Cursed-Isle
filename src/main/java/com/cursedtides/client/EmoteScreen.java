package com.cursedtides.client;

import com.cursedtides.activity.Emotes;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** A ring of emotes. Click one, or press its number. */
public class EmoteScreen extends Screen {
    public EmoteScreen() {
        super(Text.literal("Emotes"));
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
            addDrawableChild(ButtonWidget.builder(Text.literal(name), b -> {
                ClientPlayNetworking.send(new Emotes.Do(id));
                close();
            }).dimensions(x, y, 70, 20).build());
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.renderBackground(ctx, mouseX, mouseY, delta);
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("§6Emotes"), width / 2, height / 2 - 4, 0xFFFFFF);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
