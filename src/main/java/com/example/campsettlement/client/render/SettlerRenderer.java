package com.example.campsettlement.client.render;

import com.example.campsettlement.CampSettlement;
import com.example.campsettlement.client.model.SettlerModel;
import com.example.campsettlement.entity.SettlerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SettlerRenderer extends MobRenderer<SettlerEntity, SettlerModel> {
    private static final ResourceLocation VISITOR = tex("visitor");
    private static final ResourceLocation SETTLER = tex("settler");
    private static final ResourceLocation FARMER = tex("farmer");
    private static final ResourceLocation BUILDER = tex("builder");
    private static final ResourceLocation GUARD = tex("guard");
    private static final ResourceLocation WOODCUTTER = tex("woodcutter");

    public SettlerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new SettlerModel(ctx.bakeLayer(SettlerModel.LAYER_LOCATION)), 0.5F);
    }

    private static ResourceLocation tex(String name) {
        return new ResourceLocation(CampSettlement.MOD_ID, "textures/entity/" + name + ".png");
    }

    @Override
    public ResourceLocation getTextureLocation(SettlerEntity entity) {
        if (!entity.isAccepted()) return VISITOR;
        return switch (entity.getJob()) {
            case "farmer" -> FARMER;
            case "builder" -> BUILDER;
            case "guard" -> GUARD;
            case "woodcutter" -> WOODCUTTER;
            default -> SETTLER;
        };
    }
}
