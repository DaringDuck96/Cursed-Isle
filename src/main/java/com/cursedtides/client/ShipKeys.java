package com.cursedtides.client;

import com.cursedtides.entity.ShipEntity;
import com.cursedtides.ship.ShipControlPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/** Ship controls. All rebindable under Options > Controls > Cursed Tides. */
public final class ShipKeys {
    private ShipKeys() {}

    private static final String CATEGORY = "category.cursedtides.ship";

    public static void registerReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(com.cursedtides.ui.UiPayloads.Journal.ID, (payload, context) ->
                context.client().execute(() -> context.client().setScreen(new JournalScreen(payload.lines()))));
        ClientPlayNetworking.registerGlobalReceiver(com.cursedtides.ui.UiPayloads.Chart.ID, (payload, context) ->
                context.client().execute(() -> context.client().setScreen(new ChartScreen(payload))));
        ClientPlayNetworking.registerGlobalReceiver(com.cursedtides.poker.PokerTables.State.ID, (payload, context) ->
                context.client().execute(() -> {
                    var client = context.client();
                    if (payload.data().getBoolean("Closed")) {
                        if (client.currentScreen instanceof PokerScreen) client.setScreen(null);
                    } else if (client.currentScreen instanceof PokerScreen screen) {
                        screen.update(payload.data());
                    } else {
                        client.setScreen(new PokerScreen(payload.data()));
                    }
                }));
        ClientPlayNetworking.registerGlobalReceiver(com.cursedtides.dialogue.DialoguePayloads.Show.ID, (payload, context) ->
                context.client().execute(() -> {
                    var client = context.client();
                    if (payload.data().isEmpty()) {
                        if (client.currentScreen instanceof DialogueScreen) client.setScreen(null);
                    } else if (client.currentScreen instanceof DialogueScreen open) {
                        open.update(payload.data());
                    } else {
                        client.setScreen(new DialogueScreen(payload.data()));
                    }
                }));
        ClientPlayNetworking.registerGlobalReceiver(com.cursedtides.trade.Markets.Data.ID, (payload, context) ->
                context.client().execute(() -> {
                    var client = context.client();
                    if (client.currentScreen instanceof MarketScreen market) market.update(payload.data());
                    else client.setScreen(new MarketScreen(payload.data()));
                }));
        ClientPlayNetworking.registerGlobalReceiver(com.cursedtides.trade.AuctionHouse.Board.ID, (payload, context) ->
                context.client().execute(() -> {
                    var client = context.client();
                    if (client.currentScreen instanceof AuctionScreen board) board.update(payload.entries());
                    else client.setScreen(new AuctionScreen(payload.entries()));
                }));
        ClientPlayNetworking.registerGlobalReceiver(com.cursedtides.pirate.PerkData.Skills.ID, (payload, context) ->
                context.client().execute(() -> context.client().setScreen(new SkillsScreen(payload))));
        ClientPlayNetworking.registerGlobalReceiver(com.cursedtides.character.CharacterPayloads.Open.ID, (payload, context) ->
                context.client().execute(() -> context.client().setScreen(new CharacterScreen(payload))));
    }

    private static final KeyBinding SAILS_UP = register("key.cursedtides.sails_up", GLFW.GLFW_KEY_R);
    private static final KeyBinding SAILS_DOWN = register("key.cursedtides.sails_down", GLFW.GLFW_KEY_V);
    private static final KeyBinding FIRE_PORT = register("key.cursedtides.fire_port", GLFW.GLFW_KEY_Z);
    private static final KeyBinding FIRE_STARBOARD = register("key.cursedtides.fire_starboard", GLFW.GLFW_KEY_X);
    private static final KeyBinding OPEN_HOLD = register("key.cursedtides.open_hold", GLFW.GLFW_KEY_B);
    private static final KeyBinding CYCLE_AMMO = register("key.cursedtides.cycle_ammo", GLFW.GLFW_KEY_C);
    private static final KeyBinding JOURNAL = register("key.cursedtides.journal", GLFW.GLFW_KEY_J);
    private static final KeyBinding CHART = register("key.cursedtides.chart", GLFW.GLFW_KEY_M);
    private static final KeyBinding DODGE = register("key.cursedtides.dodge", GLFW.GLFW_KEY_LEFT_ALT);
    private static final KeyBinding SKILLS = register("key.cursedtides.skills", GLFW.GLFW_KEY_K);
    private static final KeyBinding BELOW = register("key.cursedtides.below_deck", GLFW.GLFW_KEY_H);
    private static final KeyBinding SWIVEL = register("key.cursedtides.swivel", GLFW.GLFW_KEY_G);
    private static final KeyBinding ANCHOR = register("key.cursedtides.anchor", GLFW.GLFW_KEY_N);
    private static final KeyBinding LOOKS = register("key.cursedtides.looks", GLFW.GLFW_KEY_O);
    private static final KeyBinding ULTIMATE = register("key.cursedtides.ultimate", GLFW.GLFW_KEY_U);
    private static final KeyBinding EMOTES = register("key.cursedtides.emotes", GLFW.GLFW_KEY_P);
    private static final KeyBinding QUICK_CHAT = register("key.cursedtides.quick_chat", GLFW.GLFW_KEY_Y);

    private static KeyBinding register(String id, int key) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(id, InputUtil.Type.KEYSYM, key, CATEGORY));
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(ShipKeys::tick);
    }

    private static void tick(MinecraftClient client) {
        boolean aboard = client.player != null && client.player.getVehicle() instanceof ShipEntity;
        boolean onFoot = client.player != null && !aboard && client.player.getVehicle() == null && client.currentScreen == null;
        // R: sails up aboard; on foot, lock on to a foe (again to cycle, sneak to let go)
        while (SAILS_UP.wasPressed()) {
            if (aboard) ClientPlayNetworking.send(new ShipControlPayload(ShipControlPayload.SAILS_UP));
            else if (onFoot) LockOnClient.cycle(client, client.player.isSneaking());
        }
        LockOnClient.tick(client);
        while (QUICK_CHAT.wasPressed()) {
            if (client.player != null && client.currentScreen == null) client.setScreen(new QuickChatScreen());
        }
        sendOrSkill(SAILS_DOWN, ShipControlPayload.SAILS_DOWN, aboard, onFoot, 3);
        sendOrSkill(FIRE_PORT, ShipControlPayload.FIRE_PORT, aboard, onFoot, 0);
        sendOrSkill(FIRE_STARBOARD, ShipControlPayload.FIRE_STARBOARD, aboard, onFoot, 1);
        while (OPEN_HOLD.wasPressed()) {
            if (aboard && DODGE.isPressed()) ClientPlayNetworking.send(new com.cursedtides.combat.SkillBar.Use(4));
            else if (aboard) ClientPlayNetworking.send(new ShipControlPayload(ShipControlPayload.OPEN_HOLD));
        }
        sendOrSkill(CYCLE_AMMO, ShipControlPayload.CYCLE_AMMO, aboard, onFoot, 2);
        send(SWIVEL, ShipControlPayload.FIRE_SWIVEL, aboard);
        send(ANCHOR, ShipControlPayload.ANCHOR, aboard);
        boolean inWorld = client.player != null && client.currentScreen == null;
        send(JOURNAL, ShipControlPayload.OPEN_JOURNAL, inWorld);
        send(CHART, ShipControlPayload.OPEN_MAP, inWorld);
        send(SKILLS, ShipControlPayload.OPEN_SKILLS, inWorld);
        send(BELOW, ShipControlPayload.BELOW_DECK, inWorld);
        send(ULTIMATE, ShipControlPayload.ULTIMATE, client.player != null);
        while (EMOTES.wasPressed()) {
            if (inWorld) client.setScreen(new EmoteScreen());
        }
        while (LOOKS.wasPressed()) {
            if (inWorld) client.setScreen(new AppearanceScreen(AppearanceClient.code(client.player.getUuid())));
        }
        while (DODGE.wasPressed()) {
            if (inWorld && client.player.getVehicle() == null) {
                float side = client.player.input.movementSideways;
                int action = side > 0.1f ? ShipControlPayload.DODGE_LEFT
                        : side < -0.1f ? ShipControlPayload.DODGE_RIGHT : ShipControlPayload.DODGE_BACK;
                ClientPlayNetworking.send(new ShipControlPayload(action));
            }
        }
    }

    /**
     * Aboard, the ship keys work the ship, and with Alt held they're the sailing skills; on foot, Z X C V fire the
     * skills of the weapon in hand.
     */
    private static void sendOrSkill(KeyBinding key, int action, boolean aboard, boolean onFoot, int slot) {
        while (key.wasPressed()) {
            if (aboard && DODGE.isPressed()) ClientPlayNetworking.send(new com.cursedtides.combat.SkillBar.Use(slot));
            else if (aboard) ClientPlayNetworking.send(new ShipControlPayload(action));
            else if (onFoot) ClientPlayNetworking.send(new com.cursedtides.combat.SkillBar.Use(slot));
        }
    }

    private static void send(KeyBinding key, int action, boolean aboard) {
        while (key.wasPressed()) {
            if (aboard) {
                ClientPlayNetworking.send(new ShipControlPayload(action));
            }
        }
    }
}
