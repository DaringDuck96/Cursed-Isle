package com.cursedtides.dungeon;

import com.cursedtides.CursedTides;
import com.cursedtides.block.ModBlocks;
import com.cursedtides.item.ModItems;
import com.cursedtides.world.Archipelago;
import com.cursedtides.world.Canvas;
import com.cursedtides.world.Noise;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.LanternBlock;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * One dungeon: a ladder shaft from a small surface entrance down to a web of rooms carved
 * deep in the bedrock stone. Rooms are laid out on a grid by a seeded random walk, so the
 * layout is fixed for every world. There's always a key room with a mini-boss and a vault
 * behind a Sealed Gate. Everything is placed chunk by chunk through Canvas.
 */
public final class Dungeon {
    static final int CELL = 12;
    static final int ROOM_H = 5;

    public enum Kind { START, COMBAT, TREASURE, TRAP, KEY, VAULT, PUZZLE }

    record Room(int cx, int cz, int hs, Kind kind) {
        int x1() { return cx - hs; }
        int x2() { return cx + hs; }
        int z1() { return cz - hs; }
        int z2() { return cz + hs; }
    }

    /** A straight 3-wide passage; the centre line runs from (x1,z1) to (x2,z2). */
    record Corridor(int x1, int z1, int x2, int z2, boolean alongX, boolean trapped, int gateX, int gateZ) {
        boolean sealed() { return gateX != Integer.MIN_VALUE; }
    }

    static final RegistryKey<LootTable> LOOT = RegistryKey.of(RegistryKeys.LOOT_TABLE, CursedTides.id("chests/dungeon"));
    static final RegistryKey<LootTable> VAULT_LOOT = RegistryKey.of(RegistryKeys.LOOT_TABLE, CursedTides.id("chests/dungeon_vault"));

    public final int index;
    public final String name;
    public final int ex, ez;
    public final DungeonTheme theme;
    public final int tier;
    public final int floorY;
    public final int surfaceTop;
    final List<Room> rooms = new ArrayList<>();
    final List<Corridor> corridors = new ArrayList<>();
    public int minX, minZ, maxX, maxZ;

    Dungeon(int index, String name, int ex, int ez, DungeonTheme theme, int roomCount, int level, int tier) {
        this.index = index;
        this.name = name;
        this.ex = ex;
        this.ez = ez;
        this.theme = theme;
        this.tier = tier;
        this.floorY = 24 - 9 * level;
        this.surfaceTop = Archipelago.column(ex, ez).top();
        layout(roomCount, Noise.hash(ex, index, ez, 5150));
    }

    // ------------------------------------------------------------------ layout

