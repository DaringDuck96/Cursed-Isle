package com.cursedtides.quest;

import com.cursedtides.CursedTides;
import com.cursedtides.combat.Levels;
import com.cursedtides.world.Archipelago;
import com.cursedtides.world.Areas;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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

    record Tale(String title, List<Leg> legs, String ending, java.util.function.Function<ServerPlayerEntity, ItemStack> reward) {}

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
                            "Maeve, the Gull Queen's Ghost", () -> EntityType.VEX)),
                    "Maeve fades with a sound like gulls going quiet. The locket is warm in your hand.",
                    pl -> new ItemStack(com.cursedtides.appearance.Accessories.random(pl.getRandom(), true))),
            new Tale("The Fourth Shard", List.of(
                    cache(Archipelago.ASHFALL_ISLAND, 1, "\"There was a fourth shard of Thorne's bell, never told of. I sold it to a collector and hid the money on Ashfall. God forgive me. — a bellfounder's apprentice\""),
                    foe(Archipelago.HOLLOWGRAVE_ISLAND, 2, "The money's gone; in its place a receipt: \"One bell-shard, purchased by Morrow, Bellringer of Hollowgrave.\"",
                            "Bellringer Morrow", () -> EntityType.WITHER_SKELETON)),
                    "Morrow's shard crumbles to green dust. Whatever it was for, it's done now.",
                    pl -> com.cursedtides.combat.Loot.container(com.cursedtides.combat.Loot.Tier.SKULL, 20)),
            new Tale("The Admiralty Cipher", List.of(
                    foe(Archipelago.IRONWATCH_ISLE, 0, "A Tidewatch dispatch, in cipher, with one line in plain hand: \"Lt. Hale has deserted with the signal books. Last seen in the hills behind Ironwatch.\"",
                            "Lieutenant Corwin Hale, Deserter", () -> EntityType.PILLAGER),
                    cache(Archipelago.CROWNWATCH, 0, "Hale's satchel: \"The books are cached in the hills above Crownwatch. The Admiral must never—\" The rest is blood.")),
                    "You recover the signal books. The Tidewatch will be grateful, and pay accordingly.",
                    pl -> new ItemStack(com.cursedtides.item.ModItems.DOUBLOON, 60)),
            new Tale("The Jaguar King", List.of(
                    foe(Archipelago.EMERALD_CAY, 1, "From a naturalist's journal: \"The great spotted cat they call Old Rosette rules the green ridges of Emerald Cay. I must see her. I must.\"",
                            "Old Rosette", () -> com.cursedtides.wildlife.Wildlife.JAGUAR),
                    cache(Archipelago.SERPENTS_HOLLOW, 0, "The last page: \"Rosette's cubs were taken to Serpent's Hollow by collectors. I've left my notes there for whoever comes after.\"")),
                    "You find the naturalist's notes and a fine brass spyglass.",
                    pl -> new ItemStack(net.minecraft.item.Items.SPYGLASS)),
            new Tale("A Smuggler's Will", List.of(
                    cache(Archipelago.RAVENS_LANDING, 1, "\"Being of unsound mind and very sound purse, I leave my hoard to whoever outlives my quartermaster. First, find my strongbox on Raven's Landing. — Red Maddox\""),
                    foe(Archipelago.BLOODWAKE_CAY, 1, "The strongbox holds a map to Bloodwake and a warning: \"Quill still guards it. He always did.\"",
                            "Quill, Maddox's Quartermaster", () -> EntityType.VINDICATOR)),
                    "Quill falls. Maddox's hoard is yours, as the will says.",
                    pl -> com.cursedtides.combat.Loot.container(com.cursedtides.combat.Loot.Tier.SKULL, 30)),
            new Tale("Salt in the Wound", List.of(
                    cache(Archipelago.SUNKEN_REACH, -1, "A salvage diver's log: \"Something pale follows me on every dive at the Sunken Reach. I've left my lucky charm on the reef. I'm not going back for it.\""),
                    foe(Archipelago.DROWNED_CITY, -1, "Tied to the charm, a scrap of sailcloth: \"It came from the drowned city. It wants me to come home.\"",
                            "The Pale Diver", () -> EntityType.DROWNED)),
                    "The Pale Diver sinks for the last time. The charm stops tugging toward the deep.",
                    pl -> new ItemStack(net.minecraft.item.Items.HEART_OF_THE_SEA)),
            new Tale("Last Orders", List.of(
                    foe(Archipelago.LAST_HORIZON, 0, "\"To the last of the Meridian Fleet: the gate has opened once and will again. Hold the summit. — Commodore of the Sovereign Dawn, forty years gone\"",
                            "The Last Commodore", () -> EntityType.STRAY),
                    cache(Archipelago.KINGSREST, 1, "The Commodore's orders, sealed with wax: \"If we fall, the fleet's pay chest lies in the old kings' gardens at Kingsrest.\"")),
                    "Forty years of a fleet's wages, untouched.",
                    pl -> com.cursedtides.combat.GearRarity.randomWeapon(pl.getServerWorld(), pl.getRandom(), 5)));

    // ------------------------------------------------------------------ the letter item

    public static class LetterItem extends Item {
        public LetterItem(Settings s) { super(s); }

        @Override
        public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
            ItemStack stack = user.getStackInHand(hand);
            if (world.isClient || !(user instanceof ServerPlayerEntity p)) return TypedActionResult.success(stack, true);
            NbtCompound n = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
            int tale = n.getInt("Tale"), leg = n.getInt("Leg");
            if (tale < 0 || tale >= TALES.size()) return TypedActionResult.fail(stack);
            Tale t = TALES.get(tale);
            Leg l = t.legs().get(Math.min(leg, t.legs().size() - 1));
            p.sendMessage(Text.literal(t.title()).formatted(Formatting.GOLD, Formatting.BOLD), false);
            p.sendMessage(Text.literal(l.text()).formatted(Formatting.ITALIC, Formatting.WHITE), false);
            p.sendMessage(Text.literal("(" + where(l) + ". Your radar marks the place.)").formatted(Formatting.GRAY), false);
            p.setAttached(TRACKED, tale * 10 + leg);
            return TypedActionResult.success(stack, false);
        }

        @Override
        public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
            NbtCompound n = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
            int tale = n.getInt("Tale");
            if (tale >= 0 && tale < TALES.size()) tooltip.add(Text.literal(TALES.get(tale).title()).formatted(Formatting.GOLD));
            tooltip.add(Text.literal("Use to read").formatted(Formatting.DARK_GRAY));
        }
    }

    public static final Item LETTER = Registry.register(Registries.ITEM, CursedTides.id("weathered_letter"),
            new LetterItem(new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON)));

    /** Bit i = tale i finished. */
    public static final AttachmentType<Integer> FINISHED = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT).copyOnDeath().initializer(() -> 0).buildAndRegister(CursedTides.id("letters_finished"));
    /** The letter last read: tale * 10 + leg, or -1. */
    public static final AttachmentType<Integer> TRACKED = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT).copyOnDeath().initializer(() -> -1).buildAndRegister(CursedTides.id("letters_tracked"));

    static ItemStack letter(int tale, int leg) {
        ItemStack s = new ItemStack(LETTER);
        NbtCompound n = new NbtCompound();
        n.putInt("Tale", tale);
        n.putInt("Leg", leg);
        s.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(n));
        s.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Letter: " + TALES.get(tale).title()).formatted(Formatting.YELLOW));
        return s;
    }

    /** A letter to put in plunder: the first leg of a tale this pirate hasn't finished or isn't already carrying. */
    public static ItemStack forLoot(ServerPlayerEntity p) {
        int done = p.getAttachedOrElse(FINISHED, 0);
        List<Integer> open = new ArrayList<>();
        for (int i = 0; i < TALES.size(); i++) if ((done & (1 << i)) == 0 && !carrying(p, i)) open.add(i);
        if (open.isEmpty()) return ItemStack.EMPTY;
        return letter(open.get(p.getRandom().nextInt(open.size())), 0);
    }

    private static boolean carrying(ServerPlayerEntity p, int tale) {
        var inv = p.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack s = inv.getStack(i);
            if (s.isOf(LETTER) && s.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getInt("Tale") == tale) return true;
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

    public static int[] target(ServerPlayerEntity p) {
        int tr = p.getAttachedOrElse(TRACKED, -1);
        if (tr < 0 || tr / 10 >= TALES.size() || !carrying(p, tr / 10)) return null;
        Tale t = TALES.get(tr / 10);
        return spot(t.legs().get(Math.min(tr % 10, t.legs().size() - 1)));
    }

    public static String targetLabel(ServerPlayerEntity p) {
        int tr = p.getAttachedOrElse(TRACKED, -1);
        return tr < 0 ? "" : TALES.get(tr / 10).title();
    }

    public static List<String> journal(ServerPlayerEntity p) {
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
        ItemGroupEvents.modifyEntriesEvent(RegistryKey.of(RegistryKeys.ITEM_GROUP, CursedTides.id("main"))).register(e -> e.add(LETTER));
        ServerTickEvents.END_SERVER_TICK.register(Letters::tick);
        ServerLivingEntityEvents.AFTER_DEATH.register(Letters::onDeath);
    }

    /** The letter stack in the pack for tale t, or empty. */
    private static ItemStack held(ServerPlayerEntity p, int tale) {
        var inv = p.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack s = inv.getStack(i);
            if (s.isOf(LETTER) && s.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getInt("Tale") == tale) return s;
        }
        return ItemStack.EMPTY;
    }

    private static void tick(MinecraftServer server) {
        if (server.getTicks() % 20 != 5) return;
        ServerWorld w = server.getOverworld();
        for (ServerPlayerEntity p : w.getPlayers()) {
            for (int tale = 0; tale < TALES.size(); tale++) {
                ItemStack s = held(p, tale);
                if (s.isEmpty()) continue;
                int leg = s.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getInt("Leg");
                Tale t = TALES.get(tale);
                if (leg >= t.legs().size()) continue;
                Leg l = t.legs().get(leg);
                int[] at = spot(l);
                double d2 = p.squaredDistanceTo(at[0] + 0.5, p.getY(), at[1] + 0.5);
                if (!l.guarded()) {
                    if (d2 < 8 * 8) advance(p, s, tale, leg);
                } else if (d2 < 28 * 28) {
                    final int ft = tale;
                    boolean waiting = !w.getEntitiesByClass(MobEntity.class, p.getBoundingBox().expand(64),
                            m -> m.getCommandTags().contains(TAG + p.getUuidAsString() + ":" + ft)).isEmpty();
                    if (!waiting && w.getTime() > COOLDOWN.getOrDefault(p.getUuid(), 0L)) {
                        spawnFoe(w, p, l, tale, at);
                        COOLDOWN.put(p.getUuid(), w.getTime() + 200);
                    }
                }
            }
        }
    }

    private static void spawnFoe(ServerWorld w, ServerPlayerEntity p, Leg l, int tale, int[] at) {
        BlockPos pos = com.cursedtides.living.Residents.openGround(w, at[0], at[1], 8);
        if (pos == null) pos = new BlockPos(at[0], Archipelago.standY(at[0], at[1]), at[1]);
        Entity e = l.foeType().get().create(w);
        if (!(e instanceof MobEntity m)) return;
        m.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
        m.initialize(w, w.getLocalDifficulty(pos), SpawnReason.EVENT, null);
        m.setCustomName(Text.literal(l.foe()).formatted(Formatting.DARK_RED, Formatting.BOLD));
        m.setCustomNameVisible(true);
        m.setPersistent();
        int level = Math.min(50, Levels.band(l.island())[1] + 2);
        Levels.apply(m, level);
        EntityAttributeInstance hp = m.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (hp != null) {
            hp.setBaseValue(hp.getBaseValue() * 2.5 * Levels.healthFactor(level));
            m.setHealth(m.getMaxHealth());
        }
        m.addCommandTag(TAG + p.getUuidAsString() + ":" + tale);
        m.setTarget(p);
        w.spawnEntity(m);
        p.sendMessage(Text.literal(l.foe() + " is here.").formatted(Formatting.RED), true);
    }

    private static void onDeath(LivingEntity victim, DamageSource source) {
        if (!(victim.getWorld() instanceof ServerWorld w)) return;
        for (String tag : victim.getCommandTags()) {
            if (!tag.startsWith(TAG)) continue;
            String[] parts = tag.substring(TAG.length()).split(":");
            if (!(w.getEntity(UUID.fromString(parts[0])) instanceof ServerPlayerEntity p)) return;
            int tale = Integer.parseInt(parts[1]);
            ItemStack s = held(p, tale);
            if (s.isEmpty()) return;
            int leg = s.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getInt("Leg");
            advance(p, s, tale, leg);
            return;
        }
    }

    private static void advance(ServerPlayerEntity p, ItemStack letter, int tale, int leg) {
        Tale t = TALES.get(tale);
        letter.decrement(1);
        p.getWorld().playSound(null, p.getBlockPos(), SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.PLAYERS, 1f, 1f);
        if (leg + 1 < t.legs().size()) {
            ItemStack next = letter(tale, leg + 1);
            QuestData.give(p, next);
            p.setAttached(TRACKED, tale * 10 + leg + 1);
            p.sendMessage(Text.literal(t.title() + ": ").formatted(Formatting.GOLD)
                    .append(Text.literal(t.legs().get(leg + 1).text()).formatted(Formatting.ITALIC, Formatting.WHITE)), false);
            p.sendMessage(Text.literal("(" + where(t.legs().get(leg + 1)) + ")").formatted(Formatting.GRAY), false);
            return;
        }
        p.sendMessage(Text.literal(t.title() + ": ").formatted(Formatting.GOLD, Formatting.BOLD)
                .append(Text.literal(t.ending()).formatted(Formatting.WHITE)), false);
        ItemStack reward = t.reward().apply(p);
        if (reward.isEmpty() || reward.getItem() == net.minecraft.item.Items.AIR) reward = new ItemStack(com.cursedtides.item.ModItems.DOUBLOON, 30);
        QuestData.give(p, reward);
        com.cursedtides.pirate.PirateData.addXp(p, com.cursedtides.pirate.Weapon.EXPLORATION, 80);
        int done = p.getAttachedOrElse(FINISHED, 0) | (1 << tale);
        p.setAttached(FINISHED, done);
        p.setAttached(TRACKED, -1);
        com.cursedtides.activity.Achievements.grant(p, "letter_reader");
        if (Integer.bitCount(done) >= TALES.size()) com.cursedtides.activity.Achievements.grant(p, "postmaster");
    }
}
