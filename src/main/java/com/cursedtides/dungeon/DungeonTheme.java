package com.cursedtides.dungeon;

import net.minecraft.world.entity.EntityTypes;
import com.cursedtides.entity.ModEntities;
import com.cursedtides.world.Noise;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** The look, hazards and inhabitants of a dungeon. */
public enum DungeonTheme {
    CRYPT(new Block[]{Blocks.STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS, Blocks.DEEPSLATE_BRICKS},
            new Block[]{Blocks.POLISHED_DEEPSLATE, Blocks.DEEPSLATE_TILES, Blocks.CRACKED_DEEPSLATE_TILES},
            Blocks.CHISELED_STONE_BRICKS, Blocks.SOUL_LANTERN, false,
            new EntityType<?>[]{null, EntityTypes.SKELETON, EntityTypes.ZOMBIE}, EntityTypes.WITHER_SKELETON, "Bone Warden"),

    SEA_CRYPT(new Block[]{Blocks.MOSSY_STONE_BRICKS, Blocks.PRISMARINE_BRICKS, Blocks.CRACKED_STONE_BRICKS, Blocks.MOSSY_COBBLESTONE},
            new Block[]{Blocks.SAND, Blocks.GRAVEL, Blocks.MOSSY_STONE_BRICKS},
            Blocks.SEA_LANTERN, Blocks.SEA_LANTERN, true,
            new EntityType<?>[]{EntityTypes.DROWNED, EntityTypes.DROWNED, EntityTypes.GUARDIAN}, EntityTypes.DROWNED, "Drowned Bellringer"),

    SEA_CAVE(new Block[]{Blocks.PRISMARINE, Blocks.DARK_PRISMARINE, Blocks.PRISMARINE_BRICKS, Blocks.TUBE_CORAL_BLOCK},
            new Block[]{Blocks.SAND, Blocks.SAND, Blocks.GRAVEL},
            Blocks.SEA_LANTERN, Blocks.SEA_LANTERN, true,
            new EntityType<?>[]{EntityTypes.DROWNED, EntityTypes.GUARDIAN, EntityTypes.DROWNED}, EntityTypes.DROWNED, "Tidecaller"),

    GROTTO(new Block[]{Blocks.STONE, Blocks.MOSSY_COBBLESTONE, Blocks.PRISMARINE, Blocks.TUFF},
            new Block[]{Blocks.SAND, Blocks.GRAVEL, Blocks.WATER},
            Blocks.SEA_LANTERN, Blocks.LANTERN, false,
            new EntityType<?>[]{EntityTypes.DROWNED, null, EntityTypes.SPIDER}, EntityTypes.DROWNED, "Grotto Keeper"),

    JUNGLE(new Block[]{Blocks.MOSSY_COBBLESTONE, Blocks.MOSSY_STONE_BRICKS, Blocks.CHISELED_STONE_BRICKS, Blocks.COBBLESTONE},
            new Block[]{Blocks.MOSSY_STONE_BRICKS, Blocks.MOSS_BLOCK, Blocks.COARSE_DIRT},
            Blocks.VERDANT_FROGLIGHT, Blocks.LANTERN, false,
            new EntityType<?>[]{EntityTypes.SPIDER, EntityTypes.CAVE_SPIDER, null}, EntityTypes.SKELETON, "Temple Guardian"),

    VOLCANIC(new Block[]{Blocks.BLACKSTONE, Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.BASALT, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS},
            new Block[]{Blocks.POLISHED_BLACKSTONE, Blocks.BLACKSTONE, Blocks.MAGMA_BLOCK},
            Blocks.SHROOMLIGHT, Blocks.LANTERN, false,
            new EntityType<?>[]{EntityTypes.MAGMA_CUBE, EntityTypes.BLAZE, null}, EntityTypes.BLAZE, "Cinder Wraith"),

    SMUGGLER(new Block[]{Blocks.STONE, Blocks.COBBLESTONE, Blocks.ANDESITE, Blocks.COBBLESTONE},
            new Block[]{Blocks.GRAVEL, Blocks.COARSE_DIRT, Blocks.SPRUCE_PLANKS},
            Blocks.SPRUCE_PLANKS, Blocks.LANTERN, false,
            new EntityType<?>[]{EntityTypes.PILLAGER, EntityTypes.VINDICATOR, null}, EntityTypes.VINDICATOR, "Smuggler Chief"),

