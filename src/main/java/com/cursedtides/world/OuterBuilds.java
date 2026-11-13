package com.cursedtides.world;

import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.entity.EntityTypes;
import com.cursedtides.entity.ModEntities;
import com.cursedtides.entity.StoryNpcEntity.Role;
import com.cursedtides.item.ModItems;
import com.cursedtides.wildlife.Wildlife;
import java.util.List;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BellAttachType;

import static com.cursedtides.world.Builds.TAVERN;
import static com.cursedtides.world.Builds.TREASURE;
import static com.cursedtides.world.Builds.add;
import static com.cursedtides.world.Builds.floorAt;
import static com.cursedtides.world.Builds.s;

/** The fourteen islands of the outer reaches, and everything built on them. */
final class OuterBuilds {
    private OuterBuilds() {}

    static final String BOSS_TAG = "cursedtides_boss";

    // Island centres
    private static final int KX = Archipelago.KINGSPORT.cx(), KZ = Archipelago.KINGSPORT.cz();
    private static final int CWX = Archipelago.CROWNWATCH.cx(), CWZ = Archipelago.CROWNWATCH.cz();
    private static final int VX = Archipelago.SAINT_VESPER.cx(), VZ = Archipelago.SAINT_VESPER.cz();
    private static final int MX = Archipelago.MARINERS_REST.cx(), MZ = Archipelago.MARINERS_REST.cz();
    private static final int WX = Archipelago.WIDOWS_ROCK.cx(), WZ = Archipelago.WIDOWS_ROCK.cz();
    private static final int SHX = Archipelago.SERPENTS_HOLLOW.cx(), SHZ = Archipelago.SERPENTS_HOLLOW.cz();
    private static final int BWX = Archipelago.BLOODWAKE_CAY.cx(), BWZ = Archipelago.BLOODWAKE_CAY.cz();
    private static final int SBX = Archipelago.STORMBREAK_ISLE.cx(), SBZ = Archipelago.STORMBREAK_ISLE.cz();
    private static final int RX = Archipelago.RAVENS_LANDING.cx(), RZ = Archipelago.RAVENS_LANDING.cz();
    private static final int EFX = Archipelago.EMBERFALL.cx(), EFZ = Archipelago.EMBERFALL.cz();
    private static final int FKX = Archipelago.FORGOTTEN_KEYS.cx(), FKZ = Archipelago.FORGOTTEN_KEYS.cz();
    private static final int BVX = Archipelago.BLACKVEIL_ATOLL.cx(), BVZ = Archipelago.BLACKVEIL_ATOLL.cz();
    private static final int LHX = Archipelago.LAST_HORIZON.cx(), LHZ = Archipelago.LAST_HORIZON.cz();
    private static final int CRX = Archipelago.CROOKED_REEF.cx(), CRZ = Archipelago.CROOKED_REEF.cz();

    /** The Stormbreak regatta course, relative to the island: eight buoys, weaving in and out. */
    public static final int[][] BUOYS = {{0, -80}, {41, -41}, {80, 0}, {41, 41}, {0, 80}, {-41, 41}, {-80, 0}, {-41, -41}};

    // Taverns share Saltmarrow's layout, so the card table sits in the same corner
    private static final int KT_X1 = KX + 12, KT_Z1 = KZ - 6;
    private static final int RT_X1 = RX + 8, RT_Z1 = RZ - 11;

    static final List<Builds.NpcSpot> NPC_SPOTS = List.of(
            new Builds.NpcSpot(Role.DEALER, KT_X1 + 6, KT_Z1 + 5, KT_X1 + 8.5, KT_Z1 + 4.5, 90),
            new Builds.NpcSpot(Role.GAMBLER, KT_X1 + 6, KT_Z1 + 5, KT_X1 + 4.5, KT_Z1 + 2.5, 180),
            new Builds.NpcSpot(Role.DEALER, RT_X1 + 6, RT_Z1 + 5, RT_X1 + 8.5, RT_Z1 + 4.5, 90),
            new Builds.NpcSpot(Role.GAMBLER, RT_X1 + 6, RT_Z1 + 5, RT_X1 + 4.5, RT_Z1 + 2.5, 180),
            new Builds.NpcSpot(Role.BROKER, RT_X1 + 6, RT_Z1 + 5, RT_X1 + 7.5, RT_Z1 + 8.5, 270),
            new Builds.NpcSpot(Role.BANKER, KX - 19, KZ - 2, KX - 18.5, KZ - 4.5, 0),
            new Builds.NpcSpot(Role.ADMIRAL, CWX, CWZ, CWX + 8.5, CWZ + 1.5, 90),
            new Builds.NpcSpot(Role.ABBESS, VX + 19, VZ, VX + 19.5, VZ + 0.5, 90),
            new Builds.NpcSpot(Role.RACEMASTER, SBX - 1, SBZ - 14, SBX - 0.5, SBZ - 13.5, 180),
            new Builds.NpcSpot(Role.WAYFARER, LHX - 20, LHZ + 12, LHX - 19.5, LHZ + 12.5, 0),
            new Builds.NpcSpot(Role.AUCTIONEER, KX, KZ - 23, KX + 3.5, KZ - 23.5, 180),
            new Builds.NpcSpot(Role.SMUGGLER_QUEEN, RX - 9, RZ - 12, RX - 8.5, RZ - 11.5, 90),
            new Builds.NpcSpot(Role.CONTEST_JUDGE, MX - 5, MZ - 3, MX - 4.5, MZ - 2.5, 180),
            new Builds.NpcSpot(Role.SCHOLAR, FKX, FKZ + 39, FKX + 0.5, FKZ + 39.5, 0)
    );

    static void addAll() {
        kingsport();
        crownwatch();
        saintVesper();
        marinersRest();
        widowsRock();
        serpentsHollow();
        bloodwake();
        stormbreak();
        ravensLanding();
        emberfall();
        forgottenKeys();
        blackveil();
        lastHorizon();
        crookedReef();
        goldwaterMarket();
    }

    /** Goldwater gets a cargo trader beside the mining camp. */
    private static void goldwaterMarket() {
        int gx = Archipelago.GOLDWATER_CAY.cx() - 14, gz = Archipelago.GOLDWATER_CAY.cz() + 10;
        add(gx - 2, gz - 2, gx + 2, gz + 3, c -> {
            int py = floorAt(gx, gz);
            Builds.stall(c, gx, gz, py - 1, s(Blocks.WOOL.pick(DyeColor.ORANGE)));
            vendor(c, gx + 0.5, py, gz + 2.5, "Trader Amos Kett");
        });
    }

    // =====================================================================
    // Shared helpers
    // =====================================================================

    private static void clear(Canvas c, int x1, int y1, int z1, int x2, int y2, int z2) {
        c.fill(x1, y1, z1, x2, y2, z2, s(Blocks.AIR));
    }

    /** A shopkeeper; their trade (in the name) decides what they sell. */
    private static void vendor(Canvas c, double x, int y, double z, String name) {
        c.spawn(x, y, z, 0, w -> {
            var qm = ModEntities.QUARTERMASTER.create(w, EntitySpawnReason.EVENT);
            if (qm != null) {
                qm.setCustomName(Component.literal(name));
                qm.setCustomNameVisible(true);
            }
            return qm;
        });
    }

    /** A named, persistent hostile that guards a place. */
    private static void guard(Canvas c, double x, int y, double z, Function<ServerLevel, Entity> factory, String name) {
        c.spawn(x, y, z, 0, factory, e -> {
            if (e instanceof Mob mob) {
                mob.setCustomName(Component.literal(name));
                mob.setPersistenceRequired();
            }
        });
    }

