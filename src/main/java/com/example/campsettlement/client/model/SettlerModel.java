package com.example.campsettlement.client.model;

import com.example.campsettlement.CampSettlement;
import com.example.campsettlement.entity.SettlerEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

public class SettlerModel extends HumanoidModel<SettlerEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(CampSettlement.MOD_ID, "settler"), "main");

    public SettlerModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.getChild("body");
        body.addOrReplaceChild("backpack",
                CubeListBuilder.create()
                        .texOffs(32, 32)
                        .addBox(-3.5F, 2.0F, 2.0F, 7.0F, 8.0F, 3.0F, new CubeDeformation(0.1F)),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }
}
