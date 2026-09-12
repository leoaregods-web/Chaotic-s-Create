package com.com.chaos.Blocks.Multiblock.Abyssals.GaseousConverter;

import com.com.chaos.Blocks.ModBlocks;
import com.com.chaos.Blocks.Multiblock.MultiblockBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

import java.util.List;

public final class GaseousConverterStructure implements MultiblockBuilder {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final List<Cell> PATTERN = MultiblockBuilder.pattern()
            .aisle("DAD",
                    "AVA",
                    "DAD")
            .aisle("D0D",
                    "A A",
                    "DTD")
            .aisle("DAD",
                    "AVA",
                    "DAD")
            .where('D', Type.STRUCTURE)
            .where('A', Type.ASTRAL_CASING)
            .where('V', Type.VGEARBOX)
            .where('T', Type.TURBINE)
            .anchor('0')
            .build();

    public static final Cell GEARBOX_BOTTOM = new Cell(0, 1, -1, Type.VGEARBOX);
    public static final Cell GEARBOX_TOP = new Cell(0, 1, 1, Type.VGEARBOX);

    private GaseousConverterStructure() {}

    public static Direction findValidFacing(Level level, BlockPos anchorPos) {
        return MultiblockBuilder.findValidFacing(level, anchorPos, PATTERN);
    }

    public static boolean matches(Level level, BlockPos anchorPos, Direction facing) {
        return MultiblockBuilder.matchesPattern(level, anchorPos, facing, PATTERN);
    }

    public static MatchResult findValidFacingResult(Level level, BlockPos anchorPos) {
        return MultiblockBuilder.findValidFacingResult(level, anchorPos, PATTERN);
    }

    public static String debugFacingReport(Level level, BlockPos anchorPos) {
        return MultiblockBuilder.debugFacingReport(level, anchorPos, PATTERN);
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