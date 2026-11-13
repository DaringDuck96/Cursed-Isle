package com.cursedtides.activity;

import com.cursedtides.CursedTides;
import com.cursedtides.entity.ShipEntity;
import com.cursedtides.profession.Profession;
import com.cursedtides.profession.ProfessionData;
import com.cursedtides.profession.ProfessionItems;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Two hands-on mini-games. Brewing: a Brewer's Kit opens a match-the-ingredients board; the better you play, the
 * better the tonic. Repair: a Carpenter's Mallet, used aboard a ship in trouble, opens a quick patching game
 * (hammer in time, bail fast, or saw steady) that mends the hull.
 */
public final class MiniGames {
    private MiniGames() {}

    public static final int BREW = 0, REPAIR = 1;

    /** Server to client: start a game. For repair, arg is which patch job. */
    public record Open(int kind, int arg) implements CustomPacketPayload {
        public static final Type<Open> ID = new Type<>(CursedTides.id("minigame_open"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Open> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, Open::kind, ByteBufCodecs.VAR_INT, Open::arg, Open::new);
        @Override public Type<? extends CustomPacketPayload> type() { return ID; }
    }

    /** Client to server: how it went. For brewing, arg is the tonic chosen. */
    public record Result(int kind, int arg, int score) implements CustomPacketPayload {
        public static final Type<Result> ID = new Type<>(CursedTides.id("minigame_result"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Result> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, Result::kind,
                ByteBufCodecs.VAR_INT, Result::arg, ByteBufCodecs.VAR_INT, Result::score, Result::new);
        @Override public Type<? extends CustomPacketPayload> type() { return ID; }
    }

    public static final String[] TONICS = {"Grog", "Sea-Legs Tonic", "Lantern Tonic", "Ironhide Tonic", "Powder-Monkey Tonic", "Voodoo Draught"};
    public static final String[] INGREDIENTS = {"sugar", "kelp", "glowstone dust", "an iron nugget", "gunpowder", "nether wart"};

    private static Item tonic(int i) {
        return switch (i) {
            case 0 -> ProfessionItems.GROG;
            case 1 -> ProfessionItems.SEA_LEGS_TONIC;
            case 2 -> ProfessionItems.LANTERN_TONIC;
            case 3 -> ProfessionItems.IRONHIDE_TONIC;
            case 4 -> ProfessionItems.POWDER_MONKEY_TONIC;
            default -> ProfessionItems.VOODOO_DRAUGHT;
        };
    }

    private static Item ingredient(int i) {
        return switch (i) {
            case 0 -> Items.SUGAR;
            case 1 -> Items.KELP;
            case 2 -> Items.GLOWSTONE_DUST;
            case 3 -> Items.IRON_NUGGET;
            case 4 -> Items.GUNPOWDER;
            default -> Items.NETHER_WART;
        };
    }

    private static final Map<UUID, Long> READY = new HashMap<>();

    public static class KitItem extends Item {
        final int kind;
        public KitItem(Properties s, int kind) { super(s); this.kind = kind; }

        @Override
        public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
            ItemStack stack = user.getItemInHand(hand);
            if (!(user instanceof ServerPlayer p)) return InteractionResultHolder.sidedSuccess(stack, true);
            if (kind == REPAIR) {
                if (!(p.getVehicle() instanceof ShipEntity ship) || ship.isHostile()) {
                    p.displayClientMessage(Component.literal("Use it aboard a ship that needs patching.").withStyle(ChatFormatting.GRAY), true);
                    return InteractionResultHolder.fail(stack);
                }
                if (ship.getHull() >= ship.maxHull()) {
                    p.displayClientMessage(Component.literal("She's sound. Nothing to patch.").withStyle(ChatFormatting.GRAY), true);
                    return InteractionResultHolder.fail(stack);
                }
                if (READY.getOrDefault(p.getUUID(), 0L) > p.serverLevel().getGameTime()) return InteractionResultHolder.fail(stack);
            }
            ServerPlayNetworking.send(p, new Open(kind, p.getRandom().nextInt(3)));
            return InteractionResultHolder.success(stack);
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
            tooltip.add(Component.literal(kind == BREW ? "Use to brew a tonic: you'll need a glass bottle and the ingredient."
                    : "Use aboard a damaged ship to patch her hull.").withStyle(ChatFormatting.GRAY));
        }
    }

