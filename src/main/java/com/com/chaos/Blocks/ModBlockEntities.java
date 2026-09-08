package com.com.chaos.Blocks;

import com.com.chaos.Blocks.FluidReplicator.FluidReplicatorBlockEntity;
import com.com.chaos.Blocks.Multiblock.GaseousConverter.ChaosTurbineBlock;
import com.com.chaos.Blocks.Multiblock.GaseousConverter.ChaosTurbineBlockEntity;
import com.com.chaos.Blocks.Multiblock.Reactor.ChaosCrystalBlockEntity;
import com.com.chaos.ChaoticsCreate;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ChaoticsCreate.MODID);

    public static final Supplier<BlockEntityType<AtmosphereLiquifierBlockEntity>> ATMOSPHERE_LIQUIFIER =
            BLOCK_ENTITY_TYPES.register("atmosphere_liquifier",
                    () -> BlockEntityType.Builder.of(
                            AtmosphereLiquifierBlockEntity::new,
                            ModBlocks.ATMOSPHERE_LIQUIFIER.get()
                    ).build(null));
    public static final Supplier<BlockEntityType<CapsuleConcentratorBlockEntity>> CAPSULE_CONCENTRATOR =
            BLOCK_ENTITY_TYPES.register("capsule_concentrator",
                    () -> BlockEntityType.Builder.of(
                            CapsuleConcentratorBlockEntity::new,
                            ModBlocks.CAPSULE_CONCENTRATOR.get()
                    ).build(null));
    public static final Supplier<BlockEntityType<ProcessorInscriberBlockEntity>> PROCESSOR_INSCRIBER =
            BLOCK_ENTITY_TYPES.register("processor_inscriber",
                    () -> BlockEntityType.Builder.of(
                            ProcessorInscriberBlockEntity::new,
                            ModBlocks.PROCESSOR_INSCRIBER.get()
                    ).build(null));
    public static final Supplier<BlockEntityType<ChaosTurbineBlockEntity>> CHAOS_TURBINE =
            BLOCK_ENTITY_TYPES.register("chaos_turbine",
                    () -> BlockEntityType.Builder.of(
                            ChaosTurbineBlockEntity::new,
                            ModBlocks.CHAOS_TURBINE.get()
                    ).build(null));
    public static final Supplier<BlockEntityType<FluidReplicatorBlockEntity>> FLUID_REPLICATOR =
            BLOCK_ENTITY_TYPES.register("fluid_replicator",
                    () -> BlockEntityType.Builder.of(
                            FluidReplicatorBlockEntity::new,
                            ModBlocks.FLUID_REPLICATOR.get()
                    ).build(null));

    public static final Supplier<BlockEntityType<AstraAbyssGeneratorBlockEntity>> ASTRA_ABYSS_GENERATOR =
            BLOCK_ENTITY_TYPES.register("astra-abyss_generator",
                    () -> BlockEntityType.Builder.of(
                            AstraAbyssGeneratorBlockEntity::new,
                            ModBlocks.ASTRA_ABYSS_GENERATOR.get()
                    ).build(null));
    public static final Supplier<BlockEntityType<AbyssalOreCrusherBlockEntity>> ABYSSAL_ORE_CRUSHER =
            BLOCK_ENTITY_TYPES.register("abyssal_ore_crusher",
                    () -> BlockEntityType.Builder.of(
                            AbyssalOreCrusherBlockEntity::new,
                            ModBlocks.ABYSSAL_ORE_CRUSHER.get()
                    ).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChaosCrystalBlockEntity>> CHAOS_CRYSTAL =
            BLOCK_ENTITY_TYPES.register("chaos_crystal",
                    () -> BlockEntityType.Builder.of(ChaosCrystalBlockEntity::new, ModBlocks.CHAOS_CRYSTALS.get()).build(null));

    public static void register(IEventBus modBus) {
        BLOCK_ENTITY_TYPES.register(modBus);
    }
}
