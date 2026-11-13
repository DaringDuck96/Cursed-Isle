package com.cursedtides.client;

import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Models from the Cursed Tides Mob Workshop (its starting designs), generated; textures are in
 * textures/entity/workshop/. Regenerate rather than editing by hand.
 */
public final class WorkshopModels {
    private WorkshopModels() {}

    public static net.minecraft.client.model.geom.ModelLayerLocation layer(String id) {
        return new net.minecraft.client.model.geom.ModelLayerLocation(com.cursedtides.CursedTides.id("workshop_" + id), "main");
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

    public static LayerDefinition keeperWren() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(28, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(12, 33).addBox(-4.3f, -8.3f, 0.5f, 8.6f, 10.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 17).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 0).addBox(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).texOffs(38, 33).addBox(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 17).addBox(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(36, 17).addBox(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(48, 17).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 33).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition ysolde() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(28, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 48).addBox(-6.0f, -9.0f, -6.0f, 12.0f, 1.0f, 12.0f).texOffs(0, 61).addBox(-4.5f, -11.5f, -4.5f, 9.0f, 3.0f, 9.0f).texOffs(0, 73).addBox(-5.5f, -10.5f, -6.5f, 11.0f, 2.0f, 1.0f).texOffs(12, 33).addBox(-4.3f, -8.3f, 0.5f, 8.6f, 10.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 17).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 0).addBox(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).texOffs(36, 61).addBox(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 17).addBox(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(36, 17).addBox(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(48, 17).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 33).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 128);
    }

