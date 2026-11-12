package com.cursedtides.faction;

import com.cursedtides.entity.CursedPirateEntity;
import com.cursedtides.entity.DrownedCaptainEntity;
import com.cursedtides.entity.MarineEntity;
import com.cursedtides.ship.ShipFaction;
import com.cursedtides.world.Archipelago;
import com.cursedtides.world.ModWorld;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** How deeds change reputation: kills, sinkings, captures. */
public final class FactionEvents {
    private FactionEvents() {}

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register(FactionEvents::onDeath);

        // Remind wanted players when they sail into Tidewatch waters.
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 600 != 0 || !ModWorld.isArchipelago()) return;
            Archipelago.Island fort = Archipelago.IRONWATCH_ISLE;
            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                double dx = p.getX() - fort.cx(), dz = p.getZ() - fort.cz();
                if (FactionData.isWanted(p) && dx * dx + dz * dz < 140 * 140) {
                    p.sendMessage(Text.literal("Ironwatch's guns are watching you. Bounty: "
                            + FactionData.bounty(p) + " doubloons.").formatted(Formatting.RED), true);
                }
            }
        });
    }

    private static void onDeath(LivingEntity victim, DamageSource source) {
        if (!(source.getAttacker() instanceof ServerPlayerEntity player)) {
            return;
        }
        if (victim instanceof MarineEntity) {
            FactionData.adjust(player, Faction.NAVY, -25);
            FactionData.adjust(player, Faction.BRETHREN, 5);
            FactionData.progress(player, ContractType.BRETHREN_MARINES);
        } else if (victim instanceof DrownedCaptainEntity) {
            FactionData.adjust(player, Faction.NAVY, 100);
            FactionData.adjust(player, Faction.BRETHREN, 100);
        } else if (victim instanceof CursedPirateEntity) {
            FactionData.adjust(player, Faction.NAVY, 3);
            FactionData.adjust(player, Faction.BRETHREN, 1);
            FactionData.progress(player, ContractType.NAVY_UNDEAD);
            FactionData.progress(player, ContractType.BRETHREN_UNDEAD);
            com.cursedtides.activity.Achievements.grant(player, "first_blood");
            com.cursedtides.activity.Challenges.progress(player, "undead", 1);
            if (player.getRandom().nextInt(25) == 0) victim.dropStack(com.cursedtides.combat.GearRarity.randomWeapon(player.getServerWorld(), player.getRandom(), 0));
        }
    }

    /** Called by a ship when a player sends her to the bottom. */
    public static void onShipSunk(ServerPlayerEntity sinker, ShipFaction faction) {
        com.cursedtides.profession.ProfessionData.addXp(sinker, com.cursedtides.profession.Profession.LEADERSHIP, 20);
        com.cursedtides.activity.Achievements.grant(sinker, "sink_ship");
        if (sinker.getRandom().nextInt(4) == 0) sinker.giveItemStack(com.cursedtides.explore.Collectibles.randomFor(sinker, com.cursedtides.explore.Collectibles.Kind.LOG));
        com.cursedtides.activity.Challenges.progress(sinker, "sink", 1);
        com.cursedtides.activity.Leaderboards.add(sinker, "sinkings", 1);
        switch (faction) {
            case DROWNED -> {
                FactionData.adjust(sinker, Faction.NAVY, 40);
                FactionData.adjust(sinker, Faction.BRETHREN, 20);
                FactionData.progress(sinker, ContractType.NAVY_GHOST);
            }
            case NAVY -> {
                FactionData.adjust(sinker, Faction.NAVY, -120);
                FactionData.adjust(sinker, Faction.BRETHREN, 40);
                FactionData.progress(sinker, ContractType.BRETHREN_PATROL);
            }
            case MERCHANT -> {
                FactionData.adjust(sinker, Faction.NAVY, -40);
                FactionData.adjust(sinker, Faction.BRETHREN, 25);
                FactionData.adjust(sinker, Faction.SMUGGLERS, 5);
            }
            case PIRATE -> {
                FactionData.adjust(sinker, Faction.NAVY, 30);
                FactionData.adjust(sinker, Faction.BRETHREN, 5);
            }
            default -> { }
        }
    }

    /** Called by a ship when a player captures her. */
    public static void onShipCaptured(ServerPlayerEntity captor, ShipFaction faction) {
        com.cursedtides.profession.ProfessionData.addXp(captor, com.cursedtides.profession.Profession.LEADERSHIP, 35);
        com.cursedtides.activity.Achievements.grant(captor, "capture_ship");
        if (faction == ShipFaction.NAVY) {
            FactionData.adjust(captor, Faction.NAVY, -150);
            FactionData.adjust(captor, Faction.BRETHREN, 60);
            FactionData.progress(captor, ContractType.BRETHREN_PATROL);
        } else if (faction == ShipFaction.DROWNED) {
            FactionData.adjust(captor, Faction.NAVY, 40);
            FactionData.adjust(captor, Faction.BRETHREN, 30);
            FactionData.progress(captor, ContractType.NAVY_GHOST);
        } else if (faction == ShipFaction.MERCHANT) {
            FactionData.adjust(captor, Faction.NAVY, -50);
            FactionData.adjust(captor, Faction.BRETHREN, 35);
            FactionData.adjust(captor, Faction.SMUGGLERS, 15);
            FactionData.progress(captor, ContractType.SMUGGLER_CONVOY);
        } else if (faction == ShipFaction.PIRATE) {
            FactionData.adjust(captor, Faction.NAVY, 30);
        }
    }

    /** Firing on a Tidewatch ship or marine without being wanted costs a little standing each time. */
    public static void onAttackedNavy(ServerPlayerEntity player) {
        FactionData.adjust(player, Faction.NAVY, -4);
    }
}