    FORT(new Block[]{Blocks.STONE_BRICKS, Blocks.POLISHED_ANDESITE, Blocks.CRACKED_STONE_BRICKS, Blocks.ANDESITE},
            new Block[]{Blocks.STONE_BRICKS, Blocks.POLISHED_ANDESITE, Blocks.COBBLESTONE},
            Blocks.CHISELED_STONE_BRICKS, Blocks.LANTERN, false,
            new EntityType<?>[]{EntityTypes.SKELETON, EntityTypes.STRAY, null}, EntityTypes.STRAY, "Forgotten Gunner"),

    BAYOU(new Block[]{Blocks.MUD_BRICKS, Blocks.PACKED_MUD, Blocks.MANGROVE_PLANKS, Blocks.MUDDY_MANGROVE_ROOTS},
            new Block[]{Blocks.MUD, Blocks.MOSS_BLOCK, Blocks.MUD},
            Blocks.OCHRE_FROGLIGHT, Blocks.LANTERN, false,
            new EntityType<?>[]{EntityTypes.BOGGED, EntityTypes.SLIME, EntityTypes.SPIDER}, EntityTypes.BOGGED, "Bog Warden"),

    PRISON(new Block[]{Blocks.STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS, Blocks.COBBLESTONE, Blocks.POLISHED_ANDESITE},
            new Block[]{Blocks.COBBLESTONE, Blocks.GRAVEL, Blocks.STONE_BRICKS},
            Blocks.GLOWSTONE, Blocks.LANTERN, false,
            new EntityType<?>[]{EntityTypes.ZOMBIE, EntityTypes.PILLAGER, EntityTypes.SKELETON}, EntityTypes.VINDICATOR, "The Gaoler"),

    ANCIENT(new Block[]{Blocks.CUT_SANDSTONE, Blocks.CHISELED_SANDSTONE, Blocks.SMOOTH_SANDSTONE, Blocks.MOSSY_STONE_BRICKS},
            new Block[]{Blocks.SMOOTH_SANDSTONE, Blocks.CUT_SANDSTONE, Blocks.SAND},
            Blocks.SEA_LANTERN, Blocks.SOUL_LANTERN, false,
            new EntityType<?>[]{EntityTypes.HUSK, EntityTypes.SKELETON, EntityTypes.CAVE_SPIDER}, EntityTypes.WITHER_SKELETON, "Gilded Sentinel"),

    SPECTRAL(new Block[]{Blocks.CRYING_OBSIDIAN, Blocks.OBSIDIAN, Blocks.PURPUR_BLOCK, Blocks.DEEPSLATE_TILES},
            new Block[]{Blocks.SOUL_SAND, Blocks.SOUL_SOIL, Blocks.POLISHED_DEEPSLATE},
            Blocks.SOUL_LANTERN, Blocks.SOUL_LANTERN, false,
            new EntityType<?>[]{EntityTypes.VEX, EntityTypes.STRAY, EntityTypes.WITCH}, EntityTypes.EVOKER, "The Veil Keeper");

    private final Block[] walls;
    private final Block[] floors;
    private final Block ceilingLight;
    private final Block lantern;
    private final boolean flooded;
    private final EntityType<?>[] mobs;
    private final EntityType<?> bossType;
    private final String bossName;

    DungeonTheme(Block[] walls, Block[] floors, Block ceilingLight, Block lantern, boolean flooded,
                 EntityType<?>[] mobs, EntityType<?> bossType, String bossName) {
        this.walls = walls;
        this.floors = floors;
        this.ceilingLight = ceilingLight;
        this.lantern = lantern;
        this.flooded = flooded;
        this.mobs = mobs;
        this.bossType = bossType;
        this.bossName = bossName;
    }

    public BlockState wall(int x, int y, int z) {
        return walls[(int) (Noise.rand(x, y, z, 301) * walls.length)].defaultBlockState();
    }

    public BlockState floor(int x, int y, int z) {
        double r = Noise.rand(x, y, z, 302);
        // The last floor block is the rare accent (magma, planks...)
        int i = r < 0.85 ? (int) (r / 0.85 * (floors.length - 1)) : floors.length - 1;
        return floors[i].defaultBlockState();
    }

    public BlockState ceilingLight() { return ceilingLight.defaultBlockState(); }
    public Block lantern() { return lantern; }
    public boolean flooded() { return flooded; }
    public EntityType<?> bossType() { return bossType; }
    public String bossName() { return bossName; }

    /** A mob for a spawner; a null slot means the mod's own Cursed Pirate. */
    public EntityType<?> mob(int index) {
        EntityType<?> type = mobs[Math.floorMod(index, mobs.length)];
        return type == null ? ModEntities.CURSED_PIRATE : type;
    }
}
