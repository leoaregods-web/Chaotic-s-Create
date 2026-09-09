package com.com.chaos.Pressure;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Per-level cache of {@link RoomPressureSampler} results - and, since a machine can now push a
 * room's pressure around via {@link PressureSource}, the thing that actually tracks a room's felt
 * pressure over time rather than just memoizing an instantaneous snapshot.
 * <p>
 * Each room has a target ratio (what the current boundary - fluids plus any active
 * {@link PressureSource} machines - implies) and a current ratio (what callers actually receive),
 * with current stepping toward target by {@link PressureRatio#DRIFT_PER_TICK} every tick. A
 * brand-new room's current ratio starts equal to its target (discovering a room doesn't make it
 * ramp up from NORMAL), but from then on any change to the target - a fluid placed, a machine
 * switched on - only takes effect gradually.
 * <p>
 * Nothing here needs to be saved to disk - a reload just rediscovers each room fresh, and a
 * freshly-discovered room starts at its own equilibrium rather than easing up from NORMAL (see
 * the {@code state == null} branch below), so a reload doesn't cause a visible pressure dip.
 */
public final class PressureManager {

    /** How long a room's target is trusted before being resampled, in ticks. */
    private static final int CACHE_LIFETIME_TICKS = 40; // 2 seconds

    private static final Map<Level, PressureManager> INSTANCES = new WeakHashMap<>();

    private final Map<BlockPos, RoomState> cache = new HashMap<>();

    private PressureManager() {
    }

    public static PressureManager get(Level level) {
        return INSTANCES.computeIfAbsent(level, l -> new PressureManager());
    }

    /**
     * Returns the current (felt) pressure ratio for the sealed pocket of space containing
     * {@code pos}, advancing it one step closer to the room's target if time has passed since it
     * was last queried. Safe to call every tick from any number of block entities - see
     * {@link PressureRatio} for how to interpret the returned value.
     */
    public float getPressureRatio(Level level, BlockPos pos) {
        BlockPos key = pos.immutable();
        long now = level.getGameTime();

        RoomState state = cache.get(key);
        if (state == null || now - state.targetComputedAtTick >= CACHE_LIFETIME_TICKS) {
            RoomPressureSampler.Result result = RoomPressureSampler.sample(level, key);
            float target = result.toPressureRatio();

            if (state == null) {
                // First time this position has been sampled - nothing to drift from yet, so
                // start the room at its own equilibrium rather than easing up from NORMAL.
                state = new RoomState(target, now, target, now);
            } else {
                state.targetRatio = target;
                state.targetComputedAtTick = now;
                // currentRatio / currentUpdatedAtTick deliberately untouched here, so the room
                // keeps drifting from wherever it currently sits toward the refreshed target.
            }

            // Stamp every cell of the room, not just the queried one, so sibling blocks in a
            // multiblock chamber share this state - and its momentum - instead of each tracking
            // their own drift independently.
            for (BlockPos interiorPos : result.interior()) {
                cache.put(interiorPos, state);
            }
        }

        advanceTowardTarget(state, now);
        return state.currentRatio;
    }

    /** Steps a room's current ratio toward its target by up to DRIFT_PER_TICK per elapsed tick. */
    private void advanceTowardTarget(RoomState state, long now) {
        long elapsed = now - state.currentUpdatedAtTick;
        if (elapsed <= 0) {
            return;
        }
        state.currentUpdatedAtTick = now;

        float diff = state.targetRatio - state.currentRatio;
        float maxStep = PressureRatio.DRIFT_PER_TICK * elapsed;
        if (Math.abs(diff) <= maxStep) {
            state.currentRatio = state.targetRatio;
        } else {
            state.currentRatio += Math.signum(diff) * maxStep;
        }
        state.currentRatio = PressureRatio.clamp(state.currentRatio);
    }

    /**
     * Call from a block's neighbor-changed / fluid-changed hook to force a resample on next
     * query. Note this only clears {@code pos}'s own entry - other cells still sharing the old
     * room's state keep their momentum until they're separately invalidated or age out.
     */
    public void invalidate(BlockPos pos) {
        cache.remove(pos.immutable());
    }

    public void invalidateAll() {
        cache.clear();
    }

    /** Mutable per-room tracking state - deliberately not a record, since drift mutates it in place. */
    private static final class RoomState {
        private float targetRatio;
        private long targetComputedAtTick;
        private float currentRatio;
        private long currentUpdatedAtTick;

        private RoomState(float targetRatio, long targetComputedAtTick, float currentRatio, long currentUpdatedAtTick) {
            this.targetRatio = targetRatio;
            this.targetComputedAtTick = targetComputedAtTick;
            this.currentRatio = currentRatio;
            this.currentUpdatedAtTick = currentUpdatedAtTick;
        }
    }
}
