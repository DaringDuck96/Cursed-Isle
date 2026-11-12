package com.cursedtides.activity;

import com.cursedtides.CursedTides;
import com.cursedtides.entity.ShipEntity;
import com.cursedtides.profession.Profession;
import com.cursedtides.profession.ProfessionData;
import com.cursedtides.profession.ProfessionItems;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

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
    public record Open(int kind, int arg) implements CustomPayload {
        public static final Id<Open> ID = new Id<>(CursedTides.id("minigame_open"));
        public static final PacketCodec<RegistryByteBuf, Open> CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, Open::kind, PacketCodecs.VAR_INT, Open::arg, Open::new);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    /** Client to server: how it went. For brewing, arg is the tonic chosen. */
    public record Result(int kind, int arg, int score) implements CustomPayload {
        public static final Id<Result> ID = new Id<>(CursedTides.id("minigame_result"));
        public static final PacketCodec<RegistryByteBuf, Result> CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, Result::kind,
                PacketCodecs.VAR_INT, Result::arg, PacketCodecs.VAR_INT, Result::score, Result::new);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
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
        public KitItem(Settings s, int kind) { super(s); this.kind = kind; }

        @Override
        public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
            ItemStack stack = user.getStackInHand(hand);
            if (!(user instanceof ServerPlayerEntity p)) return TypedActionResult.success(stack, true);
            if (kind == REPAIR) {
                if (!(p.getVehicle() instanceof ShipEntity ship) || ship.isHostile()) {
                    p.sendMessage(Text.literal("Use it aboard a ship that needs patching.").formatted(Formatting.GRAY), true);
                    return TypedActionResult.fail(stack);
                }
                if (ship.getHull() >= ship.maxHull()) {
                    p.sendMessage(Text.literal("She's sound. Nothing to patch.").formatted(Formatting.GRAY), true);
                    return TypedActionResult.fail(stack);
                }
                if (READY.getOrDefault(p.getUuid(), 0L) > p.getServerWorld().getTime()) return TypedActionResult.fail(stack);
            }
            ServerPlayNetworking.send(p, new Open(kind, p.getRandom().nextInt(3)));
            return TypedActionResult.success(stack);
        }

        @Override
        public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
            tooltip.add(Text.literal(kind == BREW ? "Use to brew a tonic: you'll need a glass bottle and the ingredient."
                    : "Use aboard a damaged ship to patch her hull.").formatted(Formatting.GRAY));
        }
    }

    public static final Item BREWERS_KIT = Registry.register(Registries.ITEM, CursedTides.id("brewers_kit"), new KitItem(new Item.Settings().maxCount(1), BREW));
    public static final Item CARPENTERS_MALLET = Registry.register(Registries.ITEM, CursedTides.id("carpenters_mallet"), new KitItem(new Item.Settings().maxCount(1), REPAIR));

    public static void register() {
        PayloadTypeRegistry.playS2C().register(Open.ID, Open.CODEC);
        PayloadTypeRegistry.playC2S().register(Result.ID, Result.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(Result.ID, (payload, ctx) -> ctx.server().execute(() -> result(ctx.player(), payload)));
        ItemGroupEvents.modifyEntriesEvent(RegistryKey.of(RegistryKeys.ITEM_GROUP, CursedTides.id("main"))).register(e -> {
            e.add(BREWERS_KIT);
            e.add(CARPENTERS_MALLET);
        });
    }

    private static void result(ServerPlayerEntity p, Result r) {
        if (r.kind() == BREW) {
            int t = Math.floorMod(r.arg(), TONICS.length);
            var inv = p.getInventory();
            if (inv.count(Items.GLASS_BOTTLE) < 1 || inv.count(ingredient(t)) < 1) {
                p.sendMessage(Text.literal("You need a glass bottle and " + INGREDIENTS[t] + " to brew " + TONICS[t] + ".").formatted(Formatting.RED), false);
                return;
            }
            inv.remove(s -> s.isOf(Items.GLASS_BOTTLE), 1, p.playerScreenHandler.getCraftingInput());
            inv.remove(s -> s.isOf(ingredient(t)), 1, p.playerScreenHandler.getCraftingInput());
            int score = Math.max(0, Math.min(10000, r.score()));
            ItemStack out = new ItemStack(tonic(t));
            int q = score >= 2400 ? 2 : score >= 1400 ? 1 : 0;
            if (q > 0) ProfessionItems.setInt(out, "Quality", q);
            p.giveItemStack(out);
            ProfessionData.addXp(p, Profession.BREWING, 10 + score / 150);
            p.sendMessage(Text.literal("You brew " + (q == 2 ? "a masterwork " : q == 1 ? "a fine " : "a plain ") + TONICS[t] + ".").formatted(Formatting.GOLD), true);
            p.getWorld().playSound(null, p.getBlockPos(), SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.PLAYERS, 1f, 1.2f);
        } else if (r.kind() == REPAIR) {
            if (!(p.getVehicle() instanceof ShipEntity ship) || ship.isHostile()) return;
            long now = p.getServerWorld().getTime();
            if (READY.getOrDefault(p.getUuid(), 0L) > now) return;
            READY.put(p.getUuid(), now + 100);
            if (r.score() <= 0) {
                p.sendMessage(Text.literal("The patch doesn't hold.").formatted(Formatting.GRAY), true);
                return;
            }
            float mend = 6 + com.cursedtides.pirate.PirateData.level(p, com.cursedtides.pirate.Weapon.SAILING) / 3f
                    + ProfessionData.level(p, Profession.LEADERSHIP) / 5f;
            ship.setHull(Math.min(ship.maxHull(), ship.getHull() + mend));
            p.getWorld().playSound(null, p.getBlockPos(), SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.PLAYERS, 1f, 0.9f);
            p.sendMessage(Text.literal("Patched! Hull " + (int) ship.getHull() + "/" + ship.maxHull()).formatted(Formatting.GREEN), true);
            com.cursedtides.pirate.PirateData.addXp(p, com.cursedtides.pirate.Weapon.SAILING, 5);
        }
    }
}
