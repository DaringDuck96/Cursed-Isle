package com.cursedtides.appearance;

import com.cursedtides.CursedTides;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/** Looks, accessories and biographies: stored on the player, shared with everyone who can see them. */
public final class Appearance {
    private Appearance() {}

    public static final AttachmentType<Integer> LOOK = AttachmentRegistry.<Integer>builder()
            .persistent(Codec.INT).copyOnDeath().initializer(() -> 0).buildAndRegister(CursedTides.id("look"));
    public static final AttachmentType<List<String>> WORN = AttachmentRegistry.<List<String>>builder()
            .persistent(Codec.STRING.listOf()).copyOnDeath().initializer(List::of).buildAndRegister(CursedTides.id("accessories"));
    public static final AttachmentType<String> BIO = AttachmentRegistry.<String>builder()
            .persistent(Codec.STRING).copyOnDeath().initializer(() -> "").buildAndRegister(CursedTides.id("bio"));

    // ------------------------------------------------------------------ payloads

    public record Entry(UUID id, int code) {
        public static final PacketCodec<RegistryByteBuf, Entry> CODEC =
                PacketCodec.tuple(Uuids.PACKET_CODEC, Entry::id, PacketCodecs.VAR_INT, Entry::code, Entry::new);
    }

    /** Server to client: these players look like this. */
    public record Sync(List<Entry> entries) implements CustomPayload {
        public static final Id<Sync> ID = new Id<>(CursedTides.id("look_sync"));
        public static final PacketCodec<RegistryByteBuf, Sync> CODEC =
                PacketCodec.tuple(Entry.CODEC.collect(PacketCodecs.toList()), Sync::entries, Sync::new);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    /** Server to client: open the looking glass with this look. */
    public record Open(int code) implements CustomPayload {
        public static final Id<Open> ID = new Id<>(CursedTides.id("look_open"));
        public static final PacketCodec<RegistryByteBuf, Open> CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, Open::code, Open::new);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    /** Client to server: I want to look like this. */
    public record Set(int code) implements CustomPayload {
        public static final Id<Set> ID = new Id<>(CursedTides.id("look_set"));
        public static final PacketCodec<RegistryByteBuf, Set> CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, Set::code, Set::new);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    /** Players who've paid a barber or tattooist and may restyle, until this world time. */
    private static final java.util.Map<UUID, Long> RESTYLE = new java.util.HashMap<>();

    public static java.util.List<String> visitStylist(ServerPlayerEntity p, String what) {
        if (!com.cursedtides.trade.Wallet.pay(p, 50)) return java.util.List.of("Five doubloons for the chair. Come back when you have it.");
        RESTYLE.put(p.getUuid(), p.getServerWorld().getTime() + 6000);
        ServerPlayNetworking.send(p, new Open(code(p)));
        return null;
    }

