package com.com.chaos.Entities.Client;

import com.com.chaos.ChaoticsCreate;
import com.com.chaos.Entities.ChaosGolemEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ChaosGolemRenderer extends MobRenderer<ChaosGolemEntity, ChaosGolemCore<ChaosGolemEntity>> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ChaoticsCreate.MODID, "textures/entity/chaos_golem/chaos_golem_core.png");

    public ChaosGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new ChaosGolemCore<>(context.bakeLayer(ChaosGolemCore.LAYER_LOCATION)), 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(ChaosGolemEntity entity) {
        return TEXTURE;
    }
}
