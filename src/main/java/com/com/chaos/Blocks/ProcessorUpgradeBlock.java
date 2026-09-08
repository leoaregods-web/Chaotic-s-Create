package com.com.chaos.Blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public abstract class ProcessorUpgradeBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private final int speedBonus;

    protected ProcessorUpgradeBlock(Properties properties, int speedBonus) {
        super(properties);
        this.speedBonus = speedBonus;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    public int getSpeedBonus() {
        return speedBonus;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        BlockPos supportPos = context.getClickedPos().relative(face.getOpposite());
        if (!canAttachTo(context.getLevel(), supportPos)) {
            return null;
        }
        return this.defaultBlockState().setValue(FACING, face);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        return canAttachTo(level, pos.relative(facing.getOpposite()));
    }

    private static boolean canAttachTo(BlockGetter level, BlockPos supportPos) {
        BlockState targetState = level.getBlockState(supportPos);

        if (targetState.getBlock() instanceof UpgradeableMachine) {
            return true;
        }

        String registryName = targetState.getBlock().builtInRegistryHolder().key().location().toString();
        return registryName.startsWith("replication:");
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}