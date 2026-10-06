package com.example.campsettlement.client;

import com.example.campsettlement.CampSettlement;
import com.example.campsettlement.data.CampSavedData;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid=CampSettlement.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public class CampClient {
    public static final KeyMapping OPEN_CAMP = new KeyMapping("key.campsettlement.open", GLFW.GLFW_KEY_G, "key.categories.campsettlement");

    @SubscribeEvent public static void keys(RegisterKeyMappingsEvent e){e.register(OPEN_CAMP);}

    @Mod.EventBusSubscriber(modid=CampSettlement.MOD_ID,value=Dist.CLIENT)
    public static class InputHandler {
        @SubscribeEvent public static void input(InputEvent.Key e){
            if(e.getAction()==GLFW.GLFW_PRESS && OPEN_CAMP.consumeClick()){
                Minecraft mc=Minecraft.getInstance();
                if(mc.player!=null && mc.getSingleplayerServer()!=null) mc.setScreen(new CampScreen());
            }
        }
    }

    public static void serverAction(java.util.function.Consumer<CampSavedData> action){
        Minecraft mc=Minecraft.getInstance();
        if(mc.getSingleplayerServer()==null)return;
        mc.getSingleplayerServer().execute(()->{
            var level=mc.getSingleplayerServer().overworld();
            CampSavedData d=CampSavedData.get(level);
            if(d.hasCamp()) action.accept(d);
        });
    }
}
