package com.cursedtides.trade;

import com.cursedtides.entity.MarineEntity;
import com.cursedtides.entity.ShipEntity;
import com.cursedtides.faction.ContractType;
import com.cursedtides.faction.Faction;
import com.cursedtides.faction.FactionData;
import com.cursedtides.ship.ShipFaction;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tidewatch inspections. A patrol that comes within hailing distance of a ship carrying contraband
 * orders her to heave to. Stay close for fifteen seconds and she's searched; open the range and you've
 * given them the slip. Marines in port sometimes search pockets too.
 */
public final class Smuggling {
    private Smuggling() {}

    private static final double HAIL = 40, ESCAPE = 70;
    private static final int SEARCH_TICKS = 20 * 15;

    private record Inspection(UUID patrol, int deadline) {}
    private static final Map<UUID, Inspection> ACTIVE = new HashMap<>();
    private static final Map<UUID, Integer> COOLDOWN = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(Smuggling::tick);
    }

    private static void tick(MinecraftServer server) {
        if (server.getTickCount() % 20 != 0) return;
        int now = server.getTickCount();
        ServerLevel world = server.overworld();
        for (ServerPlayer p : world.players()) {
            if (p.isCreative() || p.isSpectator()) continue;
            // The Lantern Court looks after its own: the Tidewatch never searches a Lantern-Bearer
            if (com.cursedtides.quest.HorizonArc.ending(p) == com.cursedtides.quest.HorizonArc.COURT_ENDING) continue;
            Inspection insp = ACTIVE.get(p.getUUID());
            if (insp != null) {
                follow(world, p, insp, now);
                continue;
            }
            if (COOLDOWN.getOrDefault(p.getUUID(), 0) > now) continue;
            ShipEntity ship = p.getVehicle() instanceof ShipEntity s ? s : null;
            if (contraband(p, ship) == 0) continue;

            if (p.getVehicle() instanceof Boat) {
                var patrols = world.getEntitiesOfClass(ShipEntity.class, p.getBoundingBox().inflate(HAIL),
                        s -> s.getFaction() == ShipFaction.NAVY);
                if (!patrols.isEmpty()) {
                    ShipEntity patrol = patrols.get(0);
                    ACTIVE.put(p.getUUID(), new Inspection(patrol.getUUID(), now + SEARCH_TICKS));
                    p.connection.send(new ClientboundSetTitleTextPacket(Component.literal("HEAVE TO!").withStyle(ChatFormatting.BLUE)));
                    p.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("The Tidewatch means to search your hold")
                            .withStyle(ChatFormatting.GRAY)));
                    p.displayClientMessage(Component.literal("The ").append(patrol.shipName()).append(Component.literal(
                            " signals: heave to for inspection! Stay and be searched, or put " + (int) ESCAPE
                                    + " blocks between you within 15 seconds.")).withStyle(ChatFormatting.BLUE), false);
                    world.playSound(null, p.blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.HOSTILE, 1.0f, 1.2f);
                }
            } else if (world.random.nextFloat() < 0.12f) {
                // On foot in port: a marine may pat down a suspicious-looking sailor
                var marines = world.getEntitiesOfClass(MarineEntity.class, p.getBoundingBox().inflate(6), m -> m.isAlive());
                if (!marines.isEmpty()) {
                    int seized = seize(p, null);
                    COOLDOWN.put(p.getUUID(), now + 20 * 120);
                    if (seized > 0) {
                        FactionData.adjust(p, Faction.NAVY, -25);
                        p.displayClientMessage(Component.literal("A marine stops you and turns out your pockets. " + seized
                                + " crates of contraband seized!").withStyle(ChatFormatting.RED), false);
                    }
                }
            }
        }
        ACTIVE.keySet().removeIf(id -> server.getPlayerList().getPlayer(id) == null);
    }

    private static void follow(ServerLevel world, ServerPlayer p, Inspection insp, int now) {
        var patrol = world.getEntity(insp.patrol());
        double dist = patrol == null ? Double.MAX_VALUE : patrol.distanceTo(p);
        if (patrol == null || !patrol.isAlive() || dist > ESCAPE) {
            ACTIVE.remove(p.getUUID());
            COOLDOWN.put(p.getUUID(), now + 20 * 120);
            FactionData.adjust(p, Faction.NAVY, -10);
            FactionData.adjust(p, Faction.SMUGGLERS, 8);
            FactionData.progress(p, ContractType.SMUGGLER_SLIP);
            p.displayClientMessage(Component.literal("You've given the Tidewatch the slip. They'll remember your sails.")
                    .withStyle(ChatFormatting.DARK_PURPLE), false);
            return;
        }
        int left = (insp.deadline() - now) / 20;
        if (left > 0) {
            p.displayClientMessage(Component.literal("Inspection in " + left + "s  (" + (int) dist + "/" + (int) ESCAPE + " blocks)")
                    .withStyle(ChatFormatting.BLUE), true);
            return;
        }
        ACTIVE.remove(p.getUUID());
        COOLDOWN.put(p.getUUID(), now + 20 * 180);
        ShipEntity ship = p.getVehicle() instanceof ShipEntity s ? s : null;
        int seized = seize(p, ship);
        if (seized > 0) {
            FactionData.adjust(p, Faction.NAVY, -40);
            p.displayClientMessage(Component.literal("Marines come aboard and search everything. " + seized
                    + " crates of contraband seized, and your name goes in the Tidewatch ledger.").withStyle(ChatFormatting.RED), false);
        } else {
            p.displayClientMessage(Component.literal("The marines search your hold and find nothing. They let you go.").withStyle(ChatFormatting.GRAY), false);
        }
    }

    private static int contraband(ServerPlayer p, ShipEntity ship) {
        int n = 0;
        for (TradeGood g : TradeGood.values()) {
            if (!g.contraband) continue;
            n += p.getInventory().countItem(g.item());
            if (ship != null) n += ship.cargo().countItem(g.item());
        }
        return n;
    }

    private static int seize(ServerPlayer p, ShipEntity ship) {
        int n = clear(p.getInventory());
        if (ship != null) n += clear(ship.cargo());
        return n;
    }

    private static int clear(Container inv) {
        int n = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (TradeGood.isContraband(stack)) {
                n += stack.getCount();
                inv.setItem(i, ItemStack.EMPTY);
            }
        }
        inv.setChanged();
        return n;
    }
}
