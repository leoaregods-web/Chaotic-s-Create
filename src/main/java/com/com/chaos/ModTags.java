package com.com.chaos;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public class ModTags {
    public static final TagKey<Fluid> GASES = FluidTags.create(
            ResourceLocation.fromNamespaceAndPath(ChaoticsCreate.MODID, "gases"));
}