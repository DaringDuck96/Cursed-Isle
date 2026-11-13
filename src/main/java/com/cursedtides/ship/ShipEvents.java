package com.cursedtides.ship;

import net.minecraft.world.entity.EntityTypes;
import com.cursedtides.entity.ModEntities;
import com.cursedtides.entity.ShipEntity;
import com.cursedtides.faction.FactionData;
import com.cursedtides.world.Archipelago;
import com.cursedtides.world.ModWorld;
import com.cursedtides.item.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

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
        PayloadTypeRegistry.serverboundPlay().register(ShipControlPayload.ID, ShipControlPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ShipControlPayload.ID, (payload, context) ->
                context.server().execute(() -> handle(context.player(), payload.action())));
        ServerTickEvents.END_SERVER_TICK.register(ShipEvents::tick);
    }

    private static void handle(ServerPlayer player, int action) {
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
                    player.sendSystemMessage(Component.literal("Only the captain at the helm trims the sails.").withStyle(ChatFormatting.GRAY), true);
                    return;
                }
                ship.setSail(ship.getSail() + (action == ShipControlPayload.SAILS_UP ? 1 : -1));
                player.level().playSound(null, ship.getX(), ship.getY(), ship.getZ(),
                        SoundEvents.WOOL_PLACE, SoundSource.PLAYERS, 1.0f, 0.6f);
            }
            case ShipControlPayload.FIRE_SWIVEL -> {
                if (!ship.isHostile() && (ship.isOwner(player)
                        || com.cursedtides.social.CrewState.shipmates(player.level().getServer(), ship.getOwner(), player.getUUID()))) {
                    ship.fireSwivel(player);
                }
            }
            case ShipControlPayload.ANCHOR -> {
                if (!ship.isHostile() && (ship.isOwner(player)
                        || com.cursedtides.social.CrewState.shipmates(player.level().getServer(), ship.getOwner(), player.getUUID()))) {
                    ship.toggleAnchor(player);
                }
            }
            case ShipControlPayload.FIRE_PORT -> ship.playerBroadside(player, -1);
            case ShipControlPayload.FIRE_STARBOARD -> ship.playerBroadside(player, 1);
            case ShipControlPayload.OPEN_HOLD -> {
                if (ship.isOwner(player) || com.cursedtides.social.CrewState.shipmates(player.level().getServer(), ship.getOwner(), player.getUUID())) {
                    ship.openCargo(player);
                } else {
                    player.sendSystemMessage(Component.literal("Only the captain and her crew open the hold.").withStyle(ChatFormatting.GRAY), true);
                }
            }
            default -> { }
        }
    }

    // ------------------------------------------------------------------ ghost ship sightings

    private static void tick(MinecraftServer server) {
        if (server.getTickCount() % 1200 != 0) {
            return;
        }
        ServerLevel world = server.overworld();
        for (ServerPlayer player : world.players()) {
            if (!(player.getVehicle() instanceof Boat) || player.isCreative() || player.isSpectator()) {
                continue;
            }
            boolean night = world.isDarkOutside();
            var box = player.getBoundingBox().inflate(200);
            boolean ghostNear = !world.getEntitiesOfClass(ShipEntity.class, box, s -> s.getFaction() == ShipFaction.DROWNED).isEmpty();
            boolean navyNear = !world.getEntitiesOfClass(ShipEntity.class, box, s -> s.getFaction() == ShipFaction.NAVY).isEmpty();

            float ghostChance = (night ? 0.30f : 0.10f)
                    * (com.cursedtides.quest.HorizonArc.ending(player) == com.cursedtides.quest.HorizonArc.SEALED ? 0.4f : 1.0f);
            float navyChance = (night ? 0.06f : 0.15f) + (FactionData.isWanted(player) ? 0.20f : 0.0f) + nearIronwatch(player);
            float roll = world.getRandom().nextFloat();
            if (roll < ghostChance) {
                if (!ghostNear) trySpawn(world, player, ShipFaction.DROWNED);
            } else if (roll < ghostChance + navyChance) {
                if (!navyNear) trySpawn(world, player, ShipFaction.NAVY);
            }
        }
    }

    /** Patrols are thicker near the fortress. */
    private static float nearIronwatch(ServerPlayer player) {
        if (!ModWorld.isArchipelago()) return 0.0f;
        var fort = Archipelago.IRONWATCH_ISLE;
        double dx = player.getX() - fort.cx(), dz = player.getZ() - fort.cz();
        return dx * dx + dz * dz < 250 * 250 ? 0.20f : 0.0f;
    }

    private static void trySpawn(ServerLevel world, ServerPlayer player, ShipFaction faction) {
        for (int attempt = 0; attempt < 6; attempt++) {
            double angle = world.getRandom().nextDouble() * Math.PI * 2;
            double distance = 60 + world.getRandom().nextDouble() * 20;
            int x = (int) (player.getX() + Math.cos(angle) * distance);
            int z = (int) (player.getZ() + Math.sin(angle) * distance);
            BlockPos column = new BlockPos(x, 0, z);
            if (!world.hasChunkAt(column)) {
                continue;
            }
            int top = world.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
            BlockPos surface = new BlockPos(x, top - 1, z);
            if (!world.getFluidState(surface).is(FluidTags.WATER) || !world.getFluidState(surface.below(3)).is(FluidTags.WATER)) {
                continue; // needs open, deep-ish water
            }
            spawnShip(world, faction, x + 0.5, top - 0.1, z + 0.5, player);
            String bearing = Wind.compass((float) Math.toDegrees(Math.atan2(-(x - player.getX()), z - player.getZ())));
            if (faction == ShipFaction.DROWNED) {
                player.sendSystemMessage(Component.literal("Sails on the horizon to the " + bearing + "... a ghost ship!")
                        .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC), false);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BELL_RESONATE,
                        SoundSource.HOSTILE, 1.0f, 0.5f);
            } else {
                boolean wanted = FactionData.isWanted(player);
                player.sendSystemMessage(Component.literal("Tidewatch colours to the " + bearing
                        + (wanted ? ". She's coming about to hunt you!" : ". A patrol on her rounds."))
                        .withStyle(wanted ? ChatFormatting.RED : ChatFormatting.BLUE, ChatFormatting.ITALIC), false);
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
    public static BlockPos findSeaSpot(ServerLevel world, ServerPlayer player, double minDist, double maxDist) {
        for (int attempt = 0; attempt < 8; attempt++) {
            double angle = world.getRandom().nextDouble() * Math.PI * 2;
            double distance = minDist + world.getRandom().nextDouble() * (maxDist - minDist);
            int x = (int) (player.getX() + Math.cos(angle) * distance);
            int z = (int) (player.getZ() + Math.sin(angle) * distance);
            if (!world.hasChunkAt(new BlockPos(x, 0, z))) continue;
            int top = world.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
            BlockPos surface = new BlockPos(x, top - 1, z);
            if (world.getFluidState(surface).is(FluidTags.WATER) && world.getFluidState(surface.below(3)).is(FluidTags.WATER)) {
                return new BlockPos(x, top, z);
            }
        }
        return null;
    }

    public static ShipEntity spawnShip(ServerLevel world, ShipFaction faction, double x, double y, double z, ServerPlayer facing) {
        ShipEntity ship = ModEntities.SHIP.create(world, EntitySpawnReason.EVENT);
        if (ship == null) {
            return null;
        }
        int roll = world.getRandom().nextInt(4);
        ShipClass cls = switch (faction) {
            case NAVY -> roll == 0 ? ShipClass.FRIGATE : roll == 1 ? ShipClass.SLOOP : roll == 2 ? ShipClass.BRIG : ShipClass.BRIGANTINE;
            case MERCHANT -> roll == 0 ? ShipClass.GALLEON : roll == 1 ? ShipClass.SCHOONER : ShipClass.BRIGANTINE;
            case PIRATE -> roll == 0 ? ShipClass.BRIGANTINE : roll == 1 ? ShipClass.SCHOONER : ShipClass.SLOOP;
            default -> roll == 0 ? ShipClass.GALLEON : ShipClass.BRIGANTINE;
        };
        ship.setShipClass(cls);
        // The old game's light and war ships: the Navy favours war ships, raiders light ones
        float vr = world.getRandom().nextFloat();
        if (faction == ShipFaction.NAVY && vr < 0.3f) ship.setRefit(ShipEntity.WAR);
        else if (faction == ShipFaction.PIRATE && vr < 0.35f) ship.setRefit(ShipEntity.LIGHT);
        else if (faction == ShipFaction.DROWNED && vr < 0.2f) ship.setRefit(ShipEntity.WAR);
        ship.setHull(ship.maxHull());
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
        ChatFormatting color;
        switch (faction) {
            case NAVY -> { name = "TWS " + NAVY_NAMES[world.getRandom().nextInt(NAVY_NAMES.length)]; color = ChatFormatting.BLUE; }
            case MERCHANT -> { name = MERCHANT_NAMES[world.getRandom().nextInt(MERCHANT_NAMES.length)]; color = ChatFormatting.GREEN; }
            case PIRATE -> { name = PIRATE_NAMES[world.getRandom().nextInt(PIRATE_NAMES.length)]; color = ChatFormatting.RED; }
            default -> { name = GHOST_NAMES[world.getRandom().nextInt(GHOST_NAMES.length)]; color = ChatFormatting.DARK_AQUA; }
        }
        // The sea has levels as the islands do: the further out, the tougher the crews
        int[] band = com.cursedtides.combat.Levels.bandAt(net.minecraft.core.BlockPos.containing(x, y, z));
        int level = band[0] + world.getRandom().nextInt(band[1] - band[0] + 1);
        ship.addTag(com.cursedtides.combat.Levels.TAG_PREFIX + level);
        ship.setCustomName(Component.literal(name).withStyle(color).append(Component.literal(" [Lv " + level + "]").withStyle(ChatFormatting.GRAY)));
        float yaw = (float) Math.toDegrees(Math.atan2(-(facing.getX() - x), facing.getZ() - z));
        if ((faction == ShipFaction.NAVY && !FactionData.isWanted(facing)) || faction == ShipFaction.MERCHANT) {
            yaw += 90.0f; // sail past, not at you
        }
        ship.snapTo(x, y, z, yaw, 0.0f);
        ship.refreshDimensions();
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
            for (int i = 0; i < 6 + world.getRandom().nextInt(5); i++) {
                Item item = goods[world.getRandom().nextInt(goods.length)];
                ship.cargo().setItem(i, new ItemStack(item, Math.min(item.getDefaultMaxStackSize(), 2 + world.getRandom().nextInt(14))));
            }
        }
        world.addFreshEntity(ship);
        if (faction == ShipFaction.MERCHANT) {
            return ship; // unarmed and uncrewed: board her and she's yours
        }
        int crew = Math.max(2, Math.min(4, cls.seats() - 2));
        for (int i = 0; i < crew; i++) {
            Mob sailor = switch (faction) {
                case NAVY -> ModEntities.MARINE.create(world, EntitySpawnReason.EVENT);
                case PIRATE -> i % 2 == 0 ? EntityTypes.VINDICATOR.create(world, EntitySpawnReason.EVENT) : EntityTypes.PILLAGER.create(world, EntitySpawnReason.EVENT);
                default -> ModEntities.CURSED_PIRATE.create(world, EntitySpawnReason.EVENT);
            };
            if (sailor == null) continue;
            sailor.snapTo(x, y + 1, z, yaw, 0.0f);
            sailor.finalizeSpawn(world, world.getCurrentDifficultyAt(ship.blockPosition()), EntitySpawnReason.EVENT, null);
            if (faction == ShipFaction.DROWNED) {
                sailor.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET)); // keeps the sun off
            }
            world.addFreshEntity(sailor);
            sailor.startRiding(ship, true, true);
        }
        return ship;
    }

    public static boolean isOverworld(Level world) {
        return world.dimension() == Level.OVERWORLD;
    }
}
