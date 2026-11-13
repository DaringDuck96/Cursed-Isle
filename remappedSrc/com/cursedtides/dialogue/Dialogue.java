package com.cursedtides.dialogue;

import com.cursedtides.CursedTides;
import com.cursedtides.character.CharacterData;
import com.cursedtides.entity.QuartermasterEntity;
import com.cursedtides.entity.StoryNpcEntity;
import com.cursedtides.quest.HorizonArc;
import com.cursedtides.quest.StoryDialogue;
import com.cursedtides.world.Archipelago;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Conversations. Every talking NPC has a dialogue file in data/cursedtides/dialogue/: nodes of text
 * (with variants picked at random and overrides by condition) and replies the player clicks. Replies can
 * lead to other nodes, open the NPC's shop or market, or run the character's quest business, whose
 * lines appear in the same window. "common.json" holds shared nodes such as rumours ("common:rumours").
 */
public final class Dialogue {
    private Dialogue() {}

    private static final Map<String, JsonObject> FILES = new HashMap<>();
    private static final Map<UUID, Session> SESSIONS = new HashMap<>();

    /** One open conversation. */
    private static final class Session {
        final int npcId;
        final String file;
        String node = "root";
        final List<JsonObject> shown = new ArrayList<>();
        int choice;
        List<String> captured;

