package com.com.chaos.Items;

import com.com.chaos.Blocks.ModBlocks;
import com.com.chaos.ChaoticsCreate;
import com.simibubi.create.api.data.datamaps.BlazeBurnerFuel;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.datamaps.builtin.FurnaceFuel;

import java.util.function.Supplier;

public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ChaoticsCreate.MODID);

    public static final Supplier<Item> SMALL_CAPSULE =
            ITEMS.register("small_capsule", () -> new GasCapsuleItem(new Item.Properties(), 250));

    public static final Supplier<Item> CAPSULE =
            ITEMS.register("capsule", () -> new GasCapsuleItem(new Item.Properties(), 1000));

    public static final Supplier<Item> LARGE_CAPSULE =
            ITEMS.register("large_capsule", () -> new GasCapsuleItem(new Item.Properties(), 4000));

    public static final Supplier<Item> REINFORCED_SMALL_CAPSULE =
            ITEMS.register("reinforced_small_capsule", () -> new GasCapsuleItem(new Item.Properties(), 500));

    public static final Supplier<Item> REINFORCED_CAPSULE =
            ITEMS.register("reinforced_capsule", () -> new GasCapsuleItem(new Item.Properties(), 2000));

    public static final Supplier<Item> REINFORCED_LARGE_CAPSULE =
            ITEMS.register("reinforced_large_capsule", () -> new GasCapsuleItem(new Item.Properties(), 8000));

    public static final Supplier<Item> ASTRAL_ALLOY =
            ITEMS.registerSimpleItem("astral_alloy", new Item.Properties());

    public static final Supplier<Item> ASTRAL_ABYSS_ALLOY =
            ITEMS.registerSimpleItem("astral-abyss_alloy", new Item.Properties());

    public static final Supplier<Item> ABYSS_ALLOY =
            ITEMS.registerSimpleItem("abyss_alloy", new Item.Properties());

    public static final Supplier<Item> RAW_MYTHRIL =
            ITEMS.registerSimpleItem("raw_mythril", new Item.Properties());

    public static final Supplier<Item> MYTHRIL_INGOT =
            ITEMS.registerSimpleItem("mythril_ingot", new Item.Properties());

    public static final Supplier<Item> RAW_ABYSSAL_IRON =
            ITEMS.registerSimpleItem("raw_abyssal_iron", new Item.Properties());

    public static final Supplier<Item> ABYSSAL_IRON_INGOT =
            ITEMS.registerSimpleItem("abyssal_iron_ingot", new Item.Properties());

    public static final Supplier<Item> ASTRA_ABYSS_INGOT =
            ITEMS.registerSimpleItem("astra-abyss_ingot", new Item.Properties());

    public static final Supplier<Item> RUPTURED_SMALL_CAPSULE =
            ITEMS.register("ruptured_small_capsule",
                    () -> new Item(new Item.Properties()));

    public static final Supplier<Item> RUPTURED_CAPSULE =
            ITEMS.register("ruptured_capsule",
                    () -> new Item(new Item.Properties()));

    public static final Supplier<Item> BASIC_COIL =
            ITEMS.registerSimpleItem("basic_coil", new Item.Properties());

    public static final Supplier<Item> ADVANCED_COIL =
            ITEMS.registerSimpleItem("advanced_coil", new Item.Properties());

    public static final Supplier<Item> ELITE_COIL =
            ITEMS.registerSimpleItem("elite_coil", new Item.Properties());

    public static final Supplier<Item> BASIC_PROCESSOR =
            ITEMS.register("basic_processor",
                    () -> new BlockItem(ModBlocks.BASIC_PROCESSOR_UPGRADE.get(), new Item.Properties()));

    public static final Supplier<Item> ADVANCED_PROCESSOR =
            ITEMS.register("advanced_processor",
                    () -> new BlockItem(ModBlocks.ADVANCED_PROCESSOR_UPGRADE.get(), new Item.Properties()));

    public static final Supplier<Item> ELITE_PROCESSOR =
            ITEMS.register("elite_processor",
                    () -> new BlockItem(ModBlocks.ELITE_PROCESSOR_UPGRADE.get(), new Item.Properties()));

    public static final Supplier<Item> CHAOS_PROCESSOR =
            ITEMS.register("chaos_processor",
                    () -> new BlockItem(ModBlocks.CHAOS_PROCESSOR_UPGRADE.get(), new Item.Properties()));


    public static final Supplier<Item> MYTHRIL_GEAR =
            ITEMS.registerSimpleItem("mythril_gear", new Item.Properties());

    public static final Supplier<Item> ASTRA_ABYSS_GEAR =
            ITEMS.registerSimpleItem("astra-abyss_gear", new Item.Properties());

    public static final Supplier<Item> ABYSSAL_IRON_GEAR =
            ITEMS.registerSimpleItem("abyssal_iron_gear", new Item.Properties());

    public static final Supplier<Item> MYTHRIL_NUGGET =
            ITEMS.registerSimpleItem("mythril_nugget", new Item.Properties());

    public static final Supplier<Item> ASTRA_ABYSS_NUGGET =
            ITEMS.registerSimpleItem("astra-abyss_nugget", new Item.Properties());

    public static final Supplier<Item> ABYSSAL_IRON_NUGGET =
            ITEMS.registerSimpleItem("abyssal_iron_nugget", new Item.Properties());

    public static final DeferredItem<Item> OXYGENATED_COAL = ITEMS.register("oxygenated_coal", () -> new OxygenatedCoal(new Item.Properties(), 3200));

    public static final Supplier<Item> RUPTURED_LARGE_CAPSULE =
            ITEMS.register("ruptured_large_capsule",
                    () -> new Item(new Item.Properties()));

    public static final Supplier<Item> EMPTY_CAPSULE =
            ITEMS.register("empty_capsule",
                    () -> new Item(new Item.Properties()));

    public static final Supplier<Item> SHARD_OF_CHAOS =
            ITEMS.register("shard_of_chaos",
                    () -> new ThrowableCapsuleItem(new Item.Properties().rarity(Rarity.EPIC).stacksTo(8)));

    public static final Supplier<Item> UNFORGED_ASTRA_ABYSS_INGOT =
            ITEMS.registerSimpleItem("unforged_astra-abyss_ingot", new Item.Properties());

    public static final Supplier<Item> INCOMPLETE_BUCKET_OF_LIQUID_SPACE =
            ITEMS.register("incomplete_bucket_of_liquid_space",
                    () -> new SequencedAssemblyItem(new Item.Properties()));

    public static void register(IEventBus modbus) {
        ITEMS.register(modbus);
    }
}