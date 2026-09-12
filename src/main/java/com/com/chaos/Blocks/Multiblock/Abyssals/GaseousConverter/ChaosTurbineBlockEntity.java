package com.com.chaos.Blocks.Multiblock.Abyssals.GaseousConverter;

import com.buuz135.replication.api.matter_fluid.MatterStack;
import com.buuz135.replication.api.matter_fluid.MatterTank;
import com.com.chaos.Blocks.ModBlockEntities;
import com.com.chaos.Blocks.Multiblock.MultiblockBuilder;
import com.com.chaos.Matter.ModMatterTypes;
import com.com.chaos.ModTags;
import com.com.chaos.Pressure.PressureAware;
import com.com.chaos.Pressure.PressureManager;
import com.com.chaos.Pressure.PressureRatio;
import com.mojang.logging.LogUtils;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.slf4j.Logger;

/**
 * Controller BE for the 3x3x3 gaseous-converter structure.
 *
 * The important kinetic change is that this now uses Create's
 * GeneratingKineticBlockEntity lifecycle instead of manually poking the
 * RotationPropagator. That lets Create own source/network creation and
 * teardown while we only provide the generated speed through getGeneratedSpeed().
 */
public class ChaosTurbineBlockEntity extends GeneratingKineticBlockEntity implements PressureAware {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int REVALIDATE_INTERVAL_TICKS = 10;
    private static final int PROCESS_INTERVAL_TICKS = 20;

    public static final int TANK_CAPACITY_MB = 4000;
    public static final double MATTER_TANK_CAPACITY = 4000;

    // Balance knobs.
    private static final float SPEED_PER_PRESSURE = 1024f;
private static final float STRESS_PER_PRESSURE = SPEED_PER_PRESSURE / 2;
    private static final double GAS_MB_PER_MATTER = 10.0;
    private static final double MATTER_PER_TICK = 1.0;

    private final FluidTank gasBuffer = new FluidTank(
            TANK_CAPACITY_MB,
            fluid -> fluid.is(ModTags.GASES)) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    private final MatterTank matterBuffer = new MatterTank(
            (int) MATTER_TANK_CAPACITY,
            stack -> stack.getMatterType() == ModMatterTypes.GASEOUS.get());

    private boolean isController;
    private boolean structureValid;
    private Direction facing = Direction.NORTH;

    private boolean dirtyRevalidate = true;
    private int ticksUntilRevalidate;
    private int processCooldown;

    private float currentGeneratedSpeed;
    private float currentGeneratedStress;
    private float currentPressureRatio = PressureRatio.NORMAL;

    /** Last structure signature logged, so a broken structure does not spam the log every 10 ticks. */
    private String lastStructureLog = "";

