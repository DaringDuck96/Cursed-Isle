package com.cursedtides.client;

import com.cursedtides.trade.Markets;
import com.cursedtides.trade.TradeGood;
import com.cursedtides.trade.Wallet;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;

/** A port's cargo market: buy and sell crates; prices move as you trade. */
public class MarketScreen extends Screen {
    private static final int ROW = 22;
    private NbtCompound data;
    private int scroll;

    public MarketScreen(NbtCompound data) {
        super(Text.literal("Market"));
        this.data = data;
    }

    public void update(NbtCompound data) {
        this.data = data;
        this.clearAndInit();
    }

    private NbtList goods() {
        return data.getList("Goods", NbtElement.COMPOUND_TYPE);
    }

    private int rows() {
        return Math.max(1, Math.min(goods().size(), (this.height - 110) / ROW));
    }

    private int left() { return this.width / 2 - 200; }
    private int top() { return 44; }

    private void send(int good, int action) {
        ClientPlayNetworking.send(new Markets.Action(data.getInt("Merchant"), good, action));
    }

    @Override
    protected void init() {
        NbtList goods = goods();
        int rows = rows();
        scroll = Math.max(0, Math.min(scroll, goods.size() - rows));
        for (int i = 0; i < rows && i + scroll < goods.size(); i++) {
            NbtCompound g = goods.getCompound(i + scroll);
            int id = g.getInt("Id");
            int y = top() + i * ROW;
            int x = left() + 236;
            addDrawableChild(ButtonWidget.builder(Text.literal("Buy"), b -> send(id, Markets.BUY_1)).dimensions(x, y, 34, 18).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("x5"), b -> send(id, Markets.BUY_5)).dimensions(x + 36, y, 26, 18).build());
            ButtonWidget sell = addDrawableChild(ButtonWidget.builder(Text.literal("Sell"), b -> send(id, Markets.SELL_1)).dimensions(x + 66, y, 34, 18).build());
            ButtonWidget all = addDrawableChild(ButtonWidget.builder(Text.literal("All"), b -> send(id, Markets.SELL_ALL)).dimensions(x + 102, y, 30, 18).build());
            sell.active = all.active = g.getInt("Have") > 0;
        }
        if (goods.size() > rows) {
            addDrawableChild(ButtonWidget.builder(Text.literal("▲"), b -> { scroll = Math.max(0, scroll - 1); clearAndInit(); })
                    .dimensions(left() + 376, top(), 20, 18).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("▼"), b -> { scroll++; clearAndInit(); })
                    .dimensions(left() + 376, top() + (rows - 1) * ROW, 20, 18).build());
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.renderBackground(ctx, mouseX, mouseY, delta);
        super.render(ctx, mouseX, mouseY, delta);
        boolean black = data.getBoolean("Black");
        String title = (black ? "§5§lBlack Market§r§5, " : "§6§lMarket§r§6, ") + data.getString("Port");
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal(title), width / 2, 12, 0xFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("§7Purse: §e" + Wallet.format(data.getInt("Wallet"))
                + "   §7(goods in your nearby ship's hold count too)"), width / 2, 26, 0xFFFFFF);
        int x = left();
        ctx.drawTextWithShadow(textRenderer, Text.literal("§7Cargo"), x + 22, top() - 10, 0xFFFFFF);
        ctx.drawTextWithShadow(textRenderer, Text.literal("§7Buy"), x + 136, top() - 10, 0xFFFFFF);
        ctx.drawTextWithShadow(textRenderer, Text.literal("§7Sell"), x + 176, top() - 10, 0xFFFFFF);
        ctx.drawTextWithShadow(textRenderer, Text.literal("§7Have"), x + 210, top() - 10, 0xFFFFFF);
        NbtList goods = goods();
        int rows = rows();
        for (int i = 0; i < rows && i + scroll < goods.size(); i++) {
            NbtCompound g = goods.getCompound(i + scroll);
            int y = top() + i * ROW;
            ctx.fill(x - 4, y - 2, x + 372, y + 19, i % 2 == 0 ? 0x40000000 : 0x20000000);
            TradeGood good = TradeGood.values()[g.getInt("Id")];
            if (good.item() != null) ctx.drawItem(new ItemStack(good.item()), x, y);
            String note = g.getString("Note");
            String color = g.getBoolean("Contraband") ? "§c" : "§f";
            ctx.drawTextWithShadow(textRenderer, Text.literal(color + g.getString("Name")), x + 22, y + 1, 0xFFFFFF);
            if (!note.isEmpty()) {
                String nc = note.equals("SHORTAGE") ? "§c" : note.equals("GLUT") ? "§a" : note.equals("wanted") ? "§6" : "§b";
                ctx.drawTextWithShadow(textRenderer, Text.literal(nc + note), x + 22, y + 10, 0xFFFFFF);
            }
            ctx.drawTextWithShadow(textRenderer, Text.literal("§e" + g.getInt("Buy") + "s"), x + 136, y + 5, 0xFFFFFF);
            ctx.drawTextWithShadow(textRenderer, Text.literal("§a" + g.getInt("Sell") + "s"), x + 176, y + 5, 0xFFFFFF);
            ctx.drawTextWithShadow(textRenderer, Text.literal("§f" + g.getInt("Have")), x + 214, y + 5, 0xFFFFFF);
        }
        NbtList news = data.getList("News", NbtElement.STRING_TYPE);
        int ny = top() + rows * ROW + 6;
        ctx.drawTextWithShadow(textRenderer, Text.literal("§7Prices in silver (10 silver = 1 doubloon). Sneak-click the merchant for their shop."),
                x, ny, 0xFFFFFF);
        for (int i = 0; i < news.size() && ny + 12 * (i + 1) < height - 4; i++) {
            ctx.drawTextWithShadow(textRenderer, Text.literal("§e⚑ " + news.getString(i)), x, ny + 12 * (i + 1), 0xFFFFFF);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double h, double v) {
        int before = scroll;
        scroll = Math.max(0, Math.min(goods().size() - rows(), scroll - (int) Math.signum(v)));
        if (scroll != before) clearAndInit();
        return true;
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
