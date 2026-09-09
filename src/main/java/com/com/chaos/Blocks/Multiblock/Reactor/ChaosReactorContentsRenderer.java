package com.com.chaos.Blocks.Multiblock.Reactor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

/**
 * Draws the reactor's current fluid/gas and item contents inside the three
 * hollow core cells of a validated Chaos Reactor. Only ever runs for the
 * controller (front) crystal, and only once the structure is valid - side
 * and structural crystals render nothing.
 *
 * Everything here is positioned relative to the controller's own render
 * origin and clipped to the shaft's footprint, so contents never draw
 * outside the casing shell regardless of fill level.
 */
public class ChaosReactorContentsRenderer implements BlockEntityRenderer<ChaosCrystalBlockEntity> {

    // Bottom core cell: fluid level. Middle: floating solid item. Top: gas wisp.
    private static final int FLUID_CELL_UP = -1;
    private static final int ITEM_CELL_UP = 0;
    private static final int GAS_CELL_UP = 1;

    public ChaosReactorContentsRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ChaosCrystalBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        BlockPos anchor = be.getBlockPos();
        Direction facing = be.getFacing();

        FluidStack fluid = be.getDisplayFluid();
        ItemStack item = be.getDisplayItem();

        if (!fluid.isEmpty()) {
            renderFluidCell(poseStack, bufferSource, packedLight, anchor, facing, fluid);
            // A gas-like fill also gets a soft wisp up top so it visibly fills the shaft.
            renderGasWisp(poseStack, bufferSource, packedLight, anchor, facing, fluid, partialTick);
        }

        if (!item.isEmpty()) {
            renderFloatingItem(be, poseStack, bufferSource, packedLight, packedOverlay, anchor, facing, item, partialTick);
        }
    }

    private void translateToCore(PoseStack poseStack, BlockPos anchor, Direction facing, int up) {
        BlockPos target = ChaosReactorStructure.resolve(anchor, facing,
            ChaosReactorStructure.CORE_RIGHT, ChaosReactorStructure.CORE_BACK, up);
        poseStack.translate(
            target.getX() - anchor.getX(),
            target.getY() - anchor.getY(),
            target.getZ() - anchor.getZ()
        );
    }

    private void renderFluidCell(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                                  BlockPos anchor, Direction facing, FluidStack fluid) {
        poseStack.pushPose();
        translateToCore(poseStack, anchor, facing, FLUID_CELL_UP);

        int color = IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor(fluid);
        float a = 0.75f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        // Fill height scales with how full a "full tank" would be, kept simple here
        // since the buffer is a single-fluid, single-tank buffer.
        float fillHeight = Math.min(1.0f,
            fluid.getAmount() / (float) ChaosCrystalBlockEntity.TANK_CAPACITY_MB);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
        addFilledBox(consumer, poseStack, 0.2f, 0.0f, 0.2f, 0.8f, fillHeight, 0.8f, r, g, b, a, packedLight);

        poseStack.popPose();
    }

    private void renderGasWisp(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                                BlockPos anchor, Direction facing, FluidStack fluid, float partialTick) {
        poseStack.pushPose();
        translateToCore(poseStack, anchor, facing, GAS_CELL_UP);
        poseStack.translate(0.5, 0.5, 0.5);

        // Cheap billboard-ish cross of two quads, softly pulsing - swap for a proper
        // particle-sheet texture pass once you've got gas art to work with.
        int color = IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor(fluid);
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float pulse = 0.35f + 0.15f * (float) Math.sin((System.currentTimeMillis() / 250.0) + partialTick);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
        poseStack.pushPose();
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(45));
        addFilledBox(consumer, poseStack, -0.3f, -0.3f, -0.01f, 0.3f, 0.3f, 0.01f, r, g, b, pulse, packedLight);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-45));
        addFilledBox(consumer, poseStack, -0.3f, -0.3f, -0.01f, 0.3f, 0.3f, 0.01f, r, g, b, pulse, packedLight);
        poseStack.popPose();

        poseStack.popPose();
    }

    private void renderFloatingItem(ChaosCrystalBlockEntity be, PoseStack poseStack, MultiBufferSource bufferSource,
                                     int packedLight, int packedOverlay, BlockPos anchor, Direction facing,
                                     ItemStack item, float partialTick) {
        poseStack.pushPose();
        translateToCore(poseStack, anchor, facing, ITEM_CELL_UP);
        poseStack.translate(0.5, 0.5 + 0.1f * (float) Math.sin(System.currentTimeMillis() / 400.0), 0.5);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees((System.currentTimeMillis() / 20L) % 360L));
        poseStack.scale(0.6f, 0.6f, 0.6f);

        net.minecraft.client.Minecraft.getInstance().getItemRenderer().renderStatic(
            item, net.minecraft.world.item.ItemDisplayContext.GROUND,
            packedLight, packedOverlay, poseStack, bufferSource, be.getLevel(), 0);

        poseStack.popPose();
    }

    /** Axis-aligned filled box, min -> max in local space, single flat color. */
    private void addFilledBox(VertexConsumer consumer, PoseStack poseStack,
                               float minX, float minY, float minZ, float maxX, float maxY, float maxZ,
                               float r, float g, float b, float a, int packedLight) {
        var pose = poseStack.last();
        // Top and bottom faces are enough to read clearly as "liquid in a shaft"
        // from the normal player viewing angle; add the four sides if you want
        // it readable from directly beside the reactor too.
        quad(consumer, pose, packedLight, r, g, b, a,
            minX, maxY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, minX, maxY, maxZ, 0, 1, 0);
        quad(consumer, pose, packedLight, r, g, b, a,
            minX, minY, maxZ, maxX, minY, maxZ, maxX, minY, minZ, minX, minY, minZ, 0, -1, 0);
    }

    private void quad(VertexConsumer consumer, PoseStack.Pose pose, int packedLight,
                       float r, float g, float b, float a,
                       float x1, float y1, float z1, float x2, float y2, float z2,
                       float x3, float y3, float z3, float x4, float y4, float z4,
                       float nx, float ny, float nz) {
        consumer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setUv(0, 0)
            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setUv(1, 0)
            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x3, y3, z3).setColor(r, g, b, a).setUv(1, 1)
            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x4, y4, z4).setColor(r, g, b, a).setUv(0, 1)
            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, nx, ny, nz);
    }
}