    public ChaosTurbineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHAOS_TURBINE.get(), pos, state);
    }

    public void requestRevalidate() {
        dirtyRevalidate = true;
        ticksUntilRevalidate = 0;
    }

    public boolean isController() {
        return isController;
    }

    public boolean isStructureValid() {
        return structureValid;
    }

    public Direction getFacing() {
        return facing;
    }

    @Override
    public void onPressureUpdated(float pressureRatio) {
        currentPressureRatio = pressureRatio;
    }

    public float getPressureRatio() {
        return currentPressureRatio;
    }

    @Override
    public float minOperatingPressure() {
        return PressureRatio.VACUUM_THRESHOLD;
    }

    @Override
    public float maxOperatingPressure() {
        return PressureRatio.CRUSHING_THRESHOLD;
    }

    public static void serverTick(
            Level level,
            BlockPos pos,
            BlockState state,
            ChaosTurbineBlockEntity be) {

        if (be.dirtyRevalidate || --be.ticksUntilRevalidate <= 0) {
            be.dirtyRevalidate = false;
            be.ticksUntilRevalidate = REVALIDATE_INTERVAL_TICKS;
            be.revalidate(level, pos);
        }

        if (be.isController && be.structureValid) {
            be.onPressureUpdated(PressureManager.get(level).getPressureRatio(level, pos));
            be.process(level, pos);
        }
    }

    /**
     * The block is entirely server-driven today. Keeping this method present
     * gives the block a client ticker without doing redundant client work.
     */
    public static void clientTick(
            Level level,
            BlockPos pos,
            BlockState state,
            ChaosTurbineBlockEntity be) {
        // Rendering reads Create's kinetic speed directly.
    }

    private void revalidate(Level level, BlockPos pos) {
        GaseousConverterStructure.MatchResult match =
                GaseousConverterStructure.findValidFacingResult(level, pos);

        boolean nowController = match != null;
        boolean wasValid = structureValid;

        isController = nowController;

        if (nowController) {
            structureValid = true;
            facing = match.facing();
        } else {
            // Not an anchor myself — am I the rear turbine of someone else's valid structure?
            structureValid = GaseousConverterStructure.findControllerFromRearCell(level, pos) != null;
        }

        if (wasValid != structureValid) {
            currentGeneratedSpeed = 0;
            currentGeneratedStress = 0;
            setChanged();
            level.sendBlockUpdated(pos, getBlockState(), getBlockState(), 3);
            updateGeneratedRotation();
        }
    }

    private void process(Level level, BlockPos pos) {
        if (--processCooldown > 0) {
            return;
        }
        processCooldown = PROCESS_INTERVAL_TICKS;

        // In current Create, hasSource() means this BE is receiving kinetic
        // power from another KineticBlockEntity. getSpeed() is the actual
        // propagated speed after overstress/freeze rules are applied.
        boolean externallyDriven = hasSource() && getSpeed() != 0;

        if (externallyDriven) {
            // We are a consumer in this mode, not a second source.
            setKineticOutput(0, 0);
            generateMatter(level, pos);
        } else {
            generateRotation(level, pos);
        }
    }

    private void generateRotation(Level level, BlockPos pos) {
        if (gasAvailableAcrossCells(level, pos) <= 0 || !isWithinOperatingRange(currentPressureRatio)) {
            setKineticOutput(0, 0);
            return;
        }
        setKineticOutput(currentPressureRatio * SPEED_PER_PRESSURE, currentPressureRatio * STRESS_PER_PRESSURE);
    }

    private void generateMatter(Level level, BlockPos pos) {
        if (gasAvailableAcrossCells(level, pos) < GAS_MB_PER_MATTER
                || matterBuffer.getSpace() < MATTER_PER_TICK) {
            return;
        }
        drainAcrossCells(level, pos, (int) GAS_MB_PER_MATTER);
        matterBuffer.fill(new MatterStack(ModMatterTypes.GASEOUS.get(), MATTER_PER_TICK),
                IFluidHandler.FluidAction.EXECUTE);
        setChanged();
    }

    private void setKineticOutput(float speed, float capacity) {
        if (currentGeneratedSpeed == speed && currentGeneratedStress == capacity) {
            return;
        }

        currentGeneratedSpeed = speed;
        currentGeneratedStress = capacity;
        setChanged();

        // Create's GeneratingKineticBlockEntity applies source/network
        // transitions safely here. Do not call RotationPropagator directly.
        updateGeneratedRotation();
    }

    @Override
    public float getGeneratedSpeed() {
        return structureValid ? currentGeneratedSpeed : 0;
    }

    @Override
    public float calculateAddedStressCapacity() {
        return structureValid ? currentGeneratedStress : 0;
    }

    public String describeStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("controller=").append(isController);
        sb.append(", structureValid=").append(structureValid);
        sb.append(", facing=").append(facing);

        if (isController && structureValid) {
            sb.append(", pressure=").append(currentPressureRatio)
                    .append(" (").append(PressureRatio.tierOf(currentPressureRatio)).append(")");
            sb.append(", speed=").append(currentGeneratedSpeed);
            sb.append(", stress=").append(currentGeneratedStress);
            sb.append(", hasSource=").append(hasSource());
            sb.append(", networkSpeed=").append(getSpeed());
            sb.append(", gas=").append(gasBuffer.getFluidAmount())
                    .append("/").append(gasBuffer.getCapacity()).append("mb");
            sb.append(", matterSpaceLeft=").append(matterBuffer.getSpace());
        } else if (level != null) {
            sb.append(" - ").append(GaseousConverterStructure.debugFacingReport(level, getBlockPos()));
        }

        String status = sb.toString();
        // Always log this - it's only ever triggered by an explicit shift-click,
        // so there's no spam risk, and it means the report can be copy-pasted
        // straight out of latest.log instead of off the chat screen.
        LOGGER.info("[ChaosTurbine] Status @ {}: {}", getBlockPos(), status);
        return status;
    }

    @Override
    public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("GasBuffer", gasBuffer.writeToNBT(registries, new CompoundTag()));
        tag.put("MatterBuffer", matterBuffer.serializeNBT(registries));
        tag.putBoolean("IsController", isController);
        tag.putBoolean("StructureValid", structureValid);
        tag.putString("Facing", facing.getSerializedName());
        tag.putFloat("CurrentGeneratedSpeed", currentGeneratedSpeed);
        tag.putFloat("CurrentGeneratedStress", currentGeneratedStress);
    }

    public IFluidHandler getFluidHandler(Direction side) {
        return gasBuffer;
    }

    private ChaosTurbineBlockEntity rearTurbineBE(Level level, BlockPos anchorPos) {
        BlockPos pos = MultiblockBuilder.resolve(anchorPos, facing, 0, 2, 0); // TURBINE cell per MultiblockPattern
        return level.getBlockEntity(pos) instanceof ChaosTurbineBlockEntity be && be != this ? be : null;
    }

    /** Drains up to `amount` mb, preferring this cell's own tank, then the rear turbine's. */
    private FluidStack drainAcrossCells(Level level, BlockPos pos, int amount) {
        FluidStack drained = gasBuffer.drain(amount, IFluidHandler.FluidAction.EXECUTE);
        int remaining = amount - drained.getAmount();
        if (remaining > 0) {
            ChaosTurbineBlockEntity rear = rearTurbineBE(level, pos);
            if (rear != null) {
                FluidStack fromRear = rear.gasBuffer.drain(remaining, IFluidHandler.FluidAction.EXECUTE);
                if (!fromRear.isEmpty()) {
                    drained = drained.isEmpty() ? fromRear : drained.copyWithAmount(drained.getAmount() + fromRear.getAmount());
                }
            }
        }
        return drained;
    }

    private int gasAvailableAcrossCells(Level level, BlockPos pos) {
        int total = gasBuffer.getFluidAmount();
        ChaosTurbineBlockEntity rear = rearTurbineBE(level, pos);
        if (rear != null) total += rear.gasBuffer.getFluidAmount();
        return total;
    }


    @Override
    public void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        gasBuffer.readFromNBT(registries, tag.getCompound("GasBuffer"));
        matterBuffer.deserializeNBT(registries, tag.getCompound("MatterBuffer"));
        isController = tag.getBoolean("IsController");
        structureValid = tag.getBoolean("StructureValid");

        Direction loadedFacing = Direction.byName(tag.getString("Facing"));
        facing = loadedFacing != null ? loadedFacing : Direction.NORTH;

        currentGeneratedSpeed = tag.getFloat("CurrentGeneratedSpeed");
        currentGeneratedStress = tag.getFloat("CurrentGeneratedStress");

        // Never trust the saved structure result forever. Re-check after load.
        dirtyRevalidate = true;
        ticksUntilRevalidate = 0;
        lastStructureLog = "";
    }
}
