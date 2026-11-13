package com.cursedtides.entity;

import com.cursedtides.CursedTides;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class ModEntities {
    private ModEntities() {}

    public static final EntityType<CursedPirateEntity> CURSED_PIRATE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE, CursedTides.id("cursed_pirate"),
            EntityType.Builder.<CursedPirateEntity>of(CursedPirateEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.99f)
                    .eyeHeight(1.74f)
                    .clientTrackingRange(8)
                    .build("cursed_pirate"));

    public static final EntityType<QuartermasterEntity> QUARTERMASTER = Registry.register(
            BuiltInRegistries.ENTITY_TYPE, CursedTides.id("quartermaster"),
            EntityType.Builder.<QuartermasterEntity>of(QuartermasterEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.95f)
                    .eyeHeight(1.62f)
                    .clientTrackingRange(10)
                    .build("quartermaster"));

    public static final EntityType<StoryNpcEntity> STORY_NPC = Registry.register(
            BuiltInRegistries.ENTITY_TYPE, CursedTides.id("story_npc"),
            EntityType.Builder.<StoryNpcEntity>of(StoryNpcEntity::new, MobCategory.MISC)
                    .sized(0.6f, 1.95f)
                    .eyeHeight(1.62f)
                    .clientTrackingRange(10)
                    .build("story_npc"));

    public static final EntityType<DrownedCaptainEntity> DROWNED_CAPTAIN = Registry.register(
            BuiltInRegistries.ENTITY_TYPE, CursedTides.id("drowned_captain"),
            EntityType.Builder.<DrownedCaptainEntity>of(DrownedCaptainEntity::new, MobCategory.MONSTER)
                    .sized(0.75f, 2.5f)
                    .eyeHeight(2.15f)
                    .clientTrackingRange(10)
                    .build("drowned_captain"));

    public static final EntityType<ShipEntity> SHIP = Registry.register(
            BuiltInRegistries.ENTITY_TYPE, CursedTides.id("ship"),
            EntityType.Builder.<ShipEntity>of(ShipEntity::new, MobCategory.MISC)
                    .sized(2.4f, 0.5f)
                    .clientTrackingRange(12)
                    .build("ship"));

    public static final EntityType<ShipwrightEntity> SHIPWRIGHT = Registry.register(
            BuiltInRegistries.ENTITY_TYPE, CursedTides.id("shipwright"),
            EntityType.Builder.<ShipwrightEntity>of(ShipwrightEntity::new, MobCategory.MISC)
                    .sized(0.6f, 1.95f)
                    .eyeHeight(1.62f)
                    .clientTrackingRange(10)
                    .build("shipwright"));

    public static final EntityType<MarineEntity> MARINE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE, CursedTides.id("marine"),
            EntityType.Builder.<MarineEntity>of(MarineEntity::new, MobCategory.MISC)
                    .sized(0.6f, 1.95f)
                    .eyeHeight(1.62f)
                    .clientTrackingRange(10)
                    .build("marine"));

    public static final EntityType<CrewmateEntity> CREWMATE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE, CursedTides.id("crewmate"),
            EntityType.Builder.<CrewmateEntity>of(CrewmateEntity::new, MobCategory.MISC)
                    .sized(0.6f, 1.95f)
                    .eyeHeight(1.62f)
                    .clientTrackingRange(10)
                    .build("crewmate"));

    public static final EntityType<ThrowingKnifeEntity> THROWING_KNIFE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE, CursedTides.id("throwing_knife"),
            EntityType.Builder.<ThrowingKnifeEntity>of(ThrowingKnifeEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("throwing_knife"));

    public static final EntityType<CannonballEntity> CANNONBALL = Registry.register(
            BuiltInRegistries.ENTITY_TYPE, CursedTides.id("cannonball"),
            EntityType.Builder.<CannonballEntity>of(CannonballEntity::new, MobCategory.MISC)
                    .sized(0.4f, 0.4f)
                    .clientTrackingRange(8)
                    .updateInterval(5)
                    .build("cannonball"));

    public static final EntityType<HexBoltEntity> HEX_BOLT = Registry.register(
            BuiltInRegistries.ENTITY_TYPE, CursedTides.id("hex_bolt"),
            EntityType.Builder.<HexBoltEntity>of(HexBoltEntity::new, MobCategory.MISC)
                    .sized(0.3f, 0.3f)
                    .clientTrackingRange(8)
                    .updateInterval(2)
                    .build("hex_bolt"));

    public static void register() {
        FabricDefaultAttributeRegistry.register(CURSED_PIRATE, CursedPirateEntity.createPirateAttributes());
        FabricDefaultAttributeRegistry.register(QUARTERMASTER, Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.5));
        FabricDefaultAttributeRegistry.register(STORY_NPC, Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.5));
        FabricDefaultAttributeRegistry.register(DROWNED_CAPTAIN, DrownedCaptainEntity.createCaptainAttributes());
        FabricDefaultAttributeRegistry.register(MARINE, MarineEntity.createMarineAttributes());
        FabricDefaultAttributeRegistry.register(CREWMATE, CrewmateEntity.createCrewAttributes());
        FabricDefaultAttributeRegistry.register(SHIPWRIGHT, Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.5));
    }

    /** Cursed crews wash up on beaches at night; quartermasters set up shop on some beaches. */
    public static void registerSpawns() {
        BiomeModifications.addSpawn(BiomeSelectors.tag(BiomeTags.IS_BEACH),
                MobCategory.MONSTER, CURSED_PIRATE, 60, 2, 4);
        BiomeModifications.addSpawn(BiomeSelectors.tag(BiomeTags.IS_BEACH),
                MobCategory.CREATURE, QUARTERMASTER, 4, 1, 1);
    }
}
