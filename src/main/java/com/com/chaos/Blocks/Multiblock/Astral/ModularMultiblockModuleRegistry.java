package com.com.chaos.Blocks.Multiblock.Astral;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Global registry of module types, grouped by which controller type they can dock on.
 *
 * Every ModularMultiblockController is built for one controller type id (e.g.
 * "chaos:nexus"), and only ever sees module types registered under that same id - the
 * Nexus and some future Monolith controller each get entirely separate module pools even
 * though they share this same framework underneath.
 *
 * This is the extension point for addons, other mods, or datapack-driven setup: register(...)
 * can be called from anywhere - this mod's own setup, a separate addon mod's init, a resource
 * reload listener - to add a module type to an existing controller type without touching the
 * controller's own code at all. A future weapons-addon mod, for example, would just call
 * register(NEXUS_TYPE, ITS_OWN_MODULE) from its own init and the Nexus picks it up.
 *
 * Lookups are live: a ModularMultiblockController queries get(...) fresh on every
 * revalidate() rather than caching a snapshot at construction time, so modules registered
 * later (mod load order, a later-loading addon, a datapack reload) still apply without
 * needing the controller to be recreated.
 */
public final class ModularMultiblockModuleRegistry {
    private static final Map<ResourceLocation, List<ModularMultiblockModule>> BY_CONTROLLER_TYPE = new HashMap<>();

    private ModularMultiblockModuleRegistry() {}

    /**
     * Registers a module type as dockable on the given controller type. Safe to call
     * multiple times for different controller types with the same module instance, or for
     * the same controller type with many different modules.
     */
    public static void register(ResourceLocation controllerType, ModularMultiblockModule module) {
        BY_CONTROLLER_TYPE.computeIfAbsent(controllerType, k -> new ArrayList<>()).add(module);
    }

    /** All module types currently registered for the given controller type, in registration order. */
    public static List<ModularMultiblockModule> get(ResourceLocation controllerType) {
        return Collections.unmodifiableList(BY_CONTROLLER_TYPE.getOrDefault(controllerType, List.of()));
    }

    /**
     * Clears every registration. Intended for a datapack/resource reload listener that's
     * about to re-populate the registry from scratch - don't call this mid-game without
     * immediately re-registering, or every controller of every type stops recognizing any
     * module until you do.
     */
    public static void clear() {
        BY_CONTROLLER_TYPE.clear();
    }
}