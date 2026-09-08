package com.com.chaos.Blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class AstraAbyssGeneratorBlock extends BaseEntityBlock implements UpgradeableMachine {
    public AstraAbyssGeneratorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AstraAbyssGeneratorBlockEntity(pos, state);
    }

    // FIXED: This hooks the block entity's static tick loop into the world engine
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null; // Only tick on the server side

        return createTickerHelper(type, ModBlockEntities.ASTRA_ABYSS_GENERATOR.get(), AstraAbyssGeneratorBlockEntity::tick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // FIXED: Changes the default invisible state to standard model rendering
        return RenderShape.MODEL;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, net.minecraft.world.level.BlockGetter reader, net.minecraft.core.BlockPos pos) {
        return false;
    }

    @Override
    public int getLightBlock(BlockState state, net.minecraft.world.level.BlockGetter world, net.minecraft.core.BlockPos pos) {
        return world.getMaxLightLevel(); // Blocks light like a solid stone block
    }
}
