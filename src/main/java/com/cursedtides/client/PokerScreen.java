package com.cursedtides.client;

import com.cursedtides.poker.PokerTables;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;

/** A Hold'em table seen from your chair: you at the bottom, three opponents around the felt. */
public class PokerScreen extends Screen {
    private static final int FELT = 0xFF1E5B35, FELT_EDGE = 0xFF5A3A1E, CARD = 0xFFF4EFE2, CARD_BACK = 0xFF7A1F25;

    private NbtCompound state;
    private ButtonWidget fold, call, raiseSmall, raisePot, allIn, deal, leave;

    public PokerScreen(NbtCompound state) {
        super(Text.literal("Hold'em"));
        this.state = state;
    }

    public void update(NbtCompound state) {
        this.state = state;
        refreshButtons();
    }

    private static void send(int action, int amount) {
        ClientPlayNetworking.send(new PokerTables.Action(action, amount));
    }

    @Override
    protected void init() {
        int y = this.height - 28;
        int x = this.width / 2 - 215;
        fold = addDrawableChild(ButtonWidget.builder(Text.literal("Fold"), b -> send(0, 0)).dimensions(x, y, 60, 20).build());
        call = addDrawableChild(ButtonWidget.builder(Text.literal("Check"), b -> send(1, 0)).dimensions(x + 64, y, 80, 20).build());
        raiseSmall = addDrawableChild(ButtonWidget.builder(Text.literal("Raise"), b -> send(2, state.getInt("MinRaiseTo")))
                .dimensions(x + 148, y, 80, 20).build());
        raisePot = addDrawableChild(ButtonWidget.builder(Text.literal("Raise pot"),
                b -> send(2, state.getInt("CurrentBet") + Math.max(state.getInt("Pot"), state.getInt("MinRaiseTo") - state.getInt("CurrentBet"))))
                .dimensions(x + 232, y, 70, 20).build());
        allIn = addDrawableChild(ButtonWidget.builder(Text.literal("All-in"), b -> send(3, 0)).dimensions(x + 306, y, 54, 20).build());
        deal = addDrawableChild(ButtonWidget.builder(Text.literal("Deal"), b -> send(PokerTables.DEAL, 0)).dimensions(x + 364, y, 66, 20).build());
        leave = addDrawableChild(ButtonWidget.builder(Text.literal("Cash out"), b -> send(PokerTables.LEAVE, 0))
                .dimensions(this.width - 70, 6, 64, 20).build());
        refreshButtons();
    }