        Session(int npcId, String file) {
            this.npcId = npcId;
            this.file = file;
        }
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(DialoguePayloads.Show.ID, DialoguePayloads.Show.CODEC);
        PayloadTypeRegistry.playC2S().register(DialoguePayloads.Choose.ID, DialoguePayloads.Choose.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(DialoguePayloads.Choose.ID,
                (payload, ctx) -> ctx.server().execute(() -> choose(ctx.player(), payload.index())));
        // Marines and hired hands chat too
        net.fabricmc.fabric.api.event.player.UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClientSide || hand != net.minecraft.world.InteractionHand.MAIN_HAND || player.isShiftKeyDown()) return net.minecraft.world.InteractionResult.PASS;
            String file = entity instanceof com.cursedtides.entity.MarineEntity ? "marine"
                    : entity instanceof com.cursedtides.entity.CrewmateEntity ? "crewmate" : null;
            if (file != null && player instanceof ServerPlayer sp && open(sp, entity, file)) return net.minecraft.world.InteractionResult.SUCCESS;
            return net.minecraft.world.InteractionResult.PASS;
        });
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return CursedTides.id("dialogue");
            }

            @Override
            public void onResourceManagerReload(ResourceManager manager) {
                FILES.clear();
                manager.listResources("dialogue", id -> id.getPath().endsWith(".json")).forEach((id, resource) -> {
                    try (Reader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                        String name = id.getPath().substring("dialogue/".length(), id.getPath().length() - ".json".length());
                        FILES.put(name, JsonParser.parseReader(reader).getAsJsonObject());
                    } catch (Exception e) {
                        CursedTides.LOGGER.error("Bad dialogue file {}: {}", id, e.getMessage());
                    }
                });
                CursedTides.LOGGER.info("Loaded {} dialogue files", FILES.size());
            }
        });
    }

    public static boolean has(String file) {
        return FILES.containsKey(file);
    }

    public static String flagTag(String flag) {
        return "ctd_" + flag;
    }

    // ------------------------------------------------------------------ the handlers' side

    /** Quest code says its lines through here; inside a conversation they go to the window, not the chat. */
    public static boolean capture(ServerPlayer player, String line) {
        Session s = SESSIONS.get(player.getUUID());
        if (s == null || s.captured == null) return false;
        s.captured.add(line);
        return true;
    }

    /** Which reply the player chose for a two- or three-way decision: 0 the plain one, 1 the alternative, 2 the third. */
    public static int choice(ServerPlayer player) {
        Session s = SESSIONS.get(player.getUUID());
        if (s != null && s.captured != null) return s.choice;
        return player.isShiftKeyDown() ? 1 : 0;
    }

    // ------------------------------------------------------------------ opening and showing

    public static boolean open(ServerPlayer player, Entity npc, String file) {
        if (!FILES.containsKey(file)) return false;
        Session s = new Session(npc.getId(), file);
        SESSIONS.put(player.getUUID(), s);
        JsonObject root = FILES.get(file);
        String start = "root";
        if (root.has("start")) {
            for (JsonElement e : root.getAsJsonArray("start")) {
                JsonObject o = e.getAsJsonObject();
                if (Conditions.all(list(o, "if"), player, npc)) {
                    start = o.get("node").getAsString();
                    break;
                }
            }
        }
        player.level().playSound(null, npc.blockPosition(), SoundEvents.VILLAGER_AMBIENT, SoundSource.NEUTRAL, 0.5f, 0.9f
                + player.getRandom().nextFloat() * 0.2f);
        show(player, npc, s, start, null);
        return true;
    }

    private static JsonObject node(String file, String name) {
        String f = file, n = name;
        int colon = name.indexOf(':');
        if (colon > 0) {
            f = name.substring(0, colon);
            n = name.substring(colon + 1);
        }
        JsonObject data = FILES.get(f);
        if (data == null || !data.has("nodes")) return null;
        JsonObject nodes = data.getAsJsonObject("nodes");
        return nodes.has(n) ? nodes.getAsJsonObject(n) : null;
    }

    private static void show(ServerPlayer player, Entity npc, Session s, String nodeName, List<String> override) {
        JsonObject node = node(s.file, nodeName);
        if (node == null) {
            close(player);
            return;
        }
        s.node = nodeName.contains(":") ? nodeName : nodeName;
        List<String> text = override != null && !override.isEmpty() ? override : pickText(node, player, npc);
        s.shown.clear();
        ListTag options = new ListTag();
        JsonArray opts = node.has("options") ? node.getAsJsonArray("options") : new JsonArray();
        // Nodes without their own replies borrow the speaker's main menu
        if (opts.isEmpty() && !nodeName.equals("root")) {
            opts = new JsonArray();
            JsonObject back = new JsonObject();
            back.addProperty("label", "Something else...");
            back.addProperty("goto", "root");
            opts.add(back);
            JsonObject bye = new JsonObject();
            bye.addProperty("label", "Farewell.");
            bye.addProperty("action", "close");
            opts.add(bye);
        }
        for (JsonElement e : opts) {
            JsonObject o = e.getAsJsonObject();
            if (!Conditions.all(list(o, "if"), player, npc)) continue;
            if (o.has("once") && player.getTags().contains(flagTag(o.get("once").getAsString()))) continue;
            s.shown.add(o);
            options.add(StringTag.valueOf(fill(o.get("label").getAsString(), player, npc)));
            if (s.shown.size() >= 6) break;
        }
        if (s.shown.isEmpty()) {
            JsonObject bye = new JsonObject();
            bye.addProperty("label", "Farewell.");
            bye.addProperty("action", "close");
            s.shown.add(bye);
            options.add(StringTag.valueOf("Farewell."));
        }
        CompoundTag data = new CompoundTag();
        data.putInt("Npc", npc.getId());
        data.putString("Name", speaker(s.file, npc));
        ListTag lines = new ListTag();
        for (String t : text) lines.add(StringTag.valueOf(fill(t, player, npc)));
        data.put("Text", lines);
        data.put("Options", options);
        ServerPlayNetworking.send(player, new DialoguePayloads.Show(data));
    }

    private static List<String> pickText(JsonObject node, ServerPlayer player, Entity npc) {
        JsonArray variants = null;
        if (node.has("text_if")) {
            for (JsonElement e : node.getAsJsonArray("text_if")) {
                JsonObject o = e.getAsJsonObject();
                if (Conditions.all(list(o, "if"), player, npc)) {
                    variants = o.getAsJsonArray("text");
                    break;
                }
            }
        }
        if (variants == null && node.has("text")) variants = node.getAsJsonArray("text");
        List<String> out = new ArrayList<>();
        if (variants == null || variants.isEmpty()) return out;
        JsonElement pick = variants.get(player.getRandom().nextInt(variants.size()));
        // A variant is either a string or a list of paragraphs
        if (pick.isJsonArray()) for (JsonElement p : pick.getAsJsonArray()) out.add(p.getAsString());
        else out.add(pick.getAsString());
        // Optional afterword that depends on the moment (weather, time, standing)
        if (node.has("aside")) {
            for (JsonElement e : node.getAsJsonArray("aside")) {
                JsonObject o = e.getAsJsonObject();
                if (Conditions.all(list(o, "if"), player, npc)) {
                    JsonArray t = o.getAsJsonArray("text");
                    out.add(t.get(player.getRandom().nextInt(t.size())).getAsString());
                    break;
                }
            }
        }
        return out;
    }

    private static String speaker(String file, Entity npc) {
        if (npc instanceof StoryNpcEntity story) return story.getRole().displayName();
        if (npc.hasCustomName() && npc.getCustomName() != null) return npc.getCustomName().getString();
        JsonObject data = FILES.get(file);
        return data != null && data.has("name") ? data.get("name").getAsString() : npc.getName().getString();
    }

    private static String fill(String s, ServerPlayer p, Entity npc) {
        if (s.indexOf('{') < 0) return s;
        long time = p.level().getDayTime() % 24000;
        String tod = time < 1000 || time >= 23000 ? "dawn" : time < 6000 ? "morning" : time < 12000 ? "afternoon" : time < 13000 ? "evening" : "night";
        String weather = p.level().isThundering() ? "storm" : p.level().isRaining() ? "rain" : "fair weather";
        int island = Archipelago.islandIndexAt(npc.getBlockX(), npc.getBlockZ());
        String title = HorizonArc.title(p);
        return s.replace("{player}", CharacterData.pirateName(p))
                .replace("{time}", tod)
                .replace("{weather}", weather)
                .replace("{island}", island >= 0 ? Archipelago.ISLANDS.get(island).name() : "these waters")
                .replace("{title}", title != null ? title : "captain")
                .replace("{relics}", String.valueOf(HorizonArc.relicCount(p)))
                .replace("{festival}", com.cursedtides.living.Festivals.currentName())
                .replace("{town_mood}", com.cursedtides.living.Prosperity.mood(p.getServer(), npc.getBlockX(), npc.getBlockZ()))
                .replace("{prosperity}", String.valueOf(com.cursedtides.living.Prosperity.at(p.getServer(), npc.getBlockX(), npc.getBlockZ())))
                .replace("{bounty}", String.valueOf(com.cursedtides.faction.FactionData.bounty(p)))
                .replace("{challenge}", com.cursedtides.activity.Challenges.describeDaily(p))
                .replace("{npc}", npc.hasCustomName() && npc.getCustomName() != null ? npc.getCustomName().getString() : npc.getName().getString());
    }

    private static List<String> list(JsonObject o, String key) {
        if (!o.has(key)) return null;
        List<String> out = new ArrayList<>();
        JsonElement e = o.get(key);
        if (e.isJsonArray()) for (JsonElement x : e.getAsJsonArray()) out.add(x.getAsString());
        else out.add(e.getAsString());
        return out;
    }

    // ------------------------------------------------------------------ choosing

    private static void choose(ServerPlayer player, int index) {
        Session s = SESSIONS.get(player.getUUID());
        if (s == null) return;
        Entity npc = player.level().getEntity(s.npcId);
        if (index < 0 || npc == null || !npc.isAlive() || npc.distanceToSqr(player) > 10 * 10 || index >= s.shown.size()) {
            SESSIONS.remove(player.getUUID());
            if (index >= 0) close(player);
            return;
        }
        JsonObject o = s.shown.get(index);
        if (o.has("set")) player.addTag(flagTag(o.get("set").getAsString()));
        if (o.has("once")) player.addTag(flagTag(o.get("once").getAsString()));
        String action = o.has("action") ? o.get("action").getAsString() : null;
        String then = o.has("then") ? o.get("then").getAsString() : "root";
        if (action == null) {
            show(player, npc, s, o.has("goto") ? o.get("goto").getAsString() : "root", null);
            return;
        }
        switch (action) {
            case "close" -> close(player);
            case "trade" -> {
                close(player);
                if (npc instanceof QuartermasterEntity qm) qm.openShop(player);
            }
            case "market" -> {
                close(player);
                com.cursedtides.trade.Markets.open(player, npc);
            }
            case "quest_screen" -> {
                // Business that opens its own window (the card table, the auction board, the strongbox)
                close(player);
                if (npc instanceof StoryNpcEntity story) StoryDialogue.talk(story, player);
            }
            case "quest", "quest_alt", "quest_third" -> {
                if (!(npc instanceof StoryNpcEntity story)) {
                    close(player);
                    return;
                }
                s.choice = action.equals("quest") ? 0 : action.equals("quest_alt") ? 1 : 2;
                s.captured = new ArrayList<>();
                try {
                    StoryDialogue.talk(story, player);
                } finally {
                    List<String> lines = s.captured;
                    s.captured = null;
                    s.choice = 0;
                    if (SESSIONS.get(player.getUUID()) == s) {
                        if (lines.isEmpty()) {
                            // Nothing said: the business opened its own window (or had nothing to add)
                            close(player);
                        } else {
                            show(player, npc, s, then, lines);
                        }
                    }
                }
            }
            default -> {
                if (!action.startsWith("special:")) {
                    close(player);
                    return;
                }
                List<String> lines = Specials.run(action.substring(8), player, npc);
                if (SESSIONS.get(player.getUUID()) != s) return; // the special opened its own window
                if (lines == null || lines.isEmpty()) close(player);
                else show(player, npc, s, then, lines);
            }
        }
    }

    public static void close(ServerPlayer player) {
        SESSIONS.remove(player.getUUID());
        ServerPlayNetworking.send(player, new DialoguePayloads.Show(new CompoundTag()));
    }
}
