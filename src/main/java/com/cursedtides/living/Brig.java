package com.cursedtides.living;

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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * The brig. Wanted pirates who fall wake up in a Tidewatch cell on Ironwatch. Serve your two minutes (the Navy
 * forgets a little of your record), pay your bail (/bail), or try a lockpick.
 */
public final class Brig {
    private Brig() {}

    public static final AttachmentType<Long> RELEASE_AT = AttachmentRegistry.<Long>builder()
            .persistent(Codec.LONG).copyOnDeath().initializer(() -> 0L).buildAndRegister(CursedTides.id("brig_release"));

    public static class LockpickItem extends Item {
        public LockpickItem(Settings s) { super(s); }

        @Override
        public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
            ItemStack stack = user.getStackInHand(hand);
            if (!(user instanceof ServerPlayerEntity p)) return TypedActionResult.success(stack, true);
            if (!jailed(p)) {
                p.sendMessage(Text.literal("Nothing here to pick.").formatted(Formatting.GRAY), true);
                return TypedActionResult.fail(stack);
            }
            stack.decrement(1);
            if (p.getRandom().nextFloat() < 0.35f) {
                p.sendMessage(Text.literal("Click. The lock gives. You slip out while the gaoler snores.").formatted(Formatting.GOLD), false);
                FactionData.adjust(p, Faction.NAVY, -20);
                release(p, false);
            } else {
                p.sendMessage(Text.literal("Snap. The pick breaks off in the lock.").formatted(Formatting.GRAY), false);
                p.getWorld().playSound(null, p.getBlockPos(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1f, 1.4f);
            }
            return TypedActionResult.success(stack);
        }

        @Override
        public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
            tooltip.add(Text.literal("Use in a cell. About one chance in three.").formatted(Formatting.GRAY));
        }
    }

    public static final Item LOCKPICK = Registry.register(Registries.ITEM, CursedTides.id("lockpick"), new LockpickItem(new Item.Settings().maxCount(8)));

    public static boolean jailed(ServerPlayerEntity p) {
        return p.getAttachedOrElse(RELEASE_AT, 0L) > 0;
    }

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((victim, source) -> {
            if (!(victim instanceof ServerPlayerEntity p) || !ModWorld.isArchipelago() || !FactionData.isWanted(p)) return;
            if (com.cursedtides.social.Duels.inDuel(p.getUuid())) return;
            p.setAttached(RELEASE_AT, p.getServerWorld().getTime() + 2400);
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldP, p, alive) -> {
            if (!jailed(p)) return;
            int[] b = HiddenBuilds.brig();
            p.teleport(p.getServer().getOverworld(), b[0] + 0.5, b[1], b[2] + 0.5, p.getYaw(), p.getPitch());
            p.sendMessage(Text.literal("You wake in a Tidewatch cell. Two minutes, the gaoler says, or bail: /bail. Or... a lockpick?")
                    .formatted(Formatting.RED), false);
        });
        ServerTickEvents.END_SERVER_TICK.register(Brig::tick);
        CommandRegistrationCallback.EVENT.register((d, a, e) -> d.register(literal("bail").executes(ctx -> {
            ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
            if (!jailed(p)) return 0;
            int cost = Math.max(5, FactionData.bounty(p)) * 2;
            if (!com.cursedtides.trade.Wallet.pay(p, cost * 10)) {
                p.sendMessage(Text.literal("Bail is " + cost + " doubloons. You don't have it.").formatted(Formatting.RED), false);
                return 0;
            }
            p.sendMessage(Text.literal("Bail paid. The gaoler unlocks the door with ill grace.").formatted(Formatting.GOLD), false);
            release(p, false);
            return 1;
        })));
    }

    private static void tick(MinecraftServer server) {
        if (server.getTicks() % 20 != 0) return;
        ServerWorld w = server.getOverworld();
        int[] b = HiddenBuilds.brig();
        for (ServerPlayerEntity p : w.getPlayers()) {
            long at = p.getAttachedOrElse(RELEASE_AT, 0L);
            if (at <= 0 || p.isDead()) continue;
            if (w.getTime() >= at) {
                p.sendMessage(Text.literal("Your time's served. The Tidewatch lets you go, with a lecture.").formatted(Formatting.GOLD), false);
                FactionData.adjust(p, Faction.NAVY, 40);
                release(p, false);
            } else if (p.squaredDistanceTo(b[0] + 0.5, b[1], b[2] + 0.5) > 25 && p.getWorld() == w) {
                // Somehow got out: back in you go
                p.teleport(w, b[0] + 0.5, b[1], b[2] + 0.5, p.getYaw(), p.getPitch());
            }
        }
    }

    static void release(ServerPlayerEntity p, boolean silent) {
        p.setAttached(RELEASE_AT, 0L);
        int[] b = HiddenBuilds.brig();
        p.teleport(p.getServer().getOverworld(), b[0] + 0.5, b[1], b[2] + 4.5, p.getYaw(), p.getPitch());
        p.getWorld().playSound(null, p.getBlockPos(), SoundEvents.BLOCK_IRON_DOOR_OPEN, SoundCategory.BLOCKS, 1f, 0.8f);
    }
}
