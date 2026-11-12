package com.cursedtides.world;

import com.cursedtides.CursedTides;
import com.cursedtides.entity.ModEntities;
import com.cursedtides.entity.StoryNpcEntity;
import com.cursedtides.item.ModItems;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.LanternBlock;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.enums.Attachment;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Every prebuilt structure in the Shattered Reach, at fixed coordinates. */
public final class Builds {
    private Builds() {}

    public record Build(int x1, int z1, int x2, int z2, boolean clearsPlants, Consumer<Canvas> placer) {
        boolean intersects(Canvas c) {
            return c.intersects(x1, z1, x2, z2);
        }
    }

    public static final List<Build> ALL = new ArrayList<>();

    static final RegistryKey<LootTable> TREASURE = RegistryKey.of(RegistryKeys.LOOT_TABLE, CursedTides.id("chests/island_treasure"));
    static final RegistryKey<LootTable> TAVERN = RegistryKey.of(RegistryKeys.LOOT_TABLE, CursedTides.id("chests/tavern_stores"));

    static BlockState s(Block b) {
        return b.getDefaultState();
    }

    static void add(int x1, int z1, int x2, int z2, Consumer<Canvas> placer) {
        ALL.add(new Build(x1, z1, x2, z2, true, placer));
    }

    static {
        // ===== Driftwood Island: the castaway start =====
        add(3, -17, 15, -5, c -> lighthouse(c, 9, -11));
        add(-14, 1, -2, 10, c -> shack(c));
        add(-3, 16, 5, 50, c -> {
            pier(c, 0, 16, 48, 1);
            c.spawn(3.5, 63, 44.5, 0, w -> new BoatEntity(w, 0, 0, 0));
        });
        add(-26, 10, -16, 22, c -> wreckedRaft(c, -21, 16));

        // ===== Saltmarrow: the freeport =====
        int sx = Archipelago.SALTMARROW.cx(), sz = Archipelago.SALTMARROW.cz();
        add(sx - 42, sz - 42, sx + 42, sz + 14, c -> plazaAndRoads(c, sx, sz, "Quartermaster Brack", "Trader Oona"));
        add(sx + 11, sz - 7, sx + 25, sz + 5, c -> tavern(c, sx + 12, sz - 6, sx + 24, sz + 4));
        add(sx - 6, sz + 14, sx + 6, sz + 26, c -> harborHouse(c, sx, sz + 20));
        add(sx - 4, sz + 25, sx + 8, sz + 92, c -> {
            pier(c, sx, sz + 25, sz + 90, 2);
            c.spawn(sx + 4.5, 63, sz + 82.5, 0, w -> new BoatEntity(w, 0, 0, 0));
            c.spawn(sx - 3.5, 63, sz + 70.5, 90, w -> new BoatEntity(w, 0, 0, 0));
        });
        int[][] houses = {{-24, -14}, {-24, 8}, {-8, -26}, {10, -26}, {30, -18}, {16, 18}, {-34, -2}};
        for (int[] h : houses) {
            int hx = sx + h[0], hz = sz + h[1];
            add(hx - 4, hz - 4, hx + 4, hz + 4, c -> house(c, hx, hz, sx, sz));
        }
        add(sx - 30, sz + 14, sx - 16, sz + 26, c -> shipyard(c, sx - 23, sz + 20));

        // ===== The Sunken Reach: underwater ruins =====
        int rx = Archipelago.SUNKEN_REACH.cx(), rz = Archipelago.SUNKEN_REACH.cz();
        add(rx - 7, rz - 7, rx + 7, rz + 7, c -> sunkenBellTower(c, rx, rz));
        add(rx + 14, rz + 10, rx + 30, rz + 22, c -> shipwreck(c, rx + 15, rz + 16));

        // ===== Ashfall Island: volcano, smugglers' forge, bellfounder =====
        int ax = Archipelago.ASHFALL_ISLAND.cx(), az = Archipelago.ASHFALL_ISLAND.cz();
        add(ax - 42, az - 6, ax - 28, az + 6, c -> forge(c, ax - 41, az - 5, ax - 29, az + 5));
        add(ax + 32, az - 4, ax + 44, az + 4, c -> bellfounderHut(c, ax + 35, az - 3, ax + 41, az + 3));

        // ===== Ironwatch Isle: the Tidewatch fortress =====
        int ix = Archipelago.IRONWATCH_ISLE.cx(), iz = Archipelago.IRONWATCH_ISLE.cz();
        add(ix - 21, iz - 21, ix + 21, iz + 21, c -> fortress(c, ix, iz));
        add(ix - 3, iz + 17, ix + 3, iz + 78, c -> pier(c, ix, iz + 22, iz + 76, 2));

        // ===== Mirefen Bayou: Hookline Landing, a stilt village =====
        int mx = Archipelago.MIREFEN_BAYOU.cx(), mz = Archipelago.MIREFEN_BAYOU.cz();
        add(mx - 20, mz - 20, mx + 20, mz + 20, c -> hooklineLanding(c, mx, mz));
        add(mx - 2, mz + 20, mx + 2, mz + 78, c -> pier(c, mx, mz + 20, mz + 76, 1));

        // ===== Port Meridian: the colonial port =====
        int px = Archipelago.PORT_MERIDIAN.cx(), pz = Archipelago.PORT_MERIDIAN.cz();
        add(px - 42, pz - 42, px + 42, pz + 14, c -> plazaAndRoads(c, px, pz, "Merchant Pell Abernathy", "Chandler Rosalind Wick"));
        add(px + 13, pz - 7, px + 27, pz + 7, c -> mansion(c, px + 14, pz - 6, px + 26, pz + 6));
        add(px - 27, pz - 7, px - 15, pz + 7, c -> chapel(c, px - 26, pz - 6, px - 16, pz + 6));
        int[][] meridianHouses = {{-12, -22}, {10, -22}, {-12, 18}, {10, 18}, {26, 20}};
        for (int[] h : meridianHouses) {
            int hx = px + h[0], hz = pz + h[1];
            add(hx - 4, hz - 4, hx + 4, hz + 4, c -> house(c, hx, hz, px, pz));
        }
        add(px - 3, pz + 27, px + 3, pz + 92, c -> pier(c, px, pz + 28, pz + 90, 2));
        add(px - 6, pz - 6, px + 6, pz + 6, c -> {
            int y = Archipelago.column(px, pz).top() + 1;
            c.spawn(px - 5.5, y, pz + 6.5, 0, w -> ModEntities.MARINE.create(w));
            c.spawn(px + 6.5, y, pz - 5.5, 0, w -> ModEntities.MARINE.create(w));
        });

        // ===== Blackthorn Isle: the Brethren's hideout =====
        int bx = Archipelago.BLACKTHORN_ISLE.cx(), bz = Archipelago.BLACKTHORN_ISLE.cz();
        add(bx - 15, bz - 15, bx + 15, bz + 15, c -> blackthornCamp(c, bx, bz));

        // ===== Emerald Cay: jungle and the old stepped temple =====
        int ex = Archipelago.EMERALD_CAY.cx(), ez = Archipelago.EMERALD_CAY.cz();
        add(ex - 10, ez - 10, ex + 10, ez + 10, c -> steppedTemple(c, ex, ez));
        add(ex + 26, ez - 4, ex + 36, ez + 4, c -> naturalistCamp(c, ex + 30, ez));

        // ===== Whispering Key: the haunted mansion =====
        int wx = Archipelago.WHISPERING_KEY.cx(), wz = Archipelago.WHISPERING_KEY.cz();
        add(wx - 8, wz - 7, wx + 8, wz + 7, c -> hauntedMansion(c, wx - 7, wz - 6, wx + 7, wz + 6));
        add(wx + 13, wz + 3, wx + 25, wz + 17, c -> cemetery(c, wx + 14, wz + 4, wx + 24, wz + 16));
        add(wx - 20, wz + 5, wx - 10, wz + 15, c -> mediumHut(c, wx - 19, wz + 7, wx - 13, wz + 13));

        // ===== Goldwater Cay: the mining camp =====
        int gx = Archipelago.GOLDWATER_CAY.cx(), gz = Archipelago.GOLDWATER_CAY.cz();
        add(gx - 12, gz - 12, gx + 12, gz + 12, c -> miningCamp(c, gx, gz));

        // ===== The outer reaches (0.9) =====
        OuterBuilds.addAll();
        SunkenCity.addAll();

        // ===== Dungeons: underground layouts, plus a plant-free plot around each entrance =====
        for (com.cursedtides.dungeon.Dungeon d : com.cursedtides.dungeon.Dungeons.ALL) {
            ALL.add(new Build(d.minX, d.minZ, d.maxX, d.maxZ, false, d::place));
            ALL.add(new Build(d.ex - 3, d.ez - 3, d.ex + 3, d.ez + 3, true, c -> { }));
        }

        // ===== Hollowgrave Island: cemetery and temple =====
        add(Archipelago.ALTAR_X - 12, Archipelago.ALTAR_Z - 12, Archipelago.ALTAR_X + 12, Archipelago.ALTAR_Z + 12,
                c -> temple(c, Archipelago.ALTAR_X, Archipelago.ALTAR_Z));
        add(Archipelago.ALTAR_X + 14, Archipelago.ALTAR_Z - 8, Archipelago.ALTAR_X + 26, Archipelago.ALTAR_Z + 8,
                c -> cemetery(c, Archipelago.ALTAR_X + 15, Archipelago.ALTAR_Z - 7, Archipelago.ALTAR_X + 25, Archipelago.ALTAR_Z + 7));
    }

    /** Where each named character stands, so lost NPCs can be put back (/cursedtides restore_npcs). */
    public record NpcSpot(StoryNpcEntity.Role role, int floorX, int floorZ, double x, double z, float yaw) {
        public int y() {
            return floorAt(floorX, floorZ);
        }
    }

