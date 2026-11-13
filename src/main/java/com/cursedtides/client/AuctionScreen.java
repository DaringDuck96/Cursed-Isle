package com.cursedtides.client;

import com.cursedtides.trade.AuctionHouse;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.List;

/** The Kingsport auction board: every lot, its seller and price, with a Buy button. */
public class AuctionScreen extends Screen {
    private static final int ROW = 22;
    private List<AuctionHouse.Entry> entries;
    private int page;

    public AuctionScreen(List<AuctionHouse.Entry> entries) {
        super(Component.literal("Auction House"));
        this.entries = entries;
    }

    public void update(List<AuctionHouse.Entry> entries) {
        this.entries = entries;
        this.rebuildWidgets();
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
            addRenderableWidget(Button.builder(Component.literal("Buy"), b -> ClientPlayNetworking.send(new AuctionHouse.Buy(e.id())))
                    .bounds(left() + 270, 40 + i * ROW, 50, 18).build());
        }
        if (page > 0) addRenderableWidget(Button.builder(Component.literal("< Prev"), b -> { page--; rebuildWidgets(); })
                .bounds(width / 2 - 110, height - 28, 70, 20).build());
        if (page < pages - 1) addRenderableWidget(Button.builder(Component.literal("Next >"), b -> { page++; rebuildWidgets(); })
                .bounds(width / 2 + 40, height - 28, 70, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        super.extractBackground(ctx, mouseX, mouseY, delta);
        super.extractRenderState(ctx, mouseX, mouseY, delta);
        ctx.centeredText(font, Component.literal("§6§lKingsport Auction House"), width / 2, 12, 0xFFFFFFFF);
        ctx.centeredText(font, Component.literal("§7To sell: hold the item and type §f/auction sell <doubloons>§7. The house keeps 5%."),
                width / 2, 24, 0xFFFFFFFF);
        if (entries.isEmpty()) {
            ctx.centeredText(font, Component.literal("§7The board is empty. Be the first to list something!"), width / 2, 60, 0xFFFFFFFF);
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
            ctx.item(e.stack(), x, y);
            ctx.itemDecorations(font, e.stack(), x, y);
            ctx.text(font, e.stack().getHoverName(), x + 22, y + 1, 0xFFFFFFFF);
            ctx.text(font, Component.literal("§7#" + e.id() + " from " + e.seller()), x + 22, y + 10, 0xFFFFFFFF);
            ctx.text(font, Component.literal("§e" + e.price() + " doubloons"), x + 190, y + 5, 0xFFFFFFFF);
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                ctx.setTooltipForNextFrame(font, e.stack(), mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /** Screen.render() draws the background itself, and 1.21 allows only one blur per frame: we draw it first, by hand. */
    @Override
    public void extractBackground(net.minecraft.client.gui.GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    }
}
