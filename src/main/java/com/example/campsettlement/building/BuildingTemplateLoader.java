package com.example.campsettlement.building;

import com.example.campsettlement.CampSettlement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public final class BuildingTemplateLoader {
    private BuildingTemplateLoader() {}

    public static BuildingTemplate load(ServerLevel level, String type) {
        try {
            ResourceLocation id = new ResourceLocation(CampSettlement.MOD_ID, "building_templates/" + type + ".json");
            Resource resource = level.getServer().getResourceManager().getResource(id)
                    .orElseThrow(() -> new IllegalStateException("Missing building template " + id));
            try (Reader reader = resource.openAsReader()) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                int width = root.get("width").getAsInt();
                int height = root.get("height").getAsInt();
                int depth = root.get("depth").getAsInt();
                List<BuildingTemplate.BlockEntry> blocks = new ArrayList<>();

                JsonArray arr = root.getAsJsonArray("blocks");
                for (var el : arr) {
                    JsonObject b = el.getAsJsonObject();
                    int x = b.get("x").getAsInt();
                    int y = b.get("y").getAsInt();
                    int z = b.get("z").getAsInt();
                    ResourceLocation blockId = new ResourceLocation(b.get("block").getAsString());
                    Block block = BuiltInRegistries.BLOCK.get(blockId);
                    BlockState state = block.defaultBlockState();
                    blocks.add(new BuildingTemplate.BlockEntry(new BlockPos(x, y, z), state));
                }
                return new BuildingTemplate(width, height, depth, List.copyOf(blocks));
            }
        } catch (Exception ex) {
            CampSettlement.LOGGER.error("Failed to load building template {}", type, ex);
            return null;
        }
    }
}
