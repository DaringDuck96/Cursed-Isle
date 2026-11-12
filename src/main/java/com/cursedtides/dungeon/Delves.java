package com.cursedtides.dungeon;

import com.cursedtides.CursedTides;
import com.cursedtides.combat.Levels;
import com.cursedtides.entity.ModEntities;
import com.cursedtides.world.Archipelago;
import com.cursedtides.world.Builds;
import com.cursedtides.world.Canvas;
import com.cursedtides.world.Noise;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.LanternBlock;
import net.minecraft.block.PillarBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTables;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Two new kinds of dungeon besides the tunnels and the grand caverns.
 *
 * Wrecks: great hulks lying on the sea floor off the islands, their weather decks just clear of the water. Climb
 * the side, then down through the hatches deck by deck (the captain's deck, the crew deck, the gun deck) to the
 * hold, where the dead captain sits on what's left of the treasure. Every deck has its own dead crew, and they
 * come back while you're gone.
 *
 * Spires: ruined towers on the wild hinterlands of the islands. Fight up floor by floor to whatever holds the roof.
 */
public final class Delves {
    private Delves() {}

    public enum Kind { WRECK, SPIRE }

    public enum Look {
        WRECK_DARK(Blocks.DARK_OAK_PLANKS, Blocks.SPRUCE_PLANKS, Blocks.STRIPPED_DARK_OAK_LOG, Blocks.SOUL_LANTERN),
        WRECK_PALE(Blocks.SPRUCE_PLANKS, Blocks.OAK_PLANKS, Blocks.STRIPPED_SPRUCE_LOG, Blocks.LANTERN),
        GALLOWS(Blocks.CUT_SANDSTONE, Blocks.SMOOTH_SANDSTONE, Blocks.CHISELED_SANDSTONE, Blocks.LANTERN),
        WITCHLIGHT(Blocks.MUD_BRICKS, Blocks.MANGROVE_PLANKS, Blocks.MANGROVE_LOG, Blocks.OCHRE_FROGLIGHT),
        SIGNAL(Blocks.STONE_BRICKS, Blocks.POLISHED_ANDESITE, Blocks.CHISELED_STONE_BRICKS, Blocks.LANTERN);

        final Block wall, floor, trim, light;

        Look(Block wall, Block floor, Block trim, Block light) {
            this.wall = wall;
            this.floor = floor;
            this.trim = trim;
            this.light = light;
        }
    }

    /** One floor of a delve, where its crew appear. */
    public record Floor(String name, int x1, int y1, int z1, int x2, int y2, int z2, boolean boss) {
        boolean contains(double x, double y, double z) {
            return x >= x1 && x <= x2 + 1 && z >= z1 && z <= z2 + 1 && y >= y1 - 0.5 && y <= y2 + 1;
        }
    }

    public static final class Delve {
        public final int index;
        public final String name;
        public final Kind kind;
        public final Look look;
        public final Archipelago.Island island;
        public final String bossName;
        final Supplier<EntityType<? extends MobEntity>> bossType;
        final List<Supplier<EntityType<? extends MobEntity>>> crew;
        public int x, z, baseY;
        public final List<Floor> floors = new ArrayList<>();

        Delve(int index, String name, Kind kind, Look look, Archipelago.Island island, String bossName,
              Supplier<EntityType<? extends MobEntity>> bossType, List<Supplier<EntityType<? extends MobEntity>>> crew) {
            this.index = index;
            this.name = name;
            this.kind = kind;
            this.look = look;
            this.island = island;
            this.bossName = bossName;
            this.bossType = bossType;
            this.crew = crew;
        }

        public int level() {
            return Math.min(50, Levels.band(island)[1] + 2);
        }
    }

    public static final List<Delve> ALL = List.of(
            new Delve(0, "The Weeping Duchess", Kind.WRECK, Look.WRECK_DARK, Archipelago.SUNKEN_REACH, "Captain Saltbones",
                    () -> EntityType.WITHER_SKELETON, List.of(() -> EntityType.DROWNED, () -> ModEntities.CURSED_PIRATE, () -> EntityType.SKELETON)),
            new Delve(1, "The Gilded Gull", Kind.WRECK, Look.WRECK_PALE, Archipelago.MARINERS_REST, "The Barnacle Bride",
                    () -> EntityType.DROWNED, List.of(() -> EntityType.DROWNED, () -> EntityType.DROWNED, () -> ModEntities.CURSED_PIRATE)),
            new Delve(2, "The Corsair's Lament", Kind.WRECK, Look.WRECK_DARK, Archipelago.BLACKTHORN_ISLE, "Bosun Grimmet Saltbones",
                    () -> EntityType.WITHER_SKELETON, List.of(() -> ModEntities.CURSED_PIRATE, () -> EntityType.SKELETON, () -> EntityType.STRAY)),
            new Delve(3, "The Mary Clare", Kind.WRECK, Look.WRECK_PALE, Archipelago.CROOKED_REEF, "The Barnacle Bride's Sister",
                    () -> EntityType.DROWNED, List.of(() -> EntityType.DROWNED, () -> EntityType.GUARDIAN, () -> ModEntities.CURSED_PIRATE)),
            new Delve(4, "The Gallows Spire", Kind.SPIRE, Look.GALLOWS, Archipelago.BLOODWAKE_CAY, "The Hangman of Bloodwake",
                    () -> EntityType.VINDICATOR, List.of(() -> EntityType.HUSK, () -> EntityType.SKELETON, () -> EntityType.PILLAGER)),
            new Delve(5, "Witchlight Tower", Kind.SPIRE, Look.WITCHLIGHT, Archipelago.MIREFEN_BAYOU, "The Witchlight Hag",
                    () -> EntityType.WITCH, List.of(() -> EntityType.BOGGED, () -> EntityType.SPIDER, () -> EntityType.WITCH)),
            new Delve(6, "The Old Signal Tower", Kind.SPIRE, Look.SIGNAL, Archipelago.IRONWATCH_ISLE, "The Signal Keeper",
                    () -> EntityType.STRAY, List.of(() -> EntityType.STRAY, () -> EntityType.SKELETON, () -> EntityType.ZOMBIE))
    );

    // Wreck layout, along x: decks' floors (the hold sits on the sea floor) and the weather deck just above the water
    private static final int HALF_LEN = 21, HALF_BEAM = 6;
    private static final int[] WRECK_FLOORS = {0, 7, 13, 19};   // offsets from the keel
    private static final String[] WRECK_DECKS = {"the hold", "the gun deck", "the crew deck", "the captain's deck"};
    private static final int SPIRE_R = 6, SPIRE_STOREY = 7, SPIRE_STOREYS = 5;

    private static volatile boolean placed;

    /** Called near the end of Builds' setup: find each delve a place and lay it out. */
    public static synchronized void addBuilds(List<Builds.Build> out) {
        if (placed) return;
        for (Delve d : ALL) {
            if (d.kind == Kind.WRECK) placeWreck(d, out);
            else placeSpire(d, out);
            CursedTides.LOGGER.info("Delve {}: {} at {} {} {}", d.name, d.kind, d.x, d.baseY, d.z);
        }
        placed = true;
    }

    // ------------------------------------------------------------------ wrecks

    private static boolean openRect(int x1, int z1, int x2, int z2) {
        for (int x = x1; x <= x2; x += 3) for (int z = z1; z <= z2; z += 3) {
            if (Archipelago.column(x, z).island() != null || Builds.blocksPlants(x, z)) return false;
        }
        return Archipelago.column(x2, z2).island() == null;
    }

    private static void placeWreck(Delve d, List<Builds.Build> out) {
        Archipelago.Island is = d.island;
        int bx = is.cx(), bz = is.cz() + is.radius() + 40;
        search:
        for (int r = is.radius() + 30; r < is.radius() + 200; r += 6) {
            for (int k = 0; k < 24; k++) {
                double a = Math.PI / 2 + k * Math.PI * 2 / 24 * ((k % 2 == 0) ? 1 : -1) * 0.5 + (k / 2) * 0.26;
                int x = is.cx() + (int) (Math.cos(a) * r), z = is.cz() + (int) (Math.sin(a) * r);
                if (is.edgeDistance(x, z) < 25) continue;
                if (openRect(x - HALF_LEN - 3, z - HALF_BEAM - 3, x + HALF_LEN + 3, z + HALF_BEAM + 3)) { bx = x; bz = z; break search; }
            }
        }
        d.x = bx;
        d.z = bz;
        d.baseY = Archipelago.SEA_FLOOR + 1;
        int keel = d.baseY;
        for (int i = 0; i < WRECK_FLOORS.length; i++) {
            int fy = keel + WRECK_FLOORS[i] + 1;
            int top = (i + 1 < WRECK_FLOORS.length ? keel + WRECK_FLOORS[i + 1] : keel + 24) - 1;
            d.floors.add(new Floor(WRECK_DECKS[i], bx - HALF_LEN + 1, fy, bz - HALF_BEAM + 1, bx + HALF_LEN - 1, top, bz + HALF_BEAM - 1, i == 0));
        }
        out.add(new Builds.Build(bx - HALF_LEN - 2, bz - HALF_BEAM - 2, bx + HALF_LEN + 2, bz + HALF_BEAM + 2, false, c -> wreck(c, d)));
    }

    private static int halfBeam(int dx) {
        if (dx > 10) return Math.max(0, HALF_BEAM - (int) Math.round((dx - 10) * 0.55));
        if (dx < -18) return HALF_BEAM - 1;
        return HALF_BEAM;
    }

    private static boolean inHull(int dx, int dz) {
        return dx >= -HALF_LEN && dx <= HALF_LEN && Math.abs(dz) <= halfBeam(dx);
    }

    private static BlockState st(Block b) { return b.getDefaultState(); }

    private static BlockState light(Look L) {
        BlockState s = st(L.light);
        return s.getBlock() instanceof LanternBlock ? s.with(LanternBlock.HANGING, true) : s;
    }

    private static void wreck(Canvas c, Delve d) {
        int x0 = d.x, z0 = d.z, keel = d.baseY;
        int deckTop = keel + 24; // the weather deck, one above the water
        Look L = d.look;
        for (int dx = -HALF_LEN; dx <= HALF_LEN; dx++) {
            for (int dz = -HALF_BEAM; dz <= HALF_BEAM; dz++) {
                if (!inHull(dx, dz)) continue;
                boolean edge = !inHull(dx + 1, dz) || !inHull(dx - 1, dz) || !inHull(dx, dz + 1) || !inHull(dx, dz - 1);
                int x = x0 + dx, z = z0 + dz;
                for (int y = keel - 1; y <= deckTop + 1; y++) {
                    int off = y - keel;
                    BlockState s;
                    if (y == keel - 1) s = st(L.wall);
                    else if (edge) {
                        if (y == deckTop + 1) s = st(Blocks.DARK_OAK_FENCE);
                        else {
                            double n = Noise.rand(x, y, z, 7101);
                            s = y < Archipelago.WATER_TOP - 3 && n < 0.12 ? st(Blocks.TUBE_CORAL_BLOCK)
                                    : y < Archipelago.WATER_TOP && n < 0.22 ? st(Blocks.MOSSY_COBBLESTONE)
                                    : (y - keel) % 6 == 3 ? st(L.trim) : st(L.wall);
                        }
                    } else if (y == deckTop) s = st(L.floor);
                    else if (y == deckTop + 1) s = st(Blocks.AIR);
                    else if (off == WRECK_FLOORS[1] || off == WRECK_FLOORS[2] || off == WRECK_FLOORS[3] || off == 0) s = st(L.floor);
                    else s = st(Blocks.AIR);
                    c.set(x, y, z, s);
                }
            }
        }
        // Hatches and ladders: weather deck to captain's deck, and down deck by deck
        int[][] hatches = {{6, WRECK_FLOORS[3], 24}, {-6, WRECK_FLOORS[2], WRECK_FLOORS[3]}, {9, WRECK_FLOORS[1], WRECK_FLOORS[2]}, {-12, WRECK_FLOORS[0], WRECK_FLOORS[1]}};
        BlockState ladder = st(Blocks.LADDER).with(LadderBlock.FACING, Direction.NORTH);
        for (int[] h : hatches) {
            int x = x0 + h[0];
            for (int y = keel + h[1] + 1; y <= keel + h[2]; y++) {
                c.set(x, y, z0 + 1, st(L.trim));
                c.set(x, y, z0, ladder);
            }
        }
        // Inside each deck: lanterns, cobwebs, barrels, guns along the gun deck, chests in the hold
        for (int i = 0; i < WRECK_FLOORS.length; i++) {
            int fy = keel + WRECK_FLOORS[i] + 1;
            int ceil = (i + 1 < WRECK_FLOORS.length ? keel + WRECK_FLOORS[i + 1] : deckTop) - 1;
            for (int dx = -HALF_LEN + 3; dx <= HALF_LEN - 6; dx += 6) {
                c.set(x0 + dx, ceil, z0 - 2, light(L));
                if (Noise.rand(x0 + dx, fy, z0, 7202) < 0.6) c.set(x0 + dx + 2, ceil, z0 + 3, st(Blocks.COBWEB));
                if (Noise.rand(x0 + dx, fy, z0, 7203) < 0.5) c.set(x0 + dx + 1, fy, z0 + halfBeam(dx + 1) - 1, st(Blocks.BARREL));
            }
            if (i == 1) {
                for (int dx = -15; dx <= 9; dx += 4) {
                    for (int side : new int[]{-1, 1}) {
                        int zz = z0 + side * (halfBeam(dx) - 1);
                        c.set(x0 + dx, fy, zz, st(Blocks.POLISHED_BLACKSTONE));
                        c.set(x0 + dx, fy, zz - side, st(Blocks.POLISHED_BLACKSTONE_WALL));
                    }
                }
            }
            if (i == 0) {
                c.chest(x0 - 18, fy, z0 - 3, Direction.EAST, LootTables.SHIPWRECK_TREASURE_CHEST);
                c.chest(x0 + 12, fy, z0 + 3, Direction.WEST, LootTables.SHIPWRECK_SUPPLY_CHEST);
                for (int dx = -8; dx <= 4; dx += 3) c.set(x0 + dx, fy, z0 - 4, st(Blocks.BARREL));
            }
            if (i == 3) {
                // The captain's cabin aft: a table, a chart, the captain's sea chest
                c.set(x0 - 17, fy, z0, st(Blocks.DARK_OAK_PLANKS));
                c.set(x0 - 17, fy + 1, z0, st(Blocks.CANDLE).with(net.minecraft.block.CandleBlock.LIT, true));
                c.chest(x0 - 19, fy, z0 + 2, Direction.EAST, LootTables.SHIPWRECK_MAP_CHEST);
                c.set(x0 - 15, fy, z0 - 3, st(Blocks.RED_CARPET));
                c.set(x0 - 16, fy, z0 - 3, st(Blocks.RED_CARPET));
            }
        }
        // The weather deck: broken masts and rags of sail, a ladder down the side to the water
        int[] masts = {-10, 2, 13};
        for (int k = 0; k < masts.length; k++) {
            int h = 3 + (int) (Noise.rand(x0, k, z0, 7301) * 9);
            for (int y = deckTop + 1; y <= deckTop + h; y++) c.set(x0 + masts[k], y, z0, st(Blocks.STRIPPED_DARK_OAK_LOG));
            if (h > 6) for (int y = deckTop + 3; y < deckTop + h - 1; y++) c.set(x0 + masts[k] + 1, y, z0 - 1, st(y % 2 == 0 ? Blocks.LIGHT_GRAY_WOOL : Blocks.GRAY_WOOL));
        }
        BlockState sideLadder = st(Blocks.LADDER).with(LadderBlock.FACING, Direction.SOUTH);
        for (int y = Archipelago.WATER_TOP - 1; y <= deckTop + 1; y++) c.set(x0, y, z0 + HALF_BEAM + 1, sideLadder);
        c.set(x0, deckTop + 1, z0 + HALF_BEAM, st(Blocks.AIR));
    }

    // ------------------------------------------------------------------ spires

    private static int slope(int x, int z, int r) {
        int lo = Integer.MAX_VALUE, hi = Integer.MIN_VALUE;
        for (int dx = -r; dx <= r; dx += 3) for (int dz = -r; dz <= r; dz += 3) {
            Archipelago.Column col = Archipelago.column(x + dx, z + dz);
            if (!col.isLand() || col.top() < Archipelago.WATER_TOP + 2) return -1;
            lo = Math.min(lo, col.top());
            hi = Math.max(hi, col.top());
        }
        return hi - lo;
    }

    private static void placeSpire(Delve d, List<Builds.Build> out) {
        Archipelago.Island is = d.island;
        int ccx = is.hasLobe() ? is.lobeX() : is.cx(), ccz = is.hasLobe() ? is.lobeZ() : is.cz();
        int maxR = is.hasLobe() ? is.lobeR() : is.radius();
        int[] best = null;
        int bestSlope = Integer.MAX_VALUE;
        for (int r = 10; r < maxR * 0.85; r += 5) {
            for (int k = 0; k < 24; k++) {
                double a = k * Math.PI * 2 / 24 + r * 0.31;
                int x = ccx + (int) (Math.cos(a) * r), z = ccz + (int) (Math.sin(a) * r);
                if (Math.hypot(x - is.cx(), z - is.cz()) < is.radius() * 0.7) continue; // keep clear of the town
                boolean claimed = false;
                for (int dx = -12; dx <= 12 && !claimed; dx += 6)
                    for (int dz = -12; dz <= 12 && !claimed; dz += 6)
                        if (Builds.blocksPlants(x + dx, z + dz)) claimed = true;
                if (claimed) continue;
                int sl = slope(x, z, SPIRE_R + 2);
                if (sl >= 0 && sl < bestSlope) { bestSlope = sl; best = new int[]{x, z}; }
            }
            if (best != null && bestSlope <= 3) break;
        }
        if (best == null) best = new int[]{ccx, ccz};
        d.x = best[0];
        d.z = best[1];
        int ground = Integer.MIN_VALUE;
        for (int dx = -SPIRE_R; dx <= SPIRE_R; dx += 2) for (int dz = -SPIRE_R; dz <= SPIRE_R; dz += 2) ground = Math.max(ground, Builds.groundY(d.x + dx, d.z + dz));
        d.baseY = ground;
        for (int i = 0; i < SPIRE_STOREYS; i++) {
            int fy = ground + i * SPIRE_STOREY;
            d.floors.add(new Floor(i == 0 ? "the ground floor" : "floor " + (i + 1), d.x - SPIRE_R + 1, fy, d.z - SPIRE_R + 1, d.x + SPIRE_R - 1, fy + SPIRE_STOREY - 2, d.z + SPIRE_R - 1, false));
        }
        int roof = ground + SPIRE_STOREYS * SPIRE_STOREY - 1;
        d.floors.add(new Floor("the roof", d.x - SPIRE_R, roof + 1, d.z - SPIRE_R, d.x + SPIRE_R, roof + 6, d.z + SPIRE_R, true));
        final int[] at = best;
        out.add(new Builds.Build(at[0] - SPIRE_R - 3, at[1] - SPIRE_R - 3, at[0] + SPIRE_R + 3, at[1] + SPIRE_R + 3, true, c -> spire(c, d)));
    }

    private static boolean inTower(int dx, int dz) {
        return Math.abs(dx) <= SPIRE_R && Math.abs(dz) <= SPIRE_R && Math.abs(dx) + Math.abs(dz) <= SPIRE_R * 2 - 3;
    }

    private static void spire(Canvas c, Delve d) {
        int x0 = d.x, z0 = d.z, g = d.baseY;
        Look L = d.look;
        int roof = g + SPIRE_STOREYS * SPIRE_STOREY - 1;
        c.foundation(x0 - SPIRE_R, z0 - SPIRE_R, x0 + SPIRE_R, z0 + SPIRE_R, g, st(L.wall));
        for (int dx = -SPIRE_R; dx <= SPIRE_R; dx++) {
            for (int dz = -SPIRE_R; dz <= SPIRE_R; dz++) {
                if (!inTower(dx, dz)) continue;
                boolean edge = !inTower(dx + 1, dz) || !inTower(dx - 1, dz) || !inTower(dx, dz + 1) || !inTower(dx, dz - 1);
                int x = x0 + dx, z = z0 + dz;
                for (int y = g - 1; y <= roof + 1; y++) {
                    int k = y - g;
                    BlockState s;
                    if (y == g - 1 || (k % SPIRE_STOREY == SPIRE_STOREY - 1 && y < roof) || y == roof) s = st(edge ? L.trim : L.floor);
                    else if (y == roof + 1) s = edge && (dx + dz) % 2 == 0 ? st(L.wall) : st(Blocks.AIR);
                    else if (edge) {
                        double n = Noise.rand(x, y, z, 7401);
                        boolean window = (dx == 0 || dz == 0) && k % SPIRE_STOREY == 3;
                        s = window ? st(Blocks.IRON_BARS) : n < 0.1 ? st(L.trim) : st(L.wall);
                    } else s = st(Blocks.AIR);
                    c.set(x, y, z, s);
                }
            }
        }
        // Battlements on the roof
        for (int dx = -SPIRE_R; dx <= SPIRE_R; dx++) for (int dz = -SPIRE_R; dz <= SPIRE_R; dz++) {
            if (!inTower(dx, dz)) continue;
            boolean edge = !inTower(dx + 1, dz) || !inTower(dx - 1, dz) || !inTower(dx, dz + 1) || !inTower(dx, dz - 1);
            if (edge && (dx + dz) % 2 == 0) c.set(x0 + dx, roof + 2, z0 + dz, st(L.wall));
        }
        // A door on the south side
        c.set(x0, g, z0 + SPIRE_R, st(Blocks.AIR));
        c.set(x0, g + 1, z0 + SPIRE_R, st(Blocks.AIR));
        // Ladders up through the floors, alternating walls
        for (int i = 0; i < SPIRE_STOREYS; i++) {
            int y1 = g + i * SPIRE_STOREY, y2 = y1 + SPIRE_STOREY - 1;
            boolean east = i % 2 == 0;
            int lx = east ? x0 + SPIRE_R - 1 : x0 - SPIRE_R + 1;
            Direction face = east ? Direction.WEST : Direction.EAST;
            for (int y = y1; y <= y2; y++) c.set(lx, y, z0 - 2, st(Blocks.LADDER).with(LadderBlock.FACING, face));
        }
        // Lights and furnishings on each floor
        for (int i = 0; i < SPIRE_STOREYS; i++) {
            int fy = g + i * SPIRE_STOREY;
            c.set(x0, fy + SPIRE_STOREY - 2, z0, light(L));
            switch (d.look) {
                case GALLOWS -> { c.set(x0 + 2, fy, z0 + 2, st(Blocks.HAY_BLOCK)); for (int y = fy + 2; y < fy + SPIRE_STOREY - 1; y++) c.set(x0 - 2, y, z0 + 1, st(Blocks.CHAIN)); }
                case WITCHLIGHT -> { c.set(x0 + 2, fy, z0 + 2, st(Blocks.CAULDRON)); c.set(x0 - 3, fy, z0 + 3, st(Blocks.BREWING_STAND)); }
                default -> { c.set(x0 + 3, fy, z0 + 3, st(Blocks.BARREL)); c.set(x0 - 3, fy, z0 + 3, st(Blocks.BARREL)); c.set(x0 - 3, fy + 1, z0 + 3, st(Blocks.BARREL)); }
            }
        }
        c.chest(x0 - 3, roof + 1, z0 - 3, Direction.SOUTH, LootTables.SIMPLE_DUNGEON_CHEST);
        // The signal fire on top
        c.set(x0, roof + 1, z0, st(d.look == Look.WITCHLIGHT ? Blocks.SOUL_CAMPFIRE : Blocks.CAMPFIRE));
    }

    // ------------------------------------------------------------------ crews and bosses

    public static final AttachmentType<Integer> CLEARED = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT).copyOnDeath().initializer(() -> 0).buildAndRegister(CursedTides.id("delves_cleared"));
    private static final String TAG = "ct_delve:", BOSS = "ct_delve_boss:";
    private static final Map<String, Long> LAST_SPAWN = new HashMap<>();
    private static final Map<UUID, Integer> SEEN = new HashMap<>();
    private static final long RESPAWN = 20L * 60 * 8, BOSS_RESPAWN = 20L * 60 * 20;

    public static void register() {
        CLEARED.identifier();
        ServerTickEvents.END_SERVER_TICK.register(Delves::tick);
        ServerLivingEntityEvents.AFTER_DEATH.register(Delves::onDeath);
    }

    private static void tick(MinecraftServer server) {
        if (server.getTicks() % 20 != 11 || !com.cursedtides.world.ModWorld.isArchipelago() || !placed) return;
        ServerWorld w = server.getOverworld();
        long now = w.getTime();
        for (ServerPlayerEntity p : w.getPlayers()) {
            for (Delve d : ALL) {
                double dist = Math.hypot(p.getX() - d.x, p.getZ() - d.z);
                if (dist > 60) continue;
                if (dist < 40 && SEEN.getOrDefault(p.getUuid(), -1) != d.index) {
                    SEEN.put(p.getUuid(), d.index);
                    int lv = d.level();
                    p.networkHandler.sendPacket(new TitleFadeS2CPacket(10, 50, 20));
                    p.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal((d.kind == Kind.WRECK ? "A wreck" : "A ruined spire")
                            + " · Lv " + (lv - 2) + "-" + (lv + 2)).formatted(Formatting.GRAY)));
                    p.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(d.name).formatted(Formatting.GOLD)));
                }
                for (int i = 0; i < d.floors.size(); i++) {
                    Floor f = d.floors.get(i);
                    if (!f.contains(p.getX(), p.getY(), p.getZ())) continue;
                    String key = d.index + ":" + i;
                    if (now - LAST_SPAWN.getOrDefault(key, -RESPAWN * 4) < (f.boss() ? BOSS_RESPAWN : RESPAWN)) continue;
                    String tag = TAG + key;
                    Box box = new Box(f.x1() - 2, f.y1() - 2, f.z1() - 2, f.x2() + 3, f.y2() + 3, f.z2() + 3);
                    if (!w.getEntitiesByClass(MobEntity.class, box, m -> m.getCommandTags().contains(tag)).isEmpty()) continue;
                    LAST_SPAWN.put(key, now);
                    populate(w, d, f, tag, p);
                }
            }
        }
    }

    private static void populate(ServerWorld w, Delve d, Floor f, String tag, ServerPlayerEntity p) {
        int level = d.level();
        int count = f.boss() ? 3 : 4 + w.random.nextInt(3);
        for (int i = 0; i < count; i++) {
            BlockPos at = spot(w, f, p);
            if (at == null) continue;
            EntityType<? extends MobEntity> type = d.crew.get(w.random.nextInt(d.crew.size())).get();
            mob(w, type, at, level - 2 + w.random.nextInt(4), tag, null);
        }
        if (f.boss()) {
            BlockPos at = spot(w, f, p);
            if (at == null) at = BlockPos.ofFloored(p.getPos());
            MobEntity boss = mob(w, d.bossType.get(), at, level + 3, tag, d.bossName);
            if (boss != null) {
                boss.addCommandTag(BOSS + d.index);
                boss.addCommandTag("cursedtides_boss");
                EntityAttributeInstance hp = boss.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
                if (hp != null) { hp.setBaseValue(hp.getBaseValue() * 6 * Levels.healthFactor(level + 3)); boss.setHealth(boss.getMaxHealth()); }
                EntityAttributeInstance size = boss.getAttributeInstance(EntityAttributes.GENERIC_SCALE);
                if (size != null) size.setBaseValue(1.2);
                boss.equipStack(EquipmentSlot.MAINHAND, new ItemStack(d.kind == Kind.WRECK ? com.cursedtides.item.ModItems.CUTLASS : net.minecraft.item.Items.IRON_AXE));
                boss.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0f);
                p.sendMessage(Text.literal(d.bossName).formatted(Formatting.RED, Formatting.BOLD)
                        .append(Text.literal(" waits on " + f.name() + ".").formatted(Formatting.GRAY)), false);
                w.playSound(null, at, SoundEvents.ENTITY_WITHER_SKELETON_AMBIENT, SoundCategory.HOSTILE, 2f, 0.6f);
            }
        }
    }

    private static BlockPos spot(ServerWorld w, Floor f, ServerPlayerEntity p) {
        for (int k = 0; k < 20; k++) {
            int x = f.x1() + w.random.nextInt(f.x2() - f.x1() + 1);
            int z = f.z1() + w.random.nextInt(f.z2() - f.z1() + 1);
            BlockPos at = new BlockPos(x, f.y1(), z);
            if (p.squaredDistanceTo(x, f.y1(), z) < 16) continue;
            if (w.getBlockState(at).isAir() && w.getBlockState(at.up()).isAir() && !w.getBlockState(at.down()).isAir()) return at;
        }
        return null;
    }

    private static MobEntity mob(ServerWorld w, EntityType<? extends MobEntity> type, BlockPos at, int level, String tag, String name) {
        MobEntity m = type.create(w);
        if (m == null) return null;
        m.refreshPositionAndAngles(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, w.random.nextFloat() * 360, 0);
        m.initialize(w, w.getLocalDifficulty(at), SpawnReason.EVENT, null);
        if (name != null) {
            m.setCustomName(Text.literal(name).formatted(Formatting.GOLD, Formatting.BOLD));
            m.setCustomNameVisible(true);
        }
        Levels.apply(m, Math.max(1, level));
        m.addCommandTag(tag);
        m.setPersistent();
        w.spawnEntity(m);
        return m;
    }

    private static void onDeath(LivingEntity victim, DamageSource source) {
        for (String t : victim.getCommandTags()) {
            if (!t.startsWith(BOSS)) continue;
            Delve d = ALL.get(Integer.parseInt(t.substring(BOSS.length())));
            if (!(victim.getWorld() instanceof ServerWorld w)) return;
            for (ServerPlayerEntity p : w.getPlayers(q -> q.squaredDistanceTo(victim) < 40 * 40)) {
                int cleared = p.getAttachedOrElse(CLEARED, 0);
                boolean first = (cleared & (1 << d.index)) == 0;
                p.setAttached(CLEARED, cleared | (1 << d.index));
                com.cursedtides.quest.QuestData.give(p, com.cursedtides.combat.Loot.container(com.cursedtides.combat.Loot.Tier.SKULL, d.level()));
                com.cursedtides.trade.Wallet.give(p, (first ? 60 : 20) * com.cursedtides.trade.Wallet.SILVER_PER_DOUBLOON);
                com.cursedtides.pirate.PirateData.addXp(p, com.cursedtides.pirate.Weapon.CUTLASS, first ? 200 : 60);
                p.sendMessage(Text.literal(d.name + " is cleared" + (first ? " for the first time!" : ".")).formatted(Formatting.GOLD, Formatting.BOLD), false);
                com.cursedtides.activity.Achievements.grant(p, d.kind == Kind.WRECK ? "wreck_diver" : "spire_climber");
                if (Integer.bitCount(p.getAttachedOrElse(CLEARED, 0)) >= ALL.size()) com.cursedtides.activity.Achievements.grant(p, "all_delves");
            }
            return;
        }
    }

    public static List<String> journal(ServerPlayerEntity p) {
        int cleared = p.getAttachedOrElse(CLEARED, 0);
        List<String> lines = new ArrayList<>();
        lines.add("§fWrecks and spires cleared: " + Integer.bitCount(cleared) + "/" + ALL.size());
        for (Delve d : ALL) {
            boolean done = (cleared & (1 << d.index)) != 0;
            lines.add((done ? "§a✔ " : "§7· ") + d.name + " §8(" + (d.kind == Kind.WRECK ? "wreck off " : "spire on ") + d.island.name() + ", Lv " + d.level() + ")");
        }
        return lines;
    }

    public static Delve byName(String name) {
        for (Delve d : ALL) if (d.name.equals(name)) return d;
        return null;
    }
}
