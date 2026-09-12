package com.com.chaos.Tabs;

import com.com.chaos.Blocks.ModBlocks;
import com.com.chaos.ChaoticsCreate;
import com.com.chaos.Fluids.ModFluids;
import com.com.chaos.Items.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ChaoticsCreate.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CHAOTICS_CREATE_TAB =
            CREATIVE_MODE_TABS.register("chaotics_create", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + ChaoticsCreate.MODID + ".chaotics_create"))
                    .icon(() -> new ItemStack(ModFluids.SPACE_BUCKET.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModFluids.SPACE_BUCKET.get());
                        output.accept(ModFluids.LIQUID_BUCKET.get());
                        output.accept(ModBlocks.ASTRAL_CASING_ITEM.get());
                        output.accept(ModBlocks.ATMOSPHERE_LIQUIFIER_ITEM.get());
                        output.accept(ModItems.ABYSS_ALLOY.get());
                        output.accept(ModItems.ASTRAL_ALLOY.get());
                        output.accept(ModItems.RAW_MYTHRIL.get());
                        output.accept(ModItems.MYTHRIL_INGOT.get());
                        output.accept(ModItems.RAW_ABYSSAL_IRON.get());
                        output.accept(ModItems.ABYSSAL_IRON_INGOT.get());
                        output.accept(ModBlocks.MYTHRIL_ORE_ITEM.get());
                        output.accept(ModBlocks.ABYSSAL_IRON_ORE_ITEM.get());
                        output.accept(ModBlocks.ABYSS_CASING_ITEM.get());
                        output.accept(ModBlocks.CAPSULE_CONCENTRATOR_ITEM.get());
                        output.accept(ModItems.ASTRA_ABYSS_INGOT.get());
                        output.accept(ModItems.RUPTURED_SMALL_CAPSULE.get());
                        output.accept(ModItems.RUPTURED_LARGE_CAPSULE.get());
                        output.accept(ModItems.RUPTURED_CAPSULE.get());
                        output.accept(ModItems.BASIC_COIL.get());
                        output.accept(ModItems.ADVANCED_COIL.get());
                        output.accept(ModItems.ELITE_COIL.get());
                        output.accept(ModItems.BASIC_PROCESSOR.get());
                        output.accept(ModItems.ADVANCED_PROCESSOR.get());
                        output.accept(ModItems.ELITE_PROCESSOR.get());
                        output.accept(ModItems.CHAOS_PROCESSOR.get());
                        output.accept(ModItems.MYTHRIL_GEAR.get());
                        output.accept(ModItems.ASTRA_ABYSS_GEAR.get());
                        output.accept(ModItems.ABYSSAL_IRON_GEAR.get());
                        output.accept(ModItems.MYTHRIL_NUGGET.get());
                        output.accept(ModItems.ASTRA_ABYSS_NUGGET.get());
                        output.accept(ModItems.ABYSSAL_IRON_NUGGET.get());
                        output.accept(ModItems.OXYGENATED_COAL.get());
                        output.accept(ModBlocks.ASTRA_ABYSS_GENERATOR_ITEM.get());
                        output.accept(ModBlocks.PROCESSOR_INSCRIBER_ITEM.get());
                        output.accept(ModItems.ASTRAL_ABYSS_ALLOY.get());
                        output.accept(ModBlocks.ASTRAL_ABYSS_CASING_ITEM.get());
                        output.accept(ModBlocks.ABYSSAL_ORE_CRUSHER_ITEM.get());
                        output.accept(ModBlocks.CHAOS_CRYSTALS_ITEM.get());
                        output.accept(ModBlocks.CHAOS_TURBINE_ITEM.get());
                        output.accept(ModBlocks.PRESSURIZED_ABYSSAL_IRON_BLOCK_ITEM.get());
                        output.accept(ModItems.SHARD_OF_CHAOS.get());
                        output.accept(ModBlocks.NEXUS_CONTROLLER_ITEM.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
