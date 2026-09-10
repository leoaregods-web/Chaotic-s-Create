package com.com.chaos.Blocks.Multiblock.AstraAbyssals.Reactor;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

/**
 * A Chaos Reactor recipe: two independent input requirements (left/right
 * crystal buffers) and a list of results that can mix plain items, a raw
 * fluid, and/or filled capsules.
 *
 * assemble()/getResultItem() only exist to satisfy the Recipe interface
 * (recipe book, JEI-style displays) - actual placement into the
 * controller's output slots + fluid tank happens in
 * ChaosCrystalBlockEntity.processRecipes(), since results can span two
 * item slots (plain items and/or filled capsules) plus a fluid tank at once.
 */
public record ChaosReactorRecipe(
    ReactorIngredient leftInput,
    ReactorIngredient rightInput,
    List<ReactorResult> results
) implements Recipe<ChaosReactorRecipeInput> {

    @Override
    public boolean matches(ChaosReactorRecipeInput input, Level level) {
        return leftInput.matches(input.leftItem(), input.leftFluid())
            && rightInput.matches(input.rightItem(), input.rightFluid());
    }

    @Override
    public ItemStack assemble(ChaosReactorRecipeInput input, HolderLookup.Provider registries) {
        return getResultItem(registries);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return results.stream().filter(ReactorResult::isItem).findFirst().map(ReactorResult::toItemStack)
            .or(() -> results.stream().filter(ReactorResult::isCapsule).findFirst().map(ReactorResult::toCapsuleStack))
            .orElse(ItemStack.EMPTY);
    }

    /** Plain (non-capsule) item results. */
    public List<ItemStack> getItemResults() {
        return results.stream().filter(ReactorResult::isItem).map(ReactorResult::toItemStack).toList();
    }

    /** Filled capsule stacks - already stamped with their GAS_CONTENT. */
    public List<ItemStack> getCapsuleResults() {
        return results.stream().filter(ReactorResult::isCapsule).map(ReactorResult::toCapsuleStack).toList();
    }

    public FluidStack getFluidResult() {
        return results.stream()
            .filter(ReactorResult::isFluid)
            .map(ReactorResult::toFluidStack)
            .findFirst()
            .orElse(FluidStack.EMPTY);
    }

    @Override
    public RecipeSerializer<? extends Recipe<ChaosReactorRecipeInput>> getSerializer() {
        return ModChaosReactorRecipes.CHAOS_REACTOR_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<ChaosReactorRecipeInput>> getType() {
        return ModChaosReactorRecipes.CHAOS_REACTOR_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<ChaosReactorRecipe> {
        public static final MapCodec<ChaosReactorRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ReactorIngredient.CODEC.fieldOf("left_input").forGetter(ChaosReactorRecipe::leftInput),
            ReactorIngredient.CODEC.fieldOf("right_input").forGetter(ChaosReactorRecipe::rightInput),
            ReactorResult.CODEC.listOf().fieldOf("results").forGetter(ChaosReactorRecipe::results)
        ).apply(inst, ChaosReactorRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ChaosReactorRecipe> STREAM_CODEC = StreamCodec.composite(
            ReactorIngredient.STREAM_CODEC, ChaosReactorRecipe::leftInput,
            ReactorIngredient.STREAM_CODEC, ChaosReactorRecipe::rightInput,
            ReactorResult.STREAM_CODEC.apply(ByteBufCodecs.list()), ChaosReactorRecipe::results,
            ChaosReactorRecipe::new
        );

        @Override
        public MapCodec<ChaosReactorRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ChaosReactorRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
