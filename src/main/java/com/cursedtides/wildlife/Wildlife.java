package com.cursedtides.wildlife;

import com.cursedtides.CursedTides;
import com.cursedtides.world.Archipelago;
import com.cursedtides.world.Builds;
import com.cursedtides.world.ModWorld;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;

/** The creatures of the islands and the sea, and the spawner that keeps them around players. */
public final class Wildlife {
    private Wildlife() {}

    public static final String TAG = "cursedtides_wild";

    private static <T extends net.minecraft.entity.Entity> EntityType<T> type(String name, EntityType.EntityFactory<T> factory,
                                                                             SpawnGroup group, float w, float h) {
        return Registry.register(Registries.ENTITY_TYPE, CursedTides.id(name),
                EntityType.Builder.create(factory, group).dimensions(w, h).maxTrackingRange(8).build(name));
    }

    public static final EntityType<SharkEntity> SHARK = type("shark", SharkEntity::new, SpawnGroup.WATER_CREATURE, 1.3f, 0.9f);
    public static final EntityType<CrocodileEntity> CROCODILE = type("crocodile", CrocodileEntity::new, SpawnGroup.MONSTER, 1.2f, 0.6f);
    public static final EntityType<JaguarEntity> JAGUAR = type("jaguar", JaguarEntity::new, SpawnGroup.MONSTER, 0.7f, 0.8f);
    public static final EntityType<SnakeEntity> SNAKE = type("snake", SnakeEntity::new, SpawnGroup.MONSTER, 0.6f, 0.3f);
    public static final EntityType<WildAnimalEntity> BOAR = type("boar", WildAnimalEntity::new, SpawnGroup.CREATURE, 0.9f, 0.9f);
    public static final EntityType<WildAnimalEntity> CRAB = type("crab", WildAnimalEntity::new, SpawnGroup.CREATURE, 0.6f, 0.35f);
    public static final EntityType<MonkeyEntity> MONKEY = type("monkey", MonkeyEntity::new, SpawnGroup.CREATURE, 0.5f, 0.9f);

    private static Item egg(String name, EntityType<? extends MobEntity> type, int a, int b) {
        return Registry.register(Registries.ITEM, CursedTides.id(name + "_spawn_egg"), new SpawnEggItem(type, a, b, new Item.Settings()));
    }

    public static final Item SHARK_EGG = egg("shark", SHARK, 0x627080, 0xE2E4E6);
    public static final Item CROCODILE_EGG = egg("crocodile", CROCODILE, 0x3E5430, 0xAAA878);
    public static final Item JAGUAR_EGG = egg("jaguar", JAGUAR, 0xD4A03E, 0x281E16);
    public static final Item SNAKE_EGG = egg("snake", SNAKE, 0x467832, 0xC8C46E);
    public static final Item BOAR_EGG = egg("boar", BOAR, 0x60422E, 0x302018);
    public static final Item CRAB_EGG = egg("crab", CRAB, 0xCC462C, 0xECBE96);
    public static final Item MONKEY_EGG = egg("monkey", MONKEY, 0x6E4C30, 0xD6AA82);

