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

        EntityRendererRegistry.register(ModEntities.CURSED_PIRATE, CursedPirateRenderer::new);
        EntityRendererRegistry.register(ModEntities.DROWNED_CAPTAIN, DrownedCaptainRenderer::new);
        EntityRendererRegistry.register(ModEntities.QUARTERMASTER, QuartermasterRenderer::new);
        EntityRendererRegistry.register(ModEntities.SHIPWRIGHT, ShipwrightRenderer::new);
        EntityRendererRegistry.register(ModEntities.STORY_NPC, StoryNpcRenderer::new);
        EntityRendererRegistry.register(ModEntities.SHIP, ShipRenderer::new);
        EntityRendererRegistry.register(ModEntities.MARINE, MarineRenderer::new);
        EntityRendererRegistry.register(ModEntities.CANNONBALL, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.HEX_BOLT, FlyingItemEntityRenderer::new);

        EntityRendererRegistry.register(ModEntities.CREWMATE, CrewmateRenderer::new);
        EntityRendererRegistry.register(ModEntities.THROWING_KNIFE, FlyingItemEntityRenderer::new);

        // Sailor's garb icons take the cloth's dye colour (the overlay layer stays as drawn)
        net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry.ITEM.register(
                (stack, tintIndex) -> tintIndex > 0 ? -1
                        : 0xFF000000 | net.minecraft.component.type.DyedColorComponent.getColor(stack, 0xB0A890),
                com.cursedtides.item.ModClothing.SAILOR_SET.toArray(new net.minecraft.item.Item[0]));

        ShipKeys.register();
        ShipKeys.registerReceivers();
        AppearanceClient.register();
        net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE.putBlock(
                com.cursedtides.block.ModBlocks.MOON_SEAL, net.minecraft.client.render.RenderLayer.getCutout());
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
}
