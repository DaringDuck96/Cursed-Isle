package com.cursedtides.faction;

import com.cursedtides.CursedTides;
import com.cursedtides.item.ModItems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

/**
 * Reputation (-1000..1000) with each faction, ranks and one-time rank rewards,
 * Navy infamy (wanted status and bounty), and the player's active contract.
 */
public final class FactionData {
    private FactionData() {}

    public static final int WANTED_AT = -100;
    public static final int MIN = -1000, MAX = 1000;

    public record Contract(int type, int progress) {
        public static final Contract NONE = new Contract(0, 0);
        public static final Codec<Contract> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("type").forGetter(Contract::type),
                Codec.INT.fieldOf("progress").forGetter(Contract::progress)
        ).apply(i, Contract::new));

        public ContractType contractType() {
            return ContractType.byId(type);
        }
    }

    private static AttachmentType<Integer> intAttachment(String name) {
        return AttachmentRegistry.<Integer>builder()
                .persistent(Codec.INT)
                .copyOnDeath()
                .initializer(() -> 0)
                .buildAndRegister(CursedTides.id(name));
    }

    public static final AttachmentType<Integer> NAVY_REP = intAttachment("rep_navy");
    public static final AttachmentType<Integer> BRETHREN_REP = intAttachment("rep_brethren");
    public static final AttachmentType<Integer> SMUGGLER_REP = intAttachment("rep_smugglers");
    /** Bits: 0 navy rank 300, 1 navy rank 600, 2 brethren rank 300, 3 brethren rank 600. */
    public static final AttachmentType<Integer> REWARDS = intAttachment("rank_rewards");
    public static final AttachmentType<Contract> CONTRACT = AttachmentRegistry.<Contract>builder()
            .persistent(Contract.CODEC)
            .copyOnDeath()
            .initializer(() -> Contract.NONE)
            .buildAndRegister(CursedTides.id("contract"));

    public static void init() {
        NAVY_REP.identifier();
    }

    private static AttachmentType<Integer> type(Faction f) {
        return switch (f) {
            case NAVY -> NAVY_REP;
            case BRETHREN -> BRETHREN_REP;
            case SMUGGLERS -> SMUGGLER_REP;
        };
    }

    // ------------------------------------------------------------------ reputation

    public static int rep(PlayerEntity player, Faction faction) {
        return player.getAttachedOrElse(type(faction), 0);
    }

    public static void set(ServerPlayerEntity player, Faction faction, int value) {
        adjust(player, faction, value - rep(player, faction));
    }

    public static void adjust(ServerPlayerEntity player, Faction faction, int delta) {
        if (delta == 0) return;
        int before = rep(player, faction);
        int after = MathHelper.clamp(before + delta, MIN, MAX);
        player.setAttached(type(faction), after);

        String oldRank = rankName(faction, before);
        String newRank = rankName(faction, after);
        if (!oldRank.equals(newRank)) {
            player.sendMessage(Text.literal(faction.displayName() + ": you are now ").formatted(Formatting.GRAY)
                    .append(Text.literal(newRank).formatted(faction.color(), Formatting.BOLD)), false);
        }
        if (faction == Faction.NAVY) {
            if (before > WANTED_AT && after <= WANTED_AT) {
                com.cursedtides.activity.Achievements.grant(player, "wanted");
                player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("WANTED").formatted(Formatting.DARK_RED)));
                player.networkHandler.sendPacket(new SubtitleS2CPacket(
                        Text.literal("Bounty: " + bounty(player) + " doubloons").formatted(Formatting.GOLD)));
                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLOCK_BELL_USE, SoundCategory.PLAYERS, 1.0f, 0.5f);
                player.sendMessage(Text.literal("The Tidewatch has put a price on your head. Marines will fire on sight "
                        + "and patrols will hunt you. The Commodore on Ironwatch Isle takes payment for pardons.")
                        .formatted(Formatting.RED), false);
            } else if (before <= WANTED_AT && after > WANTED_AT) {
                player.sendMessage(Text.literal("Your name is cleared with the Tidewatch.").formatted(Formatting.GREEN), false);
            }
        }
        grantRankRewards(player, faction, after);
    }

    public static String rankName(Faction faction, int rep) {
        if (faction == Faction.NAVY) {
            if (rep <= -500) return "Most Wanted";
            if (rep <= WANTED_AT) return "Wanted";
            if (rep < 100) return "Unknown to the Tidewatch";
            if (rep < 300) return "Friend of the Tidewatch";
            if (rep < 600) return "Privateer";
            return "Commodore's Right Hand";
        }
        if (faction == Faction.SMUGGLERS) {
            if (rep < -100) return "Informer";
            if (rep < 100) return "Stranger";
            if (rep < 300) return "Runner";
            if (rep < 600) return "Smuggler";
            return "Shadow Captain";
        }
        if (rep < -100) return "Turncoat";
        if (rep < 100) return "Outsider";
        if (rep < 300) return "Brethren Deckhand";
        if (rep < 600) return "Sworn Brethren";
        return "Brethren Captain";
    }

    private static void grantRankRewards(ServerPlayerEntity player, Faction faction, int rep) {
        int mask = player.getAttachedOrElse(REWARDS, 0);
        int base = faction == Faction.NAVY ? 0 : faction == Faction.BRETHREN ? 2 : 4;
        if (faction == Faction.SMUGGLERS && rep >= 300 && (mask & (1 << base)) == 0) {
            mask |= 1 << base;
            give(player, new ItemStack(ModItems.CARGO_BULKHEAD, 2));
            give(player, new ItemStack(ModItems.EXPLOSIVE_SHELL, 8));
            player.sendMessage(Text.literal("The smugglers of Raven's Landing look after their own: two hidden-hold bulkheads and a crate of shells.")
                    .formatted(Formatting.GOLD), false);
        }
        if (faction != Faction.SMUGGLERS && rep >= 300 && (mask & (1 << base)) == 0) {
            mask |= 1 << base;
            ItemStack gun = new ItemStack(faction == Faction.NAVY ? ModItems.MUSKET : ModItems.BLUNDERBUSS);
            give(player, gun);
            give(player, new ItemStack(ModItems.PISTOL_SHOT, 16));
            // and the clothes to match
            (faction == Faction.NAVY ? com.cursedtides.item.ModClothing.NAVY_SET
                    : com.cursedtides.item.ModClothing.CAPTAIN_SET).forEach(item -> give(player, new ItemStack(item)));
            player.sendMessage(Text.literal("For your service, the " + faction.displayName() + " sends you a gift: ")
                    .append(gun.getName()).formatted(Formatting.GOLD), false);
        }
        if (rep >= 600 && (mask & (1 << (base + 1))) == 0) {
            mask |= 1 << (base + 1);
            give(player, new ItemStack(ModItems.DOUBLOON, 64));
            player.sendMessage(Text.literal("The " + faction.displayName() + " counts you among its finest. 64 doubloons, with thanks.")
                    .formatted(Formatting.GOLD), false);
        }
        player.setAttached(REWARDS, mask);
    }

    // ------------------------------------------------------------------ infamy

    public static boolean isWanted(PlayerEntity player) {
        return rep(player, Faction.NAVY) <= WANTED_AT;
    }

    public static int bounty(PlayerEntity player) {
        int navy = rep(player, Faction.NAVY);
        return navy > WANTED_AT ? 0 : Math.min(64, 15 + (-navy) / 16);
    }

    // ------------------------------------------------------------------ contracts

    public static Contract contract(PlayerEntity player) {
        return player.getAttachedOrElse(CONTRACT, Contract.NONE);
    }

    public static void setContract(PlayerEntity player, Contract contract) {
        player.setAttached(CONTRACT, contract);
    }

    /** Advances the player's contract if it is of this type. */
    public static void progress(ServerPlayerEntity player, ContractType type) {
        Contract c = contract(player);
        if (c.type() != type.id() || c.progress() >= type.goal()) {
            return;
        }
        int now = c.progress() + 1;
        setContract(player, new Contract(c.type(), now));
        if (now >= type.goal()) {
            player.sendMessage(Text.literal("Contract complete: " + type.description() + ". Return to your "
                    + (type.giver() == Faction.NAVY ? "Commodore" : type.giver() == Faction.SMUGGLERS ? "Smugglers' Queen" : "Broker")
                    + " for payment.").formatted(Formatting.GOLD), false);
            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 0.5f, 1.4f);
        } else {
            player.sendMessage(Text.literal(type.description() + ": " + now + "/" + type.goal()).formatted(Formatting.GRAY), true);
        }
    }

    static void give(PlayerEntity player, ItemStack stack) {
        if (!player.getInventory().insertStack(stack)) {
            player.dropItem(stack, false);
        }
    }
}
