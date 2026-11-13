package com.cursedtides.client;

import com.cursedtides.trade.Markets;
import com.cursedtides.trade.TradeGood;
import com.cursedtides.trade.Wallet;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** A port's cargo market: buy and sell crates; prices move as you trade. */
public class MarketScreen extends Screen {
    private static final int ROW = 22;
    private CompoundTag data;
    private int scroll;

    public MarketScreen(CompoundTag data) {
        super(Component.literal("Market"));
        this.data = data;
    }

    public void update(CompoundTag data) {
        this.data = data;
        this.rebuildWidgets();
    }

    private ListTag goods() {
        return data.getList("Goods", Tag.TAG_COMPOUND);
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
        ListTag goods = goods();
        int rows = rows();
        scroll = Math.max(0, Math.min(scroll, goods.size() - rows));
        for (int i = 0; i < rows && i + scroll < goods.size(); i++) {
            CompoundTag g = goods.getCompound(i + scroll);
            int id = g.getInt("Id");
            int y = top() + i * ROW;
            int x = left() + 236;
            addRenderableWidget(Button.builder(Component.literal("Buy"), b -> send(id, Markets.BUY_1)).bounds(x, y, 34, 18).build());
            addRenderableWidget(Button.builder(Component.literal("x5"), b -> send(id, Markets.BUY_5)).bounds(x + 36, y, 26, 18).build());
            Button sell = addRenderableWidget(Button.builder(Component.literal("Sell"), b -> send(id, Markets.SELL_1)).bounds(x + 66, y, 34, 18).build());
            Button all = addRenderableWidget(Button.builder(Component.literal("All"), b -> send(id, Markets.SELL_ALL)).bounds(x + 102, y, 30, 18).build());
            sell.active = all.active = g.getInt("Have") > 0;
        }
        if (goods.size() > rows) {
            addRenderableWidget(Button.builder(Component.literal("▲"), b -> { scroll = Math.max(0, scroll - 1); rebuildWidgets(); })
                    .bounds(left() + 376, top(), 20, 18).build());
            addRenderableWidget(Button.builder(Component.literal("▼"), b -> { scroll++; rebuildWidgets(); })
                    .bounds(left() + 376, top() + (rows - 1) * ROW, 20, 18).build());
        }
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        super.renderBackground(ctx, mouseX, mouseY, delta);
        super.render(ctx, mouseX, mouseY, delta);
        boolean black = data.getBoolean("Black");
        String title = (black ? "§5§lBlack Market§r§5, " : "§6§lMarket§r§6, ") + data.getString("Port");
        ctx.drawCenteredString(font, Component.literal(title), width / 2, 12, 0xFFFFFF);
        ctx.drawCenteredString(font, Component.literal("§7Purse: §e" + Wallet.format(data.getInt("Wallet"))
                + "   §7(goods in your nearby ship's hold count too)"), width / 2, 26, 0xFFFFFF);
        int x = left();
        ctx.drawString(font, Component.literal("§7Cargo"), x + 22, top() - 10, 0xFFFFFF);
        ctx.drawString(font, Component.literal("§7Buy"), x + 136, top() - 10, 0xFFFFFF);
        ctx.drawString(font, Component.literal("§7Sell"), x + 176, top() - 10, 0xFFFFFF);
        ctx.drawString(font, Component.literal("§7Have"), x + 210, top() - 10, 0xFFFFFF);
        ListTag goods = goods();
        int rows = rows();
        for (int i = 0; i < rows && i + scroll < goods.size(); i++) {
            CompoundTag g = goods.getCompound(i + scroll);
            int y = top() + i * ROW;
            ctx.fill(x - 4, y - 2, x + 372, y + 19, i % 2 == 0 ? 0x40000000 : 0x20000000);
            TradeGood good = TradeGood.values()[g.getInt("Id")];
            if (good.item() != null) ctx.renderItem(new ItemStack(good.item()), x, y);
            String note = g.getString("Note");
            String color = g.getBoolean("Contraband") ? "§c" : "§f";
            ctx.drawString(font, Component.literal(color + g.getString("Name")), x + 22, y + 1, 0xFFFFFF);
            if (!note.isEmpty()) {
                String nc = note.equals("SHORTAGE") ? "§c" : note.equals("GLUT") ? "§a" : note.equals("wanted") ? "§6" : "§b";
                ctx.drawString(font, Component.literal(nc + note), x + 22, y + 10, 0xFFFFFF);
            }
            ctx.drawString(font, Component.literal("§e" + g.getInt("Buy") + "s"), x + 136, y + 5, 0xFFFFFF);
            ctx.drawString(font, Component.literal("§a" + g.getInt("Sell") + "s"), x + 176, y + 5, 0xFFFFFF);
            ctx.drawString(font, Component.literal("§f" + g.getInt("Have")), x + 214, y + 5, 0xFFFFFF);
        }
        ListTag news = data.getList("News", Tag.TAG_STRING);
        int ny = top() + rows * ROW + 6;
        ctx.drawString(font, Component.literal("§7Prices in silver (10 silver = 1 doubloon). Sneak-click the merchant for their shop."),
                x, ny, 0xFFFFFF);
        for (int i = 0; i < news.size() && ny + 12 * (i + 1) < height - 4; i++) {
            ctx.drawString(font, Component.literal("§e⚑ " + news.getString(i)), x, ny + 12 * (i + 1), 0xFFFFFF);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double h, double v) {
        int before = scroll;
        scroll = Math.max(0, Math.min(goods().size() - rows(), scroll - (int) Math.signum(v)));
        if (scroll != before) rebuildWidgets();
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /** Screen.render() draws the background itself, and 1.21 allows only one blur per frame: we draw it first, by hand. */
    @Override
    public void renderBackground(net.minecraft.client.gui.GuiGraphics context, int mouseX, int mouseY, float delta) {
    }
}
