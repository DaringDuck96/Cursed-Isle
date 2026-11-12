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
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

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
                literal("cursedtides").requires(src -> src.hasPermissionLevel(2))
                        .then(literal("event").then(argument("kind", StringArgumentType.word())
                                .suggests((ctx, b) -> {
                                    for (Kind k : Kind.values()) b.suggest(k.name().toLowerCase());
                                    return b.buildFuture();
                                })
                                .executes(ctx -> {
                                    ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                                    Kind kind;
                                    try {
                                        kind = Kind.valueOf(StringArgumentType.getString(ctx, "kind").toUpperCase());
                                    } catch (IllegalArgumentException e) {
                                        ctx.getSource().sendError(Text.literal("Unknown event."));
                                        return 0;
                                    }
                                    boolean ok = start(kind, p, true);
                                    if (!ok) ctx.getSource().sendError(Text.literal("Couldn't stage that here (try it at sea, or near a town)."));
                                    return ok ? 1 : 0;
                                })))));
    }

    private static void tick(MinecraftServer server) {
        if (server.getTicks() % INTERVAL != 0 || server.getTicks() == 0) return;
        ServerWorld world = server.getOverworld();
        List<ServerPlayerEntity> players = new ArrayList<>(world.getPlayers(p -> !p.isSpectator() && !p.isCreative()));
        if (players.isEmpty() || world.random.nextFloat() > CHANCE) return;
        ServerPlayerEntity player = players.get(world.random.nextInt(players.size()));

        List<Kind> options = new ArrayList<>();
        boolean atSea = player.getVehicle() instanceof BoatEntity;
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
    public static boolean start(Kind kind, ServerPlayerEntity player, boolean forced) {
        ServerWorld world = player.getServerWorld();
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
                announce(server, Text.literal("A merchant convoy has been sighted " + where(at)
                        + ", holds heavy with trade goods. They'll run if you chase them.").formatted(Formatting.GOLD));
                return lead != null;
            }
            case RIVAL -> {
                BlockPos at = ShipEvents.findSeaSpot(world, player, 70, 100);
                if (at == null) return false;
                ShipEntity ship = ShipEvents.spawnShip(world, ShipFaction.PIRATE, at.getX() + 0.5, at.getY() - 0.1, at.getZ() + 0.5, player);
                String name = ship != null && ship.getCustomName() != null ? ship.getCustomName().getString() : "a rival ship";
                announce(server, Text.literal("Rival pirates aboard the " + name + " are prowling " + where(at)
                        + ". They answer to no Brethren and fire on anyone.").formatted(Formatting.RED));
                return ship != null;
            }
            case BLOCKADE -> {
                if (!ModWorld.isArchipelago()) return false;
                Archipelago.Island town = Archipelago.SALTMARROW;
                int placed = 0;
                for (int i = 0; i < 3; i++) {
                    double angle = (Math.PI * 2 / 3) * i + world.random.nextDouble() * 0.6;
                    BlockPos center = BlockPos.ofFloored(town.cx() + Math.cos(angle) * 85, 0, town.cz() + Math.sin(angle) * 85);
                    BlockPos at = findSeaSpotNear(world, center, 0, 12);
                    if (at == null) continue;
                    if (ShipEvents.spawnShip(world, ShipFaction.NAVY, at.getX() + 0.5, at.getY() - 0.1, at.getZ() + 0.5, player) != null) placed++;
                }
                if (placed == 0) return false;
                com.cursedtides.living.Prosperity.adjust(server, town.cx(), town.cz(), -8, "the blockade chokes its trade");
                announce(server, Text.literal("The Tidewatch has thrown a blockade around Saltmarrow! " + placed
                        + " warships ride off the harbour. Wanted pirates beware; everyone else, break it if you dare.").formatted(Formatting.BLUE));
                return true;
            }
            case MONSTER -> {
                BlockPos at = ShipEvents.findSeaSpot(world, player, 30, 50);
                if (at == null) return false;
                int y = Math.max(world.getBottomY() + 2, at.getY() - 4);
                var wyrm = EntityType.ELDER_GUARDIAN.create(world);
                if (wyrm == null) return false;
                wyrm.refreshPositionAndAngles(at.getX() + 0.5, y, at.getZ() + 0.5, 0, 0);
                wyrm.setCustomName(Text.literal("The Brinewyrm").formatted(Formatting.DARK_GREEN, Formatting.BOLD));
                wyrm.setCustomNameVisible(true);
                wyrm.addCommandTag(MONSTER_TAG);
                wyrm.initialize(world, world.getLocalDifficulty(at), SpawnReason.EVENT, null);
                world.spawnEntity(wyrm);
                for (int i = 0; i < 3; i++) {
                    var spawn = EntityType.GUARDIAN.create(world);
                    if (spawn == null) continue;
                    spawn.refreshPositionAndAngles(at.getX() + world.random.nextInt(9) - 4, y, at.getZ() + world.random.nextInt(9) - 4, 0, 0);
                    spawn.initialize(world, world.getLocalDifficulty(at), SpawnReason.EVENT, null);
                    world.spawnEntity(spawn);
                }
                world.playSound(null, at, SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.HOSTILE, 2.0f, 0.6f);
                announce(server, Text.literal("Something vast is moving under the water " + where(at)
                        + ". Sailors are calling it the Brinewyrm.").formatted(Formatting.DARK_GREEN, Formatting.ITALIC));
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
                    if (!world.isChunkLoaded(column)) continue;
                    int top = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
                    MobEntity pirate = ModEntities.CURSED_PIRATE.create(world);
                    if (pirate == null) continue;
                    pirate.refreshPositionAndAngles(x + 0.5, top, z + 0.5, world.random.nextFloat() * 360, 0);
                    pirate.initialize(world, world.getLocalDifficulty(column), SpawnReason.EVENT, null);
                    pirate.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET)); // dawn won't save the town
                    pirate.addCommandTag(INVADER_TAG);
                    world.spawnEntity(pirate);
                    spawned++;
                }
                if (spawned == 0) return false;
                world.playSound(null, town.cx(), player.getY(), town.cz(), com.cursedtides.sound.ModSounds.BELL_TOLL.value(), SoundCategory.HOSTILE, 4.0f, 0.8f);
                com.cursedtides.living.Prosperity.adjust(server, town.cx(), town.cz(), -12, "the cursed came ashore");
                announce(server, Text.literal("The cursed have come ashore at " + town.name()
                        + "! Every blade to the plaza!").formatted(Formatting.DARK_RED, Formatting.BOLD));
                return true;
            }
        }
        return false;
    }

    private static void onDeath(LivingEntity victim, DamageSource source) {
        if (victim.getCommandTags().contains(MONSTER_TAG)) {
            victim.dropStack(new ItemStack(ModItems.DOUBLOON, 40 + victim.getRandom().nextInt(25)));
            victim.dropStack(new ItemStack(Items.HEART_OF_THE_SEA));
            victim.dropStack(new ItemStack(ModItems.TREASURE_MAP));
            victim.dropStack(new ItemStack(Items.PRISMARINE_SHARD, 16));
            if (victim.getServer() != null) {
                String who = source.getAttacker() instanceof ServerPlayerEntity p ? p.getName().getString() : "the sea itself";
                announce(victim.getServer(), Text.literal("The Brinewyrm is slain, by " + who + "!").formatted(Formatting.GOLD, Formatting.BOLD));
            }
        } else if (victim.getCommandTags().contains("cursedtides_boss")) {
            victim.dropStack(new ItemStack(ModItems.DOUBLOON, 30 + victim.getRandom().nextInt(30)));
            victim.dropStack(new ItemStack(ModItems.ANCIENT_RELIC));
            victim.dropStack(new ItemStack(ModItems.TREASURE_MAP));
            if (victim.getRandom().nextBoolean()) victim.dropStack(new ItemStack(ModItems.MAP_FRAGMENT));
            if (victim.getRandom().nextInt(3) == 0) victim.dropStack(new ItemStack(com.cursedtides.ship.FlagItem.randomRare(victim.getRandom())));
            victim.dropStack(new ItemStack(com.cursedtides.appearance.Accessories.random(victim.getRandom(), true)));
            if (source.getAttacker() instanceof ServerPlayerEntity bossKiller) victim.dropStack(com.cursedtides.explore.Collectibles.randomFor(bossKiller, com.cursedtides.explore.Collectibles.Kind.INSTRUMENT));
            if (victim.getWorld() instanceof ServerWorld bw) victim.dropStack(com.cursedtides.combat.GearRarity.randomWeapon(bw, victim.getRandom(), 3));
            if (victim.getServer() != null && victim.getCustomName() != null) {
                String who = source.getAttacker() instanceof ServerPlayerEntity p ? p.getName().getString() : "the elements";
                announce(victim.getServer(), Text.literal(victim.getCustomName().getString() + " has fallen to " + who + "!")
                        .formatted(Formatting.GOLD, Formatting.BOLD));
            }
        } else if (victim.getCommandTags().contains(INVADER_TAG) && source.getAttacker() instanceof ServerPlayerEntity) {
            victim.dropStack(new ItemStack(ModItems.DOUBLOON, 2 + victim.getRandom().nextInt(3)));
            if (victim.getWorld() instanceof ServerWorld world) {
                var box = victim.getBoundingBox().expand(64);
                boolean anyLeft = !world.getEntitiesByClass(MobEntity.class, box,
                        m -> m != victim && m.isAlive() && m.getCommandTags().contains(INVADER_TAG)).isEmpty();
                if (!anyLeft && world.getServer() != null) {
                    com.cursedtides.living.Prosperity.adjust(world.getServer(), victim.getBlockX(), victim.getBlockZ(), 15, "its defenders held");
                    announce(world.getServer(), Text.literal("The invasion is beaten back. The town drinks to its defenders tonight.")
                            .formatted(Formatting.GREEN));
                }
            }
        }
    }

    private static Archipelago.Island nearestTown(ServerPlayerEntity player) {
        for (Archipelago.Island is : Archipelago.ISLANDS) {
            if (is.style() == Archipelago.Style.TOWN && distanceTo(player, is) < 110) return is;
        }
        return null;
    }

    private static double distanceTo(ServerPlayerEntity player, Archipelago.Island is) {
        return Math.hypot(player.getX() - is.cx(), player.getZ() - is.cz());
    }

    private static BlockPos findSeaSpotNear(ServerWorld world, BlockPos center, int minDist, int maxDist) {
        for (int attempt = 0; attempt < 10; attempt++) {
            double angle = world.random.nextDouble() * Math.PI * 2;
            double d = minDist + world.random.nextDouble() * (maxDist - minDist);
            int x = (int) (center.getX() + Math.cos(angle) * d);
            int z = (int) (center.getZ() + Math.sin(angle) * d);
            if (!world.isChunkLoaded(new BlockPos(x, 0, z))) continue;
            int top = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);
            BlockPos surface = new BlockPos(x, top - 1, z);
            if (world.getFluidState(surface).isIn(FluidTags.WATER) && world.getFluidState(surface.down(3)).isIn(FluidTags.WATER)) {
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

    private static void announce(MinecraftServer server, Text text) {
        server.getPlayerManager().broadcast(text, false);
    }
}
