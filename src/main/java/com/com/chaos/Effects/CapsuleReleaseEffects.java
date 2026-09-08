package com.com.chaos.Effects;

import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.FluidStack;
import com.com.chaos.Fluids.ModFluids;
import com.com.chaos.Items.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.com.chaos.ModDataComponents;

public final class CapsuleReleaseEffects {
    private CapsuleReleaseEffects() {

    }

    public static boolean trigger(Level level, Vec3 pos, ItemStack originalInput) {
        if (originalInput.is(ModItems.SHARD_OF_CHAOS.get())) {
            shard(level, pos, level.getRandom());
            return true;
        }

        SimpleFluidContent content = originalInput.getOrDefault(ModDataComponents.GAS_CONTENT.get(), SimpleFluidContent.EMPTY);
        FluidStack fluid = content.copy();
        if (fluid.isEmpty()) {
            return false;
        }

        boolean small = fluid.getAmount() <= 500; // matches the small-tier capacity ceiling - tune if you change tier sizes

        if (fluid.getFluid().isSame(ModFluids.LIQUID_OXYGEN_SOURCE.get())) {
            if (small) oxygen_small(level, pos); else oxygen(level, pos);
        } else if (fluid.getFluid().isSame(ModFluids.LIQUID_NITROGEN_SOURCE.get())) {
            if (small) nitrogen_small(level, pos); else nitrogen(level, pos);
        } else if (fluid.getFluid().isSame(ModFluids.LIQUID_ARGON_SOURCE.get())) {
            if (small) argon_small(level, pos); else argon(level, pos);
        } else {
            return false;
        }
        return true;
    }

    private static void burst(Level level, Vec3 pos) {
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.EXPLOSION, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
            server.sendParticles(ParticleTypes.CLOUD, pos.x, pos.y, pos.z, 12, 0.2, 0.1, 0.2, 0.02);
        }

