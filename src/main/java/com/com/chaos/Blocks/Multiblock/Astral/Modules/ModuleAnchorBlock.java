package com.com.chaos.Blocks.Multiblock.Astral.Modules;

import com.com.chaos.Blocks.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Universal anchor block for all Nexus modules.
 * Different module types (battery, processor, etc.) all use this same block as their anchor point.
 * The module type is determined by the multiblock pattern detected around this anchor.
 */
public class ModuleAnchorBlock extends BaseEntityBlock {

    public ModuleAnchorBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ModuleAnchorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type) {
        return level.isClientSide
                ? createTickerHelper(type, ModBlockEntities.MODULE_ANCHOR.get(), ModuleAnchorBlockEntity::clientTick)
                : createTickerHelper(type, ModBlockEntities.MODULE_ANCHOR.get(), ModuleAnchorBlockEntity::serverTick);
    }

    @SuppressWarnings("unchecked")
    private static <T extends BlockEntity, E extends BlockEntity> BlockEntityTicker<T> createTickerHelper(
            BlockEntityType<T> type,
            BlockEntityType<E> expected,
            BlockEntityTicker<? super E> ticker) {
        return type == expected ? (BlockEntityTicker<T>) ticker : null;
    }
}
