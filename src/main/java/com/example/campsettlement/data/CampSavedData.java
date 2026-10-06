package com.example.campsettlement.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;

public class CampSavedData extends SavedData {
    private static final String NAME = "campsettlement_data";

    public record BuildingEntry(String type, BlockPos pos) {}

    private boolean camp;
    private int x, y, z;
    private long nextVisitor;
    private long nextRaid;
    private final List<BuildingEntry> buildings = new ArrayList<>();

    public static CampSavedData load(CompoundTag t) {
        CampSavedData d = new CampSavedData();
        d.camp = t.getBoolean("Camp");
        d.x = t.getInt("X"); d.y = t.getInt("Y"); d.z = t.getInt("Z");
        d.nextVisitor = t.getLong("NextVisitor");
        d.nextRaid = t.getLong("NextRaid");

        ListTag list = t.getList("Buildings", Tag.TAG_COMPOUND);
        for (Tag a : list) {
            CompoundTag q = (CompoundTag) a;
            d.buildings.add(new BuildingEntry(
                    q.getString("Type"),
                    new BlockPos(q.getInt("X"), q.getInt("Y"), q.getInt("Z"))
            ));
        }
        return d;
    }

    @Override
    public CompoundTag save(CompoundTag t) {
        t.putBoolean("Camp", camp);
        t.putInt("X", x); t.putInt("Y", y); t.putInt("Z", z);
        t.putLong("NextVisitor", nextVisitor);
        t.putLong("NextRaid", nextRaid);

        ListTag list = new ListTag();
        for (BuildingEntry e : buildings) {
            CompoundTag q = new CompoundTag();
            q.putString("Type", e.type());
            q.putInt("X", e.pos().getX()); q.putInt("Y", e.pos().getY()); q.putInt("Z", e.pos().getZ());
            list.add(q);
        }
        t.put("Buildings", list);
        return t;
    }

    public static CampSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(CampSavedData::load, CampSavedData::new, NAME);
    }

    public void createCamp(BlockPos p, long gameTime) {
        camp = true;
        x = p.getX(); y = p.getY(); z = p.getZ();
        buildings.clear();
        nextVisitor = gameTime + 600L;
        nextRaid = gameTime + 18000L;
        setDirty();
    }

    public void removeCamp() {
        camp = false;
        buildings.clear();
        setDirty();
    }

    public boolean hasCamp() { return camp; }
    public BlockPos getCampPos() { return new BlockPos(x, y, z); }

    public long getNextVisitor() { return nextVisitor; }
    public void setNextVisitor(long v) { nextVisitor = v; setDirty(); }

    public long getNextRaid() { return nextRaid; }
    public void setNextRaid(long v) { nextRaid = v; setDirty(); }

    public List<BuildingEntry> getBuildings() { return List.copyOf(buildings); }

    public void addBuilding(String type, BlockPos pos) {
        buildings.add(new BuildingEntry(type, pos.immutable()));
        setDirty();
    }

    public int countBuildings(String type) {
        int n = 0;
        for (BuildingEntry e : buildings) if (e.type().equals(type)) n++;
        return n;
    }

    public BlockPos nearestBuilding(String type, BlockPos from) {
        BlockPos best = null;
        double dist = Double.MAX_VALUE;
        for (BuildingEntry e : buildings) {
            if (!e.type().equals(type)) continue;
            double d = e.pos().distSqr(from);
            if (d < dist) { dist = d; best = e.pos(); }
        }
        return best;
    }

    public int residentCapacity() {
        return 2 + countBuildings("tent") * 2;
    }
}
