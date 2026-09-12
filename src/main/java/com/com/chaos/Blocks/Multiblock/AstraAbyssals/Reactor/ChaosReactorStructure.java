package com.com.chaos.Blocks.Multiblock.AstraAbyssals.Reactor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import com.com.chaos.Blocks.Multiblock.MultiblockBuilder;

import java.util.List;

/**
 * Defines and validates the Chaos Reactor multiblock.
 *
 * The structure is a 3x3x3 volume, three layers tall, anchored on the
 * OUTPUT Chaos Crystal in the middle layer - that's the block a player
 * places facing outward, and the one whose block entity runs the scan
 * and does the actual recipe processing.
 *
 * All offsets below are in the anchor's local space:
 *   right -> perpendicular to facing, positive = clockwise from facing
 *   back  -> 0 at the anchor's row (the front row), increasing toward the rear
 *   up    -> 0 at the middle layer, -1 = base layer, +1 = top layer
 *
 * Layout (top-down view of each layer, X = right, rows = back):
 *
 *   Base (up=-1)          Middle (up=0)          Top (up=1)
 *   D  C  D               A  OUT A              A  A  A
 *   C  .  C               Ci  .  Ci               A  .  A
 *   D  C  D               A  A  A                A  A  A
 *
 *   D = Deepslate Bricks   A = Abyss Casing (base), Astral Casing (top)
 *   C = Chaos Crystal (structural)   Ci = Chaos Crystal (input)
 *   OUT = Chaos Crystal (output, the anchor)   . = hollow core (must be air)
 *
 * The three "." cells stack into a single 3-tall vertical shaft - that's
 * the hollow chamber the contents renderer fills with the reactor's
 * current fluid / gas / item contents.
 */
public final class ChaosReactorStructure implements MultiblockBuilder{
    /** The two input-crystal cells, exposed so the controller can find them directly. */
    public static final Cell INPUT_LEFT = new Cell(-1, 1, 0, Type.CHAOS_INPUT);
    public static final Cell INPUT_RIGHT = new Cell(1, 1, 0, Type.CHAOS_INPUT);

    /** The three hollow-core cells (base -> top), for the contents renderer. */
    public static final int[] CORE_UP_OFFSETS = { -1, 0, 1 };
    public static final int CORE_RIGHT = 0;
    public static final int CORE_BACK = 1;


    private static final List<Cell> PATTERN = MultiblockBuilder.pattern()
            .aisle("DSD",
                    "S S",
                    "DSD")
            .aisle("B0B",
                    "I I",
                    "BBB")
            .aisle("AAA",
                    "A A",
                    "AAA")
            .where('D', Type.STRUCTURE)
            .where('S', Type.CHAOS_STRUCTURAL)
            .where('B', Type.ABYSS_CASING)
            .where('I', Type.CHAOS_INPUT)
            .where('A', Type.ASTRAL_CASING)
            .anchor('0')
            .build();


    private ChaosReactorStructure() {}

    public static Direction findValidFacing(Level level, BlockPos anchorPos) {
        return MultiblockBuilder.findValidFacing(level, anchorPos, PATTERN);
    }

    public static boolean matches(Level level, BlockPos anchorPos, Direction facing) {
        return MultiblockBuilder.matchesPattern(level, anchorPos, facing, PATTERN);
    }

    public static List<Cell> debugCells() {
        return MultiblockBuilder.debugCells(PATTERN);
    }

    public static boolean matchesRoleDebug(BlockState state, Type type) {
        return MultiblockBuilder.matchesRoleDebug(state, type);
    }
}