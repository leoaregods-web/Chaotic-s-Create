package com.com.chaos.Pressure;

import com.com.chaos.Fluids.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/**
 * Stateless flood-fill sampler. Starting from a seed position it explores the connected pocket of
 * passable space (air, and anything else with no collision - matches the usual "sealed room"
 * convention other atmosphere-style mods use) and tallies which sides of that pocket touch Deep
 * Water, Liquid Space, plain solid boundary, or an active {@link PressureSource} machine.
 * <p>
 * Deep Water / Liquid Space block the flood fill just like a solid wall would - the fill will never
 * path through them - but their exposed faces contribute pressure to the room they border. Put them
 * in tanks, pipes, or membrane blocks wrapped around a chamber; you don't need to flood the interior
 * itself for it to be affected. A boundary block whose block entity implements
 * {@link PressureSource} is checked first and contributes its own signed weight instead of being
 * classified by fluid state, so a machine doesn't need to be a fluid to move the room's pressure.
 * <p>
 * This is a lightweight approximation (single BFS pass, capped volume), not a full gas simulation -
 * that's intentional, since it needs to run cheaply from block entity ticks.
 */
public final class RoomPressureSampler {

    /** Safety cap so an open room (a hole to the sky, an unsealed cave) can't hang the server. */
    public static final int MAX_VOLUME = 4096;

    private RoomPressureSampler() {
    }

    public static Result sample(Level level, BlockPos seed) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        BlockPos seedImmutable = seed.immutable();
        queue.add(seedImmutable);
        visited.add(seedImmutable);

        long deepWaterFaces = 0;
        long spaceFaces = 0;
        long plainFaces = 0;
        long machineFaces = 0;
        float machineContribution = 0f;
        boolean sealed = true;

        while (!queue.isEmpty()) {
            if (visited.size() > MAX_VOLUME) {
                sealed = false;
                break;
            }

            BlockPos current = queue.poll();

            if (level.isOutsideBuildHeight(current)) {
                sealed = false;
                continue;
            }

            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction);
                if (visited.contains(neighbor)) {
                    continue;
                }

                if (isPassable(level, neighbor)) {
                    BlockPos neighborImmutable = neighbor.immutable();
                    visited.add(neighborImmutable);
                    queue.add(neighborImmutable);
                    continue;
                }

                // Boundary cell - classify what it's made of rather than exploring into it.
                // An active machine takes priority over fluid state: a Deep Water tank behind a
                // compressor's casing shouldn't also get counted as a plain fluid face.
                if (level.getBlockEntity(neighbor) instanceof PressureSource source) {
                    machineFaces++;
                    machineContribution += source.getPressureContribution();
                    continue;
                }

                FluidType type = level.getFluidState(neighbor).getFluidType();
                if (type == ModFluids.LIQUID_TYPE.get()) {
                    deepWaterFaces++;
                } else if (type == ModFluids.SPACE_TYPE.get()) {
                    spaceFaces++;
                } else {
                    plainFaces++;
                }
            }
        }

        return new Result(sealed, visited, deepWaterFaces, spaceFaces, plainFaces, machineFaces, machineContribution);
    }

    private static boolean isPassable(Level level, BlockPos pos) {
        if (!level.getFluidState(pos).isEmpty()) {
            return false; // any fluid, vanilla or modded, is a wall for flood-fill purposes
        }
        return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }

    public record Result(boolean sealed, Set<BlockPos> interior, long deepWaterFaces, long spaceFaces,
                          long plainFaces, long machineFaces, float machineContribution) {

        public long totalBoundaryFaces() {
            return deepWaterFaces + spaceFaces + plainFaces + machineFaces;
        }

        public float toPressureRatio() {
            if (!sealed || totalBoundaryFaces() == 0) {
                return PressureRatio.NORMAL;
            }
            float net = (deepWaterFaces - spaceFaces) + machineContribution;
            float swing = (net / totalBoundaryFaces()) * PressureRatio.MAX_SWING;
            return PressureRatio.clamp(PressureRatio.NORMAL + swing);
        }
    }
}
