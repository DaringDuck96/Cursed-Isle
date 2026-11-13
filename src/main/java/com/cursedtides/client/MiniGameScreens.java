package com.cursedtides.client;

import com.cursedtides.activity.MiniGames;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Random;

/** The brewing board and the hull-patching jobs. */
public final class MiniGameScreens {
    private MiniGameScreens() {}

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(MiniGames.Open.ID, (payload, context) -> context.client().execute(() ->
                context.client().gui.setScreen(payload.kind() == MiniGames.BREW ? new BrewScreen() : new RepairScreen(payload.arg()))));
    }

    // ================================================================== brewing: match three

    public static class BrewScreen extends Screen {
        private static final int N = 7, CELL = 22, KINDS = 5, MOVES = 15;
        private static final int[] COLOURS = {0xFFD94B3A, 0xFF3AA0D9, 0xFF6BC24A, 0xFFE8C547, 0xFFA45FD1};
        private static final String[] SYMBOLS = {"♥", "≈", "♣", "☼", "✦"};
        private final int[][] grid = new int[N][N];
        private final Random random = new Random();
        private int tonic = -1, score, moves = MOVES, selX = -1, selY = -1;
        private String note = "";

        public BrewScreen() {
            super(Component.literal("Brewing"));
            for (int x = 0; x < N; x++) for (int y = 0; y < N; y++) grid[x][y] = fresh(x, y);
        }

        private int fresh(int x, int y) {
            int k;
            do { k = random.nextInt(KINDS); }
            while ((x >= 2 && grid[x - 1][y] == k && grid[x - 2][y] == k) || (y >= 2 && grid[x][y - 1] == k && grid[x][y - 2] == k));
            return k;
        }

        @Override
        protected void init() {
            if (tonic < 0) {
                for (int i = 0; i < MiniGames.TONICS.length; i++) {
                    final int t = i;
                    addRenderableWidget(Button.builder(Component.literal(MiniGames.TONICS[i] + " (" + MiniGames.INGREDIENTS[i] + ")"), b -> {
                        tonic = t;
                        rebuildWidgets();
                    }).bounds(width / 2 - 110, height / 2 - 70 + i * 24, 220, 20).build());
                }
            } else {
                addRenderableWidget(Button.builder(Component.literal("Bottle it"), b -> finish()).bounds(width / 2 - 40, top() + N * CELL + 8, 80, 20).build());
            }
        }

        private int left() { return width / 2 - N * CELL / 2; }
        private int top() { return height / 2 - N * CELL / 2 - 10; }

        @Override
        public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event__, boolean dbl__) {
        double mx = event__.x(), my = event__.y(); int button = UiCompat.button(event__);
            if (tonic >= 0 && moves > 0) {
                int x = (int) ((mx - left()) / CELL), y = (int) ((my - top()) / CELL);
                if (mx >= left() && my >= top() && x < N && y < N) {
                    if (selX < 0) { selX = x; selY = y; }
                    else {
                        if (Math.abs(selX - x) + Math.abs(selY - y) == 1) trySwap(selX, selY, x, y);
                        selX = -1; selY = -1;
                    }
                    return true;
                }
            }
            return super.mouseClicked(event__, dbl__);
        }

        private void trySwap(int ax, int ay, int bx, int by) {
            swap(ax, ay, bx, by);
            int gained = resolve();
            if (gained == 0) {
                swap(ax, ay, bx, by);
                note = "That doesn't mix.";
                return;
            }
            moves--;
            score += gained;
            note = gained >= 300 ? "A fine reaction!" : "Stirring nicely.";
            if (moves == 0) note = "Out of time. Bottle it!";
        }

        private void swap(int ax, int ay, int bx, int by) {
            int t = grid[ax][ay]; grid[ax][ay] = grid[bx][by]; grid[bx][by] = t;
        }

        /** Clears every line of three or more, drops the rest, refills; repeats for cascades. */
        private int resolve() {
            int total = 0, combo = 1;
            while (true) {
                boolean[][] clear = new boolean[N][N];
                int found = 0;
                for (int y = 0; y < N; y++) for (int x = 0; x + 2 < N; x++)
                    if (grid[x][y] == grid[x + 1][y] && grid[x][y] == grid[x + 2][y]) { clear[x][y] = clear[x + 1][y] = clear[x + 2][y] = true; }
                for (int x = 0; x < N; x++) for (int y = 0; y + 2 < N; y++)
                    if (grid[x][y] == grid[x][y + 1] && grid[x][y] == grid[x][y + 2]) { clear[x][y] = clear[x][y + 1] = clear[x][y + 2] = true; }
                for (int x = 0; x < N; x++) for (int y = 0; y < N; y++) if (clear[x][y]) found++;
                if (found == 0) return total;
                total += found * 30 * combo;
                combo++;
                for (int x = 0; x < N; x++) {
                    int write = N - 1;
                    for (int y = N - 1; y >= 0; y--) if (!clear[x][y]) grid[x][write--] = grid[x][y];
                    for (int y = write; y >= 0; y--) grid[x][y] = random.nextInt(KINDS);
                }
            }
        }

        private void finish() {
            ClientPlayNetworking.send(new MiniGames.Result(MiniGames.BREW, tonic, score));
            onClose();
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
            super.extractBackground(ctx, mouseX, mouseY, delta);
            super.extractRenderState(ctx, mouseX, mouseY, delta);
            if (tonic < 0) {
                ctx.centeredText(font, Component.literal("§6What will you brew? §7(needs a glass bottle and the ingredient)"), width / 2, height / 2 - 92, 0xFFFFFFFF);
                return;
            }
            int l = left(), t = top();
            ctx.centeredText(font, Component.literal("§6" + MiniGames.TONICS[tonic] + "§f   score " + score + "   stirs left " + moves),
                    width / 2, t - 24, 0xFFFFFFFF);
            ctx.centeredText(font, Component.literal("§7Swap neighbours to line up three. Fine at 1400, masterwork at 2400."), width / 2, t - 12, 0xFFFFFFFF);
            ctx.fill(l - 3, t - 3, l + N * CELL + 3, t + N * CELL + 3, 0xCC2A1E14);
            for (int x = 0; x < N; x++) {
                for (int y = 0; y < N; y++) {
                    int cx = l + x * CELL, cy = t + y * CELL;
                    boolean sel = x == selX && y == selY;
                    ctx.fill(cx + 1, cy + 1, cx + CELL - 1, cy + CELL - 1, sel ? 0xFFFFFFFF : COLOURS[grid[x][y]]);
                    ctx.centeredText(font, Component.literal(SYMBOLS[grid[x][y]]), cx + CELL / 2, cy + 7, sel ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            ctx.centeredText(font, Component.literal("§e" + note), width / 2, t + N * CELL + 32, 0xFFFFFFFF);
        }

        @Override public void extractBackground(GuiGraphicsExtractor c, int x, int y, float d) { }
        @Override public boolean isPauseScreen() { return false; }
    }

    // ================================================================== patching the hull

    public static class RepairScreen extends Screen {
        private final int job; // 0 hammer, 1 bail, 2 saw
        private int ticks, hits, misses, clicks, lastKey = -1, strokes;
        private boolean done;
        private float marker, dir = 1;
        private static final String[] TITLES = {"Hammer the patch", "Bail her out", "Saw the plank"};
        private static final String[] HELP = {"Press SPACE (or click) when the marker is in the green. Three good blows.",
                "Click as fast as you can! 25 buckets in 6 seconds.", "Saw with A and D, back and forth: 20 strokes in 7 seconds."};

        public RepairScreen(int job) {
            super(Component.literal("Repair"));
            this.job = Math.floorMod(job, 3);
        }

        @Override
        protected void init() {
            if (job == 1) addRenderableWidget(Button.builder(Component.literal("BAIL!"), b -> { if (!done) clicks++; check(); })
                    .bounds(width / 2 - 50, height / 2 + 10, 100, 30).build());
        }

        @Override
        public void tick() {
            if (done) return;
            ticks++;
            if (job == 0) {
                marker += dir * (0.04f + hits * 0.012f);
                if (marker > 1) { marker = 1; dir = -1; }
                if (marker < 0) { marker = 0; dir = 1; }
            }
            int limit = job == 0 ? 400 : job == 1 ? 120 : 140;
            if (ticks >= limit) send(false);
        }

        private void check() {
            if (done) return;
            if (job == 0 && hits >= 3) send(true);
            if (job == 0 && misses >= 3) send(false);
            if (job == 1 && clicks >= 25) send(true);
            if (job == 2 && strokes >= 20) send(true);
        }

        private void strike() {
            if (job != 0 || done) return;
            if (Math.abs(marker - 0.5f) < 0.12f) hits++; else misses++;
            check();
        }

        private void send(boolean ok) {
            done = true;
            ClientPlayNetworking.send(new MiniGames.Result(MiniGames.REPAIR, job, ok ? 1 : 0));
            onClose();
        }

        @Override
        public boolean keyPressed(net.minecraft.client.input.KeyEvent key__) {
        int key = key__.key(), scan = key__.keycode(), mods = key__.modifiers();
            if (job == 0 && key == com.mojang.blaze3d.platform.InputConstants.KEY_SPACE) { strike(); return true; }
            if (job == 2 && (key == com.mojang.blaze3d.platform.InputConstants.KEY_A || key == com.mojang.blaze3d.platform.InputConstants.KEY_D || key == com.mojang.blaze3d.platform.InputConstants.KEY_LEFT || key == com.mojang.blaze3d.platform.InputConstants.KEY_RIGHT)) {
                int side = key == com.mojang.blaze3d.platform.InputConstants.KEY_A || key == com.mojang.blaze3d.platform.InputConstants.KEY_LEFT ? 0 : 1;
                if (side != lastKey) strokes++;
                lastKey = side;
                check();
                return true;
            }
            return super.keyPressed(key__);
        }

        @Override
        public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event__, boolean dbl__) {
        double mx = event__.x(), my = event__.y(); int button = UiCompat.button(event__);
            if (job == 0) { strike(); return true; }
            return super.mouseClicked(event__, dbl__);
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
            super.extractBackground(ctx, mouseX, mouseY, delta);
            super.extractRenderState(ctx, mouseX, mouseY, delta);
            int cx = width / 2, cy = height / 2;
            ctx.centeredText(font, Component.literal("§6" + TITLES[job]), cx, cy - 50, 0xFFFFFFFF);
            ctx.centeredText(font, Component.literal("§7" + HELP[job]), cx, cy - 36, 0xFFFFFFFF);
            if (job == 0) {
                int w = 200, x0 = cx - w / 2;
                ctx.fill(x0, cy - 10, x0 + w, cy + 4, 0xFF4A3424);
                ctx.fill(x0 + (int) (w * 0.38f), cy - 10, x0 + (int) (w * 0.62f), cy + 4, 0xFF3A9A3A);
                int m = x0 + (int) (w * marker);
                ctx.fill(m - 2, cy - 14, m + 2, cy + 8, 0xFFFFFFFF);
                ctx.centeredText(font, Component.literal("Good blows " + hits + "/3   misses " + misses + "/3"), cx, cy + 16, 0xFFFFFFFF);
            } else if (job == 1) {
                ctx.centeredText(font, Component.literal("Buckets: " + clicks + "/25   " + Math.max(0, (120 - ticks) / 20) + "s"), cx, cy - 8, 0xFFFFFFFF);
            } else {
                ctx.centeredText(font, Component.literal("Strokes: " + strokes + "/20   " + Math.max(0, (140 - ticks) / 20) + "s"), cx, cy - 8, 0xFFFFFFFF);
                int off = (strokes % 2 == 0 ? -20 : 20);
                ctx.fill(cx - 60, cy + 10, cx + 60, cy + 16, 0xFF8A6A44);
                ctx.fill(cx + off - 30, cy + 4, cx + off + 30, cy + 10, 0xFFB0B0B8);
            }
        }

        @Override public void extractBackground(GuiGraphicsExtractor c, int x, int y, float d) { }
        @Override public boolean isPauseScreen() { return false; }
    }
}
