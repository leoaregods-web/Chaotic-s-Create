package com.com.chaos.Blocks;

import com.com.chaos.Recipes.AbyssalCrushingRecipe;
import com.com.chaos.Recipes.ModRecipes;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.crusher.AbstractCrushingRecipe;
import com.simibubi.create.content.kinetics.crusher.CrushingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class AbyssalOreCrusherBlockEntity extends BlockEntity implements WorldlyContainer {
    private static final int[] SLOT_ACCESS = new int[]{0};
    private static final int FE_PER_TICK = 20;

    public final MachineEnergyStorage energyStorage = new MachineEnergyStorage(2000, 40);

    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private int progress = 0;

    // Cached recipe match for the item currently in the slot. Resolved on insert/change,
    // not re-queried every tick - a RecipeManager lookup is too expensive to run raw at 20/s.
    @Nullable
    private RecipeHolder<? extends AbstractCrushingRecipe> currentRecipe;

    private final IItemHandler upHandler = new SidedInvWrapper(this, Direction.UP);
    private final IItemHandler downHandler = new SidedInvWrapper(this, Direction.DOWN);
    private final IItemHandler northHandler = new SidedInvWrapper(this, Direction.NORTH);
    private final IItemHandler southHandler = new SidedInvWrapper(this, Direction.SOUTH);
    private final IItemHandler westHandler = new SidedInvWrapper(this, Direction.WEST);
    private final IItemHandler eastHandler = new SidedInvWrapper(this, Direction.EAST);
    private final IItemHandler nullSideHandler = new SidedInvWrapper(this, null);

    public AbyssalOreCrusherBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ABYSSAL_ORE_CRUSHER.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() { return energyStorage; }

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

    // --- Recipe resolution ---------------------------------------------------

    /**
     * Checks your custom AbyssalCrushingRecipe type first, falls back to native
     * create:crushing so any modded ore with a registered crushing recipe also works here.
     */
    private Optional<RecipeHolder<? extends AbstractCrushingRecipe>> findRecipe(ItemStack stack, Level level) {
        if (stack.isEmpty()) return Optional.empty();
        SingleRecipeInput wrapped = new SingleRecipeInput(stack);

        Optional<RecipeHolder<AbyssalCrushingRecipe>> custom = level.getRecipeManager()
                .getRecipeFor(ModRecipes.ABYSSALCRUSHING.<RecipeInput, AbyssalCrushingRecipe>getType(), wrapped, level);
        if (custom.isPresent()) {
            return Optional.of(custom.get());
        }

        return level.getRecipeManager()
                .getRecipeFor(AllRecipeTypes.CRUSHING.<RecipeInput, CrushingRecipe>getType(), wrapped, level)
                .map(holder -> holder);
    }

    private boolean hasRecipeFor(ItemStack stack) {
        if (stack.isEmpty() || level == null) return false;
        return findRecipe(stack, level).isPresent();
    }

    /** Re-resolves currentRecipe from whatever is currently in the slot. Call after any item change. */
    private void updateRecipe() {
        ItemStack stack = items.get(0);
        if (stack.isEmpty() || level == null) {
            currentRecipe = null;
            return;
        }
        currentRecipe = findRecipe(stack, level).orElse(null);
    }

    // --- Ticking ---------------------------------------------------------------

    public static void tick(Level level, BlockPos pos, BlockState state, AbyssalOreCrusherBlockEntity be) {
        if (level.isClientSide()) return;

        // Covers the case where the recipe cache wasn't populated yet (e.g. just after chunk load)
        if (be.currentRecipe == null && !be.items.get(0).isEmpty()) {
            be.updateRecipe();
        }

        if (be.currentRecipe == null) {
            if (be.progress != 0) {
                be.progress = 0;
                be.sync();
            }
            return;
        }

        int feCost = FE_PER_TICK;
        if (be.energyStorage.extractEnergy(feCost, true) != feCost) {
            return; // waiting on power, don't advance progress
        }
        be.energyStorage.extractEnergy(feCost, false);
        be.progress += MachineUpgrades.getInstalledSpeedBonus(level, pos);

        int processingTime = be.currentRecipe.value().getProcessingDuration();
        if (be.progress >= processingTime) {
            be.applyOutputs(level, pos);
            be.progress = 0;
            be.updateRecipe(); // slot contents changed (main result replaced input) - re-resolve
            be.sync();
        } else {
            be.setChanged();
        }
    }

    /**
     * Result 0 is treated as the guaranteed roll and replaces the slot in place (matches the old
     * single-slot in/out behaviour). Every other result is an independently-rolled bonus, boosted
     * by the installed upgrade modifier, dropped as a free item above the block.
     */
    private void applyOutputs(Level level, BlockPos pos) {
        List<ProcessingOutput> results = currentRecipe.value().getRollableResults();
        if (results.isEmpty()) return;

        double bonusChance = MachineUpgrades.getInstalledBonusChanceModifier(level, pos);

        items.set(0, results.get(0).getStack().copy());

        for (int i = 1; i < results.size(); i++) {
            ProcessingOutput output = results.get(i);
            float effectiveChance = (float) Math.min(1.0, output.getChance() + bonusChance);
            if (level.random.nextFloat() < effectiveChance) {
                ItemEntity bonusDrop = new ItemEntity(level,
                        pos.getX() + 0.5D, pos.getY() + 1.1D, pos.getZ() + 0.5D,
                        output.getStack().copy());
                bonusDrop.setDeltaMovement(0.0D, 0.15D, 0.0D);
                level.addFreshEntity(bonusDrop);
            }
        }
    }

    // --- Container / automation ---------------------------------------------------

    public boolean tryInsert(ItemStack stack) {
        if (!items.get(0).isEmpty()) return false;
        if (!hasRecipeFor(stack)) return false;
        ItemStack copy = stack.copy();
        copy.setCount(1);
        items.set(0, copy);
        progress = 0;
        updateRecipe();
        sync();
        return true;
    }

    public ItemStack extractItem() {
        ItemStack stack = items.get(0).copy();
        items.set(0, ItemStack.EMPTY);
        progress = 0;
        currentRecipe = null;
        sync();
        return stack;
    }

    public boolean isProcessing() { return currentRecipe != null; }
    public ItemStack getRenderStack() { return items.get(0); }
    public int getProgress() { return progress; }

    private boolean isAutomationOutput(ItemStack stack) {
        return !stack.isEmpty() && !hasRecipeFor(stack);
    }

    private void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override public int getContainerSize() { return 1; }
    @Override public boolean isEmpty() { return items.get(0).isEmpty(); }
    @Override public ItemStack getItem(int slot) { return slot == 0 ? items.get(0) : ItemStack.EMPTY; }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot != 0) return ItemStack.EMPTY;
        ItemStack stack = items.get(0);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack removed = stack.split(amount);
        if (stack.isEmpty()) {
            items.set(0, ItemStack.EMPTY);
            progress = 0;
            currentRecipe = null;
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
        currentRecipe = null;
        sync();
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot != 0) return;
        if (stack.isEmpty()) {
            items.set(0, ItemStack.EMPTY);
            progress = 0;
            currentRecipe = null;
        } else {
            ItemStack copy = stack.copy();
            copy.setCount(1);
            items.set(0, copy);
            updateRecipe();
            if (currentRecipe == null) progress = 0;
        }
        sync();
    }

    @Override
    public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) return false;
        return player.distanceToSqr(
                worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D
        ) <= 64.0D;
    }

    @Override
    public void clearContent() {
        items.set(0, ItemStack.EMPTY);
        progress = 0;
        currentRecipe = null;
        sync();
    }

    @Override public int getMaxStackSize() { return 1; }
    @Override public int[] getSlotsForFace(Direction side) { return SLOT_ACCESS; }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        if (slot != 0 || !items.get(0).isEmpty() || side == Direction.DOWN) return false;
        return hasRecipeFor(stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot == 0 && isAutomationOutput(stack);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == 0 && items.get(0).isEmpty() && hasRecipeFor(stack);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("Progress", progress);
        tag.put("Energy", energyStorage.serializeNBT());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, items, registries);
        progress = tag.getInt("Progress");
        if (tag.contains("Energy")) {
            energyStorage.deserializeNBT(tag.getCompound("Energy"));
        }
        // currentRecipe intentionally left null here - level isn't guaranteed attached yet;
        // tick() resolves it lazily on the first tick after load.
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