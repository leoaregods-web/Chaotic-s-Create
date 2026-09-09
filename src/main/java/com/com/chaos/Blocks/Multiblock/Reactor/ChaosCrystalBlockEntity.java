package com.com.chaos.Blocks.Multiblock.Reactor;

import com.com.chaos.Blocks.ModBlockEntities;
import com.com.chaos.ChaoticsCreate;
import com.com.chaos.ModDataComponents;
import com.simibubi.create.content.kinetics.RotationPropagator;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.List;
import java.util.Optional;

/**
 * Every Chaos Crystal - structural, input, or the output controller - runs
 * one of these. Each carries a 2-slot item buffer and a fluid tank so it
 * can accept insertion from a hopper/pipe/bucket (or a direct right-click,
 * see ChaosCrystalBlock) regardless of its eventual role; what changes is
 * how that buffer gets *used*:
 *
 *  - Structural (base cardinals): buffer sits unused. Easy extension point
 *    for later (e.g. a solid-intake feature) rather than wired to anything yet.
 *  - Input (middle sides): slot 0 / the fluid tank is read directly by the
 *    controller as one recipe input.
 *  - Output/controller (middle front, the anchor): both slots + the tank
 *    hold the reactor's current result, populated by processRecipes().
 *
 * Structure validity is only ever checked by scanning outward using *this*
 * block as the anchor - see ChaosReactorStructure. Side and structural
 * crystals never find a matching facing, so they never become controller.
 *
 * DEBUG_LOGGING: set true to get a play-by-play in the log of structure
 * detection and recipe matching for this block entity. Cheap enough to
 * leave on during testing; flip off (or gate behind a config) before
 *
 * shipping since it'll spam every second per active reactor.
 */
public class ChaosCrystalBlockEntity extends KineticBlockEntity {

    private float currentGeneratedSpeed = 0;
    private float currentGeneratedStress = 0;

    public static boolean DEBUG_LOGGING = true;

    private static final int REVALIDATE_INTERVAL_TICKS = 10;
    private static final int PROCESS_INTERVAL_TICKS = 20;
    public static final int TANK_CAPACITY_MB = 4000;

    private final ItemStackHandler itemBuffer = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final FluidTank fluidBuffer = new FluidTank(TANK_CAPACITY_MB) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    private boolean dirtyRevalidate = true;
    private int ticksUntilRevalidate = 0;
    private int processCooldown = 0;

    private boolean isController = false;
    private boolean structureValid = false;
    private Direction facing = Direction.NORTH;

