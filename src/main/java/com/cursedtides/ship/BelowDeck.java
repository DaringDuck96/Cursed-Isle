package com.cursedtides.ship;

import net.minecraft.world.item.DyeColor;
import com.cursedtides.CursedTides;
import com.cursedtides.entity.ShipEntity;
import com.cursedtides.social.CrewState;
import com.cursedtides.ui.UiData;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.saveddata.SavedData;
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

    public static final ResourceKey<Level> WORLD = ResourceKey.create(Registries.DIMENSION, CursedTides.id("below_deck"));
    private static final int CELL = 64, FLOOR = 64, WIDTH = 9, ROOM = 7;

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (world.isClientSide() || world.dimension() != WORLD || !(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
            BlockPos pos = hit.getBlockPos();
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof TrapDoorBlock) {
                surface(sp);
                return InteractionResult.SUCCESS;
            }
            ShipEntity ship = shipFor(sp.level().getServer(), cellOf(pos.getX()));
            if (state.is(Blocks.BARREL) && isHoldHatch(pos)) {
                if (ship != null && mayUse(sp, ship)) ship.openCargo(sp);
                else sp.sendSystemMessage(Component.literal("The hold is shut (is she still afloat?).").withStyle(ChatFormatting.GRAY), true);
                return InteractionResult.SUCCESS;
            }
            if (state.is(Blocks.CARTOGRAPHY_TABLE)) {
                UiData.sendMap(sp);
                return InteractionResult.SUCCESS;
            }
            if (state.is(Blocks.LECTERN)) {
                UiData.sendJournal(sp);
                return InteractionResult.SUCCESS;
            }
            boolean placing = player.getItemInHand(hand).getItem() instanceof BlockItem;
            if (placing && !mayBuild(sp, pos.relative(hit.getDirection()))) {
                sp.sendSystemMessage(Component.literal("This isn't your ship to refit.").withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, be) -> {
            if (world.dimension() != WORLD || !(player instanceof ServerPlayer sp)) return true;
            if (mayBuild(sp, pos)) return true;
            sp.sendSystemMessage(Component.literal("This isn't your ship to refit.").withStyle(ChatFormatting.RED), true);
            return false;
        });
    }

    // ------------------------------------------------------------------ going below and coming up

    /** The H key: below if on deck, up if below. */
    public static void toggle(ServerPlayer player) {
        if (player.level().dimension() == WORLD) {
            surface(player);
            return;
        }
        ShipEntity ship = player.getVehicle() instanceof ShipEntity s ? s : null;
        if (ship == null) {
            var nearby = player.level().getEntitiesOfClass(ShipEntity.class, player.getBoundingBox().inflate(6), s -> !s.isHostile());
            ship = nearby.isEmpty() ? null : nearby.get(0);
        }
        if (ship == null || ship.isHostile()) {
            player.sendSystemMessage(Component.literal("You need to be aboard a ship to go below.").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        if (!mayUse(player, ship)) {
            player.sendSystemMessage(Component.literal("Only her captain and crew go below.").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        MinecraftServer server = player.level().getServer();
        ServerLevel below = server.getLevel(WORLD);
        if (below == null) {
            player.sendSystemMessage(Component.literal("The below-deck world isn't loaded (is the mod's data pack enabled?).").withStyle(ChatFormatting.RED), false);
            return;
        }
        State s = State.get(server);
        int cell = s.cellFor(ship.getUUID());
        int built = s.builtClass.getOrDefault(cell, -1);
        if (built != ship.shipClass().ordinal()) {
            build(below, cell, ship.shipClass());
            s.builtClass.put(cell, ship.shipClass().ordinal());
        }
        s.lastPos.put(ship.getUUID(), new double[]{ship.getX(), ship.getY(), ship.getZ()});
        s.setDirty();
        ChunkPos chunk = ship.chunkPosition();
        ((ServerLevel) ship.level()).setChunkForced(chunk.x(), chunk.z(), true);
        ship.setAnchored(true);
        ship.setSail(0);
        player.stopRiding();
        int x0 = cell * CELL;
        com.cursedtides.util.Compat.teleport(player, below, x0 + 2.5, FLOOR, WIDTH / 2 + 0.5, -90.0f, 0.0f);
        below.playSound(null, player.blockPosition(), SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundSource.PLAYERS, 1.0f, 0.9f);
        player.sendSystemMessage(Component.literal("You go below. She rides at anchor while you're down here. (The trapdoor or H takes you up.)")
                .withStyle(ChatFormatting.GRAY), true);
    }

    public static void surface(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        State s = State.get(server);
        int cell = cellOf(player.getBlockX());
        UUID shipId = s.shipByCell(cell);
        ServerLevel overworld = server.overworld();
        ShipEntity ship = shipId == null ? null : overworld.getEntity(shipId) instanceof ShipEntity se ? se : null;
        if (ship != null) {
            double y = ship.getY() + 0.9 * ship.shipClass().scale();
            com.cursedtides.util.Compat.teleport(player, overworld, ship.getX(), y, ship.getZ(), ship.getYRot(), 0.0f);
            if (ship.getPassengers().size() < ship.shipClass().seats()) player.startRiding(ship, true, true);
        } else {
            double[] last = shipId == null ? null : s.lastPos.get(shipId);
            BlockPos spawn = overworld.getRespawnData().pos();
            if (last != null) com.cursedtides.util.Compat.teleport(player, overworld, last[0], last[1] + 2, last[2], player.getYRot(), 0.0f);
            else com.cursedtides.util.Compat.teleport(player, overworld, spawn.getX() + 0.5, spawn.getY() + 1, spawn.getZ() + 0.5, player.getYRot(), 0.0f);
            player.sendSystemMessage(Component.literal("You come up to find your ship gone.").withStyle(ChatFormatting.RED), false);
        }
        // Let her chunk go once nobody is left below
        ServerLevel below = server.getLevel(WORLD);
        boolean others = below != null && below.getPlayers(p -> p != player && cellOf(p.getBlockX()) == cell).size() > 0;
        if (!others && shipId != null && s.lastPos.containsKey(shipId)) {
            double[] last = s.lastPos.get(shipId);
            ChunkPos chunk = ChunkPos.containing(BlockPos.containing(last[0], 0, last[2]));
            overworld.setChunkForced(chunk.x(), chunk.z(), false);
        }
    }

    // ------------------------------------------------------------------ permissions

    private static int cellOf(int x) {
        return Math.floorDiv(x, CELL);
    }

    private static boolean mayUse(ServerPlayer p, ShipEntity ship) {
        return ship.isOwner(p) || CrewState.shipmates(p.level().getServer(), ship.getOwner(), p.getUUID());
    }

    private static boolean mayBuild(ServerPlayer p, BlockPos pos) {
        if (p.isCreative()) return true;
        int cell = cellOf(pos.getX());
        UUID shipId = State.get(p.level().getServer()).shipByCell(cell);
        if (shipId == null) return false;
        ShipEntity ship = p.level().getServer().overworld().getEntity(shipId) instanceof ShipEntity se ? se : null;
        boolean allowed = ship != null && mayUse(p, ship);
        int localX = pos.getX() - cell * CELL;
        return allowed && localX >= 1 && localX < CELL - 8 && pos.getZ() >= 1 && pos.getZ() < WIDTH + 1;
    }

    private static boolean isHoldHatch(BlockPos pos) {
        return pos.getZ() == 1 && pos.getY() == FLOOR;
    }

    private static ShipEntity shipFor(MinecraftServer server, int cell) {
        UUID id = State.get(server).shipByCell(cell);
        return id != null && server.overworld().getEntity(id) instanceof ShipEntity s ? s : null;
    }

    // ------------------------------------------------------------------ building the interior

    private static BlockState s(net.minecraft.world.level.block.Block b) {
        return b.defaultBlockState();
    }

    private static void build(ServerLevel w, int cell, ShipClass cls) {
        int x0 = cell * CELL;
        String[] rooms = switch (cls) {
            case SLOOP -> new String[]{"quarters", "hold"};
            case SCHOONER -> new String[]{"quarters", "galley", "hold"};
            case BRIGANTINE -> new String[]{"quarters", "crew", "galley", "hold"};
            case BRIG -> new String[]{"quarters", "crew", "galley", "brig", "hold"};
            case FRIGATE, GALLEON -> new String[]{"quarters", "chart", "crew", "galley", "brig", "hold"};
            case SHIP_OF_THE_LINE -> new String[]{"quarters", "chart", "crew", "crew", "galley", "brig", "hold"};
        };
        boolean gunDeck = cls == ShipClass.FRIGATE || cls == ShipClass.GALLEON || cls == ShipClass.BRIG || cls == ShipClass.SHIP_OF_THE_LINE;
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
                    set(w, x, fy + 1, z, s(Blocks.STAINED_GLASS.pick(DyeColor.LIGHT_BLUE)));
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
            set(w, rx + ROOM / 2, FLOOR + height - 2, WIDTH / 2 + 1, s(Blocks.LANTERN).setValue(LanternBlock.HANGING, true));
        }
        // The hatch: a ladder up to a trapdoor, in the first room
        for (int y = FLOOR; y < FLOOR + height * levels - 1; y++) {
            set(w, x0 + 1, y, WIDTH / 2 + 1, s(Blocks.LADDER).setValue(LadderBlock.FACING, Direction.EAST));
        }
        for (int level = 1; level < levels; level++) {
            set(w, x0 + 1, FLOOR + level * height - 1, WIDTH / 2 + 1, s(Blocks.AIR));
        }
        set(w, x0 + 1, FLOOR + height * levels - 1, WIDTH / 2 + 1, s(Blocks.SPRUCE_TRAPDOOR)
                .setValue(TrapDoorBlock.HALF, Half.TOP).setValue(TrapDoorBlock.FACING, Direction.EAST));
        if (gunDeck) {
            // Upper level: the gun deck, a row of cannons down each side and shot in racks
            int fy = FLOOR + height;
            for (int x = x0 + 4; x < x0 + length - 1; x += 3) {
                for (int z : new int[]{1, WIDTH}) {
                    set(w, x, fy, z, s(Blocks.SPRUCE_SLAB));
                    set(w, x, fy + 1, z, s(Blocks.CONCRETE.pick(DyeColor.BLACK)));
                    set(w, x, fy + 1, z == 1 ? 0 : WIDTH + 1, s(Blocks.IRON_BARS)); // gun port
                }
                set(w, x, fy, WIDTH / 2 - 1, s(Blocks.BLACKSTONE_SLAB)); // shot rack
            }
            set(w, x0 + length - 2, fy, WIDTH / 2 + 1, s(Blocks.BARREL));
            set(w, x0 + length - 2, fy, WIDTH / 2, s(Blocks.TNT));
            set(w, x0 + length / 2, fy + height - 2, WIDTH / 2 + 1, s(Blocks.LANTERN).setValue(LanternBlock.HANGING, true));
        }
    }

    private static void furnish(ServerLevel w, String room, int rx, ShipClass cls) {
        int y = FLOOR, mid = WIDTH / 2 + 1;
        switch (room) {
            case "quarters" -> {
                // Captain's quarters: desk and chair, the ship's log, a bed, a rug and a chest
                for (int x = rx + 2; x <= rx + 5; x++) for (int z = 3; z <= WIDTH - 2; z++) set(w, x, y - 1, z, s(Blocks.WOOL.pick(DyeColor.RED)));
                set(w, rx + 4, y, 2, s(Blocks.DARK_OAK_SLAB));
                set(w, rx + 5, y, 2, s(Blocks.LECTERN));
                set(w, rx + 4, y, 3, s(Blocks.DARK_OAK_STAIRS).setValue(StairBlock.FACING, Direction.NORTH));
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
                set(w, rx + 5, y, 1, s(Blocks.IRON_CHAIN));
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

    private static void bed(ServerLevel w, int x, int y, int z, Direction facing) {
        BlockPos foot = new BlockPos(x, y, z);
        BlockPos head = foot.relative(facing);
        set(w, foot.getX(), y, foot.getZ(), s(Blocks.BED.pick(DyeColor.BLUE)).setValue(BedBlock.FACING, facing).setValue(BedBlock.PART, BedPart.FOOT));
        set(w, head.getX(), y, head.getZ(), s(Blocks.BED.pick(DyeColor.BLUE)).setValue(BedBlock.FACING, facing).setValue(BedBlock.PART, BedPart.HEAD));
    }

    private static void set(ServerLevel w, int x, int y, int z, BlockState state) {
        w.setBlock(new BlockPos(x, y, z), state, 2 | 16);
    }

    // ------------------------------------------------------------------ saved state

    static final class State extends SavedData {
        final Map<UUID, Integer> cells = new HashMap<>();
        final Map<Integer, Integer> builtClass = new HashMap<>();
        final Map<UUID, double[]> lastPos = new HashMap<>();
        int nextCell;
        private static final net.minecraft.world.level.saveddata.SavedDataType<State> TYPE = com.cursedtides.util.Saved.type("cursedtides_below_deck", State::new, State::fromNbt, (st__, l__) -> st__.save(new CompoundTag(), l__));

        static State get(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(TYPE);
        }

        int cellFor(UUID ship) {
            return cells.computeIfAbsent(ship, k -> { setDirty(); return nextCell++; });
        }

        UUID shipByCell(int cell) {
            for (var e : cells.entrySet()) if (e.getValue() == cell) return e.getKey();
            return null;
        }

                public CompoundTag save(CompoundTag nbt, HolderLookup.Provider lookup) {
            nbt.putInt("Next", nextCell);
            ListTag list = new ListTag();
            cells.forEach((id, cell) -> {
                CompoundTag c = new CompoundTag();
                c.store("Ship", net.minecraft.core.UUIDUtil.CODEC, id);
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

        private static State fromNbt(CompoundTag nbt, HolderLookup.Provider lookup) {
            State s = new State();
            s.nextCell = nbt.getIntOr("Next", 0);
            ListTag list = nbt.getListOrEmpty("Cells");
            for (int i = 0; i < list.size(); i++) {
                CompoundTag c = list.getCompoundOrEmpty(i);
                UUID id = c.read("Ship", net.minecraft.core.UUIDUtil.CODEC).orElse(null);
                int cell = c.getIntOr("Cell", 0);
                s.cells.put(id, cell);
                if (c.getIntOr("Built", 0) >= 0) s.builtClass.put(cell, c.getIntOr("Built", 0));
                if (c.contains("X")) s.lastPos.put(id, new double[]{c.getDoubleOr("X", 0d), c.getDoubleOr("Y", 0d), c.getDoubleOr("Z", 0d)});
            }
            return s;
        }
    }
}
