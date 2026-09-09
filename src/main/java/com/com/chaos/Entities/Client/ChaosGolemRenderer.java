package com.com.chaos.Entities.Client;

import com.com.chaos.ChaoticsCreate;
import com.com.chaos.Entities.ChaosGolemEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ChaosGolemRenderer extends MobRenderer<ChaosGolemEntity, ChaosGolemCore<ChaosGolemEntity>> {

    private static final ResourceLocation TEXTURE_PHASE_1 = ResourceLocation.fromNamespaceAndPath(
            ChaoticsCreate.MODID, "textures/entity/chaos_golem/chaos_golem_core.png");
    // these two don't exist yet - paint phase2/phase3 variants (cracked/glowing) at the same UV
    // layout as chaos_golem_core.png and drop them next to it; until then they'll just render
    // as the missing-texture checkerboard instead of crashing
    private static final ResourceLocation TEXTURE_PHASE_2 = ResourceLocation.fromNamespaceAndPath(
            ChaoticsCreate.MODID, "textures/entity/chaos_golem/chaos_golem_core_phase2.png");
    private static final ResourceLocation TEXTURE_PHASE_3 = ResourceLocation.fromNamespaceAndPath(
            ChaoticsCreate.MODID, "textures/entity/chaos_golem/chaos_golem_core_phase3.png");

    public ChaosGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new ChaosGolemCore<>(context.bakeLayer(ChaosGolemCore.LAYER_LOCATION)), 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(ChaosGolemEntity entity) {
        return switch (entity.getPhase()) {
            case 3 -> TEXTURE_PHASE_3;
            case 2 -> TEXTURE_PHASE_2;
            default -> TEXTURE_PHASE_1;
        };
    }

    @Override
    protected void scale(ChaosGolemEntity entity, PoseStack poseStack, float partialTickTime) {
        // ~10% bigger per phase - purely visual, the hitbox/attributes are unchanged, so it'll
        // look like it's swelling with power without needing to touch its collision box at runtime
        float scale = 1.0F + (entity.getPhase() - 1) * 0.1F;
        poseStack.scale(scale, scale, scale);
    }
}
