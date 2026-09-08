package com.com.chaos.Recipes;

import com.com.chaos.ChaoticsCreate;
import com.mojang.serialization.Codec;
import com.simibubi.create.Create;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipeParams;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.createmod.catnip.lang.Lang;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public enum ModRecipes implements IRecipeTypeInfo, StringRepresentable  {

    ABYSSALCRUSHING(AbyssalCrushingRecipe::new);

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, ChaoticsCreate.MODID);

    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, ChaoticsCreate.MODID);

    public final ResourceLocation id;
    public final Supplier<RecipeSerializer<?>> serializerSupplier;
    private final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> serializerObject;
    @Nullable
    private final DeferredHolder<RecipeType<?>, RecipeType<?>> typeObject;
    private final Supplier<RecipeType<?>> type;

    private boolean isProcessingRecipe;

    public static final Codec<ModRecipes> CODEC = StringRepresentable.fromEnum(ModRecipes::values);

    private static class Registers {
        private static final DeferredRegister<RecipeSerializer<?>> SERIALIZER_REGISTER = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, ChaoticsCreate.MODID);
        private static final DeferredRegister<RecipeType<?>> TYPE_REGISTER = DeferredRegister.create(Registries.RECIPE_TYPE, ChaoticsCreate.MODID);
    }

    @ApiStatus.Internal
    public static void register(IEventBus modEventBus) {
        ModRecipes.Registers.SERIALIZER_REGISTER.register(modEventBus);
        ModRecipes.Registers.TYPE_REGISTER.register(modEventBus);
    }

    ModRecipes(Supplier<RecipeSerializer<?>> serializerSupplier, Supplier<RecipeType<?>> typeSupplier, boolean registerType) {
        String name = Lang.asId(name());
        id = ChaoticsCreate.rl(name);
        this.serializerSupplier = serializerSupplier;
        serializerObject = Registers.SERIALIZER_REGISTER.register(name, serializerSupplier);
        if (registerType) {
            typeObject = Registers.TYPE_REGISTER.register(name, typeSupplier);
            type = typeObject;
        } else {
            typeObject = null;
            type = typeSupplier;
        }
        isProcessingRecipe = false;
    }

    ModRecipes(Supplier<RecipeSerializer<?>> serializerSupplier) {
        String name = Lang.asId(name());
        id = ChaoticsCreate.rl(name);
        this.serializerSupplier = serializerSupplier;
        serializerObject = ModRecipes.Registers.SERIALIZER_REGISTER.register(name, serializerSupplier);
        typeObject = ModRecipes.Registers.TYPE_REGISTER.register(name, () -> RecipeType.simple(id));
        type = typeObject;
        isProcessingRecipe = false;
    }

    ModRecipes(StandardProcessingRecipe.Factory<?> processingFactory) {
        this(() -> new StandardProcessingRecipe.Serializer<>(processingFactory));
        isProcessingRecipe = true;
    }

    ModRecipes(ProcessingRecipe.Factory<ItemApplicationRecipeParams, ? extends ItemApplicationRecipe> itemApplicationFactory) {
        this(() -> new ItemApplicationRecipe.Serializer<>(itemApplicationFactory));
        isProcessingRecipe = true;
    }

    public <I extends RecipeInput, R extends Recipe<I>> Optional<RecipeHolder<R>> find(I inv, Level world) {
        return world.getRecipeManager()
                .getRecipeFor(getType(), inv, world);
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public <T extends RecipeSerializer<?>> T getSerializer() {
        return (T) serializerObject.get();
    }

    @Override
    public <I extends RecipeInput, R extends Recipe<I>> RecipeType<R> getType() {
        return (RecipeType<R>) type.get();
    }

    @Override
    public String getSerializedName() {
        return id.toString();
    }
}
