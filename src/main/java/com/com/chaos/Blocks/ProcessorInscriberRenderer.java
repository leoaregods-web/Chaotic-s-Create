package com.com.chaos.Blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public class ProcessorInscriberRenderer implements BlockEntityRenderer<ProcessorInscriberBlockEntity> {
    private final ItemRenderer itemRenderer;

    public ProcessorInscriberRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(ProcessorInscriberBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack stack = blockEntity.getRenderStack();
        if (stack.isEmpty()) {
            return;
        }

        // --- 1. Render the Horizontal Item on the Plate ---
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.075D, 0.5D);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));

        // Slow spin animation
        float rotationTime = (blockEntity.getLevel() != null) ? (blockEntity.getLevel().getGameTime() + partialTick) * 0.8F : 0.0F;
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotationTime));
        poseStack.scale(0.4F, 0.4F, 0.4F);

        itemRenderer.renderStatic(
                stack,
                ItemDisplayContext.FIXED,
                packedLight,
                packedOverlay,
                poseStack,
                bufferSource,
                blockEntity.getLevel(),
                0
        );
        poseStack.popPose();

        // --- 2. Render the Inscriber Laser (Only when actively processing) ---
        // We use blockEntity.getProgress() > 0 to know when to turn the beam on
        if (blockEntity.getLevel() != null && blockEntity.getProgress() > 0) {
            renderLaserBeam(poseStack, bufferSource, partialTick, blockEntity.getLevel().getGameTime());
        }
    }

    private void renderLaserBeam(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, long gameTime) {
        poseStack.pushPose();

        // Center the laser horizontally in the block
        poseStack.translate(0.5D, 0.0D, 0.5D);

        // Define beam geometry limits
        // Your ceiling model element sits at Y=8 voxels (0.5D height)
        // The item sits on the plate floor at Y=1 voxel (0.0625D height)
        float beamTopY = 0.5F;
        float beamBottomY = 0.075F;

        // Dynamic animation: Make the beam radius pulse slightly over time
        float pulse = (float) Math.sin((gameTime + partialTick) * 0.5F) * 0.01F;
        float radius = 0.02F + pulse; // Thin laser beam core

        // Choose a glowing render type that ignores light maps (always full bright)
        // RenderType.lightning() works perfectly for full-bright laser cores
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        // Laser color configuration (RGBA)
        // Let's make it a high-intensity energy Cyan/Blue laser
        int r = 0;
        int g = 220;
        int b = 255;
        int a = 255;

        // Draw a 4-sided vertical square column beam
        // Face 1 (North)
        renderLaserVertex(matrix, consumer, -radius, beamTopY, -radius, r, g, b, a);
        renderLaserVertex(matrix, consumer, radius, beamTopY, -radius, r, g, b, a);
        renderLaserVertex(matrix, consumer, radius, beamBottomY, -radius, r, g, b, a);
        renderLaserVertex(matrix, consumer, -radius, beamBottomY, -radius, r, g, b, a);

        // Face 2 (South)
        renderLaserVertex(matrix, consumer, radius, beamTopY, radius, r, g, b, a);
        renderLaserVertex(matrix, consumer, -radius, beamTopY, radius, r, g, b, a);
        renderLaserVertex(matrix, consumer, -radius, beamBottomY, radius, r, g, b, a);
        renderLaserVertex(matrix, consumer, radius, beamBottomY, radius, r, g, b, a);

        // Face 3 (East)
        renderLaserVertex(matrix, consumer, radius, beamTopY, -radius, r, g, b, a);
        renderLaserVertex(matrix, consumer, radius, beamTopY, radius, r, g, b, a);
        renderLaserVertex(matrix, consumer, radius, beamBottomY, radius, r, g, b, a);
        renderLaserVertex(matrix, consumer, radius, beamBottomY, -radius, r, g, b, a);

        // Face 4 (West)
        renderLaserVertex(matrix, consumer, -radius, beamTopY, radius, r, g, b, a);
        renderLaserVertex(matrix, consumer, -radius, beamTopY, -radius, r, g, b, a);
        renderLaserVertex(matrix, consumer, -radius, beamBottomY, -radius, r, g, b, a);
        renderLaserVertex(matrix, consumer, -radius, beamBottomY, radius, r, g, b, a);

        poseStack.popPose();
    }

    private void renderLaserVertex(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z, int r, int g, int b, int a) {
        // Build position geometry and apply color mapping properties directly
        consumer.addVertex(matrix, x, y, z)
                .setColor(r, g, b, a);
    }
}
