package com.example.campsettlement.client;

import com.example.campsettlement.CampSettlement;
import com.example.campsettlement.client.model.SettlerModel;
import com.example.campsettlement.client.render.SettlerRenderer;
import com.example.campsettlement.registry.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CampSettlement.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions e) {
        e.registerLayerDefinition(SettlerModel.LAYER_LOCATION, SettlerModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(ModEntities.SETTLER.get(), SettlerRenderer::new);
    }
}
