package com.com.chaos.Recipes;

import com.simibubi.create.content.kinetics.crusher.AbstractCrushingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;

public class AbyssalCrushingRecipe extends AbstractCrushingRecipe {

    public AbyssalCrushingRecipe(ProcessingRecipeParams params) {
        super(ModRecipes.ABYSSALCRUSHING, params);
    }

    @Override
    public boolean matches(RecipeInput inv, Level worldIn) {
        if (inv.isEmpty())
            return false;
        return ingredients.get(0).test(inv.getItem(0));
    }

    @Override
    protected int getMaxOutputCount() {
        return 4; // or lower, if you want your crusher capped tighter than vanilla crushing wheels
    }
}