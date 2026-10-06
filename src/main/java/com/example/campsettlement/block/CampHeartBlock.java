package com.example.campsettlement.block;

import com.example.campsettlement.data.CampSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class CampHeartBlock extends Block {
    public CampHeartBlock(Properties p) { super(p); }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            BlockPos campPos = pos.immutable();
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> com.example.campsettlement.client.CampClient.openCampScreen(campPos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel server) {
            CampSavedData data = CampSavedData.get(server.getServer().overworld());
            if (data.hasCamp() && data.getCampPos().equals(pos)) data.removeCamp();
        }
        super.onRemove(state, level, pos, newState, moving);
    }
}
