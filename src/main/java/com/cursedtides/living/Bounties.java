package com.cursedtides.living;

import net.minecraft.world.entity.EntityTypes;
import com.cursedtides.faction.Faction;
import com.cursedtides.faction.FactionData;
import com.cursedtides.item.ModItems;
import com.cursedtides.social.Duels;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static net.minecraft.commands.Commands.literal;

/**
 * Prices on heads. Wanted pirates draw bounty hunters out of the brush; anyone who isn't wanted can claim a wanted
 * player's bounty by beating them, which also wipes some of the slate clean for the one who fell.
 */
public final class Bounties {
    private Bounties() {}

    public static final String HUNTER_TAG = "cursedtides_hunter";
    private static final String[] HUNTER_NAMES = {"Marrow Quint", "Sable Hart", "Ezra Lusk", "The Grey Widow", "Cutter Fane",
            "Bram Oakes", "Nell Corrigan", "Jasper Slate"};

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(Bounties::tick);
        ServerLivingEntityEvents.AFTER_DEATH.register((victim, source) -> {
            if (victim.entityTags().contains(HUNTER_TAG)) {
                com.cursedtides.util.Compat.drop(victim, new ItemStack(ModItems.DOUBLOON, 4 + victim.getRandom().nextInt(6)));
                if (victim.getRandom().nextInt(4) == 0) com.cursedtides.util.Compat.drop(victim, new ItemStack(ModItems.WANTED_POSTER));
                return;
            }
            if (!(victim instanceof ServerPlayer dead) || !(source.getEntity() instanceof ServerPlayer killer)) return;
            if (killer == dead || Duels.inDuel(dead.getUUID()) || !FactionData.isWanted(dead) || FactionData.isWanted(killer)) return;
            int bounty = FactionData.bounty(dead);
            killer.addItem(new ItemStack(ModItems.DOUBLOON, bounty));
            com.cursedtides.activity.Achievements.grant(killer, "bounty_claimed");
            FactionData.adjust(dead, Faction.NAVY, 60);
            FactionData.adjust(killer, Faction.NAVY, 20);
            dead.level().getServer().getPlayerList().broadcastSystemMessage(Component.literal(killer.getName().getString() + " has claimed the "
                    + bounty + "-doubloon bounty on " + dead.getName().getString() + "!").withStyle(ChatFormatting.GOLD), false);
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> dispatcher.register(literal("bounties").executes(ctx -> {
            ServerPlayer p = ctx.getSource().getPlayerOrException();
            for (Component line : board(ctx.getSource().getServer())) p.sendSystemMessage(line, false);
            return 1;
        })));
    }

    /** The wanted list: every pirate with a price on their head, highest first. */
    public static List<Component> board(MinecraftServer server) {
        List<ServerPlayer> wanted = new ArrayList<>(server.getPlayerList().getPlayers().stream().filter(FactionData::isWanted).toList());
        wanted.sort(Comparator.comparingInt(FactionData::bounty).reversed());
        List<Component> out = new ArrayList<>();
        out.add(Component.literal("— WANTED, by order of the Tidewatch —").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        if (wanted.isEmpty()) out.add(Component.literal("  No pirate in these waters is worth the ink today.").withStyle(ChatFormatting.GRAY));
        for (ServerPlayer w : wanted) {
            out.add(Component.literal("  " + com.cursedtides.character.CharacterData.pirateName(w) + " (" + w.getName().getString() + ")  ")
                    .withStyle(ChatFormatting.WHITE).append(Component.literal(FactionData.bounty(w) + " doubloons").withStyle(ChatFormatting.GOLD)));
        }
        out.add(Component.literal("  Bring them down (if you're not wanted yourself) to claim it.").withStyle(ChatFormatting.DARK_GRAY));
        return out;
    }

    private static void tick(MinecraftServer server) {
        if (server.getTickCount() % 4800 != 0 || server.getTickCount() == 0) return;
        ServerLevel world = server.overworld();
        for (ServerPlayer p : world.players()) {
            if (!FactionData.isWanted(p) || FactionData.bounty(p) < 20 || p.isPassenger() || p.isCreative() || p.isSpectator()) continue;
            if (world.getRandom().nextFloat() > 0.25f + FactionData.bounty(p) / 200f) continue;
            ambush(world, p);
        }
    }

    public static int ambush(ServerLevel world, ServerPlayer p) {
        int count = 2 + Math.min(3, FactionData.bounty(p) / 20);
        int spawned = 0;
        String leader = HUNTER_NAMES[world.getRandom().nextInt(HUNTER_NAMES.length)];
        for (int i = 0; i < count; i++) {
            double a = world.getRandom().nextDouble() * Math.PI * 2;
            BlockPos at = Residents.openGround(world, (int) (p.getX() + Math.cos(a) * 14), (int) (p.getZ() + Math.sin(a) * 14), 6);
            if (at == null) continue;
            Monster hunter = (i % 2 == 0 ? EntityTypes.VINDICATOR : EntityTypes.PILLAGER).create(world, EntitySpawnReason.EVENT);
            if (hunter == null) continue;
            hunter.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0, 0);
            hunter.finalizeSpawn(world, world.getCurrentDifficultyAt(at), EntitySpawnReason.EVENT, null);
            hunter.setCustomName(Component.literal(i == 0 ? leader : "Hunter in " + leader.split(" ")[0] + "'s pay").withStyle(ChatFormatting.DARK_RED));
            hunter.setCustomNameVisible(i == 0);
            hunter.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET)); // keeps the sun off
            hunter.addTag(HUNTER_TAG);
            hunter.setTarget(p);
            hunter.setPersistenceRequired();
            Residents.markTransient(hunter, world.getGameTime() + 6000);
            world.addFreshEntity(hunter);
            spawned++;
        }
        if (spawned > 0) {
            p.sendSystemMessage(Component.literal(leader + ": \"" + com.cursedtides.character.CharacterData.pirateName(p) + "! There's "
                    + FactionData.bounty(p) + " doubloons says you come quietly.\"").withStyle(ChatFormatting.RED), false);
            world.playSound(null, p.blockPosition(), SoundEvents.RAID_HORN.value(), SoundSource.HOSTILE, 0.6f, 1.4f);
        }
        return spawned;
    }
}
