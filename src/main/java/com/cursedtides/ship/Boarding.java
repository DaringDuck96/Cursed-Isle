package com.cursedtides.ship;

import com.cursedtides.combat.Levels;
import com.cursedtides.entity.CursedPirateEntity;
import com.cursedtides.entity.ModEntities;
import com.cursedtides.entity.ShipEntity;
import com.cursedtides.item.ModItems;
import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import com.cursedtides.world.Archipelago;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Boarding. Batter an enemy ship's hull below half, come alongside and throw a grappling hook: the two ships are
 * lashed together, a gangway of lashed timbers spreads between them, and you swing across. Her crew spill out to
 * meet you with their captain at their head. Cut them all down and her colours come down: take her as a prize,
 * or strip her hold and scuttle her. Rival pirates and the cursed do the same to you if you let them close.
 */
public final class Boarding {
    private Boarding() {}

    public static final String DEFENDER = "ct_board:";
    private static final int MAX_TICKS = 20 * 180;

    /** One boarding action: the enemy ship, the player ship, who started it, and the gangway blocks laid down. */
    private static final class Action {
        final UUID enemy, mine, player;
        final boolean theirs;
        final Vec3d anchor;
        final List<BlockPos> gangway = new ArrayList<>();
        int ticks;
        boolean won;

        Action(UUID enemy, UUID mine, UUID player, boolean theirs, Vec3d anchor) {
            this.enemy = enemy;
            this.mine = mine;
            this.player = player;
            this.theirs = theirs;
            this.anchor = anchor;
        }
    }

    private static final Map<UUID, Action> ACTIONS = new HashMap<>();
    private static final Map<UUID, Long> RAIDED = new HashMap<>();

    public static boolean isLashed(ShipEntity ship) {
        UUID id = ship.getUuid();
        for (Action a : ACTIONS.values()) if (a.enemy.equals(id) || a.mine.equals(id)) return true;
        return false;
    }

    /** True while any of this ship's boarding crew still stands. */
    public static boolean defending(ShipEntity ship) {
        if (!(ship.getWorld() instanceof ServerWorld w)) return false;
        String tag = DEFENDER + ship.getUuid();
        return !w.getEntitiesByClass(MobEntity.class, ship.getBoundingBox().expand(24), m -> m.isAlive() && m.getCommandTags().contains(tag)).isEmpty();
    }

    // ------------------------------------------------------------------ the grappling hook

    public static class GrapplingHookItem extends Item {
        public GrapplingHookItem(Settings settings) {
            super(settings);
        }

        @Override
        public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
            ItemStack stack = user.getStackInHand(hand);
            if (world.isClient || !(user instanceof ServerPlayerEntity p) || !(world instanceof ServerWorld w)) {
                return TypedActionResult.success(stack, world.isClient);
            }
            ShipEntity mine = ownShip(w, p);
            if (mine == null) {
                p.sendMessage(Text.literal("You need a deck under you to throw a grapple from.").formatted(Formatting.GRAY), true);
                return TypedActionResult.fail(stack);
            }
            ShipEntity target = null;
            double best = 20 * 20;
            for (ShipEntity s : w.getEntitiesByClass(ShipEntity.class, mine.getBoundingBox().expand(20), s -> s != mine && s.isHostile())) {
                double d = s.squaredDistanceTo(mine);
                if (d < best) { best = d; target = s; }
            }
            if (target == null) {
                p.sendMessage(Text.literal("No enemy ship within a grapple's throw (20 blocks).").formatted(Formatting.GRAY), true);
                return TypedActionResult.fail(stack);
            }
            if (isLashed(target) || isLashed(mine)) {
                p.sendMessage(Text.literal("Already lashed fast.").formatted(Formatting.GRAY), true);
                return TypedActionResult.fail(stack);
            }
            if (target.getFaction() != ShipFaction.MERCHANT && target.getHull() > target.maxHull() * 0.5f) {
                p.sendMessage(Text.literal("Her guns are still hot. Batter her hull below half before you board ("
                        + (int) target.getHull() + "/" + target.maxHull() + ").").formatted(Formatting.RED), true);
                return TypedActionResult.fail(stack);
            }
            p.getItemCooldownManager().set(this, 20 * 8);
            stack.damage(1, p, hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            begin(w, target, mine, p, false);
            return TypedActionResult.success(stack);
        }
    }

