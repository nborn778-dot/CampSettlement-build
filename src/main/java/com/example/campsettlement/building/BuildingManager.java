package com.example.campsettlement.building;

import com.example.campsettlement.data.CampSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.LinkedHashMap;
import java.util.Map;

public final class BuildingManager {
    public static final String TENT = "tent";
    public static final String FARM = "farm";
    public static final String LUMBER = "lumber";

    private static final int[][] OFFSETS = {
            {10,0},{-10,0},{0,10},{0,-10},
            {10,10},{10,-10},{-10,10},{-10,-10}
    };

    private BuildingManager() {}

    public static int slotCount() { return OFFSETS.length; }

    public static String displayName(String type) {
        return switch (type) {
            case TENT -> "Палатка";
            case FARM -> "Огород";
            case LUMBER -> "Лесной навес";
            default -> type;
        };
    }

    public static String costText(String type) {
        return switch (type) {
            case TENT -> "12 досок + 16 белой шерсти";
            case FARM -> "12 заборов + 24 земли + 8 семян";
            case LUMBER -> "16 брёвен + 12 досок + 8 булыжника";
            default -> "";
        };
    }

    public static Map<Item,Integer> cost(String type) {
        LinkedHashMap<Item,Integer> out = new LinkedHashMap<>();
        switch (type) {
            case TENT -> {
                out.put(Items.OAK_PLANKS, 12);
                out.put(Items.WHITE_WOOL, 16);
            }
            case FARM -> {
                out.put(Items.OAK_FENCE, 12);
                out.put(Items.DIRT, 24);
                out.put(Items.WHEAT_SEEDS, 8);
            }
            case LUMBER -> {
                out.put(Items.OAK_LOG, 16);
                out.put(Items.OAK_PLANKS, 12);
                out.put(Items.COBBLESTONE, 8);
            }
        }
        return out;
    }

    public static BlockPos plotAnchor(ServerLevel level, BlockPos campPos, String type, int slot) {
        if (slot < 0 || slot >= OFFSETS.length) return null;
        BuildingTemplate template = BuildingTemplateLoader.load(level, type);
        if (template == null) return null;
        int cx = campPos.getX() + OFFSETS[slot][0];
        int cz = campPos.getZ() + OFFSETS[slot][1];
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cx, cz);
        return new BlockPos(cx - template.width()/2, y, cz - template.depth()/2);
    }

    public static boolean isSlotFree(ServerLevel level, BlockPos campPos, String type, int slot) {
        BuildingTemplate template = BuildingTemplateLoader.load(level, type);
        BlockPos anchor = plotAnchor(level, campPos, type, slot);
        if (template == null || anchor == null) return false;

        for (int x = 0; x < template.width(); x++) {
            for (int z = 0; z < template.depth(); z++) {
                BlockPos below = anchor.offset(x, -1, z);
                if (level.getBlockState(below).isAir()) return false;
                for (int y = 0; y < template.height(); y++) {
                    BlockPos p = anchor.offset(x, y, z);
                    if (!level.getBlockState(p).canBeReplaced()) return false;
                }
            }
        }
        return true;
    }

    public static boolean build(ServerPlayer player, BlockPos campPos, String type, int slot) {
        ServerLevel level = player.serverLevel();
        CampSavedData data = CampSavedData.get(level.getServer().overworld());
        if (!data.hasCamp() || !data.getCampPos().equals(campPos)) {
            player.sendSystemMessage(Component.literal("§cЛагерь не найден."));
            return false;
        }

        BuildingTemplate template = BuildingTemplateLoader.load(level, type);
        BlockPos anchor = plotAnchor(level, campPos, type, slot);
        if (template == null || anchor == null || !isSlotFree(level, campPos, type, slot)) {
            player.sendSystemMessage(Component.literal("§cЭта площадка занята или не подходит."));
            return false;
        }

        if (!player.getAbilities().instabuild && !hasCost(player, type)) {
            player.sendSystemMessage(Component.literal("§cНе хватает ресурсов: " + costText(type)));
            return false;
        }
        if (!player.getAbilities().instabuild) takeCost(player, type);

        for (BuildingTemplate.BlockEntry e : template.blocks()) {
            level.setBlock(anchor.offset(e.offset()), e.state(), 3);
        }
        data.addBuilding(type, anchor);
        player.sendSystemMessage(Component.literal("§aПостроено: " + displayName(type)));
        return true;
    }

    private static boolean hasCost(ServerPlayer player, String type) {
        for (Map.Entry<Item,Integer> req : cost(type).entrySet()) {
            int count = 0;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.is(req.getKey())) count += stack.getCount();
            }
            if (count < req.getValue()) return false;
        }
        return true;
    }

    private static void takeCost(ServerPlayer player, String type) {
        for (Map.Entry<Item,Integer> req : cost(type).entrySet()) {
            int left = req.getValue();
            for (int i = 0; i < player.getInventory().getContainerSize() && left > 0; i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.is(req.getKey())) continue;
                int take = Math.min(left, stack.getCount());
                stack.shrink(take);
                left -= take;
            }
        }
    }
}
