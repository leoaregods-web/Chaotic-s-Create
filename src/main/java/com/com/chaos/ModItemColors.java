package com.com.chaos;

import com.com.chaos.Items.ModItems;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

public class ModItemColors {

    private static final int EMPTY_COLOR = 0x0; // neutral gray shown when a capsule holds nothing

    public static void register(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> {
                    if (tintIndex != 1) {
                        return -1;
                    }
                    SimpleFluidContent content = stack.getOrDefault(ModDataComponents.GAS_CONTENT.get(), SimpleFluidContent.EMPTY);
                    FluidStack fluidStack = content.copy();
                    if (fluidStack.isEmpty()) {
                        return EMPTY_COLOR;
                    }
                    Fluid fluid = fluidStack.getFluid();
                    return IClientFluidTypeExtensions.of(fluid).getTintColor(fluidStack);
                },
                ModItems.SMALL_CAPSULE.get(),
                ModItems.CAPSULE.get(),
                ModItems.LARGE_CAPSULE.get(),
                ModItems.REINFORCED_SMALL_CAPSULE.get(),
                ModItems.REINFORCED_CAPSULE.get(),
                ModItems.REINFORCED_LARGE_CAPSULE.get());
    }
}