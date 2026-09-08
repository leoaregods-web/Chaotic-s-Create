package com.com.chaos.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModMatterValueProvider implements DataProvider {

    private static final String MODID = "chaoticscreate";
    private static final String METALLIC = "replication:metallic";
    private static final String EARTH = "replication:earth";
    private static final String PRECIOUS = "replication:precious";
    private static final String ORGANIC = "replication:organic";

    private final PackOutput.PathProvider pathProvider;

    public ModMatterValueProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        futures.add(itemValue(cache, "raw_mythril", "chaoticscreate:raw_mythril",
                matter(METALLIC, 8)));
        futures.add(itemValue(cache, "raw_abyssal_iron", "chaoticscreate:raw_abyssal_iron",
                matter(METALLIC, 14)));

        futures.add(itemValue(cache, "mythril_ingot", "chaoticscreate:mythril_ingot",
                matter(METALLIC, 8)));
        futures.add(itemValue(cache, "abyssal_iron_ingot", "chaoticscreate:abyssal_iron_ingot",
                matter(METALLIC, 14)));

        futures.add(itemValue(cache, "oxygen_capsule", "chaoticscreate:oxygen_capsule",
                matter(METALLIC, 9), matter(EARTH, 2)));
        futures.add(itemValue(cache, "nitrogen_capsule", "chaoticscreate:nitrogen_capsule",
                matter(METALLIC, 9), matter(EARTH, 5)));
        futures.add(itemValue(cache, "argon_capsule", "chaoticscreate:argon_capsule",
                matter(METALLIC, 9), matter(EARTH, 12)));
        futures.add(itemValue(cache, "reinforced_oxygen_capsule", "chaoticscreate:reinforced_oxygen_capsule",
                matter(METALLIC, 17), matter(EARTH, 2), matter(PRECIOUS, 4)));
        futures.add(itemValue(cache, "reinforced_nitrogen_capsule", "chaoticscreate:reinforced_nitrogen_capsule",
                matter(METALLIC, 17), matter(EARTH, 5), matter(PRECIOUS, 4)));
        futures.add(itemValue(cache, "reinforced_argon_capsule", "chaoticscreate:reinforced_argon_capsule",
                matter(METALLIC, 17), matter(EARTH, 12), matter(PRECIOUS, 4)));

        futures.add(itemValue(cache, "small_oxygen_capsule", "chaoticscreate:small_oxygen_capsule",
                matter(METALLIC, 9), matter(EARTH, 1)));
        futures.add(itemValue(cache, "small_nitrogen_capsule", "chaoticscreate:small_nitrogen_capsule",
                matter(METALLIC, 9), matter(EARTH, 3)));
        futures.add(itemValue(cache, "small_argon_capsule", "chaoticscreate:small_argon_capsule",
                matter(METALLIC, 9), matter(EARTH, 6)));
        futures.add(itemValue(cache, "reinforced_small_oxygen_capsule", "chaoticscreate:reinforced_small_oxygen_capsule",
                matter(METALLIC, 17), matter(EARTH, 1), matter(PRECIOUS, 4)));
        futures.add(itemValue(cache, "reinforced_small_nitrogen_capsule", "chaoticscreate:reinforced_small_nitrogen_capsule",
                matter(METALLIC, 17), matter(EARTH, 3), matter(PRECIOUS, 4)));
        futures.add(itemValue(cache, "reinforced_small_argon_capsule", "chaoticscreate:reinforced_small_argon_capsule",
                matter(METALLIC, 17), matter(EARTH, 6), matter(PRECIOUS, 4)));

        futures.add(itemValue(cache, "large_oxygen_capsule", "chaoticscreate:large_oxygen_capsule",
                matter(METALLIC, 18), matter(EARTH, 1)));
        futures.add(itemValue(cache, "large_nitrogen_capsule", "chaoticscreate:large_nitrogen_capsule",
                matter(METALLIC, 18), matter(EARTH, 3)));
        futures.add(itemValue(cache, "large_argon_capsule", "chaoticscreate:large_argon_capsule",
                matter(METALLIC, 18), matter(EARTH, 6)));
        futures.add(itemValue(cache, "reinforced_large_oxygen_capsule", "chaoticscreate:reinforced_large_oxygen_capsule",
                matter(METALLIC, 26), matter(EARTH, 1), matter(PRECIOUS, 4)));
        futures.add(itemValue(cache, "reinforced_large_nitrogen_capsule", "chaoticscreate:reinforced_large_nitrogen_capsule",
                matter(METALLIC, 26), matter(EARTH, 3), matter(PRECIOUS, 4)));
        futures.add(itemValue(cache, "reinforced_large_argon_capsule", "chaoticscreate:reinforced_large_argon_capsule",
                matter(METALLIC, 26), matter(EARTH, 6), matter(PRECIOUS, 4)));

        futures.add(itemValue(cache, "astral_alloy", "chaoticscreate:astral_alloy",
                matter(METALLIC, 2), matter(EARTH, 10)));
        futures.add(itemValue(cache, "abyss_alloy", "chaoticscreate:abyss_alloy",
                matter(METALLIC, 2), matter(EARTH, 8)));
        futures.add(itemValue(cache, "astral-abyss_alloy", "chaoticscreate:astral-abyss_alloy",
                matter(METALLIC, 4), matter(EARTH, 18)));
        futures.add(itemValue(cache, "astral_casing", "chaoticscreate:astral_casing",
                matter(METALLIC, 2), matter(EARTH, 14), matter(ORGANIC, 4)));
        futures.add(itemValue(cache, "abyss_casing", "chaoticscreate:abyss_casing",
                matter(METALLIC, 2), matter(EARTH, 12), matter(ORGANIC, 4)));
        futures.add(itemValue(cache, "astral-abyss_casing", "chaoticscreate:astral-abyss_casing",
                matter(METALLIC, 4), matter(EARTH, 22), matter(ORGANIC, 4)));

        futures.add(itemValue(cache, "astra-abyss_ingot", "chaoticscreate:astra-abyss_ingot",
                matter(METALLIC, 22), matter(EARTH, 12)));

        futures.add(itemValue(cache, "basic_processor", "chaoticscreate:basic_processor",
                matter(METALLIC, 20)));
        futures.add(itemValue(cache, "advanced_processor", "chaoticscreate:advanced_processor",
                matter(METALLIC, 34)));
        futures.add(itemValue(cache, "elite_processor", "chaoticscreate:elite_processor",
                matter(METALLIC, 56), matter(EARTH, 12)));

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private JsonObject matter(String type, double amount) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", type);
        obj.addProperty("amount", amount);
        return obj;
    }

    private CompletableFuture<?> itemValue(CachedOutput cache, String fileName, String itemId, JsonObject... matterEntries) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "replication:matter_value");

        JsonObject input = new JsonObject();
        input.addProperty("item", itemId);
        json.add("input", input);

        JsonArray matterArray = new JsonArray();
        for (JsonObject entry : matterEntries) matterArray.add(entry);
        json.add("matter", matterArray);

        return DataProvider.saveStable(cache, json,
                pathProvider.json(ResourceLocation.fromNamespaceAndPath(MODID, "matter_values/" + fileName)));
    }

    @Override
    public String getName() {
        return "Chaotics Create Matter Values";
    }
}