    private static ShipEntity ownShip(ServerWorld w, ServerPlayerEntity p) {
        if (p.getVehicle() instanceof ShipEntity s && !s.isHostile()) return s;
        ShipEntity best = null;
        double bestD = 6 * 6;
        for (ShipEntity s : w.getEntitiesByClass(ShipEntity.class, p.getBoundingBox().expand(6), s -> !s.isHostile())) {
            double d = s.squaredDistanceTo(p);
            if (d < bestD) { bestD = d; best = s; }
        }
        return best;
    }

    // ------------------------------------------------------------------ starting a boarding action

    private static void begin(ServerWorld w, ShipEntity enemy, ShipEntity mine, ServerPlayerEntity p, boolean theirs) {
        // Haul her in alongside: seven blocks off our beam
        Vec3d dir = enemy.getPos().subtract(mine.getPos()).multiply(1, 0, 1);
        if (dir.lengthSquared() < 0.01) dir = new Vec3d(1, 0, 0);
        Vec3d at = mine.getPos().add(dir.normalize().multiply(7));
        enemy.refreshPositionAndAngles(at.x, enemy.getY(), at.z, enemy.getYaw(), 0);
        enemy.setVelocity(Vec3d.ZERO);
        mine.setVelocity(Vec3d.ZERO);
        Action a = new Action(enemy.getUuid(), mine.getUuid(), p.getUuid(), theirs, at);
        ACTIONS.put(enemy.getUuid(), a);
        layGangway(w, a, mine.getPos(), at);

        w.playSound(null, enemy.getBlockPos(), SoundEvents.ITEM_TRIDENT_HIT, SoundCategory.PLAYERS, 1.5f, 0.6f);
        w.playSound(null, enemy.getBlockPos(), SoundEvents.ENTITY_LEASH_KNOT_PLACE, SoundCategory.PLAYERS, 1.5f, 0.7f);
        int deck = Archipelago.WATER_TOP + 1;
        if (!theirs) {
            p.stopRiding();
            p.requestTeleport(at.x + dir.normalize().x * -1.5, deck, at.z + dir.normalize().z * -1.5);
            p.sendMessage(Text.literal("Grapples bite! You swing across onto the ").append(enemy.shipName())
                    .append(Text.literal("'s deck. Cut down her crew!")).formatted(Formatting.GOLD, Formatting.BOLD), false);
        } else {
            p.sendMessage(Text.literal("Grapples bite into your rail! The ").append(enemy.shipName())
                    .append(Text.literal("'s crew are coming over the side!")).formatted(Formatting.RED, Formatting.BOLD), false);
        }
        musterCrew(w, enemy, a, theirs ? mine.getPos() : at);
    }

    /** Planks laid over the water between the two hulls, and under both: the deck you fight on. */
    private static void layGangway(ServerWorld w, Action a, Vec3d from, Vec3d to) {
        int y = Archipelago.WATER_TOP;
        int x1 = (int) Math.floor(Math.min(from.x, to.x)) - 3, x2 = (int) Math.floor(Math.max(from.x, to.x)) + 3;
        int z1 = (int) Math.floor(Math.min(from.z, to.z)) - 3, z2 = (int) Math.floor(Math.max(from.z, to.z)) + 3;
        for (int x = x1; x <= x2; x++) {
            for (int z = z1; z <= z2; z++) {
                BlockPos pos = new BlockPos(x, y, z);
                if (!w.getFluidState(pos).isIn(FluidTags.WATER) || !w.getFluidState(pos).isStill() || !w.getBlockState(pos.up()).isAir()) continue;
                // Under the hulls, invisible; between them, rough timber
                boolean underHull = from.squaredDistanceTo(x + 0.5, from.y, z + 0.5) < 9 || to.squaredDistanceTo(x + 0.5, to.y, z + 0.5) < 9;
                w.setBlockState(pos, underHull ? Blocks.BARRIER.getDefaultState()
                        : ((x + z) % 3 == 0 ? Blocks.STRIPPED_SPRUCE_WOOD : Blocks.SPRUCE_PLANKS).getDefaultState());
                a.gangway.add(pos);
            }
        }
    }

