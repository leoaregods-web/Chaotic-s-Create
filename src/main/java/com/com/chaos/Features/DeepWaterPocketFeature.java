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

public class DeepWaterPocketFeature extends Feature<NoneFeatureConfiguration> {
    public DeepWaterPocketFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        if (origin.getY() > -20) {
            return false;
        }

        int radiusXZ = 3 + random.nextInt(3); // 3-5
        int radiusY = 2 + random.nextInt(2);  // 2-3
        int outerRadiusXZ = radiusXZ + 1;
        int outerRadiusY = radiusY + 1;

        boolean placedAny = false;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int dx = -outerRadiusXZ; dx <= outerRadiusXZ; dx++) {
            for (int dy = -outerRadiusY; dy <= outerRadiusY; dy++) {
                for (int dz = -outerRadiusXZ; dz <= outerRadiusXZ; dz++) {
                    double outerNorm = (dx * dx) / (double) (outerRadiusXZ * outerRadiusXZ)
                            + (dy * dy) / (double) (outerRadiusY * outerRadiusY)
                            + (dz * dz) / (double) (outerRadiusXZ * outerRadiusXZ);
                    if (outerNorm > 1.0D) {
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

                    double innerNorm = (dx * dx) / (double) (radiusXZ * radiusXZ)
                            + (dy * dy) / (double) (radiusY * radiusY)
                            + (dz * dz) / (double) (radiusXZ * radiusXZ);

                    if (innerNorm <= 1.0D) {
                        if (dy <= 0) {
                            float oreChance = dy <= -1 ? 0.22f : 0.12f;

                            if (random.nextFloat() < oreChance) {
                                this.setBlock(level, pos, ModBlocks.ABYSSAL_IRON_ORE.get().defaultBlockState());
                            } else {
                                this.setBlock(level, pos, ModFluids.LIQUID_SOURCE.get().defaultFluidState().createLegacyBlock());
                            }
                        } else {
                            this.setBlock(level, pos, Blocks.AIR.defaultBlockState());
                        }
                        placedAny = true;
                    } else {
                        BlockState shell = random.nextFloat() < 0.2f
                                ? Blocks.COBBLED_DEEPSLATE.defaultBlockState()
                                : Blocks.DEEPSLATE.defaultBlockState();
                        this.setBlock(level, pos, shell);
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
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.WATER);
    }
}
