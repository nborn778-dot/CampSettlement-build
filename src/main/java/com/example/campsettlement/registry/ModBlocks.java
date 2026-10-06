package com.example.campsettlement.registry;
import com.example.campsettlement.CampSettlement;
import com.example.campsettlement.block.CampHeartBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CampSettlement.MOD_ID);
    public static final RegistryObject<Block> CAMP_HEART = BLOCKS.register("camp_heart",
        () -> new CampHeartBlock(BlockBehaviour.Properties.of().strength(5.0F).sound(SoundType.STONE)));
}
