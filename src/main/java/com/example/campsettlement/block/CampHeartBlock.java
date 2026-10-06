package com.example.campsettlement.block;
import com.example.campsettlement.data.CampSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class CampHeartBlock extends Block {
    public CampHeartBlock(Properties p) { super(p); }

    @Override
    public void onRemove(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel server) {
            CampSavedData data = CampSavedData.get(server);
            if (data.hasCamp() && data.getCampPos().equals(pos)) data.removeCamp();
        }
        super.onRemove(state, level, pos, newState, moving);
    }
}
