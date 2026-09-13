package com.com.chaos.Energy;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Manages Chaos Energy storage and decay for the Nexus Controller.
 * Each nexus has its own energy pool that decays over time - energy must be constantly replenished.
 * <p>
 * This manager handles:
 * - Storing chaos energy for each nexus
 * - Automatic decay calculation (both idle and active)
 * - Energy insertion/extraction
 * - Energy tier classification
 */
public final class ChaosEnergyManager {

    private static final Map<Level, ChaosEnergyManager> INSTANCES = new WeakHashMap<>();

    private final Map<BlockPos, ChaosEnergyStorage> storage = new HashMap<>();

    private ChaosEnergyManager() {
    }

    /**
     * Gets or creates the ChaosEnergyManager for a level.
     */
    public static ChaosEnergyManager get(Level level) {
        return INSTANCES.computeIfAbsent(level, l -> new ChaosEnergyManager());
    }

    /**
     * Gets or creates a Chaos Energy storage for a position (typically a Nexus Controller).
     * @param level the level
     * @param pos the position of the nexus controller
     * @return the energy storage for this position
     */
    public ChaosEnergyStorage getOrCreateStorage(Level level, BlockPos pos) {
        BlockPos key = pos.immutable();
        return storage.computeIfAbsent(key, k -> new ChaosEnergyStorage(
                ChaosEnergy.STANDARD_CAPACITY,
                0L // Start with zero energy
        ));
    }

    /**
     * Gets the current energy storage at a position, or null if none exists.
     */
    @Nullable
    public ChaosEnergyStorage getStorage(BlockPos pos) {
        return storage.get(pos.immutable());
    }

    /**
     * Applies decay to stored energy at a position.
     * Call this every tick from your nexus controller.
     * <p>
     * @param pos the nexus controller position
     * @param isActive true if modules are currently processing
     * @return the decay amount that was applied
     */
    public long applyDecay(BlockPos pos, boolean isActive) {
        ChaosEnergyStorage store = getStorage(pos);
        if (store == null) return 0L;

        long decayAmount = ChaosEnergy.calculateDecay(isActive);
        store.energy = ChaosEnergy.clamp(store.energy - decayAmount, store.capacity);
        return decayAmount;
    }

    /**
     * Inserts chaos energy into a nexus. Decay still applies on the same tick.
     * @param pos the nexus controller position
     * @param amount the energy to insert
     * @return the amount actually inserted (clamped by capacity)
     */
    public long insertEnergy(BlockPos pos, long amount) {
        ChaosEnergyStorage store = getStorage(pos);
        if (store == null) return 0L;

        long oldEnergy = store.energy;
        store.energy = ChaosEnergy.clamp(store.energy + amount, store.capacity);
        return store.energy - oldEnergy;
    }

    /**
     * Extracts chaos energy from a nexus (used by modules that consume energy).
     * @param pos the nexus controller position
     * @param amount the energy to extract
     * @return the amount actually extracted
     */
    public long extractEnergy(BlockPos pos, long amount) {
        ChaosEnergyStorage store = getStorage(pos);
        if (store == null) return 0L;

        long oldEnergy = store.energy;
        store.energy = Math.max(ChaosEnergy.MIN_ENERGY, store.energy - amount);
        return oldEnergy - store.energy;
    }

    /**
     * Increases the max capacity of a nexus (e.g., when a battery module is attached).
     * @param pos the nexus controller position
     * @param extraCapacity the additional capacity to add
     */
    public void increaseCapacity(BlockPos pos, long extraCapacity) {
        ChaosEnergyStorage store = getStorage(pos);
        if (store != null) {
            store.capacity = Math.min(
                    ChaosEnergy.MAX_ENERGY,
                    store.capacity + extraCapacity
            );
        }
    }

    /**
     * Clears all energy storage for a position (e.g., when a nexus is broken).
     */
    public void clear(BlockPos pos) {
        storage.remove(pos.immutable());
    }

    /**
     * Clears all energy storage (usually on level unload).
     */
    public void clearAll() {
        storage.clear();
    }

    /**
     * Immutable snapshot of a nexus's chaos energy state.
     */
    public static class ChaosEnergyStorage {
        public long capacity;
        public long energy;

        public ChaosEnergyStorage(long capacity, long energy) {
            this.capacity = ChaosEnergy.clamp(capacity, ChaosEnergy.MAX_ENERGY);
            this.energy = ChaosEnergy.clamp(energy, this.capacity);
        }

        /**
         * Returns the current energy tier.
         */
        public ChaosEnergy.Tier getTier() {
            return ChaosEnergy.tierOf(energy, capacity);
        }

        /**
         * Returns the energy as a percentage (0.0 to 1.0).
         */
        public float getPercent() {
            return (float) energy / capacity;
        }

        /**
         * Whether this storage is completely depleted.
         */
        public boolean isDepleted() {
            return energy <= 0L;
        }

        /**
         * Whether this storage is at max capacity.
         */
        public boolean isFull() {
            return energy >= capacity;
        }
    }
}
