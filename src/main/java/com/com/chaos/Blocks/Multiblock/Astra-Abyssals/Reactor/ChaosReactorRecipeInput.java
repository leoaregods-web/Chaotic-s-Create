package com.com.chaos.Blocks.Multiblock.Reactor;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;

/** Snapshot of both input crystals' buffers, fed to RecipeManager.getRecipeFor each tick. */
public record ChaosReactorRecipeInput(
    ItemStack leftItem, FluidStack leftFluid,
    ItemStack rightItem, FluidStack rightFluid
) implements RecipeInput {

    @Override
    public ItemStack getItem(int index) {
        return index == 0 ? leftItem : rightItem;
    }

    @Override
    public int size() {
        return 2;
    }
}
