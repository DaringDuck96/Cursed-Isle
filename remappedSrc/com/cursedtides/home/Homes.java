package com.cursedtides.home;

import com.cursedtides.CursedTides;
import com.cursedtides.dialogue.Specials;
import com.cursedtides.social.CrewState;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.commands.Commands.literal;

/**
 * Places of your own. A deed raises a house, a hideout, a tavern or a crew headquarters, and claims the ground:
 * only you (and, for a headquarters, your crew) can break or open things inside. Taverns earn a little every day,
 * more in a thriving port. /home to see what you own, /home go to walk home, /home collect to empty the till.
 */
public class Homes extends SavedData {
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

    private static final Factory<Homes> TYPE = new Factory<>(Homes::new, Homes::fromNbt, null);

    public static Homes get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE, "cursedtides_homes");
    }

    public static final Item HOUSE_DEED = deed("house_deed", Kind.HOUSE, Rarity.UNCOMMON);
    public static final Item HIDEOUT_DEED = deed("hideout_deed", Kind.HIDEOUT, Rarity.UNCOMMON);
    public static final Item TAVERN_DEED = deed("tavern_deed", Kind.TAVERN, Rarity.RARE);
    public static final Item HQ_DEED = deed("hq_deed", Kind.HQ, Rarity.EPIC);

    private static Item deed(String name, Kind kind, Rarity r) {
        return Registry.register(BuiltInRegistries.ITEM, CursedTides.id(name), new HomeDeedItem(new Item.Properties().stacksTo(1).rarity(r), kind));
    }

    public static int price(Kind k) {
        return switch (k) { case HOUSE -> 60; case HIDEOUT -> 45; case TAVERN -> 150; case HQ -> 300; };
    }

    // ------------------------------------------------------------------ queries

    public static boolean ownsAny(ServerPlayer p) {
        return p.getServer() != null && get(p.getServer()).homes.stream().anyMatch(h -> h.owner.equals(p.getUUID()));
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

    static boolean mayUse(MinecraftServer server, Home h, Player p) {
        if (p.getUUID().equals(h.owner) || p.hasPermissions(2)) return true;
        if (h.kind == Kind.TAVERN) return false; // anyone may walk in (doors aren't blocked), but not open the stores
        CrewState.Crew crew = CrewState.get(server).crewOf(p.getUUID());
        if (crew == null) return false;
        return CrewState.shipmates(server, h.owner, p.getUUID()) || (h.crew != null && h.crew.equals(crew.name.toLowerCase()));
    }

    // ------------------------------------------------------------------ placing

    static boolean place(ServerPlayer p, Kind kind, BlockPos clicked) {
        MinecraftServer server = p.getServer();
        if (server == null || p.level() != server.overworld()) {
            p.displayClientMessage(Component.literal("Deeds only hold good in the archipelago.").withStyle(ChatFormatting.RED), false);
            return false;
        }
        Homes s = get(server);
        long owned = s.homes.stream().filter(h -> h.owner.equals(p.getUUID())).count();
        if (owned >= 3) {
            p.displayClientMessage(Component.literal("You already hold three properties. /home sell one first.").withStyle(ChatFormatting.RED), false);
            return false;
        }
        Blueprints.Size size = Blueprints.size(kind);
        BlockPos origin = clicked.above().offset(-size.w() / 2, 0, -size.d() / 2);
        BlockPos min = origin.offset(-1, -1, -1), max = origin.offset(size.w(), size.h() + 2, size.d());
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                if (com.cursedtides.world.Builds.blocksPlants(x, z)) {
                    p.displayClientMessage(Component.literal("That ground's spoken for. Build away from the town's own buildings.").withStyle(ChatFormatting.RED), false);
                    return false;
                }
            }
        }
        if (s.overlaps(min, max)) {
            p.displayClientMessage(Component.literal("Too close to someone else's property.").withStyle(ChatFormatting.RED), false);
            return false;
        }
        if (Math.abs(clicked.getY() - p.getBlockY()) > 3 || p.serverLevel().getFluidState(clicked.above()).isSource()) {
            p.displayClientMessage(Component.literal("Stand on the plot and pick a spot on dry ground.").withStyle(ChatFormatting.RED), false);
            return false;
        }
        ServerLevel w = p.serverLevel();
        BlockPos door = Blueprints.build(w, kind, origin);
        CrewState.Crew crew = CrewState.get(server).crewOf(p.getUUID());
        Home h = new Home(kind, p.getUUID(), p.getName().getString(), kind == Kind.HQ && crew != null ? crew.name.toLowerCase() : null, min, max, door);
        h.lastPaid = w.getGameTime();
        s.homes.add(h);
        s.setDirty();
        if (kind == Kind.TAVERN) {
            var keep = com.cursedtides.entity.ModEntities.QUARTERMASTER.create(w);
            if (keep != null) {
                keep.setCustomName(Component.literal("Barkeep (cook)"));
                keep.setCustomNameVisible(true);
                keep.moveTo(origin.getX() + size.w() / 2 + 0.5, origin.getY(), origin.getZ() + 3.5, 180, 0);
                keep.setPersistenceRequired();
                w.addFreshEntity(keep);
            }
        }
        com.cursedtides.activity.Achievements.grant(p, "homeowner");
        p.displayClientMessage(Component.literal("Your " + kind.title.toLowerCase() + " stands! It's yours: nobody else can break or open anything inside.")
                .withStyle(ChatFormatting.GOLD), false);
        return true;
    }

    // ------------------------------------------------------------------ save / load

    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider lookup) {
        ListTag list = new ListTag();
        for (Home h : homes) {
            CompoundTag c = new CompoundTag();
            c.putString("Kind", h.kind.name());
            c.putUUID("Owner", h.owner);
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

    private static Homes fromNbt(CompoundTag nbt, HolderLookup.Provider lookup) {
        Homes s = new Homes();
        ListTag list = nbt.getList("Homes", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag c = list.getCompound(i);
            int[] b = c.getIntArray("Box");
            if (b.length < 9) continue;
            Kind kind;
            try { kind = Kind.valueOf(c.getString("Kind")); } catch (IllegalArgumentException e) { continue; }
            Home h = new Home(kind, c.getUUID("Owner"), c.getString("Name"), c.contains("Crew") ? c.getString("Crew") : null,
                    new BlockPos(b[0], b[1], b[2]), new BlockPos(b[3], b[4], b[5]), new BlockPos(b[6], b[7], b[8]));
            h.till = c.getInt("Till");
            h.lastPaid = c.getLong("Paid");
            s.homes.add(h);
        }
        return s;
    }

    // ------------------------------------------------------------------ events and commands

    public static void register() {
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, CursedTides.id("main"))).register(entries -> {
            entries.accept(HOUSE_DEED);
            entries.accept(HIDEOUT_DEED);
            entries.accept(TAVERN_DEED);
            entries.accept(HQ_DEED);
            entries.accept(com.cursedtides.explore.Diving.HELMET);
            entries.accept(com.cursedtides.activity.Deliveries.PARCEL);
            for (var e : com.cursedtides.explore.Collectibles.ALL) entries.accept(com.cursedtides.explore.Collectibles.stack(e));
        });
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, be) -> {
            if (world.isClientSide || world.getServer() == null || world != world.getServer().overworld()) return true;
            Home h = get(world.getServer()).at(pos);
            if (h == null || mayUse(world.getServer(), h, player)) return true;
            player.displayClientMessage(Component.literal("This is " + h.ownerName + "'s " + h.kind.title.toLowerCase() + ". Hands off.").withStyle(ChatFormatting.RED), true);
            return false;
        });
        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (world.isClientSide || world.getServer() == null || world != world.getServer().overworld()) return InteractionResult.PASS;
            BlockPos pos = hit.getBlockPos();
            Home h = get(world.getServer()).at(pos);
            if (h == null || mayUse(world.getServer(), h, player)) return InteractionResult.PASS;
            var state = world.getBlockState(pos);
            boolean doorish = state.getBlock() instanceof net.minecraft.world.level.block.DoorBlock || state.getBlock() instanceof net.minecraft.world.level.block.BedBlock
                    || state.is(net.minecraft.world.level.block.Blocks.OAK_PRESSURE_PLATE) || state.is(net.minecraft.world.level.block.Blocks.JUKEBOX);
            if (doorish && h.kind != Kind.HIDEOUT) return InteractionResult.PASS; // guests can come in and sit
            player.displayClientMessage(Component.literal("That belongs to " + h.ownerName + ".").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 1200 != 0) return;
            Homes s = get(server);
            long now = server.overworld().getGameTime();
            for (Home h : s.homes) {
                if (h.kind != Kind.TAVERN || now - h.lastPaid < 24000) continue;
                int mood = com.cursedtides.living.Prosperity.at(server, h.door.getX(), h.door.getZ());
                int takings = 30 + mood / 2 + (com.cursedtides.living.Festivals.current() != null ? 20 : 0); // silver
                h.till = Math.min(h.till + takings, 2000);
                h.lastPaid = now;
                s.setDirty();
                ServerPlayer owner = server.getPlayerList().getPlayer(h.owner);
                if (owner != null) owner.displayClientMessage(Component.literal("Your tavern took " + com.cursedtides.trade.Wallet.format(takings)
                        + " today. Till: " + com.cursedtides.trade.Wallet.format(h.till) + ". /home collect").withStyle(ChatFormatting.GOLD), false);
            }
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> dispatcher.register(literal("home")
                .executes(ctx -> list(ctx.getSource().getPlayerOrException()))
                .then(literal("go").executes(ctx -> go(ctx.getSource().getPlayerOrException())))
                .then(literal("collect").executes(ctx -> collect(ctx.getSource().getPlayerOrException())))
                .then(literal("sell").executes(ctx -> sell(ctx.getSource().getPlayerOrException())))));

        Specials.register("buy_house", (p, npc) -> buy(p, Kind.HOUSE));
        Specials.register("buy_hideout", (p, npc) -> buy(p, Kind.HIDEOUT));
        Specials.register("buy_tavern", (p, npc) -> buy(p, Kind.TAVERN));
        Specials.register("buy_hq", (p, npc) -> {
            CrewState.Crew crew = p.getServer() == null ? null : CrewState.get(p.getServer()).crewOf(p.getUUID());
            if (crew == null || crew.rank(p.getUUID()) < 3) return List.of("Headquarters are sold to captains of a crew, and only captains.");
            if (crew.bank < price(Kind.HQ) * 10) return List.of("That's paid from the crew chest: " + price(Kind.HQ) + " doubloons. Your chest holds "
                    + com.cursedtides.trade.Wallet.format(crew.bank) + ".");
            crew.bank -= price(Kind.HQ) * 10;
            CrewState.get(p.getServer()).setDirty();
            p.addItem(new ItemStack(HQ_DEED));
            return List.of("Signed, witnessed and stamped. Plant it somewhere defensible.");
        });
    }

    private static List<String> buy(ServerPlayer p, Kind kind) {
        if (!com.cursedtides.trade.Wallet.pay(p, price(kind) * 10)) {
            return List.of("A " + kind.title.toLowerCase() + " deed is " + price(kind) + " doubloons. Come back when your purse agrees.");
        }
        p.addItem(new ItemStack(switch (kind) { case HOUSE -> HOUSE_DEED; case HIDEOUT -> HIDEOUT_DEED; case TAVERN -> TAVERN_DEED; case HQ -> HQ_DEED; }));
        return List.of("A fine investment. Stand on open ground where you want it and use the deed. The builders work fast. Suspiciously fast.");
    }

    private static int list(ServerPlayer p) {
        List<Home> mine = get(p.getServer()).homes.stream().filter(h -> h.owner.equals(p.getUUID())).toList();
        if (mine.isEmpty()) {
            p.displayClientMessage(Component.literal("You own nothing but your boots. See a land agent about a deed.").withStyle(ChatFormatting.GRAY), false);
            return 1;
        }
        for (Home h : mine) {
            p.displayClientMessage(Component.literal(h.kind.title + " at " + h.door.getX() + ", " + h.door.getY() + ", " + h.door.getZ()
                    + (h.kind == Kind.TAVERN ? "  (till: " + com.cursedtides.trade.Wallet.format(h.till) + ")" : "")).withStyle(ChatFormatting.GOLD), false);
        }
        return 1;
    }

    private static int go(ServerPlayer p) {
        Homes s = get(p.getServer());
        Home home = s.homes.stream().filter(h -> h.owner.equals(p.getUUID())).findFirst().orElse(null);
        if (home == null) {
            CrewState.Crew crew = CrewState.get(p.getServer()).crewOf(p.getUUID());
            if (crew != null) home = s.homes.stream().filter(h -> crew.name.toLowerCase().equals(h.crew)).findFirst().orElse(null);
        }
        if (home == null) return fail(p, "You have no home to go to.");
        long now = p.serverLevel().getGameTime();
        if (LAST_GO.getOrDefault(p.getUUID(), -100000L) > now - 12000) return fail(p, "You walked home not long ago. Give it a while.");
        if (com.cursedtides.faction.FactionData.isWanted(p) || p.isPassenger()) return fail(p, "Not with the Tidewatch on your heels, or from a deck.");
        LAST_GO.put(p.getUUID(), now);
        BlockPos in = home.door.north();
        p.teleportTo(p.getServer().overworld(), in.getX() + 0.5, in.getY(), in.getZ() + 0.5, p.getYRot(), p.getXRot());
        p.displayClientMessage(Component.literal("Home again.").withStyle(ChatFormatting.GOLD), true);
        return 1;
    }

    private static int collect(ServerPlayer p) {
        Homes s = get(p.getServer());
        int total = 0;
        for (Home h : s.homes) {
            if (h.owner.equals(p.getUUID()) && h.kind == Kind.TAVERN) { total += h.till; h.till = 0; }
        }
        if (total == 0) return fail(p, "The till's empty.");
        s.setDirty();
        com.cursedtides.trade.Wallet.give(p, total);
        p.displayClientMessage(Component.literal("You empty the till: " + com.cursedtides.trade.Wallet.format(total) + ".").withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private static int sell(ServerPlayer p) {
        Homes s = get(p.getServer());
        Home h = s.at(p.blockPosition());
        if (h == null || !h.owner.equals(p.getUUID())) return fail(p, "Stand inside the property you want to sell.");
        s.homes.remove(h);
        s.setDirty();
        com.cursedtides.trade.Wallet.give(p, price(h.kind) * 5);
        p.displayClientMessage(Component.literal("Sold back to the land office for " + price(h.kind) / 2 + " doubloons. The building stays; the claim doesn't.")
                .withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private static int fail(ServerPlayer p, String msg) {
        p.displayClientMessage(Component.literal(msg).withStyle(ChatFormatting.RED), false);
        return 0;
    }
}
