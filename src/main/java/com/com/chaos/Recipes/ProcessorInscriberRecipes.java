package com.com.chaos.Recipes;

import com.com.chaos.Integration.JEI.CapsuleConcentratorJeiRecipe;
import com.com.chaos.Integration.JEI.ProcessorInscriberJeiRecipe;
import com.com.chaos.Items.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ProcessorInscriberRecipes {
    public static final int PROCESS_TIME = 50;
    public static final int requiredFE = 100;

    private ProcessorInscriberRecipes() { }

    public static boolean isValidInput(ItemStack stack) {
        return getResult(stack.getItem()) != null;
    }

    public static @Nullable Item getResult(Item item) {
        if (item == ModItems.MYTHRIL_GEAR.get()) { return ModItems.BASIC_PROCESSOR.get(); }
        if (item == ModItems.ABYSSAL_IRON_GEAR.get()) { return ModItems.ADVANCED_PROCESSOR.get(); }
        if (item == ModItems.ASTRA_ABYSS_GEAR.get()) { return ModItems.ELITE_PROCESSOR.get(); }
        return null;
    }

    // ADD THIS METHOD: Look up the specific JEI recipe configuration matching the input item
    public static @Nullable ProcessorInscriberJeiRecipe getRecipeFor(Item inputItem) {
        for (ProcessorInscriberJeiRecipe recipe : getJeiRecipes()) {
            if (recipe.input().is(inputItem)) { // Assumes .input() returns an ItemStack
                return recipe;
            }
        }
        return null;
    }

    public static List<ProcessorInscriberJeiRecipe> getJeiRecipes() {
        return List.of(
                new ProcessorInscriberJeiRecipe( new ItemStack(ModItems.MYTHRIL_GEAR.get()), new ItemStack(ModItems.BASIC_PROCESSOR.get()), PROCESS_TIME, requiredFE ),
                new ProcessorInscriberJeiRecipe( new ItemStack(ModItems.ABYSSAL_IRON_GEAR.get()), new ItemStack(ModItems.ADVANCED_PROCESSOR.get()), PROCESS_TIME + 50, requiredFE + 50 ),
                new ProcessorInscriberJeiRecipe( new ItemStack(ModItems.ASTRA_ABYSS_GEAR.get()), new ItemStack(ModItems.ELITE_PROCESSOR.get()), PROCESS_TIME + 100, requiredFE + 100 )
        );
    }
}