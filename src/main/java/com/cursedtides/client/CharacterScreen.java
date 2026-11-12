package com.cursedtides.client;

import com.cursedtides.character.Background;
import com.cursedtides.character.CharacterPayloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;

import java.util.Random;

/** Make your pirate: a name, where you came from, and the colour of your clothes. */
public class CharacterScreen extends Screen {
    private static final String[] FIRST = {"Mara", "Tobias", "Wren", "Isolde", "Caspian", "Bess", "Hollis", "Nell", "Ezra",
            "Rowan", "Delphine", "Silas", "Odette", "Barnaby", "Liesl", "Ambrose", "Kit", "Sable", "Jory", "Petra"};
    private static final String[] EPITHET = {"Saltwhistle", "Blackbrine", "the Lantern", "Quickhand", "Ashcombe",
            "Graywater", "the Gull", "Redmoor", "Stormcrow", "Tallowmere", "Ninefingers", "Coldharbour", "the Fox",
            "Wavecutter", "Brightpenny", "Ironsides", "Mudlark", "Cinderly", "Hollowell", "Stillwater"};

    private final boolean firstTime;
    private final String initialName;
    private int background;
    private int color = DyeColor.RED.getId();
    private TextFieldWidget nameField;
    private final ButtonWidget[] backgroundButtons = new ButtonWidget[Background.values().length];
    private ButtonWidget colorButton;
    private final Random random = new Random();

    public CharacterScreen(CharacterPayloads.Open open) {
        super(Text.literal("Your Pirate"));
        this.firstTime = open.firstTime();
        this.initialName = open.currentName();
        this.background = open.background();
    }

    private String randomName() {
        return FIRST[random.nextInt(FIRST.length)] + " " + EPITHET[random.nextInt(EPITHET.length)];
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = 40;
        nameField = new TextFieldWidget(this.textRenderer, cx - 110, y, 160, 20, Text.literal("Pirate name"));
        nameField.setMaxLength(28);
        nameField.setText(firstTime ? randomName() : initialName);
        addDrawableChild(nameField);
        addDrawableChild(ButtonWidget.builder(Text.literal("Random"), b -> nameField.setText(randomName()))
                .dimensions(cx + 54, y, 56, 20).build());

        y += 34;
        Background[] all = Background.values();
        for (int i = 0; i < all.length; i++) {
            final int index = i;
            backgroundButtons[i] = addDrawableChild(ButtonWidget.builder(Text.literal(all[i].title()), b -> {
                background = index;
                refresh();
            }).dimensions(cx - 110 + (i % 2) * 112, y + (i / 2) * 22, 108, 20).build());
            backgroundButtons[i].active = firstTime;
        }

        y += 50 + 40;
        colorButton = addDrawableChild(ButtonWidget.builder(Text.literal(""), b -> {
            color = (color + 1) % 16;
            refresh();
        }).dimensions(cx - 110, y, 160, 20).build());
        colorButton.active = firstTime;

        addDrawableChild(ButtonWidget.builder(Text.literal(firstTime ? "Set sail" : "Save name"), b -> confirm())
                .dimensions(cx - 60, this.height - 34, 120, 20).build());
        refresh();
    }

    private void refresh() {
        Background[] all = Background.values();
        for (int i = 0; i < all.length; i++) {
            backgroundButtons[i].setMessage(Text.literal((i == background ? "» " : "") + all[i].title()));
        }
        String colorName = DyeColor.byId(color).getName().replace('_', ' ');
        colorButton.setMessage(Text.literal("Outfit colour: " + colorName));
    }

    private void confirm() {
        ClientPlayNetworking.send(new CharacterPayloads.Create(background, color, nameField.getText()));
        this.close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        int cx = this.width / 2;
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal(firstTime ? "§6§lWho washed up on Driftwood Island?" : "§6§lYour pirate"), cx, 14, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, Text.literal("§7Name"), cx - 110, 30, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, Text.literal(firstTime ? "§7Background" : "§7Background (chosen)"),
                cx - 110, 64, 0xFFFFFF);

        Background bg = Background.byIndex(background);
        int y = 74 + 46;
        context.drawTextWithShadow(this.textRenderer, Text.literal("§f" + bg.story()), cx - 110, y, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, Text.literal("§a" + bg.perks()), cx - 110, y + 12, 0xFFFFFF);

        int swatch = 0xFF000000 | DyeColor.byId(color).getEntityColor();
        int sy = y + 40;
        context.fill(cx + 56, sy, cx + 110, sy + 20, 0xFF000000);
        context.fill(cx + 57, sy + 1, cx + 109, sy + 19, swatch);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !firstTime;
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
