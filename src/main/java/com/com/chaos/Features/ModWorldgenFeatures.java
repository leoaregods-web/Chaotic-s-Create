package com.com.chaos.Features;

import com.com.chaos.ChaoticsCreate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModWorldgenFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(BuiltInRegistries.FEATURE, ChaoticsCreate.MODID);

    public static final DeferredHolder<Feature<?>, LiquidSpaceMeteoriteFeature> LIQUID_SPACE_METEORITE =
            FEATURES.register("liquid_space_meteorite", () -> new LiquidSpaceMeteoriteFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, DeepWaterPocketFeature> DEEP_WATER_POCKET =
            FEATURES.register("deep_water_pocket", () -> new DeepWaterPocketFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, SolidMeteoriteFeature> SOLID_METEOR =
            FEATURES.register("solid_meteor", () -> new SolidMeteoriteFeature(NoneFeatureConfiguration.CODEC));

    public static void register(IEventBus modBus) {
        FEATURES.register(modBus);
    }
}
