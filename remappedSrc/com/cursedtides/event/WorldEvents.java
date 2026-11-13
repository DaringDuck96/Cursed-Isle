package com.cursedtides.event;

import com.cursedtides.entity.ModEntities;
import com.cursedtides.entity.ShipEntity;
import com.cursedtides.item.ModItems;
import com.cursedtides.ship.ShipEvents;
import com.cursedtides.ship.ShipFaction;
import com.cursedtides.world.Archipelago;
import com.cursedtides.world.ModWorld;
import com.mojang.brigadier.arguments.StringArgumentType;
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
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.Heightmap;
import java.util.ArrayList;
import java.util.List;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/**
 * Things that happen on the sea without you: convoys to raid, rival captains, Tidewatch blockades,
 * a monster in the deep, and the cursed coming ashore at night. One roll every five minutes.
 */
public final class WorldEvents {
    private WorldEvents() {}

    public static final String MONSTER_TAG = "cursedtides_monster";
    public static final String INVADER_TAG = "cursedtides_invader";
    private static final int INTERVAL = 6000;
    private static final float CHANCE = 0.35f;

    public enum Kind { CONVOY, RIVAL, BLOCKADE, MONSTER, INVASION, LEVIATHAN, TREASURE_FLEET, DREAD_SHIP }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(WorldEvents::tick);
        ServerLivingEntityEvents.AFTER_DEATH.register(WorldEvents::onDeath);
        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> dispatcher.register(
                literal("cursedtides").requires(src -> src.hasPermission(2))
                        .then(literal("event").then(argument("kind", StringArgumentType.word())
                                .suggests((ctx, b) -> {
                                    for (Kind k : Kind.values()) b.suggest(k.name().toLowerCase());
                                    return b.buildFuture();
                                })
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    Kind kind;
                                    try {
                                        kind = Kind.valueOf(StringArgumentType.getString(ctx, "kind").toUpperCase());
                                    } catch (IllegalArgumentException e) {
                                        ctx.getSource().sendFailure(Component.literal("Unknown event."));
                                        return 0;
                                    }
                                    boolean ok = start(kind, p, true);
                                    if (!ok) ctx.getSource().sendFailure(Component.literal("Couldn't stage that here (try it at sea, or near a town)."));
                                    return ok ? 1 : 0;
                                })))));
    }

    private static void tick(MinecraftServer server) {
        if (server.getTickCount() % INTERVAL != 0 || server.getTickCount() == 0) return;
        ServerLevel world = server.overworld();
        List<ServerPlayer> players = new ArrayList<>(world.getPlayers(p -> !p.isSpectator() && !p.isCreative()));
        if (players.isEmpty() || world.random.nextFloat() > CHANCE) return;
        ServerPlayer player = players.get(world.random.nextInt(players.size()));

        List<Kind> options = new ArrayList<>();
        boolean atSea = player.getVehicle() instanceof Boat;
        if (atSea) {
            options.add(Kind.CONVOY);
            options.add(Kind.CONVOY);
            options.add(Kind.RIVAL);
            options.add(Kind.MONSTER);
            options.add(Kind.TREASURE_FLEET);
            options.add(Kind.DREAD_SHIP);
            options.add(Kind.LEVIATHAN);
        }
        if (ModWorld.isArchipelago()) {
            if (distanceTo(player, Archipelago.SALTMARROW) < 160) options.add(Kind.BLOCKADE);
            boolean sealed = com.cursedtides.quest.HorizonArc.ending(player) == com.cursedtides.quest.HorizonArc.SEALED;
            if (world.isNight() && nearestTown(player) != null && !sealed) {
                options.add(Kind.INVASION);
                options.add(Kind.INVASION);
            }
        }
        if (options.isEmpty()) return;
        start(options.get(world.random.nextInt(options.size())), player, false);
    }

    /** Stages an event around a player. Returns false when the place doesn't suit it. */
    public static boolean start(Kind kind, ServerPlayer player, boolean forced) {
        ServerLevel world = player.serverLevel();
        MinecraftServer server = world.getServer();
        switch (kind) {
            case CONVOY -> {
                BlockPos at = ShipEvents.findSeaSpot(world, player, 70, 100);
                if (at == null) return false;
                ShipEntity lead = ShipEvents.spawnShip(world, ShipFaction.MERCHANT, at.getX() + 0.5, at.getY() - 0.1, at.getZ() + 0.5, player);
                BlockPos second = findSeaSpotNear(world, at, 10, 20);
                if (second != null) {
                    ShipEvents.spawnShip(world, ShipFaction.MERCHANT, second.getX() + 0.5, second.getY() - 0.1, second.getZ() + 0.5, player);
                }
                announce(server, Component.literal("A merchant convoy has been sighted " + where(at)
                        + ", holds heavy with trade goods. They'll run if you chase them.").withStyle(ChatFormatting.GOLD));
                return lead != null;
            }
            case RIVAL -> {
                BlockPos at = ShipEvents.findSeaSpot(world, player, 70, 100);
                if (at == null) return false;
                ShipEntity ship = ShipEvents.spawnShip(world, ShipFaction.PIRATE, at.getX() + 0.5, at.getY() - 0.1, at.getZ() + 0.5, player);
                String name = ship != null && ship.getCustomName() != null ? ship.getCustomName().getString() : "a rival ship";
                announce(server, Component.literal("Rival pirates aboard the " + name + " are prowling " + where(at)
                        + ". They answer to no Brethren and fire on anyone.").withStyle(ChatFormatting.RED));
                return ship != null;
            }
            case BLOCKADE -> {
                if (!ModWorld.isArchipelago()) return false;
                Archipelago.Island town = Archipelago.SALTMARROW;
                int placed = 0;
                for (int i = 0; i < 3; i++) {
                    double angle = (Math.PI * 2 / 3) * i + world.random.nextDouble() * 0.6;
                    BlockPos center = BlockPos.containing(town.cx() + Math.cos(angle) * 85, 0, town.cz() + Math.sin(angle) * 85);
                    BlockPos at = findSeaSpotNear(world, center, 0, 12);
                    if (at == null) continue;
                    if (ShipEvents.spawnShip(world, ShipFaction.NAVY, at.getX() + 0.5, at.getY() - 0.1, at.getZ() + 0.5, player) != null) placed++;
                }
                if (placed == 0) return false;
                com.cursedtides.living.Prosperity.adjust(server, town.cx(), town.cz(), -8, "the blockade chokes its trade");
                announce(server, Component.literal("The Tidewatch has thrown a blockade around Saltmarrow! " + placed
                        + " warships ride off the harbour. Wanted pirates beware; everyone else, break it if you dare.").withStyle(ChatFormatting.BLUE));
                return true;
            }
            case MONSTER -> {
                BlockPos at = ShipEvents.findSeaSpot(world, player, 30, 50);
                if (at == null) return false;
                int y = Math.max(world.getMinBuildHeight() + 2, at.getY() - 4);
                var wyrm = EntityType.ELDER_GUARDIAN.create(world);
                if (wyrm == null) return false;
                wyrm.moveTo(at.getX() + 0.5, y, at.getZ() + 0.5, 0, 0);
                wyrm.setCustomName(Component.literal("The Brinewyrm").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));
                wyrm.setCustomNameVisible(true);
                wyrm.addTag(MONSTER_TAG);
                wyrm.finalizeSpawn(world, world.getCurrentDifficultyAt(at), MobSpawnType.EVENT, null);
                world.addFreshEntity(wyrm);
                for (int i = 0; i < 3; i++) {
                    var spawn = EntityType.GUARDIAN.create(world);
                    if (spawn == null) continue;
                    spawn.moveTo(at.getX() + world.random.nextInt(9) - 4, y, at.getZ() + world.random.nextInt(9) - 4, 0, 0);
                    spawn.finalizeSpawn(world, world.getCurrentDifficultyAt(at), MobSpawnType.EVENT, null);
                    world.addFreshEntity(spawn);
                }
                world.playSound(null, at, SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 2.0f, 0.6f);
                announce(server, Component.literal("Something vast is moving under the water " + where(at)
                        + ". Sailors are calling it the Brinewyrm.").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC));
                return true;
            }
            case LEVIATHAN -> {
                return player.getVehicle() instanceof ShipEntity ship && com.cursedtides.sea.Leviathan.rise(world, ship);
            }
            case TREASURE_FLEET -> {
                return com.cursedtides.sea.SeaEvents.treasureFleet(world, player);
            }
            case DREAD_SHIP -> {
                return com.cursedtides.sea.SeaEvents.bossShip(world, player);
            }
            case INVASION -> {
                if (!ModWorld.isArchipelago()) return false;
                Archipelago.Island town = nearestTown(player);
                if (town == null || (!forced && !world.isNight())) return false;
                int spawned = 0;
                for (int i = 0; i < 8; i++) {
                    int x = town.cx() + world.random.nextInt(31) - 15;
                    int z = town.cz() + world.random.nextInt(31) - 15;
                    BlockPos column = new BlockPos(x, 0, z);
                    if (!world.hasChunkAt(column)) continue;
                    int top = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                    Mob pirate = ModEntities.CURSED_PIRATE.create(world);
                    if (pirate == null) continue;
                    pirate.moveTo(x + 0.5, top, z + 0.5, world.random.nextFloat() * 360, 0);
                    pirate.finalizeSpawn(world, world.getCurrentDifficultyAt(column), MobSpawnType.EVENT, null);
                    pirate.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET)); // dawn won't save the town
                    pirate.addTag(INVADER_TAG);
                    world.addFreshEntity(pirate);
                    spawned++;
                }
                if (spawned == 0) return false;
                world.playSound(null, town.cx(), player.getY(), town.cz(), com.cursedtides.sound.ModSounds.BELL_TOLL.value(), SoundSource.HOSTILE, 4.0f, 0.8f);
                com.cursedtides.living.Prosperity.adjust(server, town.cx(), town.cz(), -12, "the cursed came ashore");
                announce(server, Component.literal("The cursed have come ashore at " + town.name()
                        + "! Every blade to the plaza!").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
                return true;
            }
        }
        return false;
    }

    private static void onDeath(LivingEntity victim, DamageSource source) {
        if (victim.getTags().contains(MONSTER_TAG)) {
            victim.spawnAtLocation(new ItemStack(ModItems.DOUBLOON, 40 + victim.getRandom().nextInt(25)));
            victim.spawnAtLocation(new ItemStack(Items.HEART_OF_THE_SEA));
            victim.spawnAtLocation(new ItemStack(ModItems.TREASURE_MAP));
            victim.spawnAtLocation(new ItemStack(Items.PRISMARINE_SHARD, 16));
            if (victim.getServer() != null) {
                String who = source.getEntity() instanceof ServerPlayer p ? p.getName().getString() : "the sea itself";
                announce(victim.getServer(), Component.literal("The Brinewyrm is slain, by " + who + "!").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            }
        } else if (victim.getTags().contains("cursedtides_boss")) {
            victim.spawnAtLocation(new ItemStack(ModItems.DOUBLOON, 30 + victim.getRandom().nextInt(30)));
            victim.spawnAtLocation(new ItemStack(ModItems.ANCIENT_RELIC));
            victim.spawnAtLocation(new ItemStack(ModItems.TREASURE_MAP));
            if (victim.getRandom().nextBoolean()) victim.spawnAtLocation(new ItemStack(ModItems.MAP_FRAGMENT));
            if (victim.getRandom().nextInt(3) == 0) victim.spawnAtLocation(new ItemStack(com.cursedtides.ship.FlagItem.randomRare(victim.getRandom())));
            victim.spawnAtLocation(new ItemStack(com.cursedtides.appearance.Accessories.random(victim.getRandom(), true)));
            if (source.getEntity() instanceof ServerPlayer bossKiller) victim.spawnAtLocation(com.cursedtides.explore.Collectibles.randomFor(bossKiller, com.cursedtides.explore.Collectibles.Kind.INSTRUMENT));
            if (victim.level() instanceof ServerLevel bw) victim.spawnAtLocation(com.cursedtides.combat.GearRarity.randomWeapon(bw, victim.getRandom(), 3));
            if (victim.getServer() != null && victim.getCustomName() != null) {
                String who = source.getEntity() instanceof ServerPlayer p ? p.getName().getString() : "the elements";
                announce(victim.getServer(), Component.literal(victim.getCustomName().getString() + " has fallen to " + who + "!")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            }
        } else if (victim.getTags().contains(INVADER_TAG) && source.getEntity() instanceof ServerPlayer) {
            victim.spawnAtLocation(new ItemStack(ModItems.DOUBLOON, 2 + victim.getRandom().nextInt(3)));
            if (victim.level() instanceof ServerLevel world) {
                var box = victim.getBoundingBox().inflate(64);
                boolean anyLeft = !world.getEntitiesOfClass(Mob.class, box,
                        m -> m != victim && m.isAlive() && m.getTags().contains(INVADER_TAG)).isEmpty();
                if (!anyLeft && world.getServer() != null) {
                    com.cursedtides.living.Prosperity.adjust(world.getServer(), victim.getBlockX(), victim.getBlockZ(), 15, "its defenders held");
                    announce(world.getServer(), Component.literal("The invasion is beaten back. The town drinks to its defenders tonight.")
                            .withStyle(ChatFormatting.GREEN));
                }
            }
        }
    }

    private static Archipelago.Island nearestTown(ServerPlayer player) {
        for (Archipelago.Island is : Archipelago.ISLANDS) {
            if (is.style() == Archipelago.Style.TOWN && distanceTo(player, is) < 110) return is;
        }
        return null;
    }

    private static double distanceTo(ServerPlayer player, Archipelago.Island is) {
        return Math.hypot(player.getX() - is.cx(), player.getZ() - is.cz());
    }

    private static BlockPos findSeaSpotNear(ServerLevel world, BlockPos center, int minDist, int maxDist) {
        for (int attempt = 0; attempt < 10; attempt++) {
            double angle = world.random.nextDouble() * Math.PI * 2;
            double d = minDist + world.random.nextDouble() * (maxDist - minDist);
            int x = (int) (center.getX() + Math.cos(angle) * d);
            int z = (int) (center.getZ() + Math.sin(angle) * d);
            if (!world.hasChunkAt(new BlockPos(x, 0, z))) continue;
            int top = world.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
            BlockPos surface = new BlockPos(x, top - 1, z);
            if (world.getFluidState(surface).is(FluidTags.WATER) && world.getFluidState(surface.below(3)).is(FluidTags.WATER)) {
                return new BlockPos(x, top, z);
            }
        }
        return null;
    }

    private static String where(BlockPos at) {
        int island = ModWorld.isArchipelago() ? nearestIsland(at) : -1;
        String coords = "(" + at.getX() + ", " + at.getZ() + ")";
        return island >= 0 ? "off " + Archipelago.ISLANDS.get(island).name() + " " + coords : "at " + coords;
    }

    private static int nearestIsland(BlockPos at) {
        int best = -1;
        double bestD = 260;
        for (int i = 0; i < Archipelago.ISLANDS.size(); i++) {
            Archipelago.Island is = Archipelago.ISLANDS.get(i);
            double d = Math.hypot(at.getX() - is.cx(), at.getZ() - is.cz());
            if (d < bestD) { bestD = d; best = i; }
        }
        return best;
    }

    private static void announce(MinecraftServer server, Component text) {
        server.getPlayerList().broadcastSystemMessage(text, false);
    }
}
