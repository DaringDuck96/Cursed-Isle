package com.cursedtides.item;

import net.minecraft.world.InteractionResult;
import com.cursedtides.CursedTides;
import com.cursedtides.pirate.PirateData;
import com.cursedtides.pirate.Weapon;
import com.cursedtides.ship.Wind;
import com.cursedtides.world.Archipelago;
import com.cursedtides.world.Builds;
import com.cursedtides.world.ModWorld;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * A weathered treasure map. The first time you read it, it shows where the treasure
 * lies (on one of the islands in a Cursed Archipelago world, or somewhere within a few
 * hundred blocks otherwise). Hold it to see the bearing and distance. Stand on the X and
 * use it again: a chest is buried two blocks down. Dig!
 */
public class TreasureMapItem extends Item {
    static final ResourceKey<LootTable> BURIED = ResourceKey.create(Registries.LOOT_TABLE, CursedTides.id("chests/buried_treasure"));
    static final ResourceKey<LootTable> LEGENDARY = ResourceKey.create(Registries.LOOT_TABLE, CursedTides.id("chests/legendary_treasure"));

    public TreasureMapItem(Properties settings) {
        super(settings);
    }

    public static ItemStack legendary() {
        ItemStack map = new ItemStack(ModItems.TREASURE_MAP);
        CompoundTag data = new CompoundTag();
        data.putBoolean("Legendary", true);
        map.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        map.set(DataComponents.CUSTOM_NAME, Component.literal("Legendary Treasure Map").withStyle(ChatFormatting.GOLD));
        return map;
    }

    private static CompoundTag data(ItemStack stack) {
        CustomData c = stack.get(DataComponents.CUSTOM_DATA);
        return c == null ? new CompoundTag() : c.copyTag();
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack map = user.getItemInHand(hand);
        if (!(world instanceof ServerLevel serverWorld)) {
            return InteractionResult.SUCCESS;
        }
        CompoundTag data = data(map);
        if (!data.contains("TargetX")) {
            int[] target = chooseTarget(serverWorld, user, user.getRandom());
            data.putInt("TargetX", target[0]);
            data.putInt("TargetZ", target[1]);
            map.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 0.8f);
            com.cursedtides.util.Compat.tell(user, Component.literal("The ink resolves into a coastline and an X... "
                    + describe(user, target[0], target[1])).withStyle(ChatFormatting.GOLD), false);
            return InteractionResult.SUCCESS;
        }

        int tx = data.getIntOr("TargetX", 0), tz = data.getIntOr("TargetZ", 0);
        double dist = Math.hypot(tx + 0.5 - user.getX(), tz + 0.5 - user.getZ());
        if (dist > 4.0) {
            com.cursedtides.util.Compat.tell(user, Component.literal(describe(user, tx, tz)).withStyle(ChatFormatting.YELLOW), true);
            return InteractionResult.SUCCESS;
        }

        // Bury the chest under the X
        int surface = serverWorld.getHeight(Heightmap.Types.OCEAN_FLOOR, tx, tz) - 1;
        BlockPos chestPos = new BlockPos(tx, surface - 2, tz);
        world.setBlock(chestPos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH), 3);
        if (world.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setLootTable(data.getBooleanOr("Legendary", false) ? LEGENDARY : BURIED, user.getRandom().nextLong());
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (Math.abs(dx) == Math.abs(dz)) {
                    BlockPos mark = new BlockPos(tx + dx, surface, tz + dz);
                    if (world.getBlockState(mark).isSolidRender()) {
                        world.setBlock(mark, Blocks.COARSE_DIRT.defaultBlockState(), 3);
                    }
                }
            }
        }
        serverWorld.sendParticles(ParticleTypes.HAPPY_VILLAGER, tx + 0.5, surface + 1.2, tz + 0.5, 20, 0.6, 0.3, 0.6, 0.0);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6f, 1.6f);
        com.cursedtides.util.Compat.tell(user, Component.literal("X marks the spot! Something is buried right here. Dig two blocks down.")
                .withStyle(ChatFormatting.GOLD), false);
        if (user instanceof ServerPlayer sp) {
            PirateData.addXp(sp, Weapon.EXPLORATION, data.getBooleanOr("Legendary", false) ? 120 : 40);
            int fee = (int) com.cursedtides.pirate.PerkData.stat(sp, com.cursedtides.pirate.Perk.Stat.FINDERS_FEE);
            if (fee > 0 && !sp.getInventory().add(new ItemStack(ModItems.DOUBLOON, fee))) {
                sp.drop(new ItemStack(ModItems.DOUBLOON, fee), false, net.minecraft.util.Prediction.SERVER_ONLY);
            }
        }
        if (!user.getAbilities().instabuild) {
            map.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    /** Shows the bearing while the map is held. */
    @Override
    public void inventoryTick(ItemStack stack, net.minecraft.server.level.ServerLevel world, Entity entity, net.minecraft.world.entity.EquipmentSlot slot) {
        if (slot != net.minecraft.world.entity.EquipmentSlot.MAINHAND || entity.tickCount % 10 != 0 || !(entity instanceof ServerPlayer player)) {
            return;
        }
        CompoundTag data = data(stack);
        if (data.contains("TargetX")) {
            player.sendSystemMessage(Component.literal(describe(player, data.getIntOr("TargetX", 0), data.getIntOr("TargetZ", 0)))
                    .withStyle(ChatFormatting.YELLOW), true);
        }
    }

    static String describe(Player player, int tx, int tz) {
        double dx = tx + 0.5 - player.getX(), dz = tz + 0.5 - player.getZ();
        int dist = (int) Math.round(Math.hypot(dx, dz));
        if (dist <= 4) return "X marks the spot: you're standing on it. Use the map here.";
        String bearing = Wind.compass((float) Math.toDegrees(Math.atan2(-dx, dz)));
        return "X marks the spot: " + dist + " blocks " + bearing + " (" + tx + ", " + tz + ")";
    }

    private static int[] chooseTarget(ServerLevel world, Player user, RandomSource random) {
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
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display__, java.util.function.Consumer<Component> adder__, TooltipFlag type) {
        java.util.List<Component> tooltip = new java.util.ArrayList<>();
        CompoundTag data = data(stack);
        if (data.contains("TargetX")) {
            tooltip.add(Component.literal("X at " + data.getIntOr("TargetX", 0) + ", " + data.getIntOr("TargetZ", 0)).withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.literal("Hold it to follow the bearing. Use it on the X.").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.literal("Unread. Use it to study the markings.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
        tooltip.forEach(adder__);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return data(stack).getBooleanOr("Legendary", false);
    }
}
