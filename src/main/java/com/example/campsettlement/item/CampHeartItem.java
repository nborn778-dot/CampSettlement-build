package com.example.campsettlement.item;

import com.example.campsettlement.data.CampSavedData;
import com.example.campsettlement.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public class CampHeartItem extends Item {
    public CampHeartItem(Properties p) { super(p); }

    @Override
    public InteractionResult useOn(UseOnContext c) {
        if (!(c.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        CampSavedData data = CampSavedData.get(level.getServer().overworld());
        if (data.hasCamp()) {
            if (c.getPlayer() != null) c.getPlayer().sendSystemMessage(Component.literal("§cУ тебя уже есть лагерь."));
            return InteractionResult.FAIL;
        }

        BlockPos pos = c.getClickedPos().relative(c.getClickedFace());
        if (!level.getBlockState(pos).canBeReplaced()) return InteractionResult.FAIL;

        level.setBlock(pos, ModBlocks.CAMP_HEART.get().defaultBlockState(), 3);
        data.createCamp(pos, level.getGameTime());

        if (c.getPlayer() != null && !c.getPlayer().getAbilities().instabuild) c.getItemInHand().shrink(1);
        if (c.getPlayer() != null) c.getPlayer().sendSystemMessage(Component.literal("§aЛагерь основан. Нажми ПКМ по костру, чтобы открыть управление."));
        return InteractionResult.CONSUME;
    }
}
