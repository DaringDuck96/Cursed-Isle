package com.cursedtides.activity;

import com.cursedtides.entity.ShipEntity;
import com.cursedtides.item.ModItems;
import com.cursedtides.ship.ShipEvents;
import com.cursedtides.ship.ShipFaction;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.registry.tag.FluidTags;

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
        final Vec3d harbour; final List<UUID> players = new ArrayList<>(); final List<UUID> ships = new ArrayList<>();
        int wave, lives = LIVES, pause = 100, sunk;
        Game(Vec3d harbour) { this.harbour = harbour; }
    }
    private static Game game;

    public static List<String> start(ServerPlayerEntity p, Entity sergeant) {
        if (game != null) {
            if (!game.players.contains(p.getUuid())) {
                game.players.add(p.getUuid());
                lend(p);
                return List.of("Another gun on the wall! Take this and get to the battlements. Wave " + game.wave + " is coming in.");
            }
            return List.of("You're already on the guns! Fire!");
        }
        game = new Game(sergeant.getPos());
        game.players.add(p.getUuid());
        lend(p);
        p.getServer().getPlayerManager().broadcast(Text.literal("Cannon Defense at " + nearestName(sergeant)
                + "! Ships are making for the harbour. Every gun counts; talk to the gunnery sergeant to man one.").formatted(Formatting.GOLD), false);
        return List.of("Here: a cannon and shot. Ships will come straight for the docks. Sink them before they arrive.",
                "Eight waves. We can take five ships in the harbour before the town's lost. Fire at will!");
    }

    private static String nearestName(Entity e) {
        var port = com.cursedtides.living.Prosperity.nearestPort(e.getBlockX(), e.getBlockZ(), 200);
        return port == null ? "the harbour" : port.island().name();
    }

    private static void lend(ServerPlayerEntity p) {
        ItemStack cannon = new ItemStack(ModItems.SHIPS_CANNON);
        NbtCompound n = new NbtCompound();
        n.putBoolean(LOANER, true);
        cannon.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(n));
        cannon.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Loaned Harbour Cannon").formatted(Formatting.GRAY));
        p.giveItemStack(cannon);
        p.giveItemStack(new ItemStack(ModItems.CANNONBALL, 32));
    }

    private static void takeBack(ServerPlayerEntity p) {
        var inv = p.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack s = inv.getStack(i);
            if (s.isOf(ModItems.SHIPS_CANNON) && s.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getBoolean(LOANER)) {
                inv.setStack(i, ItemStack.EMPTY);
            }
        }
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(CannonDefense::tick);
    }

    private static void tick(MinecraftServer server) {
        if (game == null || server.getTicks() % 10 != 0) return;
        ServerWorld w = server.getOverworld();
        Game g = game;
        g.players.removeIf(id -> server.getPlayerManager().getPlayer(id) == null);
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
            if (ship.getPos().squaredDistanceTo(g.harbour.x, ship.getY(), g.harbour.z) < 22 * 22) {
                g.ships.remove(id);
                g.lives--;
                w.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, ship.getX(), ship.getY() + 2, ship.getZ(), 1, 0, 0, 0, 0);
                w.playSound(null, ship.getBlockPos(), com.cursedtides.sound.ModSounds.CANNON_BOOM.value(), SoundCategory.HOSTILE, 3f, 0.6f);
                ship.getPassengerList().forEach(Entity::discard);
                ship.discard();
                tell(server, Text.literal("A ship broke through into the harbour! Lives left: " + g.lives).formatted(Formatting.RED));
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
            if (!w.isChunkLoaded(new BlockPos(x, 0, z))) continue;
            int top = w.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);
            if (!w.getFluidState(new BlockPos(x, top - 1, z)).isIn(FluidTags.WATER) || !w.getFluidState(new BlockPos(x, top - 4, z)).isIn(FluidTags.WATER)) continue;
            ServerPlayerEntity facing = server.getPlayerManager().getPlayer(g.players.get(0));
            ShipEntity ship = ShipEvents.spawnShip(w, g.wave >= 6 ? ShipFaction.DROWNED : ShipFaction.PIRATE, x + 0.5, top - 0.1, z + 0.5, facing);
            if (ship == null) continue;
            ship.forcedDestination = g.harbour;
            ship.addCommandTag("cursedtides_defense");
            g.ships.add(ship.getUuid());
            spawned++;
        }
        tell(server, Text.literal("Wave " + g.wave + " of " + WAVES + ": " + spawned + " ships inbound!").formatted(Formatting.GOLD, Formatting.BOLD));
    }

    private static void tell(MinecraftServer server, Text text) {
        if (game == null) return;
        for (UUID id : game.players) {
            ServerPlayerEntity p = server.getPlayerManager().getPlayer(id);
            if (p != null) p.sendMessage(text, false);
        }
    }

    private static void end(MinecraftServer server, boolean won) {
        Game g = game;
        game = null;
        ServerWorld w = server.getOverworld();
        for (UUID id : g.ships) {
            if (w.getEntity(id) instanceof ShipEntity ship) {
                ship.getPassengerList().forEach(Entity::discard);
                ship.discard();
            }
        }
        int held = won ? WAVES : Math.max(0, g.wave - 1);
        for (UUID id : g.players) {
            ServerPlayerEntity p = server.getPlayerManager().getPlayer(id);
            if (p == null) continue;
            takeBack(p);
            p.giveItemStack(new ItemStack(ModItems.DOUBLOON, 4 * held + g.sunk));
            Leaderboards.submit(p, "defense", held);
            com.cursedtides.pirate.PirateData.addXp(p, com.cursedtides.pirate.Weapon.CANNON, 30 * held);
            if (won) Achievements.grant(p, "harbour_guard");
            p.sendMessage(Text.literal(won ? "The harbour holds! Eight waves beaten back, " + g.sunk + " ships sunk."
                    : "The defence is over. Waves held: " + held + ", ships sunk: " + g.sunk + ".").formatted(won ? Formatting.GOLD : Formatting.GRAY), false);
        }
        if (won) com.cursedtides.living.Prosperity.adjust(server, (int) g.harbour.x, (int) g.harbour.z, 10, "its harbour held");
    }
}
