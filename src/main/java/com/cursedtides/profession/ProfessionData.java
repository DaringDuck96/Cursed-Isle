package com.cursedtides.profession;

import com.cursedtides.CursedTides;
import com.cursedtides.pirate.PirateData;
import com.cursedtides.wildlife.Wildlife;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Profession experience (same curve as the proficiencies: level 30 at the top). */
public final class ProfessionData {
    private ProfessionData() {}

    private static final Map<Profession, AttachmentType<Integer>> XP = new EnumMap<>(Profession.class);

    static {
        for (Profession p : Profession.values()) {
            XP.put(p, AttachmentRegistry.<Integer>builder()
                    .persistent(Codec.INT)
                    .copyOnDeath()
                    .initializer(() -> 0)
                    .buildAndRegister(CursedTides.id("profession_" + p.name().toLowerCase())));
        }
    }

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register(ProfessionData::onKill);
        Fishing.register();
        FishingContest.register();
    }

    public static int xp(PlayerEntity player, Profession p) {
        return player.getAttachedOrElse(XP.get(p), 0);
    }

    public static int level(PlayerEntity player, Profession p) {
        return PirateData.levelForXp(xp(player, p));
    }

    public static void addXp(ServerPlayerEntity player, Profession p, int amount) {
        int before = level(player, p);
        amount = (int) Math.round(amount * (1.0 + com.cursedtides.living.Festivals.bonus(p)));
        player.setAttached(XP.get(p), xp(player, p) + amount);
        int after = level(player, p);
        if (after > before) {
            player.sendMessage(Text.literal(p.displayName + " is now level " + after + "!").formatted(Formatting.GOLD, Formatting.BOLD), false);
            player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.6f, 1.3f);
        } else {
            player.sendMessage(Text.literal("+" + amount + " " + p.displayName + " xp").formatted(Formatting.DARK_AQUA), true);
        }
    }

    /** Journal section. */
    public static List<String> journal(PlayerEntity player) {
        List<String> lines = new ArrayList<>();
        for (Profession p : Profession.values()) {
            int level = level(player, p);
            String bar = "|".repeat(Math.round(level / 30f * 20));
            lines.add("§f" + p.displayName + " §7Lv " + level + "  §a" + bar);
        }
        lines.add("§8Fish with any rod; cook and brew at a crafting table; hone gear with a whetstone or kit.");
        return lines;
    }

    // ------------------------------------------------------------------ hunting

    private static void onKill(LivingEntity victim, DamageSource source) {
        if (!(source.getAttacker() instanceof ServerPlayerEntity hunter)) return;
        EntityType<?> t = victim.getType();
        ItemStack spoils;
        int xp;
        if (t == Wildlife.SHARK) { spoils = new ItemStack(com.cursedtides.item.ModItems.SHARK_TOOTH); xp = 14; }
        else if (t == Wildlife.CROCODILE) { spoils = new ItemStack(Items.LEATHER, 2); xp = victim.getCommandTags().contains("cursedtides_boss") ? 120 : 14; }
        else if (t == Wildlife.JAGUAR) { spoils = new ItemStack(Items.LEATHER); xp = 12; }
        else if (t == Wildlife.BOAR) { spoils = new ItemStack(Items.PORKCHOP); xp = 6; }
        else if (t == Wildlife.SNAKE) { spoils = new ItemStack(Items.STRING); xp = 5; }
        else if (t == Wildlife.CRAB) { spoils = new ItemStack(Items.BONE_MEAL); xp = 3; }
        else if (t == EntityType.COW || t == EntityType.PIG || t == EntityType.SHEEP || t == EntityType.CHICKEN
                || t == EntityType.GOAT || t == EntityType.RABBIT) { spoils = null; xp = 2; }
        else return;
        addXp(hunter, Profession.HUNTING, xp);
        if (xp > 2) com.cursedtides.activity.Challenges.progress(hunter, "hunt", 1);
        int level = level(hunter, Profession.HUNTING);
        if (spoils != null && hunter.getRandom().nextFloat() < level * 0.025f) {
            victim.dropStack(spoils);
        }
    }
}