    private static void liftGangway(ServerWorld w, Action a) {
        for (BlockPos pos : a.gangway) {
            var st = w.getBlockState(pos);
            if (st.isOf(Blocks.BARRIER) || st.isOf(Blocks.SPRUCE_PLANKS) || st.isOf(Blocks.STRIPPED_SPRUCE_WOOD)) {
                w.setBlockState(pos, Blocks.WATER.getDefaultState());
            }
        }
        a.gangway.clear();
    }

    /** Her crew leave their stations, and more come up from below, with the captain at their head. */
    private static void musterCrew(ServerWorld w, ShipEntity enemy, Action a, Vec3d around) {
        String tag = DEFENDER + enemy.getUuid();
        int level = Math.max(1, Levels.levelOf(enemy));
        for (Entity e : new ArrayList<>(enemy.getPassengerList())) {
            if (e instanceof MobEntity m) {
                m.stopRiding();
                m.addCommandTag(tag);
                m.refreshPositionAndAngles(around.x + w.random.nextGaussian() * 1.5, Archipelago.WATER_TOP + 1, around.z + w.random.nextGaussian() * 1.5, m.getYaw(), 0);
            }
        }
        if (enemy.getFaction() == ShipFaction.MERCHANT) {
            // A merchant's few hands put up a token fight
            for (int i = 0; i < 2; i++) spawnHand(w, enemy, EntityType.VINDICATOR, around, level, tag, null);
            return;
        }
        int hands = 2 + enemy.shipClass().ordinal() / 2 + enemy.shipClass().cannonsPerSide() / 2;
        for (int i = 0; i < hands; i++) {
            EntityType<? extends MobEntity> type = switch (enemy.getFaction()) {
                case NAVY -> ModEntities.MARINE;
                case PIRATE -> i % 2 == 0 ? EntityType.VINDICATOR : EntityType.PILLAGER;
                default -> ModEntities.CURSED_PIRATE;
            };
            spawnHand(w, enemy, type, around, level, tag, null);
        }
        // The captain
        if (enemy.getCommandTags().contains(com.cursedtides.quest.MusterArc.FLAGSHIP)) {
            com.cursedtides.quest.MusterArc.spawnHale(w, around, level, tag);
            return;
        }
        String name = switch (enemy.getFaction()) {
            case NAVY -> "Lieutenant " + pick(w, "Ashby", "Crane", "Pell", "Whitlock", "Danvers");
            case PIRATE -> pick(w, "Captain Red Teague", "Captain Ma Gristle", "Captain One-Ear Dobbs", "Captain Saffron Lyle");
            default -> pick(w, "Captain Hollow Jack", "Captain Weeping Mary", "Captain Bones Barrow", "Captain Cold Annet");
        };
        EntityType<? extends MobEntity> ct = switch (enemy.getFaction()) {
            case NAVY -> ModEntities.MARINE;
            case PIRATE -> EntityType.VINDICATOR;
            default -> ModEntities.CURSED_PIRATE;
        };
        MobEntity captain = spawnHand(w, enemy, ct, around, level + 2, tag, name);
        if (captain instanceof CursedPirateEntity cp) cp.setVariant(cp.kind(), CursedPirateEntity.Rank.CAPTAIN);
        if (captain != null) {
            EntityAttributeInstance hp = captain.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
            if (hp != null) { hp.setBaseValue(hp.getBaseValue() * 3 * Levels.healthFactor(level + 2)); captain.setHealth(captain.getMaxHealth()); }
            captain.addCommandTag("ct_board_captain");
            captain.equipStack(EquipmentSlot.MAINHAND, new ItemStack(ModItems.CUTLASS));
        }
    }

    private static String pick(ServerWorld w, String... names) { return names[w.random.nextInt(names.length)]; }

