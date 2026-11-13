package com.cursedtides.entity;

import com.cursedtides.quest.StoryDialogue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** A named story character who stays put and talks instead of trading. Can't be hurt. */
public class StoryNpcEntity extends WanderingTrader {

    public enum Role {
        KEEPER("Keeper Wren"),
        HARBORMASTER("Harbormistress Ysolde Vane"),
        BELLWRIGHT("Tamsin Coil, Bellfounder"),
        COMMODORE("Commodore Aldous Vey"),
        BROKER("Mags Tallow, Brethren Broker"),
        GAMBLER("Lucky Jory Bones, Dice Man"),
        GOVERNOR("Governor Ottoline Harrow"),
        ROOK("Captain Rook Varga"),
        NATURALIST("Ibbie Crane, Naturalist"),
        MEDIUM("Sorrel Vane-Ashby, Medium"),
        FOREMAN("Foreman Dagny Bell"),
        DEALER("Card Dealer"),
        BANKER("Cornelius Fairweather, Banker"),
        ABBESS("Mother Celestine"),
        ADMIRAL("Admiral Hester Galloway"),
        RACEMASTER("Pip Halyard, Regatta Master"),
        WAYFARER("The Wayfarer"),
        SMUGGLER_QUEEN("Isadora Vance, Smugglers' Queen"),
        AUCTIONEER("Barnaby Lott, Auctioneer"),
        CONTEST_JUDGE("Marlowe Finch, Contest Judge"),
        SCHOLAR("Professor Ansel Thwaite"),
        OLD_MARINER("The Old Mariner"),
        ARENA_MASTER("Grista Vell, Arena Master"),
        RANGE_MASTER("Corporal Ned Flint, Range Master"),
        TAVERN_KEEPER("Bess Tankard, Tavern Keeper"),
        COURSE_MASTER("Skip Lanyard, Course Master"),
        LAND_AGENT("Percival Deedes, Land Agent"),
        BOUNTY_CLERK("Clerk Abel Marsh, Bounty Office"),
        ARCHIVIST("Odalys Quill, Archivist"),
        DIVER("Tobiah Sounding, Salvage Diver"),
        GUNNERY_SERGEANT("Sergeant Maud Kettle, Harbour Guns"),
        BARBER("Flossie Snips, Barber"),
        TATTOOIST("Ink-Eyed Moss, Tattooist");

        private final String displayName;

        Role(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }
    }

    private static final EntityDataAccessor<Integer> ROLE =
            SynchedEntityData.defineId(StoryNpcEntity.class, EntityDataSerializers.INT);

    public StoryNpcEntity(EntityType<? extends WanderingTrader> type, Level world) {
        super(type, world);
        this.setInvulnerable(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ROLE, 0);
    }

    public Role getRole() {
        Role[] roles = Role.values();
        return roles[Mth.clamp(this.entityData.get(ROLE), 0, roles.length - 1)];
    }

    public void setRole(Role role) {
        this.entityData.set(ROLE, role.ordinal());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    @Override
    protected void updateTrades() {
        // Story characters don't trade.
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer serverPlayer) {
            if (com.cursedtides.quest.Errands.interact(this, serverPlayer)) return InteractionResult.SUCCESS;
            String file = getRole().name().toLowerCase();
            if (!com.cursedtides.dialogue.Dialogue.open(serverPlayer, this, file)) {
                StoryDialogue.talk(this, serverPlayer);
            }
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("StoryRole", this.entityData.get(ROLE));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        this.entityData.set(ROLE, nbt.getInt("StoryRole"));
    }
}
