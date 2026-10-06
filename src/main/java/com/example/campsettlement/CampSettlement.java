package com.example.campsettlement;

import com.example.campsettlement.registry.ModBlocks;
import com.example.campsettlement.registry.ModEntities;
import com.example.campsettlement.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(CampSettlement.MOD_ID)
public class CampSettlement {
    public static final String MOD_ID = "campsettlement";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CampSettlement() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(bus);
        ModBlocks.BLOCKS.register(bus);
        ModEntities.ENTITIES.register(bus);
        bus.addListener(this::creativeTab);
        MinecraftForge.EVENT_BUS.register(com.example.campsettlement.event.CampEvents.class);
    }

    private void creativeTab(BuildCreativeModeTabContentsEvent e) {
        if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) e.accept(ModItems.CAMP_HEART);
    }
}
