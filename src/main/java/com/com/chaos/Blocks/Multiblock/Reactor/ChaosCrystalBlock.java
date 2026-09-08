package com.com.chaos.Blocks.Multiblock.Reactor;

import com.com.chaos.Blocks.ModBlockEntities;
import com.com.chaos.Fluids.ModFluids;
import com.com.chaos.Items.GasCapsuleItem;
import com.com.chaos.ModDataComponents;
import com.com.chaos.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Replaces the old plain "chaos_crystals" block. Every placed crystal runs
 * the same block entity; which of the three roles (structural / input /
 * output-controller) it ends up playing is decided entirely by where it
 * sits relative to the rest of a valid Chaos Reactor structure - see
 * ChaosReactorStructure. Only the front-middle position will ever
 * successfully find a matching facing when it scans using itself as the
 * anchor, so only that block becomes the active controller.
 *
 * Interactions, for manual testing:
 *  - Right-click with an item: insert it (or fill/drain a bucket, or
 *    fill/drain a gas capsule - see below).
 *  - Right-click empty-handed: pull whatever's in the buffer out.
 *  - SHIFT + right-click empty-handed: print this crystal's debug status
 *    to chat (controller/valid/facing/buffer contents) instead of
 *    extracting - the quickest way to check what a given crystal thinks
 *    is going on without digging through the log.
 */
public class ChaosCrystalBlock extends Block implements EntityBlock {
    public ChaosCrystalBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChaosCrystalBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide
            ? createTickerHelper(type, ModBlockEntities.CHAOS_CRYSTAL.get(), ChaosCrystalBlockEntity::clientTick)
            : createTickerHelper(type, ModBlockEntities.CHAOS_CRYSTAL.get(), ChaosCrystalBlockEntity::serverTick);
    }

    @SuppressWarnings("unchecked")
    private static <T extends BlockEntity, E extends BlockEntity> BlockEntityTicker<T> createTickerHelper(
        BlockEntityType<T> type, BlockEntityType<E> expected,
        BlockEntityTicker<? super E> ticker) {
        return type == expected ? (BlockEntityTicker<T>) ticker : null;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (level.getBlockEntity(pos) instanceof ChaosCrystalBlockEntity crystal) {
            crystal.requestRevalidate();
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (level.getBlockEntity(pos) instanceof ChaosCrystalBlockEntity crystal) {
            crystal.requestRevalidate();
        }
    }

    // ------------------------------------------------------------------
    // Manual testing interaction
    // ------------------------------------------------------------------

    /**
     * Only these fluids can ever move into or out of a capsule through
     * crystal interaction - deliberately excludes deep_water/liquid_space
     * (and anything from another mod) even though the capsule's own
     * FluidHandlerItemStack capability (see ModCapabilities) has no such
     * restriction and would accept any fluid via pipes/hoppers/other mods.
     * This is the one and only gate that keeps manual capsule filling
     * "gases from my mod only".
     */
    private static boolean isAllowedGas(FluidStack stack) {
        return !stack.isEmpty() && stack.is(ModTags.GASES);
    }

    /**
     * Tries to fill a held capsule from the crystal's tank, if the tank
     * currently holds one of our own gases and the capsule has room for it
     * (and doesn't already hold a different fluid).
     *
     * Deliberately one-directional - there's no capsule->tank drain here.
     * That used to exist, but it actively broke things: recipes with a
     * "capsule_fluid" ingredient check the capsule ITEM sitting in the slot
     * (see ReactorIngredient.matches()), never the tank. So right-clicking
     * an already-filled capsule against an input crystal with an empty tank
     * (exactly what loading a capsule for a synthesis-style recipe looks
     * like) would drain the capsule's gas straight into the tank instead of
     * placing the capsule in the slot - the capsule then sits there empty,
     * and its (now discarded) gas is somewhere the recipe never checks.
     *
     * Returns CONSUME if a transfer happened, PASS_TO_DEFAULT_BLOCK_INTERACTION
     * otherwise (tank empty/foreign fluid, capsule already full) - callers
     * should fall through to normal item-insert handling in that case, which
     * is exactly what lets a filled capsule be placed into a slot untouched.
     */
    private static ItemInteractionResult tryFillCapsuleFromTank(ChaosCrystalBlockEntity crystal, Level level, BlockPos pos,
                                                                ItemStack heldItem, GasCapsuleItem capsuleItem) {
        if (heldItem.getCount() != 1) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        FluidStack tankFluid = crystal.getDisplayFluid();
        FluidStack contained = heldItem.getOrDefault(ModDataComponents.GAS_CONTENT.get(), SimpleFluidContent.EMPTY).copy();

        if (tankFluid.isEmpty()) {
            com.mojang.logging.LogUtils.getLogger().info("[CapsuleFill] rejected: tank is empty");
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!isAllowedGas(tankFluid)) {
            com.mojang.logging.LogUtils.getLogger().info("[CapsuleFill] rejected: {} is not in ModTags.GASES",
                    net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(tankFluid.getFluid()));
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!contained.isEmpty() && contained.getFluid() != tankFluid.getFluid()) {
            com.mojang.logging.LogUtils.getLogger().info("[CapsuleFill] rejected: capsule already holds {}, tank has {}",
                    net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(contained.getFluid()),
                    net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(tankFluid.getFluid()));
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        int space = capsuleItem.getCapacityMb() - contained.getAmount();
        if (space <= 0) {
            com.mojang.logging.LogUtils.getLogger().info("[CapsuleFill] rejected: capsule full ({} / {})",
                    contained.getAmount(), capsuleItem.getCapacityMb());
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        FluidStack drained = crystal.drainFluid(Math.min(space, tankFluid.getAmount()), false);
        if (drained.isEmpty()) {
            com.mojang.logging.LogUtils.getLogger().info("[CapsuleFill] rejected: drainFluid returned empty");
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        FluidStack newContent = contained.isEmpty() ? drained.copy() : contained.copy();
        if (!contained.isEmpty()) {
            newContent.grow(drained.getAmount());
        }
        heldItem.set(ModDataComponents.GAS_CONTENT.get(), SimpleFluidContent.copyOf(newContent));
        level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1f, 1.4f);
        return ItemInteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ChaosCrystalBlockEntity crystal)) return InteractionResult.PASS;

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                    Component.literal("Chaos Crystal @ " + pos.toShortString() + " - ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(crystal.describeStatus()).withStyle(ChatFormatting.AQUA)),
                    false);
            }
            return InteractionResult.SUCCESS;
        }

        if (level.isClientSide) return InteractionResult.SUCCESS;

        ItemStack extracted = crystal.extractAnyItem(false);
        if (!extracted.isEmpty()) {
            player.getInventory().placeItemBackInInventory(extracted);
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.6f, 1.4f);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }
}
