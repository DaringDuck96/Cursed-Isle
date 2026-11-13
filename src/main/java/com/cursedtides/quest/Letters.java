package com.cursedtides.quest;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityTypes;
import com.cursedtides.CursedTides;
import com.cursedtides.combat.Levels;
import com.cursedtides.world.Archipelago;
import com.cursedtides.world.Areas;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Found letters: eight short tales that begin with a letter turned up in plunder (a loot chest, a skull chest, now
 * and then a pouch). Each letter tells you where to go next; there you'll find either something hidden or someone
 * waiting, and with it the next letter, until the tale ends in a reward. The letter you last read is what the radar
 * points at.
 */
public final class Letters {
    private Letters() {}

    /** One leg of a tale: where (an island, and which of its named areas, or -1 for its heart), and what's there. */
    record Leg(Archipelago.Island island, int area, String text, String foe, Supplier<EntityType<?>> foeType) {
        boolean guarded() { return foe != null; }
    }

    record Tale(String title, List<Leg> legs, String ending, java.util.function.Function<ServerPlayer, ItemStack> reward) {}

    private static Leg cache(Archipelago.Island is, int area, String text) { return new Leg(is, area, text, null, null); }
    private static Leg foe(Archipelago.Island is, int area, String text, String foe, Supplier<EntityType<?>> type) {
        return new Leg(is, area, text, foe, type);
    }

    static final List<Tale> TALES = List.of(
            new Tale("The Cooper's Debt", List.of(
                    foe(Archipelago.MIREFEN_BAYOU, 0, "\"To whoever finds this: Silas Crook owes me forty doubloons and a barrel of good rum. He hides in the swamps of Mirefen. Make him pay. — Abner the Cooper\"",
                            "Silas Crook, Smuggler", () -> com.cursedtides.entity.ModEntities.CURSED_PIRATE),
                    cache(Archipelago.SALTMARROW, 0, "Crook's pocket held a second note: \"Abner, the barrel's buried in the woods above Saltmarrow, under the crooked marker. We're square. — S.\"")),
                    "You dig up the barrel. Abner's not around to claim it, so it's yours.",
                    pl -> new ItemStack(com.cursedtides.item.ModItems.DOUBLOON, 40)),
            new Tale("The Gull Queen", List.of(
                    cache(Archipelago.MARINERS_REST, 1, "\"My love, I left the locket where we used to watch the gulls, on Mariner's Rest. Wear it and come to me on the rock. — your Maeve\""),
                    foe(Archipelago.GULLSCREAM_ROCK, 0, "Inside the locket, a lock of grey hair and a scrap: \"Gullscream Rock. I'll be waiting, however long it takes.\"",
                            "Maeve, the Gull Queen's Ghost", () -> EntityTypes.VEX)),
                    "Maeve fades with a sound like gulls going quiet. The locket is warm in your hand.",
                    pl -> new ItemStack(com.cursedtides.appearance.Accessories.random(pl.getRandom(), true))),
            new Tale("The Fourth Shard", List.of(
                    cache(Archipelago.ASHFALL_ISLAND, 1, "\"There was a fourth shard of Thorne's bell, never told of. I sold it to a collector and hid the money on Ashfall. God forgive me. — a bellfounder's apprentice\""),
                    foe(Archipelago.HOLLOWGRAVE_ISLAND, 2, "The money's gone; in its place a receipt: \"One bell-shard, purchased by Morrow, Bellringer of Hollowgrave.\"",
                            "Bellringer Morrow", () -> EntityTypes.WITHER_SKELETON)),
                    "Morrow's shard crumbles to green dust. Whatever it was for, it's done now.",
                    pl -> com.cursedtides.combat.Loot.container(com.cursedtides.combat.Loot.Tier.SKULL, 20)),
            new Tale("The Admiralty Cipher", List.of(
                    foe(Archipelago.IRONWATCH_ISLE, 0, "A Tidewatch dispatch, in cipher, with one line in plain hand: \"Lt. Hale has deserted with the signal books. Last seen in the hills behind Ironwatch.\"",
                            "Lieutenant Corwin Hale, Deserter", () -> EntityTypes.PILLAGER),
                    cache(Archipelago.CROWNWATCH, 0, "Hale's satchel: \"The books are cached in the hills above Crownwatch. The Admiral must never—\" The rest is blood.")),
                    "You recover the signal books. The Tidewatch will be grateful, and pay accordingly.",
                    pl -> new ItemStack(com.cursedtides.item.ModItems.DOUBLOON, 60)),
            new Tale("The Jaguar King", List.of(
                    foe(Archipelago.EMERALD_CAY, 1, "From a naturalist's journal: \"The great spotted cat they call Old Rosette rules the green ridges of Emerald Cay. I must see her. I must.\"",
                            "Old Rosette", () -> com.cursedtides.wildlife.Wildlife.JAGUAR),
                    cache(Archipelago.SERPENTS_HOLLOW, 0, "The last page: \"Rosette's cubs were taken to Serpent's Hollow by collectors. I've left my notes there for whoever comes after.\"")),
                    "You find the naturalist's notes and a fine brass spyglass.",
                    pl -> new ItemStack(net.minecraft.world.item.Items.SPYGLASS)),
            new Tale("A Smuggler's Will", List.of(
                    cache(Archipelago.RAVENS_LANDING, 1, "\"Being of unsound mind and very sound purse, I leave my hoard to whoever outlives my quartermaster. First, find my strongbox on Raven's Landing. — Red Maddox\""),
                    foe(Archipelago.BLOODWAKE_CAY, 1, "The strongbox holds a map to Bloodwake and a warning: \"Quill still guards it. He always did.\"",
                            "Quill, Maddox's Quartermaster", () -> EntityTypes.VINDICATOR)),
                    "Quill falls. Maddox's hoard is yours, as the will says.",
                    pl -> com.cursedtides.combat.Loot.container(com.cursedtides.combat.Loot.Tier.SKULL, 30)),
            new Tale("Salt in the Wound", List.of(
                    cache(Archipelago.SUNKEN_REACH, -1, "A salvage diver's log: \"Something pale follows me on every dive at the Sunken Reach. I've left my lucky charm on the reef. I'm not going back for it.\""),
                    foe(Archipelago.DROWNED_CITY, -1, "Tied to the charm, a scrap of sailcloth: \"It came from the drowned city. It wants me to come home.\"",
                            "The Pale Diver", () -> EntityTypes.DROWNED)),
                    "The Pale Diver sinks for the last time. The charm stops tugging toward the deep.",
                    pl -> new ItemStack(net.minecraft.world.item.Items.HEART_OF_THE_SEA)),
            new Tale("Last Orders", List.of(
                    foe(Archipelago.LAST_HORIZON, 0, "\"To the last of the Meridian Fleet: the gate has opened once and will again. Hold the summit. — Commodore of the Sovereign Dawn, forty years gone\"",
                            "The Last Commodore", () -> EntityTypes.STRAY),
                    cache(Archipelago.KINGSREST, 1, "The Commodore's orders, sealed with wax: \"If we fall, the fleet's pay chest lies in the old kings' gardens at Kingsrest.\"")),
                    "Forty years of a fleet's wages, untouched.",
                    pl -> com.cursedtides.combat.GearRarity.randomWeapon(pl.level(), pl.getRandom(), 5)));

