package com.com.chaos.Blocks;

import com.com.chaos.Items.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class MachineUpgrades {
    MachineUpgrades() { }

    public static final int BASIC_SPEED = 2;
    public static final int ADVANCED_SPEED = 4;
    public static final int ELITE_SPEED = 6;
    public static final int CHAOTIC_SPEED = 8;

    public static boolean isProcessor(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item == ModItems.BASIC_PROCESSOR.get()
                || item == ModItems.ADVANCED_PROCESSOR.get()
                || item == ModItems.ELITE_PROCESSOR.get()
                || item == ModItems.CHAOS_PROCESSOR.get();
    }

    public static int getInstalledSpeedBonus(Level level, BlockPos machinePos) {
        int total = 0;
        for (Direction direction : Direction.values()) {
            BlockState neighborState = level.getBlockState(machinePos.relative(direction));
            if (neighborState.getBlock() instanceof ProcessorUpgradeBlock upgrade
                    && neighborState.getValue(ProcessorUpgradeBlock.FACING) == direction) {
                total += upgrade.getSpeedBonus();
            }
        }
        return total > 0 ? total : 1;
    }

    public static double getInstalledBonusChanceModifier(Level level, BlockPos machinePos) {
        double bonus = 0.0;
        for (Direction direction : Direction.values()) {
            BlockState neighborState = level.getBlockState(machinePos.relative(direction));
            Block block = neighborState.getBlock();
            if (!(block instanceof ProcessorUpgradeBlock upgrade)) continue;
            if (neighborState.getValue(ProcessorUpgradeBlock.FACING) != direction) continue;
            if (block instanceof AdvancedProcessorUpgradeBlock) bonus += 0.05;
            if (block instanceof EliteProcessorUpgradeBlock) bonus += 0.10;
            if (block instanceof ChaosProcessorUpgradeBlock) bonus += 0.18;
        }
        return bonus;
    }
}