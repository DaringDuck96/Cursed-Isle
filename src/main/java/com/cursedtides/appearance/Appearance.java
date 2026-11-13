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
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

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
        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> CODEC =
                StreamCodec.composite(UUIDUtil.STREAM_CODEC, Entry::id, ByteBufCodecs.VAR_INT, Entry::code, Entry::new);
    }

    /** Server to client: these players look like this. */
    public record Sync(List<Entry> entries) implements CustomPacketPayload {
        public static final Type<Sync> ID = new Type<>(CursedTides.id("look_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Sync> CODEC =
                StreamCodec.composite(Entry.CODEC.apply(ByteBufCodecs.list()), Sync::entries, Sync::new);
        @Override public Type<? extends CustomPacketPayload> type() { return ID; }
    }

    /** Server to client: open the looking glass with this look. */
    public record Open(int code) implements CustomPacketPayload {
        public static final Type<Open> ID = new Type<>(CursedTides.id("look_open"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Open> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, Open::code, Open::new);
        @Override public Type<? extends CustomPacketPayload> type() { return ID; }
    }

    /** Client to server: I want to look like this. */
    public record Set(int code) implements CustomPacketPayload {
        public static final Type<Set> ID = new Type<>(CursedTides.id("look_set"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Set> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, Set::code, Set::new);
        @Override public Type<? extends CustomPacketPayload> type() { return ID; }
    }

    /** Players who've paid a barber or tattooist and may restyle, until this world time. */
    private static final java.util.Map<UUID, Long> RESTYLE = new java.util.HashMap<>();

    public static java.util.List<String> visitStylist(ServerPlayer p, String what) {
        if (!com.cursedtides.trade.Wallet.pay(p, 50)) return java.util.List.of("Five doubloons for the chair. Come back when you have it.");
        RESTYLE.put(p.getUUID(), p.level().getGameTime() + 6000);
        ServerPlayNetworking.send(p, new Open(code(p)));
        return null;
    }

    public static void register() {
        com.cursedtides.dialogue.Specials.register("barber", (p, npc) -> visitStylist(p, "hair"));
        com.cursedtides.dialogue.Specials.register("tattoo", (p, npc) -> visitStylist(p, "ink"));
        PayloadTypeRegistry.clientboundPlay().register(Sync.ID, Sync.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(Open.ID, Open.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(Set.ID, Set.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Set.ID, (payload, ctx) -> ctx.server().execute(() -> {
            ServerPlayer p = ctx.player();
            boolean firstLook = !Look.enabled(p.getAttachedOrElse(LOOK, 0));
            Long pass = RESTYLE.get(p.getUUID());
            if (!firstLook && payload.code() != 0 && (pass == null || pass < p.level().getGameTime())) {
                p.sendSystemMessage(Component.literal("A new look takes a barber or a tattooist. You'll find them in Saltmarrow and Kingsport.")
                        .withStyle(ChatFormatting.GRAY), false);
                broadcast(p);
                return;
            }
            RESTYLE.remove(p.getUUID());
            p.setAttached(LOOK, Look.withAccessories(payload.code(), 0));
            broadcast(p);
            p.sendSystemMessage(Component.literal("You check yourself in the glass. Looking piratical.").withStyle(ChatFormatting.GOLD), true);
        }));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer joined = handler.getPlayer();
            com.cursedtides.quest.CrownArc.applyEnding(joined);
            applyAccessories(joined);
            List<Entry> all = new ArrayList<>();
            for (ServerPlayer p : server.getPlayerList().getPlayers()) all.add(new Entry(p.getUUID(), code(p)));
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
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    ServerPlayNetworking.send(p, new Open(p.getAttachedOrElse(LOOK, 0)));
                    return 1;
                }))
                .then(literal("accessories").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    List<String> worn = worn(p);
                    p.sendSystemMessage(Component.literal("Accessories:").withStyle(ChatFormatting.GOLD), false);
                    for (AccessoryItem.Slot s : AccessoryItem.Slot.values()) {
                        String id = worn.get(s.ordinal());
                        Item item = id.isEmpty() ? null : BuiltInRegistries.ITEM.getValue(Identifier.parse(idOf(id)));
                        p.sendSystemMessage(Component.literal("  " + s.name().toLowerCase() + ": ").withStyle(ChatFormatting.GRAY)
                                .append(item == null ? Component.literal("-") : item.getName(new ItemStack(item))), false);
                    }
                    p.sendSystemMessage(Component.literal("Take one off with /pirate unequip <slot>.").withStyle(ChatFormatting.DARK_GRAY), false);
                    return 1;
                }))
                .then(literal("unequip").then(argument("slot", StringArgumentType.word()).executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    AccessoryItem.Slot slot;
                    try {
                        slot = AccessoryItem.Slot.valueOf(StringArgumentType.getString(ctx, "slot").toUpperCase());
                    } catch (IllegalArgumentException e) {
                        p.sendSystemMessage(Component.literal("Slots: head, eyes, ear, neck, ring, trinket, gloves, belt, cloak").withStyle(ChatFormatting.RED), false);
                        return 0;
                    }
                    takeOff(p, slot);
                    return 1;
                })))
                .then(literal("bio").then(argument("text", StringArgumentType.greedyString()).executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    String text = StringArgumentType.getString(ctx, "text");
                    p.setAttached(BIO, text.length() > 200 ? text.substring(0, 200) : text);
                    p.sendSystemMessage(Component.literal("Your story is written.").withStyle(ChatFormatting.GOLD), false);
                    return 1;
                })))
                .then(literal("whois").then(argument("player", EntityArgument.player()).executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    ServerPlayer t = EntityArgument.getPlayer(ctx, "player");
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
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(idOf(entry))));
        if (qualityOf(entry) > 0) AccessoryItem.setQuality(stack, qualityOf(entry));
        return stack;
    }

    public static int code(ServerPlayer p) {
        int acc = 0;
        for (String id : worn(p)) {
            if (id.isEmpty()) continue;
            if (BuiltInRegistries.ITEM.getValue(Identifier.parse(idOf(id))) instanceof AccessoryItem a) acc |= a.visual;
        }
        return Look.withAccessories(p.getAttachedOrElse(LOOK, 0), acc);
    }

    public static void broadcast(ServerPlayer p) {
        MinecraftServer server = p.level().getServer();
        if (server == null) return;
        Sync sync = new Sync(List.of(new Entry(p.getUUID(), code(p))));
        for (ServerPlayer other : server.getPlayerList().getPlayers()) ServerPlayNetworking.send(other, sync);
    }

    private static void describe(ServerPlayer viewer, ServerPlayer t) {
        String title = com.cursedtides.quest.HorizonArc.title(t);
        viewer.sendSystemMessage(Component.literal(com.cursedtides.character.CharacterData.pirateName(t)).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                .append(Component.literal(title != null ? ", " + title : "").withStyle(ChatFormatting.LIGHT_PURPLE)), false);
        if (com.cursedtides.character.CharacterData.isCreated(t)) {
            viewer.sendSystemMessage(Component.literal(com.cursedtides.character.CharacterData.background(t).title()).withStyle(ChatFormatting.GRAY), false);
        }
        viewer.sendSystemMessage(Component.literal("Notoriety " + com.cursedtides.pirate.PirateData.notoriety(t)).withStyle(ChatFormatting.GRAY), false);
        String bio = t.getAttachedOrElse(BIO, "");
        viewer.sendSystemMessage(Component.literal(bio.isEmpty() ? "(No story told yet.)" : "\"" + bio + "\"").withStyle(ChatFormatting.ITALIC), false);
    }

    // ------------------------------------------------------------------ accessories

    public static List<String> worn(ServerPlayer p) {
        List<String> worn = new ArrayList<>(p.getAttachedOrElse(WORN, List.of()));
        while (worn.size() < AccessoryItem.Slot.values().length) worn.add("");
        return worn;
    }

    /** Sum of a special effect across everything worn ("crit", "explore", "voodoo"). */
    public static double special(ServerPlayer p, String key) {
        double total = 0;
        for (String id : worn(p)) {
            if (id.isEmpty()) continue;
            if (BuiltInRegistries.ITEM.getValue(Identifier.parse(idOf(id))) instanceof AccessoryItem a && key.equals(a.special)) total += a.amount * AccessoryItem.factor(qualityOf(id));
        }
        return total;
    }

    static void wear(ServerPlayer p, ItemStack stack) {
        if (!(stack.getItem() instanceof AccessoryItem item)) return;
        List<String> worn = worn(p);
        String old = worn.get(item.slot.ordinal());
        int q = AccessoryItem.quality(stack);
        worn.set(item.slot.ordinal(), BuiltInRegistries.ITEM.getKey(item).toString() + (q > 0 ? "#" + q : ""));
        stack.shrink(1);
        if (!old.isEmpty()) {
            ItemStack back = stackOf(old);
            if (!p.getInventory().add(back)) p.drop(back, false, net.minecraft.util.Prediction.SERVER_ONLY);
        }
        p.setAttached(WORN, worn);
        if (worn.stream().noneMatch(String::isEmpty)) com.cursedtides.activity.Achievements.grant(p, "dressed_to_kill");
        applyAccessories(p);
        broadcast(p);
        p.level().playSound(null, p.blockPosition(), SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.PLAYERS, 0.8f, 1.2f);
        p.sendSystemMessage(Component.literal("You put on the ").append(item.getName(new ItemStack(item))).append(Component.literal(".")).withStyle(ChatFormatting.GOLD), true);
    }

    static void takeOff(ServerPlayer p, AccessoryItem.Slot slot) {
        List<String> worn = worn(p);
        String old = worn.get(slot.ordinal());
        if (old.isEmpty()) {
            p.sendSystemMessage(Component.literal("Nothing worn there.").withStyle(ChatFormatting.GRAY), false);
            return;
        }
        worn.set(slot.ordinal(), "");
        p.setAttached(WORN, worn);
        ItemStack back = stackOf(old);
        if (!p.getInventory().add(back)) p.drop(back, false, net.minecraft.util.Prediction.SERVER_ONLY);
        applyAccessories(p);
        broadcast(p);
    }

    public static void applyAccessories(ServerPlayer p) {
        List<String> worn = worn(p);
        for (AccessoryItem.Slot s : AccessoryItem.Slot.values()) {
            Identifier modId = CursedTides.id("accessory_" + s.name().toLowerCase());
            // Remove from every attribute this slot could ever have touched
            for (Item i : Accessories.ALL) {
                if (i instanceof AccessoryItem a && a.attribute != null) {
                    AttributeInstance inst = p.getAttribute(a.attribute);
                    if (inst != null) inst.removeModifier(modId);
                }
            }
            String id = worn.get(s.ordinal());
            if (id.isEmpty()) continue;
            if (BuiltInRegistries.ITEM.getValue(Identifier.parse(idOf(id))) instanceof AccessoryItem a && a.attribute != null) {
                AttributeInstance inst = p.getAttribute(a.attribute);
                if (inst != null) inst.addPermanentModifier(new AttributeModifier(modId, a.amount * AccessoryItem.factor(qualityOf(id)),
                        a.amount < 1 && a.amount > 0 && a.special != null && a.special.equals("percent")
                                ? AttributeModifier.Operation.ADD_MULTIPLIED_BASE : AttributeModifier.Operation.ADD_VALUE));
            }
        }
        if (p.getHealth() > p.getMaxHealth()) p.setHealth(p.getMaxHealth());
    }
}
