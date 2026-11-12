package com.cursedtides.living;

import com.cursedtides.appearance.Accessories;
import com.cursedtides.dialogue.Specials;
import com.cursedtides.faction.FactionData;
import com.cursedtides.item.ModItems;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/** Wires up the living-world systems and the conversation business that belongs to them. */
public final class LivingWorld {
    private LivingWorld() {}

    public static void register() {
        Prosperity.register();
        Festivals.register();
        Residents.register();
        Bounties.register();

        Specials.register("bounty_board", (p, npc) -> {
            List<String> lines = new ArrayList<>();
            for (Text t : Bounties.board(p.getServer())) lines.add(t.getString().trim());
            return lines;
        });
        Specials.register("pay_bounty", (p, npc) -> {
            int bounty = FactionData.bounty(p);
            if (bounty == 0) return List.of("You're not on my lists. Lucky you.");
            if (com.cursedtides.trade.Wallet.total(p) < bounty * 10) {
                return List.of("Settling a bounty of " + bounty + " doubloons means paying " + bounty + " doubloons. You haven't got it.");
            }
            com.cursedtides.trade.Wallet.pay(p, bounty * 10);
            FactionData.set(p, com.cursedtides.faction.Faction.NAVY, Math.max(FactionData.rep(p, com.cursedtides.faction.Faction.NAVY),
                    FactionData.WANTED_AT + 1));
            return List.of("Paid in full. I'll strike your name off the posters. Don't make me paint it back on.");
        });
        Specials.register("buy_poster", (p, npc) -> {
            if (com.cursedtides.trade.Wallet.total(p) < 10) return List.of("A doubloon a sheet. Come back with one.");
            com.cursedtides.trade.Wallet.pay(p, 10);
            p.giveItemStack(new ItemStack(ModItems.WANTED_POSTER));
            return List.of("Here. It updates itself, near enough: the Tidewatch prints a fresh one every tide.");
        });
        Specials.register("mariner_gift", (p, npc) -> {
            String flag = "cursedtides_mariner_gift";
            if (p.getCommandTags().contains(flag)) {
                return List.of("I've given you all I had to give. The rest you'll have to find, same as I did.");
            }
            p.addCommandTag(flag);
            p.giveItemStack(new ItemStack(Accessories.COMPASS_PENDANT));
            p.giveItemStack(new ItemStack(ModItems.TREASURE_MAP));
            return List.of("Take this. It's never once pointed me home, but it's always pointed me somewhere worth going.",
                    "And this chart. I marked it in a hand steadier than mine is now.");
        });
        Specials.register("town_news", (p, npc) -> {
            List<String> lines = new ArrayList<>();
            lines.add("The town's " + Prosperity.mood(p.getServer(), npc.getBlockX(), npc.getBlockZ()) + " (" + Prosperity.at(p.getServer(), npc.getBlockX(), npc.getBlockZ()) + "/100).");
            Festivals.Festival f = Festivals.current();
            lines.add(f == null ? "No festival on. Next one'll come round with the season." : "It's the " + f.title + "! " + f.perk + ".");
            return lines;
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> dispatcher.register(
                literal("cursedtides").requires(src -> src.hasPermissionLevel(2))
                        .then(literal("visitor").then(argument("who", StringArgumentType.word())
                                .suggests((c, b) -> b.suggest("peddler").suggest("mariner").suggest("hunters").buildFuture())
                                .executes(ctx -> {
                                    ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                                    var w = p.getServerWorld();
                                    boolean ok = switch (StringArgumentType.getString(ctx, "who")) {
                                        case "peddler" -> Residents.spawnPeddler(w, p);
                                        case "mariner" -> Residents.spawnMariner(w, p);
                                        case "hunters" -> Bounties.ambush(w, p) > 0;
                                        default -> false;
                                    };
                                    if (!ok) ctx.getSource().sendError(Text.literal("Nowhere suitable nearby."));
                                    return ok ? 1 : 0;
                                })))));
    }
}
