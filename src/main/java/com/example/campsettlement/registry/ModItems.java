package com.example.campsettlement.registry;
import com.example.campsettlement.CampSettlement;
import com.example.campsettlement.item.CampHeartItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CampSettlement.MOD_ID);
    public static final RegistryObject<Item> CAMP_HEART = ITEMS.register("camp_heart",
        () -> new CampHeartItem(new Item.Properties().stacksTo(1)));
}
