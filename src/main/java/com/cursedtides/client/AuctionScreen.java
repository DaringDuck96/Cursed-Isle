package com.cursedtides.client;

import com.cursedtides.trade.AuctionHouse;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

/** The Kingsport auction board: every lot, its seller and price, with a Buy button. */
public class AuctionScreen extends Screen {
    private static final int ROW = 22;
    private List<AuctionHouse.Entry> entries;
    private int page;

    public AuctionScreen(List<AuctionHouse.Entry> entries) {
        super(Text.literal("Auction House"));
        this.entries = entries;
    }

    public void update(List<AuctionHouse.Entry> entries) {
        this.entries = entries;
        this.clearAndInit();
    }

    private int perPage() {
        return Math.max(1, (this.height - 90) / ROW);
    }

    private int left() { return this.width / 2 - 160; }

    @Override
    protected void init() {
        int per = perPage();
        int pages = Math.max(1, (entries.size() + per - 1) / per);
        page = Math.min(page, pages - 1);
        for (int i = 0; i < per; i++) {
            int index = page * per + i;
            if (index >= entries.size()) break;
            AuctionHouse.Entry e = entries.get(index);
            addDrawableChild(ButtonWidget.builder(Text.literal("Buy"), b -> ClientPlayNetworking.send(new AuctionHouse.Buy(e.id())))
                    .dimensions(left() + 270, 40 + i * ROW, 50, 18).build());
        }
        if (page > 0) addDrawableChild(ButtonWidget.builder(Text.literal("< Prev"), b -> { page--; clearAndInit(); })
                .dimensions(width / 2 - 110, height - 28, 70, 20).build());
        if (page < pages - 1) addDrawableChild(ButtonWidget.builder(Text.literal("Next >"), b -> { page++; clearAndInit(); })
                .dimensions(width / 2 + 40, height - 28, 70, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.renderBackground(ctx, mouseX, mouseY, delta);
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("§6§lKingsport Auction House"), width / 2, 12, 0xFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("§7To sell: hold the item and type §f/auction sell <doubloons>§7. The house keeps 5%."),
                width / 2, 24, 0xFFFFFF);
        if (entries.isEmpty()) {
            ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("§7The board is empty. Be the first to list something!"), width / 2, 60, 0xFFFFFF);
            return;
        }
        int per = perPage();
        int x = left();
        for (int i = 0; i < per; i++) {
            int index = page * per + i;
            if (index >= entries.size()) break;
            AuctionHouse.Entry e = entries.get(index);
            int y = 40 + i * ROW;
            ctx.fill(x - 4, y - 2, x + 324, y + 19, i % 2 == 0 ? 0x40000000 : 0x20000000);
            ctx.drawItem(e.stack(), x, y);
            ctx.drawItemInSlot(textRenderer, e.stack(), x, y);
            ctx.drawTextWithShadow(textRenderer, e.stack().getName(), x + 22, y + 1, 0xFFFFFF);
            ctx.drawTextWithShadow(textRenderer, Text.literal("§7#" + e.id() + " from " + e.seller()), x + 22, y + 10, 0xFFFFFF);
            ctx.drawTextWithShadow(textRenderer, Text.literal("§e" + e.price() + " doubloons"), x + 190, y + 5, 0xFFFFFF);
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                ctx.drawItemTooltip(textRenderer, e.stack(), mouseX, mouseY);
            }
        }
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
