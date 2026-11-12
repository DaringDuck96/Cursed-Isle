package com.cursedtides.quest;

import com.cursedtides.CursedTides;
import com.cursedtides.combat.Levels;
import com.cursedtides.combat.Loot;
import com.cursedtides.living.Gaols;
import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import com.cursedtides.world.Areas;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Contract boards: in every port, beside the warp stone, a notice board posts four jobs a day for the wilds and
 * waters nearby. Hunt the foes of a named area, put down its elite, sink ships, or survey a landmark. Take up to
 * three at once; each pays the moment it's done (coin, a loot container and reputation), and new ones go up the next day.
 */
public final class Contracts {
    private Contracts() {}

    public enum Kind { HUNT, ELITE, SINK, SURVEY }

    /** One job: what, where (index into Areas.ALL; -1 for none), how many, done so far, and the level it was posted at. */
    public record Job(Kind kind, int area, int need, int have, int level) {
        String encode() { return kind.ordinal() + ":" + area + ":" + need + ":" + have + ":" + level; }

        static Job decode(String s) {
            String[] p = s.split(":");
            return new Job(Kind.values()[Integer.parseInt(p[0])], Integer.parseInt(p[1]), Integer.parseInt(p[2]),
                    Integer.parseInt(p[3]), Integer.parseInt(p[4]));
        }

        Areas.Area where() { return area >= 0 && area < Areas.ALL.size() ? Areas.ALL.get(area) : null; }

        public String describe() {
            Areas.Area a = where();
            String place = a == null ? "" : cap(a.name()) + " on " + a.island().name();
            return switch (kind) {
                case HUNT -> "Hunt " + need + " foes in " + place;
                case ELITE -> "Slay the elite who keeps " + place;
                case SINK -> "Sink " + need + " hostile ships of level " + level + " or more";
                case SURVEY -> "Survey the landmark in " + place;
            };
        }

        public int doubloons() {
            return switch (kind) {
                case HUNT -> 6 + level * 2;
                case ELITE -> 10 + level * 3;
                case SINK -> 12 + level * 3;
                case SURVEY -> 4 + level;
            };
        }
    }

    private static String cap(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static final AttachmentType<String> ACTIVE = AttachmentRegistry.<String>builder()
            .persistent(Codec.STRING).copyOnDeath().initializer(() -> "").buildAndRegister(CursedTides.id("contracts"));
    /** Contracts finished, ever (for the feat), and the day's taken offers (so a board can't be emptied twice). */
    public static final AttachmentType<Integer> DONE = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT).copyOnDeath().initializer(() -> 0).buildAndRegister(CursedTides.id("contracts_done"));
    public static final AttachmentType<String> TAKEN = AttachmentRegistry.<String>builder()
            .persistent(Codec.STRING).copyOnDeath().initializer(() -> "").buildAndRegister(CursedTides.id("contracts_taken"));

    public static final int MAX = 3;

    public static List<Job> active(ServerPlayerEntity p) {
        List<Job> out = new ArrayList<>();
        String s = p.getAttachedOrElse(ACTIVE, "");
        if (s.isEmpty()) return out;
        for (String part : s.split(";")) {
            try { out.add(Job.decode(part)); } catch (RuntimeException ignored) { }
        }
        return out;
    }

    private static void save(ServerPlayerEntity p, List<Job> jobs) {
        StringBuilder b = new StringBuilder();
        for (Job j : jobs) {
            if (b.length() > 0) b.append(';');
            b.append(j.encode());
        }
        p.setAttached(ACTIVE, b.toString());
    }

    private static long day(ServerPlayerEntity p) {
        return p.getServerWorld().getTimeOfDay() / 24000L;
    }

    /** Today's four offers on the board at gaol (port) index g. */
    public static List<Job> offers(ServerPlayerEntity p, int g) {
        Gaols.Gaol port = Gaols.all().get(g);
        List<Integer> nearby = new ArrayList<>();
        for (int i = 0; i < Areas.ALL.size(); i++) nearby.add(i);
        nearby.sort(Comparator.comparingDouble(i -> Math.hypot(Areas.ALL.get(i).x() - port.x(), Areas.ALL.get(i).z() - port.z())));
        List<Integer> pool = nearby.subList(0, Math.min(9, nearby.size()));
        Random r = Random.create(day(p) * 7919 + g * 104729L);
        List<Job> out = new ArrayList<>();
        for (int k = 0; k < 4; k++) {
            Kind kind = Kind.values()[r.nextInt(Kind.values().length)];
            int area = pool.get(r.nextInt(pool.size()));
            int level = Levels.bandAt(new BlockPos(Areas.ALL.get(area).x(), 64, Areas.ALL.get(area).z()))[1];
            int need = switch (kind) {
                case HUNT -> 6 + r.nextInt(7);
                case SINK -> 1 + r.nextInt(3);
                default -> 1;
            };
            if (kind == Kind.SINK) {
                level = Math.max(1, Levels.bandAt(new BlockPos(port.x(), 64, port.z()))[0]);
                area = -1;
            }
            out.add(new Job(kind, area, need, 0, level));
        }
        return out;
    }

