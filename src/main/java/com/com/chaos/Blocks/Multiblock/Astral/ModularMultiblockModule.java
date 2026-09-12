package com.com.chaos.Blocks.Multiblock.Astral;

import com.com.chaos.Blocks.Multiblock.MultiblockBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * One kind of pluggable module a ModularMultiblockController can dock at a Port.
 *
 * A module is just another MultiblockBuilder-style shape - build its pattern() exactly
 * like you would GaseousConverterStructure or ChaosReactorStructure's PATTERN, anchored
 * at (0,0,0) in its own local space (MultiblockBuilder.pattern() / .aisle() / .where() /
 * .anchor() / .build() all work the same way here).
 *
 * The one difference from a standalone structure: a module never gets a free 4-way facing
 * search. The Port it's docking into dictates both its anchor position and its facing,
 * since a physical socket only accepts one orientation.
 *
 * A module type is registered against one or more controller types via
 * ModularMultiblockModuleRegistry.register(...) - it isn't handed to a specific
 * ModularMultiblockController directly.
 *
 * Example module type:
 * <pre>{@code
 * public enum ExampleModules implements ModularMultiblockModule {
 *     SPEED_COIL {
 *         private final List<Cell> pattern = MultiblockBuilder.pattern()
 *                 .aisle("A0A")
 *                 .where('A', Type.ASTRAL_CASING)
 *                 .anchor('0')
 *                 .build();
 *
 *         public String id() { return "speed_coil"; }
 *         public List<Cell> pattern() { return pattern; }
 *     };
 *
 *     // during mod setup:
 *     // ModularMultiblockModuleRegistry.register(NexusControllerBlockEntity.CONTROLLER_TYPE, SPEED_COIL);
 * }
 * }</pre>
 *
 * What a module actually DOES once docked is deliberately left open - this interface
 * doesn't know about Forge Energy, Create stress, or any other specific system, since
 * different modules may want completely different things (buff other modules, run a new
 * kind of processing, generate FE, or introduce an entirely new energy type of their own).
 * serverTick(...) is where that behavior lives, and it gets a reference to the owning
 * controller so a module can inspect what else is docked:
 * <pre>{@code
 * public void serverTick(Level level, BlockPos anchorPos, Direction facing, ModularMultiblockController controller) {
 *     long speedBoosts = controller.getActiveModules().values().stream()
 *             .filter(m -> m instanceof SpeedBoostModule)
 *             .count();
 *     // ...apply that to this module's own output, or to a capability at anchorPos, etc.
 * }
 * }</pre>
 * For modules that need to affect each other, a common pattern is to define your own small
 * marker interface (e.g. {@code interface SpeedBoostModule extends ModularMultiblockModule { double bonus(); }})
 * that only the relevant module types implement, and have consumers filter/cast for it -
 * this framework doesn't need to know that interface exists.
 */
public interface ModularMultiblockModule {

    /** Stable id for this module type - used to tell module types apart (logging, save data, etc). */
    String id();

    /** This module's shape, anchored at (0,0,0), same convention as any MultiblockBuilder pattern. */
    List<MultiblockBuilder.Cell> pattern();

    /** Called once when this module type is newly detected filling a port. */
    default void onAttach(Level level, BlockPos anchorPos, Direction facing) {}

    /** Called once when this module type is no longer present at a port it previously filled. */
    default void onDetach(Level level, BlockPos anchorPos) {}

    /**
     * Called every tick this module is docked (see ModularMultiblockController.tickModules).
     * This is where a module does whatever it actually does - buff siblings, run its own
     * processing, push/pull energy, etc. controller lets it look at what else is docked via
     * controller.getActiveModules().
     */
    default void serverTick(Level level, BlockPos anchorPos, Direction facing, ModularMultiblockController controller) {}
}