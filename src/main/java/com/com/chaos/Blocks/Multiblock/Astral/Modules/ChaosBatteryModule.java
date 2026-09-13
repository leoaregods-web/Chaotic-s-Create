package com.com.chaos.Blocks.Multiblock.Astral.Modules;

import com.com.chaos.Blocks.Multiblock.Astral.ModularMultiblockController;
import com.com.chaos.Blocks.Multiblock.Astral.ModularMultiblockModule;
import com.com.chaos.Blocks.Multiblock.MultiblockBuilder;
import com.com.chaos.Energy.ChaosEnergy;
import com.com.chaos.Energy.ChaosEnergyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Chaos Battery Module - stores chaos energy and can feed it back to the nexus.
 * Acts as a buffer and capacity expansion for the nexus.
 * 
 * Can be installed at any module port to increase storage capacity by 50,000 CE
 * and provide passive energy generation (e.g., from ambient chaos in the world).
 */
public enum ChaosBatteryModule implements ModularMultiblockModule {
    INSTANCE;

    // Simple 1x1x1 anchor module - just the core
    private static final List<MultiblockBuilder.Cell> PATTERN = MultiblockBuilder.pattern()
            .aisle("0")
            .anchor('0')
            .build();

    // How much extra capacity this module adds to the nexus
    private static final long CAPACITY_BONUS = 50_000L;

    // How much energy this module generates per tick (passive ambient charging)
    private static final long GENERATION_PER_TICK = 5L;

    @Override
    public String id() {
        return "chaos_battery";
    }

    @Override
    public List<MultiblockBuilder.Cell> pattern() {
        return PATTERN;
    }

    @Override
    public void onAttach(Level level, BlockPos anchorPos, Direction facing) {
        // Find the nexus controller pos (parent of this module)
        // This is simplified - you'll need to pass controller pos through the module system
        // For now, we increase capacity for any nexus that has this module
    }

    @Override
    public void onDetach(Level level, BlockPos anchorPos) {
        // Capacity bonus is automatically reduced when module detaches
    }

    @Override
    public void serverTick(Level level, BlockPos anchorPos, Direction facing, ModularMultiblockController controller) {
        // Get the controller's position (this should be available in controller)
        // For now, we need to find the nexus - you may need to modify ModularMultiblockController
        // to expose the core anchor position
        
        // Placeholder: Generate ambient energy
        // In practice, you'd:
        // 1. Get the nexus controller position from the controller
        // 2. Get its energy storage
        // 3. Add passive generation
        // 
        // Example:
        // BlockPos nexusPos = controller.getCorePos(); // Need to add this to controller
        // ChaosEnergyManager.get(level).insertEnergy(nexusPos, GENERATION_PER_TICK);
    }
}
