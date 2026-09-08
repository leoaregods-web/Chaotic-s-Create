package com.com.chaos;

import com.com.chaos.Blocks.AbyssalOreCrusherRenderer;
import com.com.chaos.Blocks.CapsuleConcentratorRenderer;
import com.com.chaos.Blocks.ModBlockEntities;
import com.com.chaos.Blocks.Multiblock.GaseousConverter.ChaosTurbineRenderer;
import com.com.chaos.Blocks.Multiblock.Reactor.ChaosReactorContentsRenderer;
import com.com.chaos.Blocks.ProcessorInscriberRenderer;
import com.com.chaos.Entities.ChaosGolemEntity;
import com.com.chaos.Entities.Client.ChaosGolemCore;
import com.com.chaos.Entities.Client.ChaosGolemRenderer;
import com.com.chaos.Entities.ModEntities;
import com.com.chaos.Menu.AtmosphereLiquifierScreen;
import com.com.chaos.Menu.ModMenus;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = "chaoticscreate", bus = EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class ClientModEvents {

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        // PartialModel.of() gives Create/Flywheel the model location; the
        // standalone registration makes the intent explicit for NeoForge.
        event.register(ModelResourceLocation.standalone(ChaosTurbineRenderer.BLADES.modelLocation()));
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.ATMOSPHERE_LIQUIFIER_MENU.get(), AtmosphereLiquifierScreen::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ChaosGolemCore.LAYER_LOCATION, ChaosGolemCore::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.CAPSULE_CONCENTRATOR.get(), CapsuleConcentratorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PROCESSOR_INSCRIBER.get(), ProcessorInscriberRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ABYSSAL_ORE_CRUSHER.get(), AbyssalOreCrusherRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CHAOS_TURBINE.get(), ChaosTurbineRenderer::new);
        event.registerEntityRenderer((EntityType) ModEntities.THROWN_CAPSULE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer((EntityType<ChaosGolemEntity>) ModEntities.CHAOS_GOLEM.get(), ChaosGolemRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CHAOS_CRYSTAL.get(), ChaosReactorContentsRenderer::new);
    }
}
