package com.com.chaos.Blocks.Multiblock.Astral.NexusOfChaos;

import com.com.chaos.Blocks.Multiblock.MultiblockBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.List;

public class NexusOfChaosStructure implements MultiblockBuilder {

    private static List<Cell> PATTERN = MultiblockBuilder.pattern()
            .aisle( "AAA",
                    "AAA",
                    "AAA")
            .aisle( "A A",
                    " 0 ",
                    "A A")
            .where('A', Type.ASTRAL_CASING)
            .anchor('0')
            .build();

    private NexusOfChaosStructure() {}

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
}