    private void layout(int roomCount, long seed) {
        List<int[]> cells = new ArrayList<>();
        Map<Long, Integer> taken = new HashMap<>();
        List<int[]> links = new ArrayList<>(); // {fromIndex, toIndex}
        cells.add(new int[]{0, 0});
        taken.put(key(0, 0), 0);
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        int guard = 0;
        while (cells.size() < roomCount + 1 && guard++ < 400) {
            double pick = Noise.rand(guard, cells.size(), 7, seed);
            int baseIndex = pick < 0.55 ? cells.size() - 1 : (int) (Noise.rand(guard, 3, 9, seed) * cells.size());
            int[] base = cells.get(baseIndex);
            int start = (int) (Noise.rand(guard, 5, 11, seed) * 4);
            for (int k = 0; k < 4; k++) {
                int[] d = dirs[(start + k) % 4];
                int nx = base[0] + d[0], nz = base[1] + d[1];
                if (Math.abs(nx) > 4 || Math.abs(nz) > 4 || taken.containsKey(key(nx, nz))) continue;
                taken.put(key(nx, nz), cells.size());
                links.add(new int[]{baseIndex, cells.size()});
                cells.add(new int[]{nx, nz});
                break;
            }
        }

        // The last cell is the vault; the one it hangs off becomes the key room.
        int vaultIndex = cells.size() - 1;
        int keyIndex = links.isEmpty() ? 0 : links.get(links.size() - 1)[0];
        if (keyIndex == 0 && cells.size() > 2) {
            keyIndex = cells.size() - 2;
        }

        Kind[] kinds = new Kind[cells.size()];
        for (int i = 0; i < cells.size(); i++) {
            if (i == 0) kinds[i] = Kind.START;
            else if (i == vaultIndex) kinds[i] = Kind.VAULT;
            else if (i == keyIndex) kinds[i] = Kind.KEY;
            else {
                double r = Noise.rand(i, 13, 17, seed);
                kinds[i] = r < 0.25 ? Kind.TREASURE : r < 0.36 ? Kind.PUZZLE : r < 0.5 ? Kind.TRAP : Kind.COMBAT;
            }
        }
        for (int i = 0; i < cells.size(); i++) {
            int hs = switch (kinds[i]) {
                case START -> 3;
                case KEY -> 4;
                case VAULT -> 3;
                default -> 2 + (int) (Noise.rand(i, 19, 23, seed) * 3);
            };
            rooms.add(new Room(ex + cells.get(i)[0] * CELL, ez + cells.get(i)[1] * CELL, hs, kinds[i]));
        }

        for (int[] link : links) {
            Room a = rooms.get(link[0]);
            Room b = rooms.get(link[1]);
            boolean alongX = a.cz() == b.cz();
            boolean trapped = b.kind() == Kind.TRAP || Noise.rand(link[0], link[1], 29, seed) < 0.2;
            int x1, z1, x2, z2;
            if (alongX) {
                Room lo = a.cx() < b.cx() ? a : b, hi = lo == a ? b : a;
                x1 = lo.x2() + 1; x2 = hi.x1() - 1; z1 = z2 = a.cz();
            } else {
                Room lo = a.cz() < b.cz() ? a : b, hi = lo == a ? b : a;
                z1 = lo.z2() + 1; z2 = hi.z1() - 1; x1 = x2 = a.cx();
            }
            int gateX = Integer.MIN_VALUE, gateZ = Integer.MIN_VALUE;
            if (b.kind() == Kind.VAULT) {
                trapped = false;
                if (alongX) { gateX = b.cx() > a.cx() ? x2 : x1; gateZ = z1; }
                else { gateZ = b.cz() > a.cz() ? z2 : z1; gateX = x1; }
            }
            corridors.add(new Corridor(x1, z1, x2, z2, alongX, trapped, gateX, gateZ));
        }

        minX = ex - 3; maxX = ex + 3; minZ = ez - 3; maxZ = ez + 3;
        for (Room r : rooms) {
            minX = Math.min(minX, r.x1() - 1); maxX = Math.max(maxX, r.x2() + 1);
            minZ = Math.min(minZ, r.z1() - 1); maxZ = Math.max(maxZ, r.z2() + 1);
        }
    }

    private static long key(int x, int z) {
        return ((long) x << 32) ^ (z & 0xFFFFFFFFL);
    }

    public boolean contains(BlockPos pos) {
        return pos.getX() >= minX && pos.getX() <= maxX && pos.getZ() >= minZ && pos.getZ() <= maxZ
                && pos.getY() >= floorY - 2 && pos.getY() <= floorY + ROOM_H + 2;
    }

    // ------------------------------------------------------------------ placement

    public void place(Canvas c) {
        if (!c.intersects(minX, minZ, maxX, maxZ)) {
            return;
        }
        BlockState open = theme.flooded() ? Blocks.WATER.getDefaultState() : Blocks.AIR.getDefaultState();
        int top = floorY + ROOM_H;

        // 1. Shells: everything solid theme wall first
        for (Room r : rooms) {
            c.fillFn(r.x1() - 1, floorY - 1, r.z1() - 1, r.x2() + 1, top, r.z2() + 1, theme::wall);
        }
        for (Corridor k : corridors) {
            c.fillFn(cx1(k) - 1, floorY - 1, cz1(k) - 1, cx2(k) + 1, floorY + 3, cz2(k) + 1, theme::wall);
        }
        c.fillFn(ex - 1, top, ez - 1, ex + 1, surfaceTop, ez + 1, theme::wall);

        // 2. Interiors
        for (Room r : rooms) {
            c.fill(r.x1(), floorY, r.z1(), r.x2(), top - 1, r.z2(), open);
            c.fillFn(r.x1(), floorY - 1, r.z1(), r.x2(), floorY - 1, r.z2(), theme::floor);
        }
        for (Corridor k : corridors) {
            c.fill(cx1(k), floorY, cz1(k), cx2(k), floorY + 2, cz2(k), open);
            c.fillFn(cx1(k), floorY - 1, cz1(k), cx2(k), floorY - 1, cz2(k), theme::floor);
        }

        // 3. Entrance hut, then the shaft: one ladder column, backed by a pillar, from the start room up
        entrance(c);
        BlockState ladder = Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, Direction.SOUTH)
                .with(Properties.WATERLOGGED, theme.flooded());
        for (int y = floorY; y <= surfaceTop + 1; y++) {
            c.set(ex, y, ez, ladder);
            c.set(ex, y, ez - 1, theme.wall(ex, y, ez - 1));
        }

