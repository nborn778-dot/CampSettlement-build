package com.example.campsettlement.action;

import com.example.campsettlement.building.BuildingManager;
import com.example.campsettlement.data.CampSavedData;
import com.example.campsettlement.entity.SettlerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

public final class CampActions {
    private CampActions() {}

    private static ServerLevel overworld(ServerPlayer player) {
        return player.serverLevel().getServer().overworld();
    }

    public static int countAccepted(ServerLevel level, BlockPos camp) {
        return level.getEntitiesOfClass(SettlerEntity.class, new AABB(camp).inflate(80),
                SettlerEntity::isAccepted).size();
    }

    public static int countPending(ServerLevel level, BlockPos camp) {
        return level.getEntitiesOfClass(SettlerEntity.class, new AABB(camp).inflate(80),
                s -> !s.isAccepted()).size();
    }

    public static void accept(ServerPlayer player, int entityId) {
        CampSavedData data = CampSavedData.get(overworld(player));
        Entity entity = player.serverLevel().getEntity(entityId);
        if (!(entity instanceof SettlerEntity settler) || settler.isAccepted()) return;

        int residents = countAccepted(player.serverLevel(), data.getCampPos());
        if (residents >= data.residentCapacity()) {
            player.sendSystemMessage(Component.literal("§cНет свободного места. Построй ещё палатку."));
            return;
        }

        settler.setAccepted(true);
        settler.setJob("idle");
        data.setNextVisitor(player.serverLevel().getGameTime() + 2400L);
        player.sendSystemMessage(Component.literal("§aПутник принят в поселение."));
    }

    public static void reject(ServerPlayer player, int entityId) {
        Entity entity = player.serverLevel().getEntity(entityId);
        if (entity instanceof SettlerEntity settler && !settler.isAccepted()) {
            settler.discard();
            CampSavedData data = CampSavedData.get(overworld(player));
            data.setNextVisitor(player.serverLevel().getGameTime() + 1200L);
            player.sendSystemMessage(Component.literal("§7Путник ушёл."));
        }
    }

    public static void assignJob(ServerPlayer player, int entityId, String job) {
        Entity entity = player.serverLevel().getEntity(entityId);
        if (!(entity instanceof SettlerEntity settler) || !settler.isAccepted()) return;

        CampSavedData data = CampSavedData.get(overworld(player));
        if ("farmer".equals(job) && data.countBuildings(BuildingManager.FARM) <= 0) {
            player.sendSystemMessage(Component.literal("§cСначала построй огород."));
            return;
        }
        if ("woodcutter".equals(job) && data.countBuildings(BuildingManager.LUMBER) <= 0) {
            player.sendSystemMessage(Component.literal("§cСначала построй лесной навес."));
            return;
        }

        settler.setJob(job);
        player.sendSystemMessage(Component.literal("§aРабота назначена: " + jobName(job)));
    }

    public static void build(ServerPlayer player, BlockPos campPos, String type, int slot) {
        BuildingManager.build(player, campPos, type, slot);
    }

    public static String jobName(String job) {
        return switch (job) {
            case "farmer" -> "фермер";
            case "builder" -> "строитель";
            case "guard" -> "страж";
            case "woodcutter" -> "лесоруб";
            default -> "без работы";
        };
    }
}
