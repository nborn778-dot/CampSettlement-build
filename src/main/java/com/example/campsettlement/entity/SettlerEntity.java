package com.example.campsettlement.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkHooks;

public class SettlerEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> ACCEPTED =
            SynchedEntityData.defineId(SettlerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(SettlerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> JOB =
            SynchedEntityData.defineId(SettlerEntity.class, EntityDataSerializers.STRING);

    public SettlerEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ACCEPTED, false);
        entityData.define(VARIANT, 0);
        entityData.define(JOB, "idle");
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.05D, true));
        goalSelector.addGoal(5, new RandomStrollGoal(this, 0.75D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) { return false; }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (level().isClientSide) {
            int id = getId();
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> com.example.campsettlement.client.CampClient.openSettlerScreen(id));
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    public boolean isAccepted() { return entityData.get(ACCEPTED); }
    public void setAccepted(boolean accepted) {
        entityData.set(ACCEPTED, accepted);
        refreshName();
    }

    public int getVariant() { return entityData.get(VARIANT); }
    public void setVariant(int variant) { entityData.set(VARIANT, Math.max(0, variant)); }

    public String getJob() { return entityData.get(JOB); }
    public void setJob(String job) {
        entityData.set(JOB, job == null ? "idle" : job);
        refreshName();
    }

    public void refreshName() {
        if (!isAccepted()) {
            setCustomName(Component.literal("Путник"));
            setCustomNameVisible(true);
            return;
        }
        String title = switch (getJob()) {
            case "farmer" -> "Фермер";
            case "builder" -> "Строитель";
            case "guard" -> "Страж";
            case "woodcutter" -> "Лесоруб";
            default -> "Поселенец";
        };
        setCustomName(Component.literal(title));
        setCustomNameVisible(true);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Accepted", isAccepted());
        tag.putInt("Variant", getVariant());
        tag.putString("Job", getJob());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setAccepted(tag.getBoolean("Accepted"));
        setVariant(tag.getInt("Variant"));
        setJob(tag.contains("Job") ? tag.getString("Job") : "idle");
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
