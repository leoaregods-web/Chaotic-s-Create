package com.com.chaos.Blocks.FluidReplicator;

import com.buuz135.replication.api.matter_fluid.IMatterHandler;
import com.buuz135.replication.api.matter_fluid.MatterStack;
import com.buuz135.replication.api.matter_fluid.MatterTank;
import com.buuz135.replication.calculation.MatterCompound;
import com.buuz135.replication.calculation.MatterValue;
import com.com.chaos.Integration.Replication.CreateMatterCalculationBridge;
import com.com.chaos.Recipes.ModFluidReplicatorRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nullable;

/**
 * The "just build a block entity for it" fallback for fluid printing, in case you'd rather not wait on
 * (or don't want to depend on) Replication ever adding real-fluid support to the Replicator itself.
 * <p>
 * Input side: a small {@link MatterTank} per matter type isn't necessary - Replication's own
 * MatterTank already buffers a single matter type at a time, and its Matter Pipes will happily push
 * different matter types into this block back-to-back, so one tank plus an internal "currently
 * accumulating" MatterCompound is enough. Exposed as {@link IMatterHandler} via
 * ReplicationRegistry.Capabilities.MATTER_HANDLER, exactly like Replication's own machines - see the
 * ModCapabilities snippet in the README.
 * <p>
 * Output side: a plain {@link FluidTank}, exposed via NeoForge's standard
 * {@code Capabilities.FluidHandler.BLOCK}, so Create's fluid pipes/pumps and vanilla buckets see it as
 * an ordinary tank - no Replication- or Create-specific consumer code needed on the output end.
 * <p>
 * Recipe lookup order for whichever fluid the player has selected (selection UI/menu is left to you -
 * a MatterPattern-style dropdown mirroring Replication's own Replicator screen is the natural fit):
 * 1. An explicit {@link com.com.chaos.Recipes.FluidMatterValueRecipe} for that fluid, if one exists.
 * 2. {@link CreateMatterCalculationBridge#FLUID_MATTER}, the value derived automatically from Create's
 *    Mixing/Filling/Emptying recipes.
 * If neither has a value, the fluid simply can't be printed yet.
 */
public class FluidReplicatorBlockEntity extends BlockEntity {

    private static final int MATTER_TANK_CAPACITY = 4000;
    private static final int FLUID_TANK_CAPACITY = 4000; // mB
    private static final int MB_PER_TICK = 20; // print speed, tune to taste / gate behind Create RPM later

    private final MatterTank matterInput = new MatterTank(MATTER_TANK_CAPACITY);
    private final FluidTank fluidOutput = new FluidTank(FLUID_TANK_CAPACITY);

    @Nullable
    private Fluid selectedFluid;

    public FluidReplicatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModFluidReplicatorRegistry.FLUID_REPLICATOR_BE.get(), pos, state);
    }

    public void setSelectedFluid(@Nullable Fluid fluid) {
        this.selectedFluid = fluid;
        setChanged();
    }

    @Nullable
    public Fluid getSelectedFluid() {
        return selectedFluid;
    }

    public void serverTick() {
        if (selectedFluid == null) return;
        if (fluidOutput.getFluidAmount() >= fluidOutput.getCapacity()) return;

        MatterCompound costPer1000mb = costFor(selectedFluid);
        if (costPer1000mb == null || costPer1000mb.getValues().isEmpty()) return; // unprintable fluid

        int toPrint = Math.min(MB_PER_TICK, fluidOutput.getCapacity() - fluidOutput.getFluidAmount());
        if (toPrint <= 0) return;

        // Check every matter type this fluid needs is available in sufficient quantity before
        // committing any drain, so a partial-match doesn't leave the tank half-drained for nothing.
        double scale = toPrint / 1000.0;
        for (MatterValue required : costPer1000mb.getValues().values()) {
            double needed = required.getAmount() * scale;
            MatterStack have = matterInput.getMatter();
            if (have.isEmpty() || have.getMatterType() != required.getMatter() || have.getAmount() < needed) {
                return; // missing or insufficient matter of this type - wait for more to arrive
            }
        }
        for (MatterValue required : costPer1000mb.getValues().values()) {
            matterInput.drain(required.getAmount() * scale, IFluidHandler.FluidAction.EXECUTE);
        }
        fluidOutput.fill(new FluidStack(selectedFluid, toPrint), IFluidHandler.FluidAction.EXECUTE);
        setChanged();
    }

    @Nullable
    private MatterCompound costFor(Fluid fluid) {
        // 1) explicit override recipe, if the world's RecipeManager has one for this fluid
        if (level != null) {
            for (var holder : level.getRecipeManager().getAllRecipesFor(
                    com.com.chaos.Recipes.ModFluidReplicatorRegistry.FLUID_MATTER_VALUE_RECIPE_TYPE.get())) {
                var recipe = holder.value();
                if (recipe.fluid == fluid) {
                    MatterCompound compound = new MatterCompound();
                    for (MatterValue value : recipe.matter) compound.add(value);
                    return compound.divide(recipe.amountMb / 1000.0);
                }
            }
        }
        // 2) fall back to whatever the Create recipe bridge derived automatically
        return CreateMatterCalculationBridge.FLUID_MATTER.get(fluid);
    }

    public IMatterHandler getMatterHandler() {
        return matterInput;
    }

    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        return fluidOutput;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("MatterInput", matterInput.serializeNBT(provider));
        tag.put("FluidOutput", fluidOutput.writeToNBT(provider, new CompoundTag()));
        if (selectedFluid != null) {
            tag.putString("SelectedFluid", net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(selectedFluid).toString());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        matterInput.deserializeNBT(provider, tag.getCompound("MatterInput"));
        fluidOutput.readFromNBT(provider, tag.getCompound("FluidOutput"));
        if (tag.contains("SelectedFluid")) {
            selectedFluid = net.minecraft.core.registries.BuiltInRegistries.FLUID.get(
                    net.minecraft.resources.ResourceLocation.parse(tag.getString("SelectedFluid")));
        }
    }
}
