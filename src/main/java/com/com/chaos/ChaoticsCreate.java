package com.com.chaos;

import com.com.chaos.Blocks.ModBlockEntities;
import com.com.chaos.Blocks.ModBlocks;
import com.com.chaos.Blocks.Multiblock.Reactor.ModChaosReactorRecipes;
import com.com.chaos.Entities.ModEntities;
import com.com.chaos.Features.ModWorldgenFeatures;
import com.com.chaos.Fluids.ModFluids;
import com.com.chaos.Items.ModItems;
import com.com.chaos.Matter.ModMatterTypes;
import com.com.chaos.Menu.ModMenus;
import com.com.chaos.Recipes.ModRecipes;
import com.com.chaos.Tabs.ModCreativeTabs;
import com.com.chaos.datagen.DataGenerators;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;
import com.com.chaos.Integration.Replication.*;

@Mod(ChaoticsCreate.MODID)
public class ChaoticsCreate {
    public static final String MODID = "chaoticscreate";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ChaoticsCreate(IEventBus modbus, ModContainer container, FMLModContainer fmlContainer, Dist dist) {
        ModFluids.register(modbus);
        ModBlocks.register(modbus, NeoForge.EVENT_BUS);
        ModBlockEntities.register(modbus);
        ModEntities.register(modbus);
        ModItems.register(modbus);
        ModCreativeTabs.register(modbus);
        ModMenus.register(modbus);
        ModRecipes.register(modbus);
        ModWorldgenFeatures.register(modbus);
        modbus.addListener(DataGenerators::gatherData);
        ModDataComponents.register(modbus);
        ModChaosReactorRecipes.register(modbus);
        ModMatterTypes.register(modbus);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, (TagsUpdatedEvent e) -> {
            if (e.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD)
                CreateMatterCalculationBridge.recalculate(e.getRegistryAccess());
        });
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent e) ->
                CreateMatterCalculationBridge.recalculate(e.getServer().registryAccess()));
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
