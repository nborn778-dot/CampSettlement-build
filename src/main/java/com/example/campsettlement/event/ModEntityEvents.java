package com.example.campsettlement.event;

import com.example.campsettlement.CampSettlement;
import com.example.campsettlement.entity.SettlerEntity;
import com.example.campsettlement.registry.ModEntities;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CampSettlement.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntityEvents {
    @SubscribeEvent
    public static void attributes(EntityAttributeCreationEvent e) {
        e.put(ModEntities.SETTLER.get(), SettlerEntity.createAttributes().build());
    }
}
