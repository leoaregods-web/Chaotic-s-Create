package com.com.chaos.Blocks.Multiblock.Reactor;

import com.com.chaos.ModDataComponents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

import java.util.Optional;

/**
 * One entry in a Chaos Reactor recipe's result list. Three mutually
 * exclusive shapes:
 *
 *  - item + count: a plain item result (e.g. oxygenated_coal).
 *  - fluid + amount: a raw fluid added straight to the controller's own
 *    tank (drain it out with a bucket, same as filling one in).
 *  - capsule_item + capsule_fluid + amount (+ count): produces `count`
 *    copies of the given capsule item (e.g. chaoticscreate:capsule),
 *    each stamped with the given fluid via the GAS_CONTENT data
 *    component - the same mechanism your capsule items already use to
 *    hold content, so a produced capsule behaves identically to one
 *    filled any other way.
 */
public record ReactorResult(
    Optional<ResourceLocation> item, int itemCount,
    Optional<FluidStack> fluid,
    Optional<ResourceLocation> capsuleItem, Optional<FluidStack> capsuleFluid
) {
    public static final Codec<ReactorResult> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        ResourceLocation.CODEC.optionalFieldOf("item").forGetter(ReactorResult::item),
        Codec.INT.optionalFieldOf("count", 1).forGetter(ReactorResult::itemCount),
        ResourceLocation.CODEC.optionalFieldOf("fluid")
            .forGetter(r -> r.fluid.map(f -> BuiltInRegistries.FLUID.getKey(f.getFluid()))),
        ResourceLocation.CODEC.optionalFieldOf("capsule_item").forGetter(ReactorResult::capsuleItem),
        ResourceLocation.CODEC.optionalFieldOf("capsule_fluid")
            .forGetter(r -> r.capsuleFluid.map(f -> BuiltInRegistries.FLUID.getKey(f.getFluid()))),
        Codec.INT.optionalFieldOf("amount", 0)
            .forGetter(r -> r.fluid.map(FluidStack::getAmount).orElseGet(() -> r.capsuleFluid.map(FluidStack::getAmount).orElse(0)))
    ).apply(inst, (item, count, fluidId, capsuleItem, capsuleFluidId, amount) -> new ReactorResult(
        item, count,
        fluidId.map(id -> new FluidStack(BuiltInRegistries.FLUID.get(id), amount)),
        capsuleItem,
        capsuleFluidId.map(id -> new FluidStack(BuiltInRegistries.FLUID.get(id), amount))
    )));

    public static final StreamCodec<RegistryFriendlyByteBuf, ReactorResult> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), ReactorResult::item,
        ByteBufCodecs.INT, ReactorResult::itemCount,
        ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
            r -> r.fluid.map(f -> BuiltInRegistries.FLUID.getKey(f.getFluid())),
        ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), ReactorResult::capsuleItem,
        ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
            r -> r.capsuleFluid.map(f -> BuiltInRegistries.FLUID.getKey(f.getFluid())),
        ByteBufCodecs.INT,
            r -> r.fluid.map(FluidStack::getAmount).orElseGet(() -> r.capsuleFluid.map(FluidStack::getAmount).orElse(0)),
        (item, count, fluidId, capsuleItem, capsuleFluidId, amount) -> new ReactorResult(
            item, count,
            fluidId.map(id -> new FluidStack(BuiltInRegistries.FLUID.get(id), amount)),
            capsuleItem,
            capsuleFluidId.map(id -> new FluidStack(BuiltInRegistries.FLUID.get(id), amount))
        )
    );

    public boolean isItem() {
        return item.isPresent();
    }

    public boolean isFluid() {
        return fluid.isPresent();
    }

    public boolean isCapsule() {
        return capsuleItem.isPresent();
    }

    public ItemStack toItemStack() {
        return item.map(id -> new ItemStack(BuiltInRegistries.ITEM.get(id), itemCount)).orElse(ItemStack.EMPTY);
    }

    public FluidStack toFluidStack() {
        return fluid.orElse(FluidStack.EMPTY);
    }

    /** Builds the actual filled capsule stack - stamps GAS_CONTENT the same way the capsule items already read it. */
    public ItemStack toCapsuleStack() {
        if (capsuleItem.isEmpty() || capsuleFluid.isEmpty()) return ItemStack.EMPTY;
        Item base = BuiltInRegistries.ITEM.get(capsuleItem.get());
        ItemStack stack = new ItemStack(base, itemCount);
        stack.set(ModDataComponents.GAS_CONTENT.get(), SimpleFluidContent.copyOf(capsuleFluid.get()));
        return stack;
    }
}
