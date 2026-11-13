package com.cursedtides.client;

import com.cursedtides.pirate.Perk;
import com.cursedtides.pirate.PerkData;
import com.cursedtides.pirate.Weapon;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Six skill trees side by side. Each tier offers two perks; pick one once you reach the
 * tier's level. Hover a perk to read what it does.
 */
public class SkillsScreen extends Screen {
    private static final int BLOCK_W = 186;
    private static final int ROW_H = 22;

    private final PerkData.Skills skills;

    public SkillsScreen(PerkData.Skills skills) {
        super(Component.literal("Skills"));
        this.skills = skills;
    }

    private int columns() {
        return this.width >= BLOCK_W * 3 + 20 ? 3 : 2;
    }

    private int blockX(int i) {
        int cols = columns();
        int total = cols * BLOCK_W;
        return (this.width - total) / 2 + (i % cols) * BLOCK_W + 2;
    }

    private int blockY(int i) {
        return 34 + (i / columns()) * (14 + 3 * ROW_H + 8);
    }

    @Override
    protected void init() {
        Weapon[] trees = Weapon.values();
        for (int t = 0; t < trees.length; t++) {
            Weapon tree = trees[t];
            int level = skills.levels().get(t);
            for (int tier = 0; tier < 3; tier++) {
                int chosen = PerkData.choice(skills.chosen(), tree, tier);
                boolean open = level >= Perk.TIER_LEVELS[tier];
                for (int option = 0; option < 2; option++) {
                    Perk perk = Perk.of(tree, tier, option);
                    if (perk == null) continue;
                    boolean picked = chosen == option + 1;
                    String label = (picked ? "✔ " : "") + perk.title();
                    final int treeIndex = t, tierIndex = tier, opt = option;
                    Button button = Button.builder(Component.literal(label), b -> {
                                ClientPlayNetworking.send(new PerkData.Choose(treeIndex, tierIndex, opt));
                            })
                            .bounds(blockX(t) + option * 91, blockY(t) + 12 + tier * ROW_H, 89, 20)
                            .tooltip(Tooltip.create(Component.literal(perk.effect()
                                    + (open ? "" : "\nUnlocks at level " + Perk.TIER_LEVELS[tier]))))
                            .build();
                    button.active = open && chosen == 0;
                    addRenderableWidget(button);
                }
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractBackground(context, mouseX, mouseY, delta);
        context.centeredText(this.font, Component.literal("§6§lSkills"), this.width / 2, 12, 0xFFFFFFFF);
        context.centeredText(this.font,
                Component.literal("§7Pick one perk per tier (levels 5, 10, 20). /pirate respec resets for "
                        + PerkData.RESPEC_COST + " doubloons."), this.width / 2, 22, 0xFFFFFFFF);
        Weapon[] trees = Weapon.values();
        for (int t = 0; t < trees.length; t++) {
            context.text(this.font,
                    Component.translatable(trees[t].translationKey()).append(Component.literal("  §7Lv " + skills.levels().get(t))),
                    blockX(t), blockY(t), 0xFFF2D9A0);
        }
        super.extractRenderState(context, mouseX, mouseY, delta);
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
