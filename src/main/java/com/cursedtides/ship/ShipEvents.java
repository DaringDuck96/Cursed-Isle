package com.cursedtides.ship;

import com.cursedtides.entity.ModEntities;
import com.cursedtides.entity.ShipEntity;
import com.cursedtides.faction.FactionData;
import com.cursedtides.world.Archipelago;
import com.cursedtides.world.ModWorld;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import com.cursedtides.item.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
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
import net.minecraft.world.World;

/** Ship orders from the keyboard, and ghost ship sightings at sea. */
public final class ShipEvents {
    private ShipEvents() {}

    private static final String[] GHOST_NAMES = {
            "The Weeping Maren", "Saltgrave", "The Lantern Widow", "Coldwake",
            "The Ninth Fathom", "Mourning Gull", "The Hollow Psalm", "Brinewraith"
    };
    private static final String[] NAVY_NAMES = {
            "Steadfast", "Vigilant", "Ironwatch", "Lodestar", "Sentinel", "Bulwark", "Warden", "Tideguard"
    };

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ShipControlPayload.ID, ShipControlPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ShipControlPayload.ID, (payload, context) ->
                context.server().execute(() -> handle(context.player(), payload.action())));
        ServerTickEvents.END_SERVER_TICK.register(ShipEvents::tick);
    }

    private static void handle(ServerPlayerEntity player, int action) {
        if (action == ShipControlPayload.OPEN_JOURNAL) {
            com.cursedtides.ui.UiData.sendJournal(player);
            return;
        }
        if (action == ShipControlPayload.OPEN_MAP) {
            com.cursedtides.ui.UiData.sendMap(player);
            return;
        }
        if (action == ShipControlPayload.OPEN_SKILLS) {
            com.cursedtides.pirate.PerkData.sendSkills(player);
            return;
        }
        if (action == ShipControlPayload.ULTIMATE) {
            com.cursedtides.combat.Ultimates.use(player);
            return;
        }
        if (action == ShipControlPayload.BELOW_DECK) {
            BelowDeck.toggle(player);
            return;
        }
        if (action >= ShipControlPayload.DODGE_LEFT && action <= ShipControlPayload.DODGE_BACK) {
            com.cursedtides.combat.CombatData.dodge(player, action - ShipControlPayload.DODGE_LEFT);
            return;
        }
        if (!(player.getVehicle() instanceof ShipEntity ship)) {
            return;
        }
        if (action == ShipControlPayload.CYCLE_AMMO) {
            ship.cycleAmmo(player);
            return;
        }
        switch (action) {
            case ShipControlPayload.SAILS_UP, ShipControlPayload.SAILS_DOWN -> {
                if (ship.getControllingPassenger() != player) {
                    player.sendMessage(Text.literal("Only the captain at the helm trims the sails.").formatted(Formatting.GRAY), true);
                    return;
                }
                ship.setSail(ship.getSail() + (action == ShipControlPayload.SAILS_UP ? 1 : -1));
                player.getWorld().playSound(null, ship.getX(), ship.getY(), ship.getZ(),
                        SoundEvents.BLOCK_WOOL_PLACE, SoundCategory.PLAYERS, 1.0f, 0.6f);
            }
            case ShipControlPayload.FIRE_SWIVEL -> {
                if (!ship.isHostile() && (ship.isOwner(player)
                        || com.cursedtides.social.CrewState.shipmates(player.getServer(), ship.getOwner(), player.getUuid()))) {
                    ship.fireSwivel(player);
                }
            }
            case ShipControlPayload.ANCHOR -> {
                if (!ship.isHostile() && (ship.isOwner(player)
                        || com.cursedtides.social.CrewState.shipmates(player.getServer(), ship.getOwner(), player.getUuid()))) {
                    ship.toggleAnchor(player);
                }
            }
            case ShipControlPayload.FIRE_PORT -> ship.playerBroadside(player, -1);
            case ShipControlPayload.FIRE_STARBOARD -> ship.playerBroadside(player, 1);
            case ShipControlPayload.OPEN_HOLD -> {
                if (ship.isOwner(player) || com.cursedtides.social.CrewState.shipmates(player.getServer(), ship.getOwner(), player.getUuid())) {
                    ship.openCargo(player);
                } else {
                    player.sendMessage(Text.literal("Only the captain and her crew open the hold.").formatted(Formatting.GRAY), true);
                }
            }
            default -> { }
        }
    }

    // ------------------------------------------------------------------ ghost ship sightings

    private static void tick(MinecraftServer server) {
        if (server.getTicks() % 1200 != 0) {
            return;
        }
        ServerWorld world = server.getOverworld();
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (!(player.getVehicle() instanceof BoatEntity) || player.isCreative() || player.isSpectator()) {
                continue;
            }
            boolean night = world.isNight();
            var box = player.getBoundingBox().expand(200);
            boolean ghostNear = !world.getEntitiesByClass(ShipEntity.class, box, s -> s.getFaction() == ShipFaction.DROWNED).isEmpty();
            boolean navyNear = !world.getEntitiesByClass(ShipEntity.class, box, s -> s.getFaction() == ShipFaction.NAVY).isEmpty();

            float ghostChance = (night ? 0.30f : 0.10f)
                    * (com.cursedtides.quest.HorizonArc.ending(player) == com.cursedtides.quest.HorizonArc.SEALED ? 0.4f : 1.0f);
            float navyChance = (night ? 0.06f : 0.15f) + (FactionData.isWanted(player) ? 0.20f : 0.0f) + nearIronwatch(player);
            float roll = world.random.nextFloat();
            if (roll < ghostChance) {
                if (!ghostNear) trySpawn(world, player, ShipFaction.DROWNED);
            } else if (roll < ghostChance + navyChance) {
                if (!navyNear) trySpawn(world, player, ShipFaction.NAVY);
            }
        }
    }

    /** Patrols are thicker near the fortress. */
    private static float nearIronwatch(ServerPlayerEntity player) {
        if (!ModWorld.isArchipelago()) return 0.0f;
        var fort = Archipelago.IRONWATCH_ISLE;
        double dx = player.getX() - fort.cx(), dz = player.getZ() - fort.cz();
        return dx * dx + dz * dz < 250 * 250 ? 0.20f : 0.0f;
    }

    private static void trySpawn(ServerWorld world, ServerPlayerEntity player, ShipFaction faction) {
        for (int attempt = 0; attempt < 6; attempt++) {
            double angle = world.random.nextDouble() * Math.PI * 2;
            double distance = 60 + world.random.nextDouble() * 20;
            int x = (int) (player.getX() + Math.cos(angle) * distance);
            int z = (int) (player.getZ() + Math.sin(angle) * distance);
            BlockPos column = new BlockPos(x, 0, z);
            if (!world.isChunkLoaded(column)) {
                continue;
            }
            int top = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);
            BlockPos surface = new BlockPos(x, top - 1, z);
            if (!world.getFluidState(surface).isIn(FluidTags.WATER) || !world.getFluidState(surface.down(3)).isIn(FluidTags.WATER)) {
                continue; // needs open, deep-ish water
            }
            spawnShip(world, faction, x + 0.5, top - 0.1, z + 0.5, player);
            String bearing = Wind.compass((float) Math.toDegrees(Math.atan2(-(x - player.getX()), z - player.getZ())));
            if (faction == ShipFaction.DROWNED) {
                player.sendMessage(Text.literal("Sails on the horizon to the " + bearing + "... a ghost ship!")
                        .formatted(Formatting.DARK_AQUA, Formatting.ITALIC), false);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_BELL_RESONATE,
                        SoundCategory.HOSTILE, 1.0f, 0.5f);
            } else {
                boolean wanted = FactionData.isWanted(player);
                player.sendMessage(Text.literal("Tidewatch colours to the " + bearing
                        + (wanted ? ". She's coming about to hunt you!" : ". A patrol on her rounds."))
                        .formatted(wanted ? Formatting.RED : Formatting.BLUE, Formatting.ITALIC), false);
            }
            return;
        }
    }

    private static final String[] MERCHANT_NAMES = {
            "Fair Abundance", "Meridian Star", "Good Hope of Kingsport", "Plenty", "Saint Vesper's Grace", "Golden Bale"
    };
    private static final String[] PIRATE_NAMES = {
            "Red Gull", "Widow's Kiss", "Sharktooth", "Bloodwake", "The Hungry Tide", "Black Marrow"
    };

    /** Open, deep water between minDist and maxDist from the player, or null. */
    public static BlockPos findSeaSpot(ServerWorld world, ServerPlayerEntity player, double minDist, double maxDist) {
        for (int attempt = 0; attempt < 8; attempt++) {
            double angle = world.random.nextDouble() * Math.PI * 2;
            double distance = minDist + world.random.nextDouble() * (maxDist - minDist);
            int x = (int) (player.getX() + Math.cos(angle) * distance);
            int z = (int) (player.getZ() + Math.sin(angle) * distance);
            if (!world.isChunkLoaded(new BlockPos(x, 0, z))) continue;
            int top = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);
            BlockPos surface = new BlockPos(x, top - 1, z);
            if (world.getFluidState(surface).isIn(FluidTags.WATER) && world.getFluidState(surface.down(3)).isIn(FluidTags.WATER)) {
                return new BlockPos(x, top, z);
            }
        }
        return null;
    }

    public static ShipEntity spawnShip(ServerWorld world, ShipFaction faction, double x, double y, double z, ServerPlayerEntity facing) {
        ShipEntity ship = ModEntities.SHIP.create(world);
        if (ship == null) {
            return null;
        }
        int roll = world.random.nextInt(4);
        ShipClass cls = switch (faction) {
            case NAVY -> roll == 0 ? ShipClass.FRIGATE : roll == 1 ? ShipClass.SLOOP : roll == 2 ? ShipClass.BRIG : ShipClass.BRIGANTINE;
            case MERCHANT -> roll == 0 ? ShipClass.GALLEON : roll == 1 ? ShipClass.SCHOONER : ShipClass.BRIGANTINE;
            case PIRATE -> roll == 0 ? ShipClass.BRIGANTINE : roll == 1 ? ShipClass.SCHOONER : ShipClass.SLOOP;
            default -> roll == 0 ? ShipClass.GALLEON : ShipClass.BRIGANTINE;
        };
        ship.setShipClass(cls);
        ship.setHull(cls.maxHull());
        ship.setFaction(faction);
        ship.setLook(faction == ShipFaction.NAVY ? ShipEntity.LOOK_NAVY : faction == ShipFaction.DROWNED ? ShipEntity.LOOK_GHOST : ShipEntity.LOOK_PLAIN);
        ship.setFlag(switch (faction) {
            case NAVY -> com.cursedtides.ship.FlagItem.Design.TIDEWATCH.ordinal();
            case PIRATE -> com.cursedtides.ship.FlagItem.Design.BLACK_SKULL.ordinal();
            case MERCHANT -> com.cursedtides.ship.FlagItem.Design.MERCHANT.ordinal();
            case DROWNED -> com.cursedtides.ship.FlagItem.Design.CURSED_TIDE.ordinal();
            default -> 0;
        });
        ship.setSail(2);
        String name;
        Formatting color;
        switch (faction) {
            case NAVY -> { name = "TWS " + NAVY_NAMES[world.random.nextInt(NAVY_NAMES.length)]; color = Formatting.BLUE; }
            case MERCHANT -> { name = MERCHANT_NAMES[world.random.nextInt(MERCHANT_NAMES.length)]; color = Formatting.GREEN; }
            case PIRATE -> { name = PIRATE_NAMES[world.random.nextInt(PIRATE_NAMES.length)]; color = Formatting.RED; }
            default -> { name = GHOST_NAMES[world.random.nextInt(GHOST_NAMES.length)]; color = Formatting.DARK_AQUA; }
        }
        // The sea has levels as the islands do: the further out, the tougher the crews
        int[] band = com.cursedtides.combat.Levels.bandAt(net.minecraft.util.math.BlockPos.ofFloored(x, y, z));
        int level = band[0] + world.random.nextInt(band[1] - band[0] + 1);
        ship.addCommandTag(com.cursedtides.combat.Levels.TAG_PREFIX + level);
        ship.setCustomName(Text.literal(name).formatted(color).append(Text.literal(" [Lv " + level + "]").formatted(Formatting.GRAY)));
        float yaw = (float) Math.toDegrees(Math.atan2(-(facing.getX() - x), facing.getZ() - z));
        if ((faction == ShipFaction.NAVY && !FactionData.isWanted(facing)) || faction == ShipFaction.MERCHANT) {
            yaw += 90.0f; // sail past, not at you
        }
        ship.refreshPositionAndAngles(x, y, z, yaw, 0.0f);
        ship.calculateDimensions();
        if (faction == ShipFaction.MERCHANT) {
            com.cursedtides.trade.TradeGood[] all = com.cursedtides.trade.TradeGood.values();
            Item[] goods = new Item[all.length + 3];
            for (int k = 0; k < all.length; k++) {
                // merchants carry honest cargo; now and then something they shouldn't
                goods[k] = all[k].contraband && all[k] != com.cursedtides.trade.TradeGood.STOLEN_PLATE ? all[0].item() : all[k].item();
            }
            goods[all.length] = Items.GOLD_INGOT;
            goods[all.length + 1] = ModItems.DOUBLOON;
            goods[all.length + 2] = Items.EMERALD;
            for (int i = 0; i < 6 + world.random.nextInt(5); i++) {
                Item item = goods[world.random.nextInt(goods.length)];
                ship.cargo().setStack(i, new ItemStack(item, Math.min(item.getMaxCount(), 2 + world.random.nextInt(14))));
            }
        }
        world.spawnEntity(ship);
        if (faction == ShipFaction.MERCHANT) {
            return ship; // unarmed and uncrewed: board her and she's yours
        }
        int crew = Math.max(2, Math.min(4, cls.seats() - 2));
        for (int i = 0; i < crew; i++) {
            MobEntity sailor = switch (faction) {
                case NAVY -> ModEntities.MARINE.create(world);
                case PIRATE -> i % 2 == 0 ? EntityType.VINDICATOR.create(world) : EntityType.PILLAGER.create(world);
                default -> ModEntities.CURSED_PIRATE.create(world);
            };
            if (sailor == null) continue;
            sailor.refreshPositionAndAngles(x, y + 1, z, yaw, 0.0f);
            sailor.initialize(world, world.getLocalDifficulty(ship.getBlockPos()), SpawnReason.EVENT, null);
            if (faction == ShipFaction.DROWNED) {
                sailor.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET)); // keeps the sun off
            }
            world.spawnEntity(sailor);
            sailor.startRiding(ship, true);
        }
        return ship;
    }

    public static boolean isOverworld(World world) {
        return world.getRegistryKey() == World.OVERWORLD;
    }
}
