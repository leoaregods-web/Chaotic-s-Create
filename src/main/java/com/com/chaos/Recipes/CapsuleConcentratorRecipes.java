package com.com.chaos.Recipes;

import com.com.chaos.Fluids.ModFluids;
import com.com.chaos.Items.ModItems;
import com.com.chaos.Integration.JEI.CapsuleConcentratorJeiRecipe;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class CapsuleConcentratorRecipes {
    public static final int PROCESS_TIME = 100; // 5 seconds
    private static final int OUTPUT_CAPACITY_MB = 250; // matches SMALL_CAPSULE's capacity

    private CapsuleConcentratorRecipes() {
    }

    public static boolean isValidInput(ItemStack stack) {
        return getResult(stack) != null;
    }

    /**
     * Concentrating repackages up to 250mB from any filled capsule into a
     * freshly-filled Small Capsule of the same gas. Returns null if the
     * input has no fluid handler or is empty of fluid.
     */
    public static @Nullable ItemStack getResult(ItemStack input) {
        IFluidHandlerItem handler = input.getCapability(Capabilities.FluidHandler.ITEM);
        if (handler == null) {
            return null;
        }

        FluidStack contained = handler.getFluidInTank(0);
        if (contained.isEmpty()) {
            return null;
        }

        int amount = Math.min(contained.getAmount(), OUTPUT_CAPACITY_MB);
        ItemStack result = new ItemStack(ModItems.SMALL_CAPSULE.get());
        IFluidHandlerItem resultHandler = result.getCapability(Capabilities.FluidHandler.ITEM);
        if (resultHandler == null) {
            return null;
        }
        resultHandler.fill(new FluidStack(contained.getFluid(), amount), IFluidHandler.FluidAction.EXECUTE);
        return resultHandler.getContainer();
    }

    public static List<CapsuleConcentratorJeiRecipe> getJeiRecipes() {
        return List.of(
                exampleRecipe(ModFluids.LIQUID_OXYGEN_SOURCE.get()),
                exampleRecipe(ModFluids.LIQUID_NITROGEN_SOURCE.get()),
                exampleRecipe(ModFluids.LIQUID_ARGON_SOURCE.get())
        );
    }

    private static CapsuleConcentratorJeiRecipe exampleRecipe(net.minecraft.world.level.material.Fluid gas) {
        ItemStack input = new ItemStack(ModItems.CAPSULE.get());
        IFluidHandlerItem inputHandler = input.getCapability(Capabilities.FluidHandler.ITEM);
        inputHandler.fill(new FluidStack(gas, 1000), IFluidHandler.FluidAction.EXECUTE);
        input = inputHandler.getContainer();

        ItemStack output = getResult(input);
        return new CapsuleConcentratorJeiRecipe(input, output, PROCESS_TIME);
    }
}