package com.cursedtides.activity;

import com.cursedtides.entity.ShipEntity;
import com.cursedtides.item.ModItems;
import com.cursedtides.ship.ShipEvents;
import com.cursedtides.ship.ShipFaction;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Cannon Defense: hold the harbour against eight waves of ships making straight for the docks. The gunnery sergeant
 * lends you a cannon. Every ship that reaches the harbour costs the town one of its five lives.
 */
public final class CannonDefense {
    private CannonDefense() {}

    public static final int WAVES = 8, LIVES = 5;
    private static final String LOANER = "LoanerCannon";

    private static final class Game {
        final Vec3 harbour; final List<UUID> players = new ArrayList<>(); final List<UUID> ships = new ArrayList<>();
        int wave, lives = LIVES, pause = 100, sunk;
        Game(Vec3 harbour) { this.harbour = harbour; }
    }
    private static Game game;

    public static List<String> start(ServerPlayer p, Entity sergeant) {
        if (game != null) {
            if (!game.players.contains(p.getUUID())) {
                game.players.add(p.getUUID());
                lend(p);
                return List.of("Another gun on the wall! Take this and get to the battlements. Wave " + game.wave + " is coming in.");
            }
            return List.of("You're already on the guns! Fire!");
        }
        game = new Game(sergeant.position());
        game.players.add(p.getUUID());
        lend(p);
        p.getServer().getPlayerList().broadcastSystemMessage(Component.literal("Cannon Defense at " + nearestName(sergeant)
                + "! Ships are making for the harbour. Every gun counts; talk to the gunnery sergeant to man one.").withStyle(ChatFormatting.GOLD), false);
        return List.of("Here: a cannon and shot. Ships will come straight for the docks. Sink them before they arrive.",
                "Eight waves. We can take five ships in the harbour before the town's lost. Fire at will!");
    }

    private static String nearestName(Entity e) {
        var port = com.cursedtides.living.Prosperity.nearestPort(e.getBlockX(), e.getBlockZ(), 200);
        return port == null ? "the harbour" : port.island().name();
    }

    private static void lend(ServerPlayer p) {
        ItemStack cannon = new ItemStack(ModItems.SHIPS_CANNON);
        CompoundTag n = new CompoundTag();
        n.putBoolean(LOANER, true);
        cannon.set(DataComponents.CUSTOM_DATA, CustomData.of(n));
        cannon.set(DataComponents.CUSTOM_NAME, Component.literal("Loaned Harbour Cannon").withStyle(ChatFormatting.GRAY));
        p.addItem(cannon);
        p.addItem(new ItemStack(ModItems.CANNONBALL, 32));
    }

    private static void takeBack(ServerPlayer p) {
        var inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(ModItems.SHIPS_CANNON) && s.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean(LOANER)) {
                inv.setItem(i, ItemStack.EMPTY);
            }
        }
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(CannonDefense::tick);
    }

    private static void tick(MinecraftServer server) {
        if (game == null || server.getTickCount() % 10 != 0) return;
        ServerLevel w = server.overworld();
        Game g = game;
        g.players.removeIf(id -> server.getPlayerList().getPlayer(id) == null);
        if (g.players.isEmpty()) { end(server, false); return; }
        // Ships: arrived, sunk, still coming
        for (UUID id : List.copyOf(g.ships)) {
            Entity e = w.getEntity(id);
            if (!(e instanceof ShipEntity ship) || !ship.isAlive()) {
                g.ships.remove(id);
                g.sunk++;
                continue;
            }
            ship.forcedDestination = g.harbour;
            if (ship.position().distanceToSqr(g.harbour.x, ship.getY(), g.harbour.z) < 22 * 22) {
                g.ships.remove(id);
                g.lives--;
                w.sendParticles(ParticleTypes.EXPLOSION_EMITTER, ship.getX(), ship.getY() + 2, ship.getZ(), 1, 0, 0, 0, 0);
                w.playSound(null, ship.blockPosition(), com.cursedtides.sound.ModSounds.CANNON_BOOM.value(), SoundSource.HOSTILE, 3f, 0.6f);
                ship.getPassengers().forEach(Entity::discard);
                ship.discard();
                tell(server, Component.literal("A ship broke through into the harbour! Lives left: " + g.lives).withStyle(ChatFormatting.RED));
                if (g.lives <= 0) { end(server, false); return; }
            }
        }
        if (!g.ships.isEmpty()) return;
        if (g.wave >= WAVES) { end(server, true); return; }
        if ((g.pause -= 10) > 0) return;
        g.wave++;
        g.pause = 160;
        int count = 1 + (g.wave + 1) / 2;
        int spawned = 0;
        for (int tries = 0; tries < 40 && spawned < count; tries++) {
            double a = w.random.nextDouble() * Math.PI * 2;
            int x = (int) (g.harbour.x + Math.cos(a) * 95), z = (int) (g.harbour.z + Math.sin(a) * 95);
            if (!w.hasChunkAt(new BlockPos(x, 0, z))) continue;
            int top = w.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
            if (!w.getFluidState(new BlockPos(x, top - 1, z)).is(FluidTags.WATER) || !w.getFluidState(new BlockPos(x, top - 4, z)).is(FluidTags.WATER)) continue;
            ServerPlayer facing = server.getPlayerList().getPlayer(g.players.get(0));
            ShipEntity ship = ShipEvents.spawnShip(w, g.wave >= 6 ? ShipFaction.DROWNED : ShipFaction.PIRATE, x + 0.5, top - 0.1, z + 0.5, facing);
            if (ship == null) continue;
            ship.forcedDestination = g.harbour;
            ship.addTag("cursedtides_defense");
            g.ships.add(ship.getUUID());
            spawned++;
        }
        tell(server, Component.literal("Wave " + g.wave + " of " + WAVES + ": " + spawned + " ships inbound!").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
    }

    private static void tell(MinecraftServer server, Component text) {
        if (game == null) return;
        for (UUID id : game.players) {
            ServerPlayer p = server.getPlayerList().getPlayer(id);
            if (p != null) p.displayClientMessage(text, false);
        }
    }

    private static void end(MinecraftServer server, boolean won) {
        Game g = game;
        game = null;
        ServerLevel w = server.overworld();
        for (UUID id : g.ships) {
            if (w.getEntity(id) instanceof ShipEntity ship) {
                ship.getPassengers().forEach(Entity::discard);
                ship.discard();
            }
        }
        int held = won ? WAVES : Math.max(0, g.wave - 1);
        for (UUID id : g.players) {
            ServerPlayer p = server.getPlayerList().getPlayer(id);
            if (p == null) continue;
            takeBack(p);
            p.addItem(new ItemStack(ModItems.DOUBLOON, 4 * held + g.sunk));
            Leaderboards.submit(p, "defense", held);
            com.cursedtides.pirate.PirateData.addXp(p, com.cursedtides.pirate.Weapon.CANNON, 30 * held);
            if (won) Achievements.grant(p, "harbour_guard");
            p.displayClientMessage(Component.literal(won ? "The harbour holds! Eight waves beaten back, " + g.sunk + " ships sunk."
                    : "The defence is over. Waves held: " + held + ", ships sunk: " + g.sunk + ".").withStyle(won ? ChatFormatting.GOLD : ChatFormatting.GRAY), false);
        }
        if (won) com.cursedtides.living.Prosperity.adjust(server, (int) g.harbour.x, (int) g.harbour.z, 10, "its harbour held");
    }
}
