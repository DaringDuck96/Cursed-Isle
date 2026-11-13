package com.cursedtides.sound;

import com.cursedtides.CursedTides;
import com.cursedtides.entity.ShipEntity;
import com.cursedtides.entity.StoryNpcEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.phys.AABB;
import java.util.ArrayList;
import java.util.List;

/**
 * The mod's own sounds: four original shanties (as music, and as discs for any jukebox), sword clashes, crits and
 * finishers, cannon booms, ship creaks, gulls, tavern murmur, the drowned bell, whirlpools and coin.
 * Also plays the ambience of the Reach around players: gulls over the ports, murmur by the bar, timbers at sea.
 */
public final class ModSounds {
    private ModSounds() {}

    private static Holder.Reference<SoundEvent> reg(String path) {
        var id = CursedTides.id(path);
        return Registry.registerForHolder(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static final Holder.Reference<SoundEvent> WRECKERS_REEL = reg("music.wreckers_reel");
    public static final Holder.Reference<SoundEvent> LANTERNS_DROWN = reg("music.lanterns_drown");
    public static final Holder.Reference<SoundEvent> SALTMARROW_HORNPIPE = reg("music.saltmarrow_hornpipe");
    public static final Holder.Reference<SoundEvent> NINTH_WAVE = reg("music.ninth_wave");
    public static final Holder.Reference<SoundEvent> MUSIC_SEA = reg("music.sea");
    public static final Holder.Reference<SoundEvent> UNDER_THE_CANOPY = reg("music.under_the_canopy");
    public static final Holder.Reference<SoundEvent> HOLLOW_BONES = reg("music.hollow_bones");
    public static final Holder.Reference<SoundEvent> FULL_SAIL = reg("music.full_sail");
    public static final Holder.Reference<SoundEvent> STEEL_AND_POWDER = reg("music.steel_and_powder");
    public static final Holder.Reference<SoundEvent> ADMIRALTY_MARCH = reg("music.admiralty_march");

    public static final Holder.Reference<SoundEvent> SWORD_CLASH = reg("sfx.sword_clash");
    public static final Holder.Reference<SoundEvent> CRITICAL = reg("sfx.critical");
    public static final Holder.Reference<SoundEvent> FINISHER = reg("sfx.finisher");
    public static final Holder.Reference<SoundEvent> CANNON_BOOM = reg("sfx.cannon_boom");
    public static final Holder.Reference<SoundEvent> SHIP_CREAK = reg("sfx.ship_creak");
    public static final Holder.Reference<SoundEvent> SEAGULLS = reg("sfx.seagulls");
    public static final Holder.Reference<SoundEvent> WAVES = reg("sfx.waves");
    public static final Holder.Reference<SoundEvent> TAVERN_CROWD = reg("sfx.tavern_crowd");
    public static final Holder.Reference<SoundEvent> BELL_TOLL = reg("sfx.bell_toll");
    public static final Holder.Reference<SoundEvent> WHIRLPOOL = reg("sfx.whirlpool");
    public static final Holder.Reference<SoundEvent> COINS = reg("sfx.coins");
    public static final Holder.Reference<SoundEvent> ULTIMATE = reg("sfx.ultimate");
    public static final Holder.Reference<SoundEvent> FESTIVAL = reg("sfx.festival");

    public static final List<Item> DISCS = new ArrayList<>();

    private static Item disc(String name) {
        ResourceKey<JukeboxSong> song = ResourceKey.create(Registries.JUKEBOX_SONG, CursedTides.id(name));
        Item item = Registry.register(BuiltInRegistries.ITEM, com.cursedtides.util.Reg.itemId("music_disc_" + name),
                new Item(com.cursedtides.util.Reg.props().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(song)));
        DISCS.add(item);
        return item;
    }

    public static final Item DISC_REEL = disc("wreckers_reel");
    public static final Item DISC_LANTERNS = disc("lanterns_drown");
    public static final Item DISC_HORNPIPE = disc("saltmarrow_hornpipe");
    public static final Item DISC_NINTH = disc("ninth_wave");

    public static void register() {
        net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents.modifyOutputEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB, CursedTides.id("main")))
                .register(entries -> DISCS.forEach(entries::accept));
        ServerTickEvents.END_SERVER_TICK.register(ModSounds::ambience);
    }

    private static void ambience(MinecraftServer server) {
        int t = server.getTickCount();
        if (t % 100 != 0) return;
        ServerLevel world = server.overworld();
        long tod = world.getOverworldClockTime() % 24000;
        boolean day = tod > 0 && tod < 12500;
        for (ServerPlayer p : world.players()) {
            var r = p.getRandom();
            if ((!p.getShoulderEntityLeft().isEmpty() || !p.getShoulderEntityRight().isEmpty()) && r.nextInt(12) == 0) {
                String[] lines = {"Silver! Silver!", "Mind the boom! Mind the boom!", "Rum's gone! Rum's gone!", "Pretty bird! Pretty pirate!",
                        "All hands! All hands!", "Dead men bite! Dead men bite!", "Squawk! Doubloons!", "Who's a good captain?"};
                Component line = Component.literal("[Parrot] " + lines[r.nextInt(lines.length)]).withStyle(ChatFormatting.GREEN);
                for (ServerPlayer o : world.getPlayers(o -> o.distanceToSqr(p) < 16 * 16)) o.sendSystemMessage(line, false);
                world.playSound(null, p.blockPosition(), net.minecraft.sounds.SoundEvents.PARROT_AMBIENT, SoundSource.NEUTRAL, 1f, 1f);
            }
            if (p.getVehicle() instanceof ShipEntity ship) {
                if (r.nextInt(3) == 0) com.cursedtides.util.Compat.sound(p, SHIP_CREAK.value(), SoundSource.AMBIENT, 0.5f, 0.8f + r.nextFloat() * 0.4f);
                if (r.nextInt(4) == 0) com.cursedtides.util.Compat.sound(p, WAVES.value(), SoundSource.AMBIENT, 0.4f, 1f);
                continue;
            }
            AABB near = p.getBoundingBox().inflate(14);
            boolean tavern = !world.getEntitiesOfClass(StoryNpcEntity.class, near,
                    n -> n.getRole() == StoryNpcEntity.Role.TAVERN_KEEPER || n.getRole() == StoryNpcEntity.Role.DEALER).isEmpty();
            if (tavern && r.nextInt(2) == 0) {
                com.cursedtides.util.Compat.sound(p, TAVERN_CROWD.value(), SoundSource.AMBIENT, 0.35f, 1f);
            } else if (day && com.cursedtides.living.Prosperity.nearestPort(p.getBlockX(), p.getBlockZ(), 110) != null
                    && world.canSeeSky(p.blockPosition()) && r.nextInt(4) == 0) {
                com.cursedtides.util.Compat.sound(p, SEAGULLS.value(), SoundSource.AMBIENT, 0.45f, 0.9f + r.nextFloat() * 0.2f);
            } else if (!day && p.getY() < 70 && r.nextInt(30) == 0) {
                com.cursedtides.util.Compat.sound(p, BELL_TOLL.value(), SoundSource.AMBIENT, 0.25f, 0.6f); // somewhere far off, the Drowned Bell
            }
        }
    }
}