    public static final Item BREWERS_KIT = Registry.register(BuiltInRegistries.ITEM, CursedTides.id("brewers_kit"), new KitItem(new Item.Properties().stacksTo(1), BREW));
    public static final Item CARPENTERS_MALLET = Registry.register(BuiltInRegistries.ITEM, CursedTides.id("carpenters_mallet"), new KitItem(new Item.Properties().stacksTo(1), REPAIR));

    public static void register() {
        PayloadTypeRegistry.playS2C().register(Open.ID, Open.CODEC);
        PayloadTypeRegistry.playC2S().register(Result.ID, Result.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Result.ID, (payload, ctx) -> ctx.server().execute(() -> result(ctx.player(), payload)));
        ItemGroupEvents.modifyEntriesEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB, CursedTides.id("main"))).register(e -> {
            e.accept(BREWERS_KIT);
            e.accept(CARPENTERS_MALLET);
        });
    }

    private static void result(ServerPlayer p, Result r) {
        if (r.kind() == BREW) {
            int t = Math.floorMod(r.arg(), TONICS.length);
            var inv = p.getInventory();
            if (inv.countItem(Items.GLASS_BOTTLE) < 1 || inv.countItem(ingredient(t)) < 1) {
                p.displayClientMessage(Component.literal("You need a glass bottle and " + INGREDIENTS[t] + " to brew " + TONICS[t] + ".").withStyle(ChatFormatting.RED), false);
                return;
            }
            inv.clearOrCountMatchingItems(s -> s.is(Items.GLASS_BOTTLE), 1, p.inventoryMenu.getCraftSlots());
            inv.clearOrCountMatchingItems(s -> s.is(ingredient(t)), 1, p.inventoryMenu.getCraftSlots());
            int score = Math.max(0, Math.min(10000, r.score()));
            ItemStack out = new ItemStack(tonic(t));
            int q = score >= 2400 ? 2 : score >= 1400 ? 1 : 0;
            if (q > 0) ProfessionItems.setInt(out, "Quality", q);
            p.addItem(out);
            ProfessionData.addXp(p, Profession.BREWING, 10 + score / 150);
            p.displayClientMessage(Component.literal("You brew " + (q == 2 ? "a masterwork " : q == 1 ? "a fine " : "a plain ") + TONICS[t] + ".").withStyle(ChatFormatting.GOLD), true);
            p.level().playSound(null, p.blockPosition(), SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 1f, 1.2f);
        } else if (r.kind() == REPAIR) {
            if (!(p.getVehicle() instanceof ShipEntity ship) || ship.isHostile()) return;
            long now = p.serverLevel().getGameTime();
            if (READY.getOrDefault(p.getUUID(), 0L) > now) return;
            READY.put(p.getUUID(), now + 100);
            if (r.score() <= 0) {
                p.displayClientMessage(Component.literal("The patch doesn't hold.").withStyle(ChatFormatting.GRAY), true);
                return;
            }
            float mend = 6 + com.cursedtides.pirate.PirateData.level(p, com.cursedtides.pirate.Weapon.SAILING) / 3f
                    + ProfessionData.level(p, Profession.LEADERSHIP) / 5f;
            ship.setHull(Math.min(ship.maxHull(), ship.getHull() + mend));
            p.level().playSound(null, p.blockPosition(), SoundEvents.WOOD_PLACE, SoundSource.PLAYERS, 1f, 0.9f);
            p.displayClientMessage(Component.literal("Patched! Hull " + (int) ship.getHull() + "/" + ship.maxHull()).withStyle(ChatFormatting.GREEN), true);
            com.cursedtides.pirate.PirateData.addXp(p, com.cursedtides.pirate.Weapon.SAILING, 5);
        }
    }
}
