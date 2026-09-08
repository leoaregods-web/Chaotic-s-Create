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

public class AbyssalCrusherRecipeCategory implements IRecipeCategory<AbyssalCrusherJeiRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(ChaoticsCreate.MODID, "abyssal_crushing");
    public static final RecipeType<AbyssalCrusherJeiRecipe> TYPE = RecipeType.create(ChaoticsCreate.MODID, "abyssal_crushing", AbyssalCrusherJeiRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public AbyssalCrusherRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(120, 38);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.ABYSSAL_ORE_CRUSHER.get()));
    }

    @Override
    public RecipeType<AbyssalCrusherJeiRecipe> getRecipeType() { return TYPE; }

    @Override
    public Component getTitle() { return Component.translatable("jei.chaoticscreate.abyssal_crushing"); }

    @Override
    public IDrawable getBackground() { return background; }

    @Override
    public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AbyssalCrusherJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 11).addItemStack(recipe.input());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 92, 4).addItemStack(recipe.output());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 92, 24).addItemStack(recipe.bonusOutput());
    }

    @Override
    public void draw(AbyssalCrusherJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        guiGraphics.drawString(minecraft.font, Component.literal(recipe.processTime() / 20 + "s"), 51, 2, 0x808080, false);
        guiGraphics.fill(34, 18, 80, 20, 0xFF8A8A8A);
        guiGraphics.fill(74, 14, 80, 24, 0xFF8A8A8A);
    }
}