package com.com.chaos.Blocks;

import com.com.chaos.ChaoticsCreate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import com.com.chaos.Recipes.CapsuleConcentratorRecipes;

public class CapsuleConcentratorBlock extends Block implements EntityBlock, UpgradeableMachine {
    public CapsuleConcentratorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CapsuleConcentratorBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof CapsuleConcentratorBlockEntity concentrator)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!CapsuleConcentratorRecipes.isValidInput(stack) || !concentrator.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        ItemStack single = stack.copy();
        single.setCount(1);

        if (concentrator.tryInsert(single)) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof CapsuleConcentratorBlockEntity concentrator)) {
            return InteractionResult.PASS;
        }

        if (concentrator.isEmpty()) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack extracted = concentrator.extractItem();
        if (!player.getInventory().add(extracted)) {
            player.drop(extracted, false);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }

        return (lvl, pos, blockState, blockEntity) -> {
            if (blockEntity instanceof CapsuleConcentratorBlockEntity concentrator) {
                CapsuleConcentratorBlockEntity.tick(lvl, pos, blockState, concentrator);
            }
        };
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof CapsuleConcentratorBlockEntity concentrator)) {
            return;
        }

        if (!concentrator.isProcessing()) {
            return;
        }

        double x = pos.getX() + 0.5D;
        double z = pos.getZ() + 0.5D;

        level.addParticle(
                ParticleTypes.SMOKE,
                x + (random.nextDouble() - 0.5D) * 0.18D,
                pos.getY() + 0.88D,
                z + (random.nextDouble() - 0.5D) * 0.18D,
                0.0D,
                0.02D + random.nextDouble() * 0.02D,
                0.0D
        );

        if (random.nextBoolean()) {
            level.addParticle(
                    ParticleTypes.FLAME,
                    x + (random.nextDouble() - 0.5D) * 0.12D,
                    pos.getY() + 0.68D + random.nextDouble() * 0.08D,
                    z + (random.nextDouble() - 0.5D) * 0.12D,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }

        if (random.nextInt(12) == 0) {
            level.playLocalSound(
                    x,
                    pos.getY() + 0.5D,
                    z,
                    SoundEvents.FURNACE_FIRE_CRACKLE,
                    SoundSource.BLOCKS,
                    0.25F,
                    1.0F,
                    false
            );
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof CapsuleConcentratorBlockEntity concentrator) {
                ItemStack stack = concentrator.removeItemNoUpdate(0);
                if (!stack.isEmpty()) {
                    Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack);
                }
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}