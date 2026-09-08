package com.com.chaos.Pressure;

/**
 * Central definition of the atmospheric pressure scale used throughout Chaotics Create.
 * <p>
 * Pressure is expressed as a single float "ratio" where {@link #NORMAL} (1.0f) is normal
 * atmosphere - a room with no Deep Water / Liquid Space influence on its walls. Values above
 * 1.0f mean higher-than-normal pressure (Deep Water dominant), values below 1.0f mean
 * lower-than-normal pressure / vacuum (Liquid Space dominant).
 * <p>
 * Blocks and multiblocks should compare against the named thresholds here rather than hardcoding
 * numbers against {@link PressureManager} output, so balance changes only ever happen in one place.
 */
public final class PressureRatio {

    private PressureRatio() {
    }

    /** Hard floor - a room can never report below this, even fully surrounded by Liquid Space. */
    public static final float MIN_RATIO = 0.05f;

    /** Hard ceiling - a room can never report above this, even fully surrounded by Deep Water. */
    public static final float MAX_RATIO = 4.0f;

    /** Baseline for an unsealed, untouched, or purely ambient room. */
    public static final float NORMAL = 1.0f;

    /** How far a wall that is 100% Deep Water or 100% Liquid Space can swing the ratio away from NORMAL. */
    public static final float MAX_SWING = 3.0f;

    // Named bands - use these instead of magic numbers when a block decides how to react.
    public static final float VACUUM_THRESHOLD = 0.25f;
    public static final float LOW_THRESHOLD = 0.75f;
    public static final float HIGH_THRESHOLD = 1.5f;
    public static final float CRUSHING_THRESHOLD = 3.0f;

    public static Tier tierOf(float ratio) {
        if (ratio <= VACUUM_THRESHOLD) return Tier.VACUUM;
        if (ratio <= LOW_THRESHOLD) return Tier.LOW;
        if (ratio < HIGH_THRESHOLD) return Tier.NORMAL;
        if (ratio < CRUSHING_THRESHOLD) return Tier.HIGH;
        return Tier.CRUSHING;
    }

    public static float clamp(float ratio) {
        return Math.max(MIN_RATIO, Math.min(MAX_RATIO, ratio));
    }

    public enum Tier {
        VACUUM,
        LOW,
        NORMAL,
        HIGH,
        CRUSHING
    }
}
