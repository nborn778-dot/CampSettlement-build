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
    private boolean camp;
    private int x, y, z;
    private int workers, guards;
    private int food = 20, wood = 10, stone = 10;
    private long nextRaid;
    private long nextProduction;
    private final List<BlockPos> outposts = new ArrayList<>();

    public static CampSavedData load(CompoundTag t) {
        CampSavedData d = new CampSavedData();
        d.camp=t.getBoolean("Camp");
        d.x=t.getInt("X"); d.y=t.getInt("Y"); d.z=t.getInt("Z");
        d.workers=t.getInt("Workers"); d.guards=t.getInt("Guards");
        d.food=t.contains("Food") ? t.getInt("Food") : 20;
        d.wood=t.contains("Wood") ? t.getInt("Wood") : 10;
        d.stone=t.contains("Stone") ? t.getInt("Stone") : 10;
        d.nextRaid=t.getLong("NextRaid");
        d.nextProduction=t.getLong("NextProduction");
        ListTag list=t.getList("Outposts", Tag.TAG_COMPOUND);
        for(Tag a:list){CompoundTag p=(CompoundTag)a;d.outposts.add(new BlockPos(p.getInt("X"),p.getInt("Y"),p.getInt("Z")));}
        return d;
    }

    @Override public CompoundTag save(CompoundTag t){
        t.putBoolean("Camp",camp);t.putInt("X",x);t.putInt("Y",y);t.putInt("Z",z);
        t.putInt("Workers",workers);t.putInt("Guards",guards);
        t.putInt("Food",food);t.putInt("Wood",wood);t.putInt("Stone",stone);
        t.putLong("NextRaid",nextRaid);t.putLong("NextProduction",nextProduction);
        ListTag list=new ListTag();
        for(BlockPos p:outposts){CompoundTag q=new CompoundTag();q.putInt("X",p.getX());q.putInt("Y",p.getY());q.putInt("Z",p.getZ());list.add(q);}t.put("Outposts",list);
        return t;
    }

    public static CampSavedData get(ServerLevel l){
        return l.getDataStorage().computeIfAbsent(CampSavedData::load,CampSavedData::new,NAME);
    }
    public void createCamp(BlockPos p,long gameTime){camp=true;x=p.getX();y=p.getY();z=p.getZ();workers=0;guards=0;food=20;wood=10;stone=10;nextRaid=gameTime+12000L;nextProduction=gameTime+600L;setDirty();}
    public void removeCamp(){camp=false;workers=guards=0;outposts.clear();setDirty();}
    public boolean hasCamp(){return camp;}
    public BlockPos getCampPos(){return new BlockPos(x,y,z);}
    public int getWorkers(){return workers;} public int getGuards(){return guards;}
    public void addWorker(){addWorker(1);} public void addWorker(int v){workers=Math.max(0,workers+v);setDirty();} public void addGuard(){addGuard(1);} public void addGuard(int v){guards=Math.max(0,guards+v);setDirty();}
    public long getNextRaid(){return nextRaid;} public void setNextRaid(long v){nextRaid=v;setDirty();}
    public long getNextProduction(){return nextProduction;} public void setNextProduction(long v){nextProduction=v;setDirty();}
    public int getFood(){return food;} public int getWood(){return wood;} public int getStone(){return stone;}
    public void addFood(int v){food=Math.max(0,Math.min(9999,food+v));setDirty();}
    public void addWood(int v){wood=Math.max(0,Math.min(9999,wood+v));setDirty();}
    public void addStone(int v){stone=Math.max(0,Math.min(9999,stone+v));setDirty();}
    public boolean spend(int f,int w,int s){if(food<f||wood<w||stone<s)return false;food-=f;wood-=w;stone-=s;setDirty();return true;}
    public List<BlockPos> getOutposts(){return outposts;}
    public void addOutpost(BlockPos p){if(!outposts.contains(p)){outposts.add(p);setDirty();}}
}
