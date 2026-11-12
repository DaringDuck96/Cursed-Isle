package com.cursedtides.dungeon;

import com.cursedtides.CursedTides;
import com.cursedtides.world.Canvas;
import com.cursedtides.world.Noise;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LanternBlock;
import net.minecraft.block.PointedDripstoneBlock;
import net.minecraft.block.enums.Thickness;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;

/**
 * A grand cavern, deep under an island: a chain of huge irregular chambers (domed, pillared, with stalagmites,
 * pools, a chasm spanned by a rope bridge) joined by winding tunnels, ending in a boss arena. Players climb down
 * an old rope from a cave mouth on the surface. Everything is computed from the layout, so each chunk carves its
 * own part independently.
 */
public class Cavern {
    public enum Kind { ENTRY, HALL, CHASM, POOL, GROTTO, ARENA }

    public record Chamber(int cx, int cz, int floor, int rx, int rz, int h, Kind kind, int pillars) {}

    record Sample(double x, double z, int floor, double r) {}

    static final RegistryKey<LootTable> LOOT = RegistryKey.of(RegistryKeys.LOOT_TABLE, CursedTides.id("chests/dungeon"));
    static final RegistryKey<LootTable> VAULT = RegistryKey.of(RegistryKeys.LOOT_TABLE, CursedTides.id("chests/dungeon_vault"));

    public final int index;
    public final String name;
    public final CavernTheme theme;
    public final List<Chamber> chambers = new ArrayList<>();
    final List<List<Sample>> tunnels = new ArrayList<>();
    public int minX, minZ, maxX, maxZ;
    private final long seed;
    /** The cave mouth on the surface (set once the island's buildings are known). */
    public int mouthX, mouthZ, mouthY;

    /** The chamber chain curls one way (variant's sign) from a heading set by the variant, so it can be steered clear of others. */
    private final int variant;

    Cavern(int index, String name, int x, int z, CavernTheme theme, int chamberCount, int variant) {
        this.index = index;
        this.name = name;
        this.theme = theme;
        this.variant = variant;
        this.seed = Noise.hash(x, index, z, 7777);
        this.mouthX = x;
        this.mouthZ = z;
        layout(x, z, chamberCount);
    }

    boolean overlaps(Cavern o, int margin) {
        return minX - margin <= o.maxX && maxX + margin >= o.minX && minZ - margin <= o.maxZ && maxZ + margin >= o.minZ;
    }

    public boolean contains(BlockPos p) {
        return p.getX() >= minX && p.getX() <= maxX && p.getZ() >= minZ && p.getZ() <= maxZ && p.getY() < 2 && p.getY() > -62;
    }

    /** The chamber a position is in (or null in the tunnels). */
    public Chamber chamberAt(BlockPos p) {
        for (Chamber ch : chambers) {
            if (reach(ch, p.getX(), p.getZ()) < 1.0 && p.getY() >= ch.floor() - 13 && p.getY() <= ch.floor() + ch.h()) return ch;
        }
        return null;
    }

    public double reachOf(Chamber ch, int x, int z) {
        return reach(ch, x, z);
    }

    /** Somewhere a mob could stand in a chamber, or null. */
    public BlockPos standable(Chamber ch, double out, double angle) {
        int x = ch.cx() + (int) (Math.cos(angle) * ch.rx() * out), z = ch.cz() + (int) (Math.sin(angle) * ch.rz() * out);
        if (reach(ch, x, z) >= 0.95 || inPillar(ch, x, z)) return null;
        if (ch.kind() == Kind.POOL && pool(ch, x, z)) return null;
        if (ch.kind() == Kind.CHASM && chasm(ch, x, z)) return null;
        return new BlockPos(x, floorAt(ch, x, z), z);
    }

    private double rnd(long a, long b) {
        return Noise.rand(a, b, 31, seed);
    }