    // ------------------------------------------------------------------ the board

    public static void board(ServerPlayerEntity p, BlockPos pos) {
        List<Gaols.Gaol> all = Gaols.all();
        int g = -1;
        for (int i = 0; i < all.size(); i++) if (boardPos(all.get(i)).getSquaredDistance(pos) <= 4) g = i;
        if (g < 0) return;
        p.sendMessage(Text.literal("— Contracts posted at " + all.get(g).island().name() + " today —").formatted(Formatting.GOLD), false);
        List<Job> offers = offers(p, g);
        String taken = p.getAttachedOrElse(TAKEN, "");
        for (int k = 0; k < offers.size(); k++) {
            Job j = offers.get(k);
            String key = day(p) + "/" + g + "/" + k;
            boolean gone = taken.contains("|" + key + "|");
            MutableText line = Text.literal(gone ? "  (taken) " : "  ").formatted(Formatting.DARK_GRAY)
                    .append(Text.literal(j.describe()).formatted(gone ? Formatting.DARK_GRAY : Formatting.WHITE))
                    .append(Text.literal("  " + j.doubloons() + " doubloons").formatted(Formatting.YELLOW));
            if (!gone) {
                final int idx = k, port = g;
                line.append(Text.literal("  [Take]").styled(s -> s.withColor(Formatting.GREEN)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/contract take " + port + " " + idx))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Take this contract")))));
            }
            p.sendMessage(line, false);
        }
        list(p);
    }

    public static BlockPos boardPos(Gaols.Gaol g) {
        return new BlockPos(g.x() + 7, g.y(), g.z() - 2);
    }

    private static void take(ServerPlayerEntity p, int g, int k) {
        List<Gaols.Gaol> all = Gaols.all();
        if (g < 0 || g >= all.size() || k < 0 || k > 3) return;
        if (boardPos(all.get(g)).getSquaredDistance(p.getBlockPos()) > 12 * 12) {
            p.sendMessage(Text.literal("You'll need to be at the board to take that.").formatted(Formatting.GRAY), false);
            return;
        }
        List<Job> jobs = active(p);
        if (jobs.size() >= MAX) {
            p.sendMessage(Text.literal("You can hold " + MAX + " contracts at once. Finish or drop one first (/contract).").formatted(Formatting.GRAY), false);
            return;
        }
        String key = "|" + day(p) + "/" + g + "/" + k + "|";
        String taken = p.getAttachedOrElse(TAKEN, "");
        if (taken.contains(key)) return;
        if (!taken.startsWith("|" + day(p) + "/")) taken = ""; // yesterday's slips go in the bin
        p.setAttached(TAKEN, taken + key);
        Job j = offers(p, g).get(k);
        jobs.add(j);
        save(p, jobs);
        p.sendMessage(Text.literal("Contract taken: " + j.describe() + ".").formatted(Formatting.GREEN), false);
    }

    private static void list(ServerPlayerEntity p) {
        List<Job> jobs = active(p);
        if (jobs.isEmpty()) {
            p.sendMessage(Text.literal("You hold no contracts. Every port's board (beside its warp stone) posts new ones daily.").formatted(Formatting.GRAY), false);
            return;
        }
        p.sendMessage(Text.literal("Your contracts:").formatted(Formatting.GOLD), false);
        for (int i = 0; i < jobs.size(); i++) {
            Job j = jobs.get(i);
            final int idx = i;
            p.sendMessage(Text.literal("  " + j.describe() + (j.need() > 1 ? " (" + j.have() + "/" + j.need() + ")" : ""))
                    .formatted(Formatting.WHITE)
                    .append(Text.literal("  [Drop]").styled(s -> s.withColor(Formatting.RED)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/contract drop " + idx)))), false);
        }
    }

    public static List<String> journal(ServerPlayerEntity p) {
        List<String> out = new ArrayList<>();
        for (Job j : active(p)) out.add("§f" + j.describe() + (j.need() > 1 ? " §7(" + j.have() + "/" + j.need() + ")" : ""));
        if (out.isEmpty()) out.add("§7None held. Every port's notice board posts new ones daily.");
        return out;
    }

    /** Where the radar should point for the first contract with a place, or null. */
    public static int[] target(ServerPlayerEntity p) {
        for (Job j : active(p)) {
            Areas.Area a = j.where();
            if (a != null) return new int[]{a.x(), a.z()};
        }
        return null;
    }

    public static String targetLabel(ServerPlayerEntity p) {
        for (Job j : active(p)) if (j.where() != null) return cap(j.where().name());
        return "";
    }

    // ------------------------------------------------------------------ progress

    private static void advance(ServerPlayerEntity p, java.util.function.Predicate<Job> matches) {
        List<Job> jobs = active(p);
        boolean changed = false;
        for (int i = 0; i < jobs.size(); i++) {
            Job j = jobs.get(i);
            if (!matches.test(j)) continue;
            Job next = new Job(j.kind(), j.area(), j.need(), j.have() + 1, j.level());
            changed = true;
            if (next.have() >= next.need()) {
                jobs.remove(i--);
                pay(p, next);
            } else {
                jobs.set(i, next);
                p.sendMessage(Text.literal(next.describe() + " " + next.have() + "/" + next.need()).formatted(Formatting.AQUA), true);
            }
        }
        if (changed) save(p, jobs);
    }

    private static void pay(ServerPlayerEntity p, Job j) {
        p.sendMessage(Text.literal("Contract complete: " + j.describe() + ". ").formatted(Formatting.GOLD)
                .append(Text.literal("+" + j.doubloons() + " doubloons").formatted(Formatting.YELLOW)), false);
        com.cursedtides.trade.Wallet.give(p, j.doubloons() * com.cursedtides.trade.Wallet.SILVER_PER_DOUBLOON);
        QuestData.give(p, Loot.container(j.kind() == Kind.HUNT || j.kind() == Kind.SURVEY ? Loot.Tier.POUCH : Loot.Tier.CHEST, j.level()));
        PirateData.addXp(p, j.kind() == Kind.SINK ? Weapon.CANNON : j.kind() == Kind.SURVEY ? Weapon.EXPLORATION : Weapon.CUTLASS, 20 + j.level() * 3);
        p.getWorld().playSound(null, p.getBlockPos(), SoundEvents.ENTITY_VILLAGER_YES, SoundCategory.PLAYERS, 0.6f, 1.2f);
        int done = p.getAttachedOrElse(DONE, 0) + 1;
        p.setAttached(DONE, done);
        com.cursedtides.activity.Achievements.grant(p, "first_contract");
        if (done >= 25) com.cursedtides.activity.Achievements.grant(p, "contractor");
    }

    private static void onDeath(LivingEntity victim, DamageSource source) {
        if (!(source.getAttacker() instanceof ServerPlayerEntity p) || Levels.levelOf(victim) <= 0) return;
        Areas.Area here = Areas.at(victim.getX(), victim.getZ());
        if (here == null) return;
        int area = Areas.ALL.indexOf(here);
        boolean elite = victim.hasCustomName() && victim.isCustomNameVisible();
        advance(p, j -> j.area() == area && (j.kind() == Kind.HUNT || (j.kind() == Kind.ELITE && elite)));
    }

    /** A hostile ship went down, and this pirate sank her. */
    public static void onShipSunk(ServerPlayerEntity p, int shipLevel) {
        advance(p, j -> j.kind() == Kind.SINK && shipLevel >= j.level());
    }

    private static void tick(MinecraftServer server) {
        if (server.getTicks() % 20 != 9) return;
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            Areas.Area here = Areas.at(p.getX(), p.getZ());
            if (here == null) continue;
            int area = Areas.ALL.indexOf(here);
            if (Math.hypot(p.getX() - here.x(), p.getZ() - here.z()) > 12) continue;
            advance(p, j -> j.kind() == Kind.SURVEY && j.area() == area);
        }
    }

    public static void register() {
        ACTIVE.identifier();
        DONE.identifier();
        TAKEN.identifier();
        ServerLivingEntityEvents.AFTER_DEATH.register(Contracts::onDeath);
        ServerTickEvents.END_SERVER_TICK.register(Contracts::tick);
        CommandRegistrationCallback.EVENT.register((d, a, e) -> d.register(net.minecraft.server.command.CommandManager.literal("contract")
                .executes(ctx -> { list(ctx.getSource().getPlayerOrThrow()); return 1; })
                .then(net.minecraft.server.command.CommandManager.literal("take")
                        .then(net.minecraft.server.command.CommandManager.argument("port", IntegerArgumentType.integer(0))
                                .then(net.minecraft.server.command.CommandManager.argument("n", IntegerArgumentType.integer(0, 3)).executes(ctx -> {
                                    take(ctx.getSource().getPlayerOrThrow(), IntegerArgumentType.getInteger(ctx, "port"), IntegerArgumentType.getInteger(ctx, "n"));
                                    return 1;
                                }))))
                .then(net.minecraft.server.command.CommandManager.literal("drop")
                        .then(net.minecraft.server.command.CommandManager.argument("n", IntegerArgumentType.integer(0, MAX - 1)).executes(ctx -> {
                            ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                            List<Job> jobs = active(p);
                            int n = IntegerArgumentType.getInteger(ctx, "n");
                            if (n < jobs.size()) {
                                Job j = jobs.remove(n);
                                save(p, jobs);
                                p.sendMessage(Text.literal("Dropped: " + j.describe() + ".").formatted(Formatting.GRAY), false);
                            }
                            return 1;
                        })))));
    }
}
