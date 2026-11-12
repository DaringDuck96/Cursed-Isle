package com.cursedtides.client;

import com.cursedtides.CursedTides;
import com.cursedtides.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.entity.WanderingTraderEntityRenderer;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.util.Identifier;

public class CursedTidesClient implements ClientModInitializer {
    private static final Identifier SHIPWRIGHT_TEXTURE = CursedTides.id("textures/entity/shipwright.png");

    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(ShipRenderer.LAYER, ShipModel::getTexturedModelData);
        registerWildlife();

        // Characters, crews and the cursed, drawn with the Mob Workshop's models (see WorkshopModels)
        WorkshopModels.registerLayers();
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
        EntityRendererRegistry.register(ModEntities.CANNONBALL, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.HEX_BOLT, FlyingItemEntityRenderer::new);

        EntityRendererRegistry.register(ModEntities.THROWING_KNIFE, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(com.cursedtides.combat.Grenades.TYPE, FlyingItemEntityRenderer::new);

        // Sailor's garb icons take the cloth's dye colour (the overlay layer stays as drawn)
        net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry.ITEM.register(
                (stack, tintIndex) -> tintIndex > 0 ? -1
                        : 0xFF000000 | net.minecraft.component.type.DyedColorComponent.getColor(stack, 0xB0A890),
                com.cursedtides.item.ModClothing.SAILOR_SET.toArray(new net.minecraft.item.Item[0]));

        ShipKeys.register();
        ShipKeys.registerReceivers();
        AppearanceClient.register();
        MiniGameScreens.register();
        CombatHud.register();
        net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE.putBlock(
                com.cursedtides.block.ModBlocks.MOON_SEAL, net.minecraft.client.render.RenderLayer.getCutout());
        net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE.putBlock(
                com.cursedtides.block.ModBlocks.CAVE_ROPE, net.minecraft.client.render.RenderLayer.getCutout());
    }

    /** The trader model in the shipwright's own work clothes. */
    static class ShipwrightRenderer extends WanderingTraderEntityRenderer {
        ShipwrightRenderer(EntityRendererFactory.Context context) {
            super(context);
        }

        @Override
        public Identifier getTexture(WanderingTraderEntity entity) {
            return SHIPWRIGHT_TEXTURE;
        }
    }

    private static void registerWildlife() {
        var croc = CreatureRenderer.layer("crocodile");
        var shark = CreatureRenderer.layer("shark");
        var jaguar = CreatureRenderer.layer("jaguar");
        var boar = CreatureRenderer.layer("boar");
        var crab = CreatureRenderer.layer("crab");
        var snake = CreatureRenderer.layer("snake");
        var monkey = CreatureRenderer.layer("monkey");
        var tentacle = CreatureRenderer.layer("tentacle");
        EntityModelLayerRegistry.registerModelLayer(tentacle, WildlifeModels::tentacle);
        EntityRendererRegistry.register(com.cursedtides.sea.Leviathan.TENTACLE, ctx -> new CreatureRenderer<>(ctx, tentacle,
                WildlifeModels.TENTACLE_PARTS, 1.2f, 2.2f, e -> "tentacle"));
        EntityModelLayerRegistry.registerModelLayer(croc, WildlifeModels::crocodile);
        EntityModelLayerRegistry.registerModelLayer(shark, WildlifeModels::shark);
        EntityModelLayerRegistry.registerModelLayer(jaguar, WildlifeModels::jaguar);
        EntityModelLayerRegistry.registerModelLayer(boar, WildlifeModels::boar);
        EntityModelLayerRegistry.registerModelLayer(crab, WildlifeModels::crab);
        EntityModelLayerRegistry.registerModelLayer(snake, WildlifeModels::snake);
        EntityModelLayerRegistry.registerModelLayer(monkey, WildlifeModels::monkey);
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
    private static String npcModel(net.minecraft.entity.passive.WanderingTraderEntity e) {
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
