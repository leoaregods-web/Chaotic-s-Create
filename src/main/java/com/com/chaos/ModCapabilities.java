package com.com.chaos;

import com.buuz135.replication.ReplicationRegistry;
import com.com.chaos.Blocks.ModBlockEntities;
import com.com.chaos.Blocks.Multiblock.AstraAbyssals.Reactor.ChaosCrystalBlockEntity;
import com.com.chaos.Items.ModItems;
import com.com.chaos.Recipes.ModFluidReplicatorRegistry;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;

@EventBusSubscriber(modid = ChaoticsCreate.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModCapabilities {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.CHAOS_CRYSTAL.get(),
                ChaosCrystalBlockEntity::getItemHandler);
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.CHAOS_CRYSTAL.get(),
                ChaosCrystalBlockEntity::getFluidHandler);
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.ATMOSPHERE_LIQUIFIER.get(),
                (blockEntity, side) -> blockEntity.getFluidHandler(side)
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.CAPSULE_CONCENTRATOR.get(),
                (blockEntity, side) -> blockEntity.getItemHandler(side)
        );
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.ASTRA_ABYSS_GENERATOR.get(),
                (blockEntity, side) -> blockEntity.getEnergyStorage()
        );
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.PROCESSOR_INSCRIBER.get(),
                (blockEntity, side) -> blockEntity.getEnergyStorage()
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.ABYSSAL_ORE_CRUSHER.get(),
                (blockEntity, side) -> blockEntity.getItemHandler(side)
        );
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.ABYSSAL_ORE_CRUSHER.get(),
                (blockEntity, side) -> blockEntity.getEnergyStorage()
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.PROCESSOR_INSCRIBER.get(),
                (blockEntity, side) -> blockEntity.getItemHandler((Direction) side));

        registerCapsule(event, ModItems.SMALL_CAPSULE, 250);
        registerCapsule(event, ModItems.CAPSULE, 1000);
        registerCapsule(event, ModItems.LARGE_CAPSULE, 4000);
        registerCapsule(event, ModItems.REINFORCED_SMALL_CAPSULE, 500);
        registerCapsule(event, ModItems.REINFORCED_CAPSULE, 2000);
        registerCapsule(event, ModItems.REINFORCED_LARGE_CAPSULE, 8000);
        event.registerBlockEntity(ReplicationRegistry.Capabilities.MATTER_HANDLER,
                ModFluidReplicatorRegistry.FLUID_REPLICATOR_BE.get(),
                (be, side) -> be.getMatterHandler());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                ModFluidReplicatorRegistry.FLUID_REPLICATOR_BE.get(),
                (be, side) -> be.getFluidHandler(side));
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.CHAOS_TURBINE.get(),
                (be, side) -> be.getFluidHandler(side));
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        ModItemColors.register(event);
    }

    private static void registerCapsule(RegisterCapabilitiesEvent event, java.util.function.Supplier<net.minecraft.world.item.Item> item, int capacity) {
        event.registerItem(Capabilities.FluidHandler.ITEM,
                (stack, ctx) -> new FluidHandlerItemStack(ModDataComponents.GAS_CONTENT, stack, capacity),
                item.get());
    }
}
