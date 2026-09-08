package com.com.chaos.Integration.JEI;

import com.com.chaos.Blocks.ModBlocks;
import com.com.chaos.ChaoticsCreate;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class CapsuleConcentratorRecipeCategory implements IRecipeCategory<CapsuleConcentratorJeiRecipe> {
    public static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(ChaoticsCreate.MODID, "capsule_concentrating");

    public static final RecipeType<CapsuleConcentratorJeiRecipe> TYPE =
            RecipeType.create(ChaoticsCreate.MODID, "capsule_concentrating", CapsuleConcentratorJeiRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public CapsuleConcentratorRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(120, 38);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.CAPSULE_CONCENTRATOR.get()));
    }

    @Override
    public RecipeType<CapsuleConcentratorJeiRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.chaoticscreate.capsule_concentrating");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CapsuleConcentratorJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 11)
                .addItemStack(recipe.input());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 92, 11)
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(CapsuleConcentratorJeiRecipe recipe, IRecipeSlotsView recipeSlotsView,
                     GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Minecraft minecraft = Minecraft.getInstance();

        guiGraphics.drawString(minecraft.font, Component.literal(recipe.processTime() / 20 + "s"), 51, 2, 0x808080, false);

        // simple progress arrow
        guiGraphics.fill(34, 18, 80, 20, 0xFF8A8A8A);
        guiGraphics.fill(74, 14, 80, 24, 0xFF8A8A8A);

        // small flame indicator
        guiGraphics.fill(50, 26, 54, 30, 0xFFFFA000);
    }
}