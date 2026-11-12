package com.cursedtides.home;

import com.cursedtides.CursedTides;
import com.cursedtides.dialogue.Specials;
import com.cursedtides.social.CrewState;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Places of your own. A deed raises a house, a hideout, a tavern or a crew headquarters, and claims the ground:
 * only you (and, for a headquarters, your crew) can break or open things inside. Taverns earn a little every day,
 * more in a thriving port. /home to see what you own, /home go to walk home, /home collect to empty the till.
 */
public class Homes extends PersistentState {
    public enum Kind { HOUSE("House"), HIDEOUT("Hideout"), TAVERN("Tavern"), HQ("Crew Headquarters");
        public final String title;
        Kind(String title) { this.title = title; }
    }

    public static final class Home {
        public final Kind kind; public final UUID owner; public final String ownerName; @Nullable public String crew;
        public final BlockPos min, max, door; public int till; public long lastPaid;
        Home(Kind kind, UUID owner, String ownerName, @Nullable String crew, BlockPos min, BlockPos max, BlockPos door) {
            this.kind = kind; this.owner = owner; this.ownerName = ownerName; this.crew = crew; this.min = min; this.max = max; this.door = door;
        }
        boolean contains(BlockPos p) {
            return p.getX() >= min.getX() && p.getX() <= max.getX() && p.getY() >= min.getY() && p.getY() <= max.getY()
                    && p.getZ() >= min.getZ() && p.getZ() <= max.getZ();
        }
    }

    private final List<Home> homes = new ArrayList<>();
    private static final Map<UUID, Long> LAST_GO = new HashMap<>();

    private static final Type<Homes> TYPE = new Type<>(Homes::new, Homes::fromNbt, null);

