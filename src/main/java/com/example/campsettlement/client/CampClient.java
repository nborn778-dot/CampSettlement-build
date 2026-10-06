package com.example.campsettlement.client;

import com.example.campsettlement.action.CampActions;
import com.example.campsettlement.building.BuildingManager;
import com.example.campsettlement.data.CampSavedData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;
import java.util.function.Consumer;

public final class CampClient {
    private CampClient() {}

    public record CampStats(int residents, int pending, int capacity, int tents, int farms, int lumber) {}

    public static void openCampScreen(BlockPos pos) {
        Minecraft.getInstance().setScreen(new CampScreen(pos));
    }

    public static void openSettlerScreen(int entityId) {
        Minecraft.getInstance().setScreen(new SettlerScreen(entityId));
    }

    public static void runServer(Consumer<ServerPlayer> action) {
        Minecraft mc = Minecraft.getInstance();
        MinecraftServer server = mc.getSingleplayerServer();
        if (server == null || mc.player == null) return;
        UUID id = mc.player.getUUID();

        server.execute(() -> {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null) action.accept(player);
        });
    }

    public static CampStats stats(BlockPos campPos) {
        MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null) return new CampStats(0,0,0,0,0,0);

        ServerLevel level = server.overworld();
        CampSavedData data = CampSavedData.get(level);
        if (!data.hasCamp()) return new CampStats(0,0,0,0,0,0);

        return new CampStats(
                CampActions.countAccepted(level, campPos),
                CampActions.countPending(level, campPos),
                data.residentCapacity(),
                data.countBuildings(BuildingManager.TENT),
                data.countBuildings(BuildingManager.FARM),
                data.countBuildings(BuildingManager.LUMBER)
        );
    }

    public static boolean slotFree(BlockPos campPos, String type, int slot) {
        MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null) return false;
        return BuildingManager.isSlotFree(server.overworld(), campPos, type, slot);
    }

    public static void build(BlockPos campPos, String type, int slot) {
        runServer(player -> CampActions.build(player, campPos, type, slot));
    }

    public static void acceptSettler(int entityId) {
        runServer(player -> CampActions.accept(player, entityId));
    }

    public static void rejectSettler(int entityId) {
        runServer(player -> CampActions.reject(player, entityId));
    }

    public static void assignJob(int entityId, String job) {
        runServer(player -> CampActions.assignJob(player, entityId, job));
    }
}
