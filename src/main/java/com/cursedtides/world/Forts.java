package com.cursedtides.world;

import com.cursedtides.entity.CannonballEntity;
import com.cursedtides.entity.ShipEntity;
import com.cursedtides.faction.Faction;
import com.cursedtides.faction.FactionData;
import com.cursedtides.item.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Shore batteries. The Tidewatch forts (Ironwatch, Crownwatch) fire on the ships of wanted pirates; Blackthorn's guns
 * fire on Tidewatch loyalists. Hit a battery with cannon fire three times to knock it out. Silence every gun in a fort
 * and it stays quiet for a day, and whoever fired the last shot is paid for the trouble.
 */
public final class Forts {
    private Forts() {}

    public record Fort(String name, Archipelago.Island island, boolean navy, List<int[]> batteries) {}

    private static List<int[]> sites(Archipelago.Island is) {
        List<int[]> out = new ArrayList<>();
        for (int i = 0; i < 16 && out.size() < 4; i++) {
            double a = Math.PI * 2 * i / 16 + 0.2;
            for (double f = 0.62; f >= 0.4; f -= 0.06) {
                int x = is.cx() + (int) Math.round(Math.cos(a) * is.radius() * f);
                int z = is.cz() + (int) Math.round(Math.sin(a) * is.radius() * f);
                if (!Archipelago.column(x, z).isLand()) continue; // (must not touch Builds here: Builds reads FORTS while it loads)
                boolean spaced = true;
                for (int[] o : out) if (Math.abs(o[0] - x) + Math.abs(o[1] - z) < 18) spaced = false;
                if (spaced) { out.add(new int[]{x, z}); break; }
            }
        }
        return out;
    }

    public static final List<Fort> FORTS = List.of(
            new Fort("Ironwatch", Archipelago.IRONWATCH_ISLE, true, sites(Archipelago.IRONWATCH_ISLE)),
            new Fort("Crownwatch", Archipelago.CROWNWATCH, true, sites(Archipelago.CROWNWATCH)),
            new Fort("Blackthorn", Archipelago.BLACKTHORN_ISLE, false, sites(Archipelago.BLACKTHORN_ISLE)));

    public static final int BATTERY_HP = 3;

    // ------------------------------------------------------------------ saved state

    public static class State extends SavedData {
        /** hp per battery, flattened fort by fort. */
        final int[] hp = new int[FORTS.stream().mapToInt(f -> f.batteries().size()).sum()];
        final long[] silencedUntil = new long[FORTS.size()];

        State() {
            java.util.Arrays.fill(hp, BATTERY_HP);
        }

        private static final net.minecraft.world.level.saveddata.SavedDataType<State> TYPE = com.cursedtides.util.Saved.type("cursedtides_forts", State::new, State::fromNbt, (st__, l__) -> st__.save(new CompoundTag(), l__));

        static State get(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(TYPE);
        }

                public CompoundTag save(CompoundTag nbt, HolderLookup.Provider lookup) {
            nbt.putIntArray("Hp", hp);
            nbt.putLongArray("Silenced", silencedUntil);
            return nbt;
        }

        private static State fromNbt(CompoundTag nbt, HolderLookup.Provider lookup) {
            State s = new State();
            int[] h = nbt.getIntArray("Hp").orElse(new int[0]);
            System.arraycopy(h, 0, s.hp, 0, Math.min(h.length, s.hp.length));
            long[] l = nbt.getLongArray("Silenced").orElse(new long[0]);
            System.arraycopy(l, 0, s.silencedUntil, 0, Math.min(l.length, s.silencedUntil.length));
            return s;
        }
    }

    private static int slot(int fort, int battery) {
        int n = 0;
        for (int i = 0; i < fort; i++) n += FORTS.get(i).batteries().size();
        return n + battery;
    }

    private static BlockPos gunPos(int[] b) {
        return new BlockPos(b[0], Builds.floorAt(b[0], b[1]), b[1]);
    }

