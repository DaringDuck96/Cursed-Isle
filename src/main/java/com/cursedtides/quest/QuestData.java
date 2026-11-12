package com.cursedtides.quest;

import com.cursedtides.CursedTides;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/** Main story: "The Drowned Bell". Progress is saved per player. */
public final class QuestData {
    private QuestData() {}

    public static final int START = 0;          // find Keeper Wren
    public static final int FIND_YSOLDE = 1;    // sail to Saltmarrow
    public static final int FIND_SHARDS = 2;    // recover both shards, bring them to Tamsin
    public static final int RING_BELL = 3;      // ring the Stilling Bell at Hollowgrave
    public static final int DONE = 4;

    public static final AttachmentType<Integer> STAGE = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT)
            .copyOnDeath()
            .initializer(() -> START)
            .buildAndRegister(CursedTides.id("quest_stage"));

    public static void init() {
        STAGE.identifier();
    }

    public static int stage(PlayerEntity player) {
        return player.getAttachedOrElse(STAGE, START);
    }

    public static void setStage(PlayerEntity player, int stage) {
        player.setAttached(STAGE, stage);
    }

    public static Text objective(int stage) {
        return Text.literal(switch (stage) {
            case START -> "Find Keeper Wren beside the lighthouse on Driftwood Island.";
            case FIND_YSOLDE -> "Row east to Saltmarrow (" + at(com.cursedtides.world.Archipelago.SALTMARROW) + ") and find Harbormistress Ysolde Vane.";
            case FIND_SHARDS -> "Recover the Reef Shard (The Sunken Reach, " + at(com.cursedtides.world.Archipelago.SUNKEN_REACH) + ") and the Ember Shard "
                    + "(smugglers' forge, Ashfall Island, " + com.cursedtides.world.Archipelago.rx(-245, 150) + ", " + com.cursedtides.world.Archipelago.rz(-245, 150) + "). Bring both to Tamsin Coil on Ashfall's east shore.";
            case RING_BELL -> "Ring the Stilling Bell at the altar on Hollowgrave Island (" + com.cursedtides.world.Archipelago.ALTAR_X + ", " + com.cursedtides.world.Archipelago.ALTAR_Z + ").";
            default -> "The Drowned Bell is silent. The Shattered Reach is yours to roam.";
        });
    }

    public static boolean has(PlayerEntity player, Item item) {
        return player.getInventory().count(item) > 0;
    }

    public static void take(PlayerEntity player, Item item) {
        var inv = player.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isOf(item)) {
                stack.decrement(1);
                return;
            }
        }
    }

    public static void give(PlayerEntity player, ItemStack stack) {
        if (!player.getInventory().insertStack(stack)) {
            player.dropItem(stack, false);
        }
    }

    private static String at(com.cursedtides.world.Archipelago.Island is) {
        return is.cx() + ", " + is.cz();
    }
}