    public static LayerDefinition tamsin() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 31).addBox(-4.5f, -8.6f, -4.5f, 9.0f, 3.0f, 9.0f).texOffs(36, 31).addBox(-1.0f, -6.5f, 4.4f, 2.0f, 3.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(48, 16).addBox(-3.5f, 3.0f, -2.6f, 7.0f, 13.0f, 0.6f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(12, 16).addBox(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(24, 16).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(36, 16).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition vey() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(28, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f).texOffs(0, 67).addBox(-4.0f, -2.0f, -4.6f, 8.0f, 3.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 59).addBox(-7.0f, -12.0f, -2.0f, 14.0f, 4.0f, 4.0f).texOffs(0, 49).addBox(-4.5f, -9.0f, -4.5f, 9.0f, 1.0f, 9.0f).texOffs(18, 67).addBox(-1.0f, -13.0f, -2.5f, 2.0f, 2.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 17).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 0).addBox(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).texOffs(36, 59).addBox(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 17).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 17).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 33).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 33).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 128);
    }

    public static LayerDefinition galloway() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(28, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 48).addBox(-7.0f, -12.0f, -2.0f, 14.0f, 4.0f, 4.0f).texOffs(12, 33).addBox(-4.5f, -9.0f, -4.5f, 9.0f, 1.0f, 9.0f).texOffs(58, 48).addBox(-1.0f, -13.0f, -2.5f, 2.0f, 2.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 17).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 0).addBox(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).texOffs(36, 48).addBox(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 17).addBox(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(36, 17).addBox(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(48, 17).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 33).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition harrow() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 18).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 49).addBox(-4.3f, -8.3f, 0.5f, 8.6f, 10.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 18).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 0).addBox(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).texOffs(32, 0).addBox(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 34).addBox(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(12, 34).addBox(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(24, 34).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(36, 34).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition rook() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(28, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f).texOffs(0, 74).addBox(-4.0f, -2.0f, -4.6f, 8.0f, 3.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 49).addBox(-6.0f, -9.0f, -6.0f, 12.0f, 1.0f, 12.0f).texOffs(0, 62).addBox(-4.5f, -11.5f, -4.5f, 9.0f, 3.0f, 9.0f).texOffs(18, 74).addBox(-5.5f, -10.5f, -6.5f, 11.0f, 2.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 17).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 0).addBox(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).texOffs(36, 62).addBox(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 17).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 17).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 33).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 33).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 128);
    }

    public static LayerDefinition thwaite() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(28, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f).texOffs(0, 49).addBox(-4.0f, -2.0f, -4.6f, 8.0f, 3.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 17).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 0).addBox(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).texOffs(32, 33).addBox(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 17).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 17).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 33).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 33).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0f, 0.0f, 0.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition wayfarer() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 38).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 0).addBox(-4.6f, -8.6f, -4.6f, 9.2f, 9.2f, 9.2f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 38).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 20).addBox(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).texOffs(32, 20).addBox(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 54).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(16, 54).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(32, 54).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(48, 54).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 128);
    }

    public static LayerDefinition quill() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 18).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 49).addBox(-4.3f, -8.3f, 0.5f, 8.6f, 10.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 18).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 0).addBox(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).texOffs(32, 0).addBox(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 34).addBox(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(12, 34).addBox(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(24, 34).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(36, 34).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition isadora() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(28, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 48).addBox(-6.0f, -9.0f, -6.0f, 12.0f, 1.0f, 12.0f).texOffs(0, 61).addBox(-4.5f, -11.5f, -4.5f, 9.0f, 3.0f, 9.0f).texOffs(0, 73).addBox(-5.5f, -10.5f, -6.5f, 11.0f, 2.0f, 1.0f).texOffs(12, 33).addBox(-4.3f, -8.3f, 0.5f, 8.6f, 10.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 17).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 0).addBox(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).texOffs(36, 61).addBox(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 17).addBox(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(36, 17).addBox(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(48, 17).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 33).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 128);
    }

    public static LayerDefinition celestine() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 38).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 0).addBox(-4.6f, -8.6f, -4.6f, 9.2f, 9.2f, 9.2f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 38).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 20).addBox(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).texOffs(32, 20).addBox(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 54).addBox(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(12, 54).addBox(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(24, 54).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(36, 54).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 128);
    }

    public static LayerDefinition sorrel() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 38).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 0).addBox(-4.6f, -8.6f, -4.6f, 9.2f, 9.2f, 9.2f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 38).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 20).addBox(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).texOffs(32, 20).addBox(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 54).addBox(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(12, 54).addBox(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(24, 54).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(36, 54).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 128);
    }

    public static LayerDefinition ibbie() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(28, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(12, 33).addBox(-4.5f, -9.5f, -4.5f, 9.0f, 2.0f, 9.0f).texOffs(22, 48).addBox(-4.5f, -8.0f, -6.5f, 9.0f, 1.0f, 3.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 17).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 0).addBox(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).texOffs(0, 48).addBox(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 17).addBox(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(36, 17).addBox(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(48, 17).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 33).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition dagny() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 31).addBox(-4.5f, -9.5f, -4.5f, 9.0f, 2.0f, 9.0f).texOffs(36, 31).addBox(-4.5f, -8.0f, -6.5f, 9.0f, 1.0f, 3.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(48, 16).addBox(-3.5f, 3.0f, -2.6f, 7.0f, 13.0f, 0.6f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(12, 16).addBox(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(24, 16).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(36, 16).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition oldMariner() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f).texOffs(36, 32).addBox(-4.0f, -2.0f, -4.6f, 8.0f, 3.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 32).addBox(-4.5f, -8.6f, -4.5f, 9.0f, 3.0f, 9.0f).texOffs(54, 32).addBox(-1.0f, -6.5f, 4.4f, 2.0f, 3.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 16).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(16, 16).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(32, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(48, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition mags() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 31).addBox(-4.5f, -8.6f, -4.5f, 9.0f, 3.0f, 9.0f).texOffs(36, 31).addBox(-1.0f, -6.5f, 4.4f, 2.0f, 3.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(12, 16).addBox(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(24, 16).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(36, 16).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition jory() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(28, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 17).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 0).addBox(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).texOffs(32, 33).addBox(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 17).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 17).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 33).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 33).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0f, 0.0f, 0.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition dealer() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(28, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 17).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 0).addBox(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).texOffs(32, 33).addBox(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 17).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 17).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 33).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 33).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0f, 0.0f, 0.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition fairweather() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(28, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f).texOffs(24, 60).addBox(-4.0f, -2.0f, -4.6f, 8.0f, 3.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 49).addBox(-4.5f, -9.5f, -4.5f, 9.0f, 2.0f, 9.0f).texOffs(0, 60).addBox(-4.5f, -8.0f, -6.5f, 9.0f, 1.0f, 3.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 17).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 0).addBox(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).texOffs(36, 49).addBox(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 17).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 17).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 33).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 33).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition pip() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 32).addBox(-4.5f, -9.5f, -4.5f, 9.0f, 2.0f, 9.0f).texOffs(36, 32).addBox(-4.5f, -8.0f, -6.5f, 9.0f, 1.0f, 3.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 16).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(16, 16).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(32, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(48, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition lott() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(28, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 17).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 0).addBox(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).texOffs(32, 33).addBox(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 17).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 17).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 33).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 33).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0f, 0.0f, 0.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition finch() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(28, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 49).addBox(-4.5f, -9.5f, -4.5f, 9.0f, 2.0f, 9.0f).texOffs(0, 60).addBox(-4.5f, -8.0f, -6.5f, 9.0f, 1.0f, 3.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 17).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 0).addBox(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).texOffs(36, 49).addBox(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 17).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 17).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 33).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 33).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition grista() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 35).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 35).addBox(-4.3f, -8.3f, 0.5f, 8.6f, 10.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0f, 0.0f, -3.0f, 10.0f, 12.0f, 6.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0f, -2.0f, -2.5f, 5.0f, 12.0f, 5.0f), PartPose.offset(-6.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 18).addBox(-1.0f, -2.0f, -2.5f, 5.0f, 12.0f, 5.0f), PartPose.offset(6.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(20, 18).addBox(-2.5f, 0.0f, -2.5f, 5.0f, 12.0f, 5.0f), PartPose.offset(-2.4f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(40, 18).addBox(-2.5f, 0.0f, -2.5f, 5.0f, 12.0f, 5.0f), PartPose.offset(2.4f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition nedFlint() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(28, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 49).addBox(-6.0f, -9.0f, -6.0f, 12.0f, 1.0f, 12.0f).texOffs(0, 62).addBox(-4.5f, -11.5f, -4.5f, 9.0f, 3.0f, 9.0f).texOffs(0, 74).addBox(-5.5f, -10.5f, -6.5f, 11.0f, 2.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 17).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 0).addBox(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).texOffs(36, 62).addBox(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 17).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 17).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 33).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 33).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 128);
    }

    public static LayerDefinition bess() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 18).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 49).addBox(-4.3f, -8.3f, 0.5f, 8.6f, 10.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 18).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 0).addBox(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).texOffs(32, 0).addBox(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f).texOffs(26, 49).addBox(-3.5f, 3.0f, -2.6f, 7.0f, 13.0f, 0.6f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 34).addBox(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(12, 34).addBox(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(24, 34).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(36, 34).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition skip() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 32).addBox(-4.5f, -8.6f, -4.5f, 9.0f, 3.0f, 9.0f).texOffs(36, 32).addBox(-1.0f, -6.5f, 4.4f, 2.0f, 3.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 16).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(16, 16).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(32, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(48, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition deedes() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(28, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 17).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 0).addBox(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).texOffs(32, 33).addBox(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 17).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 17).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 33).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 33).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0f, 0.0f, 0.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition marsh() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(28, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 49).addBox(-4.5f, -9.5f, -4.5f, 9.0f, 2.0f, 9.0f).texOffs(0, 60).addBox(-4.5f, -8.0f, -6.5f, 9.0f, 1.0f, 3.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 17).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 0).addBox(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).texOffs(36, 49).addBox(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 17).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 17).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 33).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 33).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition sounding() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f).texOffs(0, 43).addBox(-4.0f, -2.0f, -4.6f, 8.0f, 3.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 32).addBox(-4.5f, -9.5f, -4.5f, 9.0f, 2.0f, 9.0f).texOffs(36, 32).addBox(-4.5f, -8.0f, -6.5f, 9.0f, 1.0f, 3.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 16).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(16, 16).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(32, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(48, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition kettle() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(28, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(12, 33).addBox(-6.0f, -9.0f, -6.0f, 12.0f, 1.0f, 12.0f).texOffs(0, 48).addBox(-4.5f, -11.5f, -4.5f, 9.0f, 3.0f, 9.0f).texOffs(0, 60).addBox(-5.5f, -10.5f, -6.5f, 11.0f, 2.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 17).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 0).addBox(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).texOffs(36, 48).addBox(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 17).addBox(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(36, 17).addBox(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(48, 17).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 33).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition flossie() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 18).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 49).addBox(-4.3f, -8.3f, 0.5f, 8.6f, 10.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 18).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 0).addBox(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).texOffs(32, 0).addBox(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f).texOffs(26, 49).addBox(-3.5f, 3.0f, -2.6f, 7.0f, 13.0f, 0.6f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 34).addBox(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(12, 34).addBox(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(24, 34).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(36, 34).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition moss() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 32).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(16, 32).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 48).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0f, 0.0f, 0.0f));
        return LayerDefinition.create(data, 32, 64);
    }

    public static LayerDefinition quartermaster() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f).texOffs(0, 46).addBox(-4.0f, -2.0f, -4.6f, 8.0f, 3.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(16, 32).addBox(-4.5f, -8.6f, -4.5f, 9.0f, 3.0f, 9.0f).texOffs(18, 46).addBox(-1.0f, -6.5f, 4.4f, 2.0f, 3.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 32).addBox(-3.5f, 3.0f, -2.6f, 7.0f, 13.0f, 0.6f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 16).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(16, 16).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(32, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(48, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition shipwright() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(16, 32).addBox(-4.5f, -9.5f, -4.5f, 9.0f, 2.0f, 9.0f).texOffs(0, 46).addBox(-4.5f, -8.0f, -6.5f, 9.0f, 1.0f, 3.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 32).addBox(-3.5f, 3.0f, -2.6f, 7.0f, 13.0f, 0.6f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 16).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(16, 16).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(32, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(48, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition marine() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(28, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 49).addBox(-6.0f, -9.0f, -6.0f, 12.0f, 1.0f, 12.0f).texOffs(0, 62).addBox(-4.5f, -11.5f, -4.5f, 9.0f, 3.0f, 9.0f).texOffs(0, 74).addBox(-5.5f, -10.5f, -6.5f, 11.0f, 2.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 17).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 0).addBox(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).texOffs(36, 62).addBox(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 17).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 17).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 33).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 33).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 128);
    }

    public static LayerDefinition crewGunner() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 32).addBox(-4.5f, -8.6f, -4.5f, 9.0f, 3.0f, 9.0f).texOffs(36, 32).addBox(-1.0f, -6.5f, 4.4f, 2.0f, 3.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 16).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(16, 16).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(32, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(48, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition crewBosun() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 35).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f).texOffs(0, 62).addBox(-4.0f, -2.0f, -4.6f, 8.0f, 3.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 51).addBox(-4.5f, -9.5f, -4.5f, 9.0f, 2.0f, 9.0f).texOffs(36, 51).addBox(-4.5f, -8.0f, -6.5f, 9.0f, 1.0f, 3.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0f, 0.0f, -3.0f, 10.0f, 12.0f, 6.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0f, -2.0f, -2.5f, 5.0f, 12.0f, 5.0f), PartPose.offset(-6.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 18).addBox(-1.0f, -2.0f, -2.5f, 5.0f, 12.0f, 5.0f), PartPose.offset(6.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(20, 18).addBox(-2.5f, 0.0f, -2.5f, 5.0f, 12.0f, 5.0f), PartPose.offset(-2.4f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(40, 18).addBox(-2.5f, 0.0f, -2.5f, 5.0f, 12.0f, 5.0f), PartPose.offset(2.4f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 128);
    }

    public static LayerDefinition crewNavigator() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(28, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 49).addBox(-6.0f, -9.0f, -6.0f, 12.0f, 1.0f, 12.0f).texOffs(0, 62).addBox(-4.5f, -11.5f, -4.5f, 9.0f, 3.0f, 9.0f).texOffs(0, 74).addBox(-5.5f, -10.5f, -6.5f, 11.0f, 2.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 17).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 0).addBox(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).texOffs(36, 62).addBox(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 17).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 17).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 33).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 33).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 128);
    }

    public static LayerDefinition bountyHunter() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(28, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f).texOffs(0, 74).addBox(-4.0f, -2.0f, -4.6f, 8.0f, 3.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 49).addBox(-6.0f, -9.0f, -6.0f, 12.0f, 1.0f, 12.0f).texOffs(0, 62).addBox(-4.5f, -11.5f, -4.5f, 9.0f, 3.0f, 9.0f).texOffs(18, 74).addBox(-5.5f, -10.5f, -6.5f, 11.0f, 2.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 17).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 0).addBox(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).texOffs(36, 62).addBox(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 17).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 17).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 33).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 33).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 128);
    }

    public static LayerDefinition thorne() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(28, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f).texOffs(0, 74).addBox(-4.0f, -2.0f, -4.6f, 8.0f, 3.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 49).addBox(-6.0f, -9.0f, -6.0f, 12.0f, 1.0f, 12.0f).texOffs(0, 62).addBox(-4.5f, -11.5f, -4.5f, 9.0f, 3.0f, 9.0f).texOffs(18, 74).addBox(-5.5f, -10.5f, -6.5f, 11.0f, 2.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 17).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 0).addBox(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).texOffs(36, 62).addBox(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f).texOffs(42, 74).addBox(-4.0f, 9.0f, -2.4f, 8.0f, 1.0f, 0.4f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 17).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 17).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 33).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 33).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 128);
    }

    public static LayerDefinition cursedGrunt() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(24, 16).addBox(-4.5f, -8.6f, -4.5f, 9.0f, 3.0f, 9.0f).texOffs(0, 30).addBox(-1.0f, -6.5f, 4.4f, 2.0f, 3.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(56, 0).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(8, 16).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 16).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition cursedBrute() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 18).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0f, 0.0f, -3.0f, 10.0f, 12.0f, 6.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 34).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(-6.0f, 2.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(8, 34).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(6.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(16, 34).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(-2.4f, 12.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(24, 34).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(2.4f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0f, 0.0f, 0.0f));
        return LayerDefinition.create(data, 32, 64);
    }

    public static LayerDefinition cursedCaptain() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(28, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 33).addBox(-6.0f, -9.0f, -6.0f, 12.0f, 1.0f, 12.0f).texOffs(0, 46).addBox(-4.5f, -11.5f, -4.5f, 9.0f, 3.0f, 9.0f).texOffs(22, 58).addBox(-5.5f, -10.5f, -6.5f, 11.0f, 2.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 17).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 0).addBox(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).texOffs(0, 58).addBox(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 17).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).texOffs(36, 46).addBox(-1.5f, -2.5f, -1.5f, 3.0f, 7.0f, 3.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 17).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).texOffs(48, 46).addBox(-1.5f, -2.5f, -1.5f, 3.0f, 7.0f, 3.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(40, 17).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(48, 17).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 128);
    }

    public static LayerDefinition tideSovereign() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 45).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 45).addBox(-4.5f, -10.0f, -4.5f, 9.0f, 2.0f, 9.0f).texOffs(92, 45).addBox(-4.5f, -12.0f, -4.5f, 1.0f, 2.0f, 1.0f).texOffs(96, 45).addBox(3.5f, -12.0f, -4.5f, 1.0f, 2.0f, 1.0f).texOffs(100, 45).addBox(-4.5f, -12.0f, 3.5f, 1.0f, 2.0f, 1.0f).texOffs(104, 45).addBox(3.5f, -12.0f, 3.5f, 1.0f, 2.0f, 1.0f).texOffs(108, 45).addBox(-0.5f, -12.0f, -4.5f, 1.0f, 2.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 27).addBox(-5.0f, 0.0f, -3.0f, 10.0f, 12.0f, 6.0f).texOffs(28, 0).addBox(-5.6f, 0.0f, -3.6f, 11.2f, 12.0f, 7.2f).texOffs(68, 0).addBox(-5.8f, 12.0f, -3.8f, 11.6f, 11.0f, 7.6f).texOffs(0, 61).addBox(-5.0f, 9.0f, -3.4f, 10.0f, 1.0f, 0.4f).texOffs(4, 0).addBox(-5.5f, 0.0f, 3.6f, 11.0f, 20.0f, 1.0f).texOffs(112, 45).addBox(-6.0f, -3.0f, -1.0f, 1.0f, 2.0f, 1.0f).texOffs(84, 45).addBox(-5.0f, -4.5f, 0.0f, 1.0f, 3.0f, 1.0f).texOffs(76, 45).addBox(-4.0f, -6.0f, -1.0f, 1.0f, 4.0f, 1.0f).texOffs(116, 45).addBox(5.0f, -3.0f, -1.0f, 1.0f, 2.0f, 1.0f).texOffs(88, 45).addBox(4.0f, -4.5f, 0.0f, 1.0f, 3.0f, 1.0f).texOffs(80, 45).addBox(3.0f, -6.0f, -1.0f, 1.0f, 4.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(32, 27).addBox(-4.0f, -2.0f, -2.5f, 5.0f, 12.0f, 5.0f).texOffs(0, 0).addBox(-3.0f, -8.0f, -5.5f, 1.0f, 26.0f, 1.0f).texOffs(68, 45).addBox(-3.5f, -11.0f, -6.0f, 2.0f, 3.0f, 2.0f), PartPose.offset(-6.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(52, 27).addBox(-1.0f, -2.0f, -2.5f, 5.0f, 12.0f, 5.0f), PartPose.offset(6.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(72, 27).addBox(-2.5f, 0.0f, -2.5f, 5.0f, 12.0f, 5.0f), PartPose.offset(-2.4f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(92, 27).addBox(-2.5f, 0.0f, -2.5f, 5.0f, 12.0f, 5.0f), PartPose.offset(2.4f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 128, 64);
    }

    public static LayerDefinition lampwrightsHand() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 39).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(20, 0).addBox(-4.6f, -8.6f, -4.6f, 9.2f, 9.2f, 9.2f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 39).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 21).addBox(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).texOffs(32, 21).addBox(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f).texOffs(0, 0).addBox(-4.5f, 0.0f, 2.6f, 9.0f, 20.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 55).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(16, 55).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f).texOffs(12, 71).addBox(-0.5f, 10.0f, -3.0f, 1.0f, 2.0f, 1.0f).texOffs(0, 71).addBox(-1.5f, 12.0f, -4.0f, 3.0f, 4.0f, 3.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(32, 55).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(48, 55).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 128);
    }

    public static LayerDefinition cinderKing() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(32, 40).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 72).addBox(-4.5f, -10.0f, -4.5f, 9.0f, 2.0f, 9.0f).texOffs(12, 83).addBox(-4.5f, -12.0f, -4.5f, 1.0f, 2.0f, 1.0f).texOffs(16, 83).addBox(3.5f, -12.0f, -4.5f, 1.0f, 2.0f, 1.0f).texOffs(20, 83).addBox(-4.5f, -12.0f, 3.5f, 1.0f, 2.0f, 1.0f).texOffs(24, 83).addBox(3.5f, -12.0f, 3.5f, 1.0f, 2.0f, 1.0f).texOffs(28, 83).addBox(-0.5f, -12.0f, -4.5f, 1.0f, 2.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 40).addBox(-5.0f, 0.0f, -3.0f, 10.0f, 12.0f, 6.0f).texOffs(24, 0).addBox(-5.6f, 0.0f, -3.6f, 11.2f, 12.0f, 7.2f).texOffs(0, 21).addBox(-5.8f, 12.0f, -3.8f, 11.6f, 11.0f, 7.6f).texOffs(0, 0).addBox(-5.5f, 0.0f, 3.6f, 11.0f, 20.0f, 1.0f).texOffs(32, 83).addBox(-6.0f, -3.0f, -1.0f, 1.0f, 2.0f, 1.0f).texOffs(4, 83).addBox(-5.0f, -4.5f, 0.0f, 1.0f, 3.0f, 1.0f).texOffs(60, 72).addBox(-4.0f, -6.0f, -1.0f, 1.0f, 4.0f, 1.0f).texOffs(36, 83).addBox(5.0f, -3.0f, -1.0f, 1.0f, 2.0f, 1.0f).texOffs(8, 83).addBox(4.0f, -4.5f, 0.0f, 1.0f, 3.0f, 1.0f).texOffs(0, 83).addBox(3.0f, -6.0f, -1.0f, 1.0f, 4.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 58).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).texOffs(36, 72).addBox(-1.5f, -2.5f, -1.5f, 3.0f, 7.0f, 3.0f), PartPose.offset(-6.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(8, 58).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).texOffs(48, 72).addBox(-1.5f, -2.5f, -1.5f, 3.0f, 7.0f, 3.0f), PartPose.offset(6.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(16, 58).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(-2.4f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(24, 58).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(2.4f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 128);
    }

    public static LayerDefinition veilWitch() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 45).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(4, 0).addBox(-4.6f, -8.6f, -4.6f, 9.2f, 9.2f, 9.2f).texOffs(0, 76).addBox(-4.4f, -6.0f, -4.9f, 8.8f, 7.0f, 0.4f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 45).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 27).addBox(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).texOffs(32, 27).addBox(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 61).addBox(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f).texOffs(0, 0).addBox(-2.0f, -8.0f, -4.5f, 1.0f, 26.0f, 1.0f).texOffs(20, 76).addBox(-2.5f, -11.0f, -5.0f, 2.0f, 3.0f, 2.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(12, 61).addBox(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(24, 61).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(36, 61).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 128);
    }

    public static LayerDefinition redMaddox() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 56).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f).texOffs(46, 97).addBox(-4.0f, -2.0f, -4.6f, 8.0f, 3.0f, 1.0f).texOffs(24, 106).addBox(1.0f, -5.0f, -4.3f, 2.0f, 2.0f, 0.3f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 72).addBox(-6.0f, -9.0f, -6.0f, 12.0f, 1.0f, 12.0f).texOffs(0, 85).addBox(-4.5f, -11.5f, -4.5f, 9.0f, 3.0f, 9.0f).texOffs(0, 106).addBox(-5.5f, -10.5f, -6.5f, 11.0f, 2.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 21).addBox(-5.0f, 0.0f, -3.0f, 10.0f, 12.0f, 6.0f).texOffs(24, 0).addBox(-5.5f, 0.0f, -3.5f, 11.0f, 12.0f, 7.0f).texOffs(20, 97).addBox(-5.5f, 12.0f, 2.5f, 11.0f, 6.0f, 1.5f).texOffs(0, 0).addBox(-5.5f, 0.0f, 3.6f, 11.0f, 20.0f, 1.0f).texOffs(36, 85).addBox(-7.0f, -1.0f, -3.5f, 3.0f, 1.5f, 7.0f).texOffs(0, 97).addBox(4.0f, -1.0f, -3.5f, 3.0f, 1.5f, 7.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(32, 21).addBox(-4.0f, -2.0f, -2.5f, 5.0f, 12.0f, 5.0f), PartPose.offset(-6.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 39).addBox(-1.0f, -2.0f, -2.5f, 5.0f, 12.0f, 5.0f), PartPose.offset(6.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(20, 39).addBox(-2.5f, 0.0f, -2.5f, 5.0f, 12.0f, 5.0f), PartPose.offset(-2.4f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(40, 39).addBox(-2.5f, 0.0f, -2.5f, 5.0f, 12.0f, 5.0f), PartPose.offset(2.4f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 128);
    }

    public static LayerDefinition ashenHerald() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 45).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(4, 0).addBox(-4.6f, -8.6f, -4.6f, 9.2f, 9.2f, 9.2f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 45).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 27).addBox(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).texOffs(32, 27).addBox(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f).texOffs(56, 61).addBox(3.0f, 10.0f, -4.0f, 2.0f, 2.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(56, 45).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).texOffs(24, 61).addBox(-1.5f, -2.5f, -1.5f, 3.0f, 7.0f, 3.0f).texOffs(0, 0).addBox(-1.5f, -8.0f, -4.0f, 1.0f, 26.0f, 1.0f).texOffs(48, 61).addBox(-2.0f, -11.0f, -4.5f, 2.0f, 3.0f, 2.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 61).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).texOffs(36, 61).addBox(-1.5f, -2.5f, -1.5f, 3.0f, 7.0f, 3.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(8, 61).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 61).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 128);
    }

    public static LayerDefinition pretender() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 21).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 53).addBox(-4.5f, -10.0f, -4.5f, 9.0f, 2.0f, 9.0f).texOffs(32, 64).addBox(-4.5f, -12.0f, -4.5f, 1.0f, 2.0f, 1.0f).texOffs(36, 64).addBox(3.5f, -12.0f, -4.5f, 1.0f, 2.0f, 1.0f).texOffs(40, 64).addBox(-4.5f, -12.0f, 3.5f, 1.0f, 2.0f, 1.0f).texOffs(44, 64).addBox(3.5f, -12.0f, 3.5f, 1.0f, 2.0f, 1.0f).texOffs(48, 64).addBox(-0.5f, -12.0f, -4.5f, 1.0f, 2.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 21).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(20, 0).addBox(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).texOffs(36, 53).addBox(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f).texOffs(0, 71).addBox(-4.0f, 9.0f, -2.4f, 8.0f, 1.0f, 0.4f).texOffs(0, 0).addBox(-4.5f, 0.0f, 2.6f, 9.0f, 20.0f, 1.0f).texOffs(0, 64).addBox(-6.0f, -1.0f, -2.5f, 3.0f, 1.5f, 5.0f).texOffs(16, 64).addBox(3.0f, -1.0f, -2.5f, 3.0f, 1.5f, 5.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 37).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(16, 37).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(32, 37).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(48, 37).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 128);
    }

    public static LayerDefinition abyssal() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-9.0f, -8.0f, -18.0f, 18.0f, 14.0f, 18.0f).texOffs(46, 110).addBox(-6.0f, -4.0f, -30.0f, 12.0f, 8.0f, 12.0f).texOffs(110, 132).addBox(-9.5f, -6.0f, -15.0f, 2.0f, 3.0f, 3.0f).texOffs(0, 146).addBox(7.5f, -6.0f, -15.0f, 2.0f, 3.0f, 3.0f), PartPose.offset(0.0f, 8.0f, -10.0f));
        PartDefinition p1 = p0.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(48, 87).addBox(-6.0f, 0.0f, -18.0f, 12.0f, 4.0f, 18.0f), PartPose.offset(0.0f, 6.0f, -12.0f));
        PartDefinition p2 = root.addOrReplaceChild("seg1", CubeListBuilder.create().texOffs(0, 32).addBox(-7.2f, -6.3f, 0.0f, 14.4f, 12.6f, 16.0f).texOffs(94, 110).addBox(-0.5f, -10.3f, 3.0f, 1.0f, 4.0f, 10.0f), PartPose.offset(0.0f, 8.0f, -10.0f));
        PartDefinition p3 = p2.addOrReplaceChild("seg2", CubeListBuilder.create().texOffs(62, 32).addBox(-6.4f, -5.6f, 0.0f, 12.8f, 11.2f, 16.0f).texOffs(0, 132).addBox(-0.5f, -9.6f, 3.0f, 1.0f, 4.0f, 10.0f), PartPose.offset(0.0f, 0.0f, 16.0f));
        PartDefinition p4 = p3.addOrReplaceChild("seg3", CubeListBuilder.create().texOffs(0, 61).addBox(-5.6f, -4.9f, 0.0f, 11.2f, 9.8f, 16.0f).texOffs(22, 132).addBox(-0.5f, -8.9f, 3.0f, 1.0f, 4.0f, 10.0f), PartPose.offset(0.0f, 0.0f, 16.0f));
        PartDefinition p5 = p4.addOrReplaceChild("seg4", CubeListBuilder.create().texOffs(56, 61).addBox(-4.8f, -4.2f, 0.0f, 9.6f, 8.4f, 16.0f).texOffs(44, 132).addBox(-0.5f, -8.2f, 3.0f, 1.0f, 4.0f, 10.0f), PartPose.offset(0.0f, 0.0f, 16.0f));
        PartDefinition p6 = p5.addOrReplaceChild("seg5", CubeListBuilder.create().texOffs(0, 87).addBox(-4.0f, -3.5f, 0.0f, 8.0f, 7.0f, 16.0f).texOffs(66, 132).addBox(-0.5f, -7.5f, 3.0f, 1.0f, 4.0f, 10.0f), PartPose.offset(0.0f, 0.0f, 16.0f));
        PartDefinition p7 = p6.addOrReplaceChild("seg6", CubeListBuilder.create().texOffs(0, 110).addBox(-3.2f, -2.8f, 0.0f, 6.4f, 5.6f, 16.0f).texOffs(88, 132).addBox(-0.5f, -6.8f, 3.0f, 1.0f, 4.0f, 10.0f), PartPose.offset(0.0f, 0.0f, 16.0f));
        return LayerDefinition.create(data, 128, 256);
    }

    public static LayerDefinition brannoch() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 35).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f).texOffs(0, 51).addBox(-4.0f, -2.0f, -4.6f, 8.0f, 3.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0f, 0.0f, -3.0f, 10.0f, 12.0f, 6.0f).texOffs(18, 51).addBox(-5.0f, 9.0f, -3.4f, 10.0f, 1.0f, 0.4f).texOffs(44, 35).addBox(-5.0f, 2.0f, -3.7f, 1.0f, 9.0f, 0.5f).texOffs(36, 35).addBox(-3.0f, 4.0f, -3.7f, 1.0f, 11.0f, 0.5f).texOffs(32, 35).addBox(-1.0f, 2.0f, -3.7f, 1.0f, 13.0f, 0.5f).texOffs(48, 35).addBox(1.0f, 4.0f, -3.7f, 1.0f, 9.0f, 0.5f).texOffs(40, 35).addBox(3.0f, 2.0f, -3.7f, 1.0f, 11.0f, 0.5f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0f, -2.0f, -2.5f, 5.0f, 12.0f, 5.0f), PartPose.offset(-6.0f, 2.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 18).addBox(-1.0f, -2.0f, -2.5f, 5.0f, 12.0f, 5.0f), PartPose.offset(6.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(20, 18).addBox(-2.5f, 0.0f, -2.5f, 5.0f, 12.0f, 5.0f), PartPose.offset(-2.4f, 12.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(40, 18).addBox(-2.5f, 0.0f, -2.5f, 5.0f, 12.0f, 5.0f), PartPose.offset(2.4f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0f, 0.0f, 0.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition greyLamplighter() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 45).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(4, 0).addBox(-4.6f, -8.6f, -4.6f, 9.2f, 9.2f, 9.2f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 45).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 27).addBox(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).texOffs(32, 27).addBox(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(56, 45).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).texOffs(24, 61).addBox(-1.5f, -2.5f, -1.5f, 3.0f, 7.0f, 3.0f).texOffs(0, 0).addBox(-1.5f, -8.0f, -4.0f, 1.0f, 26.0f, 1.0f).texOffs(0, 75).addBox(-2.0f, -11.0f, -4.5f, 2.0f, 3.0f, 2.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 61).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).texOffs(36, 61).addBox(-1.5f, -2.5f, -1.5f, 3.0f, 7.0f, 3.0f).texOffs(8, 75).addBox(-0.5f, 10.0f, -2.0f, 1.0f, 2.0f, 1.0f).texOffs(48, 61).addBox(-1.5f, 12.0f, -3.0f, 3.0f, 4.0f, 3.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(8, 61).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 61).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 128);
    }

    public static LayerDefinition motherOfGulls() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 38).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 0).addBox(-4.6f, -8.6f, -4.6f, 9.2f, 9.2f, 9.2f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 38).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 20).addBox(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).texOffs(32, 20).addBox(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f).texOffs(0, 54).addBox(-13.0f, -2.0f, 2.0f, 9.0f, 14.0f, 1.0f).texOffs(20, 54).addBox(4.0f, -2.0f, 2.0f, 9.0f, 14.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 54).addBox(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(52, 54).addBox(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 69).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(12, 69).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 128);
    }

    public static LayerDefinition boneAbbot() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(32, 66).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 84).addBox(-3.5f, -14.0f, -3.0f, 7.0f, 6.0f, 6.0f).texOffs(24, 98).addBox(-0.5f, -15.0f, -3.2f, 1.0f, 6.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 66).addBox(-5.0f, 0.0f, -3.0f, 10.0f, 12.0f, 6.0f).texOffs(0, 27).addBox(-5.6f, 0.0f, -3.6f, 11.2f, 12.0f, 7.2f).texOffs(0, 47).addBox(-5.8f, 12.0f, -3.8f, 11.6f, 11.0f, 7.6f).texOffs(4, 0).addBox(-5.5f, 0.0f, 3.6f, 11.0f, 20.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 84).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).texOffs(0, 98).addBox(-1.5f, -2.5f, -1.5f, 3.0f, 7.0f, 3.0f).texOffs(0, 0).addBox(-1.5f, -8.0f, -4.0f, 1.0f, 26.0f, 1.0f).texOffs(28, 98).addBox(-2.0f, -11.0f, -4.5f, 2.0f, 3.0f, 2.0f), PartPose.offset(-6.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(8, 84).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).texOffs(12, 98).addBox(-1.5f, -2.5f, -1.5f, 3.0f, 7.0f, 3.0f), PartPose.offset(6.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(16, 84).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(-2.4f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(24, 84).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(2.4f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 128);
    }

    public static LayerDefinition cinderMatron() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 38).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 0).addBox(-4.6f, -8.6f, -4.6f, 9.2f, 9.2f, 9.2f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 38).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 20).addBox(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).texOffs(32, 20).addBox(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f).texOffs(16, 69).addBox(-4.0f, 9.0f, -2.4f, 8.0f, 1.0f, 0.4f).texOffs(8, 69).addBox(-5.0f, -3.0f, -1.0f, 1.0f, 2.0f, 1.0f).texOffs(0, 69).addBox(-4.0f, -4.5f, 0.0f, 1.0f, 3.0f, 1.0f).texOffs(48, 54).addBox(-3.0f, -6.0f, -1.0f, 1.0f, 4.0f, 1.0f).texOffs(12, 69).addBox(4.0f, -3.0f, -1.0f, 1.0f, 2.0f, 1.0f).texOffs(4, 69).addBox(3.0f, -4.5f, 0.0f, 1.0f, 3.0f, 1.0f).texOffs(52, 54).addBox(2.0f, -6.0f, -1.0f, 1.0f, 4.0f, 1.0f).texOffs(56, 54).addBox(3.0f, 10.0f, -4.0f, 2.0f, 2.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 54).addBox(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(12, 54).addBox(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(24, 54).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(36, 54).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 128);
    }

    public static LayerDefinition oldGnasher() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-8.5f, -5.1f, -17.0f, 17.0f, 10.2f, 34.0f), PartPose.offset(0.0f, 15.5f, 0.0f));
        PartDefinition p1 = p0.addOrReplaceChild("head", CubeListBuilder.create().texOffs(50, 73).addBox(-5.95f, -4.25f, -10.2f, 11.9f, 8.5f, 10.2f).texOffs(0, 73).addBox(-4.25f, -1.7f, -25.5f, 8.5f, 5.1f, 15.3f).texOffs(72, 113).addBox(-5.1f, -5.95f, -6.8f, 3.4f, 1.7f, 3.4f).texOffs(88, 113).addBox(1.7f, -5.95f, -6.8f, 3.4f, 1.7f, 3.4f), PartPose.offset(0.0f, -1.7f, -17.0f));
        PartDefinition p2 = p1.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(0, 95).addBox(-4.25f, 0.0f, -15.3f, 8.5f, 1.7f, 15.3f), PartPose.offset(0.0f, 3.4f, -10.2f));
        PartDefinition p3 = p0.addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(0, 45).addBox(-5.1f, -3.4f, 0.0f, 10.2f, 6.8f, 20.4f), PartPose.offset(0.0f, 0.0f, 17.0f));
        PartDefinition p4 = p3.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(64, 45).addBox(-3.4f, -2.55f, 0.0f, 6.8f, 5.1f, 17.0f), PartPose.offset(0.0f, 0.85f, 20.4f));
        PartDefinition p5 = p4.addOrReplaceChild("tail3", CubeListBuilder.create().texOffs(50, 95).addBox(-1.7f, -1.7f, 0.0f, 3.4f, 3.4f, 13.6f), PartPose.offset(0.0f, 0.85f, 17.0f));
        PartDefinition p6 = root.addOrReplaceChild("leg_fl", CubeListBuilder.create().texOffs(86, 95).addBox(-3.4f, 0.0f, -2.55f, 5.1f, 5.1f, 5.1f), PartPose.offset(-8.5f, 18.9f, -11.9f));
        PartDefinition p7 = root.addOrReplaceChild("leg_fr", CubeListBuilder.create().texOffs(0, 113).addBox(-1.7f, 0.0f, -2.55f, 5.1f, 5.1f, 5.1f), PartPose.offset(8.5f, 18.9f, -11.9f));
        PartDefinition p8 = root.addOrReplaceChild("leg_bl", CubeListBuilder.create().texOffs(24, 113).addBox(-3.4f, 0.0f, -2.55f, 5.1f, 5.1f, 5.1f), PartPose.offset(-8.5f, 18.9f, 11.9f));
        PartDefinition p9 = root.addOrReplaceChild("leg_br", CubeListBuilder.create().texOffs(48, 113).addBox(-1.7f, 0.0f, -2.55f, 5.1f, 5.1f, 5.1f), PartPose.offset(8.5f, 18.9f, 11.9f));
        return LayerDefinition.create(data, 128, 128);
    }

    public static LayerDefinition grubb() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 35).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f).texOffs(0, 62).addBox(-4.0f, -2.0f, -4.6f, 8.0f, 3.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 51).addBox(-4.5f, -9.5f, -4.5f, 9.0f, 2.0f, 9.0f).texOffs(36, 51).addBox(-4.5f, -8.0f, -6.5f, 9.0f, 1.0f, 3.0f).texOffs(18, 62).addBox(-1.0f, -10.0f, -5.2f, 2.0f, 2.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0f, 0.0f, -3.0f, 10.0f, 12.0f, 6.0f).texOffs(32, 35).addBox(-4.5f, 3.0f, -3.6f, 9.0f, 13.0f, 0.6f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0f, -2.0f, -2.5f, 5.0f, 12.0f, 5.0f), PartPose.offset(-6.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 18).addBox(-1.0f, -2.0f, -2.5f, 5.0f, 12.0f, 5.0f), PartPose.offset(6.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(20, 18).addBox(-2.5f, 0.0f, -2.5f, 5.0f, 12.0f, 5.0f), PartPose.offset(-2.4f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(40, 18).addBox(-2.5f, 0.0f, -2.5f, 5.0f, 12.0f, 5.0f), PartPose.offset(2.4f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 128);
    }

    public static LayerDefinition motherBrine() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(32, 27).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(12, 60).addBox(-4.3f, -8.3f, 0.5f, 8.6f, 10.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 44).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(4, 0).addBox(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).texOffs(0, 27).addBox(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f).texOffs(8, 75).addBox(-4.0f, 9.0f, -2.4f, 8.0f, 1.0f, 0.4f).texOffs(50, 60).addBox(-4.0f, 2.0f, -2.7f, 1.0f, 9.0f, 0.5f).texOffs(42, 60).addBox(-2.4f, 4.0f, -2.7f, 1.0f, 11.0f, 0.5f).texOffs(38, 60).addBox(-0.8f, 2.0f, -2.7f, 1.0f, 13.0f, 0.5f).texOffs(54, 60).addBox(0.8f, 4.0f, -2.7f, 1.0f, 9.0f, 0.5f).texOffs(46, 60).addBox(2.4f, 2.0f, -2.7f, 1.0f, 11.0f, 0.5f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 44).addBox(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f).texOffs(0, 0).addBox(-2.0f, -8.0f, -4.5f, 1.0f, 26.0f, 1.0f).texOffs(0, 75).addBox(-2.5f, -11.0f, -5.0f, 2.0f, 3.0f, 2.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(36, 44).addBox(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(48, 44).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 60).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 128);
    }

    public static LayerDefinition slagBeast() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0f, -7.0f, -12.0f, 16.0f, 13.0f, 24.0f).texOffs(0, 37).addBox(-6.0f, -11.0f, -8.0f, 12.0f, 4.0f, 14.0f), PartPose.offset(0.0f, 8.0f, 0.0f));
        PartDefinition p1 = p0.addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(24, 55).addBox(-2.5f, -2.0f, 0.0f, 5.0f, 4.0f, 10.0f), PartPose.offset(0.0f, -2.0f, 12.0f));
        PartDefinition p2 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(80, 0).addBox(-5.0f, -5.0f, -10.0f, 10.0f, 10.0f, 10.0f).texOffs(54, 55).addBox(-6.0f, -8.0f, -6.0f, 2.0f, 4.0f, 2.0f).texOffs(62, 55).addBox(4.0f, -8.0f, -6.0f, 2.0f, 4.0f, 2.0f), PartPose.offset(0.0f, 6.0f, -12.0f));
        PartDefinition p3 = root.addOrReplaceChild("leg_fl", CubeListBuilder.create().texOffs(52, 37).addBox(-3.0f, 0.0f, -3.0f, 6.0f, 11.0f, 6.0f), PartPose.offset(-6.0f, 13.0f, -8.0f));
        PartDefinition p4 = root.addOrReplaceChild("leg_fr", CubeListBuilder.create().texOffs(76, 37).addBox(-3.0f, 0.0f, -3.0f, 6.0f, 11.0f, 6.0f), PartPose.offset(6.0f, 13.0f, -8.0f));
        PartDefinition p5 = root.addOrReplaceChild("leg_bl", CubeListBuilder.create().texOffs(100, 37).addBox(-3.0f, 0.0f, -3.0f, 6.0f, 11.0f, 6.0f), PartPose.offset(-6.0f, 13.0f, 8.0f));
        PartDefinition p6 = root.addOrReplaceChild("leg_br", CubeListBuilder.create().texOffs(0, 55).addBox(-3.0f, 0.0f, -3.0f, 6.0f, 11.0f, 6.0f), PartPose.offset(6.0f, 13.0f, 8.0f));
        return LayerDefinition.create(data, 128, 128);
    }

    public static LayerDefinition lanternWarden() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 94).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 0).addBox(-4.6f, -8.6f, -4.6f, 9.2f, 9.2f, 9.2f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 59).addBox(-5.0f, 0.0f, -3.0f, 10.0f, 12.0f, 6.0f).texOffs(0, 20).addBox(-5.6f, 0.0f, -3.6f, 11.2f, 12.0f, 7.2f).texOffs(0, 40).addBox(-5.8f, 12.0f, -3.8f, 11.6f, 11.0f, 7.6f).texOffs(44, 94).addBox(-1.5f, 3.0f, -5.5f, 3.0f, 4.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(32, 59).addBox(-4.0f, -2.0f, -2.5f, 5.0f, 12.0f, 5.0f), PartPose.offset(-6.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 77).addBox(-1.0f, -2.0f, -2.5f, 5.0f, 12.0f, 5.0f).texOffs(54, 94).addBox(-0.5f, 10.0f, -3.5f, 1.0f, 2.0f, 1.0f).texOffs(32, 94).addBox(-1.5f, 12.0f, -4.5f, 3.0f, 4.0f, 3.0f), PartPose.offset(6.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(20, 77).addBox(-2.5f, 0.0f, -2.5f, 5.0f, 12.0f, 5.0f), PartPose.offset(-2.4f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(40, 77).addBox(-2.5f, 0.0f, -2.5f, 5.0f, 12.0f, 5.0f), PartPose.offset(2.4f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 128);
    }

    public static LayerDefinition corvinHale() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(32, 27).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 78).addBox(-7.0f, -12.0f, -2.0f, 14.0f, 4.0f, 4.0f).texOffs(0, 59).addBox(-4.5f, -9.0f, -4.5f, 9.0f, 1.0f, 9.0f).texOffs(8, 86).addBox(-1.0f, -13.0f, -2.5f, 2.0f, 2.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 27).addBox(-5.0f, 0.0f, -3.0f, 10.0f, 12.0f, 6.0f).texOffs(28, 0).addBox(-5.5f, 0.0f, -3.5f, 11.0f, 12.0f, 7.0f).texOffs(36, 78).addBox(-5.5f, 12.0f, 2.5f, 11.0f, 6.0f, 1.5f).texOffs(4, 0).addBox(-5.5f, 0.0f, 3.6f, 11.0f, 20.0f, 1.0f).texOffs(0, 69).addBox(-7.0f, -1.0f, -3.5f, 3.0f, 1.5f, 7.0f).texOffs(20, 69).addBox(4.0f, -1.0f, -3.5f, 3.0f, 1.5f, 7.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 45).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).texOffs(36, 59).addBox(-1.5f, -2.5f, -1.5f, 3.0f, 7.0f, 3.0f).texOffs(0, 0).addBox(-1.5f, -8.0f, -4.0f, 1.0f, 26.0f, 1.0f).texOffs(0, 86).addBox(-2.0f, -11.0f, -4.5f, 2.0f, 3.0f, 2.0f), PartPose.offset(-6.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(8, 45).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).texOffs(48, 59).addBox(-1.5f, -2.5f, -1.5f, 3.0f, 7.0f, 3.0f), PartPose.offset(6.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(16, 45).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(-2.4f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(24, 45).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(2.4f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 128);
    }

    public static LayerDefinition captainSaltbones() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 21).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f).texOffs(46, 76).addBox(1.0f, -5.0f, -4.3f, 2.0f, 2.0f, 0.3f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 51).addBox(-6.0f, -9.0f, -6.0f, 12.0f, 1.0f, 12.0f).texOffs(0, 64).addBox(-4.5f, -11.5f, -4.5f, 9.0f, 3.0f, 9.0f).texOffs(22, 76).addBox(-5.5f, -10.5f, -6.5f, 11.0f, 2.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 21).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(20, 0).addBox(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).texOffs(0, 76).addBox(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f).texOffs(0, 0).addBox(-4.5f, 0.0f, 2.6f, 9.0f, 20.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(56, 21).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).texOffs(36, 64).addBox(-1.5f, -2.5f, -1.5f, 3.0f, 7.0f, 3.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 37).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).texOffs(48, 64).addBox(-1.5f, -2.5f, -1.5f, 3.0f, 7.0f, 3.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(8, 37).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 37).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 128);
    }

    public static LayerDefinition barnacleBride() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 38).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 0).addBox(-4.6f, -8.6f, -4.6f, 9.2f, 9.2f, 9.2f).texOffs(4, 69).addBox(-4.4f, -6.0f, -4.9f, 8.8f, 7.0f, 0.4f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 38).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 20).addBox(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).texOffs(32, 20).addBox(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f).texOffs(24, 69).addBox(-4.0f, 9.0f, -2.4f, 8.0f, 1.0f, 0.4f).texOffs(60, 54).addBox(-4.0f, 2.0f, -2.7f, 1.0f, 9.0f, 0.5f).texOffs(52, 54).addBox(-2.4f, 4.0f, -2.7f, 1.0f, 11.0f, 0.5f).texOffs(48, 54).addBox(-0.8f, 2.0f, -2.7f, 1.0f, 13.0f, 0.5f).texOffs(0, 69).addBox(0.8f, 4.0f, -2.7f, 1.0f, 9.0f, 0.5f).texOffs(56, 54).addBox(2.4f, 2.0f, -2.7f, 1.0f, 11.0f, 0.5f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 54).addBox(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(12, 54).addBox(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(24, 54).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(36, 54).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 128);
    }

    public static LayerDefinition hangman() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 62).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(4, 0).addBox(-4.6f, -8.6f, -4.6f, 9.2f, 9.2f, 9.2f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 27).addBox(-5.0f, 0.0f, -3.0f, 10.0f, 12.0f, 6.0f).texOffs(32, 62).addBox(-4.5f, 3.0f, -3.6f, 9.0f, 13.0f, 0.6f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(32, 27).addBox(-4.0f, -2.0f, -2.5f, 5.0f, 12.0f, 5.0f).texOffs(0, 0).addBox(-3.0f, -8.0f, -5.5f, 1.0f, 26.0f, 1.0f).texOffs(52, 62).addBox(-3.5f, -11.0f, -6.0f, 2.0f, 3.0f, 2.0f), PartPose.offset(-6.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 45).addBox(-1.0f, -2.0f, -2.5f, 5.0f, 12.0f, 5.0f), PartPose.offset(6.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(20, 45).addBox(-2.5f, 0.0f, -2.5f, 5.0f, 12.0f, 5.0f), PartPose.offset(-2.4f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(40, 45).addBox(-2.5f, 0.0f, -2.5f, 5.0f, 12.0f, 5.0f), PartPose.offset(2.4f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 128);
    }

    public static LayerDefinition witchlightHag() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 45).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(4, 0).addBox(-4.6f, -8.6f, -4.6f, 9.2f, 9.2f, 9.2f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 45).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 27).addBox(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).texOffs(32, 27).addBox(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 61).addBox(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f).texOffs(0, 0).addBox(-2.0f, -8.0f, -4.5f, 1.0f, 26.0f, 1.0f).texOffs(0, 76).addBox(-2.5f, -11.0f, -5.0f, 2.0f, 3.0f, 2.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(12, 61).addBox(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f).texOffs(8, 76).addBox(-0.5f, 10.0f, -2.5f, 1.0f, 2.0f, 1.0f).texOffs(48, 61).addBox(-1.5f, 12.0f, -3.5f, 3.0f, 4.0f, 3.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(24, 61).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(36, 61).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 128);
    }

    public static LayerDefinition signalKeeper() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(28, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 43).addBox(-7.0f, -12.0f, -2.0f, 14.0f, 4.0f, 4.0f).texOffs(0, 33).addBox(-4.5f, -9.0f, -4.5f, 9.0f, 1.0f, 9.0f).texOffs(44, 51).addBox(-1.0f, -13.0f, -2.5f, 2.0f, 2.0f, 1.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 17).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 0).addBox(-4.5f, 0.0f, -2.5f, 9.0f, 12.0f, 5.0f).texOffs(36, 43).addBox(-4.5f, 12.0f, 1.5f, 9.0f, 6.0f, 1.5f).texOffs(0, 51).addBox(-6.0f, -1.0f, -2.5f, 3.0f, 1.5f, 5.0f).texOffs(16, 51).addBox(3.0f, -1.0f, -2.5f, 3.0f, 1.5f, 5.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 17).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).texOffs(36, 33).addBox(-1.5f, -2.5f, -1.5f, 3.0f, 7.0f, 3.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 17).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f).texOffs(48, 33).addBox(-1.5f, -2.5f, -1.5f, 3.0f, 7.0f, 3.0f).texOffs(50, 51).addBox(-0.5f, 10.0f, -2.0f, 1.0f, 2.0f, 1.0f).texOffs(32, 51).addBox(-1.5f, 12.0f, -3.0f, 3.0f, 4.0f, 3.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(40, 17).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(48, 17).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition reachSerpent() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-9.0f, -8.0f, -18.0f, 18.0f, 14.0f, 18.0f).texOffs(46, 110).addBox(-6.0f, -4.0f, -30.0f, 12.0f, 8.0f, 12.0f).texOffs(110, 132).addBox(-9.5f, -6.0f, -15.0f, 2.0f, 3.0f, 3.0f).texOffs(0, 146).addBox(7.5f, -6.0f, -15.0f, 2.0f, 3.0f, 3.0f), PartPose.offset(0.0f, 8.0f, -10.0f));
        PartDefinition p1 = p0.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(48, 87).addBox(-6.0f, 0.0f, -18.0f, 12.0f, 4.0f, 18.0f), PartPose.offset(0.0f, 6.0f, -12.0f));
        PartDefinition p2 = root.addOrReplaceChild("seg1", CubeListBuilder.create().texOffs(0, 32).addBox(-7.2f, -6.3f, 0.0f, 14.4f, 12.6f, 16.0f).texOffs(94, 110).addBox(-0.5f, -10.3f, 3.0f, 1.0f, 4.0f, 10.0f), PartPose.offset(0.0f, 8.0f, -10.0f));
        PartDefinition p3 = p2.addOrReplaceChild("seg2", CubeListBuilder.create().texOffs(62, 32).addBox(-6.4f, -5.6f, 0.0f, 12.8f, 11.2f, 16.0f).texOffs(0, 132).addBox(-0.5f, -9.6f, 3.0f, 1.0f, 4.0f, 10.0f), PartPose.offset(0.0f, 0.0f, 16.0f));
        PartDefinition p4 = p3.addOrReplaceChild("seg3", CubeListBuilder.create().texOffs(0, 61).addBox(-5.6f, -4.9f, 0.0f, 11.2f, 9.8f, 16.0f).texOffs(22, 132).addBox(-0.5f, -8.9f, 3.0f, 1.0f, 4.0f, 10.0f), PartPose.offset(0.0f, 0.0f, 16.0f));
        PartDefinition p5 = p4.addOrReplaceChild("seg4", CubeListBuilder.create().texOffs(56, 61).addBox(-4.8f, -4.2f, 0.0f, 9.6f, 8.4f, 16.0f).texOffs(44, 132).addBox(-0.5f, -8.2f, 3.0f, 1.0f, 4.0f, 10.0f), PartPose.offset(0.0f, 0.0f, 16.0f));
        PartDefinition p6 = p5.addOrReplaceChild("seg5", CubeListBuilder.create().texOffs(0, 87).addBox(-4.0f, -3.5f, 0.0f, 8.0f, 7.0f, 16.0f).texOffs(66, 132).addBox(-0.5f, -7.5f, 3.0f, 1.0f, 4.0f, 10.0f), PartPose.offset(0.0f, 0.0f, 16.0f));
        PartDefinition p7 = p6.addOrReplaceChild("seg6", CubeListBuilder.create().texOffs(0, 110).addBox(-3.2f, -2.8f, 0.0f, 6.4f, 5.6f, 16.0f).texOffs(88, 132).addBox(-0.5f, -6.8f, 3.0f, 1.0f, 4.0f, 10.0f), PartPose.offset(0.0f, 0.0f, 16.0f));
        return LayerDefinition.create(data, 128, 256);
    }

    public static LayerDefinition siren() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 18).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 49).addBox(-4.3f, -8.3f, 0.5f, 8.6f, 10.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 18).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).texOffs(0, 0).addBox(-4.6f, 0.0f, -2.6f, 9.2f, 12.0f, 5.2f).texOffs(32, 0).addBox(-4.8f, 12.0f, -2.8f, 9.6f, 11.0f, 5.6f).texOffs(46, 49).addBox(-4.0f, 9.0f, -2.4f, 8.0f, 1.0f, 0.4f).texOffs(38, 49).addBox(-4.0f, 2.0f, -2.7f, 1.0f, 9.0f, 0.5f).texOffs(30, 49).addBox(-2.4f, 4.0f, -2.7f, 1.0f, 11.0f, 0.5f).texOffs(26, 49).addBox(-0.8f, 2.0f, -2.7f, 1.0f, 13.0f, 0.5f).texOffs(42, 49).addBox(0.8f, 4.0f, -2.7f, 1.0f, 9.0f, 0.5f).texOffs(34, 49).addBox(2.4f, 2.0f, -2.7f, 1.0f, 11.0f, 0.5f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 34).addBox(-2.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-5.0f, 2.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(12, 34).addBox(-1.0f, -2.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(5.0f, 2.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(24, 34).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(-1.9f, 12.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(36, 34).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition giantWasp() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(28, 0).addBox(-2.5f, -2.5f, -3.0f, 5.0f, 5.0f, 6.0f), PartPose.offset(0.0f, 12.0f, 0.0f));
        PartDefinition p1 = p0.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 14).addBox(-2.0f, -2.0f, -4.0f, 4.0f, 4.0f, 4.0f).texOffs(58, 22).addBox(-2.4f, -1.6f, -3.6f, 1.0f, 2.0f, 2.0f).texOffs(0, 29).addBox(1.4f, -1.6f, -3.6f, 1.0f, 2.0f, 2.0f).texOffs(14, 29).addBox(-1.5f, 1.5f, -4.6f, 1.0f, 1.0f, 1.0f).texOffs(18, 29).addBox(0.5f, 1.5f, -4.6f, 1.0f, 1.0f, 1.0f).texOffs(6, 29).addBox(-1.5f, -4.0f, -3.5f, 0.5f, 2.0f, 0.5f).texOffs(10, 29).addBox(1.0f, -4.0f, -3.5f, 0.5f, 2.0f, 0.5f), PartPose.offset(0.0f, -0.5f, -3.0f));
        PartDefinition p2 = p0.addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0f, -3.0f, 0.0f, 6.0f, 6.0f, 8.0f), PartPose.offsetAndRotation(0.0f, 0.5f, 3.0f, -0.314f, 0.0f, 0.0f));
        PartDefinition p3 = p2.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(50, 22).addBox(-0.5f, -0.5f, 0.0f, 1.0f, 1.0f, 3.0f), PartPose.offsetAndRotation(0.0f, 0.5f, 8.0f, 0.349f, 0.0f, 0.0f));
        PartDefinition p4 = p0.addOrReplaceChild("wing_l", CubeListBuilder.create().texOffs(16, 14).addBox(-11.0f, 0.0f, -2.0f, 11.0f, 0.5f, 6.0f), PartPose.offsetAndRotation(-1.5f, -2.5f, -1.0f, 0.0f, 0.0f, 0.349f));
        PartDefinition p5 = p0.addOrReplaceChild("wing_r", CubeListBuilder.create().texOffs(0, 22).addBox(0.0f, 0.0f, -2.0f, 11.0f, 0.5f, 6.0f), PartPose.offsetAndRotation(1.5f, -2.5f, -1.0f, 0.0f, 0.0f, -0.349f));
        PartDefinition p6 = p0.addOrReplaceChild("leg_fl", CubeListBuilder.create().texOffs(34, 22).addBox(-0.5f, 0.0f, -0.5f, 1.0f, 5.0f, 1.0f), PartPose.offset(-2.0f, 2.5f, -2.0f));
        PartDefinition p7 = p0.addOrReplaceChild("leg_fr", CubeListBuilder.create().texOffs(38, 22).addBox(-0.5f, 0.0f, -0.5f, 1.0f, 5.0f, 1.0f), PartPose.offset(2.0f, 2.5f, -2.0f));
        PartDefinition p8 = p0.addOrReplaceChild("leg_bl", CubeListBuilder.create().texOffs(42, 22).addBox(-0.5f, 0.0f, -0.5f, 1.0f, 5.0f, 1.0f), PartPose.offset(-2.0f, 2.5f, 2.0f));
        PartDefinition p9 = p0.addOrReplaceChild("leg_br", CubeListBuilder.create().texOffs(46, 22).addBox(-0.5f, 0.0f, -0.5f, 1.0f, 5.0f, 1.0f), PartPose.offset(2.0f, 2.5f, 2.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition caveBat() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0f, -3.0f, -3.0f, 6.0f, 7.0f, 6.0f), PartPose.offset(0.0f, 10.0f, 0.0f));
        PartDefinition p1 = p0.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 33).addBox(-2.5f, -4.0f, -3.0f, 5.0f, 4.0f, 5.0f).texOffs(20, 33).addBox(-2.5f, -6.5f, -1.0f, 1.5f, 3.0f, 1.0f).texOffs(26, 33).addBox(1.0f, -6.5f, -1.0f, 1.5f, 3.0f, 1.0f).texOffs(6, 44).addBox(-1.8f, -2.8f, -3.3f, 1.0f, 1.0f, 0.5f).texOffs(10, 44).addBox(0.8f, -2.8f, -3.3f, 1.0f, 1.0f, 0.5f).texOffs(0, 44).addBox(-1.0f, -1.0f, -3.6f, 2.0f, 1.0f, 1.0f), PartPose.offset(0.0f, -3.0f, -1.0f));
        PartDefinition p2 = p0.addOrReplaceChild("wing_l", CubeListBuilder.create().texOffs(0, 13).addBox(-15.0f, 0.0f, -1.0f, 15.0f, 0.6f, 9.0f).texOffs(0, 42).addBox(-15.0f, -0.5f, -1.0f, 15.0f, 1.0f, 1.0f), PartPose.offsetAndRotation(-3.0f, -2.0f, 0.0f, 0.0f, 0.0f, 0.175f));
        PartDefinition p3 = p0.addOrReplaceChild("wing_r", CubeListBuilder.create().texOffs(0, 23).addBox(0.0f, 0.0f, -1.0f, 15.0f, 0.6f, 9.0f).texOffs(32, 42).addBox(0.0f, -0.5f, -1.0f, 15.0f, 1.0f, 1.0f), PartPose.offsetAndRotation(3.0f, -2.0f, 0.0f, 0.0f, 0.0f, -0.175f));
        PartDefinition p4 = p0.addOrReplaceChild("leg_fl", CubeListBuilder.create().texOffs(32, 33).addBox(-0.5f, 0.0f, -0.5f, 1.0f, 3.0f, 1.0f), PartPose.offset(-1.5f, 4.0f, 1.0f));
        PartDefinition p5 = p0.addOrReplaceChild("leg_fr", CubeListBuilder.create().texOffs(36, 33).addBox(-0.5f, 0.0f, -0.5f, 1.0f, 3.0f, 1.0f), PartPose.offset(1.5f, 4.0f, 1.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition duneScorpion() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -2.5f, -6.0f, 8.0f, 5.0f, 12.0f), PartPose.offset(0.0f, 19.0f, 0.0f));
        PartDefinition p1 = p0.addOrReplaceChild("head", CubeListBuilder.create().texOffs(16, 17).addBox(-3.0f, -2.0f, -3.0f, 6.0f, 3.5f, 3.0f).texOffs(42, 36).addBox(-2.0f, -2.6f, -2.6f, 1.0f, 1.0f, 1.0f).texOffs(46, 36).addBox(1.0f, -2.6f, -2.6f, 1.0f, 1.0f, 1.0f), PartPose.offset(0.0f, 0.0f, -6.0f));
        PartDefinition p2 = p0.addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(40, 0).addBox(-1.5f, -1.5f, 0.0f, 3.0f, 3.0f, 6.0f), PartPose.offsetAndRotation(0.0f, -1.5f, 6.0f, 0.96f, 0.0f, 0.0f));
        PartDefinition p3 = p2.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(0, 17).addBox(-1.2f, -1.2f, 0.0f, 2.4f, 2.4f, 5.0f), PartPose.offsetAndRotation(0.0f, 0.0f, 6.0f, 0.698f, 0.0f, 0.0f));
        PartDefinition p4 = p3.addOrReplaceChild("tail3", CubeListBuilder.create().texOffs(48, 25).addBox(-1.0f, -1.0f, 0.0f, 2.0f, 2.0f, 4.0f).texOffs(16, 32).addBox(-0.5f, -0.5f, 4.0f, 1.0f, 1.0f, 2.0f), PartPose.offsetAndRotation(0.0f, 0.0f, 5.0f, 0.785f, 0.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("claw_l", CubeListBuilder.create().texOffs(34, 17).addBox(-3.0f, -1.0f, -5.0f, 3.0f, 2.0f, 5.0f).texOffs(0, 25).addBox(-4.0f, -1.5f, -9.0f, 4.0f, 3.0f, 4.0f).texOffs(0, 32).addBox(-4.0f, -1.0f, -11.0f, 2.0f, 2.0f, 2.0f), PartPose.offsetAndRotation(-4.0f, 19.0f, -6.0f, 0.0f, -0.349f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("claw_r", CubeListBuilder.create().texOffs(16, 25).addBox(0.0f, -1.0f, -5.0f, 3.0f, 2.0f, 5.0f).texOffs(32, 25).addBox(0.0f, -1.5f, -9.0f, 4.0f, 3.0f, 4.0f).texOffs(8, 32).addBox(2.0f, -1.0f, -11.0f, 2.0f, 2.0f, 2.0f), PartPose.offsetAndRotation(4.0f, 19.0f, -6.0f, 0.0f, 0.349f, 0.0f));
        PartDefinition p7 = root.addOrReplaceChild("leg_fl", CubeListBuilder.create().texOffs(22, 32).addBox(-6.0f, -0.5f, -0.5f, 6.0f, 1.0f, 1.0f), PartPose.offsetAndRotation(-4.0f, 20.0f, -3.0f, 0.0f, 0.0f, -0.611f));
        PartDefinition p8 = root.addOrReplaceChild("leg_ml", CubeListBuilder.create().texOffs(36, 32).addBox(-6.0f, -0.5f, -0.5f, 6.0f, 1.0f, 1.0f), PartPose.offsetAndRotation(-4.0f, 20.0f, 0.0f, 0.0f, 0.0f, -0.611f));
        PartDefinition p9 = root.addOrReplaceChild("leg_bl", CubeListBuilder.create().texOffs(50, 32).addBox(-6.0f, -0.5f, -0.5f, 6.0f, 1.0f, 1.0f), PartPose.offsetAndRotation(-4.0f, 20.0f, 3.0f, 0.0f, 0.0f, -0.611f));
        PartDefinition p10 = root.addOrReplaceChild("leg_fr", CubeListBuilder.create().texOffs(0, 36).addBox(0.0f, -0.5f, -0.5f, 6.0f, 1.0f, 1.0f), PartPose.offsetAndRotation(4.0f, 20.0f, -3.0f, 0.0f, 0.0f, 0.611f));
        PartDefinition p11 = root.addOrReplaceChild("leg_mr", CubeListBuilder.create().texOffs(14, 36).addBox(0.0f, -0.5f, -0.5f, 6.0f, 1.0f, 1.0f), PartPose.offsetAndRotation(4.0f, 20.0f, 0.0f, 0.0f, 0.0f, 0.611f));
        PartDefinition p12 = root.addOrReplaceChild("leg_br", CubeListBuilder.create().texOffs(28, 36).addBox(0.0f, -0.5f, -0.5f, 6.0f, 1.0f, 1.0f), PartPose.offsetAndRotation(4.0f, 20.0f, 3.0f, 0.0f, 0.0f, 0.611f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition snapjaw() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("seg1", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5f, -12.0f, -1.5f, 3.0f, 12.0f, 3.0f), PartPose.offset(0.0f, 24.0f, 0.0f));
        PartDefinition p1 = p0.addOrReplaceChild("head", CubeListBuilder.create().texOffs(12, 0).addBox(-5.0f, -4.0f, -9.0f, 10.0f, 4.0f, 10.0f).texOffs(30, 37).addBox(-4.5f, 0.0f, -9.0f, 1.0f, 1.5f, 1.0f).texOffs(34, 37).addBox(-2.5f, 0.0f, -9.0f, 1.0f, 1.5f, 1.0f).texOffs(38, 37).addBox(-0.5f, 0.0f, -9.0f, 1.0f, 1.5f, 1.0f).texOffs(42, 37).addBox(1.5f, 0.0f, -9.0f, 1.0f, 1.5f, 1.0f).texOffs(46, 37).addBox(3.5f, 0.0f, -9.0f, 1.0f, 1.5f, 1.0f), PartPose.offset(0.0f, -12.0f, 0.0f));
        PartDefinition p2 = p1.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(0, 15).addBox(-5.0f, 0.0f, -10.0f, 10.0f, 3.0f, 10.0f).texOffs(50, 37).addBox(-3.5f, -1.5f, -9.5f, 1.0f, 1.5f, 1.0f).texOffs(54, 37).addBox(-1.5f, -1.5f, -9.5f, 1.0f, 1.5f, 1.0f).texOffs(58, 37).addBox(0.5f, -1.5f, -9.5f, 1.0f, 1.5f, 1.0f).texOffs(0, 44).addBox(2.5f, -1.5f, -9.5f, 1.0f, 1.5f, 1.0f).texOffs(0, 28).addBox(-4.0f, -0.2f, -9.0f, 8.0f, 0.5f, 8.0f), PartPose.offset(0.0f, 0.0f, 1.0f));
        PartDefinition p3 = root.addOrReplaceChild("fin_l", CubeListBuilder.create().texOffs(32, 28).addBox(-9.0f, 0.0f, -3.0f, 9.0f, 0.5f, 6.0f), PartPose.offsetAndRotation(-1.5f, 23.5f, 0.0f, 0.0f, 0.0f, 0.436f));
        PartDefinition p4 = root.addOrReplaceChild("fin_r", CubeListBuilder.create().texOffs(0, 37).addBox(0.0f, 0.0f, -3.0f, 9.0f, 0.5f, 6.0f), PartPose.offsetAndRotation(1.5f, 23.5f, 0.0f, 0.0f, 0.0f, -0.436f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition livingStump() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0f, 0.0f, -5.0f, 10.0f, 16.0f, 10.0f).texOffs(0, 26).addBox(-5.5f, -2.0f, -5.5f, 11.0f, 2.0f, 11.0f).texOffs(0, 47).addBox(-3.0f, 4.0f, -5.3f, 2.0f, 2.0f, 0.5f).texOffs(6, 47).addBox(1.0f, 4.0f, -5.3f, 2.0f, 2.0f, 0.5f).texOffs(16, 39).addBox(-2.5f, 9.0f, -5.3f, 5.0f, 1.5f, 0.5f), PartPose.offset(0.0f, 4.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("arm_l", CubeListBuilder.create().texOffs(40, 0).addBox(-3.0f, -1.0f, -1.5f, 3.0f, 13.0f, 3.0f).texOffs(28, 39).addBox(-5.0f, 6.0f, -1.0f, 3.0f, 1.0f, 2.0f), PartPose.offsetAndRotation(-5.0f, 8.0f, 0.0f, 0.0f, 0.0f, 0.262f));
        PartDefinition p2 = root.addOrReplaceChild("arm_r", CubeListBuilder.create().texOffs(52, 0).addBox(0.0f, -1.0f, -1.5f, 3.0f, 13.0f, 3.0f).texOffs(38, 39).addBox(2.0f, 9.0f, -1.0f, 3.0f, 1.0f, 2.0f), PartPose.offsetAndRotation(5.0f, 8.0f, 0.0f, 0.0f, 0.0f, -0.262f));
        PartDefinition p3 = root.addOrReplaceChild("leg_fl", CubeListBuilder.create().texOffs(44, 26).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 4.0f, 4.0f).texOffs(48, 39).addBox(-3.5f, 3.0f, -3.0f, 2.0f, 1.0f, 2.0f), PartPose.offset(-2.5f, 20.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("leg_fr", CubeListBuilder.create().texOffs(0, 39).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 4.0f, 4.0f).texOffs(56, 39).addBox(1.5f, 3.0f, 1.0f, 2.0f, 1.0f, 2.0f), PartPose.offset(2.5f, 20.0f, 0.0f));
        return LayerDefinition.create(data, 64, 64);
    }

    public static LayerDefinition shipSloop() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("keel", CubeListBuilder.create().texOffs(0, 72).addBox(-10.0f, 0.0f, -26.0f, 20.0f, 3.0f, 52.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("side_left", CubeListBuilder.create().texOffs(8, 0).addBox(-12.0f, -10.0f, -26.0f, 2.0f, 13.0f, 52.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("side_right", CubeListBuilder.create().texOffs(116, 0).addBox(10.0f, -10.0f, -26.0f, 2.0f, 13.0f, 52.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("stern", CubeListBuilder.create().texOffs(44, 209).addBox(-12.0f, -10.0f, -28.0f, 24.0f, 13.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("bow", CubeListBuilder.create().texOffs(110, 181).addBox(-8.0f, -10.0f, 26.0f, 16.0f, 13.0f, 6.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("bow_tip", CubeListBuilder.create().texOffs(210, 181).addBox(-5.0f, -9.0f, 32.0f, 10.0f, 11.0f, 5.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("deck", CubeListBuilder.create().texOffs(108, 127).addBox(-10.0f, -4.0f, -26.0f, 20.0f, 1.0f, 52.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p7 = root.addOrReplaceChild("rail_left", CubeListBuilder.create().texOffs(144, 72).addBox(-12.0f, -12.0f, -26.0f, 2.0f, 2.0f, 52.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p8 = root.addOrReplaceChild("rail_right", CubeListBuilder.create().texOffs(0, 127).addBox(10.0f, -12.0f, -26.0f, 2.0f, 2.0f, 52.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p9 = root.addOrReplaceChild("cabin", CubeListBuilder.create().texOffs(154, 181).addBox(-9.0f, -17.0f, -26.0f, 18.0f, 8.0f, 10.0f).texOffs(96, 209).addBox(-10.0f, -18.0f, -27.0f, 20.0f, 1.0f, 12.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p10 = root.addOrReplaceChild("mast1", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, -74.0f, 4.0f, 2.0f, 70.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p11 = root.addOrReplaceChild("yard1", CubeListBuilder.create().texOffs(0, 225).addBox(-18.0f, -62.0f, 3.0f, 36.0f, 2.0f, 2.0f).texOffs(76, 225).addBox(-16.0f, -39.0f, 3.0f, 32.0f, 2.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p12 = root.addOrReplaceChild("sail1", CubeListBuilder.create().texOffs(0, 181).addBox(-17.0f, 0.0f, 6.0f, 34.0f, 27.0f, 1.0f), PartPose.offset(0.0f, -60.0f, 0.0f));
        PartDefinition p13 = root.addOrReplaceChild("bowsprit", CubeListBuilder.create().texOffs(70, 181).addBox(-1.0f, -12.0f, 34.0f, 2.0f, 2.0f, 18.0f), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.244f, 0.0f, 0.0f));
        PartDefinition p14 = root.addOrReplaceChild("figurehead", CubeListBuilder.create().texOffs(160, 209).addBox(-1.5f, -8.0f, 36.0f, 3.0f, 6.0f, 3.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p15 = root.addOrReplaceChild("flag", CubeListBuilder.create().texOffs(0, 209).addBox(0.0f, -74.0f, -6.0f, 1.0f, 6.0f, 10.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p16 = root.addOrReplaceChild("emblem", CubeListBuilder.create().texOffs(22, 209).addBox(0.0f, 0.0f, -5.0f, 1.0f, 6.0f, 10.0f), PartPose.offsetAndRotation(0.0f, -53.0f, 7.6f, 0.0f, 1.571f, 0.0f));
        PartDefinition p17 = root.addOrReplaceChild("cannon_l1", CubeListBuilder.create().texOffs(172, 209).addBox(-14.0f, -6.0f, -14.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p18 = root.addOrReplaceChild("cannon_r1", CubeListBuilder.create().texOffs(184, 209).addBox(12.0f, -6.0f, -14.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p19 = root.addOrReplaceChild("cannon_l2", CubeListBuilder.create().texOffs(196, 209).addBox(-14.0f, -6.0f, 14.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p20 = root.addOrReplaceChild("cannon_r2", CubeListBuilder.create().texOffs(208, 209).addBox(12.0f, -6.0f, 14.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        return LayerDefinition.create(data, 256, 256);
    }

    public static LayerDefinition shipSchooner() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("keel", CubeListBuilder.create().texOffs(8, 83).addBox(-10.0f, 0.0f, -30.0f, 20.0f, 3.0f, 60.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("side_left", CubeListBuilder.create().texOffs(8, 0).addBox(-12.0f, -10.0f, -30.0f, 2.0f, 13.0f, 60.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("side_right", CubeListBuilder.create().texOffs(132, 0).addBox(10.0f, -10.0f, -30.0f, 2.0f, 13.0f, 60.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("stern", CubeListBuilder.create().texOffs(44, 304).addBox(-12.0f, -10.0f, -32.0f, 24.0f, 13.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("bow", CubeListBuilder.create().texOffs(106, 277).addBox(-8.0f, -10.0f, 30.0f, 16.0f, 13.0f, 6.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("bow_tip", CubeListBuilder.create().texOffs(206, 277).addBox(-5.0f, -9.0f, 36.0f, 10.0f, 11.0f, 5.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("deck", CubeListBuilder.create().texOffs(0, 216).addBox(-10.0f, -4.0f, -30.0f, 20.0f, 1.0f, 60.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p7 = root.addOrReplaceChild("rail_left", CubeListBuilder.create().texOffs(0, 154).addBox(-12.0f, -12.0f, -30.0f, 2.0f, 2.0f, 60.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p8 = root.addOrReplaceChild("rail_right", CubeListBuilder.create().texOffs(124, 154).addBox(10.0f, -12.0f, -30.0f, 2.0f, 2.0f, 60.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p9 = root.addOrReplaceChild("cabin", CubeListBuilder.create().texOffs(150, 277).addBox(-9.0f, -17.0f, -30.0f, 18.0f, 8.0f, 10.0f).texOffs(96, 304).addBox(-10.0f, -18.0f, -31.0f, 20.0f, 1.0f, 12.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p10 = root.addOrReplaceChild("mast1", CubeListBuilder.create().texOffs(0, 83).addBox(-1.0f, -73.0f, -11.0f, 2.0f, 69.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p11 = root.addOrReplaceChild("yard1", CubeListBuilder.create().texOffs(144, 320).addBox(-15.5f, -61.0f, -12.0f, 31.0f, 2.0f, 2.0f).texOffs(0, 324).addBox(-13.5f, -38.0f, -12.0f, 27.0f, 2.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p12 = root.addOrReplaceChild("sail1", CubeListBuilder.create().texOffs(0, 277).addBox(-14.5f, 0.0f, -9.0f, 29.0f, 26.0f, 1.0f), PartPose.offset(0.0f, -59.0f, 0.0f));
        PartDefinition p13 = root.addOrReplaceChild("mast2", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, -85.0f, 12.0f, 2.0f, 81.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p14 = root.addOrReplaceChild("yard2", CubeListBuilder.create().texOffs(0, 320).addBox(-18.0f, -73.0f, 11.0f, 36.0f, 2.0f, 2.0f).texOffs(76, 320).addBox(-16.0f, -45.0f, 11.0f, 32.0f, 2.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p15 = root.addOrReplaceChild("sail2", CubeListBuilder.create().texOffs(160, 216).addBox(-17.0f, 0.0f, 14.0f, 34.0f, 33.0f, 1.0f), PartPose.offset(0.0f, -71.0f, 0.0f));
        PartDefinition p16 = root.addOrReplaceChild("bowsprit", CubeListBuilder.create().texOffs(60, 277).addBox(-1.0f, -12.0f, 38.0f, 2.0f, 2.0f, 21.0f), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.244f, 0.0f, 0.0f));
        PartDefinition p17 = root.addOrReplaceChild("figurehead", CubeListBuilder.create().texOffs(160, 304).addBox(-1.5f, -8.0f, 40.0f, 3.0f, 6.0f, 3.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p18 = root.addOrReplaceChild("flag", CubeListBuilder.create().texOffs(0, 304).addBox(0.0f, -85.0f, 2.0f, 1.0f, 6.0f, 10.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p19 = root.addOrReplaceChild("emblem", CubeListBuilder.create().texOffs(22, 304).addBox(0.0f, 0.0f, -5.0f, 1.0f, 6.0f, 10.0f), PartPose.offsetAndRotation(0.0f, -61.0f, 15.6f, 0.0f, 1.571f, 0.0f));
        PartDefinition p20 = root.addOrReplaceChild("cannon_l1", CubeListBuilder.create().texOffs(172, 304).addBox(-14.0f, -6.0f, -18.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p21 = root.addOrReplaceChild("cannon_r1", CubeListBuilder.create().texOffs(184, 304).addBox(12.0f, -6.0f, -18.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p22 = root.addOrReplaceChild("cannon_l2", CubeListBuilder.create().texOffs(196, 304).addBox(-14.0f, -6.0f, 18.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p23 = root.addOrReplaceChild("cannon_r2", CubeListBuilder.create().texOffs(208, 304).addBox(12.0f, -6.0f, 18.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        return LayerDefinition.create(data, 256, 512);
    }

    public static LayerDefinition shipBrigantine() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("keel", CubeListBuilder.create().texOffs(288, 0).addBox(-12.0f, 0.0f, -33.0f, 24.0f, 3.0f, 66.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("side_left", CubeListBuilder.create().texOffs(8, 0).addBox(-14.0f, -12.0f, -33.0f, 2.0f, 15.0f, 66.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("side_right", CubeListBuilder.create().texOffs(144, 0).addBox(12.0f, -12.0f, -33.0f, 2.0f, 15.0f, 66.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("stern", CubeListBuilder.create().texOffs(356, 159).addBox(-14.0f, -12.0f, -35.0f, 28.0f, 15.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("bow", CubeListBuilder.create().texOffs(206, 159).addBox(-10.0f, -12.0f, 33.0f, 20.0f, 15.0f, 6.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("bow_tip", CubeListBuilder.create().texOffs(322, 159).addBox(-6.0f, -11.0f, 39.0f, 12.0f, 13.0f, 5.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("deck", CubeListBuilder.create().texOffs(272, 91).addBox(-12.0f, -4.0f, -33.0f, 24.0f, 1.0f, 66.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p7 = root.addOrReplaceChild("rail_left", CubeListBuilder.create().texOffs(0, 91).addBox(-14.0f, -14.0f, -33.0f, 2.0f, 2.0f, 66.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p8 = root.addOrReplaceChild("rail_right", CubeListBuilder.create().texOffs(136, 91).addBox(12.0f, -14.0f, -33.0f, 2.0f, 2.0f, 66.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p9 = root.addOrReplaceChild("cabin", CubeListBuilder.create().texOffs(258, 159).addBox(-11.0f, -19.0f, -33.0f, 22.0f, 8.0f, 10.0f).texOffs(0, 197).addBox(-12.0f, -20.0f, -34.0f, 24.0f, 1.0f, 12.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p10 = root.addOrReplaceChild("mast1", CubeListBuilder.create().texOffs(280, 0).addBox(-1.0f, -80.0f, -12.0f, 2.0f, 76.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p11 = root.addOrReplaceChild("yard1", CubeListBuilder.create().texOffs(328, 197).addBox(-18.5f, -68.0f, -13.0f, 37.0f, 2.0f, 2.0f).texOffs(406, 197).addBox(-16.5f, -42.0f, -13.0f, 33.0f, 2.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p12 = root.addOrReplaceChild("sail1", CubeListBuilder.create().texOffs(84, 159).addBox(-17.5f, 0.0f, -10.0f, 35.0f, 30.0f, 1.0f), PartPose.offset(0.0f, -66.0f, 0.0f));
        PartDefinition p13 = root.addOrReplaceChild("mast2", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, -93.0f, 13.0f, 2.0f, 89.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p14 = root.addOrReplaceChild("yard2", CubeListBuilder.create().texOffs(156, 197).addBox(-21.5f, -81.0f, 12.0f, 43.0f, 2.0f, 2.0f).texOffs(246, 197).addBox(-19.5f, -49.0f, 12.0f, 39.0f, 2.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p15 = root.addOrReplaceChild("sail2", CubeListBuilder.create().texOffs(0, 159).addBox(-20.5f, 0.0f, 15.0f, 41.0f, 37.0f, 1.0f), PartPose.offset(0.0f, -79.0f, 0.0f));
        PartDefinition p16 = root.addOrReplaceChild("bowsprit", CubeListBuilder.create().texOffs(156, 159).addBox(-1.0f, -14.0f, 41.0f, 2.0f, 2.0f, 23.0f), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.244f, 0.0f, 0.0f));
        PartDefinition p17 = root.addOrReplaceChild("figurehead", CubeListBuilder.create().texOffs(72, 197).addBox(-1.5f, -10.0f, 43.0f, 3.0f, 6.0f, 3.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p18 = root.addOrReplaceChild("flag", CubeListBuilder.create().texOffs(416, 159).addBox(0.0f, -93.0f, 3.0f, 1.0f, 6.0f, 10.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p19 = root.addOrReplaceChild("emblem", CubeListBuilder.create().texOffs(438, 159).addBox(0.0f, 0.0f, -5.0f, 1.0f, 6.0f, 10.0f), PartPose.offsetAndRotation(0.0f, -67.0f, 16.6f, 0.0f, 1.571f, 0.0f));
        PartDefinition p20 = root.addOrReplaceChild("cannon_l1", CubeListBuilder.create().texOffs(84, 197).addBox(-16.0f, -8.0f, -21.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p21 = root.addOrReplaceChild("cannon_r1", CubeListBuilder.create().texOffs(96, 197).addBox(14.0f, -8.0f, -21.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p22 = root.addOrReplaceChild("cannon_l2", CubeListBuilder.create().texOffs(108, 197).addBox(-16.0f, -8.0f, 0.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p23 = root.addOrReplaceChild("cannon_r2", CubeListBuilder.create().texOffs(120, 197).addBox(14.0f, -8.0f, 0.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p24 = root.addOrReplaceChild("cannon_l3", CubeListBuilder.create().texOffs(132, 197).addBox(-16.0f, -8.0f, 21.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p25 = root.addOrReplaceChild("cannon_r3", CubeListBuilder.create().texOffs(144, 197).addBox(14.0f, -8.0f, 21.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        return LayerDefinition.create(data, 512, 256);
    }

    public static LayerDefinition shipBrig() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("keel", CubeListBuilder.create().texOffs(304, 0).addBox(-12.0f, 0.0f, -35.0f, 24.0f, 3.0f, 70.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("side_left", CubeListBuilder.create().texOffs(8, 0).addBox(-14.0f, -12.0f, -35.0f, 2.0f, 15.0f, 70.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("side_right", CubeListBuilder.create().texOffs(152, 0).addBox(12.0f, -12.0f, -35.0f, 2.0f, 15.0f, 70.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("stern", CubeListBuilder.create().texOffs(360, 169).addBox(-14.0f, -12.0f, -37.0f, 28.0f, 15.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("bow", CubeListBuilder.create().texOffs(210, 169).addBox(-10.0f, -12.0f, 35.0f, 20.0f, 15.0f, 6.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("bow_tip", CubeListBuilder.create().texOffs(326, 169).addBox(-6.0f, -11.0f, 41.0f, 12.0f, 13.0f, 5.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("deck", CubeListBuilder.create().texOffs(288, 97).addBox(-12.0f, -4.0f, -35.0f, 24.0f, 1.0f, 70.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p7 = root.addOrReplaceChild("rail_left", CubeListBuilder.create().texOffs(0, 97).addBox(-14.0f, -14.0f, -35.0f, 2.0f, 2.0f, 70.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p8 = root.addOrReplaceChild("rail_right", CubeListBuilder.create().texOffs(144, 97).addBox(12.0f, -14.0f, -35.0f, 2.0f, 2.0f, 70.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p9 = root.addOrReplaceChild("cabin", CubeListBuilder.create().texOffs(262, 169).addBox(-11.0f, -19.0f, -35.0f, 22.0f, 8.0f, 10.0f).texOffs(0, 210).addBox(-12.0f, -20.0f, -36.0f, 24.0f, 1.0f, 12.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p10 = root.addOrReplaceChild("mast1", CubeListBuilder.create().texOffs(296, 0).addBox(-1.0f, -85.0f, -13.0f, 2.0f, 81.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p11 = root.addOrReplaceChild("yard1", CubeListBuilder.create().texOffs(352, 210).addBox(-18.5f, -73.0f, -14.0f, 37.0f, 2.0f, 2.0f).texOffs(430, 210).addBox(-16.5f, -45.0f, -14.0f, 33.0f, 2.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p12 = root.addOrReplaceChild("sail1", CubeListBuilder.create().texOffs(84, 169).addBox(-17.5f, 0.0f, -11.0f, 35.0f, 33.0f, 1.0f), PartPose.offset(0.0f, -71.0f, 0.0f));
        PartDefinition p13 = root.addOrReplaceChild("mast2", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, -99.0f, 14.0f, 2.0f, 95.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p14 = root.addOrReplaceChild("yard2", CubeListBuilder.create().texOffs(180, 210).addBox(-21.5f, -87.0f, 13.0f, 43.0f, 2.0f, 2.0f).texOffs(270, 210).addBox(-19.5f, -52.0f, 13.0f, 39.0f, 2.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p15 = root.addOrReplaceChild("sail2", CubeListBuilder.create().texOffs(0, 169).addBox(-20.5f, 0.0f, 16.0f, 41.0f, 40.0f, 1.0f), PartPose.offset(0.0f, -85.0f, 0.0f));
        PartDefinition p16 = root.addOrReplaceChild("bowsprit", CubeListBuilder.create().texOffs(156, 169).addBox(-1.0f, -14.0f, 43.0f, 2.0f, 2.0f, 25.0f), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.244f, 0.0f, 0.0f));
        PartDefinition p17 = root.addOrReplaceChild("figurehead", CubeListBuilder.create().texOffs(72, 210).addBox(-1.5f, -10.0f, 45.0f, 3.0f, 6.0f, 3.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p18 = root.addOrReplaceChild("flag", CubeListBuilder.create().texOffs(420, 169).addBox(0.0f, -99.0f, 4.0f, 1.0f, 6.0f, 10.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p19 = root.addOrReplaceChild("emblem", CubeListBuilder.create().texOffs(442, 169).addBox(0.0f, 0.0f, -5.0f, 1.0f, 6.0f, 10.0f), PartPose.offsetAndRotation(0.0f, -71.0f, 17.6f, 0.0f, 1.571f, 0.0f));
        PartDefinition p20 = root.addOrReplaceChild("cannon_l1", CubeListBuilder.create().texOffs(84, 210).addBox(-16.0f, -8.0f, -23.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p21 = root.addOrReplaceChild("cannon_r1", CubeListBuilder.create().texOffs(96, 210).addBox(14.0f, -8.0f, -23.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p22 = root.addOrReplaceChild("cannon_l2", CubeListBuilder.create().texOffs(108, 210).addBox(-16.0f, -8.0f, -8.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p23 = root.addOrReplaceChild("cannon_r2", CubeListBuilder.create().texOffs(120, 210).addBox(14.0f, -8.0f, -8.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p24 = root.addOrReplaceChild("cannon_l3", CubeListBuilder.create().texOffs(132, 210).addBox(-16.0f, -8.0f, 8.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p25 = root.addOrReplaceChild("cannon_r3", CubeListBuilder.create().texOffs(144, 210).addBox(14.0f, -8.0f, 8.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p26 = root.addOrReplaceChild("cannon_l4", CubeListBuilder.create().texOffs(156, 210).addBox(-16.0f, -8.0f, 23.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p27 = root.addOrReplaceChild("cannon_r4", CubeListBuilder.create().texOffs(168, 210).addBox(14.0f, -8.0f, 23.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        return LayerDefinition.create(data, 512, 256);
    }

    public static LayerDefinition shipFrigate() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("keel", CubeListBuilder.create().texOffs(0, 110).addBox(-13.0f, 0.0f, -40.0f, 26.0f, 3.0f, 80.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("side_left", CubeListBuilder.create().texOffs(8, 0).addBox(-15.0f, -14.0f, -40.0f, 2.0f, 17.0f, 80.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("side_right", CubeListBuilder.create().texOffs(172, 0).addBox(13.0f, -14.0f, -40.0f, 2.0f, 17.0f, 80.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("stern", CubeListBuilder.create().texOffs(308, 275).addBox(-15.0f, -14.0f, -42.0f, 30.0f, 17.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("bow", CubeListBuilder.create().texOffs(216, 275).addBox(-11.0f, -14.0f, 40.0f, 22.0f, 17.0f, 6.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("bow_tip", CubeListBuilder.create().texOffs(272, 275).addBox(-6.5f, -13.0f, 46.0f, 13.0f, 15.0f, 5.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("deck", CubeListBuilder.create().texOffs(164, 193).addBox(-13.0f, -4.0f, -40.0f, 26.0f, 1.0f, 80.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p7 = root.addOrReplaceChild("rail_left", CubeListBuilder.create().texOffs(212, 110).addBox(-15.0f, -16.0f, -40.0f, 2.0f, 2.0f, 80.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p8 = root.addOrReplaceChild("rail_right", CubeListBuilder.create().texOffs(0, 193).addBox(13.0f, -16.0f, -40.0f, 2.0f, 2.0f, 80.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p9 = root.addOrReplaceChild("cabin", CubeListBuilder.create().texOffs(372, 275).addBox(-12.0f, -21.0f, -40.0f, 24.0f, 8.0f, 10.0f).texOffs(0, 315).addBox(-13.0f, -22.0f, -41.0f, 26.0f, 1.0f, 12.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p10 = root.addOrReplaceChild("mast1", CubeListBuilder.create().texOffs(336, 0).addBox(-1.0f, -96.0f, -21.0f, 2.0f, 92.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p11 = root.addOrReplaceChild("yard1", CubeListBuilder.create().texOffs(396, 315).addBox(-20.0f, -84.0f, -22.0f, 40.0f, 2.0f, 2.0f).texOffs(84, 328).addBox(-18.0f, -51.0f, -22.0f, 36.0f, 2.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p12 = root.addOrReplaceChild("sail1", CubeListBuilder.create().texOffs(0, 275).addBox(-19.0f, 0.0f, -19.0f, 38.0f, 39.0f, 1.0f), PartPose.offset(0.0f, -82.0f, 0.0f));
        PartDefinition p13 = root.addOrReplaceChild("mast2", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, -112.0f, 3.0f, 2.0f, 108.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p14 = root.addOrReplaceChild("yard2", CubeListBuilder.create().texOffs(208, 315).addBox(-23.5f, -100.0f, 2.0f, 47.0f, 2.0f, 2.0f).texOffs(306, 315).addBox(-21.5f, -59.0f, 2.0f, 43.0f, 2.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p15 = root.addOrReplaceChild("sail2", CubeListBuilder.create().texOffs(376, 193).addBox(-22.5f, 0.0f, 5.0f, 45.0f, 47.0f, 1.0f), PartPose.offset(0.0f, -98.0f, 0.0f));
        PartDefinition p16 = root.addOrReplaceChild("mast3", CubeListBuilder.create().texOffs(344, 0).addBox(-1.0f, -96.0f, 24.0f, 2.0f, 92.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p17 = root.addOrReplaceChild("yard3", CubeListBuilder.create().texOffs(0, 328).addBox(-20.0f, -84.0f, 23.0f, 40.0f, 2.0f, 2.0f).texOffs(160, 328).addBox(-18.0f, -51.0f, 23.0f, 36.0f, 2.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p18 = root.addOrReplaceChild("sail3", CubeListBuilder.create().texOffs(78, 275).addBox(-19.0f, 0.0f, 26.0f, 38.0f, 39.0f, 1.0f), PartPose.offset(0.0f, -82.0f, 0.0f));
        PartDefinition p19 = root.addOrReplaceChild("bowsprit", CubeListBuilder.create().texOffs(156, 275).addBox(-1.0f, -16.0f, 48.0f, 2.0f, 2.0f, 28.0f), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.244f, 0.0f, 0.0f));
        PartDefinition p20 = root.addOrReplaceChild("figurehead", CubeListBuilder.create().texOffs(76, 315).addBox(-1.5f, -12.0f, 50.0f, 3.0f, 6.0f, 3.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p21 = root.addOrReplaceChild("flag", CubeListBuilder.create().texOffs(440, 275).addBox(0.0f, -112.0f, -7.0f, 1.0f, 6.0f, 10.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p22 = root.addOrReplaceChild("emblem", CubeListBuilder.create().texOffs(462, 275).addBox(0.0f, 0.0f, -5.0f, 1.0f, 6.0f, 10.0f), PartPose.offsetAndRotation(0.0f, -81.0f, 6.6f, 0.0f, 1.571f, 0.0f));
        PartDefinition p23 = root.addOrReplaceChild("cannon_l1", CubeListBuilder.create().texOffs(88, 315).addBox(-17.0f, -10.0f, -28.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p24 = root.addOrReplaceChild("cannon_r1", CubeListBuilder.create().texOffs(100, 315).addBox(15.0f, -10.0f, -28.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p25 = root.addOrReplaceChild("cannon_l2", CubeListBuilder.create().texOffs(112, 315).addBox(-17.0f, -10.0f, -14.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p26 = root.addOrReplaceChild("cannon_r2", CubeListBuilder.create().texOffs(124, 315).addBox(15.0f, -10.0f, -14.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p27 = root.addOrReplaceChild("cannon_l3", CubeListBuilder.create().texOffs(136, 315).addBox(-17.0f, -10.0f, 0.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p28 = root.addOrReplaceChild("cannon_r3", CubeListBuilder.create().texOffs(148, 315).addBox(15.0f, -10.0f, 0.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p29 = root.addOrReplaceChild("cannon_l4", CubeListBuilder.create().texOffs(160, 315).addBox(-17.0f, -10.0f, 14.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p30 = root.addOrReplaceChild("cannon_r4", CubeListBuilder.create().texOffs(172, 315).addBox(15.0f, -10.0f, 14.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p31 = root.addOrReplaceChild("cannon_l5", CubeListBuilder.create().texOffs(184, 315).addBox(-17.0f, -10.0f, 28.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p32 = root.addOrReplaceChild("cannon_r5", CubeListBuilder.create().texOffs(196, 315).addBox(15.0f, -10.0f, 28.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        return LayerDefinition.create(data, 512, 512);
    }

    public static LayerDefinition shipGalleon() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("keel", CubeListBuilder.create().texOffs(0, 115).addBox(-15.0f, 0.0f, -42.0f, 30.0f, 3.0f, 84.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("side_left", CubeListBuilder.create().texOffs(8, 0).addBox(-17.0f, -17.0f, -42.0f, 2.0f, 20.0f, 84.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("side_right", CubeListBuilder.create().texOffs(180, 0).addBox(15.0f, -17.0f, -42.0f, 2.0f, 20.0f, 84.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("stern", CubeListBuilder.create().texOffs(104, 330).addBox(-17.0f, -17.0f, -44.0f, 34.0f, 20.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("bow", CubeListBuilder.create().texOffs(0, 330).addBox(-13.0f, -17.0f, 42.0f, 26.0f, 20.0f, 6.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("bow_tip", CubeListBuilder.create().texOffs(64, 330).addBox(-7.5f, -16.0f, 48.0f, 15.0f, 18.0f, 5.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("deck", CubeListBuilder.create().texOffs(172, 202).addBox(-15.0f, -4.0f, -42.0f, 30.0f, 1.0f, 84.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p7 = root.addOrReplaceChild("rail_left", CubeListBuilder.create().texOffs(228, 115).addBox(-17.0f, -19.0f, -42.0f, 2.0f, 2.0f, 84.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p8 = root.addOrReplaceChild("rail_right", CubeListBuilder.create().texOffs(0, 202).addBox(15.0f, -19.0f, -42.0f, 2.0f, 2.0f, 84.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p9 = root.addOrReplaceChild("cabin", CubeListBuilder.create().texOffs(180, 288).addBox(-14.0f, -30.0f, -42.0f, 28.0f, 14.0f, 24.0f).texOffs(346, 288).addBox(-15.0f, -31.0f, -43.0f, 30.0f, 1.0f, 26.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p10 = root.addOrReplaceChild("forecastle", CubeListBuilder.create().texOffs(176, 330).addBox(-13.0f, -23.0f, 30.0f, 26.0f, 6.0f, 12.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p11 = root.addOrReplaceChild("mast1", CubeListBuilder.create().texOffs(352, 0).addBox(-1.0f, -100.0f, -22.0f, 2.0f, 96.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p12 = root.addOrReplaceChild("yard1", CubeListBuilder.create().texOffs(216, 356).addBox(-23.0f, -88.0f, -23.0f, 46.0f, 2.0f, 2.0f).texOffs(408, 356).addBox(-21.0f, -53.0f, -23.0f, 42.0f, 2.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p13 = root.addOrReplaceChild("sail1", CubeListBuilder.create().texOffs(0, 288).addBox(-22.0f, 0.0f, -20.0f, 44.0f, 41.0f, 1.0f), PartPose.offset(0.0f, -86.0f, 0.0f));
        PartDefinition p14 = root.addOrReplaceChild("mast2", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, -117.0f, 3.0f, 2.0f, 113.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p15 = root.addOrReplaceChild("yard2", CubeListBuilder.create().texOffs(0, 356).addBox(-27.0f, -105.0f, 2.0f, 54.0f, 2.0f, 2.0f).texOffs(112, 356).addBox(-25.0f, -62.0f, 2.0f, 50.0f, 2.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p16 = root.addOrReplaceChild("sail2", CubeListBuilder.create().texOffs(400, 202).addBox(-26.0f, 0.0f, 5.0f, 52.0f, 50.0f, 1.0f), PartPose.offset(0.0f, -103.0f, 0.0f));
        PartDefinition p17 = root.addOrReplaceChild("mast3", CubeListBuilder.create().texOffs(360, 0).addBox(-1.0f, -100.0f, 25.0f, 2.0f, 96.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p18 = root.addOrReplaceChild("yard3", CubeListBuilder.create().texOffs(312, 356).addBox(-23.0f, -88.0f, 24.0f, 46.0f, 2.0f, 2.0f).texOffs(0, 360).addBox(-21.0f, -53.0f, 24.0f, 42.0f, 2.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p19 = root.addOrReplaceChild("sail3", CubeListBuilder.create().texOffs(90, 288).addBox(-22.0f, 0.0f, 27.0f, 44.0f, 41.0f, 1.0f), PartPose.offset(0.0f, -86.0f, 0.0f));
        PartDefinition p20 = root.addOrReplaceChild("bowsprit", CubeListBuilder.create().texOffs(284, 288).addBox(-1.0f, -19.0f, 50.0f, 2.0f, 2.0f, 29.0f), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.244f, 0.0f, 0.0f));
        PartDefinition p21 = root.addOrReplaceChild("figurehead", CubeListBuilder.create().texOffs(296, 330).addBox(-1.5f, -15.0f, 52.0f, 3.0f, 6.0f, 3.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p22 = root.addOrReplaceChild("flag", CubeListBuilder.create().texOffs(252, 330).addBox(0.0f, -117.0f, -7.0f, 1.0f, 6.0f, 10.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p23 = root.addOrReplaceChild("emblem", CubeListBuilder.create().texOffs(274, 330).addBox(0.0f, 0.0f, -5.0f, 1.0f, 6.0f, 10.0f), PartPose.offsetAndRotation(0.0f, -85.0f, 6.6f, 0.0f, 1.571f, 0.0f));
        PartDefinition p24 = root.addOrReplaceChild("cannon_l1", CubeListBuilder.create().texOffs(308, 330).addBox(-19.0f, -13.0f, -30.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p25 = root.addOrReplaceChild("cannon_r1", CubeListBuilder.create().texOffs(320, 330).addBox(17.0f, -13.0f, -30.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p26 = root.addOrReplaceChild("cannon_l2", CubeListBuilder.create().texOffs(332, 330).addBox(-19.0f, -13.0f, -10.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p27 = root.addOrReplaceChild("cannon_r2", CubeListBuilder.create().texOffs(344, 330).addBox(17.0f, -13.0f, -10.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p28 = root.addOrReplaceChild("cannon_l3", CubeListBuilder.create().texOffs(356, 330).addBox(-19.0f, -13.0f, 10.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p29 = root.addOrReplaceChild("cannon_r3", CubeListBuilder.create().texOffs(368, 330).addBox(17.0f, -13.0f, 10.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p30 = root.addOrReplaceChild("cannon_l4", CubeListBuilder.create().texOffs(380, 330).addBox(-19.0f, -13.0f, 30.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p31 = root.addOrReplaceChild("cannon_r4", CubeListBuilder.create().texOffs(392, 330).addBox(17.0f, -13.0f, 30.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p32 = root.addOrReplaceChild("cannon_lb1", CubeListBuilder.create().texOffs(404, 330).addBox(-19.0f, -8.0f, -30.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p33 = root.addOrReplaceChild("cannon_rb1", CubeListBuilder.create().texOffs(416, 330).addBox(17.0f, -8.0f, -30.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p34 = root.addOrReplaceChild("cannon_lb2", CubeListBuilder.create().texOffs(428, 330).addBox(-19.0f, -8.0f, -10.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p35 = root.addOrReplaceChild("cannon_rb2", CubeListBuilder.create().texOffs(440, 330).addBox(17.0f, -8.0f, -10.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p36 = root.addOrReplaceChild("cannon_lb3", CubeListBuilder.create().texOffs(452, 330).addBox(-19.0f, -8.0f, 10.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p37 = root.addOrReplaceChild("cannon_rb3", CubeListBuilder.create().texOffs(464, 330).addBox(17.0f, -8.0f, 10.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p38 = root.addOrReplaceChild("cannon_lb4", CubeListBuilder.create().texOffs(476, 330).addBox(-19.0f, -8.0f, 30.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p39 = root.addOrReplaceChild("cannon_rb4", CubeListBuilder.create().texOffs(488, 330).addBox(17.0f, -8.0f, 30.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        return LayerDefinition.create(data, 512, 512);
    }

    public static LayerDefinition shipShipOfTheLine() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("keel", CubeListBuilder.create().texOffs(0, 132).addBox(-16.0f, 0.0f, -48.0f, 32.0f, 3.0f, 96.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p1 = root.addOrReplaceChild("side_left", CubeListBuilder.create().texOffs(8, 0).addBox(-18.0f, -21.0f, -48.0f, 2.0f, 24.0f, 96.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p2 = root.addOrReplaceChild("side_right", CubeListBuilder.create().texOffs(204, 0).addBox(16.0f, -21.0f, -48.0f, 2.0f, 24.0f, 96.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p3 = root.addOrReplaceChild("stern", CubeListBuilder.create().texOffs(232, 390).addBox(-18.0f, -21.0f, -50.0f, 36.0f, 24.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p4 = root.addOrReplaceChild("bow", CubeListBuilder.create().texOffs(122, 390).addBox(-14.0f, -21.0f, 48.0f, 28.0f, 24.0f, 6.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p5 = root.addOrReplaceChild("bow_tip", CubeListBuilder.create().texOffs(190, 390).addBox(-8.0f, -20.0f, 54.0f, 16.0f, 22.0f, 5.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p6 = root.addOrReplaceChild("deck", CubeListBuilder.create().texOffs(196, 231).addBox(-16.0f, -4.0f, -48.0f, 32.0f, 1.0f, 96.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p7 = root.addOrReplaceChild("rail_left", CubeListBuilder.create().texOffs(256, 132).addBox(-18.0f, -23.0f, -48.0f, 2.0f, 2.0f, 96.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p8 = root.addOrReplaceChild("rail_right", CubeListBuilder.create().texOffs(0, 231).addBox(16.0f, -23.0f, -48.0f, 2.0f, 2.0f, 96.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p9 = root.addOrReplaceChild("cabin", CubeListBuilder.create().texOffs(306, 329).addBox(-15.0f, -34.0f, -48.0f, 30.0f, 14.0f, 27.0f).texOffs(0, 390).addBox(-16.0f, -35.0f, -49.0f, 32.0f, 1.0f, 29.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p10 = root.addOrReplaceChild("forecastle", CubeListBuilder.create().texOffs(308, 390).addBox(-14.0f, -27.0f, 36.0f, 28.0f, 6.0f, 12.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p11 = root.addOrReplaceChild("mast1", CubeListBuilder.create().texOffs(400, 0).addBox(-1.0f, -115.0f, -25.0f, 2.0f, 111.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p12 = root.addOrReplaceChild("yard1", CubeListBuilder.create().texOffs(0, 426).addBox(-24.5f, -103.0f, -26.0f, 49.0f, 2.0f, 2.0f).texOffs(204, 426).addBox(-22.5f, -61.0f, -26.0f, 45.0f, 2.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p13 = root.addOrReplaceChild("sail1", CubeListBuilder.create().texOffs(114, 329).addBox(-23.5f, 0.0f, -23.0f, 47.0f, 49.0f, 1.0f), PartPose.offset(0.0f, -101.0f, 0.0f));
        PartDefinition p14 = root.addOrReplaceChild("mast2", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, -134.0f, 4.0f, 2.0f, 130.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p15 = root.addOrReplaceChild("yard2", CubeListBuilder.create().texOffs(276, 420).addBox(-29.0f, -122.0f, 3.0f, 58.0f, 2.0f, 2.0f).texOffs(396, 420).addBox(-27.0f, -72.0f, 3.0f, 54.0f, 2.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p16 = root.addOrReplaceChild("sail2", CubeListBuilder.create().texOffs(0, 329).addBox(-28.0f, 0.0f, 6.0f, 56.0f, 60.0f, 1.0f), PartPose.offset(0.0f, -120.0f, 0.0f));
        PartDefinition p17 = root.addOrReplaceChild("mast3", CubeListBuilder.create().texOffs(408, 0).addBox(-1.0f, -115.0f, 29.0f, 2.0f, 111.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p18 = root.addOrReplaceChild("yard3", CubeListBuilder.create().texOffs(102, 426).addBox(-24.5f, -103.0f, 28.0f, 49.0f, 2.0f, 2.0f).texOffs(298, 426).addBox(-22.5f, -61.0f, 28.0f, 45.0f, 2.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p19 = root.addOrReplaceChild("sail3", CubeListBuilder.create().texOffs(210, 329).addBox(-23.5f, 0.0f, 31.0f, 47.0f, 49.0f, 1.0f), PartPose.offset(0.0f, -101.0f, 0.0f));
        PartDefinition p20 = root.addOrReplaceChild("bowsprit", CubeListBuilder.create().texOffs(420, 329).addBox(-1.0f, -23.0f, 56.0f, 2.0f, 2.0f, 34.0f), PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.244f, 0.0f, 0.0f));
        PartDefinition p21 = root.addOrReplaceChild("figurehead", CubeListBuilder.create().texOffs(432, 390).addBox(-1.5f, -19.0f, 58.0f, 3.0f, 6.0f, 3.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p22 = root.addOrReplaceChild("flag", CubeListBuilder.create().texOffs(388, 390).addBox(0.0f, -134.0f, -6.0f, 1.0f, 6.0f, 10.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p23 = root.addOrReplaceChild("emblem", CubeListBuilder.create().texOffs(410, 390).addBox(0.0f, 0.0f, -5.0f, 1.0f, 6.0f, 10.0f), PartPose.offsetAndRotation(0.0f, -98.0f, 7.6f, 0.0f, 1.571f, 0.0f));
        PartDefinition p24 = root.addOrReplaceChild("cannon_l1", CubeListBuilder.create().texOffs(444, 390).addBox(-20.0f, -17.0f, -36.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p25 = root.addOrReplaceChild("cannon_r1", CubeListBuilder.create().texOffs(456, 390).addBox(18.0f, -17.0f, -36.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p26 = root.addOrReplaceChild("cannon_l2", CubeListBuilder.create().texOffs(468, 390).addBox(-20.0f, -17.0f, -24.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p27 = root.addOrReplaceChild("cannon_r2", CubeListBuilder.create().texOffs(480, 390).addBox(18.0f, -17.0f, -24.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p28 = root.addOrReplaceChild("cannon_l3", CubeListBuilder.create().texOffs(492, 390).addBox(-20.0f, -17.0f, -12.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p29 = root.addOrReplaceChild("cannon_r3", CubeListBuilder.create().texOffs(0, 420).addBox(18.0f, -17.0f, -12.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p30 = root.addOrReplaceChild("cannon_l4", CubeListBuilder.create().texOffs(12, 420).addBox(-20.0f, -17.0f, 0.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p31 = root.addOrReplaceChild("cannon_r4", CubeListBuilder.create().texOffs(24, 420).addBox(18.0f, -17.0f, 0.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p32 = root.addOrReplaceChild("cannon_l5", CubeListBuilder.create().texOffs(36, 420).addBox(-20.0f, -17.0f, 12.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p33 = root.addOrReplaceChild("cannon_r5", CubeListBuilder.create().texOffs(48, 420).addBox(18.0f, -17.0f, 12.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p34 = root.addOrReplaceChild("cannon_l6", CubeListBuilder.create().texOffs(60, 420).addBox(-20.0f, -17.0f, 24.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p35 = root.addOrReplaceChild("cannon_r6", CubeListBuilder.create().texOffs(72, 420).addBox(18.0f, -17.0f, 24.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p36 = root.addOrReplaceChild("cannon_l7", CubeListBuilder.create().texOffs(84, 420).addBox(-20.0f, -17.0f, 36.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p37 = root.addOrReplaceChild("cannon_r7", CubeListBuilder.create().texOffs(96, 420).addBox(18.0f, -17.0f, 36.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p38 = root.addOrReplaceChild("cannon_lb1", CubeListBuilder.create().texOffs(108, 420).addBox(-20.0f, -12.0f, -36.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p39 = root.addOrReplaceChild("cannon_rb1", CubeListBuilder.create().texOffs(120, 420).addBox(18.0f, -12.0f, -36.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p40 = root.addOrReplaceChild("cannon_lb2", CubeListBuilder.create().texOffs(132, 420).addBox(-20.0f, -12.0f, -24.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p41 = root.addOrReplaceChild("cannon_rb2", CubeListBuilder.create().texOffs(144, 420).addBox(18.0f, -12.0f, -24.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p42 = root.addOrReplaceChild("cannon_lb3", CubeListBuilder.create().texOffs(156, 420).addBox(-20.0f, -12.0f, -12.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p43 = root.addOrReplaceChild("cannon_rb3", CubeListBuilder.create().texOffs(168, 420).addBox(18.0f, -12.0f, -12.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p44 = root.addOrReplaceChild("cannon_lb4", CubeListBuilder.create().texOffs(180, 420).addBox(-20.0f, -12.0f, 0.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p45 = root.addOrReplaceChild("cannon_rb4", CubeListBuilder.create().texOffs(192, 420).addBox(18.0f, -12.0f, 0.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p46 = root.addOrReplaceChild("cannon_lb5", CubeListBuilder.create().texOffs(204, 420).addBox(-20.0f, -12.0f, 12.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p47 = root.addOrReplaceChild("cannon_rb5", CubeListBuilder.create().texOffs(216, 420).addBox(18.0f, -12.0f, 12.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p48 = root.addOrReplaceChild("cannon_lb6", CubeListBuilder.create().texOffs(228, 420).addBox(-20.0f, -12.0f, 24.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p49 = root.addOrReplaceChild("cannon_rb6", CubeListBuilder.create().texOffs(240, 420).addBox(18.0f, -12.0f, 24.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p50 = root.addOrReplaceChild("cannon_lb7", CubeListBuilder.create().texOffs(252, 420).addBox(-20.0f, -12.0f, 36.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition p51 = root.addOrReplaceChild("cannon_rb7", CubeListBuilder.create().texOffs(264, 420).addBox(18.0f, -12.0f, 36.0f, 2.0f, 2.0f, 4.0f), PartPose.offset(0.0f, 0.0f, 0.0f));
        return LayerDefinition.create(data, 512, 512);
    }
}