    private void refreshButtons() {
        if (fold == null) return;
        boolean myTurn = state.getInt("Acting") == 0 && !state.getBoolean("HandOver");
        int toCall = state.getInt("ToCall");
        int myStack = state.getIntArray("Stacks").length > 0 ? state.getIntArray("Stacks")[0] : 0;
        fold.active = myTurn && toCall > 0;
        call.active = myTurn;
        call.setMessage(Text.literal(toCall == 0 ? "Check" : "Call " + Math.min(toCall, myStack)));
        raiseSmall.active = myTurn && myStack > toCall;
        raiseSmall.setMessage(Text.literal((state.getInt("CurrentBet") == 0 ? "Bet " : "Raise to ") + state.getInt("MinRaiseTo")));
        raisePot.active = myTurn && myStack > toCall;
        allIn.active = myTurn && myStack > 0;
        deal.active = state.getBoolean("HandOver");
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        int cx = this.width / 2, cy = this.height / 2 - 14;
        int w = Math.min(380, this.width - 40), h = Math.min(170, this.height - 90);

        // Table: a rounded felt rectangle with a wooden rail
        roundRect(context, cx - w / 2 - 6, cy - h / 2 - 6, w + 12, h + 12, FELT_EDGE);
        roundRect(context, cx - w / 2, cy - h / 2, w, h, FELT);

        String[] names = names();
        int[] stacks = state.getIntArray("Stacks"), bets = state.getIntArray("Bets"), flags = state.getIntArray("Flags");
        int[] cards = state.getIntArray("Cards");
        int acting = state.getInt("Acting"), dealer = state.getInt("Dealer");
        // seat positions: 0 bottom, 1 left, 2 top, 3 right
        int[][] pos = {{cx, cy + h / 2 - 22}, {cx - w / 2 + 50, cy}, {cx, cy - h / 2 + 22}, {cx + w / 2 - 50, cy}};
        for (int i = 0; i < names.length && i < 4; i++) {
            boolean folded = (flags[i] & 1) != 0, allIn = (flags[i] & 2) != 0;
            int px = pos[i][0], py = pos[i][1];
            String label = (i == dealer ? "§e(D) §r" : "") + (i == acting ? "§a▶ §r" : "") + names[i];
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(label), px, py - 26, folded ? 0x888888 : 0xFFFFFF);
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.literal("§6" + stacks[i] + "§7 chips" + (allIn ? " §c(all-in)" : folded ? " §8(folded)" : "")), px, py - 16, 0xFFFFFF);
            drawCard(context, px - 17, py - 6, cards[i * 2], folded && i != 0);
            drawCard(context, px + 1, py - 6, cards[i * 2 + 1], folded && i != 0);
            if (bets[i] > 0) {
                int bx = px + (cx - px) / 3, by = py + (cy - py) / 3 + 14;
                context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§f● " + bets[i]), bx, by, 0xFFFFFF);
            }
        }

        // Board and pot
        int[] board = state.getIntArray("Board");
        for (int i = 0; i < 5; i++) {
            int x = cx - 46 + i * 19;
            if (i < board.length) drawCard(context, x, cy - 12, board[i], false);
            else context.fill(x, cy - 12, x + 16, cy + 10, 0x40FFFFFF);
        }
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§ePot " + state.getInt("Pot")), cx, cy + 14, 0xFFFFFF);

        // Table talk
        String[] log = state.getString("Log").split("\n");
        int ly = cy + h / 2 + 10;
        for (int i = Math.max(0, log.length - 3); i < log.length; i++) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§7" + log[i]), cx, ly, 0xFFFFFF);
            ly += 10;
        }
        context.drawTextWithShadow(this.textRenderer, Text.literal("§6§lHold'em §r§7 blinds 1/2"), 8, 10, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    private String[] names() {
        var list = state.getList("Names", NbtElement.STRING_TYPE);
        String[] out = new String[list.size()];
        for (int i = 0; i < out.length; i++) out[i] = list.getString(i);
        return out;
    }

    private void drawCard(DrawContext context, int x, int y, int card, boolean dim) {
        if (card < 0) {
            context.fill(x, y, x + 16, y + 22, 0xFF000000);
            context.fill(x + 1, y + 1, x + 15, y + 21, dim ? 0xFF3A2020 : CARD_BACK);
            return;
        }
        context.fill(x, y, x + 16, y + 22, 0xFF000000);
        context.fill(x + 1, y + 1, x + 15, y + 21, dim ? 0xFF9A968C : CARD);
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
        String[] suits = {"♠", "♥", "♦", "♣"};
        int suit = card % 4;
        int color = suit == 1 || suit == 2 ? 0xC0202A : 0x1A1A1A;
        context.drawText(this.textRenderer, Text.literal(ranks[card / 4]), x + 2, y + 2, color, false);
        context.drawText(this.textRenderer, Text.literal(suits[suit]), x + 5, y + 12, color, false);
    }

    private static void roundRect(DrawContext context, int x, int y, int w, int h, int color) {
        int r = Math.min(24, h / 3);
        context.fill(x + r, y, x + w - r, y + h, color);
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int inset = (int) Math.round(r - Math.sqrt(r * r - dy * dy));
            context.fill(x + inset, y + i, x + r, y + i + 1, color);
            context.fill(x + inset, y + h - i - 1, x + r, y + h - i, color);
            context.fill(x + w - r, y + i, x + w - inset, y + i + 1, color);
            context.fill(x + w - r, y + h - i - 1, x + w - inset, y + h - i, color);
        }
        context.fill(x, y + r, x + r, y + h - r, color);
        context.fill(x + w - r, y + r, x + w, y + h - r, color);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        // Closing the screen doesn't leave the table; use Cash out. Talk to the dealer to come back.
        super.close();
    }

    /** Screen.render() draws the background itself, and 1.21 allows only one blur per frame: we draw it first, by hand. */
    @Override
    public void renderBackground(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
    }
}
