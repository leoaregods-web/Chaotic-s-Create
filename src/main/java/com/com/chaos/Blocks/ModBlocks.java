package com.com.chaos.Blocks;

import com.com.chaos.Blocks.FluidReplicator.FluidReplicatorBlock;
import com.com.chaos.Blocks.Multiblock.GaseousConverter.ChaosTurbineBlock;
import com.com.chaos.Blocks.Multiblock.Reactor.ChaosCrystalBlock;
import com.com.chaos.ChaoticsCreate;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(ChaoticsCreate.MODID);

    public static final DeferredRegister.Items BLOCKITEMS =
            DeferredRegister.createItems(ChaoticsCreate.MODID);

    public static final DeferredBlock<AstralCasing> ASTRAL_CASING =
            BLOCKS.registerBlock(
                    "astral_casing",
                    AstralCasing::new,
                    BlockBehaviour.Properties.of().strength(1.5f, 6.0f)
            );
    public static final DeferredBlock<AbyssCasing> ABYSS_CASING =
            BLOCKS.registerBlock(
                    "abyss_casing",
                    AbyssCasing::new,
                    BlockBehaviour.Properties.of().strength(1.5f, 6.0f)
            );
    public static final DeferredBlock<AstralAbyssCasing> ASTRAL_ABYSS_CASING =
            BLOCKS.registerBlock(
                    "astral-abyss_casing",
                    AstralAbyssCasing::new,
                    BlockBehaviour.Properties.of().strength(1.5f, 6.0f)
            );
    public static final DeferredBlock<PressurizedAbyssalIronBlock> PRESSURIZED_ABYSSAL_IRON_BLOCK =
            BLOCKS.registerBlock(
                "pressurized_abyssal_iron_block",
                    PressurizedAbyssalIronBlock::new,
                    BlockBehaviour.Properties.of().strength(1.5f, 12)
            );

    public static final DeferredBlock<AtmosphereLiquifierBlock> ATMOSPHERE_LIQUIFIER =
            BLOCKS.registerBlock("atmosphere_liquifier", AtmosphereLiquifierBlock::new, BlockBehaviour.Properties.of().strength(1.5f, 6.0f).noOcclusion().sound(SoundType.METAL));
    public static final DeferredBlock<ChaosTurbineBlock> CHAOS_TURBINE =
            BLOCKS.registerBlock("chaos_turbine", ChaosTurbineBlock::new, BlockBehaviour.Properties.of().strength(1.5f, 6.0f).noOcclusion().sound(SoundType.METAL));
    public static final DeferredBlock<FluidReplicatorBlock> FLUID_REPLICATOR =
            BLOCKS.registerBlock("fluid_replicator", FluidReplicatorBlock::new, BlockBehaviour.Properties.of().strength(1.5f, 6.0f).noOcclusion().sound(SoundType.METAL));
    public static final DeferredBlock<CapsuleConcentratorBlock> CAPSULE_CONCENTRATOR =
            BLOCKS.registerBlock("capsule_concentrator", CapsuleConcentratorBlock::new, BlockBehaviour.Properties.of().strength(1.5f, 6.0f).noOcclusion().sound(SoundType.METAL));
    public static final DeferredBlock<AstraAbyssGeneratorBlock> ASTRA_ABYSS_GENERATOR =
            BLOCKS.registerBlock("astra-abyss_generator", AstraAbyssGeneratorBlock::new, BlockBehaviour.Properties.of().strength(1.5f, 6.0f).noOcclusion().sound(SoundType.METAL));
    public static final DeferredBlock<ProcessorInscriberBlock> PROCESSOR_INSCRIBER =
            BLOCKS.registerBlock("processor_inscriber", ProcessorInscriberBlock::new, BlockBehaviour.Properties.of().strength(1.5f, 6.0f).noOcclusion().sound(SoundType.METAL));
    public static final DeferredBlock<AbyssalOreCrusherBlock> ABYSSAL_ORE_CRUSHER =
            BLOCKS.registerBlock("abyssal_ore_crusher", AbyssalOreCrusherBlock::new,
                    BlockBehaviour.Properties.of().strength(2.0f, 6.0f).noOcclusion().sound(SoundType.METAL));
    public static final DeferredBlock<ChaosCrystalBlock> CHAOS_CRYSTALS =
            BLOCKS.registerBlock("chaos_crystals", ChaosCrystalBlock::new,
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                            .strength(4.0f, 6.0f)
                            .sound(SoundType.AMETHYST)
                            .lightLevel(state -> 5));
    public static final DeferredBlock<Block> MYTHRIL_ORE =
            BLOCKS.registerBlock(
                    "mythril_ore",
                    Block::new,
                    BlockBehaviour.Properties.of().strength(2.5f, 4.0f).sound(SoundType.DEEPSLATE)
                            .requiresCorrectToolForDrops()
            );
    public static final DeferredBlock<Block> ABYSSAL_IRON_ORE =
            BLOCKS.registerBlock(
                    "abyssal_iron_ore",
                    Block::new,
                    BlockBehaviour.Properties.of().strength(3.0f, 5.0f).sound(SoundType.DEEPSLATE)
                            .requiresCorrectToolForDrops()
            );

    public static final DeferredItem<BlockItem> ASTRAL_CASING_ITEM =
            BLOCKITEMS.registerSimpleBlockItem(ASTRAL_CASING);
    public static final DeferredItem<BlockItem> ABYSS_CASING_ITEM =
            BLOCKITEMS.registerSimpleBlockItem(ABYSS_CASING);
    public static final DeferredItem<BlockItem> ATMOSPHERE_LIQUIFIER_ITEM =
            BLOCKITEMS.registerSimpleBlockItem(ATMOSPHERE_LIQUIFIER);
    public static final DeferredItem<BlockItem> CAPSULE_CONCENTRATOR_ITEM =
            BLOCKITEMS.registerSimpleBlockItem(CAPSULE_CONCENTRATOR);
    public static final DeferredItem<BlockItem> MYTHRIL_ORE_ITEM =
            BLOCKITEMS.registerSimpleBlockItem(MYTHRIL_ORE);
    public static final DeferredItem<BlockItem> ABYSSAL_IRON_ORE_ITEM =
            BLOCKITEMS.registerSimpleBlockItem(ABYSSAL_IRON_ORE);
    public static final DeferredItem<BlockItem> ASTRA_ABYSS_GENERATOR_ITEM =
            BLOCKITEMS.registerSimpleBlockItem(ASTRA_ABYSS_GENERATOR);
    public static final DeferredItem<BlockItem> PROCESSOR_INSCRIBER_ITEM =
            BLOCKITEMS.registerSimpleBlockItem(PROCESSOR_INSCRIBER);
    public static final DeferredItem<BlockItem> ASTRAL_ABYSS_CASING_ITEM =
            BLOCKITEMS.registerSimpleBlockItem(ASTRAL_ABYSS_CASING);
    public static final DeferredItem<BlockItem> ABYSSAL_ORE_CRUSHER_ITEM =
            BLOCKITEMS.registerSimpleBlockItem(ABYSSAL_ORE_CRUSHER);
    public static final DeferredItem<BlockItem> CHAOS_CRYSTALS_ITEM =
            BLOCKITEMS.registerSimpleBlockItem(CHAOS_CRYSTALS);
    public static final DeferredItem<BlockItem> CHAOS_TURBINE_ITEM =
            BLOCKITEMS.registerSimpleBlockItem(CHAOS_TURBINE);
    public static final DeferredItem<BlockItem> FLUID_REPLICATOR_ITEM =
            BLOCKITEMS.registerSimpleBlockItem(FLUID_REPLICATOR);
    public static final DeferredItem<BlockItem> PRESSURIZED_ABYSSAL_IRON_BLOCK_ITEM =
            BLOCKITEMS.registerSimpleBlockItem(PRESSURIZED_ABYSSAL_IRON_BLOCK);
    public static final DeferredBlock<BasicProcessorUpgradeBlock> BASIC_PROCESSOR_UPGRADE =
            BLOCKS.registerBlock("basic_processor_upgrade", BasicProcessorUpgradeBlock::new,
                    BlockBehaviour.Properties.of().noCollission().strength(0.5f).sound(SoundType.METAL));
    public static final DeferredBlock<AdvancedProcessorUpgradeBlock> ADVANCED_PROCESSOR_UPGRADE =
            BLOCKS.registerBlock("advanced_processor_upgrade", AdvancedProcessorUpgradeBlock::new,
                    BlockBehaviour.Properties.of().noCollission().strength(0.5f).sound(SoundType.METAL));
    public static final DeferredBlock<EliteProcessorUpgradeBlock> ELITE_PROCESSOR_UPGRADE =
            BLOCKS.registerBlock("elite_processor_upgrade", EliteProcessorUpgradeBlock::new,
                    BlockBehaviour.Properties.of().noCollission().strength(0.5f).sound(SoundType.METAL));
    public static final DeferredBlock<ChaosProcessorUpgradeBlock> CHAOS_PROCESSOR_UPGRADE =
            BLOCKS.registerBlock("chaos_processor_upgrade", ChaosProcessorUpgradeBlock::new,
                    BlockBehaviour.Properties.of().noCollission().strength(0.5f).sound(SoundType.METAL));

    public static void register(IEventBus modbus, IEventBus eventBus) {
        BLOCKS.register(modbus);
        BLOCKITEMS.register(modbus);
    }
}
