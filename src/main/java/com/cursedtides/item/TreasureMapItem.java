package com.cursedtides.item;

import com.cursedtides.CursedTides;
import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import com.cursedtides.ship.Wind;
import com.cursedtides.world.Archipelago;
import com.cursedtides.world.Builds;
import com.cursedtides.world.ModWorld;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.List;

/**
 * A weathered treasure map. The first time you read it, it shows where the treasure
 * lies (on one of the islands in a Cursed Archipelago world, or somewhere within a few
 * hundred blocks otherwise). Hold it to see the bearing and distance. Stand on the X and
 * use it again: a chest is buried two blocks down. Dig!
 */
public class TreasureMapItem extends Item {
    static final RegistryKey<LootTable> BURIED = RegistryKey.of(RegistryKeys.LOOT_TABLE, CursedTides.id("chests/buried_treasure"));
    static final RegistryKey<LootTable> LEGENDARY = RegistryKey.of(RegistryKeys.LOOT_TABLE, CursedTides.id("chests/legendary_treasure"));

    public TreasureMapItem(Settings settings) {
        super(settings);
    }

    public static ItemStack legendary() {
        ItemStack map = new ItemStack(ModItems.TREASURE_MAP);
        NbtCompound data = new NbtCompound();
        data.putBoolean("Legendary", true);
        map.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(data));
        map.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Legendary Treasure Map").formatted(Formatting.GOLD));
        return map;
    }

    private static NbtCompound data(ItemStack stack) {
        NbtComponent c = stack.get(DataComponentTypes.CUSTOM_DATA);
        return c == null ? new NbtCompound() : c.copyNbt();
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack map = user.getStackInHand(hand);
        if (!(world instanceof ServerWorld serverWorld)) {
            return TypedActionResult.success(map, true);
        }
        NbtCompound data = data(map);
        if (!data.contains("TargetX")) {
            int[] target = chooseTarget(serverWorld, user, user.getRandom());
            data.putInt("TargetX", target[0]);
            data.putInt("TargetZ", target[1]);
            map.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(data));
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.PLAYERS, 1.0f, 0.8f);
            user.sendMessage(Text.literal("The ink resolves into a coastline and an X... "
                    + describe(user, target[0], target[1])).formatted(Formatting.GOLD), false);
            return TypedActionResult.success(map, false);
        }

        int tx = data.getInt("TargetX"), tz = data.getInt("TargetZ");
        double dist = Math.hypot(tx + 0.5 - user.getX(), tz + 0.5 - user.getZ());
        if (dist > 4.0) {
            user.sendMessage(Text.literal(describe(user, tx, tz)).formatted(Formatting.YELLOW), true);
            return TypedActionResult.success(map, false);
        }

        // Bury the chest under the X
        int surface = serverWorld.getTopY(Heightmap.Type.OCEAN_FLOOR, tx, tz) - 1;
        BlockPos chestPos = new BlockPos(tx, surface - 2, tz);
        world.setBlockState(chestPos, Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, Direction.NORTH), 3);
        if (world.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setLootTable(data.getBoolean("Legendary") ? LEGENDARY : BURIED, user.getRandom().nextLong());
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (Math.abs(dx) == Math.abs(dz)) {
                    BlockPos mark = new BlockPos(tx + dx, surface, tz + dz);
                    if (world.getBlockState(mark).isOpaqueFullCube(world, mark)) {
                        world.setBlockState(mark, Blocks.COARSE_DIRT.getDefaultState(), 3);
                    }
                }
            }
        }
        serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, tx + 0.5, surface + 1.2, tz + 0.5, 20, 0.6, 0.3, 0.6, 0.0);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.6f, 1.6f);
        user.sendMessage(Text.literal("X marks the spot! Something is buried right here. Dig two blocks down.")
                .formatted(Formatting.GOLD), false);
        if (user instanceof ServerPlayerEntity sp) {
            PirateData.addXp(sp, Weapon.EXPLORATION, data.getBoolean("Legendary") ? 120 : 40);
            int fee = (int) com.cursedtides.pirate.PerkData.stat(sp, com.cursedtides.pirate.Perk.Stat.FINDERS_FEE);
            if (fee > 0 && !sp.getInventory().insertStack(new ItemStack(ModItems.DOUBLOON, fee))) {
                sp.dropItem(new ItemStack(ModItems.DOUBLOON, fee), false);
            }
        }
        if (!user.getAbilities().creativeMode) {
            map.decrement(1);
        }
        return TypedActionResult.success(map, false);
    }

    /** Shows the bearing while the map is held. */
    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient || !selected || entity.age % 10 != 0 || !(entity instanceof ServerPlayerEntity player)) {
            return;
        }
        NbtCompound data = data(stack);
        if (data.contains("TargetX")) {
            player.sendMessage(Text.literal(describe(player, data.getInt("TargetX"), data.getInt("TargetZ")))
                    .formatted(Formatting.YELLOW), true);
        }
    }

    static String describe(PlayerEntity player, int tx, int tz) {
        double dx = tx + 0.5 - player.getX(), dz = tz + 0.5 - player.getZ();
        int dist = (int) Math.round(Math.hypot(dx, dz));
        if (dist <= 4) return "X marks the spot: you're standing on it. Use the map here.";
        String bearing = Wind.compass((float) Math.toDegrees(Math.atan2(-dx, dz)));
        return "X marks the spot: " + dist + " blocks " + bearing + " (" + tx + ", " + tz + ")";
    }

    private static int[] chooseTarget(ServerWorld world, PlayerEntity user, Random random) {
        if (ModWorld.isArchipelago()) {
            List<Archipelago.Island> islands = Archipelago.ISLANDS.stream()
                    .filter(i -> i.style() != Archipelago.Style.REEF).toList();
            for (int attempt = 0; attempt < 60; attempt++) {
                Archipelago.Island is = islands.get(random.nextInt(islands.size()));
                double a = random.nextDouble() * Math.PI * 2;
                double r = random.nextDouble() * is.radius() * 0.6;
                int x = is.cx() + (int) Math.round(Math.cos(a) * r);
                int z = is.cz() + (int) Math.round(Math.sin(a) * r);
                var col = Archipelago.column(x, z);
                if (col.isLand() && !col.crater() && !Builds.blocksPlants(x, z)) {
                    return new int[]{x, z};
                }
            }
        }
        double a = random.nextDouble() * Math.PI * 2;
        double r = 150 + random.nextDouble() * 250;
        return new int[]{(int) (user.getX() + Math.cos(a) * r), (int) (user.getZ() + Math.sin(a) * r)};
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        NbtCompound data = data(stack);
        if (data.contains("TargetX")) {
            tooltip.add(Text.literal("X at " + data.getInt("TargetX") + ", " + data.getInt("TargetZ")).formatted(Formatting.GOLD));
            tooltip.add(Text.literal("Hold it to follow the bearing. Use it on the X.").formatted(Formatting.GRAY));
        } else {
            tooltip.add(Text.literal("Unread. Use it to study the markings.").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return data(stack).getBoolean("Legendary");
    }
}
