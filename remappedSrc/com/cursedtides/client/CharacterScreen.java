package com.cursedtides.client;

import com.cursedtides.character.Background;
import com.cursedtides.character.CharacterPayloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
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
    private EditBox nameField;
    private final Button[] backgroundButtons = new Button[Background.values().length];
    private Button colorButton;
    private final Random random = new Random();

    public CharacterScreen(CharacterPayloads.Open open) {
        super(Component.literal("Your Pirate"));
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
        nameField = new EditBox(this.font, cx - 110, y, 160, 20, Component.literal("Pirate name"));
        nameField.setMaxLength(28);
        nameField.setValue(firstTime ? randomName() : initialName);
        addRenderableWidget(nameField);
        addRenderableWidget(Button.builder(Component.literal("Random"), b -> nameField.setValue(randomName()))
                .bounds(cx + 54, y, 56, 20).build());

        y += 34;
        Background[] all = Background.values();
        for (int i = 0; i < all.length; i++) {
            final int index = i;
            backgroundButtons[i] = addRenderableWidget(Button.builder(Component.literal(all[i].title()), b -> {
                background = index;
                refresh();
            }).bounds(cx - 110 + (i % 2) * 112, y + (i / 2) * 22, 108, 20).build());
            backgroundButtons[i].active = firstTime;
        }

        y += 50 + 40;
        colorButton = addRenderableWidget(Button.builder(Component.literal(""), b -> {
            color = (color + 1) % 16;
            refresh();
        }).bounds(cx - 110, y, 160, 20).build());
        colorButton.active = firstTime;

        addRenderableWidget(Button.builder(Component.literal(firstTime ? "Set sail" : "Save name"), b -> confirm())
                .bounds(cx - 60, this.height - 34, 120, 20).build());
        refresh();
    }

    private void refresh() {
        Background[] all = Background.values();
        for (int i = 0; i < all.length; i++) {
            backgroundButtons[i].setMessage(Component.literal((i == background ? "» " : "") + all[i].title()));
        }
        String colorName = DyeColor.byId(color).getName().replace('_', ' ');
        colorButton.setMessage(Component.literal("Outfit colour: " + colorName));
    }

    private void confirm() {
        ClientPlayNetworking.send(new CharacterPayloads.Create(background, color, nameField.getValue()));
        this.onClose();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        int cx = this.width / 2;
        context.drawCenteredString(this.font,
                Component.literal(firstTime ? "§6§lWho washed up on Driftwood Island?" : "§6§lYour pirate"), cx, 14, 0xFFFFFF);
        context.drawString(this.font, Component.literal("§7Name"), cx - 110, 30, 0xFFFFFF);
        context.drawString(this.font, Component.literal(firstTime ? "§7Background" : "§7Background (chosen)"),
                cx - 110, 64, 0xFFFFFF);

        Background bg = Background.byIndex(background);
        int y = 74 + 46;
        context.drawString(this.font, Component.literal("§f" + bg.story()), cx - 110, y, 0xFFFFFF);
        context.drawString(this.font, Component.literal("§a" + bg.perks()), cx - 110, y + 12, 0xFFFFFF);

        int swatch = 0xFF000000 | DyeColor.byId(color).getTextureDiffuseColor();
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
    public boolean isPauseScreen() {
        return false;
    }

    /** Screen.render() draws the background itself, and 1.21 allows only one blur per frame: we draw it first, by hand. */
    @Override
    public void renderBackground(net.minecraft.client.gui.GuiGraphics context, int mouseX, int mouseY, float delta) {
    }
}