    // ------------------------------------------------------------------ the letter item

    public static class LetterItem extends Item {
        public LetterItem(Properties s) { super(s); }

        @Override
        public InteractionResult use(Level world, Player user, InteractionHand hand) {
            ItemStack stack = user.getItemInHand(hand);
            if (world.isClientSide() || !(user instanceof ServerPlayer p)) return InteractionResult.SUCCESS;
            CompoundTag n = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            int tale = n.getIntOr("Tale", 0), leg = n.getIntOr("Leg", 0);
            if (tale < 0 || tale >= TALES.size()) return InteractionResult.FAIL;
            Tale t = TALES.get(tale);
            Leg l = t.legs().get(Math.min(leg, t.legs().size() - 1));
            p.sendSystemMessage(Component.literal(t.title()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
            p.sendSystemMessage(Component.literal(l.text()).withStyle(ChatFormatting.ITALIC, ChatFormatting.WHITE), false);
            p.sendSystemMessage(Component.literal("(" + where(l) + ". Your radar marks the place.)").withStyle(ChatFormatting.GRAY), false);
            p.setAttached(TRACKED, tale * 10 + leg);
            return InteractionResult.SUCCESS;
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display__, java.util.function.Consumer<Component> adder__, TooltipFlag type) {
        java.util.List<Component> tooltip = new java.util.ArrayList<>();
            CompoundTag n = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            int tale = n.getIntOr("Tale", 0);
            if (tale >= 0 && tale < TALES.size()) tooltip.add(Component.literal(TALES.get(tale).title()).withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.literal("Use to read").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.forEach(adder__);
    }
    }

    public static final Item LETTER = Registry.register(BuiltInRegistries.ITEM, com.cursedtides.util.Reg.itemId("weathered_letter"),
            new LetterItem(com.cursedtides.util.Reg.props().stacksTo(1).rarity(Rarity.UNCOMMON)));

    /** Bit i = tale i finished. */
    public static final AttachmentType<Integer> FINISHED = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT).copyOnDeath().initializer(() -> 0).buildAndRegister(CursedTides.id("letters_finished"));
    /** The letter last read: tale * 10 + leg, or -1. */
    public static final AttachmentType<Integer> TRACKED = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT).copyOnDeath().initializer(() -> -1).buildAndRegister(CursedTides.id("letters_tracked"));

