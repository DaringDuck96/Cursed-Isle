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
            case FIND_YSOLDE -> "Sail north-east to Saltmarrow (230, -30) and find Harbormistress Ysolde Vane.";
            case FIND_SHARDS -> "Recover the Reef Shard (The Sunken Reach, 70, 240) and the Ember Shard "
                    + "(smugglers' forge, Ashfall Island, -245, 150). Bring both to Tamsin Coil on Ashfall's east shore.";
            case RING_BELL -> "Ring the Stilling Bell at the altar on Hollowgrave Island (-110, -250).";
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
}
