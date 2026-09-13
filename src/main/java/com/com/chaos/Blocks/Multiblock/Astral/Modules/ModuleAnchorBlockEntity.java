package com.com.chaos.Blocks.Multiblock.Astral.Modules;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BlockEntity for module anchors. Minimal - modules define their own logic in ModularMultiblockModule.
 * This entity just provides the anchor point where modules connect to the nexus.
 */
public class ModuleAnchorBlockEntity extends BlockEntity {

    public ModuleAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(null, pos, state);
    }

    public ModuleAnchorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ModuleAnchorBlockEntity be) {
        // Modules handle their own logic via ModularMultiblockModule.serverTick()
        // This entity is just a placeholder anchor
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ModuleAnchorBlockEntity be) {
        // No client-side logic needed
    }
}