    /** A boss: more health, bigger, armoured, and it drops a relic and a hoard when it falls. */
    private static void boss(Canvas c, double x, int y, double z, Function<ServerLevel, Entity> factory, String name,
                             float health, double scale, ItemStack weapon) {
        c.spawn(x, y, z, 0, factory, e -> {
            if (!(e instanceof Mob mob)) return;
            mob.setCustomName(Component.literal(name).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            mob.setCustomNameVisible(true);
            mob.setPersistenceRequired();
            mob.addTag(BOSS_TAG);
            var hp = mob.getAttribute(Attributes.MAX_HEALTH);
            if (hp != null) hp.setBaseValue(health);
            mob.setHealth(health);
            var size = mob.getAttribute(Attributes.SCALE);
            if (size != null) size.setBaseValue(scale);
            var armor = mob.getAttribute(Attributes.ARMOR);
            if (armor != null) armor.setBaseValue(10);
            if (weapon != null) {
                mob.setItemSlot(EquipmentSlot.MAINHAND, weapon);
                mob.setDropChance(EquipmentSlot.MAINHAND, 0.0f);
            }
        });
    }

    private static void tent(Canvas c, int tx, int tz, BlockState cloth) {
        int base = Archipelago.column(tx, tz).top() + 1;
        for (int z = tz; z <= tz + 3; z++) {
            for (int dx = -2; dx <= 2; dx++) {
                c.set(tx + dx, base + (2 - Math.abs(dx)), z, cloth);
                for (int y = base; y < base + (2 - Math.abs(dx)); y++) c.set(tx + dx, y, z, s(Blocks.AIR));
            }
        }
    }

    private static void campfire(Canvas c, int x, int z) {
        int y = Archipelago.column(x, z).top() + 1;
        c.set(x, y, z, s(Blocks.CAMPFIRE));
        c.set(x - 2, y, z, s(Blocks.STRIPPED_SPRUCE_LOG).setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z));
        c.set(x + 2, y, z, s(Blocks.STRIPPED_SPRUCE_LOG).setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z));
    }

    // =====================================================================
    // Kingsport: the colonial capital
    // =====================================================================

    private static void kingsport() {
        add(KX - 42, KZ - 42, KX + 42, KZ + 14, c -> {
            Builds.plazaAndRoads(c, KX, KZ, "Clothier Agatha Pryce", "Gunsmith Abel Marsh");
            int py = Archipelago.column(KX, KZ).top() + 1;
            vendor(c, KX + 3.5, py, KZ - 3.5, "Armorer Bram Cole");
            vendor(c, KX - 3.5, py, KZ + 3.5, "Chandler Tobin Reed");
            c.spawn(KX - 6.5, py, KZ + 7.5, 0, w -> ModEntities.MARINE.create(w, EntitySpawnReason.EVENT));
            c.spawn(KX + 7.5, py, KZ - 6.5, 0, w -> ModEntities.MARINE.create(w, EntitySpawnReason.EVENT));
            c.spawn(KX + 7.5, py, KZ + 7.5, 0, w -> ModEntities.MARINE.create(w, EntitySpawnReason.EVENT));
        });
        add(KT_X1 - 1, KT_Z1 - 1, KT_X1 + 13, KT_Z1 + 11, c -> Builds.tavern(c, KT_X1, KT_Z1, KT_X1 + 12, KT_Z1 + 10));
        add(KX - 25, KZ - 8, KX - 13, KZ + 4, c -> bank(c, KX - 24, KZ - 7, KX - 14, KZ + 3));
        add(KX - 7, KZ - 29, KX + 7, KZ - 17, c -> townHall(c, KX - 6, KZ - 28, KX + 6, KZ - 18));
        int[][] houses = {{-10, 14}, {10, 14}, {-13, -15}, {13, -15}};
        for (int[] h : houses) {
            int hx = KX + h[0], hz = KZ + h[1];
            add(hx - 4, hz - 4, hx + 4, hz + 4, c -> Builds.house(c, hx, hz, KX, KZ));
        }
        add(KX - 29, KZ + 10, KX - 15, KZ + 20, c -> {
            warehouse(c, KX - 28, KZ + 11, KX - 18, KZ + 19);
            vendor(c, KX - 16.5, floorAt(KX - 16, KZ + 15), KZ + 15.5, "Merchant Josiah Thorpe");
        });
        add(KX - 3, KZ + 26, KX + 3, KZ + 94, c -> {
            Builds.pier(c, KX, KZ + 30, KZ + 92, 2);
            c.spawn(KX + 4.5, 63, KZ + 84.5, 0, w -> EntityTypes.OAK_BOAT.create(w, EntitySpawnReason.STRUCTURE));
        });
    }

    /** Stone counting-house with a barred vault: the banker keeps your strongbox. */
    private static void bank(Canvas c, int x1, int z1, int x2, int z2) {
        int cx = (x1 + x2) / 2, cz = (z1 + z2) / 2;
        int floor = floorAt(cx, cz);
        Builds.room(c, x1, z1, x2, z2, floor, 5, s(Blocks.POLISHED_ANDESITE), s(Blocks.CHISELED_STONE_BRICKS), s(Blocks.POLISHED_DIORITE));
        clear(c, x1 + 1, floor, z1 + 1, x2 - 1, floor + 4, z2 - 1);
        Builds.roof(c, x1, z1, x2, z2, floor + 5, s(Blocks.DEEPSLATE_TILES));
        Builds.windows(c, x1, z1, x2, z2, floor + 2);
        Builds.doorway(c, cx, z2, floor);
        // Counter across the room, vault behind bars
        c.fill(x1 + 1, floor, cz - 1, x2 - 1, floor, cz - 1, s(Blocks.POLISHED_ANDESITE_SLAB));
        c.fill(x1 + 1, floor, z1 + 2, x2 - 1, floor + 3, z1 + 2, s(Blocks.IRON_BARS));
        c.set(cx, floor, z1 + 1, s(Blocks.GOLD_BLOCK));
        c.chest(x1 + 1, floor, z1 + 1, Direction.SOUTH, TREASURE);
        c.chest(x2 - 1, floor, z1 + 1, Direction.SOUTH, null, new ItemStack(ModItems.DOUBLOON, 12));
        c.set(cx, floor + 4, cz + 1, Builds.hangingLantern());
        Builds.spawnNpc(c, Role.BANKER);
    }

    private static void townHall(Canvas c, int x1, int z1, int x2, int z2) {
        int cx = (x1 + x2) / 2, cz = (z1 + z2) / 2;
        int floor = floorAt(cx, cz);
        Builds.room(c, x1, z1, x2, z2, floor, 6, s(Blocks.DYED_TERRACOTTA.pick(DyeColor.WHITE)), s(Blocks.QUARTZ_PILLAR), s(Blocks.DARK_OAK_PLANKS));
        clear(c, x1 + 1, floor, z1 + 1, x2 - 1, floor + 5, z2 - 1);
        Builds.roof(c, x1, z1, x2, z2, floor + 6, s(Blocks.DEEPSLATE_TILES));
        Builds.windows(c, x1, z1, x2, z2, floor + 2);
        Builds.doorway(c, cx, z2, floor);
        // Council table and the colony's charter
        c.fill(cx - 3, floor, cz - 1, cx + 3, floor, cz, s(Blocks.DARK_OAK_SLAB));
        c.set(cx, floor, z1 + 1, s(Blocks.LECTERN));
        c.fill(x1 + 1, floor, z1 + 1, x1 + 1, floor + 2, z1 + 1, s(Blocks.BOOKSHELF));
        c.fill(x2 - 1, floor, z1 + 1, x2 - 1, floor + 2, z1 + 1, s(Blocks.BOOKSHELF));
        c.set(cx, floor + 5, cz, Builds.hangingLantern());
        // Clock tower over the door
        c.fill(cx - 1, floor + 6, z2 - 2, cx + 1, floor + 12, z2, s(Blocks.DYED_TERRACOTTA.pick(DyeColor.WHITE)));
        c.set(cx, floor + 10, z2 + 1, s(Blocks.GOLD_BLOCK)); // the clock face
        c.set(cx, floor + 13, z2 - 1, s(Blocks.LIGHTNING_ROD.weathering().unaffected()));
        c.spawn(cx + 0.5, floor, z2 + 1.5, 0, w -> ModEntities.MARINE.create(w, EntitySpawnReason.EVENT));
        Builds.spawnNpc(c, Role.AUCTIONEER);
    }

    /** Merchant warehouse: crates and barrels of trade goods. */
    private static void warehouse(Canvas c, int x1, int z1, int x2, int z2) {
        int cx = (x1 + x2) / 2, cz = (z1 + z2) / 2;
        int floor = floorAt(cx, cz);
        Builds.room(c, x1, z1, x2, z2, floor, 5, s(Blocks.SPRUCE_PLANKS), s(Blocks.STRIPPED_SPRUCE_LOG), s(Blocks.SPRUCE_PLANKS));
        clear(c, x1 + 1, floor, z1 + 1, x2 - 1, floor + 4, z2 - 1);
        Builds.roof(c, x1, z1, x2, z2, floor + 5, s(Blocks.SPRUCE_PLANKS));
        Builds.doorway(c, x2, cz, floor);
        Builds.doorway(c, x2, cz + 1, floor);
        for (int x = x1 + 1; x <= x2 - 2; x += 2) {
            c.fill(x, floor, z1 + 1, x, floor + 1, z1 + 1, s(Blocks.BARREL));
            c.fill(x, floor, z2 - 1, x, floor + (x % 4 == 0 ? 2 : 1), z2 - 1, s(Blocks.BARREL));
        }
        c.set(x1 + 1, floor, cz, s(Blocks.HAY_BLOCK));
        c.chest(x1 + 1, floor, cz + 1, Direction.EAST, TAVERN);
        c.set(cx, floor + 4, cz, Builds.hangingLantern());
    }

    // =====================================================================
    // Crownwatch: naval headquarters
    // =====================================================================

    private static void crownwatch() {
        add(CWX - 21, CWZ - 21, CWX + 21, CWZ + 21, c -> {
            Builds.fortress(c, CWX, CWZ);
            Builds.spawnNpc(c, Role.ADMIRAL);
        });
        add(CWX - 3, CWZ + 17, CWX + 3, CWZ + 78, c -> Builds.pier(c, CWX, CWZ + 22, CWZ + 76, 2));
        add(CWX + 6, CWZ + 24, CWX + 20, CWZ + 36, c -> drydock(c, CWX + 13, CWZ + 30));
    }

    /** A frigate on the slipway, ribs half planked, and the navy's chandler. */
    private static void drydock(Canvas c, int cx, int cz) {
        int floor = floorAt(cx, cz);
        c.foundation(cx - 7, cz - 6, cx + 7, cz + 6, floor, s(Blocks.STONE_BRICKS));
        c.fill(cx - 7, floor - 1, cz - 6, cx + 7, floor - 1, cz + 6, s(Blocks.STONE_BRICKS));
        clear(c, cx - 7, floor, cz - 6, cx + 7, floor + 8, cz + 6);
        for (int z = cz - 5; z <= cz + 5; z++) {
            c.set(cx, floor, z, s(Blocks.STRIPPED_DARK_OAK_LOG).setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z));
            if (Math.floorMod(z, 2) == 0) {
                c.fill(cx - 3, floor + 1, z, cx - 3, floor + 4, z, s(Blocks.DARK_OAK_FENCE));
                c.fill(cx + 3, floor + 1, z, cx + 3, floor + 4, z, s(Blocks.DARK_OAK_FENCE));
                c.fill(cx - 2, floor + 1, z, cx + 2, floor + 1, z, s(Blocks.DARK_OAK_SLAB));
            } else if (z < cz) {
                c.fill(cx - 3, floor + 1, z, cx - 3, floor + 3, z, s(Blocks.DARK_OAK_PLANKS));
                c.fill(cx + 3, floor + 1, z, cx + 3, floor + 3, z, s(Blocks.DARK_OAK_PLANKS));
            }
        }
        // Crane
        c.fill(cx - 6, floor, cz - 5, cx - 6, floor + 9, cz - 5, s(Blocks.SPRUCE_LOG));
        c.fill(cx - 6, floor + 9, cz - 5, cx - 1, floor + 9, cz - 5, s(Blocks.SPRUCE_LOG).setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
        c.fill(cx - 1, floor + 5, cz - 5, cx - 1, floor + 8, cz - 5, s(Blocks.IRON_CHAIN));
        vendor(c, cx + 5.5, floor, cz - 4.5, "Navy Chandler Ruth Ashby");
        vendor(c, cx - 4.5, floor, cz + 4.5, "Navy Purser Edmund Hale");
    }

    // =====================================================================
    // Saint Vesper: the cathedral isle
    // =====================================================================

    private static void saintVesper() {
        add(VX - 10, VZ - 13, VX + 10, VZ + 13, c -> cathedral(c, VX - 9, VZ - 12, VX + 9, VZ + 12));
        add(VX + 13, VZ - 8, VX + 25, VZ + 8, c -> infirmary(c, VX + 14, VZ - 7, VX + 24, VZ + 7));
        add(VX - 25, VZ - 11, VX - 13, VZ + 3, c -> Builds.cemetery(c, VX - 24, VZ - 10, VX - 14, VZ + 2));
        for (int[] h : new int[][]{{-3, -25}, {7, -25}}) {
            int hx = VX + h[0], hz = VZ + h[1];
            add(hx - 4, hz - 4, hx + 4, hz + 4, c -> Builds.house(c, hx, hz, VX, VZ));
        }
        add(VX - 13, VZ + 15, VX - 5, VZ + 23, c -> {
            int py = floorAt(VX - 9, VZ + 19);
            Builds.stall(c, VX - 9, VZ + 19, py - 1, s(Blocks.WOOL.pick(DyeColor.PURPLE)));
            c.set(VX - 9, py, VZ + 19, s(Blocks.BREWING_STAND));
            vendor(c, VX - 8.5, py, VZ + 21.5, "Apothecary Brother Anselm");
            vendor(c, VX - 6.5, py, VZ + 21.5, "Merchant Sister Odile");
        });
        add(VX - 2, VZ + 26, VX + 2, VZ + 82, c -> Builds.pier(c, VX, VZ + 28, VZ + 80, 1));
    }

    private static void cathedral(Canvas c, int x1, int z1, int x2, int z2) {
        int cx = (x1 + x2) / 2;
        int floor = floorAt(cx, (z1 + z2) / 2);
        int h = 10;
        Builds.room(c, x1, z1, x2, z2, floor, h, s(Blocks.SMOOTH_SANDSTONE), s(Blocks.CHISELED_SANDSTONE), s(Blocks.POLISHED_DIORITE));
        clear(c, x1 + 1, floor, z1 + 1, x2 - 1, floor + h - 1, z2 - 1);
        // Buttresses down both long sides
        for (int z = z1 + 3; z <= z2 - 3; z += 4) {
            c.fill(x1 - 1, floor, z, x1 - 1, floor + h - 3, z, s(Blocks.CUT_SANDSTONE));
            c.fill(x2 + 1, floor, z, x2 + 1, floor + h - 3, z, s(Blocks.CUT_SANDSTONE));
        }
        // Tall stained windows
        BlockState[] glass = {s(Blocks.STAINED_GLASS.pick(DyeColor.BLUE)), s(Blocks.STAINED_GLASS.pick(DyeColor.RED)), s(Blocks.STAINED_GLASS.pick(DyeColor.YELLOW))};
        int k = 0;
        for (int z = z1 + 2; z <= z2 - 2; z += 4) {
            c.fill(x1, floor + 2, z, x1, floor + 6, z, glass[k % 3]);
            c.fill(x2, floor + 2, z, x2, floor + 6, z, glass[(k + 1) % 3]);
            k++;
        }
        c.fill(cx - 1, floor + 4, z1, cx + 1, floor + 7, z1, s(Blocks.STAINED_GLASS.pick(DyeColor.ORANGE))); // rose window over the altar
        Builds.roof(c, x1, z1, x2, z2, floor + h, s(Blocks.DARK_OAK_PLANKS));
        // Doors at the south end, nave, pews and altar at the north
        for (int dx = -1; dx <= 1; dx++) Builds.doorway(c, cx + dx, z2, floor);
        c.fill(cx - 1, floor - 1, z1 + 1, cx + 1, floor - 1, z2 - 1, s(Blocks.DYED_TERRACOTTA.pick(DyeColor.RED)));
        for (int z = z1 + 5; z <= z2 - 3; z += 2) {
            c.fill(x1 + 2, floor, z, cx - 2, floor, z, s(Blocks.SPRUCE_STAIRS));
            c.fill(cx + 2, floor, z, x2 - 2, floor, z, s(Blocks.SPRUCE_STAIRS));
        }
        c.fill(cx - 2, floor, z1 + 2, cx + 2, floor, z1 + 2, s(Blocks.QUARTZ_BLOCK));
        c.set(cx, floor + 1, z1 + 2, s(Blocks.CANDLE));
        c.set(cx - 2, floor + 1, z1 + 2, s(Blocks.CANDLE));
        c.set(cx + 2, floor + 1, z1 + 2, s(Blocks.CANDLE));
        c.set(cx, floor, z1 + 3, s(Blocks.LECTERN));
        for (int z = z1 + 3; z <= z2 - 3; z += 5) c.set(cx, floor + h - 1, z, Builds.hangingLantern());
        // Bell tower over the doors
        int tz1 = z2 - 3, tz2 = z2;
        c.walls(cx - 2, floor + h, tz1, cx + 2, floor + h + 9, tz2 + 1, s(Blocks.SMOOTH_SANDSTONE));
        clear(c, cx - 1, floor + h, tz1 + 1, cx + 1, floor + h + 9, tz2);
        c.fill(cx - 2, floor + h + 6, tz1 + 2, cx + 2, floor + h + 7, tz1 + 2, s(Blocks.AIR));
        c.set(cx, floor + h + 7, tz1 + 2, s(Blocks.BELL).setValue(BellBlock.ATTACHMENT, BellAttachType.CEILING).setValue(BellBlock.FACING, Direction.NORTH));
        c.fill(cx - 2, floor + h + 10, tz1, cx + 2, floor + h + 10, tz2 + 1, s(Blocks.DARK_OAK_PLANKS));
        c.fill(cx, floor + h + 11, tz1 + 2, cx, floor + h + 13, tz1 + 2, s(Blocks.CUT_SANDSTONE));
        c.chest(x2 - 1, floor, z1 + 1, Direction.WEST, null, new ItemStack(ModItems.ANCIENT_RELIC), new ItemStack(Items.GOLDEN_APPLE, 2));
    }

    /** The infirmary: rows of beds, and Mother Celestine, who mends anyone. */
    private static void infirmary(Canvas c, int x1, int z1, int x2, int z2) {
        int cx = (x1 + x2) / 2, cz = (z1 + z2) / 2;
        int floor = floorAt(cx, cz);
        Builds.room(c, x1, z1, x2, z2, floor, 5, s(Blocks.SMOOTH_SANDSTONE), s(Blocks.CUT_SANDSTONE), s(Blocks.BIRCH_PLANKS));
        clear(c, x1 + 1, floor, z1 + 1, x2 - 1, floor + 4, z2 - 1);
        Builds.roof(c, x1, z1, x2, z2, floor + 5, s(Blocks.DARK_OAK_PLANKS));
        Builds.windows(c, x1, z1, x2, z2, floor + 2);
        Builds.doorway(c, x1, cz, floor);
        for (int z = z1 + 1; z <= z2 - 2; z += 3) {
            Builds.bed(c, x2 - 2, floor, z, Direction.EAST);
        }
        c.set(x1 + 1, floor, z1 + 1, s(Blocks.BREWING_STAND));
        c.set(x1 + 2, floor, z1 + 1, s(Blocks.CAULDRON));
        c.set(cx, floor + 4, cz, Builds.hangingLantern());
        Builds.spawnNpc(c, Role.ABBESS);
    }

    // =====================================================================
    // Mariner's Rest: the fishing village
    // =====================================================================

    private static void marinersRest() {
        int[][] huts = {{-7, -9}, {5, -11}, {13, 1}};
        for (int[] h : huts) {
            int hx = MX + h[0], hz = MZ + h[1];
            add(hx - 4, hz - 4, hx + 4, hz + 4, c -> fisherHut(c, hx, hz));
        }
        add(MX - 13, MZ - 1, MX - 5, MZ + 7, c -> smokehouse(c, MX - 12, MZ, MX - 6, MZ + 6));
        add(MX - 3, MZ - 3, MX + 7, MZ + 3, c -> {
            // Drying racks of fish and kelp
            for (int x = MX - 2; x <= MX + 6; x += 4) {
                int y = floorAt(x, MZ);
                c.fill(x, y, MZ - 2, x, y + 2, MZ - 2, s(Blocks.OAK_FENCE));
                c.fill(x, y, MZ + 2, x, y + 2, MZ + 2, s(Blocks.OAK_FENCE));
                c.fill(x, y + 2, MZ - 1, x, y + 2, MZ + 1, s(Blocks.OAK_FENCE));
                c.set(x, y + 1, MZ, s(Blocks.DRIED_KELP_BLOCK));
            }
        });
        add(MX - 3, MZ + 8, MX + 3, MZ + 62, c -> {
            Builds.pier(c, MX, MZ + 10, MZ + 60, 1);
            c.spawn(MX + 2.5, 63, MZ + 52.5, 0, w -> EntityTypes.OAK_BOAT.create(w, EntitySpawnReason.STRUCTURE));
            c.spawn(MX - 2.5, 63, MZ + 40.5, 90, w -> EntityTypes.OAK_BOAT.create(w, EntitySpawnReason.STRUCTURE));
            for (int z = MZ + 20; z <= MZ + 58; z += 9) c.set(MX + 1, 64, z, s(Blocks.BARREL));
        });
    }

    private static void fisherHut(Canvas c, int cx, int cz) {
        int x1 = cx - 3, x2 = cx + 3, z1 = cz - 3, z2 = cz + 3;
        int floor = floorAt(cx, cz) + 1; // up on short stilts
        for (int[] k : new int[][]{{x1, z1}, {x1, z2}, {x2, z1}, {x2, z2}}) {
            int top = Archipelago.column(k[0], k[1]).top();
            c.fill(k[0], top + 1, k[1], k[0], floor - 1, k[1], s(Blocks.STRIPPED_OAK_LOG));
        }
        c.fill(x1, floor - 1, z1, x2, floor - 1, z2, s(Blocks.OAK_PLANKS));
        c.walls(x1, floor, z1, x2, floor + 2, z2, s(Blocks.BIRCH_PLANKS));
        clear(c, x1 + 1, floor, z1 + 1, x2 - 1, floor + 2, z2 - 1);
        Builds.roof(c, x1, z1, x2, z2, floor + 3, s(Blocks.SPRUCE_PLANKS));
        Builds.doorway(c, cx, z2, floor);
        c.set(cx, floor - 1, z2 + 1, s(Blocks.OAK_SLAB));
        Builds.bed(c, x1 + 1, floor, z1 + 1, Direction.SOUTH);
        c.set(x2 - 1, floor, z1 + 1, s(Blocks.BARREL));
        c.chest(x2 - 1, floor, z2 - 1, Direction.WEST, null, new ItemStack(Items.FISHING_ROD), new ItemStack(Items.COD, 6));
        c.set(cx, floor + 2, cz, Builds.hangingLantern());
    }

    private static void smokehouse(Canvas c, int x1, int z1, int x2, int z2) {
        int cx = (x1 + x2) / 2, cz = (z1 + z2) / 2;
        int floor = floorAt(cx, cz);
        Builds.room(c, x1, z1, x2, z2, floor, 4, s(Blocks.COBBLESTONE), s(Blocks.MOSSY_COBBLESTONE), s(Blocks.COBBLESTONE));
        clear(c, x1 + 1, floor, z1 + 1, x2 - 1, floor + 3, z2 - 1);
        Builds.roof(c, x1, z1, x2, z2, floor + 4, s(Blocks.SPRUCE_PLANKS));
        Builds.doorway(c, x2, cz, floor);
        c.set(cx - 1, floor, cz, s(Blocks.SMOKER));
        c.set(cx, floor, z1 + 1, s(Blocks.CAMPFIRE));
        c.fill(cx, floor + 4, z1 + 1, cx, floor + 6, z1 + 1, s(Blocks.COBBLESTONE_WALL));
        vendor(c, x2 + 1.5, floor, cz + 0.5, "Fishmonger Gull Harlan");
        vendor(c, x2 + 1.5, floor, cz + 2.5, "Cook Bess Haddock");
        vendor(c, x2 + 1.5, floor, cz + 4.5, "Trader Ezra Gull");
        Builds.spawnNpc(c, Role.CONTEST_JUDGE);
    }

    // =====================================================================
    // Widow's Rock: wreckers on the cliffs
    // =====================================================================

    private static void widowsRock() {
        add(WX - 6, WZ - 6, WX + 6, WZ + 6, c -> watchtower(c, WX, WZ));
        add(WX + 11, WZ + 1, WX + 21, WZ + 11, c -> widowsHouse(c, WX + 12, WZ + 2, WX + 20, WZ + 10));
        add(WX - 19, WZ - 13, WX - 7, WZ - 1, c -> {
            tent(c, WX - 15, WZ - 11, s(Blocks.WOOL.pick(DyeColor.BLACK)));
            tent(c, WX - 10, WZ - 11, s(Blocks.WOOL.pick(DyeColor.GRAY)));
            campfire(c, WX - 13, WZ - 4);
            int y = Archipelago.column(WX - 9, WZ - 4).top() + 1;
            c.chest(WX - 9, y, WZ - 4, Direction.WEST, TREASURE);
            for (int[] g : new int[][]{{-16, -5}, {-11, -6}, {-14, -2}, {-8, -8}}) {
                int gx = WX + g[0], gz = WZ + g[1];
                guard(c, gx + 0.5, Archipelago.column(gx, gz).top() + 1, gz + 0.5, w__ -> EntityTypes.PILLAGER.create(w__, EntitySpawnReason.STRUCTURE), "Wrecker");
            }
        });
        // A way up: a tunnel into the cliff from the sea, and a ladder shaft to the top
        add(WX + 24, WZ - 2, WX + 46, WZ + 2, OuterBuilds::cliffShaft);
    }

    /** The old watchtower; wreckers hang a false light in it to lure ships onto the rocks. */
    private static void watchtower(Canvas c, int cx, int cz) {
        int floor = floorAt(cx, cz);
        c.foundation(cx - 4, cz - 4, cx + 4, cz + 4, floor, s(Blocks.COBBLESTONE));
        c.columns(cx - 4, cz - 4, cx + 4, cz + 4, (x, z) -> {
            double d = Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
            int ruinTop = floor + 12 - (int) (Noise.rand(x, z, 191) * 5);
            if (d <= 4.2) c.set(x, floor - 1, z, s(Blocks.COBBLESTONE));
            for (int y = floor; y <= floor + 14; y++) {
                if (d <= 4.2 && d > 3.2 && y <= ruinTop) {
                    double r = Noise.rand(x, y, z, 192);
                    c.set(x, y, z, s(r < 0.5 ? Blocks.STONE_BRICKS : r < 0.8 ? Blocks.CRACKED_STONE_BRICKS : Blocks.MOSSY_STONE_BRICKS));
                } else if (d <= 3.2) {
                    c.set(x, y, z, s(Blocks.AIR));
                }
            }
        });
        Builds.doorway(c, cx, cz + 4, floor);
        c.fill(cx - 3, floor + 8, cz - 3, cx + 3, floor + 8, cz + 3, s(Blocks.SPRUCE_PLANKS));
        clear(c, cx, floor + 8, cz + 2, cx, floor + 8, cz + 2);
        BlockState ladder = s(Blocks.LADDER).setValue(LadderBlock.FACING, Direction.NORTH);
        for (int y = floor; y <= floor + 8; y++) c.set(cx, y, cz + 3, ladder);
        c.set(cx, floor + 9, cz, s(Blocks.SPRUCE_FENCE));
        c.set(cx, floor + 10, cz, s(Blocks.LANTERN));
    }

    private static void widowsHouse(Canvas c, int x1, int z1, int x2, int z2) {
        int cx = (x1 + x2) / 2, cz = (z1 + z2) / 2;
        int floor = floorAt(cx, cz);
        Builds.room(c, x1, z1, x2, z2, floor, 4, s(Blocks.DARK_OAK_PLANKS), s(Blocks.DARK_OAK_LOG), s(Blocks.SPRUCE_PLANKS));
        clear(c, x1 + 1, floor, z1 + 1, x2 - 1, floor + 3, z2 - 1);
        Builds.roof(c, x1, z1, x2, z2, floor + 4, s(Blocks.DEEPSLATE_TILES));
        Builds.windows(c, x1, z1, x2, z2, floor + 1);
        Builds.doorway(c, x1, cz, floor);
        // The widow's walk: a railed platform on the roof, facing the sea
        c.set(x2 - 1, floor, cz, s(Blocks.CANDLE));
        c.set(x2 - 1, floor, cz - 1, s(Blocks.SPRUCE_STAIRS));
        Builds.bed(c, x1 + 1, floor, z1 + 1, Direction.SOUTH);
        c.chest(x2 - 1, floor, z2 - 1, Direction.WEST, null, new ItemStack(ModItems.ANCIENT_COIN, 3),
                new ItemStack(Items.WRITABLE_BOOK), new ItemStack(Items.SPYGLASS));
    }

    private static void cliffShaft(Canvas c) {
        // Find the outermost land column east of the centre
        int edge = WX;
        for (int x = WX; x <= WX + 60; x++) {
            if (Archipelago.column(x, WZ).top() > Archipelago.WATER_TOP) edge = x;
        }
        int shaftX = edge - 3;
        int top = Archipelago.column(shaftX, WZ).top();
        clear(c, shaftX, 63, WZ, edge, 64, WZ);
        clear(c, shaftX, 63, WZ, shaftX, top + 1, WZ);
        BlockState ladder = s(Blocks.LADDER).setValue(LadderBlock.FACING, Direction.EAST);
        for (int y = 63; y <= top; y++) {
            c.set(shaftX - 1, y, WZ, s(Blocks.STONE));
            c.set(shaftX, y, WZ, ladder);
        }
        c.set(edge, 65, WZ, s(Blocks.LANTERN));
        c.fill(edge + 1, 63, WZ - 1, edge + 3, 63, WZ + 1, s(Blocks.SPRUCE_PLANKS));
    }

    // =====================================================================
    // Serpent's Hollow: jungle ruins and the old crocodile
    // =====================================================================

    private static void serpentsHollow() {
        add(SHX - 12, SHZ - 12, SHX + 12, SHZ + 12, c -> serpentRuins(c, SHX, SHZ));
        add(SHX + 14, SHZ - 6, SHX + 24, SHZ + 4, c -> {
            int water = Archipelago.column(SHX + 19, SHZ - 1).top();
            c.columns(SHX + 15, SHZ - 5, SHX + 23, SHZ + 3, (x, z) -> {
                double d = Math.hypot(x - (SHX + 19), z - (SHZ - 1));
                if (d > 4.3) return;
                clear(c, x, water - 2, z, x, water + 6, z);
                c.fill(x, water - 3, z, x, water - 3, z, s(Blocks.MUD));
                c.fill(x, water - 2, z, x, water - 1, z, s(Blocks.WATER));
                if (Noise.rand(x, z, 211) < 0.2) c.set(x, water, z, s(Blocks.LILY_PAD));
            });
            boss(c, SHX + 19.5, water - 1, SHZ - 0.5, w -> {
                var croc = Wildlife.CROCODILE.create(w, EntitySpawnReason.EVENT);
                return croc;
            }, "Old Gnasher", 240, 2.6, null);
        });
        add(SHX - 26, SHZ + 14, SHX - 16, SHZ + 24, c -> {
            tent(c, SHX - 23, SHZ + 15, s(Blocks.WOOL.pick(DyeColor.GREEN)));
            tent(c, SHX - 18, SHZ + 17, s(Blocks.WOOL.pick(DyeColor.BROWN)));
            campfire(c, SHX - 21, SHZ + 21);
            for (int[] g : new int[][]{{-24, 21}, {-18, 22}, {-20, 15}}) {
                int gx = SHX + g[0], gz = SHZ + g[1];
                guard(c, gx + 0.5, Archipelago.column(gx, gz).top() + 1, gz + 0.5, w__ -> EntityTypes.VINDICATOR.create(w__, EntitySpawnReason.STRUCTURE), "Jungle Raider");
            }
            int y = Archipelago.column(SHX - 17, SHZ + 21).top() + 1;
            c.chest(SHX - 17, y, SHZ + 21, Direction.WEST, TREASURE);
        });
    }

    private static void serpentRuins(Canvas c, int cx, int cz) {
        int floor = floorAt(cx, cz);
        // Cracked paving in a broad circle
        c.columns(cx - 11, cz - 11, cx + 11, cz + 11, (x, z) -> {
            double d = Math.hypot(x - cx, z - cz);
            if (d > 11) return;
            int top = Archipelago.column(x, z).top();
            if (top < floor - 4) return;
            double r = Noise.rand(x, z, 221);
            for (int y = top + 1; y < floor; y++) c.set(x, y, z, s(Blocks.MOSSY_COBBLESTONE));
            if (r < 0.75) c.set(x, floor - 1, z, s(r < 0.35 ? Blocks.MOSSY_STONE_BRICKS : r < 0.6 ? Blocks.CRACKED_STONE_BRICKS : Blocks.STONE_BRICKS));
            clear(c, x, floor, z, x, floor + 6, z);
        });
        // A ring of broken pillars
        for (int i = 0; i < 10; i++) {
            double a = i * Math.PI / 5;
            int px = cx + (int) Math.round(Math.cos(a) * 9), pz = cz + (int) Math.round(Math.sin(a) * 9);
            int height = 2 + (int) (Noise.rand(px, pz, 223) * 5);
            c.fill(px, floor, pz, px, floor + height, pz, s(Blocks.CHISELED_STONE_BRICKS));
            if (height > 4) c.set(px, floor + height + 1, pz, s(Blocks.MOSSY_STONE_BRICK_SLAB));
            c.set(px + 1, floor + 1, pz, s(Blocks.VINE).setValue(net.minecraft.world.level.block.VineBlock.WEST, true));
        }
        // The serpent: a coiled body of green terracotta rising to a head
        int[][] coil = {{-2, 0}, {-2, 1}, {-1, 2}, {0, 2}, {1, 2}, {2, 1}, {2, 0}, {2, -1}, {1, -2}, {0, -2}, {-1, -2}};
        for (int i = 0; i < coil.length; i++) {
            c.set(cx + coil[i][0], floor + i / 4, cz + coil[i][1], s(Blocks.DYED_TERRACOTTA.pick(DyeColor.GREEN)));
        }
        c.fill(cx - 1, floor + 3, cz - 1, cx - 1, floor + 5, cz - 1, s(Blocks.DYED_TERRACOTTA.pick(DyeColor.GREEN)));
        c.fill(cx - 2, floor + 6, cz - 2, cx, floor + 6, cz, s(Blocks.DYED_TERRACOTTA.pick(DyeColor.GREEN)));
        c.set(cx - 2, floor + 6, cz - 1, s(Blocks.EMERALD_BLOCK));
        c.set(cx, floor + 6, cz - 1, s(Blocks.EMERALD_BLOCK));
        c.chest(cx, floor, cz, Direction.NORTH, null, new ItemStack(ModItems.ANCIENT_RELIC), new ItemStack(ModItems.ANCIENT_COIN, 5));
    }

    // =====================================================================
    // Bloodwake Cay: the pirate warlord's stronghold
    // =====================================================================

    private static void bloodwake() {
        add(BWX - 16, BWZ - 16, BWX + 16, BWZ + 16, c -> stronghold(c, BWX, BWZ));
        add(BWX - 2, BWZ + 15, BWX + 2, BWZ + 72, c -> Builds.pier(c, BWX, BWZ + 18, BWZ + 70, 1));
    }

    private static void stronghold(Canvas c, int cx, int cz) {
        int floor = floorAt(cx, cz);
        BlockState wall = s(Blocks.RED_SANDSTONE);
        c.foundation(cx - 15, cz - 15, cx + 15, cz + 15, floor, s(Blocks.SANDSTONE));
        c.columns(cx - 14, cz - 14, cx + 14, cz + 14, (x, z) -> {
            c.set(x, floor - 1, z, s(Noise.rand(x, z, 231) < 0.6 ? Blocks.SMOOTH_SANDSTONE : Blocks.SAND));
            clear(c, x, floor, z, x, floor + 9, z);
        });
        c.walls(cx - 15, floor, cz - 15, cx + 15, floor + 5, cz + 15, wall);
        // Crenellations and black flags
        c.columns(cx - 15, cz - 15, cx + 15, cz + 15, (x, z) -> {
            boolean edge = x == cx - 15 || x == cx + 15 || z == cz - 15 || z == cz + 15;
            if (edge && Math.floorMod(x + z, 2) == 0) c.set(x, floor + 6, z, wall);
        });
        for (int[] k : new int[][]{{cx - 15, cz - 15}, {cx + 15, cz - 15}, {cx - 15, cz + 15}, {cx + 15, cz + 15}}) {
            c.fill(k[0], floor, k[1], k[0], floor + 8, k[1], s(Blocks.CHISELED_RED_SANDSTONE));
            c.fill(k[0], floor + 9, k[1], k[0], floor + 12, k[1], s(Blocks.DARK_OAK_FENCE));
            c.set(k[0] + 1, floor + 12, k[1], s(Blocks.WOOL.pick(DyeColor.BLACK)));
            c.set(k[0] + 1, floor + 11, k[1], s(Blocks.WOOL.pick(DyeColor.BLACK)));
            c.set(k[0] + 2, floor + 12, k[1], s(Blocks.WOOL.pick(DyeColor.RED)));
        }
        // Wall-walk on the inside, gate on the south
        c.walls(cx - 14, floor + 4, cz - 14, cx + 14, floor + 4, cz + 14, s(Blocks.DARK_OAK_PLANKS));
        clear(c, cx - 1, floor, cz + 15, cx + 1, floor + 2, cz + 15);
        c.fill(cx - 1, floor + 3, cz + 15, cx + 1, floor + 3, cz + 15, s(Blocks.IRON_BARS));
        // Cannons on the walls: black barrels on carriages
        for (int[] g : new int[][]{{-8, -14}, {8, -14}, {-14, 0}, {14, 0}}) {
            c.set(cx + g[0], floor + 5, cz + g[1], s(Blocks.SPRUCE_SLAB));
            c.set(cx + g[0], floor + 6, cz + g[1], s(Blocks.CONCRETE.pick(DyeColor.BLACK)));
        }
        // The keep
        int kx1 = cx - 6, kx2 = cx + 6, kz1 = cz - 11, kz2 = cz - 2;
        Builds.room(c, kx1, kz1, kx2, kz2, floor, 5, s(Blocks.CUT_RED_SANDSTONE), s(Blocks.DARK_OAK_LOG), s(Blocks.DARK_OAK_PLANKS));
        clear(c, kx1 + 1, floor, kz1 + 1, kx2 - 1, floor + 4, kz2 - 1);
        Builds.roof(c, kx1, kz1, kx2, kz2, floor + 5, s(Blocks.DARK_OAK_PLANKS));
        Builds.doorway(c, cx, kz2, floor);
        Builds.doorway(c, cx + 1, kz2, floor);
        Builds.windows(c, kx1, kz1, kx2, kz2, floor + 2);
        c.fill(cx - 2, floor, kz1 + 1, cx + 2, floor, kz1 + 1, s(Blocks.CARPET.pick(DyeColor.RED)));
        c.set(cx, floor, kz1 + 1, s(Blocks.DARK_OAK_STAIRS));
        c.chest(kx1 + 1, floor, kz1 + 1, Direction.SOUTH, Builds.TREASURE);
        c.chest(kx2 - 1, floor, kz1 + 1, Direction.SOUTH, null, new ItemStack(ModItems.DOUBLOON, 40),
                new ItemStack(ModItems.MAP_FRAGMENT), new ItemStack(Items.GOLD_INGOT, 8));
        c.set(cx, floor + 4, cz - 6, Builds.hangingLantern());
        // Gallows in the yard
        c.fill(cx + 9, floor, cz + 6, cx + 9, floor + 4, cz + 6, s(Blocks.DARK_OAK_FENCE));
        c.fill(cx + 9, floor + 4, cz + 6, cx + 11, floor + 4, cz + 6, s(Blocks.DARK_OAK_FENCE));
        c.set(cx + 11, floor + 3, cz + 6, s(Blocks.IRON_CHAIN));
        // The warlord and his crew
        boss(c, cx + 0.5, floor, cz - 8.5, w -> EntityTypes.VINDICATOR.create(w, EntitySpawnReason.EVENT), "Red Maddox, Pirate Warlord", 180, 1.25,
                new ItemStack(Items.NETHERITE_AXE));
        int[][] crew = {{-9, 4}, {9, 4}, {-5, 8}, {5, 8}, {-11, -9}, {11, -9}};
        for (int i = 0; i < crew.length; i++) {
            boolean musket = i % 2 == 1;
            guard(c, cx + crew[i][0] + 0.5, floor, cz + crew[i][1] + 0.5,
                    musket ? w__ -> EntityTypes.PILLAGER.create(w__, EntitySpawnReason.STRUCTURE) : w__ -> EntityTypes.VINDICATOR.create(w__, EntitySpawnReason.STRUCTURE),
                    musket ? "Bloodwake Musketeer" : "Bloodwake Cutthroat");
        }
    }

    // =====================================================================
    // Stormbreak Isle: the regatta
    // =====================================================================

    private static void stormbreak() {
        add(SBX - 6, SBZ - 18, SBX + 4, SBZ - 10, c -> raceHut(c, SBX - 5, SBZ - 17, SBX + 3, SBZ - 11));
        add(SBX - 2, SBZ - 54, SBX + 2, SBZ - 16, c -> {
            Builds.pier(c, SBX, SBZ - 52, SBZ - 18, 1);
            c.spawn(SBX + 2.5, 63, SBZ - 46.5, 0, w -> EntityTypes.OAK_BOAT.create(w, EntitySpawnReason.STRUCTURE));
        });
        for (int i = 0; i < BUOYS.length; i++) {
            int bx = SBX + BUOYS[i][0], bz = SBZ + BUOYS[i][1];
            int n = i;
            add(bx - 1, bz - 1, bx + 1, bz + 1, c -> buoy(c, bx, bz, n == 0));
        }
    }

    private static void raceHut(Canvas c, int x1, int z1, int x2, int z2) {
        int cx = (x1 + x2) / 2, cz = (z1 + z2) / 2;
        int floor = floorAt(cx, cz);
        Builds.room(c, x1, z1, x2, z2, floor, 4, s(Blocks.SPRUCE_PLANKS), s(Blocks.STRIPPED_SPRUCE_LOG), s(Blocks.SPRUCE_PLANKS));
        clear(c, x1 + 1, floor, z1 + 1, x2 - 1, floor + 3, z2 - 1);
        Builds.roof(c, x1, z1, x2, z2, floor + 4, s(Blocks.DARK_OAK_PLANKS));
        Builds.windows(c, x1, z1, x2, z2, floor + 1);
        Builds.doorway(c, cx, z2, floor);
        c.set(x1 + 1, floor, z1 + 1, s(Blocks.CARTOGRAPHY_TABLE));
        c.set(x2 - 1, floor, z1 + 1, s(Blocks.BARREL));
        // Weathervane and a tall striped flag pole
        c.fill(x2 + 1, floor, z1, x2 + 1, floor + 9, z1, s(Blocks.SPRUCE_FENCE));
        c.set(x2 + 2, floor + 9, z1, s(Blocks.WOOL.pick(DyeColor.WHITE)));
        c.set(x2 + 2, floor + 8, z1, s(Blocks.WOOL.pick(DyeColor.RED)));
        c.set(x2 + 1, floor + 10, z1, s(Blocks.LIGHTNING_ROD.weathering().unaffected()));
        Builds.spawnNpc(c, Role.RACEMASTER);
    }

    /** A course marker: a post driven into the sea floor with a light on top. */
    private static void buoy(Canvas c, int x, int z, boolean start) {
        int floor = Archipelago.column(x, z).top();
        c.fill(x, floor + 1, z, x, 64, z, s(Blocks.STRIPPED_SPRUCE_LOG));
        c.set(x, 65, z, s(start ? Blocks.WOOL.pick(DyeColor.YELLOW) : Blocks.WOOL.pick(DyeColor.RED)));
        c.set(x, 66, z, s(Blocks.SEA_LANTERN));
    }

    // =====================================================================
    // Raven's Landing: the smugglers' port
    // =====================================================================

    private static void ravensLanding() {
        add(RX - 42, RZ - 42, RX + 42, RZ + 14, c -> Builds.plazaAndRoads(c, RX, RZ, "Fence Corvin Black", "Smuggler Nell Brigg"));
        add(RT_X1 - 1, RT_Z1 - 1, RT_X1 + 13, RT_Z1 + 11, c -> Builds.tavern(c, RT_X1, RT_Z1, RT_X1 + 12, RT_Z1 + 10));
        add(RX - 15, RZ - 17, RX - 3, RZ - 7, c -> {
            warehouse(c, RX - 14, RZ - 16, RX - 4, RZ - 8);
            Builds.spawnNpc(c, Role.SMUGGLER_QUEEN);
        });
        add(RX - 21, RZ - 1, RX - 9, RZ + 9, c -> warehouse(c, RX - 20, RZ, RX - 10, RZ + 8));
        for (int[] h : new int[][]{{11, 13}, {21, 13}}) {
            int hx = RX + h[0], hz = RZ + h[1];
            add(hx - 4, hz - 4, hx + 4, hz + 4, c -> Builds.house(c, hx, hz, RX, RZ));
        }
        add(RX - 2, RZ + 26, RX + 2, RZ + 92, c -> {
            Builds.pier(c, RX, RZ + 30, RZ + 90, 1);
            c.spawn(RX + 2.5, 63, RZ + 80.5, 0, w -> EntityTypes.OAK_BOAT.create(w, EntitySpawnReason.STRUCTURE));
        });
    }

    // =====================================================================
    // Emberfall: the crater sanctum
    // =====================================================================

    private static void emberfall() {
        add(EFX - 17, EFZ - 17, EFX + 17, EFZ + 17, c -> {
            int rim = Archipelago.EMBERFALL.peak() - 4;
            c.columns(EFX - 16, EFZ - 16, EFX + 16, EFZ + 16, (x, z) -> {
                double d = Math.hypot(x - EFX, z - EFZ);
                double raw = d / Archipelago.EMBERFALL.radius();
                if (raw < 0.125 || d > 16) return;
                int top = Archipelago.column(x, z).top();
                for (int y = top + 1; y < rim; y++) c.set(x, y, z, s(Blocks.BLACKSTONE));
                c.set(x, rim - 1, z, s(Noise.rand(x, z, 241) < 0.3 ? Blocks.CRYING_OBSIDIAN : Blocks.POLISHED_BLACKSTONE_BRICKS));
                clear(c, x, rim, z, x, rim + 8, z);
            });
            for (int i = 0; i < 8; i++) {
                double a = i * Math.PI / 4;
                int px = EFX + (int) Math.round(Math.cos(a) * 14), pz = EFZ + (int) Math.round(Math.sin(a) * 14);
                c.fill(px, rim, pz, px, rim + 4, pz, s(Blocks.OBSIDIAN));
                c.set(px, rim + 5, pz, s(Blocks.NETHERRACK));
                c.set(px, rim + 6, pz, s(Blocks.FIRE));
            }
            c.chest(EFX + 12, rim, EFZ, Direction.WEST, null, new ItemStack(ModItems.ANCIENT_RELIC, 2),
                    new ItemStack(Items.DIAMOND, 3), new ItemStack(ModItems.DOUBLOON, 50));
            boss(c, EFX + 11.5, rim, EFZ + 4.5, w -> EntityTypes.BLAZE.create(w, EntitySpawnReason.EVENT), "The Cinder King", 220, 2.0, null);
            for (int[] g : new int[][]{{-11, 3}, {3, 11}, {-3, -11}}) {
                c.spawn(EFX + g[0] + 0.5, rim, EFZ + g[1] + 0.5, 0, w -> EntityTypes.BLAZE.create(w, EntitySpawnReason.EVENT), e -> {
                    if (e instanceof Mob m) m.setPersistenceRequired();
                });
            }
        });
    }

    // =====================================================================
    // The Forgotten Keys: ruins of the first people of the sea
    // =====================================================================

    private static final int[][] KEY_RUINS = {{-52, -4}, {-40, 26}, {-28, 20}, {-10, 2}, {20, 2}, {8, -10}, {44, -34}, {38, 32},
            {56, 14}, {-46, -16}};

    private static void forgottenKeys() {
        for (int i = 0; i < KEY_RUINS.length; i++) {
            int x = FKX + KEY_RUINS[i][0], z = FKZ + KEY_RUINS[i][1];
            int kind = i % 4;
            add(x - 3, z - 3, x + 3, z + 3, c -> ancientRuin(c, x, z, kind));
        }
        int ax = FKX + 2, az = FKZ + 38;
        add(ax - 4, az - 4, ax + 4, az + 4, c -> {
            tent(c, ax - 1, az - 3, s(Blocks.WOOL.pick(DyeColor.WHITE)));
            campfire(c, ax, az + 2);
            int y = Archipelago.column(ax + 3, az).top() + 1;
            c.set(ax + 3, y, az, s(Blocks.LECTERN));
            vendor(c, ax + 2.5, y, az + 1.5, "Antiquarian Perpetua Lowe");
            Builds.spawnNpc(c, Role.SCHOLAR);
        });
    }

    private static void ancientRuin(Canvas c, int x, int z, int kind) {
        int floor = floorAt(x, z);
        BlockState stone = s(Blocks.CUT_SANDSTONE);
        BlockState glyph = s(Blocks.CHISELED_SANDSTONE);
        switch (kind) {
            case 0 -> { // obelisk
                c.fill(x - 1, floor - 1, z - 1, x + 1, floor - 1, z + 1, s(Blocks.SMOOTH_SANDSTONE));
                c.fill(x, floor, z, x, floor + 7, z, stone);
                c.set(x, floor + 3, z, glyph);
                c.set(x, floor + 8, z, s(Blocks.GOLD_BLOCK));
            }
            case 1 -> { // ring of columns around a plinth
                for (int[] k : new int[][]{{-2, -2}, {2, -2}, {-2, 2}, {2, 2}}) {
                    int h = 2 + (int) (Noise.rand(x + k[0], z + k[1], 251) * 4);
                    c.fill(x + k[0], floor, z + k[1], x + k[0], floor + h, z + k[1], s(Blocks.SANDSTONE_WALL));
                }
                c.set(x, floor, z, glyph);
                c.chest(x, floor + 1, z, Direction.NORTH, null, new ItemStack(ModItems.ANCIENT_COIN, 2 + (int) (Noise.rand(x, z, 252) * 4)));
            }
            case 2 -> { // fallen statue: a head on its side
                c.fill(x - 1, floor, z - 1, x + 1, floor + 2, z + 1, s(Blocks.SMOOTH_SANDSTONE));
                c.set(x - 1, floor + 1, z - 1, s(Blocks.LAPIS_BLOCK));
                c.set(x + 1, floor + 1, z - 1, s(Blocks.LAPIS_BLOCK));
                c.fill(x - 1, floor, z + 2, x + 1, floor, z + 3, s(Blocks.CUT_SANDSTONE_SLAB));
            }
            default -> { // collapsed doorway with a buried cache
                c.fill(x - 2, floor, z, x - 2, floor + 3, z, stone);
                c.fill(x + 2, floor, z, x + 2, floor + 3, z, stone);
                c.fill(x - 2, floor + 4, z, x + 1, floor + 4, z, glyph);
                c.chest(x, floor - 1, z, Direction.NORTH, null, new ItemStack(ModItems.ANCIENT_COIN, 4),
                        new ItemStack(ModItems.ANCIENT_RELIC));
            }
        }
    }

    // =====================================================================
    // Blackveil Atoll: the spirit shrine
    // =====================================================================

    private static void blackveil() {
        add(BVX - 7, BVZ - 7, BVX + 7, BVZ + 7, c -> {
            int floor = Archipelago.WATER_TOP + 2;
            c.columns(BVX - 6, BVZ - 6, BVX + 6, BVZ + 6, (x, z) -> {
                double d = Math.hypot(x - BVX, z - BVZ);
                if (d > 6.5) return;
                int top = Archipelago.column(x, z).top();
                for (int y = top + 1; y < floor; y++) c.set(x, y, z, s(Blocks.DEEPSLATE_BRICKS));
                c.set(x, floor - 1, z, s(d < 2 ? Blocks.CRYING_OBSIDIAN : Blocks.POLISHED_DEEPSLATE));
                clear(c, x, floor, z, x, floor + 6, z);
            });
            for (int i = 0; i < 6; i++) {
                double a = i * Math.PI / 3;
                int px = BVX + (int) Math.round(Math.cos(a) * 5), pz = BVZ + (int) Math.round(Math.sin(a) * 5);
                c.fill(px, floor, pz, px, floor + 3, pz, s(Blocks.OBSIDIAN));
                c.set(px, floor + 4, pz, s(Blocks.SOUL_LANTERN));
            }
            c.set(BVX, floor - 1, BVZ, s(Blocks.SOUL_SOIL));
            c.set(BVX, floor, BVZ, s(Blocks.SOUL_FIRE));
            c.chest(BVX + 2, floor, BVZ, Direction.WEST, null, new ItemStack(ModItems.ANCIENT_RELIC, 2),
                    new ItemStack(Items.ECHO_SHARD, 4), new ItemStack(ModItems.DOUBLOON, 40));
            boss(c, BVX - 2.5, floor, BVZ + 0.5, w -> EntityTypes.WITCH.create(w, EntitySpawnReason.EVENT), "The Veil Witch", 160, 1.15, null);
        });
        add(BVX - 32, BVZ + 16, BVX - 16, BVZ + 22, c -> Builds.shipwreck(c, BVX - 31, BVZ + 19));
    }

    // =====================================================================
    // The Last Horizon: the gate at the edge of the chart
    // =====================================================================

    private static void lastHorizon() {
        add(LHX - 8, LHZ - 6, LHX + 8, LHZ + 6, c -> horizonGate(c, LHX, LHZ));
        add(LHX + 10, LHZ - 20, LHX + 20, LHZ - 10, c -> observatory(c, LHX + 15, LHZ - 15));
        add(LHX - 24, LHZ + 8, LHX - 16, LHZ + 16, c -> {
            tent(c, LHX - 21, LHZ + 8, s(Blocks.WOOL.pick(DyeColor.BLUE)));
            campfire(c, LHX - 20, LHZ + 14);
            Builds.spawnNpc(c, Role.WAYFARER);
        });
    }

    private static void horizonGate(Canvas c, int cx, int cz) {
        int floor = floorAt(cx, cz);
        c.foundation(cx - 7, cz - 5, cx + 7, cz + 5, floor, s(Blocks.STONE_BRICKS));
        c.fill(cx - 7, floor - 1, cz - 5, cx + 7, floor - 1, cz + 5, s(Blocks.SMOOTH_QUARTZ));
        clear(c, cx - 7, floor, cz - 5, cx + 7, floor + 16, cz + 5);
        // Two great pillars and a lintel facing the open sea to the south
        for (int px : new int[]{cx - 5, cx + 5}) {
            c.fill(px - 1, floor, cz - 1, px + 1, floor + 12, cz + 1, s(Blocks.QUARTZ_PILLAR));
            c.fill(px - 1, floor + 13, cz - 1, px + 1, floor + 13, cz + 1, s(Blocks.CHISELED_QUARTZ_BLOCK));
        }
        c.fill(cx - 6, floor + 14, cz - 1, cx + 6, floor + 15, cz + 1, s(Blocks.SMOOTH_QUARTZ));
        c.fill(cx - 2, floor + 14, cz - 1, cx + 2, floor + 14, cz - 1, s(Blocks.GOLD_BLOCK));
        // The sealed way: seven empty sockets around the threshold
        for (int i = 0; i < 7; i++) {
            c.set(cx - 3 + i, floor - 1, cz + 3, s(Blocks.END_PORTAL_FRAME));
        }
        c.fill(cx - 4, floor, cz, cx + 4, floor + 12, cz, s(Blocks.IRON_BARS));
        c.set(cx, floor, cz - 3, s(Blocks.LECTERN));
    }

    private static void observatory(Canvas c, int cx, int cz) {
        int floor = floorAt(cx, cz);
        c.foundation(cx - 5, cz - 5, cx + 5, cz + 5, floor, s(Blocks.STONE_BRICKS));
        c.columns(cx - 5, cz - 5, cx + 5, cz + 5, (x, z) -> {
            double d = Math.hypot(x - cx, z - cz);
            if (d > 5.2) return;
            c.set(x, floor - 1, z, s(Blocks.POLISHED_ANDESITE));
            for (int y = floor; y <= floor + 9; y++) {
                double dome = Math.sqrt(Math.max(0, 25 - (y - floor - 3) * (y - floor - 3) * 1.6));
                boolean shell = y <= floor + 3 ? d > 4.2 : d <= dome + 0.5 && d > dome - 0.7;
                boolean broken = Noise.rand(x, y, z, 261) < 0.3 && y > floor + 4;
                c.set(x, y, z, shell && !broken ? s(Blocks.CUT_COPPER.weathering().weathered()) : s(Blocks.AIR));
            }
        });
        Builds.doorway(c, cx, cz + 5, floor);
        // The great glass: a copper tube aimed at the sky
        c.set(cx, floor, cz, s(Blocks.CHISELED_STONE_BRICKS));
        c.fill(cx, floor + 1, cz, cx, floor + 3, cz - 2, s(Blocks.LIGHTNING_ROD.weathering().unaffected()));
        c.chest(cx + 3, floor, cz, Direction.WEST, null, new ItemStack(ModItems.ANCIENT_RELIC),
                new ItemStack(ModItems.MAP_FRAGMENT), new ItemStack(Items.SPYGLASS));
    }

    // =====================================================================
    // Crooked Reef: the ship graveyard
    // =====================================================================

    private static final int[][] WRECKS = {{-30, -10}, {-12, 20}, {12, -26}, {26, 10}, {-26, 30}, {-6, -4}};

    private static void crookedReef() {
        for (int[] w : WRECKS) {
            int x0 = CRX + w[0], wz = CRZ + w[1];
            add(x0, wz - 3, x0 + 14, wz + 3, c -> {
                Builds.shipwreck(c, x0, wz);
                int y = Archipelago.column(x0 + 9, wz).top() + 1;
                c.spawn(x0 + 9.5, y, wz + 0.5, 0, wr -> EntityTypes.DROWNED.create(wr, EntitySpawnReason.EVENT), e -> {
                    if (e instanceof Mob m) m.setPersistenceRequired();
                });
            });
        }
        // The salvager's raft-house, moored over the reef
        int sx = CRX - 36, sz = CRZ;
        add(sx - 5, sz - 5, sx + 5, sz + 5, c -> {
            c.columns(sx - 4, sz - 4, sx + 4, sz + 4, (x, z) -> {
                int top = Archipelago.column(x, z).top();
                if (Math.abs(x - sx) == 4 && Math.abs(z - sz) == 4) c.fill(x, top + 1, z, x, 62, z, s(Blocks.STRIPPED_SPRUCE_LOG));
                c.set(x, 63, z, s(Blocks.SPRUCE_PLANKS));
            });
            c.walls(sx - 2, 64, sz - 2, sx + 2, 66, sz + 2, s(Blocks.SPRUCE_PLANKS));
            clear(c, sx - 1, 64, sz - 1, sx + 1, 66, sz + 1);
            Builds.doorway(c, sx, sz + 2, 64);
            c.fill(sx - 3, 67, sz - 3, sx + 3, 67, sz + 3, s(Blocks.SPRUCE_SLAB));
            c.set(sx - 1, 64, sz - 1, s(Blocks.BARREL));
            c.set(sx + 1, 66, sz, Builds.hangingLantern());
            c.set(sx + 4, 64, sz - 4, s(Blocks.IRON_CHAIN));
            vendor(c, sx + 0.5, 64, sz + 3.5, "Salvager Nan Brightwater");
            c.spawn(sx + 6.5, 63, sz + 0.5, 0, w -> EntityTypes.OAK_BOAT.create(w, EntitySpawnReason.STRUCTURE));
        });
    }
}