    public static final List<NpcSpot> NPC_SPOTS = new ArrayList<>(List.of(
            new NpcSpot(StoryNpcEntity.Role.KEEPER, -4, 5, -3.5, 5.5, 90),
            new NpcSpot(StoryNpcEntity.Role.HARBORMASTER, Archipelago.SALTMARROW.cx(), Archipelago.SALTMARROW.cz() + 20,
                    Archipelago.SALTMARROW.cx() + 1.5, Archipelago.SALTMARROW.cz() + 20.5, 180),
            new NpcSpot(StoryNpcEntity.Role.BROKER, Archipelago.SALTMARROW.cx() + 18, Archipelago.SALTMARROW.cz() - 1,
                    Archipelago.SALTMARROW.cx() + 19.5, Archipelago.SALTMARROW.cz() + 2.5, 270),
            new NpcSpot(StoryNpcEntity.Role.BELLWRIGHT, Archipelago.ASHFALL_ISLAND.cx() + 43, Archipelago.ASHFALL_ISLAND.cz(),
                    Archipelago.ASHFALL_ISLAND.cx() + 43.5, Archipelago.ASHFALL_ISLAND.cz() + 0.5, 90),
            new NpcSpot(StoryNpcEntity.Role.COMMODORE, Archipelago.IRONWATCH_ISLE.cx(), Archipelago.IRONWATCH_ISLE.cz(),
                    Archipelago.IRONWATCH_ISLE.cx() + 8.5, Archipelago.IRONWATCH_ISLE.cz() + 1.5, 90),
            new NpcSpot(StoryNpcEntity.Role.GAMBLER, Archipelago.SALTMARROW.cx() + 18, Archipelago.SALTMARROW.cz() - 1,
                    Archipelago.SALTMARROW.cx() + 16.5, Archipelago.SALTMARROW.cz() - 3.5, 180),
            new NpcSpot(StoryNpcEntity.Role.DEALER, Archipelago.SALTMARROW.cx() + 18, Archipelago.SALTMARROW.cz() - 1,
                    Archipelago.SALTMARROW.cx() + 20.5, Archipelago.SALTMARROW.cz() - 1.5, 90),
            new NpcSpot(StoryNpcEntity.Role.DEALER, Archipelago.BLACKTHORN_ISLE.cx(), Archipelago.BLACKTHORN_ISLE.cz() + 3,
                    Archipelago.BLACKTHORN_ISLE.cx() + 0.5, Archipelago.BLACKTHORN_ISLE.cz() + 3.5, 180),
            new NpcSpot(StoryNpcEntity.Role.GOVERNOR, Archipelago.PORT_MERIDIAN.cx() + 20, Archipelago.PORT_MERIDIAN.cz(),
                    Archipelago.PORT_MERIDIAN.cx() + 22.5, Archipelago.PORT_MERIDIAN.cz() + 0.5, 90),
            new NpcSpot(StoryNpcEntity.Role.ROOK, Archipelago.BLACKTHORN_ISLE.cx() + 3, Archipelago.BLACKTHORN_ISLE.cz(),
                    Archipelago.BLACKTHORN_ISLE.cx() + 3.5, Archipelago.BLACKTHORN_ISLE.cz() + 0.5, 90),
            new NpcSpot(StoryNpcEntity.Role.NATURALIST, Archipelago.EMERALD_CAY.cx() + 32, Archipelago.EMERALD_CAY.cz(),
                    Archipelago.EMERALD_CAY.cx() + 32.5, Archipelago.EMERALD_CAY.cz() + 0.5, 90),
            new NpcSpot(StoryNpcEntity.Role.MEDIUM, Archipelago.WHISPERING_KEY.cx() - 12, Archipelago.WHISPERING_KEY.cz() + 10,
                    Archipelago.WHISPERING_KEY.cx() - 11.5, Archipelago.WHISPERING_KEY.cz() + 10.5, 270),
            new NpcSpot(StoryNpcEntity.Role.FOREMAN, Archipelago.GOLDWATER_CAY.cx() + 3, Archipelago.GOLDWATER_CAY.cz() - 4,
                    Archipelago.GOLDWATER_CAY.cx() + 3.5, Archipelago.GOLDWATER_CAY.cz() - 3.5, 180)
    ));

    static {
        NPC_SPOTS.addAll(OuterBuilds.NPC_SPOTS);
    }

    static void spawnNpc(Canvas c, StoryNpcEntity.Role role) {
        for (NpcSpot spot : NPC_SPOTS) {
            if (spot.role() == role) {
                c.spawn(spot.x(), spot.y(), spot.z(), spot.yaw(), w -> npc(w, role));
            }
        }
    }

    /** Trees and grass stay out of building plots. */
    public static boolean blocksPlants(int x, int z) {
        for (Build b : ALL) {
            if (b.clearsPlants() && x >= b.x1() - 2 && x <= b.x2() + 2 && z >= b.z1() - 2 && z <= b.z2() + 2) {
                return true;
            }
        }
        return false;
    }

    static void placeAll(Canvas c) {
        for (Build b : ALL) {
            if (b.intersects(c)) {
                b.placer().accept(c);
            }
        }
    }

    // =====================================================================
    // Shared pieces
    // =====================================================================

    /** Standing height at x, z (for code outside the world package). */
    public static int groundY(int x, int z) {
        return floorAt(x, z);
    }

    static int floorAt(int x, int z) {
        return Math.max(Archipelago.WATER_TOP + 1, Archipelago.column(x, z).top() + 1);
    }

    /** Floor, hollow walls with log corners, and an empty interior. */
    static void room(Canvas c, int x1, int z1, int x2, int z2, int floor, int height,
                             BlockState wall, BlockState corner, BlockState floorBlock) {
        c.foundation(x1, z1, x2, z2, floor, s(Blocks.COBBLESTONE));
        c.fill(x1, floor - 1, z1, x2, floor - 1, z2, floorBlock);
        c.walls(x1, floor, z1, x2, floor + height - 1, z2, wall);
        for (int[] k : new int[][]{{x1, z1}, {x1, z2}, {x2, z1}, {x2, z2}}) {
            c.fill(k[0], floor, k[1], k[0], floor + height - 1, k[1], corner);
        }
    }

    /** Stepped roof: shrinking rings of blocks, capped. */
    static void roof(Canvas c, int x1, int z1, int x2, int z2, int y, BlockState block) {
        int ax = x1 - 1, bx = x2 + 1, az = z1 - 1, bz = z2 + 1;
        int k = 0;
        while (ax + k <= bx - k && az + k <= bz - k) {
            if (ax + k + 1 >= bx - k - 1 || az + k + 1 >= bz - k - 1) {
                c.fill(ax + k, y + k, az + k, bx - k, y + k, bz - k, block);
                break;
            }
            c.walls(ax + k, y + k, az + k, bx - k, y + k, bz - k, block);
            k++;
        }
    }

    static void windows(Canvas c, int x1, int z1, int x2, int z2, int y) {
        for (int x = x1 + 2; x <= x2 - 2; x += 3) {
            c.set(x, y, z1, s(Blocks.GLASS));
            c.set(x, y, z2, s(Blocks.GLASS));
        }
        for (int z = z1 + 2; z <= z2 - 2; z += 3) {
            c.set(x1, y, z, s(Blocks.GLASS));
            c.set(x2, y, z, s(Blocks.GLASS));
        }
    }

    static void doorway(Canvas c, int x, int z, int floor) {
        c.set(x, floor, z, s(Blocks.AIR));
        c.set(x, floor + 1, z, s(Blocks.AIR));
    }

    static BlockState hangingLantern() {
        return s(Blocks.LANTERN).with(LanternBlock.HANGING, true);
    }

    static void bed(Canvas c, int x, int floor, int z, Direction headFacing) {
        BlockState bed = s(Blocks.RED_BED).with(BedBlock.FACING, headFacing);
        c.set(x, floor, z, bed.with(BedBlock.PART, BedPart.FOOT));
        c.set(x + headFacing.getOffsetX(), floor, z + headFacing.getOffsetZ(), bed.with(BedBlock.PART, BedPart.HEAD));
    }

    /** A plank pier on log posts; follows the ground on land. */
    static void pier(Canvas c, int x, int zStart, int zEnd, int halfWidth) {
        c.columns(x - halfWidth, zStart, x + halfWidth, zEnd, (px, pz) -> {
            int top = Archipelago.column(px, pz).top();
            if (top >= Archipelago.WATER_TOP + 1) {
                c.set(px, top, pz, s(Blocks.SPRUCE_PLANKS));
                c.set(px, top + 1, pz, s(Blocks.AIR));
            } else {
                c.set(px, 63, pz, s(Blocks.SPRUCE_PLANKS));
                if (Math.abs(px - x) == halfWidth && Math.floorMod(pz, 4) == 0) {
                    c.fill(px, top + 1, pz, px, 64, pz, s(Blocks.SPRUCE_LOG));
                    if (Math.floorMod(pz, 12) == 0) {
                        c.set(px, 65, pz, s(Blocks.LANTERN));
                    }
                }
            }
        });
    }

    public static StoryNpcEntity npc(net.minecraft.world.World w, StoryNpcEntity.Role role) {
        StoryNpcEntity npc = ModEntities.STORY_NPC.create(w);
        if (npc != null) {
            npc.setRole(role);
            npc.setCustomName(Text.literal(role.displayName()));
            npc.setCustomNameVisible(true);
        }
        return npc;
    }

    // =====================================================================
    // Driftwood Island
    // =====================================================================

