package com.com.chaos.Integration.JEI;

import com.com.chaos.Blocks.ModBlocks;
import com.com.chaos.ChaoticsCreate;
import com.com.chaos.Integration.JEI.ProcessorInscriberJeiRecipe; // Ensure your package path here matches exactly
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

public class ProcessorInscriberRecipeCategory implements IRecipeCategory<ProcessorInscriberJeiRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(ChaoticsCreate.MODID, "processor_inscribing");
    public static final RecipeType<ProcessorInscriberJeiRecipe> TYPE = RecipeType.create(ChaoticsCreate.MODID, "processor_inscribing", ProcessorInscriberJeiRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public ProcessorInscriberRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(120, 38);
        // FIX: Replaced CAPSULE_CONCENTRATOR with PROCESSOR_INSCRIBER so JEI maps it to the right machine block
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.PROCESSOR_INSCRIBER.get()));
    }

    @Override
    public RecipeType<ProcessorInscriberJeiRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.chaoticscreate.processor_inscribing");
    }

    // FIX: Added the mandatory background getter required by modern JEI
    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ProcessorInscriberJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 11)
                .addItemStack(recipe.input());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 92, 11)
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(ProcessorInscriberJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        guiGraphics.drawString(minecraft.font, Component.literal(recipe.processTime() / 20 + "s"), 51, 2, 0x808080, false);

        // Simple progress arrow
        guiGraphics.fill(34, 18, 80, 20, 0xFF8A8A8A);
        guiGraphics.fill(74, 14, 80, 24, 0xFF8A8A8A);

        // Small flame indicator
        guiGraphics.fill(50, 26, 54, 30, 0xFFFFA000);
    }
}
