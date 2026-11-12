package com.cursedtides.living;

import com.cursedtides.pirate.Weapon;
import com.cursedtides.profession.Profession;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.MonthDay;

/**
 * Seasonal festivals, kept by the real calendar. Each favours a pastime with bonus xp,
 * colours the towns at night, and changes what people talk about.
 */
public final class Festivals {
    private Festivals() {}

    public enum Festival {
        SPRING_TIDE_FAIR("Spring Tide Fair", MonthDay.of(3, 18), MonthDay.of(4, 3), "Fishing and cooking xp +50%, fish sell high", null, Profession.FISHING),
        REGATTA_WEEK("Regatta Week", MonthDay.of(6, 20), MonthDay.of(7, 4), "Sailing xp +50%, regatta purses doubled", Weapon.SAILING, null),
        SAINT_VESPERS_DAY("Saint Vesper's Feast", MonthDay.of(8, 10), MonthDay.of(8, 17), "Brewing xp +50%, the abbey bells ring", null, Profession.BREWING),
        NIGHT_OF_LANTERNS("Night of Lanterns", MonthDay.of(10, 24), MonthDay.of(11, 3), "Hex xp +50%, the dead walk closer", Weapon.HEX, null),
        SALT_HARVEST("Salt Harvest", MonthDay.of(11, 8), MonthDay.of(11, 22), "Smithing and hunting xp +50%, markets bustle", null, Profession.SMITHING),
        MIDWINTER_FEAST("Midwinter Feast", MonthDay.of(12, 18), MonthDay.of(1, 3), "Cooking xp +50%, gifts at the tavern", null, Profession.COOKING);

        public final String title; final MonthDay from, to; public final String perk;
        @Nullable public final Weapon weapon; @Nullable public final Profession profession;

        Festival(String title, MonthDay from, MonthDay to, String perk, @Nullable Weapon weapon, @Nullable Profession profession) {
            this.title = title; this.from = from; this.to = to; this.perk = perk; this.weapon = weapon; this.profession = profession;
        }

        boolean on(MonthDay d) {
            return from.isBefore(to) || from.equals(to) ? !d.isBefore(from) && !d.isAfter(to) : !d.isBefore(from) || !d.isAfter(to);
        }
    }

    @Nullable
    public static Festival current() {
        MonthDay today = MonthDay.from(LocalDate.now());
        for (Festival f : Festival.values()) if (f.on(today)) return f;
        return null;
    }

    public static String currentName() {
        Festival f = current();
        return f == null ? "no festival" : f.title;
    }

    public static double bonus(Weapon w) {
        Festival f = current();
        return f != null && f.weapon == w ? 0.5 : 0;
    }

    public static double bonus(Profession p) {
        Festival f = current();
        if (f == null) return 0;
        if (f.profession == p) return 0.5;
        if (f == Festival.SPRING_TIDE_FAIR && p == Profession.COOKING) return 0.5;
        if (f == Festival.SALT_HARVEST && p == Profession.HUNTING) return 0.5;
        return 0;
    }

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            Festival f = current();
            if (f == null) return;
            com.cursedtides.activity.Achievements.grant(handler.getPlayer(), "festival");
            handler.getPlayer().playSoundToPlayer(com.cursedtides.sound.ModSounds.FESTIVAL.value(), net.minecraft.sound.SoundCategory.MASTER, 0.8f, 1f);
            handler.getPlayer().sendMessage(Text.literal("✦ The " + f.title + " is on! ").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD)
                    .append(Text.literal(f.perk + ".").formatted(Formatting.LIGHT_PURPLE)), false);
        });
        ServerTickEvents.END_SERVER_TICK.register(Festivals::tick);
    }

    /** Lanterns and fireworks over any town with a player nearby, after dark. */
    private static void tick(MinecraftServer server) {
        if (server.getTicks() % 40 != 0) return;
        Festival f = current();
        if (f == null) return;
        ServerWorld world = server.getOverworld();
        long t = world.getTimeOfDay() % 24000;
        if (t < 12500 || t > 23000) return;
        for (ServerPlayerEntity p : world.getPlayers()) {
            var town = Prosperity.nearestPort(p.getBlockX(), p.getBlockZ(), 90);
            if (town == null) continue;
            double x = town.island().cx() + world.random.nextInt(41) - 20;
            double z = town.island().cz() + world.random.nextInt(41) - 20;
            double y = com.cursedtides.world.Builds.groundY((int) x, (int) z) + 14 + world.random.nextInt(8);
            if (f == Festival.NIGHT_OF_LANTERNS) {
                world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 12, 0.6, 0.6, 0.6, 0.01);
            } else {
                world.spawnParticles(ParticleTypes.FIREWORK, x, y, z, 40, 0.2, 0.2, 0.2, 0.25);
                world.spawnParticles(ParticleTypes.FLASH, x, y, z, 1, 0, 0, 0, 0);
            }
        }
    }
}
