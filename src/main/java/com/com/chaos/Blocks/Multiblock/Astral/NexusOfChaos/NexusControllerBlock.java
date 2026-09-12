package com.com.chaos.Blocks.Multiblock.Astral.NexusOfChaos;

import com.com.chaos.Blocks.ModBlockEntities;
import com.com.chaos.Blocks.Multiblock.Abyssals.GaseousConverter.ChaosTurbineBlockEntity;
import com.com.chaos.Blocks.Multiblock.AstraAbyssals.Reactor.ChaosCrystalBlockEntity;
import com.com.chaos.Pressure.PressureManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class NexusControllerBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public NexusControllerBlock(Properties properties) {
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

    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NexusControllerBlockEntity(pos, state);
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
                ModBlockEntities.NEXUS_CONTROLLER.get(),
                NexusControllerBlockEntity::clientTick)
                : createTickerHelper(
                type,
                ModBlockEntities.NEXUS_CONTROLLER.get(),
                NexusControllerBlockEntity::serverTick);
    }

    @SuppressWarnings("unchecked")
    private static <T extends BlockEntity, E extends BlockEntity> BlockEntityTicker<T> createTickerHelper(
            BlockEntityType<T> type,
            BlockEntityType<E> expected,
            BlockEntityTicker<? super E> ticker) {
        return type == expected ? (BlockEntityTicker<T>) ticker : null;
    }

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
            if (be instanceof NexusControllerBlockEntity nexus) {
                nexus.requestRevalidate();
            }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            if (!(level.getBlockEntity(pos) instanceof NexusControllerBlockEntity nexus)) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide) {
                player.displayClientMessage(
                        Component.literal("Nexus Controller @ " + pos.toShortString() + " - ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(nexus.describeStatus()).withStyle(ChatFormatting.AQUA)),
                        false);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }
}
