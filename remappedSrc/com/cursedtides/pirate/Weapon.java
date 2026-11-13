package com.cursedtides.pirate;

import com.cursedtides.CursedTides;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.ChatFormatting;

/**
 * The proficiencies a pirate can master, each with its own saved XP. Weapon skills
 * level from kills; Sailing from distance sailed; Exploration from discovering islands.
 * (Kept under the name "Weapon" so older saves keep their data.)
 */
public enum Weapon {
    CUTLASS("cutlass", ChatFormatting.WHITE),
    PISTOL("pistol", ChatFormatting.GOLD),
    CANNON("cannon", ChatFormatting.DARK_GRAY),
    HEX("hex", ChatFormatting.DARK_AQUA),
    SAILING("sailing", ChatFormatting.AQUA),
    EXPLORATION("exploration", ChatFormatting.GREEN);

    private final String key;
    private final ChatFormatting color;
    private final AttachmentType<Integer> xpType;

    Weapon(String key, ChatFormatting color) {
        this.key = key;
        this.color = color;
        this.xpType = AttachmentRegistry.<Integer>builder()
                .persistent(Codec.INT)
                .copyOnDeath()
                .initializer(() -> 0)
                .buildAndRegister(CursedTides.id("mastery_" + key));
    }

    public String translationKey() {
        return "mastery.cursedtides." + key;
    }

    public ChatFormatting color() {
        return color;
    }

    AttachmentType<Integer> xpType() {
        return xpType;
    }
}
