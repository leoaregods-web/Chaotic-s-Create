package com.com.chaos;

import com.com.chaos.Blocks.ModBlocks;
import com.com.chaos.Fluids.ModFluids;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(value = ChaoticsCreate.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ChaoticsCreate.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ChaoticsCreateClient {
    private static final Logger log = LoggerFactory.getLogger(ChaoticsCreateClient.class);

    public ChaoticsCreateClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModFluids.LIQUID_SOURCE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.LIQUID_FLOWING.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.SPACE_SOURCE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.SPACE_FLOWING.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.CHAOS_CRYSTALS.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ABYSSAL_ORE_CRUSHER.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.PROCESSOR_INSCRIBER.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.CAPSULE_CONCENTRATOR.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ASTRA_ABYSS_GENERATOR.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ATMOSPHERE_LIQUIFIER.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.CHAOS_TURBINE.get(), RenderType.translucent());
        });
    }
}
