package com.cursedtides.living;

import com.cursedtides.combat.Levels;
import com.cursedtides.entity.CursedPirateEntity;
import com.cursedtides.entity.ModEntities;
import com.cursedtides.entity.ShipEntity;
import com.cursedtides.item.ModItems;
import com.cursedtides.world.Archipelago;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.List;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * The festivals as events, not just bonuses. Each brings its own visitors to the towns (who drop festival tokens),
 * the summer and winter feasts and Saint Vesper's light up the night with fireworks, and Regatta Week pays tokens for
 * every ship you sink. Spend tokens with /festival on a trinket, a treasure chest, or the festival's own title.
 *
 *   Spring Tide Fair: Golden Crabs scuttle on the beaches by day.
 *   Regatta Week: fireworks, and tokens for sinking ships.
 *   Saint Vesper's Feast: Penitent Shades drift through the towns at night; fireworks.
 *   Night of Lanterns: Lantern Ghosts walk the streets after dark.
 *   Salt Harvest: Salt Thieves raid the market stalls by day.
 *   Midwinter Feast: Frost Wraiths at night; fireworks.
 */
public final class FestivalEvents {
    private FestivalEvents() {}

    public static final String TAG = "ct_festival";

    private static List<Archipelago.Island> towns() {
        return Archipelago.ISLANDS.stream().filter(i -> i.style() == Archipelago.Style.TOWN || i == Archipelago.DRIFTWOOD_ISLAND).toList();
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(FestivalEvents::tick);
        ServerLivingEntityEvents.AFTER_DEATH.register((victim, source) -> {
            if (victim.getCommandTags().contains(TAG) && source.getAttacker() instanceof ServerPlayerEntity) {
                victim.dropStack(new ItemStack(ModItems.FESTIVAL_TOKEN, 1 + victim.getRandom().nextInt(2)));
            }
        });
        CommandRegistrationCallback.EVENT.register((d, a, env) -> d.register(literal("festival")
                .executes(ctx -> { info(ctx.getSource().getPlayerOrThrow()); return 1; })
                .then(literal("trinket").executes(ctx -> { redeem(ctx.getSource().getPlayerOrThrow(), 0); return 1; }))
                .then(literal("chest").executes(ctx -> { redeem(ctx.getSource().getPlayerOrThrow(), 1); return 1; }))
                .then(literal("title").executes(ctx -> { redeem(ctx.getSource().getPlayerOrThrow(), 2); return 1; }))));
    }

    private static void tick(MinecraftServer server) {
        if (!com.cursedtides.world.ModWorld.isArchipelago()) return;
        Festivals.Festival f = Festivals.current();
        if (f == null) return;
        ServerWorld w = server.getOverworld();
        int t = server.getTicks();
        boolean night = w.isNight();
        if (t % 600 == 77) {
            for (ServerPlayerEntity p : w.getPlayers()) {
                for (Archipelago.Island town : towns()) {
                    if (Math.hypot(p.getX() - town.cx(), p.getZ() - town.cz()) > town.radius() + 40) continue;
                    visitors(w, f, town, p, night);
                }
            }
        }
        if (night && t % 160 == 31 && (f == Festivals.Festival.REGATTA_WEEK || f == Festivals.Festival.MIDWINTER_FEAST
                || f == Festivals.Festival.SAINT_VESPERS_DAY)) {
            for (Archipelago.Island town : towns()) {
                if (w.getPlayers(q -> Math.hypot(q.getX() - town.cx(), q.getZ() - town.cz()) < 200).isEmpty()) continue;
                fireworks(w, town, f);
            }
        }
    }

