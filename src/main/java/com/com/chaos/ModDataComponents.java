package com.com.chaos;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, "chaoticscreate");

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SimpleFluidContent>> GAS_CONTENT =
            DATA_COMPONENTS.register("gas_content", () -> DataComponentType.<SimpleFluidContent>builder()
                    .persistent(SimpleFluidContent.CODEC)
                    .networkSynchronized(SimpleFluidContent.STREAM_CODEC)
                    .cacheEncoding()
                    .build());

    public static void register(IEventBus modBus) {
        DATA_COMPONENTS.register(modBus);
    }
}