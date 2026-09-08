package com.com.chaos.Pressure;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Per-level cache of {@link RoomPressureSampler} results.
 * <p>
 * Pressure is entirely derived from live block/fluid state, so nothing here needs to be saved to
 * disk - it's rebuilt on demand. The cache exists purely so many pressure-aware block entities
 * sharing the same sealed room don't each re-run their own flood fill every tick: the first query
 * into a room samples it and stamps the ratio onto every cell of that room's interior, so later
 * queries from other blocks in the same room are a free map lookup until the cache goes stale.
 */
public final class PressureManager {

    /** How long a cached result is trusted before being recomputed, in ticks. */
    private static final int CACHE_LIFETIME_TICKS = 40; // 2 seconds

    private static final Map<Level, PressureManager> INSTANCES = new WeakHashMap<>();

    private final Map<BlockPos, CachedRoom> cache = new HashMap<>();

    private PressureManager() {
    }

    public static PressureManager get(Level level) {
        return INSTANCES.computeIfAbsent(level, l -> new PressureManager());
    }

    /**
     * Returns the pressure ratio for the sealed pocket of space containing {@code pos}. Safe to call
     * every tick from any number of block entities - see {@link PressureRatio} for how to interpret
     * the returned value.
     */
    public float getPressureRatio(Level level, BlockPos pos) {
        BlockPos key = pos.immutable();
        long now = level.getGameTime();

        CachedRoom cached = cache.get(key);
        if (cached != null && now - cached.computedAtTick() < CACHE_LIFETIME_TICKS) {
            return cached.ratio();
        }

        RoomPressureSampler.Result result = RoomPressureSampler.sample(level, key);
        float ratio = result.toPressureRatio();
        CachedRoom fresh = new CachedRoom(ratio, now);

        // Stamp every cell of the room, not just the queried one, so sibling blocks in a
        // multiblock chamber share this result instead of each re-sampling independently.
        for (BlockPos interiorPos : result.interior()) {
            cache.put(interiorPos, fresh);
        }
        return ratio;
    }

    /** Call from a block's neighbor-changed / fluid-changed hook to force a recompute on next query. */
    public void invalidate(BlockPos pos) {
        cache.remove(pos.immutable());
    }

    public void invalidateAll() {
        cache.clear();
    }

    private record CachedRoom(float ratio, long computedAtTick) {
    }
}