    public static Homes get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, "cursedtides_homes");
    }

    public static final Item HOUSE_DEED = deed("house_deed", Kind.HOUSE, Rarity.UNCOMMON);
    public static final Item HIDEOUT_DEED = deed("hideout_deed", Kind.HIDEOUT, Rarity.UNCOMMON);
    public static final Item TAVERN_DEED = deed("tavern_deed", Kind.TAVERN, Rarity.RARE);
    public static final Item HQ_DEED = deed("hq_deed", Kind.HQ, Rarity.EPIC);

    private static Item deed(String name, Kind kind, Rarity r) {
        return Registry.register(Registries.ITEM, CursedTides.id(name), new HomeDeedItem(new Item.Settings().maxCount(1).rarity(r), kind));
    }

    public static int price(Kind k) {
        return switch (k) { case HOUSE -> 60; case HIDEOUT -> 45; case TAVERN -> 150; case HQ -> 300; };
    }

    // ------------------------------------------------------------------ queries

    public static boolean ownsAny(ServerPlayerEntity p) {
        return p.getServer() != null && get(p.getServer()).homes.stream().anyMatch(h -> h.owner.equals(p.getUuid()));
    }

    @Nullable
    public Home at(BlockPos pos) {
        for (Home h : homes) if (h.contains(pos)) return h;
        return null;
    }

    boolean overlaps(BlockPos min, BlockPos max) {
        for (Home h : homes) {
            if (min.getX() <= h.max.getX() + 2 && max.getX() >= h.min.getX() - 2 && min.getZ() <= h.max.getZ() + 2 && max.getZ() >= h.min.getZ() - 2) return true;
        }
        return false;
    }

    static boolean mayUse(MinecraftServer server, Home h, PlayerEntity p) {
        if (p.getUuid().equals(h.owner) || p.hasPermissionLevel(2)) return true;
        if (h.kind == Kind.TAVERN) return false; // anyone may walk in (doors aren't blocked), but not open the stores
        CrewState.Crew crew = CrewState.get(server).crewOf(p.getUuid());
        if (crew == null) return false;
        return CrewState.shipmates(server, h.owner, p.getUuid()) || (h.crew != null && h.crew.equals(crew.name.toLowerCase()));
    }

    // ------------------------------------------------------------------ placing

    static boolean place(ServerPlayerEntity p, Kind kind, BlockPos clicked) {
        MinecraftServer server = p.getServer();
        if (server == null || p.getWorld() != server.getOverworld()) {
            p.sendMessage(Text.literal("Deeds only hold good in the archipelago.").formatted(Formatting.RED), false);
            return false;
        }
        Homes s = get(server);
        long owned = s.homes.stream().filter(h -> h.owner.equals(p.getUuid())).count();
        if (owned >= 3) {
            p.sendMessage(Text.literal("You already hold three properties. /home sell one first.").formatted(Formatting.RED), false);
            return false;
        }
        Blueprints.Size size = Blueprints.size(kind);
        BlockPos origin = clicked.up().add(-size.w() / 2, 0, -size.d() / 2);
        BlockPos min = origin.add(-1, -1, -1), max = origin.add(size.w(), size.h() + 2, size.d());
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                if (com.cursedtides.world.Builds.blocksPlants(x, z)) {
                    p.sendMessage(Text.literal("That ground's spoken for. Build away from the town's own buildings.").formatted(Formatting.RED), false);
                    return false;
                }
            }
        }
        if (s.overlaps(min, max)) {
            p.sendMessage(Text.literal("Too close to someone else's property.").formatted(Formatting.RED), false);
            return false;
        }
        if (Math.abs(clicked.getY() - p.getBlockY()) > 3 || p.getServerWorld().getFluidState(clicked.up()).isStill()) {
            p.sendMessage(Text.literal("Stand on the plot and pick a spot on dry ground.").formatted(Formatting.RED), false);
            return false;
        }
        ServerWorld w = p.getServerWorld();
        BlockPos door = Blueprints.build(w, kind, origin);
        CrewState.Crew crew = CrewState.get(server).crewOf(p.getUuid());
        Home h = new Home(kind, p.getUuid(), p.getName().getString(), kind == Kind.HQ && crew != null ? crew.name.toLowerCase() : null, min, max, door);
        h.lastPaid = w.getTime();
        s.homes.add(h);
        s.markDirty();
        if (kind == Kind.TAVERN) {
            var keep = com.cursedtides.entity.ModEntities.QUARTERMASTER.create(w);
            if (keep != null) {
                keep.setCustomName(Text.literal("Barkeep (cook)"));
                keep.setCustomNameVisible(true);
                keep.refreshPositionAndAngles(origin.getX() + size.w() / 2 + 0.5, origin.getY(), origin.getZ() + 3.5, 180, 0);
                keep.setPersistent();
                w.spawnEntity(keep);
            }
        }
        com.cursedtides.activity.Achievements.grant(p, "homeowner");
        p.sendMessage(Text.literal("Your " + kind.title.toLowerCase() + " stands! It's yours: nobody else can break or open anything inside.")
                .formatted(Formatting.GOLD), false);
        return true;
    }

    // ------------------------------------------------------------------ save / load

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        NbtList list = new NbtList();
        for (Home h : homes) {
            NbtCompound c = new NbtCompound();
            c.putString("Kind", h.kind.name());
            c.putUuid("Owner", h.owner);
            c.putString("Name", h.ownerName);
            if (h.crew != null) c.putString("Crew", h.crew);
            c.putIntArray("Box", new int[]{h.min.getX(), h.min.getY(), h.min.getZ(), h.max.getX(), h.max.getY(), h.max.getZ(),
                    h.door.getX(), h.door.getY(), h.door.getZ()});
            c.putInt("Till", h.till);
            c.putLong("Paid", h.lastPaid);
            list.add(c);
        }
        nbt.put("Homes", list);
        return nbt;
    }

    private static Homes fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        Homes s = new Homes();
        NbtList list = nbt.getList("Homes", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound c = list.getCompound(i);
            int[] b = c.getIntArray("Box");
            if (b.length < 9) continue;
            Kind kind;
            try { kind = Kind.valueOf(c.getString("Kind")); } catch (IllegalArgumentException e) { continue; }
            Home h = new Home(kind, c.getUuid("Owner"), c.getString("Name"), c.contains("Crew") ? c.getString("Crew") : null,
                    new BlockPos(b[0], b[1], b[2]), new BlockPos(b[3], b[4], b[5]), new BlockPos(b[6], b[7], b[8]));
            h.till = c.getInt("Till");
            h.lastPaid = c.getLong("Paid");
            s.homes.add(h);
        }
        return s;
    }

    // ------------------------------------------------------------------ events and commands

    public static void register() {
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.registry.RegistryKey.of(
                net.minecraft.registry.RegistryKeys.ITEM_GROUP, CursedTides.id("main"))).register(entries -> {
            entries.add(HOUSE_DEED);
            entries.add(HIDEOUT_DEED);
            entries.add(TAVERN_DEED);
            entries.add(HQ_DEED);
            entries.add(com.cursedtides.explore.Diving.HELMET);
            entries.add(com.cursedtides.activity.Deliveries.PARCEL);
            for (var e : com.cursedtides.explore.Collectibles.ALL) entries.add(com.cursedtides.explore.Collectibles.stack(e));
        });
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, be) -> {
            if (world.isClient || world.getServer() == null || world != world.getServer().getOverworld()) return true;
            Home h = get(world.getServer()).at(pos);
            if (h == null || mayUse(world.getServer(), h, player)) return true;
            player.sendMessage(Text.literal("This is " + h.ownerName + "'s " + h.kind.title.toLowerCase() + ". Hands off.").formatted(Formatting.RED), true);
            return false;
        });
        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (world.isClient || world.getServer() == null || world != world.getServer().getOverworld()) return ActionResult.PASS;
            BlockPos pos = hit.getBlockPos();
            Home h = get(world.getServer()).at(pos);
            if (h == null || mayUse(world.getServer(), h, player)) return ActionResult.PASS;
            var state = world.getBlockState(pos);
            boolean doorish = state.getBlock() instanceof net.minecraft.block.DoorBlock || state.getBlock() instanceof net.minecraft.block.BedBlock
                    || state.isOf(net.minecraft.block.Blocks.OAK_PRESSURE_PLATE) || state.isOf(net.minecraft.block.Blocks.JUKEBOX);
            if (doorish && h.kind != Kind.HIDEOUT) return ActionResult.PASS; // guests can come in and sit
            player.sendMessage(Text.literal("That belongs to " + h.ownerName + ".").formatted(Formatting.RED), true);
            return ActionResult.FAIL;
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 1200 != 0) return;
            Homes s = get(server);
            long now = server.getOverworld().getTime();
            for (Home h : s.homes) {
                if (h.kind != Kind.TAVERN || now - h.lastPaid < 24000) continue;
                int mood = com.cursedtides.living.Prosperity.at(server, h.door.getX(), h.door.getZ());
                int takings = 30 + mood / 2 + (com.cursedtides.living.Festivals.current() != null ? 20 : 0); // silver
                h.till = Math.min(h.till + takings, 2000);
                h.lastPaid = now;
                s.markDirty();
                ServerPlayerEntity owner = server.getPlayerManager().getPlayer(h.owner);
                if (owner != null) owner.sendMessage(Text.literal("Your tavern took " + com.cursedtides.trade.Wallet.format(takings)
                        + " today. Till: " + com.cursedtides.trade.Wallet.format(h.till) + ". /home collect").formatted(Formatting.GOLD), false);
            }
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> dispatcher.register(literal("home")
                .executes(ctx -> list(ctx.getSource().getPlayerOrThrow()))
                .then(literal("go").executes(ctx -> go(ctx.getSource().getPlayerOrThrow())))
                .then(literal("collect").executes(ctx -> collect(ctx.getSource().getPlayerOrThrow())))
                .then(literal("sell").executes(ctx -> sell(ctx.getSource().getPlayerOrThrow())))));

        Specials.register("buy_house", (p, npc) -> buy(p, Kind.HOUSE));
        Specials.register("buy_hideout", (p, npc) -> buy(p, Kind.HIDEOUT));
        Specials.register("buy_tavern", (p, npc) -> buy(p, Kind.TAVERN));
        Specials.register("buy_hq", (p, npc) -> {
            CrewState.Crew crew = p.getServer() == null ? null : CrewState.get(p.getServer()).crewOf(p.getUuid());
            if (crew == null || crew.rank(p.getUuid()) < 3) return List.of("Headquarters are sold to captains of a crew, and only captains.");
            if (crew.bank < price(Kind.HQ) * 10) return List.of("That's paid from the crew chest: " + price(Kind.HQ) + " doubloons. Your chest holds "
                    + com.cursedtides.trade.Wallet.format(crew.bank) + ".");
            crew.bank -= price(Kind.HQ) * 10;
            CrewState.get(p.getServer()).markDirty();
            p.giveItemStack(new ItemStack(HQ_DEED));
            return List.of("Signed, witnessed and stamped. Plant it somewhere defensible.");
        });
    }

    private static List<String> buy(ServerPlayerEntity p, Kind kind) {
        if (!com.cursedtides.trade.Wallet.pay(p, price(kind) * 10)) {
            return List.of("A " + kind.title.toLowerCase() + " deed is " + price(kind) + " doubloons. Come back when your purse agrees.");
        }
        p.giveItemStack(new ItemStack(switch (kind) { case HOUSE -> HOUSE_DEED; case HIDEOUT -> HIDEOUT_DEED; case TAVERN -> TAVERN_DEED; case HQ -> HQ_DEED; }));
        return List.of("A fine investment. Stand on open ground where you want it and use the deed. The builders work fast. Suspiciously fast.");
    }

    private static int list(ServerPlayerEntity p) {
        List<Home> mine = get(p.getServer()).homes.stream().filter(h -> h.owner.equals(p.getUuid())).toList();
        if (mine.isEmpty()) {
            p.sendMessage(Text.literal("You own nothing but your boots. See a land agent about a deed.").formatted(Formatting.GRAY), false);
            return 1;
        }
        for (Home h : mine) {
            p.sendMessage(Text.literal(h.kind.title + " at " + h.door.getX() + ", " + h.door.getY() + ", " + h.door.getZ()
                    + (h.kind == Kind.TAVERN ? "  (till: " + com.cursedtides.trade.Wallet.format(h.till) + ")" : "")).formatted(Formatting.GOLD), false);
        }
        return 1;
    }

    private static int go(ServerPlayerEntity p) {
        Homes s = get(p.getServer());
        Home home = s.homes.stream().filter(h -> h.owner.equals(p.getUuid())).findFirst().orElse(null);
        if (home == null) {
            CrewState.Crew crew = CrewState.get(p.getServer()).crewOf(p.getUuid());
            if (crew != null) home = s.homes.stream().filter(h -> crew.name.toLowerCase().equals(h.crew)).findFirst().orElse(null);
        }
        if (home == null) return fail(p, "You have no home to go to.");
        long now = p.getServerWorld().getTime();
        if (LAST_GO.getOrDefault(p.getUuid(), -100000L) > now - 12000) return fail(p, "You walked home not long ago. Give it a while.");
        if (com.cursedtides.faction.FactionData.isWanted(p) || p.hasVehicle()) return fail(p, "Not with the Tidewatch on your heels, or from a deck.");
        LAST_GO.put(p.getUuid(), now);
        BlockPos in = home.door.north();
        p.teleport(p.getServer().getOverworld(), in.getX() + 0.5, in.getY(), in.getZ() + 0.5, p.getYaw(), p.getPitch());
        p.sendMessage(Text.literal("Home again.").formatted(Formatting.GOLD), true);
        return 1;
    }

    private static int collect(ServerPlayerEntity p) {
        Homes s = get(p.getServer());
        int total = 0;
        for (Home h : s.homes) {
            if (h.owner.equals(p.getUuid()) && h.kind == Kind.TAVERN) { total += h.till; h.till = 0; }
        }
        if (total == 0) return fail(p, "The till's empty.");
        s.markDirty();
        com.cursedtides.trade.Wallet.give(p, total);
        p.sendMessage(Text.literal("You empty the till: " + com.cursedtides.trade.Wallet.format(total) + ".").formatted(Formatting.GOLD), false);
        return 1;
    }

    private static int sell(ServerPlayerEntity p) {
        Homes s = get(p.getServer());
        Home h = s.at(p.getBlockPos());
        if (h == null || !h.owner.equals(p.getUuid())) return fail(p, "Stand inside the property you want to sell.");
        s.homes.remove(h);
        s.markDirty();
        com.cursedtides.trade.Wallet.give(p, price(h.kind) * 5);
        p.sendMessage(Text.literal("Sold back to the land office for " + price(h.kind) / 2 + " doubloons. The building stays; the claim doesn't.")
                .formatted(Formatting.GOLD), false);
        return 1;
    }

    private static int fail(ServerPlayerEntity p, String msg) {
        p.sendMessage(Text.literal(msg).formatted(Formatting.RED), false);
        return 0;
    }
}
