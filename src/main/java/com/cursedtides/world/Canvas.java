package com.cursedtides.world;

import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Writes blocks in absolute world coordinates but only inside one chunk. Every chunk
 * re-runs the same deterministic build code and keeps its own slice, so structures
 * that span chunks join up seamlessly.
 */
public final class Canvas {
    public interface ColumnVisitor {
        void visit(int x, int z);
    }

    public interface StateFn {
        BlockState at(int x, int y, int z);
    }

    public void fillFn(int x1, int y1, int z1, int x2, int y2, int z2, StateFn fn) {
        int ay = Math.min(y1, y2), by = Math.max(y1, y2);
        columns(x1, z1, x2, z2, (x, z) -> {
            for (int y = ay; y <= by; y++) {
                world.setBlock(pos.set(x, y, z), fn.at(x, y, z), 2);
            }
        });
    }

    private final WorldGenLevel world;
    public final int minX, minZ, maxX, maxZ;
    private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    public Canvas(WorldGenLevel world, ChunkPos chunk) {
        this.world = world;
        this.minX = chunk.getMinBlockX();
        this.minZ = chunk.getMinBlockZ();
        this.maxX = chunk.getMaxBlockX();
        this.maxZ = chunk.getMaxBlockZ();
    }

    public WorldGenLevel world() {
        return world;
    }

    public boolean contains(int x, int z) {
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    public boolean intersects(int x1, int z1, int x2, int z2) {
        return x2 >= minX && x1 <= maxX && z2 >= minZ && z1 <= maxZ;
    }

    public void set(int x, int y, int z, BlockState state) {
        if (contains(x, z)) {
            world.setBlock(pos.set(x, y, z), state, 2);
        }
    }

    public BlockState get(int x, int y, int z) {
        if (!contains(x, z)) {
            return Blocks.AIR.defaultBlockState();
        }
        return world.getBlockState(pos.set(x, y, z));
    }

    /** Visits only the columns of the rectangle that fall inside this chunk. */
    public void columns(int x1, int z1, int x2, int z2, ColumnVisitor visitor) {
        int ax = Math.max(Math.min(x1, x2), minX), bx = Math.min(Math.max(x1, x2), maxX);
        int az = Math.max(Math.min(z1, z2), minZ), bz = Math.min(Math.max(z1, z2), maxZ);
        for (int x = ax; x <= bx; x++) {
            for (int z = az; z <= bz; z++) {
                visitor.visit(x, z);
            }
        }
    }

    public void fill(int x1, int y1, int z1, int x2, int y2, int z2, BlockState state) {
        int ay = Math.min(y1, y2), by = Math.max(y1, y2);
        columns(x1, z1, x2, z2, (x, z) -> {
            for (int y = ay; y <= by; y++) {
                world.setBlock(pos.set(x, y, z), state, 2);
            }
        });
    }

    /** Four walls (no floor or ceiling). */
    public void walls(int x1, int y1, int z1, int x2, int y2, int z2, BlockState state) {
        fill(x1, y1, z1, x2, y2, z1, state);
        fill(x1, y1, z2, x2, y2, z2, state);
        fill(x1, y1, z1, x1, y2, z2, state);
        fill(x2, y1, z1, x2, y2, z2, state);
    }

    /**
     * Levels a building site: supports the footprint up to floorY-1 and cuts away
     * any hill that rises into it.
     */
    public void foundation(int x1, int z1, int x2, int z2, int floorY, BlockState support) {
        columns(x1, z1, x2, z2, (x, z) -> {
            int top = Archipelago.column(x, z).top();
            for (int y = Math.max(top + 1, Archipelago.SEA_FLOOR + 1); y <= floorY - 1; y++) {
                world.setBlock(pos.set(x, y, z), support, 2);
            }
            for (int y = floorY; y <= Math.max(top, floorY + 6); y++) {
                BlockState air = y <= Archipelago.WATER_TOP ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
                world.setBlock(pos.set(x, y, z), air, 2);
            }
        });
    }

    /** A chest holding specific items and/or a loot table. */
    public void chest(int x, int y, int z, Direction facing, ResourceKey<LootTable> loot, ItemStack... items) {
        if (!contains(x, z)) {
            return;
        }
        boolean wet = world.getBlockState(pos.set(x, y, z)).is(Blocks.WATER);
        BlockState state = Blocks.CHEST.defaultBlockState()
                .setValue(ChestBlock.FACING, facing)
                .setValue(ChestBlock.WATERLOGGED, wet);
        world.setBlock(pos.set(x, y, z), state, 2);
        if (world.getBlockEntity(pos.set(x, y, z)) instanceof ChestBlockEntity chest) {
            for (int i = 0; i < items.length; i++) {
                chest.setItem(13 + i - items.length / 2, items[i]);
            }
            if (loot != null) {
                chest.setLootTable(loot, Noise.hash(x, y, z, 99));
            }
        }
    }

    public void spawn(double x, int y, double z, float yaw, Function<ServerLevel, Entity> factory) {
        spawn(x, y, z, yaw, factory, null);
    }

    /** Spawns a persistent entity, but only from the chunk that contains it. afterInit runs after initialize(). */
    public void spawn(double x, int y, double z, float yaw, Function<ServerLevel, Entity> factory,
                      @org.jetbrains.annotations.Nullable java.util.function.Consumer<Entity> afterInit) {
        if (!contains((int) Math.floor(x), (int) Math.floor(z))) {
            return;
        }
        Entity entity = factory.apply(world.getLevel());
        if (entity == null) {
            return;
        }
        entity.snapTo(x, y, z, yaw, 0.0f);
        if (entity instanceof Mob mob) {
            mob.finalizeSpawn(world, world.getCurrentDifficultyAt(pos.set(x, y, z)), EntitySpawnReason.STRUCTURE, null);
            mob.setPersistenceRequired();
        }
        if (afterInit != null) {
            afterInit.accept(entity);
        }
        world.addFreshEntity(entity);
    }
}
