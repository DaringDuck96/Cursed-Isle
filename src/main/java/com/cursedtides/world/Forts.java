package com.cursedtides.world;

import com.cursedtides.entity.CannonballEntity;
import com.cursedtides.entity.ShipEntity;
import com.cursedtides.faction.Faction;
import com.cursedtides.faction.FactionData;
import com.cursedtides.item.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PersistentState;
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

    public static class State extends PersistentState {
        /** hp per battery, flattened fort by fort. */
        final int[] hp = new int[FORTS.stream().mapToInt(f -> f.batteries().size()).sum()];
        final long[] silencedUntil = new long[FORTS.size()];

        State() {
            java.util.Arrays.fill(hp, BATTERY_HP);
        }

        private static final Type<State> TYPE = new Type<>(State::new, State::fromNbt, null);

        static State get(MinecraftServer server) {
            return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, "cursedtides_forts");
        }

        @Override
        public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
            nbt.putIntArray("Hp", hp);
            nbt.putLongArray("Silenced", silencedUntil);
            return nbt;
        }

        private static State fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
            State s = new State();
            int[] h = nbt.getIntArray("Hp");
            System.arraycopy(h, 0, s.hp, 0, Math.min(h.length, s.hp.length));
            long[] l = nbt.getLongArray("Silenced");
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

    private static boolean hostileTo(Fort f, PlayerEntity p) {
        return f.navy() ? FactionData.isWanted(p) : FactionData.rep(p, Faction.NAVY) >= 300;
    }

    private static void tick(MinecraftServer server) {
        if (!ModWorld.isArchipelago() || server.getTicks() % 50 != 0) return;
        ServerWorld world = server.getOverworld();
        State s = State.get(server);
        long now = world.getTime();
        for (int fi = 0; fi < FORTS.size(); fi++) {
            Fort f = FORTS.get(fi);
            if (s.silencedUntil[fi] > now) continue;
            if (s.silencedUntil[fi] != 0) { // the garrison has re-armed
                s.silencedUntil[fi] = 0;
                for (int bi = 0; bi < f.batteries().size(); bi++) {
                    s.hp[slot(fi, bi)] = BATTERY_HP;
                    world.setBlockState(gunPos(f.batteries().get(bi)), Blocks.ANVIL.getDefaultState());
                }
                s.markDirty();
            }
            for (int bi = 0; bi < f.batteries().size(); bi++) {
                if (s.hp[slot(fi, bi)] <= 0) continue;
                BlockPos gun = gunPos(f.batteries().get(bi));
                if (!world.isChunkLoaded(gun)) continue;
                ShipEntity target = null;
                double best = 75 * 75;
                for (ShipEntity ship : world.getEntitiesByClass(ShipEntity.class, new net.minecraft.util.math.Box(gun).expand(75), e -> true)) {
                    if (!(ship.getControllingPassenger() instanceof ServerPlayerEntity captain) || !hostileTo(f, captain)) continue;
                    double d = ship.squaredDistanceTo(Vec3d.ofCenter(gun));
                    if (d < best) { best = d; target = ship; }
                }
                if (target == null || world.random.nextInt(3) != 0) continue;
                fire(world, gun, target);
            }
        }
    }

    private static void fire(ServerWorld world, BlockPos gun, Entity target) {
        Vec3d from = Vec3d.ofCenter(gun).add(0, 1.2, 0);
        Vec3d to = target.getPos().add(target.getVelocity().multiply(20)).add(0, 1.5, 0);
        Vec3d d = to.subtract(from);
        double flat = Math.sqrt(d.x * d.x + d.z * d.z);
        CannonballEntity ball = new CannonballEntity(world, from.x, from.y, from.z);
        ball.addCommandTag("cursedtides_fort_shot");
        ball.setVelocity(d.x, d.y + flat * 0.12, d.z, 2.4f, 4.0f);
        world.spawnEntity(ball);
        world.spawnParticles(ParticleTypes.LARGE_SMOKE, from.x, from.y, from.z, 10, 0.3, 0.3, 0.3, 0.02);
        world.playSound(null, gun, com.cursedtides.sound.ModSounds.CANNON_BOOM.value(), SoundCategory.HOSTILE, 3f, 0.8f);
    }

    /** Called where any cannonball lands. */
    public static void onImpact(ServerWorld world, Vec3d at, @Nullable Entity owner, CannonballEntity ball) {
        if (ball.getCommandTags().contains("cursedtides_fort_shot")) return;
        ServerPlayerEntity shooter = shooter(world, owner);
        State s = State.get(world.getServer());
        for (int fi = 0; fi < FORTS.size(); fi++) {
            Fort f = FORTS.get(fi);
            for (int bi = 0; bi < f.batteries().size(); bi++) {
                int k = slot(fi, bi);
                if (s.hp[k] <= 0) continue;
                BlockPos gun = gunPos(f.batteries().get(bi));
                if (at.squaredDistanceTo(Vec3d.ofCenter(gun)) > 4.5 * 4.5) continue;
                s.hp[k]--;
                s.markDirty();
                if (s.hp[k] > 0) {
                    world.setBlockState(gun, (s.hp[k] == 2 ? Blocks.CHIPPED_ANVIL : Blocks.DAMAGED_ANVIL).getDefaultState());
                    if (shooter != null) shooter.sendMessage(Text.literal("Hit on a " + f.name() + " battery!").formatted(Formatting.GOLD), true);
                    return;
                }
                world.setBlockState(gun, Blocks.BLACKSTONE.getDefaultState());
                world.setBlockState(gun.up(), Blocks.FIRE.getDefaultState());
                world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, gun.getX() + 0.5, gun.getY() + 1, gun.getZ() + 0.5, 1, 0, 0, 0, 0);
                boolean anyLeft = false;
                for (int bj = 0; bj < f.batteries().size(); bj++) if (s.hp[slot(fi, bj)] > 0) anyLeft = true;
                if (shooter != null) shooter.sendMessage(Text.literal("A " + f.name() + " battery is silenced!").formatted(Formatting.GOLD), false);
                if (!anyLeft) silenced(world, s, fi, shooter);
                return;
            }
        }
    }

    private static void silenced(ServerWorld world, State s, int fi, @Nullable ServerPlayerEntity shooter) {
        Fort f = FORTS.get(fi);
        s.silencedUntil[fi] = world.getTime() + 24000;
        s.markDirty();
        world.getServer().getPlayerManager().broadcast(Text.literal("The guns of " + f.name() + " have fallen silent"
                + (shooter != null ? ", silenced by " + shooter.getName().getString() : "") + "!").formatted(Formatting.GOLD, Formatting.BOLD), false);
        if (shooter == null) return;
        shooter.giveItemStack(new ItemStack(ModItems.DOUBLOON, 40));
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
    private static ServerPlayerEntity shooter(ServerWorld world, @Nullable Entity owner) {
        if (owner instanceof ServerPlayerEntity p) return p;
        if (owner instanceof ShipEntity ship) {
            if (ship.getControllingPassenger() instanceof ServerPlayerEntity p) return p;
            UUID id = ship.getOwner();
            return id == null ? null : world.getServer().getPlayerManager().getPlayer(id);
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
                    .append(s.silencedUntil[fi] > server.getOverworld().getTime() ? "silenced" : alive + " guns");
        }
        return sb.toString();
    }
}
