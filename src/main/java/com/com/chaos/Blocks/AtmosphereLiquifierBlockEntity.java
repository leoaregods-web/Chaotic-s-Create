package com.com.chaos.Blocks;

import com.com.chaos.ChaoticsCreate;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import com.com.chaos.Fluids.ModFluids;
import com.com.chaos.Items.ModItems;
import com.com.chaos.Menu.AtmosphereLiquifierMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

public class AtmosphereLiquifierBlockEntity extends BlockEntity implements MenuProvider, Container {
    private static final String OXYGEN_TANK_TAG = "LiquidOxygenTank";
    private static final String NITROGEN_TANK_TAG = "LiquidNitrogenTank";
    private static final String ARGON_TANK_TAG = "LiquidArgonTank";
    private static final String PROGRESS_TAG = "CollectionProgress";

    public static final int OXYGEN_INPUT_SLOT = 0;
    public static final int NITROGEN_INPUT_SLOT = 1;
    public static final int ARGON_INPUT_SLOT = 2;
    public static final int OXYGEN_OUTPUT_SLOT = 3;
    public static final int NITROGEN_OUTPUT_SLOT = 4;
    public static final int ARGON_OUTPUT_SLOT = 5;
    public static final int SLOT_COUNT = 6;

    private static final int COLLECTION_INTERVAL_TICKS = 100;
    private static final int COLLECTION_AMOUNT_MB = 25;
    private static final int TANK_CAPACITY_MB = 4000;
    private static final int CAPSULE_FILL_COST_MB = 1000;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    private final FluidTank oxygenTank = createSingleFluidTank(ModFluids.LIQUID_OXYGEN_SOURCE::get);
    private final FluidTank nitrogenTank = createSingleFluidTank(ModFluids.LIQUID_NITROGEN_SOURCE::get);
    private final FluidTank argonTank = createSingleFluidTank(ModFluids.LIQUID_ARGON_SOURCE::get);

    private final IFluidHandler fluidHandler = new LiquifierFluidHandler();

