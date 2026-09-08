package com.com.chaos.Features;

import com.com.chaos.Blocks.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.LootTable;

public class SolidMeteoriteFeature extends Feature<NoneFeatureConfiguration> {

    private static final ResourceKey<LootTable> CORE_LOOT_TABLE = ResourceKey.create(
            Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath("chaoticscreate", "chests/solid_meteor")
    );

    public SolidMeteoriteFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        int radius = 6 + random.nextInt(3); // 6-8, bigger and denser than the liquid meteorite
        int radiusSq = radius * radius;
        int veinRadius = radius - 1;
        int veinRadiusSq = veinRadius * veinRadius;

        if (origin.getY() < level.getMinBuildHeight() + radius + 1
                || origin.getY() > level.getMaxBuildHeight() - radius - 1) {
            return false;
        }

        boolean foundReplaceable = false;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        outer:
        for (int dx = -radius; dx <= radius; ++dx) {
            for (int dy = -radius; dy <= radius; ++dy) {
                for (int dz = -radius; dz <= radius; ++dz) {
                    if (dx * dx + dy * dy + dz * dz > radiusSq) continue;
                    pos.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    if (!level.isOutsideBuildHeight(pos.getY()) && canReplace(level.getBlockState((BlockPos) pos))) {
                        foundReplaceable = true;
                        break outer;
                    }
                }
            }
        }
        if (!foundReplaceable) {
            return false;
        }

        boolean placedAny = false;
        for (int dx = -radius; dx <= radius; ++dx) {
            for (int dy = -radius; dy <= radius; ++dy) {
                for (int dz = -radius; dz <= radius; ++dz) {
                    int distanceSq = dx * dx + dy * dy + dz * dz;
                    if (distanceSq > radiusSq) continue;
                    pos.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    if (level.isOutsideBuildHeight(pos.getY())) continue;

                    BlockState current = level.getBlockState((BlockPos) pos);
                    if (!canReplace(current) && !current.isAir()) continue;

                    if (distanceSq > veinRadiusSq) {
                        // Scorched crust shell
                        BlockState shell = random.nextFloat() < 0.15f
                                ? Blocks.MAGMA_BLOCK.defaultBlockState()
                                : (random.nextBoolean() ? Blocks.BASALT.defaultBlockState() : Blocks.SMOOTH_BASALT.defaultBlockState());
                        this.setBlock((LevelWriter) level, (BlockPos) pos, shell);
                        placedAny = true;
                        continue;
                    }

                    // Dense interior: solid, no hollow pocket, heavily veined with Mythril Ore
                    BlockState fill = random.nextFloat() < 0.30f
                            ? ((Block) ModBlocks.MYTHRIL_ORE.get()).defaultBlockState()
                            : Blocks.DEEPSLATE.defaultBlockState();
                    this.setBlock((LevelWriter) level, (BlockPos) pos, fill);
                    placedAny = true;
                }
            }
        }

        // Sealed core chamber: a single hollow space at dead center with the reward chest
        placeCoreChamber(level, origin, random);

        return placedAny;
    }

    private void placeCoreChamber(WorldGenLevel level, BlockPos origin, RandomSource random) {
        this.setBlock((LevelWriter) level, origin, Blocks.AIR.defaultBlockState());
        this.setBlock((LevelWriter) level, origin.above(), Blocks.AIR.defaultBlockState());
        this.setBlock((LevelWriter) level, origin.below(), Blocks.CHEST.defaultBlockState());

        BlockEntity be = level.getBlockEntity(origin.below());
        if (be instanceof ChestBlockEntity chest) {
            chest.setLootTable(CORE_LOOT_TABLE, random.nextLong());
        }
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