    private void layout(int x0, int z0, int count) {
        double heading = rnd(1, 2) * Math.PI * 2 + variant * 1.3;
        double sign = (variant & 1) == 0 ? 1 : -1;
        double x = x0, z = z0;
        int floor = -38;
        for (int i = 0; i < count; i++) {
            Kind kind = i == 0 ? Kind.ENTRY : i == count - 1 ? Kind.ARENA : (i % 3 == 1 ? Kind.CHASM : i % 3 == 2 ? Kind.POOL : Kind.HALL);
            int rx = kind == Kind.ARENA ? 22 : kind == Kind.ENTRY ? 11 : 14 + (int) (rnd(i, 3) * 8);
            int rz = kind == Kind.ARENA ? 22 : kind == Kind.ENTRY ? 11 : 14 + (int) (rnd(i, 4) * 8);
            int h = kind == Kind.ARENA ? 24 : kind == Kind.ENTRY ? 12 : 14 + (int) (rnd(i, 5) * 9);
            int pillars = kind == Kind.ARENA ? 0 : kind == Kind.ENTRY ? 0 : 1 + (int) (rnd(i, 6) * 3);
            chambers.add(new Chamber((int) x, (int) z, floor, rx, rz, h, kind, pillars));
            // step to the next chamber: far enough for a proper tunnel, turning as it goes
            heading += sign * (0.35 + rnd(i, 7) * 0.4);
            double step = 46 + rnd(i, 8) * 14;
            x += Math.cos(heading) * step;
            z += Math.sin(heading) * step;
            floor = Math.max(-46, Math.min(-30, floor + (int) ((rnd(i, 9) - 0.5) * 10)));
        }
        // A treasure grotto off one of the middle halls
        if (count >= 4) {
            Chamber mid = chambers.get(1 + (int) (rnd(9, 9) * (count - 2)));
            double a = rnd(10, 10) * Math.PI * 2;
            chambers.add(new Chamber(mid.cx() + (int) (Math.cos(a) * 34), mid.cz() + (int) (Math.sin(a) * 34), mid.floor() + 2, 7, 7, 7, Kind.GROTTO, 0));
        }
        for (int i = 0; i + 1 < count; i++) tunnels.add(tunnel(chambers.get(i), chambers.get(i + 1), i));
        if (chambers.size() > count) {
            Chamber g = chambers.get(count);
            Chamber nearest = chambers.get(0);
            for (int i = 0; i < count; i++) {
                Chamber c = chambers.get(i);
                if (dist(c, g) < dist(nearest, g)) nearest = c;
            }
            tunnels.add(tunnel(nearest, g, 50));
        }
        minX = Integer.MAX_VALUE; minZ = Integer.MAX_VALUE; maxX = Integer.MIN_VALUE; maxZ = Integer.MIN_VALUE;
        for (Chamber c : chambers) {
            minX = Math.min(minX, c.cx() - c.rx() - 6); maxX = Math.max(maxX, c.cx() + c.rx() + 6);
            minZ = Math.min(minZ, c.cz() - c.rz() - 6); maxZ = Math.max(maxZ, c.cz() + c.rz() + 6);
        }
        for (List<Sample> t : tunnels) for (Sample s : t) {
            minX = Math.min(minX, (int) s.x() - 6); maxX = Math.max(maxX, (int) s.x() + 6);
            minZ = Math.min(minZ, (int) s.z() - 6); maxZ = Math.max(maxZ, (int) s.z() + 6);
        }
    }

    private static double dist(Chamber a, Chamber b) {
        return Math.hypot(a.cx() - b.cx(), a.cz() - b.cz());
    }

