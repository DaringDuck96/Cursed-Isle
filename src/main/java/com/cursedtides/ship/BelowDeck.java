package com.cursedtides.ship;

import com.cursedtides.CursedTides;
import com.cursedtides.entity.ShipEntity;
import com.cursedtides.social.CrewState;
import com.cursedtides.ui.UiData;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.LanternBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.item.BlockItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Below deck. Each player ship has her own interior in the "below deck" dimension, built to her
 * class: captain's quarters, hold, galley, crew quarters, brig, chart room, and a gun deck on the
 * big ships. Going below drops anchor and keeps her chunk loaded; the trapdoor (or H) takes you up.
 * Owners and crew may refit the interior as they like.
 */
public final class BelowDeck {
    private BelowDeck() {}

    public static final RegistryKey<World> WORLD = RegistryKey.of(RegistryKeys.WORLD, CursedTides.id("below_deck"));
    private static final int CELL = 64, FLOOR = 64, WIDTH = 9, ROOM = 7;

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (world.isClient || world.getRegistryKey() != WORLD || !(player instanceof ServerPlayerEntity sp)) return ActionResult.PASS;
            BlockPos pos = hit.getBlockPos();
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof TrapdoorBlock) {
                surface(sp);
                return ActionResult.SUCCESS;
            }
            ShipEntity ship = shipFor(sp.getServer(), cellOf(pos.getX()));
            if (state.isOf(Blocks.BARREL) && isHoldHatch(pos)) {
                if (ship != null && mayUse(sp, ship)) ship.openCargo(sp);
                else sp.sendMessage(Text.literal("The hold is shut (is she still afloat?).").formatted(Formatting.GRAY), true);
                return ActionResult.SUCCESS;
            }
            if (state.isOf(Blocks.CARTOGRAPHY_TABLE)) {
                UiData.sendMap(sp);
                return ActionResult.SUCCESS;
            }
            if (state.isOf(Blocks.LECTERN)) {
                UiData.sendJournal(sp);
                return ActionResult.SUCCESS;
            }
            boolean placing = player.getStackInHand(hand).getItem() instanceof BlockItem;
            if (placing && !mayBuild(sp, pos.offset(hit.getSide()))) {
                sp.sendMessage(Text.literal("This isn't your ship to refit.").formatted(Formatting.RED), true);
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, be) -> {
            if (world.getRegistryKey() != WORLD || !(player instanceof ServerPlayerEntity sp)) return true;
            if (mayBuild(sp, pos)) return true;
            sp.sendMessage(Text.literal("This isn't your ship to refit.").formatted(Formatting.RED), true);
            return false;
        });
    }

    // ------------------------------------------------------------------ going below and coming up

    /** The H key: below if on deck, up if below. */
    public static void toggle(ServerPlayerEntity player) {
        if (player.getWorld().getRegistryKey() == WORLD) {
            surface(player);
            return;
        }
        ShipEntity ship = player.getVehicle() instanceof ShipEntity s ? s : null;
        if (ship == null) {
            var nearby = player.getWorld().getEntitiesByClass(ShipEntity.class, player.getBoundingBox().expand(6), s -> !s.isHostile());
            ship = nearby.isEmpty() ? null : nearby.get(0);
        }
        if (ship == null || ship.isHostile()) {
            player.sendMessage(Text.literal("You need to be aboard a ship to go below.").formatted(Formatting.GRAY), true);
            return;
        }
        if (!mayUse(player, ship)) {
            player.sendMessage(Text.literal("Only her captain and crew go below.").formatted(Formatting.GRAY), true);
            return;
        }
        MinecraftServer server = player.getServer();
        ServerWorld below = server.getWorld(WORLD);
        if (below == null) {
            player.sendMessage(Text.literal("The below-deck world isn't loaded (is the mod's data pack enabled?).").formatted(Formatting.RED), false);
            return;
        }
        State s = State.get(server);
        int cell = s.cellFor(ship.getUuid());
        int built = s.builtClass.getOrDefault(cell, -1);
        if (built != ship.shipClass().ordinal()) {
            build(below, cell, ship.shipClass());
            s.builtClass.put(cell, ship.shipClass().ordinal());
        }
        s.lastPos.put(ship.getUuid(), new double[]{ship.getX(), ship.getY(), ship.getZ()});
        s.markDirty();
        ChunkPos chunk = ship.getChunkPos();
        ((ServerWorld) ship.getWorld()).setChunkForced(chunk.x, chunk.z, true);
        ship.setAnchored(true);
        ship.setSail(0);
        player.stopRiding();
        int x0 = cell * CELL;
        player.teleport(below, x0 + 2.5, FLOOR, WIDTH / 2 + 0.5, -90.0f, 0.0f);
        below.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN, SoundCategory.PLAYERS, 1.0f, 0.9f);
        player.sendMessage(Text.literal("You go below. She rides at anchor while you're down here. (The trapdoor or H takes you up.)")
                .formatted(Formatting.GRAY), true);
    }

    public static void surface(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        State s = State.get(server);
        int cell = cellOf(player.getBlockX());
        UUID shipId = s.shipByCell(cell);
        ServerWorld overworld = server.getOverworld();
        ShipEntity ship = shipId == null ? null : overworld.getEntity(shipId) instanceof ShipEntity se ? se : null;
        if (ship != null) {
            double y = ship.getY() + 0.9 * ship.shipClass().scale();
            player.teleport(overworld, ship.getX(), y, ship.getZ(), ship.getYaw(), 0.0f);
            if (ship.getPassengerList().size() < ship.shipClass().seats()) player.startRiding(ship, true);
        } else {
            double[] last = shipId == null ? null : s.lastPos.get(shipId);
            BlockPos spawn = overworld.getSpawnPos();
            if (last != null) player.teleport(overworld, last[0], last[1] + 2, last[2], player.getYaw(), 0.0f);
            else player.teleport(overworld, spawn.getX() + 0.5, spawn.getY() + 1, spawn.getZ() + 0.5, player.getYaw(), 0.0f);
            player.sendMessage(Text.literal("You come up to find your ship gone.").formatted(Formatting.RED), false);
        }
        // Let her chunk go once nobody is left below
        ServerWorld below = server.getWorld(WORLD);
        boolean others = below != null && below.getPlayers(p -> p != player && cellOf(p.getBlockX()) == cell).size() > 0;
        if (!others && shipId != null && s.lastPos.containsKey(shipId)) {
            double[] last = s.lastPos.get(shipId);
            ChunkPos chunk = new ChunkPos(BlockPos.ofFloored(last[0], 0, last[2]));
            overworld.setChunkForced(chunk.x, chunk.z, false);
        }
    }

    // ------------------------------------------------------------------ permissions

    private static int cellOf(int x) {
        return Math.floorDiv(x, CELL);
    }

    private static boolean mayUse(ServerPlayerEntity p, ShipEntity ship) {
        return ship.isOwner(p) || CrewState.shipmates(p.getServer(), ship.getOwner(), p.getUuid());
    }

    private static boolean mayBuild(ServerPlayerEntity p, BlockPos pos) {
        if (p.isCreative()) return true;
        int cell = cellOf(pos.getX());
        UUID shipId = State.get(p.getServer()).shipByCell(cell);
        if (shipId == null) return false;
        ShipEntity ship = p.getServer().getOverworld().getEntity(shipId) instanceof ShipEntity se ? se : null;
        boolean allowed = ship != null && mayUse(p, ship);
        int localX = pos.getX() - cell * CELL;
        return allowed && localX >= 1 && localX < CELL - 8 && pos.getZ() >= 1 && pos.getZ() < WIDTH + 1;
    }

    private static boolean isHoldHatch(BlockPos pos) {
        return pos.getZ() == 1 && pos.getY() == FLOOR;
    }

    private static ShipEntity shipFor(MinecraftServer server, int cell) {
        UUID id = State.get(server).shipByCell(cell);
        return id != null && server.getOverworld().getEntity(id) instanceof ShipEntity s ? s : null;
    }

    // ------------------------------------------------------------------ building the interior

    private static BlockState s(net.minecraft.block.Block b) {
        return b.getDefaultState();
    }

    private static void build(ServerWorld w, int cell, ShipClass cls) {
        int x0 = cell * CELL;
        String[] rooms = switch (cls) {
            case SLOOP -> new String[]{"quarters", "hold"};
            case SCHOONER -> new String[]{"quarters", "galley", "hold"};
            case BRIGANTINE -> new String[]{"quarters", "crew", "galley", "hold"};
            case BRIG -> new String[]{"quarters", "crew", "galley", "brig", "hold"};
            case FRIGATE, GALLEON -> new String[]{"quarters", "chart", "crew", "galley", "brig", "hold"};
        };
        boolean gunDeck = cls == ShipClass.FRIGATE || cls == ShipClass.GALLEON || cls == ShipClass.BRIG;
        int length = rooms.length * (ROOM + 1) + 1;
        int levels = gunDeck ? 2 : 1;
        int height = 5;
        // Clear the whole cell and lay the hull
        for (int x = x0; x < x0 + CELL - 4; x++) {
            for (int y = FLOOR - 2; y < FLOOR + height * levels + 3; y++) {
                for (int z = -1; z <= WIDTH + 2; z++) set(w, x, y, z, s(Blocks.AIR));
            }
        }
        for (int level = 0; level < levels; level++) {
            int fy = FLOOR + level * height;
            for (int x = x0; x <= x0 + length; x++) {
                for (int z = 0; z <= WIDTH + 1; z++) {
                    boolean wall = x == x0 || x == x0 + length || z == 0 || z == WIDTH + 1;
                    set(w, x, fy - 1, z, s(Blocks.SPRUCE_PLANKS));
                    for (int y = fy; y < fy + height - 1; y++) {
                        set(w, x, y, z, wall ? s(Blocks.DARK_OAK_PLANKS) : s(Blocks.AIR));
                    }
                    set(w, x, fy + height - 1, z, (x - x0) % 4 == 0 ? s(Blocks.STRIPPED_DARK_OAK_LOG) : s(Blocks.DARK_OAK_PLANKS));
                }
            }
            // Portholes looking out on dark water
            for (int x = x0 + 3; x < x0 + length; x += 4) {
                for (int z : new int[]{0, WIDTH + 1}) {
                    set(w, x, fy + 1, z, s(Blocks.LIGHT_BLUE_STAINED_GLASS));
                    set(w, x, fy + 1, z + (z == 0 ? -1 : 1), s(Blocks.DARK_PRISMARINE));
                }
            }
        }
        // Rooms, divided by bulkheads with doorways down the middle
        for (int i = 0; i < rooms.length; i++) {
            int rx = x0 + 1 + i * (ROOM + 1);
            if (i > 0) {
                for (int z = 1; z <= WIDTH; z++) {
                    for (int y = FLOOR; y < FLOOR + height - 1; y++) {
                        boolean door = z >= WIDTH / 2 && z <= WIDTH / 2 + 1 && y < FLOOR + 2;
                        set(w, rx - 1, y, z, door ? s(Blocks.AIR) : s(Blocks.SPRUCE_PLANKS));
                    }
                }
            }
            furnish(w, rooms[i], rx, cls);
            set(w, rx + ROOM / 2, FLOOR + height - 2, WIDTH / 2 + 1, s(Blocks.LANTERN).with(LanternBlock.HANGING, true));
        }
        // The hatch: a ladder up to a trapdoor, in the first room
        for (int y = FLOOR; y < FLOOR + height * levels - 1; y++) {
            set(w, x0 + 1, y, WIDTH / 2 + 1, s(Blocks.LADDER).with(LadderBlock.FACING, Direction.EAST));
        }
        for (int level = 1; level < levels; level++) {
            set(w, x0 + 1, FLOOR + level * height - 1, WIDTH / 2 + 1, s(Blocks.AIR));
        }
        set(w, x0 + 1, FLOOR + height * levels - 1, WIDTH / 2 + 1, s(Blocks.SPRUCE_TRAPDOOR)
                .with(TrapdoorBlock.HALF, BlockHalf.TOP).with(TrapdoorBlock.FACING, Direction.EAST));
        if (gunDeck) {
            // Upper level: the gun deck, a row of cannons down each side and shot in racks
            int fy = FLOOR + height;
            for (int x = x0 + 4; x < x0 + length - 1; x += 3) {
                for (int z : new int[]{1, WIDTH}) {
                    set(w, x, fy, z, s(Blocks.SPRUCE_SLAB));
                    set(w, x, fy + 1, z, s(Blocks.BLACK_CONCRETE));
                    set(w, x, fy + 1, z == 1 ? 0 : WIDTH + 1, s(Blocks.IRON_BARS)); // gun port
                }
                set(w, x, fy, WIDTH / 2 - 1, s(Blocks.BLACKSTONE_SLAB)); // shot rack
            }
            set(w, x0 + length - 2, fy, WIDTH / 2 + 1, s(Blocks.BARREL));
            set(w, x0 + length - 2, fy, WIDTH / 2, s(Blocks.TNT));
            set(w, x0 + length / 2, fy + height - 2, WIDTH / 2 + 1, s(Blocks.LANTERN).with(LanternBlock.HANGING, true));
        }
    }

    private static void furnish(ServerWorld w, String room, int rx, ShipClass cls) {
        int y = FLOOR, mid = WIDTH / 2 + 1;
        switch (room) {
            case "quarters" -> {
                // Captain's quarters: desk and chair, the ship's log, a bed, a rug and a chest
                for (int x = rx + 2; x <= rx + 5; x++) for (int z = 3; z <= WIDTH - 2; z++) set(w, x, y - 1, z, s(Blocks.RED_WOOL));
                set(w, rx + 4, y, 2, s(Blocks.DARK_OAK_SLAB));
                set(w, rx + 5, y, 2, s(Blocks.LECTERN));
                set(w, rx + 4, y, 3, s(Blocks.DARK_OAK_STAIRS).with(StairsBlock.FACING, Direction.NORTH));
                bed(w, rx + 6, y, WIDTH - 1, Direction.WEST);
                set(w, rx + 6, y, 1, s(Blocks.CHEST));
                set(w, rx + 3, y, WIDTH, s(Blocks.BOOKSHELF));
            }
            case "chart" -> {
                set(w, rx + 3, y, mid, s(Blocks.CARTOGRAPHY_TABLE));
                set(w, rx + 3, y, mid - 1, s(Blocks.LODESTONE));
                set(w, rx + 1, y, 1, s(Blocks.LECTERN));
                for (int x = rx + 1; x <= rx + 6; x++) set(w, x, y + 2, 1, s(Blocks.BOOKSHELF));
            }
            case "crew" -> {
                for (int x = rx + 1; x <= rx + 5; x += 2) {
                    bed(w, x, y, 2, Direction.SOUTH);
                    bed(w, x, y, WIDTH - 1, Direction.NORTH);
                }
                set(w, rx + 6, y, mid, s(Blocks.BARREL));
            }
            case "galley" -> {
                set(w, rx + 1, y, 1, s(Blocks.SMOKER));
                set(w, rx + 2, y, 1, s(Blocks.CRAFTING_TABLE));
                set(w, rx + 3, y, 1, s(Blocks.CAULDRON));
                set(w, rx + 4, y, 1, s(Blocks.BARREL));
                set(w, rx + 5, y, 1, s(Blocks.BREWING_STAND));
                set(w, rx + 3, y, WIDTH, s(Blocks.SPRUCE_SLAB));
                set(w, rx + 4, y, WIDTH, s(Blocks.SPRUCE_SLAB));
            }
            case "brig" -> {
                for (int z = 1; z <= 4; z++) for (int yy = y; yy < y + 3; yy++) set(w, rx + 3, yy, z, s(Blocks.IRON_BARS));
                set(w, rx + 3, y, 3, s(Blocks.AIR));
                set(w, rx + 3, y + 1, 3, s(Blocks.AIR));
                set(w, rx + 5, y, 1, s(Blocks.CHAIN));
                set(w, rx + 1, y, 1, s(Blocks.HAY_BLOCK));
            }
            case "hold" -> {
                // The hold hatch (barrel at z=1) opens the ship's real cargo
                set(w, rx + 3, y, 1, s(Blocks.BARREL));
                for (int x = rx; x <= rx + 6; x++) {
                    if (x != rx + 3) set(w, x, y, 1, s(Blocks.SPRUCE_PLANKS));
                    if ((x - rx) % 2 == 0) {
                        set(w, x, y, WIDTH, s(Blocks.BARREL));
                        set(w, x, y + 1, WIDTH, s(Blocks.HAY_BLOCK));
                    }
                }
            }
            default -> { }
        }
    }

    private static void bed(ServerWorld w, int x, int y, int z, Direction facing) {
        BlockPos foot = new BlockPos(x, y, z);
        BlockPos head = foot.offset(facing);
        set(w, foot.getX(), y, foot.getZ(), s(Blocks.BLUE_BED).with(BedBlock.FACING, facing).with(BedBlock.PART, BedPart.FOOT));
        set(w, head.getX(), y, head.getZ(), s(Blocks.BLUE_BED).with(BedBlock.FACING, facing).with(BedBlock.PART, BedPart.HEAD));
    }

    private static void set(ServerWorld w, int x, int y, int z, BlockState state) {
        w.setBlockState(new BlockPos(x, y, z), state, 2 | 16);
    }

    // ------------------------------------------------------------------ saved state

    static final class State extends PersistentState {
        final Map<UUID, Integer> cells = new HashMap<>();
        final Map<Integer, Integer> builtClass = new HashMap<>();
        final Map<UUID, double[]> lastPos = new HashMap<>();
        int nextCell;
        private static final Type<State> TYPE = new Type<>(State::new, State::fromNbt, null);

        static State get(MinecraftServer server) {
            return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, "cursedtides_below_deck");
        }

        int cellFor(UUID ship) {
            return cells.computeIfAbsent(ship, k -> { markDirty(); return nextCell++; });
        }

        UUID shipByCell(int cell) {
            for (var e : cells.entrySet()) if (e.getValue() == cell) return e.getKey();
            return null;
        }

        @Override
        public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
            nbt.putInt("Next", nextCell);
            NbtList list = new NbtList();
            cells.forEach((id, cell) -> {
                NbtCompound c = new NbtCompound();
                c.putUuid("Ship", id);
                c.putInt("Cell", cell);
                c.putInt("Built", builtClass.getOrDefault(cell, -1));
                double[] p = lastPos.get(id);
                if (p != null) {
                    c.putDouble("X", p[0]);
                    c.putDouble("Y", p[1]);
                    c.putDouble("Z", p[2]);
                }
                list.add(c);
            });
            nbt.put("Cells", list);
            return nbt;
        }

        private static State fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
            State s = new State();
            s.nextCell = nbt.getInt("Next");
            NbtList list = nbt.getList("Cells", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < list.size(); i++) {
                NbtCompound c = list.getCompound(i);
                UUID id = c.getUuid("Ship");
                int cell = c.getInt("Cell");
                s.cells.put(id, cell);
                if (c.getInt("Built") >= 0) s.builtClass.put(cell, c.getInt("Built"));
                if (c.contains("X")) s.lastPos.put(id, new double[]{c.getDouble("X"), c.getDouble("Y"), c.getDouble("Z")});
            }
            return s;
        }
    }
}