    private static MobEntity spawnHand(ServerWorld w, ShipEntity enemy, EntityType<? extends MobEntity> type, Vec3d around,
                                       int level, String tag, String name) {
        MobEntity m = type.create(w);
        if (m == null) return null;
        m.refreshPositionAndAngles(around.x + w.random.nextGaussian() * 1.8, Archipelago.WATER_TOP + 1, around.z + w.random.nextGaussian() * 1.8, w.random.nextFloat() * 360, 0);
        m.initialize(w, w.getLocalDifficulty(m.getBlockPos()), SpawnReason.EVENT, null);
        if (name != null) {
            m.setCustomName(Text.literal(name).formatted(Formatting.RED, Formatting.BOLD));
            m.setCustomNameVisible(true);
        }
        Levels.apply(m, level);
        m.addCommandTag(tag);
        w.spawnEntity(m);
        PlayerEntity near = w.getClosestPlayer(m, 24);
        if (near != null) m.setTarget(near);
        return m;
    }

    // ------------------------------------------------------------------ keeping it going, and ending it

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(Boarding::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            ServerWorld w = server.getOverworld();
            for (Action a : ACTIONS.values()) liftGangway(w, a);
            ACTIONS.clear();
        });
        ServerLivingEntityEvents.AFTER_DEATH.register(Boarding::onDeath);
        CommandRegistrationCallback.EVENT.register((d, reg, env) -> d.register(literal("prize")
                .then(literal("take").executes(ctx -> { settle(ctx.getSource().getPlayerOrThrow(), true); return 1; }))
                .then(literal("plunder").executes(ctx -> { settle(ctx.getSource().getPlayerOrThrow(), false); return 1; }))));
    }

    private static void tick(MinecraftServer server) {
        ServerWorld w = server.getOverworld();
        if (!ACTIONS.isEmpty()) {
            for (Action a : List.copyOf(ACTIONS.values())) {
                a.ticks++;
                Entity e = w.getEntity(a.enemy), m = w.getEntity(a.mine);
                ServerPlayerEntity p = server.getPlayerManager().getPlayer(a.player);
                boolean gone = !(e instanceof ShipEntity enemy) || !enemy.isAlive() || !(m instanceof ShipEntity mine) || !mine.isAlive();
                boolean timeout = a.ticks > MAX_TICKS + (a.won ? 20 * 60 : 0);
                boolean fled = p == null || !p.isAlive() || (e != null && p.squaredDistanceTo(e) > 48 * 48);
                if (gone || timeout || fled) {
                    end(w, a, gone ? null : fled ? "You've left the fight. The lines part and the ships drift apart." : "The lashings give way and the ships drift apart.");
                    continue;
                }
                ShipEntity enemy = (ShipEntity) e, mine = (ShipEntity) m;
                enemy.setVelocity(Vec3d.ZERO);
                mine.setVelocity(mine.getVelocity().multiply(0, 1, 0));
                if (enemy.squaredDistanceTo(a.anchor.x, enemy.getY(), a.anchor.z) > 1) {
                    enemy.refreshPositionAndAngles(a.anchor.x, enemy.getY(), a.anchor.z, enemy.getYaw(), 0);
                }
                if (!a.won && a.ticks > 40 && a.ticks % 10 == 0 && !defending(enemy) && enemy.getPassengerList().stream().noneMatch(x -> x instanceof MobEntity && x.isAlive())) {
                    a.won = true;
                    victory(w, a, enemy, p);
                }
            }
        }
        // Rival pirates and the cursed board you, if you let them come alongside
        if (server.getTicks() % 100 != 41) return;
        for (ServerPlayerEntity p : w.getPlayers()) {
            if (!(p.getVehicle() instanceof ShipEntity mine) || mine.isHostile() || isLashed(mine)) continue;
            for (ShipEntity s : w.getEntitiesByClass(ShipEntity.class, mine.getBoundingBox().expand(12),
                    s -> (s.getFaction() == ShipFaction.PIRATE || s.getFaction() == ShipFaction.DROWNED) && !isLashed(s))) {
                long last = RAIDED.getOrDefault(s.getUuid(), 0L);
                if (w.getTime() - last < 20 * 90 || w.random.nextFloat() > 0.3f) continue;
                if (s.getPassengerList().stream().noneMatch(x -> x instanceof MobEntity)) continue;
                RAIDED.put(s.getUuid(), w.getTime());
                begin(w, s, mine, p, true);
                break;
            }
        }
    }

    private static void victory(ServerWorld w, Action a, ShipEntity enemy, ServerPlayerEntity p) {
        w.playSound(null, enemy.getBlockPos(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1f, 1f);
        PirateData.addXp(p, Weapon.CUTLASS, 80);
        PirateData.addXp(p, Weapon.SAILING, 60);
        com.cursedtides.activity.Achievements.grant(p, "boarder");
        com.cursedtides.quest.MusterArc.onBoarded(p, enemy);
        p.sendMessage(Text.literal("Her colours come down! The ").append(enemy.shipName()).append(Text.literal(" is yours to decide:"))
                .formatted(Formatting.GOLD, Formatting.BOLD), false);
        p.sendMessage(Text.literal("  [Take her as a prize]").styled(s -> s.withColor(Formatting.GREEN)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/prize take")))
                .append(Text.literal("   [Strip her hold and scuttle her]").styled(s -> s.withColor(Formatting.RED)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/prize plunder")))), false);
    }

    private static void settle(ServerPlayerEntity p, boolean take) {
        ServerWorld w = p.getServerWorld();
        for (Action a : List.copyOf(ACTIONS.values())) {
            if (!a.player.equals(p.getUuid()) || !a.won) continue;
            if (!(w.getEntity(a.enemy) instanceof ShipEntity enemy)) continue;
            end(w, a, null);
            if (take) {
                enemy.captureBy(p);
            } else {
                int level = Math.max(1, Levels.levelOf(enemy));
                for (int i = 0; i < enemy.cargo().size(); i++) {
                    ItemStack st = enemy.cargo().removeStack(i);
                    if (!st.isEmpty()) com.cursedtides.quest.QuestData.give(p, st);
                }
                com.cursedtides.quest.QuestData.give(p, com.cursedtides.combat.Loot.container(
                        enemy.shipClass().ordinal() >= 3 ? com.cursedtides.combat.Loot.Tier.SKULL : com.cursedtides.combat.Loot.Tier.CHEST, level));
                com.cursedtides.trade.Wallet.give(p, (15 + level * 2) * com.cursedtides.trade.Wallet.SILVER_PER_DOUBLOON);
                p.sendMessage(Text.literal("You strip her hold to the bilges and put a torch to her powder.").formatted(Formatting.GOLD), false);
                w.spawnParticles(ParticleTypes.EXPLOSION, enemy.getX(), enemy.getY() + 1, enemy.getZ(), 6, 1.5, 0.5, 1.5, 0.1);
                enemy.scuttle(w, p);
            }
            return;
        }
        p.sendMessage(Text.literal("There's no beaten ship waiting on your word.").formatted(Formatting.GRAY), true);
    }

    private static void end(ServerWorld w, Action a, String message) {
        ACTIONS.remove(a.enemy);
        liftGangway(w, a);
        String tag = DEFENDER + a.enemy;
        // Survivors scramble back aboard (or drown trying)
        for (MobEntity m : w.getEntitiesByClass(MobEntity.class, new net.minecraft.util.math.Box(BlockPos.ofFloored(a.anchor)).expand(40),
                x -> x.getCommandTags().contains(tag))) {
            if (!a.won) m.discard();
        }
        if (message != null && w.getServer().getPlayerManager().getPlayer(a.player) instanceof ServerPlayerEntity p) {
            p.sendMessage(Text.literal(message).formatted(Formatting.GRAY), false);
        }
    }

    private static void onDeath(LivingEntity victim, net.minecraft.entity.damage.DamageSource source) {
        if (!victim.getCommandTags().contains("ct_board_captain")) return;
        if (source.getAttacker() instanceof ServerPlayerEntity p) {
            victim.dropStack(com.cursedtides.combat.Loot.container(com.cursedtides.combat.Loot.Tier.CHEST, Math.max(1, Levels.levelOf(victim))));
            victim.dropStack(new ItemStack(ModItems.DOUBLOON, 8 + p.getRandom().nextInt(8)));
            if (p.getRandom().nextInt(4) == 0) victim.dropStack(new ItemStack(Items.SPYGLASS));
        }
    }
}
