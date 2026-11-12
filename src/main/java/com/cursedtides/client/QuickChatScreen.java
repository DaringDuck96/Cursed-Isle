package com.cursedtides.client;

import com.cursedtides.social.QuickChat;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** The quick chat menu (Y): pick a topic down the left, then a phrase; your pirate says it aloud. */
public class QuickChatScreen extends Screen {
    private int category = 0;

    public QuickChatScreen() {
        super(Text.literal("Quick Chat"));
    }

    @Override
    protected void init() {
        clearChildren();
        int left = this.width / 2 - 170, top = this.height / 2 - 100;
        for (int i = 0; i < QuickChat.CATEGORIES.size(); i++) {
            final int c = i;
            String name = QuickChat.CATEGORIES.get(i).name();
            addDrawableChild(ButtonWidget.builder(Text.literal((i == category ? "» " : "") + name), b -> {
                category = c;
                init();
            }).dimensions(left, top + i * 24, 100, 20).build());
        }
        String[] phrases = QuickChat.CATEGORIES.get(category).phrases();
        for (int j = 0; j < phrases.length; j++) {
            final int p = j;
            int col = j / 5, row = j % 5;
            addDrawableChild(ButtonWidget.builder(Text.literal(phrases[j]), b -> {
                ClientPlayNetworking.send(new QuickChat.Say(category, p));
                close();
            }).dimensions(left + 110 + col * 120, top + row * 24, 116, 20).build());
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(this.width / 2 - 178, this.height / 2 - 122, this.width / 2 + 178, this.height / 2 + 52, 0xC02A1E10);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "Quick Chat", this.width / 2, this.height / 2 - 116, 0xFFFFD24A);
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) { }

    @Override
    public boolean shouldPause() { return false; }
}
