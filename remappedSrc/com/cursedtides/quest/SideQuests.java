package com.cursedtides.quest;

import com.cursedtides.CursedTides;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** Side quest progress, one number per quest. */
public final class SideQuests {
    private SideQuests() {}

    private static AttachmentType<Integer> quest(String name) {
        return AttachmentRegistry.<Integer>builder().persistent(Codec.INT).copyOnDeath().initializer(() -> 0)
                .buildAndRegister(CursedTides.id("quest_" + name));
    }

    /** The Governor's Ledger: 0 unknown, 1 asked by the Governor, 2 returned to the Governor, 3 sold to Rook. */
    public static final AttachmentType<Integer> LEDGER = quest("ledger");
    /** Specimens of the Reach: 0 unknown, 1 asked, 2 done. */
    public static final AttachmentType<Integer> SPECIMENS = quest("specimens");
    /** The Restless Shade: 0 unknown, 1 asked, 2 shade laid to rest, 3 done. */
    public static final AttachmentType<Integer> SHADE = quest("shade");
    /** Down the Deep Seam: 0 unknown, 1 asked, 2 done. */
    public static final AttachmentType<Integer> SEAM = quest("seam");

    public static final int LEDGER_RETURNED = 2, LEDGER_SOLD = 3;

    public static int get(Player p, AttachmentType<Integer> q) {
        return p.getAttachedOrElse(q, 0);
    }

    public static void set(Player p, AttachmentType<Integer> q, int v) {
        p.setAttached(q, v);
    }

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!entity.getTags().contains("cursedtides_shade")) return;
            if (source.getEntity() instanceof ServerPlayer player && get(player, SHADE) < 2) {
                set(player, SHADE, 2);
                player.displayClientMessage(Component.literal("The shade lets out a long sigh and is gone. Sorrel will want to know.")
                        .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC), false);
            }
        });
    }

    /** Lines for the journal. */
    public static java.util.List<String> journal(Player p) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        switch (get(p, LEDGER)) {
            case 1 -> lines.add("§fThe Governor's Ledger: §7find the ledger Captain Rook stole (Blackthorn Isle, 150, -330). Return it, or not.");
            case LEDGER_RETURNED -> lines.add("§a✔ The Governor's Ledger §7(returned to Governor Harrow)");
            case LEDGER_SOLD -> lines.add("§a✔ The Governor's Ledger §7(sold to Captain Rook)");
            default -> { }
        }
        switch (get(p, SPECIMENS)) {
            case 1 -> lines.add("§fSpecimens of the Reach: §7bring Ibbie Crane a bucket of tropical fish, a sea pickle and a blue orchid.");
            case 2 -> lines.add("§a✔ Specimens of the Reach");
            default -> { }
        }
        switch (get(p, SHADE)) {
            case 1 -> lines.add("§fThe Restless Shade: §7lay Lady Morrow's shade to rest in the mansion on Whispering Key.");
            case 2 -> lines.add("§fThe Restless Shade: §7the shade is gone. Tell Sorrel.");
            case 3 -> lines.add("§a✔ The Restless Shade");
            default -> { }
        }
        switch (get(p, SEAM)) {
            case 1 -> lines.add("§fDown the Deep Seam: §7open the vault at the bottom of The Deep Seam on Goldwater Cay.");
            case 2 -> lines.add("§a✔ Down the Deep Seam");
            default -> { }
        }
        if (lines.isEmpty()) lines.add("§7None yet. Islanders on the farther islands may have work for you.");
        return lines;
    }
}
