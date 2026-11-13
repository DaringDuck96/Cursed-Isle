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
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;

public final class PirateEvents {
    private PirateEvents() {}

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register(PirateEvents::onDeath);

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            int tick = server.getTickCount();
            if (tick % 10 != 0) {
                return;
            }
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                PirateData.regenVoodoo(player, 2 + (int) PerkData.stat(player, Perk.Stat.VOODOO_REGEN)
                        + (int) com.cursedtides.appearance.Appearance.special(player, "voodoo"));
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal("pirate").executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    int notoriety = PirateData.notoriety(player);
                    player.sendSystemMessage(Component.translatable("message.cursedtides.sheet_header",
                            PirateData.title(notoriety).copy().withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                            notoriety), false);
                    for (Weapon w : Weapon.values()) {
                        int level = PirateData.level(player, w);
                        int xp = PirateData.xp(player, w);
                        Component progress = level >= PirateData.MAX_LEVEL
                                ? Component.translatable("message.cursedtides.mastered")
                                : Component.literal(xp + " / " + PirateData.xpForLevel(level + 1) + " xp");
                        player.sendSystemMessage(Component.literal("  ")
                                .append(Component.translatable(w.translationKey()).withStyle(w.color()))
                                .append(Component.literal("  Lv " + level + "  ").withStyle(ChatFormatting.WHITE))
                                .append(progress.copy().withStyle(ChatFormatting.GRAY)), false);
                    }
                    player.sendSystemMessage(Component.translatable("message.cursedtides.sheet_voodoo",
                            PirateData.voodoo(player), PirateData.maxVoodoo(player))
                            .withStyle(ChatFormatting.DARK_AQUA), false);
                    if (ModWorld.isArchipelago()) {
                        int found = Integer.bitCount(player.getAttachedOrElse(ModWorld.DISCOVERED, 0));
                        player.sendSystemMessage(Component.literal("  Islands discovered: " + found + "/" + Archipelago.ISLANDS.size())
                                .withStyle(ChatFormatting.GREEN), false);
                    }
                    for (Faction f : Faction.values()) {
                        int rep = FactionData.rep(player, f);
                        player.sendSystemMessage(Component.literal("  " + f.displayName() + ": ").withStyle(f.color())
                                .append(Component.literal(FactionData.rankName(f, rep) + " (" + rep + ")").withStyle(ChatFormatting.WHITE)), false);
                    }
                    if (FactionData.isWanted(player)) {
                        player.sendSystemMessage(Component.literal("  WANTED. Bounty: " + FactionData.bounty(player) + " doubloons")
                                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD), false);
                    }
                    FactionData.Contract contract = FactionData.contract(player);
                    if (contract.type() != 0) {
                        ContractType t = contract.contractType();
                        player.sendSystemMessage(Component.literal("  Contract: " + t.description() + " (" + contract.progress()
                                + "/" + t.goal() + ")").withStyle(ChatFormatting.YELLOW), false);
                    }
                    player.sendSystemMessage(Component.literal("  Goal: ").withStyle(ChatFormatting.YELLOW)
                            .append(QuestData.objective(player).copy().withStyle(ChatFormatting.GRAY)), false);
                    return 1;
                }).then(Commands.literal("respec").executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    if (PerkData.respec(player)) {
                        player.sendSystemMessage(Component.literal("Your perks are cleared. Press K to choose again.").withStyle(ChatFormatting.GOLD), false);
                        return 1;
                    }
                    player.sendSystemMessage(Component.literal("Nothing to reset, or you're short of " + PerkData.RESPEC_COST
                            + " doubloons.").withStyle(ChatFormatting.GRAY), false);
                    return 0;
                })).then(Commands.literal("appearance").executes(ctx -> {
                    com.cursedtides.character.CharacterData.openScreen(ctx.getSource().getPlayerOrException());
                    return 1;
                })).then(Commands.literal("contract").then(Commands.literal("abandon").executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    ContractType t = FactionData.contract(player).contractType();
                    if (t == ContractType.NONE) {
                        player.sendSystemMessage(Component.literal("You have no contract.").withStyle(ChatFormatting.GRAY), false);
                        return 0;
                    }
                    FactionData.setContract(player, FactionData.Contract.NONE);
                    FactionData.adjust(player, t.giver(), -10);
                    player.sendSystemMessage(Component.literal("Contract abandoned. The " + t.giver().displayName()
                            + " won't forget it entirely (-10 standing).").withStyle(ChatFormatting.GRAY), false);
                    return 1;
                })))));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal("cursedtides")
                        .requires(source -> source.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
                        .then(Commands.literal("restore_npcs").executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            int restored = NpcRestore.restoreNear(player);
                            ctx.getSource().sendSuccess(() -> Component.literal("Restored " + restored
                                    + " story character(s) within 96 blocks."), false);
                            return restored;
                        }))));
    }

    private static MutableComponent voodooBar(ServerPlayer player) {
        int current = PirateData.voodoo(player);
        int max = PirateData.maxVoodoo(player);
        int filled = Math.round(20f * current / max);
        return Component.literal("Voodoo ").withStyle(ChatFormatting.DARK_AQUA)
                .append(Component.literal("|".repeat(filled)).withStyle(ChatFormatting.AQUA))
                .append(Component.literal("|".repeat(20 - filled)).withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(" " + current + "/" + max).withStyle(ChatFormatting.GRAY));
    }

    /** Awards mastery XP for the weapon that landed the killing blow. */
    private static void onDeath(LivingEntity victim, DamageSource source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        // No XP for murdering shopkeepers.
        if (victim instanceof AbstractVillager || victim instanceof Villager) {
            return;
        }
        Weapon weapon = weaponFor(player, source.getDirectEntity());
        com.cursedtides.quest.Lessons.onKill(player, weapon, victim);
        if (weapon == null) {
            return;
        }
        PirateData.addXp(player, weapon, com.cursedtides.combat.Levels.killReputation(player, victim));
    }

    public static Weapon weaponFor(ServerPlayer player, Entity direct) {
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
            ItemStack held = player.getMainHandItem();
            if (held.is(ModItems.CUTLASS) || held.is(ModItems.RAPIER) || held.is(ModItems.DAGGER)
                    || held.is(ModItems.BOARDING_AXE)) {
                return Weapon.CUTLASS; // the Blades proficiency
            }
            if (held.getItem() instanceof com.cursedtides.item.FlintlockPistolItem) {
                return Weapon.PISTOL;
            }
            if (held.is(ModItems.HEX_STAFF)) {
                return Weapon.HEX;
            }
            if (com.cursedtides.util.Compat.isBlade(held)) {
                return Weapon.CUTLASS;
            }
        }
        return null;
    }
}
