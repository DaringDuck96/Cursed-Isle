package com.cursedtides.quest;

import net.minecraft.world.entity.EntityTypes;
import com.cursedtides.CursedTides;
import com.cursedtides.dialogue.Specials;
import com.cursedtides.entity.ModEntities;
import com.cursedtides.item.ModItems;
import com.cursedtides.world.Archipelago;
import com.cursedtides.world.Builds;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.phys.AABB;
import java.util.ArrayList;
import java.util.List;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/**
 * The third arc, "The Hollow Crown". With the Horizon settled, a Pretender rises: someone has found the crown of
 * drowned Vesh Amaru, empty of its three shards, and is gathering the cursed and the lawless to wear it. Find the shards
 * of Salt (the grotto beneath the drowned city), Ash (the Ashen Herald on Emberfall) and Mist (the hidden Isle That
 * Isn't, only at night), find the Pretender's seat at Kingsrest, and decide what becomes of the crown.
 */
public final class CrownArc {
    private CrownArc() {}

    public static final int NONE = 0, PROCLAIMED = 1, SHARDS = 2, SEAT = 3, CONFRONT = 4, CHOOSE = 5, DONE = 6;
    public static final int SALT = 1, ASH = 2, MIST = 4;
    public static final int SHATTERED = 1, CROWNED = 2, RETURNED = 3;