    public static void register() {
        com.cursedtides.dialogue.Specials.register("barber", (p, npc) -> visitStylist(p, "hair"));
        com.cursedtides.dialogue.Specials.register("tattoo", (p, npc) -> visitStylist(p, "ink"));
        PayloadTypeRegistry.playS2C().register(Sync.ID, Sync.CODEC);
        PayloadTypeRegistry.playS2C().register(Open.ID, Open.CODEC);
        PayloadTypeRegistry.playC2S().register(Set.ID, Set.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Set.ID, (payload, ctx) -> ctx.server().execute(() -> {
            ServerPlayerEntity p = ctx.player();
            boolean firstLook = !Look.enabled(p.getAttachedOrElse(LOOK, 0));
            Long pass = RESTYLE.get(p.getUuid());
            if (!firstLook && payload.code() != 0 && (pass == null || pass < p.getServerWorld().getTime())) {
                p.sendMessage(Text.literal("A new look takes a barber or a tattooist. You'll find them in Saltmarrow and Kingsport.")
                        .formatted(Formatting.GRAY), false);
                broadcast(p);
                return;
            }
            RESTYLE.remove(p.getUuid());
            p.setAttached(LOOK, Look.withAccessories(payload.code(), 0));
            broadcast(p);
            p.sendMessage(Text.literal("You check yourself in the glass. Looking piratical.").formatted(Formatting.GOLD), true);
        }));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity joined = handler.getPlayer();
            com.cursedtides.quest.CrownArc.applyEnding(joined);
            applyAccessories(joined);
            List<Entry> all = new ArrayList<>();
            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) all.add(new Entry(p.getUuid(), code(p)));
            ServerPlayNetworking.send(joined, new Sync(all));
            broadcast(joined);
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldP, newP, alive) -> {
            com.cursedtides.quest.CrownArc.applyEnding(newP);
            applyAccessories(newP);
            broadcast(newP);
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> dispatcher.register(literal("pirate")
                .then(literal("look").executes(ctx -> {
                    ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                    ServerPlayNetworking.send(p, new Open(p.getAttachedOrElse(LOOK, 0)));
                    return 1;
                }))
                .then(literal("accessories").executes(ctx -> {
                    ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                    List<String> worn = worn(p);
                    p.sendMessage(Text.literal("Accessories:").formatted(Formatting.GOLD), false);
                    for (AccessoryItem.Slot s : AccessoryItem.Slot.values()) {
                        String id = worn.get(s.ordinal());
                        Item item = id.isEmpty() ? null : Registries.ITEM.get(Identifier.of(idOf(id)));
                        p.sendMessage(Text.literal("  " + s.name().toLowerCase() + ": ").formatted(Formatting.GRAY)
                                .append(item == null ? Text.literal("-") : item.getName()), false);
                    }
                    p.sendMessage(Text.literal("Take one off with /pirate unequip <slot>.").formatted(Formatting.DARK_GRAY), false);
                    return 1;
                }))
                .then(literal("unequip").then(argument("slot", StringArgumentType.word()).executes(ctx -> {
                    ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                    AccessoryItem.Slot slot;
                    try {
                        slot = AccessoryItem.Slot.valueOf(StringArgumentType.getString(ctx, "slot").toUpperCase());
                    } catch (IllegalArgumentException e) {
                        p.sendMessage(Text.literal("Slots: head, eyes, ear, neck, ring, trinket, gloves, belt, cloak").formatted(Formatting.RED), false);
                        return 0;
                    }
                    takeOff(p, slot);
                    return 1;
                })))
                .then(literal("bio").then(argument("text", StringArgumentType.greedyString()).executes(ctx -> {
                    ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                    String text = StringArgumentType.getString(ctx, "text");
                    p.setAttached(BIO, text.length() > 200 ? text.substring(0, 200) : text);
                    p.sendMessage(Text.literal("Your story is written.").formatted(Formatting.GOLD), false);
                    return 1;
                })))
                .then(literal("whois").then(argument("player", EntityArgumentType.player()).executes(ctx -> {
                    ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                    ServerPlayerEntity t = EntityArgumentType.getPlayer(ctx, "player");
                    describe(p, t);
                    return 1;
                })))));
    }

    // ------------------------------------------------------------------ looks

    /** Worn entries are an item id, with "#quality" on the end for Fine (1) or Masterwork (2) pieces. */
    static String idOf(String entry) {
        int hash = entry.indexOf('#');
        return hash < 0 ? entry : entry.substring(0, hash);
    }

    static int qualityOf(String entry) {
        int hash = entry.indexOf('#');
        try { return hash < 0 ? 0 : Integer.parseInt(entry.substring(hash + 1)); } catch (NumberFormatException e) { return 0; }
    }

    static ItemStack stackOf(String entry) {
        ItemStack stack = new ItemStack(Registries.ITEM.get(Identifier.of(idOf(entry))));
        if (qualityOf(entry) > 0) AccessoryItem.setQuality(stack, qualityOf(entry));
        return stack;
    }

    public static int code(ServerPlayerEntity p) {
        int acc = 0;
        for (String id : worn(p)) {
            if (id.isEmpty()) continue;
            if (Registries.ITEM.get(Identifier.of(idOf(id))) instanceof AccessoryItem a) acc |= a.visual;
        }
        return Look.withAccessories(p.getAttachedOrElse(LOOK, 0), acc);
    }

    public static void broadcast(ServerPlayerEntity p) {
        MinecraftServer server = p.getServer();
        if (server == null) return;
        Sync sync = new Sync(List.of(new Entry(p.getUuid(), code(p))));
        for (ServerPlayerEntity other : server.getPlayerManager().getPlayerList()) ServerPlayNetworking.send(other, sync);
    }

    private static void describe(ServerPlayerEntity viewer, ServerPlayerEntity t) {
        String title = com.cursedtides.quest.HorizonArc.title(t);
        viewer.sendMessage(Text.literal(com.cursedtides.character.CharacterData.pirateName(t)).formatted(Formatting.GOLD, Formatting.BOLD)
                .append(Text.literal(title != null ? ", " + title : "").formatted(Formatting.LIGHT_PURPLE)), false);
        if (com.cursedtides.character.CharacterData.isCreated(t)) {
            viewer.sendMessage(Text.literal(com.cursedtides.character.CharacterData.background(t).title()).formatted(Formatting.GRAY), false);
        }
        viewer.sendMessage(Text.literal("Notoriety " + com.cursedtides.pirate.PirateData.notoriety(t)).formatted(Formatting.GRAY), false);
        String bio = t.getAttachedOrElse(BIO, "");
        viewer.sendMessage(Text.literal(bio.isEmpty() ? "(No story told yet.)" : "\"" + bio + "\"").formatted(Formatting.ITALIC), false);
    }

    // ------------------------------------------------------------------ accessories

    public static List<String> worn(ServerPlayerEntity p) {
        List<String> worn = new ArrayList<>(p.getAttachedOrElse(WORN, List.of()));
        while (worn.size() < AccessoryItem.Slot.values().length) worn.add("");
        return worn;
    }

    /** Sum of a special effect across everything worn ("crit", "explore", "voodoo"). */
    public static double special(ServerPlayerEntity p, String key) {
        double total = 0;
        for (String id : worn(p)) {
            if (id.isEmpty()) continue;
            if (Registries.ITEM.get(Identifier.of(idOf(id))) instanceof AccessoryItem a && key.equals(a.special)) total += a.amount * AccessoryItem.factor(qualityOf(id));
        }
        return total;
    }

    static void wear(ServerPlayerEntity p, ItemStack stack) {
        if (!(stack.getItem() instanceof AccessoryItem item)) return;
        List<String> worn = worn(p);
        String old = worn.get(item.slot.ordinal());
        int q = AccessoryItem.quality(stack);
        worn.set(item.slot.ordinal(), Registries.ITEM.getId(item).toString() + (q > 0 ? "#" + q : ""));
        stack.decrement(1);
        if (!old.isEmpty()) {
            ItemStack back = stackOf(old);
            if (!p.getInventory().insertStack(back)) p.dropItem(back, false);
        }
        p.setAttached(WORN, worn);
        if (worn.stream().noneMatch(String::isEmpty)) com.cursedtides.activity.Achievements.grant(p, "dressed_to_kill");
        applyAccessories(p);
        broadcast(p);
        p.getWorld().playSound(null, p.getBlockPos(), SoundEvents.ITEM_ARMOR_EQUIP_LEATHER.value(), SoundCategory.PLAYERS, 0.8f, 1.2f);
        p.sendMessage(Text.literal("You put on the ").append(item.getName()).append(Text.literal(".")).formatted(Formatting.GOLD), true);
    }

    static void takeOff(ServerPlayerEntity p, AccessoryItem.Slot slot) {
        List<String> worn = worn(p);
        String old = worn.get(slot.ordinal());
        if (old.isEmpty()) {
            p.sendMessage(Text.literal("Nothing worn there.").formatted(Formatting.GRAY), false);
            return;
        }
        worn.set(slot.ordinal(), "");
        p.setAttached(WORN, worn);
        ItemStack back = stackOf(old);
        if (!p.getInventory().insertStack(back)) p.dropItem(back, false);
        applyAccessories(p);
        broadcast(p);
    }

    public static void applyAccessories(ServerPlayerEntity p) {
        List<String> worn = worn(p);
        for (AccessoryItem.Slot s : AccessoryItem.Slot.values()) {
            Identifier modId = CursedTides.id("accessory_" + s.name().toLowerCase());
            // Remove from every attribute this slot could ever have touched
            for (Item i : Accessories.ALL) {
                if (i instanceof AccessoryItem a && a.attribute != null) {
                    EntityAttributeInstance inst = p.getAttributeInstance(a.attribute);
                    if (inst != null) inst.removeModifier(modId);
                }
            }
            String id = worn.get(s.ordinal());
            if (id.isEmpty()) continue;
            if (Registries.ITEM.get(Identifier.of(idOf(id))) instanceof AccessoryItem a && a.attribute != null) {
                EntityAttributeInstance inst = p.getAttributeInstance(a.attribute);
                if (inst != null) inst.addPersistentModifier(new EntityAttributeModifier(modId, a.amount * AccessoryItem.factor(qualityOf(id)),
                        a.amount < 1 && a.amount > 0 && a.special != null && a.special.equals("percent")
                                ? EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE : EntityAttributeModifier.Operation.ADD_VALUE));
            }
        }
        if (p.getHealth() > p.getMaxHealth()) p.setHealth(p.getMaxHealth());
    }
}