    private int collectionProgress;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> oxygenTank.getFluidAmount();
                case 1 -> nitrogenTank.getFluidAmount();
                case 2 -> argonTank.getFluidAmount();
                case 3 -> TANK_CAPACITY_MB;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return AtmosphereLiquifierMenu.DATA_COUNT;
        }
    };

    public AtmosphereLiquifierBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ATMOSPHERE_LIQUIFIER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AtmosphereLiquifierBlockEntity blockEntity) {
        if (level.isClientSide) {
            return;
        }

        boolean changed = false;

        if (blockEntity.hasAirAccess(level, pos) && blockEntity.hasStorageSpace()) {
            blockEntity.collectionProgress += MachineUpgrades.getInstalledSpeedBonus(level, pos);
            if (blockEntity.collectionProgress >= COLLECTION_INTERVAL_TICKS) {
                blockEntity.collectionProgress = 0;
                changed |= blockEntity.collectRandomAtmosphericFluid(level.random);
            }
        } else if (blockEntity.collectionProgress != 0) {
            blockEntity.collectionProgress = 0;
            changed = true;
        }

        changed |= blockEntity.processCapsules();

        if (changed) {
            blockEntity.setChanged();
        }
    }

    private FluidTank createSingleFluidTank(java.util.function.Supplier<Fluid> allowedFluid) {
        return new FluidTank(TANK_CAPACITY_MB) {
            @Override
            public boolean isFluidValid(FluidStack stack) {
                return !stack.isEmpty() && stack.getFluid() == allowedFluid.get();
            }

            @Override
            protected void onContentsChanged() {
                setChanged();
            }
        };
    }

    private boolean processCapsules() {
        boolean changed = false;
        changed |= tryFillCapsule(OXYGEN_INPUT_SLOT, OXYGEN_OUTPUT_SLOT, oxygenTank, ModFluids.LIQUID_OXYGEN_SOURCE.get());
        changed |= tryFillCapsule(NITROGEN_INPUT_SLOT, NITROGEN_OUTPUT_SLOT, nitrogenTank, ModFluids.LIQUID_NITROGEN_SOURCE.get());
        changed |= tryFillCapsule(ARGON_INPUT_SLOT, ARGON_OUTPUT_SLOT, argonTank, ModFluids.LIQUID_ARGON_SOURCE.get());
        return changed;
    }

    private boolean tryFillCapsule(int inputSlot, int outputSlot, FluidTank tank, Fluid fluid) {
        ItemStack inputStack = items.get(inputSlot);
        if (!inputStack.is(ModItems.EMPTY_CAPSULE.get())) {
            return false;
        }
        if (tank.getFluidAmount() < CAPSULE_FILL_COST_MB) {
            return false;
        }

        ItemStack filledCapsule = new ItemStack(ModItems.CAPSULE.get());
        IFluidHandlerItem filledHandler = filledCapsule.getCapability(Capabilities.FluidHandler.ITEM);
        if (filledHandler == null) {
            return false;
        }
        filledHandler.fill(new FluidStack(fluid, CAPSULE_FILL_COST_MB), IFluidHandler.FluidAction.EXECUTE);
        filledCapsule = filledHandler.getContainer();

        ItemStack outputStack = items.get(outputSlot);
        if (!outputStack.isEmpty() && (!ItemStack.isSameItemSameComponents(outputStack, filledCapsule)
                || outputStack.getCount() >= outputStack.getMaxStackSize())) {
            return false;
        }

        tank.drain(CAPSULE_FILL_COST_MB, IFluidHandler.FluidAction.EXECUTE);
        inputStack.shrink(1);
        if (inputStack.isEmpty()) {
            items.set(inputSlot, ItemStack.EMPTY);
        }

        if (outputStack.isEmpty()) {
            items.set(outputSlot, filledCapsule);
        } else {
            outputStack.grow(1);
        }

        return true;
    }

    private boolean hasAirAccess(Level level, BlockPos pos) {
        if (!level.getFluidState(pos.above()).isEmpty()) {
            return false;
        }

        for (Direction direction : Direction.values()) {
            BlockPos checkPos = pos.relative(direction);
            if (level.getBlockState(checkPos).isAir() && level.getFluidState(checkPos).isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private boolean collectRandomAtmosphericFluid(RandomSource random) {
        int roll = random.nextInt(1000);
        if (roll < 780) {
            return fillTank(nitrogenTank, ModFluids.LIQUID_NITROGEN_SOURCE.get(), 25)
                    || fillTank(oxygenTank, ModFluids.LIQUID_OXYGEN_SOURCE.get(), 25)
                    || fillTank(argonTank, ModFluids.LIQUID_ARGON_SOURCE.get(), 25);
        }
        if (roll < 990) {
            return fillTank(oxygenTank, ModFluids.LIQUID_OXYGEN_SOURCE.get(), 25)
                    || fillTank(nitrogenTank, ModFluids.LIQUID_NITROGEN_SOURCE.get(), 25)
                    || fillTank(argonTank, ModFluids.LIQUID_ARGON_SOURCE.get(), 25);
        }
        return fillTank(argonTank, ModFluids.LIQUID_ARGON_SOURCE.get(), 25)
                || fillTank(nitrogenTank, ModFluids.LIQUID_NITROGEN_SOURCE.get(), 25)
                || fillTank(oxygenTank, ModFluids.LIQUID_OXYGEN_SOURCE.get(), 25);
    }

    private boolean fillTank(FluidTank tank, Fluid fluid, int amount) {
        return tank.fill(new FluidStack(fluid, amount), IFluidHandler.FluidAction.EXECUTE) > 0;
    }

    public ContainerData getContainerData() {
        return data;
    }

    public boolean hasStorageSpace() {
        return oxygenTank.getFluidAmount() < oxygenTank.getCapacity()
                || nitrogenTank.getFluidAmount() < nitrogenTank.getCapacity()
                || argonTank.getFluidAmount() < argonTank.getCapacity();
    }

    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        return fluidHandler;
    }

    public int getOxygenStored() {
        return oxygenTank.getFluidAmount();
    }

    public int getNitrogenStored() {
        return nitrogenTank.getFluidAmount();
    }

    public int getArgonStored() {
        return argonTank.getFluidAmount();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block." + ChaoticsCreate.MODID + ".atmosphere_liquifier");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new AtmosphereLiquifierMenu(containerId, playerInventory, this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        collectionProgress = tag.getInt(PROGRESS_TAG);
        ContainerHelper.loadAllItems(tag, items, registries);
        oxygenTank.readFromNBT(registries, tag.getCompound(OXYGEN_TANK_TAG));
        nitrogenTank.readFromNBT(registries, tag.getCompound(NITROGEN_TANK_TAG));
        argonTank.readFromNBT(registries, tag.getCompound(ARGON_TANK_TAG));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt(PROGRESS_TAG, collectionProgress);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.put(OXYGEN_TANK_TAG, oxygenTank.writeToNBT(registries, new CompoundTag()));
        tag.put(NITROGEN_TANK_TAG, nitrogenTank.writeToNBT(registries, new CompoundTag()));
        tag.put(ARGON_TANK_TAG, argonTank.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
        if (!removed.isEmpty()) {
            setChanged();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        setChanged();
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
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot >= OXYGEN_INPUT_SLOT && slot <= ARGON_INPUT_SLOT && stack.is(ModItems.EMPTY_CAPSULE.get());
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }

    private class LiquifierFluidHandler implements IFluidHandler {
        private final FluidTank[] tanks = new FluidTank[]{oxygenTank, nitrogenTank, argonTank};

        @Override
        public int getTanks() {
            return tanks.length;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return tank >= 0 && tank < tanks.length ? tanks[tank].getFluid() : FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank >= 0 && tank < tanks.length ? tanks[tank].getCapacity() : 0;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return tank >= 0 && tank < tanks.length && tanks[tank].isFluidValid(stack);
        }

        @Override
        public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
            if (resource.isEmpty()) {
                return 0;
            }
            for (FluidTank tank : this.tanks) {
                if (tank.isFluidValid(resource)) {
                    return tank.fill(resource, action);
                }
            }
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) {
                return FluidStack.EMPTY;
            }

            for (FluidTank tank : tanks) {
                if (!tank.isEmpty() && tank.getFluid().getFluid() == resource.getFluid()) {
                    return tank.drain(resource, action);
                }
            }

            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (maxDrain <= 0) {
                return FluidStack.EMPTY;
            }

            FluidTank fullestTank = null;
            for (FluidTank tank : tanks) {
                if (tank.isEmpty()) {
                    continue;
                }
                if (fullestTank == null || tank.getFluidAmount() > fullestTank.getFluidAmount()) {
                    fullestTank = tank;
                }
            }

            if (fullestTank == null) {
                return FluidStack.EMPTY;
            }

            return fullestTank.drain(maxDrain, action);
        }
    }
}
