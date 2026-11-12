package com.cursedtides.trade;

import com.cursedtides.entity.MarineEntity;
import com.cursedtides.entity.ShipEntity;
import com.cursedtides.faction.ContractType;
import com.cursedtides.faction.Faction;
import com.cursedtides.faction.FactionData;
import com.cursedtides.ship.ShipFaction;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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
        if (server.getTicks() % 20 != 0) return;
        int now = server.getTicks();
        ServerWorld world = server.getOverworld();
        for (ServerPlayerEntity p : world.getPlayers()) {
            if (p.isCreative() || p.isSpectator()) continue;
            // The Lantern Court looks after its own: the Tidewatch never searches a Lantern-Bearer
            if (com.cursedtides.quest.HorizonArc.ending(p) == com.cursedtides.quest.HorizonArc.COURT_ENDING) continue;
            Inspection insp = ACTIVE.get(p.getUuid());
            if (insp != null) {
                follow(world, p, insp, now);
                continue;
            }
            if (COOLDOWN.getOrDefault(p.getUuid(), 0) > now) continue;
            ShipEntity ship = p.getVehicle() instanceof ShipEntity s ? s : null;
            if (contraband(p, ship) == 0) continue;

            if (p.getVehicle() instanceof BoatEntity) {
                var patrols = world.getEntitiesByClass(ShipEntity.class, p.getBoundingBox().expand(HAIL),
                        s -> s.getFaction() == ShipFaction.NAVY);
                if (!patrols.isEmpty()) {
                    ShipEntity patrol = patrols.get(0);
                    ACTIVE.put(p.getUuid(), new Inspection(patrol.getUuid(), now + SEARCH_TICKS));
                    p.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("HEAVE TO!").formatted(Formatting.BLUE)));
                    p.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal("The Tidewatch means to search your hold")
                            .formatted(Formatting.GRAY)));
                    p.sendMessage(Text.literal("The ").append(patrol.shipName()).append(Text.literal(
                            " signals: heave to for inspection! Stay and be searched, or put " + (int) ESCAPE
                                    + " blocks between you within 15 seconds.")).formatted(Formatting.BLUE), false);
                    world.playSound(null, p.getBlockPos(), SoundEvents.BLOCK_BELL_USE, SoundCategory.HOSTILE, 1.0f, 1.2f);
                }
            } else if (world.random.nextFloat() < 0.12f) {
                // On foot in port: a marine may pat down a suspicious-looking sailor
                var marines = world.getEntitiesByClass(MarineEntity.class, p.getBoundingBox().expand(6), m -> m.isAlive());
                if (!marines.isEmpty()) {
                    int seized = seize(p, null);
                    COOLDOWN.put(p.getUuid(), now + 20 * 120);
                    if (seized > 0) {
                        FactionData.adjust(p, Faction.NAVY, -25);
                        p.sendMessage(Text.literal("A marine stops you and turns out your pockets. " + seized
                                + " crates of contraband seized!").formatted(Formatting.RED), false);
                    }
                }
            }
        }
        ACTIVE.keySet().removeIf(id -> server.getPlayerManager().getPlayer(id) == null);
    }

    private static void follow(ServerWorld world, ServerPlayerEntity p, Inspection insp, int now) {
        var patrol = world.getEntity(insp.patrol());
        double dist = patrol == null ? Double.MAX_VALUE : patrol.distanceTo(p);
        if (patrol == null || !patrol.isAlive() || dist > ESCAPE) {
            ACTIVE.remove(p.getUuid());
            COOLDOWN.put(p.getUuid(), now + 20 * 120);
            FactionData.adjust(p, Faction.NAVY, -10);
            FactionData.adjust(p, Faction.SMUGGLERS, 8);
            FactionData.progress(p, ContractType.SMUGGLER_SLIP);
            p.sendMessage(Text.literal("You've given the Tidewatch the slip. They'll remember your sails.")
                    .formatted(Formatting.DARK_PURPLE), false);
            return;
        }
        int left = (insp.deadline() - now) / 20;
        if (left > 0) {
            p.sendMessage(Text.literal("Inspection in " + left + "s  (" + (int) dist + "/" + (int) ESCAPE + " blocks)")
                    .formatted(Formatting.BLUE), true);
            return;
        }
        ACTIVE.remove(p.getUuid());
        COOLDOWN.put(p.getUuid(), now + 20 * 180);
        ShipEntity ship = p.getVehicle() instanceof ShipEntity s ? s : null;
        int seized = seize(p, ship);
        if (seized > 0) {
            FactionData.adjust(p, Faction.NAVY, -40);
            p.sendMessage(Text.literal("Marines come aboard and search everything. " + seized
                    + " crates of contraband seized, and your name goes in the Tidewatch ledger.").formatted(Formatting.RED), false);
        } else {
            p.sendMessage(Text.literal("The marines search your hold and find nothing. They let you go.").formatted(Formatting.GRAY), false);
        }
    }

    private static int contraband(ServerPlayerEntity p, ShipEntity ship) {
        int n = 0;
        for (TradeGood g : TradeGood.values()) {
            if (!g.contraband) continue;
            n += p.getInventory().count(g.item());
            if (ship != null) n += ship.cargo().count(g.item());
        }
        return n;
    }

    private static int seize(ServerPlayerEntity p, ShipEntity ship) {
        int n = clear(p.getInventory());
        if (ship != null) n += clear(ship.cargo());
        return n;
    }

    private static int clear(Inventory inv) {
        int n = 0;
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (TradeGood.isContraband(stack)) {
                n += stack.getCount();
                inv.setStack(i, ItemStack.EMPTY);
            }
        }
        inv.markDirty();
        return n;
    }
}