    // ------------------------------------------------------------------ behaviour

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(Forts::tick);
    }

    private static boolean hostileTo(Fort f, Player p) {
        return f.navy() ? FactionData.isWanted(p) : FactionData.rep(p, Faction.NAVY) >= 300;
    }

    private static void tick(MinecraftServer server) {
        if (!ModWorld.isArchipelago() || server.getTickCount() % 50 != 0) return;
        ServerLevel world = server.overworld();
        State s = State.get(server);
        long now = world.getGameTime();
        for (int fi = 0; fi < FORTS.size(); fi++) {
            Fort f = FORTS.get(fi);
            if (s.silencedUntil[fi] > now) continue;
            if (s.silencedUntil[fi] != 0) { // the garrison has re-armed
                s.silencedUntil[fi] = 0;
                for (int bi = 0; bi < f.batteries().size(); bi++) {
                    s.hp[slot(fi, bi)] = BATTERY_HP;
                    world.setBlockAndUpdate(gunPos(f.batteries().get(bi)), Blocks.ANVIL.defaultBlockState());
                }
                s.setDirty();
            }
            for (int bi = 0; bi < f.batteries().size(); bi++) {
                if (s.hp[slot(fi, bi)] <= 0) continue;
                BlockPos gun = gunPos(f.batteries().get(bi));
                if (!world.hasChunkAt(gun)) continue;
                ShipEntity target = null;
                double best = 75 * 75;
                for (ShipEntity ship : world.getEntitiesOfClass(ShipEntity.class, new net.minecraft.world.phys.AABB(gun).inflate(75), e -> true)) {
                    if (!(ship.getControllingPassenger() instanceof ServerPlayer captain) || !hostileTo(f, captain)) continue;
                    double d = ship.distanceToSqr(Vec3.atCenterOf(gun));
                    if (d < best) { best = d; target = ship; }
                }
                if (target == null || world.getRandom().nextInt(3) != 0) continue;
                fire(world, gun, target);
            }
        }
    }

    private static void fire(ServerLevel world, BlockPos gun, Entity target) {
        Vec3 from = Vec3.atCenterOf(gun).add(0, 1.2, 0);
        Vec3 to = target.position().add(target.getDeltaMovement().scale(20)).add(0, 1.5, 0);
        Vec3 d = to.subtract(from);
        double flat = Math.sqrt(d.x * d.x + d.z * d.z);
        CannonballEntity ball = new CannonballEntity(world, from.x, from.y, from.z);
        ball.addTag("cursedtides_fort_shot");
        ball.shoot(d.x, d.y + flat * 0.12, d.z, 2.4f, 4.0f);
        world.addFreshEntity(ball);
        world.sendParticles(ParticleTypes.LARGE_SMOKE, from.x, from.y, from.z, 10, 0.3, 0.3, 0.3, 0.02);
        world.playSound(null, gun, com.cursedtides.sound.ModSounds.CANNON_BOOM.value(), SoundSource.HOSTILE, 3f, 0.8f);
    }

    /** Called where any cannonball lands. */
    public static void onImpact(ServerLevel world, Vec3 at, @Nullable Entity owner, CannonballEntity ball) {
        if (ball.entityTags().contains("cursedtides_fort_shot")) return;
        ServerPlayer shooter = shooter(world, owner);
        State s = State.get(world.getServer());
        for (int fi = 0; fi < FORTS.size(); fi++) {
            Fort f = FORTS.get(fi);
            for (int bi = 0; bi < f.batteries().size(); bi++) {
                int k = slot(fi, bi);
                if (s.hp[k] <= 0) continue;
                BlockPos gun = gunPos(f.batteries().get(bi));
                if (at.distanceToSqr(Vec3.atCenterOf(gun)) > 4.5 * 4.5) continue;
                s.hp[k]--;
                s.setDirty();
                if (s.hp[k] > 0) {
                    world.setBlockAndUpdate(gun, (s.hp[k] == 2 ? Blocks.CHIPPED_ANVIL : Blocks.DAMAGED_ANVIL).defaultBlockState());
                    if (shooter != null) shooter.sendSystemMessage(Component.literal("Hit on a " + f.name() + " battery!").withStyle(ChatFormatting.GOLD), true);
                    return;
                }
                world.setBlockAndUpdate(gun, Blocks.BLACKSTONE.defaultBlockState());
                world.setBlockAndUpdate(gun.above(), Blocks.FIRE.defaultBlockState());
                world.sendParticles(ParticleTypes.EXPLOSION_EMITTER, gun.getX() + 0.5, gun.getY() + 1, gun.getZ() + 0.5, 1, 0, 0, 0, 0);
                boolean anyLeft = false;
                for (int bj = 0; bj < f.batteries().size(); bj++) if (s.hp[slot(fi, bj)] > 0) anyLeft = true;
                if (shooter != null) shooter.sendSystemMessage(Component.literal("A " + f.name() + " battery is silenced!").withStyle(ChatFormatting.GOLD), false);
                if (!anyLeft) silenced(world, s, fi, shooter);
                return;
            }
        }
    }

    private static void silenced(ServerLevel world, State s, int fi, @Nullable ServerPlayer shooter) {
        Fort f = FORTS.get(fi);
        s.silencedUntil[fi] = world.getGameTime() + 24000;
        s.setDirty();
        world.getServer().getPlayerList().broadcastSystemMessage(Component.literal("The guns of " + f.name() + " have fallen silent"
                + (shooter != null ? ", silenced by " + shooter.getName().getString() : "") + "!").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        if (shooter == null) return;
        shooter.addItem(new ItemStack(ModItems.DOUBLOON, 40));
        com.cursedtides.profession.ProfessionData.addXp(shooter, com.cursedtides.profession.Profession.LEADERSHIP, 50);
        com.cursedtides.pirate.PirateData.addXp(shooter, com.cursedtides.pirate.Weapon.CANNON, 200);
        if (f.navy()) {
            FactionData.adjust(shooter, Faction.NAVY, -150);
            FactionData.adjust(shooter, Faction.BRETHREN, 80);
        } else {
            FactionData.adjust(shooter, Faction.NAVY, 80);
            FactionData.adjust(shooter, Faction.BRETHREN, -120);
        }
        com.cursedtides.activity.Achievements.grant(shooter, "fort_breaker");
    }

    @Nullable
    private static ServerPlayer shooter(ServerLevel world, @Nullable Entity owner) {
        if (owner instanceof ServerPlayer p) return p;
        if (owner instanceof ShipEntity ship) {
            if (ship.getControllingPassenger() instanceof ServerPlayer p) return p;
            UUID id = ship.getOwner();
            return id == null ? null : world.getServer().getPlayerList().getPlayer(id);
        }
        return null;
    }

    public static String status(MinecraftServer server) {
        State s = State.get(server);
        StringBuilder sb = new StringBuilder();
        for (int fi = 0; fi < FORTS.size(); fi++) {
            Fort f = FORTS.get(fi);
            int alive = 0;
            for (int bi = 0; bi < f.batteries().size(); bi++) if (s.hp[slot(fi, bi)] > 0) alive++;
            sb.append(sb.isEmpty() ? "" : "; ").append(f.name()).append(": ")
                    .append(s.silencedUntil[fi] > server.overworld().getGameTime() ? "silenced" : alive + " guns");
        }
        return sb.toString();
    }
}