        // 4. Room contents
        for (int i = 0; i < rooms.size(); i++) {
            furnish(c, rooms.get(i), i);
        }
        for (Corridor k : corridors) {
            if (k.trapped()) arrowTrap(c, k);
            if (k.sealed()) gate(c, k);
        }
    }

    private int cx1(Corridor k) { return k.alongX() ? Math.min(k.x1(), k.x2()) : k.x1() - 1; }
    private int cx2(Corridor k) { return k.alongX() ? Math.max(k.x1(), k.x2()) : k.x1() + 1; }
    private int cz1(Corridor k) { return k.alongX() ? k.z1() - 1 : Math.min(k.z1(), k.z2()); }
    private int cz2(Corridor k) { return k.alongX() ? k.z1() + 1 : Math.max(k.z1(), k.z2()); }

    /** A small ruined hut over the shaft so you can spot the way in. */
    private void entrance(Canvas c) {
        int y = surfaceTop + 1;
        c.fillFn(ex - 2, surfaceTop, ez - 2, ex + 2, surfaceTop, ez + 2, theme::wall);
        for (int x = ex - 2; x <= ex + 2; x++) {
            for (int z = ez - 2; z <= ez + 2; z++) {
                boolean ring = Math.abs(x - ex) == 2 || Math.abs(z - ez) == 2;
                for (int yy = y; yy <= y + 3; yy++) {
                    if (!ring) {
                        if (!(x == ex && z == ez) && !(x == ex && z == ez - 1)) {
                            c.set(x, yy, z, yy <= Archipelago.WATER_TOP ? Blocks.WATER.getDefaultState() : Blocks.AIR.getDefaultState());
                        }
                    } else if (yy <= y + 2 && Noise.rand(x, yy, z, 311) < 0.8) {
                        c.set(x, yy, z, theme.wall(x, yy, z));
                    }
                }
            }
        }
        c.set(ex, y + 2, ez + 2, Blocks.AIR.getDefaultState()); // doorway
        c.set(ex, y + 1, ez + 2, Blocks.AIR.getDefaultState());
        c.set(ex, y, ez + 2, Blocks.AIR.getDefaultState());
        c.fillFn(ex - 2, y + 3, ez - 2, ex + 2, y + 3, ez + 2, theme::wall);
        c.set(ex, y + 4, ez, theme.lantern().getDefaultState());
        c.set(ex + 1, surfaceTop, ez + 1, theme.ceilingLight());
    }

    private void furnish(Canvas c, Room r, int i) {
        int y = floorY;
        int top = floorY + ROOM_H;
        if (r.kind() != Kind.START) {
            c.set(r.cx(), top, r.cz(), theme.ceilingLight()); // (the start room's ceiling is the shaft)
        }
        if (!theme.flooded()) {
            c.set(r.x1(), top - 1, r.z1(), theme.lantern().getDefaultState().with(LanternBlock.HANGING, true));
            c.set(r.x2(), top - 1, r.z2(), theme.lantern().getDefaultState().with(LanternBlock.HANGING, true));
        }
        switch (r.kind()) {
            case START -> { }
            case COMBAT -> {
                spawner(c, r.cx(), y, r.cz(), theme.mob(i));
                guard(c, r.x1() + 0.5, y, r.z1() + 0.5, theme.mob(i + 1));
            }
            case TREASURE -> {
                c.chest(r.x2(), y, r.z2(), Direction.NORTH, LOOT);
                guard(c, r.cx() + 0.5, y, r.cz() + 0.5, theme.mob(i));
            }
            case TRAP -> hazards(c, r, i);
            case PUZZLE -> puzzle(c, r, i);
            case KEY -> {
                c.chest(r.x2(), y, r.z1(), Direction.WEST, LOOT, new ItemStack(ModItems.RUSTED_KEY));
                miniBoss(c, r);
                spawner(c, r.x1(), y, r.z2(), theme.mob(i));
            }
            case VAULT -> {
                c.chest(r.x1(), y, r.z1(), Direction.SOUTH, VAULT_LOOT);
                c.chest(r.x2(), y, r.z1(), Direction.SOUTH, VAULT_LOOT);
                if (tier >= 2) {
                    c.chest(r.cx(), y, r.z2(), Direction.NORTH, VAULT_LOOT, new ItemStack(ModItems.MAP_FRAGMENT));
                }
                c.set(r.x1(), y, r.z2(), Blocks.GOLD_BLOCK.getDefaultState());
                c.set(r.x2(), y, r.z2(), Blocks.GOLD_BLOCK.getDefaultState());
            }
        }
    }

    /** A glyph altar, three turning stones and a treasure chest walled in by rune seals. */
    private void puzzle(Canvas c, Room r, int i) {
        int type = (int) (Noise.rand(i, index, 47, ex * 13L + ez) * 3);
        if (type == 1) { chimePuzzle(c, r, i); return; }
        if (type == 2) { moonPuzzle(c, r, i); return; }
        int y = floorY;
        int answer = (int) (Noise.rand(i, index, 41, ex * 31L + ez) * 4);
        c.set(r.cx(), y, r.z1(), ModBlocks.GLYPH_ALTAR.getDefaultState().with(com.cursedtides.block.GlyphBlock.GLYPH, answer));
        int[][] spots = {{r.x1(), r.cz()}, {r.x2(), r.cz()}, {r.cx(), r.z2()}};
        for (int k = 0; k < spots.length; k++) {
            int start = (answer + 1 + (int) (Noise.rand(k, i, 43, ex * 17L + ez) * 3)) % 4; // never already solved
            c.set(spots[k][0], y, spots[k][1], ModBlocks.GLYPH_STONE.getDefaultState().with(com.cursedtides.block.GlyphBlock.GLYPH, start));
        }
        int chx = r.cx(), chz = r.cz();
        c.chest(chx, y, chz, Direction.SOUTH, LOOT, new ItemStack(Items.GOLD_INGOT, 3));
        BlockState seal = ModBlocks.RUNE_SEAL.getDefaultState();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) c.set(chx, y + 1, chz, seal);
                else c.fill(chx + dx, y, chz + dz, chx + dx, y + 1, chz + dz, seal);
            }
        }
    }

    /** A song altar and four chime stones; play the altar's tune to break the seals around the chest. */
    private void chimePuzzle(Canvas c, Room r, int i) {
        int y = floorY;
        c.set(r.cx(), y, r.z1(), ModBlocks.SONG_ALTAR.getDefaultState());
        int[][] spots = {{r.x1(), r.cz() - 1}, {r.x1(), r.cz() + 1}, {r.x2(), r.cz() - 1}, {r.x2(), r.cz() + 1}};
        for (int k = 0; k < 4; k++) {
            c.set(spots[k][0], y, spots[k][1], ModBlocks.CHIME_STONE.getDefaultState().with(com.cursedtides.block.PuzzleBlocks.ChimeStoneBlock.NOTE, k));
        }
        sealedChest(c, r, ModBlocks.RUNE_SEAL.getDefaultState());
    }

    /** A chest behind moon seals: they only open at night. */
    private void moonPuzzle(Canvas c, Room r, int i) {
        sealedChest(c, r, ModBlocks.MOON_SEAL.getDefaultState());
        guard(c, r.x1() + 0.5, floorY, r.z1() + 0.5, theme.mob(i));
    }

    private void sealedChest(Canvas c, Room r, BlockState seal) {
        int y = floorY, chx = r.cx(), chz = r.cz();
        c.chest(chx, y, chz, Direction.SOUTH, LOOT, new ItemStack(Items.GOLD_INGOT, 3), new ItemStack(com.cursedtides.explore.Cartography.ROUGH_GEM, 2));
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) c.set(chx, y + 1, chz, seal);
                else c.fill(chx + dx, y, chz + dz, chx + dx, y + 1, chz + dz, seal);
            }
        }
    }

    /** Trap rooms get the theme's hazard and a spawner. */
    private void hazards(Canvas c, Room r, int i) {
        int y = floorY;
        c.columns(r.x1(), r.z1(), r.x2(), r.z2(), (x, z) -> {
            double n = Noise.rand(x, z, index * 7 + 331);
            switch (theme) {
                case VOLCANIC -> {
                    if (z == r.cz() && x != r.x1() && x != r.x2()) c.set(x, y - 1, z, Blocks.LAVA.getDefaultState());
                    else if (n < 0.3) c.set(x, y - 1, z, Blocks.MAGMA_BLOCK.getDefaultState());
                }
                case CRYPT, FORT, JUNGLE -> { if (n < 0.18) c.set(x, y + (n < 0.09 ? 0 : 3), z, Blocks.COBWEB.getDefaultState()); }
                case BAYOU -> {
                    if (n < 0.35) c.set(x, y - 1, z, Blocks.WATER.getDefaultState());
                    else if (n < 0.5) c.set(x, y, z, Blocks.COBWEB.getDefaultState());
                }
                case SMUGGLER -> { if (n < 0.08) c.set(x, y, z, Blocks.TNT.getDefaultState()); }
                default -> { if (n < 0.2) c.set(x, y - 1, z, Blocks.MAGMA_BLOCK.getDefaultState()); }
            }
        });
        spawner(c, r.cx(), y, r.cz(), theme.mob(i + 2));
    }

    private void spawner(Canvas c, int x, int y, int z, EntityType<?> type) {
        if (!c.contains(x, z)) return;
        c.set(x, y, z, Blocks.SPAWNER.getDefaultState());
        StructureWorldAccess world = c.world();
        if (world.getBlockEntity(new BlockPos(x, y, z)) instanceof MobSpawnerBlockEntity spawner) {
            spawner.setEntityType(type, Random.create(Noise.hash(x, y, z, 401)));
        }
    }

    private void guard(Canvas c, double x, int y, double z, EntityType<?> type) {
        c.spawn(x, y, z, 0, w -> type.create(w));
    }

    /** A plate in the floor and a dispenser in the wall beside it. */
    private void arrowTrap(Canvas c, Corridor k) {
        int mx = (k.x1() + k.x2()) / 2, mz = (k.z1() + k.z2()) / 2;
        int px = k.alongX() ? mx : mx + 1;
        int pz = k.alongX() ? mz + 1 : mz;
        int dx = k.alongX() ? mx : mx + 2;
        int dz = k.alongX() ? mz + 2 : mz;
        Direction facing = k.alongX() ? Direction.NORTH : Direction.WEST;
        if (theme.flooded()) return; // no plates under water
        c.set(px, floorY, pz, Blocks.STONE_PRESSURE_PLATE.getDefaultState());
        c.set(dx, floorY, dz, Blocks.DISPENSER.getDefaultState().with(DispenserBlock.FACING, facing));
        if (c.contains(dx, dz) && c.world().getBlockEntity(new BlockPos(dx, floorY, dz)) instanceof DispenserBlockEntity d) {
            d.setStack(0, new ItemStack(Items.ARROW, 24));
        }
    }

    /** Fill the corridor's cross-section next to the vault with Sealed Gate. */
    private void gate(Canvas c, Corridor k) {
        BlockState gate = ModBlocks.SEALED_GATE.getDefaultState();
        if (k.alongX()) {
            c.fill(k.gateX(), floorY, k.gateZ() - 1, k.gateX(), floorY + 2, k.gateZ() + 1, gate);
        } else {
            c.fill(k.gateX() - 1, floorY, k.gateZ(), k.gateX() + 1, floorY + 2, k.gateZ(), gate);
        }
    }

    private void miniBoss(Canvas c, Room r) {
        float health = 50 + tier * 30;
        c.spawn(r.cx() + 0.5, floorY, r.cz() + 0.5, 0, w -> theme.bossType().create(w), entity -> {
            if (!(entity instanceof MobEntity boss)) return;
            boss.setCustomName(Text.literal(theme.bossName()).formatted(Formatting.GOLD));
            boss.setCustomNameVisible(true);
            var maxHealth = boss.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.setBaseValue(health);
                boss.setHealth(health);
            }
            var armor = boss.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);
            if (armor != null) {
                armor.setBaseValue(4 + tier * 2);
            }
            boss.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
            boss.setEquipmentDropChance(EquipmentSlot.HEAD, 0.0f);
        });
    }
}
