package com.com.chaos.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * Anchors for {@link com.com.chaos.integration.replication.CreateMatterCalculationBridge}.
 * <p>
 * The bridge walks Create's Mixing/Pressing/Cutting/Crushing/... recipes to derive matter values for
 * anything craftable from a base material, exactly like Replication already does for vanilla crafting
 * and smelting. But it can only walk *down* to a base material, never invent a value from nothing - a
 * raw resource with no recipe of its own (Create's raw zinc, Create Metalwork's crushed netherite
 * scrap, etc.) needs an explicit value or the entire branch built on top of it silently fails to
 * resolve. This provider is exactly that: the same "replication:matter_value" datapack recipe
 * chaoticscreate already emits for its own items in {@link ModMatterValueProvider}, just pointed at a
 * handful of Create/Create Metalwork base items instead.
 * <p>
 * This list is deliberately short - only true "roots" of the material chain. Everything else (ingots,
 * sheets, alloys, nuggets, molten fluids...) should fall out of the bridge automatically. If something
 * doesn't show a matter value in-game, that's usually a sign it's produced by Sequenced Assembly (which
 * the bridge skips on purpose, see its class comment) - add an explicit line for it here instead of
 * trying to make the bridge understand sequenced assembly.
 * <p>
 * Register alongside the existing provider in DataGenerators#gatherData:
 * <pre>
 *   generator.addProvider(event.includeServer(), new CreateCompatMatterValueProvider(packOutput));
 * </pre>
 */
public class CreateCompatMatterValueProvider implements DataProvider {

    private static final String MODID = "chaoticscreate";
    private static final String METALLIC = "replication:metallic";
    private static final String EARTH = "replication:earth";
    private static final String PRECIOUS = "replication:precious";
    private static final String ORGANIC = "replication:organic";

    private final PackOutput.PathProvider pathProvider;

    public CreateCompatMatterValueProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        ArrayList<CompletableFuture<?>> futures = new ArrayList<>();

        // --- Create: base ores/metals with no vanilla furnace or crafting path (Zinc is Create's own
        // "5th metal" and doesn't exist outside Create) ---
        futures.add(itemValue(cache, "create_raw_zinc", "create:raw_zinc", matter(METALLIC, 10.0)));
        futures.add(itemValue(cache, "create_zinc_ore", "create:zinc_ore", matter(METALLIC, 10.0), matter(EARTH, 2.0)));
        futures.add(itemValue(cache, "create_deepslate_zinc_ore", "create:deepslate_zinc_ore", matter(METALLIC, 10.0), matter(EARTH, 3.0)));
        futures.add(itemValue(cache, "create_zinc_ingot", "create:zinc_ingot", matter(METALLIC, 10.0)));
        futures.add(itemValue(cache, "create_zinc_nugget", "create:zinc_nugget", matter(METALLIC, 1.1)));

        // Create's crushing wheels turn ore into "crushed raw <metal>" via the CRUSHING recipe type,
        // which the bridge does walk - but the vanilla ore itself (iron/copper/gold ore) already has a
        // Replication-provided value, so crushed_raw_iron/copper/gold resolve automatically. Zinc has no
        // vanilla equivalent, so it needs its own anchor:
        futures.add(itemValue(cache, "create_crushed_raw_zinc", "create:crushed_raw_zinc", matter(METALLIC, 10.0), matter(EARTH, 1.0)));

        // --- Create Metalwork: the two items it adds directly in code rather than through a Create
        // material chain ---
        futures.add(itemValue(cache, "metalwork_crushed_andesite", "createmetalwork:crushed_andesite", matter(EARTH, 4.0)));
        futures.add(itemValue(cache, "metalwork_crushed_netherite_scrap", "createmetalwork:crushed_netherite_scrap", matter(METALLIC, 30.0), matter(PRECIOUS, 6.0)));

        // Andesite Alloy is Create's most-used intermediate (zinc/iron nugget mix + andesite via
        // Mixing) - it resolves through the bridge once zinc/iron have values, this line is only a
        // fallback in case a modpack overrides that recipe with something the bridge can't parse.
        // Comment out if you confirm the bridge resolves it correctly for your recipe set.
        // futures.add(itemValue(cache, "create_andesite_alloy_fallback", "create:andesite_alloy", matter(METALLIC, 2.0), matter(EARTH, 4.0)));

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private JsonObject matter(String type, double amount) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", type);
        obj.addProperty("amount", (Number) amount);
        return obj;
    }

    private CompletableFuture<?> itemValue(CachedOutput cache, String fileName, String itemId, JsonObject... matterEntries) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "replication:matter_value");
        JsonObject input = new JsonObject();
        input.addProperty("item", itemId);
        json.add("input", input);
        JsonArray matterArray = new JsonArray();
        for (JsonObject entry : matterEntries) {
            matterArray.add(entry);
        }
        json.add("matter", matterArray);
        return DataProvider.saveStable(cache, json,
                this.pathProvider.json(ResourceLocation.fromNamespaceAndPath(MODID, "matter_values/create_compat/" + fileName)));
    }

    @Override
    public String getName() {
        return "Chaotics Create - Create/Metalwork Matter Value Anchors";
    }
}
