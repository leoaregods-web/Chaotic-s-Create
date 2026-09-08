package com.com.chaos.Blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CapsuleConcentratorRenderer implements BlockEntityRenderer<CapsuleConcentratorBlockEntity> {
    private final ItemRenderer itemRenderer;

    public CapsuleConcentratorRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(CapsuleConcentratorBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack stack = blockEntity.getRenderStack();
        if (stack.isEmpty()) {
            return;
        }

        poseStack.pushPose();

        // center of the inner chamber
        poseStack.translate(0.5D, 0.53125D, 0.5D);

        // slow spin while displayed
        if (blockEntity.getLevel() != null) {
            float rotation = (blockEntity.getLevel().getGameTime() + partialTick) * 2.0F;
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        }

        poseStack.scale(0.55F, 0.55F, 0.55F);

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
    }
}