    public static void register() {
        FabricDefaultAttributeRegistry.register(SHARK, SharkEntity.createSharkAttributes());
        FabricDefaultAttributeRegistry.register(CROCODILE, CrocodileEntity.createCrocAttributes());
        FabricDefaultAttributeRegistry.register(JAGUAR, JaguarEntity.createJaguarAttributes());
        FabricDefaultAttributeRegistry.register(SNAKE, SnakeEntity.createSnakeAttributes());
        FabricDefaultAttributeRegistry.register(BOAR, WildAnimalEntity.createBoarAttributes());
        FabricDefaultAttributeRegistry.register(CRAB, WildAnimalEntity.createCrabAttributes());
        FabricDefaultAttributeRegistry.register(MONKEY, MonkeyEntity.createMonkeyAttributes());
        ItemGroupEvents.modifyEntriesEvent(RegistryKey.of(RegistryKeys.ITEM_GROUP, CursedTides.id("main"))).register(entries -> {
            for (Item egg : new Item[]{SHARK_EGG, CROCODILE_EGG, JAGUAR_EGG, SNAKE_EGG, BOAR_EGG, CRAB_EGG, MONKEY_EGG}) {
                entries.add(egg);
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(Wildlife::tick);
    }

    // ------------------------------------------------------------------ spawning

    private static void tick(MinecraftServer server) {
        if (server.getTicks() % 160 != 0 || !ModWorld.isArchipelago()) return;
        ServerWorld world = server.getOverworld();
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (player.isSpectator()) continue;
            int nearby = world.getEntitiesByClass(MobEntity.class, player.getBoundingBox().expand(64),
                    m -> m.getCommandTags().contains(TAG)).size();
            if (nearby >= 8) continue;
            trySpawn(world, player);
        }
    }

    private static void trySpawn(ServerWorld world, ServerPlayerEntity player) {
        Random r = world.random;
        double angle = r.nextDouble() * Math.PI * 2;
        double dist = 24 + r.nextDouble() * 24;
        int x = (int) (player.getX() + Math.cos(angle) * dist);
        int z = (int) (player.getZ() + Math.sin(angle) * dist);
        if (!world.isChunkLoaded(new BlockPos(x, 0, z)) || Builds.blocksPlants(x, z)) return;
        Archipelago.Column col = Archipelago.column(x, z);
        int top = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos ground = new BlockPos(x, top, z);
        boolean water = !world.getFluidState(ground.down()).isEmpty();
        boolean night = world.isNight();
        float roll = r.nextFloat();

        EntityType<? extends MobEntity> pick = null;
        double y = top;
        if (water) {
            if (col.island() == null || col.top() < 50) {
                // Open sea
                if (roll < (night ? 0.4f : 0.2f)) { pick = SHARK; y = Archipelago.WATER_TOP - 4; }
                else if (roll < 0.35f) { pick = EntityType.DOLPHIN; y = Archipelago.WATER_TOP - 1; }
            } else if (col.island().style() == Archipelago.Style.REEF || col.island().style() == Archipelago.Style.ATOLL
                    || col.island().style() == Archipelago.Style.KEYS) {
                if (roll < 0.25f) { pick = EntityType.TURTLE; y = col.top() + 1; }
                else if (roll < 0.6f) { pick = EntityType.TROPICAL_FISH; y = col.top() + 2; }
                else if (roll < 0.7f) { pick = SHARK; y = Archipelago.WATER_TOP - 3; }
            } else if (col.island().style() == Archipelago.Style.BAYOU && roll < 0.35f) {
                pick = CROCODILE; y = col.top() + 1;
            }
        } else if (col.isLand()) {
            boolean shore = col.top() <= Archipelago.WATER_TOP + 2;
            switch (col.island().style()) {
                case JUNGLE, ANCIENT -> pick = roll < 0.2f ? JAGUAR : roll < 0.38f ? SNAKE : roll < 0.6f ? MONKEY
                        : roll < 0.8f ? EntityType.PARROT : roll < 0.9f ? BOAR : null;
                case BAYOU -> pick = roll < 0.35f ? CROCODILE : roll < 0.55f ? SNAKE : roll < 0.8f ? EntityType.FROG : null;
                case DESERT -> pick = shore ? CRAB : roll < 0.3f ? SNAKE : null;
                case SANDY, KEYS -> pick = shore ? (roll < 0.6f ? CRAB : roll < 0.75f ? EntityType.TURTLE : null)
                        : roll < 0.3f ? EntityType.PARROT : roll < 0.5f ? BOAR : null;
                case TOWN -> pick = shore && roll < 0.4f ? CRAB : roll < 0.15f ? EntityType.PARROT : null;
                case FORTRESS, CLIFFS, CRAGS -> pick = shore && roll < 0.5f ? CRAB : !shore && roll < 0.15f ? BOAR : null;
                case CURSED, ATOLL -> pick = night && roll < 0.35f ? EntityType.STRAY : shore && roll < 0.4f ? CRAB : null;
                default -> { }
            }
        }
        // The island creatures (see IslandBeasts): a second roll, so they add to what's above
        if (!water && col.isLand()) {
            float r2 = r.nextFloat();
            EntityType<? extends MobEntity> beast = switch (col.island().style()) {
                case JUNGLE, ANCIENT -> r2 < 0.12f ? IslandBeasts.GIANT_WASP : r2 < 0.2f ? IslandBeasts.SNAPJAW : null;
                case BAYOU -> r2 < 0.12f ? IslandBeasts.SNAPJAW : r2 < 0.22f ? IslandBeasts.LIVING_STUMP : night && r2 < 0.32f ? IslandBeasts.CAVE_BAT : null;
                case DESERT, VOLCANIC -> r2 < 0.25f ? IslandBeasts.DUNE_SCORPION : night && r2 < 0.32f ? IslandBeasts.CAVE_BAT : null;
                case CURSED -> r2 < 0.15f ? IslandBeasts.LIVING_STUMP : night && r2 < 0.3f ? IslandBeasts.CAVE_BAT : null;
                case FORTRESS, CLIFFS, CRAGS -> night && r2 < 0.14f ? IslandBeasts.CAVE_BAT : null;
                case SANDY, KEYS -> r2 < 0.07f ? IslandBeasts.GIANT_WASP : null;
                default -> null;
            };
            if (beast != null) {
                pick = beast;
                y = top + (beast == IslandBeasts.GIANT_WASP || beast == IslandBeasts.CAVE_BAT ? 3 : 0);
            }
        }
        if (pick == null) return;
        MobEntity mob = pick.create(world);
        if (mob == null) return;
        mob.refreshPositionAndAngles(x + 0.5, y, z + 0.5, r.nextFloat() * 360, 0);
        if (!world.isSpaceEmpty(mob)) return;
        mob.initialize(world, world.getLocalDifficulty(ground), SpawnReason.NATURAL, null);
        mob.addCommandTag(TAG);
        if (mob instanceof CrocodileEntity croc) croc.setAlligator(col.island() != null && col.island().style() == Archipelago.Style.BAYOU);
        if (pick == EntityType.STRAY) {
            mob.setCustomName(Text.literal("Spectral Sailor").formatted(Formatting.AQUA));
            mob.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                    net.minecraft.entity.effect.StatusEffects.INVISIBILITY, 20 * 60 * 5, 0, false, false));
        }
        world.spawnEntity(mob);
    }
}
