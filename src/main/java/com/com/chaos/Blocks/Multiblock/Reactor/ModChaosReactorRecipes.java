package com.com.chaos.Blocks.Multiblock.Reactor;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Self-contained registration so this doesn't require editing the existing
 * ModRecipes class directly - just call register(modEventBus) once from
 * your main mod constructor (ChaoticsCreate), next to wherever ModRecipes'
 * own registries get registered. Feel free to fold these DeferredRegisters
 * into ModRecipes instead if you'd rather keep everything in one place.
 *
 * Assumes ChaoticsCreate.MOD_ID exists as your mod id constant - adjust the
 * import/reference if it's named differently.
 */
public class ModChaosReactorRecipes {

    private static final String MOD_ID = com.com.chaos.ChaoticsCreate.MODID;

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
        DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
        DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<ChaosReactorRecipe>> CHAOS_REACTOR_TYPE =
        RECIPE_TYPES.register("chaos_reactor", () -> new RecipeType<ChaosReactorRecipe>() {
            @Override
            public String toString() {
                return MOD_ID + ":chaos_reactor";
            }
        });

    public static final DeferredHolder<RecipeSerializer<?>, ChaosReactorRecipe.Serializer> CHAOS_REACTOR_SERIALIZER =
        RECIPE_SERIALIZERS.register("chaos_reactor", ChaosReactorRecipe.Serializer::new);

    public static void register(IEventBus modEventBus) {
        RECIPE_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
    }
}
