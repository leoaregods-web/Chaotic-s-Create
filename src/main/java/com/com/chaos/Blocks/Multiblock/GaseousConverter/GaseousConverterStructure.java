package com.com.chaos.Blocks.Multiblock.GaseousConverter;

import com.com.chaos.Blocks.ModBlocks;
import com.mojang.logging.LogUtils;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.gearbox.GearboxBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

import java.util.List;
import java.util.StringJoiner;

public final class GaseousConverterStructure {
    private static final Logger LOGGER = LogUtils.getLogger();

    public enum Role {
        DEEPSLATE_BRICKS,
        ASTRAL_CASING,
        TURBINE,
        VERTICAL_GEARBOX,
        EMPTY
    }

    public record Cell(int right, int back, int up, Role role) {}

    /** Detailed result used by the controller logger/debug tools. */
    public record MatchResult(
            Direction facing,
            boolean matched,
            Cell failedCell,
            BlockPos failedPos,
            Role expectedRole,
            BlockState actualState) {

        public String describe() {
            if (matched) {
                return "matched";
            }

            String actualId = BuiltInRegistries.BLOCK.getKey(actualState.getBlock()).toString();
            return "expected " + expectedRole
                    + " at " + failedPos
                    + " but found " + actualId
                    + " (state=" + actualState + ")";
        }
    }

    // The anchor itself is (0, 0, 0) and is checked separately.
    // Every entry below is one of the other 26 cells in the 3x3x3 shell.
    private static final List<Cell> PATTERN = List.of(
            // --- base layer (up = -1) ---
            new Cell(-1, 0, -1, Role.DEEPSLATE_BRICKS),
            new Cell( 0, 0, -1, Role.ASTRAL_CASING),
            new Cell( 1, 0, -1, Role.DEEPSLATE_BRICKS),
            new Cell(-1, 1, -1, Role.ASTRAL_CASING),
            new Cell( 0, 1, -1, Role.VERTICAL_GEARBOX),
            new Cell( 1, 1, -1, Role.ASTRAL_CASING),
            new Cell(-1, 2, -1, Role.DEEPSLATE_BRICKS),
            new Cell( 0, 2, -1, Role.ASTRAL_CASING),
            new Cell( 1, 2, -1, Role.DEEPSLATE_BRICKS),

            // --- middle layer (up = 0) ---
            new Cell(-1, 0, 0, Role.DEEPSLATE_BRICKS),
            new Cell( 1, 0, 0, Role.DEEPSLATE_BRICKS),
            new Cell(-1, 1, 0, Role.ASTRAL_CASING),
            new Cell( 0, 1, 0, Role.EMPTY),
            new Cell( 1, 1, 0, Role.ASTRAL_CASING),
            new Cell(-1, 2, 0, Role.DEEPSLATE_BRICKS),
            new Cell( 0, 2, 0, Role.TURBINE),
            new Cell( 1, 2, 0, Role.DEEPSLATE_BRICKS),

            // --- top layer (up = 1) ---
            new Cell(-1, 0, 1, Role.DEEPSLATE_BRICKS),
            new Cell( 0, 0, 1, Role.ASTRAL_CASING),
            new Cell( 1, 0, 1, Role.DEEPSLATE_BRICKS),
            new Cell(-1, 1, 1, Role.ASTRAL_CASING),
            new Cell( 0, 1, 1, Role.VERTICAL_GEARBOX),
            new Cell( 1, 1, 1, Role.ASTRAL_CASING),
            new Cell(-1, 2, 1, Role.DEEPSLATE_BRICKS),
            new Cell( 0, 2, 1, Role.ASTRAL_CASING),
            new Cell( 1, 2, 1, Role.DEEPSLATE_BRICKS)
    );

    public static final Cell GEARBOX_BOTTOM = new Cell(0, 1, -1, Role.TURBINE);
    public static final Cell GEARBOX_TOP = new Cell(0, 1, 1, Role.TURBINE);
    public static final Cell REAR_GEARBOX = new Cell(0, 2, 0, Role.VERTICAL_GEARBOX);

    private GaseousConverterStructure() {}

    public static Direction findValidFacing(Level level, BlockPos anchorPos) {
        MatchResult result = findValidFacingResult(level, anchorPos);
        return result == null ? null : result.facing();
    }

    /**
     * Returns the first matching horizontal orientation, or null when none match.
     * The full failure information is available through debugFacingReport().
     */
    public static MatchResult findValidFacingResult(Level level, BlockPos anchorPos) {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            MatchResult result = diagnose(level, anchorPos, facing);
            if (result.matched()) {
                return result;
            }
        }
        return null;
    }

    public static boolean matches(Level level, BlockPos anchorPos, Direction facing) {
        return diagnose(level, anchorPos, facing).matched();
    }

    public static MatchResult diagnose(Level level, BlockPos anchorPos, Direction facing) {
        BlockState anchorState = level.getBlockState(anchorPos);
        if (!anchorState.is(ModBlocks.CHAOS_TURBINE.get())) {
            return new MatchResult(
                    facing,
                    false,
                    null,
                    anchorPos,
                    Role.TURBINE,
                    anchorState);
        }

        for (Cell cell : PATTERN) {
            BlockPos pos = resolve(anchorPos, facing, cell.right(), cell.back(), cell.up());
            BlockState actual = level.getBlockState(pos);
            if (!matchesRole(actual, cell.role())) {
                return new MatchResult(
                        facing,
                        false,
                        cell,
                        pos,
                        cell.role(),
                        actual);
            }
        }

        return new MatchResult(facing, true, null, null, null, anchorState);
    }

    /**
     * Human-readable report for all four orientations. This is deliberately
     * generated only when a structure is invalid so it can be logged safely.
     */
    public static String debugFacingReport(Level level, BlockPos anchorPos) {
        StringJoiner joiner = new StringJoiner(" | ");
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            MatchResult result = diagnose(level, anchorPos, facing);
            if (result.matched()) {
                joiner.add(facing + ": MATCH");
            } else {
                joiner.add(facing + ": " + result.describe());
            }
        }
        return joiner.toString();
    }

    public static List<Cell> debugCells() {
        return PATTERN;
    }

    public static boolean matchesRoleDebug(BlockState state, Role role) {
        return matchesRole(state, role);
    }

    private static boolean matchesRole(BlockState state, Role role) {
        return switch (role) {
            case DEEPSLATE_BRICKS -> state.is(Blocks.DEEPSLATE_BRICKS);
            case ASTRAL_CASING -> state.is(ModBlocks.ASTRAL_CASING.get());
            case TURBINE -> state.is(ModBlocks.CHAOS_TURBINE.get());
            case VERTICAL_GEARBOX -> state.is(AllBlocks.GEARBOX.get())
                    && state.getValue(GearboxBlock.AXIS) == Direction.Axis.X;
            case EMPTY -> state.isAir();
        };
    }

    public static BlockPos resolve(
            BlockPos anchorPos,
            Direction facing,
            int right,
            int back,
            int up) {
        Direction rightDir = facing.getCounterClockWise();
        Direction backDir = facing.getOpposite();
        return anchorPos.relative(rightDir, right)
                .relative(backDir, back)
                .relative(Direction.UP, up);
    }

    public static BlockPos findControllerFromRearCell(Level level, BlockPos pos) {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            // Cell (0, 2, 0) is `pos` relative to anchor: anchor.relative(facing.getOpposite(), 2) == pos
            // so anchor = pos.relative(facing, 2)
            BlockPos candidateAnchor = pos.relative(facing, 2);
            if (matches(level, candidateAnchor, facing)) {
                return candidateAnchor;
            }
        }
        return null;
    }
}