    public static final AttachmentType<Integer> STAGE = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT).copyOnDeath().initializer(() -> 0).buildAndRegister(CursedTides.id("crown_stage"));
    public static final AttachmentType<Integer> SHARD_MASK = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT).copyOnDeath().initializer(() -> 0).buildAndRegister(CursedTides.id("crown_shards"));
    public static final AttachmentType<Integer> ENDING = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT).copyOnDeath().initializer(() -> 0).buildAndRegister(CursedTides.id("crown_ending"));

    private static Item item(String name, Rarity r) {
        return Registry.register(BuiltInRegistries.ITEM, com.cursedtides.util.Reg.itemId(name), new Item(com.cursedtides.util.Reg.props().stacksTo(1).rarity(r)));
    }

    public static final Item PROCLAMATION = item("pretenders_proclamation", Rarity.UNCOMMON);
    public static final Item SHARD_SALT = item("shard_of_salt", Rarity.RARE);
    public static final Item SHARD_ASH = item("shard_of_ash", Rarity.RARE);
    public static final Item SHARD_MIST = item("shard_of_mist", Rarity.RARE);
    public static final Item HOLLOW_CROWN = item("hollow_crown", Rarity.EPIC);

    private static final String HERALD = "cursedtides_herald", PRETENDER = "cursedtides_pretender";

    public static int stage(Player p) { return p.getAttachedOrElse(STAGE, 0); }
    public static int shards(Player p) { return p.getAttachedOrElse(SHARD_MASK, 0); }
    public static int ending(Player p) { return p.getAttachedOrElse(ENDING, 0); }
    public static int shardCount(Player p) { return Integer.bitCount(shards(p)); }

    static void setStage(Player p, int s) { p.setAttached(STAGE, s); }

    public static String title(Player p) {
        return switch (ending(p)) {
            case SHATTERED -> "Crownbreaker";
            case CROWNED -> "the Uncrowned Sovereign";
            case RETURNED -> "Warden of the Drowned";
            default -> null;
        };
    }

    public static List<String> journal(Player p) {
        List<String> lines = new ArrayList<>();
        int s = stage(p);
        lines.add("§f" + switch (s) {
            case NONE -> HorizonArc.stage(p) >= HorizonArc.DONE ? "Something is stirring. Keep your ears open in the taverns." : "(Finish the Seven Tides first.)";
            case PROCLAIMED -> "Show the Pretender's proclamation to Odalys Quill, the archivist at Kingsport.";
            case SHARDS -> "Recover the crown's three shards (" + shardCount(p) + "/3): Salt, beneath the drowned city's temple; "
                    + "Ash, from the Ashen Herald on Emberfall; Mist, at the shrine on the Isle That Isn't, by night.";
            case SEAT -> "Take the shards back to Odalys Quill. She'll know where the Pretender sits.";
            case CONFRONT -> "Sail to Kingsrest and face the Pretender in the throne hall.";
            case CHOOSE -> "Decide what becomes of the Hollow Crown (speak to Odalys Quill).";
            default -> "Complete. " + title(p) + ".";
        });
        return lines;
    }

    // ------------------------------------------------------------------ wiring

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(CrownArc::tick);
        ServerLivingEntityEvents.AFTER_DEATH.register((victim, source) -> {
            if (!(victim.level() instanceof ServerLevel world)) return;
            boolean herald = victim.entityTags().contains(HERALD), pretender = victim.entityTags().contains(PRETENDER);
            if (!herald && !pretender) return;
            for (ServerPlayer p : world.getPlayers(pl -> pl.distanceToSqr(victim) < 48 * 48)) {
                if (herald && stage(p) == SHARDS) giveShard(p, ASH);
                if (pretender && stage(p) == CONFRONT) {
                    setStage(p, CHOOSE);
                    QuestData.give(p, new ItemStack(HOLLOW_CROWN));
                    p.sendSystemMessage(Component.literal("The Pretender falls, and the crown rolls across the floor to your feet. It weighs nothing at all.")
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC), false);
                    p.sendSystemMessage(Component.literal("Take it to Odalys Quill and decide what becomes of it.").withStyle(ChatFormatting.GRAY), false);
                }
            }
            if (pretender) {
                world.getServer().getPlayerList().broadcastSystemMessage(Component.literal("The Pretender of Kingsrest is dead. The Hollow Crown lies unclaimed.")
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), false);
            }
        });

        Specials.register("crown_begin", (p, npc) -> {
            if (stage(p) != PROCLAIMED) return List.of("Nothing new in the archive on that, I'm afraid.");
            setStage(p, SHARDS);
            com.cursedtides.explore.Cartography.reveal(p, Archipelago.ISLE_THAT_ISNT);
            return List.of("This seal... this is the crown of Vesh Amaru. The drowned kings wore it. It was broken into three when the city sank.",
                    "Salt, Ash and Mist. Salt stayed with the city: look beneath the temple floor. Ash was carried to the fire mountain, Emberfall, by a herald of the old court, and it never came back down. Mist went to an island that isn't on any chart.",
                    "Here, I've copied the old chart of it into your book. The shrine there only shows itself at night.",
                    "Whoever this Pretender is, they have the crown but not the shards. We must not let them have those.");
        });
        Specials.register("crown_seat", (p, npc) -> {
            if (stage(p) != SEAT) return List.of("Come back when you have all three shards.");
            setStage(p, CONFRONT);
            com.cursedtides.explore.Cartography.reveal(p, Archipelago.KINGSREST);
            return List.of("All three. Do you feel them pulling? East. Further east than anyone sails.",
                    "Kingsrest. The old kings' summer seat, before the drowning. It's on your chart now. That's where the Pretender sits.",
                    "Go armed. Go with friends, if you have them.");
        });
        Specials.register("crown_shatter", (p, npc) -> choose(p, SHATTERED));
        Specials.register("crown_wear", (p, npc) -> choose(p, CROWNED));
        Specials.register("crown_return", (p, npc) -> choose(p, RETURNED));

        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> dispatcher.register(literal("cursedtides")
                .requires(src -> src.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
                .then(literal("crown").then(argument("stage", IntegerArgumentType.integer(0, DONE)).executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    setStage(p, IntegerArgumentType.getInteger(ctx, "stage"));
                    if (stage(p) >= SEAT) p.setAttached(SHARD_MASK, SALT | ASH | MIST);
                    p.sendSystemMessage(Component.literal("Hollow Crown stage set to " + stage(p)).withStyle(ChatFormatting.GRAY), false);
                    return 1;
                })))));
    }

    private static List<String> choose(ServerPlayer p, int ending) {
        if (stage(p) != CHOOSE) return List.of("That isn't a decision you're ready to make.");
        if (!QuestData.has(p, HOLLOW_CROWN)) return List.of("You'll need the crown itself.");
        com.cursedtides.util.Compat.take(p, s -> s.is(HOLLOW_CROWN), 1);
        p.setAttached(ENDING, ending);
        setStage(p, DONE);
        applyEnding(p);
        com.cursedtides.activity.Achievements.grant(p, "hollow_crown");
        MinecraftServer server = p.level().getServer();
        String who = com.cursedtides.character.CharacterData.pirateName(p);
        if (server != null) server.getPlayerList().broadcastSystemMessage(Component.literal(switch (ending) {
            case SHATTERED -> who + " has shattered the Hollow Crown. No one will ever wear it.";
            case CROWNED -> who + " has put on the Hollow Crown. The cursed bow as they pass.";
            default -> who + " has returned the Hollow Crown to the drowned city. Something down there sleeps easier.";
        }).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), false);
        return switch (ending) {
            case SHATTERED -> {
                p.addItem(com.cursedtides.combat.GearRarity.legendaryOf(p.level(), ModItems.BROADSWORD, p.getRandom()));
                com.cursedtides.faction.FactionData.adjust(p, com.cursedtides.faction.Faction.NAVY, 200);
                com.cursedtides.faction.FactionData.adjust(p, com.cursedtides.faction.Faction.BRETHREN, 200);
                yield List.of("You bring it down on the archive's anvil. It breaks like ice, and for a moment the whole room smells of the sea.",
                        "Good. Some things should stay broken. The Tidewatch and the Brethren will both drink to this, for once.");
            }
            case CROWNED -> {
                com.cursedtides.faction.FactionData.adjust(p, com.cursedtides.faction.Faction.NAVY, -300);
                yield List.of("You set it on your head. It fits. Of course it fits.",
                        "I won't pretend I'm happy about it. But better you than them. Try to be a kinder sovereign than the last.");
            }
            default -> {
                p.addItem(new ItemStack(com.cursedtides.explore.Diving.HELMET));
                yield List.of("Back to the deep, then. Take it down to the temple and lay it where the old kings lay.",
                        "When you come up, you'll find the sea a little gentler with you. The drowned remember a kindness.");
            }
        };
    }

    /** The lasting gifts of each ending: applied on choosing, and again on join and respawn. */
    public static void applyEnding(ServerPlayer p) {
        var health = p.getAttribute(Attributes.MAX_HEALTH);
        if (health == null) return;
        var id = CursedTides.id("crown_ending");
        health.removeModifier(id);
        if (ending(p) == CROWNED) health.addPermanentModifier(new AttributeModifier(id, 4, AttributeModifier.Operation.ADD_VALUE));
    }

    // ------------------------------------------------------------------ the world

    private static void tick(MinecraftServer server) {
        int t = server.getTickCount();
        if (t % 40 != 0) return;
        ServerLevel world = server.overworld();
        for (ServerPlayer p : world.players()) {
            int s = stage(p);
            if (s == NONE && t % 1200 == 0 && HorizonArc.stage(p) >= HorizonArc.DONE) {
                setStage(p, PROCLAIMED);
                QuestData.give(p, new ItemStack(PROCLAMATION));
                p.sendSystemMessage(Component.literal("A wet sheet of paper is nailed to the nearest post, and another flaps against your boot:").withStyle(ChatFormatting.GRAY), false);
                p.sendSystemMessage(Component.literal("\"By the grace of the drowned, the Crown of Vesh Amaru is found. All the cursed and all the free "
                        + "shall bend the knee. Signed, the Rightful Sovereign of the Reach.\"").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), false);
                p.sendSystemMessage(Component.literal("New quest: The Hollow Crown. (See your journal.)").withStyle(ChatFormatting.GOLD), false);
                continue;
            }
            if (ending(p) == RETURNED && p.isInWater()) {
                p.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 100, 0, true, false));
            }
            if (s == SHARDS) shardsTick(world, p);
            if (s == CONFRONT) confrontTick(world, p);
        }
    }

    private static boolean near(ServerPlayer p, int x, int z, int r) {
        double dx = p.getX() - x, dz = p.getZ() - z;
        return dx * dx + dz * dz < (double) r * r;
    }

    private static void shardsTick(ServerLevel world, ServerPlayer p) {
        Archipelago.Island city = Archipelago.DROWNED_CITY;
        int grottoY = Archipelago.column(city.cx(), city.cz()).top() - 12;
        if ((shards(p) & SALT) == 0 && near(p, city.cx(), city.cz(), 7) && Math.abs(p.getY() - grottoY) < 5) giveShard(p, SALT);

        Archipelago.Island mist = Archipelago.ISLE_THAT_ISNT;
        long tod = world.getOverworldClockTime() % 24000;
        if ((shards(p) & MIST) == 0 && near(p, mist.cx(), mist.cz(), 4)) {
            if (tod >= 13000 && tod < 23000) giveShard(p, MIST);
            else p.sendSystemMessage(Component.literal("The pedestal is empty. Something about the light says: come back after dark.").withStyle(ChatFormatting.GRAY), true);
        }

        Archipelago.Island ember = Archipelago.EMBERFALL;
        if ((shards(p) & ASH) == 0 && near(p, ember.cx(), ember.cz(), 50) && world.getGameTime() % 400 < 40) {
            AABB area = new AABB(ember.cx() - 80, world.getMinY(), ember.cz() - 80, ember.cx() + 80, world.getMaxY(), ember.cz() + 80);
            if (world.getEntitiesOfClass(Mob.class, area, m -> m.entityTags().contains(HERALD)).isEmpty()) {
                spawnBoss(world, p, EntityTypes.WITHER_SKELETON, "The Ashen Herald", HERALD, 220, ChatFormatting.RED);
                p.sendSystemMessage(Component.literal("Ash falls like snow. Something in a burnt robe climbs out of the crater, holding a glowing shard.")
                        .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC), false);
            }
        }
    }

    private static void confrontTick(ServerLevel world, ServerPlayer p) {
        Archipelago.Island kr = Archipelago.KINGSREST;
        if (!near(p, kr.cx(), kr.cz(), 22) || world.getGameTime() % 200 >= 40) return;
        AABB area = new AABB(kr.cx() - 60, world.getMinY(), kr.cz() - 60, kr.cx() + 60, world.getMaxY(), kr.cz() + 60);
        if (!world.getEntitiesOfClass(Mob.class, area, m -> m.entityTags().contains(PRETENDER)).isEmpty()) return;
        BlockPos throne = new BlockPos(kr.cx(), Builds.groundY(kr.cx(), kr.cz() - 10), kr.cz() - 10);
        Mob boss = spawnBossAt(world, throne, EntityTypes.WITHER_SKELETON, "The Pretender", PRETENDER, 400, ChatFormatting.DARK_PURPLE);
        if (boss != null) {
            net.minecraft.world.phys.Vec3 at = net.minecraft.world.phys.Vec3.atBottomCenterOf(throne).add(0, 1.8, 0);
            Cutscenes.play(p, "The Pretender", java.util.List.of(
                    new Cutscenes.Shot(at.add(0, 2, 14), at.add(0, 1, 6), at, 80, "The throne hall of Kingsrest. Someone is already sitting in the king's chair."),
                    Cutscenes.orbit(at, 4, 0.4, 70, 110, 70, "The Pretender rises, and the dead of the old court rise with him.")));
            boss.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
            boss.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.BROADSWORD));
            boss.setTarget(p);
        }
        for (int i = 0; i < 4; i++) {
            var guard = ModEntities.CURSED_PIRATE.create(world, EntitySpawnReason.EVENT);
            if (guard == null) continue;
            guard.snapTo(throne.getX() + (i % 2 == 0 ? -4 : 4) + 0.5, throne.getY(), throne.getZ() + 3 + i, 0, 0);
            guard.finalizeSpawn(world, world.getCurrentDifficultyAt(throne), EntitySpawnReason.EVENT, null);
            guard.setCustomName(Component.literal("Crown Guard").withStyle(ChatFormatting.DARK_PURPLE));
            guard.setTarget(p);
            world.addFreshEntity(guard);
        }
        p.sendSystemMessage(Component.literal("The Pretender: \"You brought my shards. How thoughtful. Kneel, and I'll let you keep your head.\"")
                .withStyle(ChatFormatting.DARK_PURPLE), false);
        world.playSound(null, throne, com.cursedtides.sound.ModSounds.BELL_TOLL.value(), SoundSource.HOSTILE, 3f, 0.6f);
    }

    private static Mob spawnBoss(ServerLevel world, ServerPlayer p, EntityType<? extends Mob> type, String name,
                                       String tag, float health, ChatFormatting colour) {
        BlockPos at = com.cursedtides.living.Residents.openGround(world, p.getBlockX() + 8, p.getBlockZ() + 8, 10);
        if (at == null) at = p.blockPosition().offset(4, 0, 4);
        return spawnBossAt(world, at, type, name, tag, health, colour);
    }

    private static Mob spawnBossAt(ServerLevel world, BlockPos at, EntityType<? extends Mob> type, String name,
                                         String tag, float health, ChatFormatting colour) {
        Mob boss = type.create(world, EntitySpawnReason.EVENT);
        if (boss == null) return null;
        boss.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0, 0);
        boss.finalizeSpawn(world, world.getCurrentDifficultyAt(at), EntitySpawnReason.EVENT, null);
        boss.setCustomName(Component.literal(name).withStyle(colour, ChatFormatting.BOLD));
        boss.setCustomNameVisible(true);
        boss.addTag(tag);
        boss.addTag("cursedtides_boss");
        boss.setPersistenceRequired();
        var maxHealth = boss.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) maxHealth.setBaseValue(health);
        boss.setHealth(health);
        var armor = boss.getAttribute(Attributes.ARMOR);
        if (armor != null) armor.setBaseValue(10);
        boss.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, -1, 0, false, false));
        world.addFreshEntity(boss);
        return boss;
    }

    static void giveShard(ServerPlayer p, int shard) {
        if ((shards(p) & shard) != 0) return;
        p.setAttached(SHARD_MASK, shards(p) | shard);
        QuestData.give(p, new ItemStack(shard == SALT ? SHARD_SALT : shard == ASH ? SHARD_ASH : SHARD_MIST));
        String name = shard == SALT ? "Salt" : shard == ASH ? "Ash" : "Mist";
        p.sendSystemMessage(Component.literal("Crown shard recovered: the Shard of " + name + " (" + shardCount(p) + "/3)").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), false);
        p.level().playSound(null, p.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1f, 1.2f);
        com.cursedtides.pirate.PirateData.addXp(p, com.cursedtides.pirate.Weapon.EXPLORATION, 120);
        if (shardCount(p) >= 3) {
            setStage(p, SEAT);
            p.sendSystemMessage(Component.literal("All three shards hum together. Odalys Quill will want to see this.").withStyle(ChatFormatting.GRAY), false);
        }
    }
}
