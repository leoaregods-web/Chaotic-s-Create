package com.com.chaos.Blocks.Multiblock.Astral;

import com.com.chaos.Blocks.Multiblock.MultiblockBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages a fixed set of docking Ports around a core multiblock (e.g. the Nexus of Chaos),
 * scanning each one against a list of registered ModularMultiblockModule types and tracking
 * which module, if any, currently occupies each port.
 *
 * This is a plain composed helper, not a BlockEntity itself. A controller BE (like
 * NexusControllerBlockEntity) owns one instance, calls revalidate(...) alongside its own
 * core-shape check, and reads getActiveModules() / isPortFilled(...) for its own logic
 * and GUI.
 */
public class ModularMultiblockController {

    /**
     * One fixed docking point on the core, in the same (right, back, up) local-space
     * convention as MultiblockBuilder.Cell, relative to the core's own anchor and facing.
     *
     * facingTurns is how many quarter-turns clockwise the port's own facing is from the
     * core's facing (0 = same direction as the core), so every port rotates together with
     * the core as it's placed facing different ways.
     */
    public record Port(String name, int right, int back, int up, int facingTurns) {

        public BlockPos anchorPos(BlockPos coreAnchor, Direction coreFacing) {
            return MultiblockBuilder.resolve(coreAnchor, coreFacing, right, back, up);
        }

        public Direction facing(Direction coreFacing) {
            Direction facing = coreFacing;
            int turns = ((facingTurns % 4) + 4) % 4;
            for (int i = 0; i < turns; i++) {
                facing = facing.getClockWise();
            }
            return facing;
        }
    }

    private final ResourceLocation controllerType;
    private final List<Port> ports;
    private final Map<Port, ModularMultiblockModule> activeModules = new LinkedHashMap<>();

    /**
     * @param controllerType identifies which registry pool this controller draws module
     *                       types from (see ModularMultiblockModuleRegistry) - e.g. the
     *                       Nexus and a future Monolith would each use their own id here,
     *                       and never see each other's registered modules.
     * @param ports          the fixed docking points around this controller's core.
     */
    public ModularMultiblockController(ResourceLocation controllerType, List<Port> ports) {
        this.controllerType = controllerType;
        this.ports = List.copyOf(ports);
    }

    public ResourceLocation getControllerType() {
        return controllerType;
    }

    /**
     * Re-scans every port against every module type currently registered for this
     * controller's type (looked up live, so late registrations still apply). A port keeps
     * whichever module currently matches it (first match wins if two module patterns could
     * both fit the same port - registration order decides priority in that case). Fires
     * onAttach/onDetach exactly on the calls where a port's occupant changes.
     */
    public void revalidate(Level level, BlockPos coreAnchor, Direction coreFacing) {
        List<ModularMultiblockModule> candidates = ModularMultiblockModuleRegistry.get(controllerType);

        for (Port port : ports) {
            BlockPos portAnchor = port.anchorPos(coreAnchor, coreFacing);
            Direction portFacing = port.facing(coreFacing);

            ModularMultiblockModule matched = null;
            for (ModularMultiblockModule candidate : candidates) {
                if (MultiblockBuilder.matchesPattern(level, portAnchor, portFacing, candidate.pattern())) {
                    matched = candidate;
                    break;
                }
            }

            ModularMultiblockModule previous = activeModules.get(port);
            if (matched == previous) {
                continue;
            }
            if (previous != null) {
                previous.onDetach(level, portAnchor);
            }
            if (matched != null) {
                activeModules.put(port, matched);
                matched.onAttach(level, portAnchor, portFacing);
            } else {
                activeModules.remove(port);
            }
        }
    }

    /**
     * Runs every currently-docked module's serverTick(...) - this is what actually lets
     * modules do things (buff siblings, process, generate power, etc). Call this on
     * whatever cadence makes sense for the owning controller; it's separate from
     * revalidate() so structure re-checks and per-tick module effects don't have to share
     * a cadence.
     */
    public void tickModules(Level level, BlockPos coreAnchor, Direction coreFacing) {
        for (Map.Entry<Port, ModularMultiblockModule> entry : activeModules.entrySet()) {
            Port port = entry.getKey();
            entry.getValue().serverTick(level, port.anchorPos(coreAnchor, coreFacing), port.facing(coreFacing), this);
        }
    }

    public Map<Port, ModularMultiblockModule> getActiveModules() {
        return Collections.unmodifiableMap(activeModules);
    }

    public boolean isPortFilled(Port port) {
        return activeModules.containsKey(port);
    }
}