package com.com.chaos.Fluids;

import com.com.chaos.ChaoticsCreate;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid.Properties;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.joml.Vector3f;

public class ModFluids {
    private static final ResourceLocation VANILLA_WATER_STILL = ResourceLocation.withDefaultNamespace("block/water_still");
    private static final ResourceLocation VANILLA_WATER_FLOW = ResourceLocation.withDefaultNamespace("block/water_flow");

    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, ChaoticsCreate.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(BuiltInRegistries.FLUID, ChaoticsCreate.MODID);
    public static final DeferredRegister<Item> BUCKETS = DeferredRegister.createItems(ChaoticsCreate.MODID);
    public static final DeferredRegister<Block> SOURCEBLOCKS = DeferredRegister.createBlocks(ChaoticsCreate.MODID);

    public static final DeferredHolder<FluidType, FluidType> LIQUID_TYPE = FLUID_TYPES.register("deep_water", () -> new FluidType(FluidType.Properties.create().descriptionId("fluid.chaoticscreate.deep_water").motionScale(0.01D)));
    public static final DeferredHolder<Fluid, FlowingFluid> LIQUID_SOURCE = FLUIDS.register("deep_water_source", () -> new BaseFlowingFluid.Source(liquidProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> LIQUID_FLOWING = FLUIDS.register("deep_water_flowing", () -> new BaseFlowingFluid.Flowing(liquidProperties()));
    public static final DeferredHolder<Item, BucketItem> LIQUID_BUCKET = BUCKETS.register("bucket_of_deep_water", () -> new BucketItem(LIQUID_SOURCE.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final DeferredHolder<Block, LiquidBlock> LIQUID_BLOCK = SOURCEBLOCKS.register("deep_water", () -> new LiquidBlock(LIQUID_SOURCE.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER)));

    public static final DeferredHolder<FluidType, FluidType> SPACE_TYPE = FLUID_TYPES.register("liquid_space", () -> new FluidType(FluidType.Properties.create().descriptionId("fluid.chaoticscreate.liquid_space").motionScale(0.01D)));
    public static final DeferredHolder<Fluid, FlowingFluid> SPACE_SOURCE = FLUIDS.register("liquid_space_source", () -> new BaseFlowingFluid.Source(spaceProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> SPACE_FLOWING = FLUIDS.register("liquid_space_flowing", () -> new BaseFlowingFluid.Flowing(spaceProperties()));
    public static final DeferredHolder<Item, BucketItem> SPACE_BUCKET = BUCKETS.register("bucket_of_liquid_space", () -> new BucketItem(SPACE_SOURCE.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final DeferredHolder<Block, LiquidBlock> SPACE_BLOCK = SOURCEBLOCKS.register("liquid_space", () -> new LiquidBlock(SPACE_SOURCE.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER)));

    public static final DeferredHolder<FluidType, FluidType> LIQUID_OXYGEN_TYPE = FLUID_TYPES.register("liquid_oxygen", () -> new FluidType(FluidType.Properties.create().descriptionId("fluid.chaoticscreate.liquid_oxygen")));
    public static final DeferredHolder<Fluid, FlowingFluid> LIQUID_OXYGEN_SOURCE = FLUIDS.register("liquid_oxygen_source", () -> new BaseFlowingFluid.Source(liquidOxygenProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> LIQUID_OXYGEN_FLOWING = FLUIDS.register("liquid_oxygen_flowing", () -> new BaseFlowingFluid.Flowing(liquidOxygenProperties()));

    public static final DeferredHolder<FluidType, FluidType> LIQUID_CARBON_TYPE = FLUID_TYPES.register("liquid_carbon", () -> new FluidType(FluidType.Properties.create().descriptionId("fluid.chaoticscreate.liquid_carbon")));
    public static final DeferredHolder<Fluid, FlowingFluid> LIQUID_CARBON_SOURCE = FLUIDS.register("liquid_carbon_source", () -> new BaseFlowingFluid.Source(liquidCarbonProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> LIQUID_CARBON_FLOWING = FLUIDS.register("liquid_carbon_flowing", () -> new BaseFlowingFluid.Flowing(liquidCarbonProperties()));

    public static final DeferredHolder<FluidType, FluidType> LIQUID_NITROGEN_TYPE = FLUID_TYPES.register("liquid_nitrogen", () -> new FluidType(FluidType.Properties.create().descriptionId("fluid.chaoticscreate.liquid_nitrogen")));
    public static final DeferredHolder<Fluid, FlowingFluid> LIQUID_NITROGEN_SOURCE = FLUIDS.register("liquid_nitrogen_source", () -> new BaseFlowingFluid.Source(liquidNitrogenProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> LIQUID_NITROGEN_FLOWING = FLUIDS.register("liquid_nitrogen_flowing", () -> new BaseFlowingFluid.Flowing(liquidNitrogenProperties()));

    public static final DeferredHolder<FluidType, FluidType> LIQUID_ARGON_TYPE = FLUID_TYPES.register("liquid_argon", () -> new FluidType(FluidType.Properties.create().descriptionId("fluid.chaoticscreate.liquid_argon")));
    public static final DeferredHolder<Fluid, FlowingFluid> LIQUID_ARGON_SOURCE = FLUIDS.register("liquid_argon_source", () -> new BaseFlowingFluid.Source(liquidArgonProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> LIQUID_ARGON_FLOWING = FLUIDS.register("liquid_argon_flowing", () -> new BaseFlowingFluid.Flowing(liquidArgonProperties()));

    public static void register(IEventBus modbus) {
        FLUID_TYPES.register(modbus);
        FLUIDS.register(modbus);
        BUCKETS.register(modbus);
        SOURCEBLOCKS.register(modbus);
        modbus.addListener(ModFluids::clientExt);
    }

    private static final IClientFluidTypeExtensions liquidOxygenExt = createTintedFluidExt(0x996EB8FF);
    private static final IClientFluidTypeExtensions liquidNitrogenExt = createTintedFluidExt(0x99FFFFFF);
    private static final IClientFluidTypeExtensions liquidArgonExt = createTintedFluidExt(0x99C9A0FF);
    private static final IClientFluidTypeExtensions liquidCarbonExt = createTintedFluidExt(0x88000000);

    private static IClientFluidTypeExtensions createTintedFluidExt(int tint) {
        return new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return VANILLA_WATER_STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return VANILLA_WATER_FLOW;
            }

            @Override
            public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
                return tint;
            }

            @Override
            public int getTintColor(FluidStack stack) {
                return tint;
            }
        };
    }

    private static IClientFluidTypeExtensions createFoggyFluidExt(
            ResourceLocation still,
            ResourceLocation flow,
            float fogRed,
            float fogGreen,
            float fogBlue,
            float fogStart,
            float fogEnd) {
        return new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return still;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return flow;
            }

            @Override
            public Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
                return new Vector3f(fogRed, fogGreen, fogBlue);
            }

            @Override
            public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape) {
                RenderSystem.setShaderFogStart(fogStart);
                RenderSystem.setShaderFogEnd(Math.min(farDistance, fogEnd));
                RenderSystem.setShaderFogShape(FogShape.CYLINDER);
            }
        };
    }

    private static final ResourceLocation DEEP_WATER_STILL =
            ResourceLocation.fromNamespaceAndPath(ChaoticsCreate.MODID, "block/deep_water_still");
    private static final ResourceLocation DEEP_WATER_FLOWING =
            ResourceLocation.fromNamespaceAndPath(ChaoticsCreate.MODID, "block/deep_water_flowing");

    private static final ResourceLocation LIQUID_SPACE_STILL =
            ResourceLocation.fromNamespaceAndPath(ChaoticsCreate.MODID, "block/liquid_space_still");
    private static final ResourceLocation LIQUID_SPACE_FLOWING =
            ResourceLocation.fromNamespaceAndPath(ChaoticsCreate.MODID, "block/liquid_space_flowing");

    private static final IClientFluidTypeExtensions liquidExt =
            createFoggyFluidExt(DEEP_WATER_STILL, DEEP_WATER_FLOWING, 0.02f, 0.10f, 0.16f, 0.5f, 12.0f);

    private static final IClientFluidTypeExtensions spaceExt =
            createFoggyFluidExt(LIQUID_SPACE_STILL, LIQUID_SPACE_FLOWING, 0.10f, 0.04f, 0.18f, 0.25f, 5.0f);

    private static void clientExt(RegisterClientExtensionsEvent event) {
        event.registerFluidType(liquidExt, LIQUID_TYPE.get());
        event.registerFluidType(spaceExt, SPACE_TYPE.get());
        event.registerFluidType(liquidOxygenExt, LIQUID_OXYGEN_TYPE.get());
        event.registerFluidType(liquidNitrogenExt, LIQUID_NITROGEN_TYPE.get());
        event.registerFluidType(liquidArgonExt, LIQUID_ARGON_TYPE.get());
        event.registerFluidType(liquidCarbonExt, LIQUID_CARBON_TYPE.get());
    }

    private static Properties liquidProperties() {
        return new Properties(LIQUID_TYPE, LIQUID_SOURCE, LIQUID_FLOWING).bucket(LIQUID_BUCKET).block(LIQUID_BLOCK);
    }

    private static Properties spaceProperties() {
        return new Properties(SPACE_TYPE, SPACE_SOURCE, SPACE_FLOWING).bucket(SPACE_BUCKET).block(SPACE_BLOCK);
    }

    private static Properties liquidOxygenProperties() {
        return new Properties(LIQUID_OXYGEN_TYPE, LIQUID_OXYGEN_SOURCE, LIQUID_OXYGEN_FLOWING);
    }

    private static Properties liquidNitrogenProperties() {
        return new Properties(LIQUID_NITROGEN_TYPE, LIQUID_NITROGEN_SOURCE, LIQUID_NITROGEN_FLOWING);
    }

    private static Properties liquidArgonProperties() {
        return new Properties(LIQUID_ARGON_TYPE, LIQUID_ARGON_SOURCE, LIQUID_ARGON_FLOWING);
    }

    private static Properties liquidCarbonProperties() {
        return new Properties(LIQUID_CARBON_TYPE, LIQUID_CARBON_SOURCE, LIQUID_CARBON_FLOWING);
    }
}
