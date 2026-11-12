package com.cursedtides.sound;

import com.cursedtides.CursedTides;
import com.cursedtides.entity.ShipEntity;
import com.cursedtides.entity.StoryNpcEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

/**
 * The mod's own sounds: four original shanties (as music, and as discs for any jukebox), sword clashes, crits and
 * finishers, cannon booms, ship creaks, gulls, tavern murmur, the drowned bell, whirlpools and coin.
 * Also plays the ambience of the Reach around players: gulls over the ports, murmur by the bar, timbers at sea.
 */
public final class ModSounds {
    private ModSounds() {}

    private static RegistryEntry.Reference<SoundEvent> reg(String path) {
        var id = CursedTides.id(path);
        return Registry.registerReference(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static final RegistryEntry.Reference<SoundEvent> WRECKERS_REEL = reg("music.wreckers_reel");
    public static final RegistryEntry.Reference<SoundEvent> LANTERNS_DROWN = reg("music.lanterns_drown");
    public static final RegistryEntry.Reference<SoundEvent> SALTMARROW_HORNPIPE = reg("music.saltmarrow_hornpipe");
    public static final RegistryEntry.Reference<SoundEvent> NINTH_WAVE = reg("music.ninth_wave");
    public static final RegistryEntry.Reference<SoundEvent> MUSIC_SEA = reg("music.sea");

    public static final RegistryEntry.Reference<SoundEvent> SWORD_CLASH = reg("sfx.sword_clash");
    public static final RegistryEntry.Reference<SoundEvent> CRITICAL = reg("sfx.critical");
    public static final RegistryEntry.Reference<SoundEvent> FINISHER = reg("sfx.finisher");
    public static final RegistryEntry.Reference<SoundEvent> CANNON_BOOM = reg("sfx.cannon_boom");
    public static final RegistryEntry.Reference<SoundEvent> SHIP_CREAK = reg("sfx.ship_creak");
    public static final RegistryEntry.Reference<SoundEvent> SEAGULLS = reg("sfx.seagulls");
    public static final RegistryEntry.Reference<SoundEvent> WAVES = reg("sfx.waves");
    public static final RegistryEntry.Reference<SoundEvent> TAVERN_CROWD = reg("sfx.tavern_crowd");
    public static final RegistryEntry.Reference<SoundEvent> BELL_TOLL = reg("sfx.bell_toll");
    public static final RegistryEntry.Reference<SoundEvent> WHIRLPOOL = reg("sfx.whirlpool");
    public static final RegistryEntry.Reference<SoundEvent> COINS = reg("sfx.coins");
    public static final RegistryEntry.Reference<SoundEvent> ULTIMATE = reg("sfx.ultimate");
    public static final RegistryEntry.Reference<SoundEvent> FESTIVAL = reg("sfx.festival");

    public static final List<Item> DISCS = new ArrayList<>();

    private static Item disc(String name) {
        RegistryKey<JukeboxSong> song = RegistryKey.of(RegistryKeys.JUKEBOX_SONG, CursedTides.id(name));
        Item item = Registry.register(Registries.ITEM, CursedTides.id("music_disc_" + name),
                new Item(new Item.Settings().maxCount(1).rarity(Rarity.RARE).jukeboxPlayable(song)));
        DISCS.add(item);
        return item;
    }

    public static final Item DISC_REEL = disc("wreckers_reel");
    public static final Item DISC_LANTERNS = disc("lanterns_drown");
    public static final Item DISC_HORNPIPE = disc("saltmarrow_hornpipe");
    public static final Item DISC_NINTH = disc("ninth_wave");

    public static void register() {
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(RegistryKey.of(RegistryKeys.ITEM_GROUP, CursedTides.id("main")))
                .register(entries -> DISCS.forEach(entries::add));
        ServerTickEvents.END_SERVER_TICK.register(ModSounds::ambience);
    }

    private static void ambience(MinecraftServer server) {
        int t = server.getTicks();
        if (t % 100 != 0) return;
        ServerWorld world = server.getOverworld();
        long tod = world.getTimeOfDay() % 24000;
        boolean day = tod > 0 && tod < 12500;
        for (ServerPlayerEntity p : world.getPlayers()) {
            var r = p.getRandom();
            if (p.getVehicle() instanceof ShipEntity ship) {
                if (r.nextInt(3) == 0) p.playSoundToPlayer(SHIP_CREAK.value(), SoundCategory.AMBIENT, 0.5f, 0.8f + r.nextFloat() * 0.4f);
                if (r.nextInt(4) == 0) p.playSoundToPlayer(WAVES.value(), SoundCategory.AMBIENT, 0.4f, 1f);
                continue;
            }
            Box near = p.getBoundingBox().expand(14);
            boolean tavern = !world.getEntitiesByClass(StoryNpcEntity.class, near,
                    n -> n.getRole() == StoryNpcEntity.Role.TAVERN_KEEPER || n.getRole() == StoryNpcEntity.Role.DEALER).isEmpty();
            if (tavern && r.nextInt(2) == 0) {
                p.playSoundToPlayer(TAVERN_CROWD.value(), SoundCategory.AMBIENT, 0.35f, 1f);
            } else if (day && com.cursedtides.living.Prosperity.nearestPort(p.getBlockX(), p.getBlockZ(), 110) != null
                    && world.isSkyVisible(p.getBlockPos()) && r.nextInt(4) == 0) {
                p.playSoundToPlayer(SEAGULLS.value(), SoundCategory.AMBIENT, 0.45f, 0.9f + r.nextFloat() * 0.2f);
            } else if (!day && p.getY() < 70 && r.nextInt(30) == 0) {
                p.playSoundToPlayer(BELL_TOLL.value(), SoundCategory.AMBIENT, 0.25f, 0.6f); // somewhere far off, the Drowned Bell
            }
        }
    }
}
