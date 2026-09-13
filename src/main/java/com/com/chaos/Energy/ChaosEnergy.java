package com.com.chaos.Energy;

/**
 * Core definition of the Chaos Energy scale used throughout Chaotics Create.
 * Chaos Energy is volatile and unstable - it naturally decays over time and requires
 * constant input to maintain. Unlike traditional energy systems, CE is fundamentally
 * chaotic and must be actively "fed" or it will dissipate.
 */
public final class ChaosEnergy {
    private ChaosEnergy() {}

    /**
     * Minimum chaos energy before the system is considered "dead" and stops operating.
     * At this point, the nexus is inert and non-functional.
     */
    public static final long MIN_ENERGY = 0L;

    /**
     * Hard ceiling - a container can never store more than this, no matter the input.
     */
    public static final long MAX_ENERGY = 1_000_000L; // 1M CE units

    /**
     * Baseline capacity for a standard Nexus Controller. Modules can increase this.
     */
    public static final long STANDARD_CAPACITY = 100_000L; // 100k CE units

    /**
     * How much CE decays per tick (20 ticks = 1 second) when idle/passive.
     * A full nexus at max capacity will decay to 0 in about 200 seconds (10k ticks).
     * This is per nexus, not per module.
     */
    public static final long DECAY_PER_TICK = 50L;

    /**
     * Multiplier applied to decay rate when the nexus is actively processing.
     * Processing = modules are working, stress is being applied, etc.
     * This makes chaos energy more "expensive" when actually doing work.
     */
    public static final float ACTIVE_DECAY_MULTIPLIER = 1.5f;

    /**
     * Minimum energy required to start a nexus operation. Below this, nothing happens.
     */
    public static final long ACTIVATION_THRESHOLD = 1_000L;

    /**
     * Named energy tiers - used for multiblock logic and module compatibility.
     */
    public enum Tier {
        DEPLETED,      // CE <= 0 (dead, no function)
        CRITICAL,      // CE <= 10% of max (warning state, reduced output)
        LOW,           // CE <= 25% of max (can still operate, not ideal)
        NORMAL,        // CE between 25-75% of max (sweet spot for operations)
        HIGH,          // CE between 75-100% of max (boosted performance possible)
        OVERCHARGED    // CE >= 100% after insertion (unstable, may cause effects)
    }

    /**
     * Determines the energy tier for a given amount of chaos energy.
     * @param current the current stored energy
     * @param capacity the container's max capacity
     * @return the appropriate Tier
     */
    public static Tier tierOf(long current, long capacity) {
        if (current <= 0) return Tier.DEPLETED;
        float percent = (float) current / capacity;
        if (percent <= 0.1f) return Tier.CRITICAL;
        if (percent <= 0.25f) return Tier.LOW;
        if (percent <= 0.75f) return Tier.NORMAL;
        if (percent < 1.0f) return Tier.HIGH;
        return Tier.OVERCHARGED;
    }

    /**
     * Clamps energy to valid range [MIN_ENERGY, capacity].
     */
    public static long clamp(long energy, long capacity) {
        return Math.max(MIN_ENERGY, Math.min(capacity, energy));
    }

    /**
     * Calculates decay for this tick. Passes active = true if modules are currently working.
     * @param active whether the nexus is actively processing
     * @return CE to subtract this tick
     */
    public static long calculateDecay(boolean active) {
        if (active) {
            return (long)(DECAY_PER_TICK * ACTIVE_DECAY_MULTIPLIER);
        }
        return DECAY_PER_TICK;
    }
}
