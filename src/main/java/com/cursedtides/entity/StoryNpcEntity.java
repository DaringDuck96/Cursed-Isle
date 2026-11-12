package com.cursedtides.entity;

import com.cursedtides.quest.StoryDialogue;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/** A named story character who stays put and talks instead of trading. Can't be hurt. */
public class StoryNpcEntity extends WanderingTraderEntity {

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

    private static final TrackedData<Integer> ROLE =
            DataTracker.registerData(StoryNpcEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public StoryNpcEntity(EntityType<? extends WanderingTraderEntity> type, World world) {
        super(type, world);
        this.setInvulnerable(true);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(ROLE, 0);
    }

    public Role getRole() {
        Role[] roles = Role.values();
        return roles[MathHelper.clamp(this.dataTracker.get(ROLE), 0, roles.length - 1)];
    }

    public void setRole(Role role) {
        this.dataTracker.set(ROLE, role.ordinal());
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(2, new LookAroundGoal(this));
    }

    @Override
    protected void fillRecipes() {
        // Story characters don't trade.
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (hand == Hand.MAIN_HAND && player instanceof ServerPlayerEntity serverPlayer) {
            String file = getRole().name().toLowerCase();
            if (!com.cursedtides.dialogue.Dialogue.open(serverPlayer, this, file)) {
                StoryDialogue.talk(this, serverPlayer);
            }
        }
        return ActionResult.success(this.getWorld().isClient);
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("StoryRole", this.dataTracker.get(ROLE));
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dataTracker.set(ROLE, nbt.getInt("StoryRole"));
    }
}
