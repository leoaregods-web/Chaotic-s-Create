package com.com.chaos.Blocks.Multiblock.Astral.NexusOfChaos;

import com.com.chaos.Blocks.ModBlockEntities;
import com.com.chaos.Blocks.Multiblock.Abyssals.GaseousConverter.ChaosTurbineBlockEntity;
import com.com.chaos.Blocks.Multiblock.Abyssals.GaseousConverter.GaseousConverterStructure;
import com.com.chaos.Blocks.Multiblock.Astral.ModularMultiblockController;
import com.com.chaos.Blocks.Multiblock.Astral.ModularMultiblockModule;
import com.com.chaos.Energy.ChaosEnergy;
import com.com.chaos.Energy.ChaosEnergyManager;
import com.com.chaos.Pressure.PressureManager;
import com.com.chaos.Pressure.PressureRatio;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

public class NexusControllerBlockEntity extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int REVALIDATE_INTERVAL_TICKS = 10;

    // Illustrative geometry, not confirmed in-game yet: NexusOfChaosStructure's own PATTERN
    // leaves the anchor's 4 orthogonal neighbours (front/back/left/right) as Type.EMPTY -
    // open corridors through the ring of Astral Casing around it. These ports sit one cell
    // further out than each corridor, so a module's own anchor lines up with that opening.
    // facingTurns points each port's expected module facing away from the core along that
    // corridor. Re-check these offsets against the real structure before relying on them.
    private static final List<ModularMultiblockController.Port> PORTS = List.of(
            new ModularMultiblockController.Port("front", 0, -1, 0, 0),
            new ModularMultiblockController.Port("rear", 0, 3, 0, 2),
            new ModularMultiblockController.Port("left", -2, 1, 0, 1),
            new ModularMultiblockController.Port("right", 2, 1, 0, 3)
    );

    // Identifies this controller's module pool in ModularMultiblockModuleRegistry - the
    // Nexus and any future controller type (a Monolith, etc) each get their own id here and
    // never see each other's registered modules. Addons register against this constant, e.g.:
    //   ModularMultiblockModuleRegistry.register(NexusControllerBlockEntity.CONTROLLER_TYPE, MY_MODULE);
    // NOTE: swap "chaos" below for your actual registered mod id if it differs.
    public static final ResourceLocation CONTROLLER_TYPE = ResourceLocation.fromNamespaceAndPath("chaos", "nexus");

    private final ModularMultiblockController modules = new ModularMultiblockController(CONTROLLER_TYPE, PORTS);

    private boolean isController;
    private boolean structureValid;
    private Direction facing = Direction.NORTH;

    private boolean dirtyRevalidate = true;
    private int ticksUntilRevalidate;
    private int processCooldown;

    private String lastStructureLog = "";

    public NexusControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NEXUS_CONTROLLER.get(), pos, state);
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

    /**
     * Gets the current chaos energy storage for this nexus.
     */
    public ChaosEnergyManager.ChaosEnergyStorage getChaosEnergy(Level level) {
        return ChaosEnergyManager.get(level).getOrCreateStorage(level, getBlockPos());
    }

    /**
     * Checks if this nexus has enough chaos energy to operate.
     */
    public boolean hasEnoughEnergy(Level level) {
        ChaosEnergyManager.ChaosEnergyStorage storage = getChaosEnergy(level);
        return storage.energy >= ChaosEnergy.ACTIVATION_THRESHOLD;
    }

    public static void serverTick(
            Level level,
            BlockPos pos,
            BlockState state,
            NexusControllerBlockEntity be) {

        if (be.dirtyRevalidate || --be.ticksUntilRevalidate <= 0) {
            be.dirtyRevalidate = false;
            be.ticksUntilRevalidate = REVALIDATE_INTERVAL_TICKS;
            be.revalidate(level, pos);
        }

        if (be.isController && be.structureValid) {
            // Check if any modules are actively working
            boolean isActive = !be.modules.getActiveModules().isEmpty();
            
            // Apply chaos energy decay
            ChaosEnergyManager.get(level).applyDecay(pos, isActive);
            
            // Only tick modules if we have enough energy
            if (be.hasEnoughEnergy(level)) {
                be.modules.revalidate(level, pos, be.facing);
                be.modules.tickModules(level, pos, be.facing);
            }
            
            be.setChanged();
        }
    }

    public static void clientTick(
            Level level,
            BlockPos pos,
            BlockState state,
            NexusControllerBlockEntity be) {
    }

    private void revalidate(Level level, BlockPos pos) {
        NexusOfChaosStructure.MatchResult match =
                NexusOfChaosStructure.findValidFacingResult(level, pos);

        boolean nowController = match != null;
        boolean wasValid = structureValid;

        isController = nowController;

        if (nowController) {
            structureValid = true;
            facing = match.facing();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        
        // Save chaos energy state
        if (level != null) {
            ChaosEnergyManager.ChaosEnergyStorage storage = getChaosEnergy(level);
            CompoundTag energyTag = new CompoundTag();
            energyTag.putLong("energy", storage.energy);
            energyTag.putLong("capacity", storage.capacity);
            tag.put("chaosEnergy", energyTag);
        }
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        
        // Restore chaos energy state
        if (tag.contains("chaosEnergy")) {
            CompoundTag energyTag = tag.getCompound("chaosEnergy");
            long energy = energyTag.getLong("energy");
            long capacity = energyTag.getLong("capacity");
            
            if (level != null) {
                ChaosEnergyManager.ChaosEnergyStorage storage = 
                    ChaosEnergyManager.get(level).getOrCreateStorage(level, getBlockPos());
                storage.energy = ChaosEnergy.clamp(energy, capacity);
                storage.capacity = capacity;
            }
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    public String describeStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("controller=").append(isController);
        sb.append(", structureValid=").append(structureValid);
        sb.append(", facing=").append(facing);

        if (level != null) {
            ChaosEnergyManager.ChaosEnergyStorage energy = getChaosEnergy(level);
            sb.append(", energy=").append(energy.energy).append("/").append(energy.capacity);
            sb.append(" (").append(String.format("%.1f%%", energy.getPercent() * 100)).append(")");
            sb.append(", tier=").append(energy.getTier());
        }

        if (isController && structureValid) {
            Map<ModularMultiblockController.Port, ModularMultiblockModule> active = modules.getActiveModules();
            for (ModularMultiblockController.Port port : PORTS) {
                ModularMultiblockModule module = active.get(port);
                sb.append(", ").append(port.name()).append("=")
                        .append(module != null ? module.id() : "empty");
            }
        } else if (level != null) {
            sb.append(" - ").append(NexusOfChaosStructure.debugFacingReport(level, getBlockPos()));
        }

        String status = sb.toString();
        // Always log this - it's only ever triggered by an explicit shift-click,
        // so there's no spam risk, and it means the report can be copy-pasted
        // straight out of latest.log instead of off the chat screen.
        LOGGER.info("[NexusController] Status @ {}: {}", getBlockPos(), status);
        return status;
    }
}