    static void lighthouse(Canvas c, int cx, int cz) {
        int floor = floorAt(cx, cz);
        c.foundation(cx - 4, cz - 4, cx + 4, cz + 4, floor, s(Blocks.STONE_BRICKS));
        c.columns(cx - 5, cz - 5, cx + 5, cz + 5, (x, z) -> {
            double d = Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
            if (d <= 3.5) {
                c.set(x, floor - 1, z, s(Blocks.STONE_BRICKS));
            }
            for (int y = floor; y < floor + 20; y++) {
                if (d <= 3.5 && d > 2.5) {
                    boolean red = ((y - floor) / 4) % 2 == 1;
                    c.set(x, y, z, s(red ? Blocks.RED_TERRACOTTA : Blocks.WHITE_TERRACOTTA));
                } else if (d <= 2.5) {
                    c.set(x, y, z, s(Blocks.AIR));
                }
            }
            if (d <= 4.5) c.set(x, floor + 20, z, s(Blocks.STONE_BRICKS));
            if (d > 3.5 && d <= 4.5) c.set(x, floor + 21, z, s(Blocks.SPRUCE_FENCE));
            for (int y = floor + 21; y <= floor + 22; y++) {
                if (d <= 0.5) c.set(x, y, z, s(Blocks.SEA_LANTERN));
                else if (d <= 1.5) c.set(x, y, z, s(Blocks.GLASS));
            }
            if (d <= 2.0) c.set(x, floor + 23, z, s(Blocks.RED_TERRACOTTA));
        });
        c.set(cx, floor + 24, cz, s(Blocks.LIGHTNING_ROD));
        doorway(c, cx, cz + 3, floor);
        BlockState ladder = s(Blocks.LADDER).with(LadderBlock.FACING, Direction.SOUTH);
        for (int y = floor; y <= floor + 20; y++) {
            c.set(cx, y, cz - 2, ladder);
        }
        c.set(cx + 1, floor, cz + 1, s(Blocks.BARREL));
    }

    static void shack(Canvas c) {
        int x1 = -12, z1 = 3, x2 = -6, z2 = 8;
        int floor = floorAt(-9, 5);
        room(c, x1, z1, x2, z2, floor, 3, s(Blocks.OAK_PLANKS), s(Blocks.STRIPPED_OAK_LOG), s(Blocks.SPRUCE_PLANKS));
        roof(c, x1, z1, x2, z2, floor + 3, s(Blocks.SPRUCE_PLANKS));
        doorway(c, x2, 5, floor);
        windows(c, x1, z1, x2, z2, floor + 1);
        c.set(x1 + 1, floor, z1 + 1, s(Blocks.CRAFTING_TABLE));
        c.set(x1 + 2, floor, z1 + 1, s(Blocks.BARREL));
        bed(c, x1 + 1, floor, z2 - 2, Direction.NORTH);
        c.set(x1 + 3, floor + 2, z1 + 2, hangingLantern());
        c.chest(x1 + 4, floor, z2 - 1, Direction.NORTH, TREASURE);
        c.spawn(-3.5, floorAt(-4, 5), 5.5, 90, w -> npc(w, StoryNpcEntity.Role.KEEPER));
    }

    /** Where the player "washed up": a broken raft and scattered cargo on the beach. */
    static void wreckedRaft(Canvas c, int cx, int cz) {
        c.columns(cx - 3, cz - 2, cx + 3, cz + 2, (x, z) -> {
            int top = Archipelago.column(x, z).top();
            double r = Noise.rand(x, z, 61);
            int y = Math.max(top + 1, 63);
            if (r < 0.55) {
                c.set(x, y, z, s(Blocks.STRIPPED_SPRUCE_LOG).with(PillarBlock.AXIS, Direction.Axis.X));
            }
        });
        c.set(cx + 4, floorAt(cx + 4, cz + 3), cz + 3, s(Blocks.BARREL));
    }

    // =====================================================================
    // Saltmarrow
    // =====================================================================