    /** A winding tube from one chamber to the next, its floor sloping between theirs. */
    private List<Sample> tunnel(Chamber a, Chamber b, int k) {
        List<Sample> out = new ArrayList<>();
        double dx = b.cx() - a.cx(), dz = b.cz() - a.cz();
        double len = Math.hypot(dx, dz);
        double px = -dz / len, pz = dx / len;
        double wiggle = 6 + rnd(k, 11) * 8, waves = 1 + (int) (rnd(k, 12) * 2);
        int n = (int) (len / 1.5);
        for (int i = 0; i <= n; i++) {
            double t = i / (double) n;
            double off = Math.sin(t * Math.PI * waves) * wiggle * Math.sin(t * Math.PI);
            int floor = (int) Math.round(a.floor() + (b.floor() - a.floor()) * t);
            double r = 2.6 + rnd(k * 1000 + i / 6, 13) * 1.2;
            out.add(new Sample(a.cx() + dx * t + px * off, a.cz() + dz * t + pz * off, floor, r));
        }
        return out;
    }

    public Chamber arena() {
        for (Chamber c : chambers) if (c.kind() == Kind.ARENA) return c;
        return chambers.get(chambers.size() - 1);
    }

    public Chamber entry() {
        return chambers.get(0);
    }

    // ------------------------------------------------------------------ shape

    /** How far into a chamber's footprint a column is: 0 at the centre, 1 at the wall (wobbly), or more outside. */
    private double reach(Chamber c, int x, int z) {
        double dx = (x - c.cx()) / (double) c.rx(), dz = (z - c.cz()) / (double) c.rz();
        double a = Math.atan2(dz, dx);
        double wob = 1 + (Noise.value(Math.cos(a) * 2 + c.cx() * 0.01, Math.sin(a) * 2 + c.cz() * 0.01, seed + c.cx()) - 0.5) * 0.45;
        return Math.sqrt(dx * dx + dz * dz) / wob;
    }

    private boolean inPillar(Chamber c, int x, int z) {
        for (int i = 0; i < c.pillars(); i++) {
            double a = rnd(c.cx() + i, 20) * Math.PI * 2, d = 0.35 + rnd(c.cz() + i, 21) * 0.3;
            double px = c.cx() + Math.cos(a) * c.rx() * d, pz = c.cz() + Math.sin(a) * c.rz() * d;
            double r = 2 + rnd(i, c.cx() + 22) * 1.5;
            if ((x - px) * (x - px) + (z - pz) * (z - pz) < r * r) return true;
        }
        return false;
    }

    // ------------------------------------------------------------------ carving

    public void place(Canvas c) {
        if (!c.intersects(minX, minZ, maxX, maxZ)) return;
        Random rng = Random.create(seed ^ (c.minX * 341873128712L + c.minZ * 132897987541L));
        c.columns(Math.max(c.minX, minX), Math.max(c.minZ, minZ), Math.min(c.maxX, maxX), Math.min(c.maxZ, maxZ), (x, z) -> column(c, x, z));
        decorate(c, rng);
    }

