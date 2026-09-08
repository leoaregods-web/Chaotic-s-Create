package com.com.chaos.Integration.Replication;

import com.buuz135.replication.calculation.MatterCompound;
import com.buuz135.replication.calculation.ReplicationCalculation;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Replication (Buuz135) only walks vanilla RecipeType.CRAFTING and RecipeType.SMELTING when it
 * auto-calculates matter values for items that don't have an explicit "replication:matter_value"
 * recipe (see com.buuz135.replication.calculation.ReplicationCalculation#organizeRecipes).
 * <p>
 * Create (and by extension Create Metalwork, which is purely data-driven on top of Create) uses its
 * own recipe types for almost everything - Mixing, Pressing, Cutting, Compacting, Crushing, Milling,
 * Washing, Filling, Emptying, Deploying and Mechanical Crafting - all of which extend the common
 * {@link ProcessingRecipe} base class and expose a uniform item+fluid ingredient/result API. None of
 * that is visible to Replication out of the box, so anything only obtainable through a Create machine
 * (most ingots, sheets, alloys, and every molten-metal fluid Create Metalwork adds) can't be printed or
 * broken down for matter unless someone hand-writes a matter_value json for every single item.
 * <p>
 * This class re-implements Replication's own graph-walk (see the private
 * ReplicationCalculation.CalculationReference#resolve) against Create's recipe types instead, and
 * merges anything it manages to resolve into Replication's own public
 * {@link ReplicationCalculation#DEFAULT_MATTER_COMPOUND} map so every other part of the mod (the
 * Replicator, the Disintegrator, JEI/EMI previews, the Matter Opedia) just sees the values as if
 * Replication had calculated them itself.
 * <p>
 * Fluids have no equivalent map in vanilla Replication at all (Replication's own "matter" happens to
 * also be modelled as a pseudo-fluid internally, which is a different thing). {@link #FLUID_MATTER}
 * is this mod's own Fluid -> MatterCompound table, kept here so the Fluid Replicator
 * (see com.com.chaos.Blocks.FluidReplicator) and JEI can query it without re-walking recipes.
 */
public final class CreateMatterCalculationBridge {

    public static final Logger LOGGER = LogManager.getLogger("ChaoticsCreate/ReplicationBridge");

    /** Fluid -> resolved matter cost. Populated by {@link #recalculate}. Read by the Fluid Replicator. */
    public static final Map<Fluid, MatterCompound> FLUID_MATTER = new HashMap<>();

    // Every StandardProcessingRecipe-based type worth walking. Sequenced Assembly is intentionally
    // left out - it's a multi-stage recipe (loop of sub-recipes) and doesn't map onto a simple
    // ingredients->results ratio, so items only reachable through it should keep an explicit
    // matter_value json (see CreateCompatMatterValueProvider) instead of relying on this bridge.
    private static final List<RecipeType<?>> ITEM_FLUID_RECIPE_TYPES = new ArrayList<>();

    static {
        ITEM_FLUID_RECIPE_TYPES.add(AllRecipeTypes.MIXING.getType());
        ITEM_FLUID_RECIPE_TYPES.add(AllRecipeTypes.COMPACTING.getType());
        ITEM_FLUID_RECIPE_TYPES.add(AllRecipeTypes.PRESSING.getType());
        ITEM_FLUID_RECIPE_TYPES.add(AllRecipeTypes.CUTTING.getType());
        ITEM_FLUID_RECIPE_TYPES.add(AllRecipeTypes.CRUSHING.getType());
        ITEM_FLUID_RECIPE_TYPES.add(AllRecipeTypes.MILLING.getType());
        ITEM_FLUID_RECIPE_TYPES.add(AllRecipeTypes.SPLASHING.getType());
        ITEM_FLUID_RECIPE_TYPES.add(AllRecipeTypes.HAUNTING.getType());
        ITEM_FLUID_RECIPE_TYPES.add(AllRecipeTypes.SANDPAPER_POLISHING.getType());
        ITEM_FLUID_RECIPE_TYPES.add(AllRecipeTypes.DEPLOYING.getType());
        ITEM_FLUID_RECIPE_TYPES.add(AllRecipeTypes.FILLING.getType());
        ITEM_FLUID_RECIPE_TYPES.add(AllRecipeTypes.EMPTYING.getType());
    }

    private CreateMatterCalculationBridge() {
    }

    /**
     * Call this from a listener on {@code TagsUpdatedEvent} (SERVER_DATA_LOAD) registered at
     * {@link EventPriority#LOWEST} on the NeoForge event bus, and again from
     * {@link ServerStartedEvent} as a safety net. Both call sites are cheap to add in
     * {@code ChaoticsCreate}'s constructor:
     * <pre>
     *   NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, (TagsUpdatedEvent e) -> {
     *       if (e.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD)
     *           CreateMatterCalculationBridge.recalculate(e.getRegistryAccess());
     *   });
     *   NeoForge.EVENT_BUS.addListener((ServerStartedEvent e) ->
     *       CreateMatterCalculationBridge.recalculate(e.getServer().registryAccess()));
     * </pre>
     */
    public static void recalculate(RegistryAccess registryAccess) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        RecipeManager recipeManager = server.getRecipeManager();

        // 1) Collect every candidate recipe, grouped by the item/fluid it produces.
        Map<Item, List<ItemRef>> itemRefs = new HashMap<>();
        Map<Fluid, List<FluidRef>> fluidRefs = new HashMap<>();

        for (RecipeType<?> type : ITEM_FLUID_RECIPE_TYPES) {
            for (RecipeHolder<?> holder : getAllRecipesFor(recipeManager, type)) {
                if (!(holder.value() instanceof ProcessingRecipe<?, ?> processing)) continue;

                collectProcessingRecipe(
                        processing,
                        registryAccess,
                        itemRefs,
                        fluidRefs
                );
            }
        }
        for (RecipeHolder<?> holder : getAllRecipesFor(
                recipeManager,
                AllRecipeTypes.MECHANICAL_CRAFTING.getType())) {
            if (!(holder.value() instanceof MechanicalCraftingRecipe recipe)) continue;

            ItemStack result = recipe.getResultItem(registryAccess);
            if (result.isEmpty()) continue;

            List<Ingredient> ingredients = new ArrayList<>(recipe.getIngredients());

            itemRefs.computeIfAbsent(result.getItem(), i -> new ArrayList<>())
                    .add(new ItemRef(ingredients, List.of(), result.getCount()));
        }
        LOGGER.info("Chaotics Create: gathered {} Create item recipes and {} Create fluid recipes to bridge into Replication",
                itemRefs.size(), fluidRefs.size());

        // 2) Resolve, exactly like Replication's own resolver: depth-first, memoised, cheapest-of-
        // multiple-recipes wins, bottoming out at whatever Replication (or our own matter_value
        // datagen for foreign items, see CreateCompatMatterValueProvider) already knows.
        Map<Item, MatterCompound> resolvedItems = new HashMap<>();
        Map<Fluid, MatterCompound> resolvedFluids = new HashMap<>();
        for (Item item : itemRefs.keySet()) {
            resolveItem(item, itemRefs, fluidRefs, resolvedItems, resolvedFluids, new HashSet<>(), new HashSet<>());
        }
        for (Fluid fluid : fluidRefs.keySet()) {
            resolveFluid(fluid, itemRefs, fluidRefs, resolvedItems, resolvedFluids, new HashSet<>(), new HashSet<>());
        }

        // 3) Publish. Never overwrite a value Replication (or an explicit matter_value recipe)
        // already produced - those are authoritative; we only fill gaps.
        int added = 0;
        for (Map.Entry<Item, MatterCompound> entry : resolvedItems.entrySet()) {
            if (ReplicationCalculation.DEFAULT_MATTER_COMPOUND.putIfAbsent(entry.getKey(), entry.getValue()) == null) {
                added++;
            }
        }
        FLUID_MATTER.clear();
        FLUID_MATTER.putAll(resolvedFluids);
        LOGGER.info("Chaotics Create: added {} new item matter values and resolved {} fluid matter values via Create recipes",
                added, resolvedFluids.size());
    }

    // ---- recipe collection -------------------------------------------------------------------

    private record ItemRef(List<Ingredient> items, List<SizedFluidIngredient> fluids, int outputCount) {
    }

    private record FluidRef(List<Ingredient> items, List<SizedFluidIngredient> fluids, int outputAmountMb) {
    }

    private static void collectProcessingRecipe(ProcessingRecipe<?, ?> recipe, RegistryAccess registryAccess,
                                                 Map<Item, List<ItemRef>> itemRefs, Map<Fluid, List<FluidRef>> fluidRefs) {
        List<Ingredient> items = new ArrayList<>(recipe.getIngredients());
        List<SizedFluidIngredient> fluids = new ArrayList<>(recipe.getFluidIngredients());
        if (items.isEmpty() && fluids.isEmpty()) return;

        List<ItemStack> itemResults = recipe.getRollableResultsAsItemStacks();
        for (ItemStack result : itemResults) {
            if (result.isEmpty()) continue;
            // Only take the highest-count/primary result stack per recipe as the "ratio" anchor -
            // secondary/chance outputs (e.g. crushing byproducts) are treated as a bonus, not
            // counted against the cost, which slightly over-values byproducts but never
            // under-values the primary product.
            itemRefs.computeIfAbsent(result.getItem(), i -> new ArrayList<>())
                    .add(new ItemRef(items, fluids, Math.max(1, result.getCount())));
        }
        for (FluidStack result : recipe.getFluidResults()) {
            if (result.isEmpty()) continue;
            fluidRefs.computeIfAbsent(result.getFluid(), f -> new ArrayList<>())
                    .add(new FluidRef(items, fluids, Math.max(1, result.getAmount())));
        }
    }

    // ---- resolution ----------------------------------------------------------------------------

    private static MatterCompound resolveItem(Item item, Map<Item, List<ItemRef>> itemRefs, Map<Fluid, List<FluidRef>> fluidRefs,
                                               Map<Item, MatterCompound> resolvedItems, Map<Fluid, MatterCompound> resolvedFluids,
                                               Set<Item> visitedItems, Set<Fluid> visitedFluids) {
        MatterCompound known = ReplicationCalculation.DEFAULT_MATTER_COMPOUND.get(item);
        if (known != null) return known;
        if (resolvedItems.containsKey(item)) return resolvedItems.get(item);
        if (!visitedItems.add(item)) return null; // cycle guard

        MatterCompound best = null;
        for (ItemRef ref : itemRefs.getOrDefault(item, List.of())) {
            MatterCompound cost = costOf(ref.items(), ref.fluids(), itemRefs, fluidRefs, resolvedItems, resolvedFluids, visitedItems, visitedFluids);
            if (cost == null) continue;
            cost = cost.duplicate().divide(ref.outputCount());
            best = best == null ? cost : best.compare(cost);
        }
        visitedItems.remove(item);
        if (best != null) resolvedItems.put(item, best);
        return best;
    }

    private static MatterCompound resolveFluid(Fluid fluid, Map<Item, List<ItemRef>> itemRefs, Map<Fluid, List<FluidRef>> fluidRefs,
                                                Map<Item, MatterCompound> resolvedItems, Map<Fluid, MatterCompound> resolvedFluids,
                                                Set<Item> visitedItems, Set<Fluid> visitedFluids) {
        if (resolvedFluids.containsKey(fluid)) return resolvedFluids.get(fluid);
        if (!visitedFluids.add(fluid)) return null;

        MatterCompound best = null;
        for (FluidRef ref : fluidRefs.getOrDefault(fluid, List.of())) {
            MatterCompound cost = costOf(ref.items(), ref.fluids(), itemRefs, fluidRefs, resolvedItems, resolvedFluids, visitedItems, visitedFluids);
            if (cost == null) continue;
            // Normalise to "matter per 1000 mB (one bucket)" so recipe scale doesn't matter.
            cost = cost.duplicate().divide(ref.outputAmountMb() / 1000.0);
            best = best == null ? cost : best.compare(cost);
        }
        visitedFluids.remove(fluid);
        if (best != null) resolvedFluids.put(fluid, best);
        return best;
    }

    @SuppressWarnings("unchecked")
    private static MatterCompound costOf(List<Ingredient> items, List<SizedFluidIngredient> fluids,
                                          Map<Item, List<ItemRef>> itemRefs, Map<Fluid, List<FluidRef>> fluidRefs,
                                          Map<Item, MatterCompound> resolvedItems, Map<Fluid, MatterCompound> resolvedFluids,
                                          Set<Item> visitedItems, Set<Fluid> visitedFluids) {
        MatterCompound total = new MatterCompound();
        for (Ingredient ingredient : items) {
            if (ingredient.isEmpty()) continue;
            ItemStack[] matching = ingredient.getItems();
            if (matching.length == 0) return null;
            // Ingredients can match several items (tags) - use whichever resolves cheapest, same
            // philosophy as Replication's own resolver.
            MatterCompound cheapest = null;
            for (ItemStack stack : matching) {
                MatterCompound m = resolveItem(stack.getItem(), itemRefs, fluidRefs, resolvedItems, resolvedFluids, visitedItems, visitedFluids);
                if (m == null) continue;
                cheapest = cheapest == null ? m : cheapest.compare(m);
            }
            if (cheapest == null) return null;
            total.add(cheapest);
        }
        for (SizedFluidIngredient fluidIngredient : fluids) {
            Fluid[] matching = Arrays.stream(fluidIngredient.getFluids())
                    .map(FluidStack::getFluid)
                    .toArray(Fluid[]::new);

            if (matching.length == 0) return null;

            MatterCompound cheapest = null;

            for (Fluid fluid : matching) {
                MatterCompound m = resolveFluid(
                        fluid,
                        itemRefs,
                        fluidRefs,
                        resolvedItems,
                        resolvedFluids,
                        visitedItems,
                        visitedFluids
                );

                if (m == null) continue;
                cheapest = cheapest == null ? m : cheapest.compare(m);
            }

            if (cheapest == null) return null;

            total.add(
                    cheapest.duplicate()
                            .divide(1000.0 / Math.max(1, fluidIngredient.amount()))
            );
        }
        return total;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static List<RecipeHolder<?>> getAllRecipesFor(RecipeManager recipeManager, RecipeType<?> type) {
        // RecipeManager requires RecipeType<T> where T extends Recipe<I>.
        // Create exposes these recipe types as RecipeType<?>, so use one contained
        // unchecked cast at the boundary and inspect the actual recipe below.
        return (List) recipeManager.getAllRecipesFor((RecipeType) type);
    }
}
