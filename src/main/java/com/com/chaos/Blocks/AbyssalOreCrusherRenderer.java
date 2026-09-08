package com.com.chaos.Blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class AbyssalOreCrusherRenderer implements BlockEntityRenderer<AbyssalOreCrusherBlockEntity> {
    private final ItemRenderer itemRenderer;

    public AbyssalOreCrusherRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(AbyssalOreCrusherBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack stack = blockEntity.getRenderStack();
        if (stack.isEmpty()) return;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.6D, 0.5D);

        if (blockEntity.getLevel() != null && blockEntity.isProcessing()) {
            float bounce = (float) Math.abs(Math.sin((blockEntity.getLevel().getGameTime() + partialTick) * 0.5F)) * 0.05F;
            poseStack.translate(0.0D, bounce, 0.0D);
        }

        poseStack.scale(0.5F, 0.5F, 0.5F);
        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                poseStack, bufferSource, blockEntity.getLevel(), 0);

        poseStack.popPose();
    }
}