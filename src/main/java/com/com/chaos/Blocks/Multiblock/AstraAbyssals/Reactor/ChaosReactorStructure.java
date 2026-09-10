package com.com.chaos.Blocks.Multiblock.AstraAbyssals.Reactor;

import com.com.chaos.Blocks.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

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
public final class ChaosReactorStructure {

    public enum Role {
        DEEPSLATE_BRICKS,
        ABYSS_CASING,
        ASTRAL_CASING,
        CRYSTAL_STRUCTURAL,   // base cardinals - anchor the structure, no IO
        CRYSTAL_INPUT,        // middle sides - accept capsules/fluids/items in
        HOLLOW_CORE           // must be air - the visible chamber
    }

    public record Cell(int right, int back, int up, Role role) {}

    // The anchor itself (the output crystal) sits at (0,0,0) and is not
    // included here - it's the block entity running the scan, not something
    // it needs to check against the world.
    private static final List<Cell> PATTERN = List.of(
        // --- base layer (up = -1) ---
        new Cell(-1, 0, -1, Role.DEEPSLATE_BRICKS),
        new Cell( 0, 0, -1, Role.CRYSTAL_STRUCTURAL),
        new Cell( 1, 0, -1, Role.DEEPSLATE_BRICKS),
        new Cell(-1, 1, -1, Role.CRYSTAL_STRUCTURAL),
        new Cell( 0, 1, -1, Role.HOLLOW_CORE),
        new Cell( 1, 1, -1, Role.CRYSTAL_STRUCTURAL),
        new Cell(-1, 2, -1, Role.DEEPSLATE_BRICKS),
        new Cell( 0, 2, -1, Role.CRYSTAL_STRUCTURAL),
        new Cell( 1, 2, -1, Role.DEEPSLATE_BRICKS),

        // --- middle layer (up = 0) - anchor at (0,0,0) omitted ---
        new Cell(-1, 0, 0, Role.ABYSS_CASING),
        new Cell( 1, 0, 0, Role.ABYSS_CASING),
        new Cell(-1, 1, 0, Role.CRYSTAL_INPUT),
        new Cell( 0, 1, 0, Role.HOLLOW_CORE),
        new Cell( 1, 1, 0, Role.CRYSTAL_INPUT),
        new Cell(-1, 2, 0, Role.ABYSS_CASING),
        new Cell( 0, 2, 0, Role.ABYSS_CASING),
        new Cell( 1, 2, 0, Role.ABYSS_CASING),

        // --- top layer (up = 1) ---
        new Cell(-1, 0, 1, Role.ASTRAL_CASING),
        new Cell( 0, 0, 1, Role.ASTRAL_CASING),
        new Cell( 1, 0, 1, Role.ASTRAL_CASING),
        new Cell(-1, 1, 1, Role.ASTRAL_CASING),
        new Cell( 0, 1, 1, Role.HOLLOW_CORE),
        new Cell( 1, 1, 1, Role.ASTRAL_CASING),
        new Cell(-1, 2, 1, Role.ASTRAL_CASING),
        new Cell( 0, 2, 1, Role.ASTRAL_CASING),
        new Cell( 1, 2, 1, Role.ASTRAL_CASING)
    );

    /** The two input-crystal cells, exposed so the controller can find them directly. */
    public static final Cell INPUT_LEFT = new Cell(-1, 1, 0, Role.CRYSTAL_INPUT);
    public static final Cell INPUT_RIGHT = new Cell(1, 1, 0, Role.CRYSTAL_INPUT);

    /** The three hollow-core cells (base -> top), for the contents renderer. */
    public static final int[] CORE_UP_OFFSETS = { -1, 0, 1 };
    public static final int CORE_RIGHT = 0;
    public static final int CORE_BACK = 1;

    private ChaosReactorStructure() {}

    /**
     * Tries all four horizontal facings around the anchor and returns the
     * one that matches, or null if the structure isn't (yet) valid.
     * Call this treating the block doing the check as the anchor/output.
     */
    public static Direction findValidFacing(Level level, BlockPos anchorPos) {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            if (matches(level, anchorPos, facing)) {
                return facing;
            }
        }
        return null;
    }

    public static boolean matches(Level level, BlockPos anchorPos, Direction facing) {
        for (Cell cell : PATTERN) {
            BlockPos pos = resolve(anchorPos, facing, cell.right(), cell.back(), cell.up());
            if (!matchesRole(level.getBlockState(pos), cell.role())) {
                return false;
            }
        }
        return true;
    }

    /** Exposes the full pattern list for debug tooling (e.g. reporting the first mismatched cell). */
    public static List<Cell> debugCells() {
        return PATTERN;
    }

    /** Public wrapper around the role check, for debug tooling outside this class. */
    public static boolean matchesRoleDebug(BlockState state, Role role) {
        return matchesRole(state, role);
    }

    private static boolean matchesRole(BlockState state, Role role) {
        return switch (role) {
            case DEEPSLATE_BRICKS -> state.is(Blocks.DEEPSLATE_BRICKS);
            case ABYSS_CASING -> state.is(ModBlocks.ABYSS_CASING.get());
            case ASTRAL_CASING -> state.is(ModBlocks.ASTRAL_CASING.get());
            case CRYSTAL_STRUCTURAL, CRYSTAL_INPUT -> state.is(ModBlocks.CHAOS_CRYSTALS.get());
            case HOLLOW_CORE -> state.isAir();
        };
    }

    /** Absolute position of a pattern cell, given the anchor and its facing. */
    public static BlockPos resolve(BlockPos anchorPos, Direction facing, int right, int back, int up) {
        Direction rightDir = facing.getCounterClockWise();
        Direction backDir = facing.getOpposite();
        return anchorPos.relative(rightDir, right)
            .relative(backDir, back)
            .relative(Direction.UP, up);
    }
}
