package com.cursedtides.pirate;

import com.cursedtides.entity.CannonballEntity;
import com.cursedtides.entity.HexBoltEntity;
import com.cursedtides.item.ModItems;
import com.cursedtides.faction.ContractType;
import com.cursedtides.faction.Faction;
import com.cursedtides.faction.FactionData;
import com.cursedtides.quest.QuestData;
import com.cursedtides.world.NpcRestore;
import com.cursedtides.world.Archipelago;
import com.cursedtides.world.ModWorld;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class PirateEvents {
    private PirateEvents() {}

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register(PirateEvents::onDeath);

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            int tick = server.getTicks();
            if (tick % 10 != 0) {
                return;
            }
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                PirateData.regenVoodoo(player, 2 + (int) PerkData.stat(player, Perk.Stat.VOODOO_REGEN)
                        + (int) com.cursedtides.appearance.Appearance.special(player, "voodoo"));
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(CommandManager.literal("pirate").executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                    int notoriety = PirateData.notoriety(player);
                    player.sendMessage(Text.translatable("message.cursedtides.sheet_header",
                            PirateData.title(notoriety).copy().formatted(Formatting.GOLD, Formatting.BOLD),
                            notoriety), false);
                    for (Weapon w : Weapon.values()) {
                        int level = PirateData.level(player, w);
                        int xp = PirateData.xp(player, w);
                        Text progress = level >= PirateData.MAX_LEVEL
                                ? Text.translatable("message.cursedtides.mastered")
                                : Text.literal(xp + " / " + PirateData.xpForLevel(level + 1) + " xp");
                        player.sendMessage(Text.literal("  ")
                                .append(Text.translatable(w.translationKey()).formatted(w.color()))
                                .append(Text.literal("  Lv " + level + "  ").formatted(Formatting.WHITE))
                                .append(progress.copy().formatted(Formatting.GRAY)), false);
                    }
                    player.sendMessage(Text.translatable("message.cursedtides.sheet_voodoo",
                            PirateData.voodoo(player), PirateData.maxVoodoo(player))
                            .formatted(Formatting.DARK_AQUA), false);
                    if (ModWorld.isArchipelago()) {
                        int found = Integer.bitCount(player.getAttachedOrElse(ModWorld.DISCOVERED, 0));
                        player.sendMessage(Text.literal("  Islands discovered: " + found + "/" + Archipelago.ISLANDS.size())
                                .formatted(Formatting.GREEN), false);
                    }
                    for (Faction f : Faction.values()) {
                        int rep = FactionData.rep(player, f);
                        player.sendMessage(Text.literal("  " + f.displayName() + ": ").formatted(f.color())
                                .append(Text.literal(FactionData.rankName(f, rep) + " (" + rep + ")").formatted(Formatting.WHITE)), false);
                    }
                    if (FactionData.isWanted(player)) {
                        player.sendMessage(Text.literal("  WANTED. Bounty: " + FactionData.bounty(player) + " doubloons")
                                .formatted(Formatting.RED, Formatting.BOLD), false);
                    }
                    FactionData.Contract contract = FactionData.contract(player);
                    if (contract.type() != 0) {
                        ContractType t = contract.contractType();
                        player.sendMessage(Text.literal("  Contract: " + t.description() + " (" + contract.progress()
                                + "/" + t.goal() + ")").formatted(Formatting.YELLOW), false);
                    }
                    player.sendMessage(Text.literal("  Goal: ").formatted(Formatting.YELLOW)
                            .append(QuestData.objective(QuestData.stage(player)).copy().formatted(Formatting.GRAY)), false);
                    return 1;
                }).then(CommandManager.literal("respec").executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                    if (PerkData.respec(player)) {
                        player.sendMessage(Text.literal("Your perks are cleared. Press K to choose again.").formatted(Formatting.GOLD), false);
                        return 1;
                    }
                    player.sendMessage(Text.literal("Nothing to reset, or you're short of " + PerkData.RESPEC_COST
                            + " doubloons.").formatted(Formatting.GRAY), false);
                    return 0;
                })).then(CommandManager.literal("appearance").executes(ctx -> {
                    com.cursedtides.character.CharacterData.openScreen(ctx.getSource().getPlayerOrThrow());
                    return 1;
                })).then(CommandManager.literal("contract").then(CommandManager.literal("abandon").executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                    ContractType t = FactionData.contract(player).contractType();
                    if (t == ContractType.NONE) {
                        player.sendMessage(Text.literal("You have no contract.").formatted(Formatting.GRAY), false);
                        return 0;
                    }
                    FactionData.setContract(player, FactionData.Contract.NONE);
                    FactionData.adjust(player, t.giver(), -10);
                    player.sendMessage(Text.literal("Contract abandoned. The " + t.giver().displayName()
                            + " won't forget it entirely (-10 standing).").formatted(Formatting.GRAY), false);
                    return 1;
                })))));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(CommandManager.literal("cursedtides")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("restore_npcs").executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                            int restored = NpcRestore.restoreNear(player);
                            ctx.getSource().sendFeedback(() -> Text.literal("Restored " + restored
                                    + " story character(s) within 96 blocks."), false);
                            return restored;
                        }))));
    }

    private static MutableText voodooBar(ServerPlayerEntity player) {
        int current = PirateData.voodoo(player);
        int max = PirateData.maxVoodoo(player);
        int filled = Math.round(20f * current / max);
        return Text.literal("Voodoo ").formatted(Formatting.DARK_AQUA)
                .append(Text.literal("|".repeat(filled)).formatted(Formatting.AQUA))
                .append(Text.literal("|".repeat(20 - filled)).formatted(Formatting.DARK_GRAY))
                .append(Text.literal(" " + current + "/" + max).formatted(Formatting.GRAY));
    }

    /** Awards mastery XP for the weapon that landed the killing blow. */
    private static void onDeath(LivingEntity victim, DamageSource source) {
        if (!(source.getAttacker() instanceof ServerPlayerEntity player)) {
            return;
        }
        // No XP for murdering shopkeepers.
        if (victim instanceof MerchantEntity || victim instanceof VillagerEntity) {
            return;
        }
        Weapon weapon = weaponFor(player, source.getSource());
        if (weapon == null) {
            return;
        }
        PirateData.addXp(player, weapon, com.cursedtides.combat.Levels.killReputation(player, victim));
    }

    public static Weapon weaponFor(ServerPlayerEntity player, Entity direct) {
        if (direct instanceof CannonballEntity) {
            return Weapon.CANNON;
        }
        if (direct instanceof HexBoltEntity) {
            return Weapon.HEX;
        }
        if (direct instanceof com.cursedtides.entity.ThrowingKnifeEntity) {
            return Weapon.CUTLASS;
        }
        if (direct == player) {
            ItemStack held = player.getMainHandStack();
            if (held.isOf(ModItems.CUTLASS) || held.isOf(ModItems.RAPIER) || held.isOf(ModItems.DAGGER)
                    || held.isOf(ModItems.BOARDING_AXE)) {
                return Weapon.CUTLASS; // the Blades proficiency
            }
            if (held.getItem() instanceof com.cursedtides.item.FlintlockPistolItem) {
                return Weapon.PISTOL;
            }
        }
        return null;
    }
}
