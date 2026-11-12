package com.cursedtides.pirate;

import com.cursedtides.CursedTides;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Formatting;

/**
 * The proficiencies a pirate can master, each with its own saved XP. Weapon skills
 * level from kills; Sailing from distance sailed; Exploration from discovering islands.
 * (Kept under the name "Weapon" so older saves keep their data.)
 */
public enum Weapon {
    CUTLASS("cutlass", Formatting.WHITE),
    PISTOL("pistol", Formatting.GOLD),
    CANNON("cannon", Formatting.DARK_GRAY),
    HEX("hex", Formatting.DARK_AQUA),
    SAILING("sailing", Formatting.AQUA),
    EXPLORATION("exploration", Formatting.GREEN);

    private final String key;
    private final Formatting color;
    private final AttachmentType<Integer> xpType;

    Weapon(String key, Formatting color) {
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

    public Formatting color() {
        return color;
    }

    AttachmentType<Integer> xpType() {
        return xpType;
    }
}
