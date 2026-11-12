package com.cursedtides.living;

import com.cursedtides.faction.Faction;
import com.cursedtides.faction.FactionData;
import com.cursedtides.item.ModItems;
import com.cursedtides.social.Duels;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static net.minecraft.server.command.CommandManager.literal;

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
            if (victim.getCommandTags().contains(HUNTER_TAG)) {
                victim.dropStack(new ItemStack(ModItems.DOUBLOON, 4 + victim.getRandom().nextInt(6)));
                if (victim.getRandom().nextInt(4) == 0) victim.dropStack(new ItemStack(ModItems.WANTED_POSTER));
                return;
            }
            if (!(victim instanceof ServerPlayerEntity dead) || !(source.getAttacker() instanceof ServerPlayerEntity killer)) return;
            if (killer == dead || Duels.inDuel(dead.getUuid()) || !FactionData.isWanted(dead) || FactionData.isWanted(killer)) return;
            int bounty = FactionData.bounty(dead);
            killer.giveItemStack(new ItemStack(ModItems.DOUBLOON, bounty));
            com.cursedtides.activity.Achievements.grant(killer, "bounty_claimed");
            FactionData.adjust(dead, Faction.NAVY, 60);
            FactionData.adjust(killer, Faction.NAVY, 20);
            dead.getServer().getPlayerManager().broadcast(Text.literal(killer.getName().getString() + " has claimed the "
                    + bounty + "-doubloon bounty on " + dead.getName().getString() + "!").formatted(Formatting.GOLD), false);
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> dispatcher.register(literal("bounties").executes(ctx -> {
            ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
            for (Text line : board(ctx.getSource().getServer())) p.sendMessage(line, false);
            return 1;
        })));
    }

    /** The wanted list: every pirate with a price on their head, highest first. */
    public static List<Text> board(MinecraftServer server) {
        List<ServerPlayerEntity> wanted = new ArrayList<>(server.getPlayerManager().getPlayerList().stream().filter(FactionData::isWanted).toList());
        wanted.sort(Comparator.comparingInt(FactionData::bounty).reversed());
        List<Text> out = new ArrayList<>();
        out.add(Text.literal("— WANTED, by order of the Tidewatch —").formatted(Formatting.DARK_RED, Formatting.BOLD));
        if (wanted.isEmpty()) out.add(Text.literal("  No pirate in these waters is worth the ink today.").formatted(Formatting.GRAY));
        for (ServerPlayerEntity w : wanted) {
            out.add(Text.literal("  " + com.cursedtides.character.CharacterData.pirateName(w) + " (" + w.getName().getString() + ")  ")
                    .formatted(Formatting.WHITE).append(Text.literal(FactionData.bounty(w) + " doubloons").formatted(Formatting.GOLD)));
        }
        out.add(Text.literal("  Bring them down (if you're not wanted yourself) to claim it.").formatted(Formatting.DARK_GRAY));
        return out;
    }

    private static void tick(MinecraftServer server) {
        if (server.getTicks() % 4800 != 0 || server.getTicks() == 0) return;
        ServerWorld world = server.getOverworld();
        for (ServerPlayerEntity p : world.getPlayers()) {
            if (!FactionData.isWanted(p) || FactionData.bounty(p) < 20 || p.hasVehicle() || p.isCreative() || p.isSpectator()) continue;
            if (world.random.nextFloat() > 0.25f + FactionData.bounty(p) / 200f) continue;
            ambush(world, p);
        }
    }

    public static int ambush(ServerWorld world, ServerPlayerEntity p) {
        int count = 2 + Math.min(3, FactionData.bounty(p) / 20);
        int spawned = 0;
        String leader = HUNTER_NAMES[world.random.nextInt(HUNTER_NAMES.length)];
        for (int i = 0; i < count; i++) {
            double a = world.random.nextDouble() * Math.PI * 2;
            BlockPos at = Residents.openGround(world, (int) (p.getX() + Math.cos(a) * 14), (int) (p.getZ() + Math.sin(a) * 14), 6);
            if (at == null) continue;
            HostileEntity hunter = (i % 2 == 0 ? EntityType.VINDICATOR : EntityType.PILLAGER).create(world);
            if (hunter == null) continue;
            hunter.refreshPositionAndAngles(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0, 0);
            hunter.initialize(world, world.getLocalDifficulty(at), SpawnReason.EVENT, null);
            hunter.setCustomName(Text.literal(i == 0 ? leader : "Hunter in " + leader.split(" ")[0] + "'s pay").formatted(Formatting.DARK_RED));
            hunter.setCustomNameVisible(i == 0);
            hunter.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET)); // keeps the sun off
            hunter.addCommandTag(HUNTER_TAG);
            hunter.setTarget(p);
            hunter.setPersistent();
            Residents.markTransient(hunter, world.getTime() + 6000);
            world.spawnEntity(hunter);
            spawned++;
        }
        if (spawned > 0) {
            p.sendMessage(Text.literal(leader + ": \"" + com.cursedtides.character.CharacterData.pirateName(p) + "! There's "
                    + FactionData.bounty(p) + " doubloons says you come quietly.\"").formatted(Formatting.RED), false);
            world.playSound(null, p.getBlockPos(), SoundEvents.EVENT_RAID_HORN.value(), SoundCategory.HOSTILE, 0.6f, 1.4f);
        }
        return spawned;
    }
}
