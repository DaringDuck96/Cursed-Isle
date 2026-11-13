package com.cursedtides.client;

import com.cursedtides.CursedTides;
import com.cursedtides.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.WanderingTraderRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;

public class CursedTidesClient implements ClientModInitializer {
    private static final Identifier SHIPWRIGHT_TEXTURE = CursedTides.id("textures/entity/shipwright.png");

    @Override
    public void onInitializeClient() {
        registerWildlife();

        // Characters, crews and the cursed, drawn with the Mob Workshop's models (see WorkshopModels)
        WorkshopModels.registerLayers();
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(com.cursedtides.social.Nameplates.Markers.ID, (payload, context) ->
                context.client().execute(() -> {
                    WorkshopBipedRenderer.MARKERS.clear();
                    for (int i = 0; i < payload.ids().size(); i++) WorkshopBipedRenderer.MARKERS.put(payload.ids().get(i), payload.kinds().get(i));
                }));
        CutsceneClient.register();
        // A pirate's tip on the loading screens, as the old game had
        net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (!(screen instanceof net.minecraft.client.gui.screens.LevelLoadingScreen
                    || screen instanceof net.minecraft.client.gui.screens.GenericMessageScreen
                    || screen instanceof net.minecraft.client.gui.screens.ProgressScreen
                    || screen instanceof net.minecraft.client.gui.screens.ConnectScreen)) return;
            String tip = com.cursedtides.ui.Tips.random(new java.util.Random());
            net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.afterExtract(screen).register((s, ctx, mx, my, delta) -> {
                var tr = net.minecraft.client.Minecraft.getInstance().font;
                int y = s.height - 40;
                ctx.centeredText(tr, "Pirate Tip", s.width / 2, y, 0xFFFFD24A);
                var lines = tr.split(net.minecraft.network.chat.Component.literal(tip), Math.min(360, s.width - 40));
                for (int i = 0; i < lines.size(); i++) ctx.centeredText(tr, lines.get(i), s.width / 2, y + 12 + i * 10, 0xFFE6DCC8);
            });
        });
        EntityRendererRegistry.register(ModEntities.CURSED_PIRATE, ctx -> BossLooks.rebuild(ctx, new WorkshopBipedRenderer<>(ctx,
                new String[]{"cursed_grunt", "cursed_brute", "cursed_captain"},
                e -> "cursed_" + e.rank().name().toLowerCase(),
                e -> "cursed_" + e.rank().name().toLowerCase() + "_" + (e.kind() == com.cursedtides.entity.CursedPirateEntity.Kind.DECKHAND ? "cursed" : e.kind().name().toLowerCase()), 1.0f)));
        EntityRendererRegistry.register(ModEntities.DROWNED_CAPTAIN, ctx -> new WorkshopBipedRenderer<>(ctx, new String[]{"thorne"}, e -> "thorne", e -> "thorne", 1.25f));
        EntityRendererRegistry.register(ModEntities.QUARTERMASTER, ctx -> WorkshopBipedRenderer.single(ctx, "quartermaster"));
        EntityRendererRegistry.register(ModEntities.SHIPWRIGHT, ctx -> WorkshopBipedRenderer.single(ctx, "shipwright"));
        EntityRendererRegistry.register(ModEntities.STORY_NPC, ctx -> new WorkshopBipedRenderer<>(ctx, new String[]{"bess", "celestine", "dagny", "dealer", "deedes", "fairweather", "finch", "flossie", "galloway", "grista", "harrow", "ibbie", "isadora", "jory", "keeper_wren", "kettle", "lott", "mags", "marsh", "moss", "ned_flint", "old_mariner", "pip", "quill", "rook", "skip", "sorrel", "sounding", "tamsin", "thwaite", "vey", "wayfarer", "ysolde"},
                CursedTidesClient::npcModel, CursedTidesClient::npcModel, 1.0f));
        EntityRendererRegistry.register(ModEntities.MARINE, ctx -> WorkshopBipedRenderer.single(ctx, "marine"));
        EntityRendererRegistry.register(ModEntities.CREWMATE, ctx -> new WorkshopBipedRenderer<>(ctx, new String[]{"crew_gunner", "crew_bosun", "crew_navigator"},
                e -> CREW[Math.floorMod(e.getRole(), 3)], e -> CREW[Math.floorMod(e.getRole(), 3)], 1.0f));
        EntityRendererRegistry.register(ModEntities.SHIP, ShipRenderer::new);
        EntityRendererRegistry.register(ModEntities.CANNONBALL, ThrownItemRenderer::new);
        EntityRendererRegistry.register(ModEntities.HEX_BOLT, ThrownItemRenderer::new);

        EntityRendererRegistry.register(ModEntities.THROWING_KNIFE, ThrownItemRenderer::new);
        EntityRendererRegistry.register(com.cursedtides.combat.Grenades.TYPE, ThrownItemRenderer::new);

        ShipKeys.register();
        ShipKeys.registerReceivers();
        AppearanceClient.register();
        MiniGameScreens.register();
        CombatHud.register();
    }

    private static void registerWildlife() {
        // The island creatures, from the Mob Workshop (see IslandBeasts)
        for (Object[] b : new Object[][]{{com.cursedtides.wildlife.IslandBeasts.GIANT_WASP, "giant_wasp", 0.4f, 1.0f},
                {com.cursedtides.wildlife.IslandBeasts.CAVE_BAT, "cave_bat", 0.4f, 1.0f},
                {com.cursedtides.wildlife.IslandBeasts.DUNE_SCORPION, "dune_scorpion", 0.6f, 1.0f},
                {com.cursedtides.wildlife.IslandBeasts.SNAPJAW, "snapjaw", 0.5f, 1.0f},
                {com.cursedtides.wildlife.IslandBeasts.LIVING_STUMP, "living_stump", 0.6f, 1.0f}}) {
            @SuppressWarnings("unchecked")
            net.minecraft.world.entity.EntityType<net.minecraft.world.entity.monster.Monster> type = (net.minecraft.world.entity.EntityType<net.minecraft.world.entity.monster.Monster>) b[0];
            String id = (String) b[1];
            float shadow = (Float) b[2], scale = (Float) b[3];
            EntityRendererRegistry.register(type, ctx -> new CreatureRenderer<>(ctx, WorkshopModels.layer(id), WorkshopModels.parts(id), shadow, scale, e -> id, "workshop"));
        }
        var croc = CreatureRenderer.layer("crocodile");
        var shark = CreatureRenderer.layer("shark");
        var jaguar = CreatureRenderer.layer("jaguar");
        var boar = CreatureRenderer.layer("boar");
        var crab = CreatureRenderer.layer("crab");
        var snake = CreatureRenderer.layer("snake");
        var monkey = CreatureRenderer.layer("monkey");
        var tentacle = CreatureRenderer.layer("tentacle");
        ModelLayerRegistry.registerModelLayer(tentacle, WildlifeModels::tentacle);
        EntityRendererRegistry.register(com.cursedtides.sea.Leviathan.TENTACLE, ctx -> new CreatureRenderer<>(ctx, tentacle,
                WildlifeModels.TENTACLE_PARTS, 1.2f, 2.2f, e -> "tentacle"));
        ModelLayerRegistry.registerModelLayer(croc, WildlifeModels::crocodile);
        ModelLayerRegistry.registerModelLayer(shark, WildlifeModels::shark);
        ModelLayerRegistry.registerModelLayer(jaguar, WildlifeModels::jaguar);
        ModelLayerRegistry.registerModelLayer(boar, WildlifeModels::boar);
        ModelLayerRegistry.registerModelLayer(crab, WildlifeModels::crab);
        ModelLayerRegistry.registerModelLayer(snake, WildlifeModels::snake);
        ModelLayerRegistry.registerModelLayer(monkey, WildlifeModels::monkey);
        EntityRendererRegistry.register(com.cursedtides.wildlife.Wildlife.CROCODILE, ctx -> new CreatureRenderer<>(ctx, croc,
                WildlifeModels.CROCODILE_PARTS, 0.8f, 1.0f, e -> e.isAlligator() ? "alligator" : "crocodile"));
        EntityRendererRegistry.register(com.cursedtides.wildlife.Wildlife.SHARK, ctx -> new CreatureRenderer<>(ctx, shark,
                WildlifeModels.SHARK_PARTS, 0.7f, 1.0f, e -> "shark"));
        EntityRendererRegistry.register(com.cursedtides.wildlife.Wildlife.JAGUAR, ctx -> new CreatureRenderer<>(ctx, jaguar,
                WildlifeModels.JAGUAR_PARTS, 0.5f, 1.0f, e -> "jaguar"));
        EntityRendererRegistry.register(com.cursedtides.wildlife.Wildlife.BOAR, ctx -> new CreatureRenderer<>(ctx, boar,
                WildlifeModels.BOAR_PARTS, 0.6f, 1.0f, e -> "boar"));
        EntityRendererRegistry.register(com.cursedtides.wildlife.Wildlife.CRAB, ctx -> new CreatureRenderer<>(ctx, crab,
                WildlifeModels.CRAB_PARTS, 0.35f, 1.0f, e -> "crab"));
        EntityRendererRegistry.register(com.cursedtides.wildlife.Wildlife.SNAKE, ctx -> new CreatureRenderer<>(ctx, snake,
                WildlifeModels.SNAKE_PARTS, 0.3f, 1.0f, e -> "snake"));
        EntityRendererRegistry.register(com.cursedtides.wildlife.Wildlife.MONKEY, ctx -> new CreatureRenderer<>(ctx, monkey,
                WildlifeModels.MONKEY_PARTS, 0.35f, 1.0f, e -> "monkey"));
    }

    private static final String[] CREW = {"crew_gunner", "crew_bosun", "crew_navigator"};

    /** Which workshop model (and texture) each story character wears. */
    private static String npcModel(WanderingTrader e) {
        if (!(e instanceof com.cursedtides.entity.StoryNpcEntity npc)) return "keeper_wren";
        return switch (npc.getRole()) {
            case KEEPER -> "keeper_wren";
            case HARBORMASTER -> "ysolde";
            case BELLWRIGHT -> "tamsin";
            case COMMODORE -> "vey";
            case BROKER -> "mags";
            case GAMBLER -> "jory";
            case GOVERNOR -> "harrow";
            case ROOK -> "rook";
            case NATURALIST -> "ibbie";
            case MEDIUM -> "sorrel";
            case FOREMAN -> "dagny";
            case DEALER -> "dealer";
            case BANKER -> "fairweather";
            case ABBESS -> "celestine";
            case ADMIRAL -> "galloway";
            case RACEMASTER -> "pip";
            case WAYFARER -> "wayfarer";
            case SMUGGLER_QUEEN -> "isadora";
            case AUCTIONEER -> "lott";
            case CONTEST_JUDGE -> "finch";
            case SCHOLAR -> "thwaite";
            case OLD_MARINER -> "old_mariner";
            case ARENA_MASTER -> "grista";
            case RANGE_MASTER -> "ned_flint";
            case TAVERN_KEEPER -> "bess";
            case COURSE_MASTER -> "skip";
            case LAND_AGENT -> "deedes";
            case BOUNTY_CLERK -> "marsh";
            case ARCHIVIST -> "quill";
            case DIVER -> "sounding";
            case GUNNERY_SERGEANT -> "kettle";
            case BARBER -> "flossie";
            case TATTOOIST -> "moss";
        };
    }
}
