package com.cursedtides.living;

import com.cursedtides.CursedTides;
import com.cursedtides.entity.ShipEntity;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Fast travel, as the old pirate games had it: every port has a warp stone beside its gaol. Touch one to attune to
 * it; from then on you can warp to it from anywhere (out of combat, on foot) with /warp or by touching any stone.
 * /warp ship puts you back aboard the last ship you captained.
 */
public final class Totems {
    private Totems() {}

    /** Bit i = attuned to the stone at gaol i. */
    public static final AttachmentType<Integer> ATTUNED = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT).copyOnDeath().initializer(() -> 0).buildAndRegister(CursedTides.id("warp_attuned"));
    /** Where you last stood at your own helm. */
    public static final AttachmentType<Long> LAST_SHIP = AttachmentRegistry.<Long>builder()
            .persistent(Codec.LONG).copyOnDeath().initializer(() -> Long.MIN_VALUE).buildAndRegister(CursedTides.id("last_ship"));

    /** Where a port's stone stands: beside its gaol's door. */
    public static BlockPos stone(Gaols.Gaol g) {
        return new BlockPos(g.x() + 7, g.y(), g.z() + 2);
    }

    public static void register() {
        ATTUNED.identifier();
        LAST_SHIP.identifier();
        ServerTickEvents.END_SERVER_TICK.register(Totems::tick);
        CommandRegistrationCallback.EVENT.register((d, a, e) -> d.register(literal("warp")
                .executes(ctx -> { list(ctx.getSource().getPlayerOrThrow()); return 1; })
                .then(literal("ship").executes(ctx -> { toShip(ctx.getSource().getPlayerOrThrow()); return 1; }))
                .then(argument("port", StringArgumentType.greedyString()).executes(ctx -> {
                    warp(ctx.getSource().getPlayerOrThrow(), StringArgumentType.getString(ctx, "port"));
                    return 1;
                }))));
    }

    public static void touch(ServerPlayerEntity p, BlockPos pos) {
        List<Gaols.Gaol> all = Gaols.all();
        for (int i = 0; i < all.size(); i++) {
            if (stone(all.get(i)).getSquaredDistance(pos) <= 2) {
                int mask = p.getAttachedOrElse(ATTUNED, 0);
                if ((mask & (1 << i)) == 0) {
                    p.setAttached(ATTUNED, mask | (1 << i));
                    p.sendMessage(Text.literal("The stone hums under your hand. You're attuned to " + all.get(i).island().name() + ".")
                            .formatted(Formatting.LIGHT_PURPLE), false);
                    p.getWorld().playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 1f, 0.7f);
                    if (p.getWorld() instanceof ServerWorld sw) sw.spawnParticles(ParticleTypes.PORTAL, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 40, 0.3, 0.6, 0.3, 0.2);
                }
            }
        }
        list(p);
    }

    private static void list(ServerPlayerEntity p) {
        List<Gaols.Gaol> all = Gaols.all();
        int mask = p.getAttachedOrElse(ATTUNED, 0);
        MutableText line = Text.literal("Warp to: ").formatted(Formatting.LIGHT_PURPLE);
        boolean any = false;
        for (int i = 0; i < all.size(); i++) {
            if ((mask & (1 << i)) == 0) continue;
            String name = all.get(i).island().name();
            line.append(Text.literal("[" + name + "]").styled(s -> s.withColor(Formatting.AQUA)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + name))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Warp to " + name)))));
            line.append(Text.literal(" "));
            any = true;
        }
        if (p.getAttachedOrElse(LAST_SHIP, Long.MIN_VALUE) != Long.MIN_VALUE) {
            line.append(Text.literal("[Your ship]").styled(s -> s.withColor(Formatting.GOLD)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp ship"))));
            any = true;
        }
        if (!any) line = Text.literal("You aren't attuned to any warp stone yet. Every port has one, beside its gaol.").formatted(Formatting.GRAY);
        p.sendMessage(line, false);
    }

    private static boolean ready(ServerPlayerEntity p) {
        if (p.hasVehicle()) {
            p.sendMessage(Text.literal("Step ashore (or off the helm) first.").formatted(Formatting.GRAY), true);
            return false;
        }
        if (p.getLastAttackedTime() > 0 && p.age - p.getLastAttackedTime() < 200) {
            p.sendMessage(Text.literal("Not with a fight on your hands.").formatted(Formatting.RED), true);
            return false;
        }
        return true;
    }

    private static void warp(ServerPlayerEntity p, String name) {
        List<Gaols.Gaol> all = Gaols.all();
        int mask = p.getAttachedOrElse(ATTUNED, 0);
        for (int i = 0; i < all.size(); i++) {
            Gaols.Gaol g = all.get(i);
            if (!g.island().name().equalsIgnoreCase(name.trim())) continue;
            if ((mask & (1 << i)) == 0) {
                p.sendMessage(Text.literal("You've never touched " + g.island().name() + "'s stone.").formatted(Formatting.GRAY), false);
                return;
            }
            if (!ready(p)) return;
            BlockPos s = stone(g);
            jump(p, s.getX() + 0.5, s.getY(), s.getZ() + 1.5);
            p.sendMessage(Text.literal("You step out at " + g.island().name() + ".").formatted(Formatting.LIGHT_PURPLE), true);
            return;
        }
        p.sendMessage(Text.literal("No warp stone by that name.").formatted(Formatting.GRAY), false);
    }

    private static void toShip(ServerPlayerEntity p) {
        long packed = p.getAttachedOrElse(LAST_SHIP, Long.MIN_VALUE);
        if (packed == Long.MIN_VALUE) {
            p.sendMessage(Text.literal("You haven't a ship to go back to.").formatted(Formatting.GRAY), false);
            return;
        }
        if (!ready(p)) return;
        BlockPos at = BlockPos.fromLong(packed);
        jump(p, at.getX() + 0.5, at.getY() + 2, at.getZ() + 0.5);
        p.sendMessage(Text.literal("Back aboard.").formatted(Formatting.GOLD), true);
    }

    private static void jump(ServerPlayerEntity p, double x, double y, double z) {
        ServerWorld w = p.getServer().getOverworld();
        if (p.getWorld() instanceof ServerWorld from) from.spawnParticles(ParticleTypes.PORTAL, p.getX(), p.getY() + 1, p.getZ(), 50, 0.4, 0.8, 0.4, 0.3);
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 25, 0, false, false));
        p.teleport(w, x, y, z, p.getYaw(), p.getPitch());
        w.playSound(null, x, y, z, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.8f, 0.8f);
    }

    private static void tick(MinecraftServer server) {
        if (server.getTicks() % 40 != 11) return;
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            if (p.getVehicle() instanceof ShipEntity ship && ship.isOwner(p)) p.setAttached(LAST_SHIP, ship.getBlockPos().asLong());
        }
    }
}
