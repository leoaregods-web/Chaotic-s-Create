package com.com.chaos.Integration.JEI;

import net.minecraft.world.item.ItemStack;

public record ProcessorInscriberJeiRecipe(ItemStack input, ItemStack output, int processTime, int requiredFE) {
}