package com.com.chaos.Recipes;

import com.com.chaos.Blocks.FluidReplicator.FluidReplicatorBlock;
import com.com.chaos.Blocks.FluidReplicator.FluidReplicatorBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Everything needed for the "print a real fluid" fallback described in the integration README:
 * a block + block entity that takes Matter in (over Replication's own Matter Pipes, via the
 * IMatterHandler capability - see ModCapabilities snippet in the README) and outputs a genuine
 * NeoForge FluidStack that Create's fluid pipes, pumps and buckets can all use normally.
 * <p>
 * Wire {@link #register} into ChaoticsCreate's constructor next to the other register() calls.
 */
public class ModFluidReplicatorRegistry {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, "chaoticscreate");
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, "chaoticscreate");
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "chaoticscreate");
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, "chaoticscreate");
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, "chaoticscreate");

    public static final DeferredHolder<Block, FluidReplicatorBlock> FLUID_REPLICATOR_BLOCK =
            BLOCKS.register(
                    "fluid_replicator",
                    () -> new FluidReplicatorBlock(
                            BlockBehaviour.Properties.of()
                                    .strength(1.5f, 6.0f)
                                    .noOcclusion()
                                    .sound(SoundType.METAL)
                    )
            );

    public static final DeferredHolder<Item, BlockItem> FLUID_REPLICATOR_ITEM =
            ITEMS.register("fluid_replicator", () -> new BlockItem(FLUID_REPLICATOR_BLOCK.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidReplicatorBlockEntity>> FLUID_REPLICATOR_BE =
            BLOCK_ENTITIES.register("fluid_replicator",
                    () -> BlockEntityType.Builder.of(FluidReplicatorBlockEntity::new, FLUID_REPLICATOR_BLOCK.get()).build(null));

    public static final DeferredHolder<RecipeType<?>, RecipeType<FluidMatterValueRecipe>> FLUID_MATTER_VALUE_RECIPE_TYPE =
            RECIPE_TYPES.register("fluid_matter_value",
                    () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath("chaoticscreate", "fluid_matter_value")));

    public static final DeferredHolder<RecipeSerializer<?>, FluidMatterValueRecipe.Serializer> FLUID_MATTER_VALUE_SERIALIZER =
            RECIPE_SERIALIZERS.register("fluid_matter_value", FluidMatterValueRecipe.Serializer::new);

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        RECIPE_TYPES.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
    }
}
