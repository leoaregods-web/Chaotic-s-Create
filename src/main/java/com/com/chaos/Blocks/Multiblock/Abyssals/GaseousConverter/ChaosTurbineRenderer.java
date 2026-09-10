package com.com.chaos.Blocks.Multiblock.Abyssals.GaseousConverter;

import com.com.chaos.ChaoticsCreate;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The block JSON supplies the static shell. This renderer supplies the
 * independent internal rotor so only the blades spin with kinetic speed.
 */
public class ChaosTurbineRenderer extends SafeBlockEntityRenderer<ChaosTurbineBlockEntity> {
    public static final PartialModel BLADES = PartialModel.of(
            ResourceLocation.fromNamespaceAndPath(
                    ChaoticsCreate.MODID,
                    "block/chaos_turbine_blades"));

    public ChaosTurbineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(
            ChaosTurbineBlockEntity be,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int light,
            int overlay) {

        BlockState state = be.getBlockState();
        Direction facing = state.getValue(ChaosTurbineBlock.FACING);
        Direction.Axis axis = facing.getAxis();

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.cutoutMipped());
        SuperByteBuffer blades = CachedBuffers.partial(BLADES, state);

        // The Blockbench blade model is authored with its shaft running along
        // local +Z/SOUTH. Rotate that model so local +Z follows the block's FACING.
        float yRotation = AngleHelper.rad(AngleHelper.horizontalAngle(facing));
        blades.rotateCentered(yRotation, Direction.UP);

        // Create supplies the correct interpolated kinetic angle, including
        // position offsets and the block's current propagated speed.
        float angle = KineticBlockEntityRenderer.getAngleForBe(
                be,
                be.getBlockPos(),
                axis);
        blades.rotateCentered(angle, Direction.get(Direction.AxisDirection.POSITIVE, axis));

        blades
                .light(light)
                .renderInto(poseStack, vertexConsumer);
    }
}
