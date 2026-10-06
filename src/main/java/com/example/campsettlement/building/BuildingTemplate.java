package com.example.campsettlement.building;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public record BuildingTemplate(int width, int height, int depth, List<BlockEntry> blocks) {
    public record BlockEntry(BlockPos offset, BlockState state) {}
}
