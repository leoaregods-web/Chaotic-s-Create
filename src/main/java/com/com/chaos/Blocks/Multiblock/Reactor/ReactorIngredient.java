package com.com.chaos.Blocks.Multiblock.Reactor;

import com.com.chaos.ChaoticsCreate;
import com.com.chaos.ModDataComponents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

import java.util.Optional;

/**
 * One side of a Chaos Reactor recipe's input - exactly one of:
 *
 *  - item + count: a plain item sitting in the slot (e.g. coal). JSON shape
 *    is NESTED because this field and Ingredient's own field are both
 *    named "item": {"item": {"item": "minecraft:coal"}, "count": 1} -
 *    Ingredient.CODEC does NOT accept a bare string like "minecraft:coal"
 *    in this version, only the object form.
 *  - fluid + amount: a raw fluid sitting in the crystal's own internal
 *    tank (filled directly via bucket/pipe, not inside a capsule).
 *  - capsule_fluid + amount: a capsule item (any size) sitting in the
 *    slot, holding at least this much of this fluid via its own
 *    GAS_CONTENT fluid-handler capability - checked through the exact
 *    same FluidHandlerItemStack ModCapabilities already registers for
 *    every capsule size.
 *  - none of the above: unused, always matches.
 *
 * "fluid" and "capsule_fluid" share one top-level "amount" field, same
 * pattern as ReactorResult - don't set both on the same entry.
 *
 * FIX (previous version): the fluid field used to be encoded as a whole
 * nested FluidStack codec wrapped in ANOTHER .optionalFieldOf("fluid"),
 * which required doubly-nested JSON ({"fluid": {"fluid": ..., "amount":
 * ...}}) that never matched the flat JSON every recipe actually uses.
 * Since it's an optional field, the shape mismatch silently decoded as
 * absent instead of erroring - meaning `fluid` (and capsule_fluid, which
 * wasn't wired in at all) always came back empty, `isEmpty()` returned
 * true, and matches() returned true UNCONDITIONALLY. That's why recipes
 * fired regardless of what was actually in the crystals. Rewritten below
 * to flat top-level fields, matching ReactorResult's already-correct shape.
 */
public record ReactorIngredient(
        Optional<Ingredient> item, int itemCount,
        Optional<FluidStack> fluid,
        Optional<FluidStack> capsuleFluid
) {

    public static final ReactorIngredient EMPTY =
            new ReactorIngredient(Optional.empty(), 0, Optional.empty(), Optional.empty());

    public static final Codec<ReactorIngredient> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Ingredient.CODEC.optionalFieldOf("item").forGetter(ReactorIngredient::item),
            Codec.INT.optionalFieldOf("count", 1).forGetter(ReactorIngredient::itemCount),
            ResourceLocation.CODEC.optionalFieldOf("fluid")
                    .forGetter(r -> r.fluid.map(f -> BuiltInRegistries.FLUID.getKey(f.getFluid()))),
            ResourceLocation.CODEC.optionalFieldOf("capsule_fluid")
                    .forGetter(r -> r.capsuleFluid.map(f -> BuiltInRegistries.FLUID.getKey(f.getFluid()))),
            Codec.INT.optionalFieldOf("amount", 0)
                    .forGetter(r -> r.fluid.map(FluidStack::getAmount)
                            .orElseGet(() -> r.capsuleFluid.map(FluidStack::getAmount).orElse(0)))
    ).apply(inst, (item, count, fluidId, capsuleFluidId, amount) -> new ReactorIngredient(
            item, count,
            fluidId.map(id -> new FluidStack(BuiltInRegistries.FLUID.get(id), amount)),
            capsuleFluidId.map(id -> new FluidStack(BuiltInRegistries.FLUID.get(id), amount))
    )));

    public static final StreamCodec<RegistryFriendlyByteBuf, ReactorIngredient> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC), ReactorIngredient::item,
            ByteBufCodecs.INT, ReactorIngredient::itemCount,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
            r -> r.fluid.map(f -> BuiltInRegistries.FLUID.getKey(f.getFluid())),
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
            r -> r.capsuleFluid.map(f -> BuiltInRegistries.FLUID.getKey(f.getFluid())),
            ByteBufCodecs.INT,
            r -> r.fluid.map(FluidStack::getAmount).orElseGet(() -> r.capsuleFluid.map(FluidStack::getAmount).orElse(0)),
            (item, count, fluidId, capsuleFluidId, amount) -> new ReactorIngredient(
                    item, count,
                    fluidId.map(id -> new FluidStack(BuiltInRegistries.FLUID.get(id), amount)),
                    capsuleFluidId.map(id -> new FluidStack(BuiltInRegistries.FLUID.get(id), amount))
            )
    );

    public boolean isEmpty() {
        return item.isEmpty() && fluid.isEmpty() && capsuleFluid.isEmpty();
    }

    /**
     * True if this requirement is satisfied by the given slot item + tank fluid.
     * Always true when this side is unused. NOTE: previously this never checked
     * capsule contents at all - ChaosCrystalBlockEntity.consumeSide() was draining
     * capsules without anything having validated them first. Fixed here.
     */
    public boolean matches(ItemStack stack, FluidStack tankFluid) {
        ChaoticsCreate.LOGGER.info("[ingredient-match] this={} stack={} tankFluid={}", this, stack, tankFluid);
        if (isEmpty()) return true;

        if (item.isPresent()) {
            return !stack.isEmpty() && item.get().test(stack) && stack.getCount() >= itemCount;
        }

        if (fluid.isPresent()) {
            FluidStack required = fluid.get();
            return !tankFluid.isEmpty()
                    && tankFluid.getFluid() == required.getFluid()
                    && tankFluid.getAmount() >= required.getAmount();
        }

        // capsule_fluid: check the actual capsule sitting in the slot via its own GAS_CONTENT
        // data component. copy() returns the full FluidStack (fluid + amount); getFluid() on
        // SimpleFluidContent only returns the raw Fluid, same as FluidStack's own getFluid().
        FluidStack required = capsuleFluid.orElseThrow();
        if (stack.isEmpty()) return false;
        FluidStack contained = stack.getOrDefault(ModDataComponents.GAS_CONTENT.get(), SimpleFluidContent.EMPTY).copy();
        return !contained.isEmpty()
                && contained.getFluid() == required.getFluid()
                && contained.getAmount() >= required.getAmount();
    }

    /** Builds the concrete ItemStack this entry represents - only meaningful for "item" entries. */
    public ItemStack toItemStack() {
        if (item.isEmpty()) return ItemStack.EMPTY;
        ItemStack[] matching = item.get().getItems();
        return matching.length > 0 ? matching[0].copyWithCount(itemCount) : ItemStack.EMPTY;
    }

    public FluidStack toFluidStack() {
        return fluid.orElse(FluidStack.EMPTY);
    }
}