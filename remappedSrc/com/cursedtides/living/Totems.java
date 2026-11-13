package com.cursedtides.living;

import com.cursedtides.CursedTides;
import com.cursedtides.entity.ShipEntity;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import java.util.List;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

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
                .executes(ctx -> { list(ctx.getSource().getPlayerOrException()); return 1; })
                .then(literal("ship").executes(ctx -> { toShip(ctx.getSource().getPlayerOrException()); return 1; }))
                .then(argument("port", StringArgumentType.greedyString()).executes(ctx -> {
                    warp(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "port"));
                    return 1;
                }))));
    }

    public static void touch(ServerPlayer p, BlockPos pos) {
        List<Gaols.Gaol> all = Gaols.all();
        for (int i = 0; i < all.size(); i++) {
            if (stone(all.get(i)).distSqr(pos) <= 2) {
                int mask = p.getAttachedOrElse(ATTUNED, 0);
                if ((mask & (1 << i)) == 0) {
                    p.setAttached(ATTUNED, mask | (1 << i));
                    p.displayClientMessage(Component.literal("The stone hums under your hand. You're attuned to " + all.get(i).island().name() + ".")
                            .withStyle(ChatFormatting.LIGHT_PURPLE), false);
                    p.level().playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1f, 0.7f);
                    if (p.level() instanceof ServerLevel sw) sw.sendParticles(ParticleTypes.PORTAL, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 40, 0.3, 0.6, 0.3, 0.2);
                }
            }
        }
        list(p);
    }

    private static void list(ServerPlayer p) {
        List<Gaols.Gaol> all = Gaols.all();
        int mask = p.getAttachedOrElse(ATTUNED, 0);
        MutableComponent line = Component.literal("Warp to: ").withStyle(ChatFormatting.LIGHT_PURPLE);
        boolean any = false;
        for (int i = 0; i < all.size(); i++) {
            if ((mask & (1 << i)) == 0) continue;
            String name = all.get(i).island().name();
            line.append(Component.literal("[" + name + "]").withStyle(s -> s.withColor(ChatFormatting.AQUA)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + name))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Warp to " + name)))));
            line.append(Component.literal(" "));
            any = true;
        }
        if (p.getAttachedOrElse(LAST_SHIP, Long.MIN_VALUE) != Long.MIN_VALUE) {
            line.append(Component.literal("[Your ship]").withStyle(s -> s.withColor(ChatFormatting.GOLD)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp ship"))));
            any = true;
        }
        if (!any) line = Component.literal("You aren't attuned to any warp stone yet. Every port has one, beside its gaol.").withStyle(ChatFormatting.GRAY);
        p.displayClientMessage(line, false);
    }

    private static boolean ready(ServerPlayer p) {
        if (p.isPassenger()) {
            p.displayClientMessage(Component.literal("Step ashore (or off the helm) first.").withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        if (p.getLastHurtByMobTimestamp() > 0 && p.tickCount - p.getLastHurtByMobTimestamp() < 200) {
            p.displayClientMessage(Component.literal("Not with a fight on your hands.").withStyle(ChatFormatting.RED), true);
            return false;
        }
        return true;
    }

    private static void warp(ServerPlayer p, String name) {
        List<Gaols.Gaol> all = Gaols.all();
        int mask = p.getAttachedOrElse(ATTUNED, 0);
        for (int i = 0; i < all.size(); i++) {
            Gaols.Gaol g = all.get(i);
            if (!g.island().name().equalsIgnoreCase(name.trim())) continue;
            if ((mask & (1 << i)) == 0) {
                p.displayClientMessage(Component.literal("You've never touched " + g.island().name() + "'s stone.").withStyle(ChatFormatting.GRAY), false);
                return;
            }
            if (!ready(p)) return;
            BlockPos s = stone(g);
            jump(p, s.getX() + 0.5, s.getY(), s.getZ() + 1.5);
            p.displayClientMessage(Component.literal("You step out at " + g.island().name() + ".").withStyle(ChatFormatting.LIGHT_PURPLE), true);
            return;
        }
        p.displayClientMessage(Component.literal("No warp stone by that name.").withStyle(ChatFormatting.GRAY), false);
    }

    private static void toShip(ServerPlayer p) {
        long packed = p.getAttachedOrElse(LAST_SHIP, Long.MIN_VALUE);
        if (packed == Long.MIN_VALUE) {
            p.displayClientMessage(Component.literal("You haven't a ship to go back to.").withStyle(ChatFormatting.GRAY), false);
            return;
        }
        if (!ready(p)) return;
        BlockPos at = BlockPos.of(packed);
        jump(p, at.getX() + 0.5, at.getY() + 2, at.getZ() + 0.5);
        p.displayClientMessage(Component.literal("Back aboard.").withStyle(ChatFormatting.GOLD), true);
    }

    private static void jump(ServerPlayer p, double x, double y, double z) {
        ServerLevel w = p.getServer().overworld();
        if (p.level() instanceof ServerLevel from) from.sendParticles(ParticleTypes.PORTAL, p.getX(), p.getY() + 1, p.getZ(), 50, 0.4, 0.8, 0.4, 0.3);
        p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 25, 0, false, false));
        p.teleportTo(w, x, y, z, p.getYRot(), p.getXRot());
        w.playSound(null, x, y, z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8f, 0.8f);
    }

    private static void tick(MinecraftServer server) {
        if (server.getTickCount() % 40 != 11) return;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (p.getVehicle() instanceof ShipEntity ship && ship.isOwner(p)) p.setAttached(LAST_SHIP, ship.blockPosition().asLong());
        }
    }
}
