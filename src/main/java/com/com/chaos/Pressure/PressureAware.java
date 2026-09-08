package com.com.chaos.Pressure;

/**
 * Implement on a BlockEntity that wants to react to the pressure of the sealed room / multiblock
 * chamber it sits inside.
 * <p>
 * Typical wiring, called from your own {@code serverTick}:
 * <pre>{@code
 * float ratio = PressureManager.get(level).getPressureRatio(level, pos);
 * blockEntity.onPressureUpdated(ratio);
 * }</pre>
 */
public interface PressureAware {

    /**
     * @param pressureRatio 1.0f = normal atmosphere, higher = Deep Water dominant / crushing,
     *                      lower = Liquid Space dominant / vacuum. Always within
     *                      [{@link PressureRatio#MIN_RATIO}, {@link PressureRatio#MAX_RATIO}].
     */
    void onPressureUpdated(float pressureRatio);

    /** Below this ratio the block should refuse to operate. Default: no lower limit. */
    default float minOperatingPressure() {
        return PressureRatio.MIN_RATIO;
    }

    /** Above this ratio the block should refuse to operate. Default: no upper limit. */
    default float maxOperatingPressure() {
        return PressureRatio.MAX_RATIO;
    }

    default boolean isWithinOperatingRange(float pressureRatio) {
        return pressureRatio >= minOperatingPressure() && pressureRatio <= maxOperatingPressure();
    }
}
