package com.com.chaos.Blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.Map;

public class BasicProcessorUpgradeBlock extends ProcessorUpgradeBlock {
    private static Map<Direction, VoxelShape> SHAPES_CACHE = null;

    public BasicProcessorUpgradeBlock(Properties properties) {
        super(properties, MachineUpgrades.BASIC_SPEED);
    }

    private static Map<Direction, VoxelShape> getShapes() {
        if (SHAPES_CACHE == null) {
            SHAPES_CACHE = new HashMap<>();

            VoxelShape northShape = Shapes.empty();
            // Your exact exported Blockbench model math
            northShape = Shapes.join(northShape, Shapes.box(0.411765, 0.117646875, 0, 0.470588125, 0.882353125, 0.05882375), BooleanOp.OR);
            northShape = Shapes.join(northShape, Shapes.box(0.470588125, 0.117646875, 0, 0.529411875, 0.882353125, 0.05882375), BooleanOp.OR);
            northShape = Shapes.join(northShape, Shapes.box(0.529411875, 0.117646875, 0, 0.588235, 0.882353125, 0.05882375), BooleanOp.OR);
            northShape = Shapes.join(northShape, Shapes.box(0.35294125, 0.176470625, 0, 0.411765, 0.823529375, 0.05882375), BooleanOp.OR);
            northShape = Shapes.join(northShape, Shapes.box(0.588235, 0.176470625, 0, 0.64705875, 0.823529375, 0.05882375), BooleanOp.OR);
            northShape = Shapes.join(northShape, Shapes.box(0.2941175, 0.235294375, 0, 0.35294125, 0.764705625, 0.05882375), BooleanOp.OR);
            northShape = Shapes.join(northShape, Shapes.box(0.64705875, 0.235294375, 0, 0.7058825, 0.764705625, 0.05882375), BooleanOp.OR);
            northShape = Shapes.join(northShape, Shapes.box(0.235294375, 0.2941175, 0, 0.2941175, 0.7058825, 0.05882375), BooleanOp.OR);
            northShape = Shapes.join(northShape, Shapes.box(0.7058825, 0.2941175, 0, 0.764705625, 0.7058825, 0.05882375), BooleanOp.OR);
            northShape = Shapes.join(northShape, Shapes.box(0.176470625, 0.35294125, 0, 0.235294375, 0.64705875, 0.05882375), BooleanOp.OR);
            northShape = Shapes.join(northShape, Shapes.box(0.764705625, 0.35294125, 0, 0.823529375, 0.64705875, 0.05882375), BooleanOp.OR);
            northShape = Shapes.join(northShape, Shapes.box(0.117646875, 0.411765, 0, 0.176470625, 0.588235, 0.05882375), BooleanOp.OR);
            northShape = Shapes.join(northShape, Shapes.box(0.823529375, 0.411765, 0, 0.882353125, 0.588235, 0.05882375), BooleanOp.OR);

            // Assign the base shapes with SOUTH as the starting point
            SHAPES_CACHE.put(Direction.SOUTH, northShape);
            SHAPES_CACHE.put(Direction.NORTH, rotateShape(northShape, Direction.NORTH));
            SHAPES_CACHE.put(Direction.EAST,  rotateShape(northShape, Direction.EAST));
            SHAPES_CACHE.put(Direction.WEST,  rotateShape(northShape, Direction.WEST));
            SHAPES_CACHE.put(Direction.UP,    rotateShape(northShape, Direction.UP));
            SHAPES_CACHE.put(Direction.DOWN,  rotateShape(northShape, Direction.DOWN));

        }
        return SHAPES_CACHE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = Direction.NORTH;

        // Dynamically find which property your parent class used (6-way vs 4-way horizontal)
        if (state.hasProperty(BlockStateProperties.FACING)) {
            facing = state.getValue(BlockStateProperties.FACING);
        } else if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
            facing = state.getValue(HorizontalDirectionalBlock.FACING);
        }

        return getShapes().getOrDefault(facing, getShapes().get(Direction.NORTH));
    }

    // 1. REPLACED: We let the parent handle layout definitions completely to prevent duplicates
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        // Cleaned up: No extra builder.add() calls to avoid registry system errors
    }

    // --- REUSABLE ROTATION LOGIC ---
    private static class ShapeHolder {
        VoxelShape value = Shapes.empty();
    }

    private static VoxelShape rotateShape(VoxelShape source, Direction targetDirection) {
        // If the model was drawn facing South, SOUTH is our true base shape (no rotation needed)
        if (targetDirection == Direction.SOUTH) return source;

        final BasicProcessorUpgradeBlock.ShapeHolder holder = new BasicProcessorUpgradeBlock.ShapeHolder();
        source.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double rMinX = minX, rMinY = minY, rMinZ = minZ;
            double rMaxX = maxX, rMaxY = maxY, rMaxZ = maxZ;

            switch (targetDirection) {
                case NORTH -> {
                    // Flip the Z-axis for North since South is our base
                    rMinX = minX;       rMaxX = maxX;
                    rMinZ = 1.0 - maxZ; rMaxZ = 1.0 - minZ;
                }
                case EAST -> {
                    rMinX = minZ;       rMaxX = maxZ;
                    rMinZ = 1.0 - maxX; rMaxZ = 1.0 - minX;
                }
                case WEST -> {
                    rMinX = 1.0 - maxZ; rMaxX = 1.0 - minZ;
                    rMinZ = minX;       rMaxZ = maxX;
                }
                case UP -> {
                    rMinY = minZ;       rMaxY = maxZ;
                    rMinZ = 1.0 - maxY; rMaxZ = 1.0 - minY;
                }
                case DOWN -> {
                    rMinY = 1.0 - maxZ; rMaxY = 1.0 - minZ;
                    rMinZ = minY;       rMaxZ = maxY;
                }
                default -> {}
            }
            holder.value = Shapes.join(holder.value, Shapes.box(rMinX, rMinY, rMinZ, rMaxX, rMaxY, rMaxZ), BooleanOp.OR);
        });
        return holder.value;
    }
}