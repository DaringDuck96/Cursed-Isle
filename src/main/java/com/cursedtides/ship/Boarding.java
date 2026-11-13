package com.cursedtides.ship;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityTypes;
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
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.commands.Commands.literal;

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
        final Vec3 anchor;
        final List<BlockPos> gangway = new ArrayList<>();
        int ticks;
        boolean won;

        Action(UUID enemy, UUID mine, UUID player, boolean theirs, Vec3 anchor) {
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
        UUID id = ship.getUUID();
        for (Action a : ACTIONS.values()) if (a.enemy.equals(id) || a.mine.equals(id)) return true;
        return false;
    }

    /** True while any of this ship's boarding crew still stands. */
    public static boolean defending(ShipEntity ship) {
        if (!(ship.level() instanceof ServerLevel w)) return false;
        String tag = DEFENDER + ship.getUUID();
        return !w.getEntitiesOfClass(Mob.class, ship.getBoundingBox().inflate(24), m -> m.isAlive() && m.entityTags().contains(tag)).isEmpty();
    }

    // ------------------------------------------------------------------ the grappling hook

    public static class GrapplingHookItem extends Item {
        public GrapplingHookItem(Properties settings) {
            super(settings);
        }

        @Override
        public InteractionResult use(Level world, Player user, InteractionHand hand) {
            ItemStack stack = user.getItemInHand(hand);
            if (world.isClientSide() || !(user instanceof ServerPlayer p) || !(world instanceof ServerLevel w)) {
                return InteractionResult.SUCCESS;
            }
            ShipEntity mine = ownShip(w, p);
            if (mine == null) {
                p.sendSystemMessage(Component.literal("You need a deck under you to throw a grapple from.").withStyle(ChatFormatting.GRAY), true);
                return InteractionResult.FAIL;
            }
            ShipEntity target = null;
            double best = 20 * 20;
            for (ShipEntity s : w.getEntitiesOfClass(ShipEntity.class, mine.getBoundingBox().inflate(20), s -> s != mine && s.isHostile())) {
                double d = s.distanceToSqr(mine);
                if (d < best) { best = d; target = s; }
            }
            if (target == null) {
                p.sendSystemMessage(Component.literal("No enemy ship within a grapple's throw (20 blocks).").withStyle(ChatFormatting.GRAY), true);
                return InteractionResult.FAIL;
            }
            if (isLashed(target) || isLashed(mine)) {
                p.sendSystemMessage(Component.literal("Already lashed fast.").withStyle(ChatFormatting.GRAY), true);
                return InteractionResult.FAIL;
            }
            if (target.getFaction() != ShipFaction.MERCHANT && target.getHull() > target.maxHull() * 0.5f) {
                p.sendSystemMessage(Component.literal("Her guns are still hot. Batter her hull below half before you board ("
                        + (int) target.getHull() + "/" + target.maxHull() + ").").withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }
            p.getCooldowns().addCooldown(new ItemStack(this), 20 * 8);
            stack.hurtAndBreak(1, p, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            begin(w, target, mine, p, false);
            return InteractionResult.SUCCESS;
        }
    }

    private static ShipEntity ownShip(ServerLevel w, ServerPlayer p) {
        if (p.getVehicle() instanceof ShipEntity s && !s.isHostile()) return s;
        ShipEntity best = null;
        double bestD = 6 * 6;
        for (ShipEntity s : w.getEntitiesOfClass(ShipEntity.class, p.getBoundingBox().inflate(6), s -> !s.isHostile())) {
            double d = s.distanceToSqr(p);
            if (d < bestD) { bestD = d; best = s; }
        }
        return best;
    }

    // ------------------------------------------------------------------ starting a boarding action

    private static void begin(ServerLevel w, ShipEntity enemy, ShipEntity mine, ServerPlayer p, boolean theirs) {
        // Haul her in alongside: seven blocks off our beam
        Vec3 dir = enemy.position().subtract(mine.position()).multiply(1, 0, 1);
        if (dir.lengthSqr() < 0.01) dir = new Vec3(1, 0, 0);
        Vec3 at = mine.position().add(dir.normalize().scale(7));
        enemy.snapTo(at.x, enemy.getY(), at.z, enemy.getYRot(), 0);
        enemy.setDeltaMovement(Vec3.ZERO);
        mine.setDeltaMovement(Vec3.ZERO);
        Action a = new Action(enemy.getUUID(), mine.getUUID(), p.getUUID(), theirs, at);
        ACTIONS.put(enemy.getUUID(), a);
        layGangway(w, a, mine.position(), at);

        w.playSound(null, enemy.blockPosition(), SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS, 1.5f, 0.6f);
        w.playSound(null, enemy.blockPosition(), SoundEvents.LEAD_TIED, SoundSource.PLAYERS, 1.5f, 0.7f);
        int deck = Archipelago.WATER_TOP + 1;
        if (!theirs) {
            p.stopRiding();
            p.teleportTo(at.x + dir.normalize().x * -1.5, deck, at.z + dir.normalize().z * -1.5);
            p.sendSystemMessage(Component.literal("Grapples bite! You swing across onto the ").append(enemy.shipName())
                    .append(Component.literal("'s deck. Cut down her crew!")).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        } else {
            p.sendSystemMessage(Component.literal("Grapples bite into your rail! The ").append(enemy.shipName())
                    .append(Component.literal("'s crew are coming over the side!")).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), false);
        }
        musterCrew(w, enemy, a, theirs ? mine.position() : at);
    }

    /** Planks laid over the water between the two hulls, and under both: the deck you fight on. */
    private static void layGangway(ServerLevel w, Action a, Vec3 from, Vec3 to) {
        int y = Archipelago.WATER_TOP;
        int x1 = (int) Math.floor(Math.min(from.x, to.x)) - 3, x2 = (int) Math.floor(Math.max(from.x, to.x)) + 3;
        int z1 = (int) Math.floor(Math.min(from.z, to.z)) - 3, z2 = (int) Math.floor(Math.max(from.z, to.z)) + 3;
        for (int x = x1; x <= x2; x++) {
            for (int z = z1; z <= z2; z++) {
                BlockPos pos = new BlockPos(x, y, z);
                if (!w.getFluidState(pos).is(FluidTags.WATER) || !w.getFluidState(pos).isSource() || !w.getBlockState(pos.above()).isAir()) continue;
                // Under the hulls, invisible; between them, rough timber
                boolean underHull = from.distanceToSqr(x + 0.5, from.y, z + 0.5) < 9 || to.distanceToSqr(x + 0.5, to.y, z + 0.5) < 9;
                w.setBlockAndUpdate(pos, underHull ? Blocks.BARRIER.defaultBlockState()
                        : ((x + z) % 3 == 0 ? Blocks.STRIPPED_SPRUCE_WOOD : Blocks.SPRUCE_PLANKS).defaultBlockState());
                a.gangway.add(pos);
            }
        }
    }

    private static void liftGangway(ServerLevel w, Action a) {
        for (BlockPos pos : a.gangway) {
            var st = w.getBlockState(pos);
            if (st.is(Blocks.BARRIER) || st.is(Blocks.SPRUCE_PLANKS) || st.is(Blocks.STRIPPED_SPRUCE_WOOD)) {
                w.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
            }
        }
        a.gangway.clear();
    }

    /** Her crew leave their stations, and more come up from below, with the captain at their head. */
    private static void musterCrew(ServerLevel w, ShipEntity enemy, Action a, Vec3 around) {
        String tag = DEFENDER + enemy.getUUID();
        int level = Math.max(1, Levels.levelOf(enemy));
        for (Entity e : new ArrayList<>(enemy.getPassengers())) {
            if (e instanceof Mob m) {
                m.stopRiding();
                m.addTag(tag);
                m.snapTo(around.x + w.getRandom().nextGaussian() * 1.5, Archipelago.WATER_TOP + 1, around.z + w.getRandom().nextGaussian() * 1.5, m.getYRot(), 0);
            }
        }
        if (enemy.getFaction() == ShipFaction.MERCHANT) {
            // A merchant's few hands put up a token fight
            for (int i = 0; i < 2; i++) spawnHand(w, enemy, EntityTypes.VINDICATOR, around, level, tag, null);
            return;
        }
        int hands = 2 + enemy.shipClass().ordinal() / 2 + enemy.shipClass().cannonsPerSide() / 2;
        for (int i = 0; i < hands; i++) {
            EntityType<? extends Mob> type = switch (enemy.getFaction()) {
                case NAVY -> ModEntities.MARINE;
                case PIRATE -> i % 2 == 0 ? EntityTypes.VINDICATOR : EntityTypes.PILLAGER;
                default -> ModEntities.CURSED_PIRATE;
            };
            spawnHand(w, enemy, type, around, level, tag, null);
        }
        // The captain
        if (enemy.entityTags().contains(com.cursedtides.quest.MusterArc.FLAGSHIP)) {
            com.cursedtides.quest.MusterArc.spawnHale(w, around, level, tag);
            return;
        }
        String name = switch (enemy.getFaction()) {
            case NAVY -> "Lieutenant " + pick(w, "Ashby", "Crane", "Pell", "Whitlock", "Danvers");
            case PIRATE -> pick(w, "Captain Red Teague", "Captain Ma Gristle", "Captain One-Ear Dobbs", "Captain Saffron Lyle");
            default -> pick(w, "Captain Hollow Jack", "Captain Weeping Mary", "Captain Bones Barrow", "Captain Cold Annet");
        };
        EntityType<? extends Mob> ct = switch (enemy.getFaction()) {
            case NAVY -> ModEntities.MARINE;
            case PIRATE -> EntityTypes.VINDICATOR;
            default -> ModEntities.CURSED_PIRATE;
        };
        Mob captain = spawnHand(w, enemy, ct, around, level + 2, tag, name);
        if (captain instanceof CursedPirateEntity cp) cp.setVariant(cp.kind(), CursedPirateEntity.Rank.CAPTAIN);
        if (captain != null) {
            AttributeInstance hp = captain.getAttribute(Attributes.MAX_HEALTH);
            if (hp != null) { hp.setBaseValue(hp.getBaseValue() * 3 * Levels.healthFactor(level + 2)); captain.setHealth(captain.getMaxHealth()); }
            captain.addTag("ct_board_captain");
            captain.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.CUTLASS));
        }
    }

    private static String pick(ServerLevel w, String... names) { return names[w.getRandom().nextInt(names.length)]; }

    private static Mob spawnHand(ServerLevel w, ShipEntity enemy, EntityType<? extends Mob> type, Vec3 around,
                                       int level, String tag, String name) {
        Mob m = type.create(w, EntitySpawnReason.EVENT);
        if (m == null) return null;
        m.snapTo(around.x + w.getRandom().nextGaussian() * 1.8, Archipelago.WATER_TOP + 1, around.z + w.getRandom().nextGaussian() * 1.8, w.getRandom().nextFloat() * 360, 0);
        m.finalizeSpawn(w, w.getCurrentDifficultyAt(m.blockPosition()), EntitySpawnReason.EVENT, null);
        if (name != null) {
            m.setCustomName(Component.literal(name).withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
            m.setCustomNameVisible(true);
        }
        Levels.apply(m, level);
        m.addTag(tag);
        w.addFreshEntity(m);
        Player near = w.getNearestPlayer(m, 24);
        if (near != null) m.setTarget(near);
        return m;
    }

    // ------------------------------------------------------------------ keeping it going, and ending it

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(Boarding::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            ServerLevel w = server.overworld();
            for (Action a : ACTIONS.values()) liftGangway(w, a);
            ACTIONS.clear();
        });
        ServerLivingEntityEvents.AFTER_DEATH.register(Boarding::onDeath);
        CommandRegistrationCallback.EVENT.register((d, reg, env) -> d.register(literal("prize")
                .then(literal("take").executes(ctx -> { settle(ctx.getSource().getPlayerOrException(), true); return 1; }))
                .then(literal("plunder").executes(ctx -> { settle(ctx.getSource().getPlayerOrException(), false); return 1; }))));
    }

    private static void tick(MinecraftServer server) {
        ServerLevel w = server.overworld();
        if (!ACTIONS.isEmpty()) {
            for (Action a : List.copyOf(ACTIONS.values())) {
                a.ticks++;
                Entity e = w.getEntity(a.enemy), m = w.getEntity(a.mine);
                ServerPlayer p = server.getPlayerList().getPlayer(a.player);
                boolean gone = !(e instanceof ShipEntity enemy) || !enemy.isAlive() || !(m instanceof ShipEntity mine) || !mine.isAlive();
                boolean timeout = a.ticks > MAX_TICKS + (a.won ? 20 * 60 : 0);
                boolean fled = p == null || !p.isAlive() || (e != null && p.distanceToSqr(e) > 48 * 48);
                if (gone || timeout || fled) {
                    end(w, a, gone ? null : fled ? "You've left the fight. The lines part and the ships drift apart." : "The lashings give way and the ships drift apart.");
                    continue;
                }
                ShipEntity enemy = (ShipEntity) e, mine = (ShipEntity) m;
                enemy.setDeltaMovement(Vec3.ZERO);
                mine.setDeltaMovement(mine.getDeltaMovement().multiply(0, 1, 0));
                if (enemy.distanceToSqr(a.anchor.x, enemy.getY(), a.anchor.z) > 1) {
                    enemy.snapTo(a.anchor.x, enemy.getY(), a.anchor.z, enemy.getYRot(), 0);
                }
                if (!a.won && a.ticks > 40 && a.ticks % 10 == 0 && !defending(enemy) && enemy.getPassengers().stream().noneMatch(x -> x instanceof Mob && x.isAlive())) {
                    a.won = true;
                    victory(w, a, enemy, p);
                }
            }
        }
        // Rival pirates and the cursed board you, if you let them come alongside
        if (server.getTickCount() % 100 != 41) return;
        for (ServerPlayer p : w.players()) {
            if (!(p.getVehicle() instanceof ShipEntity mine) || mine.isHostile() || isLashed(mine)) continue;
            for (ShipEntity s : w.getEntitiesOfClass(ShipEntity.class, mine.getBoundingBox().inflate(12),
                    s -> (s.getFaction() == ShipFaction.PIRATE || s.getFaction() == ShipFaction.DROWNED) && !isLashed(s))) {
                long last = RAIDED.getOrDefault(s.getUUID(), 0L);
                if (w.getGameTime() - last < 20 * 90 || w.getRandom().nextFloat() > 0.3f) continue;
                if (s.getPassengers().stream().noneMatch(x -> x instanceof Mob)) continue;
                RAIDED.put(s.getUUID(), w.getGameTime());
                begin(w, s, mine, p, true);
                break;
            }
        }
    }

    private static void victory(ServerLevel w, Action a, ShipEntity enemy, ServerPlayer p) {
        w.playSound(null, enemy.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1f, 1f);
        PirateData.addXp(p, Weapon.CUTLASS, 80);
        PirateData.addXp(p, Weapon.SAILING, 60);
        com.cursedtides.activity.Achievements.grant(p, "boarder");
        com.cursedtides.quest.MusterArc.onBoarded(p, enemy);
        p.sendSystemMessage(Component.literal("Her colours come down! The ").append(enemy.shipName()).append(Component.literal(" is yours to decide:"))
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        p.sendSystemMessage(Component.literal("  [Take her as a prize]").withStyle(s -> s.withColor(ChatFormatting.GREEN)
                        .withClickEvent(new ClickEvent.RunCommand("/prize take")))
                .append(Component.literal("   [Strip her hold and scuttle her]").withStyle(s -> s.withColor(ChatFormatting.RED)
                        .withClickEvent(new ClickEvent.RunCommand("/prize plunder")))), false);
    }

    private static void settle(ServerPlayer p, boolean take) {
        ServerLevel w = p.level();
        for (Action a : List.copyOf(ACTIONS.values())) {
            if (!a.player.equals(p.getUUID()) || !a.won) continue;
            if (!(w.getEntity(a.enemy) instanceof ShipEntity enemy)) continue;
            end(w, a, null);
            if (take) {
                enemy.captureBy(p);
            } else {
                int level = Math.max(1, Levels.levelOf(enemy));
                for (int i = 0; i < enemy.cargo().getContainerSize(); i++) {
                    ItemStack st = enemy.cargo().removeItemNoUpdate(i);
                    if (!st.isEmpty()) com.cursedtides.quest.QuestData.give(p, st);
                }
                com.cursedtides.quest.QuestData.give(p, com.cursedtides.combat.Loot.container(
                        enemy.shipClass().ordinal() >= 3 ? com.cursedtides.combat.Loot.Tier.SKULL : com.cursedtides.combat.Loot.Tier.CHEST, level));
                com.cursedtides.trade.Wallet.give(p, (15 + level * 2) * com.cursedtides.trade.Wallet.SILVER_PER_DOUBLOON);
                p.sendSystemMessage(Component.literal("You strip her hold to the bilges and put a torch to her powder.").withStyle(ChatFormatting.GOLD), false);
                w.sendParticles(ParticleTypes.EXPLOSION, enemy.getX(), enemy.getY() + 1, enemy.getZ(), 6, 1.5, 0.5, 1.5, 0.1);
                enemy.scuttle(w, p);
            }
            return;
        }
        p.sendSystemMessage(Component.literal("There's no beaten ship waiting on your word.").withStyle(ChatFormatting.GRAY), true);
    }

    private static void end(ServerLevel w, Action a, String message) {
        ACTIONS.remove(a.enemy);
        liftGangway(w, a);
        String tag = DEFENDER + a.enemy;
        // Survivors scramble back aboard (or drown trying)
        for (Mob m : w.getEntitiesOfClass(Mob.class, new net.minecraft.world.phys.AABB(BlockPos.containing(a.anchor)).inflate(40),
                x -> x.entityTags().contains(tag))) {
            if (!a.won) m.discard();
        }
        if (message != null && w.getServer().getPlayerList().getPlayer(a.player) instanceof ServerPlayer p) {
            p.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.GRAY), false);
        }
    }

    private static void onDeath(LivingEntity victim, net.minecraft.world.damagesource.DamageSource source) {
        if (!victim.entityTags().contains("ct_board_captain")) return;
        if (source.getEntity() instanceof ServerPlayer p) {
            com.cursedtides.util.Compat.drop(victim, com.cursedtides.combat.Loot.container(com.cursedtides.combat.Loot.Tier.CHEST, Math.max(1, Levels.levelOf(victim))));
            com.cursedtides.util.Compat.drop(victim, new ItemStack(ModItems.DOUBLOON, 8 + p.getRandom().nextInt(8)));
            if (p.getRandom().nextInt(4) == 0) com.cursedtides.util.Compat.drop(victim, new ItemStack(Items.SPYGLASS));
        }
    }
}
