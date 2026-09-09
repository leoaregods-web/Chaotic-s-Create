package com.com.chaos.Entities;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, "chaoticscreate");

    public static final DeferredHolder<EntityType<?>, EntityType<ThrownCapsuleEntity>> THROWN_CAPSULE = ENTITY_TYPES.register("thrown_capsule",
            registryName -> EntityType.Builder.<ThrownCapsuleEntity>of(ThrownCapsuleEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(String.valueOf(ResourceKey.create(Registries.ENTITY_TYPE, registryName))));

    public static final DeferredHolder<EntityType<?>, EntityType<ChaosGolemEntity>> CHAOS_GOLEM = ENTITY_TYPES.register("chaos_golem",
            registryName -> EntityType.Builder.<ChaosGolemEntity>of(ChaosGolemEntity::new, MobCategory.MONSTER)
                    .sized(2.5f, 2.5f)
                    .fireImmune()
                    .clientTrackingRange(12)
                    .updateInterval(3)
                    .build(String.valueOf(ResourceKey.create(Registries.ENTITY_TYPE, registryName))));

    public static final DeferredHolder<EntityType<?>, EntityType<ChaosBoltEntity>> CHAOS_BOLT = ENTITY_TYPES.register("chaos_bolt",
            registryName -> EntityType.Builder.<ChaosBoltEntity>of(ChaosBoltEntity::new, MobCategory.MISC)
                    .sized(0.35f, 0.35f)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(String.valueOf(ResourceKey.create(Registries.ENTITY_TYPE, registryName))));

    public static void register(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
    }
}
