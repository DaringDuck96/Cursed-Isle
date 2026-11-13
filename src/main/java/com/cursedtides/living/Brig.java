package com.cursedtides.living;

import net.minecraft.world.InteractionResult;
import com.cursedtides.CursedTides;
import com.cursedtides.faction.Faction;
import com.cursedtides.faction.FactionData;
import com.cursedtides.world.HiddenBuilds;
import com.cursedtides.world.ModWorld;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import java.util.List;

import static net.minecraft.commands.Commands.literal;

/**
 * The brig. Wanted pirates who fall wake up in a Tidewatch cell on Ironwatch. Serve your two minutes (the Navy
 * forgets a little of your record), pay your bail (/bail), or try a lockpick.
 */
public final class Brig {
    private Brig() {}

    public static final AttachmentType<Long> RELEASE_AT = AttachmentRegistry.<Long>builder()
            .persistent(Codec.LONG).copyOnDeath().initializer(() -> 0L).buildAndRegister(CursedTides.id("brig_release"));

    public static class LockpickItem extends Item {
        public LockpickItem(Properties s) { super(s); }

        @Override
        public InteractionResult use(Level world, Player user, InteractionHand hand) {
            ItemStack stack = user.getItemInHand(hand);
            if (!(user instanceof ServerPlayer p)) return InteractionResult.SUCCESS;
            if (!jailed(p)) {
                p.sendSystemMessage(Component.literal("Nothing here to pick.").withStyle(ChatFormatting.GRAY), true);
                return InteractionResult.FAIL;
            }
            stack.shrink(1);
            if (p.getRandom().nextFloat() < 0.35f) {
                p.sendSystemMessage(Component.literal("Click. The lock gives. You slip out while the gaoler snores.").withStyle(ChatFormatting.GOLD), false);
                FactionData.adjust(p, Faction.NAVY, -20);
                release(p, false);
            } else {
                p.sendSystemMessage(Component.literal("Snap. The pick breaks off in the lock.").withStyle(ChatFormatting.GRAY), false);
                p.level().playSound(null, p.blockPosition(), SoundEvents.ITEM_BREAK.value(), SoundSource.PLAYERS, 1f, 1.4f);
            }
            return InteractionResult.SUCCESS;
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display__, java.util.function.Consumer<Component> adder__, TooltipFlag type) {
        java.util.List<Component> tooltip = new java.util.ArrayList<>();
            tooltip.add(Component.literal("Use in a cell. About one chance in three.").withStyle(ChatFormatting.GRAY));
        tooltip.forEach(adder__);
    }
    }

    public static final Item LOCKPICK = Registry.register(BuiltInRegistries.ITEM, com.cursedtides.util.Reg.itemId("lockpick"), new LockpickItem(com.cursedtides.util.Reg.props().stacksTo(8)));

    public static boolean jailed(ServerPlayer p) {
        return p.getAttachedOrElse(RELEASE_AT, 0L) > 0;
    }

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((victim, source) -> {
            if (!(victim instanceof ServerPlayer p) || !ModWorld.isArchipelago() || !FactionData.isWanted(p)) return;
            if (com.cursedtides.social.Duels.inDuel(p.getUUID())) return;
            p.setAttached(RELEASE_AT, p.level().getGameTime() + 2400);
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldP, p, alive) -> {
            if (!jailed(p)) return;
            int[] b = HiddenBuilds.brig();
            com.cursedtides.util.Compat.teleport(p, p.level().getServer().overworld(), b[0] + 0.5, b[1], b[2] + 0.5, p.getYRot(), p.getXRot());
            p.sendSystemMessage(Component.literal("You wake in a Tidewatch cell. Two minutes, the gaoler says, or bail: /bail. Or... a lockpick?")
                    .withStyle(ChatFormatting.RED), false);
        });
        ServerTickEvents.END_SERVER_TICK.register(Brig::tick);
        CommandRegistrationCallback.EVENT.register((d, a, e) -> d.register(literal("bail").executes(ctx -> {
            ServerPlayer p = ctx.getSource().getPlayerOrException();
            if (!jailed(p)) return 0;
            int cost = Math.max(5, FactionData.bounty(p)) * 2;
            if (!com.cursedtides.trade.Wallet.pay(p, cost * 10)) {
                p.sendSystemMessage(Component.literal("Bail is " + cost + " doubloons. You don't have it.").withStyle(ChatFormatting.RED), false);
                return 0;
            }
            p.sendSystemMessage(Component.literal("Bail paid. The gaoler unlocks the door with ill grace.").withStyle(ChatFormatting.GOLD), false);
            release(p, false);
            return 1;
        })));
    }

    private static void tick(MinecraftServer server) {
        if (server.getTickCount() % 20 != 0) return;
        ServerLevel w = server.overworld();
        int[] b = HiddenBuilds.brig();
        for (ServerPlayer p : w.players()) {
            long at = p.getAttachedOrElse(RELEASE_AT, 0L);
            if (at <= 0 || p.isDeadOrDying()) continue;
            if (w.getGameTime() >= at) {
                p.sendSystemMessage(Component.literal("Your time's served. The Tidewatch lets you go, with a lecture.").withStyle(ChatFormatting.GOLD), false);
                FactionData.adjust(p, Faction.NAVY, 40);
                release(p, false);
            } else if (p.distanceToSqr(b[0] + 0.5, b[1], b[2] + 0.5) > 25 && p.level() == w) {
                // Somehow got out: back in you go
                com.cursedtides.util.Compat.teleport(p, w, b[0] + 0.5, b[1], b[2] + 0.5, p.getYRot(), p.getXRot());
            }
        }
    }

    static void release(ServerPlayer p, boolean silent) {
        p.setAttached(RELEASE_AT, 0L);
        int[] b = HiddenBuilds.brig();
        com.cursedtides.util.Compat.teleport(p, p.level().getServer().overworld(), b[0] + 0.5, b[1], b[2] + 4.5, p.getYRot(), p.getXRot());
        p.level().playSound(null, p.blockPosition(), SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1f, 0.8f);
    }
}
