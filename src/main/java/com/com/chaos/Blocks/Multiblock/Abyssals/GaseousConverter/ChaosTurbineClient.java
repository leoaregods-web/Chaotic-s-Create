package com.com.chaos.Blocks.Multiblock.Abyssals.GaseousConverter;

import com.com.chaos.Blocks.ModBlockEntities;
import com.com.chaos.ChaoticsCreate;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(
        modid = ChaoticsCreate.MODID,
        bus = EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT)
public final class ChaosTurbineClient {
    private ChaosTurbineClient() {
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        // PartialModel.of() gives Create/Flywheel the model location; the
        // standalone registration makes the intent explicit for NeoForge.
        event.register(ModelResourceLocation.standalone(ChaosTurbineRenderer.BLADES.modelLocation()));
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                ModBlockEntities.CHAOS_TURBINE.get(),
                ChaosTurbineRenderer::new);
    }
}
