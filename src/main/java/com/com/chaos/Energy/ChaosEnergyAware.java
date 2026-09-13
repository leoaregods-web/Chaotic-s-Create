package com.com.chaos.Energy;

/**
 * Implement on a BlockEntity or Module that wants to interact with Chaos Energy.
 * <p>
 * Typical wiring, called from your own {@code serverTick}:
 * <pre>{@code
 * ChaosEnergyManager.ChaosEnergyStorage storage = ChaosEnergyManager.getStorage(level, pos);
 * blockEntity.onChaosEnergyUpdated(storage);
 * }</pre>
 */
public interface ChaosEnergyAware {

    /**
     * Called whenever the chaos energy state changes (energy added/removed/decayed).
     * @param storage the current energy storage state
     */
    void onChaosEnergyUpdated(ChaosEnergyManager.ChaosEnergyStorage storage);

    /** Below this energy the block should refuse to operate. Default: ACTIVATION_THRESHOLD. */
    default long minOperatingEnergy() {
        return ChaosEnergy.ACTIVATION_THRESHOLD;
    }

    /** Whether this energy level is sufficient for operation. */
    default boolean canOperateWith(long energy) {
        return energy >= minOperatingEnergy();
    }
}