    static ItemStack letter(int tale, int leg) {
        ItemStack s = new ItemStack(LETTER);
        CompoundTag n = new CompoundTag();
        n.putInt("Tale", tale);
        n.putInt("Leg", leg);
        s.set(DataComponents.CUSTOM_DATA, CustomData.of(n));
        s.set(DataComponents.CUSTOM_NAME, Component.literal("Letter: " + TALES.get(tale).title()).withStyle(ChatFormatting.YELLOW));
        return s;
    }

    /** A letter to put in plunder: the first leg of a tale this pirate hasn't finished or isn't already carrying. */
    public static ItemStack forLoot(ServerPlayer p) {
        int done = p.getAttachedOrElse(FINISHED, 0);
        List<Integer> open = new ArrayList<>();
        for (int i = 0; i < TALES.size(); i++) if ((done & (1 << i)) == 0 && !carrying(p, i)) open.add(i);
        if (open.isEmpty()) return ItemStack.EMPTY;
        return letter(open.get(p.getRandom().nextInt(open.size())), 0);
    }

    private static boolean carrying(ServerPlayer p, int tale) {
        var inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(LETTER) && s.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getIntOr("Tale", 0) == tale) return true;
        }
        return false;
    }

    // ------------------------------------------------------------------ places

    static int[] spot(Leg l) {
        Archipelago.Island is = l.island();
        if (l.area() >= 0) {
            int k = 0;
            for (Areas.Area a : Areas.ALL) {
                if (a.island() != is) continue;
                if (k++ == l.area()) return new int[]{a.x() - 9, a.z() + 6};
            }
        }
        return new int[]{is.cx() + is.radius() / 3, is.cz() - is.radius() / 3};
    }

    static String where(Leg l) {
        if (l.area() >= 0) {
            int k = 0;
            for (Areas.Area a : Areas.ALL) {
                if (a.island() != l.island()) continue;
                if (k++ == l.area()) return Character.toUpperCase(a.name().charAt(0)) + a.name().substring(1) + ", " + l.island().name();
            }
        }
        return l.island().name();
    }

    public static int[] target(ServerPlayer p) {
        int tr = p.getAttachedOrElse(TRACKED, -1);
        if (tr < 0 || tr / 10 >= TALES.size() || !carrying(p, tr / 10)) return null;
        Tale t = TALES.get(tr / 10);
        return spot(t.legs().get(Math.min(tr % 10, t.legs().size() - 1)));
    }

    public static String targetLabel(ServerPlayer p) {
        int tr = p.getAttachedOrElse(TRACKED, -1);
        return tr < 0 ? "" : TALES.get(tr / 10).title();
    }

    public static List<String> journal(ServerPlayer p) {
        int done = p.getAttachedOrElse(FINISHED, 0);
        List<String> out = new ArrayList<>();
        out.add("§7Tales finished: " + Integer.bitCount(done) + "/" + TALES.size() + " (letters turn up in plunder)");
        for (int i = 0; i < TALES.size(); i++) {
            if ((done & (1 << i)) != 0) out.add("§a✔ " + TALES.get(i).title());
            else if (carrying(p, i)) out.add("§f• " + TALES.get(i).title() + " §7(read your letter)");
        }
        return out;
    }

    // ------------------------------------------------------------------ progress

    private static final String TAG = "ct_letter:";
    private static final Map<UUID, Long> COOLDOWN = new HashMap<>();

    public static void register() {
        FINISHED.identifier();
        TRACKED.identifier();
        CreativeModeTabEvents.modifyOutputEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB, CursedTides.id("main"))).register(e -> e.accept(LETTER));
        ServerTickEvents.END_SERVER_TICK.register(Letters::tick);
        ServerLivingEntityEvents.AFTER_DEATH.register(Letters::onDeath);
    }

    /** The letter stack in the pack for tale t, or empty. */
    private static ItemStack held(ServerPlayer p, int tale) {
        var inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(LETTER) && s.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getIntOr("Tale", 0) == tale) return s;
        }
        return ItemStack.EMPTY;
    }

    private static void tick(MinecraftServer server) {
        if (server.getTickCount() % 20 != 5) return;
        ServerLevel w = server.overworld();
        for (ServerPlayer p : w.players()) {
            for (int tale = 0; tale < TALES.size(); tale++) {
                ItemStack s = held(p, tale);
                if (s.isEmpty()) continue;
                int leg = s.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getIntOr("Leg", 0);
                Tale t = TALES.get(tale);
                if (leg >= t.legs().size()) continue;
                Leg l = t.legs().get(leg);
                int[] at = spot(l);
                double d2 = p.distanceToSqr(at[0] + 0.5, p.getY(), at[1] + 0.5);
                if (!l.guarded()) {
                    if (d2 < 8 * 8) advance(p, s, tale, leg);
                } else if (d2 < 28 * 28) {
                    final int ft = tale;
                    boolean waiting = !w.getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(64),
                            m -> m.entityTags().contains(TAG + p.getStringUUID() + ":" + ft)).isEmpty();
                    if (!waiting && w.getGameTime() > COOLDOWN.getOrDefault(p.getUUID(), 0L)) {
                        spawnFoe(w, p, l, tale, at);
                        COOLDOWN.put(p.getUUID(), w.getGameTime() + 200);
                    }
                }
            }
        }
    }

    private static void spawnFoe(ServerLevel w, ServerPlayer p, Leg l, int tale, int[] at) {
        BlockPos pos = com.cursedtides.living.Residents.openGround(w, at[0], at[1], 8);
        if (pos == null) pos = new BlockPos(at[0], Archipelago.standY(at[0], at[1]), at[1]);
        Entity e = l.foeType().get().create(w, EntitySpawnReason.EVENT);
        if (!(e instanceof Mob m)) return;
        m.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
        m.finalizeSpawn(w, w.getCurrentDifficultyAt(pos), EntitySpawnReason.EVENT, null);
        m.setCustomName(Component.literal(l.foe()).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        m.setCustomNameVisible(true);
        m.setPersistenceRequired();
        int level = Math.min(50, Levels.band(l.island())[1] + 2);
        Levels.apply(m, level);
        AttributeInstance hp = m.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null) {
            hp.setBaseValue(hp.getBaseValue() * 2.5 * Levels.healthFactor(level));
            m.setHealth(m.getMaxHealth());
        }
        m.addTag(TAG + p.getStringUUID() + ":" + tale);
        m.setTarget(p);
        w.addFreshEntity(m);
        p.sendSystemMessage(Component.literal(l.foe() + " is here.").withStyle(ChatFormatting.RED), true);
    }

    private static void onDeath(LivingEntity victim, DamageSource source) {
        if (!(victim.level() instanceof ServerLevel w)) return;
        for (String tag : victim.entityTags()) {
            if (!tag.startsWith(TAG)) continue;
            String[] parts = tag.substring(TAG.length()).split(":");
            if (!(w.getEntity(UUID.fromString(parts[0])) instanceof ServerPlayer p)) return;
            int tale = Integer.parseInt(parts[1]);
            ItemStack s = held(p, tale);
            if (s.isEmpty()) return;
            int leg = s.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getIntOr("Leg", 0);
            advance(p, s, tale, leg);
            return;
        }
    }

    private static void advance(ServerPlayer p, ItemStack letter, int tale, int leg) {
        Tale t = TALES.get(tale);
        letter.shrink(1);
        p.level().playSound(null, p.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1f, 1f);
        if (leg + 1 < t.legs().size()) {
            ItemStack next = letter(tale, leg + 1);
            QuestData.give(p, next);
            p.setAttached(TRACKED, tale * 10 + leg + 1);
            p.sendSystemMessage(Component.literal(t.title() + ": ").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(t.legs().get(leg + 1).text()).withStyle(ChatFormatting.ITALIC, ChatFormatting.WHITE)), false);
            p.sendSystemMessage(Component.literal("(" + where(t.legs().get(leg + 1)) + ")").withStyle(ChatFormatting.GRAY), false);
            return;
        }
        p.sendSystemMessage(Component.literal(t.title() + ": ").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                .append(Component.literal(t.ending()).withStyle(ChatFormatting.WHITE)), false);
        ItemStack reward = t.reward().apply(p);
        if (reward.isEmpty() || reward.getItem() == net.minecraft.world.item.Items.AIR) reward = new ItemStack(com.cursedtides.item.ModItems.DOUBLOON, 30);
        QuestData.give(p, reward);
        com.cursedtides.pirate.PirateData.addXp(p, com.cursedtides.pirate.Weapon.EXPLORATION, 80);
        int done = p.getAttachedOrElse(FINISHED, 0) | (1 << tale);
        p.setAttached(FINISHED, done);
        p.setAttached(TRACKED, -1);
        com.cursedtides.activity.Achievements.grant(p, "letter_reader");
        if (Integer.bitCount(done) >= TALES.size()) com.cursedtides.activity.Achievements.grant(p, "postmaster");
    }
}
