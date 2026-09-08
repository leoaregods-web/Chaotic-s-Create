package com.com.chaos.Blocks;

import com.com.chaos.Recipes.CapsuleConcentratorRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.Nullable;

public class CapsuleConcentratorBlockEntity extends BlockEntity implements WorldlyContainer {
    private static final int[] SLOT_ACCESS = new int[]{0};

    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private int progress = 0;

    // Side-aware handlers for pipes/funnels/etc.
    private final IItemHandler upHandler = new SidedInvWrapper(this, Direction.UP);
    private final IItemHandler downHandler = new SidedInvWrapper(this, Direction.DOWN);
    private final IItemHandler northHandler = new SidedInvWrapper(this, Direction.NORTH);
    private final IItemHandler southHandler = new SidedInvWrapper(this, Direction.SOUTH);
    private final IItemHandler westHandler = new SidedInvWrapper(this, Direction.WEST);
    private final IItemHandler eastHandler = new SidedInvWrapper(this, Direction.EAST);
    private final IItemHandler nullSideHandler = new SidedInvWrapper(this, null);

    public CapsuleConcentratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CAPSULE_CONCENTRATOR.get(), pos, state);
    }

    public @Nullable IItemHandler getItemHandler(@Nullable Direction side) {
        if (side == null) return nullSideHandler;

        return switch (side) {
            case UP -> upHandler;
            case DOWN -> downHandler;
            case NORTH -> northHandler;
            case SOUTH -> southHandler;
            case WEST -> westHandler;
            case EAST -> eastHandler;
        };
    }

    public static void tick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, CapsuleConcentratorBlockEntity be) {
        ItemStack stack = be.items.get(0);

        if (stack.isEmpty() || !CapsuleConcentratorRecipes.isValidInput(stack)) {
            if (be.progress != 0) {
                be.progress = 0;
                be.sync();
            }
            return;
        }

        be.progress += MachineUpgrades.getInstalledSpeedBonus(level, pos);

        if (be.progress >= CapsuleConcentratorRecipes.PROCESS_TIME) {
            ItemStack resultStack = CapsuleConcentratorRecipes.getResult(stack);
            if (resultStack != null) {
                be.items.set(0, resultStack);
                be.progress = 0;
                be.sync();
            }
        } else {
            be.setChanged();
        }
    }

    public boolean tryInsert(ItemStack stack) {
        if (!items.get(0).isEmpty()) return false;
        if (!CapsuleConcentratorRecipes.isValidInput(stack)) return false;

        ItemStack copy = stack.copy();
        copy.setCount(1);
        items.set(0, copy);
        progress = 0;
        sync();
        return true;
    }

    public ItemStack extractItem() {
        ItemStack stack = items.get(0).copy();
        items.set(0, ItemStack.EMPTY);
        progress = 0;
        sync();
        return stack;
    }

    public boolean isProcessing() {
        return CapsuleConcentratorRecipes.isValidInput(items.get(0));
    }

    public ItemStack getRenderStack() {
        return items.get(0);
    }

    public int getProgress() {
        return progress;
    }

    private boolean isAutomationOutput(ItemStack stack) {
        return !stack.isEmpty() && !CapsuleConcentratorRecipes.isValidInput(stack);
    }

    private void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // ---------------- Container / WorldlyContainer ----------------

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return items.get(0).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == 0 ? items.get(0) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot != 0) return ItemStack.EMPTY;

        ItemStack stack = items.get(0);
        if (stack.isEmpty()) return ItemStack.EMPTY;

        ItemStack removed = stack.split(amount);
        if (stack.isEmpty()) {
            items.set(0, ItemStack.EMPTY);
            progress = 0;
        }

        sync();
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot != 0) return ItemStack.EMPTY;

        ItemStack stack = items.get(0);
        items.set(0, ItemStack.EMPTY);
        progress = 0;
        sync();
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot != 0) return;

        if (stack.isEmpty()) {
            items.set(0, ItemStack.EMPTY);
            progress = 0;
        } else {
            ItemStack copy = stack.copy();
            copy.setCount(1);
            items.set(0, copy);

            if (!CapsuleConcentratorRecipes.isValidInput(copy)) {
                progress = 0;
            }
        }

        sync();
    }

    @Override
    public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) {
            return false;
        }

        return player.distanceToSqr(
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D
        ) <= 64.0D;
    }

    @Override
    public void clearContent() {
        items.set(0, ItemStack.EMPTY);
        progress = 0;
        sync();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return SLOT_ACCESS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        if (slot != 0) return false;
        if (!items.get(0).isEmpty()) return false;

        // allow insert from top or sides, not from bottom
        if (side == Direction.DOWN) return false;

        return CapsuleConcentratorRecipes.isValidInput(stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        if (slot != 0) return false;

        // only finished small capsules can be auto-extracted
        return isAutomationOutput(stack);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == 0 && items.get(0).isEmpty() && CapsuleConcentratorRecipes.isValidInput(stack);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("Progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, items, registries);
        progress = tag.getInt("Progress");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}