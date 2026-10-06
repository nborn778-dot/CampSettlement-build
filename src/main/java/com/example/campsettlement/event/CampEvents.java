package com.example.campsettlement.event;

import com.example.campsettlement.CampSettlement;
import com.example.campsettlement.action.CampActions;
import com.example.campsettlement.building.BuildingManager;
import com.example.campsettlement.data.CampSavedData;
import com.example.campsettlement.entity.SettlerEntity;
import com.example.campsettlement.registry.ModEntities;
import com.example.campsettlement.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = CampSettlement.MOD_ID)
public class CampEvents {
    private static final Random R = new Random();

    @SubscribeEvent
    public static void playerLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer player)) return;
        ServerLevel overworld = player.getServer().overworld();
        CampSavedData data = CampSavedData.get(overworld);

        if (!data.hasCamp() && !hasCampHeart(player)) {
            moveHeldItemAway(player);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.CAMP_HEART.get()));
            player.sendSystemMessage(Component.literal("§6Ты получил Сердце лагеря. Поставь его в подходящем месте."));
        }
    }

    private static boolean hasCampHeart(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(ModItems.CAMP_HEART.get())) return true;
        }
        return false;
    }

    private static void moveHeldItemAway(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) return;

        ItemStack saved = held.copy();
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        int selected = player.getInventory().selected;

        for (int i = 0; i < 36; i++) {
            if (i == selected) continue;
            if (player.getInventory().getItem(i).isEmpty()) {
                player.getInventory().setItem(i, saved);
                return;
            }
        }
        player.drop(saved, false);
    }

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || e.getServer().isDedicatedServer()) return;

        ServerLevel level = e.getServer().overworld();
        CampSavedData data = CampSavedData.get(level);
        if (!data.hasCamp()) return;

        long now = level.getGameTime();
        if (now % 20L != 0L) return;

        handleVisitor(level, data, now);
        handleJobs(level, data);

        if (now >= data.getNextRaid()) {
            spawnSmallRaid(level, data, now);
        }
    }

    private static void handleVisitor(ServerLevel level, CampSavedData data, long now) {
        if (now < data.getNextVisitor()) return;

        BlockPos camp = data.getCampPos();
        int accepted = CampActions.countAccepted(level, camp);
        int pending = CampActions.countPending(level, camp);

        if (accepted >= data.residentCapacity() || pending > 0) {
            data.setNextVisitor(now + 1200L);
            return;
        }

        double angle = R.nextDouble() * Math.PI * 2.0;
        double distance = 16.0 + R.nextDouble() * 8.0;
        int x = camp.getX() + (int)(Math.cos(angle) * distance);
        int z = camp.getZ() + (int)(Math.sin(angle) * distance);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

        SettlerEntity settler = ModEntities.SETTLER.get().create(level);
        if (settler == null) {
            data.setNextVisitor(now + 1200L);
            return;
        }

        settler.moveTo(x + 0.5D, y, z + 0.5D, R.nextFloat() * 360F, 0F);
        settler.setVariant(R.nextInt(3));
        settler.setAccepted(false);
        settler.setJob("idle");
        level.addFreshEntity(settler);

        for (ServerPlayer p : level.players()) {
            p.sendSystemMessage(Component.literal("§eК лагерю пришёл путник. Поговори с ним и реши, впустить ли его."));
        }
        data.setNextVisitor(now + 3600L);
    }

    private static void handleJobs(ServerLevel level, CampSavedData data) {
        BlockPos camp = data.getCampPos();
        List<SettlerEntity> settlers = level.getEntitiesOfClass(
                SettlerEntity.class,
                new AABB(camp).inflate(80),
                SettlerEntity::isAccepted
        );

        for (SettlerEntity settler : settlers) {
            String job = settler.getJob();
            if ("guard".equals(job)) {
                List<Monster> hostiles = level.getEntitiesOfClass(
                        Monster.class,
                        settler.getBoundingBox().inflate(12),
                        Monster::isAlive
                );
                hostiles.stream()
                        .min(Comparator.comparingDouble(settler::distanceToSqr))
                        .ifPresentOrElse(settler::setTarget, () -> walkTo(settler, camp, 0.85D));
                continue;
            }

            BlockPos target = switch (job) {
                case "farmer" -> data.nearestBuilding(BuildingManager.FARM, settler.blockPosition());
                case "woodcutter" -> data.nearestBuilding(BuildingManager.LUMBER, settler.blockPosition());
                case "builder" -> camp;
                default -> null;
            };

            if (target != null) walkTo(settler, target, 0.75D);
        }
    }

    private static void walkTo(SettlerEntity settler, BlockPos target, double speed) {
        if (settler.blockPosition().distSqr(target) > 12.0D) {
            settler.getNavigation().moveTo(target.getX() + 0.5D, target.getY() + 1.0D, target.getZ() + 0.5D, speed);
        }
    }

    private static void spawnSmallRaid(ServerLevel level, CampSavedData data, long now) {
        BlockPos camp = data.getCampPos();
        int residents = CampActions.countAccepted(level, camp);
        int attackers = Math.max(2, 2 + residents / 2);

        ServerPlayer target = level.players().stream().findFirst().orElse(null);
        if (target == null) {
            data.setNextRaid(now + 1200L);
            return;
        }

        for (int i = 0; i < attackers; i++) {
            double angle = R.nextDouble() * Math.PI * 2.0;
            int x = camp.getX() + (int)(Math.cos(angle) * 26);
            int z = camp.getZ() + (int)(Math.sin(angle) * 26);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            Zombie zombie = EntityType.ZOMBIE.create(level);
            if (zombie != null) {
                zombie.moveTo(x + 0.5D, y, z + 0.5D, 0F, 0F);
                zombie.setTarget(target);
                level.addFreshEntity(zombie);
            }
        }

        for (ServerPlayer p : level.players()) {
            p.sendSystemMessage(Component.literal("§cНа поселение напали! Стражи вступят в бой автоматически."));
        }
        data.setNextRaid(now + 18000L);
    }
}
