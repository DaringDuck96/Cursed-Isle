package com.cursedtides.entity;

import com.cursedtides.CursedTides;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.BiomeTags;

public final class ModEntities {
    private ModEntities() {}

    public static final EntityType<CursedPirateEntity> CURSED_PIRATE = Registry.register(
            Registries.ENTITY_TYPE, CursedTides.id("cursed_pirate"),
            EntityType.Builder.<CursedPirateEntity>create(CursedPirateEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.6f, 1.99f)
                    .eyeHeight(1.74f)
                    .maxTrackingRange(8)
                    .build("cursed_pirate"));

    public static final EntityType<QuartermasterEntity> QUARTERMASTER = Registry.register(
            Registries.ENTITY_TYPE, CursedTides.id("quartermaster"),
            EntityType.Builder.<QuartermasterEntity>create(QuartermasterEntity::new, SpawnGroup.CREATURE)
                    .dimensions(0.6f, 1.95f)
                    .eyeHeight(1.62f)
                    .maxTrackingRange(10)
                    .build("quartermaster"));

    public static final EntityType<StoryNpcEntity> STORY_NPC = Registry.register(
            Registries.ENTITY_TYPE, CursedTides.id("story_npc"),
            EntityType.Builder.<StoryNpcEntity>create(StoryNpcEntity::new, SpawnGroup.MISC)
                    .dimensions(0.6f, 1.95f)
                    .eyeHeight(1.62f)
                    .maxTrackingRange(10)
                    .build("story_npc"));

    public static final EntityType<DrownedCaptainEntity> DROWNED_CAPTAIN = Registry.register(
            Registries.ENTITY_TYPE, CursedTides.id("drowned_captain"),
            EntityType.Builder.<DrownedCaptainEntity>create(DrownedCaptainEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.75f, 2.5f)
                    .eyeHeight(2.15f)
                    .maxTrackingRange(10)
                    .build("drowned_captain"));

    public static final EntityType<ShipEntity> SHIP = Registry.register(
            Registries.ENTITY_TYPE, CursedTides.id("ship"),
            EntityType.Builder.<ShipEntity>create(ShipEntity::new, SpawnGroup.MISC)
                    .dimensions(2.4f, 0.5f)
                    .maxTrackingRange(12)
                    .build("ship"));

    public static final EntityType<ShipwrightEntity> SHIPWRIGHT = Registry.register(
            Registries.ENTITY_TYPE, CursedTides.id("shipwright"),
            EntityType.Builder.<ShipwrightEntity>create(ShipwrightEntity::new, SpawnGroup.MISC)
                    .dimensions(0.6f, 1.95f)
                    .eyeHeight(1.62f)
                    .maxTrackingRange(10)
                    .build("shipwright"));

    public static final EntityType<MarineEntity> MARINE = Registry.register(
            Registries.ENTITY_TYPE, CursedTides.id("marine"),
            EntityType.Builder.<MarineEntity>create(MarineEntity::new, SpawnGroup.MISC)
                    .dimensions(0.6f, 1.95f)
                    .eyeHeight(1.62f)
                    .maxTrackingRange(10)
                    .build("marine"));

    public static final EntityType<CrewmateEntity> CREWMATE = Registry.register(
            Registries.ENTITY_TYPE, CursedTides.id("crewmate"),
            EntityType.Builder.<CrewmateEntity>create(CrewmateEntity::new, SpawnGroup.MISC)
                    .dimensions(0.6f, 1.95f)
                    .eyeHeight(1.62f)
                    .maxTrackingRange(10)
                    .build("crewmate"));

    public static final EntityType<ThrowingKnifeEntity> THROWING_KNIFE = Registry.register(
            Registries.ENTITY_TYPE, CursedTides.id("throwing_knife"),
            EntityType.Builder.<ThrowingKnifeEntity>create(ThrowingKnifeEntity::new, SpawnGroup.MISC)
                    .dimensions(0.25f, 0.25f)
                    .maxTrackingRange(4)
                    .trackingTickInterval(10)
                    .build("throwing_knife"));

    public static final EntityType<CannonballEntity> CANNONBALL = Registry.register(
            Registries.ENTITY_TYPE, CursedTides.id("cannonball"),
            EntityType.Builder.<CannonballEntity>create(CannonballEntity::new, SpawnGroup.MISC)
                    .dimensions(0.4f, 0.4f)
                    .maxTrackingRange(8)
                    .trackingTickInterval(5)
                    .build("cannonball"));

    public static final EntityType<HexBoltEntity> HEX_BOLT = Registry.register(
            Registries.ENTITY_TYPE, CursedTides.id("hex_bolt"),
            EntityType.Builder.<HexBoltEntity>create(HexBoltEntity::new, SpawnGroup.MISC)
                    .dimensions(0.3f, 0.3f)
                    .maxTrackingRange(8)
                    .trackingTickInterval(2)
                    .build("hex_bolt"));

    public static void register() {
        FabricDefaultAttributeRegistry.register(CURSED_PIRATE, CursedPirateEntity.createPirateAttributes());
        FabricDefaultAttributeRegistry.register(QUARTERMASTER, MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5));
        FabricDefaultAttributeRegistry.register(STORY_NPC, MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5));
        FabricDefaultAttributeRegistry.register(DROWNED_CAPTAIN, DrownedCaptainEntity.createCaptainAttributes());
        FabricDefaultAttributeRegistry.register(MARINE, MarineEntity.createMarineAttributes());
        FabricDefaultAttributeRegistry.register(CREWMATE, CrewmateEntity.createCrewAttributes());
        FabricDefaultAttributeRegistry.register(SHIPWRIGHT, MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5));
    }

    /** Cursed crews wash up on beaches at night; quartermasters set up shop on some beaches. */
    public static void registerSpawns() {
        BiomeModifications.addSpawn(BiomeSelectors.tag(BiomeTags.IS_BEACH),
                SpawnGroup.MONSTER, CURSED_PIRATE, 60, 2, 4);
        BiomeModifications.addSpawn(BiomeSelectors.tag(BiomeTags.IS_BEACH),
                SpawnGroup.CREATURE, QUARTERMASTER, 4, 1, 1);
    }
}
