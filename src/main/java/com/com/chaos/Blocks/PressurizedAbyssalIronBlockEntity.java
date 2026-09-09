package com.com.chaos.Blocks;

import com.com.chaos.Pressure.PressureSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PressurizedAbyssalIronBlockEntity extends BlockEntity implements PressureSource {

    public PressurizedAbyssalIronBlockEntity(BlockPos pos, BlockState blockState) {
        this(ModBlockEntities.PRESSURIZED_ABYSSAL_IRON_BLOCK.get(), pos, blockState);
    }

    public PressurizedAbyssalIronBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public float getPressureContribution() {
        return 3.0f;
    }
}
