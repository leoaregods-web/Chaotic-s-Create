package com.com.chaos.Blocks.Multiblock.GaseousConverter;

import com.com.chaos.Blocks.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import org.jetbrains.annotations.Nullable;

/**
 * The turbine is a real Create kinetic block. Its rotor axis follows FACING,
 * while its controller/multiblock role is handled by the BlockEntity.
 */
public class ChaosTurbineBlock extends KineticBlock implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ChaosTurbineBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // The turbine faces the player; its rear points toward the structure's gearbox.
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == getRotationAxis(state);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChaosTurbineBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type) {
        return level.isClientSide
                ? createTickerHelper(
                        type,
                        ModBlockEntities.CHAOS_TURBINE.get(),
                        ChaosTurbineBlockEntity::clientTick)
                : createTickerHelper(
                        type,
                        ModBlockEntities.CHAOS_TURBINE.get(),
                        ChaosTurbineBlockEntity::serverTick);
    }

    @SuppressWarnings("unchecked")
    private static <T extends BlockEntity, E extends BlockEntity> BlockEntityTicker<T> createTickerHelper(
            BlockEntityType<T> type,
            BlockEntityType<E> expected,
            BlockEntityTicker<? super E> ticker) {
        return type == expected ? (BlockEntityTicker<T>) ticker : null;
    }

    /**
     * Tell a turbine controller to re-check its multiblock when a player places
     * another block directly around it. The periodic scan is still the fallback
     * for changes elsewhere in the 3x3x3 structure.
     */
    @Override
    public void neighborChanged(
            BlockState state,
            Level level,
            BlockPos pos,
            Block block,
            BlockPos neighborPos,
            boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, neighborPos, movedByPiston);
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ChaosTurbineBlockEntity turbine) {
                turbine.requestRevalidate();
            }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            if (!(level.getBlockEntity(pos) instanceof ChaosTurbineBlockEntity turbine)) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide) {
                player.displayClientMessage(
                        Component.literal("Chaos Turbine @ " + pos.toShortString() + " - ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(turbine.describeStatus()).withStyle(ChatFormatting.AQUA)),
                        false);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }
}
