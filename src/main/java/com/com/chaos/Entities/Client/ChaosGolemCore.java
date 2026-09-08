package com.com.chaos.Entities.Client;

import com.com.chaos.ChaoticsCreate;
import com.com.chaos.Entities.ChaosGolemEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

public class ChaosGolemCore<T extends ChaosGolemEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ChaoticsCreate.MODID, "chaos_golem_core"), "main");
    private final ModelPart root;
    private final ModelPart Core;
    private final ModelPart bone16;
    private final ModelPart bone3;
    private final ModelPart bone15;
    private final ModelPart bone4;
    private final ModelPart bone17;
    private final ModelPart bone2;
    private final ModelPart bone14;
    private final ModelPart bone;
    private final ModelPart bone10;
    private final ModelPart bone7;
    private final ModelPart bone12;
    private final ModelPart bone8;
    private final ModelPart bone5;
    private final ModelPart bone6;
    private final ModelPart bone11;
    private final ModelPart bone9;
    private final ModelPart bone13;
    private final ModelPart bone22;
    private final ModelPart bone19;
    private final ModelPart bone24;
    private final ModelPart bone21;
    private final ModelPart bone23;
    private final ModelPart bone25;
    private final ModelPart bone20;
    private final ModelPart bone18;
    private final ModelPart bone26;
    private final ModelPart Ring1;
    private final ModelPart Ring2;

    public ChaosGolemCore(ModelPart root) {
        this.root = root;
        this.Core = root.getChild("Core");
        this.bone16 = this.Core.getChild("bone16");
        this.bone3 = this.Core.getChild("bone3");
        this.bone15 = this.Core.getChild("bone15");
        this.bone4 = this.Core.getChild("bone4");
        this.bone17 = this.Core.getChild("bone17");
        this.bone2 = this.Core.getChild("bone2");
        this.bone14 = this.Core.getChild("bone14");
        this.bone = this.Core.getChild("bone");
        this.bone10 = this.Core.getChild("bone10");
        this.bone7 = this.Core.getChild("bone7");
        this.bone12 = this.Core.getChild("bone12");
        this.bone8 = this.Core.getChild("bone8");
        this.bone5 = this.Core.getChild("bone5");
        this.bone6 = this.Core.getChild("bone6");
        this.bone11 = this.Core.getChild("bone11");
        this.bone9 = this.Core.getChild("bone9");
        this.bone13 = this.Core.getChild("bone13");
        this.bone22 = this.Core.getChild("bone22");
        this.bone19 = this.Core.getChild("bone19");
        this.bone24 = this.Core.getChild("bone24");
        this.bone21 = this.Core.getChild("bone21");
        this.bone23 = this.Core.getChild("bone23");
        this.bone25 = this.Core.getChild("bone25");
        this.bone20 = this.Core.getChild("bone20");
        this.bone18 = this.Core.getChild("bone18");
        this.bone26 = this.Core.getChild("bone26");
        this.Ring1 = root.getChild("Ring1");
        this.Ring2 = root.getChild("Ring2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Core = partdefinition.addOrReplaceChild("Core", CubeListBuilder.create(), PartPose.offset(0.0F, 15.0F, 0.0F));

        PartDefinition bone16 = Core.addOrReplaceChild("bone16", CubeListBuilder.create().texOffs(32, 6).addBox(-3.0F, -2.0F, -4.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bone3 = Core.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(32, 22).addBox(-3.0F, -2.0F, -4.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 0.0F));

        PartDefinition bone15 = Core.addOrReplaceChild("bone15", CubeListBuilder.create(), PartPose.offset(-7.0F, 0.0F, 0.0F));

        PartDefinition cube_r1 = bone15.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(32, 10).addBox(-3.0F, -2.0F, -4.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone4 = Core.addOrReplaceChild("bone4", CubeListBuilder.create(), PartPose.offset(-7.0F, -1.0F, 0.0F));

        PartDefinition cube_r2 = bone4.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(32, 18).addBox(-3.0F, -2.0F, -4.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone17 = Core.addOrReplaceChild("bone17", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r3 = bone17.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(32, 12).addBox(-3.0F, -2.0F, -4.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone2 = Core.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offset(0.0F, -1.0F, 0.0F));

        PartDefinition cube_r4 = bone2.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 36).addBox(-3.0F, -2.0F, -4.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone14 = Core.addOrReplaceChild("bone14", CubeListBuilder.create().texOffs(32, 8).addBox(-3.0F, -2.0F, -4.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 7.0F));

        PartDefinition bone = Core.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(32, 20).addBox(-3.0F, -2.0F, -4.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 7.0F));

        PartDefinition bone10 = Core.addOrReplaceChild("bone10", CubeListBuilder.create().texOffs(36, 34).addBox(-1.0F, -1.0F, -2.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, 0.0F));

        PartDefinition bone7 = Core.addOrReplaceChild("bone7", CubeListBuilder.create(), PartPose.offset(0.0F, -4.0F, 0.0F));

        PartDefinition cube_r5 = bone7.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(44, 34).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition bone12 = Core.addOrReplaceChild("bone12", CubeListBuilder.create(), PartPose.offset(0.0F, -4.0F, 0.0F));

        PartDefinition cube_r6 = bone12.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, 38).addBox(-1.0F, -1.0F, -2.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone8 = Core.addOrReplaceChild("bone8", CubeListBuilder.create().texOffs(24, 36).addBox(-2.0F, -1.0F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, 0.0F));

        PartDefinition bone5 = Core.addOrReplaceChild("bone5", CubeListBuilder.create().texOffs(24, 24).addBox(-3.0F, -1.0F, -2.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.0F, 0.0F));

        PartDefinition bone6 = Core.addOrReplaceChild("bone6", CubeListBuilder.create(), PartPose.offset(0.0F, -3.0F, 0.0F));

        PartDefinition cube_r7 = bone6.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 30).addBox(-3.0F, -1.0F, -2.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition bone11 = Core.addOrReplaceChild("bone11", CubeListBuilder.create(), PartPose.offset(0.0F, -3.0F, 0.0F));

        PartDefinition cube_r8 = bone11.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(14, 32).addBox(-3.0F, -1.0F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition bone9 = Core.addOrReplaceChild("bone9", CubeListBuilder.create().texOffs(32, 14).addBox(-3.0F, -1.0F, -3.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.0F, 0.0F));

        PartDefinition bone13 = Core.addOrReplaceChild("bone13", CubeListBuilder.create().texOffs(34, 36).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -5.0F, 0.0F));

        PartDefinition bone22 = Core.addOrReplaceChild("bone22", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, 0.0F));

        PartDefinition cube_r9 = bone22.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(46, 12).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition bone19 = Core.addOrReplaceChild("bone19", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r10 = bone19.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(24, 30).addBox(-3.0F, -1.0F, -2.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition bone24 = Core.addOrReplaceChild("bone24", CubeListBuilder.create().texOffs(14, 37).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, 0.0F));

        PartDefinition bone21 = Core.addOrReplaceChild("bone21", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, 0.0F));

        PartDefinition cube_r11 = bone21.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(34, 39).addBox(-1.0F, -1.0F, -2.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone23 = Core.addOrReplaceChild("bone23", CubeListBuilder.create().texOffs(36, 24).addBox(-2.0F, -1.0F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, 0.0F));

        PartDefinition bone25 = Core.addOrReplaceChild("bone25", CubeListBuilder.create().texOffs(0, 40).addBox(-1.0F, -1.0F, -2.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, 0.0F));

        PartDefinition bone20 = Core.addOrReplaceChild("bone20", CubeListBuilder.create().texOffs(32, 0).addBox(-3.0F, -1.0F, -2.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bone18 = Core.addOrReplaceChild("bone18", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r12 = bone18.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(36, 29).addBox(-3.0F, -1.0F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition bone26 = Core.addOrReplaceChild("bone26", CubeListBuilder.create().texOffs(32, 16).addBox(-3.0F, -1.0F, -3.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Ring1 = partdefinition.addOrReplaceChild("Ring1", CubeListBuilder.create().texOffs(0, 18).addBox(-7.0F, -5.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(44, 3).addBox(-6.0F, 5.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(14, 43).addBox(-7.0F, 5.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(44, 0).addBox(-6.0F, 6.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(42, 36).addBox(5.0F, 5.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 42).addBox(5.0F, 6.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(38, 41).addBox(6.0F, 5.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(24, 0).addBox(5.0F, -5.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 13.0F, 0.0F));

        PartDefinition cube_r13 = Ring1.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(20, 41).addBox(0.0F, -7.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -6.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition cube_r14 = Ring1.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(32, 41).addBox(0.0F, -7.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition cube_r15 = Ring1.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(26, 41).addBox(0.0F, -7.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -7.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition cube_r16 = Ring1.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(24, 12).addBox(-1.0F, -15.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.0F, 6.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition cube_r17 = Ring1.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(8, 38).addBox(0.0F, -7.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-12.0F, -6.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition cube_r18 = Ring1.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(14, 40).addBox(0.0F, -7.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-12.0F, -7.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition cube_r19 = Ring1.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(8, 41).addBox(0.0F, -7.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-13.0F, -6.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition cube_r20 = Ring1.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(8, 18).addBox(-1.0F, -15.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.0F, -6.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition Ring2 = partdefinition.addOrReplaceChild("Ring2", CubeListBuilder.create().texOffs(0, 0).addBox(-10.0F, -8.0F, -1.0F, 2.0F, 16.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(32, 44).addBox(-8.0F, 8.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(38, 44).addBox(-9.0F, 8.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(44, 39).addBox(-8.0F, 9.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(44, 42).addBox(7.0F, 8.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 45).addBox(7.0F, 9.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(44, 45).addBox(8.0F, 8.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(8, 0).addBox(8.0F, -8.0F, -1.0F, 2.0F, 16.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 13.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition cube_r21 = Ring2.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(12, 46).addBox(0.0F, -7.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -9.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition cube_r22 = Ring2.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(46, 9).addBox(0.0F, -7.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -9.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition cube_r23 = Ring2.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(46, 6).addBox(0.0F, -7.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -10.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition cube_r24 = Ring2.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(16, 16).addBox(-1.0F, -17.0F, -1.0F, 2.0F, 14.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.0F, 10.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition cube_r25 = Ring2.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(26, 44).addBox(0.0F, -7.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-14.0F, -9.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition cube_r26 = Ring2.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(20, 44).addBox(0.0F, -7.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-14.0F, -10.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition cube_r27 = Ring2.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(6, 44).addBox(0.0F, -7.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-15.0F, -9.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition cube_r28 = Ring2.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(16, 0).addBox(-1.0F, -17.0F, -1.0F, 2.0F, 14.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.0F, -10.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.animate(entity.idleAnimationState, ChaosGolemCoreAnimation.IdleCore, ageInTicks, 1.0F);
        this.animate(entity.deathAnimationState, ChaosGolemCoreAnimation.DeathCore, ageInTicks, 1.0F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}