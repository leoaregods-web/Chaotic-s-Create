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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ProcessorInscriberBlock extends Block implements EntityBlock, UpgradeableMachine{

    public ProcessorInscriberBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        // Return a clean codec mapping or null if relying on standard deferred registration
        return null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ProcessorInscriberBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // ---------------- FIX 1: Allow Right-Clicking with Items to Insert ----------------
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof ProcessorInscriberBlockEntity be) {
                // If player is holding an item, try to place it inside the machine
                if (!stack.isEmpty()) {
                    if (be.tryInsert(stack)) {
                            stack.shrink(1);
                        return ItemInteractionResult.SUCCESS;
                    }
                }
            }
        }
        return level.isClientSide() ? ItemInteractionResult.SUCCESS : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    // ---------------- FIX 2: Allow Empty Hand Right-Clicking to Extract ----------------
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof ProcessorInscriberBlockEntity be) {
                if (!be.isEmpty()) {
                    ItemStack extracted = be.extractItem();
                    if (!extracted.isEmpty()) {
                        if (!player.getInventory().add(extracted)) {
                            player.drop(extracted, false);
                        }
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return (lvl, pos, blockState, blockEntity) -> {
            if (blockEntity instanceof ProcessorInscriberBlockEntity processorInscriber) {
                ProcessorInscriberBlockEntity.tick(lvl, pos, blockState, processorInscriber);
            }
        };
    }

    // ---------------- FIX 3: Fixed Particle Checking logic ----------------
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Corrected to look for your actual ProcessorInscriberBlockEntity class
        if (!(level.getBlockEntity(pos) instanceof ProcessorInscriberBlockEntity inscriber)) {
            return;
        }
        if (!inscriber.isProcessing()) {
            return;
        }

        double x = pos.getX() + 0.5D;
        double z = pos.getZ() + 0.5D;

        level.addParticle(
                ParticleTypes.SMOKE,
                x + (random.nextDouble() - 0.5D) * 0.18D,
                pos.getY() + 0.88D,
                z + (random.nextDouble() - 0.5D) * 0.18D,
                0.0D, 0.02D + random.nextDouble() * 0.02D, 0.0D
        );

        if (random.nextBoolean()) {
            level.addParticle(
                    ParticleTypes.FLAME,
                    x + (random.nextDouble() - 0.5D) * 0.12D,
                    pos.getY() + 0.68D + random.nextDouble() * 0.08D,
                    z + (random.nextDouble() - 0.5D) * 0.12D,
                    0.0D, 0.0D, 0.0D
            );
        }

        if (random.nextInt(12) == 0) {
            level.playLocalSound(
                    x, pos.getY() + 0.5D, z,
                    SoundEvents.FURNACE_FIRE_CRACKLE,
                    SoundSource.BLOCKS,
                    0.25F, 1.0F, false
            );
        }
    }

    // ---------------- FIX 4: Let Ambient World Light Reach Inside the Machine ----------------
    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true; // Let sky light down so the horizontal item rendering has bright lighting colors
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter world, BlockPos pos) {
        return 0; // Ensures the interior space is bright instead of masked by pure black shadows
    }

    private static final VoxelShape SHAPE = Shapes.or(
            Shapes.box(0.0625, 0.0625, 0.0625, 0.9375, 0.125, 0.125),
            Shapes.box(0.9375, 0.0625, 0, 1, 0.5, 0.0625),
            Shapes.box(0, 0.0625, 0, 0.0625, 0.5, 0.0625),
            Shapes.box(0, 0.0625, 0.9375, 0.0625, 0.5, 1),
            Shapes.box(0.9375, 0.0625, 0.9375, 1, 0.5, 1),
            Shapes.box(0.0625, 0.0625, 0.875, 0.9375, 0.125, 0.9375),
            Shapes.box(0.0625, 0.0625, 0.125, 0.125, 0.125, 0.875),
            Shapes.box(0, 0, 0, 1, 0.0625, 1),
            Shapes.box(0, 0.5, 0, 1, 0.5625, 1),
            Shapes.box(0.0625, 0.4375, 0.0625, 0.9375, 0.5, 0.9375),
            Shapes.box(0.875, 0.0625, 0.125, 0.9375, 0.125, 0.875)
    );

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
