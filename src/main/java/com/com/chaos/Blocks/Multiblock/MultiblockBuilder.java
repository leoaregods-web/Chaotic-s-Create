package com.com.chaos.Blocks.Multiblock;

import com.com.chaos.Blocks.ModBlocks;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.gearbox.GearboxBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public interface MultiblockBuilder {

    public record Cell(int right, int back, int z, Type type) {}

    public enum Type {
        STRUCTURE,
        ASTRAL_CASING,
        ABYSS_CASING,
        ASTRAL_ABYSS_CASING,
        TURBINE,
        VGEARBOX,
        HGEARBOX,
        CHAOS_STRUCTURAL,
        CHAOS_INPUT,
        EMPTY
    }

    /** Absolute position of a pattern cell, given the anchor and its facing. */
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

    static boolean matchesRole(BlockState state, Type type) {
        return switch (type) {
            case STRUCTURE -> state.is(Blocks.DEEPSLATE_BRICKS);
            case ABYSS_CASING -> state.is(ModBlocks.ABYSS_CASING.get());
            case ASTRAL_CASING -> state.is(ModBlocks.ASTRAL_CASING.get());
            case ASTRAL_ABYSS_CASING -> state.is(ModBlocks.ASTRAL_ABYSS_CASING.get());
            case TURBINE -> state.is(ModBlocks.CHAOS_TURBINE.get());
            case VGEARBOX -> state.is(AllBlocks.GEARBOX.get())
                    && state.getValue(GearboxBlock.AXIS) == Direction.Axis.X;
            case HGEARBOX -> state.is(AllBlocks.GEARBOX.get())
                    && state.getValue(GearboxBlock.AXIS) == Direction.Axis.Y;
            case CHAOS_STRUCTURAL, CHAOS_INPUT -> state.is(ModBlocks.CHAOS_CRYSTALS.get());
            case EMPTY -> state.isAir();
        };
    }

    /** Detailed result used by the controller logger/debug tools. */
    public record MatchResult(
            Direction facing,
            boolean matched,
            Cell failedCell,
            BlockPos failedPos,
            Type expectedRole,
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

    static Direction findValidFacing(Level level, BlockPos anchorPos, List<Cell> pattern) {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            if (matchesPattern(level, anchorPos, facing, pattern)) return facing;
        }
        return null;
    }

    static boolean matchesPattern(Level level, BlockPos anchorPos, Direction facing, List<Cell> pattern) {
        return diagnosePattern(level, anchorPos, facing, pattern).matched();
    }

    static MatchResult diagnosePattern(Level level, BlockPos anchorPos, Direction facing, List<Cell> pattern) {
        for (Cell cell : pattern) {
            BlockPos pos = resolve(anchorPos, facing, cell.right(), cell.back(), cell.z());
            BlockState actual = level.getBlockState(pos);
            if (!matchesRole(actual, cell.type())) {
                return new MatchResult(facing, false, cell, pos, cell.type(), actual);
            }
        }
        return new MatchResult(facing, true, null, null, null, null);
    }

    /**
     * Like findValidFacing, but hands back the full MatchResult for whichever facing matched
     * (facing/matched/etc.), instead of forcing the caller to re-diagnose after the fact.
     * Returns null if no facing matches.
     */
    static MatchResult findValidFacingResult(Level level, BlockPos anchorPos, List<Cell> pattern) {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            MatchResult result = diagnosePattern(level, anchorPos, facing, pattern);
            if (result.matched()) {
                return result;
            }
        }
        return null;
    }

    /**
     * One-line-per-facing debug report: for each of the 4 horizontal facings, either "matched"
     * or the first mismatched cell for that facing, per MatchResult.describe().
     */
    static String debugFacingReport(Level level, BlockPos anchorPos, List<Cell> pattern) {
        StringJoiner joiner = new StringJoiner("; ");
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            MatchResult result = diagnosePattern(level, anchorPos, facing, pattern);
            joiner.add(facing + ": " + result.describe());
        }
        return joiner.toString();
    }

    /** Exposes a structure's pattern cells to debug tooling without needing direct access to its private PATTERN field. */
    static List<Cell> debugCells(List<Cell> pattern) {
        return pattern;
    }

    /** Public, debug-tool-friendly name for matchesRole - same check, callable from outside the interface's own static methods. */
    static boolean matchesRoleDebug(BlockState state, Type type) {
        return matchesRole(state, type);
    }

    static PatternBuilder pattern() {
        return new PatternBuilder();
    }

    /**
     * Fluent, aisle-based way to write a pattern instead of hand-listing (right, back, up, Type)
     * cells one at a time - the same idiom vanilla Minecraft uses for things like the wither,
     * iron golem, and nether portal frame (BlockPatternBuilder), adapted for our Cell/Type model.
     * Works for any width, depth, or number of layers - the shape isn't limited to 3x3x3.
     *
     * <pre>{@code
     * private static final List<Cell> PATTERN = MultiblockBuilder.pattern()
     *         .aisle("DAD",
     *                "AVA",
     *                "DAD")
     *         .aisle("D0D",
     *                "A A",
     *                "DTD")
     *         .aisle("DAD",
     *                "AVA",
     *                "DAD")
     *         .where('D', Type.STRUCTURE)
     *         .where('A', Type.ASTRAL_CASING)
     *         .where('V', Type.VGEARBOX)
     *         .where('T', Type.TURBINE)
     *         .anchor('0')
     *         .build();
     * }</pre>
     *
     * Reading order:
     * <ul>
     *   <li>Each {@code .aisle(...)} call is one horizontal layer. Aisles are listed bottom to
     *       top - {@code up} increases with each successive aisle.</li>
     *   <li>Within an aisle, each row string is one line running left-to-right across the
     *       structure. Rows are listed front to back - {@code back} increases with each row.</li>
     *   <li>Within a row, each character is one column. Reading left to right, {@code right}
     *       increases.</li>
     * </ul>
     * A blank space {@code ' '} always means {@link Type#EMPTY} and never needs a
     * {@code .where()} entry. Exactly one cell across the whole shape must be the anchor
     * character set via {@code .anchor(...)} - that's the block entity doing the scan, so
     * unlike every other cell it is NOT included in the built pattern (its own role is never
     * checked - it's already known to be there).
     */
    final class PatternBuilder {
        private final List<String[]> aisles = new ArrayList<>();
        private final Map<Character, Type> key = new HashMap<>();
        private Character anchorChar;

        private PatternBuilder() {}

        /** One horizontal layer, listed bottom to top across successive calls. Rows run front to back. */
        public PatternBuilder aisle(String... rows) {
            if (rows.length == 0) {
                throw new IllegalArgumentException("aisle() needs at least one row");
            }
            aisles.add(rows);
            return this;
        }

        /** Maps a character used in an aisle row to the role it represents. */
        public PatternBuilder where(char c, Type type) {
            if (c == ' ') {
                throw new IllegalArgumentException("' ' always means Type.EMPTY and can't be remapped");
            }
            key.put(c, type);
            return this;
        }

        /** Marks which character is the anchor - the block entity itself, excluded from the built pattern. */
        public PatternBuilder anchor(char c) {
            this.anchorChar = c;
            return this;
        }

        public List<Cell> build() {
            if (aisles.isEmpty()) {
                throw new IllegalStateException("Pattern has no aisle() calls");
            }
            if (anchorChar == null) {
                throw new IllegalStateException("Pattern has no anchor() character set");
            }

            int rowCount = aisles.get(0).length;
            int colCount = aisles.get(0)[0].length();
            for (String[] aisle : aisles) {
                if (aisle.length != rowCount) {
                    throw new IllegalArgumentException(
                            "Every aisle must have the same number of rows (expected " + rowCount
                                    + ", got " + aisle.length + ")");
                }
                for (String row : aisle) {
                    if (row.length() != colCount) {
                        throw new IllegalArgumentException(
                                "Every row must have the same length (expected " + colCount
                                        + ", got " + row.length() + " for \"" + row + "\")");
                    }
                }
            }

            int anchorUp = -1, anchorBack = -1, anchorRight = -1, anchorHits = 0;
            for (int up = 0; up < aisles.size(); up++) {
                String[] aisle = aisles.get(up);
                for (int back = 0; back < aisle.length; back++) {
                    String row = aisle[back];
                    for (int right = 0; right < row.length(); right++) {
                        if (row.charAt(right) == anchorChar) {
                            anchorUp = up;
                            anchorBack = back;
                            anchorRight = right;
                            anchorHits++;
                        }
                    }
                }
            }
            if (anchorHits != 1) {
                throw new IllegalStateException(
                        "Pattern must contain exactly one anchor '" + anchorChar + "' cell, found " + anchorHits);
            }

            List<Cell> cells = new ArrayList<>();
            for (int up = 0; up < aisles.size(); up++) {
                String[] aisle = aisles.get(up);
                for (int back = 0; back < aisle.length; back++) {
                    String row = aisle[back];
                    for (int right = 0; right < row.length(); right++) {
                        if (up == anchorUp && back == anchorBack && right == anchorRight) {
                            continue; // the anchor cell isn't part of the pattern
                        }
                        char c = row.charAt(right);
                        Type type;
                        if (c == ' ') {
                            type = Type.EMPTY;
                        } else {
                            type = key.get(c);
                            if (type == null) {
                                throw new IllegalStateException("No where('" + c + "', ...) mapping was given");
                            }
                        }
                        cells.add(new Cell(right - anchorRight, back - anchorBack, up - anchorUp, type));
                    }
                }
            }
            return List.copyOf(cells);
        }
    }
}