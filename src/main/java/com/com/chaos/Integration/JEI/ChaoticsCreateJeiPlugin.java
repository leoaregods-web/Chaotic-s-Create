package com.com.chaos.Integration.JEI;

import com.com.chaos.Recipes.CapsuleConcentratorRecipes;
import com.com.chaos.Blocks.ModBlocks;
import com.com.chaos.ChaoticsCreate;
import com.com.chaos.Recipes.ProcessorInscriberRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class ChaoticsCreateJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_UID =
            ResourceLocation.fromNamespaceAndPath(ChaoticsCreate.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new CapsuleConcentratorRecipeCategory(registration.getJeiHelpers().getGuiHelper())
        );
        registration.addRecipeCategories(
                new ProcessorInscriberRecipeCategory(registration.getJeiHelpers().getGuiHelper())
        );
        registration.addRecipeCategories(new AbyssalCrusherRecipeCategory(
                registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(
                CapsuleConcentratorRecipeCategory.TYPE,
                CapsuleConcentratorRecipes.getJeiRecipes()
        );
        registration.addRecipes(
                ProcessorInscriberRecipeCategory.TYPE,
                ProcessorInscriberRecipes.getJeiRecipes()
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.CAPSULE_CONCENTRATOR.get()),
                CapsuleConcentratorRecipeCategory.TYPE
        );
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.PROCESSOR_INSCRIBER.get()),
                ProcessorInscriberRecipeCategory.TYPE
        );
        registration.addRecipeCatalyst(new ItemStack(
                ModBlocks.ABYSSAL_ORE_CRUSHER.get()), AbyssalCrusherRecipeCategory.TYPE
        );

    }
}