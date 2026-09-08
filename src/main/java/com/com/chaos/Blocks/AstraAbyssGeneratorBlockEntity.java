package com.com.chaos.Blocks;

import com.com.chaos.Fluids.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class AstraAbyssGeneratorBlockEntity extends BlockEntity{
    public final AstraAbyssGenEnergyStorage energyStorage = new AstraAbyssGenEnergyStorage(100000, 1000);
    private int generationRate = 1; // FE per tick


    public AstraAbyssGeneratorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.ASTRA_ABYSS_GENERATOR.get(), pos, blockState);
    }

    // Required getter for the capability registration
    public IEnergyStorage getEnergyStorage() {
        return this.energyStorage;
    }


    public static void tick(Level level, BlockPos pos, BlockState state, AstraAbyssGeneratorBlockEntity blockEntity) {
        if (level.isClientSide()) return;

        boolean changed = false;

        // 1. GENERATION LOGIC: Only add energy if the fluid source conditions are met
        if (blockEntity.hasBothLiquidSources(level, pos)) {
            // Check if there is actually room to add energy before modifying anything
            if (blockEntity.energyStorage.getEnergyStored() < blockEntity.energyStorage.getMaxEnergyStored()) {
                int output = blockEntity.generationRate;
                blockEntity.energyStorage.addEnergy(output * MachineUpgrades.getInstalledSpeedBonus(level, pos));
                changed = true;
            }
        }

        // 2. DISTRIBUTION LOGIC: Move this OUTSIDE the fluid check loop!
        // This ensures energy safely drains out even if the fluid blocks are broken or removed.
        if (blockEntity.energyStorage.getEnergyStored() > 0) {
            int energyBeforeDist = blockEntity.energyStorage.getEnergyStored();
            blockEntity.distributeEnergy(level, pos);

            // If any energy was successfully pushed out, mark the block entity as changed
            if (blockEntity.energyStorage.getEnergyStored() != energyBeforeDist) {
                changed = true;
            }
        }

        // 3. PERFORMANCE OPTIMIZATION: Only save to disk and update network packets if data actually shifted
        if (changed) {
            blockEntity.setChanged();
        }
    }

    private void distributeEnergy(Level level, BlockPos pos) {
        if (this.energyStorage.getEnergyStored() <= 0) return;

        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = pos.relative(direction);

            // FIXED: Fetching capabilities via NeoForge 1.21 lookup system
            IEnergyStorage targetStorage = level.getCapability(Capabilities.EnergyStorage.BLOCK, adjacentPos, direction.getOpposite());

            if (targetStorage != null && targetStorage.canReceive()) {
                // Determine how much energy we can extract and push
                int toSend = this.energyStorage.extractEnergy(1000, true); // Simulate extract
                int received = targetStorage.receiveEnergy(toSend, false); // Actual push

                this.energyStorage.extractEnergy(received, false); // Actual extract

                if (this.energyStorage.getEnergyStored() <= 0) break;
            }
        }
    }

    private boolean hasBothLiquidSources(Level level, BlockPos pos) {
        boolean foundFluidA = false;
        boolean foundFluidB = false;

        // Fetch your custom Fluid instances from your Mod Fluids Deferred Register
        Fluid fluidA = ModFluids.LIQUID_SOURCE.get();
        Fluid fluidB = ModFluids.SPACE_SOURCE.get();

        // Scan all 6 directions surrounding the generator block
        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = pos.relative(direction);
            FluidState fluidState = level.getFluidState(adjacentPos);

            // 1.21.1 Check: Is the fluid a true source block, and does it match our registries?
            if (fluidState.isSource()) {
                if (fluidState.is(fluidA)) {
                    foundFluidA = true;
                } else if (fluidState.is(fluidB)) {
                    foundFluidB = true;
                }
            }

            // Optimization: exit loop early if both are already confirmed
            if (foundFluidA && foundFluidB) {
                return true;
            }
        }

        return false;
    }
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("GeneratorEnergy")) {
            CompoundTag energyTag = tag.getCompound("GeneratorEnergy");
            this.energyStorage.deserializeNBT(energyTag);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.put("GeneratorEnergy", this.energyStorage.serializeNBT());
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        // Save the exact same data we use for disk saving
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
