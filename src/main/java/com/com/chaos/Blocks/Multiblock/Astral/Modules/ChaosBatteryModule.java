package com.com.chaos.Blocks.Multiblock.Astral.Modules;

import com.com.chaos.Blocks.Multiblock.Astral.ModularMultiblockController;
import com.com.chaos.Blocks.Multiblock.Astral.ModularMultiblockModule;
import com.com.chaos.Blocks.Multiblock.MultiblockBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Chaos Battery Module - stores chaos energy and expands nexus capacity.
 * 
 * Uses the shared ModuleAnchorBlock as its anchor point.
 * Structure: 3x3x2 tower of Astral-Abyss Casing with the ModuleAnchorBlock at the center bottom.
 * When attached to a Nexus port, increases storage capacity by 50,000 CE and generates 5 CE/tick passively.
 * 
 * Physical build (when placed at a port):
 *   Layer 1 (top):        Layer 0 (bottom):
 *   A A A                 A A A
 *   A A A                 A M A   (M=ModuleAnchorBlock)
 *   A A A                 A A A
 */
public enum ChaosBatteryModule implements ModularMultiblockModule {
    INSTANCE;

    // 3x3x2 structure with ModuleAnchorBlock in the center bottom
    private static final List<MultiblockBuilder.Cell> PATTERN = MultiblockBuilder.pattern()
            .aisle("AAA",
                   "AAA",
                   "AAA")
            .aisle("AAA",
                   "A@A",
                   "AAA")
            .where('A', MultiblockBuilder.Type.ASTRAL_ABYSS_CASING)
            .anchor('@')
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
        // Capacity bonus is applied when module is detected
        // Note: We need the nexus core position to apply capacity bonus
        // For now, this is a placeholder - you'll need to modify ModularMultiblockController
        // to expose the core anchor position so modules can reference it
    }

    @Override
    public void onDetach(Level level, BlockPos anchorPos) {
        // Capacity bonus is automatically reduced when module detaches
        // The manager handles this automatically via capacity limits
    }

    @Override
    public void serverTick(Level level, BlockPos anchorPos, Direction facing, ModularMultiblockController controller) {
        // Generate ambient energy
        // Note: We need the nexus core position from the controller
        // Currently a limitation - controller doesn't expose its core anchor position
        // 
        // Intended logic:
        // BlockPos nexusPos = controller.getCorePos(); // Need to add to ModularMultiblockController
        // ChaosEnergyManager.get(level).insertEnergy(nexusPos, GENERATION_PER_TICK);
    }

    public static long getCapacityBonus() {
        return CAPACITY_BONUS;
    }

    public static long getGenerationPerTick() {
        return GENERATION_PER_TICK;
    }
}
