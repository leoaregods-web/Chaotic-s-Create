package com.com.chaos.Blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class AbyssalOreCrusherBlock extends Block implements EntityBlock, UpgradeableMachine {

    public AbyssalOreCrusherBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AbyssalOreCrusherBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof AbyssalOreCrusherBlockEntity be)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!stack.isEmpty() && be.tryInsert(stack)) {
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof AbyssalOreCrusherBlockEntity be)) {
            return InteractionResult.PASS;
        }

        if (be.isEmpty()) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;

        ItemStack extracted = be.extractItem();
        if (!player.getInventory().add(extracted)) {
            player.drop(extracted, false);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (lvl, pos, blockState, blockEntity) -> {
            if (blockEntity instanceof AbyssalOreCrusherBlockEntity be) {
                AbyssalOreCrusherBlockEntity.tick(lvl, pos, blockState, be);
            }
        };
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof AbyssalOreCrusherBlockEntity be) || !be.isProcessing()) {
            return;
        }
        double x = pos.getX() + 0.5D;
        double z = pos.getZ() + 0.5D;

        level.addParticle(ParticleTypes.CRIT,
                x + (random.nextDouble() - 0.5D) * 0.5D, pos.getY() + 0.7D, z + (random.nextDouble() - 0.5D) * 0.5D,
                0.0D, 0.02D, 0.0D);

        if (random.nextInt(10) == 0) {
            level.playLocalSound(x, pos.getY() + 0.5D, z, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 0.3F, 0.7F, false);
        }
    }
}