    private void column(Canvas c, int x, int z) {
        int lo = Integer.MAX_VALUE, hi = Integer.MIN_VALUE, shellLo = Integer.MAX_VALUE, shellHi = Integer.MIN_VALUE;
        Chamber inside = null;
        double insideReach = 9;
        for (Chamber ch : chambers) {
            double d = reach(ch, x, z);
            if (d < 1.0) {
                if (inPillar(ch, x, z)) { shellLo = Math.min(shellLo, ch.floor() - 2); shellHi = Math.max(shellHi, ch.floor() + ch.h()); continue; }
                double dome = Math.sqrt(Math.max(0, 1 - d * d));
                int f = floorAt(ch, x, z);
                int top = ch.floor() + (int) (ch.h() * (0.35 + 0.65 * dome) * (0.88 + 0.12 * Noise.value(x / 7.0, z / 7.0, seed + 4)));
                if (ch.kind() == Kind.POOL && pool(ch, x, z)) f -= 3;
                if (ch.kind() == Kind.CHASM && chasm(ch, x, z) && !bridge(ch, x, z)) f -= 12;
                lo = Math.min(lo, f);
                hi = Math.max(hi, top);
                if (d < insideReach) { insideReach = d; inside = ch; }
            } else if (d < 1.25) {
                shellLo = Math.min(shellLo, ch.floor() - 2);
                shellHi = Math.max(shellHi, ch.floor() + ch.h());
            }
        }
        for (List<Sample> t : tunnels) {
            for (Sample s : t) {
                double dx = x - s.x(), dz = z - s.z();
                double d2 = dx * dx + dz * dz;
                if (d2 < s.r() * s.r()) {
                    lo = Math.min(lo, s.floor());
                    hi = Math.max(hi, s.floor() + 4 + (int) ((s.r() - Math.sqrt(d2)) * 0.6));
                } else if (d2 < (s.r() + 2) * (s.r() + 2)) {
                    shellLo = Math.min(shellLo, s.floor() - 1);
                    shellHi = Math.max(shellHi, s.floor() + 6);
                }
            }
        }
        // Themed rock around the open space
        if (shellLo < shellHi) for (int y = shellLo; y <= shellHi; y++) c.set(x, y, z, theme.wall(x, y, z));
        if (lo > hi) return;
        for (int y = lo - 2; y < lo; y++) c.set(x, y, z, theme.floor(x, y, z));
        for (int y = lo; y <= hi; y++) c.set(x, y, z, Blocks.CAVE_AIR.getDefaultState());
        c.set(x, hi + 1, z, theme.wall(x, hi + 1, z));
        // Liquids in pools and at the bottom of the chasm
        if (inside != null && inside.kind() == Kind.POOL && pool(inside, x, z)) {
            for (int y = lo; y < inside.floor() - 1; y++) c.set(x, y, z, theme.liquid());
        }
        if (inside != null && inside.kind() == Kind.CHASM && chasm(inside, x, z)) {
            if (bridge(inside, x, z)) {
                c.set(x, inside.floor() - 1, z, Blocks.SPRUCE_PLANKS.getDefaultState());
                if (bridgeEdge(inside, x, z)) c.set(x, inside.floor(), z, Blocks.SPRUCE_FENCE.getDefaultState());
                for (int y = inside.floor() - 12; y < inside.floor() - 2; y++) c.set(x, y, z, Blocks.CAVE_AIR.getDefaultState());
                for (int y = inside.floor() - 12; y < inside.floor() - 9; y++) c.set(x, y, z, theme.liquid());
            } else {
                for (int y = lo; y < lo + 3; y++) c.set(x, y, z, theme.liquid());
            }
        }
        // Arena dais
        if (inside != null && inside.kind() == Kind.ARENA && reach(inside, x, z) < 0.24) c.set(x, lo - 1, z, theme.dais());
        // Bits of the cave that hang and rise
        double r = Noise.rand(x, z, seed + 5);
        if (r < theme.spikeChance() && lo + 4 < hi) spike(c, x, lo, z, true);
        else if (r > 1 - theme.spikeChance() && hi - 4 > lo) spike(c, x, hi, z, false);
        else if (r > 0.5 && r < 0.5 + theme.scatterChance()) c.set(x, lo, z, theme.scatter(x, z));
        double light = Noise.rand(x, z, seed + 6);
        if (light < 0.004 && hi - lo > 6) c.set(x, hi, z, theme.hangingLight());
        else if (light > 0.996) c.set(x, lo - 1, z, theme.floorLight());
    }

    /** Where you'd stand in this column of a chamber (its first open block). */
    public int floorAt(Chamber ch, int x, int z) {
        double d = reach(ch, x, z);
        int f = ch.floor() + (int) (Noise.value(x / 5.0, z / 5.0, seed + 3) * 2.2 * Math.min(1, d));
        if (ch.kind() == Kind.ARENA && d < 0.24) f += 1; // the boss's dais
        if (ch.kind() == Kind.CHASM && chasm(ch, x, z)) return ch.floor(); // the bridge runs level
        return f;
    }

