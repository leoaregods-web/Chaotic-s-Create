package com.com.chaos.Recipes;

import com.buuz135.replication.calculation.MatterValue;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

import java.util.List;

/**
 * Explicit "print this fluid for this much matter" override, for the Fluid Replicator. Modelled 1:1 on
 * Replication's own {@code replication:matter_value} recipe, just aimed at a Fluid + amount (per 1000mB
 * / one bucket) instead of an Item. The Fluid Replicator checks these first and falls back to
 * {@link com.com.chaos.integration.replication.CreateMatterCalculationBridge#FLUID_MATTER} (the value it
 * derived automatically from Create's Mixing/Filling/Emptying recipes) when no override exists, so you
 * only ever need one of these for a fluid that the bridge can't work out on its own, or to hand-tune a
 * cost for balance.
 * <p>
 * Not registered as a Recipe<CraftingInput> that ever "crafts" anything (matches() always returns
 * false) - like MatterValueRecipe, it exists purely to be data-driven and looked up by id/fluid.
 * <p>
 * Example datapack json, data/chaoticscreate/recipe/fluid_matter_values/molten_iron.json:
 * <pre>
 * {
 *   "type": "chaoticscreate:fluid_matter_value",
 *   "fluid": "createmetalwork:molten_iron",
 *   "amount_mb": 1000,
 *   "matter": [ { "type": "replication:metallic", "amount": 12.0 } ]
 * }
 * </pre>
 */
public class FluidMatterValueRecipe implements Recipe<CraftingInput> {

    public static final MapCodec<FluidMatterValueRecipe> CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
            BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(r -> r.fluid),
            Codec.INT.optionalFieldOf("amount_mb", 1000).forGetter(r -> r.amountMb),
            MatterValue.CODEC.listOf().fieldOf("matter").forGetter(r -> r.matter)
    ).apply(in, FluidMatterValueRecipe::new));

    // MatterValue only ships a Codec (JSON), not a StreamCodec - ByteBufCodecs.fromCodec adapts any
    // Codec<T> into a StreamCodec<ByteBuf, T> generically (it round-trips through NBT), which is all we
    // need here since MatterValue's list is tiny and only sent when the recipe cache syncs to a client.
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidMatterValueRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.FLUID), r -> r.fluid,
            ByteBufCodecs.VAR_INT, r -> r.amountMb,
            ByteBufCodecs.fromCodec(MatterValue.CODEC.listOf()), r -> r.matter,
            FluidMatterValueRecipe::new);

    public final Fluid fluid;
    public final int amountMb;
    public final List<MatterValue> matter;

    public FluidMatterValueRecipe(Fluid fluid, int amountMb, List<MatterValue> matter) {
        this.fluid = fluid;
        this.amountMb = amountMb;
        this.matter = matter;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModFluidReplicatorRegistry.FLUID_MATTER_VALUE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModFluidReplicatorRegistry.FLUID_MATTER_VALUE_RECIPE_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<FluidMatterValueRecipe> {
        @Override
        public MapCodec<FluidMatterValueRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, FluidMatterValueRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
