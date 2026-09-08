package com.com.chaos.Blocks;

import com.com.chaos.Recipes.ProcessorInscriberRecipes;
import com.com.chaos.Integration.JEI.ProcessorInscriberJeiRecipe;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.Nullable;

public class ProcessorInscriberBlockEntity extends BlockEntity implements WorldlyContainer {
    public final MachineEnergyStorage energyStorage = new MachineEnergyStorage(1000, 10);
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

    public ProcessorInscriberBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PROCESSOR_INSCRIBER.get(), pos, state);
    }

    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL; // Ensures the normal json model AND the BER both render
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

    public boolean tryInsert(ItemStack stack) {
        if (!items.get(0).isEmpty()) return false;
        if (!ProcessorInscriberRecipes.isValidInput(stack)) return false;
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
        return ProcessorInscriberRecipes.isValidInput(items.get(0));
    }

    public ItemStack getRenderStack() {
        return items.get(0);
    }

    public int getProgress() {
        return progress;
    }

    private boolean isAutomationOutput(ItemStack stack) {
        return !stack.isEmpty() && !ProcessorInscriberRecipes.isValidInput(stack);
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
            if (!ProcessorInscriberRecipes.isValidInput(copy)) {
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
        if (side == Direction.DOWN) return false;
        return ProcessorInscriberRecipes.isValidInput(stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        if (slot != 0) return false;
        return isAutomationOutput(stack);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == 0 && items.get(0).isEmpty() && ProcessorInscriberRecipes.isValidInput(stack);
    }

    public IEnergyStorage getEnergyStorage() {
        return this.energyStorage;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ProcessorInscriberBlockEntity be) {
        if (level.isClientSide()) return;

        boolean changed = false;
        ItemStack stack = be.items.get(0);

        ProcessorInscriberJeiRecipe activeRecipe = ProcessorInscriberRecipes.getRecipeFor(stack.getItem());

        if (activeRecipe == null) {
            if (be.progress != 0) {
                be.progress = 0;
                changed = true;
            }
            if (changed) {
                be.sync();
            }
            return;
        }

        int totalRecipeTime = activeRecipe.processTime();
        int totalRecipeFE = activeRecipe.requiredFE();
        int speed = MachineUpgrades.getInstalledSpeedBonus(level, pos);

        int nextProgress = Math.min(be.progress + speed, totalRecipeTime);
        int currentFeConsumedSoFar = (be.progress * totalRecipeFE) / totalRecipeTime;
        int nextFeConsumedTotal = (nextProgress * totalRecipeFE) / totalRecipeTime;
        int feCostThisTick = nextFeConsumedTotal - currentFeConsumedSoFar;

        boolean hasEnoughEnergy = false;
        IEnergyStorage energy = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, state, be, null);

        if (energy != null) {
            if (energy.extractEnergy(feCostThisTick, true) == feCostThisTick) {
                energy.extractEnergy(feCostThisTick, false);
                hasEnoughEnergy = true;
            }
        }

        if (hasEnoughEnergy) {
            be.progress = nextProgress;
            changed = true;

            if (be.progress >= totalRecipeTime) {
                be.items.set(0, activeRecipe.output().copy());
                be.progress = 0;
            }
        }

        if (changed) {
            be.sync();
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        // FIX: Extract energy first, then load the items/progress OUTSIDE the bracket block
        if (tag.contains("ProcessorInscriberEnergy")) {
            CompoundTag energyTag = tag.getCompound("ProcessorInscriberEnergy");
            this.energyStorage.deserializeNBT(registries, energyTag);
        }

        // This was previously stuck inside the above if-statement
        ContainerHelper.loadAllItems(tag, items, registries);
        this.progress = tag.getInt("Progress");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("Progress", progress);
        tag.put("ProcessorInscriberEnergy", this.energyStorage.serializeNBT(registries));
    }

    // ------------------- Network Syncing & Packet Fixes -------------------

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        // Pack the current item lists so the network chunk packet carries it to the client
        ContainerHelper.saveAllItems(tag, this.items, registries);
        tag.putInt("Progress", this.progress);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        // Creates the packet that tells the client "hey, parse this block entity data"
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        super.onDataPacket(net, pkt, registries);
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            // CRITICAL: Force the client-side block entity to unload its old EMPTY item stack
            // and load the real item data into its inventory list right away.
            ContainerHelper.loadAllItems(tag, this.items, registries);
            this.progress = tag.getInt("Progress");
        }
    }

}