    private boolean pool(Chamber ch, int x, int z) {
        double px = ch.cx() + ch.rx() * 0.25, pz = ch.cz() - ch.rz() * 0.2;
        double r = Math.min(ch.rx(), ch.rz()) * 0.45;
        return (x - px) * (x - px) + (z - pz) * (z - pz) < r * r;
    }

    /** The chasm runs across the chamber; the bridge runs along the way through. */
    private boolean chasm(Chamber ch, int x, int z) {
        double a = rnd(ch.cx(), 30) * Math.PI;
        double along = (x - ch.cx()) * Math.cos(a) + (z - ch.cz()) * Math.sin(a);
        return Math.abs(along) < 3.5;
    }

    private double across(Chamber ch, int x, int z) {
        double a = rnd(ch.cx(), 30) * Math.PI;
        return -(x - ch.cx()) * Math.sin(a) + (z - ch.cz()) * Math.cos(a);
    }

    private boolean bridge(Chamber ch, int x, int z) {
        return Math.abs(across(ch, x, z)) <= 1.6;
    }

    private boolean bridgeEdge(Chamber ch, int x, int z) {
        return Math.abs(across(ch, x, z)) > 0.9;
    }

    private void spike(Canvas c, int x, int base, int z, boolean up) {
        int n = 1 + (int) (Noise.rand(x, z, seed + 8) * 3);
        Direction dir = up ? Direction.UP : Direction.DOWN;
        for (int i = 0; i < n; i++) {
            int y = up ? base + i : base - i;
            BlockState s = theme.spikeBlock();
            if (s.isOf(Blocks.POINTED_DRIPSTONE)) {
                Thickness th = i == n - 1 ? Thickness.TIP : i == 0 && n > 2 ? Thickness.BASE : Thickness.MIDDLE;
                if (n == 1) th = Thickness.TIP;
                s = s.with(PointedDripstoneBlock.VERTICAL_DIRECTION, dir).with(PointedDripstoneBlock.THICKNESS, th);
            }
            c.set(x, y, z, s);
        }
    }

    // ------------------------------------------------------------------ furnishings

    private void decorate(Canvas c, Random rng) {
        for (int i = 0; i < chambers.size(); i++) {
            Chamber ch = chambers.get(i);
            switch (ch.kind()) {
                case ENTRY -> {
                    // The rope back up, and a camp left by the last party down here
                    BlockPos rope = ropeLanding();
                    for (int y = rope.getY(); y < rope.getY() + 6; y++) c.set(rope.getX(), y, rope.getZ(), CaveRopeHolder.rope());
                    c.set(ch.cx() + 3, floorAt(ch, ch.cx() + 3, ch.cz() + 2), ch.cz() + 2, Blocks.CAMPFIRE.getDefaultState());
                    c.set(ch.cx() + 5, floorAt(ch, ch.cx() + 5, ch.cz() + 2), ch.cz() + 2, Blocks.BARREL.getDefaultState());
                    c.set(ch.cx() + 4, floorAt(ch, ch.cx() + 4, ch.cz() + 4), ch.cz() + 4, Blocks.LANTERN.getDefaultState());
                }
                case HALL, CHASM, POOL -> {
                    spawner(c, ch, ch.cx() + ch.rx() / 3, ch.cz() - ch.rz() / 3, theme.mob(i));
                    if (ch.rx() > 16) spawner(c, ch, ch.cx() - ch.rx() / 3, ch.cz() + ch.rz() / 4, theme.mob(i + 1));
                    chest(c, ch, 0.7, LOOT);
                    if (!inPillar(ch, ch.cx() - 4, ch.cz() + 3) && !(ch.kind() == Kind.POOL && pool(ch, ch.cx() - 4, ch.cz() + 3))
                            && !(ch.kind() == Kind.CHASM && chasm(ch, ch.cx() - 4, ch.cz() + 3))) {
                        lampPost(c, ch.cx() - 4, floorAt(ch, ch.cx() - 4, ch.cz() + 3), ch.cz() + 3);
                    }
                }
                case GROTTO -> {
                    chest(c, ch, 0.0, VAULT, new ItemStack(com.cursedtides.item.ModItems.TREASURE_MAP));
                    chest(c, ch, 0.5, LOOT);
                    c.set(ch.cx(), floorAt(ch, ch.cx(), ch.cz()) + 5, ch.cz(), theme.hangingLight());
                }
                case ARENA -> {
                    for (int k = 0; k < 8; k++) {
                        double a = Math.PI * 2 * k / 8;
                        int px = ch.cx() + (int) Math.round(Math.cos(a) * 15), pz = ch.cz() + (int) Math.round(Math.sin(a) * 15);
                        int fy = floorAt(ch, px, pz);
                        c.fill(px, fy, pz, px, fy + 9, pz, theme.dais());
                        c.set(px, fy + 10, pz, theme.floorLight());
                    }
                    BlockPos behind = new BlockPos(ch.cx(), floorAt(ch, ch.cx(), ch.cz() - 4), ch.cz() - 4);
                    c.chest(behind.getX(), behind.getY(), behind.getZ(), Direction.SOUTH, VAULT,
                            new ItemStack(com.cursedtides.item.ModItems.ANCIENT_RELIC));
                }
            }
        }
    }

