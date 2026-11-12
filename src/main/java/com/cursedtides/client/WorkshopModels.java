package com.cursedtides.client;

import net.minecraft.client.model.*;

/**
 * Models from the Cursed Tides Mob Workshop (its starting designs), generated; textures are in
 * textures/entity/workshop/. Regenerate rather than editing by hand.
 */
public final class WorkshopModels {
    private WorkshopModels() {}

    public static net.minecraft.client.render.entity.model.EntityModelLayer layer(String id) {
        return new net.minecraft.client.render.entity.model.EntityModelLayer(com.cursedtides.CursedTides.id("workshop_" + id), "main");
    }

    /** Part paths of the creature-shaped bosses, for CreatureModel; null for people. */
    public static String[] parts(String id) {
        return switch (id) {
            case "abyssal" -> new String[]{"head", "head/jaw", "seg1", "seg1/seg2", "seg1/seg2/seg3", "seg1/seg2/seg3/seg4", "seg1/seg2/seg3/seg4/seg5", "seg1/seg2/seg3/seg4/seg5/seg6"};
            case "old_gnasher" -> new String[]{"body", "body/head", "body/head/jaw", "leg_fl", "leg_fr", "leg_bl", "leg_br", "body/tail1", "body/tail1/tail2", "body/tail1/tail2/tail3"};
            case "slag_beast" -> new String[]{"body", "head", "leg_fl", "leg_fr", "leg_bl", "leg_br", "body/tail1"};
            case "reach_serpent" -> new String[]{"head", "head/jaw", "seg1", "seg1/seg2", "seg1/seg2/seg3", "seg1/seg2/seg3/seg4", "seg1/seg2/seg3/seg4/seg5", "seg1/seg2/seg3/seg4/seg5/seg6"};
            case "giant_wasp" -> new String[]{"body", "body/head", "body/tail1", "body/tail1/tail2", "body/wing_l", "body/wing_r", "body/leg_fl", "body/leg_fr", "body/leg_bl", "body/leg_br"};
            case "cave_bat" -> new String[]{"body", "body/head", "body/wing_l", "body/wing_r", "body/leg_fl", "body/leg_fr"};
            case "dune_scorpion" -> new String[]{"body", "body/head", "claw_l", "claw_r", "leg_fl", "leg_ml", "leg_bl", "leg_fr", "leg_mr", "leg_br", "body/tail1", "body/tail1/tail2", "body/tail1/tail2/tail3"};
            case "snapjaw" -> new String[]{"seg1", "seg1/head", "seg1/head/jaw", "fin_l", "fin_r"};
            case "living_stump" -> new String[]{"body", "arm_l", "arm_r", "leg_fl", "leg_fr"};
            default -> null;
        };
    }

