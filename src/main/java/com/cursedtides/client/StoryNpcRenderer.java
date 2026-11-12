package com.cursedtides.client;

import com.cursedtides.CursedTides;
import com.cursedtides.entity.StoryNpcEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.WanderingTraderEntityRenderer;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.util.Identifier;

/** One texture per story character. */
public class StoryNpcRenderer extends WanderingTraderEntityRenderer {
    private static final Identifier KEEPER = CursedTides.id("textures/entity/keeper_wren.png");
    private static final Identifier HARBORMASTER = CursedTides.id("textures/entity/ysolde_vane.png");
    private static final Identifier BELLWRIGHT = CursedTides.id("textures/entity/tamsin_coil.png");
    private static final Identifier COMMODORE = CursedTides.id("textures/entity/commodore_vey.png");
    private static final Identifier BROKER = CursedTides.id("textures/entity/mags_tallow.png");

    private static Identifier tex(String name) {
        return CursedTides.id("textures/entity/" + name + ".png");
    }

    public StoryNpcRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(WanderingTraderEntity entity) {
        if (entity instanceof StoryNpcEntity npc) {
            return switch (npc.getRole()) {
                case KEEPER -> KEEPER;
                case HARBORMASTER -> HARBORMASTER;
                case BELLWRIGHT -> BELLWRIGHT;
                case COMMODORE -> COMMODORE;
                case BROKER -> BROKER;
                case GAMBLER, DEALER -> tex("dealer");
                case GOVERNOR -> tex("governor_harrow");
                case ROOK -> tex("rook_varga");
                case NATURALIST -> tex("ibbie_crane");
                case MEDIUM -> tex("sorrel_medium");
                case FOREMAN -> tex("dagny_bell");
                case BANKER -> tex("banker");
                case ABBESS -> tex("abbess");
                case ADMIRAL -> tex("admiral");
                case RACEMASTER -> tex("racemaster");
                case WAYFARER -> tex("wayfarer");
                case SMUGGLER_QUEEN -> tex("smuggler_queen");
                case AUCTIONEER -> tex("auctioneer");
                case CONTEST_JUDGE -> tex("contest_judge");
                case SCHOLAR -> tex("scholar");
                case OLD_MARINER -> tex("old_mariner");
                case ARENA_MASTER -> tex("arena_master");
                case RANGE_MASTER -> tex("range_master");
                case TAVERN_KEEPER -> tex("tavern_keeper");
                case COURSE_MASTER -> tex("course_master");
                case LAND_AGENT -> tex("land_agent");
                case BOUNTY_CLERK -> tex("bounty_clerk");
                case ARCHIVIST -> tex("archivist");
                case DIVER -> tex("diver");
                case GUNNERY_SERGEANT -> tex("gunnery_sergeant");
                case BARBER -> tex("barber");
                case TATTOOIST -> tex("tattooist");
            };
        }
        return KEEPER;
    }
}