    static void plazaAndRoads(Canvas c, int sx, int sz, String trader1, String trader2) {
        // Roads first, then the plaza paving over their ends
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] d : dirs) {
            for (int i = 8; i <= 40; i++) {
                for (int w = -1; w <= 1; w++) {
                    int x = sx + d[0] * i + d[1] * w;
                    int z = sz + d[1] * i + d[0] * w;
                    if (!c.contains(x, z)) continue;
                    var col = Archipelago.column(x, z);
                    if (!col.isLand()) continue;
                    BlockState here = c.get(x, col.top(), z);
                    if (here.isOf(Blocks.GRASS_BLOCK) || here.isOf(Blocks.DIRT) || here.isOf(Blocks.SAND)) {
                        c.set(x, col.top(), z, s(Blocks.DIRT_PATH));
                        c.set(x, col.top() + 1, z, s(Blocks.AIR));
                    }
                }
            }
        }
        int py = Archipelago.column(sx, sz).top();
        c.columns(sx - 7, sz - 7, sx + 7, sz + 7, (x, z) -> {
            double r = Noise.rand(x, z, 71);
            c.set(x, py, z, s(r < 0.5 ? Blocks.STONE_BRICKS : r < 0.8 ? Blocks.COBBLESTONE : Blocks.MOSSY_COBBLESTONE));
            for (int y = py + 1; y <= py + 6; y++) {
                c.set(x, y, z, s(Blocks.AIR));
            }
            int top = Archipelago.column(x, z).top();
            for (int y = top + 1; y < py; y++) {
                c.set(x, y, z, s(Blocks.COBBLESTONE));
            }
        });
        // Well
        c.walls(sx - 1, py + 1, sz - 1, sx + 1, py + 1, sz + 1, s(Blocks.STONE_BRICKS));
        c.set(sx, py, sz, s(Blocks.WATER));
        c.set(sx, py - 1, sz, s(Blocks.WATER));
        for (int[] k : new int[][]{{sx - 1, sz - 1}, {sx + 1, sz + 1}}) {
            c.fill(k[0], py + 2, k[1], k[0], py + 3, k[1], s(Blocks.OAK_FENCE));
        }
        c.fill(sx - 1, py + 4, sz - 1, sx + 1, py + 4, sz + 1, s(Blocks.SPRUCE_SLAB));
        // Market stalls
        stall(c, sx - 5, sz - 5, py, s(Blocks.RED_WOOL));
        stall(c, sx + 5, sz - 5, py, s(Blocks.YELLOW_WOOL));
        stall(c, sx - 5, sz + 5, py, s(Blocks.BLUE_WOOL));
        stall(c, sx + 5, sz + 5, py, s(Blocks.GREEN_WOOL));
        c.spawn(sx + 3.5, py + 1, sz + 3.5, 0, w -> {
            var qm = ModEntities.QUARTERMASTER.create(w);
            if (qm != null) {
                qm.setCustomName(Text.literal(trader1));
                qm.setCustomNameVisible(true);
            }
            return qm;
        });
        c.spawn(sx - 3.5, py + 1, sz - 3.5, 0, w -> {
            var qm = ModEntities.QUARTERMASTER.create(w);
            if (qm != null) {
                qm.setCustomName(Text.literal(trader2));
                qm.setCustomNameVisible(true);
            }
            return qm;
        });
    }

    static void stall(Canvas c, int x, int z, int py, BlockState canopy) {
        for (int[] k : new int[][]{{x - 1, z - 1}, {x - 1, z + 1}, {x + 1, z - 1}, {x + 1, z + 1}}) {
            c.fill(k[0], py + 1, k[1], k[0], py + 3, k[1], s(Blocks.OAK_FENCE));
        }
        c.fill(x - 1, py + 4, z - 1, x + 1, py + 4, z + 1, canopy);
        c.set(x, py + 1, z, s(Blocks.BARREL));
        c.set(x + 1, py + 1, z, s(Blocks.OAK_SLAB));
    }

    static void tavern(Canvas c, int x1, int z1, int x2, int z2) {
        int floor = floorAt((x1 + x2) / 2, (z1 + z2) / 2);
        int mz = (z1 + z2) / 2;
        room(c, x1, z1, x2, z2, floor, 4, s(Blocks.STONE_BRICKS), s(Blocks.SPRUCE_LOG), s(Blocks.SPRUCE_PLANKS));
        // Upper floor
        c.fill(x1, floor + 4, z1, x2, floor + 4, z2, s(Blocks.SPRUCE_PLANKS));
        c.walls(x1, floor + 5, z1, x2, floor + 7, z2, s(Blocks.SPRUCE_PLANKS));
        c.fill(x1 + 1, floor + 5, z1 + 1, x2 - 1, floor + 7, z2 - 1, s(Blocks.AIR));
        roof(c, x1, z1, x2, z2, floor + 8, s(Blocks.DARK_OAK_PLANKS));
        windows(c, x1, z1, x2, z2, floor + 1);
        windows(c, x1, z1, x2, z2, floor + 6);
        doorway(c, x1, mz, floor);
        // Bar counter, kegs, tables
        c.fill(x2 - 3, floor, z1 + 1, x2 - 3, floor, z2 - 3, s(Blocks.SPRUCE_PLANKS));
        c.fill(x2 - 1, floor, z1 + 1, x2 - 1, floor + 1, z2 - 3, s(Blocks.BARREL));
        for (int[] t : new int[][]{{x1 + 3, z1 + 2}, {x1 + 3, z2 - 2}, {x1 + 6, mz}}) {
            c.set(t[0], floor, t[1], s(Blocks.OAK_FENCE));
            c.set(t[0], floor + 1, t[1], s(Blocks.OAK_PRESSURE_PLATE));
        }
        c.set(x1 + 3, floor + 3, mz, hangingLantern());
        c.set(x2 - 4, floor + 3, mz, hangingLantern());
        // Ladder to the rooms upstairs
        BlockState ladder = s(Blocks.LADDER).with(LadderBlock.FACING, Direction.SOUTH);
        for (int y = floor; y <= floor + 4; y++) {
            c.set(x1 + 1, y, z1 + 1, ladder);
        }
        bed(c, x1 + 3, floor + 5, z1 + 1, Direction.SOUTH);
        bed(c, x1 + 6, floor + 5, z1 + 1, Direction.SOUTH);
        bed(c, x1 + 9, floor + 5, z1 + 1, Direction.SOUTH);
        c.chest(x2 - 1, floor + 5, z2 - 1, Direction.WEST, TAVERN);
        c.set(x1 + 6, floor + 7, mz, hangingLantern());
        spawnNpc(c, StoryNpcEntity.Role.BROKER);
        spawnNpc(c, StoryNpcEntity.Role.GAMBLER);
        spawnNpc(c, StoryNpcEntity.Role.DEALER);
    }

    static void harborHouse(Canvas c, int cx, int cz) {
        int x1 = cx - 4, x2 = cx + 4, z1 = cz - 4, z2 = cz + 4;
        int floor = floorAt(cx, cz);
        room(c, x1, z1, x2, z2, floor, 5, s(Blocks.SPRUCE_PLANKS), s(Blocks.STRIPPED_DARK_OAK_LOG), s(Blocks.POLISHED_ANDESITE));
        c.fill(x1, floor, z1, x2, floor, z2, s(Blocks.STONE_BRICKS));
        c.fill(x1 + 1, floor, z1 + 1, x2 - 1, floor, z2 - 1, s(Blocks.AIR));
        roof(c, x1, z1, x2, z2, floor + 5, s(Blocks.DARK_OAK_PLANKS));
        windows(c, x1, z1, x2, z2, floor + 2);
        doorway(c, cx, z1, floor);
        doorway(c, cx, z2, floor);
        c.set(x1 + 1, floor, z1 + 1, s(Blocks.CARTOGRAPHY_TABLE));
        c.set(x1 + 2, floor, z1 + 1, s(Blocks.LECTERN));
        c.set(x2 - 1, floor, z1 + 1, s(Blocks.BARREL));
        c.set(cx, floor + 4, cz, hangingLantern());
        c.spawn(cx + 1.5, floor, cz + 0.5, 180, w -> npc(w, StoryNpcEntity.Role.HARBORMASTER));
    }

    static void house(Canvas c, int cx, int cz, int townX, int townZ) {
        int x1 = cx - 3, x2 = cx + 3, z1 = cz - 3, z2 = cz + 3;
        int floor = floorAt(cx, cz);
        long h = Noise.hash(cx, 0, cz, 81);
        BlockState wall = s(((h & 1) == 0) ? Blocks.OAK_PLANKS : Blocks.BIRCH_PLANKS);
        BlockState roofBlock = s(((h >>> 1 & 1) == 0) ? Blocks.SPRUCE_PLANKS : Blocks.MANGROVE_PLANKS);
        room(c, x1, z1, x2, z2, floor, 4, wall, s(Blocks.OAK_LOG), s(Blocks.SPRUCE_PLANKS));
        roof(c, x1, z1, x2, z2, floor + 4, roofBlock);
        windows(c, x1, z1, x2, z2, floor + 1);
        int dx = townX - cx, dz = townZ - cz;
        if (Math.abs(dx) > Math.abs(dz)) doorway(c, dx > 0 ? x2 : x1, cz, floor);
        else doorway(c, cx, dz > 0 ? z2 : z1, floor);
        bed(c, x1 + 1, floor, z1 + 2, Direction.NORTH);
        c.set(x2 - 1, floor, z1 + 1, s(Blocks.CRAFTING_TABLE));
        c.set(x2 - 1, floor, z2 - 1, s(Blocks.BARREL));
        c.set(cx, floor + 3, cz, hangingLantern());
    }

    /** A dry dock with a half-built hull on the slipway: future home of ship purchasing. */
    static void shipyard(Canvas c, int cx, int cz) {
        int floor = floorAt(cx, cz);
        c.foundation(cx - 7, cz - 6, cx + 7, cz + 6, floor, s(Blocks.COBBLESTONE));
        c.fill(cx - 7, floor - 1, cz - 6, cx + 7, floor - 1, cz + 6, s(Blocks.SPRUCE_PLANKS));
        // Ribs of an unfinished sloop
        for (int x = cx - 5; x <= cx + 5; x += 2) {
            c.set(x, floor, cz, s(Blocks.STRIPPED_SPRUCE_LOG).with(PillarBlock.AXIS, Direction.Axis.X));
            c.fill(x, floor + 1, cz - 2, x, floor + 2, cz - 2, s(Blocks.SPRUCE_FENCE));
            c.fill(x, floor + 1, cz + 2, x, floor + 2, cz + 2, s(Blocks.SPRUCE_FENCE));
            c.set(x, floor + 1, cz - 1, s(Blocks.SPRUCE_SLAB));
            c.set(x, floor + 1, cz + 1, s(Blocks.SPRUCE_SLAB));
        }
        c.fill(cx - 6, floor, cz, cx + 6, floor, cz, s(Blocks.STRIPPED_SPRUCE_LOG).with(PillarBlock.AXIS, Direction.Axis.X));
        c.set(cx - 6, floor, cz - 5, s(Blocks.STONECUTTER));
        c.set(cx - 5, floor, cz - 5, s(Blocks.CRAFTING_TABLE));
        c.fill(cx + 4, floor, cz - 5, cx + 6, floor + 1, cz - 5, s(Blocks.STRIPPED_OAK_LOG).with(PillarBlock.AXIS, Direction.Axis.X));
        c.spawn(cx + 0.5, floor, cz - 4.5, 180, w -> {
            var shipwright = ModEntities.SHIPWRIGHT.create(w);
            if (shipwright != null) {
                shipwright.setCustomName(Text.literal("Shipwright Hollis Pike"));
                shipwright.setCustomNameVisible(true);
            }
            return shipwright;
        });
    }

    // =====================================================================
    // The Sunken Reach
    // =====================================================================

    static void sunkenBellTower(Canvas c, int cx, int cz) {
        int floor = Archipelago.column(cx, cz).top() + 1;
        int x1 = cx - 3, x2 = cx + 3, z1 = cz - 3, z2 = cz + 3;
        c.columns(x1, z1, x2, z2, (x, z) -> {
            boolean wall = x == x1 || x == x2 || z == z1 || z == z2;
            int ruinTop = 70 - (int) (Noise.rand(x, z, 91) * 7);
            c.set(x, floor - 1, z, s(Blocks.MOSSY_STONE_BRICKS));
            for (int y = floor; y <= 70; y++) {
                BlockState st;
                if (wall && y <= ruinTop) {
                    double r = Noise.rand(x, y, z, 92);
                    st = s(r < 0.45 ? Blocks.STONE_BRICKS : r < 0.75 ? Blocks.MOSSY_STONE_BRICKS : Blocks.CRACKED_STONE_BRICKS);
                } else {
                    st = y <= Archipelago.WATER_TOP ? s(Blocks.WATER) : s(Blocks.AIR);
                }
                c.set(x, y, z, st);
            }
        });
        for (int[] d : new int[][]{{cx, z1}, {cx, z2}, {x1, cz}, {x2, cz}}) {
            for (int y = floor; y <= floor + 2; y++) c.set(d[0], y, d[1], s(Blocks.WATER));
        }
        // Beam and the old bell, just above the waterline
        c.fill(x1 + 1, 66, cz, x2 - 1, 66, cz, s(Blocks.STONE_BRICK_SLAB));
        c.set(cx, 65, cz, s(Blocks.BELL).with(BellBlock.ATTACHMENT, Attachment.CEILING).with(BellBlock.FACING, Direction.NORTH));
        c.chest(cx, floor, z2 - 1, Direction.NORTH, null, new ItemStack(ModItems.REEF_SHARD));
        c.chest(x1 + 1, floor, z1 + 1, Direction.SOUTH, TREASURE);
        c.set(x2 - 1, floor, z1 + 1, s(Blocks.SEA_LANTERN));
        // Treasure guardians
        for (int[] g : new int[][]{{cx - 5, cz}, {cx + 5, cz}, {cx, cz - 5}}) {
            int gy = Archipelago.column(g[0], g[1]).top() + 1;
            c.spawn(g[0] + 0.5, gy, g[1] + 0.5, 0, EntityType.DROWNED::create);
        }
    }

    /** A small sloop broken on the reef, tilted into the sand. */
    static void shipwreck(Canvas c, int x0, int cz) {
        int base = Archipelago.column(x0 + 7, cz).top() + 1;
        for (int i = 0; i < 14; i++) {
            int x = x0 + i;
            int y = base + (i < 7 ? 0 : (i - 7) / 3);
            int halfBeam = (i < 2 || i > 11) ? 1 : 2;
            boolean broken = Noise.rand(x, cz, 95) < 0.18;
            c.set(x, y - 1, cz, s(Blocks.DARK_OAK_LOG).with(PillarBlock.AXIS, Direction.Axis.X));
            for (int w = -halfBeam; w <= halfBeam; w++) {
                c.set(x, y, cz + w, s(Blocks.DARK_OAK_PLANKS));
                if (Math.abs(w) == halfBeam && !broken) {
                    c.fill(x, y + 1, cz + w, x, y + 2, cz + w, s(Blocks.DARK_OAK_PLANKS));
                }
            }
        }
        c.fill(x0 + 7, base + 1, cz, x0 + 7, base + 6, cz, s(Blocks.STRIPPED_DARK_OAK_LOG));
        c.chest(x0 + 4, base + 1, cz, Direction.EAST, TREASURE);
    }

    // =====================================================================
    // Ashfall Island
    // =====================================================================

    static void forge(Canvas c, int x1, int z1, int x2, int z2) {
        int cx = (x1 + x2) / 2, cz = (z1 + z2) / 2;
        int floor = floorAt(cx, cz);
        room(c, x1, z1, x2, z2, floor, 5, s(Blocks.POLISHED_BLACKSTONE_BRICKS), s(Blocks.BASALT), s(Blocks.POLISHED_BLACKSTONE));
        c.fill(x1, floor + 5, z1, x2, floor + 5, z2, s(Blocks.BLACKSTONE));
        c.fill(x1 + 1, floor + 6, z1 + 1, x1 + 1, floor + 9, z1 + 1, s(Blocks.BRICKS));
        doorway(c, cx, z2, floor);
        doorway(c, cx + 1, z2, floor);
        // Lava trough
        c.fill(x1 + 2, floor - 1, cz + 2, x1 + 4, floor - 1, cz + 2, s(Blocks.LAVA));
        c.fill(x1 + 2, floor - 2, cz + 2, x1 + 4, floor - 2, cz + 2, s(Blocks.BLACKSTONE));
        // Workshop
        BlockState furnace = s(Blocks.FURNACE).with(FurnaceBlock.FACING, Direction.SOUTH);
        c.fill(x1 + 1, floor, z1 + 1, x1 + 4, floor, z1 + 1, furnace);
        c.set(x1 + 5, floor, z1 + 1, s(Blocks.BLAST_FURNACE).with(FurnaceBlock.FACING, Direction.SOUTH));
        c.set(cx + 2, floor, cz, s(Blocks.ANVIL));
        c.set(x2 - 1, floor, cz, s(Blocks.SMITHING_TABLE));
        c.set(x2 - 1, floor, cz + 1, s(Blocks.GRINDSTONE));
        c.chest(x2 - 3, floor, z1 + 1, Direction.SOUTH, null, new ItemStack(ModItems.EMBER_SHARD));
        c.chest(x2 - 1, floor, z2 - 1, Direction.NORTH, TREASURE);
        c.set(cx, floor + 4, cz, hangingLantern());
        // Smugglers guarding the shard
        for (int[] g : new int[][]{{x1 + 3, cz - 1}, {cx + 2, z2 - 2}, {x2 - 2, z1 + 2}}) {
            c.spawn(g[0] + 0.5, floor, g[1] + 0.5, 0, w -> ModEntities.CURSED_PIRATE.create(w));
        }
    }

    static void bellfounderHut(Canvas c, int x1, int z1, int x2, int z2) {
        int cx = (x1 + x2) / 2, cz = (z1 + z2) / 2;
        int floor = floorAt(cx, cz);
        room(c, x1, z1, x2, z2, floor, 4, s(Blocks.MUD_BRICKS), s(Blocks.DARK_OAK_LOG), s(Blocks.PACKED_MUD));
        roof(c, x1, z1, x2, z2, floor + 4, s(Blocks.DARK_OAK_PLANKS));
        windows(c, x1, z1, x2, z2, floor + 1);
        doorway(c, x2, cz, floor);
        c.set(cx, floor, cz, s(Blocks.BELL).with(BellBlock.ATTACHMENT, Attachment.FLOOR).with(BellBlock.FACING, Direction.EAST));
        c.set(x1 + 1, floor, z1 + 1, s(Blocks.CAULDRON));
        c.set(x1 + 1, floor, z2 - 1, s(Blocks.CHIPPED_ANVIL));
        c.set(x1 + 2, floor, z2 - 1, s(Blocks.BLAST_FURNACE).with(FurnaceBlock.FACING, Direction.NORTH));
        c.set(cx, floor + 3, cz - 1, hangingLantern());
        c.spawn(x2 + 2.5, floorAt(x2 + 2, cz), cz + 0.5, 90, w -> npc(w, StoryNpcEntity.Role.BELLWRIGHT));
    }

    // =====================================================================
    // Hollowgrave Island
    // =====================================================================

    static void temple(Canvas c, int cx, int cz) {
        int floor = Archipelago.templeFloorY();
        c.columns(cx - 11, cz - 11, cx + 11, cz + 11, (x, z) -> {
            double d = Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
            if (d > 10.5) return;
            int top = Archipelago.column(x, z).top();
            for (int y = Math.max(top + 1, Archipelago.SEA_FLOOR + 1); y <= floor - 2; y++) {
                c.set(x, y, z, s(Blocks.DEEPSLATE_BRICKS));
            }
            for (int y = floor; y <= Math.max(top, floor + 8); y++) {
                c.set(x, y, z, s(Blocks.AIR));
            }
            double r = Noise.rand(x, z, 111);
            c.set(x, floor - 1, z, s(r < 0.4 ? Blocks.POLISHED_DEEPSLATE : r < 0.75 ? Blocks.DEEPSLATE_TILES : Blocks.CRACKED_DEEPSLATE_TILES));
            if (Math.abs(d - 8) < 0.55) {
                c.set(x, floor + 6, z, s(Blocks.POLISHED_DEEPSLATE));
            }
        });
        for (int k = 0; k < 8; k++) {
            double a = Math.PI * 2 * k / 8;
            int px = cx + (int) Math.round(Math.cos(a) * 8);
            int pz = cz + (int) Math.round(Math.sin(a) * 8);
            c.fill(px, floor, pz, px, floor + 5, pz, s(Blocks.BONE_BLOCK));
            if (k % 3 == 0) c.set(px, floor + 7, pz, s(Blocks.COBWEB));
        }
        // Altar
        c.fill(cx - 1, floor, cz - 1, cx + 1, floor, cz + 1, s(Blocks.CHISELED_DEEPSLATE));
        c.set(cx, floor, cz, s(Blocks.CRYING_OBSIDIAN));
        for (int[] l : new int[][]{{cx - 2, cz - 2}, {cx + 2, cz - 2}, {cx - 2, cz + 2}, {cx + 2, cz + 2}}) {
            c.set(l[0], floor, l[1], s(Blocks.SOUL_LANTERN));
        }
        // Thorne's half-bell hanging on a dark frame
        c.fill(cx - 1, floor, cz - 4, cx - 1, floor + 2, cz - 4, s(Blocks.DARK_OAK_FENCE));
        c.fill(cx + 1, floor, cz - 4, cx + 1, floor + 2, cz - 4, s(Blocks.DARK_OAK_FENCE));
        c.fill(cx - 1, floor + 3, cz - 4, cx + 1, floor + 3, cz - 4, s(Blocks.DARK_OAK_PLANKS));
        c.set(cx, floor + 2, cz - 4, s(Blocks.BELL).with(BellBlock.ATTACHMENT, Attachment.CEILING).with(BellBlock.FACING, Direction.EAST));
        c.set(cx - 5, floor, cz, s(Blocks.SOUL_CAMPFIRE));
        c.set(cx + 5, floor, cz, s(Blocks.SOUL_CAMPFIRE));
    }

    // =====================================================================
    // Mirefen Bayou
    // =====================================================================

    static final int DECK = 64;

    /** Stilt houses on mangrove posts, joined by boardwalks to a central landing. */
    static void hooklineLanding(Canvas c, int cx, int cz) {
        BlockState plank = s(Blocks.MANGROVE_PLANKS);
        // Central landing platform
        c.fill(cx - 4, DECK, cz - 4, cx + 4, DECK, cz + 4, plank);
        c.fill(cx - 4, DECK + 1, cz - 4, cx + 4, DECK + 4, cz + 4, s(Blocks.AIR));
        stilts(c, cx - 4, cz - 4, cx + 4, cz + 4);
        for (int[] k : new int[][]{{cx - 4, cz - 4}, {cx + 4, cz - 4}, {cx - 4, cz + 4}, {cx + 4, cz + 4}}) {
            c.fill(k[0], DECK + 1, k[1], k[0], DECK + 2, k[1], s(Blocks.MANGROVE_FENCE));
            c.set(k[0], DECK + 3, k[1], s(Blocks.LANTERN));
        }
        c.set(cx, DECK + 1, cz - 2, s(Blocks.BARREL));
        c.set(cx + 1, DECK + 1, cz - 2, s(Blocks.CAULDRON));
        c.set(cx - 1, DECK + 1, cz - 2, s(Blocks.BREWING_STAND));
        c.spawn(cx + 0.5, DECK + 1, cz + 1.5, 180, w -> {
            var trader = ModEntities.QUARTERMASTER.create(w);
            if (trader != null) {
                trader.setCustomName(Text.literal("Silas Fenn, Bayou Trader"));
                trader.setCustomNameVisible(true);
            }
            return trader;
        });
        for (int i = 0; i < 3; i++) {
            int fx = cx - 6 + i * 6;
            c.spawn(fx + 0.5, Archipelago.standY(fx, cz + 8), cz + 8.5, 0, w -> EntityType.FROG.create(w));
        }

        // Houses and the boardwalks that reach them
        int[][] houses = {{-13, -10}, {11, -12}, {14, 6}, {-9, 12}, {-15, 2}};
        for (int[] h : houses) {
            int hx = cx + h[0], hz = cz + h[1];
            boardwalk(c, cx, cz, hx, hz);
            stiltHouse(c, hx, hz);
        }
        boardwalk(c, cx, cz, cx, cz + 20); // to the pier
    }

    static void stilts(Canvas c, int x1, int z1, int x2, int z2) {
        for (int[] k : new int[][]{{x1, z1}, {x1, z2}, {x2, z1}, {x2, z2}}) {
            int ground = Archipelago.column(k[0], k[1]).top();
            c.fill(k[0], ground + 1, k[1], k[0], DECK - 1, k[1], s(Blocks.MANGROVE_LOG));
        }
    }

    /** Two-wide plank walk: first along x, then along z, on posts every few blocks. */
    static void boardwalk(Canvas c, int x1, int z1, int x2, int z2) {
        BlockState plank = s(Blocks.SPRUCE_PLANKS);
        int stepX = Integer.signum(x2 - x1);
        for (int x = x1; x != x2 + stepX && stepX != 0; x += stepX) {
            c.fill(x, DECK, z1, x, DECK, z1 + 1, plank);
            if (Math.floorMod(x, 5) == 0) {
                int ground = Archipelago.column(x, z1).top();
                c.fill(x, ground + 1, z1, x, DECK - 1, z1, s(Blocks.MANGROVE_LOG));
            }
        }
        int stepZ = Integer.signum(z2 - z1);
        for (int z = z1; z != z2 + stepZ && stepZ != 0; z += stepZ) {
            c.fill(x2, DECK, z, x2 + 1, DECK, z, plank);
            if (Math.floorMod(z, 5) == 0) {
                int ground = Archipelago.column(x2, z).top();
                c.fill(x2, ground + 1, z, x2, DECK - 1, z, s(Blocks.MANGROVE_LOG));
                if (Math.floorMod(z, 10) == 0) {
                    c.set(x2, DECK + 1, z, s(Blocks.MANGROVE_FENCE));
                    c.set(x2, DECK + 2, z, s(Blocks.LANTERN));
                }
            }
        }
    }

    static void stiltHouse(Canvas c, int cx, int cz) {
        int x1 = cx - 3, x2 = cx + 3, z1 = cz - 3, z2 = cz + 3;
        int floor = DECK + 1;
        stilts(c, x1, z1, x2, z2);
        c.fill(x1, DECK, z1, x2, DECK, z2, s(Blocks.MANGROVE_PLANKS));
        c.fill(x1, floor, z1, x2, floor + 4, z2, s(Blocks.AIR));
        c.walls(x1, floor, z1, x2, floor + 2, z2, s(Blocks.MANGROVE_PLANKS));
        for (int[] k : new int[][]{{x1, z1}, {x1, z2}, {x2, z1}, {x2, z2}}) {
            c.fill(k[0], floor, k[1], k[0], floor + 2, k[1], s(Blocks.STRIPPED_MANGROVE_LOG));
        }
        roof(c, x1, z1, x2, z2, floor + 3, s(Blocks.DARK_OAK_PLANKS));
        windows(c, x1, z1, x2, z2, floor + 1);
        // The boardwalk's last leg runs along z, so the door faces back toward the landing on that side.
        int dz = Integer.signum(Archipelago.MIREFEN_BAYOU.cz() - cz);
        doorway(c, cx, dz >= 0 ? z2 : z1, floor);
        doorway(c, cx + 1, dz >= 0 ? z2 : z1, floor);
        bed(c, x1 + 1, floor, z1 + 2, Direction.NORTH);
        c.set(x2 - 1, floor, z1 + 1, s(Blocks.BARREL));
        c.set(x2 - 1, floor, z2 - 1, s(Blocks.FLOWER_POT));
        c.set(cx, floor + 2, cz, hangingLantern());
    }

    // =====================================================================
    // Ironwatch Isle
    // =====================================================================

    static void fortress(Canvas c, int cx, int cz) {
        int floor = floorAt(cx, cz);
        BlockState wall = s(Blocks.STONE_BRICKS);
        c.foundation(cx - 17, cz - 17, cx + 17, cz + 17, floor, s(Blocks.STONE_BRICKS));

        // Courtyard
        c.columns(cx - 16, cz - 16, cx + 16, cz + 16, (x, z) -> {
            double r = Noise.rand(x, z, 131);
            c.set(x, floor - 1, z, s(r < 0.5 ? Blocks.STONE_BRICKS : r < 0.8 ? Blocks.COBBLESTONE : Blocks.GRAVEL));
        });

        // Curtain wall: two blocks thick with a walkway on top, crenellated
        c.walls(cx - 15, floor, cz - 15, cx + 15, floor + 6, cz + 15, wall);
        c.walls(cx - 14, floor, cz - 14, cx + 14, floor + 6, cz + 14, wall);
        c.columns(cx - 15, cz - 15, cx + 15, cz + 15, (x, z) -> {
            boolean outer = Math.abs(x - cx) == 15 || Math.abs(z - cz) == 15;
            if (outer && Math.floorMod(x + z, 2) == 0) {
                c.set(x, floor + 7, z, wall);
            }
        });
        // Gate facing west, toward Saltmarrow
        for (int x = cx - 15; x <= cx - 14; x++) {
            c.fill(x, floor, cz - 1, x, floor + 2, cz + 1, s(Blocks.AIR));
            c.set(x, floor + 3, cz, s(Blocks.CHISELED_STONE_BRICKS));
        }
        c.set(cx - 16, floor + 2, cz - 2, s(Blocks.LANTERN));
        c.set(cx - 16, floor + 2, cz + 2, s(Blocks.LANTERN));
        // Inner ladder up to the walkway
        BlockState ladder = s(Blocks.LADDER).with(LadderBlock.FACING, Direction.SOUTH);
        for (int y = floor; y <= floor + 6; y++) {
            c.set(cx + 6, y, cz - 13, ladder);
        }

        // Corner bastions with guns on top
        for (int[] k : new int[][]{{-15, -15}, {-15, 15}, {15, -15}, {15, 15}}) {
            int tx = cx + k[0], tz = cz + k[1];
            c.columns(tx - 5, tz - 5, tx + 5, tz + 5, (x, z) -> {
                double d = Math.sqrt((x - tx) * (x - tx) + (z - tz) * (z - tz));
                if (d <= 3.5) {
                    for (int y = floor; y <= floor + 9; y++) {
                        double r = Noise.rand(x, y, z, 132);
                        c.set(x, y, z, s(r < 0.75 ? Blocks.STONE_BRICKS : Blocks.MOSSY_STONE_BRICKS));
                    }
                }
                if (d <= 4.5) c.set(x, floor + 10, z, wall);
                if (d > 3.5 && d <= 4.5 && Math.floorMod(x + z, 2) == 0) c.set(x, floor + 11, z, wall);
            });
            c.set(tx + Integer.signum(k[0]) * 2, floor + 11, tz, s(Blocks.ANVIL));
            c.set(tx, floor + 11, tz + Integer.signum(k[1]) * 2, s(Blocks.ANVIL));
            c.set(tx, floor + 11, tz, s(Blocks.LANTERN));
        }

        // Barracks along the north wall
        int bx1 = cx - 12, bz1 = cz - 12, bx2 = cx + 2, bz2 = cz - 6;
        room(c, bx1, bz1, bx2, bz2, floor, 4, s(Blocks.SPRUCE_PLANKS), s(Blocks.STONE_BRICKS), s(Blocks.SPRUCE_PLANKS));
        c.fill(bx1, floor, bz1, bx2, floor, bz2, wall);
        c.fill(bx1 + 1, floor, bz1 + 1, bx2 - 1, floor, bz2 - 1, s(Blocks.AIR));
        roof(c, bx1, bz1, bx2, bz2, floor + 4, s(Blocks.DARK_OAK_PLANKS));
        windows(c, bx1, bz1, bx2, bz2, floor + 2);
        doorway(c, (bx1 + bx2) / 2, bz2, floor);
        for (int x = bx1 + 1; x <= bx2 - 2; x += 2) {
            bed(c, x, floor, bz1 + 2, Direction.NORTH);
        }
        c.fill(bx2 - 1, floor, bz2 - 1, bx2 - 1, floor + 1, bz2 - 1, s(Blocks.BARREL));
        c.set((bx1 + bx2) / 2, floor + 3, (bz1 + bz2) / 2, hangingLantern());

        // The brig, south-west
        int gx1 = cx - 12, gz1 = cz + 6, gx2 = cx - 7, gz2 = cz + 12;
        room(c, gx1, gz1, gx2, gz2, floor, 4, wall, s(Blocks.POLISHED_ANDESITE), s(Blocks.COBBLESTONE));
        c.fill(gx1, floor + 4, gz1, gx2, floor + 4, gz2, wall);
        c.fill(gx2, floor, gz1 + 1, gx2, floor + 2, gz2 - 1, s(Blocks.IRON_BARS));
        c.set(gx1 + 1, floor, gz2 - 1, s(Blocks.CAULDRON));
        c.set(gx1 + 2, floor + 3, gz1 + 3, hangingLantern());

        // Commodore's hall, east
        int hx1 = cx + 5, hz1 = cz - 4, hx2 = cx + 12, hz2 = cz + 6;
        room(c, hx1, hz1, hx2, hz2, floor, 6, s(Blocks.STONE_BRICKS), s(Blocks.POLISHED_ANDESITE), s(Blocks.POLISHED_ANDESITE));
        roof(c, hx1, hz1, hx2, hz2, floor + 6, s(Blocks.DEEPSLATE_TILES));
        c.fill(hx1, floor + 4, hz1, hx2, floor + 4, hz1, s(Blocks.BLUE_WOOL));
        c.fill(hx1, floor + 4, hz2, hx2, floor + 4, hz2, s(Blocks.BLUE_WOOL));
        windows(c, hx1, hz1, hx2, hz2, floor + 2);
        doorway(c, hx1, cz + 1, floor);
        c.set(hx2 - 1, floor, cz, s(Blocks.CARTOGRAPHY_TABLE));
        c.set(hx2 - 1, floor, cz + 1, s(Blocks.LECTERN));
        c.set(hx2 - 1, floor, cz + 2, s(Blocks.CARTOGRAPHY_TABLE));
        c.set(hx1 + 2, floor, hz1 + 1, s(Blocks.BARREL));
        c.chest(hx2 - 1, floor, hz2 - 1, Direction.WEST, TREASURE);
        c.set(cx + 8, floor + 5, cz + 1, hangingLantern());
        spawnNpc(c, StoryNpcEntity.Role.COMMODORE);

        // Flagpole in the parade ground: Tidewatch blue with a white band
        c.fill(cx, floor, cz, cx, floor + 13, cz, s(Blocks.SPRUCE_FENCE));
        c.fill(cx + 1, floor + 10, cz, cx + 5, floor + 12, cz, s(Blocks.BLUE_WOOL));
        c.fill(cx + 1, floor + 11, cz, cx + 5, floor + 11, cz, s(Blocks.WHITE_WOOL));

        // Garrison
        int[][] posts = {{cx - 10, cz - 2}, {cx - 10, cz + 3}, {cx - 2, cz + 9}, {cx + 2, cz - 3}};
        for (int[] p : posts) {
            c.spawn(p[0] + 0.5, floor, p[1] + 0.5, 0, w -> ModEntities.MARINE.create(w));
        }
        c.spawn(cx + 0.5, floor + 7, cz - 14 + 0.5, 180, w -> ModEntities.MARINE.create(w));
        c.spawn(cx + 0.5, floor + 7, cz + 14 + 0.5, 0, w -> ModEntities.MARINE.create(w));
    }

    // =====================================================================
    // Port Meridian
    // =====================================================================

    /** The Governor's residence: stone ground floor, whitewashed upper floor. */
    static void mansion(Canvas c, int x1, int z1, int x2, int z2) {
        int cx = (x1 + x2) / 2, cz = (z1 + z2) / 2;
        int floor = floorAt(cx, cz);
        room(c, x1, z1, x2, z2, floor, 4, s(Blocks.STONE_BRICKS), s(Blocks.POLISHED_ANDESITE), s(Blocks.DARK_OAK_PLANKS));
        c.fill(x1, floor + 4, z1, x2, floor + 4, z2, s(Blocks.DARK_OAK_PLANKS));
        c.walls(x1, floor + 5, z1, x2, floor + 7, z2, s(Blocks.WHITE_TERRACOTTA));
        c.fill(x1 + 1, floor + 5, z1 + 1, x2 - 1, floor + 7, z2 - 1, s(Blocks.AIR));
        roof(c, x1, z1, x2, z2, floor + 8, s(Blocks.DEEPSLATE_TILES));
        windows(c, x1, z1, x2, z2, floor + 1);
        windows(c, x1, z1, x2, z2, floor + 6);
        doorway(c, x1, cz, floor);
        c.fill(x1 + 1, floor, cz, x2 - 1, floor, cz, s(Blocks.RED_CARPET));
        c.fill(x2 - 2, floor, cz - 1, x2 - 2, floor, cz + 1, s(Blocks.DARK_OAK_PLANKS)); // the Governor's desk
        c.set(x2 - 1, floor, cz - 2, s(Blocks.BOOKSHELF));
        c.set(x2 - 1, floor, cz + 2, s(Blocks.BOOKSHELF));
        c.set(x2 - 1, floor + 1, cz - 2, s(Blocks.BOOKSHELF));
        c.set(x2 - 1, floor + 1, cz + 2, s(Blocks.BOOKSHELF));
        BlockState ladder = s(Blocks.LADDER).with(LadderBlock.FACING, Direction.SOUTH);
        for (int y = floor; y <= floor + 4; y++) c.set(x1 + 1, y, z1 + 1, ladder);
        c.set(cx, floor + 3, cz, hangingLantern());
        bed(c, x2 - 2, floor + 5, z1 + 1, Direction.SOUTH);
        c.chest(x2 - 1, floor + 5, z2 - 1, Direction.WEST, TREASURE);
        spawnNpc(c, StoryNpcEntity.Role.GOVERNOR);
    }

    static void chapel(Canvas c, int x1, int z1, int x2, int z2) {
        int cx = (x1 + x2) / 2, cz = (z1 + z2) / 2;
        int floor = floorAt(cx, cz);
        room(c, x1, z1, x2, z2, floor, 6, s(Blocks.SMOOTH_SANDSTONE), s(Blocks.CUT_SANDSTONE), s(Blocks.SPRUCE_PLANKS));
        roof(c, x1, z1, x2, z2, floor + 6, s(Blocks.DARK_OAK_PLANKS));
        windows(c, x1, z1, x2, z2, floor + 3);
        doorway(c, x2, cz, floor);
        for (int x = x1 + 3; x <= x2 - 2; x += 2) {
            c.fill(x, floor, z1 + 1, x, floor, cz - 1, s(Blocks.SPRUCE_SLAB));
            c.fill(x, floor, cz + 1, x, floor, z2 - 1, s(Blocks.SPRUCE_SLAB));
        }
        c.set(x1 + 1, floor, cz, s(Blocks.LECTERN));
        c.set(x1 + 1, floor + 3, cz, s(Blocks.CANDLE));
        int roofTop = floor + 6 + Math.min((x2 - x1) / 2, (z2 - z1) / 2) + 1;
        c.fill(cx, roofTop, cz, cx, roofTop + 2, cz, s(Blocks.SPRUCE_FENCE));
        c.set(cx, roofTop + 3, cz, s(Blocks.DARK_OAK_PLANKS));
        c.set(cx, roofTop + 2, cz + 1, s(Blocks.BELL).with(BellBlock.ATTACHMENT, Attachment.SINGLE_WALL)
                .with(BellBlock.FACING, Direction.NORTH));
    }

    // =====================================================================
    // Blackthorn Isle
    // =====================================================================

    static void blackthornCamp(Canvas c, int cx, int cz) {
        // Palisade with a south gate
        c.columns(cx - 14, cz - 14, cx + 14, cz + 14, (x, z) -> {
            double d = Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
            if (d < 12.5 || d > 13.5) return;
            if (z > cz && Math.abs(x - cx) <= 1) return;
            int top = Archipelago.column(x, z).top();
            c.fill(x, top + 1, z, x, top + 4, z, s(Blocks.SPRUCE_LOG));
        });
        // Tents
        BlockState[] cloth = {s(Blocks.RED_WOOL), s(Blocks.BLACK_WOOL), s(Blocks.BROWN_WOOL), s(Blocks.GRAY_WOOL)};
        int[][] tents = {{-7, -6}, {6, -7}, {-7, 5}, {6, 5}};
        for (int i = 0; i < tents.length; i++) {
            int tx = cx + tents[i][0], tz = cz + tents[i][1];
            int base = Archipelago.column(tx, tz).top() + 1;
            for (int z = tz; z <= tz + 3; z++) {
                for (int dx = -2; dx <= 2; dx++) {
                    c.set(tx + dx, base + (2 - Math.abs(dx)), z, cloth[i]);
                }
            }
            c.set(tx, base, tz + 1, s(Blocks.RED_BED).with(BedBlock.FACING, Direction.SOUTH).with(BedBlock.PART, BedPart.FOOT));
            c.set(tx, base, tz + 2, s(Blocks.RED_BED).with(BedBlock.FACING, Direction.SOUTH).with(BedBlock.PART, BedPart.HEAD));
            if (i == 0) {
                // the stolen ledger lies here
                c.chest(tx - 1, base, tz + 2, Direction.EAST, null, new ItemStack(ModItems.GOVERNORS_LEDGER));
            } else {
                c.set(tx + 1, base, tz + 2, s(Blocks.BARREL));
            }
        }
        // Bonfire and seats
        int fy = Archipelago.column(cx, cz).top() + 1;
        c.set(cx, fy, cz, s(Blocks.CAMPFIRE));
        c.set(cx - 2, fy, cz, s(Blocks.STRIPPED_SPRUCE_LOG).with(PillarBlock.AXIS, Direction.Axis.Z));
        c.set(cx, fy, cz - 2, s(Blocks.STRIPPED_SPRUCE_LOG).with(PillarBlock.AXIS, Direction.Axis.X));
        // Lookout
        int lx = cx + 9, lz = cz + 9;
        int ly = Archipelago.column(lx, lz).top() + 1;
        for (int[] k : new int[][]{{lx - 1, lz - 1}, {lx + 1, lz - 1}, {lx - 1, lz + 1}, {lx + 1, lz + 1}}) {
            c.fill(k[0], ly, k[1], k[0], ly + 7, k[1], s(Blocks.SPRUCE_LOG));
        }
        c.fill(lx - 1, ly + 8, lz - 1, lx + 1, ly + 8, lz + 1, s(Blocks.SPRUCE_PLANKS));
        BlockState ladder = s(Blocks.LADDER).with(LadderBlock.FACING, Direction.NORTH);
        for (int y = ly; y <= ly + 8; y++) c.set(lx, y, lz + 2, ladder);
        c.set(lx, ly + 9, lz, s(Blocks.LANTERN));
        spawnNpc(c, StoryNpcEntity.Role.ROOK);
        spawnNpc(c, StoryNpcEntity.Role.DEALER);
        c.spawn(cx - 3.5, fy, cz + 2.5, 0, w -> {
            var fence = ModEntities.QUARTERMASTER.create(w);
            if (fence != null) {
                fence.setCustomName(Text.literal("Tilda Crook, Fence"));
                fence.setCustomNameVisible(true);
            }
            return fence;
        });
    }

    // =====================================================================
    // Emerald Cay
    // =====================================================================

    /** A vine-hung stepped temple, one block per step, with a shrine on top. */
    static void steppedTemple(Canvas c, int cx, int cz) {
        int base = floorAt(cx, cz);
        c.foundation(cx - 9, cz - 9, cx + 9, cz + 9, base, s(Blocks.MOSSY_COBBLESTONE));
        for (int k = 0; k <= 7; k++) {
            int half = 8 - k;
            final int y = base + k;
            c.fillFn(cx - half, y, cz - half, cx + half, y, cz + half, (x, yy, z) -> {
                double r = Noise.rand(x, yy, z, 141);
                return s(r < 0.4 ? Blocks.MOSSY_STONE_BRICKS : r < 0.7 ? Blocks.MOSSY_COBBLESTONE
                        : r < 0.9 ? Blocks.CRACKED_STONE_BRICKS : Blocks.CHISELED_STONE_BRICKS);
            });
        }
        int top = base + 8;
        c.set(cx, top, cz, s(Blocks.LODESTONE));
        c.chest(cx, top, cz + 1, Direction.SOUTH, TREASURE);
        for (int[] k : new int[][]{{cx - 1, cz - 1}, {cx + 1, cz - 1}, {cx - 1, cz + 1}, {cx + 1, cz + 1}}) {
            c.set(k[0], top, k[1], s(Blocks.LANTERN));
        }
        c.columns(cx - 9, cz - 9, cx + 9, cz + 9, (x, z) -> {
            int edge = Math.max(Math.abs(x - cx), Math.abs(z - cz));
            if (edge > 8 || Noise.rand(x, z, 142) > 0.18) return;
            int y = base + (8 - edge);
            Direction out = Math.abs(x - cx) >= Math.abs(z - cz)
                    ? (x > cx ? Direction.EAST : Direction.WEST) : (z > cz ? Direction.SOUTH : Direction.NORTH);
            var prop = net.minecraft.block.VineBlock.getFacingProperty(out.getOpposite());
            c.set(x + out.getOffsetX(), y, z + out.getOffsetZ(), s(Blocks.VINE).with(prop, true));
        });
    }

    static void naturalistCamp(Canvas c, int x, int z) {
        int y = floorAt(x, z);
        for (int dz = -2; dz <= 2; dz++) {
            for (int dx = -2; dx <= 2; dx++) {
                c.set(x + dx, y + (2 - Math.abs(dx)), z + dz, s(Blocks.GREEN_WOOL));
            }
        }
        c.set(x - 1, y, z, s(Blocks.CRAFTING_TABLE));
        c.set(x, y, z - 1, s(Blocks.COMPOSTER));
        c.set(x + 1, y, z - 1, s(Blocks.BARREL));
        c.set(x, y, z + 1, s(Blocks.FLOWER_POT));
        spawnNpc(c, StoryNpcEntity.Role.NATURALIST);
    }

    // =====================================================================
    // Whispering Key
    // =====================================================================

    static void hauntedMansion(Canvas c, int x1, int z1, int x2, int z2) {
        int cx = (x1 + x2) / 2, cz = (z1 + z2) / 2;
        int floor = floorAt(cx, cz);
        room(c, x1, z1, x2, z2, floor, 4, s(Blocks.DARK_OAK_PLANKS), s(Blocks.STRIPPED_DARK_OAK_LOG), s(Blocks.DARK_OAK_PLANKS));
        c.fill(x1, floor + 4, z1, x2, floor + 4, z2, s(Blocks.DARK_OAK_PLANKS));
        c.walls(x1, floor + 5, z1, x2, floor + 7, z2, s(Blocks.DARK_OAK_PLANKS));
        c.fill(x1 + 1, floor + 5, z1 + 1, x2 - 1, floor + 7, z2 - 1, s(Blocks.AIR));
        roof(c, x1, z1, x2, z2, floor + 8, s(Blocks.DEEPSLATE_TILES));
        // Broken windows
        c.columns(x1, z1, x2, z2, (x, z) -> {
            boolean wall = x == x1 || x == x2 || z == z1 || z == z2;
            if (!wall) return;
            for (int y : new int[]{floor + 1, floor + 6}) {
                if (Math.floorMod(x + z, 3) == 0 && !(x == x1 && z == cz)) {
                    double r = Noise.rand(x, y, z, 151);
                    c.set(x, y, z, r < 0.5 ? s(Blocks.AIR) : r < 0.75 ? s(Blocks.GLASS_PANE) : s(Blocks.COBWEB));
                }
            }
        });
        doorway(c, x1, cz, floor);
        c.columns(x1 + 1, z1 + 1, x2 - 1, z2 - 1, (x, z) -> {
            double r = Noise.rand(x, z, 152);
            if (r < 0.06) c.set(x, floor + 3, z, s(Blocks.COBWEB));
            else if (r < 0.1) c.set(x, floor + 7, z, s(Blocks.COBWEB));
        });
        c.fill(x2 - 1, floor, z1 + 1, x2 - 1, floor + 2, z1 + 3, s(Blocks.BOOKSHELF));
        c.set(cx, floor + 3, cz, s(Blocks.SOUL_LANTERN).with(LanternBlock.HANGING, true));
        c.set(cx, floor + 7, cz, s(Blocks.SOUL_LANTERN).with(LanternBlock.HANGING, true));
        BlockState ladder = s(Blocks.LADDER).with(LadderBlock.FACING, Direction.SOUTH);
        for (int y = floor; y <= floor + 4; y++) c.set(x1 + 1, y, z1 + 1, ladder);
        c.chest(x2 - 1, floor + 5, z2 - 1, Direction.WEST, TREASURE);
        // The shade that haunts the place (the Medium's quest)
        c.spawn(cx + 0.5, floor, cz + 0.5, 0, w -> EntityType.WITHER_SKELETON.create(w), e -> {
            if (!(e instanceof net.minecraft.entity.mob.MobEntity shade)) return;
            shade.setCustomName(Text.literal("The Shade of Lady Morrow").formatted(Formatting.DARK_AQUA));
            shade.setCustomNameVisible(true);
            shade.addCommandTag("cursedtides_shade");
            var hp = shade.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.GENERIC_MAX_HEALTH);
            if (hp != null) {
                hp.setBaseValue(90);
                shade.setHealth(90);
            }
        });
    }

    static void mediumHut(Canvas c, int x1, int z1, int x2, int z2) {
        int cx = (x1 + x2) / 2, cz = (z1 + z2) / 2;
        int floor = floorAt(cx, cz);
        room(c, x1, z1, x2, z2, floor, 3, s(Blocks.SPRUCE_PLANKS), s(Blocks.DARK_OAK_LOG), s(Blocks.SPRUCE_PLANKS));
        roof(c, x1, z1, x2, z2, floor + 3, s(Blocks.DARK_OAK_PLANKS));
        doorway(c, x2, cz, floor);
        c.set(x1 + 1, floor, cz, s(Blocks.ENCHANTING_TABLE));
        c.set(x1 + 1, floor, z1 + 1, s(Blocks.CANDLE));
        c.set(x1 + 1, floor, z2 - 1, s(Blocks.SKELETON_SKULL));
        c.set(cx, floor + 2, cz, s(Blocks.SOUL_LANTERN).with(LanternBlock.HANGING, true));
        spawnNpc(c, StoryNpcEntity.Role.MEDIUM);
    }

    // =====================================================================
    // Goldwater Cay
    // =====================================================================

    static void miningCamp(Canvas c, int cx, int cz) {
        int y = Archipelago.column(cx, cz).top() + 1;
        // Headframe over an old shaft
        for (int[] k : new int[][]{{cx - 2, cz - 2}, {cx + 2, cz - 2}, {cx - 2, cz + 2}, {cx + 2, cz + 2}}) {
            int ky = Archipelago.column(k[0], k[1]).top() + 1;
            c.fill(k[0], ky, k[1], k[0], y + 6, k[1], s(Blocks.SPRUCE_LOG));
        }
        c.fill(cx - 2, y + 7, cz, cx + 2, y + 7, cz, s(Blocks.STRIPPED_SPRUCE_LOG).with(PillarBlock.AXIS, Direction.Axis.X));
        c.fill(cx, y + 3, cz, cx, y + 6, cz, s(Blocks.CHAIN));
        c.fill(cx - 1, y - 1, cz - 1, cx + 1, y - 1, cz + 1, s(Blocks.SPRUCE_TRAPDOOR));
        // Rails and ore heaps
        for (int x = cx + 3; x <= cx + 10; x++) {
            c.set(x, Archipelago.column(x, cz).top() + 1, cz, s(Blocks.RAIL));
        }
        c.set(cx + 10, Archipelago.column(cx + 10, cz + 2).top() + 1, cz + 2, s(Blocks.RAW_GOLD_BLOCK));
        c.set(cx + 9, Archipelago.column(cx + 9, cz + 2).top() + 1, cz + 2, s(Blocks.GOLD_ORE));
        c.set(cx + 10, Archipelago.column(cx + 10, cz + 3).top() + 1, cz + 3, s(Blocks.COBBLESTONE));
        // Tents
        int[][] tents = {{-7, -6}, {-8, 5}, {5, -9}};
        for (int[] t : tents) {
            int tx = cx + t[0], tz = cz + t[1];
            int base = Archipelago.column(tx, tz).top() + 1;
            for (int z = tz; z <= tz + 3; z++) {
                for (int dx = -2; dx <= 2; dx++) {
                    c.set(tx + dx, base + (2 - Math.abs(dx)), z, s(Blocks.WHITE_WOOL));
                }
            }
            c.set(tx, base, tz + 2, s(Blocks.BARREL));
        }
        spawnNpc(c, StoryNpcEntity.Role.FOREMAN);
        c.spawn(cx - 3.5, y, cz + 3.5, 0, w -> {
            var assayer = ModEntities.QUARTERMASTER.create(w);
            if (assayer != null) {
                assayer.setCustomName(Text.literal("Quill Hart, Assayer"));
                assayer.setCustomNameVisible(true);
            }
            return assayer;
        });
    }

    static void cemetery(Canvas c, int x1, int z1, int x2, int z2) {
        c.columns(x1, z1, x2, z2, (x, z) -> {
            var col = Archipelago.column(x, z);
            if (!col.isLand()) return;
            int y = col.top() + 1;
            boolean edge = x == x1 || x == x2 || z == z1 || z == z2;
            if (edge) {
                if (Noise.rand(x, z, 121) < 0.8) c.set(x, y, z, s(Blocks.DARK_OAK_FENCE));
                return;
            }
            if ((x - x1) % 3 == 1 && (z - z1) % 3 == 1) {
                double r = Noise.rand(x, z, 122);
                c.set(x, y, z, s(r < 0.5 ? Blocks.COBBLESTONE_WALL : r < 0.8 ? Blocks.MOSSY_COBBLESTONE_WALL : Blocks.STONE_BRICK_WALL));
                c.set(x, col.top(), z + 1, s(Blocks.COARSE_DIRT));
            }
        });
        int gx = (x1 + x2) / 2, gz = (z1 + z2) / 2;
        var col = Archipelago.column(gx, gz);
        if (col.isLand()) {
            c.chest(gx, col.top(), gz, Direction.NORTH, TREASURE); // buried under the central grave
        }
    }
}
