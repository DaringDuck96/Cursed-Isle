package com.cursedtides.character;

import com.cursedtides.CursedTides;
import com.cursedtides.faction.Faction;
import com.cursedtides.faction.FactionData;
import com.cursedtides.item.ModClothing;
import com.cursedtides.item.ModItems;
import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Your pirate: name, background and outfit colour, chosen once when you first arrive. */
public final class CharacterData {
    private CharacterData() {}

    public static final AttachmentType<Boolean> CREATED = AttachmentRegistry.<Boolean>builder()
            .persistent(Codec.BOOL).copyOnDeath().initializer(() -> false)
            .buildAndRegister(CursedTides.id("character_created"));
    public static final AttachmentType<String> NAME = AttachmentRegistry.<String>builder()
            .persistent(Codec.STRING).copyOnDeath().initializer(() -> "")
            .buildAndRegister(CursedTides.id("pirate_name"));
    public static final AttachmentType<Integer> BACKGROUND = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT).copyOnDeath().initializer(() -> 0)
            .buildAndRegister(CursedTides.id("background"));

    private static final Map<UUID, Integer> pendingOpen = new HashMap<>();

    public static void register() {
        PayloadTypeRegistry.playS2C().register(CharacterPayloads.Open.ID, CharacterPayloads.Open.CODEC);
        PayloadTypeRegistry.playC2S().register(CharacterPayloads.Create.ID, CharacterPayloads.Create.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(CharacterPayloads.Create.ID, (payload, context) ->
                context.server().execute(() -> create(context.player(), payload)));

        // Give the world a moment to finish loading before the screen opens
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (!isCreated(handler.getPlayer())) {
                pendingOpen.put(handler.getPlayer().getUuid(), server.getTicks() + 40);
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (pendingOpen.isEmpty()) return;
            pendingOpen.entrySet().removeIf(entry -> {
                if (entry.getValue() > server.getTicks()) return false;
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
                if (player != null && !isCreated(player)) openScreen(player);
                return true;
            });
        });
    }

    public static boolean isCreated(PlayerEntity player) {
        return player.getAttachedOrElse(CREATED, false);
    }

    public static String pirateName(PlayerEntity player) {
        String name = player.getAttachedOrElse(NAME, "");
        return name.isEmpty() ? player.getName().getString() : name;
    }

    public static Background background(PlayerEntity player) {
        return Background.byIndex(player.getAttachedOrElse(BACKGROUND, 0));
    }

    public static void openScreen(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, new CharacterPayloads.Open(!isCreated(player), pirateName(player),
                player.getAttachedOrElse(BACKGROUND, 0)));
    }

    private static String clean(String raw, ServerPlayerEntity player) {
        String name = raw == null ? "" : raw.replaceAll("[^A-Za-z '\\-]", "").trim().replaceAll(" +", " ");
        if (name.length() > 28) name = name.substring(0, 28).trim();
        return name.length() < 2 ? player.getName().getString() : name;
    }

    private static void create(ServerPlayerEntity player, CharacterPayloads.Create choice) {
        String name = clean(choice.name(), player);
        player.setAttached(NAME, name);
        if (isCreated(player)) {
            player.sendMessage(Text.literal("You'll be known as " + name + " from now on.").formatted(Formatting.GOLD), false);
            return;
        }
        Background bg = Background.byIndex(choice.background());
        int rgb = DyeColor.byId(Math.max(0, Math.min(15, choice.color()))).getEntityColor();
        player.setAttached(CREATED, true);
        player.setAttached(BACKGROUND, bg.ordinal());

        switch (bg) {
            case CASTAWAY -> {
                dyed(player, ModClothing.SAILOR_BANDANA, rgb);
                dyed(player, ModClothing.SAILOR_SHIRT, rgb);
                dyed(player, ModClothing.SAILOR_BREECHES, rgb);
                dyed(player, ModClothing.SAILOR_BOOTS, rgb);
                give(player, new ItemStack(Items.SPYGLASS));
                PirateData.addXp(player, Weapon.SAILING, 120);
            }
            case DESERTER -> {
                give(player, new ItemStack(ModClothing.NAVY_COAT));
                dyed(player, ModClothing.SAILOR_BREECHES, rgb);
                dyed(player, ModClothing.SAILOR_BOOTS, rgb);
                give(player, new ItemStack(ModItems.FLINTLOCK_PISTOL));
                give(player, new ItemStack(ModItems.PISTOL_SHOT, 10));
                PirateData.addXp(player, Weapon.PISTOL, 120);
                FactionData.adjust(player, Faction.NAVY, -60);
                FactionData.adjust(player, Faction.BRETHREN, 20);
            }
            case SMUGGLER -> {
                dyed(player, ModClothing.SAILOR_BANDANA, rgb);
                dyed(player, ModClothing.SAILOR_SHIRT, rgb);
                dyed(player, ModClothing.SAILOR_BREECHES, rgb);
                dyed(player, ModClothing.SAILOR_BOOTS, rgb);
                give(player, new ItemStack(ModItems.DAGGER));
                give(player, new ItemStack(ModItems.THROWING_KNIFE, 8));
                give(player, new ItemStack(ModItems.DOUBLOON, 15));
                PirateData.addXp(player, Weapon.CUTLASS, 120);
                FactionData.adjust(player, Faction.BRETHREN, 60);
            }
            case HEXER -> {
                ModClothing.BAYOU_SET.forEach(item -> give(player, new ItemStack(item)));
                give(player, new ItemStack(ModItems.HEX_STAFF));
                PirateData.addXp(player, Weapon.HEX, 150);
            }
        }
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 0.6f, 1.2f);
        player.sendMessage(Text.literal("Welcome to the Shattered Reach, " + name + ", " + bg.title() + ".")
                .formatted(Formatting.GOLD), false);
        player.sendMessage(Text.literal("Your gear is in your pockets: put the clothes on! Press J for your journal, M for the chart.")
                .formatted(Formatting.GRAY), false);
    }

    private static void dyed(PlayerEntity player, Item item, int rgb) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(rgb, true));
        give(player, stack);
    }

    private static void give(PlayerEntity player, ItemStack stack) {
        if (!player.getInventory().insertStack(stack)) {
            player.dropItem(stack, false);
        }
    }
}