        level.playSound(null, BlockPos.containing(pos), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 0.6f, 1.2f);    }

    private static void burst2(Level level, Vec3 pos) {
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.EXPLOSION, pos.x, pos.y, pos.z, 2, 0, 0, 0, 0);
            server.sendParticles(ParticleTypes.CLOUD, pos.x, pos.y, pos.z, 24, 0.2, 0.1, 0.2, 0.02);
        }

        level.playSound(null, BlockPos.containing(pos), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 0.6f, 1.2f);    }


    private static void oxygen(Level level, Vec3 pos) {
        burst(level, pos);

        if (level instanceof ServerLevel server) {
            server.explode(
                    null,                                   // source entity
                    server.damageSources().explosion(null, null),
                    null,                                   // default damage calculator
                    pos.x, pos.y, pos.z,
                    4.0f,                                   // strength
                    true,                                   // sets fires
                    Level.ExplosionInteraction.TNT,         // breaks blocks
                    ParticleTypes.EXPLOSION,
                    ParticleTypes.EXPLOSION_EMITTER,
                    SoundEvents.GENERIC_EXPLODE
            );
        }
    }

    private static void oxygen_small(Level level, Vec3 pos) {
        burst2(level, pos);

        if (level instanceof ServerLevel server) {
            server.explode(
                    null,                                   // source entity
                    server.damageSources().explosion(null, null),
                    null,                                   // default damage calculator
                    pos.x, pos.y, pos.z,
                    8.0f,                                   // strength
                    true,                                   // sets fires
                    Level.ExplosionInteraction.TNT,         // breaks blocks
                    ParticleTypes.EXPLOSION,
                    ParticleTypes.EXPLOSION_EMITTER,
                    SoundEvents.GENERIC_EXPLODE
            );
        }
    }

    private static void nitrogen(Level level, Vec3 pos) {
        burst(level, pos);

        if (level instanceof ServerLevel server) {
            server.explode(
                    null,
                    server.damageSources().explosion(null, null),
                    null,
                    pos.x, pos.y, pos.z,
                    3.5f,                                   // a bit smaller than oxygen
                    false,                                  // no fire
                    Level.ExplosionInteraction.TNT,         // breaks blocks
                    ParticleTypes.EXPLOSION,
                    ParticleTypes.EXPLOSION_EMITTER,
                    SoundEvents.GENERIC_EXPLODE
            );
        }

        double radius = 3.0;
        AABB box = new AABB(pos.x - radius, pos.y - 1, pos.z - radius, pos.x + radius, pos.y + 3, pos.z + radius);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box)) {
            double dx = entity.getX() - pos.x;
            double dz = entity.getZ() - pos.z;
            if (dx * dx + dz * dz <= radius * radius) {
                entity.setTicksFrozen(Math.max(entity.getTicksFrozen(), 300));
            }
        }

        BlockPos center = BlockPos.containing(pos);
        for (BlockPos bp : BlockPos.betweenClosed(center.offset(-3, -1, -3), center.offset(3, 2, 3))) {
            double dx = (bp.getX() + 0.5) - pos.x;
            double dz = (bp.getZ() + 0.5) - pos.z;
            if (dx * dx + dz * dz > radius * radius) {
                continue;
            }

            if (level.getFluidState(bp).is(Fluids.WATER)) {
                level.setBlockAndUpdate(bp, Blocks.ICE.defaultBlockState());
            }

            BlockPos above = bp.above();
            if (level.getBlockState(bp).isFaceSturdy(level, bp, Direction.UP) && level.isEmptyBlock(above)) {
                level.setBlockAndUpdate(above, Blocks.SNOW.defaultBlockState());
            }
        }
    }

    private static void nitrogen_small(Level level, Vec3 pos) {
        burst2(level, pos);

        if (level instanceof ServerLevel server) {
            server.explode(
                    null,
                    server.damageSources().explosion(null, null),
                    null,
                    pos.x, pos.y, pos.z,
                    7f,                                   // a bit smaller than oxygen
                    false,                                  // no fire
                    Level.ExplosionInteraction.TNT,         // breaks blocks
                    ParticleTypes.EXPLOSION,
                    ParticleTypes.EXPLOSION_EMITTER,
                    SoundEvents.GENERIC_EXPLODE
            );
        }

        double radius = 6.0;
        AABB box = new AABB(pos.x - radius, pos.y - 2, pos.z - radius, pos.x + radius, pos.y + 6, pos.z + radius);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box)) {
            double dx = entity.getX() - pos.x;
            double dz = entity.getZ() - pos.z;
            if (dx * dx + dz * dz <= radius * radius) {
                entity.setTicksFrozen(Math.max(entity.getTicksFrozen(), 600));
            }
        }

        BlockPos center = BlockPos.containing(pos);
        for (BlockPos bp : BlockPos.betweenClosed(center.offset(-6, -2, -6), center.offset(6, 4, 6))) {
            double dx = (bp.getX() + 0.5) - pos.x;
            double dz = (bp.getZ() + 0.5) - pos.z;
            if (dx * dx + dz * dz > radius * radius) {
                continue;
            }

            if (level.getFluidState(bp).is(Fluids.WATER)) {
                level.setBlockAndUpdate(bp, Blocks.ICE.defaultBlockState());
            }

            BlockPos above = bp.above();
            if (level.getBlockState(bp).isFaceSturdy(level, bp, Direction.UP) && level.isEmptyBlock(above)) {
                level.setBlockAndUpdate(above, Blocks.SNOW.defaultBlockState());
            }
        }
    }

    private static void argon(Level level, Vec3 pos) {
        burst(level, pos);

        double radius = 3.0;
        BlockPos center = BlockPos.containing(pos);
        for (BlockPos bp : BlockPos.betweenClosed(center.offset(-3, -1, -3), center.offset(3, 2, 3))) {
            double dx = (bp.getX() + 0.5) - pos.x;
            double dz = (bp.getZ() + 0.5) - pos.z;
            if (dx * dx + dz * dz > radius * radius) {
                continue;
            }

            if (level.getBlockState(bp).is(Blocks.FIRE)) {
                level.setBlockAndUpdate(bp, Blocks.AIR.defaultBlockState());
            }
        }
    }

    private static void argon_small(Level level, Vec3 pos) {
        burst2(level, pos);

        if (level instanceof ServerLevel server) {
            server.explode(
                    null,
                    server.damageSources().explosion(null, null),
                    null,
                    pos.x, pos.y, pos.z,
                    0.5f,
                    false,
                    Level.ExplosionInteraction.TNT,
                    ParticleTypes.EXPLOSION,
                    ParticleTypes.EXPLOSION_EMITTER,
                    SoundEvents.GENERIC_EXPLODE
            );
        }

        double radius = 6.0;
        BlockPos center = BlockPos.containing(pos);
        for (BlockPos bp : BlockPos.betweenClosed(center.offset(-6, -2, -6), center.offset(6, 4, 6))) {
            double dx = (bp.getX() + 0.5) - pos.x;
            double dz = (bp.getZ() + 0.5) - pos.z;
            if (dx * dx + dz * dz > radius * radius) {
                continue;
            }

            if (level.getBlockState(bp).is(Blocks.FIRE)) {
                level.setBlockAndUpdate(bp, Blocks.AIR.defaultBlockState());
            }
        }
    }

    private static void shard(Level level, Vec3 pos, RandomSource random) {
        double radius = 2.0;
        BlockPos center = BlockPos.containing(pos);
        for (BlockPos bp : BlockPos.betweenClosed(center.offset(-2, -2, -2), center.offset(2, 2, 2))) {
            double dx = (bp.getX() + 0.5) - pos.x;
            double dy = (bp.getY() + 0.5) - pos.y;
            double dz = (bp.getZ() + 0.5) - pos.z;
            if (dx * dx + dy * dy + dz * dz > radius * radius) {
                continue; // keep the affected area spherical
            }

            BlockState current = level.getBlockState(bp);

            if (current.is(Blocks.BEDROCK)) {
                continue; // never touch bedrock
            }
            if (current.isAir() || level.isEmptyBlock(bp)) {
                continue; // only affect actual ground, not empty space
            }

            int roll = random.nextInt(1000);

            if (roll < 10) {
                level.setBlockAndUpdate(bp, Blocks.AIR.defaultBlockState());
            } else if (roll < 780) {
                level.setBlockAndUpdate(bp, ModFluids.SPACE_BLOCK.get().defaultBlockState());
            } else {
                level.setBlockAndUpdate(bp, ModFluids.LIQUID_BLOCK.get().defaultBlockState());
            }
        }
    }
}