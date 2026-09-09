package com.com.chaos.Pressure;

/**
 * Implement on a BlockEntity that actively contributes to (or draws down) the pressure of a
 * sealed room it sits on the boundary of. This is the machine-driven counterpart to the passive
 * Deep Water / Liquid Space fluid faces {@link RoomPressureSampler} already counts - a compressor,
 * vent, or regulator block entity implements this instead of needing to be a fluid at all.
 * <p>
 * {@link RoomPressureSampler} checks for this capability on every boundary cell it walks, and
 * folds the returned value into the same weighted-average formula it uses for fluid faces: a
 * single Deep Water face contributes +1, a single Liquid Space face contributes -1, so a
 * "heavier" pressure machine is simply one whose {@link #getPressureContribution()} returns a
 * larger magnitude than that.
 */
public interface PressureSource {

    /**
     * @return the signed pressure weight this block contributes to whichever sealed room it
     * borders. Positive pushes the room's target ratio toward Deep-Water-dominant (higher ratio,
     * see {@link PressureRatio}), negative pushes it toward Liquid-Space-dominant (lower ratio).
     * Return 0 when installed but currently idle (unpowered, out of fuel, disabled) rather than
     * omitting the interface entirely - the sampler still counts an idle source as one boundary
     * face, which is usually what you want so the room doesn't read as more "sealed by plain
     * wall" than it actually is once the machine is built in.
     */
    float getPressureContribution();
}