    public BlockPos ropeLanding() {
        Chamber e = entry();
        int x = e.cx() - e.rx() / 2, z = e.cz();
        return new BlockPos(x, floorAt(e, x, z), z);
    }

    private void lampPost(Canvas c, int x, int y, int z) {
        c.set(x, y, z, Blocks.SPRUCE_FENCE.getDefaultState());
        c.set(x, y + 1, z, Blocks.SPRUCE_FENCE.getDefaultState());
        c.set(x, y + 2, z, theme == CavernTheme.CURSED ? Blocks.SOUL_LANTERN.getDefaultState() : Blocks.LANTERN.getDefaultState());
    }

    private void chest(Canvas c, Chamber ch, double out, RegistryKey<LootTable> loot, ItemStack... items) {
        double a = rnd(ch.cx() + (int) (out * 10), 40) * Math.PI * 2;
        int x = ch.cx() + (int) (Math.cos(a) * ch.rx() * out * 0.8), z = ch.cz() + (int) (Math.sin(a) * ch.rz() * out * 0.8);
        if (!c.contains(x, z) || inPillar(ch, x, z) || (ch.kind() == Kind.POOL && pool(ch, x, z)) || (ch.kind() == Kind.CHASM && chasm(ch, x, z))) return;
        c.chest(x, floorAt(ch, x, z), z, Direction.NORTH, loot, items);
    }

    private void spawner(Canvas c, Chamber ch, int x, int z, EntityType<?> type) {
        if (!c.contains(x, z)) return;
        if (inPillar(ch, x, z) || (ch.kind() == Kind.POOL && pool(ch, x, z)) || (ch.kind() == Kind.CHASM && chasm(ch, x, z))) return;
        int y = floorAt(ch, x, z);
        c.set(x, y, z, Blocks.SPAWNER.getDefaultState());
        if (c.world().getBlockEntity(new BlockPos(x, y, z)) instanceof MobSpawnerBlockEntity spawner) {
            spawner.setEntityType(type, Random.create(Noise.hash(x, y, z, 404)));
        }
    }

    /** Indirection so the world generator doesn't load block classes before they're registered. */
    static final class CaveRopeHolder {
        static BlockState rope() { return com.cursedtides.block.ModBlocks.CAVE_ROPE.getDefaultState(); }
    }

    @SuppressWarnings("unused")
    private static Block b(Block b) { return b; }

    @SuppressWarnings("unused")
    private static BlockState hanging(Block lantern) { return lantern.getDefaultState().with(LanternBlock.HANGING, true); }
}
