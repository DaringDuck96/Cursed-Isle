package com.cursedtides.client;

import com.cursedtides.pirate.Perk;
import com.cursedtides.pirate.PerkData;
import com.cursedtides.pirate.Weapon;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Six skill trees side by side. Each tier offers two perks; pick one once you reach the
 * tier's level. Hover a perk to read what it does.
 */
public class SkillsScreen extends Screen {
    private static final int BLOCK_W = 186;
    private static final int ROW_H = 22;

    private final PerkData.Skills skills;

    public SkillsScreen(PerkData.Skills skills) {
        super(Text.literal("Skills"));
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
                    ButtonWidget button = ButtonWidget.builder(Text.literal(label), b -> {
                                ClientPlayNetworking.send(new PerkData.Choose(treeIndex, tierIndex, opt));
                            })
                            .dimensions(blockX(t) + option * 91, blockY(t) + 12 + tier * ROW_H, 89, 20)
                            .tooltip(Tooltip.of(Text.literal(perk.effect()
                                    + (open ? "" : "\nUnlocks at level " + Perk.TIER_LEVELS[tier]))))
                            .build();
                    button.active = open && chosen == 0;
                    addDrawableChild(button);
                }
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§6§lSkills"), this.width / 2, 12, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("§7Pick one perk per tier (levels 5, 10, 20). /pirate respec resets for "
                        + PerkData.RESPEC_COST + " doubloons."), this.width / 2, 22, 0xFFFFFF);
        Weapon[] trees = Weapon.values();
        for (int t = 0; t < trees.length; t++) {
            context.drawTextWithShadow(this.textRenderer,
                    Text.translatable(trees[t].translationKey()).append(Text.literal("  §7Lv " + skills.levels().get(t))),
                    blockX(t), blockY(t), 0xF2D9A0);
        }
        super.render(context, mouseX, mouseY, delta);
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