    public ChaosCrystalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHAOS_CRYSTAL.get(), pos, state);
    }

    private void debug(String msg) {
        if (DEBUG_LOGGING) {
            ChaoticsCreate.LOGGER.info("[ChaosReactor @ {}] {}", worldPosition, msg);
        }
    }

    // ------------------------------------------------------------------
    // Structure detection
    // ------------------------------------------------------------------

    public void requestRevalidate() {
        dirtyRevalidate = true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ChaosCrystalBlockEntity be) {
        if (be.dirtyRevalidate || --be.ticksUntilRevalidate <= 0) {
            be.dirtyRevalidate = false;
            be.ticksUntilRevalidate = REVALIDATE_INTERVAL_TICKS;
            be.revalidate(level, pos);
        }

        if (be.isController && be.structureValid) {
            {
                be.processRecipes(level, pos); // Fallback to Reactor processing
            }
        }
    }


    public static void clientTick(Level level, BlockPos pos, BlockState state, ChaosCrystalBlockEntity be) {
        // The contents renderer reads isController()/isStructureValid()/getDisplayFluid()/
        // getDisplayItem() directly each frame - nothing needed here.
    }

    private void revalidate(Level level, BlockPos pos) {
        Direction foundReactor = ChaosReactorStructure.findValidFacing(level, pos);

        Direction found = foundReactor;
        boolean nowController = found != null;

        if (DEBUG_LOGGING && !nowController && !isController) {
            logFirstMismatch(level, pos, facing);
        }

        if (nowController != isController(found != null && found != facing)){
            isController = nowController;
            structureValid = nowController;

            if (found != null) {
                facing = found;
            }
            setChanged();
            level.sendBlockUpdated(pos, getBlockState(), getBlockState(), 3);

            if (nowController) {
                debug("Structure INVALID - no longer controller");
            }
        }
    }

    /** Debug helper: reports the first pattern cell that doesn't match, for the given facing guess. */
    private void logFirstMismatch(Level level, BlockPos anchorPos, Direction facingGuess) {
        for (ChaosReactorStructure.Cell cell : ChaosReactorStructure.debugCells()) {
            BlockPos target = ChaosReactorStructure.resolve(anchorPos, facingGuess, cell.right(), cell.back(), cell.up());
            if (!ChaosReactorStructure.matchesRoleDebug(level.getBlockState(target), cell.role())) {
                debug("First mismatch (facing guess " + facingGuess + "): expected " + cell.role()
                    + " at " + target + " but found " + level.getBlockState(target).getBlock());
                return;
            }
        }
    }

    // ------------------------------------------------------------------
    // Controller-only recipe processing
    // ------------------------------------------------------------------

    private void processRecipes(Level level, BlockPos pos) {
        if (--processCooldown > 0) return;
        processCooldown = PROCESS_INTERVAL_TICKS;

        ChaosCrystalBlockEntity left = crystalAt(level, pos, ChaosReactorStructure.INPUT_LEFT);
        ChaosCrystalBlockEntity right = crystalAt(level, pos, ChaosReactorStructure.INPUT_RIGHT);
        if (left == null || right == null) {
            debug("Skipping process tick - input crystal missing (left=" + (left != null) + ", right=" + (right != null) + ")");
            return;
        }

        ChaosReactorRecipeInput input = new ChaosReactorRecipeInput(
            left.itemBuffer.getStackInSlot(0), left.fluidBuffer.getFluid(),
            right.itemBuffer.getStackInSlot(0), right.fluidBuffer.getFluid());

        debug("Checking recipes with left=[" + describe(input.leftItem(), input.leftFluid())
            + "] right=[" + describe(input.rightItem(), input.rightFluid()) + "]");

        Optional<RecipeHolder<ChaosReactorRecipe>> found =
            level.getRecipeManager().getRecipeFor(ModChaosReactorRecipes.CHAOS_REACTOR_TYPE.get(), input, level);
        if (found.isEmpty()) {
            debug("No recipe matched current inputs");
            return;
        }

        ChaosReactorRecipe recipe = found.get().value();
        debug("Matched recipe " + found.get().id());
        // Plain item results and filled-capsule results both live in the same
        // 2-slot item buffer, so they have to be placed together - previously
        // only itemResults was ever written to the buffer and capsule results
        // (e.g. water splitting's two output capsules) were silently dropped.
        List<ItemStack> slotResults = new java.util.ArrayList<>(recipe.getItemResults());
        slotResults.addAll(recipe.getCapsuleResults());
        FluidStack fluidResult = recipe.getFluidResult();

        if (!canAcceptResults(slotResults, fluidResult)) {
            debug("Recipe matched but output buffer is full/blocked - waiting");
            return;
        }

        consumeSide(left, recipe.leftInput());
        consumeSide(right, recipe.rightInput());

        for (int i = 0; i < slotResults.size() && i < 2; i++) {
            ItemStack result = slotResults.get(i);
            ItemStack existing = itemBuffer.getStackInSlot(i);
            if (existing.isEmpty()) {
                itemBuffer.setStackInSlot(i, result.copy());
            } else {
                existing.grow(result.getCount());
            }
        }
        if (!fluidResult.isEmpty()) {
            fluidBuffer.fill(fluidResult.copy(), IFluidHandler.FluidAction.EXECUTE);
        }
        setChanged();
        debug("Produced results: " + slotResults + (fluidResult.isEmpty() ? "" : " + fluid " + fluidResult));
    }

    private static String describe(ItemStack item, FluidStack fluid) {
        return "item=" + (item.isEmpty() ? "none" : item) + ", fluid=" + (fluid.isEmpty() ? "none" : fluid);
    }

    /** slotResults holds both plain item results and filled-capsule results - they share the same 2 output slots. */
    private boolean canAcceptResults(List<ItemStack> slotResults, FluidStack fluidResult) {
        for (int i = 0; i < slotResults.size() && i < 2; i++) {
            ItemStack existing = itemBuffer.getStackInSlot(i);
            ItemStack result = slotResults.get(i);
            if (!existing.isEmpty()) {
                if (!ItemStack.isSameItemSameComponents(existing, result)) return false;
                if (existing.getCount() + result.getCount() > existing.getMaxStackSize()) return false;
            }
        }
        if (!fluidResult.isEmpty()) {
            FluidStack existingFluid = fluidBuffer.getFluid();
            if (!existingFluid.isEmpty() && existingFluid.getFluid() != fluidResult.getFluid()) return false;
            if (fluidBuffer.getSpace() < fluidResult.getAmount()) return false;
        }
        return true;
    }

    private void consumeSide(ChaosCrystalBlockEntity crystal, ReactorIngredient requirement) {
        if (requirement.isEmpty()) return;
        if (requirement.item().isPresent()) {
            crystal.itemBuffer.extractItem(0, requirement.itemCount(), false);
        } else if (requirement.fluid().isPresent()) {
            crystal.fluidBuffer.drain(requirement.fluid().get().getAmount(), IFluidHandler.FluidAction.EXECUTE);
        } else {
            // capsule_fluid: this used to fall into an `else` branch that assumed
            // fluid.isPresent() and called requirement.fluid().orElseThrow() - for
            // every capsule_fluid ingredient (e.g. synthesis's inputs) fluid is
            // always empty (capsuleFluid is the populated field instead), so this
            // threw NoSuchElementException the moment such a recipe ever matched.
            // Drain the required amount out of whatever capsule is actually sitting
            // in slot 0 - the same place matches() reads it from - leaving the
            // capsule item itself in place, just less full (or emptied, not removed).
            crystal.drainCapsuleSlot(requirement.capsuleFluid().orElseThrow().getAmount());
        }
    }

    /**
     * Drains `amount` out of whatever capsule (if any) is sitting in slot 0's own
     * GAS_CONTENT - used by consumeSide() to consume a capsule_fluid ingredient
     * without removing the capsule item itself.
     */
    public void drainCapsuleSlot(int amount) {
        ItemStack stack = itemBuffer.getStackInSlot(0);
        if (stack.isEmpty()) return;
        FluidStack contained = stack.getOrDefault(ModDataComponents.GAS_CONTENT.get(), SimpleFluidContent.EMPTY).copy();
        if (contained.isEmpty()) return;
        contained.shrink(amount);
        stack.set(ModDataComponents.GAS_CONTENT.get(),
            contained.isEmpty() ? SimpleFluidContent.EMPTY : SimpleFluidContent.copyOf(contained));
        setChanged();
    }

    private ChaosCrystalBlockEntity crystalAt(Level level, BlockPos anchorPos, ChaosReactorStructure.Cell cell) {
        BlockPos target = ChaosReactorStructure.resolve(anchorPos, facing, cell.right(), cell.back(), cell.up());
        return level.getBlockEntity(target) instanceof ChaosCrystalBlockEntity c ? c : null;
    }

    // ------------------------------------------------------------------
    // Manual insert/extract, used by ChaosCrystalBlock's right-click handling
    // ------------------------------------------------------------------

    /** Tries to insert the given stack into slot 0. Returns the remainder. */
    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        return itemBuffer.insertItem(0, stack, simulate);
    }

    /** Removes and returns whatever's in slot 0 (or slot 1 if 0 is empty), or ItemStack.EMPTY. */
    public ItemStack extractAnyItem(boolean simulate) {
        ItemStack fromZero = itemBuffer.extractItem(0, 64, simulate);
        if (!fromZero.isEmpty()) return fromZero;
        return itemBuffer.extractItem(1, 64, simulate);
    }

    public int fillFluid(FluidStack stack, boolean simulate) {
        return fluidBuffer.fill(stack, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
    }

    public FluidStack drainFluid(int amount, boolean simulate) {
        return fluidBuffer.drain(amount, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
    }

    /** Human-readable status line for the /right-click debug readout. */
    public String describeStatus() {
        return "controller=" + isController + " valid=" + structureValid + " facing=" + facing
            + " item0=" + itemBuffer.getStackInSlot(0) + " item1=" + itemBuffer.getStackInSlot(1)
            + " fluid=" + (fluidBuffer.getFluid().isEmpty() ? "none" : fluidBuffer.getFluid());
    }

    // ------------------------------------------------------------------
    // Accessors for capabilities and the contents renderer
    // ------------------------------------------------------------------

    public boolean isController(boolean b) {
        return isController;
    }

    public boolean isStructureValid() {
        return structureValid;
    }

    public Direction getFacing() {
        return facing;
    }

    public IItemHandler getItemHandler(Direction side) {
        return itemBuffer;
    }

    public IFluidHandler getFluidHandler(Direction side) {
        return fluidBuffer;
    }

    /** For the renderer: whichever slot has an item, preferring slot 0. */
    public ItemStack getDisplayItem() {
        ItemStack slot0 = itemBuffer.getStackInSlot(0);
        return !slot0.isEmpty() ? slot0 : itemBuffer.getStackInSlot(1);
    }

    public FluidStack getDisplayFluid() {
        return fluidBuffer.getFluid();
    }

    private void updateKineticOutput(Level level, float rpm, float capacity) {
        if (this.currentGeneratedSpeed != rpm || this.currentGeneratedStress != capacity) {
            this.currentGeneratedSpeed = rpm;
            this.currentGeneratedStress = capacity;
            setChanged();

            if (!level.isClientSide) {
                // Because 'this' is now natively recognized as a KineticBlockEntity,
                // these methods compile safely and instantly re-route torque lines.
                RotationPropagator.handleRemoved(level, this.worldPosition, this);
                if (rpm != 0) {
                    RotationPropagator.handleAdded(level, this.worldPosition, this);
                }
                // Notifies nearby gears and shafts to look at our block for network changes
                this.notifyUpdate();
            }
        }
    }

    // ------------------------------------------------------------------
    // Persistence
    // ------------------------------------------------------------------

    @Override
    public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        // Essential call to allow Create to save its internal components and kinetic data layers
        super.write(tag, registries, clientPacket);

        tag.put("ItemBuffer", itemBuffer.serializeNBT(registries));
        tag.put("FluidBuffer", fluidBuffer.writeToNBT(registries, new CompoundTag()));
        tag.putBoolean("IsController", isController);
        tag.putBoolean("StructureValid", structureValid);
        tag.putString("Facing", facing.getSerializedName());

        // Save your custom multiblock pressure and kinetic physics states
        tag.putFloat("CurrentGeneratedSpeed", currentGeneratedSpeed);
        tag.putFloat("CurrentGeneratedStress", currentGeneratedStress);
    }

    @Override
    public void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        // Essential call to let Create parse its structural configurations safely
        super.read(tag, registries, clientPacket);

        itemBuffer.deserializeNBT(registries, tag.getCompound("ItemBuffer"));
        fluidBuffer.readFromNBT(registries, tag.getCompound("FluidBuffer"));
        isController = tag.getBoolean("IsController");
        structureValid = tag.getBoolean("StructureValid");
        facing = Direction.byName(tag.getString("Facing"));
        if (facing == null) facing = Direction.NORTH;

        // Load your custom states
        currentGeneratedSpeed = tag.getFloat("CurrentGeneratedSpeed");
        currentGeneratedStress = tag.getFloat("CurrentGeneratedStress");
    }
}