    private static void visitors(ServerWorld w, Festivals.Festival f, Archipelago.Island town, ServerPlayerEntity p, boolean night) {
        boolean wantsNight = f == Festivals.Festival.NIGHT_OF_LANTERNS || f == Festivals.Festival.SAINT_VESPERS_DAY || f == Festivals.Festival.MIDWINTER_FEAST;
        if (f == Festivals.Festival.REGATTA_WEEK || wantsNight != night) return;
        if (w.getEntitiesByClass(MobEntity.class, p.getBoundingBox().expand(48), m -> m.getCommandTags().contains(TAG)).size() >= 4) return;
        BlockPos at = Residents.openGround(w, p.getBlockX() + w.random.nextBetween(-20, 20), p.getBlockZ() + w.random.nextBetween(-20, 20), 8);
        if (at == null || at.isWithinDistance(p.getBlockPos(), 8)) return;
        MobEntity m;
        String name;
        switch (f) {
            case SPRING_TIDE_FAIR -> { m = com.cursedtides.wildlife.Wildlife.CRAB.create(w); name = "Golden Crab"; }
            case SAINT_VESPERS_DAY -> { m = EntityType.VEX.create(w); name = "Penitent Shade"; }
            case NIGHT_OF_LANTERNS -> { m = ModEntities.CURSED_PIRATE.create(w); name = "Lantern Ghost"; }
            case SALT_HARVEST -> { m = EntityType.PILLAGER.create(w); name = "Salt Thief"; }
            default -> { m = EntityType.STRAY.create(w); name = "Frost Wraith"; }
        }
        if (m == null) return;
        m.refreshPositionAndAngles(at.getX() + 0.5, at.getY() + (f == Festivals.Festival.SAINT_VESPERS_DAY ? 2 : 0), at.getZ() + 0.5, w.random.nextFloat() * 360, 0);
        m.initialize(w, w.getLocalDifficulty(at), SpawnReason.EVENT, null);
        m.setCustomName(Text.literal(name).formatted(Formatting.LIGHT_PURPLE));
        m.addCommandTag(TAG);
        if (m instanceof CursedPirateEntity cp) cp.setVariant(CursedPirateEntity.Kind.BONE, CursedPirateEntity.Rank.GRUNT);
        if (f == Festivals.Festival.NIGHT_OF_LANTERNS) m.equipStack(net.minecraft.entity.EquipmentSlot.HEAD, new ItemStack(Items.JACK_O_LANTERN));
        if (f == Festivals.Festival.SPRING_TIDE_FAIR || f == Festivals.Festival.NIGHT_OF_LANTERNS) {
            m.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 20 * 60 * 5, 0, false, false));
        }
        int[] band = Levels.bandAt(at);
        Levels.apply(m, band[0]);
        w.spawnEntity(m);
    }

    private static void fireworks(ServerWorld w, Archipelago.Island town, Festivals.Festival f) {
        int[][] palette = switch (f) {
            case MIDWINTER_FEAST -> new int[][]{{0xFFFFFF, 0x88CCFF}, {0xCC2222, 0x22AA44}};
            case SAINT_VESPERS_DAY -> new int[][]{{0xFFD24A, 0xFFFFFF}, {0xB088FF, 0xFFD24A}};
            default -> new int[][]{{0xFF4A3A, 0xFFD24A}, {0x3A8AFF, 0xFFFFFF}, {0x3AFF8A, 0xFF3AFF}};
        };
        FireworkExplosionComponent.Type[] shapes = FireworkExplosionComponent.Type.values();
        for (int k = 0; k < 3; k++) {
            int[] cols = palette[w.random.nextInt(palette.length)];
            ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET);
            rocket.set(DataComponentTypes.FIREWORKS, new FireworksComponent(2, List.of(new FireworkExplosionComponent(
                    shapes[w.random.nextInt(shapes.length)], IntList.of(cols), IntList.of(0xFFFFFF), w.random.nextBoolean(), true))));
            double x = town.cx() + w.random.nextGaussian() * 12, z = town.cz() + w.random.nextGaussian() * 12;
            int y = Math.max(Archipelago.WATER_TOP + 2, Archipelago.column((int) x, (int) z).top() + 2);
            w.spawnEntity(new FireworkRocketEntity(w, x, y, z, rocket));
        }
    }

    /** Regatta Week pays tokens for every ship sunk. */
    public static void onShipSunk(ServerPlayerEntity p, ShipEntity s) {
        if (Festivals.current() == Festivals.Festival.REGATTA_WEEK) {
            com.cursedtides.quest.QuestData.give(p, new ItemStack(ModItems.FESTIVAL_TOKEN, 2 + s.shipClass().ordinal() / 2));
        }
    }

    private static final int TRINKET = 10, CHEST = 15, TITLE = 25;

    private static void info(ServerPlayerEntity p) {
        Festivals.Festival f = Festivals.current();
        int have = p.getInventory().count(ModItems.FESTIVAL_TOKEN);
        if (f == null) {
            p.sendMessage(Text.literal("No festival on just now. (" + have + " tokens saved; they keep.)").formatted(Formatting.GRAY), false);
            return;
        }
        p.sendMessage(Text.literal(f.title + ": " + f.perk).formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD), false);
        p.sendMessage(Text.literal("You have " + have + " festival tokens. Spend them:").formatted(Formatting.GRAY), false);
        p.sendMessage(Text.literal("  [A festival trinket: " + TRINKET + "]").styled(s -> s.withColor(Formatting.AQUA)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/festival trinket")))
                .append(Text.literal("  [A sea chest: " + CHEST + "]").styled(s -> s.withColor(Formatting.GOLD)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/festival chest"))))
                .append(Text.literal("  [The title \"" + honour(f) + "\": " + TITLE + "]").styled(s -> s.withColor(Formatting.LIGHT_PURPLE)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/festival title")))), false);
    }

    private static String honour(Festivals.Festival f) {
        return switch (f) {
            case SPRING_TIDE_FAIR -> "Crab Wrangler of the Fair";
            case REGATTA_WEEK -> "Regatta Raider";
            case SAINT_VESPERS_DAY -> "Penitent of Saint Vesper";
            case NIGHT_OF_LANTERNS -> "Lantern-Night Reveller";
            case SALT_HARVEST -> "Salt Warden";
            case MIDWINTER_FEAST -> "Midwinter Merrymaker";
        };
    }

    private static void redeem(ServerPlayerEntity p, int what) {
        Festivals.Festival f = Festivals.current();
        if (f == null) { p.sendMessage(Text.literal("The stalls open when a festival's on.").formatted(Formatting.GRAY), false); return; }
        int cost = what == 0 ? TRINKET : what == 1 ? CHEST : TITLE;
        if (p.getInventory().count(ModItems.FESTIVAL_TOKEN) < cost) {
            p.sendMessage(Text.literal("You need " + cost + " festival tokens.").formatted(Formatting.RED), false);
            return;
        }
        int left = cost;
        var inv = p.getInventory();
        for (int i = 0; i < inv.size() && left > 0; i++) {
            ItemStack s = inv.getStack(i);
            if (!s.isOf(ModItems.FESTIVAL_TOKEN)) continue;
            int k = Math.min(left, s.getCount());
            s.decrement(k);
            left -= k;
        }
        switch (what) {
            case 0 -> com.cursedtides.quest.QuestData.give(p, new ItemStack(com.cursedtides.appearance.Accessories.random(p.getRandom(), true)));
            case 1 -> com.cursedtides.quest.QuestData.give(p, com.cursedtides.combat.Loot.container(com.cursedtides.combat.Loot.Tier.CHEST,
                    Math.max(5, com.cursedtides.pirate.PirateData.notoriety(p))));
            default -> com.cursedtides.social.Titles.grant(p, honour(f));
        }
        p.sendMessage(Text.literal("Happy " + f.title + "!").formatted(Formatting.LIGHT_PURPLE), false);
    }
}
