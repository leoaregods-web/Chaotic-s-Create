package com.com.chaos.Features;

import com.com.chaos.Blocks.ModBlocks;
import com.com.chaos.Fluids.ModFluids;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class LiquidSpaceMeteoriteFeature extends Feature<NoneFeatureConfiguration> {
    public LiquidSpaceMeteoriteFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        int radius = 4 + random.nextInt(4); // 4-7
        int innerRadius = Math.max(2, radius - 1);
        int radiusSq = radius * radius;
        int innerRadiusSq = innerRadius * innerRadius;

        if (origin.getY() < level.getMinBuildHeight() + radius + 1 || origin.getY() > level.getMaxBuildHeight() - radius - 1) {
            return false;
        }

        boolean foundReplaceable = false;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int distanceSq = dx * dx + dy * dy + dz * dz;
                    if (distanceSq > radiusSq) {
                        continue;
                    }

                    pos.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    if (!level.isOutsideBuildHeight(pos.getY()) && canReplace(level.getBlockState(pos))) {
                        foundReplaceable = true;
                        break;
                    }
                }
                if (foundReplaceable) break;
            }
            if (foundReplaceable) break;
        }

        if (!foundReplaceable) {
            return false;
        }

        boolean placedAny = false;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int distanceSq = dx * dx + dy * dy + dz * dz;
                    if (distanceSq > radiusSq) {
                        continue;
                    }

                    pos.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    if (level.isOutsideBuildHeight(pos.getY())) {
                        continue;
                    }

                    BlockState current = level.getBlockState(pos);
                    if (!canReplace(current) && !current.isAir()) {
                        continue;
                    }

                    if (distanceSq > innerRadiusSq) {
                        BlockState shell = random.nextFloat() < 0.18f
                                ? Blocks.COBBLED_DEEPSLATE.defaultBlockState()
                                : Blocks.DEEPSLATE.defaultBlockState();
                        this.setBlock(level, pos, shell);
                        placedAny = true;
                    } else {
                        if (dy <= 0) {
                            float oreChance = dy <= -1 ? 0.18f : 0.10f;

                            if (random.nextFloat() < oreChance) {
                                this.setBlock(level, pos, ModBlocks.MYTHRIL_ORE.get().defaultBlockState());
                            } else {
                                this.setBlock(level, pos, ModFluids.SPACE_SOURCE.get().defaultFluidState().createLegacyBlock());
                            }
                        } else {
                            this.setBlock(level, pos, Blocks.AIR.defaultBlockState());
                        }
                        placedAny = true;
                    }
                }
            }
        }

        return placedAny;
    }

    private static boolean canReplace(BlockState state) {
        return state.isAir()
                || state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(Blocks.DEEPSLATE)
                || state.is(Blocks.COBBLED_DEEPSLATE)
                || state.is(Blocks.TUFF)
                || state.is(Blocks.CALCITE)
                || state.is(Blocks.GRAVEL);
    }
}