    public static void registerLayers() {
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("keeper_wren"), WorkshopModels::keeperWren);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("ysolde"), WorkshopModels::ysolde);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("tamsin"), WorkshopModels::tamsin);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("vey"), WorkshopModels::vey);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("galloway"), WorkshopModels::galloway);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("harrow"), WorkshopModels::harrow);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("rook"), WorkshopModels::rook);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("thwaite"), WorkshopModels::thwaite);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("wayfarer"), WorkshopModels::wayfarer);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("quill"), WorkshopModels::quill);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("isadora"), WorkshopModels::isadora);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("celestine"), WorkshopModels::celestine);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("sorrel"), WorkshopModels::sorrel);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("ibbie"), WorkshopModels::ibbie);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("dagny"), WorkshopModels::dagny);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("old_mariner"), WorkshopModels::oldMariner);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("mags"), WorkshopModels::mags);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("jory"), WorkshopModels::jory);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("dealer"), WorkshopModels::dealer);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("fairweather"), WorkshopModels::fairweather);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("pip"), WorkshopModels::pip);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("lott"), WorkshopModels::lott);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("finch"), WorkshopModels::finch);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("grista"), WorkshopModels::grista);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("ned_flint"), WorkshopModels::nedFlint);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("bess"), WorkshopModels::bess);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("skip"), WorkshopModels::skip);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("deedes"), WorkshopModels::deedes);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("marsh"), WorkshopModels::marsh);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("sounding"), WorkshopModels::sounding);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("kettle"), WorkshopModels::kettle);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("flossie"), WorkshopModels::flossie);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("moss"), WorkshopModels::moss);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("quartermaster"), WorkshopModels::quartermaster);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("shipwright"), WorkshopModels::shipwright);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("marine"), WorkshopModels::marine);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("crew_gunner"), WorkshopModels::crewGunner);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("crew_bosun"), WorkshopModels::crewBosun);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("crew_navigator"), WorkshopModels::crewNavigator);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("bounty_hunter"), WorkshopModels::bountyHunter);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("thorne"), WorkshopModels::thorne);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("cursed_grunt"), WorkshopModels::cursedGrunt);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("cursed_brute"), WorkshopModels::cursedBrute);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("cursed_captain"), WorkshopModels::cursedCaptain);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("tide_sovereign"), WorkshopModels::tideSovereign);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("lampwrights_hand"), WorkshopModels::lampwrightsHand);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("cinder_king"), WorkshopModels::cinderKing);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("veil_witch"), WorkshopModels::veilWitch);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("red_maddox"), WorkshopModels::redMaddox);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("ashen_herald"), WorkshopModels::ashenHerald);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("pretender"), WorkshopModels::pretender);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("abyssal"), WorkshopModels::abyssal);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("brannoch"), WorkshopModels::brannoch);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("grey_lamplighter"), WorkshopModels::greyLamplighter);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("mother_of_gulls"), WorkshopModels::motherOfGulls);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("bone_abbot"), WorkshopModels::boneAbbot);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("cinder_matron"), WorkshopModels::cinderMatron);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("old_gnasher"), WorkshopModels::oldGnasher);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("grubb"), WorkshopModels::grubb);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("mother_brine"), WorkshopModels::motherBrine);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("slag_beast"), WorkshopModels::slagBeast);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("lantern_warden"), WorkshopModels::lanternWarden);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("corvin_hale"), WorkshopModels::corvinHale);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("captain_saltbones"), WorkshopModels::captainSaltbones);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("barnacle_bride"), WorkshopModels::barnacleBride);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("hangman"), WorkshopModels::hangman);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("witchlight_hag"), WorkshopModels::witchlightHag);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("signal_keeper"), WorkshopModels::signalKeeper);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("reach_serpent"), WorkshopModels::reachSerpent);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("siren"), WorkshopModels::siren);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("giant_wasp"), WorkshopModels::giantWasp);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("cave_bat"), WorkshopModels::caveBat);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("dune_scorpion"), WorkshopModels::duneScorpion);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("snapjaw"), WorkshopModels::snapjaw);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("living_stump"), WorkshopModels::livingStump);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("ship_sloop"), WorkshopModels::shipSloop);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("ship_schooner"), WorkshopModels::shipSchooner);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("ship_brigantine"), WorkshopModels::shipBrigantine);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("ship_brig"), WorkshopModels::shipBrig);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("ship_frigate"), WorkshopModels::shipFrigate);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("ship_galleon"), WorkshopModels::shipGalleon);
        net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry.registerModelLayer(layer("ship_ship_of_the_line"), WorkshopModels::shipShipOfTheLine);
    }

    public static TexturedModelData keeperWren() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(28, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(12, 33).cuboid(-4.3f, -8.3f, 0.5f, 8.6f, 10.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(0, 17).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 0).cuboid(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).uv(38, 33).cuboid(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(24, 17).cuboid(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(36, 17).cuboid(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(48, 17).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(0, 33).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData ysolde() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(28, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 48).cuboid(-6.0f, -9.0f, -6.0f, 12.0f, 1.0f, 12.0f).uv(0, 61).cuboid(-4.5f, -11.5f, -4.5f, 9.0f, 3.0f, 9.0f).uv(0, 73).cuboid(-5.5f, -10.5f, -6.5f, 11.0f, 2.0f, 1.0f).uv(12, 33).cuboid(-4.3f, -8.3f, 0.5f, 8.6f, 10.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(0, 17).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 0).cuboid(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).uv(36, 61).cuboid(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(24, 17).cuboid(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(36, 17).cuboid(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(48, 17).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(0, 33).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 128);
    }

    public static TexturedModelData tamsin() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 31).cuboid(-4.5f, -8.6f, -4.5f, 9.0f, 3.0f, 9.0f).uv(36, 31).cuboid(-1.0f, -6.5f, 4.4f, 2.0f, 3.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(32, 0).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(48, 16).cuboid(-3.5f, 3.0f, -2.6f, 7.0f, 13.0f, 0.6f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(0, 16).cuboid(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(12, 16).cuboid(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(24, 16).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(36, 16).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData vey() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(28, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f).uv(0, 67).cuboid(-4.0f, -2.0f, -4.6f, 8.0f, 3.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 59).cuboid(-7.0f, -12.0f, -2.0f, 14.0f, 4.0f, 4.0f).uv(0, 49).cuboid(-4.5f, -9.0f, -4.5f, 9.0f, 1.0f, 9.0f).uv(18, 67).cuboid(-1.0f, -13.0f, -2.5f, 2.0f, 2.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(0, 17).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 0).cuboid(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).uv(36, 59).cuboid(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(24, 17).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(40, 17).cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(0, 33).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(16, 33).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 128);
    }

    public static TexturedModelData galloway() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(28, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 48).cuboid(-7.0f, -12.0f, -2.0f, 14.0f, 4.0f, 4.0f).uv(12, 33).cuboid(-4.5f, -9.0f, -4.5f, 9.0f, 1.0f, 9.0f).uv(58, 48).cuboid(-1.0f, -13.0f, -2.5f, 2.0f, 2.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(0, 17).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 0).cuboid(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).uv(36, 48).cuboid(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(24, 17).cuboid(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(36, 17).cuboid(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(48, 17).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(0, 33).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData harrow() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 18).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 49).cuboid(-4.3f, -8.3f, 0.5f, 8.6f, 10.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(32, 18).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 0).cuboid(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).uv(32, 0).cuboid(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(0, 34).cuboid(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(12, 34).cuboid(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(24, 34).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(36, 34).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData rook() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(28, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f).uv(0, 74).cuboid(-4.0f, -2.0f, -4.6f, 8.0f, 3.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 49).cuboid(-6.0f, -9.0f, -6.0f, 12.0f, 1.0f, 12.0f).uv(0, 62).cuboid(-4.5f, -11.5f, -4.5f, 9.0f, 3.0f, 9.0f).uv(18, 74).cuboid(-5.5f, -10.5f, -6.5f, 11.0f, 2.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(0, 17).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 0).cuboid(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).uv(36, 62).cuboid(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(24, 17).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(40, 17).cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(0, 33).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(16, 33).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 128);
    }

    public static TexturedModelData thwaite() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(28, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f).uv(0, 49).cuboid(-4.0f, -2.0f, -4.6f, 8.0f, 3.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("body", ModelPartBuilder.create().uv(0, 17).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 0).cuboid(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).uv(32, 33).cuboid(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("right_arm", ModelPartBuilder.create().uv(24, 17).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p3 = root.addChild("left_arm", ModelPartBuilder.create().uv(40, 17).cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("right_leg", ModelPartBuilder.create().uv(0, 33).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p5 = root.addChild("left_leg", ModelPartBuilder.create().uv(16, 33).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("hat", ModelPartBuilder.create(), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData wayfarer() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 38).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 0).cuboid(-4.6f, -8.6f, -4.6f, 9.2f, 9.2f, 9.2f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(32, 38).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 20).cuboid(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).uv(32, 20).cuboid(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(0, 54).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(16, 54).cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(32, 54).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(48, 54).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 128);
    }

    public static TexturedModelData quill() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 18).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 49).cuboid(-4.3f, -8.3f, 0.5f, 8.6f, 10.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(32, 18).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 0).cuboid(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).uv(32, 0).cuboid(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(0, 34).cuboid(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(12, 34).cuboid(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(24, 34).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(36, 34).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData isadora() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(28, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 48).cuboid(-6.0f, -9.0f, -6.0f, 12.0f, 1.0f, 12.0f).uv(0, 61).cuboid(-4.5f, -11.5f, -4.5f, 9.0f, 3.0f, 9.0f).uv(0, 73).cuboid(-5.5f, -10.5f, -6.5f, 11.0f, 2.0f, 1.0f).uv(12, 33).cuboid(-4.3f, -8.3f, 0.5f, 8.6f, 10.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(0, 17).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 0).cuboid(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).uv(36, 61).cuboid(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(24, 17).cuboid(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(36, 17).cuboid(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(48, 17).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(0, 33).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 128);
    }

    public static TexturedModelData celestine() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 38).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 0).cuboid(-4.6f, -8.6f, -4.6f, 9.2f, 9.2f, 9.2f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(32, 38).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 20).cuboid(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).uv(32, 20).cuboid(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(0, 54).cuboid(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(12, 54).cuboid(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(24, 54).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(36, 54).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 128);
    }

    public static TexturedModelData sorrel() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 38).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 0).cuboid(-4.6f, -8.6f, -4.6f, 9.2f, 9.2f, 9.2f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(32, 38).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 20).cuboid(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).uv(32, 20).cuboid(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(0, 54).cuboid(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(12, 54).cuboid(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(24, 54).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(36, 54).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 128);
    }

    public static TexturedModelData ibbie() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(28, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(12, 33).cuboid(-4.5f, -9.5f, -4.5f, 9.0f, 2.0f, 9.0f).uv(22, 48).cuboid(-4.5f, -8.0f, -6.5f, 9.0f, 1.0f, 3.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(0, 17).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 0).cuboid(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).uv(0, 48).cuboid(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(24, 17).cuboid(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(36, 17).cuboid(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(48, 17).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(0, 33).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData dagny() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 31).cuboid(-4.5f, -9.5f, -4.5f, 9.0f, 2.0f, 9.0f).uv(36, 31).cuboid(-4.5f, -8.0f, -6.5f, 9.0f, 1.0f, 3.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(32, 0).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(48, 16).cuboid(-3.5f, 3.0f, -2.6f, 7.0f, 13.0f, 0.6f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(0, 16).cuboid(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(12, 16).cuboid(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(24, 16).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(36, 16).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData oldMariner() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f).uv(36, 32).cuboid(-4.0f, -2.0f, -4.6f, 8.0f, 3.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 32).cuboid(-4.5f, -8.6f, -4.5f, 9.0f, 3.0f, 9.0f).uv(54, 32).cuboid(-1.0f, -6.5f, 4.4f, 2.0f, 3.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(32, 0).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(0, 16).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(16, 16).cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(32, 16).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(48, 16).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData mags() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 31).cuboid(-4.5f, -8.6f, -4.5f, 9.0f, 3.0f, 9.0f).uv(36, 31).cuboid(-1.0f, -6.5f, 4.4f, 2.0f, 3.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(32, 0).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(0, 16).cuboid(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(12, 16).cuboid(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(24, 16).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(36, 16).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData jory() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(28, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("body", ModelPartBuilder.create().uv(0, 17).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 0).cuboid(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).uv(32, 33).cuboid(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("right_arm", ModelPartBuilder.create().uv(24, 17).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p3 = root.addChild("left_arm", ModelPartBuilder.create().uv(40, 17).cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("right_leg", ModelPartBuilder.create().uv(0, 33).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p5 = root.addChild("left_leg", ModelPartBuilder.create().uv(16, 33).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("hat", ModelPartBuilder.create(), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData dealer() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(28, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("body", ModelPartBuilder.create().uv(0, 17).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 0).cuboid(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).uv(32, 33).cuboid(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("right_arm", ModelPartBuilder.create().uv(24, 17).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p3 = root.addChild("left_arm", ModelPartBuilder.create().uv(40, 17).cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("right_leg", ModelPartBuilder.create().uv(0, 33).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p5 = root.addChild("left_leg", ModelPartBuilder.create().uv(16, 33).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("hat", ModelPartBuilder.create(), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData fairweather() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(28, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f).uv(24, 60).cuboid(-4.0f, -2.0f, -4.6f, 8.0f, 3.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 49).cuboid(-4.5f, -9.5f, -4.5f, 9.0f, 2.0f, 9.0f).uv(0, 60).cuboid(-4.5f, -8.0f, -6.5f, 9.0f, 1.0f, 3.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(0, 17).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 0).cuboid(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).uv(36, 49).cuboid(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(24, 17).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(40, 17).cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(0, 33).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(16, 33).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData pip() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 32).cuboid(-4.5f, -9.5f, -4.5f, 9.0f, 2.0f, 9.0f).uv(36, 32).cuboid(-4.5f, -8.0f, -6.5f, 9.0f, 1.0f, 3.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(32, 0).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(0, 16).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(16, 16).cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(32, 16).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(48, 16).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData lott() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(28, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("body", ModelPartBuilder.create().uv(0, 17).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 0).cuboid(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).uv(32, 33).cuboid(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("right_arm", ModelPartBuilder.create().uv(24, 17).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p3 = root.addChild("left_arm", ModelPartBuilder.create().uv(40, 17).cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("right_leg", ModelPartBuilder.create().uv(0, 33).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p5 = root.addChild("left_leg", ModelPartBuilder.create().uv(16, 33).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("hat", ModelPartBuilder.create(), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData finch() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(28, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 49).cuboid(-4.5f, -9.5f, -4.5f, 9.0f, 2.0f, 9.0f).uv(0, 60).cuboid(-4.5f, -8.0f, -6.5f, 9.0f, 1.0f, 3.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(0, 17).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 0).cuboid(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).uv(36, 49).cuboid(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(24, 17).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(40, 17).cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(0, 33).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(16, 33).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData grista() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 35).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(32, 35).cuboid(-4.3f, -8.3f, 0.5f, 8.6f, 10.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-5.0f, 0.0f, -3.0f, 10.0f, 12.0f, 6.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(32, 0).cuboid(-4.0f, -2.0f, -2.5f, 5.0f, 12.0f, 5.0f), ModelTransform.pivot(-6.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(0, 18).cuboid(-1.0f, -2.0f, -2.5f, 5.0f, 12.0f, 5.0f), ModelTransform.pivot(6.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(20, 18).cuboid(-2.5f, 0.0f, -2.5f, 5.0f, 12.0f, 5.0f), ModelTransform.pivot(-2.4f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(40, 18).cuboid(-2.5f, 0.0f, -2.5f, 5.0f, 12.0f, 5.0f), ModelTransform.pivot(2.4f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData nedFlint() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(28, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 49).cuboid(-6.0f, -9.0f, -6.0f, 12.0f, 1.0f, 12.0f).uv(0, 62).cuboid(-4.5f, -11.5f, -4.5f, 9.0f, 3.0f, 9.0f).uv(0, 74).cuboid(-5.5f, -10.5f, -6.5f, 11.0f, 2.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(0, 17).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 0).cuboid(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).uv(36, 62).cuboid(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(24, 17).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(40, 17).cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(0, 33).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(16, 33).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 128);
    }

    public static TexturedModelData bess() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 18).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 49).cuboid(-4.3f, -8.3f, 0.5f, 8.6f, 10.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(32, 18).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 0).cuboid(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).uv(32, 0).cuboid(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f).uv(26, 49).cuboid(-3.5f, 3.0f, -2.6f, 7.0f, 13.0f, 0.6f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(0, 34).cuboid(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(12, 34).cuboid(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(24, 34).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(36, 34).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData skip() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 32).cuboid(-4.5f, -8.6f, -4.5f, 9.0f, 3.0f, 9.0f).uv(36, 32).cuboid(-1.0f, -6.5f, 4.4f, 2.0f, 3.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(32, 0).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(0, 16).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(16, 16).cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(32, 16).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(48, 16).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData deedes() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(28, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("body", ModelPartBuilder.create().uv(0, 17).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 0).cuboid(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).uv(32, 33).cuboid(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("right_arm", ModelPartBuilder.create().uv(24, 17).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p3 = root.addChild("left_arm", ModelPartBuilder.create().uv(40, 17).cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("right_leg", ModelPartBuilder.create().uv(0, 33).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p5 = root.addChild("left_leg", ModelPartBuilder.create().uv(16, 33).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("hat", ModelPartBuilder.create(), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData marsh() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(28, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 49).cuboid(-4.5f, -9.5f, -4.5f, 9.0f, 2.0f, 9.0f).uv(0, 60).cuboid(-4.5f, -8.0f, -6.5f, 9.0f, 1.0f, 3.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(0, 17).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 0).cuboid(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).uv(36, 49).cuboid(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(24, 17).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(40, 17).cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(0, 33).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(16, 33).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData sounding() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f).uv(0, 43).cuboid(-4.0f, -2.0f, -4.6f, 8.0f, 3.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 32).cuboid(-4.5f, -9.5f, -4.5f, 9.0f, 2.0f, 9.0f).uv(36, 32).cuboid(-4.5f, -8.0f, -6.5f, 9.0f, 1.0f, 3.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(32, 0).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(0, 16).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(16, 16).cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(32, 16).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(48, 16).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData kettle() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(28, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(12, 33).cuboid(-6.0f, -9.0f, -6.0f, 12.0f, 1.0f, 12.0f).uv(0, 48).cuboid(-4.5f, -11.5f, -4.5f, 9.0f, 3.0f, 9.0f).uv(0, 60).cuboid(-5.5f, -10.5f, -6.5f, 11.0f, 2.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(0, 17).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 0).cuboid(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).uv(36, 48).cuboid(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(24, 17).cuboid(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(36, 17).cuboid(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(48, 17).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(0, 33).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData flossie() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 18).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 49).cuboid(-4.3f, -8.3f, 0.5f, 8.6f, 10.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(32, 18).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 0).cuboid(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).uv(32, 0).cuboid(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f).uv(26, 49).cuboid(-3.5f, 3.0f, -2.6f, 7.0f, 13.0f, 0.6f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(0, 34).cuboid(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(12, 34).cuboid(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(24, 34).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(36, 34).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData moss() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("body", ModelPartBuilder.create().uv(0, 16).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("right_arm", ModelPartBuilder.create().uv(0, 32).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p3 = root.addChild("left_arm", ModelPartBuilder.create().uv(16, 32).cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("right_leg", ModelPartBuilder.create().uv(0, 48).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p5 = root.addChild("left_leg", ModelPartBuilder.create().uv(16, 48).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("hat", ModelPartBuilder.create(), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        return TexturedModelData.of(data, 32, 64);
    }

    public static TexturedModelData quartermaster() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f).uv(0, 46).cuboid(-4.0f, -2.0f, -4.6f, 8.0f, 3.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(16, 32).cuboid(-4.5f, -8.6f, -4.5f, 9.0f, 3.0f, 9.0f).uv(18, 46).cuboid(-1.0f, -6.5f, 4.4f, 2.0f, 3.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(32, 0).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 32).cuboid(-3.5f, 3.0f, -2.6f, 7.0f, 13.0f, 0.6f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(0, 16).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(16, 16).cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(32, 16).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(48, 16).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData shipwright() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(16, 32).cuboid(-4.5f, -9.5f, -4.5f, 9.0f, 2.0f, 9.0f).uv(0, 46).cuboid(-4.5f, -8.0f, -6.5f, 9.0f, 1.0f, 3.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(32, 0).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 32).cuboid(-3.5f, 3.0f, -2.6f, 7.0f, 13.0f, 0.6f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(0, 16).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(16, 16).cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(32, 16).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(48, 16).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData marine() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(28, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 49).cuboid(-6.0f, -9.0f, -6.0f, 12.0f, 1.0f, 12.0f).uv(0, 62).cuboid(-4.5f, -11.5f, -4.5f, 9.0f, 3.0f, 9.0f).uv(0, 74).cuboid(-5.5f, -10.5f, -6.5f, 11.0f, 2.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(0, 17).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 0).cuboid(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).uv(36, 62).cuboid(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(24, 17).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(40, 17).cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(0, 33).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(16, 33).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 128);
    }

    public static TexturedModelData crewGunner() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 32).cuboid(-4.5f, -8.6f, -4.5f, 9.0f, 3.0f, 9.0f).uv(36, 32).cuboid(-1.0f, -6.5f, 4.4f, 2.0f, 3.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(32, 0).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(0, 16).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(16, 16).cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(32, 16).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(48, 16).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData crewBosun() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 35).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f).uv(0, 62).cuboid(-4.0f, -2.0f, -4.6f, 8.0f, 3.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 51).cuboid(-4.5f, -9.5f, -4.5f, 9.0f, 2.0f, 9.0f).uv(36, 51).cuboid(-4.5f, -8.0f, -6.5f, 9.0f, 1.0f, 3.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-5.0f, 0.0f, -3.0f, 10.0f, 12.0f, 6.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(32, 0).cuboid(-4.0f, -2.0f, -2.5f, 5.0f, 12.0f, 5.0f), ModelTransform.pivot(-6.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(0, 18).cuboid(-1.0f, -2.0f, -2.5f, 5.0f, 12.0f, 5.0f), ModelTransform.pivot(6.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(20, 18).cuboid(-2.5f, 0.0f, -2.5f, 5.0f, 12.0f, 5.0f), ModelTransform.pivot(-2.4f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(40, 18).cuboid(-2.5f, 0.0f, -2.5f, 5.0f, 12.0f, 5.0f), ModelTransform.pivot(2.4f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 128);
    }

    public static TexturedModelData crewNavigator() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(28, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 49).cuboid(-6.0f, -9.0f, -6.0f, 12.0f, 1.0f, 12.0f).uv(0, 62).cuboid(-4.5f, -11.5f, -4.5f, 9.0f, 3.0f, 9.0f).uv(0, 74).cuboid(-5.5f, -10.5f, -6.5f, 11.0f, 2.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(0, 17).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 0).cuboid(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).uv(36, 62).cuboid(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(24, 17).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(40, 17).cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(0, 33).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(16, 33).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 128);
    }

    public static TexturedModelData bountyHunter() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(28, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f).uv(0, 74).cuboid(-4.0f, -2.0f, -4.6f, 8.0f, 3.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 49).cuboid(-6.0f, -9.0f, -6.0f, 12.0f, 1.0f, 12.0f).uv(0, 62).cuboid(-4.5f, -11.5f, -4.5f, 9.0f, 3.0f, 9.0f).uv(18, 74).cuboid(-5.5f, -10.5f, -6.5f, 11.0f, 2.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(0, 17).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 0).cuboid(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).uv(36, 62).cuboid(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(24, 17).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(40, 17).cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(0, 33).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(16, 33).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 128);
    }

    public static TexturedModelData thorne() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(28, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f).uv(0, 74).cuboid(-4.0f, -2.0f, -4.6f, 8.0f, 3.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 49).cuboid(-6.0f, -9.0f, -6.0f, 12.0f, 1.0f, 12.0f).uv(0, 62).cuboid(-4.5f, -11.5f, -4.5f, 9.0f, 3.0f, 9.0f).uv(18, 74).cuboid(-5.5f, -10.5f, -6.5f, 11.0f, 2.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(0, 17).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 0).cuboid(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).uv(36, 62).cuboid(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f).uv(42, 74).cuboid(-4.0f, 9.0f, -2.4f, 8.0f, 1.0f, 0.4f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(24, 17).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(40, 17).cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(0, 33).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(16, 33).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 128);
    }

    public static TexturedModelData cursedGrunt() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(24, 16).cuboid(-4.5f, -8.6f, -4.5f, 9.0f, 3.0f, 9.0f).uv(0, 30).cuboid(-1.0f, -6.5f, 4.4f, 2.0f, 3.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(32, 0).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(56, 0).cuboid(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(0, 16).cuboid(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(8, 16).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(16, 16).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData cursedBrute() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 18).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-5.0f, 0.0f, -3.0f, 10.0f, 12.0f, 6.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("right_arm", ModelPartBuilder.create().uv(0, 34).cuboid(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f), ModelTransform.pivot(-6.0f, 2.0f, 0.0f));
        ModelPartData p3 = root.addChild("left_arm", ModelPartBuilder.create().uv(8, 34).cuboid(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f), ModelTransform.pivot(6.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("right_leg", ModelPartBuilder.create().uv(16, 34).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), ModelTransform.pivot(-2.4f, 12.0f, 0.0f));
        ModelPartData p5 = root.addChild("left_leg", ModelPartBuilder.create().uv(24, 34).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), ModelTransform.pivot(2.4f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("hat", ModelPartBuilder.create(), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        return TexturedModelData.of(data, 32, 64);
    }

    public static TexturedModelData cursedCaptain() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(28, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 33).cuboid(-6.0f, -9.0f, -6.0f, 12.0f, 1.0f, 12.0f).uv(0, 46).cuboid(-4.5f, -11.5f, -4.5f, 9.0f, 3.0f, 9.0f).uv(22, 58).cuboid(-5.5f, -10.5f, -6.5f, 11.0f, 2.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(0, 17).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 0).cuboid(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).uv(0, 58).cuboid(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(24, 17).cuboid(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).uv(36, 46).cuboid(-1.5f, -2.5f, -1.5f, 3.0f, 7.0f, 3.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(32, 17).cuboid(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).uv(48, 46).cuboid(-1.5f, -2.5f, -1.5f, 3.0f, 7.0f, 3.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(40, 17).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(48, 17).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 128);
    }

    public static TexturedModelData tideSovereign() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 45).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(32, 45).cuboid(-4.5f, -10.0f, -4.5f, 9.0f, 2.0f, 9.0f).uv(92, 45).cuboid(-4.5f, -12.0f, -4.5f, 1.0f, 2.0f, 1.0f).uv(96, 45).cuboid(3.5f, -12.0f, -4.5f, 1.0f, 2.0f, 1.0f).uv(100, 45).cuboid(-4.5f, -12.0f, 3.5f, 1.0f, 2.0f, 1.0f).uv(104, 45).cuboid(3.5f, -12.0f, 3.5f, 1.0f, 2.0f, 1.0f).uv(108, 45).cuboid(-0.5f, -12.0f, -4.5f, 1.0f, 2.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(0, 27).cuboid(-5.0f, 0.0f, -3.0f, 10.0f, 12.0f, 6.0f).uv(28, 0).cuboid(-5.6f, 0.0f, -3.6f, 11.2f, 12.0f, 7.2f).uv(68, 0).cuboid(-5.8f, 12.0f, -3.8f, 11.6f, 11.0f, 7.6f).uv(0, 61).cuboid(-5.0f, 9.0f, -3.4f, 10.0f, 1.0f, 0.4f).uv(4, 0).cuboid(-5.5f, 0.0f, 3.6f, 11.0f, 20.0f, 1.0f).uv(112, 45).cuboid(-6.0f, -3.0f, -1.0f, 1.0f, 2.0f, 1.0f).uv(84, 45).cuboid(-5.0f, -4.5f, 0.0f, 1.0f, 3.0f, 1.0f).uv(76, 45).cuboid(-4.0f, -6.0f, -1.0f, 1.0f, 4.0f, 1.0f).uv(116, 45).cuboid(5.0f, -3.0f, -1.0f, 1.0f, 2.0f, 1.0f).uv(88, 45).cuboid(4.0f, -4.5f, 0.0f, 1.0f, 3.0f, 1.0f).uv(80, 45).cuboid(3.0f, -6.0f, -1.0f, 1.0f, 4.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(32, 27).cuboid(-4.0f, -2.0f, -2.5f, 5.0f, 12.0f, 5.0f).uv(0, 0).cuboid(-3.0f, -8.0f, -5.5f, 1.0f, 26.0f, 1.0f).uv(68, 45).cuboid(-3.5f, -11.0f, -6.0f, 2.0f, 3.0f, 2.0f), ModelTransform.pivot(-6.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(52, 27).cuboid(-1.0f, -2.0f, -2.5f, 5.0f, 12.0f, 5.0f), ModelTransform.pivot(6.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(72, 27).cuboid(-2.5f, 0.0f, -2.5f, 5.0f, 12.0f, 5.0f), ModelTransform.pivot(-2.4f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(92, 27).cuboid(-2.5f, 0.0f, -2.5f, 5.0f, 12.0f, 5.0f), ModelTransform.pivot(2.4f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 128, 64);
    }

    public static TexturedModelData lampwrightsHand() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 39).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(20, 0).cuboid(-4.6f, -8.6f, -4.6f, 9.2f, 9.2f, 9.2f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(32, 39).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 21).cuboid(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).uv(32, 21).cuboid(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f).uv(0, 0).cuboid(-4.5f, 0.0f, 2.6f, 9.0f, 20.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(0, 55).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(16, 55).cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f).uv(12, 71).cuboid(-0.5f, 10.0f, -3.0f, 1.0f, 2.0f, 1.0f).uv(0, 71).cuboid(-1.5f, 12.0f, -4.0f, 3.0f, 4.0f, 3.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(32, 55).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(48, 55).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 128);
    }

    public static TexturedModelData cinderKing() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(32, 40).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 72).cuboid(-4.5f, -10.0f, -4.5f, 9.0f, 2.0f, 9.0f).uv(12, 83).cuboid(-4.5f, -12.0f, -4.5f, 1.0f, 2.0f, 1.0f).uv(16, 83).cuboid(3.5f, -12.0f, -4.5f, 1.0f, 2.0f, 1.0f).uv(20, 83).cuboid(-4.5f, -12.0f, 3.5f, 1.0f, 2.0f, 1.0f).uv(24, 83).cuboid(3.5f, -12.0f, 3.5f, 1.0f, 2.0f, 1.0f).uv(28, 83).cuboid(-0.5f, -12.0f, -4.5f, 1.0f, 2.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(0, 40).cuboid(-5.0f, 0.0f, -3.0f, 10.0f, 12.0f, 6.0f).uv(24, 0).cuboid(-5.6f, 0.0f, -3.6f, 11.2f, 12.0f, 7.2f).uv(0, 21).cuboid(-5.8f, 12.0f, -3.8f, 11.6f, 11.0f, 7.6f).uv(0, 0).cuboid(-5.5f, 0.0f, 3.6f, 11.0f, 20.0f, 1.0f).uv(32, 83).cuboid(-6.0f, -3.0f, -1.0f, 1.0f, 2.0f, 1.0f).uv(4, 83).cuboid(-5.0f, -4.5f, 0.0f, 1.0f, 3.0f, 1.0f).uv(60, 72).cuboid(-4.0f, -6.0f, -1.0f, 1.0f, 4.0f, 1.0f).uv(36, 83).cuboid(5.0f, -3.0f, -1.0f, 1.0f, 2.0f, 1.0f).uv(8, 83).cuboid(4.0f, -4.5f, 0.0f, 1.0f, 3.0f, 1.0f).uv(0, 83).cuboid(3.0f, -6.0f, -1.0f, 1.0f, 4.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(0, 58).cuboid(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).uv(36, 72).cuboid(-1.5f, -2.5f, -1.5f, 3.0f, 7.0f, 3.0f), ModelTransform.pivot(-6.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(8, 58).cuboid(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).uv(48, 72).cuboid(-1.5f, -2.5f, -1.5f, 3.0f, 7.0f, 3.0f), ModelTransform.pivot(6.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(16, 58).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), ModelTransform.pivot(-2.4f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(24, 58).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), ModelTransform.pivot(2.4f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 128);
    }

    public static TexturedModelData veilWitch() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 45).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(4, 0).cuboid(-4.6f, -8.6f, -4.6f, 9.2f, 9.2f, 9.2f).uv(0, 76).cuboid(-4.4f, -6.0f, -4.9f, 8.8f, 7.0f, 0.4f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(32, 45).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 27).cuboid(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).uv(32, 27).cuboid(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(0, 61).cuboid(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f).uv(0, 0).cuboid(-2.0f, -8.0f, -4.5f, 1.0f, 26.0f, 1.0f).uv(20, 76).cuboid(-2.5f, -11.0f, -5.0f, 2.0f, 3.0f, 2.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(12, 61).cuboid(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(24, 61).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(36, 61).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 128);
    }

    public static TexturedModelData redMaddox() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 56).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f).uv(46, 97).cuboid(-4.0f, -2.0f, -4.6f, 8.0f, 3.0f, 1.0f).uv(24, 106).cuboid(1.0f, -5.0f, -4.3f, 2.0f, 2.0f, 0.3f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 72).cuboid(-6.0f, -9.0f, -6.0f, 12.0f, 1.0f, 12.0f).uv(0, 85).cuboid(-4.5f, -11.5f, -4.5f, 9.0f, 3.0f, 9.0f).uv(0, 106).cuboid(-5.5f, -10.5f, -6.5f, 11.0f, 2.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(0, 21).cuboid(-5.0f, 0.0f, -3.0f, 10.0f, 12.0f, 6.0f).uv(24, 0).cuboid(-5.5f, 0.0f, -3.5f, 11.0f, 12.0f, 7.0f).uv(20, 97).cuboid(-5.5f, 12.0f, 2.5f, 11.0f, 6.0f, 1.5f).uv(0, 0).cuboid(-5.5f, 0.0f, 3.6f, 11.0f, 20.0f, 1.0f).uv(36, 85).cuboid(-7.0f, -1.0f, -3.5f, 3.0f, 1.5f, 7.0f).uv(0, 97).cuboid(4.0f, -1.0f, -3.5f, 3.0f, 1.5f, 7.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(32, 21).cuboid(-4.0f, -2.0f, -2.5f, 5.0f, 12.0f, 5.0f), ModelTransform.pivot(-6.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(0, 39).cuboid(-1.0f, -2.0f, -2.5f, 5.0f, 12.0f, 5.0f), ModelTransform.pivot(6.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(20, 39).cuboid(-2.5f, 0.0f, -2.5f, 5.0f, 12.0f, 5.0f), ModelTransform.pivot(-2.4f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(40, 39).cuboid(-2.5f, 0.0f, -2.5f, 5.0f, 12.0f, 5.0f), ModelTransform.pivot(2.4f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 128);
    }

    public static TexturedModelData ashenHerald() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 45).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(4, 0).cuboid(-4.6f, -8.6f, -4.6f, 9.2f, 9.2f, 9.2f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(32, 45).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 27).cuboid(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).uv(32, 27).cuboid(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f).uv(56, 61).cuboid(3.0f, 10.0f, -4.0f, 2.0f, 2.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(56, 45).cuboid(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).uv(24, 61).cuboid(-1.5f, -2.5f, -1.5f, 3.0f, 7.0f, 3.0f).uv(0, 0).cuboid(-1.5f, -8.0f, -4.0f, 1.0f, 26.0f, 1.0f).uv(48, 61).cuboid(-2.0f, -11.0f, -4.5f, 2.0f, 3.0f, 2.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(0, 61).cuboid(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).uv(36, 61).cuboid(-1.5f, -2.5f, -1.5f, 3.0f, 7.0f, 3.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(8, 61).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(16, 61).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 128);
    }

    public static TexturedModelData pretender() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 21).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 53).cuboid(-4.5f, -10.0f, -4.5f, 9.0f, 2.0f, 9.0f).uv(32, 64).cuboid(-4.5f, -12.0f, -4.5f, 1.0f, 2.0f, 1.0f).uv(36, 64).cuboid(3.5f, -12.0f, -4.5f, 1.0f, 2.0f, 1.0f).uv(40, 64).cuboid(-4.5f, -12.0f, 3.5f, 1.0f, 2.0f, 1.0f).uv(44, 64).cuboid(3.5f, -12.0f, 3.5f, 1.0f, 2.0f, 1.0f).uv(48, 64).cuboid(-0.5f, -12.0f, -4.5f, 1.0f, 2.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(32, 21).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(20, 0).cuboid(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).uv(36, 53).cuboid(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f).uv(0, 71).cuboid(-4.0f, 9.0f, -2.4f, 8.0f, 1.0f, 0.4f).uv(0, 0).cuboid(-4.5f, 0.0f, 2.6f, 9.0f, 20.0f, 1.0f).uv(0, 64).cuboid(-6.0f, -1.0f, -2.5f, 3.0f, 1.5f, 5.0f).uv(16, 64).cuboid(3.0f, -1.0f, -2.5f, 3.0f, 1.5f, 5.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(0, 37).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(16, 37).cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(32, 37).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(48, 37).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 128);
    }

    public static TexturedModelData abyssal() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 0).cuboid(-9.0f, -8.0f, -18.0f, 18.0f, 14.0f, 18.0f).uv(46, 110).cuboid(-6.0f, -4.0f, -30.0f, 12.0f, 8.0f, 12.0f).uv(110, 132).cuboid(-9.5f, -6.0f, -15.0f, 2.0f, 3.0f, 3.0f).uv(0, 146).cuboid(7.5f, -6.0f, -15.0f, 2.0f, 3.0f, 3.0f), ModelTransform.pivot(0.0f, 8.0f, -10.0f));
        ModelPartData p1 = p0.addChild("jaw", ModelPartBuilder.create().uv(48, 87).cuboid(-6.0f, 0.0f, -18.0f, 12.0f, 4.0f, 18.0f), ModelTransform.pivot(0.0f, 6.0f, -12.0f));
        ModelPartData p2 = root.addChild("seg1", ModelPartBuilder.create().uv(0, 32).cuboid(-7.2f, -6.3f, 0.0f, 14.4f, 12.6f, 16.0f).uv(94, 110).cuboid(-0.5f, -10.3f, 3.0f, 1.0f, 4.0f, 10.0f), ModelTransform.pivot(0.0f, 8.0f, -10.0f));
        ModelPartData p3 = p2.addChild("seg2", ModelPartBuilder.create().uv(62, 32).cuboid(-6.4f, -5.6f, 0.0f, 12.8f, 11.2f, 16.0f).uv(0, 132).cuboid(-0.5f, -9.6f, 3.0f, 1.0f, 4.0f, 10.0f), ModelTransform.pivot(0.0f, 0.0f, 16.0f));
        ModelPartData p4 = p3.addChild("seg3", ModelPartBuilder.create().uv(0, 61).cuboid(-5.6f, -4.9f, 0.0f, 11.2f, 9.8f, 16.0f).uv(22, 132).cuboid(-0.5f, -8.9f, 3.0f, 1.0f, 4.0f, 10.0f), ModelTransform.pivot(0.0f, 0.0f, 16.0f));
        ModelPartData p5 = p4.addChild("seg4", ModelPartBuilder.create().uv(56, 61).cuboid(-4.8f, -4.2f, 0.0f, 9.6f, 8.4f, 16.0f).uv(44, 132).cuboid(-0.5f, -8.2f, 3.0f, 1.0f, 4.0f, 10.0f), ModelTransform.pivot(0.0f, 0.0f, 16.0f));
        ModelPartData p6 = p5.addChild("seg5", ModelPartBuilder.create().uv(0, 87).cuboid(-4.0f, -3.5f, 0.0f, 8.0f, 7.0f, 16.0f).uv(66, 132).cuboid(-0.5f, -7.5f, 3.0f, 1.0f, 4.0f, 10.0f), ModelTransform.pivot(0.0f, 0.0f, 16.0f));
        ModelPartData p7 = p6.addChild("seg6", ModelPartBuilder.create().uv(0, 110).cuboid(-3.2f, -2.8f, 0.0f, 6.4f, 5.6f, 16.0f).uv(88, 132).cuboid(-0.5f, -6.8f, 3.0f, 1.0f, 4.0f, 10.0f), ModelTransform.pivot(0.0f, 0.0f, 16.0f));
        return TexturedModelData.of(data, 128, 256);
    }

    public static TexturedModelData brannoch() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 35).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f).uv(0, 51).cuboid(-4.0f, -2.0f, -4.6f, 8.0f, 3.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-5.0f, 0.0f, -3.0f, 10.0f, 12.0f, 6.0f).uv(18, 51).cuboid(-5.0f, 9.0f, -3.4f, 10.0f, 1.0f, 0.4f).uv(44, 35).cuboid(-5.0f, 2.0f, -3.7f, 1.0f, 9.0f, 0.5f).uv(36, 35).cuboid(-3.0f, 4.0f, -3.7f, 1.0f, 11.0f, 0.5f).uv(32, 35).cuboid(-1.0f, 2.0f, -3.7f, 1.0f, 13.0f, 0.5f).uv(48, 35).cuboid(1.0f, 4.0f, -3.7f, 1.0f, 9.0f, 0.5f).uv(40, 35).cuboid(3.0f, 2.0f, -3.7f, 1.0f, 11.0f, 0.5f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("right_arm", ModelPartBuilder.create().uv(32, 0).cuboid(-4.0f, -2.0f, -2.5f, 5.0f, 12.0f, 5.0f), ModelTransform.pivot(-6.0f, 2.0f, 0.0f));
        ModelPartData p3 = root.addChild("left_arm", ModelPartBuilder.create().uv(0, 18).cuboid(-1.0f, -2.0f, -2.5f, 5.0f, 12.0f, 5.0f), ModelTransform.pivot(6.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("right_leg", ModelPartBuilder.create().uv(20, 18).cuboid(-2.5f, 0.0f, -2.5f, 5.0f, 12.0f, 5.0f), ModelTransform.pivot(-2.4f, 12.0f, 0.0f));
        ModelPartData p5 = root.addChild("left_leg", ModelPartBuilder.create().uv(40, 18).cuboid(-2.5f, 0.0f, -2.5f, 5.0f, 12.0f, 5.0f), ModelTransform.pivot(2.4f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("hat", ModelPartBuilder.create(), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData greyLamplighter() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 45).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(4, 0).cuboid(-4.6f, -8.6f, -4.6f, 9.2f, 9.2f, 9.2f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(32, 45).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 27).cuboid(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).uv(32, 27).cuboid(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(56, 45).cuboid(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).uv(24, 61).cuboid(-1.5f, -2.5f, -1.5f, 3.0f, 7.0f, 3.0f).uv(0, 0).cuboid(-1.5f, -8.0f, -4.0f, 1.0f, 26.0f, 1.0f).uv(0, 75).cuboid(-2.0f, -11.0f, -4.5f, 2.0f, 3.0f, 2.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(0, 61).cuboid(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).uv(36, 61).cuboid(-1.5f, -2.5f, -1.5f, 3.0f, 7.0f, 3.0f).uv(8, 75).cuboid(-0.5f, 10.0f, -2.0f, 1.0f, 2.0f, 1.0f).uv(48, 61).cuboid(-1.5f, 12.0f, -3.0f, 3.0f, 4.0f, 3.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(8, 61).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(16, 61).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 128);
    }

    public static TexturedModelData motherOfGulls() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 38).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 0).cuboid(-4.6f, -8.6f, -4.6f, 9.2f, 9.2f, 9.2f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(32, 38).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 20).cuboid(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).uv(32, 20).cuboid(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f).uv(0, 54).cuboid(-13.0f, -2.0f, 2.0f, 9.0f, 14.0f, 1.0f).uv(20, 54).cuboid(4.0f, -2.0f, 2.0f, 9.0f, 14.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(40, 54).cuboid(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(52, 54).cuboid(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(0, 69).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(12, 69).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 128);
    }

    public static TexturedModelData boneAbbot() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(32, 66).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(32, 84).cuboid(-3.5f, -14.0f, -3.0f, 7.0f, 6.0f, 6.0f).uv(24, 98).cuboid(-0.5f, -15.0f, -3.2f, 1.0f, 6.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(0, 66).cuboid(-5.0f, 0.0f, -3.0f, 10.0f, 12.0f, 6.0f).uv(0, 27).cuboid(-5.6f, 0.0f, -3.6f, 11.2f, 12.0f, 7.2f).uv(0, 47).cuboid(-5.8f, 12.0f, -3.8f, 11.6f, 11.0f, 7.6f).uv(4, 0).cuboid(-5.5f, 0.0f, 3.6f, 11.0f, 20.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(0, 84).cuboid(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).uv(0, 98).cuboid(-1.5f, -2.5f, -1.5f, 3.0f, 7.0f, 3.0f).uv(0, 0).cuboid(-1.5f, -8.0f, -4.0f, 1.0f, 26.0f, 1.0f).uv(28, 98).cuboid(-2.0f, -11.0f, -4.5f, 2.0f, 3.0f, 2.0f), ModelTransform.pivot(-6.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(8, 84).cuboid(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).uv(12, 98).cuboid(-1.5f, -2.5f, -1.5f, 3.0f, 7.0f, 3.0f), ModelTransform.pivot(6.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(16, 84).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), ModelTransform.pivot(-2.4f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(24, 84).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), ModelTransform.pivot(2.4f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 128);
    }

    public static TexturedModelData cinderMatron() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 38).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 0).cuboid(-4.6f, -8.6f, -4.6f, 9.2f, 9.2f, 9.2f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(32, 38).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 20).cuboid(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).uv(32, 20).cuboid(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f).uv(16, 69).cuboid(-4.0f, 9.0f, -2.4f, 8.0f, 1.0f, 0.4f).uv(8, 69).cuboid(-5.0f, -3.0f, -1.0f, 1.0f, 2.0f, 1.0f).uv(0, 69).cuboid(-4.0f, -4.5f, 0.0f, 1.0f, 3.0f, 1.0f).uv(48, 54).cuboid(-3.0f, -6.0f, -1.0f, 1.0f, 4.0f, 1.0f).uv(12, 69).cuboid(4.0f, -3.0f, -1.0f, 1.0f, 2.0f, 1.0f).uv(4, 69).cuboid(3.0f, -4.5f, 0.0f, 1.0f, 3.0f, 1.0f).uv(52, 54).cuboid(2.0f, -6.0f, -1.0f, 1.0f, 4.0f, 1.0f).uv(56, 54).cuboid(3.0f, 10.0f, -4.0f, 2.0f, 2.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(0, 54).cuboid(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(12, 54).cuboid(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(24, 54).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(36, 54).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 128);
    }

    public static TexturedModelData oldGnasher() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-8.5f, -5.1f, -17.0f, 17.0f, 10.2f, 34.0f), ModelTransform.pivot(0.0f, 15.5f, 0.0f));
        ModelPartData p1 = p0.addChild("head", ModelPartBuilder.create().uv(50, 73).cuboid(-5.95f, -4.25f, -10.2f, 11.9f, 8.5f, 10.2f).uv(0, 73).cuboid(-4.25f, -1.7f, -25.5f, 8.5f, 5.1f, 15.3f).uv(72, 113).cuboid(-5.1f, -5.95f, -6.8f, 3.4f, 1.7f, 3.4f).uv(88, 113).cuboid(1.7f, -5.95f, -6.8f, 3.4f, 1.7f, 3.4f), ModelTransform.pivot(0.0f, -1.7f, -17.0f));
        ModelPartData p2 = p1.addChild("jaw", ModelPartBuilder.create().uv(0, 95).cuboid(-4.25f, 0.0f, -15.3f, 8.5f, 1.7f, 15.3f), ModelTransform.pivot(0.0f, 3.4f, -10.2f));
        ModelPartData p3 = p0.addChild("tail1", ModelPartBuilder.create().uv(0, 45).cuboid(-5.1f, -3.4f, 0.0f, 10.2f, 6.8f, 20.4f), ModelTransform.pivot(0.0f, 0.0f, 17.0f));
        ModelPartData p4 = p3.addChild("tail2", ModelPartBuilder.create().uv(64, 45).cuboid(-3.4f, -2.55f, 0.0f, 6.8f, 5.1f, 17.0f), ModelTransform.pivot(0.0f, 0.85f, 20.4f));
        ModelPartData p5 = p4.addChild("tail3", ModelPartBuilder.create().uv(50, 95).cuboid(-1.7f, -1.7f, 0.0f, 3.4f, 3.4f, 13.6f), ModelTransform.pivot(0.0f, 0.85f, 17.0f));
        ModelPartData p6 = root.addChild("leg_fl", ModelPartBuilder.create().uv(86, 95).cuboid(-3.4f, 0.0f, -2.55f, 5.1f, 5.1f, 5.1f), ModelTransform.pivot(-8.5f, 18.9f, -11.9f));
        ModelPartData p7 = root.addChild("leg_fr", ModelPartBuilder.create().uv(0, 113).cuboid(-1.7f, 0.0f, -2.55f, 5.1f, 5.1f, 5.1f), ModelTransform.pivot(8.5f, 18.9f, -11.9f));
        ModelPartData p8 = root.addChild("leg_bl", ModelPartBuilder.create().uv(24, 113).cuboid(-3.4f, 0.0f, -2.55f, 5.1f, 5.1f, 5.1f), ModelTransform.pivot(-8.5f, 18.9f, 11.9f));
        ModelPartData p9 = root.addChild("leg_br", ModelPartBuilder.create().uv(48, 113).cuboid(-1.7f, 0.0f, -2.55f, 5.1f, 5.1f, 5.1f), ModelTransform.pivot(8.5f, 18.9f, 11.9f));
        return TexturedModelData.of(data, 128, 128);
    }

    public static TexturedModelData grubb() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 35).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f).uv(0, 62).cuboid(-4.0f, -2.0f, -4.6f, 8.0f, 3.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 51).cuboid(-4.5f, -9.5f, -4.5f, 9.0f, 2.0f, 9.0f).uv(36, 51).cuboid(-4.5f, -8.0f, -6.5f, 9.0f, 1.0f, 3.0f).uv(18, 62).cuboid(-1.0f, -10.0f, -5.2f, 2.0f, 2.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-5.0f, 0.0f, -3.0f, 10.0f, 12.0f, 6.0f).uv(32, 35).cuboid(-4.5f, 3.0f, -3.6f, 9.0f, 13.0f, 0.6f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(32, 0).cuboid(-4.0f, -2.0f, -2.5f, 5.0f, 12.0f, 5.0f), ModelTransform.pivot(-6.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(0, 18).cuboid(-1.0f, -2.0f, -2.5f, 5.0f, 12.0f, 5.0f), ModelTransform.pivot(6.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(20, 18).cuboid(-2.5f, 0.0f, -2.5f, 5.0f, 12.0f, 5.0f), ModelTransform.pivot(-2.4f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(40, 18).cuboid(-2.5f, 0.0f, -2.5f, 5.0f, 12.0f, 5.0f), ModelTransform.pivot(2.4f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 128);
    }

    public static TexturedModelData motherBrine() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(32, 27).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(12, 60).cuboid(-4.3f, -8.3f, 0.5f, 8.6f, 10.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(0, 44).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(4, 0).cuboid(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).uv(0, 27).cuboid(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f).uv(8, 75).cuboid(-4.0f, 9.0f, -2.4f, 8.0f, 1.0f, 0.4f).uv(50, 60).cuboid(-4.0f, 2.0f, -2.7f, 1.0f, 9.0f, 0.5f).uv(42, 60).cuboid(-2.4f, 4.0f, -2.7f, 1.0f, 11.0f, 0.5f).uv(38, 60).cuboid(-0.8f, 2.0f, -2.7f, 1.0f, 13.0f, 0.5f).uv(54, 60).cuboid(0.8f, 4.0f, -2.7f, 1.0f, 9.0f, 0.5f).uv(46, 60).cuboid(2.4f, 2.0f, -2.7f, 1.0f, 11.0f, 0.5f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(24, 44).cuboid(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f).uv(0, 0).cuboid(-2.0f, -8.0f, -4.5f, 1.0f, 26.0f, 1.0f).uv(0, 75).cuboid(-2.5f, -11.0f, -5.0f, 2.0f, 3.0f, 2.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(36, 44).cuboid(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(48, 44).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(0, 60).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 128);
    }

    public static TexturedModelData slagBeast() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-8.0f, -7.0f, -12.0f, 16.0f, 13.0f, 24.0f).uv(0, 37).cuboid(-6.0f, -11.0f, -8.0f, 12.0f, 4.0f, 14.0f), ModelTransform.pivot(0.0f, 8.0f, 0.0f));
        ModelPartData p1 = p0.addChild("tail1", ModelPartBuilder.create().uv(24, 55).cuboid(-2.5f, -2.0f, 0.0f, 5.0f, 4.0f, 10.0f), ModelTransform.pivot(0.0f, -2.0f, 12.0f));
        ModelPartData p2 = root.addChild("head", ModelPartBuilder.create().uv(80, 0).cuboid(-5.0f, -5.0f, -10.0f, 10.0f, 10.0f, 10.0f).uv(54, 55).cuboid(-6.0f, -8.0f, -6.0f, 2.0f, 4.0f, 2.0f).uv(62, 55).cuboid(4.0f, -8.0f, -6.0f, 2.0f, 4.0f, 2.0f), ModelTransform.pivot(0.0f, 6.0f, -12.0f));
        ModelPartData p3 = root.addChild("leg_fl", ModelPartBuilder.create().uv(52, 37).cuboid(-3.0f, 0.0f, -3.0f, 6.0f, 11.0f, 6.0f), ModelTransform.pivot(-6.0f, 13.0f, -8.0f));
        ModelPartData p4 = root.addChild("leg_fr", ModelPartBuilder.create().uv(76, 37).cuboid(-3.0f, 0.0f, -3.0f, 6.0f, 11.0f, 6.0f), ModelTransform.pivot(6.0f, 13.0f, -8.0f));
        ModelPartData p5 = root.addChild("leg_bl", ModelPartBuilder.create().uv(100, 37).cuboid(-3.0f, 0.0f, -3.0f, 6.0f, 11.0f, 6.0f), ModelTransform.pivot(-6.0f, 13.0f, 8.0f));
        ModelPartData p6 = root.addChild("leg_br", ModelPartBuilder.create().uv(0, 55).cuboid(-3.0f, 0.0f, -3.0f, 6.0f, 11.0f, 6.0f), ModelTransform.pivot(6.0f, 13.0f, 8.0f));
        return TexturedModelData.of(data, 128, 128);
    }

    public static TexturedModelData lanternWarden() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 94).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 0).cuboid(-4.6f, -8.6f, -4.6f, 9.2f, 9.2f, 9.2f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(0, 59).cuboid(-5.0f, 0.0f, -3.0f, 10.0f, 12.0f, 6.0f).uv(0, 20).cuboid(-5.6f, 0.0f, -3.6f, 11.2f, 12.0f, 7.2f).uv(0, 40).cuboid(-5.8f, 12.0f, -3.8f, 11.6f, 11.0f, 7.6f).uv(44, 94).cuboid(-1.5f, 3.0f, -5.5f, 3.0f, 4.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(32, 59).cuboid(-4.0f, -2.0f, -2.5f, 5.0f, 12.0f, 5.0f), ModelTransform.pivot(-6.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(0, 77).cuboid(-1.0f, -2.0f, -2.5f, 5.0f, 12.0f, 5.0f).uv(54, 94).cuboid(-0.5f, 10.0f, -3.5f, 1.0f, 2.0f, 1.0f).uv(32, 94).cuboid(-1.5f, 12.0f, -4.5f, 3.0f, 4.0f, 3.0f), ModelTransform.pivot(6.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(20, 77).cuboid(-2.5f, 0.0f, -2.5f, 5.0f, 12.0f, 5.0f), ModelTransform.pivot(-2.4f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(40, 77).cuboid(-2.5f, 0.0f, -2.5f, 5.0f, 12.0f, 5.0f), ModelTransform.pivot(2.4f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 128);
    }

    public static TexturedModelData corvinHale() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(32, 27).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 78).cuboid(-7.0f, -12.0f, -2.0f, 14.0f, 4.0f, 4.0f).uv(0, 59).cuboid(-4.5f, -9.0f, -4.5f, 9.0f, 1.0f, 9.0f).uv(8, 86).cuboid(-1.0f, -13.0f, -2.5f, 2.0f, 2.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(0, 27).cuboid(-5.0f, 0.0f, -3.0f, 10.0f, 12.0f, 6.0f).uv(28, 0).cuboid(-5.5f, 0.0f, -3.5f, 11.0f, 12.0f, 7.0f).uv(36, 78).cuboid(-5.5f, 12.0f, 2.5f, 11.0f, 6.0f, 1.5f).uv(4, 0).cuboid(-5.5f, 0.0f, 3.6f, 11.0f, 20.0f, 1.0f).uv(0, 69).cuboid(-7.0f, -1.0f, -3.5f, 3.0f, 1.5f, 7.0f).uv(20, 69).cuboid(4.0f, -1.0f, -3.5f, 3.0f, 1.5f, 7.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(0, 45).cuboid(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).uv(36, 59).cuboid(-1.5f, -2.5f, -1.5f, 3.0f, 7.0f, 3.0f).uv(0, 0).cuboid(-1.5f, -8.0f, -4.0f, 1.0f, 26.0f, 1.0f).uv(0, 86).cuboid(-2.0f, -11.0f, -4.5f, 2.0f, 3.0f, 2.0f), ModelTransform.pivot(-6.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(8, 45).cuboid(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).uv(48, 59).cuboid(-1.5f, -2.5f, -1.5f, 3.0f, 7.0f, 3.0f), ModelTransform.pivot(6.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(16, 45).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), ModelTransform.pivot(-2.4f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(24, 45).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), ModelTransform.pivot(2.4f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 128);
    }

    public static TexturedModelData captainSaltbones() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 21).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f).uv(46, 76).cuboid(1.0f, -5.0f, -4.3f, 2.0f, 2.0f, 0.3f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 51).cuboid(-6.0f, -9.0f, -6.0f, 12.0f, 1.0f, 12.0f).uv(0, 64).cuboid(-4.5f, -11.5f, -4.5f, 9.0f, 3.0f, 9.0f).uv(22, 76).cuboid(-5.5f, -10.5f, -6.5f, 11.0f, 2.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(32, 21).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(20, 0).cuboid(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).uv(0, 76).cuboid(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f).uv(0, 0).cuboid(-4.5f, 0.0f, 2.6f, 9.0f, 20.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(56, 21).cuboid(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).uv(36, 64).cuboid(-1.5f, -2.5f, -1.5f, 3.0f, 7.0f, 3.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(0, 37).cuboid(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).uv(48, 64).cuboid(-1.5f, -2.5f, -1.5f, 3.0f, 7.0f, 3.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(8, 37).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(16, 37).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 128);
    }

    public static TexturedModelData barnacleBride() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 38).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 0).cuboid(-4.6f, -8.6f, -4.6f, 9.2f, 9.2f, 9.2f).uv(4, 69).cuboid(-4.4f, -6.0f, -4.9f, 8.8f, 7.0f, 0.4f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(32, 38).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 20).cuboid(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).uv(32, 20).cuboid(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f).uv(24, 69).cuboid(-4.0f, 9.0f, -2.4f, 8.0f, 1.0f, 0.4f).uv(60, 54).cuboid(-4.0f, 2.0f, -2.7f, 1.0f, 9.0f, 0.5f).uv(52, 54).cuboid(-2.4f, 4.0f, -2.7f, 1.0f, 11.0f, 0.5f).uv(48, 54).cuboid(-0.8f, 2.0f, -2.7f, 1.0f, 13.0f, 0.5f).uv(0, 69).cuboid(0.8f, 4.0f, -2.7f, 1.0f, 9.0f, 0.5f).uv(56, 54).cuboid(2.4f, 2.0f, -2.7f, 1.0f, 11.0f, 0.5f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(0, 54).cuboid(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(12, 54).cuboid(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(24, 54).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(36, 54).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 128);
    }

    public static TexturedModelData hangman() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 62).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(4, 0).cuboid(-4.6f, -8.6f, -4.6f, 9.2f, 9.2f, 9.2f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(0, 27).cuboid(-5.0f, 0.0f, -3.0f, 10.0f, 12.0f, 6.0f).uv(32, 62).cuboid(-4.5f, 3.0f, -3.6f, 9.0f, 13.0f, 0.6f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(32, 27).cuboid(-4.0f, -2.0f, -2.5f, 5.0f, 12.0f, 5.0f).uv(0, 0).cuboid(-3.0f, -8.0f, -5.5f, 1.0f, 26.0f, 1.0f).uv(52, 62).cuboid(-3.5f, -11.0f, -6.0f, 2.0f, 3.0f, 2.0f), ModelTransform.pivot(-6.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(0, 45).cuboid(-1.0f, -2.0f, -2.5f, 5.0f, 12.0f, 5.0f), ModelTransform.pivot(6.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(20, 45).cuboid(-2.5f, 0.0f, -2.5f, 5.0f, 12.0f, 5.0f), ModelTransform.pivot(-2.4f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(40, 45).cuboid(-2.5f, 0.0f, -2.5f, 5.0f, 12.0f, 5.0f), ModelTransform.pivot(2.4f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 128);
    }

    public static TexturedModelData witchlightHag() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 45).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(4, 0).cuboid(-4.6f, -8.6f, -4.6f, 9.2f, 9.2f, 9.2f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(32, 45).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 27).cuboid(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).uv(32, 27).cuboid(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(0, 61).cuboid(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f).uv(0, 0).cuboid(-2.0f, -8.0f, -4.5f, 1.0f, 26.0f, 1.0f).uv(0, 76).cuboid(-2.5f, -11.0f, -5.0f, 2.0f, 3.0f, 2.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(12, 61).cuboid(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f).uv(8, 76).cuboid(-0.5f, 10.0f, -2.5f, 1.0f, 2.0f, 1.0f).uv(48, 61).cuboid(-1.5f, 12.0f, -3.5f, 3.0f, 4.0f, 3.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(24, 61).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(36, 61).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 128);
    }

    public static TexturedModelData signalKeeper() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(28, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 43).cuboid(-7.0f, -12.0f, -2.0f, 14.0f, 4.0f, 4.0f).uv(0, 33).cuboid(-4.5f, -9.0f, -4.5f, 9.0f, 1.0f, 9.0f).uv(44, 51).cuboid(-1.0f, -13.0f, -2.5f, 2.0f, 2.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(0, 17).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 0).cuboid(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).uv(36, 43).cuboid(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f).uv(0, 51).cuboid(-6.0f, -1.0f, -2.5f, 3.0f, 1.5f, 5.0f).uv(16, 51).cuboid(3.0f, -1.0f, -2.5f, 3.0f, 1.5f, 5.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(24, 17).cuboid(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).uv(36, 33).cuboid(-1.5f, -2.5f, -1.5f, 3.0f, 7.0f, 3.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(32, 17).cuboid(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).uv(48, 33).cuboid(-1.5f, -2.5f, -1.5f, 3.0f, 7.0f, 3.0f).uv(50, 51).cuboid(-0.5f, 10.0f, -2.0f, 1.0f, 2.0f, 1.0f).uv(32, 51).cuboid(-1.5f, 12.0f, -3.0f, 3.0f, 4.0f, 3.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(40, 17).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(48, 17).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData reachSerpent() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 0).cuboid(-9.0f, -8.0f, -18.0f, 18.0f, 14.0f, 18.0f).uv(46, 110).cuboid(-6.0f, -4.0f, -30.0f, 12.0f, 8.0f, 12.0f).uv(110, 132).cuboid(-9.5f, -6.0f, -15.0f, 2.0f, 3.0f, 3.0f).uv(0, 146).cuboid(7.5f, -6.0f, -15.0f, 2.0f, 3.0f, 3.0f), ModelTransform.pivot(0.0f, 8.0f, -10.0f));
        ModelPartData p1 = p0.addChild("jaw", ModelPartBuilder.create().uv(48, 87).cuboid(-6.0f, 0.0f, -18.0f, 12.0f, 4.0f, 18.0f), ModelTransform.pivot(0.0f, 6.0f, -12.0f));
        ModelPartData p2 = root.addChild("seg1", ModelPartBuilder.create().uv(0, 32).cuboid(-7.2f, -6.3f, 0.0f, 14.4f, 12.6f, 16.0f).uv(94, 110).cuboid(-0.5f, -10.3f, 3.0f, 1.0f, 4.0f, 10.0f), ModelTransform.pivot(0.0f, 8.0f, -10.0f));
        ModelPartData p3 = p2.addChild("seg2", ModelPartBuilder.create().uv(62, 32).cuboid(-6.4f, -5.6f, 0.0f, 12.8f, 11.2f, 16.0f).uv(0, 132).cuboid(-0.5f, -9.6f, 3.0f, 1.0f, 4.0f, 10.0f), ModelTransform.pivot(0.0f, 0.0f, 16.0f));
        ModelPartData p4 = p3.addChild("seg3", ModelPartBuilder.create().uv(0, 61).cuboid(-5.6f, -4.9f, 0.0f, 11.2f, 9.8f, 16.0f).uv(22, 132).cuboid(-0.5f, -8.9f, 3.0f, 1.0f, 4.0f, 10.0f), ModelTransform.pivot(0.0f, 0.0f, 16.0f));
        ModelPartData p5 = p4.addChild("seg4", ModelPartBuilder.create().uv(56, 61).cuboid(-4.8f, -4.2f, 0.0f, 9.6f, 8.4f, 16.0f).uv(44, 132).cuboid(-0.5f, -8.2f, 3.0f, 1.0f, 4.0f, 10.0f), ModelTransform.pivot(0.0f, 0.0f, 16.0f));
        ModelPartData p6 = p5.addChild("seg5", ModelPartBuilder.create().uv(0, 87).cuboid(-4.0f, -3.5f, 0.0f, 8.0f, 7.0f, 16.0f).uv(66, 132).cuboid(-0.5f, -7.5f, 3.0f, 1.0f, 4.0f, 10.0f), ModelTransform.pivot(0.0f, 0.0f, 16.0f));
        ModelPartData p7 = p6.addChild("seg6", ModelPartBuilder.create().uv(0, 110).cuboid(-3.2f, -2.8f, 0.0f, 6.4f, 5.6f, 16.0f).uv(88, 132).cuboid(-0.5f, -6.8f, 3.0f, 1.0f, 4.0f, 10.0f), ModelTransform.pivot(0.0f, 0.0f, 16.0f));
        return TexturedModelData.of(data, 128, 256);
    }

    public static TexturedModelData siren() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("head", ModelPartBuilder.create().uv(0, 18).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("hat", ModelPartBuilder.create().uv(0, 49).cuboid(-4.3f, -8.3f, 0.5f, 8.6f, 10.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("body", ModelPartBuilder.create().uv(32, 18).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 0).cuboid(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).uv(32, 0).cuboid(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f).uv(46, 49).cuboid(-4.0f, 9.0f, -2.4f, 8.0f, 1.0f, 0.4f).uv(38, 49).cuboid(-4.0f, 2.0f, -2.7f, 1.0f, 9.0f, 0.5f).uv(30, 49).cuboid(-2.4f, 4.0f, -2.7f, 1.0f, 11.0f, 0.5f).uv(26, 49).cuboid(-0.8f, 2.0f, -2.7f, 1.0f, 13.0f, 0.5f).uv(42, 49).cuboid(0.8f, 4.0f, -2.7f, 1.0f, 9.0f, 0.5f).uv(34, 49).cuboid(2.4f, 2.0f, -2.7f, 1.0f, 11.0f, 0.5f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("right_arm", ModelPartBuilder.create().uv(0, 34).cuboid(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
        ModelPartData p4 = root.addChild("left_arm", ModelPartBuilder.create().uv(12, 34).cuboid(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(5.0f, 2.0f, 0.0f));
        ModelPartData p5 = root.addChild("right_leg", ModelPartBuilder.create().uv(24, 34).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
        ModelPartData p6 = root.addChild("left_leg", ModelPartBuilder.create().uv(36, 34).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData giantWasp() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("body", ModelPartBuilder.create().uv(28, 0).cuboid(-2.5f, -2.5f, -3.0f, 5.0f, 5.0f, 6.0f), ModelTransform.pivot(0.0f, 12.0f, 0.0f));
        ModelPartData p1 = p0.addChild("head", ModelPartBuilder.create().uv(0, 14).cuboid(-2.0f, -2.0f, -4.0f, 4.0f, 4.0f, 4.0f).uv(58, 22).cuboid(-2.4f, -1.6f, -3.6f, 1.0f, 2.0f, 2.0f).uv(0, 29).cuboid(1.4f, -1.6f, -3.6f, 1.0f, 2.0f, 2.0f).uv(14, 29).cuboid(-1.5f, 1.5f, -4.6f, 1.0f, 1.0f, 1.0f).uv(18, 29).cuboid(0.5f, 1.5f, -4.6f, 1.0f, 1.0f, 1.0f).uv(6, 29).cuboid(-1.5f, -4.0f, -3.5f, 0.5f, 2.0f, 0.5f).uv(10, 29).cuboid(1.0f, -4.0f, -3.5f, 0.5f, 2.0f, 0.5f), ModelTransform.pivot(0.0f, -0.5f, -3.0f));
        ModelPartData p2 = p0.addChild("tail1", ModelPartBuilder.create().uv(0, 0).cuboid(-3.0f, -3.0f, 0.0f, 6.0f, 6.0f, 8.0f), ModelTransform.of(0.0f, 0.5f, 3.0f, -0.314f, 0.0f, 0.0f));
        ModelPartData p3 = p2.addChild("tail2", ModelPartBuilder.create().uv(50, 22).cuboid(-0.5f, -0.5f, 0.0f, 1.0f, 1.0f, 3.0f), ModelTransform.of(0.0f, 0.5f, 8.0f, 0.349f, 0.0f, 0.0f));
        ModelPartData p4 = p0.addChild("wing_l", ModelPartBuilder.create().uv(16, 14).cuboid(-11.0f, 0.0f, -2.0f, 11.0f, 0.5f, 6.0f), ModelTransform.of(-1.5f, -2.5f, -1.0f, 0.0f, 0.0f, 0.349f));
        ModelPartData p5 = p0.addChild("wing_r", ModelPartBuilder.create().uv(0, 22).cuboid(0.0f, 0.0f, -2.0f, 11.0f, 0.5f, 6.0f), ModelTransform.of(1.5f, -2.5f, -1.0f, 0.0f, 0.0f, -0.349f));
        ModelPartData p6 = p0.addChild("leg_fl", ModelPartBuilder.create().uv(34, 22).cuboid(-0.5f, 0.0f, -0.5f, 1.0f, 5.0f, 1.0f), ModelTransform.pivot(-2.0f, 2.5f, -2.0f));
        ModelPartData p7 = p0.addChild("leg_fr", ModelPartBuilder.create().uv(38, 22).cuboid(-0.5f, 0.0f, -0.5f, 1.0f, 5.0f, 1.0f), ModelTransform.pivot(2.0f, 2.5f, -2.0f));
        ModelPartData p8 = p0.addChild("leg_bl", ModelPartBuilder.create().uv(42, 22).cuboid(-0.5f, 0.0f, -0.5f, 1.0f, 5.0f, 1.0f), ModelTransform.pivot(-2.0f, 2.5f, 2.0f));
        ModelPartData p9 = p0.addChild("leg_br", ModelPartBuilder.create().uv(46, 22).cuboid(-0.5f, 0.0f, -0.5f, 1.0f, 5.0f, 1.0f), ModelTransform.pivot(2.0f, 2.5f, 2.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData caveBat() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-3.0f, -3.0f, -3.0f, 6.0f, 7.0f, 6.0f), ModelTransform.pivot(0.0f, 10.0f, 0.0f));
        ModelPartData p1 = p0.addChild("head", ModelPartBuilder.create().uv(0, 33).cuboid(-2.5f, -4.0f, -3.0f, 5.0f, 4.0f, 5.0f).uv(20, 33).cuboid(-2.5f, -6.5f, -1.0f, 1.5f, 3.0f, 1.0f).uv(26, 33).cuboid(1.0f, -6.5f, -1.0f, 1.5f, 3.0f, 1.0f).uv(6, 44).cuboid(-1.8f, -2.8f, -3.3f, 1.0f, 1.0f, 0.5f).uv(10, 44).cuboid(0.8f, -2.8f, -3.3f, 1.0f, 1.0f, 0.5f).uv(0, 44).cuboid(-1.0f, -1.0f, -3.6f, 2.0f, 1.0f, 1.0f), ModelTransform.pivot(0.0f, -3.0f, -1.0f));
        ModelPartData p2 = p0.addChild("wing_l", ModelPartBuilder.create().uv(0, 13).cuboid(-15.0f, 0.0f, -1.0f, 15.0f, 0.6f, 9.0f).uv(0, 42).cuboid(-15.0f, -0.5f, -1.0f, 15.0f, 1.0f, 1.0f), ModelTransform.of(-3.0f, -2.0f, 0.0f, 0.0f, 0.0f, 0.175f));
        ModelPartData p3 = p0.addChild("wing_r", ModelPartBuilder.create().uv(0, 23).cuboid(0.0f, 0.0f, -1.0f, 15.0f, 0.6f, 9.0f).uv(32, 42).cuboid(0.0f, -0.5f, -1.0f, 15.0f, 1.0f, 1.0f), ModelTransform.of(3.0f, -2.0f, 0.0f, 0.0f, 0.0f, -0.175f));
        ModelPartData p4 = p0.addChild("leg_fl", ModelPartBuilder.create().uv(32, 33).cuboid(-0.5f, 0.0f, -0.5f, 1.0f, 3.0f, 1.0f), ModelTransform.pivot(-1.5f, 4.0f, 1.0f));
        ModelPartData p5 = p0.addChild("leg_fr", ModelPartBuilder.create().uv(36, 33).cuboid(-0.5f, 0.0f, -0.5f, 1.0f, 3.0f, 1.0f), ModelTransform.pivot(1.5f, 4.0f, 1.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData duneScorpion() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -2.5f, -6.0f, 8.0f, 5.0f, 12.0f), ModelTransform.pivot(0.0f, 19.0f, 0.0f));
        ModelPartData p1 = p0.addChild("head", ModelPartBuilder.create().uv(16, 17).cuboid(-3.0f, -2.0f, -3.0f, 6.0f, 3.5f, 3.0f).uv(42, 36).cuboid(-2.0f, -2.6f, -2.6f, 1.0f, 1.0f, 1.0f).uv(46, 36).cuboid(1.0f, -2.6f, -2.6f, 1.0f, 1.0f, 1.0f), ModelTransform.pivot(0.0f, 0.0f, -6.0f));
        ModelPartData p2 = p0.addChild("tail1", ModelPartBuilder.create().uv(40, 0).cuboid(-1.5f, -1.5f, 0.0f, 3.0f, 3.0f, 6.0f), ModelTransform.of(0.0f, -1.5f, 6.0f, 0.96f, 0.0f, 0.0f));
        ModelPartData p3 = p2.addChild("tail2", ModelPartBuilder.create().uv(0, 17).cuboid(-1.2f, -1.2f, 0.0f, 2.4f, 2.4f, 5.0f), ModelTransform.of(0.0f, 0.0f, 6.0f, 0.698f, 0.0f, 0.0f));
        ModelPartData p4 = p3.addChild("tail3", ModelPartBuilder.create().uv(48, 25).cuboid(-1.0f, -1.0f, 0.0f, 2.0f, 2.0f, 4.0f).uv(16, 32).cuboid(-0.5f, -0.5f, 4.0f, 1.0f, 1.0f, 2.0f), ModelTransform.of(0.0f, 0.0f, 5.0f, 0.785f, 0.0f, 0.0f));
        ModelPartData p5 = root.addChild("claw_l", ModelPartBuilder.create().uv(34, 17).cuboid(-3.0f, -1.0f, -5.0f, 3.0f, 2.0f, 5.0f).uv(0, 25).cuboid(-4.0f, -1.5f, -9.0f, 4.0f, 3.0f, 4.0f).uv(0, 32).cuboid(-4.0f, -1.0f, -11.0f, 2.0f, 2.0f, 2.0f), ModelTransform.of(-4.0f, 19.0f, -6.0f, 0.0f, -0.349f, 0.0f));
        ModelPartData p6 = root.addChild("claw_r", ModelPartBuilder.create().uv(16, 25).cuboid(0.0f, -1.0f, -5.0f, 3.0f, 2.0f, 5.0f).uv(32, 25).cuboid(0.0f, -1.5f, -9.0f, 4.0f, 3.0f, 4.0f).uv(8, 32).cuboid(2.0f, -1.0f, -11.0f, 2.0f, 2.0f, 2.0f), ModelTransform.of(4.0f, 19.0f, -6.0f, 0.0f, 0.349f, 0.0f));
        ModelPartData p7 = root.addChild("leg_fl", ModelPartBuilder.create().uv(22, 32).cuboid(-6.0f, -0.5f, -0.5f, 6.0f, 1.0f, 1.0f), ModelTransform.of(-4.0f, 20.0f, -3.0f, 0.0f, 0.0f, -0.611f));
        ModelPartData p8 = root.addChild("leg_ml", ModelPartBuilder.create().uv(36, 32).cuboid(-6.0f, -0.5f, -0.5f, 6.0f, 1.0f, 1.0f), ModelTransform.of(-4.0f, 20.0f, 0.0f, 0.0f, 0.0f, -0.611f));
        ModelPartData p9 = root.addChild("leg_bl", ModelPartBuilder.create().uv(50, 32).cuboid(-6.0f, -0.5f, -0.5f, 6.0f, 1.0f, 1.0f), ModelTransform.of(-4.0f, 20.0f, 3.0f, 0.0f, 0.0f, -0.611f));
        ModelPartData p10 = root.addChild("leg_fr", ModelPartBuilder.create().uv(0, 36).cuboid(0.0f, -0.5f, -0.5f, 6.0f, 1.0f, 1.0f), ModelTransform.of(4.0f, 20.0f, -3.0f, 0.0f, 0.0f, 0.611f));
        ModelPartData p11 = root.addChild("leg_mr", ModelPartBuilder.create().uv(14, 36).cuboid(0.0f, -0.5f, -0.5f, 6.0f, 1.0f, 1.0f), ModelTransform.of(4.0f, 20.0f, 0.0f, 0.0f, 0.0f, 0.611f));
        ModelPartData p12 = root.addChild("leg_br", ModelPartBuilder.create().uv(28, 36).cuboid(0.0f, -0.5f, -0.5f, 6.0f, 1.0f, 1.0f), ModelTransform.of(4.0f, 20.0f, 3.0f, 0.0f, 0.0f, 0.611f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData snapjaw() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("seg1", ModelPartBuilder.create().uv(0, 0).cuboid(-1.5f, -12.0f, -1.5f, 3.0f, 12.0f, 3.0f), ModelTransform.pivot(0.0f, 24.0f, 0.0f));
        ModelPartData p1 = p0.addChild("head", ModelPartBuilder.create().uv(12, 0).cuboid(-5.0f, -4.0f, -9.0f, 10.0f, 4.0f, 10.0f).uv(30, 37).cuboid(-4.5f, 0.0f, -9.0f, 1.0f, 1.5f, 1.0f).uv(34, 37).cuboid(-2.5f, 0.0f, -9.0f, 1.0f, 1.5f, 1.0f).uv(38, 37).cuboid(-0.5f, 0.0f, -9.0f, 1.0f, 1.5f, 1.0f).uv(42, 37).cuboid(1.5f, 0.0f, -9.0f, 1.0f, 1.5f, 1.0f).uv(46, 37).cuboid(3.5f, 0.0f, -9.0f, 1.0f, 1.5f, 1.0f), ModelTransform.pivot(0.0f, -12.0f, 0.0f));
        ModelPartData p2 = p1.addChild("jaw", ModelPartBuilder.create().uv(0, 15).cuboid(-5.0f, 0.0f, -10.0f, 10.0f, 3.0f, 10.0f).uv(50, 37).cuboid(-3.5f, -1.5f, -9.5f, 1.0f, 1.5f, 1.0f).uv(54, 37).cuboid(-1.5f, -1.5f, -9.5f, 1.0f, 1.5f, 1.0f).uv(58, 37).cuboid(0.5f, -1.5f, -9.5f, 1.0f, 1.5f, 1.0f).uv(0, 44).cuboid(2.5f, -1.5f, -9.5f, 1.0f, 1.5f, 1.0f).uv(0, 28).cuboid(-4.0f, -0.2f, -9.0f, 8.0f, 0.5f, 8.0f), ModelTransform.pivot(0.0f, 0.0f, 1.0f));
        ModelPartData p3 = root.addChild("fin_l", ModelPartBuilder.create().uv(32, 28).cuboid(-9.0f, 0.0f, -3.0f, 9.0f, 0.5f, 6.0f), ModelTransform.of(-1.5f, 23.5f, 0.0f, 0.0f, 0.0f, 0.436f));
        ModelPartData p4 = root.addChild("fin_r", ModelPartBuilder.create().uv(0, 37).cuboid(0.0f, 0.0f, -3.0f, 9.0f, 0.5f, 6.0f), ModelTransform.of(1.5f, 23.5f, 0.0f, 0.0f, 0.0f, -0.436f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData livingStump() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-5.0f, 0.0f, -5.0f, 10.0f, 16.0f, 10.0f).uv(0, 26).cuboid(-5.5f, -2.0f, -5.5f, 11.0f, 2.0f, 11.0f).uv(0, 47).cuboid(-3.0f, 4.0f, -5.3f, 2.0f, 2.0f, 0.5f).uv(6, 47).cuboid(1.0f, 4.0f, -5.3f, 2.0f, 2.0f, 0.5f).uv(16, 39).cuboid(-2.5f, 9.0f, -5.3f, 5.0f, 1.5f, 0.5f), ModelTransform.pivot(0.0f, 4.0f, 0.0f));
        ModelPartData p1 = root.addChild("arm_l", ModelPartBuilder.create().uv(40, 0).cuboid(-3.0f, -1.0f, -1.5f, 3.0f, 13.0f, 3.0f).uv(28, 39).cuboid(-5.0f, 6.0f, -1.0f, 3.0f, 1.0f, 2.0f), ModelTransform.of(-5.0f, 8.0f, 0.0f, 0.0f, 0.0f, 0.262f));
        ModelPartData p2 = root.addChild("arm_r", ModelPartBuilder.create().uv(52, 0).cuboid(0.0f, -1.0f, -1.5f, 3.0f, 13.0f, 3.0f).uv(38, 39).cuboid(2.0f, 9.0f, -1.0f, 3.0f, 1.0f, 2.0f), ModelTransform.of(5.0f, 8.0f, 0.0f, 0.0f, 0.0f, -0.262f));
        ModelPartData p3 = root.addChild("leg_fl", ModelPartBuilder.create().uv(44, 26).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 4.0f, 4.0f).uv(48, 39).cuboid(-3.5f, 3.0f, -3.0f, 2.0f, 1.0f, 2.0f), ModelTransform.pivot(-2.5f, 20.0f, 0.0f));
        ModelPartData p4 = root.addChild("leg_fr", ModelPartBuilder.create().uv(0, 39).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 4.0f, 4.0f).uv(56, 39).cuboid(1.5f, 3.0f, 1.0f, 2.0f, 1.0f, 2.0f), ModelTransform.pivot(2.5f, 20.0f, 0.0f));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData shipSloop() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("keel", ModelPartBuilder.create().uv(0, 72).cuboid(-10.0f, 0.0f, -26.0f, 20.0f, 3.0f, 52.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("side_left", ModelPartBuilder.create().uv(8, 0).cuboid(-12.0f, -10.0f, -26.0f, 2.0f, 13.0f, 52.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("side_right", ModelPartBuilder.create().uv(116, 0).cuboid(10.0f, -10.0f, -26.0f, 2.0f, 13.0f, 52.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("stern", ModelPartBuilder.create().uv(44, 209).cuboid(-12.0f, -10.0f, -28.0f, 24.0f, 13.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p4 = root.addChild("bow", ModelPartBuilder.create().uv(110, 181).cuboid(-8.0f, -10.0f, 26.0f, 16.0f, 13.0f, 6.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p5 = root.addChild("bow_tip", ModelPartBuilder.create().uv(210, 181).cuboid(-5.0f, -9.0f, 32.0f, 10.0f, 11.0f, 5.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p6 = root.addChild("deck", ModelPartBuilder.create().uv(108, 127).cuboid(-10.0f, -4.0f, -26.0f, 20.0f, 1.0f, 52.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p7 = root.addChild("rail_left", ModelPartBuilder.create().uv(144, 72).cuboid(-12.0f, -12.0f, -26.0f, 2.0f, 2.0f, 52.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p8 = root.addChild("rail_right", ModelPartBuilder.create().uv(0, 127).cuboid(10.0f, -12.0f, -26.0f, 2.0f, 2.0f, 52.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p9 = root.addChild("cabin", ModelPartBuilder.create().uv(154, 181).cuboid(-9.0f, -17.0f, -26.0f, 18.0f, 8.0f, 10.0f).uv(96, 209).cuboid(-10.0f, -18.0f, -27.0f, 20.0f, 1.0f, 12.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p10 = root.addChild("mast1", ModelPartBuilder.create().uv(0, 0).cuboid(-1.0f, -74.0f, 4.0f, 2.0f, 70.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p11 = root.addChild("yard1", ModelPartBuilder.create().uv(0, 225).cuboid(-18.0f, -62.0f, 3.0f, 36.0f, 2.0f, 2.0f).uv(76, 225).cuboid(-16.0f, -39.0f, 3.0f, 32.0f, 2.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p12 = root.addChild("sail1", ModelPartBuilder.create().uv(0, 181).cuboid(-17.0f, 0.0f, 6.0f, 34.0f, 27.0f, 1.0f), ModelTransform.pivot(0.0f, -60.0f, 0.0f));
        ModelPartData p13 = root.addChild("bowsprit", ModelPartBuilder.create().uv(70, 181).cuboid(-1.0f, -12.0f, 34.0f, 2.0f, 2.0f, 18.0f), ModelTransform.of(0.0f, 0.0f, 0.0f, -0.244f, 0.0f, 0.0f));
        ModelPartData p14 = root.addChild("figurehead", ModelPartBuilder.create().uv(160, 209).cuboid(-1.5f, -8.0f, 36.0f, 3.0f, 6.0f, 3.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p15 = root.addChild("flag", ModelPartBuilder.create().uv(0, 209).cuboid(0.0f, -74.0f, -6.0f, 1.0f, 6.0f, 10.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p16 = root.addChild("emblem", ModelPartBuilder.create().uv(22, 209).cuboid(0.0f, 0.0f, -5.0f, 1.0f, 6.0f, 10.0f), ModelTransform.of(0.0f, -53.0f, 7.6f, 0.0f, 1.571f, 0.0f));
        ModelPartData p17 = root.addChild("cannon_l1", ModelPartBuilder.create().uv(172, 209).cuboid(-14.0f, -6.0f, -14.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p18 = root.addChild("cannon_r1", ModelPartBuilder.create().uv(184, 209).cuboid(12.0f, -6.0f, -14.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p19 = root.addChild("cannon_l2", ModelPartBuilder.create().uv(196, 209).cuboid(-14.0f, -6.0f, 14.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p20 = root.addChild("cannon_r2", ModelPartBuilder.create().uv(208, 209).cuboid(12.0f, -6.0f, 14.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        return TexturedModelData.of(data, 256, 256);
    }

    public static TexturedModelData shipSchooner() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("keel", ModelPartBuilder.create().uv(8, 83).cuboid(-10.0f, 0.0f, -30.0f, 20.0f, 3.0f, 60.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("side_left", ModelPartBuilder.create().uv(8, 0).cuboid(-12.0f, -10.0f, -30.0f, 2.0f, 13.0f, 60.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("side_right", ModelPartBuilder.create().uv(132, 0).cuboid(10.0f, -10.0f, -30.0f, 2.0f, 13.0f, 60.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("stern", ModelPartBuilder.create().uv(44, 304).cuboid(-12.0f, -10.0f, -32.0f, 24.0f, 13.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p4 = root.addChild("bow", ModelPartBuilder.create().uv(106, 277).cuboid(-8.0f, -10.0f, 30.0f, 16.0f, 13.0f, 6.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p5 = root.addChild("bow_tip", ModelPartBuilder.create().uv(206, 277).cuboid(-5.0f, -9.0f, 36.0f, 10.0f, 11.0f, 5.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p6 = root.addChild("deck", ModelPartBuilder.create().uv(0, 216).cuboid(-10.0f, -4.0f, -30.0f, 20.0f, 1.0f, 60.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p7 = root.addChild("rail_left", ModelPartBuilder.create().uv(0, 154).cuboid(-12.0f, -12.0f, -30.0f, 2.0f, 2.0f, 60.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p8 = root.addChild("rail_right", ModelPartBuilder.create().uv(124, 154).cuboid(10.0f, -12.0f, -30.0f, 2.0f, 2.0f, 60.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p9 = root.addChild("cabin", ModelPartBuilder.create().uv(150, 277).cuboid(-9.0f, -17.0f, -30.0f, 18.0f, 8.0f, 10.0f).uv(96, 304).cuboid(-10.0f, -18.0f, -31.0f, 20.0f, 1.0f, 12.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p10 = root.addChild("mast1", ModelPartBuilder.create().uv(0, 83).cuboid(-1.0f, -73.0f, -11.0f, 2.0f, 69.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p11 = root.addChild("yard1", ModelPartBuilder.create().uv(144, 320).cuboid(-15.5f, -61.0f, -12.0f, 31.0f, 2.0f, 2.0f).uv(0, 324).cuboid(-13.5f, -38.0f, -12.0f, 27.0f, 2.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p12 = root.addChild("sail1", ModelPartBuilder.create().uv(0, 277).cuboid(-14.5f, 0.0f, -9.0f, 29.0f, 26.0f, 1.0f), ModelTransform.pivot(0.0f, -59.0f, 0.0f));
        ModelPartData p13 = root.addChild("mast2", ModelPartBuilder.create().uv(0, 0).cuboid(-1.0f, -85.0f, 12.0f, 2.0f, 81.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p14 = root.addChild("yard2", ModelPartBuilder.create().uv(0, 320).cuboid(-18.0f, -73.0f, 11.0f, 36.0f, 2.0f, 2.0f).uv(76, 320).cuboid(-16.0f, -45.0f, 11.0f, 32.0f, 2.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p15 = root.addChild("sail2", ModelPartBuilder.create().uv(160, 216).cuboid(-17.0f, 0.0f, 14.0f, 34.0f, 33.0f, 1.0f), ModelTransform.pivot(0.0f, -71.0f, 0.0f));
        ModelPartData p16 = root.addChild("bowsprit", ModelPartBuilder.create().uv(60, 277).cuboid(-1.0f, -12.0f, 38.0f, 2.0f, 2.0f, 21.0f), ModelTransform.of(0.0f, 0.0f, 0.0f, -0.244f, 0.0f, 0.0f));
        ModelPartData p17 = root.addChild("figurehead", ModelPartBuilder.create().uv(160, 304).cuboid(-1.5f, -8.0f, 40.0f, 3.0f, 6.0f, 3.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p18 = root.addChild("flag", ModelPartBuilder.create().uv(0, 304).cuboid(0.0f, -85.0f, 2.0f, 1.0f, 6.0f, 10.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p19 = root.addChild("emblem", ModelPartBuilder.create().uv(22, 304).cuboid(0.0f, 0.0f, -5.0f, 1.0f, 6.0f, 10.0f), ModelTransform.of(0.0f, -61.0f, 15.6f, 0.0f, 1.571f, 0.0f));
        ModelPartData p20 = root.addChild("cannon_l1", ModelPartBuilder.create().uv(172, 304).cuboid(-14.0f, -6.0f, -18.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p21 = root.addChild("cannon_r1", ModelPartBuilder.create().uv(184, 304).cuboid(12.0f, -6.0f, -18.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p22 = root.addChild("cannon_l2", ModelPartBuilder.create().uv(196, 304).cuboid(-14.0f, -6.0f, 18.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p23 = root.addChild("cannon_r2", ModelPartBuilder.create().uv(208, 304).cuboid(12.0f, -6.0f, 18.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        return TexturedModelData.of(data, 256, 512);
    }

    public static TexturedModelData shipBrigantine() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("keel", ModelPartBuilder.create().uv(288, 0).cuboid(-12.0f, 0.0f, -33.0f, 24.0f, 3.0f, 66.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("side_left", ModelPartBuilder.create().uv(8, 0).cuboid(-14.0f, -12.0f, -33.0f, 2.0f, 15.0f, 66.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("side_right", ModelPartBuilder.create().uv(144, 0).cuboid(12.0f, -12.0f, -33.0f, 2.0f, 15.0f, 66.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("stern", ModelPartBuilder.create().uv(356, 159).cuboid(-14.0f, -12.0f, -35.0f, 28.0f, 15.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p4 = root.addChild("bow", ModelPartBuilder.create().uv(206, 159).cuboid(-10.0f, -12.0f, 33.0f, 20.0f, 15.0f, 6.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p5 = root.addChild("bow_tip", ModelPartBuilder.create().uv(322, 159).cuboid(-6.0f, -11.0f, 39.0f, 12.0f, 13.0f, 5.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p6 = root.addChild("deck", ModelPartBuilder.create().uv(272, 91).cuboid(-12.0f, -4.0f, -33.0f, 24.0f, 1.0f, 66.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p7 = root.addChild("rail_left", ModelPartBuilder.create().uv(0, 91).cuboid(-14.0f, -14.0f, -33.0f, 2.0f, 2.0f, 66.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p8 = root.addChild("rail_right", ModelPartBuilder.create().uv(136, 91).cuboid(12.0f, -14.0f, -33.0f, 2.0f, 2.0f, 66.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p9 = root.addChild("cabin", ModelPartBuilder.create().uv(258, 159).cuboid(-11.0f, -19.0f, -33.0f, 22.0f, 8.0f, 10.0f).uv(0, 197).cuboid(-12.0f, -20.0f, -34.0f, 24.0f, 1.0f, 12.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p10 = root.addChild("mast1", ModelPartBuilder.create().uv(280, 0).cuboid(-1.0f, -80.0f, -12.0f, 2.0f, 76.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p11 = root.addChild("yard1", ModelPartBuilder.create().uv(328, 197).cuboid(-18.5f, -68.0f, -13.0f, 37.0f, 2.0f, 2.0f).uv(406, 197).cuboid(-16.5f, -42.0f, -13.0f, 33.0f, 2.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p12 = root.addChild("sail1", ModelPartBuilder.create().uv(84, 159).cuboid(-17.5f, 0.0f, -10.0f, 35.0f, 30.0f, 1.0f), ModelTransform.pivot(0.0f, -66.0f, 0.0f));
        ModelPartData p13 = root.addChild("mast2", ModelPartBuilder.create().uv(0, 0).cuboid(-1.0f, -93.0f, 13.0f, 2.0f, 89.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p14 = root.addChild("yard2", ModelPartBuilder.create().uv(156, 197).cuboid(-21.5f, -81.0f, 12.0f, 43.0f, 2.0f, 2.0f).uv(246, 197).cuboid(-19.5f, -49.0f, 12.0f, 39.0f, 2.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p15 = root.addChild("sail2", ModelPartBuilder.create().uv(0, 159).cuboid(-20.5f, 0.0f, 15.0f, 41.0f, 37.0f, 1.0f), ModelTransform.pivot(0.0f, -79.0f, 0.0f));
        ModelPartData p16 = root.addChild("bowsprit", ModelPartBuilder.create().uv(156, 159).cuboid(-1.0f, -14.0f, 41.0f, 2.0f, 2.0f, 23.0f), ModelTransform.of(0.0f, 0.0f, 0.0f, -0.244f, 0.0f, 0.0f));
        ModelPartData p17 = root.addChild("figurehead", ModelPartBuilder.create().uv(72, 197).cuboid(-1.5f, -10.0f, 43.0f, 3.0f, 6.0f, 3.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p18 = root.addChild("flag", ModelPartBuilder.create().uv(416, 159).cuboid(0.0f, -93.0f, 3.0f, 1.0f, 6.0f, 10.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p19 = root.addChild("emblem", ModelPartBuilder.create().uv(438, 159).cuboid(0.0f, 0.0f, -5.0f, 1.0f, 6.0f, 10.0f), ModelTransform.of(0.0f, -67.0f, 16.6f, 0.0f, 1.571f, 0.0f));
        ModelPartData p20 = root.addChild("cannon_l1", ModelPartBuilder.create().uv(84, 197).cuboid(-16.0f, -8.0f, -21.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p21 = root.addChild("cannon_r1", ModelPartBuilder.create().uv(96, 197).cuboid(14.0f, -8.0f, -21.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p22 = root.addChild("cannon_l2", ModelPartBuilder.create().uv(108, 197).cuboid(-16.0f, -8.0f, 0.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p23 = root.addChild("cannon_r2", ModelPartBuilder.create().uv(120, 197).cuboid(14.0f, -8.0f, 0.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p24 = root.addChild("cannon_l3", ModelPartBuilder.create().uv(132, 197).cuboid(-16.0f, -8.0f, 21.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p25 = root.addChild("cannon_r3", ModelPartBuilder.create().uv(144, 197).cuboid(14.0f, -8.0f, 21.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        return TexturedModelData.of(data, 512, 256);
    }

    public static TexturedModelData shipBrig() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("keel", ModelPartBuilder.create().uv(304, 0).cuboid(-12.0f, 0.0f, -35.0f, 24.0f, 3.0f, 70.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("side_left", ModelPartBuilder.create().uv(8, 0).cuboid(-14.0f, -12.0f, -35.0f, 2.0f, 15.0f, 70.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("side_right", ModelPartBuilder.create().uv(152, 0).cuboid(12.0f, -12.0f, -35.0f, 2.0f, 15.0f, 70.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("stern", ModelPartBuilder.create().uv(360, 169).cuboid(-14.0f, -12.0f, -37.0f, 28.0f, 15.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p4 = root.addChild("bow", ModelPartBuilder.create().uv(210, 169).cuboid(-10.0f, -12.0f, 35.0f, 20.0f, 15.0f, 6.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p5 = root.addChild("bow_tip", ModelPartBuilder.create().uv(326, 169).cuboid(-6.0f, -11.0f, 41.0f, 12.0f, 13.0f, 5.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p6 = root.addChild("deck", ModelPartBuilder.create().uv(288, 97).cuboid(-12.0f, -4.0f, -35.0f, 24.0f, 1.0f, 70.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p7 = root.addChild("rail_left", ModelPartBuilder.create().uv(0, 97).cuboid(-14.0f, -14.0f, -35.0f, 2.0f, 2.0f, 70.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p8 = root.addChild("rail_right", ModelPartBuilder.create().uv(144, 97).cuboid(12.0f, -14.0f, -35.0f, 2.0f, 2.0f, 70.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p9 = root.addChild("cabin", ModelPartBuilder.create().uv(262, 169).cuboid(-11.0f, -19.0f, -35.0f, 22.0f, 8.0f, 10.0f).uv(0, 210).cuboid(-12.0f, -20.0f, -36.0f, 24.0f, 1.0f, 12.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p10 = root.addChild("mast1", ModelPartBuilder.create().uv(296, 0).cuboid(-1.0f, -85.0f, -13.0f, 2.0f, 81.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p11 = root.addChild("yard1", ModelPartBuilder.create().uv(352, 210).cuboid(-18.5f, -73.0f, -14.0f, 37.0f, 2.0f, 2.0f).uv(430, 210).cuboid(-16.5f, -45.0f, -14.0f, 33.0f, 2.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p12 = root.addChild("sail1", ModelPartBuilder.create().uv(84, 169).cuboid(-17.5f, 0.0f, -11.0f, 35.0f, 33.0f, 1.0f), ModelTransform.pivot(0.0f, -71.0f, 0.0f));
        ModelPartData p13 = root.addChild("mast2", ModelPartBuilder.create().uv(0, 0).cuboid(-1.0f, -99.0f, 14.0f, 2.0f, 95.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p14 = root.addChild("yard2", ModelPartBuilder.create().uv(180, 210).cuboid(-21.5f, -87.0f, 13.0f, 43.0f, 2.0f, 2.0f).uv(270, 210).cuboid(-19.5f, -52.0f, 13.0f, 39.0f, 2.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p15 = root.addChild("sail2", ModelPartBuilder.create().uv(0, 169).cuboid(-20.5f, 0.0f, 16.0f, 41.0f, 40.0f, 1.0f), ModelTransform.pivot(0.0f, -85.0f, 0.0f));
        ModelPartData p16 = root.addChild("bowsprit", ModelPartBuilder.create().uv(156, 169).cuboid(-1.0f, -14.0f, 43.0f, 2.0f, 2.0f, 25.0f), ModelTransform.of(0.0f, 0.0f, 0.0f, -0.244f, 0.0f, 0.0f));
        ModelPartData p17 = root.addChild("figurehead", ModelPartBuilder.create().uv(72, 210).cuboid(-1.5f, -10.0f, 45.0f, 3.0f, 6.0f, 3.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p18 = root.addChild("flag", ModelPartBuilder.create().uv(420, 169).cuboid(0.0f, -99.0f, 4.0f, 1.0f, 6.0f, 10.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p19 = root.addChild("emblem", ModelPartBuilder.create().uv(442, 169).cuboid(0.0f, 0.0f, -5.0f, 1.0f, 6.0f, 10.0f), ModelTransform.of(0.0f, -71.0f, 17.6f, 0.0f, 1.571f, 0.0f));
        ModelPartData p20 = root.addChild("cannon_l1", ModelPartBuilder.create().uv(84, 210).cuboid(-16.0f, -8.0f, -23.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p21 = root.addChild("cannon_r1", ModelPartBuilder.create().uv(96, 210).cuboid(14.0f, -8.0f, -23.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p22 = root.addChild("cannon_l2", ModelPartBuilder.create().uv(108, 210).cuboid(-16.0f, -8.0f, -8.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p23 = root.addChild("cannon_r2", ModelPartBuilder.create().uv(120, 210).cuboid(14.0f, -8.0f, -8.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p24 = root.addChild("cannon_l3", ModelPartBuilder.create().uv(132, 210).cuboid(-16.0f, -8.0f, 8.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p25 = root.addChild("cannon_r3", ModelPartBuilder.create().uv(144, 210).cuboid(14.0f, -8.0f, 8.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p26 = root.addChild("cannon_l4", ModelPartBuilder.create().uv(156, 210).cuboid(-16.0f, -8.0f, 23.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p27 = root.addChild("cannon_r4", ModelPartBuilder.create().uv(168, 210).cuboid(14.0f, -8.0f, 23.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        return TexturedModelData.of(data, 512, 256);
    }

    public static TexturedModelData shipFrigate() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("keel", ModelPartBuilder.create().uv(0, 110).cuboid(-13.0f, 0.0f, -40.0f, 26.0f, 3.0f, 80.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("side_left", ModelPartBuilder.create().uv(8, 0).cuboid(-15.0f, -14.0f, -40.0f, 2.0f, 17.0f, 80.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("side_right", ModelPartBuilder.create().uv(172, 0).cuboid(13.0f, -14.0f, -40.0f, 2.0f, 17.0f, 80.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("stern", ModelPartBuilder.create().uv(308, 275).cuboid(-15.0f, -14.0f, -42.0f, 30.0f, 17.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p4 = root.addChild("bow", ModelPartBuilder.create().uv(216, 275).cuboid(-11.0f, -14.0f, 40.0f, 22.0f, 17.0f, 6.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p5 = root.addChild("bow_tip", ModelPartBuilder.create().uv(272, 275).cuboid(-6.5f, -13.0f, 46.0f, 13.0f, 15.0f, 5.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p6 = root.addChild("deck", ModelPartBuilder.create().uv(164, 193).cuboid(-13.0f, -4.0f, -40.0f, 26.0f, 1.0f, 80.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p7 = root.addChild("rail_left", ModelPartBuilder.create().uv(212, 110).cuboid(-15.0f, -16.0f, -40.0f, 2.0f, 2.0f, 80.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p8 = root.addChild("rail_right", ModelPartBuilder.create().uv(0, 193).cuboid(13.0f, -16.0f, -40.0f, 2.0f, 2.0f, 80.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p9 = root.addChild("cabin", ModelPartBuilder.create().uv(372, 275).cuboid(-12.0f, -21.0f, -40.0f, 24.0f, 8.0f, 10.0f).uv(0, 315).cuboid(-13.0f, -22.0f, -41.0f, 26.0f, 1.0f, 12.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p10 = root.addChild("mast1", ModelPartBuilder.create().uv(336, 0).cuboid(-1.0f, -96.0f, -21.0f, 2.0f, 92.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p11 = root.addChild("yard1", ModelPartBuilder.create().uv(396, 315).cuboid(-20.0f, -84.0f, -22.0f, 40.0f, 2.0f, 2.0f).uv(84, 328).cuboid(-18.0f, -51.0f, -22.0f, 36.0f, 2.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p12 = root.addChild("sail1", ModelPartBuilder.create().uv(0, 275).cuboid(-19.0f, 0.0f, -19.0f, 38.0f, 39.0f, 1.0f), ModelTransform.pivot(0.0f, -82.0f, 0.0f));
        ModelPartData p13 = root.addChild("mast2", ModelPartBuilder.create().uv(0, 0).cuboid(-1.0f, -112.0f, 3.0f, 2.0f, 108.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p14 = root.addChild("yard2", ModelPartBuilder.create().uv(208, 315).cuboid(-23.5f, -100.0f, 2.0f, 47.0f, 2.0f, 2.0f).uv(306, 315).cuboid(-21.5f, -59.0f, 2.0f, 43.0f, 2.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p15 = root.addChild("sail2", ModelPartBuilder.create().uv(376, 193).cuboid(-22.5f, 0.0f, 5.0f, 45.0f, 47.0f, 1.0f), ModelTransform.pivot(0.0f, -98.0f, 0.0f));
        ModelPartData p16 = root.addChild("mast3", ModelPartBuilder.create().uv(344, 0).cuboid(-1.0f, -96.0f, 24.0f, 2.0f, 92.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p17 = root.addChild("yard3", ModelPartBuilder.create().uv(0, 328).cuboid(-20.0f, -84.0f, 23.0f, 40.0f, 2.0f, 2.0f).uv(160, 328).cuboid(-18.0f, -51.0f, 23.0f, 36.0f, 2.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p18 = root.addChild("sail3", ModelPartBuilder.create().uv(78, 275).cuboid(-19.0f, 0.0f, 26.0f, 38.0f, 39.0f, 1.0f), ModelTransform.pivot(0.0f, -82.0f, 0.0f));
        ModelPartData p19 = root.addChild("bowsprit", ModelPartBuilder.create().uv(156, 275).cuboid(-1.0f, -16.0f, 48.0f, 2.0f, 2.0f, 28.0f), ModelTransform.of(0.0f, 0.0f, 0.0f, -0.244f, 0.0f, 0.0f));
        ModelPartData p20 = root.addChild("figurehead", ModelPartBuilder.create().uv(76, 315).cuboid(-1.5f, -12.0f, 50.0f, 3.0f, 6.0f, 3.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p21 = root.addChild("flag", ModelPartBuilder.create().uv(440, 275).cuboid(0.0f, -112.0f, -7.0f, 1.0f, 6.0f, 10.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p22 = root.addChild("emblem", ModelPartBuilder.create().uv(462, 275).cuboid(0.0f, 0.0f, -5.0f, 1.0f, 6.0f, 10.0f), ModelTransform.of(0.0f, -81.0f, 6.6f, 0.0f, 1.571f, 0.0f));
        ModelPartData p23 = root.addChild("cannon_l1", ModelPartBuilder.create().uv(88, 315).cuboid(-17.0f, -10.0f, -28.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p24 = root.addChild("cannon_r1", ModelPartBuilder.create().uv(100, 315).cuboid(15.0f, -10.0f, -28.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p25 = root.addChild("cannon_l2", ModelPartBuilder.create().uv(112, 315).cuboid(-17.0f, -10.0f, -14.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p26 = root.addChild("cannon_r2", ModelPartBuilder.create().uv(124, 315).cuboid(15.0f, -10.0f, -14.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p27 = root.addChild("cannon_l3", ModelPartBuilder.create().uv(136, 315).cuboid(-17.0f, -10.0f, 0.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p28 = root.addChild("cannon_r3", ModelPartBuilder.create().uv(148, 315).cuboid(15.0f, -10.0f, 0.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p29 = root.addChild("cannon_l4", ModelPartBuilder.create().uv(160, 315).cuboid(-17.0f, -10.0f, 14.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p30 = root.addChild("cannon_r4", ModelPartBuilder.create().uv(172, 315).cuboid(15.0f, -10.0f, 14.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p31 = root.addChild("cannon_l5", ModelPartBuilder.create().uv(184, 315).cuboid(-17.0f, -10.0f, 28.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p32 = root.addChild("cannon_r5", ModelPartBuilder.create().uv(196, 315).cuboid(15.0f, -10.0f, 28.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        return TexturedModelData.of(data, 512, 512);
    }

    public static TexturedModelData shipGalleon() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("keel", ModelPartBuilder.create().uv(0, 115).cuboid(-15.0f, 0.0f, -42.0f, 30.0f, 3.0f, 84.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("side_left", ModelPartBuilder.create().uv(8, 0).cuboid(-17.0f, -17.0f, -42.0f, 2.0f, 20.0f, 84.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("side_right", ModelPartBuilder.create().uv(180, 0).cuboid(15.0f, -17.0f, -42.0f, 2.0f, 20.0f, 84.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("stern", ModelPartBuilder.create().uv(104, 330).cuboid(-17.0f, -17.0f, -44.0f, 34.0f, 20.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p4 = root.addChild("bow", ModelPartBuilder.create().uv(0, 330).cuboid(-13.0f, -17.0f, 42.0f, 26.0f, 20.0f, 6.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p5 = root.addChild("bow_tip", ModelPartBuilder.create().uv(64, 330).cuboid(-7.5f, -16.0f, 48.0f, 15.0f, 18.0f, 5.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p6 = root.addChild("deck", ModelPartBuilder.create().uv(172, 202).cuboid(-15.0f, -4.0f, -42.0f, 30.0f, 1.0f, 84.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p7 = root.addChild("rail_left", ModelPartBuilder.create().uv(228, 115).cuboid(-17.0f, -19.0f, -42.0f, 2.0f, 2.0f, 84.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p8 = root.addChild("rail_right", ModelPartBuilder.create().uv(0, 202).cuboid(15.0f, -19.0f, -42.0f, 2.0f, 2.0f, 84.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p9 = root.addChild("cabin", ModelPartBuilder.create().uv(180, 288).cuboid(-14.0f, -30.0f, -42.0f, 28.0f, 14.0f, 24.0f).uv(346, 288).cuboid(-15.0f, -31.0f, -43.0f, 30.0f, 1.0f, 26.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p10 = root.addChild("forecastle", ModelPartBuilder.create().uv(176, 330).cuboid(-13.0f, -23.0f, 30.0f, 26.0f, 6.0f, 12.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p11 = root.addChild("mast1", ModelPartBuilder.create().uv(352, 0).cuboid(-1.0f, -100.0f, -22.0f, 2.0f, 96.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p12 = root.addChild("yard1", ModelPartBuilder.create().uv(216, 356).cuboid(-23.0f, -88.0f, -23.0f, 46.0f, 2.0f, 2.0f).uv(408, 356).cuboid(-21.0f, -53.0f, -23.0f, 42.0f, 2.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p13 = root.addChild("sail1", ModelPartBuilder.create().uv(0, 288).cuboid(-22.0f, 0.0f, -20.0f, 44.0f, 41.0f, 1.0f), ModelTransform.pivot(0.0f, -86.0f, 0.0f));
        ModelPartData p14 = root.addChild("mast2", ModelPartBuilder.create().uv(0, 0).cuboid(-1.0f, -117.0f, 3.0f, 2.0f, 113.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p15 = root.addChild("yard2", ModelPartBuilder.create().uv(0, 356).cuboid(-27.0f, -105.0f, 2.0f, 54.0f, 2.0f, 2.0f).uv(112, 356).cuboid(-25.0f, -62.0f, 2.0f, 50.0f, 2.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p16 = root.addChild("sail2", ModelPartBuilder.create().uv(400, 202).cuboid(-26.0f, 0.0f, 5.0f, 52.0f, 50.0f, 1.0f), ModelTransform.pivot(0.0f, -103.0f, 0.0f));
        ModelPartData p17 = root.addChild("mast3", ModelPartBuilder.create().uv(360, 0).cuboid(-1.0f, -100.0f, 25.0f, 2.0f, 96.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p18 = root.addChild("yard3", ModelPartBuilder.create().uv(312, 356).cuboid(-23.0f, -88.0f, 24.0f, 46.0f, 2.0f, 2.0f).uv(0, 360).cuboid(-21.0f, -53.0f, 24.0f, 42.0f, 2.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p19 = root.addChild("sail3", ModelPartBuilder.create().uv(90, 288).cuboid(-22.0f, 0.0f, 27.0f, 44.0f, 41.0f, 1.0f), ModelTransform.pivot(0.0f, -86.0f, 0.0f));
        ModelPartData p20 = root.addChild("bowsprit", ModelPartBuilder.create().uv(284, 288).cuboid(-1.0f, -19.0f, 50.0f, 2.0f, 2.0f, 29.0f), ModelTransform.of(0.0f, 0.0f, 0.0f, -0.244f, 0.0f, 0.0f));
        ModelPartData p21 = root.addChild("figurehead", ModelPartBuilder.create().uv(296, 330).cuboid(-1.5f, -15.0f, 52.0f, 3.0f, 6.0f, 3.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p22 = root.addChild("flag", ModelPartBuilder.create().uv(252, 330).cuboid(0.0f, -117.0f, -7.0f, 1.0f, 6.0f, 10.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p23 = root.addChild("emblem", ModelPartBuilder.create().uv(274, 330).cuboid(0.0f, 0.0f, -5.0f, 1.0f, 6.0f, 10.0f), ModelTransform.of(0.0f, -85.0f, 6.6f, 0.0f, 1.571f, 0.0f));
        ModelPartData p24 = root.addChild("cannon_l1", ModelPartBuilder.create().uv(308, 330).cuboid(-19.0f, -13.0f, -30.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p25 = root.addChild("cannon_r1", ModelPartBuilder.create().uv(320, 330).cuboid(17.0f, -13.0f, -30.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p26 = root.addChild("cannon_l2", ModelPartBuilder.create().uv(332, 330).cuboid(-19.0f, -13.0f, -10.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p27 = root.addChild("cannon_r2", ModelPartBuilder.create().uv(344, 330).cuboid(17.0f, -13.0f, -10.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p28 = root.addChild("cannon_l3", ModelPartBuilder.create().uv(356, 330).cuboid(-19.0f, -13.0f, 10.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p29 = root.addChild("cannon_r3", ModelPartBuilder.create().uv(368, 330).cuboid(17.0f, -13.0f, 10.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p30 = root.addChild("cannon_l4", ModelPartBuilder.create().uv(380, 330).cuboid(-19.0f, -13.0f, 30.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p31 = root.addChild("cannon_r4", ModelPartBuilder.create().uv(392, 330).cuboid(17.0f, -13.0f, 30.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p32 = root.addChild("cannon_lb1", ModelPartBuilder.create().uv(404, 330).cuboid(-19.0f, -8.0f, -30.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p33 = root.addChild("cannon_rb1", ModelPartBuilder.create().uv(416, 330).cuboid(17.0f, -8.0f, -30.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p34 = root.addChild("cannon_lb2", ModelPartBuilder.create().uv(428, 330).cuboid(-19.0f, -8.0f, -10.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p35 = root.addChild("cannon_rb2", ModelPartBuilder.create().uv(440, 330).cuboid(17.0f, -8.0f, -10.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p36 = root.addChild("cannon_lb3", ModelPartBuilder.create().uv(452, 330).cuboid(-19.0f, -8.0f, 10.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p37 = root.addChild("cannon_rb3", ModelPartBuilder.create().uv(464, 330).cuboid(17.0f, -8.0f, 10.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p38 = root.addChild("cannon_lb4", ModelPartBuilder.create().uv(476, 330).cuboid(-19.0f, -8.0f, 30.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p39 = root.addChild("cannon_rb4", ModelPartBuilder.create().uv(488, 330).cuboid(17.0f, -8.0f, 30.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        return TexturedModelData.of(data, 512, 512);
    }

    public static TexturedModelData shipShipOfTheLine() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData p0 = root.addChild("keel", ModelPartBuilder.create().uv(0, 132).cuboid(-16.0f, 0.0f, -48.0f, 32.0f, 3.0f, 96.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p1 = root.addChild("side_left", ModelPartBuilder.create().uv(8, 0).cuboid(-18.0f, -21.0f, -48.0f, 2.0f, 24.0f, 96.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p2 = root.addChild("side_right", ModelPartBuilder.create().uv(204, 0).cuboid(16.0f, -21.0f, -48.0f, 2.0f, 24.0f, 96.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p3 = root.addChild("stern", ModelPartBuilder.create().uv(232, 390).cuboid(-18.0f, -21.0f, -50.0f, 36.0f, 24.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p4 = root.addChild("bow", ModelPartBuilder.create().uv(122, 390).cuboid(-14.0f, -21.0f, 48.0f, 28.0f, 24.0f, 6.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p5 = root.addChild("bow_tip", ModelPartBuilder.create().uv(190, 390).cuboid(-8.0f, -20.0f, 54.0f, 16.0f, 22.0f, 5.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p6 = root.addChild("deck", ModelPartBuilder.create().uv(196, 231).cuboid(-16.0f, -4.0f, -48.0f, 32.0f, 1.0f, 96.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p7 = root.addChild("rail_left", ModelPartBuilder.create().uv(256, 132).cuboid(-18.0f, -23.0f, -48.0f, 2.0f, 2.0f, 96.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p8 = root.addChild("rail_right", ModelPartBuilder.create().uv(0, 231).cuboid(16.0f, -23.0f, -48.0f, 2.0f, 2.0f, 96.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p9 = root.addChild("cabin", ModelPartBuilder.create().uv(306, 329).cuboid(-15.0f, -34.0f, -48.0f, 30.0f, 14.0f, 27.0f).uv(0, 390).cuboid(-16.0f, -35.0f, -49.0f, 32.0f, 1.0f, 29.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p10 = root.addChild("forecastle", ModelPartBuilder.create().uv(308, 390).cuboid(-14.0f, -27.0f, 36.0f, 28.0f, 6.0f, 12.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p11 = root.addChild("mast1", ModelPartBuilder.create().uv(400, 0).cuboid(-1.0f, -115.0f, -25.0f, 2.0f, 111.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p12 = root.addChild("yard1", ModelPartBuilder.create().uv(0, 426).cuboid(-24.5f, -103.0f, -26.0f, 49.0f, 2.0f, 2.0f).uv(204, 426).cuboid(-22.5f, -61.0f, -26.0f, 45.0f, 2.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p13 = root.addChild("sail1", ModelPartBuilder.create().uv(114, 329).cuboid(-23.5f, 0.0f, -23.0f, 47.0f, 49.0f, 1.0f), ModelTransform.pivot(0.0f, -101.0f, 0.0f));
        ModelPartData p14 = root.addChild("mast2", ModelPartBuilder.create().uv(0, 0).cuboid(-1.0f, -134.0f, 4.0f, 2.0f, 130.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p15 = root.addChild("yard2", ModelPartBuilder.create().uv(276, 420).cuboid(-29.0f, -122.0f, 3.0f, 58.0f, 2.0f, 2.0f).uv(396, 420).cuboid(-27.0f, -72.0f, 3.0f, 54.0f, 2.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p16 = root.addChild("sail2", ModelPartBuilder.create().uv(0, 329).cuboid(-28.0f, 0.0f, 6.0f, 56.0f, 60.0f, 1.0f), ModelTransform.pivot(0.0f, -120.0f, 0.0f));
        ModelPartData p17 = root.addChild("mast3", ModelPartBuilder.create().uv(408, 0).cuboid(-1.0f, -115.0f, 29.0f, 2.0f, 111.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p18 = root.addChild("yard3", ModelPartBuilder.create().uv(102, 426).cuboid(-24.5f, -103.0f, 28.0f, 49.0f, 2.0f, 2.0f).uv(298, 426).cuboid(-22.5f, -61.0f, 28.0f, 45.0f, 2.0f, 2.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p19 = root.addChild("sail3", ModelPartBuilder.create().uv(210, 329).cuboid(-23.5f, 0.0f, 31.0f, 47.0f, 49.0f, 1.0f), ModelTransform.pivot(0.0f, -101.0f, 0.0f));
        ModelPartData p20 = root.addChild("bowsprit", ModelPartBuilder.create().uv(420, 329).cuboid(-1.0f, -23.0f, 56.0f, 2.0f, 2.0f, 34.0f), ModelTransform.of(0.0f, 0.0f, 0.0f, -0.244f, 0.0f, 0.0f));
        ModelPartData p21 = root.addChild("figurehead", ModelPartBuilder.create().uv(432, 390).cuboid(-1.5f, -19.0f, 58.0f, 3.0f, 6.0f, 3.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p22 = root.addChild("flag", ModelPartBuilder.create().uv(388, 390).cuboid(0.0f, -134.0f, -6.0f, 1.0f, 6.0f, 10.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p23 = root.addChild("emblem", ModelPartBuilder.create().uv(410, 390).cuboid(0.0f, 0.0f, -5.0f, 1.0f, 6.0f, 10.0f), ModelTransform.of(0.0f, -98.0f, 7.6f, 0.0f, 1.571f, 0.0f));
        ModelPartData p24 = root.addChild("cannon_l1", ModelPartBuilder.create().uv(444, 390).cuboid(-20.0f, -17.0f, -36.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p25 = root.addChild("cannon_r1", ModelPartBuilder.create().uv(456, 390).cuboid(18.0f, -17.0f, -36.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p26 = root.addChild("cannon_l2", ModelPartBuilder.create().uv(468, 390).cuboid(-20.0f, -17.0f, -24.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p27 = root.addChild("cannon_r2", ModelPartBuilder.create().uv(480, 390).cuboid(18.0f, -17.0f, -24.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p28 = root.addChild("cannon_l3", ModelPartBuilder.create().uv(492, 390).cuboid(-20.0f, -17.0f, -12.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p29 = root.addChild("cannon_r3", ModelPartBuilder.create().uv(0, 420).cuboid(18.0f, -17.0f, -12.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p30 = root.addChild("cannon_l4", ModelPartBuilder.create().uv(12, 420).cuboid(-20.0f, -17.0f, 0.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p31 = root.addChild("cannon_r4", ModelPartBuilder.create().uv(24, 420).cuboid(18.0f, -17.0f, 0.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p32 = root.addChild("cannon_l5", ModelPartBuilder.create().uv(36, 420).cuboid(-20.0f, -17.0f, 12.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p33 = root.addChild("cannon_r5", ModelPartBuilder.create().uv(48, 420).cuboid(18.0f, -17.0f, 12.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p34 = root.addChild("cannon_l6", ModelPartBuilder.create().uv(60, 420).cuboid(-20.0f, -17.0f, 24.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p35 = root.addChild("cannon_r6", ModelPartBuilder.create().uv(72, 420).cuboid(18.0f, -17.0f, 24.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p36 = root.addChild("cannon_l7", ModelPartBuilder.create().uv(84, 420).cuboid(-20.0f, -17.0f, 36.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p37 = root.addChild("cannon_r7", ModelPartBuilder.create().uv(96, 420).cuboid(18.0f, -17.0f, 36.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p38 = root.addChild("cannon_lb1", ModelPartBuilder.create().uv(108, 420).cuboid(-20.0f, -12.0f, -36.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p39 = root.addChild("cannon_rb1", ModelPartBuilder.create().uv(120, 420).cuboid(18.0f, -12.0f, -36.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p40 = root.addChild("cannon_lb2", ModelPartBuilder.create().uv(132, 420).cuboid(-20.0f, -12.0f, -24.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p41 = root.addChild("cannon_rb2", ModelPartBuilder.create().uv(144, 420).cuboid(18.0f, -12.0f, -24.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p42 = root.addChild("cannon_lb3", ModelPartBuilder.create().uv(156, 420).cuboid(-20.0f, -12.0f, -12.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p43 = root.addChild("cannon_rb3", ModelPartBuilder.create().uv(168, 420).cuboid(18.0f, -12.0f, -12.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p44 = root.addChild("cannon_lb4", ModelPartBuilder.create().uv(180, 420).cuboid(-20.0f, -12.0f, 0.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p45 = root.addChild("cannon_rb4", ModelPartBuilder.create().uv(192, 420).cuboid(18.0f, -12.0f, 0.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p46 = root.addChild("cannon_lb5", ModelPartBuilder.create().uv(204, 420).cuboid(-20.0f, -12.0f, 12.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p47 = root.addChild("cannon_rb5", ModelPartBuilder.create().uv(216, 420).cuboid(18.0f, -12.0f, 12.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p48 = root.addChild("cannon_lb6", ModelPartBuilder.create().uv(228, 420).cuboid(-20.0f, -12.0f, 24.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p49 = root.addChild("cannon_rb6", ModelPartBuilder.create().uv(240, 420).cuboid(18.0f, -12.0f, 24.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p50 = root.addChild("cannon_lb7", ModelPartBuilder.create().uv(252, 420).cuboid(-20.0f, -12.0f, 36.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData p51 = root.addChild("cannon_rb7", ModelPartBuilder.create().uv(264, 420).cuboid(18.0f, -12.0f, 36.0f, 2.0f, 2.0f, 4.0f), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        return TexturedModelData.of(data, 512, 512);
    }
}
